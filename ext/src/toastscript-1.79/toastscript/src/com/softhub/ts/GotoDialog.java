
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.softhub.ts.event.NavigationEvent;
import com.softhub.ts.event.NavigationListener;
import com.softhub.ts.event.ViewEvent;
import com.softhub.ts.event.ViewEventListener;

public class GotoDialog extends JDialog implements ViewEventListener {

    private JPanel controlPane = new JPanel();
    private FlowLayout flowLayout1 = new FlowLayout();
    private JButton okButton = new JButton();
    private JButton cancelButton = new JButton();
    private JTextField textField = new JTextField();
    private JLabel label = new JLabel();
    private FlowLayout flowLayout2 = new FlowLayout();
    private JPanel editPane = new JPanel();
	private Vector listeners = new Vector();

	public GotoDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
		    jbInit();
		    pack();
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

	public GotoDialog() {
	    this(null, "", false);
	}

	private void jbInit() throws Exception {
        editPane.setLayout(flowLayout2);
        flowLayout2.setVgap(20);
        label.setText("Page:");
        textField.setPreferredSize(new Dimension(40, 21));
		controlPane.setPreferredSize(new Dimension(180, 40));
        controlPane.setLayout(flowLayout1);
        okButton.setMaximumSize(new Dimension(75, 27));
        okButton.setMinimumSize(new Dimension(75, 27));
        okButton.setPreferredSize(new Dimension(75, 27));
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButtonAction(e);
            }
        });
        cancelButton.setMaximumSize(new Dimension(75, 27));
        cancelButton.setMinimumSize(new Dimension(75, 27));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButtonAction(e);
            }
        });
        this.setResizable(false);
        editPane.setPreferredSize(new Dimension(180, 60));
        this.getContentPane().add(controlPane, BorderLayout.SOUTH);
        controlPane.add(okButton, null);
        controlPane.add(cancelButton, null);
        this.getContentPane().add(editPane, BorderLayout.NORTH);
        editPane.add(label, null);
        editPane.add(textField, null);
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
		switch (evt.getEventType()) {
		case ViewEvent.PAGE_ADJUST:
		case ViewEvent.PAGE_CHANGE:
			Viewable page = (Viewable) evt.getSource();
			setPageNumber(page.getPageIndex() + 1);
			break;
		}
	}

	public void setPageNumber(int pageno) {
		textField.setText(String.valueOf(pageno));
	}

    private void cancelButtonAction(ActionEvent evt) {
    	setVisible(false);
    }

    private void okButtonAction(ActionEvent evt) {
		try {
			int index = Integer.valueOf(textField.getText()).intValue();
		    fireNavigationEvent(index - 1);
		} finally {
	    	setVisible(false);
		}
    }

}
