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
* XFAFormObject.java
* ---------------
*/
package com.idrsolutions.pdf.acroforms.xfa;

import java.awt.Rectangle;

import javax.swing.JTextField;

import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.utils.LogWriter;


/**
 * @author chris
 *
 */
public class XFAFormObject extends FormObject {
	
	/** layout orientations to be applied to this object*/
	public static final int LAYOUT_POSITION = 1;
	public static final int LAYOUT_LEFTtoRIGHT = 2;
	public static final int LAYOUT_RIGHTtoLEFT = 3;
	public static final int LAYOUT_TABLE = 4;
	public static final int LAYOUT_TOPtoBOTTOM = 5;
	
	/** holds the layout of this form*/
	public int layout;
	
	/** name for script expressions */
	public String script;
	/** the script type, e.g. javascript*/
	public String scriptType;
	
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 3;
	public static final int BOTTOM = 4;
	public static final int INLINE = 0;
	
	/** defines the area the caption can fit in */
	public int heightOfCaption;
	public int widthOfCaption;
	
	/** holds the captions position relative to the content, DEFAULT = INLINE*/
	public int captionPosition = 0;
	
	public static final int SQUARE = 50;
	public static final int ROUND = 51;
	
	/** stores the shape of the outline of the field */
	public int outline;
	
	/** the way the choice list can be shown */
	public static final int CHOICE_CLICK = 1;
	public static final int CHOICE_ENTRY = 2;
	public static final int CHOICE_ALWAYS = 3;
	
	/** stores the way the choice list is shown */
	public int choiceShown;
	
	/** rectangle dash */
	public static final int BORDER_DASHED = 1;
	/** round dot */
	public static final int BORDER_DOTTED = 2;
	/** rectangle dash followed by round dot */
	public static final int BORDER_DASHDOT = 3;
	/** rectangle dash folloed by 2 round dots */
	public static final int BORDER_DASHDOTDOT = 4;
	public static final int BORDER_SOLID = 5;
	public static final int BORDER_LOWERED = 6;
	public static final int BORDER_RAISED = 7;
	public static final int BORDER_ETCHED = 8;
	public static final int BORDER_EMBOSSED = 9;
	
	/** stores the border stroke to be used for this field*/
	public int borderStroke;
	
	/** if anything is actioned in the field, keystroke, paste, new item, button click */
	public static final int ACTION_CHANGE = 1;
	public static final int ACTION_MOUSECLICK = 2;
	/** after the document is saved and validation emmits no more than error */
	public static final int ACTION_DOCSAVED = 3;
	/** event before the document is rendered, but after data binding, it comes after the ready event from associated Form DOM */
	public static final int ACTION_PRERENDER = 4;
	public static final int ACTION_GAINFOCUS = 5;
	public static final int ACTION_LOSSFOCUS = 6;
	/** when the field content is full */
	public static final int ACTION_FIELDFULL = 7;
	/** initilise the field instance, one for each instance, called after data binding */
	public static final int ACTION_INIT = 8;
	public static final int ACTION_MOUSEPRESS = 9;
	public static final int ACTION_MOUSEENTER = 10;
	public static final int ACTION_MOUSEEXIT = 11;
	public static final int ACTION_MOUSERELEASE = 12;
	/**
	 * 'postExecute' Occurs when data is sent to a web service via WSDL, 
	 * just after the reply to the request has been received and 
	 * the received data is marshalled in a connectionDataelement underneath $datasets. 
	 * A script triggered by this event has the chance to examine and process the recieved data. 
	 * After execution of this event the received data is deleted
	 */
	public static final int ACTION_WEBSENT = 13;
	/**
	 * Occurs when a request is sent to a web service via WSDL, 
	 * just after the data has been marshalled in a connectionData element underneath 
	 * $datasets but before the request has been sent. A script triggered by this event 
	 * has the chance to examine and alter the data before the request is sent. 
	 * If the script is marked to be run only at the server, the data is sent 
	 * to the server with an indication that it should run the associated script 
	 * before performing the rest of the processing
	 */
	public static final int ACTION_WEBSENDING = 14;
	/** after the form has been written out to pdf or xdp format */
	public static final int ACTION_POSTSAVE = 15;
	/** just before the form is written out to pdf or xdp format */
	public static final int ACTION_PRESAVE = 16;
	public static final int ACTION_POSTPRINT = 17;
	/**
	 * Occurs when data is submitted to the host via the HTTP protocol, 
	 * just after the data has been marshalled in a connectionData element 
	 * underneath $datasets but before the data is submitted to the host. 
	 * A script triggered by this event has the chance to examine and alter 
	 * the data before it is submitted. If the script is marked to be run 
	 * only at the server, the data is sent to the server with an indication 
	 * that it should run the associated script before performing the rest of the processing
	 */
	public static final int ACTION_SUBMITVIAHTTP = 18;
	/** occurs when DOM has finished loading */
	public static final int ACTION_DOMLOADED = 19;
	
