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
* PageLines.java
* ---------------
*/
package org.jpedal.objects;

import java.awt.*;
import java.io.*;

import org.jpedal.color.PdfColor;
import org.jpedal.utils.repositories.*;

/**
 * @author markee
 *
 * holds data on lines on the page - used internally by IDR (not part of API)
 */
public class PageLines implements Serializable {

    private int initSize=20;

    //co-ords of tiny lines which may fit together
    private Vector_Int t_x1 = new Vector_Int(initSize);
    private Vector_Int t_x2 = new Vector_Int(initSize);
    private Vector_Int t_y1 = new Vector_Int(initSize);
    private Vector_Int t_y2 = new Vector_Int(initSize);

    //co-ords of filled boxes
    int boxCount=0;

    public Vector_Float box_x1 = new Vector_Float(initSize);

    public Vector_Float box_y1 = new Vector_Float(initSize);

    public Vector_Float box_x2 = new Vector_Float(initSize);

    public Vector_Float box_y2 = new Vector_Float(initSize);

    private Vector_Rectangle box_shape = new Vector_Rectangle(initSize);

    private Vector_Object box_col = new Vector_Object(initSize);

    private Vector_boolean box_isLive = new Vector_boolean(initSize);

    private Vector_boolean box_isComposite = new Vector_boolean(initSize);

    //co-ords of lines
    private Vector_Float v_x1 = new Vector_Float(initSize);

    private Vector_Float v_x2 = new Vector_Float(initSize);

    private Vector_Float v_y1 = new Vector_Float(initSize);

    private Vector_Float v_y2 = new Vector_Float(initSize);

    private Vector_Rectangle v_shape = new Vector_Rectangle(initSize);

    private Vector_Float h_x1 = new Vector_Float(initSize);

    private Vector_boolean h_isLive = new Vector_boolean(initSize);

    private Vector_Float h_x2 = new Vector_Float(initSize);

    private Vector_Float h_y1 = new Vector_Float(initSize);

    private Vector_Float h_y2 = new Vector_Float(initSize);

    private Vector_Float add_Hx1 = new Vector_Float(initSize);

    private Vector_Float add_Hx2 = new Vector_Float(initSize);

    private Vector_Float add_Hy1 = new Vector_Float(initSize);


    private Vector_Float add_Vx1 = new Vector_Float(initSize);

    private Vector_Float add_Vy1 = new Vector_Float(initSize);

    private Vector_Float add_Vy2 = new Vector_Float(initSize);

    private Vector_Rectangle add_box = new Vector_Rectangle(initSize);


    private Vector_Rectangle h_shape = new Vector_Rectangle(initSize);


    /** holds dividing line found when test for vertical line */
    private float vertical_x_divide = -1,vertical_y_divide=-1;

    private int pageWidth,pageHeight;

    public  PageLines(){}
    
