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
* T3Glyphs.java
* ---------------
*/
package org.jpedal.fonts.glyph;

import org.jpedal.fonts.glyph.GlyphFactory;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.T3Glyph;

import java.util.Map;
import java.util.Hashtable;

public class T3Glyphs extends PdfJavaGlyphs {

	/**holds lookup to type 3 charProcs*/
	private PdfGlyph[] charProcs = new PdfGlyph[256];

	/**
	 * template used by t1/tt fonts
	 */
	public PdfGlyph getEmbeddedGlyph(GlyphFactory factory, String glyph, float[][] Trm, int rawInt, String displayValue, float currentWidth, String key) {

		/**flush cache if needed*/
		if((lastTrm[0][0]!=Trm[0][0])|(lastTrm[1][0]!=Trm[1][0])|
				(lastTrm[0][1]!=Trm[0][1])|(lastTrm[1][1]!=Trm[1][1])){
			lastTrm=Trm;
			flush();
		}

		//either calculate the glyph to draw or reuse if alreasy drawn
		PdfGlyph transformedGlyph2 = getEmbeddedCachedShape(rawInt);

		double dY = -1,dX=1;

		//allow for text running up the page
		if (((Trm[1][0] < 0)&(Trm[0][1] >= 0))|((Trm[0][1] < 0)&(Trm[1][0] >= 0))) {
			dX=1f;
			dY =-1f;

		}

		/**else if ((Trm[1][0] < 0)|(Trm[0][1] < 0)) {
		 dX=-dX;
		 dY =-dY;
		 } */

		if (transformedGlyph2 == null) {

			//shape to draw onto
			transformedGlyph2=charProcs[rawInt];

			//save so we can reuse if it occurs again in this TJ command
			setEmbeddedCachedShape(rawInt, transformedGlyph2);

		}

		//System.out.println(transformedGlyph2);
		return transformedGlyph2;
	}

   // make key Integer and don't lookup Glyph on T3 and make charProcs an array with int key
    public void setT3Glyph(int key, int altKey, PdfGlyph glyph) {

       charProcs[key]=glyph;
       
       if(altKey!=-1 && charProcs[altKey]==null)
    	   charProcs[altKey]=glyph;

       //debug code
//       BufferedImage img=new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
//       Graphics2D g2=img.createGraphics();
//       g2.setPaint(Color.GREEN);
//       g2.fillRect(0,0,img.getWidth(),img.getHeight());
//       glyph.render(0,g2,1);
//
//       if(key==49)
//       ShowGUIMessage.showGUIMessage("x",img,"x");

	}
}
