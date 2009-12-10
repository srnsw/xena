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
* SwingActionFactory.java
* ---------------
*/
package org.jpedal.objects.acroforms.actions;

import org.jpedal.PdfDecoder;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.acroforms.actions.privateclasses.FieldsHideObject;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.acroforms.gui.Summary;
import org.jpedal.objects.acroforms.overridingImplementations.FixImageIcon;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.acroforms.rendering.DefaultAcroRenderer;
import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.Map;

public class SwingActionFactory implements ActionFactory {

    AcroRenderer acrorend;

    PdfDecoder decode_pdf=null;

	public void showMessageDialog(String s) {
        JOptionPane.showMessageDialog(decode_pdf, s);

    }

    /**
     * pick up key press or return ' '
     */
    public char getKeyPressed(Object raw) {
    	
    	try{
        ComponentEvent ex=(ComponentEvent)raw;

        if (ex instanceof KeyEvent)
            return ((KeyEvent) ex).getKeyChar();
        else
            return ' ';
        
    	}catch(Exception ee){
    		System.out.println("Exception "+ee);
    	}
    	
    	return ' ';

    }

    /** 
     * shows and hides the appropriate fields as defined within the map defined
     * @param fieldsToHide - the field names to which we want to hide
     * @param whetherToHide - flags to show if we hide or show the respective field
     * both arrays must be the same length.
     */
    public void setFieldVisibility(FieldsHideObject fieldToHide) {
    	
    	String[] fieldsToHide = fieldToHide.getFieldArray();
    	boolean[] whetherToHide = fieldToHide.getHideArray();
    	
        if (fieldsToHide.length != whetherToHide.length) {
        	//this will exit internally only and the production version will carry on regardless.
            LogWriter.writeFormLog("{custommouselistener} number of fields and nuber of hides or not the same", FormStream.debugUnimplemented);
            return;
        }

        for (int i = 0; i < fieldsToHide.length; i++) {

            Component[] checkObj = (Component[]) acrorend.getComponentsByName(fieldsToHide[i]);
            if (checkObj != null) {
                for (int j = 0; j < checkObj.length; j++) {
                    checkObj[j].setVisible(!whetherToHide[i]);
                }
            }
        }
    }

    public void setPageandPossition(Object location) {


        //scroll to 'location'
		if (location != null)
			decode_pdf.scrollRectToVisible((Rectangle)location);
       
        decode_pdf.invalidate();
		decode_pdf.updateUI();
    }

    public void print() {
    	
        //ask if user ok with printing and print if yes
        if (JOptionPane.showConfirmDialog(decode_pdf, Messages.getMessage("PdfViewerPrinting.ConfirmPrint"),
                Messages.getMessage("PdfViewerPrint.Printing"), JOptionPane.YES_NO_OPTION) == 0) {

            //setup print job and objects
            PrinterJob printJob = PrinterJob.getPrinterJob();
            PageFormat pf = printJob.defaultPage();

            // Set PageOrientation to best use page layout
            int orientation = decode_pdf.getPDFWidth() < decode_pdf
                    .getPDFHeight() ? PageFormat.PORTRAIT
                    : PageFormat.LANDSCAPE;

            pf.setOrientation(orientation);

            Paper paper = new Paper();
            paper.setSize(595, 842);
            paper.setImageableArea(43, 43, 509, 756);

            pf.setPaper(paper);
            //          allow user to edit settings and select printing
            printJob.setPrintable(decode_pdf, pf);

            try {
                printJob.print();
            } catch (PrinterException e1) {
            }
        }
    }

