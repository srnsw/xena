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
* T1Glyph.java
* ---------------
*/
package org.jpedal.fonts.glyph;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import org.jpedal.color.PdfPaint;
import org.jpedal.io.PathSerializer;
import org.jpedal.objects.GraphicsState;
import org.jpedal.utils.repositories.Vector_Path;
import org.jpedal.PdfDecoder;


/**
 * <p>defines the current shape which is created by command stream</p> 
 * <p><b>This class is NOT part of the API</b></p>.
 * Shapes can be drawn onto pdf or used as a clip on other image/shape/text.
 * Shape is built up by storing commands and then turning these commands into a
 * shape. Has to be done this way as Winding rule is not necessarily
 * declared at start.
  */
public class T1Glyph implements PdfGlyph, Serializable
{
	/** marked as transient so it wont be serialized */
    private transient Vector_Path cached_current_path=null;
	
    private float  glyfwidth=1000f;

    private boolean isStroked=false;
    private Paint strokePaint;

    private Map strokedPositions=new HashMap();

    public T1Glyph(){}
	

	/**
	 * store scaling factors
	*/
	public T1Glyph(Vector_Path cached_current_path){
		this.cached_current_path=cached_current_path;
    }
	
    //////////////////////////////////////////////////////////////////////////
	/**
	 * turn shape commands into a Shape object, storing info for later. Has to
	 * be done this way because we need the winding rule to initialise the shape
	 * in Java, and it could be set awywhere in the command stream
	 */
	public void render(int text_fill_type, Graphics2D g2, float scaling)
	{
       
		if(cached_current_path!=null){

//Shape c=g2.getClip();
//			
//			g2.setClip(null);
//			
//			g2.setPaint(Color.RED);
//			g2.fillRect(0, 0, 300, 600);
//			g2.setPaint(Color.BLUE);
//			g2.fillRect(300, 0, 300, 600);
//			g2.drawLine(0,0,600,600);
//			g2.setClip(c);
//			
//			
			GeneralPath[] paths=cached_current_path.get();
			int cacheCount=paths.length;
			for(int i=0;i<cacheCount;i++){
				
				if(paths[i]==null)
					break;
				
				if((text_fill_type==GraphicsState.FILL)){

                    //replicate shadow effect
                    if(isStroked){
                        Paint fillPaint=g2.getPaint();
                        if(((Color)strokePaint).getRGB()!=((Color)fillPaint).getRGB() &&
                                strokedPositions.containsKey(""+(int)g2.getTransform().getTranslateX()+'-'+(int)g2.getTransform().getTranslateY())){

                            Stroke fillStroke=g2.getStroke();

                            g2.setPaint(strokePaint);
                            g2.setStroke(new BasicStroke((float) (scaling/g2.getTransform().getScaleX())));
                            g2.draw(paths[i]);

                            g2.setPaint(fillPaint);
                            g2.setStroke(fillStroke);

                            //System.out.println(this.getID()+" "+this.getGlyphName());
                        }

                    }
					g2.fill(paths[i]);
                }

				if(text_fill_type==GraphicsState.STROKE){

                    //ensure visible if just stroke
                    if(text_fill_type!=GraphicsState.FILL && scaling>1.0f){
                       // System.out.println(">>"+glyfwidth+" "+scaling+" "+g2.getTransform()+" "+g2.getTransform().getScaleX());
                        g2.setStroke(new BasicStroke((float) (scaling/g2.getTransform().getScaleX())));
                        //g2.setStroke(new BasicStroke(200));
                        //System.out.println(((scaling/g2.getTransform().getScaleX())));
                        
                    }
                    
					g2.draw(paths[i]);

                    strokePaint=g2.getPaint();

                    strokedPositions.put(""+(int)g2.getTransform().getTranslateX()+'-'+(int)g2.getTransform().getTranslateY(),"x");

                }
			}
        }
	}


    /* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#getmaxWidth()
     */
    public float getmaxWidth() {
    	
        return glyfwidth;
    }

	/* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#getmaxHeight()
     */
    public int getmaxHeight() {
        return 0;
    }


	/* (non-Javadoc)
	 * @see org.jpedal.fonts.PdfGlyph#lockColors(java.awt.Color, java.awt.Color)
	 */
	public void lockColors(PdfPaint strokeColor, PdfPaint nonstrokeColor) {

	}


	/* (non-Javadoc)
	 * @see org.jpedal.fonts.PdfGlyph#ignoreColors()
	 */
	public boolean ignoreColors() {
		return false;
	}


	
	Area glyphShape=null;
	
	/**return shape of glyph*/
	public Area getShape() {
		
		if((cached_current_path!=null && glyphShape==null)){
		
			GeneralPath[] paths=cached_current_path.get();
			int cacheCount=paths.length;
			
			for(int i=1;i<cacheCount;i++){
				
				if(paths[i]==null)
					break;
				
				paths[0].append(paths[i],false);
				
			}
			
			if((paths!=null)&&(paths[0]!=null))
			glyphShape=new Area(paths[0]);
			
		}
		
		return glyphShape;
	}

    //used by Type3 fonts
    public String getGlyphName() {
        return "";  
    }

    /**
	 * method to set the paths after the object has be deserialized.
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param vp - the Vector_Path to set
	 */
	public void setPaths(Vector_Path vp){
		cached_current_path = vp;
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
		if((cached_current_path!=null)){
			
			GeneralPath[] paths=cached_current_path.get();
			
			int count=0;
			
			/** find out how many items are in the collection */
			for (int i = 0; i < paths.length; i++) {
				if(paths[i]==null){
					count = i;
					break;
				}
			}
			
			/** write out the number of items are in the collection */
			os.writeObject(new Integer(count));
			
			/** iterate throught the collection, and write out each path individualy */
			for (int i = 0; i < count; i++) {
				PathIterator pathIterator = paths[i].getPathIterator(new AffineTransform());
				PathSerializer.serializePath(os, pathIterator);
			}
			
		}
	}

	public void flushArea() {
		glyphShape=null;
	}


	public void setWidth(float width) {
		this.glyfwidth=width;
		
	}

    int minX=0,minY=0,maxX=0,maxY=0;

    public int getFontBB(int type) {

        //calc if not worked out
        if(minX==0 && minY==0 && maxX==0 && maxY==0){

            if(cached_current_path!=null){

                GeneralPath[] paths=cached_current_path.get();
                int cacheCount=paths.length;

                for(int i=0;i<cacheCount;i++){

                    if(paths[i]==null)
                        break;

                    Rectangle b=paths[i].getBounds();
                    if(i==0){
                        minX=b.x;
                        minY=b.y;
                        maxX=b.width;
                        maxY=b.height;

                    }else{

                        if(minX>b.x)
                            minX=b.x;
                        if(minY>b.y)
                            minY=b.y;

                        if(maxX<b.width)
                            maxX=b.width;
                        if(maxY<b.height)
                            maxY=b.height;

                    }
                }
            }
        }

        if(type== PdfGlyph.FontBB_X)
            return minX;
        else if(type== PdfGlyph.FontBB_Y)
            return minY;
        else if(type== PdfGlyph.FontBB_WIDTH)
            return maxX;
        else if(type== PdfGlyph.FontBB_HEIGHT)
            return minY;
        else
            return 0;
    }

    public void setStrokedOnly(boolean flag) {
        isStroked=flag;

    }
}
