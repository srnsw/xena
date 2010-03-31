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
* ShowGUIMessage.java
* ---------------
*/
package org.jpedal.gui;

//import of JFC
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.jpedal.utils.LogWriter;

/**
 * provides a popup message if the library is being run in GUI mode <br>
 * <p>
 * <b>Note </b> these methods are not part of the API and is not guaranteed to
 * be in future versions of JPedal.
 * </p>
 *  
 */
public class ShowGUIMessage
{

	/**screen component to display*/
	private static Container contentPane = null;

	/**flag to show if GUI mode and display popup messages*/
	private static boolean outputMessages = false;

	public ShowGUIMessage()
	{

	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
	final static public void showGUIMessage( String user_message, JTextPane messages, String title )
	{

		//make sure user can't edit message
		messages.setEditable( false );

		/**
		 * create a display, including scroll pane
		 */
		JPanel display = new JPanel();
		JScrollPane display_scroll_pane = new JScrollPane();
		display_scroll_pane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		display_scroll_pane.getViewport().add( messages );
		display.setLayout( new BorderLayout() );
		display.add( display_scroll_pane, BorderLayout.CENTER );

		//add a user-defined message
		if( user_message != null )
			display.add( new JLabel( "<HTML><BODY><I>" + user_message + "</I></BODY></HTML>", 0 ), BorderLayout.SOUTH );

		//set text display to scroll
		messages.setEditable( false );
		display.setPreferredSize( new Dimension( 300, 200 ) );

		/**
		 * create the dialog
		 */
		JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
	}

	//////////////////////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
	final static public void showGUIMessage( String user_message, JLabel messages, String title )
	{

		/**
		 * create a display, including scroll pane
		 */
		JPanel display = new JPanel();
		display.setLayout( new BorderLayout() );
		display.add( messages, BorderLayout.CENTER );

		//add a user-defined message
		if( user_message != null )
			display.add( new JLabel( user_message, 0 ), BorderLayout.SOUTH );

		/**
		 * create the dialog
		 */
		JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
		contentPane.setVisible(true);
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * initialise so popup window will appear in front of correct frame.
	 */
	final public static void setParentFrame( JFrame main_frame )
	{
		contentPane = main_frame;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
	final public static void showstaticGUIMessage( StringBuffer message, String title )
	{

		/**
		 * create a display
		 */
		JTextArea text_pane = new JTextArea();
		text_pane.setEditable( false );
		text_pane.setWrapStyleWord( true );
		text_pane.append( "  " + message + "  " );
		JPanel display = new JPanel();
		display.setLayout( new BorderLayout() );
		display.add( text_pane, BorderLayout.CENTER );

		//set sizes
		int width = (int)text_pane.getSize().getWidth();
		int height = (int)text_pane.getSize().getHeight();
		display.setSize( new Dimension( width + 10, height + 10 ) );

		/**
		 * create the dialog
		 */
		JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
	}

	//////////////////////////////////////////////////////////////////////////
	/**
	 * set client flag to display
	 */
	final public static void setClientDisplay()
	{
		outputMessages = true;
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
	final static public void showGUIMessage( String user_message, ImageIcon image, String title )
	{

		/**
		 * create a display
		 */
		JPanel display = new JPanel();
		display.setLayout( new BorderLayout() );
		JLabel message_component = new JLabel( image );
		display.add( message_component, BorderLayout.CENTER );
		if( user_message != null )
			display.add( new JLabel( user_message ), BorderLayout.SOUTH );

		//set sizes
		int width = (int)message_component.getSize().getWidth();
		int height = (int)message_component.getSize().getHeight();
		display.setSize( new Dimension( width + 10, height + 10 ) );

		/**
		 * set optiontype - default is Ok
		 */
		int type = JOptionPane.DEFAULT_OPTION;
		int display_type = JOptionPane.PLAIN_MESSAGE;


		/**
		 * create the dialog
		 */
		JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, type, /* optionType*/ display_type ); // messageType
	}
	////////////////////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
	final static public void showGUIMessage( String user_message, BufferedImage image, String title )
	{

	    if(image==null)
	        return;
		/**
		 * create a display
		 */
		ImagePanel display = new ImagePanel( image );
		display.setLayout( new BorderLayout() );
		//display.setBackground(Color.cyan);
		if( user_message != null )
			display.add( new JLabel( user_message ), BorderLayout.SOUTH );

		//set sizes
		int width = image.getWidth();
		int height = image.getHeight();
		
		display.setSize( new Dimension( width + 10, height + 10 ) );

		/**
		 * create the dialog
		 */
		JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * display bufferedImage
	 */
	final public static void showGUIMessage( String file_name, String title,String dummy )
	{
		FileInputStream in;
		BufferedImage image = null;
		try
		{
			in = new FileInputStream( file_name );
			image=ImageIO.read(in);
			
		}
		catch( Exception e )
		{
			LogWriter.writeLog( "Exception " + e + " getting image" );
		}

		/**
		 * create the dialog
		 */
		if( image != null )
		{
			/**
			 * create a display, including scroll pane
			 */
			JPanel display = new JPanel();
			JScrollPane display_scroll_pane = new JScrollPane();
			display_scroll_pane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
			display.setLayout( new BorderLayout() );
			display.add( display_scroll_pane, BorderLayout.CENTER );

			//set text display to scroll
			display.setPreferredSize( new Dimension( 300, 200 ) );

			/**
			 * create the dialog
			 */
			JOptionPane.showConfirmDialog( contentPane, /* parentComponent*/ display, /* message*/ title, JOptionPane.DEFAULT_OPTION, /* optionType*/ JOptionPane.PLAIN_MESSAGE ); // messageType
		}
	}
	///////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
	final public static void showGUIMessage( String message_string, String title )
	{

		//check for user mode just in case
		if( outputMessages == true )
		{
			String output_string = "<HTML><BODY><CENTER><FONT COLOR=black>";
			StringTokenizer lines = new StringTokenizer( message_string, "\n" );
			while( lines.hasMoreTokens() )
				output_string = output_string + lines.nextToken() + "</FONT></CENTER><CENTER><FONT COLOR=black>";
			output_string = output_string + "</FONT></CENTER></BODY></HTML>";
			JLabel text_message = new JLabel( output_string );
			text_message.setBackground( Color.white );
			showGUIMessage( null, text_message, title );
		}
	}
	///////////////////////////////////////////////////////////
	/**
	 * display message if in GUI mode
	 */
	final public static void showGUIMessage( StringBuffer message, String title )
	{
		showGUIMessage( message.toString(), title );
	}
}
