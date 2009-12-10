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
* GUIData.java
* ---------------
*/
package org.jpedal.objects.acroforms.formData;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.jpedal.objects.Javascript;

import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.external.CustomFormPrint;


/**
 * Abstraction so forms can be rendered in ULC
 * - see SwingData for full details of usage
 */
public interface GUIData {

    public void invalidateForms();

	void resetDuplicates();

	void removeAllComponentsFromScreen();

	void setPageData(PdfPageData pageData, int insetW, int insetH);

	void completeField(FormObject formObject, int formNum,
                       Integer widgetType, Object retComponent, PdfObjectReader currentPdfFile);

	void completeFields(int page);

	void displayComponents(int startPage, int endPage);

	int getNextFreeField();

	void reportError(int code, Object[] args);

	/** resets the storage values for the unformatted and invalid values */
	void reset();

	List getComponentNameList(int pageNumber);

	/** returns the displayed component which is being displayed e.g. jtextfield */
	Object[] getComponentsByName(String objectName);

	int getStartComponentCountForPage(int page);

	void initParametersForPage(PdfPageData pageData, int page);

    void setLayerData(PdfLayerList layers);

	void resetComponents(int formCount, int pageCount, boolean b);

	void setJavascript(Javascript javascript);

	/**valid flag used by Javascript to allow rollback
     * &nbsp
	 * call ttf to reset all values held
     * @param name - name of the field to change the value of
     * @param value - the value to change it to
     * @param isValid - is a valid value or not
     * @param isFormatted - is formatted properly
     * @param reset - can be reset, ie if false we store for rollback if it is successfully formatted or validated
     */
	void setValue(String ref,Object value, boolean isValid,boolean isFormatted,boolean reset);

	/** returns the last valid value for the pdf ref given */
	Object getLastValidValue(String ref);

	/** returns the last unformatted value for the pdf ref given */
	Object getLastUnformattedValue(String fieldName);

	String[] getDefaultValues();

	/** returns the value for the given field, 
	 * from either the field name or the field Pdf Ref
	 */
	Object getValue(Object fieldRef);

	/** returns the widget object */
	Object getWidget(Object fieldName);
	
	void loseFocus();

	void renderFormsOntoG2(Object g2, int page, float scaling, int i, int accDisplay, Map componentsToIgnore, FormFactory formFactory);

	void resetScaledLocation(float scaling, int displayRotation, int indent);

	void setRootDisplayComponent(Object pdfDecoder);

	void setPageValues(float scaling, int rotation);


    void setPageDisplacements(int[] reached, int[] reached2);
    
    /** returns the type of form we have defined by this name, ie annotation, signature, pushbutton etc */
    Integer getTypeValueByName(String name);

	void storeRawData(FormObject formObject);

	void flushFormData();

	/** returns all the formObjects by the specified name */
	Object getRawForm(String objectName);


    /** returns the raw FormObject data */
	Map getRawFormData();

    int getMaxFieldSize();

    void setOffset(int offset);

	void invalidate(String name);
	/** converts the field index to its unique Pdf Reference */
	String convertIDtoRef(int objectID);

	/** stores the displayed fields value, into the FormObject */
	void storeDisplayValue(String fieldRef);

	String[] getChildNames(String name);

	/** updates the visible value of the changed form, used via javascript actions */
	void setCompVisible(String ref, boolean visible);

	/** defined in page 102 of javascript for acrobat api */
	//definition is stored in JpedalDefaultJavascript.alert(String cMsg,int nIcon,int nType,String cTitle,Object oDoc,Object oCheckbox)
	int alert(String cMsg,int nIcon,int nType,String cTitle,Object oDoc,Object oCheckbox);

    /**
     * allow user to lookup page with name of Form.
     * @param formName or ref (10 0 R)
     * @return page number or -1 if no page found
     */
    public int getPageForFormObject(String formName);

	void popup(FormObject formObj, PdfObjectReader currentPdfFile);

	/** used internally to correct printing display*/
	void setUnsortedListForPage(int page, List unsortedForms);

    /** sets the text color for the specified form */
	public void setTextColor(String objectRefAsString, Color textColor);

    void setCustomPrintInterface(CustomFormPrint customFormPrint);

    /** returns the display type of field this specified field is, ie text, button or list, or 01 for unknown. */
	public int getFieldType(Object swingComp);
}
