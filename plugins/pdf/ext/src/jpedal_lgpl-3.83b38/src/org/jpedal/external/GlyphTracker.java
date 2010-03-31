/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2009, IDRsolutions and Contributors.
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
* GlyphTracker.java
* ---------------
*/
package org.jpedal.external;

/**
 * allow user to recieve raw glyph data as generated
 */
public interface GlyphTracker {

    /**
     * pass user the low-level details
     * @param trm - the Trm matrix (x,y is Trm[2][0], Trm[2][1]), other values are width (usually Trm[0][0] unless
     * rotated when could be Trm[0][1]) and height (usually Trm[1][1] or sometimes Trm[1][0]) Trm is defined in PDF
     * specification
     * @param rawInt - value found in TJ command (usually encoded)
     * @param displayValue - unicode display value from rawInt
     * @param extractionValue - unicode display value from rawInt
     */
    void addGlyph(float[][] trm, int rawInt, String displayValue, String extractionValue);
}