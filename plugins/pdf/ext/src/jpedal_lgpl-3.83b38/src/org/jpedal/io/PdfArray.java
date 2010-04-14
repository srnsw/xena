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
* PdfArray.java
* ---------------
*/
package org.jpedal.io;
import java.util.StringTokenizer;

public class PdfArray {
	

	/**
	 * @param value
	 * @return
	 */
	public static float[] convertToFloatArray(String value) {
		
		
		final boolean usesNew = true;
		
		//ie [1.0 10.0 11 1 ]
		
		//do new version with flags and then get it to compare output against repository
		
		//mariusz - could be values speeded up if we converted to byte[]
		//scanned once for spaces to get number of values, ignore space following space
		//use code similar to parseDouble code we use in PostScript Factory
		float[] returnValue = null;
		if(!usesNew){
			StringTokenizer matrixValues =new StringTokenizer(value, "[] ");
			returnValue=new float[matrixValues.countTokens()];
			
			int i = 0;
			while (matrixValues.hasMoreTokens()) {
				returnValue[i] = Float.parseFloat(matrixValues.nextToken());
				i++;
			}
		} else {
			// my implementation
			//System.err.println("Value being processed : " + value+ "-end");
			char[] bts = value.toCharArray();
			returnValue = byteStreamToFloatArray(bts);

		}

		return returnValue;
	}
	
	private static float[] byteStreamToFloatArray(char[] bts) {
		
		float[] f = new float[(int)(bts.length/2)];
		int fPointer =0;

		char[] buffer = null;
		byte lb = '[';
		byte rb = ']';
		byte sp = ' ';
		
		int wStart = 0;
		int wEnd = 0;
		
		boolean found = false;

		final boolean mariuszVersion=false;
		
		if(mariuszVersion){
			for(int i=0;i<bts.length;i++){
				
				if(i>=wEnd && (bts[i]!= lb && bts[i]!= rb && bts[i]!= sp)){
					//found a number, now look for the next space
					wStart = i;
					found = false;
					
					for(int z=i;z<bts.length && !found;z++){
						if(bts[z]==sp || bts[z]==rb || z==bts.length-1){
							found=true;
							wEnd = z;
						} 
					}
					
					if((wEnd == bts.length-1)&&(wEnd == wStart)){
						buffer = new char[1];
						
						buffer[0] = bts[wStart];
					}else{
						buffer = new char[wEnd-wStart];
					
						for(int t=0;wStart<wEnd;wStart++){
							buffer[t] = bts[wStart];
							t++;
						}
					}
					
					f[fPointer] = convertToFloat(buffer);
					fPointer++;
				}
			}
		}else{
			
			final boolean debug=false;
			
			if(debug)
				System.out.println("---start---");
			
			int len=bts.length;
			int ptr=0;
			
			//create array which must fit all items
			f=new float[len];
			
			//find the first [
			while(ptr<len && bts[ptr]!='[')
				ptr++;
			
			// in case first '[' is missing start at the begging of bts 
			if(ptr==bts.length)
				ptr=0;
			else
				ptr++;
			
			
			
			
			while(ptr<len){
				
				if(debug)
					System.out.println("ptr now="+ptr+ ' '+(char) bts[ptr]);
				
				//ignore any spaces
				while(ptr<len && bts[ptr]==' ')
					ptr++;
				
				if(debug)
					System.out.println("start val ptr now="+ptr+ ' '+(char) bts[ptr]);
				
				//set start and find next space,] or end
				wStart=ptr;
				
				while(ptr<len && bts[ptr]!=' ' && bts[ptr]!=']')
					ptr++;
				
				if(debug && ptr<len)
					System.out.println("end val ptr now="+ptr+ ' '+(char) bts[ptr]);
				
				int valLen=ptr-wStart;
				
				if(debug && ptr<len)
					System.out.println("len ptr now="+ptr+ ' '+(char) bts[ptr]+" valLen="+valLen);
				
				//ensure exit or convert to float
				if(valLen<1)
					ptr=len;
				else{
					//log value and repeat above until end
					buffer = new char[valLen];
					
					for(int t=0;wStart<ptr;wStart++){
						buffer[t] = bts[wStart];
						t++;
					}
					
					f[fPointer] = convertToFloat(buffer);
					fPointer++;
				}
			}
			
			if(debug)
				System.out.println("---end---");
			
		}
		
		float[] toBeReturned = new float[fPointer];

        System.arraycopy(f, 0, toBeReturned, 0, fPointer);
		
		//generate values with StringTokwenizer and compare

		return toBeReturned;
	}


