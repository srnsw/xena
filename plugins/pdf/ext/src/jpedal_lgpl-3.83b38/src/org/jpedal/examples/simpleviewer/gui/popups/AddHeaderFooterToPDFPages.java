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
* AddHeaderFooterToPDFPages.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


import javax.print.attribute.standard.PageRanges;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
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

public class AddHeaderFooterToPDFPages extends Save
{

	private static final long serialVersionUID = -8681143216306570454L;
	
	JLabel OutputLabel = new JLabel();
	ButtonGroup buttonGroup1 = new ButtonGroup();
	ButtonGroup buttonGroup2 = new ButtonGroup();
	
	JToggleButton jToggleButton3 = new JToggleButton();
	
	JToggleButton jToggleButton2 = new JToggleButton();
	
	JRadioButton printAll=new JRadioButton();
	JRadioButton printCurrent=new JRadioButton();
	JRadioButton printPages=new JRadioButton();
	
	JTextField pagesBox=new JTextField();
	
	JTextField leftHeaderBox=new JTextField();
	JTextField centerHeaderBox=new JTextField();
	JTextField rightHeaderBox=new JTextField();
	JTextField leftFooterBox=new JTextField();
	JTextField centerFooterBox=new JTextField();
	JTextField rightFooterBox=new JTextField();
	
	JComboBox fontsList = new JComboBox(new String[] { "Courier",
			"Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
			"Helvetica", "Helvetica-Bold", "Helvetica-BoldOblique",
			"Helvetica-Oblique", "Times-Roman", "Times-Bold", "Times-Italic",
			"Times-BoldItalic", "Symbol", "ZapfDingbats"
	});
	
	JSpinner fontSize = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
	
	JLabel colorBox = new JLabel();
	
	JSpinner leftRightBox = new JSpinner(new SpinnerNumberModel(36.00, 1.00, 1000.00, 1));
	JSpinner topBottomBox = new JSpinner(new SpinnerNumberModel(36.00, 1.00, 1000.00, 1));
	
	JTextArea tagsList = new JTextArea();
	
	
	
