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
* FormFactory.java
* ---------------
*/
package org.jpedal.objects.acroforms.creation;

import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.actions.ActionFactory;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.formData.GUIData;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;

public interface FormFactory {

	/**
	 * matches types of PDF form objects
	 */
	public static final Integer UNKNOWN = new Integer(-1);
	public static final Integer LIST = new Integer(1);
	public static final Integer COMBOBOX = new Integer(2);
	public static final Integer SINGLELINETEXT = new Integer(3);
	public static final Integer SINGLELINEPASSWORD = new Integer(4);
	public static final Integer MULTILINETEXT = new Integer(5);
	public static final Integer MULTILINEPASSWORD = new Integer(6);
    public static final Integer PUSHBUTTON = new Integer(7);
    public static final Integer RADIOBUTTON = new Integer(8);
    public static final Integer CHECKBOXBUTTON = new Integer(9);
    public static final Integer ANNOTATION = new Integer(10);
    public static final Integer SIGNATURE = new Integer(11);
    
    public static int SWING = 1;
    public static int ULC = 2;

    /**
     * setup and return a List component, from the specified formObject
     * @see FormObject
     */
    public Object listField(FormObject formObject);

    /**
     * setup and return a ComboBox component, from the specified formObject
     * @see FormObject
     */
    public Object comboBox(FormObject formObject);

    /**
     * setup and return a single line Text component, from the specified formObject
     * @see FormObject
     */
    public Object singleLineText(FormObject formObject);

    /**
     * setup and return a single line Password component, from the specified formObject
     * @see FormObject
     */
    public Object singleLinePassword(FormObject formObject);

    /**
     * setup and return a multi line Text component, from the specified formObject
     * @see FormObject
     */
    public Object multiLineText(FormObject formObject);

    /**
     * setup and return a multi line Password component, from the specified formObject
     * @see FormObject
     */
    public Object multiLinePassword(FormObject formObject);

    /**
     * setup and return a push button component, from the specified formObject
     * @see FormObject
     */
    public Object pushBut(FormObject formObject);

    /**
     * setup and return a single radio button component, from the specified formObject
     * @see FormObject
     */
    public Object radioBut(FormObject formObject);

    /**
     * setup and return a single checkBox button component, from the specified formObject
     * @see FormObject
     */
    public Object checkBoxBut(FormObject formObject);

	/**
	 * setup annotations display with pop-ups, etc
	 */
	public Object annotationButton(FormObject formObject);
	
	/**
	 * setup the signature field
     */
	public Object signature(FormObject formObject);

    /**
	 * resets the factory for each page
	 */
	public void reset(AcroRenderer acroRenderer, ActionHandler formsActionHandler);

	/**
	 * return new instance of GUIData implementation to support component set
	 */
	public GUIData getCustomCompData();

    /**holds all the GUI specific action and event code*/
    ActionFactory getActionFactory();

    //return ULC or SWING constant
    int getType();


    public void setAPImages(final FormObject form, Object comp, boolean b);
}
