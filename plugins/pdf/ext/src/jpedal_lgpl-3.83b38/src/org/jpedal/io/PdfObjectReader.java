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
* PdfObjectReader.java
* ---------------
*/
package org.jpedal.io;

import java.io.File;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.Javascript;

import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfObject;

public interface PdfObjectReader {

	/**set a password for encryption*/
	public void setEncryptionPassword(String password);

	/**
	 * turns any refs into String or Map
	 */
	//public Object resolveToMapOrString(Object command, Object field);

	/**
	 * read a dictionary object
	 */
	public int readDictionaryAsObject(PdfObject pdfObject, String objectRef,
                                      int i, byte[] raw,
                                      int endPoint, String paddingString, boolean isInlineImage);

	/**
	 * read a dictionary object
	 */
	//public int readDictionary(String objectRef, int level, Map rootObject,
      //                        int i, byte[] raw, Map textFields,
        //                      int endPoint);

	/**read a stream
	 * @param isMetaData TODO*/
	//public byte[] readStream(Map objData, String objectRef, boolean cacheValue,
	//		boolean decompress, boolean keepRaw, boolean isMetaData, boolean isCompressedStream);

    /**read a stream
         * @param isMetaData TODO
     * @param cacheFile*/
        public byte[] readStream(PdfObject obj, boolean cacheValue,
                                 boolean decompress, boolean keepRaw, boolean isMetaData, boolean isCompressedStream, String cacheFile);

    /**read a stream*/
	//public byte[] readStream(String ref, boolean decompress);

	/**
	 * stop cache of last object in readObject
	 *
	 */
	//public void flushObjectCache();

	/**
	 * read an object in the pdf as an Object
	 *
	 */
	public void readObject(PdfObject pdfObject);
	
	/**
	 * read an object in the pdf into a Map which can be an indirect or an object
	 *
	 */
	//public Map readObject(PdfObject pdfObject, String objectRef,
      //                    Map textFields);

	/**
	 * read reference table start to see if new 1.5 type or traditional xref
	 * @throws PdfException
	 */
	public PdfObject readReferenceTable() throws PdfException;

	/**
	 * read the form data from the file
	 */
    public PdfFileInformation readPdfFileMetadata(PdfObject metadataObj);

	/**
	 * get value which can be direct or object
	 */
	//public String getValue(String value);

	/**
	 * get text value as byte stream which can be direct or object
	 */
	//public byte[] getByteTextStringValue(Object rawValue, Map fields);

	/**
	 * return flag to show if encrypted
	 */
	public boolean isEncrypted();

	/**
	 * return flag to show if valid password has been supplied
	 */
	public boolean isPasswordSupplied();

	/**
	 * return flag to show if encrypted
	 */
	public boolean isExtractionAllowed();

	/**show if file can be displayed*/
	public boolean isFileViewable();

	/**extract  metadata for  encryption object
	 */
	//public void readEncryptionObject(String ref) throws PdfSecurityException;

	/**
	 * return pdf data
	 */
	public byte[] getPdfBuffer();

	/**
	 * read any names
	 */
    public void readNames(PdfObject obj, Javascript javascript, boolean isKid);

    /**
	 * convert name into object ref
	 */
	public String convertNameToRef(String value);

	/**
	 * set size over which objects kept on disk
	 */
	public void setCacheSize(int miniumumCacheSize);

	/**read data directly from PDF*/
	//public byte[] readStreamFromPDF(int start, int end);

	//public void readStreamIntoMemory(Map downField);

	/**
	 * main routine which is passed list of filters to decode and the binary
	 * data. JPXDecode/DCTDecode are not handled here (we leave data as is and
	 * then put straight into a JPEG)<br>
	 * <p>
	 * <b>Note</b>
	 * </p>
	 * Not part of API
	 * </p>
	 */
//	public byte[] decodeFilters(byte[] data, String filter_list, Object rawParams,
//			int width, int height, boolean useNewCCITT, String cacheName)
//			throws Exception;

	/**
	 * get pdf type in file (found at start of file)
	 */
	public String getType();

	/**
	 * open pdf file<br> Only files allowed (not http)
	 * so we can handle Random Access of pdf
	 */
	public void openPdfFile(String filename) throws PdfException;

	/**
	 * open pdf file using a byte stream
	 */
	public void openPdfFile(byte[] data) throws PdfException;

	/**
	 * close the file
	 */
	public void closePdfFile();

	
	public PdfObject readFDF() throws PdfException;

    /**give user access to internal PDF values */
    int getPDFflag(Integer i);


    /**get stream as decompressed block on disk and return Path as String*/
    //String getStreamOnDisk(String fontFileRef);

    public byte[] decodeFilters(PdfObject DecodeParms, byte[] data, PdfArrayIterator filters,
    		int width, int height, boolean useNewCCITT,byte[] globalData,
    		String cacheName) throws Exception;

    //public int handleColorSpaces(PdfObject pdfObject,int i, byte[] raw, boolean debugFastCode, String paddingString);
    
    /**return size of object*/
    //int getObjectSize(String fontFileRef);

    /**flush object cache which can causes issues on multiple decodes for iamges*/
    //public void resetCache();

    public void checkResolved(PdfObject pdfObject);

    /**
     * allow user to access SOME objects
     * currently PdfDictionary.Encryption
     */
    PdfObject getPDFObject(int key);

	public void dispose();

    void spoolStreamDataToDisk(File tmpFile, long startStreamOnDisk) throws Exception;
}
