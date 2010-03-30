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
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.PlasticInternalFrameUI;

/**
 * Demos the <code>JDesktopPane</code>.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
final class DesktopTab {

    private static final float SIZE_FACTOR = LookUtils.IS_LOW_RESOLUTION ? 1f : 1.175f;

    /**
     * Builds the panel.
     */
    JComponent build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(Borders.DIALOG_BORDER);
        panel.add(new JScrollPane(buildDesktopPane()));
        return panel;
    }

    private JComponent buildDesktopPane() {
        int gap      = (int) (10 * SIZE_FACTOR);
        int originX1 = 10;
        int extentX1 = (int) (193 * SIZE_FACTOR);
        int originX2 = originX1 + extentX1 + gap;
        int extentX2 = extentX1;
        int originX3 = originX2 + extentX2 + gap;
        int extentX3 = (int) (150 * SIZE_FACTOR);

        JDesktopPane desktop = new JDesktopPane();
        JInternalFrame frame;

        frame = new JInternalFrame("Navigation", true, true, true, true);
        frame.setContentPane(buildFrame1ContentPane());
        frame.setBounds(originX1, 10, extentX1, 320);
        desktop.add(frame);
        frame.setVisible(true);

        frame = new JInternalFrame("Properties", true, false, true, true);
        frame.setContentPane(buildFrame2ContentPane());
        frame.setBounds(originX2, 10, extentX2, 320);
        desktop.add(frame);
        frame.setVisible(true);

        JInternalFrame palette =
            new JInternalFrame("Palette1", true, true, true, true);
        palette.putClientProperty(
            PlasticInternalFrameUI.IS_PALETTE,
            Boolean.TRUE);
        palette.setContentPane(buildPaletteContentPane());
        palette.setBounds(originX3, 10, extentX3, 150);
        palette.setVisible(true);
        desktop.add(palette, JLayeredPane.PALETTE_LAYER);

        return desktop;
    }

    private JComponent buildFrame1ContentPane() {
        JScrollPane scrollPane = new JScrollPane(new JTree());
        scrollPane.setPreferredSize(new Dimension(100, 140));
        return scrollPane;
    }

    private JComponent buildFrame2ContentPane() {
        JScrollPane scrollPane = new JScrollPane(buildTable());
        scrollPane.setPreferredSize(new Dimension(100, 140));
        return scrollPane;
    }

    private JComponent buildPaletteContentPane() {
        JCheckBox check1 = new JCheckBox("be consistent", true);
        check1.setContentAreaFilled(false);
        JCheckBox check2 = new JCheckBox("use less ink", true);
        check2.setContentAreaFilled(false);
        final Border cardDialogBorder = new EmptyBorder(10, 6, 7, 6);
        Box box = Box.createVerticalBox();
        box.add(check1);
        box.add(Box.createVerticalStrut(6));
        box.add(check2);

        JPanel generalTab = new JPanel(new BorderLayout());
        generalTab.setOpaque(false);
        generalTab.add(box);
        generalTab.setBorder(cardDialogBorder);

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
        tabbedPane.add(generalTab, "General");
        tabbedPane.add(new JLabel("Test1"), "Filter");
        return tabbedPane;
    }

    /**
     * Builds and answers a sample table.
     */
    private JTable buildTable() {
        JTable table = new JTable(
                createSampleTableData(),
                new String[] { "Key", "Value" });

        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(95);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.setRowSelectionInterval(2, 2);
        return table;
    }

    /**
     * Creates and answers sample table data.
     */
    private String[][] createSampleTableData() {
        return new String[][] {
            {"Name",           "Karsten"    },
            {"Sex",            "Male"       },
            {"Date of Birth",  "5-Dec-1967" },
            {"Place of Birth", "Kiel"       },
            {"Profession",     "UI Designer"},
            {"Business",       "Freelancer" },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
            {"",               ""           },
        };
    }

}