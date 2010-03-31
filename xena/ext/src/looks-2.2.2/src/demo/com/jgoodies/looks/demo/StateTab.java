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

import java.awt.Component;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.text.JTextComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.looks.Options;

/**
 * Presents a larger set of Swing components in different states and
 * configurations.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
final class StateTab {

    /**
     * Builds and returns the states panel.
     */
    JComponent build() {
        FormLayout layout = new FormLayout(
                "right:max(50dlu;pref), 4dlu, pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        builder.append("Standard:",        buildButtonRow(true, true));
        builder.append("No Content:",      buildButtonRow(true, false));
        builder.append("No Border:",       buildButtonRow(false, true));
        builder.append("Radio Button:",    buildRadioButtonRow());
        builder.append("Check Box:",       buildCheckBoxRow());
        builder.append("Combo Box:",       buildComboBoxRow());
        builder.append("Text Field:",      buildTextRow(JTextField.class, false));
        builder.append("Formatted Field:", buildTextRow(JFormattedTextField.class, false));
        builder.append("Text Area:",       buildTextRow(JTextArea.class, true));
        builder.append("Editor Pane:",     buildTextRow(JEditorPane.class, true));
        builder.append("Password:",        buildTextRow(JPasswordField.class, false));
        builder.append("Spinner:",         buildSpinnerRow());

        return builder.getPanel();
    }


    // Button Rows **********************************************************

    private JComponent buildButtonRow(
        boolean borderPainted,
        boolean contentAreaFilled) {
        JButton button = new JButton("Standard");
        button.setDefaultCapable(true);

        return buildButtonRow(
            new AbstractButton[] {
                button,
                new JToggleButton("Selected"),
                new JButton("Disabled"),
                new JToggleButton("Selected")},
            borderPainted,
            contentAreaFilled);
    }

    private JComponent buildCheckBoxRow() {
        return buildButtonRow(
            new AbstractButton[] {
                new JCheckBox("Deselected"),
                new JCheckBox("Selected"),
                new JCheckBox("Disabled"),
                new JCheckBox("Selected")},
            false,
            false);
    }

    private JComponent buildRadioButtonRow() {
        return buildButtonRow(
            new AbstractButton[] {
                new JRadioButton("Deselected"),
                new JRadioButton("Selected"),
                new JRadioButton("Disabled"),
                new JRadioButton("Selected")},
            false,
            false);
    }

    private JComponent buildButtonRow(
        AbstractButton[] buttons,
        boolean borderPainted,
        boolean contentAreaFilled) {
        buttons[1].setSelected(true);
        buttons[2].setEnabled(false);
        buttons[3].setEnabled(false);
        buttons[3].setSelected(true);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setBorderPainted(borderPainted);
            buttons[i].setContentAreaFilled(contentAreaFilled);
        }

        return buildGrid(buttons[0],
                          buttons[1],
                          buttons[2],
                          buttons[3],
                          FormFactory.BUTTON_COLSPEC);
    }

    // Text Rows ************************************************************

    /**
     * Creates and returns a bar with 4 text components.
     * These are created using the given class;
     * they are wrapped in a <code>JScrollpane</code> iff the
     * wrap flag is set.
     */
    private JComponent buildTextRow(Class textComponentClass, boolean wrap) {
        JTextComponent[] components = new JTextComponent[4];
        for (int i = 0; i < 4; i++) {
            try {
                components[i] =
                    (JTextComponent) textComponentClass.newInstance();
            } catch (InstantiationException e) {
                // Won't happen in the context we're using this.
            } catch (IllegalAccessException e) {
                // Won't happen in the context we're using this.
            }
        }
        components[0].setText("Editable");

        components[1].setText("Uneditable");
        components[1].setEditable(false);

        components[2].setText("Disabled");
        components[2].setEnabled(false);

        components[3].setText("Uneditable");
        components[3].setEditable(false);
        components[3].setEnabled(false);

        return wrap
            ? buildGrid(
                wrapWithScrollPane(components[0]),
                wrapWithScrollPane(components[1]),
                wrapWithScrollPane(components[2]),
                wrapWithScrollPane(components[3]))
            : buildGrid(
                components[0],
                components[1],
                components[2],
                components[3]);
    }

    private Component wrapWithScrollPane(Component c) {
        return new JScrollPane(
            c,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    // Misc *****************************************************************

    private JComponent buildComboBoxRow() {
        return buildGrid(
            createComboBox("Editable",   true, true),
            createComboBox("Uneditable", true, false),
            createComboBox("Disabled",   false, true),
            createComboBox("Uneditable", false, false));
    }

    private JComboBox createComboBox(
        String text,
        boolean enabled,
        boolean editable) {
        JComboBox box =
            new JComboBox(new String[] { text, "Two", "Three", "Four", "A Quite Long Label" });
        box.setEnabled(enabled);
        box.setEditable(editable);
        box.putClientProperty(
                Options.COMBO_POPUP_PROTOTYPE_DISPLAY_VALUE_KEY,
                "A Quite Long Label");
        return box;
    }

    private JComponent buildSpinnerRow() {
        return buildGrid(createSpinner(true, true),
                         createSpinner(true, false),
                         createSpinner(false, true),
                         createSpinner(false, false));
    }

    private JComponent createSpinner(boolean enabled, boolean editable) {
        JSpinner spinner = new JSpinner();
        spinner.setValue(new Integer(123));
        spinner.setEnabled(enabled);
        JComponent editor = spinner.getEditor();
        if (editor instanceof DefaultEditor) {
            ((DefaultEditor) editor).getTextField().setEditable(editable);
        }
        return spinner;
    }


    // Custom ComboBox Editor ***********************************************

//    private static class CustomComboBoxRenderer implements ListCellRenderer {
//
//        private static final Border EMPTY_BORDER = new EmptyBorder(1,1,1,1);
//
//        private final JLabel label = new JLabel();
//
//        public Component getListCellRendererComponent(
//            JList list,
//            Object value,
//            int index,
//            boolean isSelected,
//            boolean cellHasFocus) {
//            label.setBackground(isSelected
//                    ? list.getSelectionBackground()
//                    : list.getBackground());
//            label.setForeground(isSelected
//                    ? list.getSelectionForeground()
//                    : list.getForeground());
//
//            label.setFont(list.getFont());
//            label.setBorder(EMPTY_BORDER);
//
//            if (value instanceof Icon) {
//                label.setIcon((Icon) value);
//            } else {
//                label.setText(value == null ? "" : value.toString());
//            }
//            label.setOpaque(true);
//            return label;
//        }
//
//    }


    // Helper Code **********************************************************

    private JComponent buildGrid(
                Component c1,
                Component c2,
                Component c3,
                Component c4) {
         return buildGrid(c1, c2, c3, c4,
                            new ColumnSpec(ColumnSpec.DEFAULT,
                                            Sizes.dluX(20),
                                            ColumnSpec.DEFAULT_GROW));
    }

    private JComponent buildGrid(
                Component c1,
                Component c2,
                Component c3,
                Component c4,
                ColumnSpec colSpec) {
        FormLayout layout = new FormLayout("", "pref");
        for (int i = 0; i < 4; i++) {
            layout.appendColumn(colSpec);
            layout.appendColumn(FormFactory.RELATED_GAP_COLSPEC);
        }
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setOpaque(false);
        CellConstraints cc = new CellConstraints();
        builder.add(c1, cc.xy(1, 1));
        builder.add(c2, cc.xy(3, 1));
        builder.add(c3, cc.xy(5, 1));
        builder.add(c4, cc.xy(7, 1));
        return builder.getPanel();
    }


}