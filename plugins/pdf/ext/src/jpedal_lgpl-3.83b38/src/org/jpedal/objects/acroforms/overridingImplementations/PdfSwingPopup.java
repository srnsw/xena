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
* PdfSwingPopup.java
* ---------------
*/
package org.jpedal.objects.acroforms.overridingImplementations;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import javax.swing.*;

import java.awt.*;
import java.util.Date;

/**
 * provide PDF poup for Annotations
 */
public class PdfSwingPopup extends JPanel{
    /**
	 * 
	 */
	private static final long serialVersionUID = 796302916236391896L;
	
	public PdfSwingPopup(FormObject formObj, PdfObject popupObj) {
		super();
		
		StringBuffer date = new StringBuffer(formObj.getTextStreamValue(PdfDictionary.M));
		date.delete(0, 2);//delete D:
		date.insert(10, ':');
		date.insert(13, ':');
		date.insert(16, ' ');
		
		String year = date.substring(0, 4);
		String day = date.substring(6,8);
		date.delete(6,8);
		date.delete(0, 4);
		date.insert(0, day);
		date.insert(4, year);
		date.insert(2, '/');
		date.insert(5, '/');
		date.insert(10, ' ');
		
//		date.delete(19, date.length());//delete the +01'00' Time zone definition
		
		String Subject = formObj.getTextStreamValue(PdfDictionary.Subj);
		String popupTitle = formObj.getTextStreamValue(PdfDictionary.T);
		if(popupTitle==null)
			popupTitle = "";
		
        
		setLayout(new BorderLayout());
		
		float[] col = formObj.getFloatArray(PdfDictionary.C);
		Color bgColor = new Color(col[0],col[1],col[2]);
		setBorder(BorderFactory.createLineBorder(bgColor));
		
		JTextArea titleBar = new JTextArea(Subject+"\t"+date.toString()+"\n"+popupTitle);
		titleBar.setEditable(false);
		titleBar.setBackground(bgColor);
//		titleBar.setForeground(new Color(1f,1f,1f));//TODO find a color in popup which is white.
		add(titleBar,BorderLayout.NORTH);
		
		//main body text on contents is always a text readable form of the form or the content of the popup window.
		String contentString = formObj.getTextStreamValue(PdfDictionary.Contents);
		if(contentString==null)
			contentString = "";
		JTextArea contentArea = new JTextArea(contentString);
//		contentArea.setWrapStyleWord(true);
		add(contentArea,BorderLayout.CENTER);
		
    }
	
	/*
unsorted1\1414f3_file.pdf
baseline_screens\customers1\2007-09-17_PLUMBING_SHOP_DWGS-_PART_1.pdf
baseline_screens\extra\todd\B05016_12_TEST.pdf
baseline_screens\extra\todd\B07037_55_TEST.pdf
baseline_screens\extra\todd\B11115_99_Test.pdf
baseline_screens\extra\todd\B15002_71_TEST.pdf
baseline_screens\extra\todd\B15040_99_TEST.pdf
baseline_screens\extra\todd\B16006_02_TEST.pdf
baseline_screens\extra\todd\B16052_01_TEST.pdf
baseline_screens\extra\todd\B16099_07_TEST.pdf
baseline_screens\extra\todd\B17079_01_TEST.pdf
baseline_screens\extra\todd\B20009_03_TEST.pdf
baseline_screens\extra\todd\B25025_01_TEST.pdf
baseline_screens\extra\todd\B35023_02_test.pdf
baseline_screens\customers1\Binder1.pdf
baseline_screens\debug1\DET.0000000159.03172005-S1.pdf
baseline_screens\debug1\DrWang.pdf
baseline_screens\customers1\example_cern.pdf
pdftestfiles\rogsFiles\FelderTest.pdf
baseline_screens\adobe\form2-1.pdf
baseline_screens\abacus\Kreditorenbelege.pdf
baseline_screens\abacus\Kreditorenbelege2.pdf
baseline_screens\forms\multiplerevisions.pdf
baseline_screens\abacus\multiplerevisions2.pdf
C:\Documents and Settings\chris\My Documents\idrsolutions\tasks\Plumbing_Fixtures.pdf
baseline_screens\docusign\Problem4.pdf
baseline_screens\extra\bayer\Reportt0.pdf
baseline_screens\acroforms\smart-mortgageapp.pdf
baseline_screens\acroforms\smart-mortgageapp_signed.pdf
baseline_screens\acroforms\smart-mortgageapp_unsigned.pdf
baseline_screens\debug2\StampsProblems.pdf
extras\annotsforChris\technical-contract.pdf
baseline_screens\customers1\test_filefails_jpedal.pdf
baseline_screens\customers2\ULTRA+PETROLEUM+2006_Annual_Report.pdf
	 */
}
