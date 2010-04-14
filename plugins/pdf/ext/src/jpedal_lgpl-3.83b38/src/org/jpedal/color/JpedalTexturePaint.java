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
* JpedalTexturePaint.java
* ---------------
*/
package org.jpedal.color;

import org.jpedal.render.DynamicVectorRenderer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

public class JpedalTexturePaint implements Paint, PdfPaint {

    BufferedImage img;

	/**copy of raw tile if rotated*/
	DynamicVectorRenderer glyphDisplay=null;

	TexturePaint rotatedPaint;

	private boolean isRotated=false;

	private float[][] matrix;

	private float YStep;

	private float dx;

	private float dy;



	private AffineTransform imageScale;

	private float XStep;

	private float xx;

	private float yy;

	private boolean isUpsideDown;

	public JpedalTexturePaint(BufferedImage txtr, Rectangle2D anchor, boolean isRotated,DynamicVectorRenderer glyphDisplay) {


		this.isRotated=isRotated;

        System.out.println("isRotated = " + isRotated);

        if(isRotated)
			 this.glyphDisplay=glyphDisplay;
		else
			img=txtr;


        System.out.println("glyphDisplay @ con = " + glyphDisplay);


    }


    public PaintContext createContext(ColorModel cm,Rectangle db, Rectangle2D ub,
			AffineTransform xform, RenderingHints hints) {

        //create each rotated as single huge panel to fit gap as workaround to java

        float startX=0,startY=0;

        //workout required size
        int w=(int)(ub.getWidth());
        int h=(int)(ub.getHeight());

        BufferedImage image=new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        AffineTransform defaultAf2=g2.getTransform();

        int yCount=0;

        float offX=0,offY=0;

        float rotatedWidth=(XStep*matrix[0][0])-(YStep*matrix[1][0]);

        float rotatedHeight=-(YStep*matrix[1][1])-(XStep*matrix[0][1]);
        float shapeW=ub.getBounds().width;
        float shapeH=ub.getBounds().height;
        //int shapeCountW=(int)((shapeW/rotatedWidth));
        int shapeCountH=(int)((shapeH/rotatedHeight));

        if(shapeCountH>1){

            offX=(shapeW-(rotatedHeight*(shapeCountH)));//-19;
            offY=5-(shapeH-(rotatedWidth*shapeCountH));//-32;

        }else if(rotatedHeight>shapeW){
            offX=rotatedHeight-shapeW;//5;
            offY=shapeH-rotatedWidth;//20;
        }else{
            offX=(shapeH-rotatedHeight);//28;
            offY=(shapeW-rotatedWidth);//-5;
        }

        //if tile is smaller than Xstep,Ystep, tesselate to fill
        float y=0;
        for(y=0;y<h+YStep+dy;y=y+dy){

            startY=-yy-yy;;

            yCount++;

            for(float x=-dx;x<w+XStep+dx;x=x+dx){

//					if(isUpsideDown)
//						g2.translate(x+startX,-(y+startY));
//					else
                g2.translate(offX+x+startX,offY+y+startY);

                System.out.println("glyphDisplay = " + glyphDisplay);

                glyphDisplay.paint(g2,null,imageScale,null,false,false);
                g2.setTransform(defaultAf2);

                startY=startY+yy;

            }
            startX=startX-xx;

        }

        Rectangle rect=ub.getBounds();
        rotatedPaint=new TexturePaint(image,new Rectangle(rect.x,rect.y,rect.width,rect.height));

        System.out.println("hints = "+hints);
        
        return rotatedPaint.createContext(cm, db, ub, xform, hints);


    }

    public void setScaling(double cropX,double cropH,float scaling, float textX, float textY){

	}

	public boolean isPattern() {
		return false;
	}

	public void setPattern(int dummy) {

	}

	public int getRGB() {
		return 0;
	}

	public void setValues(float[][] matrix, float XStep, float YStep, float dx, float dy,
			AffineTransform imageScale,boolean isUpsideDown) {

		this.matrix=matrix;
		this.XStep=XStep;
		this.YStep=YStep;
		this.dx=dx;
		this.dy=dy;

		this.imageScale=imageScale;
		this.isUpsideDown=isUpsideDown;

		if(matrix[0][0]!=0 && matrix[1][1]!=0){
			xx=XStep*matrix[0][1];
			yy=YStep*matrix[1][0];

		}

		//System.out.println("Poss Xs="+(XStep*matrix[0][1])+" "+(XStep*matrix[0][0]));


		//System.out.println("values XStep="+XStep+" YStep="+YStep+" dx="+dx+" dy="+dy+
			//	" xx="+xx+" yy="+yy);

	}

    public int getTransparency() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