	/** stores the action of this field */
	public int activity=-1;
	
	/** stores the typeface for this field */
	public String typeface;
	
	/** stores the numeric value for this field, used for integer fields */
	public int numericValue;
	
	/** holds weither this field is editable or not */
	public boolean editable;
	
	/** inset values for the margin */
	public int marginBottomInset;
	public int marginTopInset;
	public int marginLeftInset;
	public int marginRightInset;
	
	public static final int DELEGATETOSERVER = 30;
	public static final int FORMAT_XDP = 1;
	public static final int FORMAT_URLENCODED = 2;
	public static final int FORMAT_PDF = 3;
	public static final int FORMAT_XFD = 4;
	public static final int FORMAT_XML = 5;

	public static final int ENCODE_DEFAULT = 0;
	public static final int ENCODE_ISO_8859_1 = 1;
	public static final int ENCODE_ISO_8859_2 = 2;
	public static final int ENCODE_ISO_8859_7 = 3;
	public static final int ENCODE_SHIFT_JIS = 4;
	public static final int ENCODE_KSC_5601 = 5;
	public static final int ENCODE_BIG_FIVE = 6;
	public static final int ENCODE_GB_2312 = 7;
	public static final int ENCODE_UTF_8 = 8;
	public static final int ENCODE_UTF_16 = 9;
	public static final int ENCODE_UCS_2 = 10;
	public static final int ENCODE_FONTSPECIFIC = 15;
	
	/** holds the format the data should be bundeled into for submition to server */
	public int submitDataFormat;
	/** the url to submit the data to */
	public String submitURL;
	/** stores the encoding used for the submit action */
	public int submitTextEncode;
	
	/** stores the size of the long side of the medium */
	public int longMedium;
	
	/** stores the short edge of the medium */
	public int shortMedium;
	
	/** store the name for the std paper size*/
	public String stdPaperName;
	
	/** justify all lines except last*/
	public static final int JUSTIFY_EXCEPT_LAST_ALIGNMENT = 2;
	/** justify all lines*/
	public static final int JUSTIFY_ALIGNMENT = 3;
	// TODO future NOT CURRENTLY KNOWN
	public static final int RADIX_ALIGNMENT = 4;
	//0 CENTER, 2 LEFT, 4 RIGHT
	
	public static final float TOP_ALIGNMENT = 0.0f;
	public static final float BOTTOM_ALIGNMENT = 1.0f;
	public static final float CENTER_ALIGNMENT = 0.5f;
	
	public float verticalAllgin;
	
	public XFAFormObject() {
		super();
		isXFAObject = true;
	}
	
	public FormObject duplicate(){
		XFAFormObject newObject = (XFAFormObject) super.duplicate();
		
		newObject.layout=layout;
		newObject.script=script;
		newObject.heightOfCaption=heightOfCaption;
		newObject.widthOfCaption=widthOfCaption;
		newObject.captionPosition=captionPosition;
		newObject.outline=outline;
		newObject.choiceShown=choiceShown;
		newObject.borderStroke=borderStroke;
		newObject.activity=activity;
		newObject.typeface=typeface;
		newObject.numericValue=numericValue;
		newObject.editable=editable;
		newObject.marginBottomInset=marginBottomInset;
		newObject.marginTopInset=marginTopInset;
		newObject.marginLeftInset=marginLeftInset;
		newObject.marginRightInset=marginRightInset;
		newObject.submitDataFormat=submitDataFormat;
		newObject.submitURL=submitURL;
		newObject.submitTextEncode=submitTextEncode;
		newObject.longMedium=longMedium;
		newObject.shortMedium=shortMedium;
		newObject.stdPaperName=stdPaperName;
		newObject.verticalAllgin=verticalAllgin;
		
		return newObject;
	}
	
