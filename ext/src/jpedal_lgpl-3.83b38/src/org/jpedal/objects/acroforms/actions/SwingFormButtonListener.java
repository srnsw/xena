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
* SwingFormButtonListener.java
* ---------------
*/
package org.jpedal.objects.acroforms.actions;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.jpedal.objects.Javascript;

import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.acroforms.utils.ConvertToString;


/**
 * class implements MouseListener to create all required actions for the associated button
 *
 * @author chris
 */
public class SwingFormButtonListener implements MouseListener {

    private static final boolean debugXFAActions = false;
    private static final boolean showMethods = false;

    /**
     * used to setup any actions for the forms
     */
    private String command = "";
    private Map enteredAction = null;
    private Map exitedAction = null;
    private Map hideAction = null;
    private Map clickedAction = null;
    private Map captionChanger = null;

    /**
     * javascript commands and action
     */
    private int javascriptWhen;

    /**
     * the url to submit data to
     */
    private String submitURL = null;

    /**
     * holds link to the acro renderer for access to components
     */
    private AcroRenderer acrorend;

    /**
     * takes a map, that specifies what to do on a specific action
     * <br>the type must be lowercase
     */
    public SwingFormButtonListener(Map actionMap, String type, AcroRenderer acrorenderer) {
    	if(showMethods)
    		System.out.println("SwingFormButtonListener.SwingFormButtonListener(map string acrorenderer)");
    	
        if (type.equals("entered"))
            enteredAction = actionMap;
        else if (type.equals("exited"))
            exitedAction = actionMap;
        else if (type.equals("Hide"))
            hideAction = actionMap;
        else if (type.equals("Clicked"))
            clickedAction = actionMap;

        acrorend = acrorenderer;
    }

    /**
     * sets up the captions to change when needed
     */
    public SwingFormButtonListener(String normalCaption, String rolloverCaption, String downCaption) {
    	if(showMethods)
    		System.out.println("SwingFormButtonListener.SwingFormButtonListener(string string string)");
    	
        //set up the captions to work for rollover and down presses of the mouse
        captionChanger = new HashMap();
        if (rolloverCaption != null)
            captionChanger.put("rollover", rolloverCaption);
        if (downCaption != null)
            captionChanger.put("down", downCaption);
        captionChanger.put("normal", normalCaption);
    }

    /**
     * sets up this mouse listener to apply the assigned action
     */
    public SwingFormButtonListener(int activity, String scriptType, String script, AcroRenderer acro) {
    	if(showMethods)
    		System.out.println("SwingFormButtonListener.SwingFormButtonListener(int string string acrorenderer)");
    	
        if (debugXFAActions)
            System.out.println("setup mouse=" + activity + ',' + scriptType + ',' + script);

        if (script.indexOf("resetData") != -1) {
        	// @mark - this is not called anymore, but reset form works
        	//i think we could get rid of most of these listeners now as most is 
        	//Dealt with in the javascript actions methods.
            command = "ResetForm";
            acrorend = acro;
        } else if (scriptType != null) {
            if (scriptType.indexOf("javascript") != -1) {
                int index = script.indexOf('(');
                //@forms
                javascriptWhen = activity;
            } else if (scriptType.indexOf("submit") != -1) {
            }
        } else {
        }
//		switch(activity) {
//		case XFAFormObject.ACTION_MOUSECLICK:
//		case XFAFormObject.ACTION_MOUSEENTER:
//		case XFAFormObject.ACTION_MOUSEEXIT:
//		case XFAFormObject.ACTION_MOUSEPRESS:
//		case XFAFormObject.ACTION_MOUSERELEASE:
    }

    public void mouseEntered(MouseEvent e) {
        if (PDFListener.debugMouseActions || showMethods)
            System.out.println("customMouseListener.mouseEntered()");

        if (e.getSource() instanceof AbstractButton && captionChanger != null) {
            if (captionChanger.containsKey("rollover")) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("rollover"));
            }
        }

        if (command.equals("togglenoview")) {
            ((JComponent) e.getSource()).setVisible(true);
            ((JComponent) e.getSource()).repaint();
        } else if (command.equals("comboEntry")) {
            ((JComboBox) e.getSource()).showPopup();
        }
        
        if (enteredAction != null) {
        	String command = (String) enteredAction.get("command");
            if (command.equals("/Hide")) {
                String name = (String) enteredAction.get("fields");
                int start = 0;
                if (name.startsWith("("))
                    start++;
                name = name.substring(start, name.length() - start);

                Object[] checkObj = acrorend.getComponentsByName(name);

                if (checkObj != null) {

                    if(checkObj[0] instanceof Component){
                        boolean hide = ((Boolean) enteredAction.get("hide")).booleanValue();

                        Component swingComponent= (Component) checkObj[0];
                        swingComponent.setVisible(!hide);
                        swingComponent.repaint();
                    }else{
                    }
                }
            } else {
            }
        }
    }

    public void mouseExited(MouseEvent e) {
        if (PDFListener.debugMouseActions || showMethods)
            System.out.println("customMouseListener.mouseExited()");

        if (e.getSource() instanceof AbstractButton && captionChanger != null) {
            if (captionChanger.containsKey("normal")) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("normal"));
            }
        }

        if (command.equals("togglenoview")) {
            ((JComponent) e.getSource()).setVisible(false);
            ((JComponent) e.getSource()).repaint();
        } else if (command.equals("comboEntry")) {
            ((JComboBox) e.getSource()).hidePopup();
        }
		
        if (exitedAction != null) {
            String command = (String) exitedAction.get("command");
            if (command.equals("/Hide")) {
                String name = (String) exitedAction.get("fields");
                int start = 0;
                if (name.startsWith("("))
                    start++;
                name = name.substring(start, name.length() - start);

                Object[] checkObj = acrorend.getComponentsByName(name);

                if (checkObj != null) {

                    if(checkObj[0] instanceof Component){
                        boolean hide = ((Boolean) exitedAction.get("hide")).booleanValue();

                        Component swingComponent= (Component) checkObj[0];
                        swingComponent.setVisible(!hide);
                        swingComponent.repaint();
                    }else{
                    }
                }
            } else {
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    	if(PDFListener.debugMouseActions || showMethods)
    		System.out.println("SwingFormButtonListener.mouseClicked()");
    }

    public void mousePressed(MouseEvent e) {
        if (PDFListener.debugMouseActions || showMethods)
            System.out.println("customMouseListener.mousePressed()");

        if (e.getSource() instanceof AbstractButton && captionChanger != null) {
            if (captionChanger.containsKey("down")) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("down"));
            }
        }

        if (command.equals("comboEntry")) {
//			((JComboBox) e.getSource()).showPopup();
        } else if (command.length() != 0) {
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (PDFListener.debugMouseActions || showMethods)
            System.out.println("customMouseListener.mouseReleased()");

        if (e.getSource() instanceof AbstractButton && captionChanger != null) {
            if (captionChanger.containsKey("rollover")) {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("rollover"));
            } else {
                ((AbstractButton) e.getSource()).setText((String) captionChanger.get("normal"));
            }
        }

        if (command.equals("comboEntry")) {
//			((JComboBox) e.getSource()).showPopup();
        } else if (command.equals("ResetForm")) {
//			ignore as is delt with in mousePressed()
        } else if (command.length() != 0) {
        }
    }
}
