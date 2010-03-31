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
 * GlyphFactory.java
 * ---------------
*/
package org.jpedal.fonts.glyph;


/**
 * template for glyph creation routines
 */
public interface GlyphFactory {
    
    /**
     * set fontMatrix and zero all arrays
     */
    void reinitialise(double[] fontMatrix);

    /**
     * @return
     */
    PdfGlyph getGlyph(boolean debug);

    /**
     * @param f
     * @param g
     * @param h
     * @param i
     * @param j
     * @param k
     */
    void curveTo(float f, float g, float h, float i, float j, float k);

    /**
     * 
     */
    void closePath();

    /**
     * @param f
     * @param g
     */
    void moveTo(float f, float g);

    /**
     * @param f
     * @param g
     */
    void lineTo(float f, float g);

    /**
     * @param f
     * @param g
     */
    void setYMin(float f, float g);

    int getLSB();

}
