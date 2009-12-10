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

package com.jgoodies.looks.windows;

import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.plaf.UIResource;

/**
 * Factory class that vends <code>Icon</code>s used in the JGoodies Windows look&amp;feel.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
final class WindowsIconFactory {

    private WindowsIconFactory() {
        // Overrides default constructor; prevents instantiation.
    }

	// Accessing and Creating Icons *****************************************************

    private static Icon checkBoxIcon;
    private static Icon radioButtonIcon;


    /**
     * Returns an <code>Icon</code> for a <code>JCheckBox</code>.
     */
    static Icon getCheckBoxIcon() {
		if (checkBoxIcon == null) {
	    	checkBoxIcon = new CheckBoxIcon();
		}
		return checkBoxIcon;
    }


    /**
     * Returns an <code>Icon</code> for a <code>JCheckBox</code>.
     */
    static Icon getRadioButtonIcon() {
		if (radioButtonIcon == null) {
	    	radioButtonIcon = new RadioButtonIcon();
		}
		return radioButtonIcon;
    }


	// Helper Classes *******************************************************************

	// Paints the icon for a check box.
    private static class CheckBoxIcon implements Icon, Serializable {

		private static final int SIZE  = 13;

		public void paintIcon(Component c, Graphics g, int x, int y) {
		    JCheckBox cb = (JCheckBox) c;
		    ButtonModel model = cb.getModel();

		    // outer bevel
		    if (!cb.isBorderPaintedFlat()) {
                // Outer top/left
                g.setColor(UIManager.getColor("CheckBox.shadow"));
                g.drawLine(x, y, x+11, y);
                g.drawLine(x, y+1, x, y+11);

                // Outer bottom/right
                g.setColor(UIManager.getColor("CheckBox.highlight"));
                g.drawLine(x+12, y, x+12, y+12);
                g.drawLine(x, y+12, x+11, y+12);

                // Inner top.left
                g.setColor(UIManager.getColor("CheckBox.darkShadow"));
                g.drawLine(x+1, y+1, x+10, y+1);
                g.drawLine(x+1, y+2, x+1, y+10);

                // Inner bottom/right
                g.setColor(UIManager.getColor("CheckBox.light"));
                g.drawLine(x+1, y+11, x+11, y+11);
                g.drawLine(x+11, y+1, x+11, y+10);
		    } else {
				g.setColor(UIManager.getColor("CheckBox.shadow"));
				g.drawRect(x+1, y+1, SIZE-3, SIZE-3);
		    }
			// inside box
			g.setColor(UIManager.getColor(
					(model.isPressed() && model.isArmed()) || !model.isEnabled()
						? "CheckBox.background"
						: "CheckBox.interiorBackground"));
			g.fillRect(x+2, y+2, SIZE-4, SIZE-4);

		    g.setColor(UIManager.getColor(model.isEnabled()
							? "CheckBox.checkColor"  // Modified by JGoodies
							: "CheckBox.shadow"));

		    // paint check
		    if (model.isSelected()) {
				g.drawLine(x+9, y+3, x+9, y+3);
				g.drawLine(x+8, y+4, x+9, y+4);
				g.drawLine(x+7, y+5, x+9, y+5);
				g.drawLine(x+6, y+6, x+8, y+6);
				g.drawLine(x+3, y+7, x+7, y+7);
				g.drawLine(x+4, y+8, x+6, y+8);
				g.drawLine(x+5, y+9, x+5, y+9);
				g.drawLine(x+3, y+5, x+3, y+5);
				g.drawLine(x+3, y+6, x+4, y+6);
		    }
		}

		public int getIconWidth()  { return SIZE; }
		public int getIconHeight() { return SIZE; }
    }


	// Paints the icon for a radio button.
	private static class RadioButtonIcon implements Icon, UIResource, Serializable {

		private static final int SIZE  = 13;

		public void paintIcon(Component c, Graphics g, int x, int y) {
		    AbstractButton b = (AbstractButton) c;
		    ButtonModel model = b.getModel();

		    // fill interior
		    g.setColor(UIManager.getColor(
		    		(model.isPressed() && model.isArmed()) || !model.isEnabled()
						? "RadioButton.background"
						: "RadioButton.interiorBackground"));
		    g.fillRect(x+2, y+2, 8, 8);


			// outter left arc
		    g.setColor(UIManager.getColor("RadioButton.shadow"));
		    g.drawLine(x+4, y+0, x+7, y+0);
		    g.drawLine(x+2, y+1, x+3, y+1);
		    g.drawLine(x+8, y+1, x+9, y+1);
		    g.drawLine(x+1, y+2, x+1, y+3);
		    g.drawLine(x+0, y+4, x+0, y+7);
		    g.drawLine(x+1, y+8, x+1, y+9);

		    // outter right arc
		    g.setColor(UIManager.getColor("RadioButton.highlight"));
		    g.drawLine(x+2, y+10, x+3, y+10);
		    g.drawLine(x+4, y+11, x+7, y+11);
		    g.drawLine(x+8, y+10, x+9, y+10);
		    g.drawLine(x+10, y+9, x+10, y+8);
		    g.drawLine(x+11, y+7, x+11, y+4);
		    g.drawLine(x+10, y+3, x+10, y+2);


		    // inner left arc
		    g.setColor(UIManager.getColor("RadioButton.darkShadow"));
		    g.drawLine(x+4, y+1, x+7, y+1);
		    g.drawLine(x+2, y+2, x+3, y+2);
		    g.drawLine(x+8, y+2, x+9, y+2);
		    g.drawLine(x+2, y+3, x+2, y+3);
		    g.drawLine(x+1, y+4, x+1, y+7);
		    g.drawLine(x+2, y+8, x+2, y+8);


		    // inner right arc
		    g.setColor(UIManager.getColor("RadioButton.light"));
		    g.drawLine(x+2,  y+9,  x+3,  y+9);
		    g.drawLine(x+4,  y+10, x+7,  y+10);
		    g.drawLine(x+8,  y+9,  x+9,  y+9);
		    g.drawLine(x+9,  y+8,  x+9,  y+8);
		    g.drawLine(x+10, y+7,  x+10, y+4);
		    g.drawLine(x+9,  y+3,  x+9,  y+3);


		    // indicate whether selected or not
		    if(model.isSelected()) {
				g.setColor(UIManager.getColor("RadioButton.checkColor")); // Modified by JGoodies
				g.fillRect(x+4, y+5, 4, 2);
				g.fillRect(x+5, y+4, 2, 4);
		    }
		}

		public int getIconWidth()  { return SIZE; }
		public int getIconHeight() { return SIZE; }

    }

}