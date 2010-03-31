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
* ActionFactory.java
* ---------------
*/
package org.jpedal.objects.acroforms.actions;

import org.jpedal.PdfDecoder;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.acroforms.actions.privateclasses.FieldsHideObject;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfObject;

import java.util.Map;

public interface ActionFactory {

    /**display popup for user to read*/
    void showMessageDialog(String s);

    /**print the page*/
    void print();

    /**reset all components to empty calues*/
    void reset();

    void setPDF(PdfDecoder decode_pdf,AcroRenderer acrorend);

    void setCursor(int eventType);

    void showSig(PdfObject sigObject);

    void submitURL(String[] listOfFields, boolean excludeList, String submitURL);

    Object getHoverCursor();

    void popup(Object raw, FormObject formObj, PdfObjectReader currentPdfFile);

    char getKeyPressed(Object ex);

    void setFieldVisibility(FieldsHideObject fieldToHide);

    void setPageandPossition(Object location);

	Object getChangingDownIconListener(Object downOff, Object downOn, int rotation);
}
