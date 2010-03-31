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
* PdfFilteredReader.java
* ---------------
*/
package org.jpedal.io;

import java.io.*;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.jpedal.decompression.CCITTFactory;
import org.jpedal.decompression.CCITTMix;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.sun.*;
import org.jpedal.utils.LogWriter;

/**
 * Adds the abilty to decode streams to the PdfFileReader class
 */
public class PdfFilteredReader extends PdfFileReader {

	final public static int A85=1116165;
	
	final public static int AHx=1120328;
	
	final public static int ASCII85Decode=1582784916;
	
	final public static int ASCIIHexDecode=2074112677;
	
	final public static int CCITTFaxDecode=2108391315;
	
	final public static int CCF=1250070;
	
	final public static int Crypt=1112096855;

	final public static int DCTDecode=1180911742;
	
	final public static int Fl=5692;
	
	final public static int FlateDecode=2005566619;
	
	final public static int JBIG2Decode=1247500931;
	
	final public static int JPXDecode=1399277700;
	
	final public static int LZW=1845799;
	
	final public static int LZWDecode=1566984326;
	
	final public static int RL=8732;
	
	final public static int RunLengthDecode=-1815163937;
	
	BufferedOutputStream streamCache = null;

	BufferedInputStream bis = null;

	private String filter_type;

	/** lookup for ASCII85 decode */
	private final static long[] base_85_indices = { 85 * 85 * 85 * 85,
			85 * 85 * 85, 85 * 85, 85, 1 };

	/** lookup for hex multiplication */
	private final static long[] hex_indices = { 256 * 256 * 256, 256 * 256,
			256, 1 };

    // ////////////////////////////////////////////////////////////////////////
    /**
     * main routine which is passed list of filters to decode and the binary
     * data. JPXDecode/DCTDecode are not handled here (we leave data as is and
     * then put straight into a JPEG)<br>
     * <p>
     * <b>Note</b>
     * </p>
     * Not part of API
     * </p>
     *
    final public byte[] decodeFilters(byte[] data, String filter_list,
    		Object rawParams, int width, int height, boolean useNewCCITT,
    		String cacheName) throws Exception {

    	streamCache = null;
    	bis = null;

    	final boolean debug = false;

    	if (debug)
    		System.out.println("=================");

    	Map decodeParams = new HashMap();

    	boolean isCached = ((cacheName != null) && ((data == null) || debugCaching));

    	// put params in Map
    	//Object rawParams = objData.get("DecodeParms");
        if (rawParams != null) {

            if (rawParams instanceof String) {
                convertStringToMap(decodeParams, rawParams);
            } else
                decodeParams = (Map) rawParams;
        }

    	// make sure no []
    	if (filter_list.startsWith("[")) {
    		if (filter_list.endsWith("]"))
    			filter_list = filter_list
    			.substring(1, filter_list.length() - 1);
    		else
    			filter_list = filter_list.substring(1);
    	} else if (filter_list.endsWith("]"))
    		filter_list = filter_list.substring(0, filter_list.length() - 1);

    	if (debug)
    		System.out.println(filter_list);
    	// allow for no filters
    	if (filter_list.length() > 0) {

    		// decode each filter in turn
    		StringTokenizer filters_to_decode = new StringTokenizer(filter_list);

    		boolean isIgnored = false;

    		if (debug)
    			System.out.println("---------");

    		// turn into list we can scan
    		while (filters_to_decode.hasMoreTokens()) {

    			// apply each filter

    			filter_type = filters_to_decode.nextToken();

    			//hack for new code
    			//@speed - bridging code lose when done
    			if(filter_type.startsWith("/")==false)
    				filter_type="/"+filter_type;
    			
    			// if(debug)
    			// System.out.println(filter_type);

    			boolean isDCT = (filter_type.indexOf("/DCTDecode") != -1)|| (filter_type.indexOf("/JPXDecode") != -1);

    			// handle cached objects
    			if (isCached && !isDCT && cacheName != null)
                    setupCachedObjectForDecoding(data, cacheName, debug);
                
                // decode filters (load data for first one)
    			if (filter_type.startsWith("/")) {

    				isIgnored = false;

    				// apply decode
    				if ((filter_type.indexOf("/FlateDecode") != -1) || (filter_type.indexOf("/Fl") != -1)) {
    					data = flateDecode(data, decodeParams, cacheName);

    				} else if ((filter_type.indexOf("/ASCII85Decode") != -1) || (filter_type.indexOf("/A85") != -1)) {

    					if (data != null) {
    						if (debug)
    							System.out.println("data=" + data.length);

    						data = ascii85DecodeNEW(data);
    						// data = ascii85DecodeOLD(data);
    						// System.out.println(data.length+"
    						// "+newData.length);

    						if (debug)
    							System.out.println("final data=" + data.length);
    					}
    					if (bis != null)
    						ascii85Decode(bis, streamCache);

    				} else if (isCCITTEncoded(filter_type)) {

                        // check JAI loaded on first call
                        JAIHelper.confirmJAIOnClasspath();

    					if (isCached) {
    						int size = bis.available();
    						data = new byte[size];
    						bis.read(data);
    					}

                        //get EncodedByteAligned
                        boolean EncodedByteAligned=false;
                        String value = (String) decodeParams.get("EncodedByteAlign");
                        if (value != null)
                            EncodedByteAligned = Boolean.valueOf(value).booleanValue();

                        byte[] newData = null;

                        
                        //@Mariusz - flag ste to ensure that only new code gets execued
                        final boolean newCCITT = false;
                        
                        if(!newCCITT){
                        	if (!EncodedByteAligned &&(useNewCCITT && JAIHelper.isJAIused() && data != null)) {
                        		TiffDecoder decode = new TiffDecoder(width, height,decodeParams, data);
                        		newData = decode.getRawBytes();
                        	}
    					}

    					
    					// if it fails or not called, fall back to old version
    					if ((newData == null))
    						if(newCCITT){
    						}else{
    							data = ccittDecode(data, decodeParams, width,height);
    						}
    					else
    						data = newData;

    					if (isCached)
    						streamCache.write(data);
    					
    				} else if (filter_type.indexOf("/LZW") != -1) {
    					data = lzwDecode(bis, streamCache, data, decodeParams,
    							width, height, cacheName);

    				} else if ((filter_type.indexOf("/RunLengthDecode") != -1)
    						| (filter_type.indexOf("/RL") != -1)) {
    					data = runLengthDecode(data,bis, streamCache);
    				} else if ((filter_type.indexOf("/ASCIIHexDecode") != -1)
    						| (filter_type.indexOf("/AHx") != -1)) {
    					if (data != null)
    						data = asciiHexDecode(data);

    					if (bis != null)
    						asciiHexDecode(bis, streamCache);
                    }else if(filter_type.indexOf("/Crypt")!=-1){ //just pass though
    				} else if (isDCT) {
    					isIgnored = true; // handled elsewhere
    				} else {
    					LogWriter
    					.writeLog("[PDF] Unsupported decompression stream "
    							+ filter_type);
    					data = null;
    				}

    				if (isCached) {
    					if (bis != null)
    						bis.close();

    					if (streamCache != null) {
    						streamCache.flush();
    						streamCache.close();
    					}
    				}

    				if ((debugCaching) && (cacheName != null) && (!isIgnored)) {
    					if (debug)
    						System.out.println(filter_type + " data="
    								+ data.length + " cacheName=" + cacheName);
    					try {

    						bis = new BufferedInputStream(new FileInputStream(
    								cacheName));
    						byte[] cachedData = new byte[bis.available()];
    						bis.read(cachedData);

    						if (cachedData.length != data.length) {
    							System.out.println(filter_type
    									+ " Different sizes inMemory="
    									+ data.length + " cached="
    									+ cachedData.length);
    						}

    						int count = data.length;
    						for (int ii = 0; ii < count; ii++) {
    							if (data[ii] != cachedData[ii]) {
    								// for(int jj=0;jj<ii+1;jj++)
    								System.out.println(ii + " Different data "
    										+ data[ii] + ' ' + cachedData[ii]);
    							}
    						}
    					} catch (Exception e2) {
    						e2.printStackTrace();
    					}
    				}
    			}
    		}
    	}

    	return data;
    }/**/
    
