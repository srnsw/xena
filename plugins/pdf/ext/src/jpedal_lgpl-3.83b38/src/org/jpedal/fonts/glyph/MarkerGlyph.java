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
 * MarkerGlyph.java
 * ---------------
*/
package org.jpedal.fonts.glyph;

import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.color.PdfPaint;

import java.awt.*;
import java.awt.geom.Area;
import java.io.Serializable;


/**
 * holds data so we can draw glyph on first appearance
 *
 */
public class MarkerGlyph implements PdfGlyph, Serializable {


    public float a,b,c,d;
    public String fontName;
	
	public MarkerGlyph(float a,float b, float c, float d,String fontName) {

		this.a=a;
		this.b=b;
		this.c=c;                                                  
		this.d=d;
		this.fontName=fontName;
		
	}
	
   public int getID() {
        return -1;
    }

    public void setID(int id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void render(int text_fill_type, Graphics2D g2, float scaling) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public float getmaxWidth() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getmaxHeight() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void lockColors(PdfPaint strokeColor, PdfPaint nonstrokeColor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean ignoreColors() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Area getShape() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //used by Type3 fonts
    public String getGlyphName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

	public void setWidth(float width) {
		// TODO Auto-generated method stub
		
	}

    public int getFontBB(int type) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStrokedOnly(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
