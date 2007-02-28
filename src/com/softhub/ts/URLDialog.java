
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
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import java.awt.event.*;

public class URLDialog extends JDialog {

	private Vector listeners = new Vector();
    private JPanel controlPane = new JPanel();
    private FlowLayout flowLayout1 = new FlowLayout();
    private JButton cancelButton = new JButton();
    private JButton okButton = new JButton();
    private JTextField textField = new JTextField();
    private JLabel label = new JLabel();
    private FlowLayout flowLayout2 = new FlowLayout();
    private JPanel editPane = new JPanel();
    private JPanel messagePane = new JPanel();
    private JLabel messageLabel = new JLabel();
    private BorderLayout borderLayout1 = new BorderLayout();


    public URLDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public URLDialog() {
		this(null, "", false);
    }

    private void jbInit() throws Exception {
        controlPane.setPreferredSize(new Dimension(10, 40));
        controlPane.setLayout(flowLayout1);
        cancelButton.setPreferredSize(new Dimension(75, 27));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButtonAction(e);
            }
        });
        okButton.setPreferredSize(new Dimension(75, 27));
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButtonAction(e);
            }
        });
        textField.setPreferredSize(new Dimension(280, 21));
        label.setText("URL:");
        flowLayout2.setVgap(15);
        editPane.setLayout(flowLayout2);
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                keyPTypedAction(e);
            }
        });
        messageLabel.setPreferredSize(new Dimension(100, 17));
        messagePane.setLayout(borderLayout1);
        this.getContentPane().add(controlPane, BorderLayout.SOUTH);
        controlPane.add(okButton, null);
        controlPane.add(cancelButton, null);
        this.getContentPane().add(editPane, BorderLayout.CENTER);
        editPane.add(label, null);
        editPane.add(textField, null);
        this.getContentPane().add(messagePane, BorderLayout.NORTH);
        messagePane.add(messageLabel, BorderLayout.CENTER);
    }

	public void addActionListener(ActionListener listener) {
		listeners.addElement(listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.removeElement(listener);
	}

	protected void fireActionEvent(ActionEvent evt) {
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			ActionListener listener = (ActionListener) e.nextElement();
			listener.actionPerformed(evt);
		}
	}

	protected void fireAction(String cmd) {
		fireActionEvent(
			new ActionEvent(this, ActionEvent.ACTION_PERFORMED, cmd)
		);
	}

	protected void onCancel() {
		setVisible(false);
	}

	protected void onError(String msg) {
		messageLabel.setText(msg);
	}

	protected void onOK() {
		String text = textField.getText();
		try {
			URL url = new URL(text);
		    setVisible(false);
			fireAction(
				"(" + text + ") statusdict /jobserver get exec"
			);
		} catch (MalformedURLException ex) {
			onError("Invalid URL: " + text);
		}
	}

	private void cancelButtonAction(ActionEvent evt) {
		onCancel();
	}

	private void okButtonAction(ActionEvent evt) {
		onOK();
	}

    void keyPTypedAction(KeyEvent evt) {
		if (evt.getKeyChar() == '\n') {
			onOK();
		}
		messageLabel.setText("");
    }

}