	/**
	 * @param value
	 * @return
	 */
	public static double[] convertToDoubleArray(String value) {
		
		//ie [1.0 10.0 11 1 ]
		
		//do new version with flags and then get it to compare output against repository
		
		//mariusz - could be values speeded up if we converted to byte[]
		//scanned once for spaces to get number of values, ignore space following space
		//use code similar to parseDouble code we use in PostScript Factory
		double[] returnValue;
		
		StringTokenizer matrixValues =new StringTokenizer(value, "[] ");
		returnValue=new double[matrixValues.countTokens()];
		int i = 0;
		while (matrixValues.hasMoreTokens()) {
			returnValue[i] = Double.parseDouble(matrixValues.nextToken());
			i++;
		}
		
		return returnValue;
	}


    /**
	 * @param value
	 * @return
	 */
	public static String[] convertToStringArray(String value) {

		//do new version with flags and then get it to compare output against repository

		//mariusz - could be values speeded up if we converted to byte[]
		//scanned once for spaces to get number of values, ignore space following space
		//use code similar to parseDouble code we use in PostScript Factory
		String[] returnValue;

		StringTokenizer matrixValues =new StringTokenizer(value, "[] ");
		returnValue=new String[matrixValues.countTokens()];
		int i = 0;
		while (matrixValues.hasMoreTokens()) {
			returnValue[i] = matrixValues.nextToken();
			i++;
		}

		return returnValue;
	}

    /**
	 * @param value
	 * @return
	 */
	public static boolean[] convertToBooleanArray(String value) {

		//ie [true true false false]

		//do new version with flags and then get it to compare output against repository

		//mariusz - could be values speeded up if we converted to byte[]
		//scanned once for spaces to get number of values, ignore space following space
		//use code similar to parseDouble code we use in PostScript Factory
		boolean[] returnValue;

		StringTokenizer matrixValues =new StringTokenizer(value, "[] ");
		returnValue=new boolean[matrixValues.countTokens()];
		int i = 0;
		while (matrixValues.hasMoreTokens()) {
			returnValue[i] = Boolean.valueOf(matrixValues.nextToken()).booleanValue();
			i++;
		}

		return returnValue;
	}

    /**
	 * @param value
	 * @return
	 */
	public static int[] convertToIntArray(String value) {
		
		//ie [1.0 10.0 11 1 ]
		
		//do new version with flags and then get it to compare output against repository
		
		//mariusz - could be values speeded up if we converted to byte[]
		//scanned once for spaces to get number of values, ignore space following space
		//use code similar to parseDouble code we use in PostScript Factory
		int[] returnValue;
		
		StringTokenizer matrixValues =new StringTokenizer(value, "[] ");
		returnValue=new int[matrixValues.countTokens()];
		int i = 0;
		while (matrixValues.hasMoreTokens()) {
			returnValue[i] = Integer.parseInt(matrixValues.nextToken());
			i++;
		}
		
		return returnValue;
	}
	