    public void reset() {
    	acrorend.getCompData().reset();

        String[] defaultValues = acrorend.getCompData().getDefaultValues();
        Component[] allFields = (Component[]) acrorend.getComponentsByName(null);

        for (int i = 0; i < allFields.length; i++) {
            if (allFields[i] != null) {// && defaultValues[i]!=null){

                if (allFields[i] instanceof AbstractButton) {
                    if (allFields[i] instanceof JCheckBox) {
                        //setSelectedItem(item)
                        if (defaultValues[i] == null) {
                            ((JCheckBox) allFields[i]).setSelected(false);
                            //reset pressedimages so that they coinside
                            ((FixImageIcon)((JCheckBox) allFields[i]).getPressedIcon()).swapImage(false);
                        } else {
                            String fieldState = allFields[i].getName();
                            int ptr = fieldState.indexOf("-(");
                            /** NOTE if indexOf string changes change ptr+# to same length */
                            if (ptr != -1) {
                                fieldState = fieldState.substring(ptr + 2, fieldState.length() - 1);
                            }

                            if (fieldState.equals(defaultValues[i])){
                                ((JCheckBox) allFields[i]).setSelected(true);
                                //reset pressedimages so that they coinside
                                ((FixImageIcon)((JCheckBox) allFields[i]).getPressedIcon()).swapImage(true);
                            }else {
                                ((JCheckBox) allFields[i]).setSelected(false);
                                //reset pressedimages so that they coinside
                                ((FixImageIcon)((JCheckBox) allFields[i]).getPressedIcon()).swapImage(false);
                            }

                            LogWriter.writeFormLog("{renderer} resetform on mouse press " + allFields[i].getClass() + " - " + defaultValues[i] + " current=" + ((JCheckBox) allFields[i]).isSelected() + ' ' + ((JCheckBox) allFields[i]).getText(), FormStream.debugUnimplemented);
                        }

                    } else if (allFields[i] instanceof JRadioButton) {
                        //on/off
                        if (defaultValues[i] == null) {
                            ((JRadioButton) allFields[i]).setSelected(false);
                            //reset pressedimages so that they coinside
                            ((FixImageIcon)((JRadioButton) allFields[i]).getPressedIcon()).swapImage(false);
                        } else {
                            String fieldState = allFields[i].getName();

                            int ptr = fieldState.indexOf("-(");
                            /** NOTE if indexOf string changes change ptr+# to same length */
                            if (ptr != -1) {
                                fieldState = fieldState.substring(ptr + 2, fieldState.length() - 1);
                            }

                            if (fieldState.equals(defaultValues[i])){
                                ((JRadioButton) allFields[i]).setSelected(true);
                                //reset pressedimages so that they coinside
                                ((FixImageIcon)((JRadioButton) allFields[i]).getPressedIcon()).swapImage(true);
                            }else {
                                ((JRadioButton) allFields[i]).setSelected(false);
                                //reset pressedimages so that they coinside
                                ((FixImageIcon)((JRadioButton) allFields[i]).getPressedIcon()).swapImage(false);
                            }

                        }
                    }
                } else if (allFields[i] instanceof JTextComponent) {

                    String fieldName = allFields[i].getName();
                    acrorend.getCompData().setValue(fieldName, defaultValues[i], true, true,false);

                } else if (allFields[i] instanceof JComboBox) {
                    // on/off
                    ((JComboBox) allFields[i]).setSelectedItem(defaultValues[i]);

                } else if (allFields[i] instanceof JList) {
                    ((JList) allFields[i]).setSelectedValue(defaultValues[i], true);
                }
                allFields[i].repaint();

            }
        }
    }

    public void setPDF(PdfDecoder decode_pdf,AcroRenderer acrorend) {
        this.decode_pdf=decode_pdf;
        this.acrorend=acrorend;
    }

