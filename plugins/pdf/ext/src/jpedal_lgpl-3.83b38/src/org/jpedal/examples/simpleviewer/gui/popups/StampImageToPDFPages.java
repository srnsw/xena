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
* StampImageToPDFPages.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.print.attribute.standard.PageRanges;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.jpedal.examples.simpleviewer.utils.FileFilterer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

public class StampImageToPDFPages extends Save
{

	JLabel OutputLabel = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	ButtonGroup buttonGroup2 = new ButtonGroup();
	
	JToggleButton jToggleButton3 = new JToggleButton();
	
	JToggleButton jToggleButton2 = new JToggleButton();
	
	JRadioButton printAll=new JRadioButton();
	JRadioButton printCurrent=new JRadioButton();
	JRadioButton printPages=new JRadioButton();
	
	JTextField pagesBox=new JTextField();
	
	JTextField imageBox=new JTextField();
	
	JSpinner rotationBox = new JSpinner(new SpinnerNumberModel(0, 0, 360, 1));
	
	JComboBox placementBox = new JComboBox(new String[] {Messages.getMessage("PdfViewerLabel.Overlay"),
			Messages.getMessage("PdfViewerLabel.Underlay")});
	
	JSpinner heightScale = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
	JSpinner widthScale = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
		
	JComboBox horizontalBox = new JComboBox(new String[]{Messages.getMessage("PdfViewerLabel.FromLeft"),
			Messages.getMessage("PdfViewerLabel.Centered"),Messages.getMessage("PdfViewerLabel.FromRight")
	});
	
	JComboBox verticalBox = new JComboBox(new String[]{Messages.getMessage("PdfViewerLabel.FromTop"),
			Messages.getMessage("PdfViewerLabel.Centered"),Messages.getMessage("PdfViewerLabel.FromBottom")
	});
	
	JSpinner horizontalOffset = new JSpinner(new SpinnerNumberModel(0.00, -1000.00, 1000.00, 1));
	JSpinner verticalOffset = new JSpinner(new SpinnerNumberModel(0.00, -1000.00, 1000.00, 1));
		
	public StampImageToPDFPages( String root_dir, int end_page, int currentPage ) 
	{
		super(root_dir, end_page, currentPage);
			
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
	final public int[] getPages()
	{
		
		int[] pagesToExport=null;
		
		if(printAll.isSelected()){
			pagesToExport=new int[end_page];
			for(int i=0;i<end_page;i++)
				pagesToExport[i]=i+1;

		}else if( printCurrent.isSelected() ){
			pagesToExport=new int[1];
			pagesToExport[0]=currentPage;
			
		}else if( printPages.isSelected() ){
			
			try{
				PageRanges pages=new PageRanges(pagesBox.getText());
				
				int count=0;
				int i = -1;
				while ((i = pages.next(i)) != -1) 
					count++;
				
				pagesToExport=new int[count];
				count=0;
				i = -1;
				while ((i = pages.next(i)) != -1){
					if(i > end_page){
						JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerText.Page")+ ' '
                                +pages+ ' ' +Messages.getMessage("PdfViewerError.OutOfBounds")+ ' ' +
								Messages.getMessage("PdfViewerText.PageCount")+ ' ' +end_page);
						return null;
					}
					pagesToExport[count]=i;
					count++;
				}
			}catch (IllegalArgumentException  e) {
				LogWriter.writeLog( "Exception " + e + " in exporting pdfs" );
				JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerError.InvalidSyntax"));
			}
		}
		
		return pagesToExport;

	}	
	
	public float getHorizontalOffset(){
		return Float.parseFloat(horizontalOffset.getValue().toString());
	}
	
	public float getVerticalOffset(){
		return Float.parseFloat(verticalOffset.getValue().toString());
	}
	
	public String getHorizontalPosition(){
		return (String) horizontalBox.getSelectedItem();
	}
	
	public String getVerticalPosition(){
		return (String) verticalBox.getSelectedItem();
	}
	
	public int getRotation(){
		return Integer.parseInt(rotationBox.getValue().toString());
	}
	
	public String getPlacement(){
		return (String) placementBox.getSelectedItem();
	}
	
	public int getHeightScale(){
		return Integer.parseInt(heightScale.getValue().toString());
	}
	
	public int getWidthScale(){
		return Integer.parseInt(widthScale.getValue().toString());
	}
	
