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

package com.jgoodies.looks.plastic;

import java.awt.Insets;

import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.MicroLayout;


/**
 * Intializes class and component defaults for the
 * JGoodies PlasticXP look&amp;feel.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
public class PlasticXPLookAndFeel extends Plastic3DLookAndFeel {

    /**
     * Constructs the JGoodies PlasticXP look&amp;feel.
     */
    public PlasticXPLookAndFeel() {
        // Provide an empty constructor for subclassing.
    }

    public String getID() {
        return "JGoodies Plastic XP";
    }

    public String getName() {
        return "JGoodies Plastic XP";
    }

    public String getDescription() {
        return "The JGoodies Plastic XP Look and Feel"
            + " - \u00a9 2001-2009 JGoodies Karsten Lentzsch";
    }

    /**
     * Initializes the PlasticXP class defaults.
     * Overrides the UIS for check box, radio button, and spinner.
     *
     * @param table   the UIDefaults table to work with
     */
    protected void initClassDefaults(UIDefaults table) {
        super.initClassDefaults(table);

        final String uiClassnamePrefix   = "com.jgoodies.looks.plastic.PlasticXP";
        Object[] uiDefaults = {
            // Uses a smooth icon
            "CheckBoxUI",      uiClassnamePrefix + "CheckBoxUI",

            // Uses a smooth icon
            "RadioButtonUI",   uiClassnamePrefix + "RadioButtonUI",

            // Changed buttons
            "SpinnerUI",       uiClassnamePrefix + "SpinnerUI",

            // Uses rounded rollover borders
            "ToolBarUI",       uiClassnamePrefix + "ToolBarUI"
        };
        table.putDefaults(uiDefaults);
    }


	/**
	 * Initializes the PlasticXP component defaults.
     *
     * @param table   the UIDefaults table to work with
	 */
	protected void initComponentDefaults(UIDefaults table) {
		super.initComponentDefaults(table);

        MicroLayout microLayout = getMicroLayoutPolicy().getMicroLayout(getName(), table);
        Insets buttonBorderInsets = microLayout.getButtonBorderInsets();

        Object buttonBorder         = PlasticXPBorders.getButtonBorder(buttonBorderInsets);
        Object toggleButtonBorder   = PlasticXPBorders.getToggleButtonBorder(buttonBorderInsets);
        Object checkBoxIcon         = PlasticXPIconFactory.getCheckBoxIcon();
        Object comboBoxButtonBorder = PlasticXPBorders.getComboBoxArrowButtonBorder();
        Border comboBoxEditorBorder = PlasticXPBorders.getComboBoxEditorBorder();
        Object radioButtonIcon      = PlasticXPIconFactory.getRadioButtonIcon();
        Object scrollPaneBorder     = PlasticXPBorders.getScrollPaneBorder();
        Object textFieldBorder      = PlasticXPBorders.getTextFieldBorder();
        Object spinnerBorder        = PlasticXPBorders.getSpinnerBorder();

        String radioCheckIconName   = LookUtils.IS_LOW_RESOLUTION
                                            ? "icons/RadioLight5x5.png"
                                            : "icons/RadioLight7x7.png";

        Insets comboEditorInsets = microLayout.getComboBoxEditorInsets();

        Insets comboEditorBorderInsets = comboBoxEditorBorder.getBorderInsets(null);
        int comboBorderSize  = comboEditorBorderInsets.left;
        int comboPopupBorderSize = microLayout.getComboPopupBorderSize();
        int comboRendererGap = comboEditorInsets.left + comboBorderSize - comboPopupBorderSize;
        Object comboRendererBorder = new EmptyBorder(1, comboRendererGap, 1, comboRendererGap);
        Object comboTableEditorInsets = new Insets(0, 0, 0, 0);

		Object[] defaults = {
            "Button.border",                  buttonBorder,
            "Button.borderPaintsFocus",       Boolean.TRUE,

            "CheckBox.icon",                  checkBoxIcon,
            "CheckBox.check",                 getToggleButtonCheckColor(),

            "ComboBox.arrowButtonBorder",     comboBoxButtonBorder,
            "ComboBox.editorBorder",          comboBoxEditorBorder,
            "ComboBox.borderPaintsFocus",     Boolean.TRUE,
            "ComboBox.editorBorderInsets",    comboEditorBorderInsets,          // Added by JGoodies
            "ComboBox.tableEditorInsets",     comboTableEditorInsets,
            "ComboBox.rendererBorder",        comboRendererBorder, // Added by JGoodies

            "FormattedTextField.border",      textFieldBorder,
            "PasswordField.border",           textFieldBorder,
            "Spinner.border", 				  spinnerBorder,

            "ScrollPane.border",              scrollPaneBorder,
            "Table.scrollPaneBorder", 		  scrollPaneBorder,

            "RadioButton.icon",               radioButtonIcon,
            "RadioButton.check",              getToggleButtonCheckColor(),
            "RadioButton.interiorBackground", getControlHighlight(),
            "RadioButton.checkIcon",          makeIcon(getClass(), radioCheckIconName),

            "TextField.border",               textFieldBorder,

            "ToggleButton.border",            toggleButtonBorder,
            "ToggleButton.borderPaintsFocus", Boolean.TRUE,

            "Tree.expandedIcon",              makeIcon(getClass(), "icons/TreeExpanded.png"),
            "Tree.collapsedIcon",             makeIcon(getClass(), "icons/TreeCollapsed.png")
		};
		table.putDefaults(defaults);
	}

    protected static void installDefaultThemes() {}

    private ColorUIResource getToggleButtonCheckColor() {
        return getPlasticTheme().getToggleButtonCheckColor();
    }


}