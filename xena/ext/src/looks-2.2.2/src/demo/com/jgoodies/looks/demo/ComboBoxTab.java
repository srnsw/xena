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
import java.awt.Color;
import java.awt.Component;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.TableColumn;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Demonstrates JComboBoxes in editable and non-editable mode with
 * different renderer margins, renderer types, and different list content.
 * This tab makes it easier to check the alignment of font baselines,
 * and proper preferred size computations.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
final class ComboBoxTab {

    private static final String TEST_STR = "A test string";

    private ListCellRenderer narrowMarginRenderer;
    private ListCellRenderer wideMarginRenderer;
    private ListCellRenderer noMarginRenderer;
    private ListCellRenderer panelRenderer;


    // Initialization *********************************************************

    private void initRenderers() {
        narrowMarginRenderer = new CustomMarginRenderer(new EmptyBorder(1, 1, 1, 1));
        wideMarginRenderer = new CustomMarginRenderer(new EmptyBorder(1, 5, 1, 5));
        noMarginRenderer = new CustomMarginRenderer(new EmptyBorder(0, 0, 0, 0));
        panelRenderer = new PanelRenderer();
    }

    // ************************************************************************

    /**
     * Builds a panel using <code>FormLayout</code> that consists
     * of rows of different Swing components all centered vertically.
     *
     * @return the built panel
     */
    JComponent build() {
        initRenderers();

        FormLayout layout = new FormLayout(
                "0:grow, pref, 0:grow",
                "pref, 9dlu, pref, 2dlu, pref, 21dlu, pref, 2dlu, 50dlu");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);
        CellConstraints cc = new CellConstraints();

        builder.add(buildHelpPanel(), cc.xy(2, 1));
        builder.addSeparator("Form",  cc.xy(2, 3));
        builder.add(buildTestPanel(), cc.xy(2, 5, "center, fill"));
        builder.addTitle("Table",     cc.xy(2, 7));
        builder.add(buildTable(),     cc.xy(2, 9));

        return builder.getPanel();
    }


    private JComponent buildHelpPanel() {
        return new JLabel("Font baselines shall be aligned and the visible value shall not be cropped.");
    }

    private JComponent buildTestPanel() {
        FormLayout layout = new FormLayout(
                "right:pref, 3dlu, left:pref, 4dlu, left:pref");

        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setOpaque(false);

        // Header
        builder.append("Renderer");
        builder.append(createCenteredLabel("Editable"));
        builder.append(createCenteredLabel("Non-Editable"));

        builder.append("Default");
        builder.append(createComboBox(TEST_STR, true,  null));
        builder.append(createComboBox(TEST_STR, false, null));

        builder.append("Panel");
        builder.append(createComboBox(TEST_STR, true,  panelRenderer));
        builder.append(createComboBox(TEST_STR, false, panelRenderer));

        builder.append("Narrow Margin");
        builder.append(createComboBox(TEST_STR, true,  narrowMarginRenderer));
        builder.append(createComboBox(TEST_STR, false, narrowMarginRenderer));

        builder.append("No Margin");
        builder.append(createComboBox(TEST_STR, true,  noMarginRenderer));
        builder.append(createComboBox(TEST_STR, false, noMarginRenderer));

        builder.append("Wide Margin");
        builder.append(createComboBox(TEST_STR, true,  wideMarginRenderer));
        builder.append(createComboBox(TEST_STR, false, wideMarginRenderer));

        return builder.getPanel();
    }


    private JComponent buildTable() {
        String[] columnNames = {"Text", "Editable", "Non-Editable"};

        Object[][] data = {
                {TEST_STR, TEST_STR, TEST_STR },
                {TEST_STR, TEST_STR, TEST_STR },
                {TEST_STR, TEST_STR, TEST_STR},
                {TEST_STR, TEST_STR, TEST_STR},
                {TEST_STR, TEST_STR, TEST_STR}};
        JTable table = new JTable(data, columnNames);

        TableColumn editableColumn = table.getColumnModel().getColumn(1);
        JComboBox editableCombo = createComboBox(TEST_STR, true, null);
        editableColumn.setCellEditor(new DefaultCellEditor(editableCombo));

        TableColumn nonEditableColumn = table.getColumnModel().getColumn(2);
        JComboBox nonEditableCombo = createComboBox(TEST_STR, false, null);
        nonEditableColumn.setCellEditor(new DefaultCellEditor(nonEditableCombo));

        return new JScrollPane(table);
    }


    // Helper Code **********************************************************

    private JLabel createCenteredLabel(String text) {
        return new JLabel(text, JLabel.CENTER);
    }

    private JComboBox createComboBox(String selectedText, boolean editable, ListCellRenderer renderer) {
        String[] values = new String[] {selectedText, "1", "2", "3", "4", "5", "Two", "Three", "Four", "Five", "Six", "Seven"};
        JComboBox combo = new JComboBox(values);
        combo.setEditable(editable);
        if (renderer != null) {
            combo.setRenderer(renderer);
        }
        return combo;
    }


    // Renderers **************************************************************

    private static final class CustomMarginRenderer extends BasicComboBoxRenderer {


        private CustomMarginRenderer(Border border) {
            setBorder(border);
        }
    }


    private static final class PanelRenderer extends JPanel implements ListCellRenderer {

        private static Border noFocusBorder;

        private final JLabel label;

        private PanelRenderer() {
            super(new BorderLayout());
            if (noFocusBorder == null) {
                noFocusBorder = new EmptyBorder(1, 1, 1, 1);
            }
            setOpaque(true);
            setBorder(noFocusBorder);
            label = new JLabel();
            add(label, BorderLayout.CENTER);
        }

        public Component getListCellRendererComponent(
                JList list,
            Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
            {
                setComponentOrientation(list.getComponentOrientation());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            if (value instanceof Icon) {
                label.setIcon((Icon)value);
                label.setText("");
            } else {
                label.setIcon(null);
                label.setText((value == null) ? "" : value.toString());
            }

            label.setEnabled(list.isEnabled());
            label.setFont(list.getFont());
            setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

            return this;
         }


        public void setForeground(Color c) {
            if (label != null) {
                label.setForeground(c);
            }
        }

    }


}