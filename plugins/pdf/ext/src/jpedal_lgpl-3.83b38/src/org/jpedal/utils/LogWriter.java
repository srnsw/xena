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
* LogWriter.java
* ---------------
*/
package org.jpedal.utils;

//imports in Standard Java
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import org.jpedal.gui.ShowGUIMessage;

/**
 * <p>logs all activity. And some low level variables/methods
 * as it is visible to all classes.
 * <p>Provided for debugging and NOT officially part of the API 
 * <p>Could be superceded by logger
 * function in Java 1.4
 */
public class LogWriter
{

	/**amount of debugging detail we put in log*/
	public static boolean debug = false;

	/**filename of logfile*/
	static public String log_name = null;

	/**amount of debugging detail we put in log*/
	private static int debug_level = 0;

	/**flag we can set to signal code being tested*/
	static public boolean testing=false;

	/**if we echo to console. VERY USEFUL for debugging*/
	private static boolean verbose = false;

	public LogWriter()
	{
	    
	}
	///////////////////////////////////////////////
	/**
	 * reset logfile
	 */
	final public static void resetLogFile()
	{
		if( log_name != null )
		{
			//write message
			PrintWriter log_file = null;
			try
			{
				log_file = new PrintWriter( new FileWriter( log_name, false ) );
				log_file.println(TimeNow.getTimeNow()+" Running Storypad");
				log_file.flush();
				log_file.close();
			}
			catch( Exception e )
			{
				System.err.println( "Exception " + e + " attempting to write to log file " + log_name );
			}
		}
	}
	///////////////////////////////////////////////
	final public static void writeLog( String message )
	{

		/**
		 * write message to pane if client active
		 * and put to front
		 */
		if( verbose == true )
			System.out.println( message );
			
		if( log_name != null )
		{

			//write message
			PrintWriter log_file = null;
			try
			{
				log_file = new PrintWriter( new FileWriter( log_name, true ) );
				
				if(!testing)
				log_file.println(TimeNow.getTimeNow()+ ' ' +message);		//write date to the log
				
				log_file.println( message );
				log_file.flush();
				log_file.close();
			}
			catch( Exception e )
			{
				System.err.println( "Exception " + e + " attempting to write to log file " + log_name );
			}
			log_file = null;

		}
	}
	///////////////////////////////////////////////
	final public static void debugFile( String message )
	{

		//write message
		PrintWriter log_file = null;
		try
		{
			log_file = new PrintWriter( new FileWriter( "D://debug.txt", true ) );
			log_file.println( message ); //write date to the log
			log_file.flush();
			log_file.close();
		}
		catch( Exception e )
		{
			System.err.println( "Exception " + e + " attempting to write to log file " + log_name );
		}
		log_file = null;
	}
	/////////////////////////////////////////////////////////////////////////
	/** test if file writable and can be written
	 *  NOT used by jpedal
	 */
	final public static boolean testLogFileWriteable()
	{
		boolean failed = false;
		File test_log = null;

		/**
		 * see if log exists
		 */
		try
		{
			test_log = new File( log_name );
		}
		catch( Exception e )
		{
			//cannot proceed so shutdown with message
			ShowGUIMessage.showGUIMessage( "Exception " + e + " testing log. Check logdir exists.", "Problem with logfile" );
			throw new RuntimeException( "Exception " + e + " testing log. Check logdir exists." );
		}

		//try to create file if it does not exit
		if( test_log.exists() == false )
		{
			try
			{
				PrintWriter log_file = new PrintWriter( new FileWriter( log_name, true ) );
				if(!testing)
				log_file.println( TimeNow.getTimeNow() + " Log created" ); //write date to the log
				log_file.flush();
				log_file.close();
				
			}
			catch( Exception e )
			{
				System.err.println( "Exception " + e + " attempting to write to log file " + test_log );
			}
		}

		//test if exists and writeable
		if( ( test_log.exists() ) && ( test_log.canWrite() == false ) )
		{
			System.err.println( "Log " + log_name + " exists, but cannot written - Check permissions" );
			failed = true;
		}
		return failed;
	}
	//////////////////////////////////////////////
	/**
	 * setup log file and check it is readable
	 * also sets command line options
	 */
	final public static void setupLogFile( boolean internal, int current_debug_level, String version, String command_line_values, boolean showMessages)
	{

        //<start-jfr>
        //if no version, default to JPedal version
		if(version.length()==0)
			version=org.jpedal.PdfDecoder.version;
		//<end-jfr>
        
        debug_level = current_debug_level;
		if( debug_level > 0 )
			debug = true;
		
		if( command_line_values != null )
		{

			//verbose mode echos to screen
			if( command_line_values.indexOf('v') != -1 )
			{
				verbose = true;
				writeLog( "Verbose on" );
			}
			else
				verbose = false;

		}

		//write out info
		if(!testing){
			writeLog( "Software version - " + version );
			writeLog( "Software started - " + TimeNow.getTimeNow() );
		}
		writeLog( "=======================================================" );
	}
	///////////////////////////////////////////////
	/**version to writeout without adding a new line*/
	final public static void writeLogWithoutCR( String message )
	{

		/**
		 * write message to pane if client active
		 * and put to front
		 */

		//write message
		PrintWriter log_file = null;
		try
		{
			log_file = new PrintWriter( new FileWriter( log_name, true ) );
			log_file.print( message ); //write date to the log
			if( verbose == true )
				System.out.print( message );
			log_file.flush();
		}
		catch( Exception e )
		{
			System.err.println( "Exception " + e + " attempting to write to log file " + log_name );
		}
		log_file = null;

	}
	/////////////////////////////////////////////////
	/**
	 * stop all logging
	 */
	final public static void noLogging()
	{
		verbose = false;
		log_name = null;
	}

