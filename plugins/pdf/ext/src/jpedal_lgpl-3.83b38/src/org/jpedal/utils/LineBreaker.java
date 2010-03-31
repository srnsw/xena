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
* LineBreaker.java
* ---------------
*/
package org.jpedal.utils;

import org.jpedal.grouping.PdfGroupingAlgorithms;


//breaks a line into 2
public class LineBreaker {

    public float endX=0,startX=0;
    public int brk=0;
    public int charsCounted=0;

    /**
     * work through text, using embedded markers to work out whether
     * each letter is IN or OUT
     */
    public void breakLine(String origData,float x1,float y1,float x2,float y2,boolean debugFlag){


        int p=0,end=origData.length();
        char[] line=origData.toCharArray();
        String raw=origData;
        String value="",pt_reached = "", char_width = "";
        charsCounted=0;
        endX=0;
        startX=0;
        brk=0;
        float ptReached=0,lastPt=0;

        while(p<end){

            //only data between min and y locations
            while (true) {

                /**
                 * read value
                 */

                if(line[p]!= PdfGroupingAlgorithms.MARKER2){
                    //find second marker and get width
                    int startPointer=p;
                    while((p<end)&&(line[p]!=PdfGroupingAlgorithms.MARKER2))
                        p++;
                    value = raw.substring(startPointer,p);

                }else{// read the next token and its location and width

                    //find first marker
                    while((p<end)&&(line[p]!=PdfGroupingAlgorithms.MARKER2))
                        p++;

                    p++;

                    //find second marker and get width
                    int startPointer=p;
                    while((p<end)&&(line[p]!=PdfGroupingAlgorithms.MARKER2))
                        p++;
                    pt_reached = raw.substring(startPointer,p);
                    p++;

                    //find third marker
                    startPointer=p;
                    while((p<end)&&(line[p]!=PdfGroupingAlgorithms.MARKER2))
                        p++;

                    char_width=raw.substring(startPointer,p);
                    p++;

                    //find next marker and number of chars
                    startPointer=p;
                    while((p<end)&&(line[p]!=PdfGroupingAlgorithms.MARKER2)){
                        charsCounted++;
                        p++;
                    }

                    value = raw.substring(startPointer,p);

                    ptReached=Float.parseFloat(pt_reached);

                    lastPt=ptReached+Float.parseFloat(char_width);


                    //update pointers
                    if(lastPt  < x1){
                        startX=lastPt;
                        brk=p;
                    }else if(ptReached  > x2 && endX==0) //first point only
                        endX=ptReached;


                    if(debugFlag)
                        System.out.println(value+ ' ' +ptReached+ ' ' +char_width+" startX="+startX+" endX="+endX);

                    if (ptReached  > x2)
                        break;

                    value = "";

                }

                if(p>=end)
                    break;
            }
        }
    }

}
