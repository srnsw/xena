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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Demonstrates the JGoodies Looks <i>narrowMargin</i> option.
 * Therefore it contains button rows that use different combinations
 * of layout managers and narrow hints.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
final class NarrowTab {

    /**
     * Builds the panel.
     */
    JComponent build() {
        FormLayout layout = new FormLayout(
                "left:pref, 0:grow",
                "pref, 14dlu, pref, 14dlu, pref, 0:grow");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        builder.add(buildButtonBoxNoNarrow(),  cc.xy (1, 1));
        builder.add(buildButtonFormNoNarrow(), cc.xy (1, 3));
        builder.add(buildButtonGridNoNarrow(), cc.xy (1, 5));

        return builder.getPanel();
    }

    // Button FlowLayout Panels *********************************************

    private Component buildButtonBoxNoNarrow() {
        return buildButtonBox(createButtons());
    }

    private Component buildButtonBox(JButton[] buttons) {
        Box box = Box.createHorizontalBox();
        for (int i = 0; i < buttons.length; i++) {
            box.add(buttons[i]);
            box.add(Box.createHorizontalStrut(6));
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(box, BorderLayout.CENTER);
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                new TitledBorder("Raw Button Widths (BoxLayout)"),
                new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    // Button DesignGrids ***************************************************

    private Component buildButtonFormNoNarrow() {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.addButton(createButtons());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(builder.getPanel(), BorderLayout.CENTER);
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                new TitledBorder("Adjusted Button Widths (FormLayout)"),
                new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }


    // Button Grids *********************************************************

    private Component buildButtonGridNoNarrow() {
        return buildButtonGrid(createButtons());
    }

    private Component buildButtonGrid(JButton[] buttons) {
        JPanel grid = new JPanel(new GridLayout(1, 4, 6, 0));
        grid.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            grid.add(buttons[i]);
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(grid, BorderLayout.CENTER);
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                new TitledBorder("Equalized Button Widths (GridLayout)"),
                new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    // Helper Code **********************************************************

    /**
     * Creates and returns an array of buttons that have no narrow hints set.
     */
    private JButton[] createButtons() {
        return new JButton[] {
            new JButton("Add\u2026"),
            new JButton("Remove"),
            new JButton("Up"),
            new JButton("Down"),
            new JButton("Copy to Clipboard")};
    }


}