    public byte[] decodeFilters(PdfObject DecodeParms, byte[] data, PdfArrayIterator filters,
    		int width, int height, boolean useNewCCITT,byte[] globalData,
    		String cacheName) throws Exception {
    	
    	streamCache = null;
    	bis = null;

    	final boolean debug = false;
    	
    	boolean isCached = (cacheName != null);

    	int filterType=PdfDictionary.Unknown;

    	int filterCount=filters.getTokenCount();
    	
    	if (debug)
    		System.out.println("=================filterCount="+filterCount+" DecodeParms="+DecodeParms);

    	// allow for no filters
    	if (filterCount>0){

    		boolean isIgnored = false,isDCT;

    		if (debug)
    			System.out.println("---------filterCount="+filterCount+" hasMore"+filters.hasMoreTokens());

    		/**
			 * apply each filter in turn to data
			 */
    		while (filters.hasMoreTokens()) {

    			filterType = filters.getNextValueAsConstant(true);

    			if (debug)
        			System.out.println("---------filter="+getFilterName(filterType)+" data length="+data.length);
        		
    			isDCT = (filterType!=DCTDecode || filterType!=JPXDecode);
				//isDCT = (filterType!=DCTDecode || filterType!=JPXDecode);

    			// handle cached objects
    			if (isCached && cacheName != null)
    				setupCachedObjectForDecoding(data, cacheName, debug);

    			isIgnored = false;

    			// apply decode
    			if (filterType==FlateDecode || filterType==Fl){
    				try{
    				data = flateDecode(data, DecodeParms, cacheName);
    				}catch(Exception w){
    					LogWriter.writeLog("Invalid flate stream");
    					
    				}catch(Error w){
    					LogWriter.writeLog("Invalid flate stream");
    				}
    			}else if (filterType==PdfFilteredReader.ASCII85Decode || filterType==PdfFilteredReader.A85) {

    				if (data != null)
    					data = ascii85Decode(data);	
    				if (bis != null)
    					ascii85Decode(bis, streamCache);

    			}else if (filterType==CCITTFaxDecode || filterType==CCF) {

    				// check JAI loaded on first call
    				JAIHelper.confirmJAIOnClasspath();

    				if (isCached) {
    					int size = bis.available();
    					data = new byte[size];
    					bis.read(data);
    				}

    				//get EncodedByteAligned
    				boolean EncodedByteAligned=false;
    				if(DecodeParms!=null)
    					EncodedByteAligned=DecodeParms.getBoolean(PdfDictionary.EncodedByteAlign);

    				byte[] newData = null;
    				/**
    				 * if NOT byte aligned
    				 * try new tiff decoder using JAI first fixes several
    				 * bugs in old code -note it has some new bugs missing
    				 * in the old code :-(
    				 */

    				//@Mariusz - flag set to ensure that only new code gets execued
    				boolean newCCITT = true;

    				if(!newCCITT){
    					if (!EncodedByteAligned &&(useNewCCITT && JAIHelper.isJAIused() && data != null)) {
    						TiffDecoder decode = new TiffDecoder(width, height,DecodeParms, data);
    						newData = decode.getRawBytes();
    					}
    				}

    				//System.out.println("-> K = " + DecodeParms.getInt(PdfDictionary.K) + " width = " +width);
    				/** if it fails or not called, fall back to old version */
    				if (newData == null)
    					if(newCCITT  && DecodeParms.getInt(PdfDictionary.K)==0){ 
    						CCITTFactory ccitt = new CCITTFactory(data, width, height, DecodeParms);
    						data = ccitt.decode();
    					//@mariusz
    					//}else if (newCCITT  && DecodeParms.getInt(PdfDictionary.K)>0){
    						//CCITTMix mix = new CCITTMix(data, width, height, DecodeParms);	
    						//mix.dummy();
    					}else{
    						data = ccittDecode(data, DecodeParms, width,height);
    					}
    				else
    					data = newData;

    				if (isCached)
    					streamCache.write(data);

    			} else if(filterType==LZWDecode || filterType==LZW){
    				data = lzwDecode(bis, streamCache, data, DecodeParms,
    						width, height, cacheName);

    			} else if(filterType==RunLengthDecode || filterType==RL) {
    				data = runLengthDecode(data,bis, streamCache);

    			} else if(filterType==JBIG2Decode){
    				
    				// Too work we need to add data as an input to JSD to save the resulting data.We do not write back to stream.
    				ByteArrayInputStream bi = new ByteArrayInputStream(data);
    				bis = new BufferedInputStream(bi);

    				data=JBIGDecode(bis, streamCache, data, globalData, cacheName);
    				
    			} else if (filterType==ASCIIHexDecode || filterType==AHx){
    				if (data != null)
    					data = asciiHexDecode(data);

    				if (bis != null)
    					asciiHexDecode(bis, streamCache);
    			}else if(filterType==Crypt){ //just pass though
    			} else if (isDCT) {
    				isIgnored = true; // handled elsewhere
    			} else {
    				LogWriter.writeLog("[PDF] Unsupported decompression stream ");
    				data = null;
    			}

    			if (isCached) {
    				if (bis != null)
    					bis.close();

    				if (streamCache != null) {
    					streamCache.flush();
    					streamCache.close();
    				}
    			}
    		}
    	}

    	return data;
    }

