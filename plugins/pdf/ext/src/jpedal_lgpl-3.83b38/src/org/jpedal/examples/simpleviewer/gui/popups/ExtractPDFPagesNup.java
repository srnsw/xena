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
* ExtractPDFPagesNup.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.popups;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.print.attribute.standard.PageRanges;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.jpedal.examples.simpleviewer.utils.ItextFunctions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

public class ExtractPDFPagesNup extends Save
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
	
    ArrayList papers;
    ArrayList paperDimensions;
    
	private javax.swing.JSpinner horizontalSpacing;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSpinner layoutColumns;
    private javax.swing.JSpinner layoutRows;
    private javax.swing.JComboBox layouts;
    private javax.swing.JSpinner leftRightMargins;
    private javax.swing.JSpinner scaleHeight;
    private javax.swing.JCheckBox pageProportionally;
    private javax.swing.JComboBox pageScalings;
    private javax.swing.JSpinner scaleWidth;
    private javax.swing.JSpinner paperHeight;
    private javax.swing.JComboBox paperOrientation;
    private javax.swing.JComboBox paperSizes;
    private javax.swing.JSpinner paperWidth;
    private javax.swing.JSpinner topBottomMargins;
    private javax.swing.JSpinner verticalSpacing;
    
	private JComboBox repeat = new JComboBox();
	private JSpinner copies = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
	private JComboBox ordering = new JComboBox();
	private JComboBox doubleSided = new JComboBox();
	
	public ExtractPDFPagesNup( String root_dir, int end_page, int currentPage ) 
	{
		super(root_dir, end_page, currentPage);
			
		genertatePaperSizes();
		
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
                                +i+ ' ' +Messages.getMessage("PdfViewerError.OutOfBounds")+ ' ' +
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
	
	public float getHorizontalSpacing(){
		return Float.parseFloat(horizontalSpacing.getValue().toString());
	}
	public float getVerticalSpacing(){
		return Float.parseFloat(verticalSpacing.getValue().toString());
	}
	public float getLeftRightMargin(){
		return Float.parseFloat(leftRightMargins.getValue().toString());
	}
	public float getTopBottomMargin(){
		return Float.parseFloat(topBottomMargins.getValue().toString());
	}
	
	public int getPaperWidth(){
		return Integer.parseInt(paperWidth.getValue().toString());
	}
	
	public int getPaperHeight(){
		return Integer.parseInt(paperHeight.getValue().toString());
	}
	
	public String getPaperOrientation(){
		return (String) paperOrientation.getSelectedItem();
	}
	
	public String getScale(){
		return (String) pageScalings.getSelectedItem();
	}
	
	public boolean isScaleProportional(){
		return pageProportionally.isSelected();
	}
	
	public float getScaleWidth(){
		return Float.parseFloat(scaleWidth.getValue().toString());
	}
	
	public float getScaleHeight(){
		return Float.parseFloat(scaleHeight.getValue().toString());
	}
	
	public String getSelectedLayout(){
		return (String) layouts.getSelectedItem();
	}
	
	public int getLayoutRows(){
		return Integer.parseInt(layoutRows.getValue().toString());
	}
	
	public int getLayoutColumns(){
		return Integer.parseInt(layoutColumns.getValue().toString());
	}
	
	public int getRepeat(){
		if(repeat.getSelectedIndex() == 0)
			return ItextFunctions.REPEAT_NONE;
		
		if(repeat.getSelectedIndex() == 1)
			return ItextFunctions.REPEAT_AUTO;
		
		return ItextFunctions.REPEAT_SPECIFIED;
	}
	
	public int getCopies(){
		return Integer.parseInt(copies.getValue().toString());
	}
	
	public int getPageOrdering(){
		if(ordering.getSelectedIndex() == 0)
			return ItextFunctions.ORDER_ACCROS;
		
		if(ordering.getSelectedIndex() == 1)
			return ItextFunctions.ORDER_DOWN;
		
		return ItextFunctions.ORDER_STACK;
	}
	
	public String getDoubleSided(){
		return (String) doubleSided.getSelectedItem();
	}
	
	private void jbInit() throws Exception
	{
		rootFilesLabel.setBounds( new Rectangle( 13, 13, 400, 26 ) );
		rootDir.setBounds( new Rectangle( 20, 40, 232, 23 ) );
		changeButton.setBounds( new Rectangle( 272, 40, 101, 23 ) );
		
		JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerNUPLabel.PaperSize"));
		textAndFont.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		textAndFont.setDisplayedMnemonic( '0' );
		textAndFont.setBounds( new Rectangle( 13, 70, 220, 26 ) );
		
		JLabel scale = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Scale"));
		scale.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		scale.setDisplayedMnemonic( '0' );
		scale.setBounds( new Rectangle( 13, 140, 220, 26 ) );
		
		JLabel layout = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Layout"));
		layout.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		layout.setDisplayedMnemonic( '0' );
		layout.setBounds( new Rectangle( 13, 210, 220, 26 ) );
		
		JLabel margins = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Margins"));
		margins.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		margins.setDisplayedMnemonic( '0' );
		margins.setBounds( new Rectangle( 13, 280, 220, 26 ) );
		
		JLabel pageSettings = new JLabel(Messages.getMessage("PdfViewerNUPLabel.PageSettings"));
		pageSettings.setFont( new java.awt.Font( "Dialog", 1, 14 ) );
		pageSettings.setDisplayedMnemonic( '0' );
		pageSettings.setBounds( new Rectangle( 13, 400, 220, 26 ) );
		
		layouts = new javax.swing.JComboBox();
		paperOrientation = new javax.swing.JComboBox();
		pageScalings = new javax.swing.JComboBox();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		topBottomMargins = new javax.swing.JSpinner(new SpinnerNumberModel(18.00, -720.00, 720.00, 1.00));
		leftRightMargins = new javax.swing.JSpinner(new SpinnerNumberModel(18.00, -720.00, 720.00, 1.00));
		pageProportionally = new javax.swing.JCheckBox();
		paperSizes = new javax.swing.JComboBox();
		jLabel11 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		paperWidth = new javax.swing.JSpinner();
		paperHeight = new javax.swing.JSpinner();
		scaleWidth = new javax.swing.JSpinner(new SpinnerNumberModel(396.00, 72.00, 5184.00, 1.00));
		scaleHeight = new javax.swing.JSpinner(new SpinnerNumberModel(612.00, 72.00, 5184.00, 1.00));
		jLabel12 = new javax.swing.JLabel();
		jLabel13 = new javax.swing.JLabel();
		layoutRows = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		layoutColumns = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
		jLabel14 = new javax.swing.JLabel();
		verticalSpacing = new javax.swing.JSpinner(new SpinnerNumberModel(7.20, 0.00, 720.00, 1.00));
		horizontalSpacing = new javax.swing.JSpinner(new SpinnerNumberModel(7.20, 0.00, 720.00, 1.00));
		jLabel16 = new javax.swing.JLabel();
		jLabel15 = new javax.swing.JLabel();
		jLabel17 = new javax.swing.JLabel();
		
		layouts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2 Up", "4 Up", "8 Up", Messages.getMessage("PdfViewerNUPOption.Custom")}));
		layouts.setSelectedIndex(0);
		layouts.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				layoutsSelectionChanged(evt);
			}
		});
		
		copies.setEnabled(false);
		
		repeat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { Messages.getMessage("PdfViewerNUPOption.None"), Messages.getMessage("PdfViewerNUPOption.Auto"), Messages.getMessage("PdfViewerNUPOption.Specified")}));
		repeat.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if(repeat.getSelectedItem().equals("None")){
					copies.getModel().setValue(new Integer(1));
					copies.setEnabled(false);
				}else if(repeat.getSelectedItem().equals("Auto")){
					int rows = Integer.parseInt(layoutRows.getValue().toString());
					int coloumns = Integer.parseInt(layoutColumns.getValue().toString());
					
					copies.getModel().setValue(new Integer(rows * coloumns));
					copies.setEnabled(false);
				}else if(repeat.getSelectedItem().equals("Specified")){
					copies.setEnabled(true);
				}
			}
		});
		
		ordering.setModel(new javax.swing.DefaultComboBoxModel(new String[] { Messages.getMessage("PdfViewerNUPOption.Across"), Messages.getMessage("PdfViewerNUPOption.Down") }));
		
		
		doubleSided.setModel(new javax.swing.DefaultComboBoxModel(new String[] { Messages.getMessage("PdfViewerNUPOption.None"), Messages.getMessage("PdfViewerNUPOption.Front&Back"), Messages.getMessage("PdfViewerNUPOption.Gutter")}));
		
		layouts.setBounds(20, 240, 110, 23);
		
		paperOrientation.setModel(new javax.swing.DefaultComboBoxModel(new String[] { Messages.getMessage("PdfViewerNUPOption.Auto"), Messages.getMessage("PdfViewerNUPOption.Portrait"), Messages.getMessage("PdfViewerNUPOption.Landscape") }));
		paperOrientation.setBounds(510, 100, 90, 23);
		
		pageScalings.setModel(new javax.swing.DefaultComboBoxModel(new String[] { Messages.getMessage("PdfViewerNUPOption.OriginalSize"), Messages.getMessage("PdfViewerNUPOption.Auto"), Messages.getMessage("PdfViewerNUPOption.Specified") }));
		pageScalings.setSelectedIndex(1);
		pageScalings.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				scalingSelectionChanged(evt);
			}
		});
		
		pageScalings.setBounds(20, 170, 200, 23);
		
		jLabel1.setText(Messages.getMessage("PdfViewerNUPLabel.Width"));
		jLabel1.setBounds(148, 100, 50, 15);
		
		jLabel2.setText(Messages.getMessage("PdfViewerNUPLabel.Height"));
		jLabel2.setBounds(278, 100, 50, 15);
		
		pageProportionally.setSelected(true);
		pageProportionally.setText(Messages.getMessage("PdfViewerNUPText.Proportionally"));
		pageProportionally.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		pageProportionally.setMargin(new java.awt.Insets(0, 0, 0, 0));
		pageProportionally.setBounds(240, 170, 120, 15);
		
		paperSizes.setModel(new javax.swing.DefaultComboBoxModel(getPaperSizes()));
		paperSizes.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				pageSelectionChanged(evt);
			}
		});
		
		paperSizes.setBounds(20, 100, 110, 23);
		
		jLabel11.setText(Messages.getMessage("PdfViewerNUPLabel.Orientation"));
		jLabel11.setBounds(408, 100, 130, 15);
		
		
		jLabel3.setText(Messages.getMessage("PdfViewerNUPLabel.Width"));
		jLabel3.setBounds(370, 170, 50, 15);
		
		
		jLabel4.setText(Messages.getMessage("PdfViewerNUPLabel.Height"));
		jLabel4.setBounds(500, 170, 50, 15);
		
		paperWidth.setEnabled(false);
		paperWidth.setBounds(195, 100, 70, 23);
		
		paperHeight.setEnabled(false);
		paperHeight.setBounds(318, 100, 70, 23);
		
		scaleWidth.setEnabled(false);
		scaleWidth.setBounds(420, 170, 70, 23);
		
		scaleHeight.setEnabled(false);
		scaleHeight.setBounds(540, 170, 70, 23);
		
		jLabel12.setText(Messages.getMessage("PdfViewerNUPLabel.Rows"));
		jLabel12.setBounds(148, 240, 50, 15);
		
		jLabel13.setText(Messages.getMessage("PdfViewerNUPLabel.Columns"));
		jLabel13.setBounds(278, 240, 50, 15);
		
		layoutRows.setEnabled(false);
		layoutRows.setBounds(195, 240, 70, 23);
		
		layoutColumns.setEnabled(false);
		layoutColumns.setBounds(328, 240, 70, 23);
		
		jLabel14.setText(Messages.getMessage("PdfViewerNUPLabel.Left&RightMargins"));
		jLabel14.setBounds(22, 326, 200, 15);
		leftRightMargins.setBounds(210, 322, 70, 23);
		
		jLabel16.setText(Messages.getMessage("PdfViewerNUPLabel.HorizontalSpacing"));
		jLabel16.setBounds(22, 356, 180, 15);
		horizontalSpacing.setBounds(210, 354, 70, 23);
		
		jLabel15.setText(Messages.getMessage("PdfViewerNUPLabel.Top&BottomMargins"));
		jLabel15.setBounds(300, 326, 180, 15);
		topBottomMargins.setBounds(480, 320, 70, 23);
		
		jLabel17.setText(Messages.getMessage("PdfViewerNUPLabel.VerticalSpacing"));
		jLabel17.setBounds(300, 356, 180, 15);
		verticalSpacing.setBounds(480, 354, 70, 23);
				
		JLabel jLabel18 = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Repeat"));
		jLabel18.setBounds(22, 446, 130, 15);
		repeat.setBounds(140, 442, 100, 23);
		
		JLabel jLabel20= new JLabel(Messages.getMessage("PdfViewerNUPLabel.Copies"));
		jLabel20.setBounds(300, 446, 130, 15);
		ordering.setBounds(140, 474, 130, 23);
		
		JLabel jLabel19= new JLabel(Messages.getMessage("PdfViewerNUPLabel.PageOrdering"));
		jLabel19.setBounds(22, 474, 130, 15);
		copies.setBounds(420, 440, 70, 23);
		
		JLabel jLabel21= new JLabel(Messages.getMessage("PdfViewerNUPLabel.DoubleSided"));
		jLabel21.setBounds(300, 476, 130, 15);
		doubleSided.setBounds(420, 474, 100, 23);		
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerNUPLabel.PageRange"));
		pageRangeLabel.setBounds( new Rectangle( 13, 530, 199, 26 ) );
		
		printAll.setText(Messages.getMessage("PdfViewerNUPOption.All"));
		printAll.setBounds( new Rectangle( 23, 560, 75, 22 ) );
		printAll.setSelected(true);
		
		printCurrent.setText(Messages.getMessage("PdfViewerNUPOption.CurrentPage"));
		printCurrent.setBounds( new Rectangle( 23, 580, 100, 22 ) );
		//printCurrent.setSelected(true);
		
		printPages.setText(Messages.getMessage("PdfViewerNUPOption.Pages"));
		printPages.setBounds( new Rectangle( 23, 600, 70, 22 ) );
		
		pagesBox.setBounds( new Rectangle( 95, 602, 230, 22 ) );
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
		pagesInfo.setBounds(new Rectangle(23,640,600,40));
		pagesInfo.setOpaque(false);
		
		pageSelectionChanged(null);
		
		this.add( rootDir, null );
		this.add( rootFilesLabel, null );
		this.add( changeButton, null );
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add(scale);
		this.add(layout);
		this.add(margins);
		
		this.add(layoutColumns);
		this.add(layoutRows);
		this.add(layouts);
		this.add(leftRightMargins);
		this.add(scaleHeight);
		this.add(pageProportionally);
		this.add(pageScalings);
		this.add(scaleWidth);
		this.add(paperHeight);
		this.add(paperOrientation);
		this.add(paperSizes);
		this.add(paperWidth);
		this.add(topBottomMargins);
		this.add(verticalSpacing);
		
		this.add(horizontalSpacing);
		this.add(jLabel1);
		this.add(jLabel2);
		this.add(jLabel3);
		this.add(jLabel4);
		this.add(jLabel11);
		this.add(jLabel12);
		this.add(jLabel13);
		this.add(jLabel14);
		this.add(jLabel15);
		this.add(jLabel16);
		this.add(jLabel17);

		this.add(pageSettings);
		this.add(jLabel18);
		this.add(repeat);
		this.add(jLabel19);
		this.add(copies);
		this.add(jLabel20);
		this.add(ordering);
		//this.add(jLabel21);
		//this.add(doubleSided);
		
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
	
    private void layoutsSelectionChanged(java.awt.event.ItemEvent evt) {
        String layout = (String)layouts.getSelectedItem();
        
        if(layout.equals("2 Up")){
        	layoutRows.getModel().setValue(new Integer(1));
        	layoutColumns.getModel().setValue(new Integer(2));
        	
        	layoutRows.setEnabled(false);
        	layoutColumns.setEnabled(false);
        }else if(layout.equals("4 Up")){
        	layoutRows.getModel().setValue(new Integer(2));
        	layoutColumns.getModel().setValue(new Integer(2));
        	
        	layoutRows.setEnabled(false);
        	layoutColumns.setEnabled(false);

        }else if(layout.equals("8 Up")){
        	layoutRows.getModel().setValue(new Integer(2));
        	layoutColumns.getModel().setValue(new Integer(4));
        	
        	layoutRows.setEnabled(false);
        	layoutColumns.setEnabled(false);

        }else if(layout.equals("Custom")){
            layoutRows.setEnabled(true);
            layoutColumns.setEnabled(true);
        }
    }

    private void scalingSelectionChanged(java.awt.event.ItemEvent evt) {
        String scaling = (String)pageScalings.getSelectedItem();
        
        if(scaling.equals("Use Original Size")){
            pageProportionally.setEnabled(false);
            scaleWidth.setEnabled(false);
            scaleHeight.setEnabled(false);
        }else if(scaling.equals("Auto")){
            pageProportionally.setEnabled(true);
            scaleWidth.setEnabled(false);
            scaleHeight.setEnabled(false);
        }else if(scaling.equals("Specified")){
            pageProportionally.setEnabled(true);
            scaleWidth.setEnabled(true);
            scaleHeight.setEnabled(true);
        }
    }

    private void pageSelectionChanged(java.awt.event.ItemEvent evt) {
        
        Dimension d = getPaperDimension((String)paperSizes.getSelectedItem());
        
        if(d == null){
            paperWidth.setEnabled(true);
            paperHeight.setEnabled(true);
        }else{
            paperWidth.setEnabled(false);
            paperHeight.setEnabled(false);
            
            paperWidth.setValue(new Integer(d.width));
            paperHeight.setValue(new Integer(d.height));
        }
    }
    
    private void genertatePaperSizes(){
    	papers = new ArrayList();
    	paperDimensions = new ArrayList();
    	
    	papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Letter"));
    	papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Legal"));
    	papers.add("11x17");
    	papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Ledger"));
    	papers.add("A2");
    	papers.add("A3");
    	papers.add("A4");
    	papers.add("A5");
    	papers.add("B3");
    	papers.add("B4");
    	papers.add("B5");
    	papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Folio"));
    	papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Status"));
    	papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Note"));
    	papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Custom"));
    	
    	paperDimensions.add(new Dimension(612,792));
    	paperDimensions.add(new Dimension(612,1008));
    	paperDimensions.add(new Dimension(792,1224));
    	paperDimensions.add(new Dimension(1224,792));
    	paperDimensions.add(new Dimension(1190,1684));
    	paperDimensions.add(new Dimension(842,1190));
    	paperDimensions.add(new Dimension(595,842));
    	paperDimensions.add(new Dimension(421,595));
    	paperDimensions.add(new Dimension(1002,1418));
    	paperDimensions.add(new Dimension(709,1002));
    	paperDimensions.add(new Dimension(501,709));
    	paperDimensions.add(new Dimension(612,936));
    	paperDimensions.add(new Dimension(396,612));
    	paperDimensions.add(new Dimension(540,720));
    	
        //paperSizesMap.put("Custom",null);
    }
    
    private String[] getPaperSizes(){
        return (String[]) papers.toArray(new String[papers.size()]);
    }
    
    private Dimension getPaperDimension(String paper){
    	if(paper.equals("Custom"))
    		return null;
    	
    	return (Dimension) paperDimensions.get(papers.indexOf(paper));
    }
	
	final public Dimension getPreferredSize()
	{
		return new Dimension( 620, 680 );
	}
	
}
