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
* SaveBitmap.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.jpedal.utils.Messages;

/**specific code for Bitmap save function*/ 
public class SaveBitmap extends Save{

	JLabel OutputLabel = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	JToggleButton jToggleButton3 = new JToggleButton();
	
	JToggleButton jToggleButton2 = new JToggleButton();
	
	JRadioButton isPNG = new JRadioButton();
	
	JRadioButton isTiff = new JRadioButton();
	
	JRadioButton isJPEG = new JRadioButton();
	
	public SaveBitmap( String root_dir, int end_page, int currentPage ) {
		super(root_dir, end_page,currentPage);

		try
		{
			jbInit();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	///////////////////////////////////////////////////////////////////////
	/**
	 * get root dir
	 */
	final public String getPrefix()
	{
		String prefix = "png";
		if( isTiff.isSelected() )
			prefix = "tif";
		if( isJPEG.isSelected() )
			prefix = "jpg";
		return prefix;
	}
	
	final public Dimension getPreferredSize()
	{
		return new Dimension( 490, 280 );
	}
	
	private void jbInit() throws Exception
	{
		
		scalingLabel.setBounds( new Rectangle( 13, 12, 400, 19 ) );
		
		scaling.setBounds( new Rectangle( 400, 12, 69, 23) );

		rootFilesLabel.setBounds( new Rectangle( 13, 55, 400, 26 ) );
		
		rootDir.setBounds( new Rectangle( 23, 82, 232, 23 ) );
		
		changeButton.setBounds( new Rectangle( 272, 82, 101, 23 ) );		
		
		OutputLabel.setText(Messages.getMessage("PdfViewerMessage.OutputType"));
		OutputLabel.setBounds( new Rectangle( 23, 216, 300, 24 ) );
		isTiff.setText( "Tiff" );
		isTiff.setBounds( new Rectangle( 180, 218, 50, 19 ) );
		isJPEG.setBounds( new Rectangle( 240, 217, 67, 19 ) );
		isJPEG.setSelected( true );
		isJPEG.setText( "JPEG" );
		isPNG.setBounds( new Rectangle( 305, 217, 62, 19 ) );
		isPNG.setText( "PNG" );
		isPNG.setName("radioPNG");
		
		optionsForFilesLabel.setBounds( new Rectangle( 13, 176, 600, 26 ) );
		
		startPage.setBounds( new Rectangle( 125, 142, 75, 22 ) );
		
		pageRangeLabel.setBounds( new Rectangle( 13, 113, 400, 26 ) );
		
		startLabel.setBounds( new Rectangle( 23, 142, 100, 22 ) );

		endLabel.setBounds( new Rectangle( 220, 142, 75, 22 ) );

		endPage.setBounds( new Rectangle( 285, 142, 75, 22 ) );

		//common
		this.add( startPage, null );
		this.add( endPage, null );
		this.add( rootDir, null );
		this.add( scaling, null );
		this.add( scalingLabel, null );
		this.add( rootFilesLabel, null );
		this.add( changeButton, null );
		this.add( endLabel, null );
		this.add( startLabel, null );
		this.add( pageRangeLabel, null );
		
		this.add( optionsForFilesLabel, null );
		this.add( OutputLabel, null );
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		this.add( isTiff, null );
		this.add( isJPEG, null );
		this.add( isPNG, null );
		
		buttonGroup1.add( isTiff );
		buttonGroup1.add( isJPEG );
		buttonGroup1.add( isPNG );
	}
	
	
}
