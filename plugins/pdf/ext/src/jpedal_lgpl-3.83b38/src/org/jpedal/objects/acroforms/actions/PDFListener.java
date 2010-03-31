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
 * PDFListener.java
 * ---------------
*/
package org.jpedal.objects.acroforms.actions;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;

/**
 * shared non component-specific code
 */
public class PDFListener {
	
	public static final boolean debugMouseActions = false;
	
    public FormObject formObject;
    public AcroRenderer acrorend;
    public ActionHandler handler;

    public PDFListener(FormObject form, AcroRenderer acroRend, ActionHandler formsHandler) {
        formObject = form;
        acrorend = acroRend;
        handler = formsHandler;
    }

    public void mouseReleased(Object e) {
    	if(debugMouseActions)
    		System.out.println("PDFListener.mouseReleased()");
        handler.A(e, formObject, ActionHandler.MOUSERELEASED);
        handler.U(e, formObject);
    }

    public void mouseClicked(Object e) {
    	if(debugMouseActions)
    		System.out.println("PDFListener.mouseClicked()");
        handler.A(e, formObject, ActionHandler.MOUSECLICKED);
    }

    public void mousePressed(Object e) {
    	if(debugMouseActions)
    		System.out.println("PDFListener.mousePressed()");
        handler.A(e, formObject, ActionHandler.MOUSEPRESSED);
        handler.D(e, formObject);
    }

    public void keyReleased(Object e) {
    	if(debugMouseActions)
    		System.out.println("PDFListener.keyReleased()");
        handler.K(e, formObject, ActionHandler.MOUSERELEASED);
        handler.V(e,formObject, ActionHandler.MOUSERELEASED);
    }

    public void focusLost(Object e) {
    	if(debugMouseActions)
    		System.out.println("PDFListener.focusLost()");
    	
        handler.Bl(e, formObject);
        handler.K(e, formObject,ActionHandler.FOCUS_EVENT);
        handler.V(e, formObject,ActionHandler.FOCUS_EVENT);
        //format the field value after it has been altered
        handler.F(formObject);
        
        //this is added in so that the popup forms do not flash with focus 
        //but is causes forms focus to be lost unexpectadly.
//        acrorend.getCompData().loseFocus();
    }

    public void focusGained(Object e) {
    	if(debugMouseActions)
    		System.out.println("PDFListener.focusGained()");
    	
        handler.Fo(e, formObject);

        //this needs to only be done on certain files, that specify this, not all.
        //user can enter some values (ie 1.10.2007 as its still valid for a date which are then turned into
        //01.10.2007 when user quits field. If user re-enters form, this sets it back to 1.10.2007
        //String fieldRef = formObject.getPDFRef();
        String fieldRef = formObject.getObjectRefAsString();
        //reset to unformatted value as Acrobat does
        Object lastUnformattedValue=acrorend.getCompData().getLastUnformattedValue(fieldRef);
        if(lastUnformattedValue!=null && !lastUnformattedValue.equals(acrorend.getCompData().getValue(fieldRef))){
        	
            acrorend.getCompData().setValue(fieldRef,lastUnformattedValue,false,false,false);
        }
    }
}