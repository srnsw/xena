/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2009, IDRsolutions and Contributors.
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

  * ShapeTracker.java
  * ---------------
  * (C) Copyright 2009, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.external;

import org.jpedal.color.PdfPaint;

import java.awt.*;

/**
 * allow user to recieve raw glyph data as generated
 */
public interface ShapeTracker {


    /**
     * pass user the low-level details
     * @param tokenNumber actual token reached in stream (useful for working out if objects behind others
     * @param type (Cmd.S, Cmd.s, Cmd.B, etc)... B,S and F comands with or without star and upper/lower case
     * to define Fill, Stroke, etc
     * @param currentShape - shape with unscaled, unrotated PDF co-ordinates
     * @param nonstrokecolor - used for Fills
     * @param strokecolor - used for stroking shape
     */
    public void addShape(int tokenNumber,int type, Shape currentShape, PdfPaint nonstrokecolor, PdfPaint strokecolor);

    /** here is an example
    private class TestShapeTracker implements ShapeTracker {
        public void addShape(int tokenNumber, int type, Shape currentShape, PdfPaint nonstrokecolor, PdfPaint strokecolor) {

            //use this to see type
            //Cmd.getCommandAsString(type);

            //print out details
            if(type==Cmd.S || type==Cmd.s){ //use stroke color to draw line
                System.out.println("-------Stroke-------PDF cmd="+Cmd.getCommandAsString(type));
                System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" stroke color="+strokecolor);

            }else if(type==Cmd.F || type==Cmd.f || type==Cmd.Fstar || type==Cmd.fstar){ //uses fill color to fill shape
                System.out.println("-------Fill-------PDF cmd="+Cmd.getCommandAsString(type));
                System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" fill color="+nonstrokecolor);

            }else{ //not yet implemented (probably B which is S and F combo)
                System.out.println("Not yet added");
                System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" type="+type+" "+Cmd.getCommandAsString(type));
               
            }
        }
    }
    /**/
}