	/**
	 * @param value
	 * @return
	 */
	public static Object[] convertToObjectArray(String value) {
		
		//Mariusz - DON't WORRY ABOUT THIS - WE MAY NOT NEED IT
		
		
		//ie [1.0 10.0 11 1 ]
		
		//do new version with flags and then get it to compare output against repository
		
		//mariusz - could be values speeded up if we converted to byte[]
		//scanned once for spaces to get number of values, ignore space following space
		//use code similar to parseDouble code we use in PostScript Factory
		Object[] returnValue;
		
		StringTokenizer matrixValues =new StringTokenizer(value, "[] ");
		returnValue=new Object[matrixValues.countTokens()];
		int i = 0;
		while (matrixValues.hasMoreTokens()) {
			returnValue[i] = matrixValues.nextToken();
			i++;
		}
		
		return returnValue;
	}
	
	
	
	private static float convertToFloat(char[] stream) {
		
		
		
		/*System.err.println("---------");
		for(int ii=0;ii<stream.length;ii++)
			System.err.println(ii+" "+stream[ii]+" "+(char)stream[ii]);*/
		
		float d=0;
		
		float dec=0f,num=0f;

		int start=0;
		int charCount=stream.length;
		

		int ptr=charCount;
		int intStart=0;
		boolean isMinus=false;
		//hand optimised float code
		//find decimal point
		for(int j=charCount-1;j>-1;j--){
			if(stream[start+j]==46){ //'.'=46
				ptr=j;
				break;
			}
		}

		int intChars=ptr;
		//allow for minus
		if(stream[start]==43){ //'+'=43
			intChars--;
			intStart++;
		}else if(stream[start]==45){ //'-'=45
			//intChars--;
			intStart++;
			isMinus=true;
		}

		//optimisations
		int intNumbers=intChars-intStart;
		int decNumbers=charCount-ptr;

		if((intNumbers>4)){ //non-optimised to cover others
			isMinus=false;
			
			d=Float.parseFloat(new String(stream));
			
		}else{

			float thous=0f,units=0f,tens=0f,hundreds=0f,tenths=0f,hundredths=0f, thousands=0f, tenthousands=0f,hunthousands=0f;
			int c;

			//thousands
			if(intNumbers>3){
				c=stream[start+intStart]-48;
				switch(c){
				case 1:
					thous=1000.0f;
					break;
				case 2:
					thous=2000.0f;
					break;
				case 3:
					thous=3000.0f;
					break;
				case 4:
					thous=4000.0f;
					break;
				case 5:
					thous=5000.0f;
					break;
				case 6:
					thous=6000.0f;
					break;
				case 7:
					thous=7000.0f;
					break;
				case 8:
					thous=8000.0f;
					break;
				case 9:
					thous=9000.0f;
					break;
				}
				intStart++;
			}
			
			//hundreds
			if(intNumbers>2){
				c=stream[start+intStart]-48;
				switch(c){
				case 1:
					hundreds=100.0f;
					break;
				case 2:
					hundreds=200.0f;
					break;
				case 3:
					hundreds=300.0f;
					break;
				case 4:
					hundreds=400.0f;
					break;
				case 5:
					hundreds=500.0f;
					break;
				case 6:
					hundreds=600.0f;
					break;
				case 7:
					hundreds=700.0f;
					break;
				case 8:
					hundreds=800.0f;
					break;
				case 9:
					hundreds=900.0f;
					break;
				}
				intStart++;
			}

			//tens
			if(intNumbers>1){
				c=stream[start+intStart]-48;
				switch(c){
				case 1:
					tens=10.0f;
					break;
				case 2:
					tens=20.0f;
					break;
				case 3:
					tens=30.0f;
					break;
				case 4:
					tens=40.0f;
					break;
				case 5:
					tens=50.0f;
					break;
				case 6:
					tens=60.0f;
					break;
				case 7:
					tens=70.0f;
					break;
				case 8:
					tens=80.0f;
					break;
				case 9:
					tens=90.0f;
					break;
				}
				intStart++;
			}

			//units
			if(intNumbers>0){
				c=stream[start+intStart]-48;
				switch(c){
				case 1:
					units=1.0f;
					break;
				case 2:
					units=2.0f;
					break;
				case 3:
					units=3.0f;
					break;
				case 4:
					units=4.0f;
					break;
				case 5:
					units=5.0f;
					break;
				case 6:
					units=6.0f;
					break;
				case 7:
					units=7.0f;
					break;
				case 8:
					units=8.0f;
					break;
				case 9:
					units=9.0f;
					break;
				}
			}

			//tenths
			if(decNumbers>1){
				ptr++; //move beyond.
				c=stream[start+ptr]-48;
				switch(c){
				case 1:
					tenths=0.1f;
					break;
				case 2:
					tenths=0.2f;
					break;
				case 3:
					tenths=0.3f;
					break;
				case 4:
					tenths=0.4f;
					break;
				case 5:
					tenths=0.5f;
					break;
				case 6:
					tenths=0.6f;
					break;
				case 7:
					tenths=0.7f;
					break;
				case 8:
					tenths=0.8f;
					break;
				case 9:
					tenths=0.9f;
					break;
				}
			}

			//hundredths
			if(decNumbers>2){
				ptr++; //move beyond.
				//c=value.charAt(floatptr)-48;
				c=stream[start+ptr]-48;
				switch(c){
				case 1:
					hundredths=0.01f;
					break;
				case 2:
					hundredths=0.02f;
					break;
				case 3:
					hundredths=0.03f;
					break;
				case 4:
					hundredths=0.04f;
					break;
				case 5:
					hundredths=0.05f;
					break;
				case 6:
					hundredths=0.06f;
					break;
				case 7:
					hundredths=0.07f;
					break;
				case 8:
					hundredths=0.08f;
					break;
				case 9:
					hundredths=0.09f;
					break;
				}
			}

			//thousands
			if(decNumbers>3){
				ptr++; //move beyond.
				c=stream[start+ptr]-48;
				switch(c){
				case 1:
					thousands=0.001f;
					break;
				case 2:
					thousands=0.002f;
					break;
				case 3:
					thousands=0.003f;
					break;
				case 4:
					thousands=0.004f;
					break;
				case 5:
					thousands=0.005f;
					break;
				case 6:
					thousands=0.006f;
					break;
				case 7:
					thousands=0.007f;
					break;
				case 8:
					thousands=0.008f;
					break;
				case 9:
					thousands=0.009f;
					break;
				}
			}

			//tenthousands
			if(decNumbers>4){
				ptr++; //move beyond.
				c=stream[start+ptr]-48;
				switch(c){
				case 1:
					tenthousands=0.0001f;
					break;
				case 2:
					tenthousands=0.0002f;
					break;
				case 3:
					tenthousands=0.0003f;
					break;
				case 4:
					tenthousands=0.0004f;
					break;
				case 5:
					tenthousands=0.0005f;
					break;
				case 6:
					tenthousands=0.0006f;
					break;
				case 7:
					tenthousands=0.0007f;
					break;
				case 8:
					tenthousands=0.0008f;
					break;
				case 9:
					tenthousands=0.0009f;
					break;
				}
			}

//			tenthousands
			if(decNumbers>5){
				ptr++; //move beyond.
				c=stream[start+ptr]-48;

				switch(c){
				case 1:
					hunthousands=0.00001f;
					break;
				case 2:
					hunthousands=0.00002f;
					break;
				case 3:
					hunthousands=0.00003f;
					break;
				case 4:
					hunthousands=0.00004f;
					break;
				case 5:
					hunthousands=0.00005f;
					break;
				case 6:
					hunthousands=0.00006f;
					break;
				case 7:
					hunthousands=0.00007f;
					break;
				case 8:
					hunthousands=0.00008f;
					break;
				case 9:
					hunthousands=0.00009f;
					break;
				}
			}

			dec=tenths+hundredths+thousands+tenthousands+hunthousands;
			num=thous+hundreds+tens+units;
			d=num+dec;

		}

		if(isMinus)
			return -d;
		else
			return d;
	}
}
