
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
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import com.softhub.ts.event.ViewEvent;
import com.softhub.ts.event.ViewEventListener;
import com.softhub.ts.event.TrayControlEvent;
import com.softhub.ts.event.TrayControlListener;
import java.awt.event.*;

public class TrayControl extends JPanel
	implements ViewEventListener
{
	private final static float scaleFactors[] = {
		2.50f, 2.00f, 1.75f, 1.50f, 1.25f, 1.00f, 0.90f, 0.75f, 0.50f
	};

	private Vector listeners = new Vector();
    private JComboBox comboBox = new JComboBox();
    private JPanel leftPane = new JPanel();
    private BorderLayout trayControlLayout = new BorderLayout(3, 0);
    private JPanel rightPane = new JPanel();
	private boolean actionLock;

    public TrayControl() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(trayControlLayout);
        comboBox.setFont(new java.awt.Font("SansSerif", 0, 10));
        comboBox.setMinimumSize(new Dimension(140, 16));
        comboBox.setPreferredSize(new Dimension(160, 16));
        comboBox.setMaximumRowCount(12);
        comboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comboBoxAction(e);
            }
        });
        this.setMinimumSize(new Dimension(80, 16));
        this.setPreferredSize(new Dimension(220, 16));
        leftPane.setBorder(BorderFactory.createEtchedBorder());
        leftPane.setPreferredSize(new Dimension(24, 16));
        rightPane.setBorder(BorderFactory.createEtchedBorder());
        rightPane.setPreferredSize(new Dimension(24, 16));
        this.add(leftPane, BorderLayout.WEST);
        this.add(comboBox, BorderLayout.CENTER);
        this.add(rightPane, BorderLayout.EAST);
		initScaleFactors();
    }

	private void initScaleFactors() {
		int i, n = scaleFactors.length;
		for (i = 0; i < n; i++) {
			addScaleFactor(scaleFactors[i]);
		}
//		comboBox.addItem("---------");
//		comboBox.addItem("Fit Window");
//		comboBox.addItem("Actual Size");
//		comboBox.addItem("Fit Width");
	}

	private void addScaleFactor(float scale) {
		String text = scaleFactorToString(scale);
		comboBox.addItem(text);
	}

	private static String scaleFactorToString(float scale) {
		return Math.round(scale * 100) + "%";
	}

	public void addTrayControlListener(TrayControlListener listener) {
		listeners.addElement(listener);
	}

	public void removeTrayControlListener(TrayControlListener listener) {
		listeners.removeElement(listener);
	}

	protected void fireTrayControlEvent(TrayControlEvent evt) {
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			TrayControlListener listener = (TrayControlListener) e.nextElement();
			listener.trayChange(evt);
		}
	}

	protected void fireTrayScaleEvent(int index) {
		float scale = scaleFactors[index];
		fireTrayControlEvent(new TrayControlEvent(this, scale));
	}

	public void viewChanged(ViewEvent evt) {
		try {
			actionLock = true;
			switch (evt.getEventType()) {
			case ViewEvent.PAGE_RESIZE:
				viewScaleChange((Viewable) evt.getSource());
				break;
			}
		} finally {
		    actionLock = false;
		}
	}

	private void viewScaleChange(Viewable page) {
		float scale = page.getScale();
		int i, n = scaleFactors.length, sel = -1;
		for (i = 0; i < n && sel < 0; i++) {
		    if (Math.abs(scale - scaleFactors[i]) < 1e-3) {
				sel = i;
		    }
		}
		if (sel >= 0) {
		    comboBox.setSelectedIndex(sel);
		} else {
			int count = comboBox.getItemCount();
			if (count > scaleFactors.length) {
			    comboBox.removeItemAt(count-1);
		    }
		    addScaleFactor(scale);
		}
	}

    private void comboBoxAction(ActionEvent evt) {
		if (!actionLock) {
			JComboBox source = (JComboBox) evt.getSource();
			int index = source.getSelectedIndex();
			if (index >= 0) {
				fireTrayScaleEvent(index);
			}
		}
    }

}
