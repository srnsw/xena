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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * Consists of static inner classes that define different
 * <code>Borders</code> used in the JGoodies Windows look&amp;feel.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
final class WindowsBorders {

    private WindowsBorders() {
        // Overrides default constructor; prevents instantiation.
    }

	// Accessing and Creating Borders ***************************************************

    private static Border menuBorder;
    private static Border xpMenuBorder;
    private static Border menuItemBorder;
    private static Border popupMenuBorder;
    private static Border noMarginPopupMenuBorder;
    private static Border separatorBorder;
    private static Border etchedBorder;
    private static Border menuBarHeaderBorder;
    private static Border toolBarHeaderBorder;
    private static Border rolloverButtonBorder;


	/**
	 * Returns a <code>Border</code> for a <code>JButton</code>.
	 */
    public static Border getButtonBorder() {
        UIDefaults table = UIManager.getLookAndFeelDefaults();
        Border outerBorder = new ButtonBorder(table.getColor("Button.shadow"),
                table.getColor("Button.darkShadow"), table
                        .getColor("Button.light"), table
                        .getColor("Button.highlight"), table
                        .getColor("controlText"));

        Border buttonBorder = new BorderUIResource.CompoundBorderUIResource(
                outerBorder, new BasicBorders.MarginBorder());
        return buttonBorder;
    }


    /**
     * Returns a Border for a JMenu in classic mode.
     */
    static Border getMenuBorder() {
		if (menuBorder == null) {
	    	menuBorder = new BorderUIResource.CompoundBorderUIResource(
						   	new MenuBorder(),
						   	new BasicBorders.MarginBorder());
		}
		return menuBorder;
    }

    /**
     * Returns a Border for a JMenu in XP mode.
     */
    static Border getXPMenuBorder() {
        if (xpMenuBorder == null) {
            xpMenuBorder = new BasicBorders.MarginBorder();
        }
        return xpMenuBorder;
    }

    /**
     * Returns a border instance for a <code>JMenuItem</code>.
     */
    static Border getMenuItemBorder() {
		if (menuItemBorder == null) {
	    	menuItemBorder = new BorderUIResource(new BasicBorders.MarginBorder());
		}
		return menuItemBorder;
    }

    /**
     * Returns a separator border instance for <code>JMenuBar</code> or <code>JToolBar</code>.
     */
    static Border getSeparatorBorder() {
		if (separatorBorder == null) {
	    	separatorBorder = new BorderUIResource.CompoundBorderUIResource(
						   			new SeparatorBorder(),
						   			new BasicBorders.MarginBorder());
		}
		return separatorBorder;
    }

    /**
     * Returns an etched border instance for <code>JMenuBar</code> or <code>JToolBar</code>.
     */
    static Border getEtchedBorder() {
		if (etchedBorder == null) {
	    	etchedBorder = new BorderUIResource.CompoundBorderUIResource(
						   			new EtchedBorder(),
						   			new BasicBorders.MarginBorder());
		}
		return etchedBorder;
    }

    /**
     * Returns a special border for a <code>JMenuBar</code> that
     * is used in a header just above a <code>JToolBar</code>.
     */
    static Border getMenuBarHeaderBorder() {
		if (menuBarHeaderBorder == null) {
	    	menuBarHeaderBorder = new BorderUIResource.CompoundBorderUIResource(
						   				new MenuBarHeaderBorder(),
						   				new BasicBorders.MarginBorder());
		}
		return menuBarHeaderBorder;
    }

    /**
     * Returns a border instance for a <code>JPopupMenu</code>.
     *
     * @return the lazily created popup menu border
     */
    static Border getPopupMenuBorder() {
        if (popupMenuBorder == null) {
            popupMenuBorder = new PopupMenuBorder();
        }
        return popupMenuBorder;
    }

    /**
     * Returns a no-margin border instance for a <code>JPopupMenu</code>.
     *
     * @return the lazily created no-margin popup menu border
     */
    static Border getNoMarginPopupMenuBorder() {
        if (noMarginPopupMenuBorder == null) {
            noMarginPopupMenuBorder = new NoMarginPopupMenuBorder();
        }
        return noMarginPopupMenuBorder;
    }

