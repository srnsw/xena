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
* StringUtils.java
* ---------------
*/
package org.jpedal.utils;

public class StringUtils {

    /**
     * quick code to make text lower case
     */
    public static String toLowerCase(String str){

        int len=str.length();
        char c;
        char[] chars= str.toCharArray();

        //strip out any odd codes
        boolean isChanged=false;
        for(int jj=0;jj<len;jj++){
            c=chars[jj];

            //ensure lower case and flip if not
            if(c>64 && c<91){
                c=(char)(c+32);
                chars[jj]=c;
                isChanged=true;
            }
        }

        if(isChanged)
            return String.copyValueOf(chars,0,len);
        else
            return str;

    }
    
    public static String toUpperCase(String str){

        int len=str.length();
        char c;
        char[] chars= str.toCharArray();

        //strip out any odd codes
        boolean isChanged=false;
        for(int jj=0;jj<len;jj++){
            c=chars[jj];

            //ensure UPPER case and flip if not
            if(c>96 && c<123){
                c=(char)(c-32);
                chars[jj]=c;
                isChanged=true;
            }
        }

        if(isChanged)
            return String.copyValueOf(chars,0,len);
        else
            return str;

    }
    
    static final public String handleEscapeChars(String value) {
		//deal with escape characters
		int escapeChar=value.indexOf('\\');

		while(escapeChar!=-1){
		    char c=value.charAt(escapeChar+1);
		    if(c=='n'){
		        c='\n';
		    }else{
		    }


		    value=value.substring(0,escapeChar)+c+value.substring(escapeChar+2,value.length());

		    escapeChar=value.indexOf('\\');
		}
		return value;
	}
          
    /**
     * turn any hex values (ie #e4) into chars 
     * @param value
     * @return
     */
    static final public String convertHexChars(String value) {

        //avoid null
        if(value==null)
        return value;

        //find char
		int escapeChar=value.indexOf('#');

        if(escapeChar==-1)
        return value;

        //process
        StringBuffer newString=new StringBuffer();
        int length=value.length();
        //newString.setLength(length);

        char c;

        for(int ii=0;ii<length;ii++){
            c=value.charAt(ii);

            if(c=='#'){
                ii++;
                int end=ii+2;
                if(end>length)
                end=length;
                String key=value.substring(ii,end);

                c=(char)Integer.parseInt(key,16);

                ii++;

                if(c!=' ')
                newString.append(c);
            }else
                newString.append(c);


        }

        return newString.toString();
	}

	public static boolean isNumber(String textString) {
		byte[] data=textString.getBytes();
		int strLength=data.length;
		boolean isNumber=true;

		//assume true and disprove
		for(int j=0;j<strLength;j++){
		    if((data[j]>='0' && data[j] <='9')|| data[j]=='.'){ //assume and disprove
		    }else{
		        isNumber=false;
		        //exit loop
		        j=strLength;
		    }
		}
		
		return isNumber;
	}
}
