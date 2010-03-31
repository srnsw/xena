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
* ImageTransformer.java
* ---------------
*/
package org.jpedal.images;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import org.jpedal.color.ColorSpaces;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.JAIHelper;
import org.jpedal.objects.GraphicsState;
import org.jpedal.utils.LogWriter;

/**
 * class to shrink and clip an extracted image
 * On reparse just calculates co-ords
 */
public class ImageTransformer {

	/**image tpye (RGB or ARGB)*/
	private int image_type;

	/**set if fast draft scaling*/
	private boolean isDraft;

	/**holds the actual image*/
	private BufferedImage current_image;

	/**matrices used in transformation*/
	private float[][] Trm, CTM;

	/**image co-ords*/
	private int i_x = 0, i_y = 0, i_w = 0, i_h = 0;

	/**dpi scale factor*/
	private float scale=1;

	/////////////////////////////////////////////////////
	/**
	 * pass in image information and apply transformation matrix
	 * to image
	 */
	public ImageTransformer(
		int dpi,
		GraphicsState current_graphics_state,
		BufferedImage new_image,
		boolean scaleImage,boolean isDraft) {

		//set scaling for final image
		scale=dpi/72;

		this.isDraft=isDraft;

		//save global values
		this.current_image = new_image;
		int w,h;

		w = current_image.getWidth(); //raw width
		h = current_image.getHeight(); //raw height

		CTM = current_graphics_state.CTM; //local copy of CTM

        //build transformation matrix by hand to avoid errors in rounding
		Trm = new float[3][3];
		Trm[0][0] = (CTM[0][0] / w);
		Trm[0][1] = -(CTM[0][1] / w);
		Trm[0][2] = 0;
		Trm[1][0] = -(CTM[1][0] / h);
		Trm[1][1] = (CTM[1][1] / h);
		Trm[1][2] = 0;
		Trm[2][0] = CTM[2][0];
		Trm[2][1] = CTM[2][1];
		Trm[2][2] = 1;

		//round numbers if close to 1
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if ((Trm[x][y] > .99) & (Trm[x][y] < 1))
					Trm[x][y] = 1;
			}
		}


		scale(scaleImage,w,h,dpi);

		completeImage();
	}

	private void scale(boolean scaleImage,int w,int h,int dpi){

        //boolean timeChange=true;

        /**
         * transform the image only if needed
         */
        if (Trm[0][0] != 1.0 || Trm[1][1] != 1.0 || Trm[0][1] != 0.0 || Trm[1][0] != 0.0 || dpi!=72) {

            //workout transformation for the image
            AffineTransform image_at =new AffineTransform(Trm[0][0]*scale,Trm[0][1]*scale,Trm[1][0]*scale,Trm[1][1]*scale,0,0);

            //apply it to the shape first so we can align
            Area r =new Area(new Rectangle(0,0,w,h));
            r.transform(image_at);

            //make sure it fits onto image (must start at 0,0)
            double ny = r.getBounds2D().getY();
            double nx = r.getBounds2D().getX();

            image_at =new AffineTransform(Trm[0][0]*scale,Trm[0][1]*scale,Trm[1][0]*scale,Trm[1][1]*scale,-nx,-ny);

            //Create the affine operation.
            //ColorSpaces.hints causes single lines to vanish);
            AffineTransformOp invert;


            if(w>1 && h>1){

                //fix image inversion if matrix (0,x,-y,0)
                if(CTM[0][0]==0 && CTM[1][1]==0 && CTM[0][1]>0 && CTM[1][0]<0){
                    image_at.scale(-1,1);
                    image_at.translate(-current_image.getWidth(),0);
                }
                
                invert = new AffineTransformOp(image_at,  ColorSpaces.hints);

            }else{

                //allow for line with changing values
                boolean isSolid=true;

                if(h==1){
                    //test all pixels set so we can keep a solid line
                    Raster ras=current_image.getRaster();
                    int bands=ras.getNumBands();
                    int width=ras.getWidth();
                    int[] elements=new int[(width*bands)+1];

                    ras.getPixels(0,0,width,1, elements);
                    for(int j=0;j<bands;j++){
                        int first=elements[0];
                        for(int i=1;i<width;i++){
                            if(elements[i*j]!=first){
                                isSolid=false;
                                i=width;
                                j=bands;
                            }
                        }
                    }
                }

                if(isSolid)
                    invert = new AffineTransformOp(image_at,  null);
                else
                    invert = new AffineTransformOp(image_at,  ColorSpaces.hints);
            }
            //ShowGUIMessage.showGUIMessage("x",current_image,"x");
            //if there is a rotation make image ARGB so we can clip
            if (CTM[1][0] != 0 || CTM[0][1] != 0)
                current_image = ColorSpaceConvertor.convertToARGB(current_image);

            //scale image to produce final version
            if(scaleImage){

                /**if not sheer/rotate, then bicubic*/
                if (isDraft || CTM[1][0] != 0 || CTM[0][1] != 0){

                    if(h>1){

                        boolean imageProcessed=false;

                        //do not use
                        if(1==2 && ( h>32 && w>32 && JAIHelper.isJAIused() && (CTM[0][0]!=0 && CTM[1][1]!=-0))){ //don't use if small or rotation included

                            imageProcessed=true;

                            float iw=CTM[0][0];
                            if(iw==0)
                                iw=(int)CTM[0][1];

                            float ih=CTM[1][1];
                            if(ih==0)
                                ih=(int)CTM[1][0];

                            if(iw<0)
                                iw=-iw;
                            if(ih<0)
                                ih=-ih;

                            float xScale = iw/w;
                            float yScale = ih/h;

                            if(xScale==1.0 && yScale==1.0){

                            }else{

                                long start=System.currentTimeMillis();

                                java.awt.image.renderable.ParameterBlock pb = new java.awt.image.renderable.ParameterBlock();
                                pb.addSource(current_image); // The source image
                                pb.add(xScale);         // The xScale
                                pb.add(yScale);         // The yScale
                                pb.add(0.0F);           // The x translation
                                pb.add(0.0F);           // The y translation
                                pb.add(new javax.media.jai.InterpolationNearest()); // The interpolation

                                try{
                                    current_image = (javax.media.jai.JAI.create("scale", pb, null)).getAsBufferedImage();

                                    //if(timeChange==true)
                                    //	System.out.println("jai="+(System.currentTimeMillis()-start));
                                }catch(Exception ee){
                                    imageProcessed=false;

                                }catch(Error err){
                                    imageProcessed=false;
                                }
                            }

                            if(!imageProcessed) {
                                //System.err.println("Unable to use JAI for image");
                                LogWriter.writeLog("Unable to use JAI for image");
                            }

                        } /**/


                        if(!imageProcessed){
                            //long start=System.currentTimeMillis();

                            boolean failed=false;
                            //allow for odd behaviour on some files
                            try{
                                current_image = invert.filter(current_image,null);
                            }catch(Exception e){
                                failed=true;
                            }
                            if(failed){
                                try{
                                    invert = new AffineTransformOp(image_at,null);
                                    current_image = invert.filter(current_image,null);
                                }catch(Exception e){
                                }
                            }
                            //if(timeChange==true)
                            //	System.out.println("non-jai="+(System.currentTimeMillis()-start));
                        }
                    }
                }else{

                    int dx=1,dy=1;

                    /**workout size and if needs to be inverted*/
                    w=(int)CTM[0][0];
                    if(w==0)
                        w=(int)CTM[0][1];
                    h=(int)CTM[1][1];
                    if(h==0)
                        h=(int)CTM[1][0];

                    if(w<0){
                        w=-w;
                        dx=-1;
                    }
                    if(h<0){
                        h=-h;
                        dy=-1;
                    }

                    /**turn around if needed*/
                    if(dx==-1 || dy==-1){

                        image_at =new AffineTransform();
                        image_at.scale(dx,dy);

                        if(dx==-1)
                            image_at.translate(-current_image.getWidth(),0);
                        if(dy==-1)
                            image_at.translate(0,-current_image.getHeight());

                        //Create the affine operation.
                        invert = new AffineTransformOp(image_at,  ColorSpaces.hints);
                        current_image = invert.filter(current_image,null);

                    }

                    Image scaledImage= current_image.getScaledInstance(w,h,BufferedImage.SCALE_SMOOTH);

                    int type=current_image.getType();
                    if((type==0)|(type==2))
                        type=BufferedImage.TYPE_INT_RGB;

                    current_image = new BufferedImage(scaledImage.getWidth(null),scaledImage.getHeight(null) , type);

                    Graphics2D g2 = current_image.createGraphics();
                    g2.drawImage(scaledImage, 0, 0,null);

                }/***/
            }
        }
    }

	/**
	 * complete image
	 */
	private void completeImage(){

		/**
		 * now workout correct screen co-ords allow for rotation
		 *
		if ((CTM[1][0] == 0) &( (CTM[0][1] == 0))){
			i_w =(int) Math.sqrt((CTM[0][0] * CTM[0][0]) + (CTM[0][1] * CTM[0][1]));
			i_h =(int) Math.sqrt((CTM[1][1] * CTM[1][1]) + (CTM[1][0] * CTM[1][0]));

		}else{
			i_h =(int) Math.sqrt((CTM[0][0] * CTM[0][0]) + (CTM[0][1] * CTM[0][1]));
			i_w =(int) Math.sqrt((CTM[1][1] * CTM[1][1]) + (CTM[1][0] * CTM[1][0]));
		}

		if (CTM[1][0] < 0)
			i_x = (int) (CTM[2][0] + CTM[1][0]);
		else
			i_x = (int) CTM[2][0];

		if (CTM[0][1] < 0) {
			i_y = (int) (CTM[2][1] + CTM[0][1]);
		} else {
			i_y = (int) CTM[2][1];
		}

		//alter to allow for back to front or reversed
		if (CTM[1][1] < 0)
			i_y = i_y - i_h;

		if (CTM[0][0] < 0)
			i_x = i_x - i_w;
*/
		calcCoordinates();
	}

	/**
	 * workout correct screen co-ords allow for rotation
	 */
	private final void calcCoordinates(){

        if (CTM[1][0] == 0 && CTM[0][1] == 0){

			i_x = (int) CTM[2][0];
			i_y = (int) CTM[2][1];

			i_w =(int) CTM[0][0];
			i_h =(int) CTM[1][1];
			if(i_w<0)
				i_w=-i_w;

			if(i_h<0)
				i_h=-i_h;

		}else{ //some rotation/skew
			i_w=(int) (Math.sqrt((CTM[0][0] * CTM[0][0]) + (CTM[0][1] * CTM[0][1])));
			i_h =(int) (Math.sqrt((CTM[1][1] * CTM[1][1]) + (CTM[1][0] * CTM[1][0])));

			if(CTM[1][0]>0 && CTM[0][1]<0){
				i_x = (int) (CTM[2][0]);
				i_y = (int) (CTM[2][1]+CTM[0][1]);
				//System.err.println("AA "+i_w+" "+i_h);

			}else if(CTM[1][0]<0 && CTM[0][1]>0){
				i_x = (int) (CTM[2][0]+CTM[1][0]);
				i_y = (int) (CTM[2][1]);
				//System.err.println("BB "+i_w+" "+i_h);
                
            }else if(CTM[1][0]>0 && CTM[0][1]>0){
				i_x = (int) (CTM[2][0]);
				i_y = (int) (CTM[2][1]);
				//System.err.println("CC "+i_w+" "+i_h);
			}else{
				//System.err.println("DD "+i_w+" "+i_h);
				i_x = (int) (CTM[2][0]);
				i_y =(int) (CTM[2][1]);
			}

		}

		//System.err.println(i_x+" "+i_y+" "+i_w+" "+i_h);
		//Matrix.show(CTM);
		//alter to allow for back to front or reversed
		if ( CTM[1][1]< 0)
			i_y = i_y - i_h;
		if ( CTM[0][0]< 0)
			i_x = i_x - i_w;

//ShowGUIMessage.showGUIMessage("",current_image,"xx="+i_x+" "+i_y+" "+i_w+" "+i_h+" h="+current_image.getHeight());


	}

	/**
	 * get y of image (x1,y1 is top left)
	 */
	final public int getImageY() {
		return i_y;
	}

	/**
	 * get image
	 */
	final public BufferedImage getImage() {
		return current_image;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get width of image
	 */
	final public int getImageW() {
		return i_w;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get height of image
	 */
	final public int getImageH() {
		return i_h;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get X of image (x,y is top left)
	 */
	final public int getImageX() {
		return i_x;
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * clip the image
	 */
	final public void clipImage(Area current_shape) {

		//create a copy of clip (so we don't alter clip)
		Area final_clip = (Area) current_shape.clone();

		//if not working at 72 dpi, alter clip to fit
		if(scale!=1){
			AffineTransform af=AffineTransform.getScaleInstance(scale,scale);
			final_clip.transform(af);
		}

		//actual size so we can trap any rounding error
		int image_w = current_image.getWidth();
		int image_h = current_image.getHeight();

		//shape of final image
		double shape_x = final_clip.getBounds2D().getX();
		double shape_y = final_clip.getBounds2D().getY();
		double shape_h = final_clip.getBounds2D().getHeight();
		double d_y = (image_h - shape_h);
		AffineTransform upside_down = new AffineTransform();
		upside_down.translate(-shape_x, -shape_y); //center
		upside_down.scale(1, -1); //reflect in x axis
		upside_down.translate(shape_x, - (shape_y + shape_h));
		final_clip.transform(upside_down);

		//line up to shape
		AffineTransform align_clip = new AffineTransform();

		//if not working at 72 dpi, alter clip to fit
		align_clip.translate(-i_x*scale, (i_y*scale) + d_y);
		final_clip.transform(align_clip);

		//co-ords of transformed shape
		//reset sizes to remove area clipped
		double x = final_clip.getBounds2D().getX();
		double y = final_clip.getBounds2D().getY();
		double w = final_clip.getBounds2D().getWidth();
		double h = final_clip.getBounds2D().getHeight();

		//get type of image used
		image_type = current_image.getType();

		//set type so ICC and RGB uses ARGB
		if ((image_type == 0))
			image_type = BufferedImage.TYPE_INT_ARGB; //
		else if ((image_type == BufferedImage.TYPE_INT_RGB))
			image_type = BufferedImage.TYPE_INT_ARGB; //

		//draw image onto graphic (with clip) and then re-extract
		BufferedImage offscreen =
			new BufferedImage(image_w, image_h, image_type);
		//image of  'canvas'
		Graphics2D image_g2 = offscreen.createGraphics(); //g2 of canvas

		//if not transparent make background white
		if (offscreen.getColorModel().hasAlpha() == false) {
			image_g2.setBackground(Color.white);
			image_g2.fill(new Rectangle(0, 0, image_w, image_h));
		}

		image_g2.setClip(final_clip);

		try {
			//redraw image clipped and extract as rectangular shape
			image_g2.drawImage(current_image, null,0, 0);
		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " plotting clipping image");
		}

		//get image (now clipped )

		//check for rounding errors
		if (y < 0) {
			h = h + y;
			y = 0;
		}
		if (x < 0) {
			w = w + x;
			x = 0;
		}
		if (w > image_w)
			w = image_w;
		if (h > image_h)
			h = image_h;
		if (y + h > image_h)
			h = image_h - y;
		if (x + w > image_w)
			w = image_w - x;

		try {
			current_image = offscreen.getSubimage((int)x, (int)y, (int)(w), (int)(h));
		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " extracting clipped image with values x="+x+" y="+y+" w="+w+" h="+h+" from image ");
		}

		//work out new co-ords from shape and current
		double x1, y1;
		if (i_x > shape_x)
			x1 = i_x;
		else
			x1 = shape_x;
		if (i_y > shape_y)
			y1 = i_y;
		else
			y1 = shape_y;

		i_x = (int) (x1/scale);
		i_y = (int) (y1/scale);
		i_w = (int) w;
		i_h = (int) h;

	}
}
