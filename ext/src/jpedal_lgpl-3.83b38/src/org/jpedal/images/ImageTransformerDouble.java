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
* ImageTransformerDouble.java
* ---------------
*/
package org.jpedal.images;

import org.jpedal.PdfDecoder;
import org.jpedal.color.ColorSpaces;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * class to shrink and clip an extracted image
 * On reparse just calculates co-ords
 */
public class ImageTransformerDouble {
	
	double ny=0,nx=0;

    /**the clip*/
	private Area clip=null;

	/**holds the actual image*/
	private BufferedImage current_image;

	/**matrices used in transformation*/
	private float[][] Trm,Trm1, Trm2,CTM;

	/**image co-ords*/
	private int i_x = 0, i_y = 0, i_w = 0, i_h = 0;
	
	/**dpi scale factor*/
	private float scale=1;

	private boolean scaleImage;

	/**flag to show image clipped*/
	private boolean hasClip=false;
	
	/**tell software to remove unneeded border on hires clip*/
	private boolean removeClippedBorder=true;


    /**
	 * pass in image information and apply transformation matrix
	 * to image
	 */
	public ImageTransformerDouble(
		int dpi,
		GraphicsState currentGS,
		BufferedImage new_image,
		boolean scaleImage,boolean removeClippedBorder) {
		
		this.removeClippedBorder=removeClippedBorder;
		
		/**
		 * set values
		 */
		//set scaling for final image
		scale=dpi/72;
		
		//save global values
		this.current_image = new_image;
		this.scaleImage=scaleImage;
		
		CTM = currentGS.CTM; //local copy of CTM

		createMatrices();
		
		// get clipped image and co-ords
		if(currentGS.getClippingShape()!=null)
			clip = (Area) currentGS.getClippingShape().clone();
		
		calcCoordinates();

    }
	
	/**
     * applies the shear/rotate of a double transformation to the clipped image
	*/
	final public void doubleScaleTransformShear(boolean alreadyTurned){

        float[][] Trm1=this.Trm1;

        boolean isSwapped=false;

        //correct rotation
        if(PdfDecoder.clipOnMac && PdfDecoder.isRunningOnMac && Trm1[0][0]==0 && Trm1[1][1]==0 && Trm1[0][1]<0 && Trm1[1][0]>0){

            isSwapped=true;
            Trm1[0][0]=-Trm1[0][1];
            Trm1[0][1]=0;
            Trm1[1][1]=Trm1[1][0];
            Trm1[1][0]=0;

        }

        scale(Trm1);

        current_image = ColorSpaceConvertor.convertToARGB(current_image);

        //create a copy of clip (so we don't alter clip)
		if(!isSwapped && clip!=null){
			Area final_clip = (Area) clip.clone();

			clipImage(true,final_clip,alreadyTurned);

			i_x=(int)clip.getBounds2D().getMinX();
			i_y=(int) clip.getBounds2D().getMinY();
			i_w=(int)((clip.getBounds2D().getMaxX())-i_x);
			i_h=(int)((clip.getBounds2D().getMaxY())-i_y);
		}
    }
	
	/**
	 * applies the scale of a double transformation to the clipped image
	 */
	final public void doubleScaleTransformScale(){
		
		if((CTM[0][0]!=0.0)&(CTM[1][1]!=0.0))
			scale(Trm2);
		//clipImage(true);
		
	}
	
	/**complete image and workout co-ordinates*/
	final public void completeImage(){
		
		//Matrix.show(CTM);
		
		/*if((CTM[0][1]>0 )&(CTM[1][0]>0 )){
			//ShowGUIMessage.showGUIMessage("",current_image,"a ");
			AffineTransform image_at =new AffineTransform();
			image_at.scale(-1,-1);
			image_at.translate(-current_image.getWidth(),-current_image.getHeight());
			AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);
			
			current_image = invert.filter(current_image,null);
			
		}*/
		
		//ShowGUIMessage.showGUIMessage("",current_image,"a ");
		
		/**/
		if(hasClip){
			i_x=(int)clip.getBounds2D().getMinX();
			i_y=(int) clip.getBounds2D().getMinY();
			i_w=(current_image.getWidth());
			i_h=(current_image.getHeight());
			
			//System.out.println(current_image.getWidth()+" "+current_image.getHeight());
			//System.out.println(i_x+" "+i_y+" "+i_w+" "+i_h+" "+clip.getBounds2D());
			
		}/***/
		
	}
	
