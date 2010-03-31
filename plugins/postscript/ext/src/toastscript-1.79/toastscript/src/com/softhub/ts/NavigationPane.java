
package com.softhub.ts;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.awt.*;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.*;
import java.awt.event.*;
import com.softhub.ts.event.NavigationEvent;
import com.softhub.ts.event.NavigationListener;
import com.softhub.ts.event.ViewEvent;
import com.softhub.ts.event.ViewEventListener;

public class NavigationPane extends JPanel
	implements ViewEventListener
{
    private BorderLayout navigationPaneLayout = new BorderLayout(3, 0);
    private BorderLayout centerPaneLayout = new BorderLayout();
    private JLabel label = new JLabel();
	private ImageIcon leftImage;
	private ImageIcon rightImage;
    private JComboBox comboBox = new JComboBox();
	private Vector listeners = new Vector();
    private JPanel centerPane = new JPanel();
    private JButton leftPane = new JButton();
    private JButton rightPane = new JButton();
	private boolean actionLock;

    public NavigationPane() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        label.setFont(new java.awt.Font("Dialog", 0, 10));
        label.setBorder(BorderFactory.createEtchedBorder());
        label.setMaximumSize(new Dimension(80, 16));
        label.setMinimumSize(new Dimension(40, 16));
        label.setPreferredSize(new Dimension(50, 16));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText("Page:");
        this.setLayout(navigationPaneLayout);
        this.setMinimumSize(new Dimension(76, 16));
        this.setPreferredSize(new Dimension(100, 16));
        comboBox.setFont(new java.awt.Font("SansSerif", 0, 10));
        comboBox.setMaximumSize(new Dimension(32767, 16));
        comboBox.setMinimumSize(new Dimension(100, 16));
        comboBox.setPreferredSize(new Dimension(155, 16));
        comboBox.setActionCommand("pageNumberChanged");
        comboBox.setMaximumRowCount(6);
        comboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comboBoxAction(e);
            }
        });
		centerPane.setLayout(centerPaneLayout);
        leftPane.setBorder(BorderFactory.createEtchedBorder());
        leftPane.setPreferredSize(new Dimension(16, 16));
        leftPane.setFocusPainted(false);
        leftPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                leftPaneMousePressed(e);
            }
        });
        rightPane.setBorder(BorderFactory.createEtchedBorder());
        rightPane.setPreferredSize(new Dimension(16, 16));
        rightPane.setFocusPainted(false);
        rightPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                rightPaneMousePressed(e);
            }
        });
        centerPane.add(label, BorderLayout.WEST);
        centerPane.add(comboBox, BorderLayout.CENTER);
		this.add(centerPane, BorderLayout.CENTER);
        this.add(leftPane, BorderLayout.WEST);
        this.add(rightPane, BorderLayout.EAST);
		leftImage = new ImageIcon(com.softhub.ts.ViewFrame.class.getResource("left.gif"));
		rightImage = new ImageIcon(com.softhub.ts.ViewFrame.class.getResource("right.gif"));
		leftPane.setIcon(leftImage);
        leftPane.setToolTipText("Previous");
		rightPane.setIcon(rightImage);
        rightPane.setToolTipText("Next");
    }

	public void addNavigationListener(NavigationListener listener) {
		listeners.addElement(listener);
	}

	public void removeNavigationListener(NavigationListener listener) {
		listeners.removeElement(listener);
	}

	protected void fireNavigationEvent(int pageIndex) {
		NavigationEvent evt = new NavigationEvent(this, pageIndex);
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			NavigationListener listener = (NavigationListener) e.nextElement();
			listener.pageChange(evt);
		}
	}

	public void viewChanged(ViewEvent evt) {
		try {
			actionLock = true;
			switch (evt.getEventType()) {
			case ViewEvent.PAGE_CHANGE:
			case ViewEvent.PAGE_ADJUST:
				pageChanged((Viewable) evt.getSource());
				break;
			}
		} finally {
		    actionLock = false;
		}
	}

    private void comboBoxAction(ActionEvent evt) {
		if (!actionLock) {
			JComboBox source = (JComboBox) evt.getSource();
			int pageIndex = source.getSelectedIndex();
			if (pageIndex >= 0) {
				fireNavigationEvent(pageIndex);
			}
		}
    }

	private void pageChanged(Viewable page) {
		int i, n = page.getPageCount();
		if (comboBox.getItemCount() != n) {
		    comboBox.removeAllItems();
			for (i = 1; i <= n; i++) {
				comboBox.addItem("" + i);
			}
		}
		int selIndex = page.getPageIndex();
		if (0 <= selIndex && selIndex < n) {
			comboBox.setSelectedIndex(selIndex);
		}
	}

	private void leftPaneMousePressed(MouseEvent evt) {
		int pageIndex = comboBox.getSelectedIndex();
		int pageCount = comboBox.getItemCount();
		if (0 < pageIndex && pageIndex < pageCount) {
		    fireNavigationEvent(pageIndex - 1);
		}
	}

	private void rightPaneMousePressed(MouseEvent evt) {
		int pageIndex = comboBox.getSelectedIndex();
		int pageCount = comboBox.getItemCount();
		if (0 <= pageIndex && pageIndex < pageCount-1) {
		    fireNavigationEvent(pageIndex + 1);
		}
	}

}
