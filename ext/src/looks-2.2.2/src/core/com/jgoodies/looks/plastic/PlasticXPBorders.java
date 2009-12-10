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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;


/**
 * This class consists of a set of <code>Border</code>s used
 * by the JGoodies Plastic XP Look and Feel UI delegates.
 *
 * @author Karsten Lentzsch
 * @author Andrej Golovnin
 * @version $Revision$
 */

final class PlasticXPBorders {

    private PlasticXPBorders() {
        // Overrides default constructor; prevents instantiation.
    }


    // Accessing and Creating Borders ***************************************

    private static Border comboBoxArrowButtonBorder;
    private static Border comboBoxEditorBorder;
    private static Border scrollPaneBorder;
    private static Border textFieldBorder;
    private static Border spinnerBorder;
    private static Border rolloverButtonBorder;


    /*
     * Returns a border instance for a <code>JButton</code>.
     */
    static Border getButtonBorder(Insets buttonMargin) {
        return new BorderUIResource.CompoundBorderUIResource(
                    new XPButtonBorder(buttonMargin),
                    new BasicBorders.MarginBorder());
    }

    /*
     * Returns a border instance for a <code>JComboBox</code>'s arrow button.
     */
    static Border getComboBoxArrowButtonBorder() {
        if (comboBoxArrowButtonBorder == null) {
            comboBoxArrowButtonBorder = new CompoundBorder(  // No UIResource
                                    new XPComboBoxArrowButtonBorder(),
                                    new BasicBorders.MarginBorder());
        }
        return comboBoxArrowButtonBorder;
    }

    /*
     * Returns a border instance for a <code>JComboBox</code>'s editor.
     */
    static Border getComboBoxEditorBorder() {
        if (comboBoxEditorBorder == null) {
            comboBoxEditorBorder = new CompoundBorder(  // No UIResource
                                    new XPComboBoxEditorBorder(),
                                    new BasicBorders.MarginBorder());
        }
        return comboBoxEditorBorder;
    }

    /*
     * Returns a border instance for a <code>JScrollPane</code>.
     */
    static Border getScrollPaneBorder() {
        if (scrollPaneBorder == null) {
            scrollPaneBorder = new XPScrollPaneBorder();
        }
        return scrollPaneBorder;
    }

    /*
     * Returns a border instance for a <code>JTextField</code>.
     */
    static Border getTextFieldBorder() {
        if (textFieldBorder == null) {
            textFieldBorder = new BorderUIResource.CompoundBorderUIResource(
                                    new XPTextFieldBorder(),
                                    new BasicBorders.MarginBorder());
        }
        return textFieldBorder;
    }

    /*
     * Returns a border instance for a <code>JToggleButton</code>.
     */
    static Border getToggleButtonBorder(Insets buttonMargin) {
         return new BorderUIResource.CompoundBorderUIResource(
                    new XPButtonBorder(buttonMargin),
                    new BasicBorders.MarginBorder());
    }

    /*
     * Returns a border instance for a <code>JSpinner</code>.
     */
    static Border getSpinnerBorder() {
        if (spinnerBorder == null) {
            spinnerBorder = new XPSpinnerBorder();
        }
        return spinnerBorder;
    }


    /**
     * Returns a rollover border for buttons in a <code>JToolBar</code>.
     *
     * @return the lazily created rollover button border
     */
    static Border getRolloverButtonBorder() {
        if (rolloverButtonBorder == null) {
            rolloverButtonBorder = new CompoundBorder(
                                        new RolloverButtonBorder(),
                                        new PlasticBorders.RolloverMarginBorder());
        }
        return rolloverButtonBorder;
    }

    /**
     * A border for XP style buttons.
     */
    private static class XPButtonBorder extends AbstractBorder implements UIResource {

        protected final Insets insets;

        protected XPButtonBorder(Insets insets) {
            this.insets = insets;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            AbstractButton button = (AbstractButton) c;
            ButtonModel    model  = button.getModel();

            if (!model.isEnabled()) {
                PlasticXPUtils.drawDisabledButtonBorder(g, x, y, w, h);
                return;
            }

            boolean isPressed = model.isPressed() && model.isArmed();
            boolean isDefault = button instanceof JButton
                                     && ((JButton) button).isDefaultButton();
            boolean isFocused = button.isFocusPainted() && button.hasFocus();

            if (isPressed)
                PlasticXPUtils.drawPressedButtonBorder(g, x, y, w, h);
            else if (isFocused)
                PlasticXPUtils.drawFocusedButtonBorder(g, x, y, w, h);
            else if (isDefault)
                PlasticXPUtils.drawDefaultButtonBorder(g, x, y, w, h);
            else
                PlasticXPUtils.drawPlainButtonBorder(g, x, y, w, h);
        }

