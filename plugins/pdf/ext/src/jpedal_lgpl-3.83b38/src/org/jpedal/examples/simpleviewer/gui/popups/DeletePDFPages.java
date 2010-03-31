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
* DeletePDFPages.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.print.attribute.standard.PageRanges;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

public class DeletePDFPages extends Save
{
	
	private static final long serialVersionUID = 7720446319401470639L;
	
	JLabel OutputLabel = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	ButtonGroup buttonGroup2 = new ButtonGroup();
	
	JToggleButton jToggleButton3 = new JToggleButton();
	
	JToggleButton jToggleButton2 = new JToggleButton();
	
	JRadioButton printAll=new JRadioButton();
	JRadioButton printCurrent=new JRadioButton();
	JRadioButton printPages=new JRadioButton();
	
	JTextField pagesBox=new JTextField();

	public DeletePDFPages( String root_dir, int end_page, int currentPage ) 
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
	final public int[] getDeletedPages()
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
						JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerText.Page")+
                                ' ' +i+ ' ' +Messages.getMessage("PdfViewerError.OutOfBounds")+ ' ' +
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
	
	private void jbInit() throws Exception
	{//58
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 13, 199, 26 ) );
		
		//printAll.setText(Messages.getMessage("PdfViewerRadioButton.All"));
		//printAll.setBounds( new Rectangle( 23, 42, 75, 22 ) );
		
		printCurrent.setText(Messages.getMessage("PdfViewerRadioButton.CurrentPage"));
		printCurrent.setBounds( new Rectangle( 23, 62, 100, 22 ) );
		printCurrent.setSelected(true);
		
		printPages.setText(Messages.getMessage("PdfViewerRadioButton.Pages"));
		printPages.setBounds( new Rectangle( 23, 84, 70, 22 ) );
		
		pagesBox.setBounds( new Rectangle( 95, 84, 200, 22 ) );
		pagesBox.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0) {
				jToggleButton2.setSelected(true);
				
			}

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
		pagesInfo.setBounds(new Rectangle(15,115,400,40));
		pagesInfo.setOpaque(false);
				
		optionsForFilesLabel.setBounds( new Rectangle( 13, 168, 199, 26 ) );
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
//		this.add( directionBox, null );
//		this.add( direction, null );
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
		return new Dimension( 370, 180 );
	}
	
}