	/////////////////////////////////////////////////
	/**
	 * write out log method
	 */
	final public static void writeMethod( String message )
	{
		long free = ( Runtime.getRuntime().freeMemory() / 1024 );
		long total = ( Runtime.getRuntime().totalMemory() / 1024 );
		long used = total - free;
		message = message + " MEM=" + used + " ( " + total + " - " + free + ')';
		writeLog( message );
	}
	/////////////////////////////////////////////////
	/**
	 * write out log method
	 */
	final public static void writeMethod( String message, int level )
	{
		if( debug_level > level )
			writeLog( message );
		
	}

	/////////////////////////////////////////////////
	/**
	 * write out log method
	 */
	final public static void write(Map m,int level){
	    if(debug_level>level)
	    {
		Iterator e=m.entrySet().iterator();
		while (e.hasNext())
		{
		    Map.Entry me=(Map.Entry) e.next();
		    writeLog(me.getKey().toString()+ '=' +me.getValue().toString());
		}
	    }

	}
	///////////////////////////////////////////////////////////
	/** write out logging information for forms,
	 * <b>print</b> is a boolean flag, if true prints to the screen
	 */
    public static void writeFormLog(String message,boolean print) {
        if(print)
            System.out.println("[forms] "+message);
        
        writeLog("[forms] "+message);
    }


///////////////////////////////////////////////////////////
/**
 * display message if in GUI mode

 public static void showGUIMessage(String message,String title){


 /**
 * create a display

 Container contentPane = null;
 JPanel display=new JPanel();
 display.setLayout(new BorderLayout());
 JLabel message_component=new JLabel(message);
 display.add(message_component,BorderLayout.CENTER);

 //set sizes
 int width=(int) message_component.getSize().getWidth();
 int height=(int) message_component.getSize().getHeight();
 display.setSize(new Dimension(width+10,height+10));

 /**
 * set optiontype - default is Ok

 int type=JOptionPane.DEFAULT_OPTION;
 int display_type=JOptionPane.PLAIN_MESSAGE;


 /**
 * create the dialog

 int result = JOptionPane.showConfirmDialog(
 contentPane, // parentComponent
 display, // message
 title,
 type, // optionType
 display_type); // messageType


 }
 */
}