    /**
     * Returns a special border for a <code>JToolBar</code> that
     * is used in a header just below a <code>JMenuBar</code>.
     */
    static Border getToolBarHeaderBorder() {
		if (toolBarHeaderBorder == null) {
	    	toolBarHeaderBorder = new BorderUIResource.CompoundBorderUIResource(
						   				new ToolBarHeaderBorder(),
						   				new BasicBorders.MarginBorder());
		}
		return toolBarHeaderBorder;
    }

    /**
     * Returns a border for a rollover <code>AbstractButton</code>.
     */
    static Border getRolloverButtonBorder() {
		if (rolloverButtonBorder == null) {
            rolloverButtonBorder = new CompoundBorder( // No UIResource
                                        new RolloverButtonBorder(),
                                        new RolloverMarginBorder());
		}
		return rolloverButtonBorder;
    }


	// Helper Classes *******************************************************************


	// Copied from BasicBorders, has correct black color for the outer default rectangle.
   private static final class ButtonBorder extends AbstractBorder implements UIResource {

   		private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

        private final Color shadow;
        private final Color darkShadow;
        private final Color highlight;
        private final Color lightHighlight;
        private final Color defaultColor;

        public ButtonBorder(Color shadow, Color darkShadow,
                             Color highlight, Color lightHighlight, Color defaultColor) {
            this.shadow = shadow;
            this.darkShadow = darkShadow;
            this.highlight = highlight;
            this.lightHighlight = lightHighlight;
            this.defaultColor = defaultColor;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            boolean isPressed = false;
            boolean isDefault = false;

            if (c instanceof AbstractButton) {
	        	AbstractButton b = (AbstractButton) c;
	        	ButtonModel model = b.getModel();

   	        	isPressed = model.isPressed() && model.isArmed();
                if (c instanceof JButton) {
                    isDefault = ((JButton) c).isDefaultButton();
                }
            }
            drawBezel(g, x, y, width, height, isPressed, isDefault, shadow,
                      darkShadow, highlight, lightHighlight, defaultColor);
        }

        public Insets getBorderInsets(Component c)       {
            return getBorderInsets(c, EMPTY_INSETS);
        }

        public Insets getBorderInsets(Component c, Insets insets)       {
            // leave room for default visual
            insets.top = 2;
            insets.left = insets.bottom = insets.right = 3;
	    	return insets;
        }

    }

	/**
	 * An abstract superclass for borders.
	 */
	private abstract static class AbstractButtonBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(2, 2, 2, 2);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			AbstractButton button = (AbstractButton) c;
			ButtonModel model = button.getModel();

			//
			//System.out.println("Pressed=" + model.isPressed() + "; armed=" + model.isArmed());
			//if (!model.isArmed()) return;

