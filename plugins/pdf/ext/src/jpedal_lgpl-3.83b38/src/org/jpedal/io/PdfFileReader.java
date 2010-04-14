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
* PdfFileReader.java
* ---------------
*/
package org.jpedal.io;


import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpedal.exception.PdfException;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_boolean;

/**
 * provides access to the file using Random access class to
 * read bytes and strings from a pdf file. Pdf file is a mix of
 * character and binary streams
 */
public class PdfFileReader
{
	
	/**list of cached objects to delete*/
	protected Map cachedObjects=new HashMap();
	
	//Colorspaces
	protected Map cachedColorspaces=new HashMap();

	boolean isFDF=false;
	
	/**file access*/
	//protected  RandomAccessFile pdf_datafile=null;
	protected RandomAccessBuffer pdf_datafile = null;
		
	/**currentGeneration used by decryption*/
	protected int currentGeneration=0;
	
	/**file length*/
	protected long eof =0;
	
	/**location from the reference table of each
	 * object in the file
	 */
	protected Vector_Int offset = new Vector_Int( 2000 );
	
	/**flag to show if compressed*/
	protected Vector_boolean isCompressed=new Vector_boolean(2000);
	
	/**generation of each object*/
	protected Vector_Int generation = new Vector_Int( 2000 );

/////////////////////////////////////////////////////////////////////////
	/**
	 * version of move pointer which takes object name
	 * as int 
	 */
	final protected long movePointer( int currentID,int generation)
	{
		currentGeneration=generation;
		
		long pointer = offset.elementAt(currentID );

		return movePointer( pointer );
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * version of move pointer which takes object name
	 * and converts before calling main routine
	 */
	final protected long movePointer( String pages_id )
	{
		long pointer = getOffset( pages_id );

		return movePointer( pointer );
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * get pdf type in file (found at start of file)
	 */
	final public String getType()
	{
		
		String pdf_type = "";
		try{
			movePointer( 0 );
			pdf_type = pdf_datafile.readLine();
			
			//strip off anything before
			int pos=pdf_type.indexOf("%PDF");
			if(pos!=-1)
				pdf_type=pdf_type.substring(pos+5);
				
		}catch( Exception e ){
			LogWriter.writeLog( "Exception " + e + " in reading type" );
		}
		return pdf_type;
	}

    /**
	 * open pdf file<br> Only files allowed (not http)
	 * so we can handle Random Access of pdf
	 */
	final public void openPdfFile( InputStream in) throws PdfException
	{

        try
		{
            //use byte[] directly if small otherwise use Memory Map
            pdf_datafile = new RandomAccessMemoryMapBuffer(in );

			eof = pdf_datafile.length();
			//pdf_datafile = new RandomAccessFile( filename, "r" );

		}catch( Exception e )
		{
			LogWriter.writeLog( "Exception " + e + " accessing file" );
			throw new PdfException( "Exception " + e + " accessing file" );
		}

    }

	/**
	 * open pdf file<br> Only files allowed (not http)
	 * so we can handle Random Access of pdf
	 */
	final public void openPdfFile( String filename ) throws PdfException
	{


		isFDF=filename.toLowerCase().endsWith(".fdf");

		try
		{
			//possible alternatve - 16042009- tested on MAc and no obvious gains
            //pdf_datafile = new RandomAccessMemoryMapBuffer( new File(filename) );
			pdf_datafile = new RandomAccessFileBuffer( filename, "r" );
			eof = pdf_datafile.length();
	
		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Exception " + e + " accessing file" );
			throw new PdfException( "Exception " + e + " accessing file" );
		}
		
	}

	/**
		 * open pdf file using a byte stream
		 */
	final public void openPdfFile( byte[] data ) throws PdfException
	{
		try
		{
            //use byte[] directly if small otherwise use Memory Map
            if(data.length<16384)
			    pdf_datafile = new RandomAccessDataBuffer( data );
            else
			    pdf_datafile = new RandomAccessMemoryMapBuffer( data );
            
			eof = pdf_datafile.length();
			//pdf_datafile = new RandomAccessFile( filename, "r" );

		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Exception " + e + " accessing file" );
			throw new PdfException( "Exception " + e + " accessing file" );
		}
		
		LogWriter.writeMethod("{openPdfFile} EOF="+eof,0);
		
	}


	//////////////////////////////////////////////////////////////////////////
	/**
	 * returns current location pointer and sets to new value
	 */
	final protected long movePointer( long pointer ) 
	{
		long old_pointer = 0;
		try
		{
			//make sure inside file
			if( pointer > eof ){
				LogWriter.writeLog( "Attempting to access ref outside file" );
			//throw new PdfException("Exception moving file pointer - ref outside file");
			}else{
				old_pointer = getPointer();
				pdf_datafile.seek( pointer );
			}
		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Exception " + e + " moving pointer to  "+pointer+" in file. EOF ="+eof);
			
		}
		return old_pointer;
	}
	//////////////////////////////////////////////////
	/**
	 * gets pointer to current location in the file
	 */
	final protected long getPointer()
	{
		long old_pointer = 0;
		try
		{
			old_pointer = pdf_datafile.getFilePointer();
		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Exception " + e + " getting pointer in file" );
		}
		return old_pointer;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * close the file
	 */
	final public void closePdfFile()
	{
		try
		{
			if(pdf_datafile!=null){
				pdf_datafile.close();
				pdf_datafile=null;
			}
			
			if(cachedObjects!=null){
				Iterator files=cachedObjects.keySet().iterator();
				while(files.hasNext()){
					String fileName=(String)files.next();
					File file=new File(fileName);
					//System.out.println("PdfFileReader - deleting file "+fileName);
					file.delete();
					if(file.exists())
						LogWriter.writeLog("Unable to delete temp file "+fileName);
				}
			}
		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Exception " + e + " closing file" );
		}
		
		//delete any colorspaces
		cachedColorspaces.clear();

	}

	/**
	 * place object details in queue
	 */
	final protected void storeObjectOffset( int current_number, int current_offset, int current_generation,boolean isEntryCompressed)
	{

        /**
		 * check it does not already exist
		 */
		int existing_generation = 0;
		int offsetNumber=0;
		
		if(current_number<generation.getCapacity()){
			existing_generation=generation.elementAt( current_number );
			offsetNumber=offset.elementAt( current_number ) ;

		}
	
		//write out if not a newer copy (ignore items from Prev tables if newer)
		if( existing_generation < current_generation  || offsetNumber== 0 )
		{
            offset.setElementAt( current_offset, current_number );
			generation.setElementAt( current_generation, current_number );
			isCompressed.setElementAt(isEntryCompressed,current_number);
		}else{
		//LogWriter.writeLog("Object "+current_number + ", generation "+
		//current_generation + " already exists as"+
		//existing_generation);
		}

    }
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * returns stream in which compressed object will be found
	 */
	protected final int getCompressedStreamObject(int currentID,int gen)
	{
		
		currentGeneration=gen;
		 
		return offset.elementAt(currentID );
	}
	
	/**
	 * returns stream in which compressed object will be found
	 * (actually reuses getOffset internally)
	 */
	protected final int getCompressedStreamObject( String value )
	{
		
		int currentID=0;
		//		handle indirect reference
		  if( value.endsWith( "R" ) == true )
		  {
			  StringTokenizer values = new StringTokenizer( value );
			  currentID = Integer.parseInt( values.nextToken() );
			  currentGeneration=Integer.parseInt( values.nextToken() );
		  }
		  else
			  LogWriter.writeLog( "Error with reference ..value=" + value+"<" );	
		
		return offset.elementAt(currentID );
	}
	
	/**
	 * general routine to turn reference into id with object name
	 */
	protected final int getOffset( String value )
	{
		
		int currentID=0;
		//		handle indirect reference
		  if( value.endsWith( "R" ) == true )
		  {
			  StringTokenizer values = new StringTokenizer( value );
			  currentID = Integer.parseInt( values.nextToken() );
			  currentGeneration=Integer.parseInt( values.nextToken() );
		  }
		  else
			  LogWriter.writeLog( "2. Error with reference .." + value+"<<" );	
		
		return offset.elementAt(currentID );
	}
	
	/**
	 * returns where in compressed stream value can be found
	 * (actually reuses getGen internally)
	 *
	protected final int getOffsetInCompressedStream( String value )
	{
		
		int currentID=0;
		//		handle indirect reference
		  if( value.endsWith( "R" ) == true )
		  {
			  StringTokenizer values = new StringTokenizer( value );
			  currentID = Integer.parseInt( values.nextToken() );
			  currentGeneration=Integer.parseInt( values.nextToken() );
		  }
		  else
			  LogWriter.writeLog( "3. Error with reference .." + value+"<" );	
		
		return generation.elementAt(currentID );
	}/**/
	
	/**
	 * general routine to turn reference into id with object name
	 *
	protected final int getGen( String value )
	{
		
		int currentID=0;
		//		handle indirect reference
		  if( value.endsWith( "R" ) == true )
		  {
			  StringTokenizer values = new StringTokenizer( value );
			  currentID = Integer.parseInt( values.nextToken() );
			  currentGeneration=Integer.parseInt( values.nextToken() );
		  }
		  else
			  LogWriter.writeLog( "4. Error with reference .." + value+"<" );	
		
		return generation.elementAt(currentID );
	}/**/

    /**
	 * general routine to turn reference into id with object name
	 */
	protected final boolean isCompressed( int ref,int gen )
	{

		currentGeneration=gen;

		return isCompressed.elementAt(ref);
	}

    /**
	 * general routine to turn reference into id with object name
	 */
	protected final boolean isCompressed( String value )
	{
		
		int currentID=0;
		//		handle indirect reference
		  if( value.endsWith( "R" ) == true )
		  {
			  StringTokenizer values = new StringTokenizer( value );
			  currentID = Integer.parseInt( values.nextToken() );
			  currentGeneration=Integer.parseInt( values.nextToken() );
		  }
		  else
			  LogWriter.writeLog( "5.Error with reference .." + value+"<" );	
		
		return isCompressed.elementAt(currentID );
	}

}

