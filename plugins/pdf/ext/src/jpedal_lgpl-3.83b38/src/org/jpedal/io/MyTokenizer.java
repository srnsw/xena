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
* MyTokenizer.java
* ---------------
*/
package org.jpedal.io;

/**
 * encapsualtes function to read values from a text string
 */
public class MyTokenizer {
	
	/**holds content*/
	private byte[] content;
	
	/**pointers to position reached in string*/
	private int currentCharPointer;

	/**length of string*/
    private int stringLength;


	/**
     * initialise text with value
     */
    public MyTokenizer(String line) {
        
        //turn string into byte stream
        content=line.getBytes();
        
        stringLength=content.length;
        
    }
	
	/** get the char*/
	final private char getChar(int pointer){
		
		int number=(content[pointer] & 0xFF);
		
		return (char) number;
	}
	
	/**
	 * read a next value up to space
	 */
	final public String nextToken() {
		
	    StringBuffer tokenValue=new StringBuffer();
	    
	    boolean hasChars=false;
	    
		char nextChar=getChar(currentCharPointer);
		currentCharPointer++;
		
		//exit on space, otherwise add to string
		while(true){
		    
		    if(nextChar!=' '){
		        tokenValue.append(nextChar);
		        hasChars=true;
		    }
		    
		    if(((nextChar==' ')&&(hasChars))|(currentCharPointer==stringLength))
		        break;
		    
		    nextChar=getChar(currentCharPointer);
		    currentCharPointer++;
		    
		}
		
		return tokenValue.toString();
	}

    /**
     * count number of tokens using space as deliminator
     */
    public int countTokens() {
        
        int tokenCount=1;
        
        /**
         * count spaces in string, ignoring double spaces and any first and last space
         */
        int count=stringLength-1;
        for(int i=1;i<count;i++){
            if((content[i]==32)&&(content[i-1]!=32))
                tokenCount++;
        }
        
        return tokenCount;
    }
}
