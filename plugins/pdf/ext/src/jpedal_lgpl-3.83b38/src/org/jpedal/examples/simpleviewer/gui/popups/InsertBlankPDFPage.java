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
* InsertBlankPDFPage.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import javax.swing.JToggleButton;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

public class InsertBlankPDFPage extends Save
{
	JLabel OutputLabel = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	//ButtonGroup buttonGroup2 = new ButtonGroup();
	
	JToggleButton jToggleButton3 = new JToggleButton();
	
	JToggleButton jToggleButton2 = new JToggleButton();
	
	JRadioButton addToEnd=new JRadioButton();
	JRadioButton addBeforePage=new JRadioButton();
	


	public InsertBlankPDFPage( String root_dir, int end_page, int currentPage ) 
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
	final public int getInsertBefore()
	{
		
		int page = -1;
		
		if(addBeforePage.isSelected()){
			try{
				page = Integer.parseInt( startPage.getText() );
			}catch( Exception e ){
				LogWriter.writeLog(Messages.getMessage("PdfViewerError.Exception")+ ' ' + e
				+ ' ' +Messages.getMessage("PdfViewerError.ExportError"));
				JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerError.InvalidSyntax"));
			}
			
			if(page < 1)
				JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerError.NegativePageValue"));
			
			if(page > end_page){
				JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerText.Page")+ ' ' +
						page+ ' ' +Messages.getMessage("PdfViewerError.OutOfBounds")+ ' ' +
						Messages.getMessage("PdfViewerText.PageCount")+ ' ' +end_page);
				
				page = -1;
			}
		}else
			page = -2;
		
		return page;

	}
	
	private void jbInit() throws Exception
	{
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerTitle.Location"));
		pageRangeLabel.setBounds( new Rectangle( 13, 13, 199, 26 ) );
		
		addToEnd.setText(Messages.getMessage("PdfViewerTitle.AddPageToEnd"));
		addToEnd.setBounds( new Rectangle( 23, 42, 400, 22 ) );
		addToEnd.setSelected(true);
		
		addBeforePage.setText(Messages.getMessage("PdfViewerTitle.InsertBeforePage"));
		addBeforePage.setBounds( new Rectangle( 23, 70, 150, 22 ) );
		
		startPage.setBounds( new Rectangle( 175, 70, 75, 22 ) );
		startPage.setText("");
		startPage.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0) {}

			public void keyReleased(KeyEvent arg0) {
				if(startPage.getText().length() == 0)
					addToEnd.setSelected(true);
				else
					addBeforePage.setSelected(true);
				
			}

			public void keyTyped(KeyEvent arg0) {}
		});
		
		this.add( changeButton, null );
		this.add( pageRangeLabel, null );
		
		this.add( addToEnd, null );
		this.add( addBeforePage, null );
		
		this.add( startPage, null );
		
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		
		
		buttonGroup1.add( addToEnd );
		buttonGroup1.add( addBeforePage );

		
	}
	
	final public Dimension getPreferredSize()
	{
		return new Dimension( 350, 180 );
	}
	
}
