/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2008, IDRsolutions and Contributors.
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

  * PdfKeyPairsIterator.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

import org.jpedal.io.PdfReader;

/**
 * allow fast access to data from PDF object
 *
 */
public class PdfKeyPairsIterator {
    
	private byte[][] keys,values;
	
	private PdfObject[] objs;

    int maxCount,current=0;

    public PdfKeyPairsIterator(byte[][] keys, byte[][] values, PdfObject[] objs) {

        this.keys=keys;
        this.values=values;
        this.objs=objs;
        
        if(keys!=null)
        maxCount=keys.length;
        
        current=0;
    }

    /**
     * number of PAIRS (or keys)
     * @return
     */
    public int getTokenCount() {
        return maxCount;
    }

    /**
     * roll onto next key and value
     * @return
     */
    public void nextPair() {

        if(current<maxCount)
            current++;
        else
             throw new RuntimeException("No keys left in PdfKeyPairsIterator");
    }

    /**
     * next key
     * @return
     */
    public String getNextKeyAsString() {

        //System.out.println((char)keys[current-1][0]+"<length="+keys[current-1].length);
        /**if(convertNumberToString){ //decide if number and convert to value
            int length=keys[current].length;
            boolean isNotNumber=false;
            for(int ii=0;ii<length;ii++){
                int nextChar=keys[current][ii];
                if(nextChar>='0' && nextChar<='9'){

                }else{
                    isNotNumber=true;
                    ii=length;
                }
            }

            if(isNotNumber){
                return new String(keys[current]);
            }else{
                int key=PdfReader.parseInt(0,length,keys[current]);

                //generate if needed and cache
                if(IntsAsChars[key]==null){
                    char[] stringChar=new char[1];
                    stringChar[0]=(char)key;
                   IntsAsChars[key]=new String(stringChar);
                }

                return IntsAsChars[key];
            }
        }else*/
            return new String(keys[current]);

    }
    
    /**
     * used by CharProcs to return number or number of key (ie /12 or /A)
     * @return
     */
    public int getNextKeyAsNumber() {

        //System.out.println((char)keys[current-1][0]+"<length="+keys[current-1].length);
        	
        int length=keys[current].length;
        boolean isNumber = isNextKeyANumber();

        if(!isNumber){
        	if(keys[current].length!=1){
        		if(1==1)
        		throw new RuntimeException("Unexpected value in getNextKeyAsNumber >"+new String(keys[current])+"<");
        	}else
        		return (keys[current][0] & 255);
        }else
             return PdfReader.parseInt(0,length,keys[current]);
       
        return -1;
    }

	public boolean isNextKeyANumber() {
		
		int length=keys[current].length;
        
		boolean isNumber=true;
        
		for(int ii=0;ii<length;ii++){
            int nextChar=keys[current][ii];
            
            //System.out.println(nextChar);
            if(nextChar>='0' && nextChar<='9'){

            }else{
                isNumber=false;
                ii=length;
            }
        }
		return isNumber;
	}

    public boolean hasMorePairs() {
        return current<maxCount;
    }

    public String getNextValueAsString() {
        if(values[current]==null)
            return null;
        else
            return new String(values[current]);
    }

    /**
     * return value as PdfObject or null
     * @return
     */
	public PdfObject getNextValueAsDictionary() {
		
		return objs[current];
	}
}