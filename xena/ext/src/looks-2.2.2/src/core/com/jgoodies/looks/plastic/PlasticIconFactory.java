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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Factory class that vends <code>Icon</code>s for the
 * JGoodies Plastic look and feel.
 * These icons are used extensively in Plastic via the defaults mechanism.
 * While other look and feels often use GIFs for icons, creating icons
 * in code facilitates switching to other themes.
 * <p>
 * Each method in this class returns either an <code>Icon</code> or <code>null</code>,
 * where <code>null</code> implies that there is no default icon.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */

final class PlasticIconFactory {


    private PlasticIconFactory() {
        // Overrides default constructor; prevents instantiation.
    }


	// Helper method utilized by the CheckBoxIcon and the CheckBoxMenuItemIcon.
	private static void drawCheck(Graphics g, int x, int y) {
		g.translate(x, y);
		g.drawLine(3, 5, 3, 5);
		g.fillRect(3, 6, 2, 2);
		g.drawLine(4, 8, 9, 3);
		g.drawLine(5, 8, 9, 4);
		g.drawLine(5, 9, 9, 5);
		g.translate(-x, -y);
	}


	private static class CheckBoxIcon implements Icon, UIResource, Serializable {

		private static final int SIZE = 13;

		public int getIconWidth()	{ return SIZE; }
		public int getIconHeight() { return SIZE; }

		public void paintIcon(Component c, Graphics g, int x, int y) {
			JCheckBox cb = (JCheckBox) c;
			ButtonModel model = cb.getModel();

			if (model.isEnabled()) {
				if (cb.isBorderPaintedFlat()) {
					g.setColor(PlasticLookAndFeel.getControlDarkShadow());
					g.drawRect(x, y, SIZE - 2, SIZE - 2);
					// inside box
					g.setColor(PlasticLookAndFeel.getControlHighlight());
					g.fillRect(x+1, y+1, SIZE-3, SIZE-3);
				} else if (model.isPressed() && model.isArmed()) {
					g.setColor(MetalLookAndFeel.getControlShadow());
					g.fillRect(x, y, SIZE - 1, SIZE - 1);
					PlasticUtils.drawPressed3DBorder(g, x, y, SIZE, SIZE);
				} else {
					PlasticUtils.drawFlush3DBorder(g, x, y, SIZE, SIZE);
				}
				g.setColor(MetalLookAndFeel.getControlInfo());
			} else {
				g.setColor(MetalLookAndFeel.getControlShadow());
				g.drawRect(x, y, SIZE - 2, SIZE - 2);
			}

			if (model.isSelected()) {
				drawCheck(g, x, y);
			}
		}

	}


    private static class CheckBoxMenuItemIcon implements Icon, UIResource, Serializable {

    	private static final int SIZE = 13;

		public int getIconWidth()	{ return SIZE; }
		public int getIconHeight() { return SIZE; }

 		public void paintIcon(Component c, Graphics g, int x, int y) {
		    JMenuItem b = (JMenuItem) c;
		    if (b.isSelected()) {
		    	drawCheck(g, x, y + 1);
		    }
 		}
    }


    private static class RadioButtonMenuItemIcon implements Icon, UIResource, Serializable {

    	private static final int SIZE = 13;

		public int getIconWidth()	{ return SIZE; }
		public int getIconHeight() { return SIZE; }

 		public void paintIcon(Component c, Graphics g, int x, int y) {
		    JMenuItem b = (JMenuItem) c;
		    if (b.isSelected()) {
		    	drawDot(g, x, y);
		    }
 		}

 		private void drawDot(Graphics g, int x, int y) {
	 		g.translate(x, y);
			g.drawLine(5, 4, 8, 4);
			g.fillRect(4, 5, 6, 4);
			g.drawLine(5, 9, 8, 9);
			g.translate(-x, -y);
		}
    }


    private static class MenuArrowIcon implements Icon, UIResource, Serializable  {

    	private static final int WIDTH  = 4;
    	private static final int HEIGHT = 8;

		public void paintIcon( Component c, Graphics g, int x, int y ) {
		    JMenuItem b = (JMenuItem) c;

		    g.translate( x, y );
	        if (PlasticUtils.isLeftToRight(b) ) {
	            g.drawLine( 0, 0, 0, 7 );
	            g.drawLine( 1, 1, 1, 6 );
	            g.drawLine( 2, 2, 2, 5 );
	            g.drawLine( 3, 3, 3, 4 );
	        } else {
	            g.drawLine( 4, 0, 4, 7 );
	            g.drawLine( 3, 1, 3, 6 );
	            g.drawLine( 2, 2, 2, 5 );
	            g.drawLine( 1, 3, 1, 4 );
	        }
		    g.translate( -x, -y );
		}

		public int getIconWidth()	{ return WIDTH; }
		public int getIconHeight() { return HEIGHT; }

    }