		/**scale image to size*/
		private void scale(float[][] Trm){
				
			/**
			 * transform the image only if needed
			 */
			if ((Trm[0][0] != 1.0)|| (Trm[1][1] != 1.0)|| (Trm[0][1] != 0.0)
					|| (Trm[1][0] != 0.0)||(scale!=1)) {
	
				int w = current_image.getWidth(); //raw width
				int h = current_image.getHeight(); //raw height
				
				//workout transformation for the image
				AffineTransform image_at =new AffineTransform(Trm[0][0]*scale,-Trm[0][1]*scale,-Trm[1][0]*scale,Trm[1][1]*scale,0,0);
				
				//apply it to the shape first so we can align
				Area r =new Area(new Rectangle(0,0,w,h));
				r.transform(image_at);
				
				//make sure it fits onto image (must start at 0,0)
				ny = r.getBounds2D().getY();
				nx = r.getBounds2D().getX();
				image_at =new AffineTransform(Trm[0][0]*scale,-Trm[0][1]*scale,-Trm[1][0]*scale,Trm[1][1]*scale,-nx,-ny);
				
				//Create the affine operation.
				//ColorSpaces.hints causes single lines to vanish);
				AffineTransformOp invert;
				if((w>10)&(h>10))
					invert = new AffineTransformOp(image_at,  ColorSpaces.hints);
				else
					invert = new AffineTransformOp(image_at,  null);

				//scale image to produce final version
				if(scaleImage)
					current_image = invert.filter(current_image,null);				
				
			}
			
		}

