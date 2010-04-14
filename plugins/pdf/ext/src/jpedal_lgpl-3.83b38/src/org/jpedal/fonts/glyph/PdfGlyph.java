/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * PdfGlyph.java
 * ---------------
*/
package org.jpedal.fonts.glyph;

import java.awt.*;
import java.awt.geom.Area;

import org.jpedal.color.PdfPaint;

/**
 * base glyph used by T1 and Truetype fonts
 */
public interface PdfGlyph {
	

    public static int FontBB_X=1;
    public static int FontBB_Y=2;
    public static int FontBB_WIDTH=3;
    public static int FontBB_HEIGHT=4;

    /**draw the glyph*/
	public abstract void render(int text_fill_type, Graphics2D g2, float scaling);

	/**
	 * return max possible glyph width in absolute units
	 */
	public abstract float getmaxWidth();

	/**
	 * return max possible glyph width in absolute units
	 */
	public abstract int getmaxHeight();

	/**
	 * used by type3 glyphs to set colour if required
	 */
	public abstract void lockColors(PdfPaint strokeColor, PdfPaint nonstrokeColor);

	/**
	 * see if we ignore colours for type 3 font
	 */
	public abstract boolean ignoreColors();

	public abstract Area getShape();

    //used by Type3 fonts
    String getGlyphName();

	public abstract void setWidth(float width);

    /**
     * retrun fontBounds paramter where type is a contant in PdfGlyh
     * @param type
     * @return
     */
    int getFontBB(int type);

    void setStrokedOnly(boolean b);
}