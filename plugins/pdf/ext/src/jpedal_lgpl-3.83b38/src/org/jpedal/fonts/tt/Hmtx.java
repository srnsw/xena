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
* Hmtx.java
* ---------------
*/
package org.jpedal.fonts.tt;

import org.jpedal.utils.LogWriter;


public class Hmtx extends Table {
	
	private int[] hMetrics;
	private short[] leftSideBearing;
    float scaling=1f/1000f;

    public Hmtx(FontFile2 currentFontFile,int glyphCount,int metricsCount,int maxAdvance){

        scaling=(float)maxAdvance;

        LogWriter.writeMethod("{readHmtxTable}", 0);

        if(metricsCount<0)
			metricsCount=-metricsCount;
		
		//move to start and check exists
		int startPointer=currentFontFile.selectTable(FontFile2.HMTX);
		
		int lsbCount=glyphCount-metricsCount;
		
		//System.out.println("start="+Integer.toHexString(startPointer)+" lsbCount="+lsbCount+" glyphCount="+glyphCount+" metricsCount="+metricsCount);
		
		hMetrics = new int[glyphCount];
		leftSideBearing = new short[glyphCount];
		
		int currentMetric=0;
		
		//read 'head' table
		if(startPointer==0)
			LogWriter.writeLog("No Htmx table found");
        else if(lsbCount<0){
            LogWriter.writeLog("Invalid Htmx table found");
        }else{
			int i=0;
			for (i = 0; i < metricsCount; i++){
				currentMetric=currentFontFile.getNextUint16();
				hMetrics[i] =currentMetric;
				leftSideBearing[i] = currentFontFile.getNextInt16();
				//System.out.println(i+"="+hMetrics[i]+" "+leftSideBearing[i]);
			}
			
			//workout actual number of values in table
			int tableLength=currentFontFile.getTableSize(FontFile2.HMTX);
			int lsbBytes=tableLength-(i*4); //each entry above used 4 bytes
			lsbCount=(lsbBytes/2); //each entry contains 2 bytes

			//read additional lsb entries
			for(int j=i;j<lsbCount;j++){

				hMetrics[j] =currentMetric;
				leftSideBearing[j] = currentFontFile.getFWord();
				//System.out.println((j)+" "+leftSideBearing[j]);
			}
		}
		
	}

    //used by OTF code for aligning CFF font data
    public short getRAWLSB(int i){
        if(leftSideBearing==null || i>=leftSideBearing.length)
            return 0;
        else
            return leftSideBearing[i];
    }

    public short getLeftSideBearing(int i) {
		if (i < hMetrics.length) {
			return (short)(hMetrics[i] & 0xffff);
		} else if(leftSideBearing==null){
			return 0;
		}else{
			try{
				return leftSideBearing[i - hMetrics.length];
			}catch(Exception e){
		        return 0;
			}
		}
	}
	
	public float getAdvanceWidth(int i) {
        /**if (i < hMetrics.length) {
			return hMetrics[i] >> 16;
		} else {
			return hMetrics[hMetrics.length - 1] >> 16;
		}*/
        return ( (hMetrics[i]-getLeftSideBearing(i))/scaling);
    }

    public float getWidth(int i) {
        /**if (i < hMetrics.length) {
			return hMetrics[i] >> 16;
		} else {
			return hMetrics[hMetrics.length - 1] >> 16;
		}*/
        float w=hMetrics[i];

        
        return ( (w)/scaling);
    }
    
    public float getUnscaledWidth(int i) {
        
        return hMetrics[i];
    }
}