	/**
	 * 
	 */
	public String toString(){
		StringBuffer buf = new StringBuffer();
		
		buf.append("\n  layout=");
		buf.append(layout);
		buf.append("\n  scriptname=");
		buf.append(script);
		buf.append("\n  heightofcaption=");
		buf.append(heightOfCaption);
		buf.append("\n  widthofcaption=");
		buf.append(widthOfCaption);
		buf.append("\n  captionposition=");
		buf.append(captionPosition);
		buf.append("\n  outline=");
		buf.append(outline);
		buf.append("\n  choiceshown=");
		buf.append(choiceShown);
		buf.append("\n  borderstroke=");
		buf.append(borderStroke);
		buf.append("\n  activity=");
		buf.append(activity);
		buf.append("\n  typeface=");
		buf.append(typeface);
		buf.append("\n  numericvalue=");
		buf.append(numericValue);
		buf.append("\n  editable=");
		buf.append(editable);
		buf.append("\n  maginbottom=");
		buf.append(marginBottomInset);
		buf.append("\n  margintop=");
		buf.append(marginTopInset);
		buf.append("\n  marginleft=");
		buf.append(marginLeftInset);
		buf.append("\n  marginright=");
		buf.append(marginRightInset);
		buf.append("\n  submitdataformat=");
		buf.append(submitDataFormat);
		buf.append("\n  submiturl=");
		buf.append(submitURL);
		buf.append("\n  submittextencoding=");
		buf.append(submitTextEncode);
		buf.append("\n  longmedium=");
		buf.append(longMedium);
		buf.append("\n  shortmedium=");
		buf.append(shortMedium);
		buf.append("\n  stdpapername=");
		buf.append(stdPaperName);
		buf.append("\n  verticalalign=");
		buf.append(verticalAllgin);
		
		return super.toString()+ buf;
	}
	
	/**
	 * reads and stores the horizontal alignment
	 * 'center' center horizontally, 
	 * 'justify' left align last line and justify the rest, 
	 * 'justifyAll' justify all lines to fill available region, 
	 * 'left' align left, 
	 * 'radix' align radix indicater at the radixOffset (in para element), 
	 * 'right' align right
	 */
	public void setHorizontalAlign(Object field) {
    	Q = JTextField.LEFT;
		if(field.equals("center")){
			Q = JTextField.CENTER;
		}else if(field.equals("justify")){
			Q = JUSTIFY_EXCEPT_LAST_ALIGNMENT;
		}else if(field.equals("justifyAll")){
			Q = JUSTIFY_ALIGNMENT;
		}else if(field.equals("left")){
			Q = JTextField.LEFT;
		}else if(field.equals("radix")){
			Q = RADIX_ALIGNMENT;
		}else if(field.equals("right")){
			Q = JTextField.RIGHT;
		}else {
			LogWriter.writeFormLog("XFAFormObject.setHorizontalAlign not taking "+field, FormStream.debugUnimplemented);
		}
		
		//System.out.println("Set XFA alignment="+alignment);
    }

	/**
	 * vAlign - vertical alignment, top, middle, bottom, 
	 * (tabDefault and tabStops are reserved for future use)
	 */
	public void setVerticalAllign(String attValue) {
		if(attValue.equals("middle")){
			verticalAllgin = CENTER_ALIGNMENT;
		}else if(attValue.equals("top")){
			verticalAllgin = TOP_ALIGNMENT;
		}else if(attValue.equals("bottom")){
			verticalAllgin = BOTTOM_ALIGNMENT;
		}else {
			LogWriter.writeFormLog("XFAFormObject.setVerticalAlign not taking "+attValue,XFAFormStream.debugUnimplemented);
		}
	}
	
