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
* RotatedTexturePaint.java
* ---------------
*/
package org.jpedal.color;

import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Matrix;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

public class RotatedTexturePaint implements Paint, PdfPaint {

    BufferedImage img;

	/**copy of raw tile if rotated*/
	DynamicVectorRenderer patternOnTile =null;


	private float[][] matrix;

	private float yStep;

	private float offsetXOnCanvas;

	private float offsetYOnCanvas;

	private AffineTransform imageScale;

	private float xStep;

	private float tileXoffset;

	private float tileYoffset;

    private float yOffset;

    boolean cacheToTile,isSideways ;

    boolean isUpsideDown=false, isRotated;
    
    boolean debug=false;

    final boolean caseInfo = false;

    int offsetIfTileLargerThanGapX=0,offsetIfTileLargerThanGapY=0;

    final boolean flagOffsetYOnCanvas = false;

    final boolean tileDebug = false; //visual debug ie: cross  and a border for each tile in cache mode

    boolean debugPattern=true;
      
    PdfObject patternObj;//for debugging
      
    /**
     * This just sets up the variables
     * @param patternObj 
     * @param cacheToTile - flag to show which version of code used
     * @param patternOnTile - the Java class containing the Java2D instructions to draw the tessalating pattern
     * @param matrix - the image size, rotaion and location
     * @param xStep - the repeating size
     * @param yStep - the repeating size
     * @param offsetXOnCanvas
     * @param offsetYOnCanvas
     * @param imageScale
     */
    public RotatedTexturePaint(boolean isSideways, PdfObject patternObj, boolean cacheToTile, 
    		DynamicVectorRenderer patternOnTile, float[][] matrix, float xStep, float yStep, 
    		float offsetXOnCanvas, float offsetYOnCanvas, AffineTransform imageScale) {

    	//patternObj.getObjectRefAsString().equals("12 0 R");
    	
    	//enable for debugging
    	//this.patternObj = patternObj;
        //assign values to local copies
        //cacheToTile=true;//hardcoded!!!!

    	this.isSideways=isSideways;
    	this.cacheToTile = cacheToTile;
        this.patternOnTile = patternOnTile;

        this.matrix = matrix; //contains any rotation and scaling of pattern cell
        this.xStep = xStep; //horizontal width of repeating pattern
        this.yStep = yStep; //vertical width of repeating pattern
        this.offsetXOnCanvas =  offsetXOnCanvas;//x offset so pattern starts from right place
        this.offsetYOnCanvas =offsetYOnCanvas; //y offset so pattern starts from right place

        //potential scaling to apply to pattern cell
        this.imageScale = imageScale;
        
       		
   
//        if(debugPattern){
//        //	System.out.println(
//         //"= " + matrix +",xStep =" + xStep +",yStep =" + yStep +",offsetXOnCanvas =" + offsetXOnCanvas +",offsetYOnCanvas =" + offsetYOnCanvas );
//        }

		//factor in any rotation into numbers
        //(ie if the tile is 'turned' we need to draw it in
        //a slightly different position
        if (matrix[0][0] != 0 && matrix[1][1] != 0) {
                tileXoffset = xStep * matrix[0][1];
            tileYoffset = yStep * matrix[1][0];
            
            /**
            if(cacheToTile){
                 tileYoffset =0 ;
                yOffset = yStep * matrix[1][0]; //y offset between rows, used to form cumulativeToffset
            }else{
                    tileYoffset = yStep * matrix[1][0];
            }/**/
        }


        this.offsetIfTileLargerThanGapX=(int)tileXoffset;
        this.offsetIfTileLargerThanGapY=(int)tileYoffset;

        if(debugPattern){
//        	System.out.println("offsetIfTileLargerThanGapX = "+ offsetIfTileLargerThanGapX);
//        	System.out.println("offsetIfTileLargerThanGapY = "+ offsetIfTileLargerThanGapY);
        }
    }

    //imp lines 
    //translates (image 340)415
    //           (context (ie: g2) 374)449
    // rotated width n height 213-214
    
