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
* PageOffsets.java
* ---------------
*/
package org.jpedal;

import org.jpedal.objects.PdfPageData;


/**
 * holds offsets for all multiple pages
 */
public final class PageOffsets {

    /**width of all pages*/
    protected int totalSingleWidth=0,totalDoubleWidth=0,gaps=0,doubleGaps=0;

    /**height of all pages*/
    protected int totalSingleHeight=0,totalDoubleHeight=0;

    /**gap between pages*/
    protected static final int pageGap=10;

    /**max widths and heights for facing and continuous pages*/
    protected int doublePageWidth=0,doublePageHeight=0,biggestWidth=0,biggestHeight=0,widestPageNR,widestPageR;

	protected boolean hasRotated;
    
    public static final int SIDE_COLLAPSED_PAGE_SIZE = 150;
    public static final int SIDE_SCROLL_PAGES = 3;

    public PageOffsets(int pageCount, PdfPageData pageData) {


			/** calulate sizes for continuous and facing page modes */
            int pageH, pageW,rotation;
            int facingW = 0, facingH = 0;
            int greatestW = 0, greatestH = 0;
            totalSingleHeight = 0;
            totalSingleWidth = 0;
			hasRotated=false;
			
			int widestLeftPage=0,widestRightPage=0,highestLeftPage=0,highestRightPage=0;

			widestPageR=0;
			widestPageNR=0;
			
			totalDoubleWidth = 0;
            totalDoubleHeight = 0;
            gaps=0;
            doubleGaps=0;

            biggestWidth = 0;
            biggestHeight = 0;
            
			for (int i = 1; i < pageCount + 1; i++) {

				//get page sizes
                pageW = pageData.getCropBoxWidth(i);
                pageH = pageData.getCropBoxHeight(i);
				rotation = pageData.getRotation(i);

				//swap if this page rotated and flag
				if((rotation==90|| rotation==270)){
	                int tmp=pageW;
	                pageW=pageH;
	                pageH=tmp;
				}
				
				gaps=gaps+pageGap;


				totalSingleWidth = totalSingleWidth + pageW;
				totalSingleHeight = totalSingleHeight + pageH;
				
				//track page sizes
				if(( i & 1)==1){//odd
					if(widestRightPage<pageW)
						widestRightPage=pageW;
					if(highestRightPage<pageH)
						highestRightPage=pageH;
				}else{
					if(widestLeftPage<pageW)
						widestLeftPage=pageW;
					if(highestLeftPage<pageH)
						highestLeftPage=pageH;
				}
				
				if(widestPageNR<pageW)
				widestPageNR=pageW;

				if(widestPageR<pageH)
				widestPageR=pageH;
				
				if (pageW > biggestWidth)
					biggestWidth = pageW;
				if (pageH > biggestHeight)
					biggestHeight = pageH;
				
				// track widest and highest combination of facing pages
                if ((i & 1) == 1) {

					if (greatestW < pageW)
                        greatestW = pageW;
                    if (greatestH < pageH)
                        greatestH = pageH;

					if (i == 1) {// first page special case
						totalDoubleWidth = pageW;
						totalDoubleHeight = pageH;
					} else {
                        totalDoubleWidth = totalDoubleWidth + greatestW;
                        totalDoubleHeight = totalDoubleHeight + greatestH;
					}

					doubleGaps=doubleGaps+pageGap;

					facingW = pageW;
                    facingH = pageH;
					
                } else {

					facingW = facingW + pageW;
                    facingH = facingH + pageH;
					
					greatestW = pageW;
					greatestH = pageH;

                    if (i == pageCount) { // allow for even number of pages
                        totalDoubleWidth = totalDoubleWidth + greatestW + pageGap;
                        totalDoubleHeight = totalDoubleHeight + greatestH + pageGap;
                    }
                }

                //choose largest (to allow for rotation on specific pages)
                int max=facingW;
                if(max<facingH)
                	max=facingH;
              
            }
			
			doublePageWidth=widestLeftPage+widestRightPage+pageGap;
			doublePageHeight=highestLeftPage+highestRightPage+pageGap;

            // subtract pageGap to make sum correct
            totalSingleWidth = totalSingleWidth - pageGap;
			totalSingleHeight = totalSingleHeight - pageGap;

		}

}