	/** 
	 * layout - strategy, 
	 * 'position' layed out according to their positions, 
	 * 'lr-tb' flowed left - right, top - bottom, 
	 * 'rl-tb' flowed right - left, top - bottom, 
	 * 'row' this is inner element of table and is one or more rows, layed out from right to left, 
	 * adjusted to height of row and width of column array, 
	 * 'table' inner elements must be rows and are layed out from top to bottom, 
	 * 'tb' flowed from top to bottom.
	 */
	public void setLayout(String val){
		if(val.equals("position")){
			layout = LAYOUT_POSITION;
		}else if(val.equals("lr-tb")){
			layout = LAYOUT_LEFTtoRIGHT;
		}else if(val.equals("rl-tb")){
			layout = LAYOUT_RIGHTtoLEFT;
		}else if(val.equals("row")){
			LogWriter.writeFormLog("row layout NOT IMPLEMENTED",XFAFormStream.debugUnimplemented);
		}else if(val.equals("table")){
			layout = LAYOUT_TABLE;
		}else if(val.equals("tb")){
			layout = LAYOUT_TOPtoBOTTOM;
		}
	}
	
	/**
	 * locale - 'ambient' use locale to application, valid locale name.
	 */
	public static void setLocale(String attValue) {
		LogWriter.writeFormLog("XFAFormObject.setLocale NOT IMPLEMENTED ="+attValue,XFAFormStream.debugUnimplemented);
	}
	
	/**
	 * name - identifier for script expressions.
	 */
	public void setScript(String attValue) {
		script = attValue;
	}
	
	/**
	 * sets up the type of binding to apply in a merdge operation
	 */
	public static void setBindtypeOnMerge(String attValue) {
		//match - the type of binding in a merge operation, 
		//'once' the standard, 
		//'none' no bindings will be applied, 
		//'global' if normal rules fail it will look out side the current record for a bind, 
		//'dataRef' bind to node specified in ref attribute
		if(XFAFormStream.debugUnimplemented)
			System.out.println("XFAFormObject.setBindtypeOnMerge() NOT IMPLEMENTED val="+attValue);
	}
	
	/**
	 * stores the position to the underlying vector 
	 */
	public static void setPositionToVector(String attValue) {
		//hand - 
		//'even' centre line on underlying vector, 
		//'left' position to left of vector, 
		//'right' position to right of vector
		if(XFAFormStream.debugUnimplemented)
			System.out.println("XFAFormObject.setPositionToVector() NOT IMPLEMENTED");
	}
	
	/**
	 * reserve - (depends on placement), 
	 * inline ignore, 
	 * left or right this property defines the width of the caption, 
	 * top or bottom defines the height of the caption
	 */
	public void setCaptionReserve(int attValue) {
		if(captionPosition==INLINE){
			//ignore
		}else if(captionPosition==TOP || captionPosition==BOTTOM){
			heightOfCaption = attValue;
		}else if(captionPosition==LEFT || captionPosition==RIGHT){
			widthOfCaption = attValue;
		}
	}
	
	/**
	 * placement - 
	 * 'left' of content, 
	 * 'right' of content, 
	 * 'top' of content, 
	 * 'bottom' of content, 
	 * 'inline' appears inline,and prior to, text content
	 */
	public void setCaptionPlacement(String attValue) {
		if(attValue.equals("left")){
			captionPosition = LEFT;
		}else if(attValue.equals("right")){
			captionPosition = RIGHT;
		}else if(attValue.equals("top")){
			captionPosition = TOP;
		}else if(attValue.equals("bottom")){
			captionPosition = BOTTOM;
		}else if(attValue.equals("inline")){
			captionPosition = INLINE;
		}else {
			if(XFAFormStream.debugUnimplemented){
				System.out.println("caption placement NOT CHECKED for ="+attValue);
			}
		}
	}
	
	/**
	 * 'square' for square outline, 
	 * 'round' for round outline
	 */
	public void setOutline(String attValue) {
		if(attValue.equals("square")){
			outline = SQUARE;
		}else if(attValue.equals("round")){
			outline = ROUND;
		}else {
		}
	}

	/**
	 * application/x-formcalc = FormCalc script p605 XFA spec v2.2, 
	 * cdata = other scripts are implementation defined
	 */
	public static void setContentType(String attValue) {
		if(XFAFormStream.debugUnimplemented)
			System.out.println("contentType not implemented");
	}
	