	public static String getFilterName(int filterType) {
		switch(filterType){
		
		case A85:
			return "A85";
			
		case AHx:
			return "AHx";
			
		case ASCII85Decode:
			return "ASCII85Decode";
			
		case ASCIIHexDecode:
			return "ASCIIHexDecode";
			
		case CCITTFaxDecode:
			return "CCITTFaxDecode";
			
		case CCF:
			return "CCF";
			
		case Crypt:
			return "Crypt";
			
		case DCTDecode:
			return "DCTDecode";
			
		case Fl:
			return "Fl";
			
		case FlateDecode:
			return "FlateDecode";
			
		case JBIG2Decode:
			return "JBIG2Decode";
			
		case JPXDecode:
			return "";
			
		case LZW:
			return "";
			
		case LZWDecode:
			return "";
			
		case RL:
			return "";
			
		case RunLengthDecode:
			return "";
		
			default:
				return "Unknown";
		}
	}

	private void setupCachedObjectForDecoding(byte[] data, String cacheName,
			final boolean debug) throws IOException {
		// rename file
		File tempFile2 = File.createTempFile("jpedal", ".raw", new File(ObjectStore.temp_dir));
		cachedObjects.put(tempFile2.getAbsolutePath(), "x"); // store to
																// delete when
																// PDF closed
		ObjectStore.copy(cacheName, tempFile2.getAbsolutePath());
		File rawFile = new File(cacheName);
		rawFile.delete();

		// where its going after decompression
		streamCache = new BufferedOutputStream(new FileOutputStream(cacheName));

		// where data is coming from
		if (debug)
			System.out.println("cache size=" + tempFile2.length());
		bis = new BufferedInputStream(new FileInputStream(tempFile2));

	}

	/**
	 * @param decodeParams
	 * @param rawParams
	 *
	private static void convertStringToMap(Map decodeParams, Object rawParams) {
		StringTokenizer paraValues = new StringTokenizer(Strip
				.removeArrayDeleminators((String) rawParams));
		while (paraValues.hasMoreTokens()) {
			String value = paraValues.nextToken();

			if (value.startsWith("<<"))
				value = value.substring(2).trim();

			if (value.startsWith("/")) {
				String key = value.substring(1);
				value = paraValues.nextToken();
				if (value.endsWith(">>"))
					value = value.substring(0, value.length() - 2).trim();

				decodeParams.put(key, value);
			}
		}
	}/**/

	/**
	 * Run length decode. If both data and cached stream are present it will check
	 * they are identical
	 */
	private static byte[] runLengthDecode(byte[] data,BufferedInputStream bis,
			BufferedOutputStream streamCache) throws Exception {

		ByteArrayOutputStream bos=null;

		int count=0,len=0,nextLen=0,value=0,value2=0;

		if(data!=null){
			count=data.length;
			bos = new ByteArrayOutputStream(count);
		}

		if(bis!=null)
			count=bis.available();

		if(data!=null && bis!=null){
			if(data.length!=bis.available()){
				System.out.println("Different lengths in RunLengthDecode");
				System.out.println(data.length+" "+bis.available());
			}
		}

		for (int i = 0; i < count; i++) {

			if(data!=null)
			len = data[i];

			if(bis!=null){
				nextLen=bis.read();
				if(nextLen>=128)
					nextLen=nextLen-256;

				if(data!=null && (len!=nextLen))
					System.out.println("Len wrong ="+len+ ' ' +nextLen);
				
				len=nextLen;
			}

			if (len < 0)
				len = 256 + len;

			if (len == 128) {

				i = count;

			} else if (len > 128) {

				i++;

				len = 257 - len;

				if(data!=null)
				value=data[i];

				if(streamCache!=null){
					value2=bis.read();
					if(value2>=128)
						value2=value2-256;
				}

				if(data!=null && bis!=null){
					if(value!=value2){
						System.out.println("Different values in RunLengthDecode");
						System.out.println(value+" "+value2+ ' ' +streamCache);
					}
				}

				for (int j = 0; j < len; j++){
					if(data!=null)
						bos.write(value);

					if(streamCache!=null)
						streamCache.write(value2);
				}

			} else {
				i++;
				len++;
				for (int j = 0; j < len; j++){
					if(data!=null){
						value=data[i+j];
						bos.write(value);
					}
					if(streamCache!=null){
						value2=bis.read();
						if(value2>=128)
							value2=value2-256;
						streamCache.write(value2);
					}

					if(data!=null && bis!=null){
						if(value!=value2){
							System.out.println("2Different values in RunLengthDecode");
							System.out.println(value+" "+value2);
						}
					}
				}

				i = i + len - 1;
			}
		}

		if(data!=null){
			bos.close();
			data=bos.toByteArray();
		}

		return data;

	}

