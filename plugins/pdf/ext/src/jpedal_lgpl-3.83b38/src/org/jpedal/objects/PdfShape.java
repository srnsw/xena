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
* PdfShape.java
* ---------------
*/
package org.jpedal.objects;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.Serializable;

import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_Int;

/**
 * <p>
 * defines the current shape which is created by command stream
 * </p>
 * <p>
 * <b>This class is NOT part of the API </b>
 * </p>. Shapes can be drawn onto pdf or used as a clip on other
 * image/shape/text. Shape is built up by storing commands and then turning
 * these commands into a shape. Has to be done this way as Winding rule is not
 * necessarily declared at start.
 */
public class PdfShape implements Serializable
{

    /**we tell user we have not used some shapes only ONCE*/
	private Vector_Float shape_primitive_x2 = new Vector_Float( 1000 );
	private Vector_Float shape_primitive_y = new Vector_Float( 1000 );

	/**store shape currently being assembled*/
	private Vector_Int shape_primitives = new Vector_Int( 1000 );

	/**type of winding rule used to draw shape*/
	private int winding_rule = GeneralPath.WIND_NON_ZERO;
	private Vector_Float shape_primitive_x3 = new Vector_Float( 1000 );
	private Vector_Float shape_primitive_y3 = new Vector_Float( 1000 );

	/**used when trying to choose which shapes to use to test furniture*/
	private Vector_Float shape_primitive_y2 = new Vector_Float( 1000 );
	private Vector_Float shape_primitive_x = new Vector_Float( 1000 );
	private static final int H = 3;
	private static final int L = 2;
	private static final int V = 6;

	/**flags for commands used*/
	private static final int M = 1;
	private static final int Y = 4;
	private static final int C = 5;
	