	/**
	 * stores the opening method of the choicelist for viewing the list
	 */
	public void setChoiceOpening(String attValue) {
		/*open - 
		 * 'userControl' list appears when user clicks, disappears 
		 * when cursor exits list or user action occurs,
		 * 'onEntry' list appears on entry to the field, disappears on exit of the
		 * of the field, 
		 * 'always' list displayed whenever the field is visible
		 */
		if(attValue.equals("userControl")){
			//when user clicks
			//ComboBox
			choiceShown = CHOICE_CLICK;
		}else if(attValue.equals("OnEntry")){
			//on entry and exit
			choiceShown = CHOICE_ENTRY;
		}else if(attValue.equals("always")){
			//list field
			choiceShown = CHOICE_ALWAYS;
		}
	}
	
	/**
	 * stroke - 
	 * solid, 
	 * dashed, 
	 * dotted (round dots), 
	 * dashDot (alternating Rect dashes and round dots), 
	 * dashDotDot, 
	 * lowered (appears lowered), 
	 * raised (appears raised), 
	 * etched (groove in surface), 
	 * embossed (ridge raised out of surface)
	 */
	public void setBorderStroke(String attValue) {
		if(attValue.equals("solid")){
			borderStroke = BORDER_SOLID;
		}else if(attValue.equals("dashed")){
			borderStroke = BORDER_DASHED;
		}else if(attValue.equals("dotted")){
			borderStroke = BORDER_DOTTED;
		}else if(attValue.equals("dashDot")){
			borderStroke = BORDER_DASHDOT;
		}else if(attValue.equals("dashDotDot")){
			borderStroke = BORDER_DASHDOTDOT;
		}else if(attValue.equals("lowered")){
			borderStroke = BORDER_LOWERED;
		}else if(attValue.equals("raised")){
			borderStroke = BORDER_RAISED;
		}else if(attValue.equals("etched")){
			borderStroke = BORDER_ETCHED;
		}else if(attValue.equals("embossed")){
			borderStroke = BORDER_EMBOSSED;
		}else {
		}
	}
	
	/**
	 * activity TYPE- event generated by the ref property. 
	 * 'change' user changes field, each key stroke, paste, new item, button click, 
	 * 'click' mouse click in field, 
	 * 'docClose' event that occurs after document is saved and if all validation emmits no more that errors, 
	 * 'docReady' event before the document is rendered, but after data binding, it comes after the ready event from associated Form DOM, 
	 * 'enter' when the field (or field within subform or exclusion group gains keyboard focus, 
	 * 'exit' same as enter except for when the keyboard loses focus, 
	 * 'full' occurs when the user has entered maximum content into the field, 
	 * 'initialize' occurs after data binding is complete, seperate event for each instance of the field in the Form DOM, 
	 * 'mouseDown' when the mouse is pressed in the field, 
	 * 'mouseEnter' when the mouse enters the field boundary, 
	 * 'mouseExit' when the mouse exits the field boundary, 
	 * 'mouseUp' when the mouse is released within field, 
	 * 'postExecute' Occurs when data is sent to a web service via WSDL, just after the reply to the request has been received and the received data is marshalled in a connectionDataelement underneath $datasets. A script triggered by this event has the chance to examine and process the recieved data. After execution of this event the received data is deleted, 
	 * 'postPrint' just after the rendered form has been sent to the printer, 
	 * 'postSave' just after the form is written out in pdf or xdp format, 
	 * 'preExecute' Occurs when a request is sent to a web service via WSDL, just after the data has been marshalled in a connectionData element underneath $datasets but before the request has been sent. A script triggered by this event has the chance to examine and alter the data before the request is sent. If the script is marked to be run only at the server, the data is sent to the server with an indication that it should run the associated script before performing the rest of the processing, 
	 * 'preSave' just before the form data is written to pdf or xdp format, 
	 * 'preSubmit' Occurs when data is submitted to the host via the HTTP protocol, just after the data has been marshalled in a connectionData element underneath $datasets but before the data is submitted to the host. A script triggered by this event has the chance to examine and alter the data before it is submitted. If the script is marked to be run only at the server, the data is sent to the server with an indication that it should run the associated script before performing the rest of the processing, 
	 * 'ready' occurs when DOM finished loading.
	 */
	public void setEventAction(String attValue) {
		//activity is defined in the ref category
		if(attValue.equals("change")){
			activity = ACTION_CHANGE;
		}else if(attValue.equals("click")){
			activity = ACTION_MOUSECLICK;
		}else if(attValue.equals("docClose")){
			activity = ACTION_DOCSAVED;
		}else if(attValue.equals("docReady")){
			activity = ACTION_PRERENDER;
		}else if(attValue.equals("enter")){
			activity = ACTION_GAINFOCUS;
		}else if(attValue.equals("exit")){
			activity = ACTION_LOSSFOCUS;
		}else if(attValue.equals("full")){
			activity = ACTION_FIELDFULL;
		}else if(attValue.equals("initialize")){
			activity = ACTION_INIT;
		}else if(attValue.equals("mouseDown")){
			activity = ACTION_MOUSEPRESS;
		}else if(attValue.equals("mouseEnter")){
			activity = ACTION_MOUSEENTER;
		}else if(attValue.equals("mouseExit")){
			activity = ACTION_MOUSEEXIT;
		}else if(attValue.equals("mouseUp")){
			activity = ACTION_MOUSERELEASE;
		}else if(attValue.equals("postExecute")){
			activity = ACTION_WEBSENT;
		}else if(attValue.equals("postPrint")){
			activity = ACTION_POSTPRINT;
		}else if(attValue.equals("postSave")){
			activity = ACTION_POSTSAVE;
		}else if(attValue.equals("preExecute")){
			activity = ACTION_WEBSENDING;
		}else if(attValue.equals("preSave")){
			activity = ACTION_PRESAVE;
		}else if(attValue.equals("preSubmit")){
			activity = ACTION_SUBMITVIAHTTP;
		}else if(attValue.equals("ready")){
			activity = ACTION_DOMLOADED;
		}else {
		}
	}
	
