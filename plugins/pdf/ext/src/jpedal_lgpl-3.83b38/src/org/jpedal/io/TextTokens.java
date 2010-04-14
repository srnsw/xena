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
* TextTokens.java
* ---------------
*/
package org.jpedal.io;

/**
 * encapsualtes function to read values from a text string
 */
public class TextTokens {
	
	/**holds content*/
	private byte[] content;
	
	/**pointers to position to see if finished*/
	private int length=0, currentCharPointer;

	/**
	 * initialise text with value
	 */
	public TextTokens(byte[] rawText) {
		
		content=rawText;
		length=rawText.length;
		currentCharPointer=0;
		
		/**remove any brackets
		if(rawText.startsWith("("))
			rawText=rawText.substring(1);
		if(rawText.endsWith(")"))

		for(int j=0;j<rawText.length;j++){
			System.out.println(j+" "+rawText[j]+" "+(char)rawText[j]);
		}
		
		//rawText=rawText.substring(0,rawText.length()-1);
		*/
		
	}

	/**
	 * see if end reached
	 */
	public boolean hasMoreTokens() {
		if(currentCharPointer<length)
			return true;
		else
			return false;
	}

	/**
	 * read the next double char
	 */
	public char nextUnicodeToken() {
		
		int first=0,second=0;
		
		first=nextToken();
		if((first==13)&(this.hasMoreTokens()))
			first=nextToken();
		
		if(this.hasMoreTokens()){
			second=nextToken();
			if((second==13)&(this.hasMoreTokens()))
				second=nextToken();
		}
		
		return (char) ((first<<8)+second);
	}

	/** get the char*/
	private char getChar(int pointer){
		
		int number=(content[pointer] & 0xFF);
		
		return (char) number;
	}
	
	/**
	 * read a single char
	 */
	public char nextToken() {
		
		char nextChar=getChar(currentCharPointer);
		currentCharPointer++;
		
		/**
		//handle escape
		if(nextChar=='\\'){
			
			nextChar=getChar(currentCharPointer);
			currentCharPointer++;
			
			//if number ,read rest of value and convert
			if(Character.isDigit(nextChar)){
				
				StringBuffer octal=new StringBuffer();
				octal.append(nextChar);
				
				for(int jj=0;jj<2;jj++){
					
					if(currentCharPointer<length){ //avoid problem at end of string
						nextChar=getChar(currentCharPointer);
						
						if(Character.isDigit(nextChar)){
							octal.append(nextChar);
							currentCharPointer++;
						}else
							jj=4;
					}else
						jj=4;
					
				}
				/**
				System.out.println("octal="+octal);
				if(octal.length()>3)
					nextChar=(char) Integer.parseInt(octal.toString(),16);
				else*/
		/**
					nextChar=(char) Integer.parseInt(octal.toString(),8);
				
			}	
		}*/
		
		return nextChar;
	}

	/**
	 * test start to see if unicode
	 */
	public boolean isUnicode() {
		
		//test if unicode by reading first 2 values
		if((length>=2)&&(nextToken()==254)&&(nextToken()==255)){
			
			return true;
		}else {
			//its not unicode to put pointer back to start
			this.currentCharPointer=0;
			return false;
		}
	}

}