    /////////////////////////////////////////////////////////////////////////
	/**
	 * end a shape, storing info for later
	 */
	final public void closeShape()
	{
		shape_primitives.addElement( H );

		//add empty values
		shape_primitive_x.addElement( 0 );
		shape_primitive_y.addElement( 0 );
		shape_primitive_x2.addElement( 0 );
		shape_primitive_y2.addElement( 0 );
		shape_primitive_x3.addElement( 0 );
		shape_primitive_y3.addElement( 0 );
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * add a curve to the shape
	 */
	final public void addBezierCurveC( float x, float y, float x2, float y2, float x3, float y3 )
	{
		shape_primitives.addElement( C );
		shape_primitive_x.addElement( x );
		shape_primitive_y.addElement( y );

		//add empty values to keep in sync
		//add empty values
		shape_primitive_x2.addElement( x2 );
		shape_primitive_y2.addElement( y2 );
		shape_primitive_x3.addElement( x3 );
		shape_primitive_y3.addElement( y3 );
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * set winding rule - non zero
	 */
	final public void setNONZEROWindingRule()
	{
		winding_rule = GeneralPath.WIND_NON_ZERO;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * add a line to the shape
	 */
	final public void lineTo( float x, float y )
	{
		shape_primitives.addElement( L );
		shape_primitive_x.addElement( x );
		shape_primitive_y.addElement( y );

		//add empty values to keep in sync
		//add empty values
		shape_primitive_x2.addElement( 0 );
		shape_primitive_y2.addElement( 0 );
		shape_primitive_x3.addElement( 0 );
		shape_primitive_y3.addElement( 0 );
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * add a curve to the shape
	 */
	final public void addBezierCurveV( float x2, float y2, float x3, float y3 )
	{
		shape_primitives.addElement( V );
		shape_primitive_x.addElement( 200 );
		shape_primitive_y.addElement( 200 );

		//add empty values to keep in sync
		//add empty values
		shape_primitive_x2.addElement( x2 );
		shape_primitive_y2.addElement( y2 );
		shape_primitive_x3.addElement( x3 );
		shape_primitive_y3.addElement( y3 );
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * turn shape commands into a Shape object, storing info for later. Has to
	 * be done this way because we need the winding rule to initialise the shape
	 * in Java, and it could be set awywhere in the command stream
	 */
	final public Shape generateShapeFromPath( Area clip,float[][] CTM, boolean is_clip,PageLines pageLines,
                                              boolean isFill,PdfPaint paint,float thickness,float pageWidth){

        //create the shape - we have to do it this way
		//because we get the WINDING RULE last and we need it
		//to initialise the shape
		GeneralPath current_path = null;
		Area current_area = null;
		Shape current_shape = null;

        //init points
		float[] x = shape_primitive_x.get();
		float[] y = shape_primitive_y.get();
		float[] x2 = shape_primitive_x2.get();
		float[] y2 = shape_primitive_y2.get();
		float[] x3 = shape_primitive_x3.get();
		float[] y3 = shape_primitive_y3.get();
		int[] command=shape_primitives.get();
		int i = 0;
		float lx=x[0],ly=y[0];
		int end = shape_primitives.size() - 1;

        //used to deebug
		boolean show = false;

        //loop through commands and add to shape
		while( i < end ){
			if( current_path == null ){
				current_path = new GeneralPath( winding_rule );
				current_path.moveTo( x[i], y[i] );
				lx=x[i];
				ly=y[i];
				if( show == true )
					LogWriter.writeLog( "==START=" + x + ' ' + y );
			}

			//only used to create clips
			if(command[i]== H ){

                current_path.closePath();
                if( is_clip == true ){

				    //current_path.lineTo(xs,ys);
					//current_path.closePath();
					if( show == true )
						LogWriter.writeLog( "==H\n\n"+current_area+ ' ' +current_path.getBounds2D() + ' ' +new Area( current_path ).getBounds2D());

					if( current_area == null ){
						current_area = new Area( current_path );

						//trap for apparent bug in Java where small paths create a 0 size Area
						if((current_area.getBounds2D().getWidth()<=0.0)||
						(current_area.getBounds2D().getHeight()<=0.0))
						current_area=new Area(current_path.getBounds2D());

					}else
						current_area.add( new Area( current_path ) );
					current_path = null;
				}else if( show == true )
						LogWriter.writeLog( "close shape "+command[i]+" i="+i);

			}

            if( command[i]== L ){

                current_path.lineTo( x[i], y[i] );

                /**add any valid vertical or horizontal lines*/
                if((pageLines!=null)){

                    //factor in scaling
                    float w=Math.abs(lx-x[i]);
                    if(CTM[0][0]!=0)
                        w=w*CTM[0][0];
                    else
                        w=w*CTM[1][0];

                    float h=Math.abs(ly-y[i]);
                    if(CTM[1][1]!=0)
                        h=h*CTM[1][1];
                    else
                        h=h*CTM[0][1];

                    //lines as possible breaks
                    if((!isFill)){

                        double pageRatio=0;
                        if(pageWidth>0)
                            pageRatio=w/pageWidth;

                        if((w>=1)&&(w<2)&&(h>8)){
                            pageLines.addVerticalLine(lx,ly,x[i],y[i]);

                            /**
                             if(lx>100 && lx<300){
                             System.out.println("=====================================");
                             System.out.println(w+" "+(Math.abs(lx-x[i]))+" "+h+" "+Math.abs(ly-y[i]));
                             System.out.println(CTM[0][0]+" "+CTM[0][1]);
                             System.out.println(CTM[1][0]+" "+CTM[1][1]);
                             System.out.println(CTM[2][0]+" "+CTM[2][1]);
                             }*/
                        }else if((h>=1)&&(h<2)&&(w>50))
                            pageLines.addHorizontalLine(lx,ly,x[i],y[i]);
                        else if((h<1)&&(pageRatio<=1.0 && pageRatio>0.8) && thickness>0.25)  //pick out key lines
                            pageLines.addHorizontalLine(lx,ly,x[i],y[i]);

                        //stroke lines for ads
                        /**if((lx<120|| x[i]<120 )&&(lx>100|| x[i]>100 )&&(ly<5 || y[i]<5)){
                        System.out.println(lx+" "+ly+" "+x[i]+" "+y[i]+" "+paint);
                            if(clip!=null)
                            System.out.println("clip="+clip.getBounds());
                            Matrix.show(CTM);
                        }  */
                        pageLines.addPossibleLineEnclosingAdvert((int)lx,(int)x[i],(int)ly,(int)y[i],(int)w,(int)h);


                    }else if(h>50 && w<1)
                        pageLines.addVerticalLine(lx,ly,x[i],y[i]);
                        
                }

                lx=x[i];
                ly=y[i];
                if( show == true )
                    LogWriter.writeLog( "==L" + x[i] + ',' + y[i] + "  " );
            }else if( command[i] == M ){
                current_path.moveTo( x[i], y[i] );
                lx=x[i];
                ly=y[i];
                if( show == true )
                    LogWriter.writeLog( "==M" + x[i] + ',' + y[i] + "  " );
            }else{
                //cubic curves which use 2 control points
                if( command[i] == Y ){
                    if( show == true )
                        LogWriter.writeLog( "==Y " + x[i] + ' ' + y[i] + ' ' + x3[i] + ' ' + y3[i] + ' ' + x3[i] + ' ' + y3[i] );
                    current_path.curveTo( x[i], y[i], x3[i], y3[i], x3[i], y3[i] );
                    lx=x3[i];
                    ly=y3[i];

                }else if( command[i] == C ){
                    if( show == true )
                        LogWriter.writeLog( "==C " + x + ' ' + y + ' ' + x2 + ' ' + y2 + ' ' + x3 + ' ' + y3 );
                    current_path.curveTo( x[i], y[i], x2[i], y2[i], x3[i], y3[i] );
                    lx=x3[i];
                    ly=y3[i];
                }else if( command[i] == V ){
                    float c_x = (float)current_path.getCurrentPoint().getX();
                    float c_y = (float)current_path.getCurrentPoint().getY();
                    if( show == true )
                        LogWriter.writeLog( "==v " + c_x + ',' + c_y + ',' + x2[i] + ',' + y2[i] + ',' + x3[i] + ',' + y3[i] );
                    current_path.curveTo( c_x, c_y, x2[i], y2[i], x3[i], y3[i] );
                    lx=x3[i];
                    ly=y3[i];
                }
            }
			i++;
		}

        if((current_path!=null)&&(current_path.getBounds().getHeight()==0))
            if(thickness>1 && current_path.getBounds2D().getWidth()<1){
                current_path.moveTo(0,-thickness/2);
                current_path.lineTo(0,thickness/2);
            }else
                current_path.moveTo(0,1);

        if((current_path!=null)&&(current_path.getBounds().getWidth()==0))
            current_path.moveTo(1,0);

        //transform matrix only if needed
		if((CTM[0][0] == (float)1.0)&&(CTM[1][0] == (float)0.0)&&
		(CTM[2][0] == (float)0.0)&&(CTM[0][1] == (float)0.0)&&
		(CTM[1][1] == (float)1.0)&&(CTM[2][1] == (float)0.0)&&
		(CTM[0][2] == (float)0.0)&&(CTM[1][2] == (float)0.0)&&(CTM[2][2] == (float)1.0)){
			//don't waste time if not needed
		}else{
			AffineTransform CTM_transform = new AffineTransform( CTM[0][0], CTM[0][1], CTM[1][0], CTM[1][1], CTM[2][0], CTM[2][1]);

			//apply CTM alterations
			if( current_path != null ){

				//transform
				current_path.transform( CTM_transform );

			}else if( current_area != null )
				current_area.transform( CTM_transform );
		}
        //set to current or clip
		if( is_clip == false ){
			if( current_area == null )
				current_shape = current_path;
			else
				current_shape = current_area;
		}else
			current_shape = current_area;

        if((pageLines!=null)&&(current_shape!=null)){

            Rectangle outline=current_shape.getBounds();


			int x1=outline.x;
			int y1=outline.y;
            int mx=(int)outline.getMaxX();
			int my=(int)outline.getMaxY();
            //factor in clip
            if(clip!=null){
                int clipX=clip.getBounds().x;
                int clipY=clip.getBounds().y;
                int clipW=(int)clip.getBounds().getMaxX();
                int clipH=(int)clip.getBounds().getMaxY();

                if(x1<clipX)
                x1=clipX;
                if(y1<clipY)
                y1=clipY;

                if(mx>clipW)
                mx=clipW;
                if(my>clipH)
                my=clipH;
            }

            int w=mx-x1;
			int h=my-y1;

            if((w<5)&&(h<5)) //dotted lines
				pageLines.addPossiblePartLine(x1,y1,x1+w,y1+h);
			else if(isFill){

                if(w>8 && h>8){//save coloured boxes which hold linked items (assume sensible smallest point size) and not just white
					PdfColor col=null;//(PdfColor)paint;
                    if(paint  instanceof PdfColor)
						col=(PdfColor)paint;
                    //ignore white

                    //System.out.println(col+" "+x1+y1+""+w+" "+w);
                    if(col!=null){// && col.getRed()>5 && col.getBlue()>5 &&col.getBlue()>5){

                        boolean isWhite=col.getRed()>253 && col.getBlue()>253 &&col.getBlue()>253;//col.isWhite();

                        if(isWhite){

                        }else
                            pageLines.addBox(x1,y1,(x1+w),(y1+h),col);

                    }
				}else if((w<4)&&(h>8))
					pageLines.addVerticalLine(x1,y1,(x1+w),(y1+h));
				else if((h<6)&&(w>50))
					pageLines.addHorizontalLine(x1,y1,(x1+w),(y1+h));
			}//else if(!isFill && w>80 && h>80){
                //pageLines.addVerticalLine(x1,y1,x1,(y1+h));

            //}
		}

        //lines for ads
        if(pageLines!=null && current_shape!=null && isFill){


            int x1=current_shape.getBounds().x;
            int y1=current_shape.getBounds().y;
            int w=current_shape.getBounds().width;
            int h=current_shape.getBounds().height;

            pageLines.addPossibleLineEnclosingAdvert(x1,x1+w,(y1+h),y1,w,h);

        }

        return current_shape;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * add a rectangle to set of shapes
	 */
	final public void appendRectangle( float x, float y, float w, float h )
	{
		moveTo( x, y );
		lineTo( x + w, y );
		lineTo( x + w, y + h );
		lineTo( x, y + h );
		lineTo( x, y );
		closeShape();
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * start a shape by creating a shape object
	 */
	final public void moveTo( float x, float y )
	{
		shape_primitives.addElement( M );
		shape_primitive_x.addElement( x );
		shape_primitive_y.addElement( y );

		//add empty values
		shape_primitive_x2.addElement( 0 );
		shape_primitive_y2.addElement( 0 );
		shape_primitive_x3.addElement( 0 );
		shape_primitive_y3.addElement( 0 );

        //delete lines for grouping over boxes
    }

	/**
	 * add a curve to the shape
	 */
	final public void addBezierCurveY( float x, float y, float x3, float y3 )
	{
		shape_primitives.addElement( Y );
		shape_primitive_x.addElement( x );
		shape_primitive_y.addElement( y );

		//add empty values to keep in sync
		//add empty values
		shape_primitive_x2.addElement( 0 );
		shape_primitive_y2.addElement( 0 );
		shape_primitive_x3.addElement( x3 );
		shape_primitive_y3.addElement( y3 );
	}
	
	/**
	 * reset path to empty
	 */
	final public void resetPath()
	{
		//reset the store
		shape_primitives.clear();
		shape_primitive_x.clear();
		shape_primitive_y.clear();
		shape_primitive_x2.clear();
		shape_primitive_y2.clear();
		shape_primitive_x3.clear();
		shape_primitive_y3.clear();

		//and reset winding rule
		winding_rule = GeneralPath.WIND_NON_ZERO;
    }
	///////////////////////////////////////////////////////////////////////////
	/**
	 * set winding rule - even odd
	 */
	final public void setEVENODDWindingRule()
	{
		winding_rule = GeneralPath.WIND_EVEN_ODD;
	}
	
	/**
	 * show the shape segments for debugging
	 */
	static final private void showShape( Shape current_shape )
	{
		PathIterator xx = current_shape.getPathIterator( null );
		double[] coords = new double[6];
		while( xx.isDone() == false )
		{
			int type = xx.currentSegment( coords );
			xx.next();
			switch( type )
			{
			  case PathIterator.SEG_MOVETO:
				LogWriter.writeLog( "MoveTo" + coords[0] + ' ' + coords[1] );
				if( ( coords[0] == 0 ) & ( coords[1] == 0 ) )
					LogWriter.writeLog( "xxx" );
				break;

			  case PathIterator.SEG_LINETO:
				LogWriter.writeLog( "LineTo" + coords[0] + ' ' + coords[1] );
				if( ( coords[0] == 0 ) & ( coords[1] == 0 ) )
					LogWriter.writeLog( "xxx" );
				break;

			  case PathIterator.SEG_CLOSE:
				LogWriter.writeLog( "CLOSE" );
				break;

			  default:
				LogWriter.writeLog( "Other" + coords[0] + ' ' + coords[1] );
				break;
			}
		}
	}

    /**
     * number of segments in current shape (0 if no shape or none)
     */
    public int getSegmentCount() {

        if( shape_primitives==null)
            return 0;
        else
            return shape_primitives.size() - 1;  
    }
}