	/**
	 * stores the typeface name for this field
	 */
	public void setTypeface(String attValue) {
		typeface = attValue;
	}
	
	/**
	 * stores the numeric value of the string passed in, used for integers
	 */
	public void setIntegerValue(String nodeValue) {
		if(nodeValue==null)
			return;
		
		try{
			numericValue = Integer.parseInt(nodeValue); 
		}catch(NumberFormatException e){
			LogWriter.writeFormLog("NumberFormatException XFAFormObject",false);
		}
	}
	
	/**
	 * save - 
	 * 0 - the values in this element are for display only, 
	 * 1 - values from this element may be entered into the field.
	 */
	public void setEditability(String attValue) {
		if(attValue.equals("0")){
			editable = false;
		}else if(attValue.equals("1")){
			editable = true;
		}else {
		}
	}
	
	/**
	 * store the width of this object in the rect array
	 */
	public void setWidth(int attValue) {
		//rect.width = attValue;
		
		if(BBox==null)
			BBox=new Rectangle(0,0,0,0);
		
		BBox.width=attValue;
	}

	/**
	 * store the height of this object in the rect array
	 */
	public void setHeight(int attValue) {
		//rect.height = attValue;
		
		if(BBox==null)
			BBox=new Rectangle(0,0,0,0);
		
		BBox.height=attValue;
	}

	/**
	 * store the x position of this object in the rect array
	 */
	public void setX(int attValue) {
		//rect.x = attValue;
		
		if(BBox==null)
			BBox=new Rectangle(0,0,0,0);
		
		BBox.x=attValue;
	}

	/**
	 * store the y position of this object in the rect array
	 */
	public void setY(int attValue) {
		//rect.y = attValue;
		
		if(BBox==null)
			BBox=new Rectangle(0,0,0,0);
		
		BBox.y=attValue;
	}

	/** setup the bottom inset for the margin */
	public void setMarginBottomInset(int attValue) {
		marginBottomInset = attValue;
	}
	
	/** setup the top inset for the margin */
	public void setMarginTopInset(int attValue) {
		marginTopInset = attValue;
	}
	
	/** setup the left inset for the margin */
	public void setMarginLeftInset(int attValue) {
		marginLeftInset = attValue;
	}
	
	/** setup the right inset for the margin */
	public void setMarginRightInset(int attValue) {
		marginRightInset = attValue;
	}
	
	/**
	 * store the long length of the medium
	 */
	public void setLongMeduim(int attValue) {
		longMedium = attValue;
	}