			if (model.isPressed())
				WindowsUtils.drawPressed3DBorder(g, x, y, w, h);
			else
				WindowsUtils.drawFlush3DBorder(g, x, y, w, h);
		}

		public Insets getBorderInsets(Component c) { return INSETS; }
	}


	/**
	 * A border used for <code>Buttons</code> that have the rollover property enabled.
	 */
	private static final class RolloverButtonBorder extends AbstractButtonBorder {

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			AbstractButton b = (AbstractButton) c;
			ButtonModel model = b.getModel();

			if (!model.isEnabled())
				return;

			if (!(c instanceof JToggleButton)) {
				if (model.isRollover()) // && !( model.isPressed() && !model.isArmed()))
					super.paintBorder(c, g, x, y, w, h);
				return;
			}

			if (model.isSelected())
				WindowsUtils.drawPressed3DBorder(g, x, y, w, h);
			else if (model.isRollover()) {
				super.paintBorder(c, g, x, y, w, h);
				/*
				if (model.isPressed() && model.isArmed()) {
				ExtMetalUtils.drawPressed3DBorder(g, x, y, w, h);
				} else {
				ExtMetalUtils.drawFlush3DBorder(g, x, y, w, h);
				}*/
			}
		}
	}


    /**
     * A border which is like a Margin border but it will only honor the margin
     * if the margin has been explicitly set by the developer.
     */
    private static final class RolloverMarginBorder extends EmptyBorder {

        private RolloverMarginBorder() {
            super(1, 1, 1, 1);
        }


        public Insets getBorderInsets(Component c) {
            return getBorderInsets(c, new Insets(0, 0, 0, 0));
        }


        public Insets getBorderInsets(Component c, Insets insets) {
            Insets margin = null;

            if (c instanceof AbstractButton) {
                margin = ((AbstractButton) c).getMargin();
            }
            if (margin == null || margin instanceof UIResource) {
                // default margin so replace
                insets.left = left;
                insets.top = top;
                insets.right = right;
                insets.bottom = bottom;
            } else {
                // Margin which has been explicitly set by the user.
                insets.left = margin.left;
                insets.top = margin.top;
                insets.right = margin.right;
                insets.bottom = margin.bottom;
            }
            return insets;
        }
    }

	/**
	 * A border that looks like a separator line; used for menu bars and tool bars.
	 */
	private static final class SeparatorBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(0, 3, 2, 1);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			g.translate(x, y);
	  		g.setColor( UIManager.getColor("Separator.foreground"));
	  		g.drawLine( 0, h - 2, w - 1, h - 2 );

	  		g.setColor( UIManager.getColor("Separator.background"));
	  		g.drawLine( 0, h - 1, w - 1, h - 1 );
			g.translate(-x, -y);
		}

		public Insets getBorderInsets(Component c) { return INSETS; }
	}


	/**
	 * A thin raised border.
	 */
	static final class ThinRaisedBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(1, 1, 1, 1);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			WindowsUtils.drawFlush3DBorder(g, x, y, w, h);
		}

		public Insets getBorderInsets(Component c) { return INSETS; }
	}


	/**
	 * A thin lowered border.
	 */
	static final class ThinLoweredBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(1, 1, 1, 1);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			WindowsUtils.drawPressed3DBorder(g, x, y, w, h);
		}

		public Insets getBorderInsets(Component c) { return INSETS; }
	}


	/**
	 * A border used for menu bars and tool bars in <code>HeaderStyle.SINGLE</code>.
	 * The bar is wrapped by an inner thin raised border,
	 * which in turn is wrapped by an outer thin lowered border.
	 */
	private static final class EtchedBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(2, 2, 2, 2);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			WindowsUtils.drawPressed3DBorder(g, x, y, w, h);
			WindowsUtils.drawFlush3DBorder  (g, x + 1, y + 1, w - 2, h - 2);
		}

		public Insets getBorderInsets(Component c) { return INSETS; }
	}


	/**
	 * A border used for menu bars in <code>HeaderStyle.BOTH</code>.
	 * The menu bar and tool bar are wrapped by a thin raised border,
	 * both together are wrapped by a thin lowered border.
	 */
	private static final class MenuBarHeaderBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(2, 2, 1, 2);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			WindowsUtils.drawPressed3DBorder(g, x, y, w, h + 1);
			WindowsUtils.drawFlush3DBorder  (g, x + 1, y + 1, w - 2, h - 1);
		}

		public Insets getBorderInsets(Component c) { return INSETS; }
	}


    private static final class PopupMenuBorder extends AbstractBorder implements UIResource {

        private static final Insets INSETS = new Insets(3, 3, 3, 3);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor(UIManager.getColor("controlShadow"));
            g.drawRect(0, 0, w-1, h-1);
            g.setColor(UIManager.getColor("MenuItem.background"));
            g.drawRect(1, 1, w-3, h-3);
            g.drawRect(2, 2, w-5, h-5);
            g.translate(-x, -y);
        }

        public Insets getBorderInsets(Component c) { return INSETS; }
    }


    private static final class NoMarginPopupMenuBorder extends AbstractBorder implements UIResource {

        private static final Insets INSETS = new Insets(1, 1, 1, 1);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor(UIManager.getColor("controlShadow"));
            g.drawRect(0, 0, w-1, h-1);
//            g.setColor(UIManager.getColor("MenuItem.background"));
//            g.drawRect(1, 1, 0, h-3);
            g.translate(-x, -y);
        }

        public Insets getBorderInsets(Component c) { return INSETS; }
    }

	/**
	 * A border used for tool bars in <code>HeaderStyle.BOTH</code>.
	 * The menu bar and tool bar are wrapped by a thin raised border,
	 * both together are wrapped by a thin lowered border.
	 */
	private static final class ToolBarHeaderBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(1, 2, 2, 2);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			WindowsUtils.drawPressed3DBorder(g, x, y - 1, w, h + 1);
			WindowsUtils.drawFlush3DBorder  (g, x + 1, y, w - 2, h - 1);
		}

		public Insets getBorderInsets(Component c) { return INSETS; }
	}


	/**
	 * A border used for menus.
	 */
	private static final class MenuBorder extends AbstractBorder implements UIResource {

		private static final Insets INSETS = new Insets(1, 1, 1, 1);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			AbstractButton b = (AbstractButton) c;
			ButtonModel model = b.getModel();

			//System.out.println("rollover=" + model.isRollover());
			//if ((3 < 4) || model.isRollover()) { // && !(model.isPressed() && !model.isArmed())) {
			if (model.isSelected())
				WindowsUtils.drawPressed3DBorder(g, x, y, w, h);
			else if (model.isRollover())
				WindowsUtils.drawFlush3DBorder(g, x, y, w, h);
			//}
		}

		public Insets getBorderInsets(Component c) { return INSETS; }

	}


    // Helper Code **********************************************************************

	// Copied from BasicGraphicsUtils, has an additional color for the default rectangle.
    private static void drawBezel(Graphics g, int x, int y, int w, int h,
                                 boolean isPressed, boolean isDefault,
                                 Color shadow, Color darkShadow,
                                 Color highlight, Color lightHighlight, Color defaultColor)
    {
        Color oldColor = g.getColor();  // Make no net change to g
        g.translate(x, y);

        if (isPressed && isDefault) {
            g.setColor(darkShadow);
            g.drawRect(0, 0, w - 1, h - 1);
            g.setColor(shadow);
            g.drawRect(1, 1, w - 3, h - 3);
        } else if (isPressed) {
            BasicGraphicsUtils.drawLoweredBezel(g, x, y, w, h,
                             shadow, darkShadow, highlight, lightHighlight);
        } else if (isDefault) {
            g.setColor(defaultColor);
            g.drawRect(0, 0, w-1, h-1);

            g.setColor(lightHighlight);
            g.drawLine(1, 1, 1, h-3);
            g.drawLine(2, 1, w-3, 1);

            g.setColor(highlight);
            g.drawLine(2, 2, 2, h-4);
            g.drawLine(3, 2, w-4, 2);

            g.setColor(shadow);
            g.drawLine(2, h-3, w-3, h-3);
            g.drawLine(w-3, 2, w-3, h-4);

            g.setColor(darkShadow);
            g.drawLine(1, h-2, w-2, h-2);
            g.drawLine(w-2, h-2, w-2, 1);
        } else {
            g.setColor(lightHighlight);
            g.drawLine(0, 0, 0, h-1);
            g.drawLine(1, 0, w-2, 0);

            g.setColor(highlight);
            g.drawLine(1, 1, 1, h-3);
            g.drawLine(2, 1, w-3, 1);

            g.setColor(shadow);
            g.drawLine(1, h-2, w-2, h-2);
            g.drawLine(w-2, 1, w-2, h-3);

            g.setColor(darkShadow);
            g.drawLine(0, h-1, w-1, h-1);
            g.drawLine(w-1, h-1, w-1, 0);
        }
		g.translate(-x, -y);
		g.setColor(oldColor);
    }

}
