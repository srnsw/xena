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
* CropPDFPages.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


import javax.print.attribute.standard.PageRanges;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

public class CropPDFPages extends Save
{

	JLabel OutputLabel = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	ButtonGroup buttonGroup2 = new ButtonGroup();
	
	JToggleButton jToggleButton3 = new JToggleButton();
	
	JToggleButton jToggleButton2 = new JToggleButton();
	
	JSpinner bottomMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	JSpinner topMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	JSpinner leftMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	JSpinner rightMargin = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 1000.00, 1.00));
	
	JCheckBox applyToCurrent = new JCheckBox();
	
	JRadioButton printAll=new JRadioButton();
	JRadioButton printCurrent=new JRadioButton();
	JRadioButton printPages=new JRadioButton();
	
	JTextField pagesBox=new JTextField();
	
	public CropPDFPages( String root_dir, int end_page, int currentPage ) 
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
	
	public boolean applyToCurrentCrop(){
		return applyToCurrent.isSelected();
	}
	
	public float[] getCrop(){
		float left = Float.parseFloat(leftMargin.getValue().toString());
		float bottom = Float.parseFloat(bottomMargin.getValue().toString());
		float right = Float.parseFloat(rightMargin.getValue().toString());
		float top = Float.parseFloat(topMargin.getValue().toString());
		
		return new float[]{left,bottom,right,top};
	}
	
	private void jbInit() throws Exception
	{
		
		JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerLabel.CropMargins"));
		textAndFont.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		textAndFont.setDisplayedMnemonic( '0' );
		textAndFont.setBounds( new Rectangle( 13, 13, 220, 26 ) );
		
        JLabel jLabel1 = new JLabel(Messages.getMessage("PdfViewerLabel.Top"));
        jLabel1.setBounds(140, 50, 70, 15);

        topMargin.setBounds(200, 45, 60, 23);

        JLabel jLabel5 = new javax.swing.JLabel(Messages.getMessage("PdfViewerLabel.Left"));
        jLabel5.setBounds(25, 100, 50, 15);

        leftMargin.setBounds(70, 95, 60, 23);
        
        JLabel jLabel6 = new javax.swing.JLabel(Messages.getMessage("PdfViewerLabel.Right"));
        jLabel6.setBounds(295, 100, 70, 15);

        rightMargin.setBounds(340, 95, 60, 23);

        JLabel jLabel7 = new javax.swing.JLabel(Messages.getMessage("PdfViewerLabel.Bottom"));
        jLabel7.setBounds(140, 150, 110, 15);
        
        bottomMargin.setBounds(200, 145, 60, 23);

        applyToCurrent.setSelected(true);
        applyToCurrent.setText(Messages.getMessage("PdfViewerCheckBox.ApplyToPriorCroppingRectangle"));
        applyToCurrent.setBounds(5, 190, 305, 15);

        JButton jButton1 = new javax.swing.JButton(Messages.getMessage("PdfViewerButton.Set2Zero"));
        jButton1.setBounds(310, 185, 130, 23);
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				leftMargin.setValue(new Integer(0));
				rightMargin.setValue(new Integer(0));
				topMargin.setValue(new Integer(0));
				bottomMargin.setValue(new Integer(0));
			}
		});
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 220, 199, 26 ) );
		
		printAll.setText(Messages.getMessage("PdfViewerRadioButton.All"));
		printAll.setBounds( new Rectangle( 23, 250, 75, 22 ) );
		
		printCurrent.setText(Messages.getMessage("PdfViewerRadioButton.CurrentPage"));
		printCurrent.setBounds( new Rectangle( 23, 270, 100, 22 ) );
		printCurrent.setSelected(true);
		
		printPages.setText(Messages.getMessage("PdfViewerRadioButton.Pages"));
		printPages.setBounds( new Rectangle( 23, 292, 70, 22 ) );
		
		pagesBox.setBounds( new Rectangle( 95, 292, 230, 22 ) );
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
		pagesInfo.setBounds(new Rectangle(23,325,400,40));
		pagesInfo.setOpaque(false);
				
		this.add(jLabel1);
        this.add(bottomMargin);
        this.add(jLabel5);
        this.add(topMargin);
        this.add(leftMargin);
        this.add(rightMargin);
        this.add(jLabel7);
        this.add(jLabel6);
        this.add(applyToCurrent);
        this.add(jButton1);		
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
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
