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
* PatternColorSpace.java
* ---------------
*/
package org.jpedal.color;


import com.idrsolutions.pdf.color.shading.ShadingFactory;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.*;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Matrix;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.HashMap;


/**
 * handle Pattern ColorSpace
 */
public class PatternColorSpace extends GenericColorSpace{

    //does the Patterns (there is also a shading class)
    
    //flag to track rotation which needs custom handling
	private boolean isRotated=false;

    private Map cachedPaints=new HashMap();

	PdfObjectReader currentPdfFile=null;
	private BufferedImage img;
	
	private int XStep,YStep;

	private boolean isPrinting=false;

	static final private boolean debug=false;
	private boolean colorsReversed;

    Map colorspacesObjects;

	public PatternColorSpace(boolean isPrinting, PdfObjectReader currentPdfFile, Map colorspacesObjects){

		value = ColorSpaces.Pattern;
		this.isPrinting=isPrinting;
        this.colorspacesObjects=colorspacesObjects;

		currentColor = new PdfColor(1.0f,1.0f,1.0f);
		this.currentPdfFile = currentPdfFile;

    }

	/**
	 * convert color value to pattern
	 */
	public void setColor(String[] value_loc,int operandCount){

        PatternObject PatternObj=(PatternObject) patterns.get(value_loc[0]);

        String ref=PatternObj.getObjectRefAsString();

        if(ref!=null && cachedPaints.containsKey(ref)){
            currentColor = (PdfPaint) cachedPaints.get(ref);

            return;
        }

        currentPdfFile.checkResolved(PatternObj);
        
        byte[] streamData=currentPdfFile.readStream(PatternObj,true,true,true, false,false, null);

        /**
		 * initialise common values
		 */

        // see which type of Pattern (shading or tiling)
        final int shadingType= PatternObj.getInt(PdfDictionary.PatternType);

        // get optional matrix value
		float[][] matrix=null;
        float[] inputs=PatternObj.getFloatArray(PdfDictionary.Matrix);

        if(inputs!=null){
			
			if(shadingType==1){
				float[][] Nmatrix={{inputs[0],inputs[1],0f},{inputs[2],inputs[3],0f},{0f,0f,1f}};
				matrix=Nmatrix;
			}else{
				float[][] Nmatrix={{inputs[0],inputs[1],0f},{inputs[2],inputs[3],0f},{inputs[4],inputs[5],1f}};

				if(Nmatrix[2][0]<0)
					colorsReversed=true;
				else
					colorsReversed=false;

				matrix=Matrix.multiply(Nmatrix,CTM);
			}
		}


        /**
		 * set pattern
		 */
		if((shadingType == 1)){ //tiling
			
            	currentColor = setupTiling(PatternObj, inputs, matrix, streamData, colorspacesObjects);
			
		}else if(shadingType == 2){ //shading

			currentColor = setupShading(PatternObj,matrix);

		}else{
		}
	}

	static int count=0;

