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
* T3Glyph.java
* ---------------
*/
package org.jpedal.fonts.glyph;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jpedal.color.PdfPaint;
import org.jpedal.render.DynamicVectorRenderer;




/**
 * <p>defines the current shape which is created by command stream</p> 
 * <p><b>This class is NOT part of the API</b></p>.
 * Shapes can be drawn onto pdf or used as a clip on other image/shape/text.
 * Shape is built up by storing commands and then turning these commands into a
 * shape. Has to be done this way as Winding rule is not necessarily
 * declared at start.
  */
public class T3Glyph implements PdfGlyph
{
	
	private boolean lockColours=false;
	
	DynamicVectorRenderer glyphDisplay;
	
	/**actual offset of glyph*/
	private int maxWidth,maxHeight;

    String stringName="";

    float glyphScale=1f;

    public T3Glyph(){}

    public String getGlyphName() {
        return stringName;
    }

    public void setStringName(String stringName) {
        this.stringName = stringName;
    }
    
    /**
	 * create the glyph as a wrapper around the DynamicVectorRenderer 
	*/
	public T3Glyph(DynamicVectorRenderer glyphDisplay,int x,int y,boolean lockColours,String pKey){
		this.glyphDisplay=glyphDisplay;
		this.maxWidth=x;
		this.maxHeight=y;
		this.lockColours=lockColours;
        this.stringName=pKey;
    }

    //used by Type3 if need to adjust scaling due to size
    public void setScaling(float glyphScaling){
        this.glyphScale=glyphScaling;
    }
	
	
    /**
	 * draw the t3 glyph
	 */
	public Area getShape()
	{
		return null;
	}
	
	/**
	 * draw the t3 glyph
	 */
	public void render(int type, Graphics2D g2, float scaling){

        glyphDisplay.setScalingValues(0,0,scaling);

        //factor in if not correct size
        AffineTransform aff=null;
        if(glyphScale!=1f){
            aff=g2.getTransform();
            g2.scale(glyphScale,glyphScale);
        }
        glyphDisplay.paint(g2,null,null,null,false,false);

        //undo
        if(aff!=null)
            g2.setTransform(aff);
    }
	
	
	/**
	 * Returns the max width
	 */
	public float getmaxWidth() {
		return maxWidth;
	}

	/**
	 * Returns the max height
	 */
	public int getmaxHeight() {
		return maxHeight;
	}


	/** 
	 * set colors for display
	 */
	public void lockColors(PdfPaint strokeColor, PdfPaint nonstrokeColor) {
		
		glyphDisplay.lockColors(strokeColor,nonstrokeColor);

    }

	/**
	 * flag if use internal colours or text colour
	 */
	public boolean ignoreColors() {
		return lockColours;
	}
	

	/**
	 * method to serialize all the paths in this object.  This method is needed because
	 * GeneralPath does not implement Serializable so we need to serialize it ourself.
	 * The correct usage is to first serialize this object, cached_current_path is marked
	 * as transient so it will not be serilized, this method should then be called, so the
	 * paths are serialized directly after the main object in the same ObjectOutput.
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param os - ObjectOutput to write to
	 * @throws IOException
	 */
	public void writePathsToStream(ObjectOutput os) throws IOException {
		
		//convert to bytes
		byte[] dvr= glyphDisplay.serializeToByteArray(null);
		
		int size=dvr.length;
		
		os.writeObject(dvr);
		os.writeInt(maxWidth);
		os.writeInt(maxHeight);
		os.writeBoolean(lockColours);
		
	}
	
	/**recreate T3 glyph from serialized data*/
	public T3Glyph(ObjectInput os) {
		
		try {
			byte[] dvr=(byte[]) os.readObject();
			
			glyphDisplay=new DynamicVectorRenderer(dvr,null);
			
			maxWidth=os.readInt();
			maxHeight=os.readInt();
			lockColours=os.readBoolean();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void flushArea() {
		
	}

	public void setDisplacement(short rawlsb, float width) {
		// TODO Auto-generated method stub
		
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