    // ////////////////////////////////////////////////
	/**
	 * lzw decode using adobes class
	 *
	*
	 * private byte[] lzwDecode(byte[] data,String filter_list, String
	 * decode_params){
	 *
	 * Object key; Object value; byte[] processed_data=null;
	 *
	 * Integer Predictor=new Integer(1); Integer Colors=new Integer(1); Integer
	 * BitsPerComponent=new Integer(8); Integer Columns=new Integer(1); Integer
	 * EarlyChange=new Integer(1);
	 *
	 * try{
	 *
	 * //put params in object FilterParams params=new FilterParams();
	 *
	 * //reset vales StringTokenizer values=new StringTokenizer(decode_params,"<
	 * >"); while(values.hasMoreTokens()){ key=values.nextToken(); String
	 * test=key.toString();
	 *
	 * if(test.startsWith("/")){ value=values.nextElement();
	 *
	 * //set values if(test.equals("/Predictor")) Predictor=new
	 * Integer(value.toString()); if(test.equals("/Colors")) Colors=new
	 * Integer(value.toString()); if(test.equals("/BitsPerComponent"))
	 * BitsPerComponent=new Integer(value.toString());
	 * if(test.equals("/Columns")) Columns=new Integer(value.toString());
	 * if(test.equals("/EarlyChange")) EarlyChange=new
	 * Integer(value.toString()); } }
	 *
	 * params.put("Predictor",Predictor); params.put("Colors",Colors);
	 * params.put("BitsPerComponent",BitsPerComponent);
	 * params.put("Columns",Columns); params.put("EarlyChange",EarlyChange);
	 *
	 * LZWInputStream lzw_filter= new LZWInputStream(new BufferedInputStream(new
	 * ByteArrayInputStream(data)),params);
	 *
	 * ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buffer =
	 * new byte[4096]; int bytes_read;
	 *
	 * //loop to write the data while((bytes_read =lzw_filter.read(buffer))!=-1)
	 * out.write(buffer,0,bytes_read);
	 *
	 * //close and get the data lzw_filter.close(); out.close();
	 *
	 * processed_data=out.toByteArray();
	 *
	 * }catch(Exception e){ LogWriter.writeLog("Exception "+e+" accessing LZW
	 * filter"); }
	 *
	 * return processed_data; }
	 */
	// ////////////////////////////////////////////////
	/**
	 * lzw decode using our own class
	 *
	 * @param cacheName
	 */
	final private byte[] lzwDecode(BufferedInputStream bis,
			BufferedOutputStream streamCache, byte[] data, PdfObject DecodeParms,
			int width, int height, String cacheName) throws Exception {

		// default values
		int predictor = 1;
		// int Colors=1;
		int BitsPerComponent = 8;
		int EarlyChange;
        
        int rows = height, columns = width;
        int colors = 1,bitsPerComponent = 8,earlyChange=1;

        if(DecodeParms!=null){
        
        	int newBitsPerComponent = DecodeParms.getInt(PdfDictionary.BitsPerComponent);
			if(newBitsPerComponent!=-1)
				bitsPerComponent=newBitsPerComponent;
			
			int newColors = DecodeParms.getInt(PdfDictionary.Colors);
			if(newColors!=-1)
                colors=newColors;
			
			int columnsSet = DecodeParms.getInt(PdfDictionary.Columns);
			if(columnsSet!=-1)
				columns=columnsSet;
			
			//earlyChange = DecodeParms.getNumber(PdfDictionary.EarlyChange);
			
			predictor = DecodeParms.getInt(PdfDictionary.Predictor);
			
			int rowsSet = DecodeParms.getInt(PdfDictionary.Rows);
			if(rowsSet!=-1)
				rows=rowsSet;
			
        }
		return lzwDecode(bis, streamCache, data, cacheName, predictor,
				BitsPerComponent, rows, columns, colors, bitsPerComponent);

	}

	/**
	 * lzw decode using our own class
	 *
	 * @param cacheName
	 *
	final private byte[] lzwDecode(BufferedInputStream bis,
			BufferedOutputStream streamCache, byte[] data, Map values,
			int width, int height, String cacheName) throws Exception {

		// default values
		int predictor = 1;
		// int Colors=1;
		int BitsPerComponent = 8;
		int EarlyChange;
        
        int rows = height, columns = width;
        int colors = 1,bitsPerComponent = 8,earlyChange=1;

		// get Predictor
		String value = (String) values.get("Predictor");
		if (value != null)
			predictor = Integer.parseInt(value);

		value = (String) values.get("Rows");
		if (value != null)
			rows = Integer.parseInt(value);

		value = (String) values.get("Columns");
		if (value != null)
			columns = Integer.parseInt(value);

		value = (String) values.get("EarlyChange");
		if (value != null)
			EarlyChange = Integer.parseInt(value);

		// get BitsPerComponent
		value = (String) values.get("BitsPerComponent");
		if (value != null)
			BitsPerComponent = Integer.parseInt(value);
		
		value = (String) values.get("Colors");
		if (value != null)
			colors = Integer.parseInt(value);

		return lzwDecode(bis, streamCache, data, cacheName, predictor,
				BitsPerComponent, rows, columns, colors, bitsPerComponent);

	}  /**/
	