    public  PageLines(ObjectInputStream os){

            try{
                
                initSize=os.readInt();

                vertical_x_divide=os.readFloat();
                vertical_y_divide=os.readFloat();

                pageWidth=os.readInt();
                pageHeight=os.readInt();

                boxCount=os.readInt();


                t_x1= (Vector_Int) os.readObject();
                t_x2= (Vector_Int) os.readObject();
                t_y1= (Vector_Int) os.readObject();
                t_y2= (Vector_Int) os.readObject();


                box_x1= (Vector_Float) os.readObject();

                box_y1= (Vector_Float) os.readObject();

                box_x2= (Vector_Float) os.readObject();

                box_y2= (Vector_Float) os.readObject();

                box_shape= (Vector_Rectangle) os.readObject();

                box_col= (Vector_Object) os.readObject();

                box_isLive= (Vector_boolean) os.readObject();

                box_isComposite= (Vector_boolean) os.readObject();

                v_x1= (Vector_Float) os.readObject();

                v_x2= (Vector_Float) os.readObject();

                v_y1= (Vector_Float) os.readObject();

                v_y2= (Vector_Float) os.readObject();

                v_shape= (Vector_Rectangle) os.readObject();

                h_x1= (Vector_Float) os.readObject();

                h_isLive= (Vector_boolean) os.readObject();

                h_x2= (Vector_Float) os.readObject();

                h_y1= (Vector_Float) os.readObject();

                h_y2= (Vector_Float) os.readObject();

                add_Hx1= (Vector_Float) os.readObject();

                add_Hx2= (Vector_Float) os.readObject();

                add_Hy1= (Vector_Float) os.readObject();


                add_Vx1= (Vector_Float) os.readObject();

                add_Vy1= (Vector_Float) os.readObject();

                add_Vy2= (Vector_Float) os.readObject();

                add_box= (Vector_Rectangle) os.readObject();


                h_shape= (Vector_Rectangle) os.readObject();




            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


    public void serializeToStream(ObjectOutputStream os){

        try{

            os.writeInt(initSize);

            os.writeFloat(vertical_x_divide);
            os.writeFloat(vertical_y_divide);

            os.writeInt(pageWidth);
            os.writeInt(pageHeight);

            os.writeInt(boxCount);


            os.writeObject(t_x1);
            os.writeObject(t_x2);
            os.writeObject(t_y1);
            os.writeObject(t_y2);


            os.writeObject(box_x1);

            os.writeObject(box_y1);

            os.writeObject(box_x2);

            os.writeObject(box_y2);

            os.writeObject(box_shape);

            os.writeObject(box_col);

            os.writeObject(box_isLive);

            os.writeObject(box_isComposite);

            os.writeObject(v_x1);

            os.writeObject(v_x2);

            os.writeObject(v_y1);

            os.writeObject(v_y2);

            os.writeObject(v_shape);

            os.writeObject(h_x1);

            os.writeObject(h_isLive);

            os.writeObject(h_x2);

            os.writeObject(h_y1);

            os.writeObject(h_y2);

            os.writeObject(add_Hx1);

            os.writeObject(add_Hx2);

            os.writeObject(add_Hy1);


            os.writeObject(add_Vx1);

            os.writeObject(add_Vy1);

            os.writeObject(add_Vy2);

            os.writeObject(add_box);


            os.writeObject(h_shape);


            os.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * test to see if any horizontal line between either points
     */
    final public boolean testHorizontalBetween(float line_min_width,
                                               float a_x1, float a_x2, float a_y1, float a_y2, float b_x1,
                                               float b_x2, float b_y1, float b_y2) {
        boolean has_divide = false;
        int line_count = h_x1.size(); //scan all lines
        float overlap_x1 = 0, overlap_x2 = 0;
        float temp, line_width, line_width_a, line_width_b;

        final float[] l_x1, l_x2, l_y1, l_y2;
        l_x1=this.h_x1.get();
        l_x2=this.h_x2.get();
        l_y1=this.h_y1.get();
        l_y2=this.h_y2.get();
        final boolean[] l_isLive=h_isLive.get();


        if (a_y2 < b_y1) { //make sure a is the top item
            temp = a_y1;
            a_y1 = b_y1;
            b_y1 = temp;
            temp = a_y2;
            a_y2 = b_y2;
            b_y2 = temp;
        }

        //calculate widths & choose smaller
        line_width_a = a_x2 - a_x1;
        line_width_b = b_x2 - b_x1;
        if (line_width_a < line_width_b)
            line_width = line_width_a;
        else
            line_width = line_width_b;

        //workout overlap
        if (a_x1 > b_x1)
            overlap_x1 = a_x1;
        else
            overlap_x1 = b_x1;
        if (a_x2 < b_x2)
            overlap_x2 = a_x2;
        else
            overlap_x2 = b_x2;

        for (int i = 0; i < line_count; i++) {

            //only live lines
            if(l_isLive[i]){
                //ignore lines out of range
                if((l_y1[i]<a_y1)|(l_y2[i]>a_y2)){
                    //check fits min requirement first (-1 for don't bother)
                    if ((line_min_width == -1) | (line_min_width > l_x2[i] - l_x1[i])) {
                        //check line is longer than either shape and fits between on x
                        // and y
                        if ((l_x2[i] - l_x1[i] > line_width) && (overlap_x1 >= l_x1[i])
                                && (overlap_x2 <= l_x2[i]) && (a_y2 > l_y1[i])
                                && (b_y1 < l_y2[i])) {
                            has_divide = true;
                            i = line_count;
                        }
                    }
                }
            }
        }

        return has_divide;
    }

    /**
     * test to see if any horizontal line across WHOLE page between either points
     */
    final public boolean testLineAcrossPage(float a_x1, float a_x2, float a_y, float b_x1,
                                                    float b_x2, float b_y) {

        boolean has_divide = false;

        float overlap_x1 = 0, overlap_x2 = 0;
        float temp, line_width, line_width_a, line_width_b;

        final float[] l_x1, l_x2, l_y1, l_y2;
        l_x1=this.h_x1.get();
        l_x2=this.h_x2.get();
        l_y1=this.h_y1.get();
        l_y2=this.h_y2.get();
        final boolean[] l_isLive=h_isLive.get();


        int line_count = l_x1.length;//scan all lines

        int  midAx=(int)((a_x1+a_x2)/2);
        int  midBx=(int)((b_x1+b_x2)/2);

        for (int i = 0; i < line_count; i++) {

            //only live
            if(l_isLive[i]){

				//ignore lines out of range
                if((l_y1[i]<a_y)&&(l_y2[i]>b_y)){

                    //System.out.println(i+" y="+l_y1[i]+" "+a_y+" "+b_y+" "+
                    //((l_x2[i]-l_x1[i]>=(700))+" "+(l_x1[i]<midAx)+" "+(l_x1[i]<midBx)+" "+(l_x2[i]>midAx)+" "+(l_x2[i]>midBx)));
                        //allow for up to 50 pixels off whole page -50
                        if ((l_x2[i]-l_x1[i]>=(700))&&(l_x1[i]<midAx)&&(l_x1[i]<midBx)&&(l_x2[i]>midAx)&&(l_x2[i]>midBx)) {
                            has_divide = true;
                            i = line_count;
                        }

                }
            }
        }

        return has_divide;
    }

    /**
     * test to see if any horizontal line between either points. Return -1 if false or length of line
     */
    final public int testHorizontalBetweenLines(float a_x1, float a_x2, float a_y1, float a_y2, float b_x1,
                                                float b_x2, float b_y1, float b_y2, boolean ignoreUnderlining, float fontSize, float allowedOverlap) {

        int Ydivide = -1;

        float temp;

        final float[] l_x1, l_x2, l_y1, l_y2;
        l_x1=this.h_x1.get();
        l_x2=this.h_x2.get();
        l_y1=this.h_y1.get();
        l_y2=this.h_y2.get();
        final boolean[] l_isLive=h_isLive.get();

        int line_count = l_x1.length;//scan all lines

        if (a_y2 < b_y1) { //make sure a is the top item
            temp = a_y1;
            a_y1 = b_y1;
            b_y1 = temp;
            temp = a_y2;
            a_y2 = b_y2;
            b_y2 = temp;
        }

        float topLineLength=a_x2-a_x1;

        int  midAx=(int)((a_x1+a_x2)/2);
        int  midBx=(int)((b_x1+b_x2)/2);

        int  midAY=(int)((a_y1+a_y2)/2);

        for (int i = 0; i < line_count; i++) {
             
            //only live
            if(l_isLive[i]){

                int lineLength= (int) (l_x2[i]-l_x1[i]);
                //ignore lines out of range
                if((l_y1[i]<midAY)&&(l_y2[i]>b_y2)){// && (!ignoreUnderlining || (1==2 &&(a_y1-a_y2>12)&&(l_x2[i]-l_x1[i])>topLineLength+10))){ //check for underline on headlines where font size over 12

                        //check line is longer than either shape and fits between on x
                        // and y
                        if ((l_x1[i]<midAx)&&(l_x1[i]<midBx)&&(l_x2[i]>midAx)&&(l_x2[i]>midBx)){

                            float lineDiff=(l_x2[i]-l_x1[i])-topLineLength;
                            if(lineDiff<0)
                            lineDiff=-lineDiff;

                            float xDiff=l_x1[i]-a_x1;
                            if(xDiff<0)
                            xDiff=-xDiff;

                            if(allowedOverlap!=-1 && lineDiff<allowedOverlap){ //allow us to ignore some lines on STI
                            }else if(ignoreUnderlining && ((a_y1-a_y2)>12) && lineDiff<20){
                                //System.out.println(a_x1+" "+a_y1+" "+a_y2+" line length="+topLineLength+" lineDiff="+lineDiff);
                            }else{
                                Ydivide=lineLength;
                                i = line_count;

                            }
                        }

                }
            }
        }

        return Ydivide;
    }

	/**
     * test to see if any horizontal line between either points
     */
    final public boolean testVerticalBetween(float a_x1, float a_x2,
                                             float a_y1, float a_y2, float b_x1, float b_x2, float b_y1,
                                             float b_y2) {
        //reset value to default
        vertical_x_divide = -1;
        vertical_y_divide=-1;

        boolean has_divide = false;
        float overlap_y1 = 0, overlap_y2;
        float temp, line_height, line_height_a, line_height_b;
        final float[] l_x1, l_x2, l_y1, l_y2;

        l_x1=this.v_x1.get();
        l_x2=this.v_x2.get();
        l_y1=this.v_y1.get();
        l_y2=this.v_y2.get();

        int line_count = l_x1.length; //count

        if (a_x1 > b_x1) { //make sure a is the left item
            temp = a_x1;
            a_x1 = b_x1;
            b_x1 = temp;
            temp = a_x2;
            a_x2 = b_x2;
            b_x2 = temp;
        }

        //calculate heights & choose smaller
        line_height_a = a_y1 - a_y2;
        line_height_b = b_y1 - b_y2;
        if (line_height_a < line_height_b)
            line_height = line_height_a;
        else
            line_height = line_height_b;

        //workout overlap
        if (a_y1 > b_y1)
            overlap_y1 = b_y1;
        else
            overlap_y1 = a_y1;
        if (a_y2 < b_y2)
            overlap_y2 = b_y2;
        else
            overlap_y2 = a_y2;

        for (int i = 0; i < line_count; i++) {

            //ignore lines out of range
            if((l_x1[i]<a_x1)|(l_x2[i]>a_x2)){
                //check line is longer than either shape and fits between on x and
                // blocks overlap on shapes
                if (((l_y1[i] - l_y2[i]) > line_height) && (overlap_y1 < l_y1[i])
                        && (l_y2[i] < overlap_y2) && (a_x2 < l_x1[i]) && (b_x1 > l_x2[i])) {
                    has_divide = true;
                    vertical_x_divide = l_x1[i];
                    vertical_y_divide = l_y1[i];
                    i = line_count;
                }
            }
        }
        return has_divide;
    }

    /**return x value found on last test for vertical line*/
    final public float getVerticalLineX() {
        return vertical_x_divide;
    }

    /**return x value found on last test for vertical line*/
    final public float getVerticalLineY() {
        return vertical_y_divide;
    }

    /**add line on page*/
    final public void addVerticalLine(float x1, float y1,float x2, float y2) {

        //have sure y1 above y2
        if(y1<y2){
            float tmp=y1;
            y1=y2;
            y2=tmp;
            tmp=x1;
            x1=x2;
            x2=tmp;
        }

        v_x1.addElement(x1);
        v_x2.addElement(x2);
        v_y1.addElement(y1);
        v_y2.addElement(y2);

        v_shape.addElement(new Rectangle((int)x1,(int)y2,(int)(x2-x1),(int)(y1-y2)));

    }

    /**get boxes made up of several items*/
    public Rectangle[] getComplexBoxes(){

        //total
        int total=0,id=0;

        boolean[] flags=box_isComposite.get();

        int boxCount=flags.length;

        //count first
        for(int count=0;count<boxCount;count++)
        if(flags[count]){
            //System.out.println(count+" "+box_shape.elementAt(count).getBounds());
            total++;
        }

        Rectangle[] boxes=new Rectangle[total];

        //populate
        for(int count=0;count<boxCount;count++)
        if(flags[count]){
            boxes[id]=box_shape.elementAt(count);
            id++;
        }

        return boxes;
    }

    /**add line on page*/
    final public void addBox(float x1, float y1,float x2, float y2,PdfColor col) {

        //have sure y1 above y2
        if(y1<y2){
            float tmp=y1;
            y1=y2;
            y2=tmp;
        }

        //have sure x1 on left of x2
        if(x2<x1){
            float tmp=x1;
            x1=x2;
            x2=tmp;
        }

        Rectangle rect=new Rectangle((int)x1,(int)y2,(int)(x2-x1),(int)(y1-y2));

        boolean used=false;

        Rectangle[] existing=box_shape.get();
        //see if used
           for(int ii=0;ii<boxCount;ii++){

               /**
                * allow for box tagged on at bottom for text (common on STI)
                */
               int xDiff=existing[ii].x-rect.x;
               if(xDiff<0)
               xDiff=-xDiff;

               int yDiff=existing[ii].y-rect.y;
               if(yDiff<0)
               yDiff=-yDiff;
                   
               int wDiff=existing[ii].width-rect.width;
               if(wDiff<0)
               wDiff=-wDiff;

               boolean taggedOntoBottom=yDiff<2 && xDiff<2 && wDiff<2;

               if(existing[ii].contains(rect) && !taggedOntoBottom){

                   used=true;
                   ii=boxCount;

               }else if(rect.contains(existing[ii])){

                   box_x1.setElementAt(x1, ii);
                   box_x2.setElementAt(x2, ii);
                   box_y1.setElementAt(y1, ii);
                   box_y2.setElementAt(y2, ii);
                   box_shape.setElementAt(rect, ii);

                   box_isComposite.setElementAt(false,ii);

                   box_isLive.setElementAt(true,ii);

                   used=true;
                   ii=boxCount;
               }else if(!taggedOntoBottom){

                   int altX1=existing[ii].getBounds().x;
                   int altX2=existing[ii].getBounds().x+existing[ii].getBounds().width;
                   int altY2=existing[ii].getBounds().y;
                   int altY1=existing[ii].getBounds().y+existing[ii].getBounds().height;

                   boolean overlapsOnX=((altX1<=x1 && altX2>=x1)||(altX1<=x2 && altX2>=x2));
                   boolean overlapsOnY=((altY1>=y1 && altY2<=y1)||(altY1>=y2 && altY2<=y2));


                   //only being used to combine small boxes
                   boolean rejectBothBigBoxes=(altX2-altX1>600 || x2-x1>600);

                   //if(rect.getBounds().width>50)
                   //System.out.println(rect.getBounds()+" "+existing[ii].getBounds()+" x="+overlapsOnX);

                   if(overlapsOnX && overlapsOnY && !rejectBothBigBoxes){

                       /**if(y1>720 || altY1>720){
                        System.out.println("\nrect="+rect.getBounds()+" "+x1+" "+y1+" "+x2+" "+y2);
                        System.out.println("exit="+existing[ii].getBounds()+" "+altX1+" "+altY1+" "+altX2+" "+altY2);
                       }*/

                       int xDiff1=(int)(x1-altX1);
                       int xDiff2=(int)(x2-altX2);
                       if(xDiff1<0)
                       xDiff1=-xDiff1;
                       if(xDiff2<0)
                       xDiff2=-xDiff2;

                       //if totally aligned, be more choosy to avoid WOBs
                       if(xDiff1>5 || xDiff2>5 ||(y1-y2>32 && altY1-altY2>32))
                       box_isComposite.setElementAt(true,ii);

                       if(x1>altX1)
                           x1=altX1;
                       if(x2<altX2)
                           x2=altX2;

                       if(y2>altY2)
                           y2=altY2;
                       if(y1<altY1)
                           y1=altY1;

                       box_x1.setElementAt(x1, ii);
                       box_x2.setElementAt(x2, ii);
                       box_y1.setElementAt(y1, ii);
                       box_y2.setElementAt(y2, ii);
                       box_shape.setElementAt(new Rectangle((int)x1,(int)y2,(int)(x2-x1),(int)(y1-y2)), ii);

                       box_isLive.setElementAt(true,ii);


                       //System.out.println(ii+" "+x1+","+y1+"   "+x2+","+y2+" "+new Rectangle((int)x1,(int)y2,(int)(x2-x1),(int)(y1-y2)));

                       used=true;
                       ii=boxCount;
                   }
               }
           }

        if(!used){

            box_x1.addElement(x1);
            box_x2.addElement(x2);
            box_y1.addElement(y1);
            box_y2.addElement(y2);

            box_shape.addElement(rect);
            box_col.addElement(col);
            box_isComposite.addElement(false);

            box_isLive.addElement(true);



        /**

            float[] FoldX1=box_x1.get();
            float[] FoldX2=box_x2.get();
            float[] FoldY1=box_y1.get();
            float[] FoldY2=box_y2.get();

        //erase any boxes it now hides
           for(int ii=0;ii<boxCount;ii++){
               float oldX1=FoldX1[ii];
               float oldX2=FoldX2[ii];
               float oldY1=FoldY1[ii];
               float oldY2=FoldY2[ii];
               if((x1<oldX1)&&(x2>oldX2)&&(y1<oldY1)&&(y2>oldY2)){
                   box_x1.setElementAt(0, ii);
                   box_x2.setElementAt(0, ii);
                   box_y1.setElementAt(0, ii);
                   box_y2.setElementAt(0, ii);
                   box_shape.setElementAt(null, ii);

               }else if(((y1>oldY2 && y1<oldY1)||(y2>oldY2 && y2<oldY1))&&
                       ((x1>oldX1 && x1<oldX2)||(x2>oldX1 && x2<oldX2))){ //trim any it overlaps if interesection

                 int w=(int)(oldX2-oldX1);
                int h=(int)(oldY1-oldY2);

                   {//if(w>20 && h>20){

                   if(x1>oldX1){
                       box_x1.setElementAt(x1, ii);
                       oldX1=x1;
                    }
                    if(x2<oldX2){
                        box_x2.setElementAt(x2, ii);
                        oldX2=x2;
                    }
                    if(y1<oldY1){
                        box_y1.setElementAt(y1, ii);
                        oldY1=y1;
                    }
                    if(y2>oldY2){
                        box_y2.setElementAt(y2, ii);
                        oldY2=y2;
                    }

                    box_shape.addElement(new Rectangle((int)oldX1,(int)oldY2,w,h));
                }
               }
           } /**/
        boxCount++;
        }

    }

    /**divide any boxes bisected by images - often Times breaks 1 box with line of images*/
    final private void checkImagesBisectingBox(float[] fX1, float[] fY1,float[] fX2, float[] fY2,int[] photos) {

        int photoCount=photos.length;

        boolean[] imageUsed=new boolean[photoCount];

        for(int currentPhoto=0;currentPhoto<photoCount;currentPhoto++){

            if(!imageUsed[currentPhoto]){

                int p=photos[currentPhoto]; //actual id
                float photoX1=fX1[p];
                float photoX2=fX2[p];
                float photoY1=fY1[p];
                float photoY2=fY2[p];

                //look for another unmatched image along side and combine
                for(int nextPhoto=currentPhoto+1;nextPhoto<photoCount;nextPhoto++){

                    if(!imageUsed[nextPhoto]){
                        int p2=photos[nextPhoto]; //actual id
                        float nphotoX1=fX1[p2];
                        float nphotoX2=fX2[p2];
                        float nphotoY1=fY1[p2];
                        float nphotoY2=fY2[p2];

                        if((nphotoX1==photoX1)&&(nphotoX2==photoX2)){ //2 vertical images
                            imageUsed[nextPhoto]=true;

                            if(nphotoY1>photoY1)
                                photoY1=nphotoY1;
                            if(nphotoY2<photoY2)
                                photoY2=nphotoY2;

                        }else if((nphotoY1==photoY1)&&(nphotoY2==photoY2)){ //2 horizontal images
                            imageUsed[nextPhoto]=true;

                            if(nphotoX1<photoX1)
                                photoX1=nphotoX1;
                            if(nphotoX2>photoX2)
                                photoX2=nphotoX2;
                        }
                    }
                }

                //test boxes
                for(int ii=0;ii<boxCount;ii++){

                    float oldX1=box_x1.elementAt(ii);
                    float oldX2=box_x2.elementAt(ii);
                    float oldY1=box_y1.elementAt(ii);
                    float oldY2=box_y2.elementAt(ii);

                    //calc differences
                    float dx1=oldX1-photoX1;
                    if(dx1<0)
                        dx1=-dx1;

                    float dx2=oldX2-photoX2;
                    if(dx2<0)
                        dx2=-dx2;

                    float dy1=oldY1-photoY1;
                    if(dy1<0)
                        dy1=-dy1;

                    float dy2=oldY2-photoY2;
                    if(dy2<0)
                        dy2=-dy2;

                    //overlap vertically so trim bottom and add second box
                    /**if(dx1<2 && dx2<2){ //horizontal

                           //trim top
                           box_y2.setElementAt(photoY1, ii);
                           box_shape.addElement(new Rectangle((int)oldX1,(int)photoY1,(int)(oldX2-oldX1),(int)(oldY1-photoY1)));

                           //add second underneath
                           box_x1.addElement(oldX1);
                           box_x2.addElement(oldX2);
                           box_y1.addElement(photoY2);
                           box_y2.addElement(oldY2);

                           box_shape.addElement(new Rectangle((int)oldX1,(int)oldY2,(int)(oldX2-oldX1),(int)(photoY2-oldY2)));
                           box_col.addElement(box_col.elementAt(ii));

                       }else */if(dy1<2 && dy2<2){ //vertical

                           //trim left
                           box_x2.setElementAt(photoX1, ii);
                           box_shape.addElement(new Rectangle((int)oldX1,(int)oldY2,(int)(photoX1-oldX1),(int)(oldY1-oldY2)));

                           //add second on right
                           box_x1.addElement(photoX2);
                           box_x2.addElement(oldX2);
                           box_y1.addElement(oldY1);
                           box_y2.addElement(oldY2);

                           box_shape.addElement(new Rectangle((int)photoX2,(int)oldY2,(int)(oldX2-photoX2),(int)(oldY1-oldY2)));
                           box_col.addElement(box_col.elementAt(ii));

                           box_isLive.addElement(true);

                       }
                }
            }
           }
    }

    /**add line on page*/
    final public void addHorizontalLine(float x1, float y1,float x2, float y2) {

        //have sure x1 on left
        if(x1>x2){
            float tmp=x1;
            x1=x2;
            x2=tmp;
        }

        //have sure y1 above
        if(y1<y2){
            float tmp=y1;
            y1=y2;
            y2=tmp;

        }

        h_x1.addElement(x1);
        h_x2.addElement(x2);
        h_y1.addElement(y1);
        h_y2.addElement(y2);

        h_shape.addElement(new Rectangle((int)x1,(int)y2,(int)(x2-x1),(int)(y1-y2)));

        h_isLive.addElement(true);
    }

    /**
     * add lines to display
     */
    public void drawLines(Graphics2D g2) {
        try{
        g2.setColor(Color.red);

        //draw vertical lines
        int count=v_shape.size();

        for (int i = 0; i < count; i++){
            Rectangle rect=v_shape.elementAt(i);
            if(rect!=null)
            g2.draw(rect);
        }
        //and horizontal
        count=h_shape.size();

        for (int i = 0; i < count; i++){
            Rectangle rect=h_shape.elementAt(i);

            if(rect!=null && h_isLive.elementAt(i))
            g2.draw(rect);

        }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * add lines to display
     */
    public void drawBoxes(Graphics2D g2) {

        try{

            //draw boxes used in grouping
            int count=box_shape.size();

            for (int i = 0; i < count; i++){
                Rectangle rect=box_shape.elementAt(i);
                if(rect!=null){
                    g2.setColor((PdfColor)box_col.elementAt(i));
                    g2.fill(rect);

                    g2.setColor(Color.magenta);
                    g2.draw(rect);
                    g2.drawLine(rect.x,rect.y,rect.x+rect.width,rect.y+rect.height);

                }
            }

            //draw ads
            count=add_box.size();

            for (int i = 0; i < count; i++){
                Rectangle rect=add_box.elementAt(i);
                if(rect!=null){
                    g2.setColor(Color.lightGray);
                    g2.fill(rect);

                    g2.setColor(Color.magenta);
                    g2.draw(rect);
                    g2.drawLine(rect.x,rect.y+rect.height,rect.x+rect.width,rect.y);

                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * take all tint sublines and see if they appear to make a whole line
     */
    public void lookForCompositeLines() {

        //copy to local arrays for speed!
        int[] x1=t_x1.get();
        int[] y1=t_y1.get();
        int[] x2=t_x2.get();
        int[] y2=t_y2.get();

        int count=x1.length;

        boolean debug=false;

        //iniital values
        int current_x1=x1[0];
        int current_y1=y1[0];
        int current_x2=x1[0];
        int current_y2=y2[0];

        int matches=3;
        //look for horizontal lines
        for(int p=1;p<count;p++){

            //ignore empty values in array
            if((x1[p]==0)&&(y1[p]==0)&&(x2[p]==0)&&(y2[p]==0))
                    continue;

            /**
            if((x1[p]>26)&&(y2[p]>461)&&
                    (x2[p]<100)&&
                    (y1[p]<500)){
                System.out.println(x1[p]+" "+y1[p]+" "+x2[p]+" "+y2[p]);
                debug=true;
            }else
                debug=false;*/


            if(debug)
            System.out.println(p+" Testing "+x1[p]+ ' ' +y1[p]+ ' ' +x2[p]+ ' ' +y2[p]);

            boolean onLine=Math.abs(current_y1-y1[p])<2;

            //if its on the same line and less than 10 pixels away, regard as continuous
            if((onLine)&&((Math.abs(x1[p]-current_x2)<10)|(Math.abs(x2[p]-current_x1)<10))){
                if(current_x2<x2[p])
                    current_x2=x2[p];
                else if(current_x1>x1[p])
                    current_x1=x1[p];

                if(debug)
                System.out.println("ON line");
            }else if((current_x1!=current_x2)){ //end of line (second test allow for underneath

                //must be over 10 pixels long to avoid spurious match
                if(current_x2-current_x1>10){
                    if(debug)
                    System.out.println("====================Line found "+current_x1+ ' ' +current_y1+ ' ' +current_x2+ ' ' +current_y2);

                    //add line
                    addHorizontalLine(current_x1, current_y1,current_x2, current_y1);
                }else if(debug)
                    System.out.println("Too short "+current_x1+ ' ' +current_x2);

                //use new  default values
                current_y1=y1[p];
                current_x1=x1[p];
                current_x2=x1[p];
                current_y2=y2[p];

            }else{ //try again
                if(debug)
                System.out.println("NO");
                current_y1=y1[p];
                current_x1=x1[p];
                current_x2=x2[p];
                current_y2=y2[p];
            }
        }

        //check for last line on exit
        if((current_x1!=current_x2)&&(current_x2-current_x1>50)){ //end of line
            if(debug)
            System.out.println("Line found "+current_x1+ ' ' +current_y1+ ' ' +current_x2+ ' ' +current_y2);

            addHorizontalLine(current_x1, current_y1,current_x2, current_y1);
        }

        //flush values
        t_x1.clear();
        t_y1.clear();
        t_x2.clear();
        t_y2.clear();

    }

    /**
     * add tiny lines to allow for long valid lines drawn as set of small lines we would miss
     */
    public void addPossiblePartLine(int x1, int y1, int x2, int y2) {

        t_x1.addElement(x1);
        t_y1.addElement(y1);
        t_x2.addElement(x2);
        t_y2.addElement(y2);
                                                                                  
    }

    /**set box id for each object or -1 if none
     * could be recoded faster - done for simplicity at this point*/
    public int[] getBoxIDs(float[] fX1, float[] rawfY1, float[] fX2, float[] fY2,int[] startFontSize, int[] images) {


        //validate boxes against images
        //some boxes drawn with image in middle so look like 2 boxes
        //we need to allow for an split boxes
        checkImagesBisectingBox(fX1, rawfY1, fX2, fY2,images);

        int count=fX1.length,id;
        int[] boxes=new  int[count];

        //clone fY1 as we alter
        float fY1[]=new float[count];
        System.arraycopy(rawfY1, 0, fY1, 0, count);

        int lastB=-1;

        float[] box_x1=this.box_x1.get();
        float[] box_x2=this.box_x2.get();
        float[] box_y1=this.box_y1.get();
        float[] box_y2=this.box_y2.get();
        boolean[] boxIsLive=this.box_isLive.get();

        int[] boxContents=new int[boxCount];

        /**count items inside*/
        for(int i=0;i<count;i++){

            //default of -1
            boxes[i]=-1;

            //actual id of object
            id=i;


			//hack for text at very top
            if(fY1[id]>pageHeight)
                    fY1[id]=pageHeight;

            //look for matching box (always used biggest box as often boxes inside boxes)
            for(int b=0;b<boxCount;b++){

				float boxHeight=box_y1[b]-box_y2[b];
                //float boxWidth=box_x2[b]-box_x1[b];

                float fontGap=startFontSize[id]/4;
                if(startFontSize[id]<=10)
                fontGap=0;

                if((boxIsLive[b])&&(fX1[id]>box_x1[b])&&(fX2[id]<box_x2[b])&&((fY1[id]-fontGap)<=box_y1[b])&&((fY2[id]+fontGap)>=box_y2[b])){
               	//if((boxIsLive[b])&& boxHeight>25 && (fX1[id]+2>=box_x1[b])&&(fX2[id]-2<=box_x2[b])&&(fY1[id]<=box_y1[b])&&(fY2[id]>=box_y2[b])){
                //if((boxIsLive[b])&&(fX2[id]>box_x1[b])&&(fX1[id]<box_x2[b])&&(fY1[id]<=box_y1[b])&&(fY2[id]>=box_y2[b])){
                    boxContents[b]++;

                }
            }
        }

        //now redo, ignoring boxes with contain 1 item or all items
        for(int i=0;i<count;i++){

            //default of -1
            boxes[i]=-1;

            //actual id of object
            id=i;

            //look for matching box (always used biggest box as often boxes inside boxes)
            for(int b=0;b<boxCount;b++){

				float boxHeight=box_y1[b]-box_y2[b];
                //float boxWidth=box_x2[b]-box_x1[b];
                //allow margin of error on large text

                //allow margin for error on large fonts
                float fontGap=startFontSize[id]/4;
                if(startFontSize[id]<=10)
                fontGap=0;

                boolean fitsBox=(boxIsLive[b])&&(fX1[id]>box_x1[b])&&(fX2[id]<box_x2[b])&&((fY1[id]-fontGap)<=box_y1[b])&&((fY2[id]+fontGap)>=box_y2[b]);
                boolean singleLine=(box_y1[b]-box_y2[b])/startFontSize[id]<2.4f;

                //workout if centred
                float x1Diff=box_x1[b]-fX1[id];
                float x2Diff=fX2[id]-box_x2[b];
                float diff=x1Diff-x2Diff;
                if(diff<0)
                diff=-diff;

                boolean isCentred=(diff<15);

                boolean isInvalid= boxContents[b]<2 || boxContents[b]==count;

                //let through single line in box just around text in TIM
                if(isInvalid  && singleLine && isCentred && fitsBox && boxContents[b]==1 && startFontSize[id]==10) {
                    isInvalid=false;

//                    if(box_y1[b]<30){
//                        System.out.println(isCentred+" "+diff +" "+startFontSize[id]+" diffs="+x1Diff+" "+x2Diff);
//
//                        System.out.println(box_x1[b]+" "+box_x2[b]+" "+diff+" <> "+fX1[id]+" "+fX2[id]);
//                    }
                }

                if(isInvalid){
                    //not relevent
                }else if(fitsBox){
                //}else if((boxIsLive[b]) && boxHeight>25 &&(fX1[id]+2>=box_x1[b])&&(fX2[id]-2<=box_x2[b])&&(fY1[id]<=box_y1[b])&&(fY2[id]>=box_y2[b])){
                //}else if((boxIsLive[b])&&(fX2[id]>box_x1[b])&&(fX1[id]<box_x2[b])&&(fY1[id]<=box_y1[b])&&(fY2[id]>=box_y2[b])){

                    if(boxes[i]==-1){
                        boxes[i]=b;
                        lastB=b;
                    }else{
                        float currentArea=(box_x2[lastB]-box_x1[lastB])*(box_y1[lastB]-box_y2[lastB]);
                        float newArea=(box_x2[b]-box_x1[b])*(box_y1[b]-box_y2[b]);

                        if(currentArea<newArea){
                            boxes[i]=b;
                            lastB=b;
                        }
                    }

                    //b=boxCount;

                    //System.out.println(id+" "+b);
                }
            }
        }

        return boxes;
    }

    /**set box id for each object or -1 if none
     * could be recoded faster - done for simplicity at this point*/
    public void removeValidTextBoxes(float[] fX1, float[] fY1, float[] fX2, float[] fY2,String[] contents,int[] idx,int[] startFontSize,int defaultFontSize) {

        int count=idx.length,id;

        float[] box_x1=this.box_x1.get();
        float[] box_x2=this.box_x2.get();
        float[] box_y1=this.box_y1.get();
        float[] box_y2=this.box_y2.get();
        boolean[] boxIsLive=this.box_isLive.get();

        /**count items inside*/
        for(int b=0;b<boxCount;b++){

            if(boxIsLive[b]){

                Vector_Int stories=new Vector_Int(20);
                int boxContents=0;

                //see how many items in box
                for(int i=0;i<count;i++){

                    //actual id of object
                    id=idx[i];

                    //hack for text at very top
                    if(fY1[id]>pageHeight)
                        fY1[id]=pageHeight;

                    if((fX1[id]>box_x1[b])&&(fX2[id]<box_x2[b])&&(fY1[id]<=box_y1[b])&&(fY2[id]>=box_y2[b])){
                        boxContents++;
                        stories.addElement(id);
                    }
                }

                if(boxContents>2){
                    int[] storyList=stories.get();
                    int storyCount=stories.size()-1;
                    int matches=0,rejections=0;
                    
                    //look for patter Head/text
                    for(int i=storyCount-1;i>0;i--){

                        int possHead=storyList[i];
                        int possText=storyList[i-1];

                        //right fonts, underneath
                        if(startFontSize[possHead]>defaultFontSize && startFontSize[possText]<defaultFontSize && fY2[possHead]>fY1[possText] &&
                                startFontSize[possHead]>startFontSize[possText] && ((fY1[possHead]-fY2[possHead])<(fY1[possText]-fY2[possText]))){

                            matches++;

                            //roll on as match
                            i--;
                        }else
                            rejections++;
                    }

                    //ignore if we found over 50%
                    if(matches>rejections && matches>2 && matches>storyCount/4)
                    boxIsLive[b]=false;

                }
            }
        }

        this.box_isLive.set(boxIsLive);
    }

    public void addPossibleLineEnclosingAdvert(int x1, int x2,int y1,int y2,int w ,int h) {

        //have sure y1 on top
        if(y1<y2){
            int tmp=y1;
            y1=y2;
            y2=tmp;
        }

        //have sure x1 on left
        if(x1>x2){
            int tmp=x1;
            x1=x2;
            x2=tmp;
        }

        //lines on left or bottom
        int yDiff=y1-y2;
        if(yDiff<0)
            yDiff=-yDiff;

        int xDiff=x1-x2;
        if(xDiff<0)
            xDiff=-xDiff;

        if((xDiff<pageWidth*.75f)&&(yDiff<pageHeight*.75f)){  //maximum of three quarters of page

            //if(x1>490 && x1<520)
              //  System.out.println("xDiff="+xDiff+" yDiff="+yDiff+" y2="+y2);

            if((xDiff<4 && (y2<1)||
                    ((x2>pageWidth-5) || x1<1 ) && yDiff<4)){


                if(w>h){
                    add_Hx1.addElement(x1);
                    add_Hx2.addElement(x2);
                    add_Hy1.addElement(y1);

                    //System.out.println("horizontal");
                }else{
                    add_Vx1.addElement(x1);
                    add_Vy1.addElement(y1);
                    add_Vy2.addElement(y2);
                    //System.out.println("vertical");
                }
            }
        }
    }


    /**work out any add shapes present*/
    private void calculateAds() {

        try{
        //get values for fast access
        float[] Hx1=add_Hx1.get();
        float[] Hx2=add_Hx2.get();
        float[] Hy1=add_Hy1.get();

        float[] Vx1=add_Vx1.get();
        float[] Vy2=add_Vy2.get();
        float[] Vy1=add_Vy1.get();

        int VLineCount=Vx1.length;
        int HLineCount=Hx1.length;


        //simple compare at present
        for(int vLine=0;vLine<VLineCount;vLine++){


            for(int HLine=0;HLine<HLineCount;HLine++){

                //see if they cross
                float verticalDiff=Vx1[vLine]-Hx2[HLine];
                if(verticalDiff<0)
                verticalDiff=-verticalDiff;

                //allof for on right
                if(verticalDiff>3){
                    verticalDiff=Vx1[vLine]-Hx1[HLine];
                    if(verticalDiff<0)
                    verticalDiff=-verticalDiff;
                }

                float horDiff=Vy1[vLine]-Hy1[HLine];
                if(horDiff<0)
                horDiff=-horDiff;

                if(verticalDiff<3 && horDiff<3){

                    int w=(int)(Hx2[HLine]-Hx1[HLine]);
                    int h=(int)(Vy1[vLine]-Vy2[vLine]);
                    if(w>32 & h>32){ //ignore really tiny objects
                    add_box.addElement(new Rectangle((int)Hx1[HLine],(int)Vy2[vLine],w,h));
                                  // System.out.println("-------------"+new Rectangle((int)Hx1[HLine],(int)Vy2[vLine],w,h));
                    }
                }
            }
        }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**return ad rectangles sorted so largest first*/
    public Rectangle[] getAds() {

        int count=add_box.size()-1;
        Rectangle[] sortedBoxes=new Rectangle[count];
        int size[] =new int[count];

        Rectangle[] rawBoxes=add_box.get();
        for(int i=0;i<count;i++){

            Rectangle newRect=add_box.elementAt(i);
            int newSize=newRect.width*newRect.height;

            if(i==0){
                sortedBoxes[0]=newRect;
                size[0]=newSize;
            }else{
                //slot in place
                for(int j=0;j<count;j++){
                    if(size[j]<newSize){//first item too big

                        //move up others
                        for(int ii=count-1;ii>j;ii--){
                            sortedBoxes[ii]=sortedBoxes[ii-1];
                            size[ii]=size[ii-1];
                        }

                        //slot in
                        sortedBoxes[j]=newRect;
                        size[j]=newSize;

                        j=i;
                    }
                }
            }

        }

        return sortedBoxes;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setMaxWidth(int maxWidth,int maxHeight) {
        this.pageWidth=maxWidth;
        this.pageHeight=maxHeight;
    }

    public void calculatePageShapes() {

        calculateAds();

        //flagged drowned horizontal lines and other boxes
        float[] box_x1=this.box_x1.get();
        float[] box_x2=this.box_x2.get();
        float[] box_y1=this.box_y1.get();
        float[] box_y2=this.box_y2.get();
        boolean[] boxIsLive=this.box_isLive.get();

        final float[] l_x1, l_x2, l_y1, l_y2;
        l_x1=this.h_x1.get();
        l_x2=this.h_x2.get();
        l_y1=this.h_y1.get();
        l_y2=this.h_y2.get();
        final boolean[] l_isLive=h_isLive.get();
        int lineCount=l_x1.length;


        /**scan each box in turn*/
        for(int i=0;i<boxCount;i++){

            //look for matching box inside and flag as used
            for(int b=0;b<boxCount;b++){
                if(boxIsLive[b]&&(i!=b)&&(box_x1[i]>=box_x1[b])&&(box_x2[i]<=box_x2[b])&&
                        (box_y1[i]<=box_y1[b])&&(box_y2[i]>=box_y2[b])){
                    boxIsLive[b]=false;

                }
            }

            //look for matching line inside and flag as used
            for(int b=0;b<lineCount;b++){
                if(l_isLive[b]&&(l_x1[b]>=box_x1[i])&&(l_x2[b]<=box_x2[i])&&
                        (l_y1[b]<=box_y1[i])&&(l_y2[b]>=box_y2[i])){
                    l_isLive[b]=false;
                    //System.out.println(b);

                }
            }
        }

        //reset new value
        box_isLive.set(boxIsLive);
        h_isLive.set(l_isLive);

    }
}