    /**
     * Paints a minus sign button icon used in trees.
     * Uses a white background, gray border, and black foreground.
     */
    private static class ExpandedTreeIcon implements Icon, Serializable {

		protected static final int SIZE      = 9;
		protected static final int HALF_SIZE = 4;

		public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.WHITE);
			g.fillRect(x, y, SIZE - 1, SIZE - 1);
			g.setColor(Color.GRAY);
			g.drawRect(x, y, SIZE - 1, SIZE - 1);
			g.setColor(Color.BLACK);
			g.drawLine(x + 2, y + HALF_SIZE, x + (SIZE - 3), y + HALF_SIZE);
		}

		public int getIconWidth()  { return SIZE; }
		public int getIconHeight() { return SIZE; }
    }


    /**
     * The plus sign button icon used in trees.
     */
    private static class CollapsedTreeIcon extends ExpandedTreeIcon {
		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			g.drawLine(x + HALF_SIZE, y + 2, x + HALF_SIZE, y + (SIZE - 3));
		}
    }


    /**
     * The arrow button used in comboboxes.
     */
    private static class ComboBoxButtonIcon implements Icon, Serializable {

	    public void paintIcon(Component c, Graphics g, int x, int y) {
	        JComponent component = (JComponent) c;
			int iconWidth = getIconWidth();

			g.translate(x, y);

			g.setColor( component.isEnabled()
						? MetalLookAndFeel.getControlInfo()
						: MetalLookAndFeel.getControlShadow() );
			g.drawLine( 0, 0, iconWidth - 1, 0 );
			g.drawLine( 1, 1, 1 + (iconWidth - 3), 1 );
			g.drawLine( 2, 2, 2 + (iconWidth - 5), 2 );
			g.drawLine( 3, 3, 3 + (iconWidth - 7), 3 );

/*
		int startY = (((h + 1) - arrowHeight) / 2) + arrowHeight - 1;
		int startX = (w / 2);

		//	    System.out.println( "startX2 :" + startX + " startY2 :"+startY);

		for (int line = 0; line < arrowHeight; line++) {
			g.drawLine(
				startX - line,
				startY - line,
				startX + line + 1,
				startY - line);
		}*/
			g.translate( -x, -y );
	    }

	    public int getIconWidth()  { return 8; }
	    public int getIconHeight() { return 4; }
    }


	// Cached Access to Icons ***********************************************************

	private static Icon checkBoxIcon;
    private static Icon checkBoxMenuItemIcon;
    private static Icon radioButtonMenuItemIcon;
    private static Icon menuArrowIcon;
    private static Icon expandedTreeIcon;
    private static Icon collapsedTreeIcon;
    private static Icon comboBoxButtonIcon;


	/**
	 * Answers an <code>Icon</code> used for <code>JCheckBox</code>es.
	 */
    static Icon getCheckBoxIcon() {
		if (checkBoxIcon == null) {
	    	checkBoxIcon = new CheckBoxIcon();
		}
		return checkBoxIcon;
    }


	/**
	 * Answers an <code>Icon</code> used for <code>JCheckButtonMenuItem</code>s.
	 */
    static Icon getCheckBoxMenuItemIcon() {
		if (checkBoxMenuItemIcon == null) {
	    	checkBoxMenuItemIcon = new CheckBoxMenuItemIcon();
		}
		return checkBoxMenuItemIcon;
    }


	/**
	 * Answers an <code>Icon</code> used for <code>JRadioButtonMenuItem</code>s.
	 */
    static Icon getRadioButtonMenuItemIcon() {
		if (radioButtonMenuItemIcon == null) {
	    	radioButtonMenuItemIcon = new RadioButtonMenuItemIcon();
		}
		return radioButtonMenuItemIcon;
    }


	/**
	 * Answers an <code>Icon</code> used for arrows in <code>JMenu</code>s.
	 */
    static Icon getMenuArrowIcon() {
		if (menuArrowIcon == null) {
	    	menuArrowIcon = new MenuArrowIcon();
		}
		return menuArrowIcon;
    }


	/**
	 * Answers an <code>Icon</code> used in <code>JTree</code>s.
	 */
    static Icon getExpandedTreeIcon() {
		if (expandedTreeIcon == null) {
	    	expandedTreeIcon = new ExpandedTreeIcon();
		}
		return expandedTreeIcon;
    }

	/**
	 * Answers an <code>Icon</code> used in <code>JTree</code>s.
	 */
    static Icon getCollapsedTreeIcon() {
		if (collapsedTreeIcon == null) {
	    	collapsedTreeIcon = new CollapsedTreeIcon();
		}
		return collapsedTreeIcon;
    }

	/**
	 * Answers an <code>Icon</code> used in <code>JComboBox</code>es.
	 */
    static Icon getComboBoxButtonIcon() {
        if (comboBoxButtonIcon == null) {
            comboBoxButtonIcon = new ComboBoxButtonIcon();
        }
		return comboBoxButtonIcon;
    }


}