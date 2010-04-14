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
* SaveImage.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.jpedal.PdfDecoder;
import org.jpedal.utils.Messages;
public class SaveImage extends Save{
	
	private ButtonGroup buttonGroup1 = new ButtonGroup();
	
	private JToggleButton jToggleButton2 = new JToggleButton();
	private JToggleButton jToggleButton3 = new JToggleButton();
	
	private JLabel OutputLabel = new JLabel();
	private JRadioButton isPNG = new JRadioButton();
	private JRadioButton isTiff = new JRadioButton();
	private JRadioButton isJPEG = new JRadioButton();
	
	private JRadioButton isHires= new JRadioButton();
	private JRadioButton isNormal= new JRadioButton();
	private JRadioButton isDownsampled= new JRadioButton();
	
	private ButtonGroup buttonGroup2= new ButtonGroup();

    public SaveImage( String root_dir, int end_page, int currentPage ) {

        super(root_dir, end_page,currentPage);

        try{
            jbInit();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
	
	/**
	 * get root dir
	 */
	final public String getPrefix(){
		String prefix = "png";
		if( isTiff.isSelected() )
			prefix = "tif";
		if( isJPEG.isSelected() )
			prefix = "jpg";
		return prefix;
	}
	
	/**
	 * get root dir
	 */
	final public int getImageType(){
		int prefix = PdfDecoder.CLIPPEDIMAGES;
		
		if( isNormal.isSelected() )
			prefix = PdfDecoder.RAWIMAGES;
		if( isDownsampled.isSelected() )
			prefix = PdfDecoder.FINALIMAGES;
		
		return prefix;
	}
	

	private void jbInit() throws Exception{
		
		rootFilesLabel.setBounds( new Rectangle( 13, 12, 400, 26 ) );
		
		rootDir.setBounds( new Rectangle( 23, 39, 232, 23 ) );
		
		changeButton.setBounds( new Rectangle( 272, 39, 101, 23 ) );
		
		startPage.setBounds( new Rectangle( 125, 99, 75, 22 ) );
		
		pageRangeLabel.setBounds( new Rectangle( 13, 70, 400, 26 ) );
		
		startLabel.setBounds( new Rectangle( 23, 100, 100, 22 ) );
		
		endLabel.setBounds( new Rectangle( 220, 99, 75, 22 ) );
		
		endPage.setBounds( new Rectangle( 285, 99, 75, 22 ) );
		
		
		optionsForFilesLabel.setBounds( new Rectangle( 13, 133, 600, 26 ) );
		
		OutputLabel.setText(Messages.getMessage("PdfViewerMessage.OutputType"));
		OutputLabel.setBounds( new Rectangle( 23, 173, 900, 24 ) );
		isTiff.setText( "Tiff" );
		isTiff.setBounds( new Rectangle( 180, 175, 50, 19 ) );
		isJPEG.setBounds( new Rectangle( 290, 174, 67, 19 ) );
		isJPEG.setSelected( true );
		isJPEG.setText( "JPEG" );
		isPNG.setBounds( new Rectangle( 360, 174, 62, 19 ) );
		isPNG.setText( "PNG" );
		
		isHires.setText(Messages.getMessage("PdfViewerOption.Hires"));
		isHires.setBounds( new Rectangle( 180, 200, 112, 19 ) );
		isHires.setSelected( true );
		isNormal.setBounds( new Rectangle( 290, 200, 73, 19 ) );
		isNormal.setText(Messages.getMessage("PdfViewerOption.Normal"));
		isDownsampled.setBounds( new Rectangle( 360, 200, 200, 19 ) );
		isDownsampled.setText(Messages.getMessage("PdfViewerOption.Downsampled"));
		
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
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		this.add( OutputLabel, null );
		this.add( isTiff, null );
		this.add( isJPEG, null );
		this.add( isPNG, null );
		buttonGroup1.add( isTiff );
		buttonGroup1.add( isJPEG );
		buttonGroup1.add( isPNG );
		
		this.add( isHires, null );
		this.add( isNormal, null );
		this.add( isDownsampled, null );
		buttonGroup2.add( isHires );
		buttonGroup2.add( isNormal );
		buttonGroup2.add( isDownsampled );
		
	}
	
	final public Dimension getPreferredSize(){
		return new Dimension( 500, 250 );
	}
	
}
