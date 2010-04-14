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
* CMAP.java
* ---------------
*/
package org.jpedal.fonts.tt;

import java.util.Hashtable;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.utils.LogWriter;

public class CMAP extends Table {
	
	private int[][] glyphIndexToChar;

    private int[] glyphToIndex;
	
    //flag 6 and use if not able to map elsewhere
    private boolean hasSix=false;
	
	/**used to 'guess' wrongly encoded fonts*/ 
	private int winScore=0,macScore=0;
	
	//used by format 4
	int segCount=0;
	
	/**which type of mapping to use*/
	int fontMapping=0;
	
	//used by format 4
	int[] endCode,startCode,idDelta,idRangeOffset,glyphIdArray,f6glyphIdArray,offset;
	
	/**CMap format used -1 shows not set*/
	private int[] CMAPformats;
	
	private boolean maybeWinEncoded=false;
	
	/**Platform-specific ID list*/
//	private static String[] PlatformSpecificID={"Roman","Japanese","Traditional Chinese","Korean",
//			"Arabic","Hebrew","Greek","Russian",
//			"RSymbol","Devanagari","Gurmukhi","Gujarati",
//			"Oriya","Bengali","Tamil","Telugu",
//			"Kannada","Malayalam","Sinhalese","Burmese",
//			"Khmer","Thai","Laotian","Georgian",
//			"Armenian","Simplified Chinese","Tibetan","Mongolian",
//			"Geez","Slavic","Vietnamese","Sindhi","(Uninterpreted)"};
//	
	/**Platform-specific ID list*/
	//private static String[] PlatformIDName={"Unicode","Macintosh","Reserved","Microsoft"};

	/**shows which encoding used*/
	int[] platformID;

	private static Hashtable exceptions;
	
	/**set up differences from Mac Roman*/
	static {
		
		exceptions=new Hashtable();
		
		String[] values={"notequal","173","infinity","176","lessequal","178","greaterequal","179",
				"partialdiff","182","summation","183","product","184","pi","185",
				"integral","186","Omega","189","radical","195","approxequal","197",
				"Delta","198","lozenge","215","Euro","219","apple","240"};
		for(int i=0;i<values.length;i=i+2)
			exceptions.put(values[i],values[i+1]);
		
	}
	
	/**which CMAP to use to decode the font*/
	private int formatToUse;

	/**encoding to use resolving tt font - should be MAC but not always*/
	private int encodingToUse=StandardFonts.MAC;

	private static boolean WINchecked;