	private byte[] lzwDecode(BufferedInputStream bis,
			BufferedOutputStream streamCache, byte[] data, String cacheName,
			int predictor, int BitsPerComponent, int rows, int columns,
			int colors, int bitsPerComponent) throws Exception, IOException {
		if (rows * columns == 1) {

			if (data != null) {
				byte[] processed_data = new byte[BitsPerComponent * rows
						* ((columns + 7) >> 3)]; // will be resized if needed
													// 9allow for not a full 8
													// bits

				TIFFLZWDecoder lzw_decode = new TIFFLZWDecoder(columns,
						predictor, BitsPerComponent);

				lzw_decode.decode(data, processed_data, rows);

				return applyPredictor(predictor, processed_data, colors, BitsPerComponent, columns);
			}else{

				return null;
			}

		} else { // version for no parameters

			/**
			 * decompress cached object
			 */
			if (bis != null) {

				LZWDecoder2 lzw2 = new LZWDecoder2();
				lzw2.decode(data, streamCache, bis);

			}

			if (data != null) {
				ByteArrayOutputStream processed = new ByteArrayOutputStream();
				LZWDecoder lzw = new LZWDecoder();
				lzw.decode(data, processed);
				processed.close();
				data = processed.toByteArray();
			}

			if (predictor != 1 && predictor != 10) {
				streamCache.flush();
				streamCache.close();
				if (cacheName != null)
					setupCachedObjectForDecoding(data, cacheName, false);
			}

			data = applyPredictor(predictor, data, colors,  bitsPerComponent, columns);

		}

		return data;
	}

    /**
	 * JBIG decode using our own class
	 *
     * @param cacheName
	 */
	static final private byte[] JBIGDecode(BufferedInputStream bis,
			BufferedOutputStream streamCache, byte[] data, byte[] globalData,
			String cacheName) throws Exception {


        //@todo -implement so work with caching as well (see examples above)
        try {

			org.jpedal.jbig2.JBIG2Decoder decoder = new org.jpedal.jbig2.JBIG2Decoder();

			if (globalData != null && globalData.length > 0) {
				decoder.setGlobalData(globalData);
			}
			
			decoder.decodeJBIG2(data);

			data = decoder.getPageAsJBIG2Bitmap(0).getData(true);
		} catch (Exception ee) {
			ee.printStackTrace();
		}

        return data;

    }
	
	// /////////////////////////////////////////////////////////////////////////
	/**
	 * ccitt decode using adobes class
	 *
	 * private byte[] ccittDecodeAdobe(byte[] data,String filter_list, String
	 * decode_params){
	 *
	 * //default paramter values Integer K=new Integer(0); Boolean EndOfLine=new
	 * Boolean(false); Boolean EncodedByteAlign=new Boolean(false); Integer
	 * Columns=new Integer(1728); Integer Rows=new Integer(0); Boolean
	 * EndOfBlock=new Boolean(true); Boolean Blackls1=new Boolean(false);
	 * Integer DamagedRowsBeforeError=new Integer(000);
	 *
	 * Object key; Object value; byte[] processed_data=null;
	 *
	 * try{ //put params in object FilterParams params=new FilterParams();
	 *
	 * //reset vales StringTokenizer values=new StringTokenizer(decode_params,"<
	 * >"); while(values.hasMoreTokens()){ key=values.nextToken(); String
	 * test=key.toString();
	 *
	 * if(test.startsWith("/")){ value=values.nextElement();
	 *
	 * //set values if(test.equals("/K")) K=new Integer(value.toString());
	 * if(test.equals("/EndOfLine")) EndOfLine=new Boolean(value.toString());
	 * if(test.equals("/EncodedByteAlign")) EncodedByteAlign=new
	 * Boolean(value.toString()); if(test.equals("/Columns")) Columns=new
	 * Integer(value.toString()); if(test.equals("/Rows")) Rows=new
	 * Integer(value.toString()); if(test.equals("/EndOfBlock")) EndOfBlock=new
	 * Boolean(value.toString()); if(test.equals("/Blackls1")) Blackls1=new
	 * Boolean(value.toString()); if(test.equals("/DamagedRowsBeforeError"))
	 * DamagedRowsBeforeError=new Integer(value.toString());
	 *  } }
	 *
	 * //put in values params.put("K",K); params.put("EndOfLine",EndOfLine);
	 * params.put("EncodedByteAlign",EncodedByteAlign);
	 * params.put("Columns",Columns); params.put("Cols",Columns);
	 * params.put("Rows",Rows); params.put("EndOfBlock",EndOfBlock);
	 * params.put("Blackls1",Blackls1);
	 * params.put("DamagedRowsBeforeError",DamagedRowsBeforeError);
	 *
	 * CCITTFaxInputStream CCITT_filter= new CCITTFaxInputStream(new
	 * BufferedInputStream(new ByteArrayInputStream(data)),params);
	 *
	 * ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buffer =
	 * new byte[4096]; int bytes_read;
	 *
	 * //loop to write the data while((bytes_read
	 * =CCITT_filter.read(buffer))!=-1) out.write(buffer,0,bytes_read);
	 *
	 * //close and get the data CCITT_filter.close(); out.close();
	 *
	 * processed_data=out.toByteArray();
	 *
	 * }catch(Exception e){ LogWriter.writeLog("Exception "+e+" accessing CCITT
	 * filter"); }
	 *
	 * ShowGUIMessage.showGUIMessage("CCITT used","CCITT used");
	 *
	 * return processed_data; }
	 */

