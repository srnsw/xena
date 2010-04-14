/*
 * Copyright (c) 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.looks.demo;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Contains a bunch of buttons to open a bunch of standard dialogs.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
final class DialogsTab {

    private Container parent;

    private JButton informationButton;
    private JButton warningButton;
    private JButton questionButton;
    private JButton errorButton;
    private JButton chooseFileNativeButton;
    private JButton chooseFileSwingButton;


    /**
     * Creates and configures the UI components.
     */
    private void initComponents() {
        informationButton = new JButton("Information");
        informationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                    getParentFrame(),
                    "We just wanted to let you know that you have pressed\n" +
                    "the Information button to open this sample message dialog.\n\n",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        warningButton = new JButton("Warning");
        warningButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                    getParentFrame(),
                    "We just wanted to let you know that you have pressed\n" +
                    "the Warning button to open this sample message dialog.\n\n",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        questionButton = new JButton("Question");
        questionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showConfirmDialog(
                    getParentFrame(),
                    "We just wanted to let you know that you have pressed\n" +
                    "the Question button to open this sample question dialog.\n\n" +
                    "Are you satisfied with the dialog's appearance?\n\n",
                    "Question",
                    JOptionPane.YES_NO_OPTION
                    );
            }
        });
        errorButton = new JButton("Error");
        errorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                    getParentFrame(),
                    "We just wanted to let you know that you have pressed\n" +
                    "the Error button to open this error message dialog.\n\n" +
                    "Just go ahead and proceed.\n\n",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        chooseFileNativeButton = new JButton("Open\u2026");
        chooseFileNativeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Dialog dialog = new FileDialog(
                        getParentFrame(), "Open File (Native)");
                dialog.setResizable(true);
                dialog.setVisible(true);
            }
        });
        chooseFileSwingButton = new JButton("Open\u2026");
        chooseFileSwingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new JFileChooser("Open File (Swing)").showOpenDialog(
                    getParentFrame());
            }
        });
    }

	/**
	 * Builds and returns the panel.
	 */
    JComponent build(Container aParent) {
        this.parent = aParent;
        initComponents();

        FormLayout layout = new FormLayout(
                "0:grow, left:pref, 0:grow",
                "0:grow, pref, 4dlu, pref, 14dlu, pref, 4dlu, pref, 14dlu, pref, 4dlu, pref, 0:grow");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addLabel("Press a button to open a message dialog.", cc.xy(2,  2));
        builder.add(buildButtonBar(),                                cc.xy(2,  4));
        builder.addLabel("This opens the native file chooser.",      cc.xy(2,  6));
        builder.add(chooseFileNativeButton,                          cc.xy(2,  8));
        builder.addLabel("This opens the Swing file chooser.",       cc.xy(2, 10));
        builder.add(chooseFileSwingButton,                           cc.xy(2, 12));

        return builder.getPanel();
    }

    /**
     * Builds and returns the message dialog button bar.
     *
     * @return the message dialog button bar
     */
    private JPanel buildButtonBar() {
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.setOpaque(false);
        builder.addGriddedButtons(new JButton[]{
            informationButton, warningButton, questionButton, errorButton});
        return builder.getPanel();
    }


    // Helper Code ************************************************************

    JFrame getParentFrame() {
        return (JFrame) (SwingUtilities.getWindowAncestor(parent));
    }

}