	public CMAP(FontFile2 currentFontFile,int startPointer, Glyf currentGlyf){

        final boolean debug=false;

        if(debug)
                System.out.println("CMAP "+this);

		//LogWriter.writeMethod("{readCMAPTable}", 0);

		//read 'cmap' table
		if(startPointer==0)
			LogWriter.writeLog("No CMAP table found");
		else{

			currentFontFile.getNextUint16();//id
			int numberSubtables=currentFontFile.getNextUint16();

			//read the subtables
			int[] CMAPsubtables=new int[numberSubtables];
			platformID=new int[numberSubtables];
			int[] platformSpecificID=new int[numberSubtables];
			CMAPformats=new int[numberSubtables];
			glyphIndexToChar =new int[numberSubtables][256];

            glyphToIndex=new int[256];

			for(int i=0;i<numberSubtables;i++){

				platformID[i]=currentFontFile.getNextUint16();
				platformSpecificID[i]=currentFontFile.getNextUint16();
				CMAPsubtables[i]=currentFontFile.getNextUint32();

                if(debug)
					System.out.println("IDs="+platformSpecificID[i]+" "+platformID[i]+" "+CMAPsubtables[i]);
				//System.out.println(PlatformID[platformID[i]]+" "+PlatformSpecificID[platformSpecificID[i]]+CMAPsubtables[i]);

			}

			//now read each subtable
			for(int j=0;j<numberSubtables;j++){
				currentFontFile.selectTable(FontFile2.CMAP);
				currentFontFile.skip(CMAPsubtables[j]);
				//assume 16 bit format to start
				CMAPformats[j]=currentFontFile.getNextUint16();
				int length=currentFontFile.getNextUint16();
				currentFontFile.getNextUint16();//lang

                if(debug)
                        System.out.println("type="+CMAPformats[j]+" length="+length);
				//flag if present
				if(CMAPformats[j]==6)
					hasSix=true;

				if(CMAPformats[j]==0 && length==262){

					StandardFonts.checkLoaded(StandardFonts.WIN);
					StandardFonts.checkLoaded(StandardFonts.MAC);

					//populate table and get valid glyphs
					//Map uniqueFontMappings=StandardFonts.getUniqueMappings(); //win or mac only values we check for
					//int total=0;

					boolean isValidOnMac,isValidOnWin;
					
					for(int glyphNum=0;glyphNum<256;glyphNum++){

						int index=currentFontFile.getNextUint8();
						glyphIndexToChar[j][glyphNum]=index;
                        glyphToIndex[index]=glyphNum;

						/**count to try and guess if wrongly encoded*/
						if(index>0){//&&(currentGlyf.isPresent(index))){
							
							isValidOnMac=StandardFonts.isValidMacEncoding(glyphNum);
							isValidOnWin=StandardFonts.isValidWinEncoding(glyphNum);
							
							//if any different flag it up
							if(isValidOnMac!=isValidOnWin)
								maybeWinEncoded=true;
								
							
							//System.out.println(Integer.toOctalString(index)+" "+glyphNum+" "+StandardFonts.isValidMacEncoding(glyphNum)+" "+StandardFonts.isValidWinEncoding(glyphNum));
							/**/
							if(isValidOnMac){
								macScore++;
							}//else	
							//	System.out.println(glyphNum+" not MAC");

							if(isValidOnWin){
								winScore++;
							}//else	
							//	System.out.println(glyphNum+" now WIN");
						
							//cumulative WIN or MAC only values found
//							Object uniqueness=uniqueFontMappings.get(new Integer(glyphNum));
//							if(uniqueness!=null){// will give a +1 (mac only) or -1 (win only) to total
//								total=total+((Integer) uniqueness).intValue();
//								//System.out.println(glyphNum+" >>"+((Integer) uniqueness).intValue());
//							}
						}
					}

					//switch to win only if scored several win only values
//					System.out.println("total="+total+" macScore="+macScore+" winScore="+winScore);
//					if(total<-2){
//						macScore=98;
//						winScore=99;
//					}

					//System.out.println("mac="+macScore+" win="+winScore);

				}else if(CMAPformats[j]==4){

					//read values
					segCount = currentFontFile.getNextUint16()/2;
					currentFontFile.getNextUint16(); //searchrange
					currentFontFile.getNextUint16();//entrrySelector
					currentFontFile.getNextUint16();//rangeShift

					//read tables and initialise size of arrays
					endCode = new int[segCount];
					for (int i = 0; i < segCount; i++) 
						endCode[i] = currentFontFile.getNextUint16();

					currentFontFile.getNextUint16(); //reserved (should be zero)

					startCode = new int[segCount];
					for (int i = 0; i < segCount; i++) 
						startCode[i] =currentFontFile.getNextUint16();

					idDelta = new int[segCount];
					for (int i = 0; i < segCount; i++)
						idDelta[i] = currentFontFile.getNextUint16();


					idRangeOffset = new int[segCount];
					for (int i = 0; i < segCount; i++) 
						idRangeOffset[i] = currentFontFile.getNextUint16();

					/**create offsets*/
					offset = new int[segCount];
					int diff=0,cumulative=0;

					for (int i = 0; i < segCount; i++) {

//                        System.out.println("seg="+i+" cumulative="+cumulative+
//                                " idDelta[i]="+idDelta[i]+" startCode[i]="+startCode[i]+
//                        " endCode[i]="+endCode[i]);
//
                        if(idDelta[i]==0){// && startCode[i]!=endCode[i]){
							offset[i]=cumulative;
							diff=1+endCode[i]-startCode[i];

                            cumulative=cumulative+diff;
						}

					}

					// glyphIdArray at end
					int count = (length -16-(segCount*8)) / 2;

					glyphIdArray = new int[count];
					for (int i = 0; i < count; i++){
						glyphIdArray[i] =currentFontFile.getNextUint16();
                    }

				}else if(CMAPformats[j]==6){
					int firstCode=currentFontFile.getNextUint16();
					int entryCount=currentFontFile.getNextUint16();

					f6glyphIdArray = new int[firstCode+entryCount];
					for(int jj=0;jj<entryCount;jj++)
						f6glyphIdArray[jj+firstCode]=currentFontFile.getNextUint16();

				}else{
					//System.out.println("Unsupported Format "+CMAPformats[j]);
					//reset to avoid setting
					CMAPformats[j]=-1;

				}

				//System.out.println(" <> "+platformID[j]);
				//System.out.println(CMAPformats[j]+" "+platformID[j]+" "+platformSpecificID[j]+" "+PlatformIDName[platformID[j]]);

			}
		}

		/**validate format zero encoding*/
		//if(formatFour!=-1)
		//validateMacEncoding(formatZero,formatFour);

	}
	

		
	/**convert raw glyph number to Character code*/
	public int convertIndexToCharacterCode(String glyph,int index,boolean remapFont,boolean isSubsetted, String[] diffTable){
		
		int index2=-1, rawIndex=index;
		
        final boolean debugMapping=false;//(index==223);

        if(debugMapping)
        System.out.println(glyph+" fontMapping="+fontMapping+" index="+index+" encodingToUse="+encodingToUse+ ' ' +encodingToUse+" WIN="+StandardFonts.WIN +" MAC="+StandardFonts.MAC);
        
        /**convert index if needed*/
		if((fontMapping==1 || (!remapFont &&fontMapping==4))&&(glyph!=null)&&(!glyph.equals("notdef"))){
			
			index=StandardFonts.getAdobeMap(glyph);

            if(debugMapping)
            System.out.println("convert index");

        }else if (fontMapping==2){


            StandardFonts.checkLoaded(encodingToUse);
			
			if(encodingToUse==StandardFonts.MAC){
			    
				Object exception=null;
				if(glyph!=null)
				    exception=exceptions.get(glyph);
				
				if(exception==null){
				    if(glyph!=null && !isSubsetted)
					index=StandardFonts.lookupCharacterIndex(glyph,encodingToUse);
				}else if(diffTable==null || diffTable[index]==null){ //not if a diff
					try{
					index=Integer.parseInt(((String)exception));
					}catch(Exception ee){
					}
				}
				//win indexed just incase
				if(glyph!=null){
					if(!WINchecked){
						StandardFonts.checkLoaded(StandardFonts.WIN);
						WINchecked=true;
					}
					index2=StandardFonts.lookupCharacterIndex(glyph,StandardFonts.WIN);
				}
			}else if(glyph!=null)
				index=StandardFonts.lookupCharacterIndex(glyph,encodingToUse);
		}

        int value=-1;
		int format=CMAPformats[formatToUse];


        //remap if flag set
		if(remapFont && format>0 && format!=6)
			index=index + 0xf000;
		
		//if no cmap use identity
		if(format==0){

			//hack
			if(index>255)
				index=0;
			
			value= glyphIndexToChar[formatToUse][index];
			if((value==0)&(index2!=-1))
				value= glyphIndexToChar[formatToUse][index2];

		}else	if(format==4){

            for (int i = 0; i < segCount; i++) {

            	if(debugMapping)
                System.out.println("Segtable="+i+" start="+startCode[i]+" "+index+
                        " end="+endCode[i]+" idRangeOffset[i]="+idRangeOffset[i]+
                        " offset[i]="+offset[i]+" idRangeOffset[i]="+idRangeOffset[i]+" idDelta[i]="+idDelta[i]);
            	
				if (endCode[i] >= index && startCode[i] <= index){
		
					int idx=0;
                    if (idRangeOffset[i] == 0) {
						
                    	if(debugMapping)
                    	System.out.println("xxx="+(idDelta[i] + index));
						
						value= (idDelta[i] + index) % 65536;

                        i=segCount;
                    }else{
						
						idx= offset[i]+(index - startCode[i]);
						value=glyphIdArray[idx];

                        if(debugMapping)
                        System.out.println("value="+value+" idx="+
                                idx+" glyphIdArrays="+glyphIdArray[0]+" "+
                                glyphIdArray[1]+" "+glyphIdArray[2]+" offset[i]="+offset[i]+
                        " index="+index+" startCode["+i+"]="+startCode[i]+" i="+i);

                        i=segCount;

                    }
				}
			}
		}
		
		//second attempt if no value found
		if(value==-1 && hasSix){
			index=rawIndex;
			format=6;
		}
		
		if(format==6){
			if(index>=f6glyphIdArray.length)
				value=0;
			else
				value=f6glyphIdArray[index];
		}
		
	//	System.out.println(value+" format="+format);

        if(debugMapping)
        System.out.println("returns "+value);

        return value;
	}