	/**
	 */
	private PdfPaint setupTiling(PdfObject PatternObj,float[] inputs, float[][] matrix,byte[] streamData,Map colorspacesObjects) {

        boolean newCode=true;
        
        final boolean showMessages=false;
        
        this.inputs=inputs;

        if(showMessages)
        System.out.println("ref="+PatternObj.getObjectRefAsString());

        /**
         * work out if upsidedown
         */
        boolean isUpsideDown=false,isSideways=false;
        if(matrix!=null){

            //markee
            isRotated=matrix[1][0]!=0 && matrix[0][1]!=0 && matrix[0][0]!=0 && matrix[1][1]!=0;

            //ignore slight rotations
            if(isRotated && matrix[0][0]!=0 && matrix[0][0]<0.001 && matrix[1][1]!=0 && matrix[1][1]<0.001){
                isRotated=false;
                matrix[0][0]=-matrix[0][1];
                matrix[1][1]=-matrix[1][0];

                matrix[1][0]=0;
                matrix[0][1]=0;
            }

            if(isRotated && matrix[0][0]>0 && matrix[0][1]<0 &&
            	 matrix[1][0]>0 && matrix[1][1]>0){
            }else if(isRotated && matrix[0][0]<0 && matrix[0][1]<0 &&
               	 matrix[1][0]<0 && matrix[1][1]>0){
            	
            	matrix[0][0]=-matrix[0][0];
            	//matrix[0][1]=-matrix[0][1];
            	matrix[1][0]=-matrix[1][0];
            	//matrix[1][1]=-matrix[1][1];
            	
            	isSideways=true;
            	if(showMessages)
            	Matrix.show(matrix);
            }
            
            isUpsideDown=(matrix[1][1]<0 || matrix[0][1]<0);

            //allow for this case
            if(matrix[0][0]>0 && matrix[0][1]<0 && matrix[1][0]>0 && matrix[1][1]>0){
                isUpsideDown=false;
                //System.out.println("x");
            }


            //breaks Scooby page so ignore
            if(matrix[0][0]>0.1f && (isRotated || isUpsideDown))
            	newCode=false;
            
            //used by rotation code
            if(isUpsideDown && matrix[0][1]>0 && matrix[1][0]>0)
                isUpsideDown=false;

        }

        /**
         * get values for pattern for PDF object
         */
        int PaintType=PatternObj.getInt(PdfDictionary.PaintType);


        //int TilingType=PatternObj.getInt(PdfDictionary.TilingType);

        //float[] BBox=PatternObj.getFloatArray(PdfDictionary.BBox);

        XStep=(int) PatternObj.getFloatNumber(PdfDictionary.XStep);
        YStep=(int) PatternObj.getFloatNumber(PdfDictionary.YStep);

        float dx=0,dy=0,xx=0,yy=0;

        //position of tile if inputs not null and less than 1
        int input_dxx=0, input_dyy=0;

        /**
         * adjust matrix to suit
         **/
        if(matrix!=null){

            xx=matrix[2][0];
            yy=matrix[2][1];

            //allow for upside down
            if(matrix[1][1]<0)
                matrix[2][1]=YStep;

            // needed for reporttype file
            if(matrix[1][0]!=0.0)
                matrix[2][1]=-matrix[1][0];

        }

        PdfObject Resources=PatternObj.getDictionary(PdfDictionary.Resources);


        //float dx=0,dy=0;

        /**
         * convert stream into an image
         */

        //decode and create graphic of glyph
        PdfStreamDecoder glyphDecoder=new PdfStreamDecoder(colorspacesObjects);
        glyphDecoder.setStreamType(PdfStreamDecoder.PATTERN);
        ObjectStore localStore = new ObjectStore();
        glyphDecoder.setStore(localStore);
        //glyphDecoder.setMultiplier(multiplyer);


        DynamicVectorRenderer glyphDisplay=new DynamicVectorRenderer(0,false,20,localStore);
        glyphDisplay.setOptimisedRotation(false);

        try{
            glyphDecoder.init(false,true,7,0,new PdfPageData(),0,glyphDisplay,currentPdfFile);

            /**read the resources for the page*/
            if (Resources != null){
                currentPdfFile.checkResolved(Resources);
                glyphDecoder.readResources(Resources,true);
            }
            glyphDecoder.setDefaultColors(gs.getStrokeColor(),gs.getNonstrokeColor());

            /**
             * setup matrix so scales correctly
             **/
            GraphicsState currentGraphicsState=new GraphicsState(0,0);
            //multiply to get new CTM
            if(matrix!=null)
                currentGraphicsState.CTM =matrix;


            glyphDecoder.decodePageContent(null,0,0, currentGraphicsState, streamData);


        } catch (PdfException e1) {
            e1.printStackTrace();
        }


        //flush as image now created
        glyphDecoder=null;

        //ensure positive
        if(XStep<0)
            XStep=-XStep;
        if(YStep<0)
            YStep=-YStep;

        /**
         * if image is generated larger than slot we draw it into we
         * will lose definition. To avoid this, we draw at full size and
         * scale only when drawn onto page
         */
        //workout unscaled tile size
        float rawWidth=0,rawHeight=0;
        boolean isDownSampled=false;

        if(matrix!=null){
            rawWidth=matrix[0][0];
            if(rawWidth==0)
                rawWidth=matrix[0][1];
            if(rawWidth<0)
                rawWidth=-rawWidth;
            rawHeight=matrix[1][1];
            if(rawHeight==0)
                rawHeight=matrix[1][0];
            if(rawHeight<0)
                rawHeight=-rawHeight;
            //isDownSampled=((rawHeight>YStep)||(rawWidth>XStep));
        }

        AffineTransform imageScale=null;

        if(matrix!=null){
            //image scaled up to fit
            if(inputs!=null && inputs[0]>1 && inputs[3]>1){

                img=new BufferedImage((int)(XStep*rawWidth),(int)(YStep*rawHeight), BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2=img.createGraphics();

                AffineTransform defaultAf=g2.getTransform();

                g2.setClip(new Rectangle(0,0,img.getWidth(),img.getHeight()));

                glyphDisplay.paint(g2,null,new AffineTransform(matrix[0][0], matrix[0][1], matrix[1][0],matrix[1][1], inputs[4],-inputs[5]/2) ,null,false,false);
                //g2.setTransform(defaultAf);

            }else{

                dx=matrix[0][0];

                if(dx==0)
                    dx=matrix[0][1];
                if(dx<0)
                    dx=-dx;
                dy=matrix[1][1];

                if(dy==0)
                    dy=matrix[1][0];
                if(dy<0)
                    dy=-dy;

                dx=dx*XStep;
                dy=dy*YStep;

                //default values
                int imgW=(int)XStep;
                int imgH=(int)YStep;

                if(isUpsideDown){

                    //System.out.println(matrix[0][0]+" "+matrix[0][1]+" "+matrix[1][0]+" "+matrix[1][1]);

                    int xCount=(int)(XStep/dx);
                    int yCount=(int)(YStep/dy);

                    if(xCount>0 && yCount>0){
                        imgW=(int)((xCount+1)*dx);//XStep;
                        imgH=(int)((yCount+1)*dy);

                        XStep=imgW;
                        YStep=imgH;
                    }
                }

                if(inputs!=null && inputs[0]>0 && inputs[0]<1 && inputs[3]>0 && inputs[3]<1 && !isUpsideDown){
                    imgW=(int)(dx);
                    imgH=(int)(dy);

                    //workout offsets
                    input_dxx=(int)inputs[4];
                    if(input_dxx>XStep){
                        while(input_dxx>0)
                        input_dxx=input_dxx-XStep;
                        input_dxx=input_dxx/2;
                    }

                    //workout offsets
                    input_dyy=(int)inputs[5];
                    if(input_dyy>imgH){
                        while(input_dyy>0)
                        input_dyy=input_dyy-imgH;
                        //input_dyy=input_dyy/2;
                    }
                }


                if(isDownSampled){
                    img=new BufferedImage((int)(rawWidth+.5f),(int)(rawHeight+.5f),BufferedImage.TYPE_INT_ARGB);
                    imageScale=AffineTransform.getScaleInstance(XStep/rawWidth,YStep/rawHeight);
                }else
                    img=new BufferedImage(imgW,imgH,BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2=img.createGraphics();

                AffineTransform defaultAf=g2.getTransform();

                g2.setClip(new Rectangle(0,0,img.getWidth(),img.getHeight()));

                /**
                 * allow for tile not draw from 0,0
                 */
                int startX=0;
                Rectangle actualTileRect=glyphDisplay.getOccupiedArea().getBounds();

                /**
                 * buffer onto Tile
                 */
                BufferedImage tileImg=null;

                int tileW,tileH;
                int dxx=0,dyy=0;
                if(actualTileRect.x<0){
                    tileW=actualTileRect.width-actualTileRect.x;
                    dxx=actualTileRect.x;
                }else
                    tileW=actualTileRect.width+actualTileRect.x;

                if(actualTileRect.y<0){
                    tileH=actualTileRect.height-actualTileRect.y;
                    dyy=actualTileRect.y;
                }else
                    tileH=actualTileRect.height+actualTileRect.y;

                if(newCode){
                    tileImg=new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D tileG2=tileImg.createGraphics();
                    tileG2.translate(-dxx,-dyy);
                    glyphDisplay.paint(tileG2,null,null,null,false,false);

//                    tileG2.translate(dxx,dyy);
//                    tileG2.setPaint(Color.CYAN);
//                    tileG2.drawRect(0,0,tileW-1,tileH-1);

                }

                int rectX=actualTileRect.x;
                if(rectX<0 && !newCode)
                    startX= (int) (-rectX*matrix[0][0]);

                if(!isRotated){
                    //if tile is smaller than Xstep,Ystep, tesselate to fill
                    float max=YStep;
                    if(newCode)
                    max=YStep+(tileImg.getHeight()*2);

                    for(float y=0;y<max;y=y+dy){
                        for(float x=startX;x<XStep;x=x+dx){

                            if(isUpsideDown)
                                g2.translate(x,-y);
                            else
                                g2.translate(x,y);

                            if(newCode){
                            	AffineTransform tileAff=new AffineTransform();
                                //tileAff.scale(1,-1);
                                //tileAff.translate(0,tileImg.getHeight());

                                g2.drawImage(tileImg,tileAff,null);
                            }else
                                glyphDisplay.paint(g2,null,imageScale,null,false,false);

                            g2.setTransform(defaultAf);

                        }
                    }
                }
            }

        }else{


            int imgW=(int)XStep;
            int imgH=(int)YStep;
            if(isDownSampled){
                img=new BufferedImage((int)(rawWidth+.5f),(int)(rawHeight+.5f),BufferedImage.TYPE_INT_ARGB);
                imageScale=AffineTransform.getScaleInstance(XStep/rawWidth,YStep/rawHeight);
            }else
                img=new BufferedImage(imgW,imgH,BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2=img.createGraphics();

            glyphDisplay.paint(g2,null,null,null,false,false);


            if(isUpsideDown && img.getHeight()>1){
                AffineTransform flip=new AffineTransform();
                flip.translate(0, img.getHeight());
                flip.scale(1, -1);
                AffineTransformOp invert =new AffineTransformOp(flip,ColorSpaces.hints);
                img=invert.filter(img,null);
            }
        }
        localStore.flush();

        /**
         * create paint using image
         */

//		g2=img.createGraphics();
//		g2.setPaint(Color.BLACK);
//		g2.drawRect(0,0,img.getWidth()-1, img.getHeight()-1);

        //ShowGUIMessage msg = new ShowGUIMessage();
        //ShowGUIMessage.showGUIMessage("x", img, "x");
//        try{
//        ImageIO.write(img,"PNG",new File("/Users/markee/Desktop/img.png"));
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        
        if(img!=null){
            PdfPaint paint=new PdfTexturePaint(img,  new Rectangle(input_dxx, input_dyy, img.getWidth() , img.getHeight()));

            if(isRotated){
            	
            	Rectangle actualTileRect=glyphDisplay.getOccupiedArea().getBounds();

            	 /**
                 * buffer onto Tile
                 */
                BufferedImage tileImg=null;

                int tileW,tileH;
                int dxx=0,dyy=0;
                if(actualTileRect.x<0){
                    tileW=actualTileRect.width-actualTileRect.x;
                    dxx=actualTileRect.x;
                }else
                    tileW=actualTileRect.width+actualTileRect.x;

                if(actualTileRect.y<0){
                    tileH=actualTileRect.height-actualTileRect.y;
                    dyy=actualTileRect.y;
                }else
                    tileH=actualTileRect.height+actualTileRect.y;
            	
                if(showMessages)
                System.out.println("tileW =" +tileW+ "");
            	
            	if(showMessages)
            	System.out.println("XStep =" + XStep + ", YStep = " + YStep + ", dx = " + dx + ", dy= " +dy);
            	
            	
            	paint = new RotatedTexturePaint(isSideways, PatternObj, newCode, glyphDisplay, matrix, XStep, YStep, dx, dy, imageScale);
            
//            	System.out.println(PatternObj.getObjectRefAsString());
//            	if(PatternObj.getObjectRefAsString().equals("19 0 R"))
//            		paint=new PdfColor(255,0,0);
//            	else
//            		paint=new PdfColor(0,255,0);
            }

            cachedPaints.put(PatternObj.getObjectRefAsString(),paint);
            return paint;
        }else
            return null;
    }

	/**
	 */
	private PdfPaint setupShading(PdfObject PatternObj,float[][] matrix) {

		/**
		 * get the shading object
		 */

		PdfObject Shading=PatternObj.getDictionary(PdfDictionary.Shading);
		
		/**
		 * work out colorspace
		 */
		PdfObject ColorSpace=Shading.getDictionary(PdfDictionary.ColorSpace);
		
		//convert colorspace and get details
		GenericColorSpace newColorSpace=ColorspaceFactory.getColorSpaceInstance(false, currentPdfFile,
                ColorSpace, colorspacesObjects, null, false);
		
		
		if(Shading==null){
			return null;
		}else{
			return ShadingFactory.createShading(Shading,isPrinting, pageHeight,newColorSpace,currentPdfFile,matrix,pageHeight,colorsReversed);
		}
	}
}