	public AddHeaderFooterToPDFPages( String root_dir, int end_page, int currentPage ) 
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
				LogWriter.writeLog(Messages.getMessage("PdfViewerError.Exception")
						+ ' ' + e + ' ' +Messages.getMessage("PdfViewerError.ExportPdfError"));
				JOptionPane.showMessageDialog(this,Messages.getMessage("PdfViewerError.InvalidSyntax"));
			}
		}
		
		return pagesToExport;

	}	
	
	public float getLeftRightMargin(){
		return Float.parseFloat(leftRightBox.getValue().toString());
	}
	
	public float getTopBottomMargin(){
		return Float.parseFloat(topBottomBox.getValue().toString());
	}
	
	public String getFontName(){
		return (String) fontsList.getSelectedItem();
	}
	
	public int getFontSize(){
		return Integer.parseInt(fontSize.getValue().toString());
	}
	
	public Color getFontColor(){
		return colorBox.getBackground();
	}

	
	public String getLeftHeader(){
		return leftHeaderBox.getText();
	}
	
	public String getCenterHeader(){
		return centerHeaderBox.getText();
	}
	
	public String getRightHeader(){
		return rightHeaderBox.getText();
	}
	
	public String getLeftFooter(){
		return leftFooterBox.getText();
	}
	
	public String getCenterFooter(){
		return centerFooterBox.getText();
	}
	
	public String getRightFooter(){
		return rightFooterBox.getText();
	}
	
	private void jbInit() throws Exception
	{
		
		JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerLabel.TextAndFont"));
		textAndFont.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		textAndFont.setDisplayedMnemonic( '0' );
		textAndFont.setBounds( new Rectangle( 13, 13, 220, 26 ) );
		
		JLabel left = new JLabel(Messages.getMessage("PdfViewerLabel.Left"));
		left.setBounds( new Rectangle( 130, 40, 50, 23 ) );
		
		JLabel center = new JLabel(Messages.getMessage("PdfViewerLabel.Center"));
		center.setBounds( new Rectangle( 300, 40, 50, 23 ) );
		
		JLabel right = new JLabel(Messages.getMessage("PdfViewerLabel.Right"));
		right.setBounds( new Rectangle( 475, 40, 50, 23 ) );
		
		JLabel header = new JLabel(Messages.getMessage("PdfViewerLabel.Header"));
		header.setBounds( new Rectangle( 20, 60, 90, 23 ) );
		
		JLabel footer = new JLabel(Messages.getMessage("PdfViewerLabel.Footer"));
		footer.setBounds( new Rectangle( 20, 90, 50, 23 ) );
		
		leftHeaderBox.setBounds( new Rectangle(85, 60, 133, 23 ) );
		centerHeaderBox.setBounds( new Rectangle(250, 60, 133, 23 ) );
		rightHeaderBox.setBounds( new Rectangle(425, 60, 133, 23 ) );
		
		leftFooterBox.setBounds( new Rectangle(85, 90, 133, 23 ) );
		centerFooterBox.setBounds( new Rectangle(250, 90, 133, 23 ) );
		rightFooterBox.setBounds( new Rectangle(425, 90, 133, 23 ) );

		JLabel font = new JLabel(Messages.getMessage("PdfViewerLabel.Font"));
		font.setBounds( new Rectangle( 20, 120, 75, 23 ) );
		
		fontsList.setBounds( new Rectangle( 85, 120, 150, 23 ) );
		fontsList.setSelectedItem("Helvetica");
		
		JLabel size = new JLabel(Messages.getMessage("PdfViewerLabel.Size"));
		size.setBounds( new Rectangle( 250, 120, 50, 23 ) );
		
		fontSize.setBounds( new Rectangle( 290, 120, 50, 23 ) );
		
		JLabel color = new JLabel(Messages.getMessage("PdfViewerLabel.Color"));
		color.setBounds( new Rectangle( 360, 120, 50, 23 ) );
		
		colorBox.setBackground(Color.black);
		colorBox.setOpaque(true);
		colorBox.setBounds( new Rectangle( 410, 120, 23, 23 ) );
		
		JButton chooseColor = new JButton(Messages.getMessage("PdfViewerButton.ChooseColor"));
		chooseColor.setBounds( new Rectangle( 450, 120, 160, 23 ) );
		chooseColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorBox.setBackground(JColorChooser.showDialog(null, "Color",colorBox.getBackground()));
			}
		});
		
        tagsList.setText("You may use the following\n" +
        		"tags as part of the text.\n\n" +
        		"<d> - Date in short format\n" +
        		"<D> - Date in long format\n" +
        		"<t> - Time in 12-hour format\n" +
        		"<T> - Time in 24-hour format\n" +
        		"<f> - Filename\n" +
        		"<F> - Full path filename\n" +
        		"<p> - Current page number\n" +
        		"<P> - Total number of pages");
        tagsList.setOpaque(false);
        tagsList.setBounds(350, 160, 200, 210);

		JLabel margins = new JLabel(Messages.getMessage("PdfViewerLabel.Margins"));

		margins.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		margins.setDisplayedMnemonic( '0' );
		margins.setBounds( new Rectangle( 13, 150, 220, 26 ) );
		
		JLabel leftRight = new JLabel(Messages.getMessage("PdfViewerLabel.LeftAndRight"));
		leftRight.setBounds( new Rectangle( 20, 185, 90, 23 ) );
		
		leftRightBox.setBounds( new Rectangle(100, 185, 70, 23 ) );
		
		JLabel topBottom = new JLabel(Messages.getMessage("PdfViewerLabel.TopAndBottom"));
		topBottom.setBounds( new Rectangle( 180, 185, 120, 23 ) );
		
		topBottomBox.setBounds( new Rectangle(300, 185, 70, 23 ) );
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 220, 400, 26 ) );
		
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

		JTextArea pagesInfo=new JTextArea(Messages.getMessage("PdfViewerMessage.PageNumberOrRangeLong"));
		pagesInfo.setBounds(new Rectangle(23,320,620,40));
		pagesInfo.setOpaque(false);
				
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
		this.add( left, null );
		this.add( center, null );
		this.add( right, null );
		this.add( header, null );
		this.add( footer, null );
		this.add( leftHeaderBox, null );
		this.add( centerHeaderBox, null );
		this.add( rightHeaderBox, null );
		this.add( leftFooterBox, null );
		this.add( centerFooterBox, null );
		this.add( rightFooterBox, null );
		this.add( font, null );
		this.add( fontsList, null );
		this.add( size, null );
		this.add( fontSize, null );
		this.add( color, null );
		this.add( colorBox, null );
		this.add( chooseColor, null );
		this.add( margins, null );
		this.add( leftRight, null );
		this.add( leftRightBox, null );
		this.add( topBottom, null );
		this.add( topBottomBox, null );
		
		this.add( textAndFont, null );
		this.add( changeButton, null );
		this.add( pageRangeLabel, null );
		
		//this.add(tagsList, null);
		
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		
		buttonGroup1.add( printAll );
		buttonGroup1.add( printCurrent );
		buttonGroup1.add( printPages );
	}
	
	final public Dimension getPreferredSize()
	{
		return new Dimension( 620, 350 );
	}
	
}
