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
 * PdfReader.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.objects.raw.*;
import org.jpedal.color.ColorSpaces;
import org.jpedal.constants.PDFflags;
import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.objects.Javascript;

import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Sorts;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.parser.PdfStreamDecoder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * extends PdfFileReader and PdfFilteredFileReader to
 * provide access at object level to data in pdf file
 */
public class PdfReader extends PdfFilteredReader implements PdfObjectReader, Serializable {

    private int newCacheSize=-1;

	private boolean debugAES=false;

	/**used to cache last compressed object*/
	private byte[] lastCompressedStream=null;

	//text fields
	//private Map fields=new HashMap();

	/**location of end ref*/
	private Vector_Int xref=new Vector_Int(100);

	/**used to cache last compressed object*/
	private Map lastOffsetStart,lastOffsetEnd;

	private PdfObject compressedObj;

	/**used to cache last compressed object*/
	private int lastFirst=-1,lastCompressedID=-1;

	/**current/last object read*/
	//private Map objData=null;

	PdfObject encyptionObj=null;

	/**names lookup table*/
	private Map nameLookup=new HashMap();

	/**allows cache of data so not reread if requested consecutive times*/
	//private String lastRef="";

	/**Information object holds information from file*/
	PdfFileInformation currentFileInformation = new PdfFileInformation();

	/**pattern to look for in objects*/
	final static private String pattern= "obj";


	/**flag to show if extraction allowed*/
	private boolean extractionIsAllowed = true;

	private final static byte[] endPattern = { 101, 110, 100, 111, 98, 106 }; //pattern endobj

	private final static byte[] newPattern = "obj".getBytes();

	private final static byte[] oldPattern = "xref".getBytes();

	private final static byte[] endObj = { 32, 111, 98, 106 }; //pattern endobj

	private final static byte[] lengthString = { 47, 76, 101, 110, 103, 116, 104}; //pattern /Length
	private final static byte[] startStream = { 115, 116, 114, 101, 97, 109};
	private final static byte[] endStream = { 101, 110, 100, 115, 116, 114, 101, 97,109 };

	/**flag to show data encrytped*/
	private boolean isEncrypted = false;

	/**flag to show provider read*/
	private boolean isInitialised=false;

	/**encryption password*/
	private byte[] encryptionPassword = new byte[ 0 ];

	/**info object*/
	private PdfObject infoObject=null;

	/**key used for encryption*/
	private byte[] encryptionKey=null;

	/**flag to show if user can view file*/
	private boolean isFileViewable=true;

	/** revision used for encryption*/
	private int rev=0;

	/**length of encryption key used*/
	private int keyLength=5;

	/**P value in encryption*/
	private int P=0;

	/**O value in encryption*/
	private byte[] O=new byte[0];

	/**U value in encryption*/
	private byte[] U=new byte[0];

	/**holds file ID*/
	private byte[] ID=null;

    final static private byte[] EOFpattern = { 37, 37, 69, 79, 70 }; //pattern %%EOF
    final static private byte[] trailerpattern = { 't','r','a','i','l','e','r' }; //pattern %%EOF
    

	/**flag if password supplied*/
	private boolean isPasswordSupplied=false;

	/**cipher used for decryption*/
	private Cipher cipher=null;
	
	/**encryption padding*/
	private String[] padding={"28","BF","4E","5E","4E","75","8A","41","64","00","4E","56","FF","FA","01","08",
			"2E","2E","00","B6","D0","68","3E","80","2F","0C","A9","FE","64","53","69","7A"};

	/**length of each object*/
	private int[] ObjLengthTable;

	private boolean refTableInvalid=false;

	// additional values for V4 option
	//String EFF,CFM;

	private boolean isMetaDataEncypted=true;

	private PdfObject StmFObj,StrFObj;

	private boolean stringsEncoded=false;

	//tell user status on password
	private int passwordStatus=0;
	private Javascript javascript;

	private boolean isAESIdentity=false;

	//show if AES encryption
	private boolean isAES=false;

	private static boolean alwaysReinitCipher=false;


	static{
		String flag=System.getProperty("org.jpedal.cipher.reinit");
		if(flag!=null && flag.toLowerCase().equals("true"))
			alwaysReinitCipher=true;

	}


	public PdfReader() {

		//setup a list of fields which are string values
//		fields.put("T","x");
//		fields.put("NM","x");
//		fields.put("TM","x");
//		fields.put("TU","x");
//		fields.put("CA","x");
//		fields.put("R","x");
//		fields.put("V","x");
//		fields.put("RC","x");
//		fields.put("DA","x");
//		fields.put("DV","x");
//		fields.put("JS","x");
//		fields.put("Contents","x");

	}

    /**
	 * set password as well
	 */
	public PdfReader(String password) {
		super();

		if(password==null)
			password="";

		setEncryptionPassword(password);
	}

	//pass in Javascript object
	public void setJavaScriptObject(Javascript javascript) {
		this.javascript=javascript;
	}

	/**
	 * read first start ref from last 1024 bytes
	 */
	private int readFirstStartRef() throws PdfException {

		//reset flag
		refTableInvalid=false;
		int pointer = -1;
		int i = 1019;
		StringBuffer startRef = new StringBuffer();

		/**move to end of file and read last 1024 bytes*/
		int block=1024;
		byte[] lastBytes = new byte[block];
		long end;

		/**
		 * set endpoint, losing null chars and anything before EOF
		 */
		final int[] EndOfFileMarker={37,37,69,79,70};
		int valReached=4;
		boolean EOFFound=false;
		try {
			end=eof;

			/**
			 * lose nulls and other trash from end of file
			 */
			int bufSize=255;
			while(true){
				byte[] buffer=new byte[bufSize];

				movePointer(end-bufSize);
				pdf_datafile.read(buffer); //get next chars

				int offset=0;

				for(int ii=bufSize-1;ii>-1;ii--){

					//see if we can decrement EOF tracker or restart check
					if(!EOFFound)
						valReached=4;

					if(buffer[ii]==EndOfFileMarker[valReached]){
						valReached--;
						EOFFound=true;
					}else
						EOFFound=false;

					//move to next byte
					offset--;

					if(valReached<0)
						ii=-1;

				}

				//exit if found values on loop
				if(valReached<0){
					end=end-offset;
					break;
				}else{
					end=end-bufSize;
				}

				//allow for no eof
				if(end<0){
					end=eof;
					break;
				}
			}

			//end=end+bufSize;

			//allow for very small file
			int count=(int)(end - block);

			if(count<0){
				count=0;
				int size=(int)eof;
				lastBytes=new byte[size];
				i=size+3; //force reset below
			}

			movePointer(count);

			pdf_datafile.read(lastBytes);


		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " reading last 1024 bytes");
			throw new PdfException( e + " reading last 1024 bytes");
		}

		//		for(int ii=0;ii<lastBytes.length;ii++){
		//		System.out.print((char)lastBytes[ii]);
		//		}
		//		System.out.println();

		//look for tref as end of startxref
		int fileSize=lastBytes.length;

		if(i>fileSize)
			i=fileSize-5;

		while (i >-1) {

			if ((lastBytes[i] == 116)
					&& (lastBytes[i + 1] == 120)
					&& (lastBytes[i + 2] == 114)
					&& (lastBytes[i + 3] == 101)
					&& (lastBytes[i + 4] == 102))
				break;


			i--;

		}

		/**trap buggy files*/
		if(i==-1){
			try {
				this.pdf_datafile.close();
			} catch (IOException e1) {
				LogWriter.writeLog("Exception "+e1+" closing file");
			}
			throw new PdfException( "No Startref found in last 1024 bytes ");
		}

		i = i + 5; //allow for word length

		//move to start of value ignoring spaces or returns
		while (i < 1024 && (lastBytes[i] == 10 || lastBytes[i] == 32 || lastBytes[i] == 13))
			i++;

		//move to start of value ignoring spaces or returns
		while ((i < 1024)
				&& (lastBytes[i] != 10)
				&& (lastBytes[i] != 32)
				&& (lastBytes[i] != 13)) {
			startRef.append((char) lastBytes[i]);
			i++;
		}

		/**convert xref to string to get pointer*/
		if (startRef.length() > 0)
			pointer = Integer.parseInt(startRef.toString());

		if (pointer == -1){
			LogWriter.writeLog("No Startref found in last 1024 bytes ");
			try {
				this.pdf_datafile.close();
			} catch (IOException e1) {
				LogWriter.writeLog("Exception "+e1+" closing file");
			}
			throw new PdfException( "No Startref found in last 1024 bytes ");
		}