    /**
     * called to fill in the whole defined by db/ub with repeating pattern
     * @param cm
     * @param db
     * @param ub
     * @param xform
     * @param hints
     * @return
     */
    public PaintContext createContext(ColorModel cm,Rectangle db, Rectangle2D ub,
			AffineTransform xform, RenderingHints hints) {

        TexturePaint rotatedPaint;
        
        
        
        double contextTranslateX=0, contextTranslateY;
    	
        //debug=false;

        if(debugPattern){
            //System.out.println(" ---"+this+" createContext db="+db+" ub="+ub+" xform="+xform);
        	
        	Matrix.show(matrix);

        }
        //create each rotated as single huge panel to fit gap as workaround to java

        //workout required size
        int w=(int)(ub.getWidth());
        int h=(int)(ub.getHeight());

        if(debug)
            System.out.println("area to fill w="+w+" h="+h);

        /**
         * create an image of size to fill gap and
         * create G2 object to draw onto
         */
        BufferedImage wholeImage =new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = wholeImage.createGraphics();

        AffineTransform defaultAf2=g2.getTransform();

        /**
         * work out actual rotated shape values
         * offX, offY are the actual offset needed on the repeating cell
         * to appear in the correct place
         */
        float offX=0,offY=0,rotatedWidth,rotatedHeight;

        rotatedWidth=(xStep *matrix[0][0])-(yStep *matrix[1][0]);
        rotatedHeight=-(yStep *matrix[1][1])-(xStep *matrix[0][1]);

        float shapeW=ub.getBounds().width;
        float shapeH=ub.getBounds().height;

        //number of rows needed to fill pattern
        int numberOfRows =(int)((shapeH/rotatedHeight));

        /**
         * workout offsets needed for various special cases
         */
        
        if(numberOfRows >1){ //multiple rows

            offX=(shapeW-(rotatedHeight*(numberOfRows)));//-19;
            offY=5-(shapeH-(rotatedWidth* numberOfRows));//-32;

            if(caseInfo || debug)
                System.out.println("case1 OffsetX="+offX+" offsetY="+offY);

        }else if(rotatedHeight>shapeW){ //one row, wholeImage is wider than gap          ???am thinking wrong but c if is a prob
            offX=rotatedHeight-shapeW;//5;
            offY=shapeH-rotatedWidth;//20;

            if(caseInfo || debug)
                System.out.println("case2 OffsetX="+offX+" offsetY="+offY);
        }else{ //general case
            offX=(shapeH-rotatedHeight);//28;
            offY=(shapeW-rotatedWidth);//-5;

            if(caseInfo || debug)
                System.out.println("case3 OffsetX="+offX+" offsetY="+offY);
        }

        /**
         * allow for tile not draw from 0,0 into gap
         * (maybe we actually have a 2x2 tile filing a 6x6 gap which needs to be drawn in
         * 2x1
         */
        //the actual cell dimensions ***** tile w and tile h its dimensions
        //if rotated it is horizontal/vertical surrounding area
        Rectangle actualTileRect= patternOnTile.getOccupiedArea().getBounds();

        int tileW,tileH;
        int patternXOffsetOnTile=0,patternYOffsetOnTile=0; //offset to where we draw the pattern inside the tile
        if(actualTileRect.x<0){
            tileW=actualTileRect.width-actualTileRect.x;
            patternXOffsetOnTile=actualTileRect.x;
        }else
            tileW=actualTileRect.width+actualTileRect.x;

        if(actualTileRect.y<0){
            tileH=actualTileRect.height-actualTileRect.y;
            patternYOffsetOnTile=actualTileRect.y;
        }else
            tileH=actualTileRect.height+actualTileRect.y;

        /**
         * buffer onto Tile
         */
        BufferedImage tileImg=null;

        double imgTranslateX=0, imgTranslateY=0; //used for showing where relative to context the cell pattern is being printed
      							 //with middle of png pic being 0,0
        
      //turn the tile once
    	if(isSideways){
    		
    		imageScale=new AffineTransform();
    		imageScale.scale(-1, 1); //1,-1
    		
    		imgTranslateX = -(offsetXOnCanvas/(xform.getScaleX()));
    		imgTranslateY = 0;
    		
    		imageScale.translate(imgTranslateX,imgTranslateY);//**shape w -77 at 100% /**-100,-170**///glyphDisplay.getOccupiedArea().height - 20);// glyphDisplay.getOccupiedArea().height);
    		//if(showMessages)
    		/**System.out.println("newCode="+xform+" "+xform.getScaleX());
    		
    		System.out.println("offsetXOnCanvas = " + offsetXOnCanvas);

    		System.out.println("***********************************");
    		
    		**/
    	}else if(((matrix[0][0] >= 0) && (matrix[1][0] >= 0)) && ((matrix[0][1] <= 0) && (matrix[1][1] >= 0))){ //fix for LHS on shirts file
    		//System.out.println("xx = " + xx + " is not sideways. "+imageScale );
    		imageScale=new AffineTransform();
    		imageScale.scale(-1,-1); 
    		
    		//imgTranslateX = -(offsetXOnCanvas/(xform.getScaleX()));
    	    
		   		//imageScale.translate(-patternOnTile.getOccupiedArea().width-60,patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y);//glyphDisplay.getOccupiedArea().height - 20);// glyphDisplay.getOccupiedArea().height);
		   
		   		imageScale.translate(0,patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y);//imgTranslateVarX


    		//System.out.println(imageScale+" "+patternOnTile.getOccupiedArea());
    	}
    		

        //if image bigger than tile, we just need 1
    	if(ub.getBounds().width<patternOnTile.getOccupiedArea().width && ub.getBounds().height<patternOnTile.getOccupiedArea().height){

    		//System.out.println("tileW="+tileW+" patternXoff="+patternXOffsetOnTile+" offX="+offX+" xform="+xform+" xstep="+xStep);
    		//System.out.println("tileXoffset="+tileXoffset+" offsetXOnCanvas="+offsetXOnCanvas+" shape="+patternOnTile.getOccupiedArea());
    		//g2.translate(-patternXOffsetOnTile-db.x,-patternYOffsetOnTile+db.y);
    		if(isSideways){
    			contextTranslateX = -g2.getTransform().getTranslateX()-(offsetXOnCanvas-db.width);
    			contextTranslateY =-g2.getTransform().getTranslateY()+(offsetIfTileLargerThanGapY-offsetYOnCanvas);
    			g2.translate(contextTranslateX,contextTranslateY);
    			 
    		}else{
    			//g2.translate(-g2.getTransform().getTranslateX()-(offsetXOnCanvas-db.width),-g2.getTransform().getTranslateY()+(offsetIfTileLargerThanGapY-offsetYOnCanvas));

    			//contextTranslateX = -(offsetXOnCanvas/(xform.getScaleX())); //if  rhs = 1 then == offsetXonCanvas if bigger is less 
    			contextTranslateX =-offX+patternOnTile.getOccupiedArea().width-patternOnTile.getOccupiedArea().x;

    			contextTranslateY =-g2.getTransform().getTranslateY()+(offsetIfTileLargerThanGapY-offsetYOnCanvas);	
    			
    			g2.translate(tileW-offX+5,contextTranslateY);//contextTranslateX

    			//if(patternObj.getObjectRefAsString().equals("12 0 R"))
    			//	System.out.println("g2="+g2.getTransform());
    		}

    		patternOnTile.paint(g2,null,imageScale,null,false,false);
            
    		/**
    		if(patternObj.getObjectRefAsString().equals("12 0 R")){
            	System.out.println("+++++++++++++++++++++++++++++++");
            	System.out.println("offsetXOnCanvas = " + offsetXOnCanvas + "\n" +
            			",tileXoffset =" + tileXoffset + "\n" +
            			",xStep =" + xStep + "\n" +
            			",w =" + w + "\n" +
            			",offX =" + offX + "\n" +
            			",rotatedWidth =" + rotatedWidth + "\n" +
            			",tileW = " + tileW +
            			", xForm = " + xform+
            			"\nAffine="+g2.getTransform()+"\nimageScale="+imageScale +
            			"\n patternXOffsetOnTile = " + patternXOffsetOnTile +
            			"\nshapeW = "+ shapeW +
            			"\n patOnTile x = " + patternOnTile.getOccupiedArea().getBounds().x +
            			"\n patOnTile w = " + patternOnTile.getOccupiedArea().width+
            			"\n contextTranslateX="+contextTranslateX
            			
            	);
            	
            	System.out.println("+++++++++++++++++++++++++++++++");
            	
            }
    		/**/


    	}else{ //if image smaller than tile

            //draw wholeImage onto tile once and then just use tile
        if(cacheToTile){

            if(debug)
            System.out.println("cache to tile size "+tileW+" "+tileH+" "+this.xStep+" "+yStep);

            tileImg=new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_ARGB);

            Graphics2D tileG2=tileImg.createGraphics();
            tileG2.translate(-patternXOffsetOnTile,-patternYOffsetOnTile);
            patternOnTile.paint(tileG2,null,null,null,false,false);

        }

        /**
         * allow for specific odd case and move to correct location
         */
        float maxYY=h+yStep+ offsetYOnCanvas;
        if(cacheToTile)
            maxYY=maxYY+(tileImg.getHeight()*2);

        /**
         * draw repeating pattern onto out tile
         * if tile is smaller than Xstep,Ystep, tesselate to fill
         */
        float startX=0,startY=0;
        for(float y=0;y<maxYY;y=y + offsetYOnCanvas){ //fill all columns           // <==would be more obvious if all code for moving it the correct ammount was here

            startY=-tileYoffset - tileYoffset;

            //if(cacheToTile)  //to correct drift happens in cached version
            //    startX=0; //reset where start on x axis on each row

            for(float x=-offsetXOnCanvas;x<w+ xStep + offsetXOnCanvas;x=x+ offsetXOnCanvas){ //fill all rows

//					if(isUpsideDown)
//						g2.translate(x+startX,-(y+startY));
//					else
                //move to correct location to draw pattern
                g2.translate(offX+x+startX,offY+y+startY);

                if(cacheToTile){ //single wholeImage of tile generated so draw wholeImage at correct location

                    //invert
                    AffineTransform tileAff=new AffineTransform();
                    tileAff.scale(1,-1);
                    tileAff.translate(0,tileImg.getHeight());

                    //draw in location
                    g2.drawImage(tileImg,tileAff,null);
                }else{ //pass in the wholeImage and get pattenr to draw itself onto it
                        patternOnTile.paint(g2,null,imageScale,null,false,false);
                        
                }

                g2.setTransform(defaultAf2);

                startY=startY+ tileYoffset;

            }
            startX=startX- tileXoffset;

        }
        }