	/**
	 * ccitt decode using Sun class
     *
	private static byte[] ccittDecode(byte[] data, Map values, int width,
			int height) throws Exception {

		// flag to show if default is black or white
		boolean isBlack = false;
		int columns = 1728;
		int rows = height;
		int k = 0;
		boolean isByteAligned = false;

		// get k
		String value = (String) values.get("K");
		if (value != null)
			k = Integer.parseInt(value);

		// get flag for white/black as default
		value = (String) values.get("EncodedByteAlign");
		if (value != null)
			isByteAligned = Boolean.valueOf(value).booleanValue();

		// get flag for white/black as default
		value = (String) values.get("BlackIs1");
		if (value != null)
			isBlack = Boolean.valueOf(value).booleanValue();

		value = (String) values.get("Rows");
		if (value != null)
			rows = Integer.parseInt(value);

		value = (String) values.get("Columns");
		if (value != null)
			columns = Integer.parseInt(value);

		return sunCCITTDecode(data, isBlack, columns, rows, k, isByteAligned);
	}

	/**
	 * ccitt decode using Sun class
     */
	private static byte[] ccittDecode(byte[] data, PdfObject DecodeParms, int width,
			int height) throws Exception {

		// flag to show if default is black or white
		boolean isBlack = false;
		int columns = 1728;
		int rows = height;
		int k = 0;
		boolean isByteAligned = false;

		if(DecodeParms!=null){
		
			isBlack = DecodeParms.getBoolean(PdfDictionary.BlackIs1);

			int columnsSet = DecodeParms.getInt(PdfDictionary.Columns);
			if(columnsSet!=-1)
				columns=columnsSet;
			
			isByteAligned = DecodeParms.getBoolean(PdfDictionary.EncodedByteAlign);

			k = DecodeParms.getInt(PdfDictionary.K);

			int rowsSet = DecodeParms.getInt(PdfDictionary.Rows);
			if(rowsSet!=-1)
				rows=rowsSet;
		}
		
		return sunCCITTDecode(data, isBlack, columns, rows, k, isByteAligned);
	}
	private static byte[] sunCCITTDecode(byte[] data, boolean isBlack, int columns,
			int rows, int k, boolean isByteAligned) {
		byte[] processed_data = new byte[rows * ((columns + 7) >> 3)]; // will
																		// be
																		// resized
																		// if
																		// needed
																		// 9allow
																		// for
																		// not a
																		// full
																		// 8
																		// bits

		try {

			TIFFFaxDecoder tiff_decode = new TIFFFaxDecoder(1, columns, rows);

			// use Sun TIFFFaxDecoder class
			if (k == 0)
				tiff_decode.decode1D(processed_data, data, 0, rows);
			else if (k > 0)
				tiff_decode.decode2D(processed_data, data, 0, rows, 0);
			else if (k < 0)
				tiff_decode.decodeT6(processed_data, data, 0, rows, 0,
						isByteAligned);

			// invert image if needed -
			// ultimately will be quicker to add into decode
			if (!isBlack) {
				for (int i = 0; i < processed_data.length; i++)
					processed_data[i] = (byte) (255 - processed_data[i]);
			}
		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " accessing CCITT filter "
					+ e);

		}