	/**
	 * work out correct CMAP table to use.
	 */
	public void setEncodingToUse(boolean hasEncoding,int fontEncoding,boolean isSubstituted,boolean isCID) {

        final boolean encodingDebug=false;

        if(encodingDebug)
        System.out.println(this+ "hasEncoding="+hasEncoding+" fontEncoding="+fontEncoding+" isSubstituted="+isSubstituted+" isCID="+isCID+"  macScore="+macScore);

        formatToUse=-1;


        int count=platformID.length;
		
		//this code changes encoding to WIN if that appears to be encoding used in spite of being MAC
		if(!isSubstituted && macScore<207){

			//System.out.println(macScore+" winScore="+winScore+" count="+count+" "+hasEncoding+"  "+fontEncoding+"  "+StandardFonts.WIN);
            
			if(glyphToIndex!=null && macScore>90 && !maybeWinEncoded){
                //System.out.println("Its Mac");
            
			}else if(glyphToIndex!=null && macScore>205 && glyphToIndex[138]!=0 && glyphToIndex[228]==0 ){
                //System.out.println("Its Mac");
            }else{

			if(count>0 && winScore>macScore)
			this.encodingToUse=StandardFonts.WIN;
			
			if(macScore>80 && hasEncoding && fontEncoding==StandardFonts.WIN && winScore>=macScore)
				encodingToUse=StandardFonts.WIN;
			}
        }
		
		//System.out.println(isSubstituted+" "+encodingToUse+" WIN="+StandardFonts.WIN+" MAC="+StandardFonts.MAC);
		
        if(encodingDebug)
                System.out.println("macScore="+macScore+" winScore="+winScore+" count="+count+" isSubstituted="+isSubstituted);

        /**case 1 */
        for(int i=0;i<count;i++){
            if(encodingDebug)
            System.out.println("Maps="+platformID[i]+" "+CMAPformats[i]);
            if((platformID[i]==3)&&(CMAPformats[i]==1)){
                formatToUse=i;
                this.fontMapping=1;
                i=count;
                //StandardFonts.loadAdobeMap();

                if(encodingDebug)
                System.out.println("case1");
            }
        }

        /**case 2*/
		if(formatToUse==-1 && !isCID && (!isSubstituted ||(CMAPformats.length==1 && CMAPformats[0]==0))){
			
			for(int i=0;i<count;i++){
				if(platformID[i]==1 && CMAPformats[i]==0){
					formatToUse=i;
					if(hasEncoding || fontEncoding==StandardFonts.WIN)
						fontMapping=2;
					else
						fontMapping=3;
						
					i=count;

                }
			}

            if(encodingDebug)
                System.out.println("case2 fontMapping="+fontMapping);
        }

        /**case 3 - no MAC cmap in other ranges and substituting font */
        boolean wasCase3=false;
        if(formatToUse==-1){
			for(int i=0;i<count;i++){
				//if((platformID[i]==1)&&(CMAPformats[i]==6)){ Altered 20050921 to fix problem with Doucsign page
				if((CMAPformats[i]==6)){
					formatToUse=i;
					if((!hasEncoding)&&(fontEncoding==StandardFonts.WIN)){
						fontMapping=2;
						StandardFonts.checkLoaded(StandardFonts.MAC);
					}else
						fontMapping=6;

                    wasCase3=true;
                    i=count;
				}
			}

            if(encodingDebug)
                System.out.println("case3");
        }

        /**case 4 - no simple maps or prefer to last 1*/
		if((formatToUse==-1) || wasCase3){//&&((!isSubstituted)|(isCID))){
		//if((formatToUse==-1)){
			for(int i=0;i<count;i++){
				if((CMAPformats[i]==4)){
					formatToUse=i;
					fontMapping=4;
					i=count;
				}
			}

            if(encodingDebug)
                System.out.println("case4 fontMapping="+fontMapping+" formatToUse="+formatToUse);
        }

		//System.out.println(formatToUse+" " +fontMapping);
		
		if(fontEncoding==StandardFonts.ZAPF){
			fontMapping=2;

            if(encodingDebug)
                System.out.println("Zapf");
        }
        
		//further test
		if(encodingToUse==StandardFonts.WIN && macScore==winScore && 
				glyphIndexToChar[formatToUse][146]==0 && glyphIndexToChar[formatToUse][213]!=0){ //quoteright
			encodingToUse=StandardFonts.MAC;
		}
	}
}