        //return single wholeImage to fill gap
        Rectangle rect=ub.getBounds();

        //debug code to show actual context
        /**
        if(xx!=-11){
            Graphics2D gg=wholeImage.createGraphics();
            if(xx==0) //top left on r0709k13
                gg.setPaint(Color.DARK_GRAY);
            else if(xx==1)
                gg.setPaint(Color.RED);
            else if(xx==2)
                gg.setPaint(Color.GREEN);
            else if(xx==3)
                gg.setPaint(Color.CYAN); //blue shirt right sleeve
            else if(xx==4)
                gg.setPaint(Color.MAGENTA);
            else if(xx==5)
                gg.setPaint(Color.YELLOW);
            else
                gg.setPaint(Color.BLUE);

            gg.fillRect(0,0,wholeImage.getWidth(),wholeImage.getHeight());
            gg.setPaint(Color.BLACK);
            gg.drawLine(0,0,wholeImage.getWidth(),wholeImage.getHeight());
            gg.drawLine(wholeImage.getWidth(),0,0,wholeImage.getHeight());
            gg.drawRect(0,0,wholeImage.getWidth(),wholeImage.getHeight());

        }
        /**/
        
        rotatedPaint=new TexturePaint(wholeImage,new Rectangle(rect.x,rect.y,rect.width,rect.height));

        return rotatedPaint.createContext(cm, db, ub, xform, new RenderingHints(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
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

    public int getTransparency() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
