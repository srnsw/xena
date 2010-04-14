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
* ImageOps.java
* ---------------
*/
package org.jpedal.images;

import org.jpedal.constants.PDFImageProcessing;

public class ImageOps {

	private static int[] bitCheck={128,64,32,16,8,4,2,1};

	private static byte[] invertByteImage(int w, int h, int componentCount, byte[] data) {
		
		
//		System.out.println("w="+w);
//		System.out.println("h="+h);
//		System.out.println("components="+componentCount);
//		System.out.println("data="+data.length);
		
		//Double check the componentCount
		int temp = (data.length/(w*h));
		
		if(temp!=componentCount){
			componentCount=temp;
		}
		
		
		int bytesInRow=w*componentCount;
		int topByte=0,bottomByte=((h-1)*bytesInRow);
		byte tempByte;

		while(topByte<bottomByte){

			for(int row=0;row<(bytesInRow);row+=componentCount){
				for(int c=0;c<componentCount;c++){
					tempByte=data[topByte+row+c];
					data[topByte+row+c]=data[bottomByte+row+c];
					data[bottomByte+row+c]=tempByte;
				}
			}
			topByte=topByte+bytesInRow;
			bottomByte=bottomByte-bytesInRow;
		}
		return data;
	}

	private static byte[] invertOneBitImage(int w, int h, byte[] data) {

		int bytesInRow=(w+7)/8;
		int topByte=0,bottomByte=(h-1)*bytesInRow;
		byte tempByte;

		while(topByte<bottomByte){

			for(int row=0;row<bytesInRow;row++){
				tempByte=data[topByte+row];
				data[topByte+row]=data[bottomByte+row];
				data[bottomByte+row]=tempByte;
			}
			topByte=topByte+bytesInRow;
			bottomByte=bottomByte-bytesInRow;
		}
		return data;
	}

	private static  byte[] rotateOneBitImage(int w, int h, byte[] data, int newH, int newW) {
		int bytesInRow=(w+7)/8;
		int newBytesInRow=(newW+7)/8;
		
		byte[] newData=new byte[newBytesInRow*newH];

		//read x,y and set as y,x
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){

				//read pixel in existing array as x,y
				int byteUsed=(((h-1)-y)*bytesInRow)+(x>>3);

				byte pixelByte=data[byteUsed];

				//clever optimisation here when ready
				//8 empty bits means ignore next 8 bits
				if(pixelByte==0){
					x=x+7;
				}else{
					int bitUsed=x & 7;

					//if set we need to set in second array, set pixel in new array as y,x
					if((pixelByte & bitCheck[bitUsed])== bitCheck[bitUsed]){

						//read pixel in existing array as x,y
						int newByteUsed=(x*newBytesInRow)+(y>>3);

						byte newPixelByte=newData[newByteUsed];

						int newBitUsed= y & 7;

						//switch on
						newPixelByte= (byte) (newPixelByte | bitCheck[newBitUsed]);

						//write back
						newData[newByteUsed]=newPixelByte;

					}
				}
			}
		}
		
		return newData;
	}

	private static byte[] rotateByteImage(int w, int h, int componentCount, byte[] data){
		
		int temp = (data.length/(w*h));
		if(temp!=componentCount){
			componentCount=temp;
		}
		
		int bytesInRow=w*componentCount;
		int newBytesInRow=h*componentCount;
		byte[] rotatedData = new byte[data.length];
		
		for(int y=0;y<h;y++){
			for(int x=0;x<bytesInRow;x+=componentCount){
				int c = 0;
				
				//ClockWise
				int convertPoint = ((x/componentCount)*(newBytesInRow))+(newBytesInRow-(y*componentCount)-componentCount);
				
				//Anti-ClockWise
//				int convertPoint = (((w-1)-(x/componentCount))*newBytesInRow)+(y*componentCount);
				
				while(c<componentCount){
					
					//x=(x+c)
					//y=(y*bytesInRow)
					rotatedData[convertPoint+c] = data[(y*bytesInRow)+(x+c)];
					c++;
				}
			}
		}
		return rotatedData;
	}

	public static byte[] invertImage(byte[] data, int w, int h, int d,int comp, byte[] index) {
		
		//either process or just return
		if(d==8){ //add in next
			if(index!=null)
				comp = 1;

			return ImageOps.invertByteImage(w, h, comp, data);

		//}else if(d==4){



		}else if(d==1){

			return ImageOps.invertOneBitImage(w, h, data);

		}
		return null;
	}

	public static byte[] rotateImage(byte[] data, int w, int h, int d,int comp, byte[] index) {
		
		//either process or just return
		if(d==8){ //add in next
			if(index!=null)
				comp = 1;
			/**Rotated H and W are calcualted within method*/
			return ImageOps.rotateByteImage(w, h, comp, data);

		}else if(d==4){



		}else if(d==1){
			/**The W and H are swapped for the new values, this gives rotated coords*/
			return ImageOps.rotateOneBitImage(w, h, data, w, h);

		}
		return null;
	}
}