		return processed_data;
	}

    // ////////////////////////////////////////////////
	/**
	 * ascii85decode using our own implementation
	 */
	static final private byte[] ascii85Decode(byte[] valuesRead) {

		int special_cases = 0, returns = 0, data_size = valuesRead.length;

		// allow for special cases
		for (int i = 0; i < data_size; i++) {
			if (valuesRead[i] == 122)
				special_cases++;
			if ((valuesRead[i] == 10) || (valuesRead[i] == 10))
				returns++;
		}

		// pointer in output buffer
		int output_pointer = 0;
		long value = 0;

		// buffer to hold data
		byte[] temp_data = new byte[data_size - returns + 1 + (special_cases * 3)];
		int ii = 0,next;

		/**
		 * translate each set of 5 to 4 bytes (note lookup tables)
		 */

		// 5 bytes in base 85
		for (int i = 0; i < data_size; i++) {
			value = 0;
			next = valuesRead[i];
			while ((next == 10) || (next == 13)) {
				i++;
				if (i == data_size)
					next = 0;
				else
					next = valuesRead[i];
			}

			// check special case first
			if (next == 122) {

				// and write out 4 bytes
				for (int i3 = 0; i3 < 4; i3++) {
					temp_data[output_pointer] = 0;
					output_pointer++;
				}
			} else if ((data_size - i > 4) && (next > 32) && (next < 118)) {

				for (ii = 0; ii < 5; ii++) {
					
					if(i<valuesRead.length)
					next = valuesRead[i];
					
					while ((next == 10) || (next == 13)) {
						i++;
						if (i == data_size)
							next = 0;
						else
							next = valuesRead[i];
					}
					i++;
					if (((next > 32) && (next < 118)) || (next == 126))
						value = value + ((next - 33) * base_85_indices[ii]);
				}

				// and write out 4 bytes
				for (int i3 = 0; i3 < 4; i3++) {
					temp_data[output_pointer] = (byte) ((value / hex_indices[i3]) & 255);
					output_pointer++;
				}
				i--;// correction as loop will also increment
			}
		}

		// now put values into processed data
		byte[] processed_data = new byte[output_pointer];
		System.arraycopy(temp_data, 0, processed_data, 0, output_pointer);

		return processed_data;
	}

	/**
	 * ascii85decode using our own implementation
	 */
	static final private void ascii85Decode(BufferedInputStream bis,
			BufferedOutputStream streamCache) {

		long value = 0;
		int nextValue = 0;

		try {
			/**
			 * translate each set of 5 to 4 bytes (note lookup tables)
			 */

			int data_size = bis.available(), lastValue = 0;
			boolean ignoreLastItem = false;
			while (bis.available() > 0) {
				value = 0;

				nextValue = read(bis);

				// check special case first
				if (nextValue == 122) {

					// and write out 4 bytes
					for (int i3 = 0; i3 < 4; i3++)
						streamCache.write(0);

				} else if ((bis.available() >= 4) && (nextValue > 32)
						&& (nextValue < 118)) {

					lastValue = nextValue;

					value = value + ((nextValue - 33) * base_85_indices[0]);

					//String list = "";

					for (int ii = 1; ii < 5; ii++) {
						nextValue = read(bis);

						//list = list + nextValue + ' ';

						if (nextValue == -1)
							nextValue = 0;
						// System.out.println(">>"+nextValue);
						// if((lastValue==126)&&(nextValue==62)&&(bis.available()<6))
						if (nextValue == -1)
							ignoreLastItem = true;

						lastValue = nextValue;
						// System.out.println(nextValue+" "+(char)nextValue);
						if (((nextValue > 32) && (nextValue < 118))
								|| (nextValue == 126))
							value = value
									+ ((nextValue - 33) * base_85_indices[ii]);
					}

					if (!ignoreLastItem) {
						// and write out 4 bytes
						for (int i3 = 0; i3 < 4; i3++) {
							byte b = (byte) ((value / hex_indices[i3]) & 255);
							
							streamCache.write(b);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static int read(BufferedInputStream bis) throws IOException {

		int nextValue = bis.read();
		while ((nextValue == 13) || (nextValue == 10))
			nextValue = bis.read();

		return nextValue;
	}

	/**
	 * asciihexdecode using our own implementation
     */
	static final private byte[] asciiHexDecode(byte[] data) throws IOException {

		String line = "";
		StringBuffer value = new StringBuffer();
		StringBuffer valuesRead = new StringBuffer();
		BufferedReader mappingStream = null;
		ByteArrayInputStream bis = null;

		// read in ASCII mode to handle line returns
		try {
			bis = new ByteArrayInputStream(data);
			mappingStream = new BufferedReader(new InputStreamReader(bis));

			// read values into lookup table
			if (mappingStream != null) {

				while (true) {
					line = mappingStream.readLine();

					if (line == null)
						break;

					// append to data
					valuesRead.append(line);

				}
			}

		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " reading ASCII stream ");

		}

		if (mappingStream != null) {
			try {
				mappingStream.close();
				bis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		int data_size = valuesRead.length();
		int i = 0, count = 0;
		char current = ' ';

		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

		/** loop to read and process */
		while (true) {

			current = valuesRead.charAt(i);

			if (((current >= '0') & (current <= '9'))
					| ((current >= 'a') & (current <= 'f'))
					| ((current >= 'A') & (current <= 'F'))) {
				value.append(current);
				if (count == 1) {
					bos.write(Integer.valueOf(value.toString(), 16).intValue());
					count = 0;
					value = new StringBuffer();
				} else
					count++;

			}

			if (current == '>')
				break;

			i++;

			if (i == data_size)
				break;
		}

		// write any last char
		if (count == 1) {
			value.append('0');
			bos.write(Integer.valueOf(value.toString(), 16).intValue());
		}

		bos.close();

		return bos.toByteArray();
	}

	/**
	 * asciihexdecode using our own implementation
	 */
	static final private void asciiHexDecode(BufferedInputStream bis,
			BufferedOutputStream streamCache) throws IOException {

		StringBuffer value = new StringBuffer();
		char current = ' ';

		/** loop to read and process */
		int count = bis.available();
		for (int i = 0; i < count; i++) {

			current = (char) bis.read();
			while (current == '\n')
				current = (char) bis.read();

			if (((current >= '0') & (current <= '9'))
					| ((current >= 'a') & (current <= 'f'))
					| ((current >= 'A') & (current <= 'F'))) {
				value.append(current);
				if (count == 1) {
					streamCache.write(Integer.valueOf(value.toString(), 16)
							.intValue());
					count = 0;
					value = new StringBuffer();
				} else
					count++;

			}

			if (current == '>')
				break;
		}

		// write any last char
		if (count == 1) {
			value.append('0');
			streamCache.write(Integer.valueOf(value.toString(), 16).intValue());
		}
	}

	/**
	 * flate decode - use a byte array stream to decompress data in memory
	 *
	 * @param cacheName
	 *
	final private byte[] flateDecode(byte[] data, Map params, String cacheName)
			throws Exception {

		byte[] returnData = null;
		int predictor = 1;
		String value = (String) params.get("Predictor");
		if (value != null)
			predictor = Integer.parseInt(value);

		// set values from dictionary

		int colors = 1,bitsPerComponent = 8,columns = 1,earlyChange=1;

		value = (String) params.get("Colors");
		if (value != null)
			colors = Integer.parseInt(value);

		value = (String) params.get("BitsPerComponent");
		if (value != null)
			bitsPerComponent = Integer.parseInt(value);

		value = (String) params.get("Columns");
		if (value != null)
			columns = Integer.parseInt(value);
		
		return flateDecode(data, cacheName, returnData, predictor, colors,
				bitsPerComponent, columns);

	}
	
	/**
	 * flate decode - use a byte array stream to decompress data in memory
	 *
	 * @param cacheName
	 */
	final private byte[] flateDecode(byte[] data, PdfObject DecodeParms, String cacheName)
			throws Exception {

		byte[] returnData = null;
		int predictor = 1;
		int colors = 1,bitsPerComponent = 8,columns = 1;

		if(DecodeParms!=null){
			
			int newBitsPerComponent = DecodeParms.getInt(PdfDictionary.BitsPerComponent);
			if(newBitsPerComponent!=-1)
				bitsPerComponent=newBitsPerComponent;
			
            int newColors = DecodeParms.getInt(PdfDictionary.Colors);
			if(newColors!=-1)
            colors=newColors;

            int columnsSet = DecodeParms.getInt(PdfDictionary.Columns);
			if(columnsSet!=-1)
				columns=columnsSet;
			
			predictor = DecodeParms.getInt(PdfDictionary.Predictor);
		}
		
		return flateDecode(data, cacheName, returnData, predictor, colors,
				bitsPerComponent, columns);

	}

	private byte[] flateDecode(byte[] data, String cacheName,
			byte[] returnData, int predictor, int colors, int bitsPerComponent,
			int columns) throws DataFormatException, Exception {
		/**
		 * decompress byte[]
		 */
		if (data != null) {
			// create a inflater and initialize it Inflater inf=new Inflater();
			Inflater inf = new Inflater();

			inf.setInput(data);

			int size = data.length;
			ByteArrayOutputStream bos = new ByteArrayOutputStream(size);

			int bufSize = 512000;
			if (size < bufSize)
				bufSize = size;

			byte[] buf = new byte[bufSize];
			int debug = 20;
			int count;
			while (!inf.finished()) {

				count = inf.inflate(buf);
				// System.out.println(data.length+" "+count+"
				// "+inf.getRemaining()+" "+inf.getTotalIn()+"
				// "+inf.getTotalOut()+" "+inf.finished());
				bos.write(buf, 0, count);

				if (inf.getRemaining() == 0)
					break;
				// System.out.println("ammount done="+count+"
				// isfinished="+inf.finished()+" bytesin input buffer
				// left="+inf.getRemaining());

			}

			data = bos.toByteArray();

		}

		/**
		 * decompress cached object
		 */
		if (bis != null) {

			
			try {
				InputStream inf;
				// create a inflater and initialize it Inflater inf=new
				// Inflater();
				// if((predictor==1) || (predictor==10) )
				inf = new java.util.zip.InflaterInputStream(bis);
				// else
				// inf=new DecodePredictor(null, predictor,params,new
				// java.util.zip.InflaterInputStream(bis));

				int i = 0;
				while (true) {

					int b = inf.read();
					if ((inf.available() == 0) || (b == -1))
						break;

					streamCache.write(b);

					i++;
				}

				if (predictor != 1 && predictor != 10) {
					streamCache.flush();
					streamCache.close();
					if (cacheName != null)
						setupCachedObjectForDecoding(data, cacheName, false);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		returnData = applyPredictor(predictor, data, colors, bitsPerComponent, columns);

		return returnData;
	}

	/**
	 * implement predictor function
	 */
	private static void applyPredictorFunction(int mainPred, BufferedInputStream bis,
			OutputStream bos,int colors, int bitsPerComponent, int columns) throws Exception {

        int predictor=mainPred;
		int bytesAvailable =bis.available();
	
        /**
		 * calculate values
		 */
	
		int bpp = (colors * bitsPerComponent + 7) / 8; //actual Bytes for a pixel;

		int rowLength = (columns * colors * bitsPerComponent + 7) / 8+bpp; //length of each row + predictor

		//array to hold 2 lines
		byte[] thisLine = new byte[rowLength];
		byte[] lastLine = new byte[rowLength];
		
		// extra predictor needed for optimization
		int curPred = 0;
		
		//actual processing loop
		try{
			int byteCount=0;
			while(true){

				//exit after all used
				if(bytesAvailable <=byteCount)
					break;

				//set predictor
				predictor=mainPred;

				
				/**
				 * read line
				 */
				int i = 0;
				int offset = bpp;
				int bytesToRead=rowLength;
				
				
				// PNG optimization.
				if(predictor>=10){
					curPred = bis.read();
					if(curPred==-1){
						break;
					}
					curPred +=10;
				}else{
					curPred = predictor;	
				}

				while (offset < bytesToRead) {

					
					i= bis.read(thisLine, offset, bytesToRead-offset);

					if (i == -1)
						break;

					offset += i;
					byteCount+=i;
				}

				if (i == -1)
					break;

				//apply
				
				switch(curPred){

				case 2:  //tiff (same as sub)
					for (int i1 = bpp; i1 < rowLength; i1++){

						int sub = thisLine[i1] & 0xff;
						int raw = thisLine[i1-bpp] & 0xff;
						thisLine[i1] = (byte) ((sub+raw) & 0xff);
						bos.write(thisLine[i1]);

					}
					break;

				case 10:  //just pass through
					for (int i1 = bpp; i1 < rowLength; i1++){
						
						bos.write(thisLine[i1]);

					}
						
					break;

				case 11:  //sub
					for (int i1 = bpp; i1 < rowLength; i1++){

						int sub = thisLine[i1] & 0xff;
						int raw = thisLine[i1-bpp] & 0xff;
						thisLine[i1] = (byte) ((sub+raw));
						bos.write(thisLine[i1]);						
					}
					break;

				case 12:  //up
					for (int i1 = bpp; i1 < rowLength; i1++){

						int sub = (lastLine[i1] & 0xff) + (thisLine[i1] & 0xff);
						thisLine[i1] = (byte) (sub);
						bos.write(thisLine[i1]);
					}

					break;

				case 13:  //avg
					for (int i1 = bpp; i1 < rowLength; i1++){

						int av = thisLine[i1] & 0xff;
						int floor = ((thisLine[i1 - bpp] & 0xff)+(lastLine[i1] & 0xff)>>1);
						thisLine[i1] = (byte) (av + floor);
						bos.write(thisLine[i1]);
					}
					break;

				case 14:  //paeth (see http://www.w3.org/TR/PNG-Filters.html)
					for (int i1 = bpp; i1 < rowLength; i1++){

						int a=thisLine[i1-bpp]&0xff, b=lastLine[i1]&0xff, c=lastLine[i1-bpp]&0xff;    

						int p = a + b - c; 

						int pa=p-a, pb=p-b, pc=p-c;

						//make sure positive
						if(pa<0)
							pa=-pa;
						if(pb<0)
							pb=-pb;
						if(pc<0)
							pc=-pc;

						if(pa<=pb && pa<=pc)
							thisLine[i1]= (byte) (thisLine[i1]+a);
						else if(pb<=pc)
							thisLine[i1]=(byte) (thisLine[i1]+b);
						else
							thisLine[i1]=(byte) (thisLine[i1]+c);

						bos.write(thisLine[i1]);
						
					}
					break;

				case 15:
					break;
				default:

				break;
				}

				//add to output and update line
                System.arraycopy(thisLine, 0, lastLine, 0, lastLine.length);

			}

			bos.flush();
			bos.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

    private byte[] applyPredictor(int predictor, byte[] data,
    		int colors, int bitsPerComponent, int columns) throws Exception {

    	//no prediction (TIFF =1 PNG=10)
    	if(predictor==1 || predictor==10){
    		return data;
    	}else{

    		boolean isCached=data==null;
    		if(isCached){
    			applyPredictorFunction(predictor,bis,streamCache, colors, bitsPerComponent, columns);
    			return null; 
    		}else{
    			BufferedInputStream bis=new BufferedInputStream(new ByteArrayInputStream(data));
    			ByteArrayOutputStream bos=new ByteArrayOutputStream();

    			applyPredictorFunction(predictor,bis,bos, colors, bitsPerComponent, columns);

    			return bos.toByteArray();
    		}
    	}
    }

	/**
	 * decide if we convert straight to image or into byte stream so we can
	 * manipulate further
	 */
	private static boolean isCCITTEncoded(String filter) {
		if (filter == null)
			return false;
		else
			return ((filter.startsWith("/CCITT")) | (filter.startsWith("/CCF")));
	}

}