    public void setCursor(int eventType) {
    	
    	if(decode_pdf==null){
    		//do nothing
    	}else if (eventType == ActionHandler.MOUSEENTERED)
            decode_pdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        else if (eventType == ActionHandler.MOUSEEXITED)
            decode_pdf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void showSig(PdfObject sigObject) {
    	
        JDialog frame = new JDialog((JFrame) null, "Signature Properties", true);

        Summary summary = new Summary(frame, sigObject);
        summary.setValues(sigObject.getTextStreamValue(PdfDictionary.Name),
                sigObject.getTextStreamValue(PdfDictionary.Reason),
                sigObject.getTextStreamValue(PdfDictionary.M),
                sigObject.getTextStreamValue(PdfDictionary.Location));

        frame.getContentPane().add(summary);
        frame.setSize(550, 220);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** @param listOfFields - defines a list of fields to either include or exclude from the submit option,
     * Dependent on the <B>flag</b>, if is null all fields are submitted.
     * @param excludeList - if true then the listOfFields defines an exclude list, 
     * if false the list is an include list, if listOfFields is null then this field is ignored.
     * @param submitURL - the URL to submit to.
     */
    public void submitURL(String[] listOfFields, boolean excludeList, String submitURL) {

        if (submitURL != null) {
            Component[] compsToSubmit = new Component[0];
            String[] includeNameList = new String[0];
            if(listOfFields!=null){
                if (excludeList) {
                    //listOfFields defines an exclude list
                    try {
                        java.util.List tmplist = acrorend.getComponentNameList();
                        if (tmplist != null) {
                            for (int i = 0; i < listOfFields.length; i++) {
                                tmplist.remove(listOfFields[i]);
                            }
                        }
                    } catch (PdfException e1) {
                        LogWriter.writeFormLog("SwingFormFactory.setupMouseListener() get component name list exception", FormStream.debugUnimplemented);
                    }
                } else {
                    //fields is an include list
                    includeNameList = listOfFields;
                }

                Component[] compsToAdd, tmp;
                for (int i = 0; i < includeNameList.length; i++) {
                    compsToAdd = (Component[]) acrorend.getComponentsByName(includeNameList[i]);
                    
                    if(compsToAdd!=null){
	                    tmp = new Component[compsToSubmit.length + compsToAdd.length];
	                    if (compsToAdd.length > 1) {
	                        LogWriter.writeFormLog("(internal only) SubmitForm multipul components with same name", FormStream.debugUnimplemented);
	                    }
	                    for (int k = 0; i < tmp.length; k++) {
	                        if (k < compsToSubmit.length) {
	                            tmp[k] = compsToSubmit[k];
	                        } else if (k - compsToSubmit.length < compsToAdd.length) {
	                            tmp[k] = compsToAdd[k - compsToSubmit.length];
	                        }
	                    }
	                    compsToSubmit = tmp;
                    }
                }
            } else {
                compsToSubmit = (Component[]) acrorend.getComponentsByName(null);
            }


            String text = "";
            for (int i = 0; i < compsToSubmit.length; i++) {
                if (compsToSubmit[i] instanceof JTextComponent) {
                    text += ((JTextComponent) compsToSubmit[i]).getText();
                } else if (compsToSubmit[i] instanceof AbstractButton) {
                    text += ((AbstractButton) compsToSubmit[i]).getText();
                } else if(compsToSubmit[i] != null){
                    LogWriter.writeFormLog("(internal only) SubmitForm field form type not accounted for", FormStream.debugUnimplemented);
                }
            }

            try {
                BrowserLauncher.openURL(submitURL + "?en&q=" + text);
            } catch (IOException e1) {
                showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
                e1.printStackTrace();
            }
        }
    }

    public Object getHoverCursor() {
        return new MouseListener(){
            public void mouseEntered(MouseEvent e) {
                setCursor(ActionHandler.MOUSEENTERED);
            }

            public void mouseExited(MouseEvent e) {
                setCursor(ActionHandler.MOUSEEXITED);
            }

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }
        };
    }

    public void popup(Object raw, FormObject formObj, PdfObjectReader currentPdfFile) {
		if (((MouseEvent)raw).getClickCount() == 2) {
        	/**/

        	acrorend.getCompData().popup(formObj,currentPdfFile);
        	
        	//move focus so that the button does not flash
        	((JButton)((MouseEvent)raw).getSource()).setFocusable(false);
        }
    }

	public Object getChangingDownIconListener(Object downOff, Object downOn, int rotation) {
		return new SwingDownIconListener(downOff, downOn, rotation);
	}
}
