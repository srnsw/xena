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
* RandomAccessFileBuffer.java
* ---------------
*/

package org.jpedal.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

import org.jpedal.utils.LogWriter;

public class RandomAccessFileBuffer extends RandomAccessFile implements RandomAccessBuffer {

	private String fileName="";

  public RandomAccessFileBuffer(File file, String mode) throws FileNotFoundException
  {
  	super(file, mode);
  	fileName=file.getAbsolutePath();
  	
  }

  public RandomAccessFileBuffer(String file, String mode) throws FileNotFoundException
  {
  	
    super(file, mode);
    fileName=file;
  
  }
  
  public byte[] getPdfBuffer(){
  	
  	URL url =null;
  	byte[] pdfByteArray = null;
  	InputStream is = null;
  	ByteArrayOutputStream os = null;
  	
  	try {
  		url = new URL("file:"+fileName);
  		is = url.openStream();
  		os = new ByteArrayOutputStream();
  		
  		// Download buffer
  		byte[] buffer = new byte[4096];
  		
  		// Download the PDF document
  		int read = 0;
  		while ((read = is.read(buffer)) != -1) {
  			os.write(buffer, 0 ,read);
  		}
  		// Copy output stream to byte array
  		pdfByteArray = os.toByteArray();
  		
  		// Close streams
  		is.close();
  		os.close();
  		
  	} catch (IOException e) {
  		LogWriter.writeLog("[PDF] Exception "+e+" getting byte[] for "+fileName);
  	}
  	
  	return pdfByteArray;
  }
}
