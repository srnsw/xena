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

import java.awt.*;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Consists exclusively of static methods that provide convenience behavior.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */

public final class PlasticUtils {

    private PlasticUtils() {
        // Overrides default constructor; prevents instantiation.
    }


	static void drawDark3DBorder(Graphics g, int x, int y, int w, int h) {
		drawFlush3DBorder(g, x, y, w, h);
		g.setColor(PlasticLookAndFeel.getControl());
		g.drawLine(x+1, y+1, 1, h - 3);
		g.drawLine(y+1, y+1, w - 3, 1);
	}


	static void drawDisabledBorder(Graphics g, int x, int y, int w, int h) {
		g.setColor(PlasticLookAndFeel.getControlShadow());
		drawRect(g, x, y, w - 1, h - 1);
	}


	/*
	 * Unlike <code>MetalUtils</code> we first draw with highlight then dark shadow
	 */
	static void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(PlasticLookAndFeel.getControlHighlight());
		drawRect(g, 1, 1, w - 2, h - 2);
		g.drawLine(0, h - 1, 0, h - 1);
		g.drawLine(w - 1, 0, w - 1, 0);
		g.setColor(PlasticLookAndFeel.getControlDarkShadow());
		drawRect(g, 0, 0, w - 2, h - 2);
		g.translate(-x, -y);
	}


	/*
	 * Copied from <code>MetalUtils</code>.
	 */
	static void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		drawFlush3DBorder(g, 0, 0, w, h);
		g.setColor(PlasticLookAndFeel.getControlShadow());
		g.drawLine(1, 1, 1, h - 3);
		g.drawLine(1, 1, w - 3, 1);
		g.translate(-x, -y);
	}


	/*
	 * Copied from <code>MetalUtils</code>.
	 */
    static void drawButtonBorder(Graphics g, int x, int y, int w, int h, boolean active) {
        if (active) {
            drawActiveButtonBorder(g, x, y, w, h);
        } else {
            drawFlush3DBorder(g, x, y, w, h);
		}
    }

	/*
	 * Copied from <code>MetalUtils</code>.
	 */
    static void drawActiveButtonBorder(Graphics g, int x, int y, int w, int h) {
        drawFlush3DBorder(g, x, y, w, h);
        g.setColor( PlasticLookAndFeel.getPrimaryControl() );
		g.drawLine( x+1, y+1, x+1, h-3 );
		g.drawLine( x+1, y+1, w-3, x+1 );
        g.setColor( PlasticLookAndFeel.getPrimaryControlDarkShadow() );
		g.drawLine( x+2, h-2, w-2, h-2 );
		g.drawLine( w-2, y+2, w-2, h-2 );
    }

	/*
	 * Modified edges.
	 */
    static void drawDefaultButtonBorder(Graphics g, int x, int y, int w, int h, boolean active) {
        drawButtonBorder(g, x+1, y+1, w-1, h-1, active);
        g.translate(x, y);
        g.setColor(PlasticLookAndFeel.getControlDarkShadow() );
		drawRect(g, 0, 0, w-3, h-3 );
		g.drawLine(w-2, 0, w-2, 0);
		g.drawLine(0, h-2, 0, h-2);
		g.setColor(PlasticLookAndFeel.getControl());
		g.drawLine(w-1, 0, w-1, 0);
		g.drawLine(0, h-1, 0, h-1);
        g.translate(-x, -y);
    }

    static void drawDefaultButtonPressedBorder(Graphics g, int x, int y, int w, int h) {
        drawPressed3DBorder(g, x + 1, y + 1, w - 1, h - 1);
        g.translate(x, y);
        g.setColor(PlasticLookAndFeel.getControlDarkShadow());
        drawRect(g, 0, 0, w - 3, h - 3);
        g.drawLine(w - 2, 0, w - 2, 0);
        g.drawLine(0, h - 2, 0, h - 2);
        g.setColor(PlasticLookAndFeel.getControl());
        g.drawLine(w - 1, 0, w - 1, 0);
        g.drawLine(0, h - 1, 0, h - 1);
        g.translate(-x, -y);
    }

	static void drawThinFlush3DBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(PlasticLookAndFeel.getControlHighlight());
		g.drawLine(0, 0, w - 2, 0);
		g.drawLine(0, 0, 0, h - 2);
		g.setColor(PlasticLookAndFeel.getControlDarkShadow());
		g.drawLine(w - 1, 0, w - 1, h - 1);
		g.drawLine(0, h - 1, w - 1, h - 1);
		g.translate(-x, -y);
	}


	static void drawThinPressed3DBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(PlasticLookAndFeel.getControlDarkShadow());
		g.drawLine(0, 0, w - 2, 0);
		g.drawLine(0, 0, 0, h - 2);
		g.setColor(PlasticLookAndFeel.getControlHighlight());
		g.drawLine(w - 1, 0, w - 1, h - 1);
		g.drawLine(0, h - 1, w - 1, h - 1);
		g.translate(-x, -y);
	}

	/*
	 * Convenience function for determining ComponentOrientation.  Helps us
	 * avoid having Munge directives throughout the code.
	 */
	static boolean isLeftToRight(Component c) {
		return c.getComponentOrientation().isLeftToRight();
	}


	// 3D Effects ***********************************************************************

	/**
	 * Checks and returns whether the specified component type has 3D effects.
     *
     * @param keyPrefix    the prefix of the key used to lookup the setting
     * @return true if the component type shall be rendered with a 3D effect
     * @see #force3D(JComponent)
     * @see #forceFlat(JComponent)
	 */
	static boolean is3D(String keyPrefix) {
		Object value = UIManager.get(keyPrefix + "is3DEnabled");
		return Boolean.TRUE.equals(value);
	}


	/**
	 * Checks and returns whether we have a custom hint that forces the 3D mode.
	 *
     * @param c   the component to inspect
     * @return true if the given component has a 3D hint set
	 * @see #forceFlat(JComponent)
	 */
	static boolean force3D(JComponent c) {
		Object value = c.getClientProperty(PlasticLookAndFeel.IS_3D_KEY);
		return Boolean.TRUE.equals(value);
	}


	/**
	 * Checks and returns whether we have a custom hint that prevents the 3D mode.
	 *
     * @param c   the component to inspect
     * @return true if the given component has a flat hint set
	 * @see #force3D(JComponent)
	 */
	static boolean forceFlat(JComponent c) {
		Object value = c.getClientProperty(PlasticLookAndFeel.IS_3D_KEY);
		return Boolean.FALSE.equals(value);
	}


	// Painting 3D Effects *************************************************************

	private static final float FRACTION_3D = 0.5f;


	private static void add3DEffekt(Graphics g, Rectangle r, boolean isHorizontal,
		Color startC0, Color stopC0, Color startC1, Color stopC1) {

		Graphics2D g2 = (Graphics2D) g;
		int xb0, yb0, xb1, yb1, xd0, yd0, xd1, yd1, width, height;
		if (isHorizontal) {
			width = r.width;
			height = (int) (r.height * FRACTION_3D);
			xb0 = r.x;
			yb0 = r.y;
			xb1 = xb0;
			yb1 = yb0 + height;
			xd0 = xb1;
			yd0 = yb1;
			xd1 = xd0;
			yd1 = r.y + r.height;
		} else {
			width = (int) (r.width * FRACTION_3D);
			height = r.height;
			xb0 = r.x;
			yb0 = r.y;
			xb1 = xb0 + width;
			yb1 = yb0;
			xd0 = xb1;
			yd0 = yb0;
			xd1 = r.x + r.width;
			yd1 = yd0;
		}
		g2.setPaint(new GradientPaint(xb0, yb0, stopC0, xb1, yb1, startC0));
		g2.fillRect(r.x, r.y, width, height);
		g2.setPaint(new GradientPaint(xd0, yd0, startC1, xd1, yd1, stopC1));
		g2.fillRect(xd0, yd0, width, height);
	}


	static void add3DEffekt(Graphics g, Rectangle r) {
		Color brightenStop = UIManager.getColor("Plastic.brightenStop");
		if (null == brightenStop)
			brightenStop = PlasticTheme.BRIGHTEN_STOP;

		// Add round sides
		Graphics2D g2 = (Graphics2D) g;
		int border = 10;
		g2.setPaint(new GradientPaint(r.x, r.y, brightenStop, r.x + border, r.y, PlasticTheme.BRIGHTEN_START));
		g2.fillRect(r.x, r.y, border, r.height);
		int x = r.x + r.width -border;
		int y = r.y;
		g2.setPaint(new GradientPaint(x, y, PlasticTheme.DARKEN_START, x + border, y, PlasticTheme.LT_DARKEN_STOP));
		g2.fillRect(x, y, border, r.height);

		add3DEffekt(g, r, true, PlasticTheme.BRIGHTEN_START, brightenStop, PlasticTheme.DARKEN_START, PlasticTheme.LT_DARKEN_STOP);
	}


	static void addLight3DEffekt(Graphics g, Rectangle r, boolean isHorizontal) {
		Color ltBrightenStop = UIManager.getColor("Plastic.ltBrightenStop");
		if (null == ltBrightenStop)
			ltBrightenStop = PlasticTheme.LT_BRIGHTEN_STOP;

		add3DEffekt(g, r, isHorizontal, PlasticTheme.BRIGHTEN_START, ltBrightenStop, PlasticTheme.DARKEN_START, PlasticTheme.LT_DARKEN_STOP);
	}


	/*
	 * TODO: Required by the Chartster and JPathReport Filler; move to a
	 * FillerUI.
	 */
	public static void addLight3DEffekt(Graphics g, Rectangle r) {
		Color ltBrightenStop = UIManager.getColor("Plastic.ltBrightenStop");
		if (null == ltBrightenStop)
			ltBrightenStop = PlasticTheme.LT_BRIGHTEN_STOP;

		add3DEffekt(g, r, true, PlasticTheme.DARKEN_START, PlasticTheme.LT_DARKEN_STOP, PlasticTheme.BRIGHTEN_START, ltBrightenStop);
	}


    // Low level graphics ***************************************************

    /*
     * An optimized version of Graphics.drawRect.
     */
    private static void drawRect(Graphics g, int x, int y, int w, int h) {
        g.fillRect(x,   y,   w+1, 1);
        g.fillRect(x,   y+1, 1,   h);
        g.fillRect(x+1, y+h, w,   1);
        g.fillRect(x+w, y+1, 1,   h);
    }


}