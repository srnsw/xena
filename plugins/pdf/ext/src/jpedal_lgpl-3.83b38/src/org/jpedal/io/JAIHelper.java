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
* JAIHelper.java
* ---------------
*/
package org.jpedal.io;

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

//<start-jfr>
import org.jpedal.PdfDecoder;
//<end-jfr>

import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.Messages;

public class JAIHelper {

    private static boolean JAI_TESTED;

    private static boolean useJAI=false;
    
    private static boolean isJAIFound=false;

    static{
        String JAIflag=System.getProperty("org.jpedal.jai");
        if(JAIflag!=null && JAIflag.toLowerCase().equals("true"))
            useJAI=true;

    }

    public static void confirmJAIOnClasspath() {

    	if(JAI_TESTED)
            return;

        JAIHelper.JAI_TESTED = true;

        if(useJAI){

            //test
            try {
                Class.forName("javax.media.jai.JAI");
                isJAIFound=true;
            } catch (ClassNotFoundException e) {
                //<start-jfr>
                if(PdfDecoder.showErrorMessages){
                	
                	String message=Messages.getMessage("PdfViewer.JAINotOnClasspathWarning")
                    + Messages.getMessage("PdfViewer.JAINotOnClasspathWarning1")
                    + Messages.getMessage("PdfViewer.JAINotOnClasspathWarning2");
                	
                	//hack for tableZoner
                	if(message.indexOf("PdfViewer")!=-1)
                		message="We recommend you add JAI to classpath";
                	
                    JEditorPane p = new JEditorPane("text/html",message);
                    p.setEditable(false);
                    p.setOpaque(false);
                    p.addHyperlinkListener( new HyperlinkListener() {
                        public void hyperlinkUpdate(HyperlinkEvent e) {
                            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                                try {
                                    BrowserLauncher.openURL("http://java.sun.com/products/java-media/jai/current.html");
                                } catch (IOException e1) {
                                    JOptionPane.showMessageDialog(null,Messages.getMessage("PdfViewer.ErrorWebsite"));
                                }
                            }
                        }
                    });

                    JOptionPane.showMessageDialog(null,p);
                }
                //<end-jfr>

            }

        }
    }

    public static boolean isJAIused() {
        return useJAI;
    }
    
    public static boolean isJAIOnClasspath() {
    	
    	if(!isJAIFound){ //already found so just return
        
    	//test
            try {
                Class.forName("javax.media.jai.JAI");
                isJAIFound=true;
                
                
            } catch (ClassNotFoundException e) {
            	
            }
    	}
    
    	return isJAIFound;
    }

    public static void useJAI(boolean use) {
        useJAI=use;
        JAI_TESTED=false; //make sure retested
    }
}