		/**workout the transformation as 1 or 2 transformations*/
		private void createMatrices(){
			
			int w = current_image.getWidth(); //raw width
			int h = current_image.getHeight(); //raw height

			//build transformation matrix by hand to avoid errors in rounding
			Trm = new float[3][3];
			Trm[0][0] = (CTM[0][0] / w);
			Trm[0][1] = (CTM[0][1] / w);
			Trm[0][2] = 0;
			Trm[1][0] = (CTM[1][0] / h);
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
			
			/**now work out as 2 matrices*/
			Trm1=new float[3][3];
			Trm2=new float[3][3];
			
			//used to handle sheer
			float x1=1,x2=0,y1=1,y2=0;
			
			x1=CTM[0][0];
			if(x1<0)
				x1=-x1;
			x2=CTM[0][1];
			if(x2<0)
				x2=-x2;
			
			y1=CTM[1][1];
			if(y1<0)
				y1=-y1;
			y2=CTM[1][0];
			if(y2<0)
				y2=-y2;

            //factor out scaling to produce just the sheer/rotation
			if((CTM[0][0]==0.0)|(CTM[1][1]==0.0)){
				Trm1=Trm;

            }else if((CTM[0][1]==0.0)&&(CTM[1][0]==0.0)){

				Trm1[0][0] = w/(CTM[0][0]);
				Trm1[0][1] = 0;
				Trm1[0][2] = 0;
			
				Trm1[1][0] =0;
				Trm1[1][1] = h/(CTM[1][1]);
				Trm1[1][2] = 0;
				
				Trm1[2][0] = 0;
				Trm1[2][1] = 0;
				Trm1[2][2] = 1;


				Trm1=Matrix.multiply(Trm,Trm1);

				//round numbers if close to 1
				for (int y = 0; y < 3; y++) {
					for (int x = 0; x < 3; x++) {
						if ((Trm1[x][y] > .99) & (Trm1[x][y] < 1))
							Trm1[x][y] = 1;
					}
				}
			}else{ //its got sheer/rotation
				
				if(x1>x2)
					Trm1[0][0] = w/(CTM[0][0]);
				else
					Trm1[0][0] = w/(CTM[0][1]);
				if (Trm1[0][0]<0)
					Trm1[0][0]=-Trm1[0][0];
				Trm1[0][1] = 0;
				Trm1[0][2] = 0;
			
				Trm1[1][0] =0;
				
				if(y1>y2)
					Trm1[1][1] = h/(CTM[1][1]);
				else
					Trm1[1][1] = h/(CTM[1][0]);
				if (Trm1[1][1]<0)
					Trm1[1][1]=-Trm1[1][1];
				Trm1[1][2] = 0;
				
				Trm1[2][0] = 0;
				Trm1[2][1] = 0;
				Trm1[2][2] = 1;
				
				
				Trm1=Matrix.multiply(Trm,Trm1);
				
				//round numbers if close to 1
				for (int y = 0; y < 3; y++) {
					for (int x = 0; x < 3; x++) {
						if ((Trm1[x][y] > .99) & (Trm1[x][y] < 1))
							Trm1[x][y] = 1;
					}
				}
			}

            //create a transformation with just the scaling
			if(x1>x2)
				Trm2[0][0] = (CTM[0][0] / w);
			else
				Trm2[0][0] = (CTM[0][1] / w);
			
			if(Trm2[0][0] <0)
				Trm2[0][0] =-Trm2[0][0] ;
			Trm2[0][1] = 0;
			Trm2[0][2] = 0;
			Trm2[1][0] = 0;
			if(y1>y2)
				Trm2[1][1] = (CTM[1][1] / h);
			else
				Trm2[1][1] = (CTM[1][0] / h);
			
			if(Trm2[1][1] <0)
				Trm2[1][1] =-Trm2[1][1] ;
			
			Trm2[1][2] = 0;
			Trm2[2][0] = 0;
			Trm2[2][1] = 0;
			Trm2[2][2] = 1;
			
			//round numbers if close to 1
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					if ((Trm2[x][y] > .99) & (Trm2[x][y] < 1))
						Trm2[x][y] = 1;
				}
			}
        }
		
		/**
		 * workout correct screen co-ords allow for rotation
		 */
		private final void calcCoordinates(){
			
			if ((CTM[1][0] == 0) &( (CTM[0][1] == 0))){
				
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
				
				if((CTM[1][0]>0)&(CTM[0][1]<0)){
					i_x = (int) (CTM[2][0]);
					i_y = (int) (CTM[2][1]+CTM[0][1]);
					//System.err.println("AA "+i_w+" "+i_h);
					
				}else if((CTM[1][0]<0)&(CTM[0][1]>0)){
					i_x = (int) (CTM[2][0]+CTM[1][0]);
					i_y = (int) (CTM[2][1]);
					//System.err.println("BB "+i_w+" "+i_h);
				}else if((CTM[1][0]>0)&(CTM[0][1]>0)){
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
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get y of image (x1,y1 is top left)
	 */
	final public int getImageY() {
		return i_y;
	}
	//////////////////////////////////////////////////////////////////////////
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
	
			/**
			 * clip the image
			 */
			final private void clipImage(boolean singleScale, Area final_clip, boolean alreadyTurned) {

				//if not working at 72 dpi, alter clip to fit
				if(scale!=1){
					AffineTransform af=AffineTransform.getScaleInstance(scale,scale);
					final_clip.transform(af);
				}
				/**
				 * invert the clip
				 */
				double shape_x = final_clip.getBounds2D().getX();
				double shape_y = final_clip.getBounds2D().getY();
				double shape_h = final_clip.getBounds2D().getHeight();

				int image_w = current_image.getWidth();
				int image_h = current_image.getHeight();

				AffineTransform image_at =new AffineTransform();
				image_at.scale(1,-1);
				image_at.translate(0,-current_image.getHeight());
				AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);

				current_image = invert.filter(current_image,null);

                AffineTransform align_clip = new AffineTransform();

				double dx=-(CTM[2][0])*scale,dy= -((CTM[2][1])*scale);

				if(CTM[1][0]<0)
					dx=dx-CTM[1][0] ;
				if((CTM[0][0]<0)&&(CTM[1][0]>=0))
					dx=dx-CTM[1][0] ;

				if(CTM[0][1]<0)
					dy=dy-CTM[0][1];
				if(CTM[1][1]<0){
					if(CTM[0][1]>0)
						dy=dy-CTM[0][1];
					else if(CTM[1][1]<0)
						dy=dy-CTM[1][1];
				}
				align_clip.translate(dx,dy );

				final_clip.transform(align_clip);

				AffineTransform invert2=new AffineTransform(1/Trm2[0][0],0,0,1/Trm2[1][1],0,0);
				//System.out.println("invert2="+invert2);

                if((!alreadyTurned)||((Trm[1][0]==0)&&(Trm[0][1]==0)))
                final_clip.transform(invert2);

				//co-ords of transformed shape
				//reset sizes to remove area clipped
				int x = (int) final_clip.getBounds().getX();
				int y = (int) final_clip.getBounds().getY();
				int w = (int) final_clip.getBounds().getWidth();
				int h = (int) final_clip.getBounds().getHeight();

                //fix for 'mirror' image on Mac
                if(x<0)
                final_clip.transform(AffineTransform.getTranslateInstance(-x,0));

                /**
				 * create inverse of clip and paint on to add transparency
				 */
				Area inverseClip =new Area(new Rectangle(0,0,image_w,image_h));
				inverseClip.exclusiveOr(final_clip);
				Graphics2D image_g2 = current_image.createGraphics(); //g2 of canvas
				image_g2.setComposite(AlphaComposite.Clear);
				image_g2.fill(inverseClip);

				AffineTransform image_at2 =new AffineTransform();
				image_at2.scale(1,-1);
				image_at2.translate(0,-current_image.getHeight());
				AffineTransformOp invert3= new AffineTransformOp(image_at2,  ColorSpaces.hints);

                //mac os needs this stage!
				current_image=current_image.getSubimage(0,0,current_image.getWidth(),current_image.getHeight());

                current_image = invert3.filter(current_image,null);

                //Matrix.show(CTM);


                //get image (now clipped )

				//if(count>21)
				//ShowGUIMessage.showGUIMessage("2count="+count,current_image,"2count="+count);
				//count++;

				//check for rounding errors
				if (y < 0) {
					h = h - y;
					y = 0;
				//	System.err.println("Negative");

				}else{

					y=image_h-h-y;

					//allow for fp error
					if(y<0)
						y=0;

				}

				if (x < 0) {
					w = w - x;
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

                //extract if needed
				if(removeClippedBorder){

					if((x==0)&&(y==0)&&(w==current_image.getWidth())&&(h==current_image.getHeight())){
						//dont bother if no change
					}else{
						try {
							current_image = current_image.getSubimage(x, y, w, h);
						} catch (Exception e) {
							LogWriter.writeLog("Exception " + e + " extracting clipped image with values x="+x+" y="+y+" w="+w+" h="+h+" from image "+current_image);


						}catch(Error err){
						}
					}
				}

				/***/

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

				i_x = (int) x1;
				i_y = (int) y1;
				i_w = w;
				i_h =  h;
			}
	
}