        public Insets getBorderInsets(Component c) { return insets; }

        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top    = insets.top;
            newInsets.left   = insets.left;
            newInsets.bottom = insets.bottom;
            newInsets.right  = insets.right;
            return newInsets;
        }
    }


    /**
     * A border for combo box arrow buttons.
     */
    private static final class XPComboBoxArrowButtonBorder extends AbstractBorder implements UIResource {

        protected static final Insets INSETS = new Insets(1, 1, 1, 1);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            PlasticComboBoxButton button = (PlasticComboBoxButton) c;
            JComboBox comboBox = button.getComboBox();
            ButtonModel model = button.getModel();

            if (!model.isEnabled()) {
                PlasticXPUtils.drawDisabledButtonBorder(g, x, y, w, h);
            } else {
                boolean isPressed = model.isPressed() && model.isArmed();
                boolean isFocused = comboBox.hasFocus();
                if (isPressed)
                    PlasticXPUtils.drawPressedButtonBorder(g, x, y, w, h);
                else if (isFocused)
                    PlasticXPUtils.drawFocusedButtonBorder(g, x, y, w, h);
                else
                    PlasticXPUtils.drawPlainButtonBorder(g, x, y, w, h);
            }
            if (comboBox.isEditable()) {
                // Paint two pixel on the arrow button's left hand side.
                g.setColor(model.isEnabled()
                                ? PlasticLookAndFeel.getControlDarkShadow()
                                : MetalLookAndFeel.getControlShadow());
                g.fillRect(x, y,       1, 1);
                g.fillRect(x, y + h-1, 1, 1);
            }
        }

        public Insets getBorderInsets(Component c) { return INSETS; }
    }


    /**
     * A border for combo box editors.
     */
    private static final class XPComboBoxEditorBorder extends AbstractBorder {

        private static final Insets INSETS  = new Insets(1, 1, 1, 0);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.setColor(c.isEnabled()
                           ? PlasticLookAndFeel.getControlDarkShadow()
                           : MetalLookAndFeel.getControlShadow());
            PlasticXPUtils.drawRect(g, x, y, w+1, h-1);
        }

        public Insets getBorderInsets(Component c) { return INSETS; }
    }


    /**
     * A border for text fields.
     */
    private static final class XPTextFieldBorder extends AbstractBorder  {

        private static final Insets INSETS = new Insets(1, 1, 1, 1);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

            boolean enabled = ((c instanceof JTextComponent)
                               && (c.isEnabled() && ((JTextComponent) c).isEditable()))
                               ||
                               c.isEnabled();

	        g.setColor(enabled
                            ? PlasticLookAndFeel.getControlDarkShadow()
                            : MetalLookAndFeel.getControlShadow());
            PlasticXPUtils.drawRect(g, x, y, w-1, h-1);
    	}

        public Insets getBorderInsets(Component c) { return INSETS; }

        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top    = INSETS.top;
            newInsets.left   = INSETS.left;
            newInsets.bottom = INSETS.bottom;
            newInsets.right  = INSETS.right;
            return newInsets;
        }
	}


    /**
     * Unlike Metal we paint a simple rectangle.
     * Being a subclass of MetalBorders.ScrollPaneBorder ensures that
     * the ScrollPaneUI will update the ScrollbarsFreeStanding property.
     */
    private static final class XPScrollPaneBorder extends MetalBorders.ScrollPaneBorder  {

        private static final Insets INSETS = new Insets(1, 1, 1, 1);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.setColor(c.isEnabled()
                    ? PlasticLookAndFeel.getControlDarkShadow()
                    : MetalLookAndFeel.getControlShadow());
            PlasticXPUtils.drawRect(g, x, y, w-1, h-1);
        }

        public Insets getBorderInsets(Component c) { return INSETS; }

        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top    = INSETS.top;
            newInsets.left   = INSETS.left;
            newInsets.bottom = INSETS.bottom;
            newInsets.right  = INSETS.right;
            return newInsets;
        }
    }


    /**
     * A border for <code>JSpinner</code> components.
     */
    private static final class XPSpinnerBorder extends MetalBorders.ScrollPaneBorder  {

        private static final Insets INSETS = new Insets(1, 1, 1, 1);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.setColor(c.isEnabled()
                    ? PlasticLookAndFeel.getControlDarkShadow()
                    : MetalLookAndFeel.getControlShadow());
            // If you change the value of arrowButtonWidth, don't forget
            // to change it in PlasticXPSpinnerUI#SpinnerXPArrowButton too.
            int arrowButtonWidth = UIManager.getInt("ScrollBar.width") - 1;
            w -= arrowButtonWidth;
            g.fillRect(x,   y,     w,   1);
            g.fillRect(x,   y+1,   1,   h-1);
            g.fillRect(x+1, y+h-1, w-1, 1);
        }

        public Insets getBorderInsets(Component c) { return INSETS; }

        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top    = INSETS.top;
            newInsets.left   = INSETS.left;
            newInsets.bottom = INSETS.bottom;
            newInsets.right  = INSETS.right;
            return newInsets;
        }
    }


    /**
     * A rollover border for buttons in toolbars.
     */
    private static final class RolloverButtonBorder extends XPButtonBorder {

        private RolloverButtonBorder() {
            super(new Insets(3, 3, 3, 3));
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();

            if (!model.isEnabled())
                return;

            if (!(c instanceof JToggleButton)) {
                if (model.isRollover() && !(model.isPressed() && !model.isArmed())) {
                    super.paintBorder( c, g, x, y, w, h );
                }
                return;
            }

            if (model.isRollover()) {
                if (model.isPressed() && model.isArmed()) {
                    PlasticXPUtils.drawPressedButtonBorder(g, x, y, w, h);
                } else {
                    PlasticXPUtils.drawPlainButtonBorder(g, x, y, w, h);
                }
            } else if (model.isSelected()) {
                PlasticXPUtils.drawPressedButtonBorder(g, x, y, w, h);
            }
        }

    }

}
