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
* ShowDocumentProperties.java
* ---------------
*/
package org.jpedal.examples;
import java.io.File;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.PdfFileInformation;


/**
 * This example opens a pdf file and gets the document properties
 */
public class ShowDocumentProperties
{
	
	/**user dir in which program can write*/
	private String user_dir = System.getProperty( "user.dir" );
	
	/**sample file which can be setup - substitute your own. */
	private static String test_file = "/mnt/win_d/sample.pdf";
	
	/**example method to open a file and return the number of pages*/
	public ShowDocumentProperties( String file_name ) 
	{
		String separator = System.getProperty( "file.separator" );
		
		//check output dir has separator
		if( user_dir.endsWith( separator ) == false )
			user_dir = user_dir + separator;
		
		/**
		 * set up PdfDecoder object telling
		 * it whether to display messages
		 * and where to find its lookup tables
		 */
		PdfDecoder decode_pdf = null;
		
		//PdfDecoder returns a PdfException if there is a problem
		try
		{
			decode_pdf = new PdfDecoder( false );
			
			/**
			 * open the file (and read metadata including pages in  file)
			 */
			System.out.println( "Opening file :" + file_name );
			decode_pdf.openPdfFile( file_name );
		}catch( Exception e ){
			System.err.println( "3.Exception " + e + " in pdf code" );	
		}
		
		/**
		 * extract data from pdf (if allowed). 
		 */
		if ((decode_pdf.isEncrypted())&&(!decode_pdf.isExtractionAllowed())) {
			System.out.println("Encrypted settings");
			System.out.println("Please look at SimpleViewer for code sample to handle such files");
			System.out.println("Or get support/consultancy");
			
		}
	
		/**get the Pdf file information object to extract info from*/
		PdfFileInformation currentFileInformation=decode_pdf.getFileInformationData();
		
		/**get the document properties*/
		String[] values=currentFileInformation.getFieldValues();
		String[] fields=currentFileInformation.getFieldNames();
		
		/**display*/
		int count=fields.length;
		
		System.out.println("Fields");
		System.out.println("======");
		for(int i=0;i<count;i++){
			System.out.println(fields[i]+" = "+values[i]);
		}	
		
		/**get and show any metadata*/
		System.out.println("\nMetadata");
		System.out.println("======");
		System.out.println(currentFileInformation.getFileXMLMetaData());
		
		/**close the pdf file*/
		decode_pdf.closePdfFile();
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * main routine which checks for any files passed and runs the demo
	 */
	public static void main( String[] args )
	{
		System.out.println( "Simple demo to extract pdf file properties" );
		
		//set to default
		String file_name = test_file;
		
		//check user has passed us a filename and use default if none
		if( args.length != 1 )
			System.out.println( "Default test file used" );
		else
		{
			file_name = args[0];
			System.out.println( "File :" + file_name );
		}
		
		//check file exists
		File pdf_file = new File( file_name );
		
		//if file exists, open and get number of pages
		if( pdf_file.exists() == false )
			System.out.println( "File " + file_name + " not found" );
		else{
			ShowDocumentProperties ShowDocumentProperties1 = new ShowDocumentProperties( file_name );
		}
	}
}