	public String getImageLocation(){
		return imageBox.getText();
	}
	
	
	private void jbInit() throws Exception
	{
		
		JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerLabel.TextAndFont"));
		textAndFont.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		textAndFont.setDisplayedMnemonic( '0' );
		textAndFont.setBounds( new Rectangle( 13, 13, 220, 26 ) );
		
		JLabel image = new JLabel(Messages.getMessage("PdfViewerLabel.Image"));
		image.setBounds( new Rectangle( 20, 40, 50, 23 ) );
		
		imageBox.setBounds( new Rectangle(55, 40, 295, 23 ) );

		JButton browse = new JButton("...");
		browse.setBounds( new Rectangle(360, 40, 23, 23 ) );
		browse.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(root_dir);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				String[] png = new String[] { "png","tif","tiff","jpg","jpeg" }; //$NON-NLS-1$
				chooser.addChoosableFileFilter(new FileFilterer(png, "Images (Tiff, Jpeg,Png)")); //$NON-NLS-1$
				int state = chooser.showOpenDialog(null);
				
				File file = chooser.getSelectedFile();
				
				if (file != null && state == JFileChooser.APPROVE_OPTION) {
					imageBox.setText(file.getAbsolutePath());
				}
			}
		});
		
		JLabel rotation = new JLabel(Messages.getMessage("PdfViewerLabel.Rotation"));
		rotation.setBounds( new Rectangle( 20, 80, 90, 23 ) );
		
		rotationBox.setBounds( new Rectangle( 80, 80, 50, 23 ) );
		
		JLabel degrees = new JLabel(Messages.getMessage("PdfViewerText.Degrees"));
		degrees.setBounds( new Rectangle( 140, 80, 50, 23 ) );
		
		JLabel placement = new JLabel(Messages.getMessage("PdfViewerLabel.Placement"));
		placement.setBounds( new Rectangle( 240, 80, 70, 23 ) );
		
		placementBox.setBounds( new Rectangle( 300, 80,83, 23 ) );
		
		JLabel wScale = new JLabel(Messages.getMessage("PdfViewerLabel.WidthScale"));
		wScale.setBounds( new Rectangle( 20, 120, 100, 23 ) );
		
		widthScale.setBounds( new Rectangle( 120, 120, 60, 23 ) );
		
		JLabel hScale = new JLabel(Messages.getMessage("PdfViewerLabel.HeightScale"));
		hScale.setBounds( new Rectangle( 240, 120, 110, 23 ) );
		
		heightScale.setBounds( new Rectangle( 330, 120, 60, 23 ) );
		
		JLabel positionAndOffset = new JLabel(Messages.getMessage("PdfViewerLabel.PositionAndOffset"));
		positionAndOffset.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		positionAndOffset.setDisplayedMnemonic( '0' );
		positionAndOffset.setBounds( new Rectangle( 13, 150, 220, 26 ) );
		
		JLabel horizontal = new JLabel(Messages.getMessage("PdfViewerLabel.Horizontal"));
		horizontal.setBounds( new Rectangle( 20, 185, 90, 23 ) );
		
		horizontalBox.setBounds( new Rectangle(80, 185, 120, 23 ) );
		horizontalBox.setSelectedItem(Messages.getMessage("PdfViewerLabel.Centered"));
		
		JLabel vertical = new JLabel(Messages.getMessage("PdfViewerLabel.Vertical"));
		vertical.setBounds( new Rectangle( 20, 215, 90, 23 ) );
		
		verticalBox.setBounds( new Rectangle(80, 215, 120, 23 ) );
		verticalBox.setSelectedItem(Messages.getMessage("PdfViewerLabel.Centered"));
		
		JLabel hOffset = new JLabel(Messages.getMessage("PdfViewerLabel.Offset"));
		hOffset.setBounds( new Rectangle( 250, 185, 90, 23 ) );
		
		horizontalOffset.setBounds( new Rectangle(320, 185, 70, 23 ) );
		
		JLabel vOffset = new JLabel(Messages.getMessage("PdfViewerLabel.Offset"));
		vOffset.setBounds( new Rectangle( 250, 215, 90, 23 ) );
		
		verticalOffset.setBounds( new Rectangle(320, 215, 70, 23 ) );
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 250, 400, 26 ) );
		
		printAll.setText(Messages.getMessage("PdfViewerRadioButton.All"));
		printAll.setBounds( new Rectangle( 23, 280, 75, 22 ) );
		
		printCurrent.setText(Messages.getMessage("PdfViewerRadioButton.CurrentPage"));
		printCurrent.setBounds( new Rectangle( 23, 300, 100, 22 ) );
		printCurrent.setSelected(true);
		
		printPages.setText(Messages.getMessage("PdfViewerRadioButton.Pages"));
		printPages.setBounds( new Rectangle( 23, 322, 70, 22 ) );
		
		pagesBox.setBounds( new Rectangle( 95, 322, 230, 22 ) );
		pagesBox.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0) {}

			public void keyReleased(KeyEvent arg0) {
				if(pagesBox.getText().length() == 0)
					printCurrent.setSelected(true);
				else
					printPages.setSelected(true);
				
			}

			public void keyTyped(KeyEvent arg0) {}
		});

		JTextArea pagesInfo=new JTextArea(Messages.getMessage("PdfViewerMessage.PageNumberOrRange")+ '\n' +
				Messages.getMessage("PdfViewerMessage.PageRangeExample"));
		pagesInfo.setBounds(new Rectangle(23,355,400,40));
		pagesInfo.setOpaque(false);
				
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
		this.add( image, null );
		this.add( browse, null );
		this.add( imageBox, null );
		this.add( rotation, null );
		this.add( rotationBox, null );
		this.add( degrees, null );
		this.add( placement, null );
		this.add( placementBox, null );
		
		
		this.add( positionAndOffset, null );
		this.add( horizontal, null );
		this.add( horizontalBox, null );
		this.add( vertical, null );
		this.add( verticalBox, null );
		this.add( hOffset, null );
		this.add( horizontalOffset, null );
		this.add( vOffset, null );
		this.add( verticalOffset, null );
		
		this.add( wScale, null );
		this.add( widthScale, null );
		this.add( hScale, null );
		this.add( heightScale, null );
		
		this.add( textAndFont, null );
		this.add( changeButton, null );
		this.add( pageRangeLabel, null );
		
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		
		buttonGroup1.add( printAll );
		buttonGroup1.add( printCurrent );
		buttonGroup1.add( printPages );
	}
	
	final public Dimension getPreferredSize()
	{
		return new Dimension( 440, 400 );
	}
	
}