	/**
	 * stores the short edge of the medium
	 */
	public void setShortMedium(int attValue) {
		shortMedium = attValue;
	}

	/**
	 * store the name for the std paper size
	 */
	public void setStdPaperName(String attValue) {
		stdPaperName = attValue;
	}
	
	/**
	 * format the data will be submitted, 
	 * 'xdp' xdp format, 
	 * 'delegate' delegate to server, the server will specify dynamically, 
	 * 'formdata' packaged in url encoded format, the textEncoding property has no effect, 
	 * 'pdf' in pdf format, 
	 * 'xfd' xfd format, 
	 * 'xml' xml format, schema is determined by rules used in the save operation described in ref
	 */
	public void setSubmitFormat(String attValue) {
		if(attValue.equals("xdp")){
			submitDataFormat = FORMAT_XDP;
		}else if(attValue.equals("delegate")){
			submitDataFormat = DELEGATETOSERVER;
		}else if(attValue.equals("formdata")){
			submitDataFormat = FORMAT_URLENCODED;
		}else if(attValue.equals("pdf")){
			submitDataFormat = FORMAT_PDF;
		}else if(attValue.equals("xfd")){
			submitDataFormat = FORMAT_XFD;
		}else if(attValue.equals("xml")){
			submitDataFormat = FORMAT_XML;
		}else {
		}
	}

	/**
	 * stores the url to submit the data too
	 */
	public void setSubmitURL(String attValue) {
		submitURL = attValue;
	}

	/**
	 * textEncodeing - text encoding, all are case-insensitive, 
	 * 'none' encoding as per the operating system, 
	 * 'ISO-8859-1' also known as latin-1, 
	 * 'iso-8859-2', 
	 * 'iso-8859-7', 
	 * 'shift-jis' encoded using jis x 0208 or shift-jis, 
	 * 'ksc-5601' encoded using the code for information interchange (hangul and hanja), 
	 * 'big-five' traditional chinese (big-five), their are several implementations of this 
	 * xfa uses microsoft as code page 950, http://www.microsoft.com/globaldev/reference/dbcs/950.htm,
	 * 'gb-2312' simplified chinese, 
	 * 'utf-8', 
	 * 'utf-16', 
	 * 'ucs-2' unicode defined by ucs-2, 
	 * 'fontSpecific' font-specific way, each character is one 8-bit byte
	 */
	public void setSubmitTextEncoding(String attValue) {
		if(submitDataFormat==FORMAT_URLENCODED){
			//in formdata format the textencoding has no effect
			return;
		}
		
		if(attValue.equalsIgnoreCase("none")){
			submitTextEncode = ENCODE_DEFAULT;
		}else if(attValue.equalsIgnoreCase("ISO-8859-1")){
			submitTextEncode = ENCODE_ISO_8859_1;
		}else if(attValue.equalsIgnoreCase("iso-8859-2")){
			submitTextEncode = ENCODE_ISO_8859_2;
		}else if(attValue.equalsIgnoreCase("iso-8859-7")){
			submitTextEncode = ENCODE_ISO_8859_7;
		}else if(attValue.equalsIgnoreCase("shift-jis")){
			submitTextEncode = ENCODE_SHIFT_JIS;
		}else if(attValue.equalsIgnoreCase("ksc-5601")){
			submitTextEncode = ENCODE_KSC_5601;
		}else if(attValue.equalsIgnoreCase("big-five")){
			submitTextEncode = ENCODE_BIG_FIVE;
		}else if(attValue.equalsIgnoreCase("gb-2312")){
			submitTextEncode = ENCODE_GB_2312;
		}else if(attValue.equalsIgnoreCase("utf-8")){
			submitTextEncode = ENCODE_UTF_8;
		}else if(attValue.equalsIgnoreCase("utf-16")){
			submitTextEncode = ENCODE_UTF_16;
		}else if(attValue.equalsIgnoreCase("ucs-2")){
			submitTextEncode = ENCODE_UCS_2;
		}else if(attValue.equalsIgnoreCase("fontSpecific")){
			submitTextEncode = ENCODE_FONTSPECIFIC;
		}else {
		}
	}

	/**
	 * stores the type of script defined in scrip variable
	 * e.g. application/x-javascript
	 */
	public void setScriptType(String item) {
		scriptType = item;
	}
}
