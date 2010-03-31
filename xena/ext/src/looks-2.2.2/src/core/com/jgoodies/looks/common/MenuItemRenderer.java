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

package com.jgoodies.looks.common;

import java.awt.*;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import com.jgoodies.looks.Options;

/**
 * Renders and lays out menu items.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */

public class MenuItemRenderer {

    /*
     * Implementation note: The protected visibility prevents
     * the String value from being encrypted by the obfuscator.
     * An encrypted String key would break the client property lookup
     * in the #paint method below.
     */
    protected static final String HTML_KEY = BasicHTML.propertyKey;

    /* Client Property keys for text and accelerator text widths */
    static final String MAX_TEXT_WIDTH  = "maxTextWidth";
	static final String MAX_ACC_WIDTH   = "maxAccWidth";

	private static final Icon   NO_ICON = new NullIcon();


    static Rectangle zeroRect		= new Rectangle(0, 0, 0, 0);
    static Rectangle iconRect		= new Rectangle();
    static Rectangle textRect		= new Rectangle();
    static Rectangle acceleratorRect= new Rectangle();
    static Rectangle checkIconRect	= new Rectangle();
    static Rectangle arrowIconRect	= new Rectangle();
    static Rectangle viewRect		= new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);
    static Rectangle r				= new Rectangle();


	private final JMenuItem	menuItem;
	private final boolean	iconBorderEnabled;  // when selected or pressed.
	private final Font		acceleratorFont;
	private final Color		selectionForeground;
	private final Color		disabledForeground;
	private final Color		acceleratorForeground;
	private final Color		acceleratorSelectionForeground;

	private final String	acceleratorDelimiter;
	private final Icon	  	fillerIcon;



	/**
	 * Constructs a MenuItemRenderer for the specified menu item and settings.
	 */
	public MenuItemRenderer(JMenuItem menuItem, boolean iconBorderEnabled,
		Font 	acceleratorFont,
		Color	selectionForeground,
		Color	disabledForeground,
		Color	acceleratorForeground,
		Color	acceleratorSelectionForeground) {
		this.menuItem				= menuItem;
		this.iconBorderEnabled		= iconBorderEnabled;
		this.acceleratorFont		= acceleratorFont;
		this.selectionForeground	= selectionForeground;
		this.disabledForeground	= disabledForeground;
		this.acceleratorForeground	= acceleratorForeground;
		this.acceleratorSelectionForeground = acceleratorSelectionForeground;
		this.acceleratorDelimiter	= UIManager.getString("MenuItem.acceleratorDelimiter");
		this.fillerIcon			= new MinimumSizedIcon();
	}


	/**
	 * Looks up and answers the appropriate menu item icon.
	 */
	private Icon getIcon(JMenuItem aMenuItem, Icon defaultIcon) {
		Icon icon = aMenuItem.getIcon();
		if (icon == null)
			return defaultIcon;

		ButtonModel model = aMenuItem.getModel();
		if (!model.isEnabled()) {
			return model.isSelected()
						? aMenuItem.getDisabledSelectedIcon()
						: aMenuItem.getDisabledIcon();
		} else if (model.isPressed() && model.isArmed()) {
			Icon pressedIcon = aMenuItem.getPressedIcon();
			return pressedIcon != null ? pressedIcon : icon;
		} else if (model.isSelected()) {
			Icon selectedIcon = aMenuItem.getSelectedIcon();
			return selectedIcon != null ? selectedIcon : icon;
		} else
			return icon;
	}


	/**
	 * Checks and answers if the menu item has a custom icon.
	 */
	private boolean hasCustomIcon() {
		return getIcon(menuItem, null) != null;
	}


	/**
	 * Answers the wrapped icon.
	 */
	private Icon getWrappedIcon(Icon icon) {
		if (hideIcons())
			return NO_ICON;
		if (icon == null)
			return fillerIcon;
		return iconBorderEnabled && hasCustomIcon()
			? new MinimumSizedCheckIcon(icon, menuItem)
			: new MinimumSizedIcon(icon);
	}


	private void resetRects() {
		iconRect.setBounds(zeroRect);
		textRect.setBounds(zeroRect);
		acceleratorRect.setBounds(zeroRect);
		checkIconRect.setBounds(zeroRect);
		arrowIconRect.setBounds(zeroRect);
		viewRect.setBounds(0, 0, Short.MAX_VALUE, Short.MAX_VALUE);
		r.setBounds(zeroRect);
	}


	public Dimension getPreferredMenuItemSize(JComponent c,
		Icon checkIcon, Icon arrowIcon, int defaultTextIconGap) {

		JMenuItem b = (JMenuItem) c;
		String text = b.getText();
		KeyStroke accelerator = b.getAccelerator();
		String acceleratorText = "";

		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
				acceleratorText += acceleratorDelimiter;
			}
			int keyCode = accelerator.getKeyCode();
			if (keyCode != 0) {
				acceleratorText += KeyEvent.getKeyText(keyCode);
			} else {
				acceleratorText += accelerator.getKeyChar();
			}
		}

		Font font = b.getFont();
		FontMetrics fm		= b.getFontMetrics(font);
		FontMetrics fmAccel = b.getFontMetrics(acceleratorFont);

		resetRects();

		Icon wrappedIcon = getWrappedIcon(getIcon(menuItem, checkIcon));
        Icon wrappedArrowIcon = new MinimumSizedIcon(arrowIcon);
        Icon icon = wrappedIcon.getIconHeight() > fillerIcon.getIconHeight()
                    ? wrappedIcon
                    : null;

		layoutMenuItem(fm, text, fmAccel, acceleratorText,
			//icon, checkIcon,
            icon, wrappedIcon,
			wrappedArrowIcon, //arrowIcon,
			b.getVerticalAlignment(), b.getHorizontalAlignment(),
			b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
			viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect,
			text == null ? 0 : defaultTextIconGap,
			defaultTextIconGap);
		// find the union of the icon and text rects
		r.setBounds(textRect);
		r = SwingUtilities.computeUnion(iconRect.x, iconRect.y, iconRect.width, iconRect.height, r);
		//   r = iconRect.union(textRect);

		// To make the accelerator texts appear in a column, find the widest MenuItem text
		// and the widest accelerator text.

		//Get the parent, which stores the information.
		Container parent = menuItem.getParent();

		//Check the parent, and see that it is not a top-level menu.
		if (parent != null
			&& parent instanceof JComponent
			&& !(menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu())) {
			JComponent p = (JComponent) parent;

			//Get widest text so far from parent, if no one exists null is returned.
			Integer maxTextWidth = (Integer) p.getClientProperty(MAX_TEXT_WIDTH);
			Integer maxAccWidth  = (Integer) p.getClientProperty(MAX_ACC_WIDTH);

			int maxTextValue = maxTextWidth != null ? maxTextWidth.intValue() : 0;
			int maxAccValue  = maxAccWidth  != null ? maxAccWidth.intValue()  : 0;

			//Compare the text widths, and adjust the r.width to the widest.
			if (r.width < maxTextValue) {
				r.width = maxTextValue;
			} else {
				p.putClientProperty(MAX_TEXT_WIDTH, new Integer(r.width));
			}

			//Compare the accelarator widths.
			if (acceleratorRect.width > maxAccValue) {
				maxAccValue = acceleratorRect.width;
				p.putClientProperty(MAX_ACC_WIDTH, new Integer(acceleratorRect.width));
			}

			//Add on the widest accelerator
			r.width += maxAccValue;
			r.width += 10;
		}

		if (useCheckAndArrow()) {
			// Add in the checkIcon
			r.width += checkIconRect.width;
			r.width += defaultTextIconGap;

			// Add in the arrowIcon
			r.width += defaultTextIconGap;
			r.width += arrowIconRect.width;
		}

		r.width += 2 * defaultTextIconGap;

		Insets insets = b.getInsets();
		if (insets != null) {
			r.width  += insets.left + insets.right;
			r.height += insets.top  + insets.bottom;
		}

		// if the width is even, bump it up one. This is critical
		// for the focus dash line to draw properly
		/* JGoodies: Can't believe the above
		 * if(r.width%2 == 0) {
		    r.width++;
		}*/

		// if the height is even, bump it up one. This is critical
		// for the text to center properly
		// JGoodies: An even height is critical to center icons properly
		if (r.height % 2 == 1) {
		    r.height++;
		}
		return r.getSize();
	}


	public void paintMenuItem(Graphics g, JComponent c,
		Icon checkIcon, Icon arrowIcon,
		Color background, Color foreground, int defaultTextIconGap) {
		JMenuItem b = (JMenuItem) c;
		ButtonModel model = b.getModel();

		//   Dimension size = b.getSize();
		int menuWidth  = b.getWidth();
		int menuHeight = b.getHeight();
		Insets i = c.getInsets();

		resetRects();

		viewRect.setBounds(0, 0, menuWidth, menuHeight);

		viewRect.x += i.left;
		viewRect.y += i.top;
		viewRect.width  -= (i.right + viewRect.x);
		viewRect.height -= (i.bottom + viewRect.y);

		Font holdf = g.getFont();
		Font f = c.getFont();
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics(f);
		FontMetrics fmAccel = g.getFontMetrics(acceleratorFont);

		// get Accelerator text
		KeyStroke accelerator = b.getAccelerator();
		String acceleratorText = "";
		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
				acceleratorText += acceleratorDelimiter;
			}

			int keyCode = accelerator.getKeyCode();
			if (keyCode != 0) {
				acceleratorText += KeyEvent.getKeyText(keyCode);
			} else {
				acceleratorText += accelerator.getKeyChar();
			}
		}

		Icon wrappedIcon = getWrappedIcon(getIcon(menuItem, checkIcon));
		Icon wrappedArrowIcon = new MinimumSizedIcon(arrowIcon);

		// layout the text and icon
		String text = layoutMenuItem(fm, b.getText(), fmAccel, acceleratorText,
				// b.getIcon(), checkIcon,
				null, wrappedIcon,
				wrappedArrowIcon, //arrowIcon,
				b.getVerticalAlignment(), b.getHorizontalAlignment(),
				b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
				viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect,
				b.getText() == null ? 0 : defaultTextIconGap,
				defaultTextIconGap);

		// Paint background
		paintBackground(g, b, background);

		// Paint icon
		Color holdc = g.getColor();
		if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
			g.setColor(foreground);
		}
		wrappedIcon.paintIcon(c, g, checkIconRect.x, checkIconRect.y);
		g.setColor(holdc);


		// Draw the Text
		if (text != null) {
			View v = (View) c.getClientProperty(HTML_KEY);
			if (v != null) {
				v.paint(g, textRect);
			} else {
				paintText(g, b, textRect, text);
			}
		}

		// Draw the Accelerator Text
		if (acceleratorText != null && !acceleratorText.equals("")) {

			//Get the maxAccWidth from the parent to calculate the offset.
			int accOffset = 0;
			Container parent = menuItem.getParent();
			if (parent != null && parent instanceof JComponent) {
				JComponent p = (JComponent) parent;
				Integer maxValueInt = (Integer) p.getClientProperty(MAX_ACC_WIDTH);
				int maxValue = maxValueInt != null ? maxValueInt.intValue() : acceleratorRect.width;

				//Calculate the offset, with which the accelerator texts will be drawn with.
                accOffset = isLeftToRight(menuItem)
                    ? maxValue - acceleratorRect.width
                    : acceleratorRect.width - maxValue;
			}

			g.setFont(acceleratorFont);
			if (!model.isEnabled()) {
				// *** paint the acceleratorText disabled
				if (!disabledTextHasShadow()) {
					g.setColor(disabledForeground);
                    RenderingUtils.drawStringUnderlineCharAt(c, g, acceleratorText, -1,
						acceleratorRect.x - accOffset,
						acceleratorRect.y + fmAccel.getAscent());
				} else {
					g.setColor(b.getBackground().brighter());
                    RenderingUtils.drawStringUnderlineCharAt(c, g, acceleratorText, -1,
						acceleratorRect.x - accOffset,
						acceleratorRect.y + fmAccel.getAscent());
					g.setColor(b.getBackground().darker());
                    RenderingUtils.drawStringUnderlineCharAt(c, g, acceleratorText, -1,
						acceleratorRect.x - accOffset - 1,
						acceleratorRect.y + fmAccel.getAscent() - 1);
				}
			} else {
				// *** paint the acceleratorText normally
				if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
					g.setColor(acceleratorSelectionForeground);
				} else {
					g.setColor(acceleratorForeground);
				}
                RenderingUtils.drawStringUnderlineCharAt(c, g, acceleratorText, -1,
					acceleratorRect.x - accOffset,
					acceleratorRect.y + fmAccel.getAscent());
			}
		}

		// Paint the Arrow
		if (arrowIcon != null) {
			if (model.isArmed() || (c instanceof JMenu && model.isSelected()))
				g.setColor(foreground);
			if (useCheckAndArrow())
				wrappedArrowIcon.paintIcon(c, g, arrowIconRect.x, arrowIconRect.y);
		}
		g.setColor(holdc);
		g.setFont(holdf);
	}

    /**
     * Compute and return the location of the icons origin, the
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewRect rectangle.
     */
	private String layoutMenuItem(FontMetrics fm, String text,
		FontMetrics fmAccel, String acceleratorText,
		Icon icon, Icon checkIcon, Icon arrowIcon,
		int verticalAlignment, int horizontalAlignment,
		int verticalTextPosition, int horizontalTextPosition,
		Rectangle viewRectangle,
		Rectangle iconRectangle,
		Rectangle textRectangle,
		Rectangle acceleratorRectangle,
		Rectangle checkIconRectangle,
		Rectangle arrowIconRectangle,
		int textIconGap,
		int menuItemGap) {

		SwingUtilities.layoutCompoundLabel(menuItem, fm, text, icon,
			verticalAlignment, horizontalAlignment,
			verticalTextPosition, horizontalTextPosition,
			viewRectangle, iconRectangle, textRectangle, textIconGap);

		/* Initialize the acceleratorText bounds rectangle textRect.  If a null
		 * or and empty String was specified we substitute "" here
		 * and use 0,0,0,0 for acceleratorTextRect.
		 */
		if ((acceleratorText == null) || acceleratorText.equals("")) {
			acceleratorRectangle.width = acceleratorRectangle.height = 0;
			acceleratorText = "";
		} else {
			acceleratorRectangle.width  = SwingUtilities.computeStringWidth(fmAccel, acceleratorText);
			acceleratorRectangle.height = fmAccel.getHeight();
		}

		boolean useCheckAndArrow = useCheckAndArrow();

		// Initialize the checkIcon bounds rectangle's width & height.

		if (useCheckAndArrow) {
			if (checkIcon != null) {
				checkIconRectangle.width  = checkIcon.getIconWidth();
				checkIconRectangle.height = checkIcon.getIconHeight();
			} else {
				checkIconRectangle.width = checkIconRectangle.height = 0;
			}

			// Initialize the arrowIcon bounds rectangle width & height.

			if (arrowIcon != null) {
				arrowIconRectangle.width  = arrowIcon.getIconWidth();
				arrowIconRectangle.height = arrowIcon.getIconHeight();
			} else {
				arrowIconRectangle.width = arrowIconRectangle.height = 0;
			}
		}

		Rectangle labelRect = iconRectangle.union(textRectangle);
		if (isLeftToRight(menuItem)) {
			textRectangle.x += menuItemGap;
			iconRectangle.x += menuItemGap;

			// Position the Accelerator text rect
			acceleratorRectangle.x = viewRectangle.x
					+ viewRectangle.width
					- arrowIconRectangle.width
					- menuItemGap
					- acceleratorRectangle.width;

			// Position the Check and Arrow Icons
			if (useCheckAndArrow) {
				checkIconRectangle.x = viewRectangle.x; // + menuItemGap;  JGoodies: No leading gap
				textRectangle.x += menuItemGap + checkIconRectangle.width;
				iconRectangle.x += menuItemGap + checkIconRectangle.width;
				arrowIconRectangle.x = viewRectangle.x + viewRectangle.width - menuItemGap - arrowIconRectangle.width;
			}
		} else {
			textRectangle.x -= menuItemGap;
			iconRectangle.x -= menuItemGap;

			// Position the Accelerator text rect
			acceleratorRectangle.x = viewRectangle.x + arrowIconRectangle.width + menuItemGap;

			// Position the Check and Arrow Icons
			if (useCheckAndArrow) {
				// JGoodies: No trailing gap
				checkIconRectangle.x = viewRectangle.x + viewRectangle.width - checkIconRectangle.width;
				textRectangle.x -= menuItemGap + checkIconRectangle.width;
				iconRectangle.x -= menuItemGap + checkIconRectangle.width;
				arrowIconRectangle.x = viewRectangle.x + menuItemGap;
			}
		}

		// Align the accelerator text and the check and arrow icons vertically
		// with the center of the label rect.
		acceleratorRectangle.y = labelRect.y + (labelRect.height / 2) - (acceleratorRectangle.height / 2);
		if (useCheckAndArrow) {
			arrowIconRectangle.y = labelRect.y + (labelRect.height / 2) - (arrowIconRectangle.height / 2);
			checkIconRectangle.y = labelRect.y + (labelRect.height / 2) - (checkIconRectangle.height / 2);
		}

		/*
		System.out.println("Layout: text="+menuItem.getText()+"\n\tv="
		                   +viewRect+"\n\tc="+checkIconRect+"\n\ti="
		                   +iconRect+"\n\tt="+textRect+"\n\tacc="
		                   +acceleratorRect+"\n\ta="+arrowIconRect+"\n");
		*/

		return text;
	}

    /*
     * Returns false if the component is a JMenu and it is a top
     * level menu (on the menubar).
     */
	private boolean useCheckAndArrow() {
		boolean isTopLevelMenu = menuItem instanceof JMenu &&
								 ((JMenu) menuItem).isTopLevelMenu();
		return !isTopLevelMenu;
	}


	private boolean isLeftToRight(Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }


	// Copies from 1.4.1 ****************************************************


    /**
     * Draws the background of the menu item.
     * Copied from 1.4.1 BasicMenuItem to make it visible to the
     * MenuItemLayouter
     *
     * @param g the paint graphics
     * @param aMenuItem menu item to be painted
     * @param bgColor selection background color
     * @since 1.4
     */
    private void paintBackground(Graphics g, JMenuItem aMenuItem, Color bgColor) {
		ButtonModel model = aMenuItem.getModel();

		if (aMenuItem.isOpaque()) {
            int menuWidth  = aMenuItem.getWidth();
            int menuHeight = aMenuItem.getHeight();
            Color c = model.isArmed() ||
                     (aMenuItem instanceof JMenu && model.isSelected())
                       ? bgColor
                       : aMenuItem.getBackground();
			Color oldColor = g.getColor();
            g.setColor(c);
            g.fillRect(0, 0, menuWidth, menuHeight);
			g.setColor(oldColor);
		}
    }


    /**
     * Renders the text of the current menu item.
     * <p>
     * @param g graphics context
     * @param aMenuItem menu item to render
     * @param textRectangle bounding rectangle for rendering the text
     * @param text string to render
     * @since 1.4
     */
    private void paintText(Graphics g, JMenuItem aMenuItem, Rectangle textRectangle, String text) {
		ButtonModel model = aMenuItem.getModel();
		FontMetrics fm = g.getFontMetrics();
		int mnemIndex = aMenuItem.getDisplayedMnemonicIndex();
        if (isMnemonicHidden()) {
            mnemIndex = -1;
        }

		if (!model.isEnabled()) {
		    if (!disabledTextHasShadow()) {
                // *** paint the text disabled
    			g.setColor(UIManager.getColor("MenuItem.disabledForeground"));
                RenderingUtils.drawStringUnderlineCharAt(aMenuItem, g, text, mnemIndex,
    						      textRectangle.x,
    						      textRectangle.y + fm.getAscent());
		    } else {
                // *** paint the text disabled with a shadow
    			g.setColor(aMenuItem.getBackground().brighter());
                RenderingUtils.drawStringUnderlineCharAt(aMenuItem, g, text, mnemIndex,
    						      textRectangle.x,
    						      textRectangle.y + fm.getAscent());
    			g.setColor(aMenuItem.getBackground().darker());
                RenderingUtils.drawStringUnderlineCharAt(aMenuItem, g, text, mnemIndex,
    						      textRectangle.x - 1,
    						      textRectangle.y + fm.getAscent() - 1);
		    }
		} else {
		    // *** paint the text normally
		    if (model.isArmed()|| (aMenuItem instanceof JMenu && model.isSelected())) {
		        g.setColor(selectionForeground); // Uses protected field.
		    }
            RenderingUtils.drawStringUnderlineCharAt(aMenuItem, g, text, mnemIndex,
						  textRectangle.x,
						  textRectangle.y + fm.getAscent());
		}
    }


    protected boolean isMnemonicHidden() {
        return false;
    }

    protected boolean disabledTextHasShadow() {
        return false;
    }


    /**
     * Checks and answers if the parent menu indicates that we should use no icons.
     */
    private boolean hideIcons() {
        Component parent = menuItem.getParent();
        if (!(parent instanceof JPopupMenu)) {
            return false;
        }
    	JPopupMenu popupMenu = (JPopupMenu) parent;
    	Object value = popupMenu.getClientProperty(Options.NO_ICONS_KEY);
    	if (value == null) {
	    	Component invoker = popupMenu.getInvoker();
	    	if (invoker != null && invoker instanceof JMenu)
		    	value = ((JMenu) invoker).getClientProperty(Options.NO_ICONS_KEY);
    	}
    	return Boolean.TRUE.equals(value);
    }


	/**
     * Used as a placeholder if icons are disabled.
     */
    private static class NullIcon implements Icon {
    	public int getIconWidth()	{ return 0; }
    	public int getIconHeight() { return 0; }
    	public void paintIcon(Component c, Graphics g, int x, int y) {
            // The NullIcon doesn't paint anything.
        }
    }



}