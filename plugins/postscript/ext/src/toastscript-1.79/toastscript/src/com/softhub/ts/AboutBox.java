
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutBox extends JDialog implements ActionListener {

    private JPanel panel1 = new JPanel();
    private JPanel panel2 = new JPanel();
    private JPanel insetsPanel1 = new JPanel();
    private JPanel insetsPanel2 = new JPanel();
    private JPanel insetsPanel3 = new JPanel();
    private JButton okButton = new JButton();
    private JLabel imageLabel = new JLabel();
    private JLabel label1 = new JLabel();
    private JLabel label2 = new JLabel();
    private JLabel label3 = new JLabel();
    private JLabel label4 = new JLabel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private FlowLayout flowLayout1 = new FlowLayout();
    private GridLayout gridLayout1 = new GridLayout();
    private String product = "ToastScript";
    private String version = "1.0";
    private String copyright = "Copyright (c) by Christian Lehner";
    private String comments = "Java PostScript Interpreter";
    private JButton printButton = new JButton();
	private PostScriptPane postScriptPane;

    public AboutBox(Frame parent, PostScriptPane postScriptPane) {
        super(parent);
		this.postScriptPane = postScriptPane;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        pack();
    }

    private void jbInit() throws Exception  {
        //imageLabel.setIcon(new ImageIcon(ViewFrame_AboutBox.class.getResource("[Your Image]")));
        this.setTitle("About");
		version = postScriptPane.getVersion();
        setResizable(false);
        panel1.setLayout(borderLayout1);
        panel2.setLayout(borderLayout2);
        insetsPanel1.setLayout(flowLayout1);
        insetsPanel2.setLayout(flowLayout1);
        insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridLayout1.setRows(4);
        gridLayout1.setColumns(1);
        label1.setText(product);
        label2.setText(version);
        label3.setText(copyright);
        label4.setText(comments);
        insetsPanel3.setLayout(gridLayout1);
        insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
        okButton.setText("Ok");
        okButton.addActionListener(this);
        printButton.setText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printButtonAction(e);
            }
        });
        insetsPanel2.add(imageLabel, null);
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel3.add(label1, null);
        insetsPanel3.add(label2, null);
        insetsPanel3.add(label3, null);
        insetsPanel3.add(label4, null);
        panel2.add(insetsPanel2, BorderLayout.WEST);
        this.getContentPane().add(panel1, null);
        insetsPanel1.add(okButton, null);
        insetsPanel1.add(printButton, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
    }

    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    void cancel() {
        dispose();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == okButton) {
            cancel();
        }
    }

    void printButtonAction(ActionEvent evt) {
        if (evt.getSource() == printButton) {
			postScriptPane.exec("statusdict /about get exec");
            cancel();
        }
    }

}