		return pointer;
	}


	/**set a password for encryption*/
	public void setEncryptionPassword(String password){

		this.encryptionPassword = password.getBytes();
	}


	/**
	 * turns any refs into String or Map
	 *
	public Object resolveToMapOrString(Object command, Object field) {


		if((fields!=null)&&(fields.get(command)!=null)&&(field instanceof byte[])){

			byte[] fieldBytes=getByteTextStringValue(field,fields);
			field=PdfReader.getTextString(fieldBytes);

		}else if((field instanceof String)&&(((String)field).endsWith(" R"))){
			Object value = field;

			Map indirectObject=readObject(new PdfObject((String) value), (String) value, fields);

			//System.out.println(value+" "+indirectObject);
			int keyCount=indirectObject.size();
			if(keyCount==1){
				Object stringValue= indirectObject.get("rawValue");
				if(stringValue!=null){
					if(stringValue instanceof String)
						value =stringValue;
					else
						value = PdfReader.getTextString((byte[]) stringValue);

				}else
					value =indirectObject;
			}else
				value =indirectObject;


			Object newObj= value;

			if(newObj instanceof Map){

				Map newField=(Map)newObj;
				//newField.put("obj",field); //store name
				this.readStream((String)field,true); //force decode of any streams
				field=newField;

			}else
				field=newObj;

		}

		return field;
	}/**/

	private byte[] readObjectData(int bufSize, PdfObject pdfObject){

		if(pdf_datafile==null)
			return new byte[0];
		
		int newCacheSize=-1,startStreamCount=0;
		boolean startStreamFound=false, reachedCacheLimit=false;
		long start=-1;

        if(pdfObject!=null) //only use if values found
			newCacheSize=this.newCacheSize;

		final int XXX=2*1024*1024;
		int rawSize=bufSize,realPos=0;
		boolean lengthSet=false; //start false and set to true if we find /Length in metadata
		boolean streamFound=false;

		if(bufSize<1)
			bufSize=128;

		if(newCacheSize!=-1 && bufSize>newCacheSize)
			bufSize=newCacheSize;

		//array for data
		int ptr=0, maxPtr=bufSize;
		byte[] readData=new byte[maxPtr];
		int charReached = 0,charReached2=0, charReached3=0;
		byte[] array=null,buffer=null,dataRead=null;
		boolean inStream=false;

		/**adjust buffer if less than bytes left in file*/
		long pointer=0,lastEndStream=-1,objStart=-1;

		/**read the object or block*/
		byte currentByte=0;
		int i=bufSize-1, offset=-bufSize, blocksRead=0, lastEnd=-1,lastComment=-1;

		while (true) {

			i++;

			if(i==bufSize){

				//cache data and update counter
				if(blocksRead==1){
					dataRead=buffer;
				}else if(blocksRead>1){

					int bytesRead=dataRead.length;
					int newBytes=buffer.length;
					byte[] tmp=new byte[bytesRead+newBytes];

					//existing data into new array
					System.arraycopy(dataRead, 0, tmp, 0, bytesRead);
					System.arraycopy(buffer, 0, tmp, bytesRead, newBytes);

					dataRead=tmp;
				}
				
				if(streamFound && reachedCacheLimit) //stop if over max size
					break;
				
				blocksRead++;

				/**
				 * read the next block
				 */
				pointer = this.getPointer();

				if(start==-1)
					start=pointer;

				/**adjust buffer if less than bytes left in file*/
				if (pointer + bufSize > eof)
					bufSize = (int) (eof - pointer);

				bufSize += 6;
				buffer = new byte[bufSize];

				/**get bytes into buffer*/
				try {
					pdf_datafile.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}

				offset=offset+i;
				i=0;

			}

			/**write out and look for endobj at end*/
			currentByte = buffer[i];

			if(currentByte=='%') //track comments
				lastComment=realPos;

			/**check for endobj at end - reset if not*/
			if (currentByte == endPattern[charReached] &&  !inStream)
				charReached++;
			else
				charReached = 0;

			//also scan for <SPACE>obj after endstream incase no endobj
			if(streamFound &&currentByte == endObj[charReached2] &&  !inStream)
				charReached2++;
			else
				charReached2 = 0;

			//look for start of stream and set inStream true
			if(newCacheSize!=-1 && !reachedCacheLimit){
				if (startStreamCount<6 && currentByte == startStream[startStreamCount]){
					startStreamCount++;
				}else
					startStreamCount=0;

				if(!startStreamFound && (startStreamCount == 6)) //stream start found so log
					startStreamFound=true;

				//PUT BACK to switch on caching
				if(startStreamFound && ((buffer!=null &&buffer.length>newCacheSize)|| (dataRead!=null &&dataRead.length>newCacheSize))){ //stop if over max size
					pdfObject.setCache(start,this);

					//if(!reachedCacheLimit)
						//System.out.println("Set cache="+start+" "+pdfObject+" "+pdfObject.getObjectRefAsString());
				
					reachedCacheLimit=true;	
				}
			}

			/**if length not set we go on endstream in data*/
			if(!lengthSet){

				//also scan for /Length if it had a valid size
				if(rawSize!=-1){
					if(!streamFound &&currentByte == lengthString[charReached3] &&  !inStream){
						charReached3++;
						if(charReached3==6)
							lengthSet=true;
					}else
						charReached3 = 0;
				}
			}

			if (charReached == 6 || charReached2==4){

				if(!lengthSet)
					break;

				charReached=0;
				charReached2=0;
				lastEnd=realPos;

			}

			if(lengthSet && realPos>=rawSize)
				break;

			if(!inStream){

				readData[ptr]=currentByte;

				ptr++;
				if(ptr==maxPtr){
					if(maxPtr<XXX)
						maxPtr=maxPtr*2;
					else
						maxPtr=maxPtr+100000;

					byte[] tmpArray=new byte[maxPtr];
					System.arraycopy(readData,0,tmpArray,0,readData.length);

					readData=tmpArray;
				}
			}

			realPos++;
		}

		if(blocksRead==1){ //scenario 1 - all in first block
			array=new byte[i];
			System.arraycopy(buffer, 0, array, 0, i);
		}else{
			int bytesRead=dataRead.length;

			array=new byte[bytesRead+i];
			//existing data
			System.arraycopy(dataRead, 0, array, 0, bytesRead);

			//data from current block
			System.arraycopy(buffer, 0, array, bytesRead, i);
		}

		if(lengthSet && lastEnd!=-1 && lastComment!=-1 && lastComment>lastEnd){
			byte[] newArray = new byte[lastEnd];
			System.arraycopy(array, 0, newArray, 0, lastEnd);
			array = newArray;
		}

		if(!lengthSet)
			array = checkEndObject(array, objStart, lastEndStream);


		return array;
	}

	public void spoolStreamDataToDisk(File tmpFile,long start) throws Exception{

        this.movePointer(start);

        boolean hasValues=false;

        // Create output file
        BufferedOutputStream array =new BufferedOutputStream(new FileOutputStream(tmpFile));

        int bufSize=-1;
        PdfObject pdfObject=null;

        int newCacheSize=-1,startStreamCount=0;
        boolean startStreamFound=false;

        if(pdfObject!=null) //only use if values found
        newCacheSize=this.newCacheSize;

		final int XXX=2*1024*1024;

		int rawSize=bufSize,realPos=0;

		final boolean debug=false;

		boolean lengthSet=false; //start false and set to true if we find /Length in metadata
		boolean streamFound=false;

		if(debug)
			System.out.println("=============================");

		if(bufSize<1)
			bufSize=128;

//        if(newCacheSize!=-1 && bufSize>newCacheSize)
        //bufSize=newCacheSize;

        //array for data
		int ptr=0, maxPtr=bufSize;

		byte[] readData=new byte[maxPtr];

		int charReached = 0,charReached2=0, charReached3=0;

		byte[] buffer=null;
		boolean inStream=false,ignoreByte;

		/**adjust buffer if less than bytes left in file*/
		long pointer=0,lastEndStream=-1,objStart=-1;

		/**read the object or block*/
		try {

			byte currentByte=0,lastByte;

			int i=bufSize-1;
			int offset=-bufSize;

			int blocksRead=0;

			int lastEnd=-1,lastComment=-1;

			while (true) {

				i++;

				if(i==bufSize){

					//cache data and update counter
//                    if(blocksRead==1){
//                        dataRead=buffer;
//                    }else if(blocksRead>1){
//
//                        int bytesRead=dataRead.length;
//                        int newBytes=buffer.length;
//                        byte[] tmp=new byte[bytesRead+newBytes];
//
//                        //existing data into new array
//                        System.arraycopy(dataRead, 0, tmp, 0, bytesRead);
//
//                        //data from current block
//                        System.arraycopy(buffer, 0, tmp, bytesRead, newBytes);
//
//                        dataRead=tmp;
//
//                        //PUT BACK to switch on caching
//                        if(1==2 && streamFound && dataRead.length>newCacheSize) //stop if over max size
//                            break;
//                    }
                    blocksRead++;

					/**
					 * read the next block
					 */
					pointer = this.getPointer();

                    if(start==-1)
                        start=pointer;

					/**adjust buffer if less than bytes left in file*/
					if (pointer + bufSize > eof)
						bufSize = (int) (eof - pointer);

					bufSize += 6;
					buffer = new byte[bufSize];

					/**get bytes into buffer*/
					pdf_datafile.read(buffer);

                    offset=offset+i;
					i=0;

				}

				/**write out and look for endobj at end*/
				lastByte=currentByte;
				currentByte = buffer[i];
				ignoreByte=false;

				//track comments
				if(currentByte=='%')
					lastComment=realPos;

				/**check for endobj at end - reset if not*/
				if (currentByte == endPattern[charReached] &&  !inStream)
					charReached++;
				else
					charReached = 0;

				//also scan for <SPACE>obj after endstream incase no endobj
				if(streamFound &&currentByte == endObj[charReached2] &&  !inStream)
					charReached2++;
				else
					charReached2 = 0;

                //look for start of stream and set inStream true

                if(startStreamFound){
                    if(hasValues || currentByte!=13 && currentByte!=10){ //avoid trailing CR/LF
                        array.write(currentByte);
                        hasValues=true;
                    }
                }

                if (startStreamCount<6 && currentByte == startStream[startStreamCount]){
                        startStreamCount++;
                }else
                    startStreamCount=0;

                if(!startStreamFound && (startStreamCount == 6)){ //stream start found so log
                    //startStreamCount=offset+startStreamCount;
                    //pdfObject.setCache(start,this);
                    startStreamFound=true;
                }


				/**if length not set we go on endstream in data*/
				if(!lengthSet){

					//also scan for /Length if it had a valid size
					if(rawSize!=-1){
						if(!streamFound &&currentByte == lengthString[charReached3] &&  !inStream){
							charReached3++;
							if(charReached3==6)
								lengthSet=true;
						}else
							charReached3 = 0;
					}
				}

				if (charReached == 6 || charReached2==4){

					if(!lengthSet)
						break;

					charReached=0;
					charReached2=0;
					lastEnd=realPos;

				}

				if(lengthSet && realPos>=rawSize)
					break;

				if(!ignoreByte && !inStream){//|| !inStream)

                    readData[ptr]=currentByte;

					ptr++;
					if(ptr==maxPtr){
						if(maxPtr<XXX)
							maxPtr=maxPtr*2;
						else
							maxPtr=maxPtr+100000;

						byte[] tmpArray=new byte[maxPtr];
						System.arraycopy(readData,0,tmpArray,0,readData.length);

						readData=tmpArray;
					}
				}

				realPos++;
			}

//            if(blocksRead==1){ //scenario 1 - all in first block
//                array=new byte[i];
//                System.arraycopy(buffer, 0, array, 0, i);
//            }else{
//                int bytesRead=dataRead.length;
//
//                array=new byte[bytesRead+i];
//                //existing data
//                System.arraycopy(dataRead, 0, array, 0, bytesRead);
//
//                //data from current block
//                System.arraycopy(buffer, 0, array, bytesRead, i);
//            }

//            if(lengthSet && lastEnd!=-1 && lastComment!=-1 && lastComment>lastEnd){
//                byte[] newArray = new byte[lastEnd];
//                System.arraycopy(array, 0, newArray, 0, lastEnd);
//                array = newArray;
//            }
//
//			if(!lengthSet)
//				array = checkEndObject(array, objStart, lastEndStream);

		} catch (Exception e) {
			e.printStackTrace();
			LogWriter.writeLog("Exception " + e + " reading object");
		}

		if(array!=null){
            array.flush();
            array.close();
        }
	}


	private static byte[] checkEndObject(byte[] array, long objStart, long lastEndStream) {
		int ObjStartCount = 0;

		//check if mising endobj
		for (int i = 0; i < array.length - 8; i++) {

			//track endstream and first or second obj
			if ((ObjStartCount < 2) && (array[i] == 32) && (array[i + 1] == 111) &&
					(array[i + 2] == 98) && (array[i + 3] == 106)) {
				ObjStartCount++;
				objStart = i;
			}
			if ((ObjStartCount < 2) && (array[i] == 101) && (array[i + 1] == 110) &&
					(array[i + 2] == 100) && (array[i + 3] == 115) &&
					(array[i + 4] == 116) && (array[i + 5] == 114) &&
					(array[i + 6] == 101) && (array[i + 7] == 97) && (array[i + 8] == 109))
				lastEndStream = i + 9;
		}

		if ((lastEndStream > 0) && (objStart > lastEndStream)) {
			byte[] newArray = new byte[(int) lastEndStream];
			System.arraycopy(array, 0, newArray, 0, (int) lastEndStream);
			array = newArray;
		}
		return array;
	}


	/**
	 * read a dictionary object
	 */
	public int readDictionaryAsObject(PdfObject pdfObject, String objectRef, int i, byte[] raw,
			int endPoint, String paddingString, boolean isInlineImage){
		
		boolean debugFastCode=false;

		//@CHRIS see whats happening
		//if(objectRef.equals("77 0 R"))
		//	debugFastCode = true;
//		if(objectRef.equals("139 0 R")){
//			debugFastCode = true;
//			org.jpedal.objects.acroforms.utils.ConvertToString
//					.printStackTrace(1);
//			
//		}
		
        if(debugFastCode)
			paddingString=paddingString+"   ";

		int PDFkeyInt=-1,pdfKeyType=-1,level=0;

        boolean isMap=false;

		//allow for no << at start
		if(isInlineImage)
			level=1;

		Object PDFkey=null;

		//show details in debug mode
		if(debugFastCode){
			System.out.println("\n\n"+paddingString+"level="+level+" ------------readDictionaryAsObject ref="+objectRef+" into "+pdfObject+"-----------------\ni="+i+"\nData=>>>>");
			System.out.print(paddingString);

			for(int jj=i;jj<raw.length;jj++){
				System.out.print((char)raw[jj]);

                //allow for comment
                if(raw[jj]==37){

                    //System.out.println("aa>"+new String(characterStream)+"\n<aa");
                    while(jj<raw.length && raw[jj]!=10 && raw[jj]!=13){
                       // System.out.println(raw[jj]+" "+(char)raw[jj]);
                        jj++;
                    }

                    //move cursor to start of text
                    while(jj<raw.length &&(raw[jj]==9 || raw[jj]==10 || raw[jj]==13 ||
                            raw[jj]==32 || raw[jj]==60))
                    jj++;
                }

				if(jj>5 && raw[jj-5]=='s' && raw[jj-4]=='t' && raw[jj-3]=='r' && raw[jj-2]=='e' && raw[jj-1]=='a' &&raw[jj]=='m')
					jj=raw.length;

				if(jj>2 && raw[jj-2]=='B' && raw[jj-1]=='D' &&raw[jj]=='C')
					jj=raw.length;
			}
			System.out.println(paddingString+"\n<<<<-----------------------------------------------------\n");
		
		}
		
		//  if(objectRef.equals("23 0 R"))
		//          throw new RuntimeException("xx");

		
		int length=raw.length;
		while(true){

			if(i>=length)
				break;

            //allow for comment
            if(raw[i]==37){

                //System.out.println("aa>"+new String(characterStream)+"\n<aa");
                while(i<raw.length && raw[i]!=10 && raw[i]!=13){
                    //System.out.println(raw[i]+" "+(char)raw[i]);
                    i++;
                }

                //move cursor to start of text
                while(i<raw.length &&(raw[i]==9 || raw[i]==10 || raw[i]==13 ||
                        raw[i]==32 || raw[i]==60))
                i++;
            }else if(raw[i]=='s' && raw[i+1]=='t' && raw[i+2]=='r' && raw[i+3]=='e' && raw[i+4]=='a' && raw[i+5]=='m')
				break;

			/**
			 * exit conditions
			 */
			if ((i>=length ||
					(endPoint!=-1 && i>=endPoint))||
					((raw[i] == 101)&& (raw[i + 1] == 110)&& (raw[i + 2] == 100)&& (raw[i + 3] == 111)))
				break;

			//if(debugFastCode)
			//	System.out.println("i= "+i+" "+raw[i]+" "+(char)raw[i]);//+""+(char)raw[i+1]+""+(char)raw[i+2]);

			/**
			 * process value
			 */
			if(raw[i]==60 && raw[i+1]==60){
				i++;
				level++;

				if(debugFastCode)
					System.out.println(paddingString+"Enter Level "+level);
			}else if(raw[i]==62 && i+1!=raw.length && raw[i+1]==62){
				i++;
				level--;

				if(debugFastCode)
					System.out.println(paddingString+"Exit Level "+level);

				if(level==0)
					break;
			}else if (raw[i] == 47 && (raw[i+1] == 47 || raw[i+1]==32)) { //allow for oddity of //DeviceGray  and / /DeviceGray in colorspace
				i++;
			}else  if (raw[i] == 47) { //everything from /

				i++; //skip /
				int keyLength=0,keyStart=i;

				while (true) { //get key up to space or [ or / or ( or < or carriage return

					if (raw[i] == 32 || raw[i] == 13 || raw[i] == 9 || raw[i] == 10 || raw[i] == 91 ||
							raw[i]==47 || raw[i]==40 || raw[i]==60 || raw[i]==62)
						break;

					i++;
					keyLength++;

					if(i==raw.length)
						return i;
				}

                int type=pdfObject.getObjectType();


                if(debugFastCode)
                	System.out.println(type+" "+PdfDictionary.OCProperties+" "+pdfObject.getID()+" chars="+(char)raw[i-1]+(char)raw[i]+" "+pdfObject);
                //ensure all go into 'pool'
				if(type==PdfDictionary.MCID && (pdfObject.getID()==PdfDictionary.RoleMap || (pdfObject.getID()==PdfDictionary.A && raw[i-2]=='/'))){
					pdfKeyType=PdfDictionary.VALUE_IS_NAME;
                    PDFkey=PdfDictionary.getKey(keyStart,keyLength,raw);
                    PDFkeyInt=PdfDictionary.MCID;
                    isMap=true;
                    
                    
                    
				}else{
                    isMap=false;
                    PDFkey=null;

                    /**
                     * get Dictionary key and type of value it takes
                     */
                    if(debugFastCode)//used in debug
                        PDFkey=PdfDictionary.getKey(keyStart,keyLength,raw);

                    PDFkeyInt=PdfDictionary.getIntKey(keyStart,keyLength,raw);

                    //work around for ColorSpace which is an Object UNLESS its in a Page Object
                    //when its a list of paired keys

                    //correct mapping
                    if(PDFkeyInt==PdfDictionary.Indexed && (type==PdfDictionary.MK ||type==PdfDictionary.Form))
                        PDFkeyInt=PdfDictionary.I;

                    if(isInlineImage){

                    	switch(PDFkeyInt){

                    	case PdfDictionary.D:
                            PDFkeyInt= PdfDictionary.Decode;
                    		break;

                    	case PdfDictionary.F:
                            PDFkeyInt= PdfDictionary.Filter;
                    		break;

                    	case PdfDictionary.G:
                            PDFkeyInt= ColorSpaces.DeviceGray;
                    		break;

                    	case PdfDictionary.H:
                            PDFkeyInt= PdfDictionary.Height;
                    		break;

                    	case PdfDictionary.RGB:
                            PDFkeyInt= ColorSpaces.DeviceRGB;
                    		break;

                    	case PdfDictionary.W:
                            PDFkeyInt= PdfDictionary.Width;
                    		break;

                    	default:

                    }
                    }

                    if(type==PdfDictionary.Resources && (PDFkeyInt==PdfDictionary.ColorSpace
                            || PDFkeyInt==PdfDictionary.ExtGState || PDFkeyInt==PdfDictionary.Shading
                            || PDFkeyInt==PdfDictionary.XObject || PDFkeyInt==PdfDictionary.Font
                            || PDFkeyInt==PdfDictionary.Pattern)){
                        pdfKeyType=PdfDictionary.VALUE_IS_DICTIONARY_PAIRS;
                    }else if (type==PdfDictionary.Outlines && PDFkeyInt== PdfDictionary.D){
                        PDFkeyInt= PdfDictionary.Dest;
                        pdfKeyType= PdfDictionary.VALUE_IS_MIXED_ARRAY;
                    }else if ((type==PdfDictionary.Form || type==PdfDictionary.MK) && PDFkeyInt== PdfDictionary.D){
                        if(pdfObject.getID()==PdfDictionary.AP || pdfObject.getID()==PdfDictionary.AA){
                            pdfKeyType= PdfDictionary.VALUE_IS_VARIOUS;
                        }else if(pdfObject.getID()==PdfDictionary.Win){
                            pdfKeyType= PdfDictionary.VALUE_IS_TEXTSTREAM;
                        }else{
                            PDFkeyInt= PdfDictionary.Dest;
                            pdfKeyType= PdfDictionary.VALUE_IS_MIXED_ARRAY;
                        }
                    }else if ((type==PdfDictionary.Form || type==PdfDictionary.MK) && (pdfObject.getID()==PdfDictionary.AP || pdfObject.getID()==PdfDictionary.AA) && PDFkeyInt== PdfDictionary.A){
                        pdfKeyType= PdfDictionary.VALUE_IS_VARIOUS;
                    }else if (PDFkeyInt== PdfDictionary.Order && type==PdfDictionary.OCProperties){
                        pdfKeyType= PdfDictionary. VALUE_IS_OBJECT_ARRAY;
                    }else if (PDFkeyInt== PdfDictionary.Name && type==PdfDictionary.OCProperties){
                        pdfKeyType= PdfDictionary.VALUE_IS_TEXTSTREAM;
                    }else if ((type==PdfDictionary.ColorSpace || type==PdfDictionary.Function) && PDFkeyInt== PdfDictionary.N){
                        pdfKeyType= PdfDictionary.VALUE_IS_FLOAT;
                    }else if(PDFkeyInt==PdfDictionary.Gamma && type==PdfDictionary.ColorSpace &&
                            pdfObject.getParameterConstant(PdfDictionary.ColorSpace)==ColorSpaces.CalGray){ //its a number not an array
                        pdfKeyType= PdfDictionary.VALUE_IS_FLOAT;
                    }else if(pdfObject.getID()==PdfDictionary.Win && pdfObject.getObjectType()==PdfDictionary.Form && 
                    		(PDFkeyInt==PdfDictionary.P || PDFkeyInt==PdfDictionary.O)){
                        pdfKeyType= PdfDictionary.VALUE_IS_TEXTSTREAM;
                    }else if (isInlineImage && PDFkeyInt==PdfDictionary.ColorSpace){
                    	pdfKeyType= PdfDictionary.VALUE_IS_DICTIONARY;
                    }else
                        pdfKeyType=PdfDictionary.getKeyType(PDFkeyInt,type);

                    boolean isPair=false;
                    
                    //allow for other values in D,N,R definitions
                    
                    /** does not work on some files
                    if((pdfKeyType==-1 && pdfObject.getID()==PdfDictionary.ClassMap) || 
                           //((pdfKeyType==-1 || PDFkeyInt==PdfDictionary.T || PDFkeyInt==PdfDictionary.I || PDFkeyInt==PdfDictionary.F) &&
                                   pdfObject.getParentID()==PdfDictionary.AP &&
                        		pdfObject.getObjectType()==PdfDictionary.Form 
                        		&& (pdfObject.getID()==PdfDictionary.D ||  pdfObject.getID()==PdfDictionary.N || pdfObject.getID()==PdfDictionary.R)//)
                        ){
/**/ //current version
                    	if((pdfKeyType==-1 && pdfObject.getID()==PdfDictionary.ClassMap) || 
                               ((pdfKeyType==-1 || (keyLength==1 && PDFkeyInt!=PdfDictionary.N && PDFkeyInt!=PdfDictionary.D && PDFkeyInt!=PdfDictionary.R)) &&
                                        pdfObject.getParentID()==PdfDictionary.AP &&
                             		pdfObject.getObjectType()==PdfDictionary.Form 
                             		&& (pdfObject.getID()==PdfDictionary.D ||  pdfObject.getID()==PdfDictionary.N || pdfObject.getID()==PdfDictionary.R))
                             ){
/**/
                    	int jj=i,aa=i, count=raw.length;

                    	boolean debugPair=false;
                    	
                    	if(debugPair){
	                    	while(aa<count){
	                    		System.out.print((char)raw[aa]);
	                    		aa++;
	                    	}
	                    	System.out.println("");
                    	}
                    	
                    	while(jj<count){
                    		
                    		//ignore any spaces
                    		while(jj<count && (raw[jj]==32 || raw[jj]==10 || raw[jj]==13 || raw[jj]==10))
                    			jj++;
                    		
                    		if(jj==count)
                    			break;
                    		
                    		//number (possibly reference)
                    		if(raw[jj]>='0' && raw[jj]<='9'){
                    			
                    			//rest of ref
                    			while(jj<count && raw[jj]>='0' && raw[jj]<='9'){
                    				if(debugPair)
                    				System.out.println("poss ref="+(char)raw[jj]);
                    				jj++;
                    			}
                    			
                    			//ignore any spaces
                        		while(jj<count && (raw[jj]==32 || raw[jj]==10 || raw[jj]==13 || raw[jj]==10))
                        			jj++;
                        		
                        		//generation
                    			while(jj<count && raw[jj]>='0' && raw[jj]<='9'){
                    				if(debugPair)
                    				System.out.println("poss gen="+(char)raw[jj]);
                    				jj++;
                    			}
                    			
                    			//ignore any spaces
                        		while(jj<count && (raw[jj]==32 || raw[jj]==10 || raw[jj]==13 || raw[jj]==10))
                        			jj++;
                        		
                        		
                        		//not a ref
                        		if(jj>=count || raw[jj]!='R'){
                        			if(debugPair)	
                        			System.out.println("not a ref "+(char) raw[jj]);
                        			break;
                        		}
                        		
                        		//roll past R
                        		jj++;
                    		}
                    		
                    			
//                    		}else if(1==2 &&raw[i]==60 && raw[i+1]==60){ //direct (ie << >>)
//                				i++;
//                				level++;
//
//                				if(debugFastCode)
//                					System.out.println(paddingString+"Enter Level "+level);
//                			}else if(raw[i]==62 && i+1!=raw.length && raw[i+1]==62){
//                				i++;
//                				level--;
//
//                				if(debugFastCode)
//                					System.out.println(paddingString+"Exit Level "+level);
//
//                				if(level==0)
//                					break;
//                    		
//                    		
//                    		
                    		//ignore any spaces
                    		while(jj<count && (raw[jj]==32 || raw[jj]==10 || raw[jj]==13 || raw[jj]==10))
                    			jj++;
                    		
                    		if(debugPair)
                    		System.out.print((char)raw[jj]);
                    		
                    		//must be next key or end
                    		if(raw[jj]=='>' && raw[jj+1]=='>'){
                    			isPair=true;
                    			break;
                    		}
                    			if(raw[jj]!='/')
                    			break;
                    		
                    		jj++;
                    		
                    		//roll past key
                    		//ignore any spaces
                    		while(jj<count && (raw[jj]!=32 && raw[jj]!=10 && raw[jj]!=13 && raw[jj]!=10))
                    			jj++;
                    		
                    	}
                    	
                    	
//                    	if(PDFkeyInt==PdfDictionary.Subtype || PDFkeyInt==PdfDictionary.Length || 
//                    			PDFkeyInt==PdfDictionary.Matrix || PDFkeyInt==PdfDictionary.ProcSet){ // /N has 2 means so ignore spurious
//                            pdfObject.setParentID(PDFkeyInt);
//                        }else{

                    	
                        	
                            //if(debugFastCode)
                            //System.out.println("XX");
                            //System.out.println(pdfObject.getID()+" pdfKeyType= "+PDFkeyInt+" "+PdfDictionary.showAsConstant(PDFkeyInt)+" "+PdfDictionary.showAsConstant(pdfObject.getID())+" "+pdfObject.getObjectRefAsString());

                    	//add I so that Down appearance streams named I will be added to the images, and not treated differently.  added= || PDFkeyInt==PdfDictionary.I
                        
                        //}
                    }
                    
                    if(isPair){
                    	
                    	if(debugFastCode) {
                    		System.out.println("IS PAIR key="+PdfDictionary.getKey(keyStart,keyLength,raw)+" "+pdfObject);
                        }
                    	
                    	 pdfKeyType=PdfDictionary.VALUE_IS_UNREAD_DICTIONARY;
                        pdfObject.setCurrentKey(PdfDictionary.getKey(keyStart,keyLength,raw));
                    }
                    
                    if(pdfKeyType==-1 && debugFastCode && pdfObject.getObjectType()!=PdfDictionary.Page){
                        System.out.println(pdfObject.getID()+" "+type);
                        System.out.println(paddingString+PDFkey+" NO type setting for "+PdfDictionary.getKey(keyStart,keyLength,raw)+" id="+pdfObject.getID());
                    }
                }
                

				if(raw[i]==47 || raw[i]==40 || raw[i] == 91) //move back cursor
					i--;

				//check for unknown Dictionary
				if(pdfKeyType==-1){
					int count=raw.length-1;
					for(int jj=i;jj<count;jj++){

						if(raw[jj]=='<' && raw[jj+1]=='<'){

							int levels=0;
							while(true){

								if(raw[jj]=='<' && raw[jj+1]=='<')
									levels++;
								else if(raw[jj]=='>' && raw[jj+1]=='>')
									levels--;

								jj++;
								if(levels==0 || jj>=count)
									break;
							}

							i=jj;

							jj=count;

						}else if(raw[jj]=='/')
							jj=count;
					}
				}

				/**
				 * now read value
				 */
				if(PDFkeyInt==-1 || pdfKeyType==-1){
					if(debugFastCode)
						System.out.println(paddingString+objectRef+" =================Not implemented="+PDFkey+" pdfKeyType="+pdfKeyType);

				}else{

					//if we only need top level do not read whole tree
					boolean ignoreRecursion=pdfObject.ignoreRecursion();

					if(debugFastCode)
						System.out.println(paddingString+objectRef+" =================Reading value for key="+PDFkey+" ("+PDFkeyInt+") type="+pdfKeyType+" ignorRecursion="+ignoreRecursion+" "+pdfObject);

					switch(pdfKeyType){

					//read text stream (this is text)
					//and also special case of [] in W in CID Fonts
					case PdfDictionary.VALUE_IS_TEXTSTREAM:{

                        if(raw[i+1]==40 && raw[i+2]==41){ //allow for empty stream
                            i=i+3;
                            pdfObject.setTextStreamValue(PDFkeyInt, new byte[1]);

                            if(raw[i]=='/')
                                i--;
                        }else
						i = readTextStream(pdfObject, objectRef, i, raw,
								paddingString, debugFastCode, PDFkeyInt,
								ignoreRecursion);


						break;

					}case PdfDictionary.VALUE_IS_NAMETREE:{

						boolean isRef=false;

						//move to start
						while(raw[i]!='[' ){ //can be number as well

							//System.out.println((char) raw[i]);
							if(raw[i]=='('){ //allow for W (7)
								isRef=false;
								break;
							}

							//allow for number as in refer 9 0 R
							if(raw[i]>='0' && raw[i]<='9'){
								isRef=true;
								break;
							}

							i++;
						}

						//allow for direct or indirect
						byte[] data=raw;

						int start=i,j=i;

						int count=0;

						//read ref data and slot in
						if(isRef){
							//number
							int keyStart2=i,keyLength2=0;
							while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){

								i++;
								keyLength2++;

							}
							int number=parseInt(keyStart2,i, raw);

							//generation
							while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
								i++;

							keyStart2=i;
							//move cursor to end of reference
							while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
								i++;
							int generation=parseInt(keyStart2,i, raw);

							//move cursor to start of R
							while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
								i++;

							if(raw[i]!=82) //we are expecting R to end ref
								throw new RuntimeException("3. Unexpected value in file "+raw[i]+" - please send to IDRsolutions for analysis");

							if(!ignoreRecursion){

								//read the Dictionary data
								data=readObjectAsByteArray(pdfObject, objectRef, isCompressed(number,generation),number,generation);


								//lose obj at start
								j=3;
								while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111)
									j++;

								//skip any spaces after
								while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
									j++;

								//reset pointer
								start=j;

							}
						}

						//move to end
						while(j<data.length){

							if(data[j]=='[' || data[j]=='(')
								count++;
							else if(data[j]==']' || data[j]==')')
								count--;

							if(count==0)
								break;

							j++;
						}

						if(!ignoreRecursion){
							int stringLength=j-start+1;
							byte[] newString=new byte[stringLength];

							System.arraycopy(data, start, newString, 0, stringLength);

							/**
							 * clean up so matches old string so old code works
							 */
							 //	                                for(int aa=0;aa<stringLength;aa++){
							//	                                    if(newString[aa]==10 || newString[aa]==13)
							//	                                        newString[aa]=32;
							//	                                }

							if(pdfObject.getObjectType()!=PdfDictionary.Encrypt){
								try {
									newString=decrypt(newString,objectRef, false,null, false,false);
								} catch (PdfSecurityException e) {
									e.printStackTrace();
								}
							}


							pdfObject.setTextStreamValue(PDFkeyInt, newString);

							if(debugFastCode)
								System.out.println(paddingString+"name="+new String(newString)+" set in "+pdfObject);
						}

						//roll on
						if(!isRef)
							i=j;

						break;

						//readDictionary keys << /A 12 0 R /B 13 0 R >>
					}case PdfDictionary.VALUE_IS_DICTIONARY_PAIRS:{

						if(debugFastCode)
							System.out.println(paddingString + ">>>Reading Dictionary Pairs i=" + i + " " + (char) raw[i] + (char) raw[i + 1] + (char) raw[i + 2] + (char) raw[i + 3] + (char) raw[i + 4] + (char) raw[i + 5]+(char)raw[i+6]);

						//move cursor to start of text
						while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47)
							i++;

						//set data which will be switched below if ref
						byte[] data=raw;
						int j=i;

						//                        System.out.println("====>");
						//                        for(int aa=i;aa<raw.length;aa++)
						//                        	System.out.print((char)raw[aa]);
						//                        System.out.println("<====");

						//get next key to see if indirect
						boolean isRef=data[j]!='<';

						if(isRef){

							//number
							int keyStart2=i,keyLength2=0;
							while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){

								i++;
								keyLength2++;
							}

							int number=parseInt(keyStart2,i, raw);

							//generation
							while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
								i++;

							keyStart2=i;
							//move cursor to end of reference
							while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
								i++;
							int generation=parseInt(keyStart2,i, raw);

							//move cursor to start of R
							while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
								i++;

							if(raw[i]!=82) //we are expecting R to end ref
								throw new RuntimeException("3. Unexpected value in file "+raw[i]+" - please send to IDRsolutions for analysis");

							if(!ignoreRecursion){

								//read the Dictionary data
								data=readObjectAsByteArray(pdfObject, objectRef, isCompressed(number,generation),number,generation);

								//							System.out.println("data read is>>>>>>>>>>>>>>>>>>>\n");
								//							for(int ab=0;ab<data.length;ab++)
								//							System.out.print((char)data[ab]);
								//							System.out.println("\n<<<<<<<<<<<<<<<<<<<\n");

								//lose obj at start
								j=3;

								while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111){
									j++;
                                    if(j==data.length){ //some missing obj so catch these
                                        j=0;
                                        break;
                                    }
                                }
                                
								//skip any spaces after
								while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
									j++;

							}
						}

						PdfObject valueObj=ObjectFactory.createObject(PDFkeyInt,objectRef, pdfObject.getObjectType(), pdfObject.getID());
						valueObj.setID(PDFkeyInt);
						/**
						 * read pairs (stream in data starting at j)
						 */
						//
						//                    System.out.println("-----------------------------\n");
						//                        for(int aa=j;aa<data.length;aa++)
						//                        System.out.print((char)data[aa]);
						//                    System.out.println("\n-----------------------------valueObj="+valueObj+" PDFkeyInt="+PDFkeyInt);

						if(ignoreRecursion) //just skip to end
							j=readKeyPairs(PDFkeyInt,data, j,-2, null,paddingString);
						else{
							//count values first
							int count=readKeyPairs(PDFkeyInt,data, j,-1, null,paddingString);

							//now set values
							j=readKeyPairs(PDFkeyInt,data, j,count,valueObj,paddingString);

							//store value
							pdfObject.setDictionary(PDFkeyInt,valueObj);

							if(debugFastCode)
								System.out.println(paddingString+"Set Dictionary "+count+" pairs type "+PDFkey+"  in "+pdfObject+" to "+valueObj);
						}

						//update pointer if direct so at end (if ref already in right place)
						if(!isRef){
							i=j;

							if(debugFastCode)
								System.out.println(i+">>>>"+data[i-2]+" "+data[i-1]+" >"+data[i]+"< "+data[i+1]+" "+data[i+2]);
							//break at end
							//if(raw[i]=='>' && raw[i+1]=='>' && raw[i-1]=='>' && raw[i-2]=='>')
							//return i;

						}

						break;

						//Strings
					}case PdfDictionary.VALUE_IS_STRING_ARRAY:{

						//if(debugFastCode)
						//    System.out.println(paddingString+"Reading String Array in "+pdfObject+" i="+i+" char="+(char)raw[i]);

						i=readArray(ignoreRecursion, i, endPoint, PdfDictionary.VALUE_IS_STRING_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode,paddingString, null, -1);

						break;

						//read Object Refs in [] (may be indirect ref)
					}case PdfDictionary.VALUE_IS_BOOLEAN_ARRAY:{


						//if(debugFastCode)
						//    System.out.println(paddingString+"Reading Key Array in "+pdfObject);

						i=readArray(false, i, endPoint, PdfDictionary.VALUE_IS_BOOLEAN_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode,paddingString, null, -1);

						break;

						//read Object Refs in [] (may be indirect ref)
					}case PdfDictionary.VALUE_IS_KEY_ARRAY:{

						//if(debugFastCode)
						//    System.out.println(paddingString+"Reading Key Array in "+pdfObject);


						i=readArray(ignoreRecursion, i, endPoint, PdfDictionary.VALUE_IS_KEY_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode,paddingString, null, -1);

						break;

						//read numbers in [] (may be indirect ref)
					}case PdfDictionary.VALUE_IS_MIXED_ARRAY:{

						//if(debugFastCode)
						//    System.out.println(paddingString+"Reading Mixed Array in "+pdfObject);

						i=readArray(ignoreRecursion, i, endPoint, PdfDictionary.VALUE_IS_MIXED_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode,paddingString, null, -1);

						break;

						//read numbers in [] (may be indirect ref)
						//same as Mixed but allow for recursion and store as objects
					}case PdfDictionary.VALUE_IS_OBJECT_ARRAY:{

						//if(debugFastCode)
						//	System.out.println(paddingString+"Reading Object Array in "+pdfObject+" PDFkeyInt="+PDFkeyInt+" "+pdfObject.getObjectType());

                        i=readArray(false, i, endPoint, PdfDictionary.VALUE_IS_OBJECT_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode,paddingString, null, -1);

						break;

						//read numbers in [] (may be indirect ref)
					}case PdfDictionary.VALUE_IS_DOUBLE_ARRAY:{

						//if(debugFastCode)
						//	System.out.println(paddingString+"Reading Double Array in "+pdfObject);

						i=readArray(false, i, endPoint, PdfDictionary.VALUE_IS_DOUBLE_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode,paddingString, null, -1);

						break;

						//read numbers in [] (may be indirect ref)
					}case PdfDictionary.VALUE_IS_INT_ARRAY:{

						//if(debugFastCode)
						//	System.out.println("Reading Float Array");

						i=readArray(false, i,endPoint, PdfDictionary.VALUE_IS_INT_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode, paddingString, null, -1);

						break;

						//read numbers in [] (may be indirect ref)
					}case PdfDictionary.VALUE_IS_FLOAT_ARRAY:{

						//if(debugFastCode)
						//	System.out.println("Reading Float Array");

						i=readArray(false, i, endPoint, PdfDictionary.VALUE_IS_FLOAT_ARRAY, raw, objectRef, pdfObject,
								PDFkeyInt, debugFastCode, paddingString, null, -1);

						break;

						//read String (may be indirect ref)
					}case PdfDictionary.VALUE_IS_NAME:{

						//if(debugFastCode)
						//	System.out.println("Reading String");

						i = readNameString(pdfObject, objectRef, i, raw,debugFastCode, PDFkeyInt,paddingString,isMap,PDFkey);

						break;

						//read true or false
					}case PdfDictionary.VALUE_IS_BOOLEAN:{

						i++;
						//if(debugFastCode)
						//	System.out.println("Reading constant "+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]);

						//move cursor to start of text
						while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47){
							//System.out.println("skip="+raw[i]);
							i++;
						}

						keyStart=i;
						keyLength=0;

						//System.out.println("firstChar="+raw[i]+" "+(char)raw[i]);

						//move cursor to end of text
						while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
							//System.out.println("key="+raw[i]+" "+(char)raw[i]);
							i++;
							keyLength++;
						}

						i--;// move back so loop works

						//store value
						if(raw[keyStart]=='t' && raw[keyStart+1]=='r' && raw[keyStart+2]=='u' && raw[keyStart+3]=='e') {
							pdfObject.setBoolean(PDFkeyInt,true);

							if(debugFastCode)
								System.out.println(paddingString+"Set Boolean true "+PDFkey+" in "+pdfObject);

						}else if(raw[keyStart]=='f' && raw[keyStart+1]=='a' && raw[keyStart+2]=='l' && raw[keyStart+3]=='s' && raw[keyStart+4]=='e'){
							pdfObject.setBoolean(PDFkeyInt,false);

							if(debugFastCode)
								System.out.println(paddingString+"Set Boolean false "+PDFkey+" in "+pdfObject);

						}else
							throw new RuntimeException("Unexpected value for Boolean value for"+PDFkeyInt+"="+PDFkey);


						break;
						//read known set of values
					}case PdfDictionary.VALUE_IS_STRING_CONSTANT:{

						i++;
						//if(debugFastCode)
						//	System.out.println("Reading constant "+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]);

						//move cursor to start of text
						while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47){
							//System.out.println("skip="+raw[i]);
							i++;
						}

						keyStart=i;
						keyLength=0;

						//System.out.println("firstChar="+raw[i]+" "+(char)raw[i]);

						//move cursor to end of text
						while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
							//System.out.println("key="+raw[i]+" "+(char)raw[i]);
							i++;
							keyLength++;
						}

						i--;// move back so loop works

						//store value
						int constant=pdfObject.setConstant(PDFkeyInt,keyStart,keyLength,raw);

						if(debugFastCode)
							System.out.println(paddingString+"Set constant "+PDFkey+" in "+pdfObject+" to "+constant);

						break;

						//read known set of values
					}case PdfDictionary.VALUE_IS_STRING_KEY:{

						i++;
						//if(debugFastCode)
						//	System.out.println("Reading constant "+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]);

						//move cursor to start of text
						while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47){
							//System.out.println("skip="+raw[i]);
							i++;
						}

						keyStart=i;
						keyLength=1;

						//System.out.println("firstChar="+raw[i]+" "+(char)raw[i]);

						//move cursor to end of text
						while(raw[i]!='R'){
							//System.out.println("key="+raw[i]+" "+(char)raw[i]);
							i++;
							keyLength++;
						}

						i--;// move back so loop works

						//set value
						byte[] stringBytes=new byte[keyLength];
						System.arraycopy(raw,keyStart,stringBytes,0,keyLength);

						//store value
						pdfObject.setStringKey(PDFkeyInt,stringBytes);

						if(debugFastCode)
							System.out.println(paddingString+"Set constant "+PDFkey+" in "+pdfObject+" to "+new String(stringBytes));

						break;

						//read number (may be indirect ref)
					}case PdfDictionary.VALUE_IS_INT:{

						//if(debugFastCode)
						//System.out.println("Reading Int number");

						//roll on
						i++;

						//move cursor to start of text
						while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47)
							i++;

						i = readNumber(pdfObject, objectRef, i, raw,
								paddingString, debugFastCode, PDFkeyInt, PDFkey);

						break;

						//read float number (may be indirect ref)
					}case PdfDictionary.VALUE_IS_FLOAT:{

						if(debugFastCode)
							System.out.println("Reading Float number");

						//roll on
						i++;

						//move cursor to start of text
						while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47)
							i++;

						keyStart=i;
						keyLength=0;

						//move cursor to end of text
						while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
							i++;
							keyLength++;
						}

						//actual value or first part of ref
						float number=parseFloat(keyStart,i, raw);

						//roll onto next nonspace char and see if number
						int jj=i;
						while(jj<raw.length &&(raw[jj]==32 || raw[jj]==13 || raw[jj]==10))
							jj++;

						//check its not a ref (assumes it XX 0 R)
						if(raw[jj]>= 48 && raw[jj]<=57){ //if next char is number 0-9 its a ref

							//move cursor to start of generation
							while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
								i++;

							/**
							 * get generation number
							 */
							keyStart=i;
							//move cursor to end of reference
							while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
								i++;

							int generation=parseInt(keyStart,i, raw);

							//move cursor to start of R
							while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
								i++;

							if(raw[i]!=82){ //we are expecting R to end ref
								throw new RuntimeException("3. Unexpected value in file - please send to IDRsolutions for analysis");
							}

							//read the Dictionary data
							byte[] data=readObjectAsByteArray(pdfObject, objectRef, isCompressed((int)number,generation),(int)number,generation);

							//lose obj at start
							int j=3;
							while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111)
								j++;

							//skip any spaces after
							while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
								j++;

							int count=j;

							//skip any spaces at end
							while(data[count]!=10 && data[count]!=13 && data[count]!=32){// || data[j]==47 || data[j]==60)
								count++;
							}

							number=parseFloat(j,count, data);

						}

						//store value
						pdfObject.setFloatNumber(PDFkeyInt,number);

						if(debugFastCode){
							System.out.println(paddingString+"set key="+PDFkey+" numberValue="+number);//+" in "+pdfObject);

							//System.out.println("i="+i+" "+(char)raw[i]+""+(char)raw[i+1]+""+(char)raw[i+2]);
						}
						//if(raw[i+1]==47)
						i--;// move back so loop works
						//if(raw[i]==47)
						//	i--;
						//	i=i-2;
						//else
						//	i--;

						//i=i+keyLength-1;

						break;

						//read known Dictionary object which may be direct or indirect

					}case PdfDictionary.VALUE_IS_UNREAD_DICTIONARY:{

                            i = getUnreadDictionary(pdfObject, objectRef, i, raw, paddingString, isInlineImage, debugFastCode, PDFkeyInt, PDFkey);

						break;

					}case PdfDictionary.VALUE_IS_VARIOUS:{

						if(raw[i]!='<')
						i++;

						if(debugFastCode)
							System.out.println(paddingString+"Various value (first char="+(char)raw[i]+(char)raw[i+1]+" )");

						if(raw[i]=='/'){
								i = readNameString(pdfObject, objectRef, i, raw, debugFastCode, PDFkeyInt, paddingString,isMap, PDFkey);
						}else if(raw[i]=='f' && raw[i+1]=='a' && raw[i+2]=='l' && raw[i+3]=='s' && raw[i+4]=='e'){
							pdfObject.setBoolean(PDFkeyInt,false);
							i=i+4;
						}else if(raw[i]=='t' && raw[i+1]=='r' && raw[i+2]=='u' && raw[i+3]=='e') {
								pdfObject.setBoolean(PDFkeyInt,true);
								i=i+3;	
						}else if(raw[i]=='(' || (raw[i]=='<' && raw[i-1]!='<' && raw[i+1]!='<')){
							i = readTextStream(pdfObject, objectRef, i, raw, paddingString, debugFastCode, PDFkeyInt, ignoreRecursion);
						}else if(raw[i]=='['){

                            if(PDFkeyInt== PdfDictionary.XFA)
                                i=readArray(ignoreRecursion, i, endPoint, PdfDictionary.VALUE_IS_MIXED_ARRAY, raw, objectRef, pdfObject, PDFkeyInt, debugFastCode,paddingString, null, -1);
                            else if(PDFkeyInt== PdfDictionary.K)
                                i=readArray(ignoreRecursion, i, endPoint, PdfDictionary.VALUE_IS_STRING_ARRAY, raw, objectRef, pdfObject, PDFkeyInt, debugFastCode,paddingString, null, -1);

                            else if(PDFkeyInt== PdfDictionary.C)
                                i=readArray(ignoreRecursion, i, endPoint, PdfDictionary.VALUE_IS_FLOAT_ARRAY, raw, objectRef, pdfObject, PDFkeyInt, debugFastCode,paddingString, null, -1);
                            else
                                i=readArray(ignoreRecursion, i, endPoint, PdfDictionary.VALUE_IS_STRING_ARRAY, raw, objectRef, pdfObject, PDFkeyInt, debugFastCode,paddingString, null, -1);
						/**}else if(1==2 && !FormObject.newCode){

							i--;

							i = readDictionary(pdfObject, objectRef, i, raw,
									paddingString, isInlineImage, debugFastCode,
									PDFkeyInt, PDFkey, ignoreRecursion);
						/**/
						}else{


							if(debugFastCode)
								System.out.println(paddingString+"general case");

							//see if number or ref
							int jj=i;
							int j=i+1;
							byte[] data=raw;
							int typeFound=0;
							boolean isNumber=true, isRef=false;

							while(true){

								//if(debugFastCode)
								//  System.out.println(i+" = "+(char)raw[i]+""+(char)raw[i+1]+""+(char)raw[i+2]);

								if(data[j]=='R'){

									isRef=true;
									int end=j;
									j=i;
									i=end;

									int ref, generation;

									//allow for [ref] at top level (may be followed by gap
											while (data[j] == 91 || data[j] == 32 || data[j] == 13 || data[j] == 10)
												j++;

											// get object ref
											keyStart = j;
											int refStart=j;
											//move cursor to end of reference
											while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62)
												j++;

											ref = parseInt(keyStart, j, data);

											//move cursor to start of generation or next value
											while (data[j] == 10 || data[j] == 13 || data[j] == 32)// || data[j]==47 || data[j]==60)
												j++;

											/**
											 * get generation number
											 */
											keyStart = j;
											//move cursor to end of reference
											while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62)
												j++;

											generation = parseInt(keyStart, j, data);

											/**
											 * check R at end of reference and abort if wrong
											 */
											//move cursor to start of R
											while (data[j] == 10 || data[j] == 13 || data[j] == 32 || data[j] == 47 || data[j] == 60)
												j++;

											if (data[j] != 82) { //we are expecting R to end ref
												throw new RuntimeException("ref=" + ref + " gen=" + ref + " 1. Unexpected value " + data[j] + " in file - please send to IDRsolutions for analysis char=" + (char) data[j]);
											}

											objectRef=new String(data,refStart,1+j-refStart);
											
											//read the Dictionary data
											data = readObjectAsByteArray(pdfObject, objectRef, isCompressed(ref, generation), ref, generation);

											jj=3;
											while(true){
												if(data[jj-2]=='o' && data[jj-1]=='b' && data[jj]=='j')
													break;

												jj++;

												if(jj==data.length){
													jj=0;
													break;
												}
											}
											jj++;

											while (data[jj] == 10 || data[jj] == 13 || data[jj] == 32)// || data[j]==47 || data[j]==60)
												jj++;

											j=jj;

											if(debugFastCode)
												System.out.println(">>"+new String(data)+"<<next="+(char)data[j]);

								}else if(data[j]=='[' || data[j]=='('){
									//typeFound=0;
									break;    
								}else if(data[j]=='<'){
									typeFound=0;
									break;

								}else if(data[j]=='>' || data[j]=='/'){
									typeFound=1;
									break;
								}else if(data[j]==32 || data[j]==10 || data[j]==13){
								}else if((data[j]>='0' && data[j] <='9')|| data[j]=='.'){ //assume and disprove
								}else{
									isNumber=false;
								}
								j++;
								if(j==data.length)
									break;
							}

							//if(data[i]!='/')
							//i--;
							
							//check if name by counting /
							int count=0;
							for(int aa=jj+1;aa<data.length;aa++){
								if(data[aa]=='/')
									count++;
							}


							if(count==0 && data[jj]=='/'){
								jj = readNameString(pdfObject, objectRef, jj, data, debugFastCode, PDFkeyInt, paddingString,isMap, PDFkey);
							}else if(data[jj]=='('){
								jj = readTextStream(pdfObject, objectRef, jj, data, paddingString, debugFastCode, PDFkeyInt, ignoreRecursion);
							}else if(data[jj]=='['){
								jj=readArray(ignoreRecursion, jj, endPoint, PdfDictionary.VALUE_IS_STRING_ARRAY, data, objectRef, pdfObject, PDFkeyInt, debugFastCode,paddingString, null, -1);                            	
								/**/
							}else if(typeFound==0){
								if(debugFastCode)
									System.out.println("Dictionary " + (char) +data[jj]+(char)data[jj+1]);

								try{
									jj = readDictionaryFromRefOrDirect(-1,pdfObject,objectRef,jj , data,debugFastCode, PDFkeyInt,PDFkey, paddingString);

									//jj = readDictionary(pdfObject, objectRef, jj, data, paddingString, isInlineImage, debugFastCode, PDFkeyInt, PDFkey, ignoreRecursion);
								}catch(Exception ee){
									ee.printStackTrace();
									System.err.println(new String(data));

								}
							
							}else if(isNumber){

								if(debugFastCode)
									System.out.println("Number");

								jj=readNumber(pdfObject, objectRef,jj, data, paddingString, debugFastCode, PDFkeyInt, PDFkey);

							}else if(typeFound==1){

								if(debugFastCode)
									System.out.println("Name");

								jj = readNameString(pdfObject, objectRef, jj, data, debugFastCode, PDFkeyInt, paddingString,isMap,PDFkey);

							}else if(debugFastCode)
								System.out.println(paddingString+"Not read");

							if(!isRef)
								i=jj;
							//if(raw[i]=='/')
							//i--;
						}

						break;

					}case PdfDictionary.VALUE_IS_DICTIONARY:{

						if(debugFastCode)
							System.out.println(paddingString+"Dictionary value (first int="+raw[i]+" char="+((char)+raw[i])+" ) i="+i);

						i = readDictionary(pdfObject, objectRef, i, raw,
								paddingString, isInlineImage, debugFastCode,
								PDFkeyInt, PDFkey, ignoreRecursion);

                        break;
					}
					}
				}
			}

			i++;

		}

		//System.out.println(paddingString+"i="+i+" Now at="+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]);

		/**
		 * look for stream afterwards
		 */

		
		int count=raw.length;
		for(int xx=i;xx<count-5;xx++){

			//System.out.println(paddingString+raw[xx]+" "+(char)raw[xx]);

			//avoid reading on subobject ie <<  /DecodeParams << >> >>
			if(raw[xx]=='>' && raw[xx+1]=='>')
				break;

			if(raw[xx] == 's' && raw[xx + 1] == 't' && raw[xx + 2] == 'r' &&
					raw[xx + 3] == 'e' && raw[xx + 4] == 'a' &&
					raw[xx + 5] == 'm'){

				if(debugFastCode)
					System.out.println(paddingString+"1. Stream found afterwards");
					
                if(!pdfObject.isCached())
				readStreamIntoObject(pdfObject, debugFastCode,xx, raw, pdfObject,paddingString);

                xx=count;
			}
		}

		if(debugFastCode && i<raw.length)
			System.out.println("return i="+i+" "+(char)raw[i]);

        return i;

	}

    private static int getUnreadDictionary(PdfObject pdfObject, String objectRef, int i, byte[] raw, String paddingString, boolean isInlineImage, boolean debugFastCode, int PDFkeyInt, Object PDFkey) {
        int keyStart;
        int keyLength;
        byte[] unresolvedData=null;

        if(debugFastCode)
		    System.out.println(paddingString+"Unread Dictionary value (first char="+(char)raw[i]+" "+raw[i]+" )");

        //roll on
        if(raw[i]!='<')
        	i++;

        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==9)
            i++;


        int start=i;

        //create and store stub
        PdfObject valueObj= ObjectFactory.createObject(PDFkeyInt,objectRef, pdfObject.getObjectType(), pdfObject.getID());
        valueObj.setID(PDFkeyInt);

        if(raw[i]=='n' && raw[i+1]=='u' && raw[i+2]=='l' && raw[i+3]=='l'){ //allow for null
        }else
            pdfObject.setDictionary(PDFkeyInt,valueObj);

        //assume not object and reset below if wrong
        int status=PdfObject.UNDECODED_DIRECT;

        //some objects can have a common value (ie /ToUnicode /Identity-H
        if(raw[i]==47){ //not worth caching

            //	System.out.println("Starts with /");

            //if it is a < (60) its a direct object, otherwise its a reference so we need to move and move back at end

            //}else if(raw[i]==60 && 1==2){

            //move cursor to start of text
            while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
                i++;

            keyStart=i;
            keyLength=0;

            //move cursor to end of text
            while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
                i++;
                keyLength++;
            }

            i--;// move back so loop works

            //store value
            int constant=valueObj.setConstant(PDFkeyInt,keyStart,keyLength,raw);

            if(constant== PdfDictionary.Unknown || isInlineImage){

                int StrLength=keyLength;
                byte[] newStr=new byte[StrLength];
                System.arraycopy(raw, keyStart, newStr, 0, StrLength);

                String s=new String(newStr);
                valueObj.setGeneralStringValue(s);

                if(debugFastCode)
                    System.out.println(paddingString+"Set Dictionary type "+PDFkey+" as String="+s+"  in "+pdfObject+" to "+valueObj);

            }else if(debugFastCode)
                System.out.println(paddingString+"Set Dictionary type "+PDFkey+" as constant="+constant+"  in "+pdfObject+" to "+valueObj);


            status=PdfObject.DECODED;

        }else //allow for empty object
            if(raw[i]=='e' && raw[i+1]=='n' && raw[i+2]=='d' && raw[i+3]=='o' && raw[i+4]=='b' ){
                //        return i;

                if(debugFastCode)
                    System.out.println(paddingString+"Empty object"+new String(raw)+"<<");


            }else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop

                if(debugFastCode)
                    System.out.println(paddingString+"3.About to read ref orDirect i="+i+" "+pdfObject+" "+PDFkeyInt);

                //roll onto first valid char
                while((raw[i]==91 && PDFkeyInt!=PdfDictionary.ColorSpace)  || raw[i]==32 || raw[i]==13 || raw[i]==10){

                    //if(raw[i]==91) //track incase /Mask [19 19]
                    //	possibleArrayStart=i;

                    i++;
                }


                //roll on and ignore
                if(raw[i]=='<' && raw[i+1]=='<'){

                    i=i+2;
                    int reflevel=1;

                    while(reflevel>0){
                        if(raw[i]=='<' && raw[i+1]=='<'){
                            i=i+2;
                            reflevel++;
                        }else if(raw[i]=='>' && raw[i+1]=='>'){
                            i=i+2;
                            reflevel--;
                        }else
                            i++;
                    }
                    // i--;

                }else if(raw[i]=='['){

                    i++;
                    int reflevel=1;

                    while(reflevel>0){
                    	
                    	if(raw[i]=='(' ){ //allow for [[ in stream ie [/Indexed /DeviceRGB 255 (abc[[z
                    		
                    		i++;
                    		//System.err.println("==>");
                    		while(raw[i]!=')' || isEscaped(raw, i)){
                    			//System.err.print((char)raw[i]);
                    			i++;
                    		}
                    		//System.err.println("<==");
                    		
                    	}else if(raw[i]=='[' ){
                            reflevel++;
                        }else if(raw[i]==']'){
                            reflevel--;
                        }

                        i++;
                    }
                    i--;
                }else if(raw[i]=='n' && raw[i+1]=='u' && raw[i+2]=='l' && raw[i+3]=='l'){ //allow for null
                    i=i+4;
            //    }else if(raw[i]=='('){

//                	i++;
//    				//find end
//    				while(i<raw.length){
//    					i++;
//    					if(raw[i]==')' && !isEscaped(raw, i))
//    						break;
//    				}
                }else{ //must be a ref

                    //assume not object and reset below if wrong
                    status=PdfObject.UNDECODED_REF;
                 
                    //System.out.println(new String(raw)+"\n\n");
                    while(raw[i]!='R' || raw[i-1]=='e') { //second condition to stop spurious match on DeviceRGB
                       //System.out.print((char)(raw[i]));
                        i++;

                        if(i==raw.length)
                            break;
                    }
                    i++;

                    if(i>=raw.length)
                        i=raw.length-1;
                    // System.out.println("read ref");
                    //i = readDictionaryFromRefOrDirect(PDFkeyInt,pdfObject,objectRef, i, raw,debugFastCode, PDFkeyInt,PDFkey, paddingString);
                }
            }


        valueObj.setStatus(status);
        if(status!=PdfObject.DECODED){

			int StrLength=i-start;
            unresolvedData=new byte[StrLength];
            System.arraycopy(raw, start, unresolvedData, 0, StrLength);

            //check for returns in data if ends with R and correct to space
            if(unresolvedData[StrLength-1]==82){

                for(int jj=0;jj<StrLength;jj++){

                if(unresolvedData[jj]==10 || unresolvedData[jj]==13)
                    unresolvedData[jj]=32;

                }
            }
            valueObj.setUnresolvedData(unresolvedData,PDFkeyInt);


            if(debugFastCode)
                System.out.println(valueObj+" store value >>"+new String(unresolvedData)+"<< into "+pdfObject);

        }

        if(raw[i]=='/' || raw[i]=='>') //move back so loop works
            i--;
        return i;
    }

    private int readDictionary(PdfObject pdfObject, String objectRef, int i,
			byte[] raw, String paddingString, boolean isInlineImage,
			final boolean debugFastCode, int PDFkeyInt, Object PDFkey,
			boolean ignoreRecursion) {
		int keyLength;
		int keyStart;

		//roll on
		if(raw[i]!='<')
			i++;

		//move cursor to start of text
		while(raw[i]==10 || raw[i]==13 || raw[i]==32)
			i++;

		//some objects can have a common value (ie /ToUnicode /Identity-H
		if(raw[i]==47){

			//	System.out.println("Starts with /");

			//if it is a < (60) its a direct object, otherwise its a reference so we need to move and move back at end

			//}else if(raw[i]==60 && 1==2){

			//move cursor to start of text
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
				i++;

			keyStart=i;
			keyLength=0;

			//move cursor to end of text
			while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
				i++;
				keyLength++;
			}

			i--;// move back so loop works

			if(!ignoreRecursion){

				PdfObject valueObj=ObjectFactory.createObject(PDFkeyInt,objectRef, pdfObject.getObjectType(), pdfObject.getID());
				valueObj.setID(PDFkeyInt);
				
				//store value
				int constant=valueObj.setConstant(PDFkeyInt,keyStart,keyLength,raw);

				if(constant==PdfDictionary.Unknown || isInlineImage){

					int StrLength=keyLength;
					byte[] newStr=new byte[StrLength];
					System.arraycopy(raw, keyStart, newStr, 0, StrLength);

					String s=new String(newStr);
					valueObj.setGeneralStringValue(s);

					if(debugFastCode)
						System.out.println(paddingString+"Set Dictionary type "+PDFkey+" as String="+s+"  in "+pdfObject+" to "+valueObj);

				}else if(debugFastCode)
					System.out.println(paddingString+"Set Dictionary type "+PDFkey+" as constant="+constant+"  in "+pdfObject+" to "+valueObj);


				//store value
				pdfObject.setDictionary(PDFkeyInt,valueObj);

			}

		}else //allow for empty object
			if(raw[i]=='e' && raw[i+1]=='n' && raw[i+2]=='d' && raw[i+3]=='o' && raw[i+4]=='b' ){
				//        return i;

				if(debugFastCode)
					System.out.println(paddingString+"Empty object"+new String(raw)+"<<");

			}else if(raw[i]=='(' && PDFkeyInt== PdfDictionary.JS){ //ie <</S/JavaScript/JS( for JS
				i++;
				int start=i;
				//find end
				while(i<raw.length){
					i++;
					if(raw[i]==')' && !isEscaped(raw, i))
						break;
				}
				byte[] data=this.readEscapedValue(i,raw,start, false);

				NamesObject JS=new NamesObject(objectRef);
				JS.setDecodedStream(data);
				pdfObject.setDictionary(PdfDictionary.JS, JS);

			}else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop

				if(debugFastCode)
					System.out.println(paddingString+"1.About to read ref orDirect i="+i+" char="+(char)raw[i]+" ignoreRecursion="+ignoreRecursion);


				if(ignoreRecursion){

					//roll onto first valid char
					while(raw[i]==91 || raw[i]==32 || raw[i]==13 || raw[i]==10){

						//if(raw[i]==91) //track incase /Mask [19 19]
						//	possibleArrayStart=i;

						i++;
					}


					//roll on and ignore
					if(raw[i]=='<' && raw[i+1]=='<'){

						i=i+2;
						int reflevel=1;

						while(reflevel>0){
							if(raw[i]=='<' && raw[i+1]=='<'){
								i=i+2;
								reflevel++;
							}else if(raw[i]=='>' && raw[i+1]=='>'){
								i=i+2;
								reflevel--;
							}else
								i++;
						}
						i--;

					}else{ //must be a ref
						//                					while(raw[i]!='R')
						//                						i++;
						//                					i++;
						//System.out.println("read ref");
						i = readDictionaryFromRefOrDirect(PDFkeyInt,pdfObject,objectRef, i, raw,debugFastCode, PDFkeyInt,PDFkey, paddingString);
					}

					if(i<raw.length && raw[i]=='/') //move back so loop works
						i--;

				}else{
                    i = readDictionaryFromRefOrDirect(PDFkeyInt,pdfObject,objectRef, i, raw,debugFastCode, PDFkeyInt,PDFkey, paddingString);
				}
			}
		return i;
	}

	private int readTextStream(PdfObject pdfObject, String objectRef, int i,
			byte[] raw, String paddingString, final boolean debugFastCode,
			int PDFkeyInt, boolean ignoreRecursion) {

        if(PDFkeyInt==PdfDictionary.W){

            boolean isRef=false;

            if(debugFastCode)
                System.out.println(paddingString+"Reading W");

            //move to start
            while(raw[i]!='[' ){ //can be number as well

                //System.out.println((char) raw[i]);
                if(raw[i]=='('){ //allow for W (7)
                    isRef=false;
                    break;
                }

                //allow for number as in refer 9 0 R
                if(raw[i]>='0' && raw[i]<='9'){
                    isRef=true;
                    break;
                }

                i++;
            }

            //allow for direct or indirect
            byte[] data=raw;

            int start=i,j=i;

            int count=0;

            //read ref data and slot in
            if(isRef){
                //number
                int keyStart2=i,keyLength2=0;
                while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){

                    i++;
                    keyLength2++;

                }
                int number=parseInt(keyStart2,i, raw);

                //generation
                while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
                    i++;

                keyStart2=i;
                //move cursor to end of reference
                while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
                    i++;
                int generation=parseInt(keyStart2,i, raw);

                //move cursor to start of R
                while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
                    i++;

                if(raw[i]!=82) //we are expecting R to end ref
                    throw new RuntimeException("3. Unexpected value in file "+raw[i]+" - please send to IDRsolutions for analysis");

                if(!ignoreRecursion){

                    //read the Dictionary data
                    data=readObjectAsByteArray(pdfObject, objectRef, isCompressed(number,generation),number,generation);

                    //lose obj at start
                    j=3;
                    while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111){
                        j++;

                        //catch for error
                        if(j==data.length){
                            j=0;
                            break;
                        }
                    }

                    //skip any spaces after
                    while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
                        j++;

                    //reset pointer
                    start=j;

                }
            }

            //move to end
            while(j<data.length){

                if(data[j]=='[' || data[j]=='(')
                    count++;
                else if(data[j]==']' || data[j]==')')
                    count--;

                if(count==0)
                    break;

                j++;
            }

            if(!ignoreRecursion){
                int stringLength=j-start+1;
                byte[] newString=new byte[stringLength];

                System.arraycopy(data, start, newString, 0, stringLength);

                /**
                 * clean up so matches old string so old code works
                 */
                if(PDFkeyInt!=PdfDictionary.JS){ //keep returns in code
	                for(int aa=0;aa<stringLength;aa++){
	                    if(newString[aa]==10 || newString[aa]==13)
	                        newString[aa]=32;
	                }
                }

                pdfObject.setTextStreamValue(PDFkeyInt, newString);

                if(debugFastCode)
                    System.out.println(paddingString+"W="+new String(newString)+" set in "+pdfObject);
            }

            //roll on
            if(!isRef)
                i=j;
        }else{
			
        	byte[] data=null;
            try{
                if(raw[i]!='<' && raw[i]!='(')
                    i++;

                while(raw[i]==10 || raw[i]==13 || raw[i]==32)
                    i++;

                //allow for no actual value but another key
                if(raw[i]==47){
                pdfObject.setTextStreamValue(PDFkeyInt, new byte[1]);
                    i--;
                    return i;
                }

                if(debugFastCode){
    				System.out.println(paddingString+"i="+i+" Reading Text from String="+new String(raw)+"<");
            	
    				System.out.println("-->>");
    				for(int zz=i;zz<raw.length;zz++){
    					System.out.print((char)raw[zz]);
    				}
    				System.out.println("<<--");
            	}
                
                //System.out.println("raw["+i+"]="+(char)raw[i]);
                //get next key to see if indirect
                boolean isRef=raw[i]!='<' && raw[i]!='(';

                int j=i;
                data=raw;
                if(isRef){

                    //number
                    int keyStart2=i,keyLength2=0;
                    while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){

                        i++;
                        keyLength2++;
                    }

                    int number=parseInt(keyStart2,i, raw);

                    //generation
                    while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
                        i++;

                    keyStart2=i;
                    //move cursor to end of reference
                    while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
                        i++;
                    int generation=parseInt(keyStart2,i, raw);
                    
                    //move cursor to start of R
                    while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
                        i++;

                    if(raw[i]!=82) //we are expecting R to end ref
                        throw new RuntimeException("3. Unexpected value in file "+(char)raw[i]+" - please send to IDRsolutions for analysis "+pdfObject);

                    if(!ignoreRecursion){

                        //read the Dictionary data
                        data=readObjectAsByteArray(pdfObject, objectRef, isCompressed(number,generation),number,generation);

                        //							System.out.println("data read is>>>>>>>>>>>>>>>>>>>\n");
                        //							for(int ab=0;ab<data.length;ab++)
                        //							System.out.print((char)data[ab]);
                        //							System.out.println("\n<<<<<<<<<<<<<<<<<<<\n");

                        //lose obj at start
                        j=3;
                        while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111)
                            j++;

                        //skip any spaces after
                        while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
                            j++;

                    }
                }
                /////////////////
                int start=0;
                if(!isRef || !ignoreRecursion){
                    //move to start
                    while(data[j]!='(' && data[j]!='<'){
                        j++;

                    }

                    byte startChar=data[j];

                    start=j;

                    //move to end (allow for ((text in brackets))
                    int bracketCount=1;
                    while(j<data.length){
                        //System.out.println(i+"="+raw[j]+" "+(char)raw[j]);
                        j++;

                        if(startChar=='(' && (data[j]==')' || data[j]=='(') && !isEscaped(data,j)){
                            //allow for non-escaped brackets
                            if(data[j]=='(')
                                bracketCount++;
                            else if(data[j]==')')
                                bracketCount--;

                            if(bracketCount==0)
                                break;
                        }

                        if(startChar=='<' && data[j]=='>')
                            break;
                    }

                }

                if(!ignoreRecursion){

                    byte[] newString=null;

                    if(data[start]=='<'){
                        start++;

                        int byteCount=(j-start)>>1;
                        newString=new byte[byteCount];

                        int byteReached=0,topHex=0,bottomHex=0;;
                        while(true){

                            if(start==j)
                                break;

                            while(data[start]==32 || data[start]==10 || data[start]==13)
                                start++;

                            topHex=data[start];

                            //convert to number
                            if(topHex>='A' && topHex<='F'){
                                topHex = topHex - 55;
                            }else if(topHex>='a' && topHex<='f'){
                                topHex = topHex - 87;
                            }else if(topHex>='0' && topHex<='9'){
                                topHex = topHex - 48;
                            }else
                                throw new RuntimeException("Unexpected number "+(char)data[start]);


                            start++;

                            while(data[start]==32 || data[start]==10 || data[start]==13)
                                start++;

                            bottomHex=data[start];

                            if(bottomHex>='A' && bottomHex<='F'){
                                bottomHex = bottomHex - 55;
                            }else if(bottomHex>='a' && bottomHex<='f'){
                                bottomHex = bottomHex - 87;
                            }else if(bottomHex>='0' && bottomHex<='9'){
                                bottomHex = bottomHex - 48;
                            }else
                                throw new RuntimeException("Unexpected number "+(char)data[start]);

                            start++;

                            //calc total
                            int finalValue=bottomHex+(topHex<<4);

                            newString[byteReached] = (byte)finalValue;

                            byteReached++;

                        }



                    }else{
                        //roll past (
                        if(data[start]=='(')
                            start++;

                        newString = readEscapedValue(j,data, start,PDFkeyInt==PdfDictionary.ID);
                    }

                    if(pdfObject.getObjectType()!=PdfDictionary.Encrypt){// && pdfObject.getObjectType()!=PdfDictionary.Outlines){

                        try {

                            if(!isAES || isMetaDataEncypted)
                                newString=decrypt(newString,objectRef, false,null, false,false);
                        } catch (PdfSecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    /**
                     * we need full names for Forms
                     */
                    if(FormObject.newfieldnameRead && pdfObject.getObjectType()==PdfDictionary.Form && PDFkeyInt==PdfDictionary.T){
                    
                    	//at this point newString is the raw byte value (99% of the time this is the
                    	//string but it can be encode in some other ways (like a set of hex values)
                    	//so we need to use PdfReader.getTextString(newString, false) rather than new String(newString)
                    	//6 0 obj <</T <FEFF0066006F0072006D0031005B0030005D>
                    	//
                    	//Most of the time you can forget about this because getTextStream() handles it for you 
                    	//
                    	//Except here where we are manipulating the bytes directly...
                    	String fieldName =PdfReader.getTextString(newString,false);//((FormObject)pdfObject).getFieldName();
                        String parent = ((FormObject)pdfObject).getStringKey(PdfDictionary.Parent);

                        // if no name, or parent has one recursively scan tree for one in Parent
                        boolean isMultiple=false;

                        while (parent != null) {

                            FormObject parentObj =new FormObject(parent,false);
                            readObject(parentObj);

                            String newName = parentObj.getTextStreamValue(PdfDictionary.T);
                            if (fieldName == null && newName != null)
                                fieldName = newName;
                            else if (newName != null){
                                //we pass in kids data so stop name.name
                                if(!fieldName.equals(newName)) {
                                    fieldName = newName + "." + fieldName;
                                    isMultiple=true;
                                }
                            }
                            if (newName == null)
                                break;

                            parent = parentObj.getParentRef();
                        }
                        
                        //set the field name to be the Fully Qualified Name
                        if(isMultiple)
                        	newString = fieldName.getBytes();
                    }
                    
                    pdfObject.setTextStreamValue(PDFkeyInt, newString);

                    if(debugFastCode)
                        System.out.println(paddingString + "TextStream=" + new String(newString)+" in pdfObject="+pdfObject);
                }

                if(!isRef)
                    i=j;

            }catch(Exception ee){
                ee.printStackTrace();

            }
        }
        return i;
    }

	private static byte[] readEscapedValue(int j, byte[] data, int start, boolean keepReturns) {
		byte[] newString;
		//see if escape values
		boolean escapedValues=false;
		for(int aa=start;aa<j;aa++){

            if(data[aa]=='\\' || data[aa]==10 || data[aa]==13){ //convert escaped chars
				escapedValues=true;
				aa=j;
			}
		}

		if(!escapedValues){ //no escapes so fastest copy
			int stringLength=j-start;
			
			if(stringLength<1)
				return new byte[0];
			
			newString=new byte[stringLength];

			System.arraycopy(data, start, newString, 0, stringLength);
		}else{ //translate escaped chars on copy

			int jj=0, stringLength=j-start; //max length
			newString=new byte[stringLength];

			for(int aa=start;aa<j;aa++){

				if(data[aa]=='\\'){ //convert escaped chars

					aa++;
					byte nextByte=data[aa];
                    if(nextByte=='b')
						newString[jj]='\b';
					else if(nextByte=='n')
						newString[jj]='\n';
					else if(nextByte=='t')
						newString[jj]='\t';
					else if(nextByte=='r')
						newString[jj]='\r';
					else if(nextByte=='f')
						newString[jj]='\f';
					else if(nextByte=='\\')
						newString[jj]='\\';

					else if(nextByte>47 && nextByte<58){ //octal

						StringBuffer octal=new StringBuffer(3);

						boolean notOctal=false;
						for(int ii=0;ii<3;ii++){

							if(data[aa]=='\\' || data[aa]==')' || data[aa]<'0' || data[aa]>'9') //allow for less than 3 values
								ii=3;
							else{
								octal.append((char)data[aa]);
								
								//catch for odd values
								if(data[aa]>'7')
									notOctal=true;
								
								aa++;
							}
						}
						//move back 1
						aa--;
						//isOctal=true;
						if(notOctal)
							newString[jj]=(byte) Integer.parseInt(octal.toString());
						else
							newString[jj]=(byte) Integer.parseInt(octal.toString(),8);

					}else if(nextByte==13 || nextByte==10){ //ignore bum data
						jj--;
                    }else
						newString[jj]=nextByte;

					jj++;
                }else if(!keepReturns && (data[aa]==13 || data[aa]==10)){ //convert returns to spaces
                    newString[jj]=32;
					jj++;
				}else{
					newString[jj]=data[aa];
					jj++;
				}
			}

			//now resize
			byte[] rawString=newString;
			newString=new byte[jj];

			System.arraycopy(rawString, 0, newString, 0, jj);
		}
		return newString;
	}

	private int readNumber(PdfObject pdfObject, String objectRef, int i,
			byte[] raw, String paddingString, final boolean debugFastCode,
			int PDFkeyInt, Object PDFkey) {
		int keyLength;
		int keyStart;

		keyStart=i;
		keyLength=0;

		//move cursor to end of text
		while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62 && raw[i]!='(' && raw[i]!='.'){
			i++;
			keyLength++;
		}

		//actual value or first part of ref
		int number=parseInt(keyStart,i, raw);


		//roll onto next nonspace char and see if number
		int jj=i;
		while(jj<raw.length &&(raw[jj]==32 || raw[jj]==13 || raw[jj]==10))
			jj++;

		boolean  isRef=false;

		//check its not a ref (assumes it XX 0 R)
		if(raw[jj]>= 48 && raw[jj]<=57){ //if next char is number 0-9 it may be a ref

			int aa=jj;

			//move cursor to end of number
			while((raw[aa]!=10 && raw[aa]!=13 && raw[aa]!=32 && raw[aa]!=47 && raw[aa]!=60 && raw[aa]!=62))
				aa++;

			//move cursor to start of text
			while(aa<raw.length && (raw[aa]==10 || raw[aa]==13 || raw[aa]==32 || raw[aa]==47))
				aa++;

			isRef=aa<raw.length && raw[aa]=='R';

		}

		if(isRef){
			//move cursor to start of generation
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
				i++;

			/**
			 * get generation number
			 */
			keyStart=i;
			//move cursor to end of reference
			while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
				i++;

			int generation=parseInt(keyStart,i, raw);

			//move cursor to start of R
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
				i++;

			if(raw[i]!=82){ //we are expecting R to end ref
				throw new RuntimeException("3. Unexpected value in file - please send to IDRsolutions for analysis");
			}

			//read the Dictionary data
			byte[] data=readObjectAsByteArray(pdfObject, objectRef, isCompressed(number,generation),number,generation);

			//lose obj at start
			int j=0;

            //allow for example where start <<
            if(data.length>1 && data[0]=='<' && data[1]=='<'){

            }else{
                j=3;
                while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111)
                    j++;
            }
			//skip any spaces after
			while(data[j]==10 || data[j]==13 || data[j]==32)// || data[j]==47 || data[j]==60)
				j++;

			int count=j;

			//skip any spaces at end
			while(data[count]!=10 && data[count]!=13 && data[count]!=32)// || data[j]==47 || data[j]==60)
				count++;

            number=parseInt(j,count, data);

		}

		//store value
		pdfObject.setIntNumber(PDFkeyInt,number);

		if(debugFastCode)
			System.out.println(paddingString+"set key="+PDFkey+" numberValue="+number);//+" in "+pdfObject);

		//System.out.println((char)raw[i]+""+(char)raw[i+1]+""+(char)raw[i+2]);

		//if(raw[i+1]==47)
		i--;// move back so loop works
		//if(raw[i]==47)
		//	i--;
		//	i=i-2;
		//else
		//	i--;

		//i=i+keyLength-1;
		return i;
	}


	private int handleColorSpaces(PdfObject pdfObject,int i, byte[] raw,
                                  boolean debugFastCode, String paddingString) {

		final boolean debugColorspace=false;//pdfObject.getObjectRefAsString().equals("194 0 R");//pdfObject.getDebugMode();// || debugFastCode;
		
		if(debugColorspace){

			System.out.println(paddingString+"Reading colorspace into "+pdfObject+" ref="+pdfObject.getObjectRefAsString()+" i="+i+" chars="+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]);

			System.out.println(paddingString+"------------>");
			for(int ii=i;ii<raw.length;ii++){
				System.out.print((char)raw[ii]);

				if(ii>5 && raw[ii-5]=='s' && raw[ii-4]=='t' && raw[ii-3]=='r' && raw[ii-2]=='e' && raw[ii-1]=='a' &&raw[ii]=='m')
					ii=raw.length;
			}

			System.out.println("<--------");

		}

		//ignore any spaces
		while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]=='[')
			i++;

		if(raw[i]=='/'){

			/**
			 * read the first value which is ID
			 **/

			i++;

			//move cursor to start of text
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47){
				//System.out.println("skip="+raw[i]);
				i++;
			}

			int keyStart=i;
			int keyLength=0;

			//System.out.println("firstChar="+raw[i]+" "+(char)raw[i]);

			if(debugColorspace)
				System.out.print(paddingString+"Colorspace is /");

			//move cursor to end of text
			while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62 && raw[i]!='[' && raw[i]!=']'){

				if(debugColorspace)
					System.out.print((char)raw[i]);

				i++;
				keyLength++;

				if(i==raw.length)
					break;
			}

			if(debugColorspace)
				System.out.println("");


			i--;// move back so loop works

			//store value
			int constant=pdfObject.setConstant(PdfDictionary.ColorSpace,keyStart,keyLength,raw);

			//allow for abreviation in ID command
			if(constant==PdfDictionary.I)
				constant=ColorSpaces.Indexed;
			
			if(debugColorspace)
				System.out.println(paddingString+"set ID="+constant+" in "+pdfObject);

			i++;//roll on

			switch(constant){

			case ColorSpaces.CalRGB:{

				if(debugColorspace)
					System.out.println(paddingString+"CalRGB Colorspace");

				i=handleColorSpaces(pdfObject, i,  raw, debugFastCode, paddingString+"    ");

				i++;

				break;
			}case ColorSpaces.CalGray:{

				if(debugColorspace)
					System.out.println(paddingString+"CalGray Colorspace");

				i=handleColorSpaces(pdfObject, i,  raw, debugFastCode, paddingString+"    ");

				i++;

				break;
			}case ColorSpaces.DeviceCMYK:{

				if(debugColorspace)
					System.out.println(paddingString+"DeviceGray Colorspace");

				break;
			}case ColorSpaces.DeviceGray:{

				if(debugColorspace)
					System.out.println(paddingString+"DeviceGray Colorspace");

				break;
			}case ColorSpaces.DeviceN:{

				if(debugColorspace)
					System.out.println(paddingString+"DeviceN Colorspace >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]);

				int endPoint=i;
				while(endPoint<raw.length && raw[endPoint]!=']'){
					//System.out.println(endPoint+" "+(char)raw[endPoint]+" "+raw[endPoint]);
					endPoint++;
				}

                //read Components
				i=readArray(false,i, endPoint, PdfDictionary.VALUE_IS_STRING_ARRAY, raw, "", pdfObject,
						PdfDictionary.Components, debugFastCode,paddingString, null, -1);

                if(debugColorspace)
                System.out.println("i="+i+" "+(char)raw[i]+" "+raw[i]);
                
                while(raw[i]==93 || raw[i]==32 || raw[i]==10 || raw[i]==13)
					i++;

				if(debugColorspace)
					System.out.println(paddingString+"i="+i+" DeviceN Reading altColorspace >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]+(char)raw[i+5]+(char)raw[i+6]+(char)raw[i+7]+(char)raw[i+8]);

                if(debugColorspace)
                                    System.out.println(paddingString+"before alt colorspace >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]);

                if(debugColorspace)
                System.out.println("i="+i+"  >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]+(char)raw[i+5]+(char)raw[i+6]+(char)raw[i+7]);

                //read the alt colorspace
				PdfObject altColorSpace=new ColorSpaceObject(-1,0);
				i=handleColorSpaces(altColorSpace, i,  raw, debugFastCode, paddingString+"    ");
				pdfObject.setDictionary(PdfDictionary.AlternateSpace,altColorSpace);

                if(debugColorspace){
                    System.out.println("i="+i+" Alt colorspace="+altColorSpace+" "+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]);

                }
                
                i++;

				if(debugColorspace)
					System.out.println(paddingString+"DeviceN Reading tintTransform >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]);

                //read the transform
				PdfObject tintTransform=new FunctionObject(-1,0);

				i=handleColorSpaces(tintTransform, i,  raw, debugFastCode, paddingString+"    ");
				pdfObject.setDictionary(PdfDictionary.tintTransform,tintTransform);

                //check for attributes
                for(int ii=i;ii<raw.length;ii++){

                   // System.out.println((char)raw[ii]+""+(char)raw[ii+1]+""+(char)raw[ii+2]+""+(char)raw[ii+3]);

                    if(raw[ii]==']'){
                        ii=raw.length;
                        break;
                    }else if(raw[ii]==32 || raw[ii]==10 || raw[ii]==13){//ignore spaces
                    }else{

                        i=ii;
                        //read the attributes
                        PdfObject attributesObj=new ColorSpaceObject(-1,0);
                        i=handleColorSpaces(attributesObj, i,  raw, false, paddingString+"    ");
                        pdfObject.setDictionary(PdfDictionary.Process,attributesObj);
                        //i--;
                       
                        return i;

                    }
                }
                if(debugColorspace)
                	System.out.println("i="+i+"  >>"+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]+(char)raw[i+5]+(char)raw[i+6]+(char)raw[i+7]);
                 
                i++;

				break;
			}case ColorSpaces.DeviceRGB:{

				if(debugColorspace)
					System.out.println(paddingString+"DeviceRGB Colorspace");

				break;
			}case ColorSpaces.ICC:{

				if(debugColorspace)
					System.out.println(paddingString+"ICC Colorspace");

				//get the colorspace data
				i=readDictionaryFromRefOrDirect(-1, pdfObject,"", i, raw,debugFastCode, PdfDictionary.ColorSpace,"ICC colorspace", paddingString);

				break;

			}case ColorSpaces.Indexed:{

                    if(debugColorspace){
                    	System.out.println(new String(raw));
                        System.out.println(paddingString + "Indexed Colorspace - Reading base >>" + (char) raw[i] + (char) raw[i + 1]+(char)+raw[i+2]+"<< i="+i+" into "+pdfObject);
                    }
                    //read the base value
                    PdfObject IndexedColorSpace=new ColorSpaceObject(-1,0,true);

                    //IndexedColorSpace.setRef(pdfObject.getObjectRefAsString());
                    i=handleColorSpaces(IndexedColorSpace, i,  raw, debugFastCode, paddingString+"    ");
                    pdfObject.setDictionary(PdfDictionary.Indexed,IndexedColorSpace);

                    if(debugColorspace)
                        System.out.println(paddingString + "Indexed Reading hival starting at >>" + (char) raw[i] + (char) raw[i + 1]+(char)+raw[i+2]+"<<i="+i);

                    //onto hival number
                    while(i<raw.length && (raw[i]==32 || raw[i]==13 || raw[i]==10 || raw[i]==']' || raw[i]=='>'))
                        i++;

                    //roll on
                    //i++;

                    //move cursor to start of text
                    //while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47)
                    //    i++;

                    //hival
                    i = readNumber(pdfObject, "", i, raw, paddingString, false, PdfDictionary.hival, "hival");

                    if(i!='(')
                        i++;

                    if(debugColorspace)
                        System.out.println(paddingString + "next chars >>" + (char) raw[i] + (char) raw[i + 1]+(char)+raw[i+2]+"<<i="+i);


                    //onto lookup
                    while(i<raw.length && (raw[i]==32 || raw[i]==13 || raw[i]==10))
                        i++;

                    if(debugColorspace)
                        System.out.println(paddingString+"Indexed Reading lookup "+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+(char)raw[i+3]+(char)raw[i+4]);

                    //read lookup
                    //get the colorspace data (base)

                    PdfObject IndexedColorSpaceData=null;

                    //can be embedded in this object (when we need to use ref or in separate object
                    //when we need to use that ref). This code switches as needed)
                    boolean needsKey=raw[i]=='[' || raw[i]=='(' || raw[i]=='<';
                    if(needsKey)
                        IndexedColorSpaceData=new ColorSpaceObject(pdfObject.getObjectRefAsString());
                    else
                        IndexedColorSpaceData=new ColorSpaceObject(-1,0);

                    //IndexedColorSpace.setRef(pdfObject.getObjectRefAsString());
                    pdfObject.setDictionary(PdfDictionary.Lookup,IndexedColorSpaceData);

                    //i=readDictionaryFromRefOrDirect(-1, pdfObject,"", i, raw,debugFastCode, -1,"", paddingString);
                    i=handleColorSpaces(IndexedColorSpaceData, i,  raw, debugFastCode, paddingString);

                    //if(debugColorspace)
                    //System.out.println(paddingString + "after stream>>" + (char) raw[i] + (char) raw[i + 1]+(char)+raw[i+2]+"<<i="+i);

                    i++;

                    break;

                }case ColorSpaces.Lab:{

				if(debugColorspace)
					System.out.println(paddingString+"Lab Colorspace");

				i=handleColorSpaces(pdfObject, i,  raw, debugFastCode, paddingString);

				i++;

				break;

			}case ColorSpaces.Pattern:{

				if(debugColorspace)
					System.out.println(paddingString+"Pattern Colorspace");

				break;
			}case ColorSpaces.Separation:{

				if(debugColorspace)
					System.out.println(paddingString+"Separation Colorspace");

				int endPoint=i;

				//roll on to start
				while(raw[endPoint]=='/' || raw[endPoint]==32 ||raw[endPoint]==10 ||raw[endPoint]==13)
					endPoint++;

				int startPt=endPoint;

				//get name length
				while(endPoint<raw.length){
					if(raw[endPoint]=='/' || raw[endPoint]==' ' || raw[endPoint]==13 || raw[endPoint]==10)
						break;

					endPoint++;
				}

				//read name
				//set value
				keyLength=endPoint-startPt;
				byte[] stringBytes=new byte[keyLength];
				System.arraycopy(raw,startPt,stringBytes,0,keyLength);

				//store value
				pdfObject.setName(PdfDictionary.Name,stringBytes);

				if(debugColorspace)
					System.out.println(paddingString+"name="+new String(stringBytes)+" "+pdfObject);

				i=endPoint;

				if(raw[i]!=47)
					i++;

				if(debugColorspace)
					System.out.println(paddingString+"Separation Reading altColorspace >>"+(char)raw[i]+(char)raw[i+1]);

				//read the alt colorspace
				PdfObject altColorSpace=new ColorSpaceObject(-1,0);
				i=handleColorSpaces(altColorSpace, i,  raw, debugFastCode, paddingString);
				pdfObject.setDictionary(PdfDictionary.AlternateSpace,altColorSpace);

				//allow for no gap
				if(raw[i]!='<')
					i++;

				//read the transform
				PdfObject tintTransform=new FunctionObject(-1,0);

				if(debugColorspace)
					System.out.println(paddingString + "Separation Reading tintTransform " + (char) raw[i - 1] + (char) raw[i]+(char)raw[i+1]+" into "+tintTransform);

				i=handleColorSpaces(tintTransform, i,  raw, debugFastCode, paddingString);
				pdfObject.setDictionary(PdfDictionary.tintTransform,tintTransform);

				i++;

				break;
			}

			}

		}else if(raw[i]=='<' && raw[i+1]=='<'){

			if(debugColorspace)
				System.out.println(paddingString + "Direct object starting " + (char) raw[i] + (char) raw[i + 1]+(char)raw[i+2]);

			//System.out.println("i1="+i);
			i = convertDirectDictionaryToObject(pdfObject, "", i, raw, debugFastCode, -1, paddingString);
			//i = convertDirectDictionaryToObject(pdfObject, "", i, raw, i==168, -1, paddingString);

			//System.out.println("i2="+i);
			//allow for stream
			/**
			 * look for stream afterwards
			 */
			if(pdfObject.hasStream()){
			int count=raw.length, ends=0;
			for(int xx=i;xx<count-5;xx++){

				//avoid reading on subobject ie <<  /DecodeParams << >> >>
				if(raw[xx]=='>' && raw[xx+1]=='>')
					ends++;
				if(ends==2){
					if(debugColorspace)
						System.out.println(paddingString+"Ignore Stream as not in sub-object "+pdfObject);

					break;
				}

				if(raw[xx] == 's' && raw[xx + 1] == 't' && raw[xx + 2] == 'r' &&
						raw[xx + 3] == 'e' && raw[xx + 4] == 'a' && raw[xx + 5] == 'm'){

					if(debugColorspace)
						System.out.println(paddingString+"2. Stream found afterwards");

					readStreamIntoObject(pdfObject, debugFastCode,xx, raw, pdfObject,paddingString);
					xx=count;

				}
			}
			}

			//if(debugColorspace)
			//System.out.println(paddingString+"At end="+(char)raw[i]+(char)raw[i+1]+(char)raw[i+2]+"<< i="+i);
		}else if(raw[i]=='<'){ // its array of hex values (ie <FFEE>)

			i++;
			//find end
			int end=i, validCharCount=0;

			//System.err.println("RAW stream ...");
			//for(int y=0;y<raw.length;y++){
			//	System.err.print((char)raw[y]);
			//}
			//System.err.println("\n\n");

			//here
			while(raw[end]!='>'){
				if(raw[end]!=32 && raw[end]!=10 && raw[end]!=13)
					validCharCount++;
				end++;
			}

			int byteCount=validCharCount>>1;
			byte[] stream=new byte[byteCount];

			int byteReached=0,topHex=0,bottomHex=0;;
			while(true){
				while(raw[i]==32 || raw[i]==10 || raw[i]==13)
					i++;

				topHex=raw[i];

				//convert to number

				//System.out.println("-> raw[i]=" + (char)topHex);

				if(topHex>='A' && topHex<='F'){
					topHex = topHex - 55;
				}else if(topHex>='a' && topHex<='f'){
					topHex = topHex - 87;
				}else if(topHex>='0' && topHex<='9'){
					topHex = topHex - 48;
				}else
					throw new RuntimeException("Unexpected number "+(char)raw[i]);


				i++;

				while(raw[i]==32 || raw[i]==10 || raw[i]==13)
					i++;

				bottomHex=raw[i];

				if(bottomHex>='A' && bottomHex<='F'){
					bottomHex = bottomHex - 55;
				}else if(bottomHex>='a' && bottomHex<='f'){
					bottomHex = bottomHex - 87;
				}else if(bottomHex>='0' && bottomHex<='9'){
					bottomHex = bottomHex - 48;
				}else
					throw new RuntimeException("Unexpected number "+(char)raw[i]);

				i++;


				//calc total
				int finalValue=bottomHex+(topHex<<4);

				stream[byteReached] = (byte)finalValue;

				byteReached++;

				//System.out.println((char)topHex+""+(char)bottomHex+" "+byteReached+"/"+byteCount);
				if(byteReached==byteCount)
					break;
			}

			pdfObject.setDecodedStream(stream);


		}else if(raw[i]=='('){ // its array of hex values (ie (\000\0\027)

			i++; //move past (

			int start=i;

			//find end of textstream
			while(true){

				if(raw[i]==')' && !isEscaped(raw,i))
					break;

				i++;				
			}
			i++;

			byte[] nRaw = this.readEscapedValue(i, raw, start, false);

			try {
				nRaw=decrypt(nRaw,pdfObject.getObjectRefAsString(), false,null, false,false);
			} catch (PdfSecurityException e) {
				e.printStackTrace();
			}

			pdfObject.setDecodedStream(nRaw);

		}else{ //assume its an object

			if(debugColorspace)
				System.out.println(paddingString+"(assume object) starts with "+raw[i]+" "+raw[i+1]+" "+pdfObject+" "+pdfObject.getObjectRefAsString());

			//number
			int keyStart2=i,keyLength2=0;
			while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){

				i++;
			}
			int number=parseInt(keyStart2,i, raw);

            if(debugColorspace)
            System.out.println(">>"+new String(raw,keyStart2,i-keyStart2));

            //generation
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
				i++;

			keyStart2=i;
			//move cursor to end of reference
			while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
				i++;
			int generation=parseInt(keyStart2,i, raw);

			//move cursor to start of R
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
				i++;

            if(debugColorspace){
                System.out.println(paddingString+">>"+number+" "+generation+" "+pdfObject.getObjectRefAsString());
                //System.out.println("raw="+new String(raw));
            }

            if(raw[i]!=82){ //we are expecting R to end ref
				throw new RuntimeException("3. Unexpected value in file "+(char)raw[i]+" - please send to IDRsolutions for analysis");
            }
            
            i++;

            if(debugColorspace){
                System.out.println(pdfObject.getObjectRefAsString());
            }

            if(debugColorspace)
                System.out.println(paddingString+"Before setRef="+number+" "+generation+" R pdfObject.getObjectRefAsString()="+pdfObject.getObjectRefAsString()+" "+pdfObject);

            if(pdfObject.getObjectRefID()==-1)
                    pdfObject.setRef(number,generation);

            if(debugColorspace)
                System.out.println(paddingString+"After setRef="+number+" "+generation+" R pdfObject.getObjectRefAsString()="+pdfObject.getObjectRefAsString());

			//read the Dictionary data
			byte[] data=readObjectAsByteArray(pdfObject, "1", isCompressed(number,generation),number,generation);

            //allow for direct (ie /DeviceCMYK)
            if(data[0]=='/'){

                handleColorSpaces(pdfObject,0, data,debugFastCode,  paddingString);
            }else{

			int j=0;
			if(data[0]!='[' && data[0]!='<'){
				//lose obj at start
				j=3;
				while(data[j-1]!=106 && data[j-2]!=98 && data[j-3]!=111){
					j++;
				}
			}

            if(debugColorspace)
            System.out.println("Read obj i="+i+" j="+j+" "+(char)data[j]+""+(char)data[j+1]);
            
            handleColorSpaces(pdfObject,j,  data, debugFastCode, paddingString);
            }
        }

		//if(debugColorspace)
		//System.out.println(paddingString+"return i="+i+" >>"+(char)raw[i]+""+(char)raw[i+1]+""+(char)raw[i+2]);

		//roll back if no gap
		if(i<raw.length && (raw[i]==47 || raw[i]=='>'))
			i--;

		return i;
	}


	//count backwards to get total number of escape chars
	private static boolean isEscaped(byte[] raw, int i) {
		int j=i-1, escapedFound=0;
		while(j>-1 && raw[j]=='\\'){
			j--;
			escapedFound++;
		}


		//System.out.println("escapedFound="+escapedFound+" "+(escapedFound & 2 ));
		if((escapedFound & 1 )==1) //cancels each other out
			return true;
		else
			return false;

	}

	/**
	 * read a dictionary object
	 *
	public int readDictionary(String objectRef, int level, Map rootObject, int i, byte[] raw,
			Map textFields, int endPoint){

		boolean preserveTextString=false;

		final boolean debug=false;
		//debug=objectRef.equals("37004 0 R");
		int keyLength=0,keyStart=-1;

		Object PDFkey=null;

		while(true){

			i++;

			if(i>=raw.length || (endPoint!=-1 && i>=endPoint))
				break;

			//break at end
			if ((raw[i] == 62) && (raw[i + 1] == 62))
				break;

			if ((raw[i] == 101)&& (raw[i + 1] == 110)&& (raw[i + 2] == 100)&& (raw[i + 3] == 111))
				break;

			//handle recursion
			if ((raw[i] == 60) && ((raw[i + 1] == 60))){
				level++;
				i++;

				if(debug)
					System.err.println(level+" << found key="+PDFkey+"= nextchar="+raw[i + 1]);

				Map dataValues=new HashMap();
				rootObject.put(PDFkey,dataValues);
				i=readDictionary(objectRef,level,dataValues,i,raw, textFields,endPoint);

				//i++;
				keyLength=0;
				level--;
				//allow for >>>> with no spaces
				if (raw[i] == 62 && raw[i + 1] == 62)
					i++;

			}else  if (raw[i] == 47 && raw[i+1] == 47) { //allow for oddity of //DeviceGray in colorspace
				i++;
			}else  if (raw[i] == 47 && keyLength==0) { //everything from /

				i++;

				while (true) { //get key up to space or [ or / or ( or < or carriage return

					if ((raw[i] == 32)|| (raw[i] == 13) || (raw[i] == 9) || (raw[i] == 10) ||(raw[i] == 91)||(raw[i]==47)||
							(raw[i]==40)||(raw[i]==60))
						break;

					if(keyLength==0){
						keyStart=i;
					}
					keyLength++;

					i++;
				}

				//allow for / /DeviceGray permutation
				if((raw[i] == 32)&&(raw[i-1] == 47)){
					keyLength++;
				}

				if(keyStart==-1)
					PDFkey=null;
				else
					PDFkey=PdfDictionary.getKey(keyStart,keyLength,raw);

				if(debug)
					System.err.println(level+" key="+PDFkey+ '<');

				//set flag to extract raw text string

				if((textFields!=null)&&(keyLength>0)&&(textFields.containsKey(PDFkey))){
					preserveTextString=true;
				}else
					preserveTextString=false;

				if(raw[i]==47 || raw[i]==40 || raw[i]==60 || raw[i] == 91) //move back cursor
					i--;

			}else if(raw[i]==32 || raw[i]==13 || raw[i]==10){
			}else if(raw[i]==60 && preserveTextString){ //text string <00ff>

				final boolean debug2=false;

				byte[] streamData;
				i++;

				ByteArrayOutputStream bis=null;
				if(debug2)
					bis=new ByteArrayOutputStream();

				int count=0,i2=i;
				// workout number of bytes

				while(true){

					i2=i2+2;

					count++;

					//ignore returns
					while(raw[i2]==13|| raw[i2]==10)
						i2++;

					if(raw[i2]==62)
						break;

				}
				streamData=new byte[count];
				count=0;

				// convert to values

				while(true){

					if(raw[i]=='>')
						break;

					StringBuffer newValue=new StringBuffer(2);
					for(int j=0;j<2;j++){
						newValue.append((char)raw[i]);
						i++;
					}

					if(debug2)
						bis.write(Integer.parseInt(newValue.toString(),16));

					streamData[count]=(byte)Integer.parseInt(newValue.toString(),16);
					count++;

					//ignore returns
					while(raw[i]==13 || raw[i]==10)
						i++;

					if(raw[i]==62)
						break;

				}

				try{

					if(debug2)
						bis.close();

					if(debug2){
						byte[] stream2=bis.toByteArray();

						if(streamData.length!=stream2.length)
							throw new RuntimeException("Different lengths "+streamData.length+ ' ' +stream2.length);

						for(int jj=0;jj<stream2.length;jj++){
							if(stream2[jj]!=streamData[jj])
								throw new RuntimeException(jj+" Different values "+stream2[jj]+ ' ' +streamData[jj]);

						}
					}
					streamData=decrypt(streamData,objectRef, false,null,false,false);

					rootObject.put(PDFkey,streamData); //save pair and reset
				}catch(Exception e){
					LogWriter.writeLog("[PDF] Problem "+e+" writing text string"+PDFkey);
					e.printStackTrace();
				}

				keyLength=0;

				//ignore spaces and returns
			}else if(raw[i]==40){ //read in (value) excluding any returns

				if(preserveTextString){
					ByteArrayOutputStream bis=new ByteArrayOutputStream();
					try{
						if(raw[i+1]!=41){ //trap empty field
							while(true){

								i++;
								boolean isOctal=false;

								//trap escape
								if((raw[i]==92)){
									i++;

									if(raw[i]=='b')
										raw[i]='\b';
									else if(raw[i]=='n')
										raw[i]='\n';
									else if(raw[i]=='t')
										raw[i]='\t';
									else if(raw[i]=='r')
										raw[i]='\r';
									else if(raw[i]=='f')
										raw[i]='\f';
									else if(raw[i]=='\\')
										raw[i]='\\';
									else if(Character.isDigit((char) raw[i])){ //octal
										StringBuffer octal=new StringBuffer(3);
										for(int ii=0;ii<3;ii++){
											octal.append((char)raw[i]);
											i++;
										}
										//move back 1
										i--;
										isOctal=true;
										raw[i]=(byte) Integer.parseInt(octal.toString(),8);
									}
								}

								//exit at end
								if(!isOctal && raw[i]==41 &&(raw[i-1]!=92 ||(raw[i-1]==92 && raw[i-2]==92)))
									break;

								bis.write(raw[i]);

							}
						}

						bis.close();

						byte[] streamData=bis.toByteArray();

						//if(stringsEncoded)
						streamData=decrypt(streamData,objectRef, false, null,false,true);

						//substitute dest key otherwise write through
						if(PDFkey.equals("Dest")){
							String destKey=PdfReader.getTextString(streamData);
							rootObject.put(PDFkey,convertNameToRef(destKey)); //save pair and reset

						}else
							rootObject.put(PDFkey,streamData); //save pair and reset

					}catch(Exception e){
						LogWriter.writeLog("[PDF] Problem "+e+" handling text string"+PDFkey);
					}
					}else if(isEncryptionObject && keyLength==1 &&(firstKey=='U' || firstKey=='O')){
						int count=32;

						ByteArrayOutputStream bis=new ByteArrayOutputStream();
						while(true){

							i++;

							byte next=raw[i];
							if(next==92){

								i++;
								next=raw[i];

								//map chars correctly
								if(next==114)
									next=13;
								else if(next==110)
									next=10;
								else if(next==116)
									next=9;
								else if(next==102) // \f
								next=12;
								else if(next==98) // \b
									next=8;
								else if(next>47 && next<58){ //octal

									StringBuffer octal=new StringBuffer(3);
									for(int ii=0;ii<3;ii++)
										octal.append((char)raw[i+ii]);

									i=i+2; //roll on extra chars

									//substitute
									next=(byte)(Integer.parseInt(octal.toString(),8));

								}

							}

							bis.write(next);

							count--;

							if(count==0)
								break;

						}
						try{
							bis.close();
							rootObject.put(PDFkey,bis.toByteArray()); //save pair and reset
						}catch(Exception e){
							LogWriter.writeLog("[PDF] Problem "+e+" writing "+PDFkey);
						}

				}else{
					int startValue=i,opPointer=0;
					boolean inComment=false;
					while(true){

						if(!inComment)
							opPointer++;

						if(((raw[i-1]!=92)||(raw[i-2]==92))&&(raw[i]==41))
							break;

						i++;

						if((raw[i]==37)&&(raw[i-1]!=92)) //ignore comments %
							inComment=true;

					}

					inComment=false;
					int p=0;
					char[] value=new char[opPointer];
					while(true){
						if(!inComment){
							if((raw[startValue]==13)|(raw[startValue]==10)){
								value[p]=' ';
								p++;
							}else{
								value[p]=(char)raw[startValue];
								p++;
							}
						}

						//avoid \\) where \\ causes problems and allow for \\\
						if((raw[startValue]!=92)&&(raw[startValue-1]==92)&&(raw[startValue-2]==92))
							raw[startValue-1]=0;

						if((raw[startValue-1]!=92)&(raw[startValue]==41))
							break;

						startValue++;

						if((raw[startValue]==37)&&(raw[startValue-1]!=92)) //ignore comments %
							inComment=true;

					}

					//save pair and reset
					String finalOp = String.copyValueOf(value,0,opPointer);

					if(!finalOp.equals("null"))
						rootObject.put(PDFkey,finalOp);

					if(debug)
						System.err.println(level+" *0 "+PDFkey+"=="+finalOp+ '<');

				}
				keyLength=0;
			}else if(raw[i]==91 && isFDF){ //read in [value] excluding any returns


				Map fdfTable=new HashMap();

				//read paired values
				while(true){

					//find <<
					while (raw[i+1]!=60 && raw[i+2]!=60 && raw[i]!=93)
						i++;


					//find >>
					int end=i;
					while (raw[end+1]!=62 && raw[end+2]!=62 && raw[end]!=93)
						end++;

					if(raw[i]==93)
						break;

					Map ref=new HashMap();
					i=readDictionary(objectRef,1,ref,i+2,raw, textFields,end);
					i--;
					i--;

					String fdfkey=null,value="";
//					byte[] pdfFile=getByteTextStringValue(ref.get("T"),ref);
//					if(pdfFile!=null)
//						fdfkey=PdfReader.getTextString(pdfFile);
//
//					pdfFile=getByteTextStringValue(ref.get("V"),ref);
//					if(pdfFile!=null)
//						value=PdfReader.getTextString(pdfFile);
//
//					if(fdfkey!=null)
//						fdfTable.put(fdfkey,value);
				}

				rootObject.put(PDFkey,fdfTable);

				keyLength=0;

			}else if(raw[i]==91){ //read in [value] excluding any returns

				int startValue=i,opPointer=0;
				boolean inComment=false,convertToHex=false;
				int squareCount=0,count=0;
				char next=' ',last=' ';
				boolean containsIndexKeyword=false;

				while(raw[i]==32){ //ignore any spaces
					opPointer++;
					i++;
				}

				while(true){
					if(raw[i]==92) //ignore any escapes
						i++;

					//check if it contains the word /Indexed
					if((opPointer>7)&&(!containsIndexKeyword)&&(raw[i-7]=='/')&&(raw[i-6]=='I')&&
							(raw[i-5]=='n')&&(raw[i-4]=='d')&&(raw[i-3]=='e')&&(raw[i-2]=='x')&&
							(raw[i-1]=='e')&&(raw[i]=='d')){
						containsIndexKeyword=true;
					}

					//track [/Indexed /xx ()] with binary values in ()
					if((raw[i]==40)&&(raw[i-1]!=92)&&(containsIndexKeyword)){
						convertToHex=true;

						opPointer=opPointer+2;
					}else if(convertToHex){
						if((raw[i]==41)&&(raw[i-1]!=92)){
							opPointer++;

							convertToHex=false;

						}else{
							String hex_value = Integer.toHexString((raw[i]) & 255);
							//pad with 0 if required
							if (hex_value.length() < 2)  //add in char
								opPointer++;

							opPointer=opPointer+hex_value.length();
						}
					}else if(!inComment){
						if((raw[i]==13)||(raw[i]==10)){ //add in char
							opPointer++;
						}else{
							next=(char)raw[i];

							//put space in [/ASCII85Decode/FlateDecode]
							if((next=='/')&&(last!=' ')) //add in char
								opPointer++;

							if((next!=' ')&&(last==')')) //put space in [()99 0 R]
								opPointer++;

							opPointer++;
							last=next;
						}
					}

					if((raw[i-1]!=92)||((raw[i-1]==92)&&(raw[i-2]==92)&&(raw[i-3]!=92))){ //allow for escape and track [] and ()
						if(raw[i]==40)
							count++;
						else if(raw[i]==41)
							count--;
						if(count==0){
							if(raw[i]==91)
								squareCount++;
							else if(raw[i]==93)
								squareCount--;
						}
					}

					if((squareCount==0)&&(raw[i-1]!=92)&&(raw[i]==93))
						break;

					i++;

					//System.err.println(count++);

					if((raw[i]==37)&&(raw[i-1]!=92)&&(squareCount==0)) //ignore comments %
						inComment=true;

				}

				// now extract char array at correct size

				char[] value=new char[opPointer*2];
				int pt=0;
				i=startValue; //move pointer back to start

				//ignore any spaces
				while(raw[i]==32){
					value[pt]=(char)raw[i];
					pt++;
					i++;
				}

				//reset defaults
				inComment=false;
				convertToHex=false;
				squareCount=0;
				count=0;
				next=' ';
				last=' ';

				while(true){

					//if(containsIndexKeyword)
					//System.out.println(i+" "+( raw[i] & 255)+" "+(char)(raw[i] & 255)+" "+Integer.toHexString((raw[i]) & 255));

					//					check if it contains the word /Indexed
					if((i>7)&&(!containsIndexKeyword)&&(raw[i-7]=='/')&&(raw[i-6]=='I')&&
							(raw[i-5]=='n')&&(raw[i-4]=='d')&&(raw[i-3]=='e')&&(raw[i-2]=='x')&&
							(raw[i-1]=='e')&&(raw[i]=='d')){
						containsIndexKeyword=true;
					}

					//track [/Indexed /xx ()] with binary values in ()
					if((raw[i]==40)&(raw[i-1]!=92)&&(containsIndexKeyword)){

						//find end
						int start=i+1,end=i;
						while(end<raw.length){
							end++;
							if(raw[end]==')' & (raw[end-1]!=92 ||(raw[end-1]==92 && raw[end-2]==92)))
								break;

						}

						//handle escape chars
						int length=end-start;
						byte[] fieldBytes=new byte[length];

						for(int a=0;a<length;a++){

							if(start==end)
								break;

							byte b=raw[start];
							if(b!=92){
								fieldBytes[a]=b;
							}else{

								start++;

								if(raw[start]=='b')
									fieldBytes[a]='\b';
								else if(raw[start]=='n')
									fieldBytes[a]='\n';
								else if(raw[start]=='t')
									fieldBytes[a]='\t';
								else if(raw[start]=='r')
									fieldBytes[a]='\r';
								else if(raw[start]=='f')
									fieldBytes[a]='\f';
								else if(raw[start]=='\\')
									fieldBytes[a]='\\';
								else if(Character.isDigit((char) raw[start])){ //octal

									StringBuffer octal=new StringBuffer(3);
									for(int ii=0;ii<3;ii++){

										//allow for less than 3 digits
										if(raw[start]==92 || raw[start]==')')
											break;

										octal.append((char)raw[start]);
										start++;
									}
									start--;
									//move back 1
									fieldBytes[a]=(byte) Integer.parseInt(octal.toString(),8);

								}else{
									//start--;
									fieldBytes[a]=raw[start];
								}
							}

							start++;
						}

						//handle encryption
						try {
							fieldBytes=decrypt(fieldBytes,objectRef, false,null,false,false);
						} catch (PdfSecurityException e) {
							e.printStackTrace();
						}

						// add to data as hex stream

						//start
						value[pt]=' ';
						pt++;
						value[pt]='<';
						pt++;

						//data
						for(int jj=0;jj<length;jj++){
							String hex_value = Integer.toHexString((fieldBytes[jj] & 255));

							if (hex_value.length() < 2){ //pad with 0 if required
								value[pt]='0';
								pt++;
							}

							int hCount=hex_value.length();
							for(int j=0; j<hCount;j++){
								value[pt]=hex_value.charAt(j);
								pt++;
							}
						}

						//end
						value[pt]='>';
						pt++;

					}else if(!inComment){
						if((raw[i]==13)|(raw[i]==10)){
							value[pt]=' ';
							pt++;
						}else{
							next=(char)raw[i];
							if((next=='/')&&(last!=' ')){ //put space in [/ASCII85Decode/FlateDecode]
								value[pt]=' ';
								pt++;
							}
							if((next!=' ')&&(last==')')){ //put space in [()99 0 R]
								value[pt]=' ';
								pt++;
							}
							value[pt]=next;
							pt++;

							last=next;
						}
					}

					if((raw[i-1]!=92)|((raw[i-1]==92)&&(raw[i-2]==92)&&(raw[i-3]!=92))){ //allow for escape and track [] and ()
						if(raw[i]==40)
							count++;
						else if(raw[i]==41)
							count--;
						if(count==0){
							if(raw[i]==91)
								squareCount++;
							else if(raw[i]==93)
								squareCount--;
						}
					}

					if((squareCount==0)&&(raw[i-1]!=92)&(raw[i]==93))
						break;

					i++;

					if((raw[i]==37)&&(raw[i-1]!=92)&&(squareCount==0)) //ignore comments %
						inComment=true;

				}


				//save pair and reset
				String finalOp= String.copyValueOf(value,0,pt);
				if(!finalOp.equals("null"))
					rootObject.put(PDFkey,finalOp);

				if(debug)
					System.err.println(level+" *1 "+PDFkey+"=="+finalOp+ '<');

				keyLength=0;

			}else if ((raw[i] != 62)&&(raw[i] != 60)&&(keyLength>0)){

				boolean inComment=false;
				int startValue=i,opPointer=0;

				//calculate size of next value
				while(true){

					if((raw[i]!=13)&&(raw[i]!=9)&&(raw[i]!=10)&&(!inComment)) //add in char
						opPointer++;

					if((raw[i+1]==47)||((raw[i]!=62)&&(raw[i+1]==62)))
						break;

					i++;

					if((raw[i]==37)&&(raw[i-1]!=92)) //ignore comments %
						inComment=true;
				}

				//lose spaces at end, save pair and reset
				while((opPointer>0)&&((raw[startValue+opPointer-1]==32)||(raw[startValue+opPointer-1]==10)||(raw[startValue+opPointer-1]==13)||
						(raw[startValue+opPointer-1]==9)))
					opPointer--;

				//get value
				char[] value=new char[opPointer];
				opPointer--;
				int p=0;
				while(true){

					if((raw[startValue]!=13)&&(raw[startValue]!=9)&&(raw[startValue]!=10)){
						value[p]=(char)raw[startValue];
						p++;
					}

					startValue++;
					if(p>opPointer)
						break;
				}

				String finalOp=String.copyValueOf(value,0,p);

				if(!finalOp.equals("null"))
					rootObject.put(PDFkey,finalOp);

				if(debug)
					System.err.println(level+"*2 "+PDFkey+"=="+finalOp+ '<');
				keyLength=0;

			}
		}

		if(debug)
			System.err.println("=====Dictionary read");

		return i;

	}/**/

	/**
	 * @param pdfObject
	 * @param objectRef
	 * @param i
	 * @param raw
	 * @param debugFastCode
	 * @param PDFkeyInt     - -1 will store in pdfObject directly, not as separate object
	 * @param PDFkey
	 * @param paddingString
	 * @return
	 */
	private int readDictionaryFromRefOrDirect(int id, PdfObject pdfObject,
			String objectRef, int i, byte[] raw,
			boolean debugFastCode,
			int PDFkeyInt, Object PDFkey,
			String paddingString) {

        //allow for subrecursion on null
        int ii2=-1;

        readDictionaryFromRefOrDirect:
			while (true) {

				int keyLength;
				int keyStart;
				int possibleArrayStart = -1;

                //@speed - find end so we can ignore once no longer reading into map as well
				//and skip to end of object
				//allow for [ref] or [<< >>] at top level (may be followed by gap)
				//good example is /PDFdata/baseline_screens/docusign/test3 _ Residential Purchase and Sale Agreement - 6-03.pdf
				while (raw[i] == 91 || raw[i] == 32 || raw[i] == 13 || raw[i] == 10) {

					if (raw[i] == 91) //track incase /Mask [19 19]
						possibleArrayStart = i;

					i++;
				}

				//some items like MAsk can be [19 19] or stream
				//and colorspace is law unto itself
				if (PDFkeyInt == PdfDictionary.ColorSpace || id == PdfDictionary.ColorSpace || pdfObject.getPDFkeyInt()==PdfDictionary.ColorSpace) { //very specific type of object

					PdfObject ColorSpace = (PdfObject) cachedColorspaces.get(objectRef);

                    if(ColorSpace==null)
                        ColorSpace = (PdfObject) cachedColorspaces.get(pdfObject.getObjectRefAsString());


                    if(debugFastCode)
                            System.out.println("Colorspace="+ColorSpace+" objectRef="+objectRef+" ref="+pdfObject.getObjectRefAsString());

					//read the base value (2 cases - Colorspace pairs or value in XObject
					if (ColorSpace == null && !pdfObject.ignoreRecursion()) {

						if (pdfObject.getObjectType() == PdfDictionary.ColorSpace) {//pairs

							return handleColorSpaces(pdfObject, i, raw, debugFastCode, paddingString);

						}else{ //Direct object in XObject
                            
                            //can be called in 2 diff ways and this is difference
                            boolean isKey=raw[i]=='/';
                            if(isKey)
                                ColorSpace = new ColorSpaceObject(objectRef);
                            else
                                ColorSpace = new ColorSpaceObject(-1,0);
							
							pdfObject.setDictionary(PdfDictionary.ColorSpace, ColorSpace);

							//cachedColorspaces.put(objectRef,ColorSpace);
							return handleColorSpaces(ColorSpace, i, raw, debugFastCode, paddingString);
						}
					} else {//roll on and ignore

						//System.out.println("Cached--------------------");
						pdfObject.setDictionary(PdfDictionary.ColorSpace, ColorSpace);

						if (raw[i] == '<' && raw[i + 1] == '<') {
							i = i + 2;
							int level = 1;

							while (level > 0) {
								//   System.out.print((char)data[start]);
								if (raw[i] == '<' && raw[i + 1] == '<') {
									i = i + 2;
									level++;
								} else if (raw[i] == '>' && raw[i + 1] == '>') {
									i = i + 2;
									level--;
								} else
									i++;
							}

						} else { //must be a ref
							while (raw[i] != 'R')
								i++;

							i++;
						}

						return i;
					}

				} else if (possibleArrayStart != -1 && (PDFkeyInt == PdfDictionary.Mask || PDFkeyInt == PdfDictionary.TR ||
						PDFkeyInt == PdfDictionary.OpenAction)) {

					//find end
					int endPoint = possibleArrayStart;
					while (raw[endPoint] != ']' && endPoint <= raw.length)
						endPoint++;

					//convert data to new Dictionary object and store
					PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, null, pdfObject.getObjectType(), pdfObject.getID());
					valueObj.setID(PDFkeyInt);
					pdfObject.setDictionary(PDFkeyInt, valueObj);
					valueObj.ignoreRecursion(pdfObject.ignoreRecursion());

					int type = PdfDictionary.VALUE_IS_INT_ARRAY;
					if (PDFkeyInt == PdfDictionary.TR)
						type = PdfDictionary.VALUE_IS_KEY_ARRAY;


					i = readArray(pdfObject.ignoreRecursion(), possibleArrayStart, endPoint,
							type, raw, objectRef,
							valueObj,
							PDFkeyInt, debugFastCode, paddingString, null, -1);

					if (debugFastCode)
						System.out.println(paddingString + "Set Array " + PDFkey + " for Mask or TR " + valueObj + " in " + pdfObject);

					//rollon
					return i;
				}

				if (raw[i] == '%') { // if %comment roll onto next line
					while (true) {
						i++;
						if (raw[i] == 13 || raw[i] == 10)
							break;

					}

					//and lose space after
					while (raw[i] == 91 || raw[i] == 32 || raw[i] == 13 || raw[i] == 10)
						i++;
				}

				if (raw[i] == 60) { //[<<data inside brackets>>]


					i = convertDirectDictionaryToObject(pdfObject, objectRef, i, raw,
							debugFastCode, PDFkeyInt, paddingString);


				} else if (raw[i] == 47) { //direct value such as /DeviceGray

					//System.out.println(PDFkeyInt+" "+pdfObject);
					i++;
					keyStart = i;
					while (i < raw.length && raw[i] != 32 && raw[i] != 10 && raw[i] != 13) {
						//System.out.println(raw[i]+" "+(char)raw[i]+" "+i+"/"+raw.length);
						i++;
					}
					//convert data to new Dictionary object
					//PdfObject valueObj= PdfObjectFactory.createObject(PDFkeyInt,null);

					//store value
					int constant = pdfObject.setConstant(PDFkeyInt, keyStart, i - keyStart, raw);
					//pdfObject.setDictionary(PDFkeyInt,valueObj);

					if (debugFastCode)
						System.out.println(paddingString + "1.Set object " + pdfObject + " to " + constant);

				} else { // ref or [ref]

                    int j = i, ref, generation;
					byte[] data = raw;

					while (true) {

						//allow for [ref] at top level (may be followed by gap
						while (data[j] == 91 || data[j] == 32 || data[j] == 13 || data[j] == 10)
							j++;

						// trap nulls  as well
						boolean hasNull = false;

						while (true) {

							//trap null arrays ie [null null]
							if (hasNull && data[j] == ']')
								return j;

							/**
							 * get object ref
							 */
							keyStart = j;
							//move cursor to end of reference
							while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {

								//trap null arrays ie [null null]
								if (hasNull && data[j] == ']')
									return j;

								j++;
							}

							ref = parseInt(keyStart, j, data);

							//move cursor to start of generation or next value
							while (data[j] == 10 || data[j] == 13 || data[j] == 32)// || data[j]==47 || data[j]==60)
								j++;

                            //handle nulls
							if (ref != 69560)
								break; //not null
							else {
								hasNull = true;
								if (data[j] == '<'){ // /DecodeParms [ null << /K -1 /Columns 1778 >>  ] ignore null and jump down to enclosed Dictionary

                                    ii2=i;
                                    raw=data;
                                    i = j;
									continue readDictionaryFromRefOrDirect;
								}
							}
						}

						/**
						 * get generation number
						 */
						keyStart = j;
						//move cursor to end of reference
						while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62)
							j++;

						generation = parseInt(keyStart, j, data);

                        /**
						 * check R at end of reference and abort if wrong
						 */
						//move cursor to start of R
						while (data[j] == 10 || data[j] == 13 || data[j] == 32 || data[j] == 47 || data[j] == 60)
							j++;

						if (data[j] != 82) { //we are expecting R to end ref
							throw new RuntimeException("ref=" + ref + " gen=" + ref + " 1. Unexpected value " + data[j] + " in file - please send to IDRsolutions for analysis char=" + (char) data[j]);
                        }

                        //read the Dictionary data
						data = readObjectAsByteArray(pdfObject, objectRef, isCompressed(ref, generation), ref, generation);

						//disregard corrputed data from start of file
						if(data!=null && data.length>4 && data[0]=='%' && data[1]=='P' && data[2]=='D' && data[3]=='F')
							data=null;

						if (data == null) {
							if (debugFastCode)
								System.out.println(paddingString + "null data");

							break;
						}

						/**
						 * get not indirect and exit if not
						 */
						int j2 = 0;

						//allow for [91 0 r]
						if (data[j2] != '[') {

							while (j2 < 3 || (j2 > 2 && data[j2 - 1] != 106 && data[j2 - 2] != 98 && data[j2 - 3] != 111)) {

								//allow for /None as value
								if (data[j2] == '/')
									break;
								j2++;
							}

							//skip any spaces
							while (data[j2] != 91 && (data[j2] == 10 || data[j2] == 13 || data[j2] == 32))// || data[j]==47 || data[j]==60)
								j2++;
						}

						//if indirect, round we go again
						if (data[j2] != 91) {
							j = 0;
							break;
						}


						j = j2;
					}

					//allow for no data found (ie /PDFdata/baseline_screens/debug/hp_broken_file.pdf)
					if (data != null) {

						/**
						 * get id from stream
						 */
						//skip any spaces
						while (data[j] == 10 || data[j] == 13 || data[j] == 32)// || data[j]==47 || data[j]==60)
							j++;

						boolean isMissingValue = j<raw.length && raw[j] == '<';

						if (isMissingValue) { //check not <</Last
							//find first valid char
							int xx = j;
							while (xx < data.length && (raw[xx] == '<' || raw[xx] == 10 || raw[xx] == 13 || raw[xx] == 32))
								xx++;

							if (raw[xx] == '/')
								isMissingValue = false;
						}

						if (isMissingValue) { //missing value at start for some reason

							/**
							 * get object ref
							 */
							keyStart = j;
							//move cursor to end of reference
							while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62)
								j++;

							ref = parseInt(keyStart, j, data);

							//move cursor to start of generation
							while (data[j] == 10 || data[j] == 13 || data[j] == 32 || data[j] == 47 || data[j] == 60)
								j++;

							/**
							 * get generation number
							 */
							keyStart = j;
							//move cursor to end of reference
							while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62)
								j++;

							generation = parseInt(keyStart, j, data);

							//lose obj at start
							while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111) {

								if (data[j] == '<')
									break;

								j++;
							}

						}

						//skip any spaces
						while (data[j] == 10 || data[j] == 13 || data[j] == 32 || data[j] == 9)// || data[j]==47 || data[j]==60)
							j++;

						//move to start of Dict values
						if (data[0] != 60)
							while (data[j] != 60 && data[j + 1] != 60) {

								//allow for null object
								if(data[j]=='n' && data[j+1]=='u' && data[j+2]=='l' && data[j+3]=='l' )
									return i;
								
								//allow for Direct value ie 2 0 obj /WinAnsiEncoding
								if (data[j] == 47)
									break;

								//allow for textStream (text)
								if(data[j]=='('){
									j = readTextStream(pdfObject, objectRef, j, data,
											paddingString, debugFastCode, PDFkeyInt,
											true);
									break;
									//return j;
								}

								j++;
							}

						if (data[j] == 47) {
							j++; //roll on past /

							keyStart = j;
							keyLength = 0;

							//move cursor to end of text
							while (data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
								j++;
								keyLength++;

							}

							i--;// move back so loop works

							if (PDFkeyInt == -1) {
								//store value directly
								int constant = pdfObject.setConstant(PDFkeyInt, keyStart, keyLength, data);

								if (debugFastCode)
									System.out.println(paddingString + "Set object Constant directly to " + constant);
							} else {
								//convert data to new Dictionary object
								PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, null,pdfObject.getObjectType(), pdfObject.getID());
								valueObj.setID(PDFkeyInt);
								//store value
								int constant = valueObj.setConstant(PDFkeyInt, keyStart, keyLength, data);
								pdfObject.setDictionary(PDFkeyInt, valueObj);

								if (debugFastCode)
									System.out.println(paddingString + "Set object to Constant " + PDFkey + " in " + valueObj + " to " + constant);
							}
						} else {

							//convert data to new Dictionary object
							PdfObject valueObj = null;
							if (PDFkeyInt == -1)
								valueObj = pdfObject;
							else {
								valueObj = ObjectFactory.createObject(PDFkeyInt, ref, generation, pdfObject.getObjectType());
								valueObj.setID(PDFkeyInt);
								// System.out.println(valueObj+" "+pdfObject.getObjectType()+" PDFkeyInt="+PDFkeyInt);
								if (PDFkeyInt != PdfDictionary.Resources)
									valueObj.ignoreRecursion(pdfObject.ignoreRecursion());
							}

							if (debugFastCode) {
								//throw new RuntimeException("xx");
								//System.out.println(paddingString+"X1------------------------"+ref+" "+generation+" R >>>>Converting to Dictionary "+valueObj);
							}

							//if(ObjLengthTable==null || this.ObjLengthTable[ref]>0){ //check it exists
							j = readDictionaryAsObject(valueObj, ref + " " + generation + " R", j, data, data.length, paddingString, false);
							/**
							 * look for stream afterwards
							 */
							//@speed - do I need this?
							//readStreamIntoObject(pdfObject, debugFastCode, j, data, valueObj);

							if (debugFastCode)
								System.out.println(paddingString + "------------------------<<<<Saving Dictionary " + valueObj + " for key " + PDFkey + " into " + pdfObject);

							//store value
							if (PDFkeyInt != -1)
								pdfObject.setDictionary(PDFkeyInt, valueObj);

						}
					}
				}

                if(ii2!=-1)
                    return ii2;
                else
                    return i;
			}
	}


	private int convertDirectDictionaryToObject(PdfObject pdfObject,
			String objectRef, int i, byte[] raw, boolean debugFastCode,
			int PDFkeyInt, String paddingString) {
		//convert data to new Dictionary object
		PdfObject valueObj=null;

		if(PDFkeyInt==-1){
			valueObj=pdfObject;

            //if only 1 item use that ref not parent and indirect (ie <</Metadata 38 0 R>>)
            int objCount=0, refStarts=-1,refEnds=-1;
            if(raw[0]=='<'){
                for(int ii=0;ii<raw.length;ii++){

                    //count keys
                    if(raw[ii]=='/')
                        objCount++;
                    //find start of ref
                    if(objCount==1){
                        if(refStarts==-1){
                            if(raw[ii]>'0' && raw[ii]<'9')
                                refStarts=ii;
                        }else{
                            if(raw[ii]=='R')
                                refEnds=ii+1;
                        }
                    }
                }

                if(objCount==1 && refStarts!=-1 && refEnds!=-1){
                    objectRef=new String(raw,refStarts,refEnds-refStarts);
                    valueObj.setRef(objectRef);
                }
            }

        }else{
			valueObj= ObjectFactory.createObject(PDFkeyInt,objectRef, pdfObject.getObjectType(), pdfObject.getID());
			valueObj.setID(PDFkeyInt);
			if(debugFastCode)
				System.out.println("valueObj="+valueObj+" "+pdfObject+" PDFkeyInt="+PDFkeyInt+" "+pdfObject.getID()+" "+pdfObject.getParentID());
		}
        

		if(debugFastCode)
			System.out.println(paddingString+"Reading [<<data>>] to "+valueObj+" into "+pdfObject+" i="+i);

		i=readDictionaryAsObject( valueObj, objectRef, i, raw, raw.length, paddingString, false);

		//needed to ensure >>>> works
		if(i<raw.length && raw[i]=='>')
			i--;

		if(debugFastCode){
			System.out.println(paddingString+"data into pdfObject="+pdfObject+" i="+i);
		}

		//store value (already set above for -1
		if(PDFkeyInt!=-1)
			pdfObject.setDictionary(PDFkeyInt,valueObj);

		//roll on to end
		int count=raw.length;
		while( i<count-1 && raw[i]==62  && raw[i+1]==62){ //
			i++;
			if(i+1<raw.length && raw[i+1]==62) //allow for >>>>
				break;
		}
		return i;
	}
	/**
	 * if pairs is -1 returns number of pairs
	 * otherwise sets pairs and returns point reached in stream
	 */
	private int readKeyPairs(int id,byte[] data, int j,int pairs, PdfObject pdfObject, String paddingString) {

		final boolean debug=false;

		int start=j,level=1;

		int numberOfPairs=pairs;

		if(debug){
			System.out.println("count="+pairs+"============================================\n");
			for(int aa=j;aa<data.length;aa++){
				System.out.print((char)data[aa]);

				if(aa>5 && data[aa-5]=='s' && data[aa-4]=='t' && data[aa-3]=='r'&& data[aa-2]=='e' && data[aa-1]=='a' && data[aa]=='m')
					aa=data.length;
			}
			System.out.println("\n============================================");
		}

		//same routine used to count first and then fill with values
		boolean isCountOnly=false,skipToEnd=false;
		byte[][] keys=null,values=null;
		PdfObject[] objs=null;

		if(pairs==-1){
			isCountOnly=true;
		}else if(pairs==-2){
			isCountOnly=true;
			skipToEnd=true;
		}else{
			keys=new byte[numberOfPairs][];
			values=new byte[numberOfPairs][];
			objs=new PdfObject[numberOfPairs];

			if(debug)
				System.out.println("Loading "+numberOfPairs+" pairs");
		}
		pairs=0;

		while(true){

			//move cursor to start of text
			while(data[start]==9 || data[start]==10 || data[start]==13 || data[start]==32 || data[start]==60)
				start++;

            //allow for comment
            if(data[start]==37){
                while(data[start]!=10 && data[start]!=13){
                    //System.out.println(data[start]+" "+(char)data[start]);
                    start++;
                }

                //move cursor to start of text
                while(data[start]==9 || data[start]==10 || data[start]==13 || data[start]==32 || data[start]==60)
				start++;
            }

			//exit at end
			if(data[start]==62)
				break;

			//count token or tell user problem
			if(data[start]==47){
				pairs++;
				start++;
			}else
				throw new RuntimeException("Unexpected value "+data[start]+" - not key pair");

			//read token key and save if on second run
			int tokenStart=start;
			while(data[start]!=32 && data[start]!=10 && data[start]!=13 && data[start]!='[' && data[start]!='<' && data[start]!='/')
				start++;

			int tokenLength=start-tokenStart;

			byte[] tokenKey=new byte[tokenLength];
			System.arraycopy(data, tokenStart, tokenKey, 0, tokenLength);

			if(!isCountOnly) //pairs already rolled on so needs to be 1 less
				keys[pairs-1]=tokenKey;

			//now skip any spaces to key or text
			while(data[start]==10 || data[start]==13 || data[start]==32)
				start++;

			boolean isDirect=data[start]==60 || data[start]=='[' || data[start]=='/';

			byte[] dictData=null;

			if(debug)
				System.out.println("token="+new String(tokenKey)+" isDirect "+isDirect);

			if(isDirect){
				//get to start at <<
				while(data[start-1]!='<' && data[start]!='<' && data[start]!='[' && data[start]!='/')
					start++;

				int streamStart=start;

				//find end
				boolean isObject=true;

				if(data[start]=='<'){
					start=start+2;
					level=1;

					while(level>0){
						//   System.out.print((char)data[start]);
						if(data[start]=='<' && data[start+1]=='<'){
							start=start+2;
							level++;
						}else if(data[start]=='>' && data[start+1]=='>'){
							start=start+2;
							level--;
						}else
							start++;
					}

					//System.out.println("\n<---------------"+start);

					//if(data[start]=='>' && data[start+1]=='>')
					//start=start+2;
				}else if(data[start]=='['){

					level=1;
					start++;

					boolean inStream=false;

					while(level>0){

						//allow for streams
						if(!inStream && data[start]=='(')
							inStream=true;
						else if(inStream && data[start]==')' && (data[start-1]!='\\' || data[start-2]=='\\' ))
							inStream=false;

						//System.out.println((char)data[start]);

						if(!inStream){
							if(data[start]=='[')
								level++;
							else if(data[start]==']')
								level--;
						}

						start++;
					}

					isObject=false;
				}else if(data[start]=='/'){
					start++;
					while(data[start]!='/' && data[start]!=10 && data[start]!=13 && data[start]!=32){
						start++;

                        if(start<data.length-1 && data[start]=='>' && data[start+1]=='>')
                            break;
                    }
				}

				if(!isCountOnly){
					int len=start-streamStart;
					dictData=new byte[len];
					System.arraycopy(data, streamStart, dictData, 0, len);
					//pairs already rolled on so needs to be 1 less
					values[pairs-1]=dictData;

					String ref=pdfObject.getObjectRefAsString();

					//@speed - will probably need to change as we add more items

					if(pdfObject.getObjectType()==PdfDictionary.ColorSpace){

						if(isObject){
							
							
							handleColorSpaces(pdfObject, 0,  dictData, debug, paddingString+"    ");
							objs[pairs-1]=pdfObject;
						}else{
							ColorSpaceObject colObject=new ColorSpaceObject(ref);
							
							if(isDirect)
								colObject.setRef(-1,0);
								
							handleColorSpaces(colObject, 0,  dictData, debug, paddingString+"    ");
							objs[pairs-1]=colObject;
						}

						//handleColorSpaces(-1, valueObj,ref, 0, dictData,debug, -1,null, paddingString);
					}else if(isObject){

                        PdfObject valueObj=ObjectFactory.createObject(id, ref, pdfObject.getObjectType(), pdfObject.getID());
						valueObj.setID(id);
						readDictionaryFromRefOrDirect(id, valueObj,ref, 0, dictData,false, -1,null, paddingString);
                        objs[pairs-1]=valueObj;
					}

					//lose >> at end
					//while(start<data.length && data[start-1]!='>' && data[start]!='>')
					//	start++;

				}

			}else{ //its 50 0 R

                //number
				int refStart=start, keyStart2=start,keyLength2=0;
				while(data[start]!=10 && data[start]!=13 && data[start]!=32 && data[start]!=47 &&
						data[start]!=60 && data[start]!=62){
					start++;
					keyLength2++;
				}
				int number=parseInt(keyStart2,start, data);

				//generation
				while(data[start]==10 || data[start]==13 || data[start]==32 || data[start]==47 || data[start]==60)
					start++;

				keyStart2=start;
				//move cursor to end of reference
				while(data[start]!=10 && data[start]!=13 && data[start]!=32 &&
						data[start]!=47 && data[start]!=60 && data[start]!=62)
					start++;

				int generation=parseInt(keyStart2,start, data);

				//move cursor to start of R
				while(data[start]==10 || data[start]==13 || data[start]==32 || data[start]==47 || data[start]==60)
					start++;

				if(data[start]!=82) //we are expecting R to end ref
					throw new RuntimeException("3. Unexpected value in file - please send to IDRsolutions for analysis");

				start++; //roll past

				if(debug)
					System.out.println("Data in object="+number+" "+generation+" R");

				//read the Dictionary data
				if(!isCountOnly){

                    if(PdfDictionary.getKeyType(id, pdfObject.getObjectType())==PdfDictionary.VALUE_IS_UNREAD_DICTIONARY){

                        String ref=new String(data, refStart,start-refStart);

                        PdfObject valueObj=ObjectFactory.createObject(id, ref, pdfObject.getObjectType(), pdfObject.getID());
                        
                        valueObj.setStatus(PdfObject.UNDECODED_REF);
                        valueObj.setUnresolvedData(ref.getBytes(),id);

						objs[pairs-1]=valueObj;

                    }else{

                        byte[] rawDictData=readObjectAsByteArray(pdfObject, "", isCompressed(number,generation),number,generation);

                        if(debug){
                            System.out.println("============================================\n");
                            for(int aa=0;aa<rawDictData.length;aa++){
                                System.out.print((char)rawDictData[aa]);

                                if(aa>5 && rawDictData[aa-5]=='s' && rawDictData[aa-4]=='t' && rawDictData[aa-3]=='r'&& rawDictData[aa-2]=='e' && rawDictData[aa-1]=='a' && rawDictData[aa]=='m')
                                    aa=rawDictData.length;
                            }
                            System.out.println("\n============================================");
                        }
                        //cleanup
                        //lose obj at start
                        int jj=0;

                        while(jj<3 ||(rawDictData[jj-1]!=106 && rawDictData[jj-2]!=98 && rawDictData[jj-3]!=111)){

                            if(rawDictData[jj]=='/' || rawDictData[jj]=='[' || rawDictData[jj]=='<')
                                break;

                            jj++;

                            if(jj==rawDictData.length){
                                jj=0;
                                break;
                            }
                        }

                        //skip any spaces after
                        while(rawDictData[jj]==10 || rawDictData[jj]==13 || rawDictData[jj]==32)// || data[j]==47 || data[j]==60)
                            jj++;

                        int len=rawDictData.length-jj;
                        dictData=new byte[len];
                        System.arraycopy(rawDictData, jj, dictData, 0, len);
                        //pairs already rolled on so needs to be 1 less
                        values[pairs-1]=dictData;

                        String ref=number+" "+generation+" R";//pdfObject.getObjectRefAsString();

                        if(pdfObject.getObjectType()==PdfDictionary.Font && id==PdfDictionary.Font){//last condition for CharProcs
                            objs[pairs-1]=null;
                            values[pairs-1]=ref.getBytes();
                        }else if(pdfObject.getObjectType()==PdfDictionary.XObject){
                            //intel Unimplemented pattern type 0 in file
                            PdfObject valueObj=ObjectFactory.createObject(id, ref, PdfDictionary.XObject, PdfDictionary.XObject);
                            valueObj.setStatus(PdfObject.UNDECODED_REF);
                            valueObj.setUnresolvedData(ref.getBytes(),id);

                            objs[pairs-1]=valueObj;    
                        }else{

                            //@speed - will probably need to change as we add more items
                            PdfObject valueObj=ObjectFactory.createObject(id, ref, pdfObject.getObjectType(), pdfObject.getID());
                            valueObj.setID(id);
                            if(debug){
                                System.out.println(ref+" ABOUT TO READ OBJ for "+valueObj+" "+pdfObject);

                                System.out.println("-------------------\n");
                                for(int aa=0;aa<dictData.length;aa++){
                                    System.out.print((char)dictData[aa]);

                                    if(aa>5 && dictData[aa-5]=='s' && dictData[aa-4]=='t' && dictData[aa-3]=='r'&& dictData[aa-2]=='e' && dictData[aa-1]=='a' && dictData[aa]=='m')
                                        aa=dictData.length;
                                }
                                System.out.println("\n-------------------");
                            }

                            if(valueObj.getObjectType()==PdfDictionary.ColorSpace)
                                handleColorSpaces(valueObj, 0,  dictData, debug, paddingString+"    ");
                            else
                                readDictionaryFromRefOrDirect(id, valueObj,ref, 0, dictData,debug, -1,null, paddingString);

                            objs[pairs-1]=valueObj;

                        }
                    }
                }
			}
		}


		if(!isCountOnly)
			pdfObject.setDictionaryPairs(keys,values,objs);

		if(debug)
			System.out.println("done=============================================");

		if(skipToEnd || !isCountOnly)
			return start;
		else
			return pairs;

	}

	private void readStreamIntoObject(PdfObject pdfObject,
			boolean debugFastCode, int j, byte[] data, PdfObject valueObj, String paddingString) {

		int count=data.length;

		if(debugFastCode)
			System.out.println(paddingString+"Looking for stream");

		byte[] stream=null;

		//may need optimising
		//debug - @speed
		for(int a=j;a<count;a++){
			if ((data[a] == 115)&& (data[a + 1] == 116)&& (data[a + 2] == 114)&&
					(data[a + 3] == 101)&& (data[a + 4] == 97)&& (data[a + 5] == 109)) {


				//ignore these characters and first return
				a = a + 6;

                while(data[a]==32)
                a++;

				if (data[a] == 13 && data[a+1] == 10) //allow for double linefeed
					a=a+2;
				else if(data[a]==10 || data[a]==13)
					a++;

				int start = a;

				a--; //move pointer back 1 to allow for zero length stream

				/**
				 * if Length set and valid use it
				 */
				int streamLength=0;
				int setStreamLength=pdfObject.getInt(PdfDictionary.Length);

				if(debugFastCode)
					System.out.println(paddingString+"setStreamLength="+setStreamLength);

				if(setStreamLength!=-1){

					streamLength=setStreamLength;

					//System.out.println("1.streamLength="+streamLength);

					a=start+streamLength;

					if((a<count) && data[a]==13 && (a+1<count) && data[a+1]==10)
						a=a+2;

					//check validity
					if (count>(a+9) && data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 &&
							data[a + 3] == 115 && data[a + 4] == 116
							&& data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109){

					}else{
						boolean	isValid=false;
						int current=a;
						//check forwards
						if(a<count){
							while(true){
								a++;
								if(isValid || a==count)
									break;

								if (data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 && data[a + 3] == 115 && data[a + 4] == 116
										&& data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109){

									streamLength=a-start;
									isValid=true;
								}
							}
						}

						if(!isValid){
							a=current;
							if(a>count)
								a=count;
							//check backwords
							while(true){
								a--;
								if(isValid || a<0)
									break;

								if (data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 && data[a + 3] == 115 && data[a + 4] == 116
										&& data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109){
									streamLength=a-start;
									isValid=true;
								}
							}
						}

						if(!isValid)
							a=current;
					}

				}else{

					/**workout length and check if length set*/
					int end;

					while (true) { //find end

						a++;

						if(a==count)
							break;
						if (data[a] == 101 && data[a + 1] == 110 && data[a + 2] == 100 && data[a + 3] == 115 && data[a + 4] == 116
								&& data[a + 5] == 114 && data[a + 6] == 101 && data[a + 7] == 97 && data[a + 8] == 109)
							break;

					}

					end=a-1;

					if((end>start))
						streamLength=end-start+1;
				}

				//lose trailing 10s or 13s
				if(streamLength>1){
					int ptr=start+streamLength-1;
					if(ptr<data.length && ptr>0 && (data[ptr]==10 || data[ptr]==13)){
						streamLength--;
						ptr--;
					}
				}


				/**
				 * read stream into object from memory
				 */
				if(start+streamLength>count)
                    streamLength=count-start;

                //@speed - switch off and investigate
                if(streamLength<0)
                    return;

                if(streamLength<0)
                    throw new RuntimeException("Negative stream length "+streamLength+" start="+start+" count="+count);
                stream = new byte[streamLength];
                System.arraycopy(data, start, stream, 0, streamLength);


				a=count;
			}

		}

		if(debugFastCode && stream!=null)
			System.out.println(paddingString+"stream read "+stream+" saved into "+valueObj);

		if(valueObj!=null){

			valueObj.setStream(stream);

			//and decompress now forsome objects
			if(valueObj.decompressStreamWhenRead())
				readStream(valueObj,true,true,false, valueObj.getObjectType()==PdfDictionary.Metadata, valueObj.isCompressedStream(), null);

		}
	}

	private int readNameString(PdfObject pdfObject, String objectRef, int i,
			byte[] raw, boolean debugFastCode, int PDFkeyInt, String paddingString, boolean isMap, Object PDFkey) {

        byte[] stringBytes;

		int keyLength,keyStart;
        
		//move cursor to end of last command if needed
		while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!='(')
			i++;

		//move cursor to start of text
		while(raw[i]==10 || raw[i]==13 || raw[i]==32)
			i++;

		//work out if direct (ie /String or read ref 27 0 R
		int j2=i;
		byte[] arrayData=raw;

		boolean isIndirect=raw[i]!=47 && raw[i]!=40; //Some /NAME values start (

		boolean startsWithBrace=raw[i]==40;

		//delete
		//@speed - lose this code once Filters done properly
		/**
		 * just check its not /Filter [/FlateDecode ] or [] or [ /ASCII85Decode /FlateDecode ]
		 * by checking next valid char not /
		 */
		boolean isInsideArray=false;
		if(isIndirect){
			int aa=i+1;
			while(aa<raw.length && (raw[aa]==10 || raw[aa]==13 || raw[aa]==32 ))
				aa++;

			if(raw[aa]==47 || raw[aa]==']'){
				isIndirect=false;
				i=aa+1;
				isInsideArray=true;
			}
		}

		if(isIndirect){ //its in another object so we need to fetch

			keyStart=i;
			keyLength=0;

			//move cursor to end of ref
			while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
				i++;
				keyLength++;
			}

			//actual value or first part of ref
			int ref=parseInt(keyStart,i, raw);

			//move cursor to start of generation
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
				i++;

			// get generation number
			keyStart=i;
			//move cursor to end of reference
			while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
				i++;

			int generation=parseInt(keyStart,i, raw);

			//move cursor to start of R
			while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
				i++;

			if(raw[i]!=82){ //we are expecting R to end ref
				throw new RuntimeException(paddingString+"2. Unexpected value in file - please send to IDRsolutions for analysis");
			}

			//read the Dictionary data
			arrayData=readObjectAsByteArray(pdfObject, objectRef, isCompressed(ref,generation),ref,generation);

			//lose obj at start and roll onto /
			j2=3;
			while(arrayData[j2]!=47)
				j2++;
		}

		//lose /
		j2++;

		//allow for no value with /Intent//Filter
		if(arrayData[j2]==47)
			return j2-1;

		int end=j2+1;


		if(isInsideArray){ //values inside []

			//move cursor to start of text
			while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || arrayData[j2]==47)
				j2++;

			int slashes=0;

			//count chars
			byte lastChar=0;
			while(true){
				if(arrayData[end]==']')
					break;

				if(arrayData[end]==47 && (lastChar==32 || lastChar==10 || lastChar==13))//count the / if gap before
					slashes++;

				lastChar=arrayData[end];
				end++;

				if(end==arrayData.length)
					break;
			}

			//set value and ensure space gap
			int charCount=end-slashes,ptr=0;
			stringBytes=new byte[charCount-j2];

			byte nextChar,previous=0;
			for(int ii=j2;ii<charCount;ii++){
				nextChar=arrayData[ii];
				if(nextChar==47){
					if(previous!=32 && previous!=10 && previous!=13){
						stringBytes[ptr]=32;
						ptr++;
					}
				}else{
					stringBytes[ptr]=nextChar;
					ptr++;
				}

				previous=nextChar;
			}
		}else{ //its in data stream directly or (string)

			//count chars
			while(true){

				if(startsWithBrace){
					if(arrayData[end]==')')
						break;
				}else if(arrayData[end]==32 || arrayData[end]==10 || arrayData[end]==13 || arrayData[end]==47 || arrayData[end]==62)
					break;

				end++;

				if(end==arrayData.length)
					break;
			}

			//set value
			int charCount=end-j2;
			stringBytes=new byte[charCount];
			System.arraycopy(arrayData,j2,stringBytes,0,charCount);

		}

		/**
		 * finally set the value
		 */
        if(isMap)
		    pdfObject.setName(PDFkey,new String(stringBytes));
        else
		    pdfObject.setName(PDFkeyInt,stringBytes);

		if(debugFastCode)
			System.out.println(paddingString+"String set as ="+new String(stringBytes)+"< written to "+pdfObject);

		//put cursor in correct place (already there if ref)
		if(!isIndirect)
			i=end-1;
		return i;
	}

	//boolean set=false;
	private int readArray(boolean ignoreRecursion, int i, int endPoint, int type, byte[] raw, String objectRef, PdfObject pdfObject, int PDFkeyInt,
			boolean debugFastCode, String paddingString, Object[] objectValuesArray, int keyReached) {

		//debugFastCode=false;
		int keyStart;
		//roll on
		if(raw[i]!=91)
			i++;

		Map isRef=new HashMap();

        boolean isHexString=false;
        
		boolean alwaysRead =(PDFkeyInt==PdfDictionary.Kids || PDFkeyInt==PdfDictionary.Annots);

		final boolean debugArray=false || debugFastCode;// || type==PdfDictionary.VALUE_IS_OBJECT_ARRAY;

		if(debugArray)
			System.out.println(paddingString+"Reading array type="+PdfDictionary.showArrayType(type)+" into "+pdfObject+" "+new String(raw));

        int currentElement=0, elementCount=0,rawCount=0;

		//move cursor to start of text
		while(raw[i]==10 || raw[i]==13 || raw[i]==32)
			i++;

        //allow for comment
        if(raw[i]==37){
            while(raw[i]!=10 && raw[i]!=13){
                i++;
            }

            //move cursor to start of text
            while(raw[i]==10 || raw[i]==13 || raw[i]==32)
                i++;
        }

		keyStart=i;

		//work out if direct or read ref ( [values] or ref to [values])
		int j2=i;
		byte[] arrayData=raw;

		//may need to add method to PdfObject is others as well as Mask
		boolean isIndirect=raw[i]!=91 && raw[i]!='(' && (PDFkeyInt!=PdfDictionary.Mask && PDFkeyInt!=PdfDictionary.TR &&
				pdfObject.getObjectType()!=PdfDictionary.ColorSpace);

		
		// allow for /Contents null
		if(raw[i]=='n' && raw[i+1]=='u' && raw[i+2]=='l' && raw[i+2]=='l'){
			isIndirect=false;
			elementCount=1;
		}

		//check not indirect Kids with 1 element and flag
		/**if(raw[i]==91 && pdfObject.getObjectType()==PdfDictionary.Form && PDFkeyInt==PdfDictionary.Kids){
			
			
			int j=i,objCount=0;
			while(raw[j]!=']'){
			
				if(raw[j]=='R')
					objCount++;
				j++;
			}
			
			if(objCount==1)
				pdfObject.kidsIndirect(true);

		}/**/
		

		if(debugArray)
			System.out.println("IsIndirect="+isIndirect+" "+raw[i]+" "+(char)raw[i]);

		
		//check indirect and not [/DeviceN[/Cyan/Magenta/Yellow/Black]/DeviceCMYK 36 0 R]
		if(isIndirect){
			//find next value and make sure not /
			int aa=i;

			while(raw[aa]!=93 ){
				aa++;

				//allow for ref (ie 7 0 R)
				if(aa>=endPoint)
					break;

				if(raw[aa]=='R' && (raw[aa-1]==32 || raw[aa-1]==10 || raw[aa-1]==13))
					break;
				else if(raw[aa]=='>' && raw[aa-1]=='>'){
					isIndirect=false;
					if(debugArray )
						System.out.println(paddingString+"1. rejected as indirect ref");

					break;
				}else if(raw[aa]==47){
					isIndirect=false;
					if(debugArray )
						System.out.println(paddingString+"2. rejected as indirect ref - starts with /");

					break;
				}
			}
		}

		if(debugArray && isIndirect)
			System.out.println(paddingString+"Indirect ref");

		boolean isSingleKey=false,isSingleDirectValue=false; //flag to show points to Single value (ie /FlateDecode)
		int endPtr=-1;

		if((raw[i]==47 || raw[i]=='(' ||
				(raw[i]=='<' && raw[i+1]=='f' && raw[i+2]=='e') && raw[i+3]=='f' && raw[i+4]=='f') &&
				type!=PdfDictionary.VALUE_IS_STRING_ARRAY && PDFkeyInt!=PdfDictionary.TR){ //single value ie /Filter /FlateDecode or (text)

			elementCount=1;
			isSingleKey=true;

			if(debugArray)
				System.out.println(paddingString+"Direct single value with /");
		}else{

			int endI=-1;//allow for jumping back to single value (ie /Contents 12 0 R )

			if(isIndirect){

				if(debugArray)
					System.out.println(paddingString+"------reading data----");

				//allow for indirect to 1 item
				int startI=i;

				if(debugArray)
					System.out.print(paddingString+"Indirect object ref=");

				//move cursor to end of ref
				while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
					i++;
				}

				//actual value or first part of ref
				int ref=parseInt(keyStart,i, raw);

				//move cursor to start of generation
				while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
					i++;

				// get generation number
				keyStart=i;
				//move cursor to end of reference
				while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
					i++;

				int generation=parseInt(keyStart,i, raw);

				if(debugFastCode)
					System.out.print(paddingString+" ref="+ref+" generation="+generation+"\n");

				// check R at end of reference and abort if wrong
				//move cursor to start of R
				while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
					i++;

				if(raw[i]!=82) //we are expecting R to end ref
					throw new RuntimeException(paddingString+"4. Unexpected value "+(char)raw[i]+" in file - please send to IDRsolutions for analysis");

				if(ignoreRecursion && !alwaysRead){

					if(debugArray)
						System.out.println(paddingString+"Ignore sublevels");
					return i;
				}

				//read the Dictionary data
				arrayData=readObjectAsByteArray(pdfObject, objectRef, isCompressed(ref,generation),ref,generation);

				if(debugArray){
					System.out.println(paddingString+"Raw data is>>>>>>>>>>>>>>");
					System.out.print(paddingString);
					for(int aa=0;aa<arrayData.length;aa++)  {
						System.out.print((char)arrayData[aa]);

						if(aa>5 && arrayData[aa-5]=='s' && arrayData[aa-4]=='t' && arrayData[aa-3]=='r' && arrayData[aa-2]=='e' && arrayData[aa-1]=='a' && arrayData[aa]=='m')
							aa=arrayData.length;
					}

					System.out.println("\n"+paddingString+"<<<<<<<<<<<<<<");
				}

				//lose obj at start and roll onto [
				j2=0;
				while(arrayData[j2]!=91){

					//allow for % comment
					if(arrayData[j2]=='%'){
						while(true){
							j2++;
							if(arrayData[j2]==13 || arrayData[j2]==10)
								break;
						}
						while(arrayData[j2]==13 || arrayData[j2]==10)
							j2++;
					}

					//allow for null
					if(arrayData[j2]=='n' && arrayData[j2+1]=='u' && arrayData[j2+2]=='l' && arrayData[j2+3]=='l')
						break;

					if(arrayData[j2]==47){ //allow for value of type  32 0 obj /FlateDecode endob
						j2--;
						isSingleDirectValue=true;
						break;
					}if ((arrayData[j2]=='<' && arrayData[j2+1]=='<')||
							((j2+4<arrayData.length) &&arrayData[j2+3]=='<' && arrayData[j2+4]=='<')){ //also check ahead to pick up [<<
						endI=i;

						j2=startI;
						arrayData=raw;

						if(debugArray)
							System.out.println(paddingString+"Single value, not indirect");

						break;
					}

					j2++;
				}
			}

			if(j2<0) //avoid exception
				j2=0;

			//skip [ and any spaces allow for [[ in recursion
			boolean startFound=false;

			while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 ||
					(arrayData[j2]==91 && !startFound)){//(type!=PdfDictionary.VALUE_IS_OBJECT_ARRAY || objectValuesArray==null)))

				if(arrayData[j2]==91)
					startFound=true;

				j2++;
			}
			
			//count number of elements
			endPtr=j2;
			boolean charIsSpace=false,lastCharIsSpace=true,isRecursive=false;

			if(debugArray)
				System.out.println(paddingString+"----counting elements----arrayData[endPtr]="+arrayData[endPtr]+" type="+type);

				while(endPtr<arrayData.length && arrayData[endPtr]!=93){

					isRecursive=false;

					//System.out.println("next Char="+arrayData[endPtr]);
					//allow for embedded object
					if(arrayData[endPtr]=='<' && arrayData[endPtr+1]=='<'){
						int levels=1;

						elementCount++;

						if(debugArray)
							System.out.println(paddingString+"Direct value elementCount="+elementCount);

						while(levels>0){
							endPtr++;

							if(arrayData[endPtr]=='<' && arrayData[endPtr+1]=='<'){
								endPtr++;
								levels++;
							}else if(arrayData[endPtr]=='>' && arrayData[endPtr-1]=='>'){
								endPtr++;
								levels--;
							}
						}
					}

					//allow for null (not Mixed!)
					if(type!=PdfDictionary.VALUE_IS_MIXED_ARRAY && arrayData[endPtr]=='n' && arrayData[endPtr+1]=='u' &&
							arrayData[endPtr+2]=='l' && arrayData[endPtr+3]=='l'){
						elementCount=1;
						break;
					}

					if(isSingleDirectValue && (arrayData[endPtr]==32 || arrayData[endPtr]==13 || arrayData[endPtr]==10))
						break;

					if(endI!=-1 && endPtr>endI)
						break;

					if(type==PdfDictionary.VALUE_IS_KEY_ARRAY){

                        if(arrayData[endPtr]==' ')
                        rawCount++;

                        if(arrayData[endPtr]=='R'  || (PDFkeyInt==PdfDictionary.TR && arrayData[endPtr]=='/'  ))
							elementCount++;

					}else{

						//handle (string)
						if(arrayData[endPtr]=='('){
							elementCount++;

							while(true){
								if(arrayData[endPtr]==')' && !isEscaped(arrayData,endPtr))
									break;

								endPtr++;

								lastCharIsSpace=true; //needs to be space for code to work eve if no actual space
							}	
                        }else if(arrayData[endPtr]=='<'){
							elementCount++;

							while(true){
								if(arrayData[endPtr]=='>')
									break;

								endPtr++;

								lastCharIsSpace=true; //needs to be space for code to work eve if no actual space
							}
						}else if(arrayData[endPtr]==91){ //handle recursion

							elementCount++;
							int level=1;
							while(true){

								endPtr++;

								if(arrayData[endPtr]==93)
									level--;
								else if(arrayData[endPtr]==91)
									level++;

								if(level==0)
									break;
							}

							isRecursive=true;

						}else{

							if(arrayData[endPtr]==10 || arrayData[endPtr]==13 || arrayData[endPtr]==32 || arrayData[endPtr]==47)
								charIsSpace=true;
							else
								charIsSpace=false;

							if(lastCharIsSpace && !charIsSpace ){
								if((type==PdfDictionary.VALUE_IS_MIXED_ARRAY || type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
										&& arrayData[endPtr]=='R' && arrayData[endPtr-1]!='/'){ //adjust so returns correct count  /R and  on 12 0 R
									elementCount--;

									isRef.put(new Integer(elementCount-1),"x");
								}else
									elementCount++;

							}
							lastCharIsSpace=charIsSpace;
						}
					}

					//allow for empty array [ ]
					if(!isRecursive && arrayData[endPtr]==93 && type!=PdfDictionary.VALUE_IS_KEY_ARRAY){

                        //get first char
                        int ptr=endPtr-1;
                        while(arrayData[ptr]==13 || arrayData[ptr]==10 || arrayData[ptr]==32)
                        ptr--;

                        if(ptr=='[') //if empty reset
						elementCount=0;
						break;
					}

					endPtr++;
				}

			if(debugArray)
				System.out.println(paddingString+"Number of elements="+elementCount+" rawCount="+rawCount);

			if(elementCount==0 && debugArray)
				System.out.println(paddingString+"zero elements found!!!!!!");

		}

		if(ignoreRecursion && !alwaysRead)
			return endPtr;

		//now create array and read values
		float[] floatValues=null;
		int[] intValues=null;
		double[] doubleValues=null;
		byte[][] mixedValues=null;
		byte[][] keyValues=null;
		byte[][] stringValues=null;
		boolean[] booleanValues=null;
		Object[] objectValues=null;

		if(type==PdfDictionary.VALUE_IS_FLOAT_ARRAY)
			floatValues=new float[elementCount];
		else if(type==PdfDictionary.VALUE_IS_INT_ARRAY)
			intValues=new int[elementCount];
		else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY)
			booleanValues=new boolean[elementCount];
		else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY)
			doubleValues=new double[elementCount];
		else if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY)
			mixedValues=new byte[elementCount][];
		else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY)
			keyValues=new byte[elementCount][];
		else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY)
			stringValues=new byte[elementCount][];
		else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
			objectValues=new Object[elementCount];

		/**
		 * read all values and convert
		 */
		 if(arrayData[j2]=='n' && arrayData[j2+1]=='u' &&
				 arrayData[j2+2]=='l' && arrayData[j2+3]=='l'){
			 j2=j2+3;

			 if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY)
				 mixedValues[0]=null;
			 else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY)
				 keyValues[0]=null;
			 else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY)
				 stringValues[0]=null;
			 else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
				 objectValues[0]=null;

		 }else{///read values
			 
			 while(arrayData[j2]!=93){

				 if(endPtr>-1 && j2>=endPtr)
					 break;

				 //move cursor to start of text
				 while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || arrayData[j2]==47)
					 j2++;

				 keyStart=j2;

				 //if(debugArray)
				 //	 System.out.print("j2="+j2+" value=");

				 boolean isKey=arrayData[j2-1]=='/';
				 boolean isRecursiveValue=false; //flag to show if processed in top part so ignore second part

				 //move cursor to end of text
				 if(type==PdfDictionary.VALUE_IS_KEY_ARRAY ||
						 ((type==PdfDictionary.VALUE_IS_MIXED_ARRAY || type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
								 && (isRef.containsKey(new Integer(currentElement))||(arrayData[j2]=='<' && arrayData[j2+1]=='<')))){

					 if(debugArray)
						 System.out.println("ref currentElement="+currentElement);

					 while(arrayData[j2]!='R' && arrayData[j2]!=']'){

						 //allow for embedded object
						 if(arrayData[j2]=='<' && arrayData[j2+1]=='<'){
							 int levels=1;

							 if(debugArray)
								 System.out.println(paddingString+"Reading Direct value");

							 while(levels>0){
								 j2++;

								 if(arrayData[j2]=='<' && arrayData[j2+1]=='<'){
									 j2++;
									 levels++;
								 }else if(arrayData[j2]=='>' && arrayData[j2+1]=='>'){
									 j2++;
									 levels--;
								 }
							 }
							 break;
						 }


						 if(isKey && PDFkeyInt==PdfDictionary.TR && arrayData[j2+1]==' ')
							 break;

						 //if(debugArray)
						 //	 System.out.print((char)arrayData[j2]);

						 j2++;
					 }
					 j2++;

				 }else{

					 // handle (string)
					 if(arrayData[j2]=='('){

						 keyStart=j2+1;
						 while(true){
							 if(arrayData[j2]==')' && !isEscaped(arrayData,j2))
								 break;

							 j2++;

						 }

						 //include end bracket
						 //j2++;

                     }else if(arrayData[j2]=='<'){

                         isHexString=true;
						 keyStart=j2+1;
						 while(true){
							 if(arrayData[j2]=='>')
								 break;

							 if(arrayData[j2]=='/')
								 isHexString=false;
							 
							 j2++;

						 }

						 //include end bracket
						 //j2++;

					 }else if(arrayData[j2]==91 && type==PdfDictionary.VALUE_IS_OBJECT_ARRAY){

						 //find end
						 int j3=j2+1;
						 int level=1;

						 while(true){

							 j3++;

							 if(arrayData[j3]==93)
								 level--;
							 else if(arrayData[j3]==91)
								 level++;

							 if(level==0)
								 break;
						 }
						 j3++;


						 j2=readArray(ignoreRecursion, j2, j3, type,  arrayData, objectRef, pdfObject,
								 PDFkeyInt, debugFastCode, paddingString+"    ", objectValues, currentElement) ;

						 //j2--;
						 j2++;

						 isRecursiveValue=true;

						 while(arrayData[j2]==']')
							 j2++;

					 }else if(!isKey && elementCount-currentElement==1 && type==PdfDictionary.VALUE_IS_MIXED_ARRAY){ //if last value just read to end in case 1 0 R

						 while(arrayData[j2]!=93 && arrayData[j2]!=47){

							 if(arrayData[j2]==62 && arrayData[j2+1]==62)
								 break;

							 j2++;
						 }
					 }else{
						 while(arrayData[j2]!=10 && arrayData[j2]!=13 && arrayData[j2]!=32 && arrayData[j2]!=93 && arrayData[j2]!=47){
							 if(arrayData[j2]==62 && arrayData[j2+1]==62)
								 break;

							 j2++;
						 }
					 }
				 }

				 // if(debugArray)
				 //     System.out.println(paddingString+"<Element -----"+currentElement+"/"+elementCount+"( j2="+j2+" )");

				 //actual value or first part of ref
				 if(type==PdfDictionary.VALUE_IS_FLOAT_ARRAY)
					 floatValues[currentElement]=parseFloat(keyStart,j2, arrayData);
				 else if(type==PdfDictionary.VALUE_IS_INT_ARRAY)
					 intValues[currentElement]=parseInt(keyStart,j2, arrayData);
				 else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY){
					 if(raw[keyStart]=='t' && raw[keyStart+1]=='r' && raw[keyStart+2]=='u' && raw[keyStart+3]=='e')
						 booleanValues[currentElement]=true; //(false id default if not set)
				 }else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY)
					 doubleValues[currentElement]=parseFloat(keyStart,j2, arrayData);
				 else if(!isRecursiveValue){

					 //include / so we can differentiate /9 and 9
					 if(keyStart>0 && arrayData[keyStart-1]==47)
						 keyStart--;

					 //lose any spurious [
					 if(keyStart>0 && arrayData[keyStart]=='[')
						 keyStart++;

                     //lose any spurious chars at start
                     while(keyStart>=0 && (arrayData[keyStart]==' ' || arrayData[keyStart]==10 || arrayData[keyStart]==13 || arrayData[keyStart]==9))
						 keyStart++;

                     byte[] newValues= readEscapedValue(j2,arrayData, keyStart, PDFkeyInt==PdfDictionary.ID);

                     if(debugArray)
                        System.out.println(paddingString+"<1.Element -----"+currentElement+"/"+elementCount+"( j2="+j2+" ) value="+new String(newValues)+"<");

                    if(arrayData[j2]=='>'){
						 j2++;
					 //roll past ) and decrypt if needed
                    }else if(arrayData[j2]==')'){
						 j2++;

					    //	 if(pdfObject.getObjectType()!=PdfDictionary.Names){
                         try {
                             newValues=decrypt(newValues,objectRef, false,null, false,false);
                         } catch (PdfSecurityException e) {
                             e.printStackTrace();
                         }

                        //convert Strings in Order now
                        if(PDFkeyInt==PdfDictionary.Order){
                            newValues=PdfReader.getTextString(newValues,false).getBytes();
                        }
                     //}
                    }

					 //update pointer if needed
					 if(isSingleKey)
						 i=j2;

                     if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY){
						 mixedValues[currentElement]=newValues;
                     }else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY){
						 keyValues[currentElement]=convertReturnsToSpaces(newValues);
                     }else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY){
                         if(isHexString){
                             //convert to byte values
                             String nextValue;
                             String str=new String(newValues);
                             byte[] IDbytes=new byte[newValues.length/2];
                             for(int ii=0;ii<newValues.length;ii=ii+2){

                            	 if(ii+2>newValues.length)
                            		 continue;
                                 
                            	 nextValue=str.substring(ii,ii+2);
                                 IDbytes[ii/2]=(byte)Integer.parseInt(nextValue,16);

                             }
                             newValues=IDbytes;
                         }

                         stringValues[currentElement]=newValues;

                     }else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY){
						 objectValues[currentElement]=(newValues);

						 if(debugArray)
							 System.out.println(paddingString+"objectValues["+currentElement+"]="+newValues+" ");
					 }
				 }
				 currentElement++;

				 if(debugArray)
					 System.out.println(paddingString+"roll onto ==================================>"+currentElement+"/"+elementCount);
				 if(currentElement==elementCount)
					 break;
			 }
		 }

		 //put cursor in correct place (already there if ref)
		 if(!isIndirect)
			 i=j2;

		 //set value
		 if(type==PdfDictionary.VALUE_IS_FLOAT_ARRAY)
			 pdfObject.setFloatArray(PDFkeyInt,floatValues);
		 else if(type==PdfDictionary.VALUE_IS_INT_ARRAY)
			 pdfObject.setIntArray(PDFkeyInt,intValues);
		 else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY)
			 pdfObject.setBooleanArray(PDFkeyInt,booleanValues);
		 else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY)
			 pdfObject.setDoubleArray(PDFkeyInt,doubleValues);
		 else if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY)
			 pdfObject.setMixedArray(PDFkeyInt,mixedValues);
		 else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY)
			 pdfObject.setKeyArray(PDFkeyInt,keyValues);
		 else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY)
			 pdfObject.setStringArray(PDFkeyInt,stringValues);
		 else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY){

             //allow for indirect order
             if(PDFkeyInt==PdfDictionary.Order && objectValues!=null && objectValues.length==1 && objectValues[0] instanceof byte[]){

                 byte[] objData=(byte[]) objectValues[0];
                 int size=objData.length;
                 if(objData[size-1]=='R'){

                     PdfObject obj=new OCObject(new String(objData));
                     byte[] newData=readObjectData(obj);

                     int jj=0,newLen=newData.length;
                     boolean hasArray=false;
                     while(jj<newLen){
                         jj++;

                         if(jj==newData.length)
                         break;

                         if(newData[jj]=='['){
                             hasArray=true;
                             break;
                         }
                     }

                     if(hasArray)
                        readArray(false, jj, newLen, PdfDictionary.VALUE_IS_OBJECT_ARRAY, newData,
                                new String(objData), pdfObject, PDFkeyInt, debugFastCode, paddingString, null, -1);

                     objectValues=null;
                     
                 }
             }

			 if(objectValuesArray!=null){
				 objectValuesArray[keyReached]=objectValues;
				 if(debugArray)
					 System.out.println(paddingString+"set Object objectValuesArray["+keyReached+"]="+objectValues);
			 }else if(objectValues!=null){
				 pdfObject.setObjectArray(PDFkeyInt,objectValues);

				 if(debugArray)
					 System.out.println(paddingString+PDFkeyInt+" set Object value="+objectValues);
             }
         }

		 if(debugArray)  {
			 String values="[";

			 if(type==PdfDictionary.VALUE_IS_FLOAT_ARRAY){
				 int count=floatValues.length;
				 for(int jj=0;jj<count;jj++)
					 values=values+floatValues[jj]+" ";

			 }else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY){
				 int count=doubleValues.length;
				 for(int jj=0;jj<count;jj++)
					 values=values+doubleValues[jj]+" ";

			 }else if(type==PdfDictionary.VALUE_IS_INT_ARRAY){
				 int count=intValues.length;
				 for(int jj=0;jj<count;jj++)
					 values=values+intValues[jj]+" ";

			 }else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY){
				 int count=booleanValues.length;
				 for(int jj=0;jj<count;jj++)
					 values=values+booleanValues[jj]+" ";

			 }else if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY){
				 int count=mixedValues.length;
				 for(int jj=0;jj<count;jj++)
					 if(mixedValues[jj]==null)
						 values=values+"null ";
					 else
						 values=values+new String(mixedValues[jj])+" ";

			 }else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY){
				 int count=keyValues.length;
				 for(int jj=0;jj<count;jj++){
					 if(keyValues[jj]==null)
						 values=values+"null ";
					 else
						 values=values+new String(keyValues[jj])+" ";
				 }
			 }else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY){
				 int count=stringValues.length;
				 for(int jj=0;jj<count;jj++){
					 if(stringValues[jj]==null)
						 values=values+"null ";
					 else
						 values=values+new String(stringValues[jj])+" ";
				 }
			 }else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY){
				 values = showMixedValuesAsString(objectValues, "");
			 }

			 values=values+" ]";

		 }

		 //roll back so loop works if no spaces
		 if(i<raw.length &&(raw[i]==47 || raw[i]==62))
			 i--;

         return i;
	}

    //replace sequence 13 10 with 32
    private static byte[] convertReturnsToSpaces(byte[] newValues) {

        if(newValues==null)
        return null;

        //see if needed
        int returnCount=0;
        int len=newValues.length;
        for(int aa=0;aa<len;aa++){
            if(newValues[aa]==13 && newValues[aa+1]==10){
                aa++;
                returnCount++;
            }
        }

        //swap out if needed
        if(returnCount>0){

            int newLen=len-returnCount;
            int jj=0;
            byte[] oldValue=newValues;
            newValues=new byte[newLen];

            for(int aa=0;aa<len;aa++){

                if(oldValue[aa]==13 && aa<len-1 && oldValue[aa+1]==10){
                    newValues[jj]=32;
                    aa++;
                }else
                    newValues[jj]=oldValue[aa];

                jj++;
            }
        }

        return newValues;

    }

    private static String showMixedValuesAsString(Object[] objectValues, String values) {

        if(objectValues==null)
        return "null";

		values=values+'[';
		int count=objectValues.length;
        
		for(int jj=0;jj<count;jj++){

			if(objectValues[jj]==null)
				values=values+"null ";
			else if(objectValues[jj] instanceof byte[]){
				values=values+new String((byte[])objectValues[jj]);
				if(count-jj>1)
					values=values+" , ";
			}else{
				values = showMixedValuesAsString((Object[])objectValues[jj], values)+"]";
				if(count-jj>1)
					values=values+" ,";
			}
		}
		return values;
	}

	//@speed - debug code
	private static boolean checkStreamsIdentical(byte[] newStream, byte[] oldStream) {


		boolean failed=false;
		try{
			if(newStream==null && oldStream==null){
			}else{

				int newLength=newStream.length;
				int oldLength=oldStream.length;

				if(newLength!=oldLength)
					System.out.println("=========old length="+oldLength+"< new length="+newLength+"<");

				int cc=oldLength;
				if(newLength<oldLength)
					cc=newLength;


				for(int aa=0;aa<cc;aa++){

					if(newStream[aa]!=oldStream[aa]){
						System.out.println(aa+" Difference new="+newStream[aa]+" old="+oldStream[aa]);

						failed=true;
					}
				}

			}
		}catch(Exception ee){

			System.err.println("old="+oldStream);
			System.err.println("new="+newStream);
			ee.printStackTrace();
		}

		return failed;
	}

	//@speed - debug code
	public static void checkStringsIdentical(String baseFont, String baseFont2) {

		try{
			if(baseFont2==null && baseFont==null){
			}else if(!baseFont2.equals(baseFont)){
				System.out.println("old="+baseFont+"<\nnew="+baseFont2+"<");


			}
		}catch(Exception ee){
			System.err.println("In exception old value="+baseFont+"< new value="+baseFont2+"<");
			ee.printStackTrace();
		}
	}

	//@speed - debug code
	private static void checkNumbersIdentical(float baseFont, float baseFont2) {

		float diff=baseFont-baseFont2;
		if(diff<0)
			diff=-diff;

		if(diff>0.001f)
			throw new RuntimeException("=========old value="+baseFont+"< new value="+baseFont2+"<");

	}

	//@speed - debug code
	private static void checkFloatArraysIdentical(float[] old,float[] newF) {

		if(old==null && newF==null)
			return;

		//System.out.println(baseFont+" "+baseFont2);
		try{
			int oldlength=old.length;
			int newlength=newF.length;

			if(oldlength!=newlength)
				throw new RuntimeException("Different lengths "+oldlength+" "+newlength);

			for(int aa=0;aa<oldlength;aa++){
				float diff=old[aa]-newF[aa];

				if(diff<0)
					diff=-diff;

				if(diff>0.0001f){
					System.out.println("Significant Difference in floats");
					System.out.println("--------------------------------");
					for(aa=0;aa<oldlength;aa++)
						System.out.println(old[aa]+" "+newF[aa]);

				}
			}

		}catch(Exception ee){
			System.err.println("old="+old);
			System.err.println("new="+newF);
			ee.printStackTrace();
		}
	}

	//@speed - debug code
	private static void checkBooleanArraysIdentical(boolean[] old,boolean[] newF) {

		if(old==null && newF==null)
			return;

		//System.out.println(baseFont+" "+baseFont2);
		try{
			int oldlength=old.length;
			int newlength=newF.length;

			if(oldlength!=newlength)
				throw new RuntimeException("Different lengths "+oldlength+" "+newlength);

			for(int aa=0;aa<oldlength;aa++){
				if(old[aa]!=newF[aa]){
					System.out.println("Significant Difference in Boolean");
					System.out.println("--------------------------------");
					for(aa=0;aa<oldlength;aa++)
						System.out.println(old[aa]+" "+newF[aa]);

				}
			}

		}catch(Exception ee){
			System.err.println("old="+old);
			System.err.println("new="+newF);
			ee.printStackTrace();
		}
	}

	//@speed - debug code
	private static void checkIntArraysIdentical(int[] old,int[] newF) {

		if(old==null && newF==null)
			return;

		//System.out.println(baseFont+" "+baseFont2);
		try{
			int oldlength=old.length;
			int newlength=newF.length;

			if(oldlength!=newlength)
				throw new RuntimeException("Different lengths "+oldlength+" "+newlength);


			for(int aa=0;aa<oldlength;aa++){
				if(old[aa]!=newF[aa]){
					System.out.println("Difference in Int");
					System.out.println("--------------------------------");
					for(aa=0;aa<oldlength;aa++)
						System.out.println(old[aa]+" "+newF[aa]);

				}
			}

		}catch(Exception ee){
			System.err.println("old="+old);
			System.err.println("new="+newF);
			ee.printStackTrace();
		}
	}

	//@speed - debug code
	private static void checkDoubleArraysIdentical(double[] old,double[] newF) {

		if(old==null && newF==null)
			return;

		//System.out.println(baseFont+" "+baseFont2);
		try{
			int oldlength=old.length;
			int newlength=newF.length;

			if(oldlength!=newlength)
				throw new RuntimeException("Different lengths "+oldlength+" "+newlength);


			for(int aa=0;aa<oldlength;aa++){
				double diff=old[aa]-newF[aa];

				if(diff<0)
					diff=-diff;

				if(diff>0.0001d)
					throw new RuntimeException("Significant Difference in doubles "+old[aa]+" "+newF[aa]);

			}

		}catch(Exception ee){
			ee.printStackTrace();
		}
	}

	/**read a stream*
	final public byte[] readStream(Map objData,String objectRef,boolean cacheValue,
			boolean decompress,boolean keepRaw, boolean isMetaData, boolean isCompressedStream)  {

		Object data=objData.get("DecodedStream");

		BufferedOutputStream streamCache=null;
		byte[] stream;
		String cacheName=null;

		boolean isCachedOnDisk = false;

		//decompress first time
		if(data==null){
			stream=(byte[]) objData.get("Stream");

			isCachedOnDisk=objData.get("startStreamOnDisk")!=null &&
			endStreamPointer - startStreamPointer >= 0;

			if(isCachedOnDisk){
				try{
					//
					File tempFile=File.createTempFile("jpedal",".bin", new File(ObjectStore.temp_dir));
					cacheName=tempFile.getAbsolutePath();
					cachedObjects.put(cacheName,"x");
					streamCache=new BufferedOutputStream(new FileOutputStream(tempFile));

					int buffer=8192;
					byte[] bytes;
					int ptr=startStreamPointer,remainingBytes;
					while(true){

						//handle last n bytes of object correctly
						remainingBytes=1+endStreamPointer-ptr;

						if(remainingBytes<buffer)
							buffer=remainingBytes;
						bytes = new byte[buffer];

						//get bytes into buffer
						this.movePointer(ptr);
						this.pdf_datafile.read(bytes);

						//spool to disk
						streamCache.write(bytes);

						ptr=ptr+buffer;
						if(ptr>=endStreamPointer)
							break;
					}
					streamCache.close();

					File tt=new File(cacheName);

				}catch(Exception e){
					e.printStackTrace();
				}

				//decrypt the stream
				try{
					if(!isCompressedStream && (isMetaDataEncypted || !isMetaData))
						decrypt(null,objectRef, false,cacheName, false,false);
				}catch(Exception e){
					e.printStackTrace();
					stream=null;
					LogWriter.writeLog("Exception "+e);

				}

				objData.put("CachedStream",cacheName);
			}

			if(stream!=null){ //decode and save stream

				//decrypt the stream
				try{
					if(!isCompressedStream && (isMetaDataEncypted || !isMetaData))
						stream=decrypt(stream,objectRef, false,null,false,false);
				}catch(Exception e){
					e.printStackTrace();
					stream=null;
					LogWriter.writeLog("Exception "+e);
				}
			}


			if(keepRaw)
				objData.remove("Stream");

			int length=1;

			if(stream!=null || isCachedOnDisk){

				//values for CCITTDecode
				int height=1,width=1;
				String value=(String) objData.get("Height");
				if(value!=null){

					//allow for object
					if(value.indexOf('R')!=-1){
						Map heightObj=this.readObject(new PdfObject(value), value, null);

						Object indirectHeight=heightObj.get("rawValue");
						if(indirectHeight!=null)
							value=(String) indirectHeight;
					}

					height = Integer.parseInt(value);
				}

				value=(String) objData.get("Width");
				if(value!=null)
					width = Integer.parseInt(value);

				value= getValue((String)objData.get("Length"));
				if(value!=null)
					length= Integer.parseInt(value);

				//allow for no width or length
				if(height*width==1)
					width=length;

				String filter = this.getValue((String) objData.get("Filter"));
				if (filter != null && !filter.startsWith("/JPXDecode") && !filter.startsWith("/DCT")){

					try{

						//ensure ref converted first
						Object param = objData.get("DecodeParms");
						
						if(param==null)
							param=objData.get("Params");
						
						if(param!=null && param instanceof String){
							String ref=(String) param;
							if(ref.endsWith(" R")){
								Map paramObj=this.readObject(new PdfObject(ref), ref, null);
								objData.put("DecodeParms",paramObj);
							}
						}

						stream =decodeFilters(stream, filter, objData.get("DecodeParms"),width,height,true,cacheName);

					}catch(Exception e){
						LogWriter.writeLog("[PDF] Problem "+e+" decompressing stream "+filter);
						stream=null;
						isCachedOnDisk=false; //make sure we return null, and not some bum values
					}

					//stop spurious match down below in caching code
					length=1;
				}else if(stream!=null && length!=-1 && length<stream.length ){

					//make sure length correct
					if(stream.length!=length){
						byte[] newStream=new byte[length];
						System.arraycopy(stream, 0, newStream, 0, length);

						stream=newStream;
					}
				}
			}

			if(stream!=null && cacheValue)
				objData.put("DecodedStream",stream);

			if((decompress)&&(isCachedOnDisk)){
				int streamLength = (int) new File(cacheName).length();

				byte[] bytes = new byte[streamLength];

				try {
					new BufferedInputStream(new FileInputStream(cacheName)).read(bytes);
				} catch (Exception e) {
					e.printStackTrace();
				}

				//resize if length supplied
				if((length!=1)&&(length<streamLength)){

					//make sure length correct
					byte[] newStream=new byte[length];
					System.arraycopy(bytes, 0, newStream, 0, length);

					bytes=newStream;

				}

				if(debugCaching){
					if(bytes.length!=stream.length)
						throw new RuntimeException("Problem with sizes in readStream "+bytes.length+ ' ' +stream.length);

				}

				return bytes;
			}

		}else
			stream=(byte[]) data;

		return  stream;
	} /**/


	/**read a stream*/
	final public byte[] readStream(PdfObject pdfObject, boolean cacheValue,
                                   boolean decompress, boolean keepRaw, boolean isMetaData,
                                   boolean isCompressedStream, String cacheName)  {

		final boolean debugStream=false;

        boolean isCachedOnDisk = pdfObject.isCached();

        byte[] data=null;

        if(!isCachedOnDisk)
		data=pdfObject.getDecodedStream();

		//BufferedOutputStream streamCache=null;
		byte[] stream;

		//decompress first time
		if(data==null){

			String objectRef=pdfObject.getObjectRefAsString();

			stream=pdfObject.stream;

			if(isCachedOnDisk){
				
				//decrypt the stream
				try{
					if(!isCompressedStream && (isMetaDataEncypted || !isMetaData)){

						decrypt(null,objectRef, false,cacheName, false,false);
                    }
				}catch(Exception e){
					e.printStackTrace();
					stream=null;
					LogWriter.writeLog("Exception "+e);
				}
            }

			if(stream!=null){ /**decode and save stream*/

				//decrypt the stream
				try{
					if(!isCompressedStream  && (isMetaDataEncypted || !isMetaData)){// && pdfObject.getObjectType()!=PdfDictionary.ColorSpace){

                        // System.out.println(objectRef+">>>"+pdfObject.getObjectRefAsString()); 
                        if(pdfObject.getObjectType()==PdfDictionary.ColorSpace && objectRef.startsWith("[")){

                        }else
                            stream=decrypt(stream,objectRef, false,null,false,false);
                        
                    }
				}catch(Exception e){
					e.printStackTrace();
					stream=null;
					LogWriter.writeLog("Exception "+e+" with "+objectRef);
				}
			}

			if(keepRaw)
				pdfObject.stream=null;

			int length=1;

			if(stream!=null || isCachedOnDisk){

				//values for CCITTDecode
				int height=1,width=1;

				int newH=pdfObject.getInt(PdfDictionary.Height);
				if(newH!=-1)
					height=newH;

				int newW=pdfObject.getInt(PdfDictionary.Width);
				if(newW!=-1)
					width=newW;

				int newLength=pdfObject.getInt(PdfDictionary.Length);
				if(newLength!=-1)
					length=newLength;

				/**allow for no width or length*/
				if(height*width==1)
					width=length;

				PdfObject DecodeParms= pdfObject.getDictionary(PdfDictionary.DecodeParms);

				PdfArrayIterator filters = pdfObject.getMixedArray(PdfDictionary.Filter);

				//check not handled elsewhere
				int firstValue=PdfDictionary.Unknown;
				if(filters!=null && filters.hasMoreTokens())
					firstValue=filters.getNextValueAsConstant(false);

				if(debugStream)
					System.out.println("First filter="+firstValue);

				if (filters != null && firstValue!=PdfDictionary.Unknown && firstValue!=PdfFilteredReader.JPXDecode &&
						firstValue!=PdfFilteredReader.DCTDecode){

					if(debugStream)
						System.out.println("Decoding stream");
					try{

						byte[] globalData=null;//used by JBIG but needs to be read now so we can decode
						if(DecodeParms!=null){
							PdfObject Globals=DecodeParms.getDictionary(PdfDictionary.JBIG2Globals);
							if(Globals!=null)
								globalData=this.readStream(Globals,true,true,false, false,false, null);
						}
						stream =decodeFilters(DecodeParms, stream, filters ,width,height,true,globalData, cacheName);

					}catch(Exception e){
						LogWriter.writeLog("[PDF] Problem "+e+" decompressing stream ");
						stream=null;
						isCachedOnDisk=false; //make sure we return null, and not some bum values
					}

					//stop spurious match down below in caching code
					length=1;
				}else if(stream!=null && length!=-1 && length<stream.length ){

					//@simon - change here seems to have broken jbig
					/**make sure length correct*/
					//if(stream.length!=length){					
					if(stream.length!=length && length>0){//<--  last item breaks jbig??
						byte[] newStream=new byte[length];
						System.arraycopy(stream, 0, newStream, 0, length);

						stream=newStream;
					}else if(stream.length==1 && length==0)
						stream=new byte[0];
				}
			}


			if(stream!=null && cacheValue)
				pdfObject.setDecodedStream(stream);

			if(decompress && isCachedOnDisk){
				int streamLength = (int) new File(cacheName).length();

				byte[] bytes = new byte[streamLength];

				try {
					new BufferedInputStream(new FileInputStream(cacheName)).read(bytes);
				} catch (Exception e) {
					e.printStackTrace();
				}

				/**resize if length supplied*/
				if((length!=1)&&(length<streamLength)){

					/**make sure length correct*/
					byte[] newStream=new byte[length];
					System.arraycopy(bytes, 0, newStream, 0, length);

					bytes=newStream;

				}

				return bytes;
			}

		}else
			stream=data;

		if(stream==null)
			return null;

		//make a a DEEP copy so we cant alter
		int len=stream.length;
		byte[] copy=new byte[len];
		System.arraycopy(stream, 0, copy, 0, len);

		return  copy;
	}

	/**read object with stream offsets and return String of full path
	final public String getStreamOnDisk(String ref)  {

		int cacheSetting=miniumumCacheSize;

		this.miniumumCacheSize=0;

		Map obj= readObject(new PdfObject(ref), ref, null);

		//readStream((String)ref,true);

		miniumumCacheSize=cacheSetting;

		if(obj==null)
			return null;
		else
			return (String) obj.get("CachedStream");

	}/**/

	/**return size of PDF object if uncompressed or compressed block if compressed in bytes
	 *  -1 if not calculated
    public int getObjectSize(String objectRef) {

        int size=-1;

        if(!refTableInvalid){

            boolean isCompressed=isCompressed(objectRef);

            if(objectRef.endsWith(" R")){

                //any stream
                byte[] stream=null,raw=null;

                //read raw object data
                if(isCompressed){

                    int objectID=Integer.parseInt(objectRef.substring(0,objectRef.indexOf(' ')));
                    int compressedID=getCompressedStreamObject(objectRef);
                    String compressedRef=compressedID+" 0 R",startID=null;
                    Map compressedObject,offsetStart=lastOffsetStart,offsetEnd=lastOffsetEnd;
                    int First=lastFirst;
                    byte[] compressedStream;
                    boolean isCached=true; //assume cached

                    //see if we already have values
                    compressedStream=lastCompressedStream;
                    if(lastOffsetStart!=null)
                        startID=(String) lastOffsetStart.get(String.valueOf(objectID));

                    //read 1 or more streams
                    while(startID==null){
                        isCached=false;

                        try{
                            movePointer(compressedRef);
                        }catch(Exception e){
                            LogWriter.writeLog("Exception moving pointer to "+objectRef);
                        }

                        raw = readObjectData(this.ObjLengthTable[compressedID]);

                        size=ObjLengthTable[compressedID];

                        compressedObject=new HashMap();

                        convertObjectBytesToMap(compressedObject, pdfObject, objectRef,false, new HashMap(), false, false, stream, raw,false);

                        //get offsets table see if in this stream
                        offsetStart=new HashMap();
                        offsetEnd=new HashMap();
                        First=Integer.parseInt((String) compressedObject.get("First"));
                        compressedStream=this.readStream(compressedObject,objectRef,true,true,false, false,true);

                        extractCompressedObjectOffset(offsetStart, offsetEnd,First, compressedStream);

                        startID=(String) offsetStart.get(String.valueOf(objectID));

                        compressedRef=(String) compressedObject.get("Extends");

                    }

                    if(!isCached){
                        lastCompressedStream=compressedStream;
                        lastOffsetStart=offsetStart;
                        lastOffsetEnd=offsetEnd;
                        lastFirst=First;
                    }


                }else{

                    int pointer=objectRef.indexOf(' ');
                    int id=Integer.parseInt(objectRef.substring(0,pointer));

                    size=ObjLengthTable[id];

                }
            }

            if(size==0)
                size=-1;
        }

        return size;
    }  /**/

	/**read a stream*
	final public byte[] readStream(String ref,boolean decompress)  {

		Map currentValues=readObject(new PdfObject(ref), ref, null);

		return readStream(currentValues,ref,true,decompress,false, false,false);
	}/**/

	/**
	 * stop cache of last object in readObject
	 *
	 *
	final public void flushObjectCache(){
		lastRef=null;
	}

	final public void resetCache(){
		lastRef=null;
	}/**/

	/**
	 * read an object in the pdf into a Object which can be an indirect or an object
	 *
	 */
	final public void readObject(PdfObject pdfObject){

		String objectRef=pdfObject.getObjectRefAsString();

		final boolean debug=false;

		if(debug)
			System.err.println("reading objectRef="+objectRef+"< isCompressed="+isCompressed(objectRef));

		boolean isCompressed=isCompressed(objectRef);
		pdfObject.setCompressedStream(isCompressed);

		//any stream
		byte[] stream=null,raw=null;

		/**read raw object data*/
		if(isCompressed){

			int objectID=Integer.parseInt(objectRef.substring(0,objectRef.indexOf(' ')));
			int compressedID=getCompressedStreamObject(objectRef);
			String compressedRef=compressedID+" 0 R",startID=null;
			int First=lastFirst;
			boolean isCached=true; //assume cached

			//see if we already have values
			byte[] compressedStream=lastCompressedStream;
			Map offsetStart=lastOffsetStart;
			Map offsetEnd=lastOffsetEnd;

			PdfObject Extends=null;

			if(lastOffsetStart!=null && compressedID==lastCompressedID)
				startID=(String) lastOffsetStart.get(String.valueOf(objectID));

			//read 1 or more streams
			while(startID==null){

				if(Extends!=null){
					compressedObj=Extends;
				}else if(compressedID!=lastCompressedID){

					isCached=false;

					try{
						movePointer(compressedRef);
					}catch(Exception e){
						LogWriter.writeLog("Exception moving pointer to "+objectRef);
					}

					raw = readObjectData(this.ObjLengthTable[compressedID],null);

					compressedObj=new CompressedObject(compressedRef);
					readDictionaryAsObject(compressedObj, objectRef,0,raw, -1, "", false);

				}

				/**get offsets table see if in this stream*/
				offsetStart=new HashMap();
				offsetEnd=new HashMap();
				First=compressedObj.getInt(PdfDictionary.First);

				compressedStream=compressedObj.getDecodedStream();

				extractCompressedObjectOffset(offsetStart, offsetEnd,First, compressedStream);

				startID=(String) offsetStart.get(String.valueOf(objectID));

				Extends=compressedObj.getDictionary(PdfDictionary.Extends);
				if(Extends==null)
					break;

			}

			if(!isCached){
				lastCompressedStream=compressedStream;
				lastCompressedID=compressedID;
				lastOffsetStart=offsetStart;
				lastOffsetEnd=offsetEnd;
				lastFirst=First;
			}

			/**put bytes in stream*/
			int start=First+Integer.parseInt(startID),end=compressedStream.length;

			String endID=(String) offsetEnd.get(String.valueOf(objectID));
			if(endID!=null)
				end=First+Integer.parseInt(endID);

			int streamLength=end-start;
			raw = new byte[streamLength];
			System.arraycopy(compressedStream, start, raw, 0, streamLength);

		}else{
			try{
				movePointer(objectRef);
			}catch(Exception e){
				LogWriter.writeLog("Exception moving pointer to "+objectRef);
			}

			if(objectRef.charAt(0)=='<'){
				raw=readObjectData(-1, pdfObject);
			}else{
				int pointer=objectRef.indexOf(' ');
				int id=Integer.parseInt(objectRef.substring(0,pointer));

				if(ObjLengthTable==null || refTableInvalid){ //isEncryptionObject

					//allow for bum object
					if(this.getPointer()==0)
						raw=new byte[0];
					else
						raw=readObjectData(-1, pdfObject);


				}else if(id>ObjLengthTable.length || ObjLengthTable[id]==0){
					LogWriter.writeLog(objectRef+ " cannot have offset 0");
					raw=new byte[0];
				}else
					raw = readObjectData(ObjLengthTable[id], pdfObject);
			}
		}

		if(raw.length>1)
			readDictionaryAsObject(pdfObject, objectRef,0,raw, -1, "", false);


	}

    /**
	 * read an object in the pdf into a Object which can be an indirect or an object
	 *
	 */
	private byte[] readObjectData(PdfObject pdfObject){

		String objectRef=pdfObject.getObjectRefAsString();

		final boolean debug=false;

		if(debug)
			System.err.println("reading objectRef="+objectRef+"< isCompressed="+isCompressed(objectRef));

		boolean isCompressed=isCompressed(objectRef);
		pdfObject.setCompressedStream(isCompressed);

		//any stream
		byte[] stream=null,raw=null;

		/**read raw object data*/
		if(isCompressed){

			int objectID=Integer.parseInt(objectRef.substring(0,objectRef.indexOf(' ')));
			int compressedID=getCompressedStreamObject(objectRef);
			String compressedRef=compressedID+" 0 R",startID=null;
			int First=lastFirst;
			boolean isCached=true; //assume cached

			//see if we already have values
			byte[] compressedStream=lastCompressedStream;
			Map offsetStart=lastOffsetStart;
			Map offsetEnd=lastOffsetEnd;

			PdfObject Extends=null;

			if(lastOffsetStart!=null)
				startID=(String) lastOffsetStart.get(String.valueOf(objectID));

			//read 1 or more streams
			while(startID==null){

				if(Extends!=null){
					compressedObj=Extends;
				}else if(compressedID!=lastCompressedID){

					isCached=false;

					try{
						movePointer(compressedRef);
					}catch(Exception e){
						LogWriter.writeLog("Exception moving pointer to "+objectRef);
					}

					raw = readObjectData(this.ObjLengthTable[compressedID],null);

					compressedObj=new CompressedObject(compressedRef);
					readDictionaryAsObject(compressedObj, objectRef,0,raw, -1, "", false);

				}

				/**get offsets table see if in this stream*/
				offsetStart=new HashMap();
				offsetEnd=new HashMap();
				First=compressedObj.getInt(PdfDictionary.First);

				compressedStream=compressedObj.getDecodedStream();;

				extractCompressedObjectOffset(offsetStart, offsetEnd,First, compressedStream);

				startID=(String) offsetStart.get(String.valueOf(objectID));

				Extends=compressedObj.getDictionary(PdfDictionary.Extends);
				if(Extends==null)
					break;

			}

			if(!isCached){
				lastCompressedStream=compressedStream;
				lastCompressedID=compressedID;
				lastOffsetStart=offsetStart;
				lastOffsetEnd=offsetEnd;
				lastFirst=First;
			}

			/**put bytes in stream*/
			int start=First+Integer.parseInt(startID),end=compressedStream.length;

			String endID=(String) offsetEnd.get(String.valueOf(objectID));
			if(endID!=null)
				end=First+Integer.parseInt(endID);

			int streamLength=end-start;
			raw = new byte[streamLength];
			System.arraycopy(compressedStream, start, raw, 0, streamLength);

		}else{
			try{
				movePointer(objectRef);
			}catch(Exception e){
				LogWriter.writeLog("Exception moving pointer to "+objectRef);
			}

			if(objectRef.charAt(0)=='<'){
				raw=readObjectData(-1, pdfObject);
			}else{
				int pointer=objectRef.indexOf(' ');
				int id=Integer.parseInt(objectRef.substring(0,pointer));

				if(ObjLengthTable==null || refTableInvalid){ //isEncryptionObject

					//allow for bum object
					if(this.getPointer()==0)
						raw=new byte[0];
					else
						raw=readObjectData(-1, pdfObject);


				}else if(id>ObjLengthTable.length || ObjLengthTable[id]==0){
					LogWriter.writeLog(objectRef+ " cannot have offset 0");
					raw=new byte[0];
				}else
					raw = readObjectData(ObjLengthTable[id], pdfObject);
			}
		}

		return raw;

	}





	/**
	 * read an object in the pdf into a Map which can be an indirect or an object
	 *
	 *
	final synchronized public Map readObject(PdfObject pdfObject, String objectRef, Map textFields)  {

		//return if last read otherwise read
		if(lastRef!=null && objectRef!=null && objectRef.equals(lastRef) && pdfObject.isImplemented()==PdfObject.NO){
			return objData;
		}else{

			//not cached if we read just PdfObject (needed as hack for Type3)
			if(pdfObject.isImplemented()!=PdfObject.FULL)
				lastRef=objectRef;

			boolean debug=false,preserveTextString=false;
			objData=new HashMap();

			//set flag to extract raw text string
			if((textFields!=null)){
				preserveTextString=true;
			}else
				preserveTextString=false;

			if(debug)
				System.err.println("reading objectRef="+objectRef+"< isCompressed="+isCompressed(objectRef));

			boolean isCompressed=isCompressed(objectRef);
			pdfObject.setCompressedStream(isCompressed);

			if(objectRef.endsWith(" R")){

				//any stream
				byte[] stream=null,raw=null;

				//read raw object data
				if(isCompressed){

					int objectID=Integer.parseInt(objectRef.substring(0,objectRef.indexOf(' ')));
					int compressedID=getCompressedStreamObject(objectRef);
					String compressedRef=compressedID+" 0 R",startID=null;
					Map offsetStart=lastOffsetStart,offsetEnd=lastOffsetEnd;
					int First=lastFirst;
					byte[] compressedStream;
					boolean isCached=true; //assume cached

					//see if we already have values
					compressedStream=lastCompressedStream;
					if(lastOffsetStart!=null)
						startID=(String) lastOffsetStart.get(String.valueOf(objectID));

					PdfObject compressedObj,Extends=null;

					//read 1 or more streams
					while(startID==null){

						if(Extends!=null){
							compressedObj=Extends;
						}else{
							isCached=false;
							try{
								movePointer(compressedRef);
							}catch(Exception e){
								LogWriter.writeLog("Exception moving pointer to "+objectRef);
							}

							raw = readObjectData(this.ObjLengthTable[compressedID]);

							compressedObj=new PdfCompressedObject(compressedRef);
							readDictionaryAsObject(compressedObj, objectRef,0,raw, -1, "", false);
						}

						//get offsets table see if in this stream
						offsetStart=new HashMap();
						offsetEnd=new HashMap();

						First=compressedObj.getInt(PdfDictionary.First);

						compressedStream=compressedObj.DecodedStream;

						extractCompressedObjectOffset(offsetStart, offsetEnd,First, compressedStream);

						startID=(String) offsetStart.get(String.valueOf(objectID));

						Extends=compressedObj.getDictionary(PdfDictionary.Extends);

					}

					if(!isCached){
						lastCompressedStream=compressedStream;
						lastOffsetStart=offsetStart;
						lastOffsetEnd=offsetEnd;
						lastFirst=First;
					}

					//put bytes in stream
					int start=First+Integer.parseInt(startID),end=compressedStream.length;

					String endID=(String) offsetEnd.get(String.valueOf(objectID));
					if(endID!=null)
						end=First+Integer.parseInt(endID);

					int streamLength=end-start;
					raw = new byte[streamLength];
					System.arraycopy(compressedStream, start, raw, 0, streamLength);

				}else{
					try{
						movePointer(objectRef);
					}catch(Exception e){
						LogWriter.writeLog("Exception moving pointer to "+objectRef);
					}
					int pointer=objectRef.indexOf(' ');
					int id=Integer.parseInt(objectRef.substring(0,pointer));

					if(ObjLengthTable==null || refTableInvalid)
						raw=readObjectData(-1);
					else if(id>ObjLengthTable.length || ObjLengthTable[id]==0){
						LogWriter.writeLog(objectRef+ " cannot have offset 0");
						raw=new byte[0];
					}else
						raw = readObjectData(ObjLengthTable[id]);

				}

				if(debug)
					System.out.println("convertObjectsToMap");

				if(startStreamPointer!=-1 || raw.length>1){

					try{

						if(pdfObject.isImplemented()!=PdfObject.NO)
							readDictionaryAsObject(pdfObject, objectRef,0,raw, -1, "", false);

					}catch(Exception e){
						e.printStackTrace();

					}

					//@speed
					if(pdfObject.isImplemented()!=PdfObject.FULL)
						convertObjectBytesToMap(objData, objectRef, textFields, debug, preserveTextString, stream, raw,isCompressed);
				}
				if(debug)
					System.out.println("converted");
			}else{

				byte[] bytes=objectRef.getBytes();

				if(bytes.length>0)
					readDictionary(objectRef,1,objData,0,bytes, textFields,-1);

				LogWriter.writeLog("Direct object read "+objectRef+"<<");

			}

			if(debug)
				System.out.println("object read");

			return objData;
		}
	}/**/


	/**
	 * get object as byte[]
	 * @param objectRef is only needed if compressed
	 * @param isCompressed
	 * @param objectID
	 * @param gen
	 * @return
	 */
	private byte[] readObjectAsByteArray(PdfObject pdfObject,String objectRef, boolean isCompressed, int objectID, int gen) {

		byte[] raw;

		/**read raw object data*/
		if(isCompressed){

            int compressedID=getCompressedStreamObject(objectID,gen);
			String startID=null,compressedRef;
			Map offsetStart=lastOffsetStart,offsetEnd=lastOffsetEnd;
			int First=lastFirst;
			byte[] compressedStream;
			boolean isCached=true; //assume cached

			PdfObject compressedObj, Extends;

			//see if we already have values
			compressedStream=lastCompressedStream;
			if(lastOffsetStart!=null)
				startID=(String) lastOffsetStart.get(String.valueOf(objectID));

			//read 1 or more streams
			while(startID==null){

				isCached=false;
				try{
					movePointer(compressedID,0);
				}catch(Exception e){
					LogWriter.writeLog("Exception moving pointer to "+objectID);
				}

                raw = readObjectData(this.ObjLengthTable[compressedID],null);
				
				//may need to use compObj and not objectRef
				String compref=compressedID+" "+gen+" R";
				compressedObj=new CompressedObject(compref);
				readDictionaryAsObject(compressedObj, objectRef,0,raw, -1, "", false);


				/**get offsets table see if in this stream*/
				offsetStart=new HashMap();
				offsetEnd=new HashMap();

				First=compressedObj.getInt(PdfDictionary.First);

				//                if(isEncrypted){
				//                    byte[] bytes=((byte[])compressedObject.get("Stream"));
				//
				//                    try{
				//                        bytes=decrypt(bytes,compressedID+" 0 R", false,null,false,false);
				//                    }catch(Exception ee){
				//
				//                        ee.printStackTrace();
				//                    }
				//                    compressedObject.put("Stream",bytes);
				//
				//                }

				//do later due to code above
				compressedStream=compressedObj.getDecodedStream();

				//start
				//compressedStream=this.readStream(compressedObject,objectRef,true,true,false, false,false);
				//PdfReader.checkStreamsIdentical(compressedStream, oldCompressedStream);
				//////////////////////////////////////


				extractCompressedObjectOffset(offsetStart, offsetEnd,First, compressedStream);

				startID=(String) offsetStart.get(String.valueOf(objectID));

				Extends=compressedObj.getDictionary(PdfDictionary.Extends);
				if(Extends==null)
					compressedRef=null;
				else
					compressedRef=Extends.getObjectRefAsString();

				if(compressedRef!=null)
					compressedID=Integer.parseInt(compressedRef.substring(0,compressedRef.indexOf(' ')));

			}

			if(!isCached){
				lastCompressedStream=compressedStream;
				lastOffsetStart=offsetStart;
				lastOffsetEnd=offsetEnd;
				lastFirst=First;
			}

			/**put bytes in stream*/
			int start=First+Integer.parseInt(startID),end=compressedStream.length;
			String endID=(String) offsetEnd.get(String.valueOf(objectID));
			if(endID!=null)
				end=First+Integer.parseInt(endID);

			int streamLength=end-start;
			raw = new byte[streamLength];
			System.arraycopy(compressedStream, start, raw, 0, streamLength);

		}else{
			try{
				movePointer(objectID,gen);
			}catch(Exception e){
				LogWriter.writeLog("Exception moving pointer to "+objectRef);
			}

			if(ObjLengthTable==null || refTableInvalid)
				raw=readObjectData(-1,pdfObject);
			else if(objectID>ObjLengthTable.length)
				return null;
			else
				raw = readObjectData(ObjLengthTable[objectID],pdfObject);
        }

		return raw;
	}

	/**
	 * @param First
	 * @param compressedStream
	 */
	private static void extractCompressedObjectOffset(Map offsetStart, Map offsetEnd,int First, byte[] compressedStream) {


		String lastKey=null,key=null,offset=null;

		final boolean debug=true;
		StringBuffer rawKey=null,rawOffset=null;
		int startKey=0,endKey=0,startOff=0,endOff=0;

		//read the offsets table
		for(int ii=0;ii<First;ii++){

			if(debug){
				rawKey=new StringBuffer();
				rawOffset=new StringBuffer();
			}

			/**work out key size*/
			startKey=ii;
			while(compressedStream[ii]!=32 && compressedStream[ii]!=13 && compressedStream[ii]!=10){
				if(debug)
					rawKey.append((char)compressedStream[ii]);
				ii++;
			}
			endKey=ii-1;

			/**extract key*/
			int length=endKey-startKey+1;
			char[] newCommand=new char[length];
			for(int i=0;i<length;i++)
				newCommand[i]=(char)compressedStream[startKey+i];

			key =new String(newCommand);

			/**test key if in debug*/
			if(debug){
				if(!key.equals(rawKey.toString()))
					throw new RuntimeException("Different="+key+"<>"+rawKey+ '<');

			}

			/**move to offset*/
			while(compressedStream[ii]==32 || compressedStream[ii]==13 || compressedStream[ii]==10)
				ii++;

			/**get size*/
			startOff=ii;
			while((compressedStream[ii]!=32 && compressedStream[ii]!=13 && compressedStream[ii]!=10)&&(ii<First)){

				if(debug)
					rawOffset.append((char)compressedStream[ii]);

				ii++;
			}
			endOff=ii-1;

			/**extract offset*/
			length=endOff-startOff+1;
			newCommand=new char[length];
			for(int i=0;i<length;i++)
				newCommand[i]=(char)compressedStream[startOff+i];

			offset =new String(newCommand);

			/**test key if in debug*/
			if(debug){
				if(!offset.equals(rawOffset.toString()))
					throw new RuntimeException("Different="+offset+"<>"+rawOffset+ '<');

			}

			/**
			 * save values
			 */
			offsetStart.put(key,offset);

			//save end as well
			if(lastKey!=null)
				offsetEnd.put(lastKey,offset);

			lastKey=key;

		}
	}



	/**private void convertObjectBytesToMap(Map objData, String objectRef, Map textFields,
			boolean debug, boolean preserveTextString,
			byte[] stream, byte[] raw, boolean isCompressed) {

		//get values
		int i = 0;

		ByteArrayOutputStream rawStringAsBytes=new ByteArrayOutputStream();

		char remainderLastChar=' ';

		StringBuffer remainder=new StringBuffer();

		if(!isCompressed){

			//remove the obj start
			while (true) {

				if ((raw[i] == 111)&& (raw[i + 1] == 98)&& (raw[i + 2] == 106))
					break;
				i++;
			}

			i = i + 2;

			//make sure no comment afterwards by rolling onto next CR or < or [ or /
			while(true){

				if(raw[i]==47) //allow for command right after obj
					break;

				i++;
				//System.out.println(i+" "+(char)raw[i]+" "+raw[i]);
				if((raw[i]==10)|(raw[i]==13)|(raw[i]==60)|(raw[i]==91)|(raw[i]==32))
					break;
			}
		}

		if(debug){
			for(int j=i;j<raw.length - 7;j++)
				System.err.print((char)raw[j]);

			System.err.print("<===\n\n");
		}

		//allow for immediate command
		if((raw[i]==47)|(raw[i]==91)) //allow for command or array right after obj
			i--;

		//look for trailer keyword
		while (i < raw.length - 7) {

			i++;

			if(debug)
				System.err.println((char)raw[i]);

			//trap for no endObj
			if(raw[i]=='o' && raw[i+1]=='b' && raw[i+2]=='j')
				break;

			//read a subdictionary
			if ((raw[i] == 60) && ((raw[i + 1] == 60)| (raw[i - 1] == 60))){

				if(raw[i - 1] != 60)
					i++;

				if(debug)
					System.err.println("Read dictionary");
				i=readDictionary(objectRef,1,objData,i,raw, textFields,-1);

				//handle a stream
			}else if ((raw[i] == 115)&& (raw[i + 1] == 116)&& (raw[i + 2] == 114)&& (raw[i + 3] == 101)&& (raw[i + 4] == 97)&& (raw[i + 5] == 109)) {

				if(debug)
					System.err.println("Reading stream");

				//ignore these characters and first return
				i = i + 6;

				if (raw[i] == 13 && raw[i+1] == 10) //allow for double linefeed
					i=i+2;
				else if((raw[i]==10)|(raw[i]==13))
					i++;

				int start = i;

				i--; //move pointer back 1 to allow for zero length stream

				int streamLength=0;
				String setLength=(String)objData.get("Length");
				if(setLength!=null){
					//read indirect
					if(setLength.indexOf(" R")!=-1){
						//read raw object data
						try{
							long currentPos=movePointer(setLength);
							int buffSize=128;
							if(currentPos+buffSize>eof)
								buffSize=(int) (eof-currentPos-1);
							StringBuffer rawChars=new StringBuffer();
							byte[] buf=new byte[buffSize];
							this.pdf_datafile.read(buf);

							int ii=3;

							//find start
							while(true){
								if((ii<buffSize)&&(buf[ii-3]==111)&&(buf[ii-2]==98)&&(buf[ii-1]==106))
									break;
								ii++;
							}

							//find first number
							while(true){
								if((ii<buffSize)&&(Character.isDigit((char)buf[ii])))
									break;
								ii++;
							}

							//read number
							while(true){
								if((ii<buffSize)&&(Character.isDigit((char)buf[ii]))){
									rawChars.append((char)buf[ii]);
									ii++;
								}else
									break;
							}

							movePointer(currentPos);
							setLength=rawChars.toString();

						}catch(Exception e){
							LogWriter.writeLog("Exception moving pointer to "+objectRef);
							setLength=null;
						}
					}

					if(setLength!=null){

						streamLength=Integer.parseInt(setLength);

						i=start+streamLength;


						if((i<raw.length) && raw[i]==13 && (i+1<raw.length) && raw[i+1]==10)
							i=i+2;

						//check validity
						if ((raw.length>(i+9))&&(raw[i] == 101)&& (raw[i + 1] == 110)&& (raw[i + 2] == 100)&& (raw[i + 3] == 115)&& (raw[i + 4] == 116)
								&& (raw[i + 5] == 114)&& (raw[i + 6] == 101)&& (raw[i + 7] == 97)&& (raw[i + 8] == 109)){

						}else{
							boolean	isValid=false;
							int current=i;
							//check forwards
							if(i<raw.length){
								while(true){
									i++;
									if((isValid)||(i==raw.length))
										break;

									if ((raw[i] == 101)&& (raw[i + 1] == 110)&& (raw[i + 2] == 100)&& (raw[i + 3] == 115)&& (raw[i + 4] == 116)
											&& (raw[i + 5] == 114)&& (raw[i + 6] == 101)&& (raw[i + 7] == 97)&& (raw[i + 8] == 109)){

										//while(raw[i-1]==10 || raw[i-1]==13)
										//	i--;

										streamLength=i-start;
										isValid=true;
									}
								}
							}

							if(!isValid){
								i=current;
								if(i>raw.length)
									i=raw.length;
								//check backwords
								while(true){
									i--;
									if((isValid)||(i<0))
										break;
									if ((raw[i] == 101)&& (raw[i + 1] == 110)&& (raw[i + 2] == 100)&& (raw[i + 3] == 115)&& (raw[i + 4] == 116)
											&& (raw[i + 5] == 114)&& (raw[i + 6] == 101)&& (raw[i + 7] == 97)&& (raw[i + 8] == 109)){
										streamLength=i-start;
										isValid=true;

									}
								}
							}

							if(!isValid)
								i=current;
						}
					}
				}else{

					//workout length and check if length set
					int end;

					while (true) { //find end

						i++;

						if(i==raw.length)
							break;
						if ((raw[i] == 101)&& (raw[i + 1] == 110)&& (raw[i + 2] == 100)&& (raw[i + 3] == 115)&& (raw[i + 4] == 116)
								&& (raw[i + 5] == 114)&& (raw[i + 6] == 101)&& (raw[i + 7] == 97)&& (raw[i + 8] == 109))
							break;

					}

					end=i-1;

					if((end>start))
						streamLength=end-start+1;
				}

				//lose trailing 10s
				if(streamLength>1){
					int ptr=start+streamLength-1;
					if(ptr<raw.length && ptr>0 && raw[ptr]==10){
						streamLength--;
						ptr--;
					}
				}
                  xx

				// either read stream into object from memory or just save position in Map

				if((startStreamPointer==-1) ||(debugCaching)){

					if(start+streamLength>raw.length)
						streamLength=raw.length-start;

					stream = new byte[streamLength];
					System.arraycopy(raw, start, stream, 0, streamLength);

				}

				if(startStreamPointer!=-1){

					objData.put("startStreamOnDisk",  new Integer(this.startStreamPointer));
					objData.put("endStreamOnDisk",  new Integer(this.endStreamPointer));

					//debug code
					if(debugCaching){
						try{
							if(start+streamLength>raw.length)
								streamLength=raw.length-start;

							byte[] stream2 = new byte[streamLength];
							System.arraycopy(raw, start, stream2, 0, streamLength);

							int cacheLength=endStreamPointer-startStreamPointer+1;

							//check it matches
							int xx=0;
							for(int jj=this.startStreamPointer;jj<this.endStreamPointer;jj++){
								byte[] buffer = new byte[1];

								//get bytes into buffer
								this.movePointer(jj);
								this.pdf_datafile.read(buffer);

								if(buffer[0]!=stream2[xx])
									throw new RuntimeException("error here");


								xx++;
							}

							if((cacheLength!=streamLength)){//&& (setLength==null)){
								System.out.println("\n");
								throw new RuntimeException("lengths cache changed="+cacheLength+" array="+streamLength+" set="+setLength);

							}

						}catch(Exception e){
							System.out.println("ERRor in debug code");
							e.printStackTrace();
						}
					}
				}

				i = i + 9; //roll on pointer

				i=raw.length;
				
			}else if(raw[i]==91){ //handle just a raw array ie [ /Separation /CMAN /DeviceCMYK 8 0 R]
				if(debug)
					System.err.println("read array");

				i=readArray(objectRef,i,objData,raw, textFields);

			}else if((raw[i]!=60)&(raw[i]!=62)){ //direct value

				if(preserveTextString){

					//allow for all escape combinations
					if((raw[i-1]==92)&&(raw[i-2]==92)){
						//stop match on //( or //)
						rawStringAsBytes.write(raw[i]);
					}else if(((raw[i]==40)|(raw[i]==41))&(raw[i-1]!=92)){
						//ignore //
					}else
						rawStringAsBytes.write(raw[i]);
				}

				if(((raw[i]==10)|(raw[i]==13)|(raw[i]==32))){

					if(remainder.length()>0)
						remainder.append(' ');

					remainderLastChar=' ';

				}else{

					//allow for no spaces in /a/b/c
					if((raw[i]==47)&&(remainderLastChar!=' '))
						remainder.append(' ');

					remainderLastChar=(char)raw[i];
					remainder.append(remainderLastChar);
				}
			}

			//}else if((raw[i]!=60)&(raw[i]!=62)&(raw[i]!=10)&(raw[i]!=13)&(raw[i]!=32)){ //direct value
        			remainder.append((char)raw[i]);				}
		}

		//strip any comment from remainder
		if(remainder.length()>0)	{
			int ii=remainder.toString().indexOf('%');
			if(ii>-1)
				remainder.setLength(ii);
		}

		if(remainder.length()>0)	{
			String rawString=remainder.toString().trim();
			if((preserveTextString)&&(rawString.startsWith("("))){
				try {
					rawStringAsBytes.close();

					byte[] streamData=rawStringAsBytes.toByteArray();
					streamData=decrypt(streamData,objectRef, false,null,false,false);
					objData.put("rawValue",streamData); //save pair and reset

				} catch (Exception e) {
					LogWriter.writeLog("Exception "+e+" writing out text string");
				}

			}else{
				if(debug)
					System.err.println("Remainder value="+remainder+"<<");
				objData.put("rawValue",rawString);
			}
		}

		if(stream!=null)
			objData.put("Stream",  stream);

		if(debug)
			System.err.println(objData);

		try {
			rawStringAsBytes.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}/**/


	/**
	 * read an array
	 *
	private int readArray(String objectRef, int i, Map objData, byte[] raw, Map textFields){


		final boolean debug=false;
		int start=0,end=0;
		boolean maybeKey=false,isSeparation=false;
		
		StringBuffer rawValue=new StringBuffer();
		StringBuffer possKey=new StringBuffer();
		
		boolean containsIndexKeyword=false,convertToHex=false;

		while(true){


			if(debug)
				System.out.println("Raw="+rawValue +"start="+start+" end="+end);

			if(maybeKey){

				int j=i;

				if(debug)
					System.out.println("Poss key char="+(char)raw[j]);

				//find first valid char
				while((raw[j]==13)|(raw[j]==10)|(raw[j]==32))
					j++;

				if(debug)
					System.out.println("now="+(char)raw[j]);
				if((raw[j]==60)&&(raw[j+1]==60)){

					if(debug)
						System.out.println("Poss key");

					i=j;

					if(isSeparation){

						if(debug)
							System.out.println("Store in same level "+possKey);

						rawValue.append(possKey);
						rawValue.append(' ');
						
						i=readDictionary(objectRef,1,objData,i,raw, textFields,-1);

					}else{
						Map subDictionary=new HashMap();
						objData.put(possKey.substring(1),subDictionary);
						i=readDictionary(objectRef,1,subDictionary,i,raw, textFields,-1);

						if(debug)
							System.out.println("Sub dictionary="+subDictionary);
					}

					//roll on if needed
					if(raw[i]==62)
						i++;
					possKey=new StringBuffer();
					
				}else{

					if(debug)
						System.out.println("Get value");

					if(rawValue.charAt(rawValue.length()-1)!=' ')						
						rawValue.append(' ');
						

					rawValue.append(possKey);
					rawValue.append(' ');

					possKey=new StringBuffer();
					
					maybeKey=false;

					i--;

					if(debug)
						System.out.println("Value="+rawValue);

				}

				//identify possible keys and read

			}else if(!convertToHex && raw[i]==47){

				if(debug)
					System.out.println("Found /");

				maybeKey=true;
				while(true){
					possKey.append((char)raw[i]);
					
					i++;

					if((raw[i]==47)||(raw[i]==13)||(raw[i]==10)||(raw[i]==32)||(raw[i]==60)||(raw[i]==91)||(raw[i]==93))
						break;

				}

				//allow for no space as in
				if((raw[i]==47)||(raw[i]==91)||(raw[i]==93)||(raw[i]==60))
					i--;
				if(debug)
					System.out.println("Key="+possKey+ '<');

				if(possKey.toString().equals("/Separation")){
					isSeparation=true;
				}else if(possKey.toString().equals("/Indexed"))
					containsIndexKeyword=true;

				//track [/Indexed /xx ()] with binary values in () and convert to hex string
			}else if((raw[i]==40)&&(raw[i-1]!=92)&&(containsIndexKeyword)){
				convertToHex=true;

				rawValue.append(" <");
				

			}else if(convertToHex){ //end of stream
				if((raw[i]==41)&&(raw[i-1]!=92)){
					rawValue.append('>');
					
					convertToHex=false;
				}else{ //values

					String hex_value=null;

					//allow for escaped octal up to 3 chars
					if(raw[i]=='\\' && raw[i+1]!=13 && raw[i+1]!=10 && raw[i+1]!=114){
						StringBuffer octal=new StringBuffer(3);
						int count=0;
						for(int ii=1;ii<4;ii++){

							char c=(char)raw[i+1];

							if(c<48 || c>57)
								break;

							octal.append(c);
							count++;

							i++;
						}

						if(count>0)
							hex_value=Integer.toHexString(Integer.parseInt(octal.toString(),8));
					}

					if(hex_value==null)
						hex_value = Integer.toHexString((raw[i]) & 255);
					//pad with 0 if required
					if (hex_value.length() < 2)
						rawValue.append('0');
					
					rawValue.append(hex_value);
					

				}
				//all other cases
			}else{

				//if(debug)
				//	System.out.println("Else"+" "+(char)raw[i]);

				if((i>0)&&(raw[i-1]==47)) //needed for [/Indexed /DeviceCMYK 60 1 0 R] to get second /
					rawValue.append('/');
				
				if ((raw[i] == 13) || (raw[i] == 10)){ //added as lines split in ghostscript output
					rawValue.append(' ');
					
				}else {

					if((raw[i]=='<')&&(raw[i-1]!=' ' && raw[i-1]!='<')) //make sure < always has a space before it
						rawValue.append(' ');
						
					if((i>0)&&(raw[i-1]==93)) //make sure ] always has a space after it
						rawValue.append(' ');
					
					rawValue.append((char) raw[i]);
					
				}
				if((i==0)||((i>0)&&(raw[i-1]!=92))){
					if(raw[i]==91)
						start++;
					else if(raw[i]==93)
						end++;
				}
			}

			if((raw[i]==93)&(start==end))
				break;
			i++;

		}
		objData.put("rawValue",rawValue.toString().trim());

		if(debug)
			System.out.println(rawValue+"<>"+objData);

		return i;
	}/**/

	/**
	 * read FDF
	 */
	final public PdfObject readFDF() throws PdfException{

		int eof=-1,start=-1;

		PdfObject fdfObj;

		try{
			eof = (int) pdf_datafile.length();

			pdf_datafile.readLine(); //lose first line with definition
			start=(int)pdf_datafile.getFilePointer();

			eof=eof-start;
			byte[] fileData=new byte[eof];
			this.pdf_datafile.read(fileData);
			
			fdfObj=new FDFObject("1 0 R");

			//find /FDF key
			int ii=0;
			while(ii<eof){
				if(fileData[ii]=='/' && fileData[ii+1]=='F' 
					&& fileData[ii+2]=='D' && fileData[ii+3]=='F')
					break;
				
				ii++;
			}
			
			ii=ii+4;
			
			//move beyond <<
			while(ii<eof){
				if(fileData[ii]=='<' && fileData[ii+1]=='<')
					break;
				
				ii++;
			}
			ii=ii+2;
			readDictionaryAsObject( fdfObj, "1 0 R", ii, fileData, fileData.length, "", false);
        
		} catch (Exception e) {
			try {
				this.pdf_datafile.close();
			} catch (IOException e1) {
				LogWriter.writeLog("Exception "+e+" closing file");
			}

			throw new PdfException("Exception " + e + " reading trailer");
		}

		return fdfObj;
	}

	/**give user access to internal flags such as user permissions*/
	public int getPDFflag(Integer flag) {

		if(flag.equals(PDFflags.USER_ACCESS_PERMISSIONS))
			return P;
		else if(flag==PDFflags.VALID_PASSWORD_SUPPLIED)
			return passwordStatus;
		else
			return -1;

	}

	/**
	 * read reference table start to see if new 1.5 type or traditional xref
	 * @throws PdfException
	 */
	final public PdfObject readReferenceTable() throws PdfException {

		int pointer = readFirstStartRef(),eof=(int)this.eof;
		xref.addElement(pointer);

        if(pointer>=eof){

			LogWriter.writeLog("Pointer not if file - trying to manually find startref");

			refTableInvalid=true;
			return findOffsets();
		}else if(isCompressedStream(pointer,eof))
            return readCompressedStream(pointer);
		else
			return readLegacyReferenceTable(pointer,eof);
	}

	/** Utility method used during processing of type1C files */
	static final private int getWord(byte[] content, int index, int size) {
		int result = 0;
		for (int i = 0; i < size; i++) {
			result = (result << 8) + (content[index + i] & 0xff);

		}
		return result;
	}

	/**
	 * read 1.5 compression stream ref table
	 * @throws PdfException
	 */
	private PdfObject readCompressedStream(int pointer) throws PdfException {

		PdfObject encryptObj=null, rootObj=null;

		while (pointer != -1) {

			/**
			 * get values to read stream ref
			 */

			/**read raw object data*/
			try{
				movePointer(pointer);
			}catch(Exception e){
				LogWriter.writeLog("Exception moving pointer to "+ pointer);
			}

			byte[] raw = readObjectData(-1,null);

			/**read the object name from the start*/
			StringBuffer objectName=new StringBuffer();
			char current1,last=' ';
			int matched=0, i1 =0;
			while(i1 <raw.length){
				current1 =(char)raw[i1];

				//treat returns same as spaces
				if(current1 ==10 || current1 ==13)
					current1 =' ';

				if(current1 ==' ' && last==' '){//lose duplicate or spaces
					matched=0;
				}else if(current1 ==pattern.charAt(matched)){ //looking for obj at end
					matched++;
				}else{
					matched=0;
					objectName.append(current1);
				}
				if(matched==3)
					break;
				last= current1;
				i1++;
			}

			//add end and put into Map
			objectName.append('R');
			String ref=objectName.toString();

			PdfObject pdfObject=new CompressedObject(ref);
			pdfObject.setCompressedStream(true);
			readDictionaryAsObject(pdfObject, ref,0,raw, -1, "", false);

			//read the field sizes
			int[] fieldSizes=pdfObject.getIntArray(PdfDictionary.W);

			//read the xrefs stream
			byte[] xrefs=pdfObject.getDecodedStream();

			//if encr
			if(xrefs==null)
				xrefs=readStream(pdfObject,true,true,false, false, true, null);


			int[] Index=pdfObject.getIntArray(PdfDictionary.Index);
			if(Index==null){ //single set of values

				//System.out.println("-------------1.Offsets-------------"+current+" "+numbEntries);
				readCompressedOffsets(0, 0, pdfObject.getInt(PdfDictionary.Size), fieldSizes, xrefs);

			}else{ //pairs of values in Index[] array
				int count=Index.length,pntr=0;

				for(int aa=0;aa<count;aa=aa+2){

					//System.out.println("-------------2.Offsets-------------"+Index[aa]+" "+Index[aa+1]);

					pntr=readCompressedOffsets(pntr,Index[aa], Index[aa+1], fieldSizes, xrefs);
				}
			}

			/**
			 * now process trailer values - only first set of table values for
			 * root, encryption and info
			 */
			if (rootObj==null) {

				rootObj=pdfObject.getDictionary(PdfDictionary.Root);

				/**
				 * handle encryption
				 */
				encryptObj=pdfObject.getDictionary(PdfDictionary.Encrypt);

				if (encryptObj != null) {

					byte[][] IDs=pdfObject.getStringArray(PdfDictionary.ID);
					if(IDs!=null)
						this.ID=IDs[0];
				}

				infoObject=pdfObject.getDictionary(PdfDictionary.Info);

			}

			//make sure first values used if several tables and code for prev
			pointer=pdfObject.getInt(PdfDictionary.Prev);
		}

		if(encryptObj!=null)
			readEncryptionObject(encryptObj.getUnresolvedData());

		calculateObjectLength();

		return rootObj;
	}

    private int readCompressedOffsets(int pntr, int current, int numbEntries, int[] fieldSizes, byte[] xrefs) throws PdfException {
        //now parse the stream and extract values

        final boolean debug=false;

        if(debug)
        System.out.println("===============read offsets============= current="+current+" numbEntries="+numbEntries);
        
        int[] defaultValue={1,0,0};

        for(int i=0;i<numbEntries;i++){

            //read the next 3 values
            int[] nextValue=new int[3];
            for(int ii=0;ii<3;ii++){
                if(fieldSizes[ii]==0){
                    nextValue[ii]=defaultValue[ii];
                }else{
                    nextValue[ii]=getWord(xrefs,pntr,fieldSizes[ii]);
                    pntr=pntr+fieldSizes[ii];
                }
            }

            //handle values appropriately
            int id=0,gen;
            switch(nextValue[0]){
            case 0: //linked list of free objects
                current++;

                if(debug)
                System.out.println("case 0 nextFree="+nextValue[1]+" gen="+nextValue[2]);

                break;
            case 1: //non-compressed
                id=nextValue[1];
                gen=nextValue[2];

                if(debug)
                System.out.println("case 1   current="+current+" byteOffset="+nextValue[1]+" gen="+nextValue[2]);

                storeObjectOffset(current, id, gen,false);

                current++;
                break;
            case 2: //compressed
                id=nextValue[1];
                gen=nextValue[2];

                if(debug)
                System.out.println("case 2  current="+current+" object number="+nextValue[1]+" index="+nextValue[2]);

                storeObjectOffset(current, id, 0,true);

                current++;

                break;
            default:

                //System.out.println(" -> nextValue[0] = " + nextValue[0]);

                throw new PdfException("Exception Unsupported Compression mode with value "+nextValue[0]);
            //current++;
            //break;
            }
        }

        return pntr;
    }

    /**
	 * test first bytes to see if new 1.5 style table with obj or contains ref
	 * @throws PdfException
	 */
	private boolean isCompressedStream(int pointer,int eof) throws PdfException {

		final boolean debug=false;

		int bufSize = 50,charReached=0;

		final int[] objStm={'O','b','j','S','t','m'};
		final int[] XRef={'X','R','e','f'};

		final int UNSET=-1;
		final int COMPRESSED=1;
		final int LEGACY=2;
		int type=UNSET;

		while (true) {

			/** adjust buffer if less than 1024 bytes left in file */
			if (pointer + bufSize > eof)
				bufSize = eof - pointer;

			if(bufSize<0)
				bufSize=50;
			byte[] buffer = new byte[bufSize];

			/** get bytes into buffer */
			movePointer(pointer);

			try{
				pdf_datafile.read(buffer);
			} catch (Exception e) {
				e.printStackTrace();
				throw new PdfException("Exception " + e + " scanning trailer for ref or obj");
			}

			/**look for xref or obj */
			for (int i = 0; i < bufSize; i++) {

				byte currentByte = buffer[i];

				if(debug)
					System.out.print((char)currentByte);

				/** check for xref OR end - reset if not */
				if ((currentByte == oldPattern[charReached])&&(type!=COMPRESSED)){
					charReached++;
					type=LEGACY;
				}else if ((currentByte == objStm[charReached] || currentByte == XRef[charReached])&& type!=LEGACY){
					charReached++;
					type=COMPRESSED;
				}else{
					charReached = 0;
					type=UNSET;
				}

				if (charReached == 3)
					break;

			}

			if(charReached==3)
				break;

			//update pointer
			pointer = pointer + bufSize;

		}

		/**
		 * throw exception if no match or tell user which type
		 */
		if(type==UNSET){
			try {
				this.pdf_datafile.close();
			} catch (IOException e1) {
				LogWriter.writeLog("Exception "+1+" closing file");
			}
			throw new PdfException("Exception unable to find ref or obj in trailer");
		}

		if(type==COMPRESSED)
			return true;
		else
			return false;
	}

	/**
	 * read reference table from file so we can locate
	 * objects in pdf file and read the trailers
	 */
	final private PdfObject readLegacyReferenceTable(int pointer,int eof) throws PdfException {

		PdfObject encryptObj=null, rootObj=null;

		//int lastPointer=-1;
		
		int current = 0; //current object number
		byte[] Bytes = null;
		int bufSize = 1024;

		int endTable = 0;

		/**read and decode 1 or more trailers*/
		while (true) {

			try {


				//allow for pointer outside file
				Bytes=readTrailer(bufSize, pointer, eof);
            } catch (Exception e) {
				Bytes=null;
				try {
					this.pdf_datafile.close();
				} catch (IOException e1) {
					LogWriter.writeLog("Exception "+e+" closing file");
				}
				throw new PdfException("Exception " + e + " reading trailer");
			}

			if (Bytes == null) //safety catch
				break;

			/**get trailer*/
			int i = 0;
			
			int maxLen=Bytes.length;

			//for(int a=0;a<100;a++)
			//	System.out.println((char)Bytes[i+a]);
			while (i <maxLen) {//look for trailer keyword
				if (Bytes[i] == 116 && Bytes[i + 1] == 114 && Bytes[i + 2] == 97 && Bytes[i + 3] == 105 &&
						Bytes[i + 4] == 108 && Bytes[i + 5] == 101 && Bytes[i + 6] == 114)
					break;

				i++;
			}

			//save endtable position for later
			endTable = i;

			//move to beyond <<
			while (Bytes[i] != 60 && Bytes[i - 1] != 60)
				i++;

			i++;
            PdfObject pdfObject=new CompressedObject("1 0 R");
			this.readDictionary(pdfObject,"1 0 R",i,Bytes,"",false,false,-1,null,true);

			//move to beyond >>
			int level=0;
			while(true){
				
				if(Bytes[i] == 60 && Bytes[i - 1] == 60){
					level++;
					i++;
                }else if(Bytes[i] =='['){
                    i++;
                    while(Bytes[i]!=']'){
                        i++;
                        if(i==Bytes.length)
                            break;
                    }
				}else if(Bytes[i] ==62 && Bytes[i - 1] ==62){
					level--;
					i++;
				}
				
				if(level==0)
					break;
				
				i++;
			}

			//handle optional XRefStm
			int XRefStm=pdfObject.getInt(PdfDictionary.XRefStm);

			if(XRefStm!=-1){
				pointer=XRefStm;
			}else{ //usual way

				boolean hasRef=true;

				//look for xref as end of startref
				while (Bytes[i] != 116 && Bytes[i + 1] != 120 &&
						Bytes[i + 2] != 114 && Bytes[i + 3] != 101 && Bytes[i + 4] != 102){

					if(Bytes[i]=='o' && Bytes[i+1]=='b' && Bytes[i+2]=='j'){
						hasRef=false;
						break;
					}
					i++;
				}

				if(hasRef){

					i = i + 8;
					//move to start of value ignoring spaces or returns
					while ((i < maxLen)&& (Bytes[i] == 10 || Bytes[i] == 32 || Bytes[i] == 13))
						i++;

					int s=i;
					
					//allow for characters between xref and startref
					while (i < maxLen && Bytes[i] != 10 && Bytes[i] != 32 && Bytes[i] != 13)
						i++;
					
					/**convert xref to string to get pointer*/
					if (s!=i)
						pointer = parseInt(s, i, Bytes);
					
				}
			}
			if (pointer == -1) {
				LogWriter.writeLog("No startRef");

				/**now read the objects for the trailers*/
			} else if (Bytes[0] == 120 && Bytes[1] == 114 && Bytes[2] == 101 && Bytes[3] == 102) { //make sure starts xref

				i = 5;

				//move to start of value ignoring spaces or returns
				while (Bytes[i] == 10 ||Bytes[i] == 32 || Bytes[i] == 13)
					i++;

				current = readXRefs(current, Bytes, endTable, i);
				i=endTable;

				/**now process trailer values - only first set of table values for root, encryption and info*/
				if (rootObj==null) {

					rootObj=pdfObject.getDictionary(PdfDictionary.Root);

					encryptObj=pdfObject.getDictionary(PdfDictionary.Encrypt);
					if(encryptObj!=null){

						byte[][] IDs=pdfObject.getStringArray(PdfDictionary.ID);
						if(IDs!=null)
							this.ID=IDs[0];
                    }

					infoObject=pdfObject.getDictionary(PdfDictionary.Info);

				}

				//make sure first values used if several tables and code for prev
				pointer=pdfObject.getInt(PdfDictionary.Prev);
				
				//see if other trailers
				if (pointer!=-1 && pointer<this.eof) {
					//reset values for loop
					bufSize = 1024;

					//track ref table so we can work out object length
					xref.addElement(pointer);

				}else //reset if fails second test above
					pointer=-1;
				
			} else{
				pointer=-1;
				rootObj = findOffsets();
				refTableInvalid=true;
			}
			if (pointer == -1)
				break;
		}

		/**
		 * check offsets
		 */

		//checkOffsets(validOffsets);
		if(encryptObj!=null)
			readEncryptionObject(encryptObj.getUnresolvedData());

		if(!refTableInvalid )
			calculateObjectLength();

		return rootObj;
	}

	/**
	 * precalculate sizes for each object
	 */
	private void calculateObjectLength() {

		//add eol to refs as catchall
		this.xref.addElement( (int) eof);


		//get order list of refs
		int[] xrefs=this.xref.get();
		int xrefCount=xrefs.length;
		int[] xrefID=new int[xrefCount];
		for(int i=0;i<xrefCount;i++)
			xrefID[i]=i;
		xrefID=Sorts.quicksort( xrefs, xrefID );

		//get ordered list of objects in offset order
		int objectCount=offset.getCapacity();
		ObjLengthTable=new int[objectCount];
		int[] id=new int[objectCount];
		int[] offsets=new int[objectCount];

		//read from local copies and pop lookup table
		int[] off=offset.get();
		boolean[] isComp=isCompressed.get();
		for(int i=0;i<objectCount;i++){
			if(!isComp[i]){
				offsets[i]=off[i];
				id[i]=i;
			}
		}

		id=Sorts.quicksort( offsets, id );

		int i=0;
		//ignore empty values
		while(true){

            if(offsets[id[i]]!=0)
				break;
			i++;

		}

		/**
		 * loop to calc all object lengths
		 * */
		int  start=offsets[id[i]],end;

		//find next xref
		int j=0;
		while(xrefs[xrefID[j]]<start+1)
			j++;

		while(i<objectCount-1){

			end=offsets[id[i+1]];
			int objLength=end-start-1;

			//adjust for any xref
			if(xrefs[xrefID[j]]<end){
				objLength=xrefs[xrefID[j]]-start-1;
				while(xrefs[xrefID[j]]<end+1)
					j++;
			}
			ObjLengthTable[id[i]]=objLength;
			//System.out.println(id[i]+" "+objLength+" "+start+" "+end);
			start=end;
			while(xrefs[xrefID[j]]<start+1)
				j++;
			i++;
		}

		//special case - last object

		ObjLengthTable[id[i]]=xrefs[xrefID[j]]-start-1;
		//System.out.println("*"+id[i]+" "+start+" "+xref+" "+eof);
	}
	/**
	 * read table of values
	 */
	private int readXRefs( int current, byte[] Bytes, int endTable, int i) {

		char flag='c';
		int id=0,tokenCount=0;
		int generation=0;
		int lineLen=0;
		int startLine,endLine;
		boolean skipNext=false;

		int[] breaks=new int[5];
		int[] starts=new int[5];

		// loop to read all references
		while (i < endTable) { //exit end at trailer

			startLine=i;
			endLine=-1;

			/**
			 * read line locations
			 */
			//move to start of value ignoring spaces or returns
			while ((Bytes[i] != 10) & (Bytes[i] != 13)) {
				//scan for %
				if((endLine==-1)&&(Bytes[i]==37))
					endLine=i-1;

				i++;
			}

			//set end if no comment
			if(endLine==-1)
				endLine=i-1;

			//strip any spaces
			while(Bytes[startLine]==32)
				startLine++;

			//strip any spaces
			while(Bytes[endLine]==32)
				endLine--;

			i++;

			/**
			 * decode the line
			 */
			tokenCount=0;
			lineLen=endLine-startLine+1;

			if(lineLen>0){

				//decide if line is a section header or value

				//first count tokens
				int lastChar=1,currentChar;
				for(int j=1;j<lineLen;j++){
					currentChar=Bytes[startLine+j];

					if((currentChar==32)&&(lastChar!=32)){
						breaks[tokenCount]=j;
						tokenCount++;
					}else if((currentChar!=32)&&(lastChar==32)){
						starts[tokenCount]=j;
					}

					lastChar=currentChar;
				}

				//update numbers so loops work
				breaks[tokenCount]=lineLen;
				tokenCount++;

				if(tokenCount==1){ //fix for first 2 values on separate lines

					if(skipNext)
						skipNext=false;
					else{
						current=parseInt(startLine,startLine+breaks[0],Bytes);
						skipNext=true;
					}

				}else if (tokenCount == 2){
					current=parseInt(startLine,startLine+breaks[0],Bytes);
				}else {

					id = parseInt(startLine,startLine+breaks[0],Bytes);
					generation=parseInt(startLine+starts[1],startLine+breaks[1],Bytes);

					flag =(char)Bytes[startLine+starts[2]];

					if ((flag=='n')) { // only add objects in use

						/**
						 * assume not valid and test to see if valid
						 */
						boolean isValid=false;

						//get bytes
						int bufSize=20;

						//adjust buffer if less than 1024 bytes left in file
						if (id + bufSize > eof)
							bufSize = (int) (eof - id);

						if(bufSize>0){
							byte[] buffer = new byte[bufSize];

							/** get bytes into buffer */
							movePointer(id);

							try {
								pdf_datafile.read(buffer);

								//look for space o b j
								for(int ii=4;ii<bufSize;ii++){
									if((buffer[ii-3]==32 || buffer[ii-3]==10)&&(buffer[ii-2]==111)&&(buffer[ii-1]==98)&&(buffer[ii]==106)){
										isValid=true;
										ii=bufSize;
									}
								}

								if(isValid){
									storeObjectOffset(current, id, generation,false);
									xref.addElement( id);
								}else{
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					}
					current++; //update our pointer
				}
			}
		}
		return current;
	}

	private final static int[] powers={1,10,100,1000,10000,100000,1000000,10000000,100000000,
		1000000000};

	/**
	 * turn stream of bytes into a number
	 */
	public static int parseInt(int i, int j, byte[] bytes) {
		int finalValue=0;
		int power=0;

		boolean isNegative=false;
		i--; //decrement  pointer to speed up
		for(int current=j-1;current>i;current--){

			if(bytes[current]=='-'){
				isNegative=true;
			}else{
				finalValue=finalValue+((bytes[current]-48)*powers[power]);
				//System.out.println(finalValue+" "+powers[power]+" "+current+" "+(char)bytes[current]+" "+bytes[current]);
				power++;
			}
		}

		if(isNegative)
			return -finalValue;
		else
			return finalValue;
	}
	
	/**
	 * turn stream of bytes into a flaot number
	 */
	public static double parseDouble(int start,int end,byte[] stream) {

		double d=0,dec=0f,num=0f;

		int ptr=end;
		int intStart=start;
		boolean isMinus=false;
		//hand optimised float code

		//find decimal point
		for(int j=end-1;j>start-1;j--){
			if(stream[j]==46){ //'.'=46
				ptr=j;
				break;
			}
		}

		int intChars=ptr;

		int decStart=ptr;

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
		int decNumbers=end-ptr;

		if((intNumbers>4)){ //non-optimised to cover others
			isMinus=false;

			int count=end-start;
			byte[] doubleValue=new byte[count];

			System.arraycopy(stream, start,doubleValue,0,count);

			//System.out.println(new String(floatVal)+"<");
			d=Double.parseDouble(new String(doubleValue));

		}else{

			double thous=0f,units=0f,tens=0f,hundreds=0f,tenths=0f,hundredths=0f, thousands=0f, tenthousands=0f,hunthousands=0f,millis=0f;
			int c;

			//thousands
			if(intNumbers>3){
				c=stream[intStart]-48;
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
				c=stream[intStart]-48;
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
				c=stream[intStart]-48;
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
				c=stream[intStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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

			//100thousands
			if(decNumbers>5){
				decStart++; //move beyond.
				c=stream[decStart]-48;

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

			if(decNumbers>6){
				decStart++; //move beyond.
				c=stream[decStart]-48;

				switch(c){
				case 1:
					millis=0.000001f;
					break;
				case 2:
					millis=0.000002f;
					break;
				case 3:
					millis=0.000003f;
					break;
				case 4:
					millis=0.000004f;
					break;
				case 5:
					millis=0.000005f;
					break;
				case 6:
					millis=0.000006f;
					break;
				case 7:
					millis=0.000007f;
					break;
				case 8:
					millis=0.000008f;
					break;
				case 9:
					millis=0.000009f;
					break;
				}
			}

			dec=tenths+hundredths+thousands+tenthousands+hunthousands+millis;
			num=thous+hundreds+tens+units;
			d=num+dec;

		}

		if(isMinus)
			return -d;
		else
			return d;
	}

	/**
	 * turn stream of bytes into a flaot number
	 */
	public static float parseFloat(int start,int end,byte[] stream) {

		float d=0,dec=0f,num=0f;

		int ptr=end;
		int intStart=start;
		boolean isMinus=false;
		//hand optimised float code

		//find decimal point
		for(int j=end-1;j>start-1;j--){
			if(stream[j]==46){ //'.'=46
				ptr=j;
				break;
			}
		}

		int intChars=ptr;

		int decStart=ptr;

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
		int decNumbers=end-ptr;

		if((intNumbers>4)){ //non-optimised to cover others
			isMinus=false;

			int count=end-start;
			byte[] floatVal=new byte[count];

			System.arraycopy(stream, start,floatVal,0,count);

			//System.out.println(new String(floatVal)+"<");
			d=Float.parseFloat(new String(floatVal));

		}else{

			float thous=0f,units=0f,tens=0f,hundreds=0f,tenths=0f,hundredths=0f, thousands=0f, tenthousands=0f,hunthousands=0f,millis=0f;
			int c;

			//thousands
			if(intNumbers>3){
				c=stream[intStart]-48;
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
				c=stream[intStart]-48;
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
				c=stream[intStart]-48;
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
				c=stream[intStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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
				decStart++; //move beyond.
				c=stream[decStart]-48;
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

			//100thousands
			if(decNumbers>5){
				decStart++; //move beyond.
				c=stream[decStart]-48;

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

			if(decNumbers>6){
				decStart++; //move beyond.
				c=stream[decStart]-48;

				switch(c){
				case 1:
					millis=0.000001f;
					break;
				case 2:
					millis=0.000002f;
					break;
				case 3:
					millis=0.000003f;
					break;
				case 4:
					millis=0.000004f;
					break;
				case 5:
					millis=0.000005f;
					break;
				case 6:
					millis=0.000006f;
					break;
				case 7:
					millis=0.000007f;
					break;
				case 8:
					millis=0.000008f;
					break;
				case 9:
					millis=0.000009f;
					break;
				}
			}

			dec=tenths+hundredths+thousands+tenthousands+hunthousands+millis;
			num=thous+hundreds+tens+units;
			d=num+dec;

		}

		if(isMinus)
			return -d;
		else
			return d;
	}

	
	/**
	 */
	private byte[] readTrailer(int bufSize, int pointer, int eof) throws IOException {

         int charReached=0,charReached2=0, trailerCount=0;

		/**read in the bytes, using the startRef as our terminator*/
		ByteArrayOutputStream bis = new ByteArrayOutputStream();

		while (true) {

			/** adjust buffer if less than 1024 bytes left in file */
			if (pointer + bufSize > eof)
				bufSize = eof - pointer;

			byte[] buffer = new byte[bufSize];

			/** get bytes into buffer */
			movePointer(pointer);
			pdf_datafile.read(buffer);

            boolean endFound=false;

			/** write out and lookf for startref at end */
			for (int i = 0; i < bufSize; i++) {

				byte currentByte = buffer[i];

				/** check for startref at end - reset if not */
				if (currentByte == EOFpattern[charReached])
					charReached++;
				else
					charReached = 0;

                /** check for trailer at end - ie second spurious trailer obj */
				if (currentByte == trailerpattern[charReached2])
					charReached2++;
				else
					charReached2 = 0;

                if(charReached2==7){
                    trailerCount++;
                    charReached2=0;
                }

				if (charReached == 5 || trailerCount==2){ //located %%EOF and get last few bytes

					for (int j = 0; j < i+1; j++)
						bis.write(buffer[j]);

					i = bufSize;
					endFound=true;

				}
			}

			//write out block if whole block used
			if(!endFound)
				bis.write(buffer);

			//update pointer
			pointer = pointer + bufSize;

			if (charReached == 5 || trailerCount==2)
				break;

		}

		bis.close();
		return bis.toByteArray();

	}
	/**
	 * read the form data from the file
	 */
	final public PdfFileInformation readPdfFileMetadata(PdfObject metadataObj) {

		//read info object (may be defined and object set in different trailers so must be done at end)
		if(infoObject!=null &&(!isEncrypted|| isPasswordSupplied))
			readInformationObject(infoObject);

		//read and set XML value
		if(metadataObj!=null){

			String objectRef=new String(metadataObj.getUnresolvedData());

			//byte[] stream= metadataObj.DecodedStream;

			//start old
			//get data
			MetadataObject oldMetaDataObj =new MetadataObject(objectRef);
			readObject(oldMetaDataObj);
			byte[] oldstream= oldMetaDataObj.getDecodedStream();
			/** breaks on encrypted (ie preptool)
            boolean failed=PdfReader.checkStreamsIdentical(stream,oldstream);

            if(failed)
                    throw new RuntimeException("Mismatch on info streams");
            /////////////////////////////////////
            /**/
			currentFileInformation.setFileXMLMetaData(oldstream);
		}

		return currentFileInformation;
	}

	//////////////////////////////////////////////////////////////////////////
	/**
	 * get value which can be direct or object
	 *
	final public String getValue(String value) {

		if ((value != null)&&(value.endsWith(" R"))){ //indirect

			Map indirectObject=readObject(new PdfObject(value), value, null);
			//System.out.println(value+" "+indirectObject);
			value=(String) indirectObject.get("rawValue");

		}

		//allow for null as string
		if(value!=null && value.equals("null"))
			value=null;

		return value;
	}/**/

	/**
	 * get text value as byte stream which can be direct or object
	 *
	final public byte[] getByteTextStringValue(Object rawValue,Map fields) {

		if(rawValue instanceof String){
			String value=(String) rawValue;
			if ((value != null)&&(value.endsWith(" R"))){ //indirect

				Map indirectObject=readObject(new PdfObject(value), value, fields);

				rawValue=indirectObject.get("rawValue");

			}else {
				return value.getBytes();
			}
		}

		return (byte[]) rawValue;
	}/**/




	/**remove any trailing spaces at end*/
	private static StringBuffer removeTrailingSpaces(StringBuffer operand) {

		/*remove any trailing spaces on operand*/
		int l = operand.length();
		for (int ii = l - 1; ii > -1; ii--) {
			if (operand.charAt(ii) == ' ')
				operand.deleteCharAt(ii);
			else
				ii = -2;
		}

		return operand;

	}

	/**
	 * return flag to show if encrypted
	 */
	final public boolean isEncrypted() {
		return isEncrypted;
	}

	/**
	 * return flag to show if valid password has been supplied
	 */
	final public boolean isPasswordSupplied() {
		return isPasswordSupplied;
	}

	/**
	 * return flag to show if encrypted
	 */
	final public boolean isExtractionAllowed() {
		return extractionIsAllowed;
	}

	/**show if file can be displayed*/
	public boolean isFileViewable() {

		return isFileViewable;
	}

    /**
     * read object setup to contain only ref to data
     * @param pdfObject
     */
	public void checkResolved(PdfObject pdfObject){

		boolean debugFastCode=false,ignoreRecursion=false;
		String paddingString="",PDFkey="";
		int i=0;

		if(pdfObject==null || pdfObject.getStatus()==PdfObject.DECODED)
			return;

		byte[] raw=pdfObject.getUnresolvedData();

        if(debugFastCode)
        System.out.println("raw="+new String(raw)+" objectRef="+pdfObject.getObjectRefAsString());

		//flag now done and flush raw data
		pdfObject.setStatus(PdfObject.DECODED);

		String objectRef=pdfObject.getObjectRefAsString();

		int PDFkeyInt=pdfObject.getPDFkeyInt();

		//allow for empty object
		if(raw[i]=='e' && raw[i+1]=='n' && raw[i+2]=='d' && raw[i+3]=='o' && raw[i+4]=='b' ){
			//        return i;

			if(debugFastCode)
				System.out.println(paddingString+"Empty object"+new String(raw)+"<--<");


		}else{ //we need to ref from ref elsewhere which may be indirect [ref], hence loop

			if(debugFastCode)
				System.out.println(paddingString+"2.About to read ref orDirect i="+i);

			if(ignoreRecursion){

				//roll onto first valid char
				while(raw[i]==91 || raw[i]==32 || raw[i]==13 || raw[i]==10){

					//if(raw[i]==91) //track incase /Mask [19 19]
					//	possibleArrayStart=i;

					i++;
				}


				//roll on and ignore
				if(raw[i]=='<' && raw[i+1]=='<'){

					i=i+2;
					int reflevel=1;

					while(reflevel>0){
						if(raw[i]=='<' && raw[i+1]=='<'){
							i=i+2;
							reflevel++;
						}else if(raw[i]=='>' && raw[i+1]=='>'){
							i=i+2;
							reflevel--;
						}else
							i++;
					}
					i--;

				}else{ //must be a ref
					//                					while(raw[i]!='R')
					//                						i++;
					//                					i++;
					if(debugFastCode) 
						System.out.println("read ref ="+objectRef);

					//pdfObject.setRef(raw)
					i = readDictionaryFromRefOrDirect(PDFkeyInt,pdfObject,objectRef, i, raw,debugFastCode, PDFkeyInt,PDFkey, paddingString);

				}

				if(raw[i]=='/') //move back so loop works
					i--;

			}else{

				if(debugFastCode)
					System.out.println("3.read ref ="+objectRef+" raw="+new String(raw));

				if(raw[raw.length-1]=='R'){

                    if(debugFastCode)
					System.out.println("4.is ref ="+new String(raw));
                    
					pdfObject.setRef(new String(raw));
                }

                //@semantico
                i = readDictionaryFromRefOrDirect(-1,pdfObject,objectRef, i, raw,debugFastCode , -1,PDFkey, paddingString);

                if(debugFastCode)
                                System.out.println(paddingString+"4.i="+i);

            }
		}

		/**/
	}

    /**
     * allow user to access SOME PDF objects
     * currently PdfDictionary.Encryption
     */
    public PdfObject getPDFObject(int key) {

        if(key==PdfDictionary.Encrypt)
            return this.encyptionObj;
        else
            throw new RuntimeException("Access to "+key+" not supported");
    }

    /**
	 * reads the line/s from file which make up an object
	 * includes move
	 */
	final private byte[] decrypt(byte[] data, String ref,boolean isEncryption,
			String cacheName,boolean alwaysUseRC4,
			boolean isString) throws PdfSecurityException{

		boolean debug=false;//ref.equals("100 0 R");

		if((isEncrypted)||(isEncryption)){

			BufferedOutputStream streamCache= null;
			BufferedInputStream bis = null;
			int streamLength=0;

			boolean isAES=false;

			if(cacheName!=null){ //this version is used if we cache large object to disk
				//rename file
				try {

					streamLength = (int) new File(cacheName).length();

					File tempFile2 = File.createTempFile("jpedal",".raw",new File(ObjectStore.temp_dir));

					cachedObjects.put(tempFile2.getAbsolutePath(),"x");
					//System.out.println(">>>"+tempFile2.getAbsolutePath());
					ObjectStore.copy(cacheName,tempFile2.getAbsolutePath());
					File rawFile=new File(cacheName);
					rawFile.delete();


					//decrypt
					streamCache = new BufferedOutputStream(new FileOutputStream(cacheName));
					bis=new BufferedInputStream(new FileInputStream(tempFile2));

				} catch (IOException e1) {
					LogWriter.writeLog("Exception "+e1+" in decrypt");
				}
			}

			//default values for rsa
			int keyLength=this.keyLength;
			String algorithm="RC4",keyType="RC4";
			IvParameterSpec ivSpec = null;

			//select for stream or string
			PdfObject AESObj=null;
			if(!isString){
				AESObj=StmFObj;
			}else{
				AESObj=StrFObj;
			}

			//AES identity
			if(!alwaysUseRC4 && AESObj==null && isAESIdentity)
				return data;

			//use RC4 as default but override if needed
			if(AESObj!=null){

				//use CF values in preference

				int AESLength=AESObj.getInt(PdfDictionary.Length);
				if(AESLength!=-1)
					keyLength=AESLength;

				String cryptName=AESObj.getName(PdfDictionary.CFM);

				if(cryptName!=null && cryptName.equals("AESV2") && !alwaysUseRC4){

					cipher=null; //force reset as may be rsa

					algorithm="AES/CBC/PKCS5Padding";
					keyType="AES";

					isAES=true;

					//setup CBC
					byte[] iv=new byte[16];
					System.arraycopy(data, 0, iv, 0, 16);
					ivSpec = new IvParameterSpec(iv);

					//and knock off iv data
					int origLen=data.length;
					int newLen=origLen-16;
					byte[] newData=new byte[newLen];
					System.arraycopy(data, 16, newData, 0, newLen);
					data=newData;

					//make sure data correct size
					int diff= (data.length & 15);
					int newLength=data.length;
					if(diff>0){
						newLength=newLength+16-diff;

						newData=new byte[newLength];

						System.arraycopy(data, 0, newData, 0, data.length);
						data=newData;
					}
				}
			}

			byte[] currentKey=new byte[keyLength];

			if(ref.length()>0)
				currentKey=new byte[keyLength+5];

			System.arraycopy(encryptionKey, 0, currentKey, 0, keyLength);

			try{
				//add in Object ref id if any
				if(ref.length()>0){
					int pointer=ref.indexOf(' ');
					int pointer2=ref.indexOf(' ',pointer+1);

					int obj=Integer.parseInt(ref.substring(0,pointer));
					int gen=Integer.parseInt(ref.substring(pointer+1,pointer2));

					currentKey[keyLength]=((byte)(obj & 0xff));
					currentKey[keyLength+1]=((byte)((obj>>8) & 0xff));
					currentKey[keyLength+2]=((byte)((obj>>16) & 0xff));
					currentKey[keyLength+3]=((byte)(gen & 0xff));
					currentKey[keyLength+4]=((byte)((gen>>8) & 0xff));
				}

				byte[] finalKey = new byte[Math.min(currentKey.length,16)];

				if(ref.length()>0){
					MessageDigest currentDigest =MessageDigest.getInstance("MD5");
					currentDigest.update(currentKey);

					//add in salt
					if(isAES && keyLength>=16){
						byte[] salt = {(byte)0x73, (byte)0x41, (byte)0x6c, (byte)0x54};

						currentDigest.update(salt);
					}
					System.arraycopy(currentDigest.digest(),0, finalKey,0, finalKey.length);
				}else{
					System.arraycopy(currentKey,0, finalKey,0, finalKey.length);
				}

				/**only initialise once - seems to take a long time*/
				if(cipher==null)
					cipher = Cipher.getInstance(algorithm);

				SecretKey testKey = new SecretKeySpec(finalKey, keyType);

				if(isEncryption)
					cipher.init(Cipher.ENCRYPT_MODE, testKey);
				else{
					if(ivSpec==null)
						cipher.init(Cipher.DECRYPT_MODE, testKey);
					else //aes
						cipher.init(Cipher.DECRYPT_MODE, testKey,ivSpec);
				}

				//if data on disk read a byte at a time and write back

                if(streamCache!=null){
                    CipherInputStream cis=new CipherInputStream(bis,cipher);
					int nextByte;
					while(true){
						nextByte=cis.read();
						if(nextByte==-1)
							break;
						streamCache.write(nextByte);
					}
					cis.close();
					streamCache.close();
					bis.close();

				}

				if(data!=null)
					data=cipher.doFinal(data);


			}catch(Exception e){

				throw new PdfSecurityException("Exception "+e+" decrypting content");

			}

		}

		if(alwaysReinitCipher)
			cipher=null;

		return data;
	}

	/**
	 * routine to create a padded key
	 */
	private byte[] getPaddedKey(byte[] password){

		/**get 32 bytes for  the key*/
		byte[] key=new byte[32];

		int passwordLength=password.length;
		if(passwordLength>32)
			passwordLength=32;

		System.arraycopy(encryptionPassword, 0, key, 0, passwordLength);

		for(int ii=passwordLength;ii<32;ii++){

			key[ii]=(byte)Integer.parseInt(padding[ii-passwordLength],16);

		}

		return key;
	}

	/**see if valid for password*/
	private boolean testPassword() throws PdfSecurityException{

		int count=32;

		byte[] rawValue=new byte[32];
		byte[] keyValue=new byte[32];
        
		for(int i=0;i<32;i++)
			rawValue[i]=(byte)Integer.parseInt(padding[i],16);

		byte[] encrypted=(byte[])rawValue.clone();

		if (rev==2) {
			encryptionKey=calculateKey(O,P,ID);
			encrypted=decrypt(encrypted,"", true,null,false,false);

		} else if(rev>=3) {

			//use StmF values in preference
			int keyLength=this.keyLength;

			if(rev==4 && StmFObj!=null){
				int lenKey=StmFObj.getInt(PdfDictionary.Length);
				if(lenKey!=-1)
					keyLength=lenKey;
			}

			count=16;
			encryptionKey=calculateKey(O,P,ID);
			byte[] originalKey=(byte[]) encryptionKey.clone();

			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (Exception e) {
				LogWriter.writeLog("Exception "+e+" with digest");
			}

			md.update(encrypted);

			//feed in ID
			keyValue = md.digest(ID);

			keyValue=decrypt(keyValue,"", true,null,true,false);

			byte[] nextKey = new byte[keyLength];

			for (int i=1; i<=19; i++) {

				for (int j=0; j<keyLength; j++)
					nextKey[j] = (byte)(originalKey[j] ^ i);

				encryptionKey=nextKey;

				keyValue=decrypt(keyValue,"", true,null,true,false);

			}

			encryptionKey=originalKey;

			encrypted = new byte[32];
			System.arraycopy(keyValue,0, encrypted,0, 16);
			System.arraycopy(rawValue,0, encrypted,16, 16);

		}

		boolean isMatch=true;

		for(int i=0;i<count;i++){
			if(U[i]!=encrypted[i]){
				isMatch=false;
				i=U.length;
			}
		}

		return isMatch;
	}

	/**set the key value*/
	private void computeEncryptionKey() throws PdfSecurityException{
		MessageDigest md=null;

		String str="";

		if(debugAES){
			System.out.println("Compute encryption key");
		}

		/**calculate key to use*/
		byte[] key=getPaddedKey(encryptionPassword);

		if(debugAES){
			str="raw before 50 times   ---- ";
			for(int ii=0;ii<key.length;ii++)
				str=str+key[ii]+ ' ';
			System.out.println(str);
		}

		/**feed into Md5 function*/
		try{

			// Obtain a message digest object.
			md = MessageDigest.getInstance("MD5");
			encryptionKey=md.digest(key);

			if(debugAES){
				str="encryptionKey before 50 times   ---- ";
				for(int ii=0;ii<key.length;ii++)
					str=str+key[ii]+ ' ';
				System.out.println(str);
			}

			/**rev 3 extra security*/
			if(rev>=3){
				for (int ii=0; ii<50; ii++)
					encryptionKey = md.digest(encryptionKey);
			}

		}catch(Exception e){
			throw new PdfSecurityException("Exception "+e+" generating encryption key");
		}

		if(debugAES){
			str="returned encryptionKey   ---- ";
			for(int ii=0;ii<encryptionKey.length;ii++)
				str=str+encryptionKey[ii]+ ' ';
			System.out.println(str);
		}

	}

	/**see if valid for password*/
	private boolean testOwnerPassword() throws PdfSecurityException{

		String str="";

		if(debugAES)
			System.out.println("testOwnerPassword "+encryptionPassword.length);

		byte[] originalPassword=this.encryptionPassword;

		byte[] userPasswd=new byte[keyLength];
		byte[] inputValue=(byte[])O.clone();

		if(debugAES){
			str="originalPassword   ---- ";
			for(int ii=0;ii<originalPassword.length;ii++)
				str=str+originalPassword[ii]+ ' ';
			System.out.println(str);

		}

		computeEncryptionKey();

		byte[] originalKey=(byte[])encryptionKey.clone();

		if(rev==2){
			userPasswd=decrypt((byte[])O.clone(),"", false,null,false,false);
		}else if(rev>=3){

			//use StmF values in preference
			int keyLength=this.keyLength;
			if(rev==4 && StmFObj!=null){
				int lenKey=StmFObj.getInt(PdfDictionary.Length);
				if(lenKey!=-1)
					keyLength=lenKey;

			}

			if(debugAES)
				System.out.println("Decrypt 20 times");

			userPasswd=inputValue;
			byte[] nextKey = new byte[keyLength];


			for (int i=19; i>=0; i--) {

				for (int j=0; j<keyLength; j++)
					nextKey[j] = (byte)(originalKey[j] ^ i);

				encryptionKey=nextKey;
				userPasswd=decrypt(userPasswd,"", false,null,true,false);

			}
		}

		//this value is the user password if correct
		//so test
		encryptionPassword = userPasswd;

		computeEncryptionKey();

		boolean isMatch=testPassword();

		if(debugAES && !isMatch)
			throw new RuntimeException("Match failed on owner key");


		//put back to original if not in fact correct
		if(isMatch==false){
			encryptionPassword=originalPassword;
			computeEncryptionKey();
		}

		return isMatch;
	}

	/**
	 * find a valid offset
	 */
	final private PdfObject findOffsets() throws PdfSecurityException {

		LogWriter.writeLog("Corrupt xref table - trying to find objects manually");

		String root_id = "";
		try {
			movePointer(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			String line = null;

			int i = (int) this.getPointer();

			try {
				line = pdf_datafile.readLine();
			} catch (Exception e) {
				LogWriter.writeLog("Exception " + e + " reading line");
			}
			if (line == null)
				break;

			if (line.indexOf(" obj") != -1) {

				int pointer = line.indexOf(' ');
				if (pointer > -1) {
					int current_number = Integer.parseInt(line.substring(0,
							pointer));
					storeObjectOffset(current_number, i, 1,false);
				}

			} else if (line.indexOf("/Root") != -1) {

				int start = line.indexOf("/Root") + 5;
				int pointer = line.indexOf('R', start);
				if (pointer > -1)
					root_id = line.substring(start, pointer + 1).trim();
			} else if (line.indexOf("/Encrypt") != -1) {
				//too much risk on corrupt file
				throw new PdfSecurityException("Corrupted, encrypted file");
			}
		}

        //needs to be read to pick up potential /Pages value
        PdfObject obj=new PageObject(root_id);
        readObject(obj);

		return obj;
	}

	/**extract  metadata for  encryption object
	 */
	private void readEncryptionObject(byte[] ref) throws PdfSecurityException {

		//reset flags
		stringsEncoded=false;
		isMetaDataEncypted=true;
		StmFObj=null;
		StrFObj=null;
		isAES=false;

		//<start-jfr>
		if (!isInitialised) {
			isInitialised = true;
			SetSecurity.init();
		}
		//<end-jfr>

		//get values
		if(encyptionObj==null){
			encyptionObj=new EncryptionObject(new String(ref));
			readObject(encyptionObj);
		}

		//check type of filter and type and see if supported
		int v = encyptionObj.getInt(PdfDictionary.V);
		//throw exception if we have an unsupported encryption method
		if(v==3)
			throw new PdfSecurityException("Unsupported Custom Adobe Encryption method");
		else if (v > 4){

			//get filter value
			PdfArrayIterator filters = encyptionObj.getMixedArray(PdfDictionary.Filter);
			int firstValue=PdfDictionary.Unknown;
			if(filters!=null && filters.hasMoreTokens())
				firstValue=filters.getNextValueAsConstant(false);

			if(firstValue!=PdfDictionary.Standard)
				throw new PdfSecurityException("Unsupported Encryption method");
		}

		int newLength=encyptionObj.getInt(PdfDictionary.Length)>>3;
		if(newLength!=-1)
			keyLength=newLength;

		//get rest of the values (which are not optional)
		rev = encyptionObj.getInt(PdfDictionary.R);
		P = encyptionObj.getInt(PdfDictionary.P);
		O = encyptionObj.getTextStreamValueAsByte(PdfDictionary.O);
		U = encyptionObj.getTextStreamValueAsByte(PdfDictionary.U);

		//get additional AES values
		if(v==4){

			isAES=true;

			String CFkey;

			PdfObject CF=encyptionObj.getDictionary(PdfDictionary.CF);

			//EFF=encyptionObj.getName(PdfDictionary.EFF);
			//CFM=encyptionObj.getName(PdfDictionary.CFM);

			isMetaDataEncypted=encyptionObj.getBoolean(PdfDictionary.EncryptMetadata);

			//now set any specific crypt values for StrF (strings) and StmF (streams)
			isAESIdentity=false;
			String key=encyptionObj.getName(PdfDictionary.StrF);

			if(key!=null){

				isAESIdentity=key.equals("Identity");

				stringsEncoded=true;

				PdfKeyPairsIterator keyPairs=CF.getKeyPairsIterator();

				while(keyPairs.hasMorePairs()){

					CFkey=keyPairs.getNextKeyAsString();

					if(CFkey.equals(key))
						StrFObj=keyPairs.getNextValueAsDictionary();

					//roll on
					keyPairs.nextPair();
				}
			}

			key=encyptionObj.getName(PdfDictionary.StmF);

			if(key!=null){

				isAESIdentity=key.equals("Identity");

				PdfKeyPairsIterator keyPairs=CF.getKeyPairsIterator();

				while(keyPairs.hasMorePairs()){

					CFkey=keyPairs.getNextKeyAsString();

					if(CFkey.equals(key))
						StmFObj=keyPairs.getNextValueAsDictionary();

					//roll on
					keyPairs.nextPair();
				}
			}
		}

		isEncrypted = true;
		isFileViewable = false;

		LogWriter.writeLog("File has encryption settings");

		try{
			verifyAccess();
		}catch(PdfSecurityException e){
			LogWriter.writeLog("File requires password");
		}

	}

	/**test password and set access settings*/
	private void verifyAccess() throws PdfSecurityException{

		/**assume false*/
		isPasswordSupplied=false;
		extractionIsAllowed=false;

		passwordStatus=PDFflags.NO_VALID_PASSWORD;

		/**workout if user or owner password valid*/
		boolean isOwnerPassword =testOwnerPassword();
		boolean isUserPassword=testPassword();

		if(isOwnerPassword)
			passwordStatus=PDFflags.VALID_OWNER_PASSWORD;

		if(isUserPassword)
			passwordStatus=passwordStatus+PDFflags.VALID_USER_PASSWORD;


		if(!isOwnerPassword){
			
			/**test if user first*/
			if(isUserPassword){

				//tell if not default value
				if(encryptionPassword.length>0)
					LogWriter.writeLog("Correct user password supplied ");

				isFileViewable=true;
				isPasswordSupplied=true;

				if((P & 16)==16)
					extractionIsAllowed=true;

			}else
				throw new PdfSecurityException("No valid password supplied");

		}else{
			LogWriter.writeLog("Correct owner password supplied");
			isFileViewable=true;
			isPasswordSupplied=true;
			extractionIsAllowed=true;
		}
	}

	/**
	 * calculate the key
	 */
	private byte[] calculateKey(byte[] O,int P,byte[] ID) throws PdfSecurityException{

		if(debugAES)
			System.out.println("calculate key");

		MessageDigest md=null;

		byte[] keyValue=null;

		/**calculate key to use*/
		byte[] key=getPaddedKey(encryptionPassword);

		/**feed into Md5 function*/
		try{

			// Obtain a message digest object.
			md = MessageDigest.getInstance("MD5");

			//add in padded key
			md.update(key);

			//write in O value
			md.update(O);

			byte[] PValue=new byte[4];
			PValue[0]=((byte)((P) & 0xff));
			PValue[1]=((byte)((P>>8) & 0xff));
			PValue[2]=((byte)((P>>16) & 0xff));
			PValue[3]=((byte)((P>>24) & 0xff));

			md.update(PValue);


			md.update(ID);

			byte[] metadataPad = {(byte)255,(byte)255,(byte)255,(byte)255};

			if (rev==4 && !this.isMetaDataEncypted)
				md.update(metadataPad);

			byte digest[] = new byte[keyLength];
			System.arraycopy(md.digest(), 0, digest, 0, keyLength);

			//for rev 3
			if(rev>=3){
				for (int i = 0; i < 50; ++i)
					System.arraycopy(md.digest(digest), 0, digest, 0, keyLength);
			}

			keyValue=new byte[keyLength];
			System.arraycopy(digest, 0, keyValue, 0, keyLength);

		}catch(Exception e){

			e.printStackTrace();
			throw new PdfSecurityException("Exception "+e+" generating encryption key");
		}

		/**put significant bytes into key*/
		byte[] returnKey = new byte[keyLength];
		System.arraycopy(keyValue,0, returnKey,0, keyLength);

		return returnKey;
	}

	///////////////////////////////////////////////////////////////////////////
	/**
	 * read information object and return pointer to correct
	 * place
	 */
	final private void readInformationObject(PdfObject infoObj) {


		//get info
		checkResolved(infoObj);

		/**
		 * set the information values
		 **/
		String newValue="";
		int id;
		byte[] data;

		int count=PdfFileInformation.information_field_IDs.length;

		//put into fields so we can display
		for (int i = 0; i < count; i++){

			id=PdfFileInformation.information_field_IDs[i];
			if(id==PdfDictionary.Trapped){
				newValue=infoObj.getName(id);

				if(newValue==null)
					newValue="";

			}else{

				data=infoObj.getTextStreamValueAsByte(id);
				if(data==null)
					newValue="";
				else
					newValue=PdfReader.getTextString(data, false);
			}

			currentFileInformation.setFieldValue( i,newValue);            
		}
	}
	/**
	 * return pdf data
	 */
	public byte[] getPdfBuffer() {
		return pdf_datafile.getPdfBuffer();
	}

	/**
	 * read a text String held in fieldName in string
	 */
	public static String getTextString(byte[] rawText, boolean keepReturns) {

		String returnText="";

		//make sure encoding loaded
		StandardFonts.checkLoaded(StandardFonts.PDF);

		String text="";

		//retest on false and true
		final boolean debug=false;

		char[] chars=null;
		if(rawText!=null)
			chars=new char[rawText.length];
		int ii=0;

		StringBuffer convertedText=null;
		if(debug)
			convertedText=new StringBuffer();

		char nextChar;

		TextTokens rawChars=new TextTokens(rawText);

		//test to see if unicode
		if(rawChars.isUnicode()){
			//its unicode
			while(rawChars.hasMoreTokens()){
				nextChar=rawChars.nextUnicodeToken();
				if(nextChar==9){
					if(debug)
						convertedText.append(' ');
					chars[ii]=32;
					ii++;
				}else if(nextChar>31 || (keepReturns && (nextChar==10 || nextChar==13))){
					if(debug)
						convertedText.append(nextChar);
					chars[ii]=nextChar;
					ii++;
				}
			}

		}else{
			//pdfDoc encoding

			while(rawChars.hasMoreTokens()){
				nextChar=rawChars.nextToken();

				if(nextChar==9){
					if(debug)
						convertedText.append(' ');
					chars[ii]=32;
					ii++;
                }else if (keepReturns && (nextChar==10 || nextChar==13)){
                    if(debug)
						convertedText.append(nextChar);
					chars[ii]=nextChar;
                    ii++;
				}else if(nextChar>31 && nextChar<253){
					String c=StandardFonts.getEncodedChar(StandardFonts.PDF,nextChar);

					if(debug)
						convertedText.append(c);

					int len=c.length();

					//resize if needed
					if(ii+len>=chars.length){
						char[] tmp=new char[len+ii+10];
						System.arraycopy(chars, 0, tmp, 0, chars.length);
						chars=tmp;
					}

					//add values
					for(int i=0;i<len;i++){
						chars[ii]=c.charAt(i);
						ii++;
					}
				}
			}
		}


		if(chars!=null)
			returnText=String.copyValueOf(chars,0,ii);

		if(debug){
			if(!convertedText.toString().equals(returnText))
				throw new RuntimeException("Different values >"+convertedText+"<>"+returnText+ '<');

		}

		return returnText;

	}


	/**
	 * read any names
	 */
	public void readNames(PdfObject nameObject, Javascript javascript, boolean isKid){

        checkResolved(nameObject);

		/**
		 *  loop to read required values into lookup
		 */
		final int[] nameLists=new int[]{PdfDictionary.Dests, PdfDictionary.JavaScript};
		int count=nameLists.length;
        if(isKid)
        count=1;
        
        PdfObject pdfObj;
		PdfArrayIterator namesArray;

		String name,value;

		for(int ii=0;ii<count;ii++){

            if(isKid)
                pdfObj=nameObject;
            else
                pdfObj=nameObject.getDictionary(nameLists[ii]);

            if(pdfObj==null)
				continue;

            //any kids
			byte[][] kidList = pdfObj.getKeyArray(PdfDictionary.Kids);
            if(kidList!=null){
			    int kidCount=kidList.length;

                /** allow for empty value and put next pages in the queue */
                if (kidCount> 0) {

                    for(int j=0;j<kidCount;j++){

                        String nextValue=new String(kidList[j]);

                        PdfObject nextObject=new NamesObject(nextValue);
                        nextObject.ignoreRecursion(false);

                        readObject(nextObject);

                        readNames(nextObject, javascript,true);
                    }
                }
            }
            
            //get any names object
			namesArray = pdfObj.getMixedArray(PdfDictionary.Names);

            //read all the values
			if (namesArray != null && namesArray.getTokenCount()>0) {
				while (namesArray.hasMoreTokens()) {
					name =namesArray.getNextValueAsString(true);
					value =namesArray.getNextValueAsString(true);

                    //if Javascript, get full value and store, otherwise just get name
					if(nameLists[ii]== PdfDictionary.JavaScript){

                        /**/
					}else //just store
						nameLookup.put(name, value);
                }
			}
        }
	}

	/**
	 * convert name into object ref
	 */
	public String convertNameToRef(String value) {

		//see if decoded
		return (String)nameLookup.get(value);

	}

	/**
	 * set size over which objects kept on disk
	 */
	public void setCacheSize(int miniumumCacheSize) {
		
		this.newCacheSize=miniumumCacheSize;

	}

	/**read data directly from PDF*
	public byte[] readStreamFromPDF(int start, int end) {

		byte[] bytes=new byte[end-start+1];

		//get bytes into buffer
		try {
			movePointer(start);
			pdf_datafile.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bytes;
	}  /**/

    /**
	public void readStreamIntoMemory(Map downField) {
		String cachedStream = ((String)downField.get("CachedStream"));

		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cachedStream));

			int streamLength = (int)new File(cachedStream).length();

			byte[] bytes = new byte[streamLength];

			bis.read(bytes);
			bis.close();

			downField.put("DecodedStream", bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}/**/
	
	public void dispose(){
		
		//this.objData=null;
		//this.lastRef=null;
		this.cachedColorspaces=null;
		this.cachedObjects=null;
		this.cipher=null;
		this.compressedObj=null;
		this.currentFileInformation=null;
		//this.fields=null;
		
		offset=null;
		generation=null;
		isCompressed=null;
		
		xref=null;
		
		try {
			if(pdf_datafile!=null)
			pdf_datafile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pdf_datafile=null;
	}

}
