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
* DefaultActionHandler.java
* ---------------
*/
package org.jpedal.objects.acroforms.actions;

import com.idrsolutions.pdf.acroforms.xfa.XFAFormObject;
import org.jpedal.PdfDecoder;
//<start-adobe><start-thin>
import org.jpedal.examples.simpleviewer.gui.swing.SwingMouseHandler;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.examples.simpleviewer.Values;
//<end-thin><end-adobe>
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.Javascript;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.acroforms.actions.privateclasses.FieldsHideObject;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.acroforms.rendering.DefaultAcroRenderer;
import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

import javax.swing.*;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;


public class DefaultActionHandler implements ActionHandler {
	
	final static private boolean showMethods = false;

	//shows the form you have hovered over with the mouse
	private static final boolean IdentifyForms = false;

	private PdfObjectReader currentPdfFile;

    private Javascript javascript;

	private AcroRenderer acrorend;

    private ActionFactory actionFactory;

    //handle so we can access
	private PdfDecoder decode_pdf;

	private int pageHeight,insetH;

	//flags to control reading of JS
	private boolean JSInitialised_A, JSInitialised_BI,JSInitialised_C, JSInitialised_D,
	JSInitialised_E,JSInitialised_F, JSInitialised_Fo, JSInitialised_K, JSInitialised_U,
	JSInitialised_V, JSInitialised_X;

	//<start-adobe><start-thin>
	private SwingMouseHandler swingMouseHandler;
	//<end-thin><end-adobe>

	public void init(PdfDecoder decode_pdf, Javascript javascript, AcroRenderer acrorend) {
		if(showMethods)
			System.out.println("DefaultActionHandler.init()");

		currentPdfFile = decode_pdf.getIO();
		this.javascript = javascript;
		this.acrorend = acrorend;
		this.decode_pdf = decode_pdf;

    }
	
	public void init(PdfObjectReader pdfFile, Javascript javascript, AcroRenderer acrorend) {
		if(showMethods)
			System.out.println("DefaultActionHandler.init()");

		currentPdfFile = decode_pdf.getIO();
		this.javascript = javascript;
		this.acrorend = acrorend;
		
    }

	public void setPageAccess(int pageHeight, int insetH) {
		if(showMethods)
			System.out.println("DefaultActionHandler.setPageAccess()");
		
		this.pageHeight=pageHeight;
		this.insetH=insetH;
	}

    public void setActionFactory(ActionFactory actionFactory) {
    	if(showMethods)
    		System.out.println("DefaultActionHandler.setActionFactory()");

        actionFactory.setPDF(decode_pdf,acrorend);
        this.actionFactory=actionFactory;

    }

    /**
	 * creates a returns an action listener that will change the down icon for each click
	 */
	public Object setupChangingDownIcon(Object downOff, Object downOn, int rotation) {
		if(showMethods)
			System.out.println("DefaultActionHandler.setupChangingDownIcon()");
		
		return actionFactory.getChangingDownIconListener(downOff, downOn, rotation);
	}

	/**
	 * sets up the captions to change as needed
	 */
	public Object setupChangingCaption(String normalCaption, String rolloverCaption, String downCaption) {
		if(showMethods)
			System.out.println("DefaultActionHandler.setupChangingCaption()");
		
		return new SwingFormButtonListener(normalCaption, rolloverCaption, downCaption);
	}
	
	public Object setHoverCursor(){
		if(showMethods)
			System.out.println("DefaultActionHandler.setHoverCursor()");
		
		return actionFactory.getHoverCursor();
		
	}

	/**
	 * sets up the specified action
	 */
	public Object setupXFAAction(int activity, String scriptType, String script) {
		if(showMethods)
			System.out.println("DefaultActionHandler.setupXFAAction()");
		
		switch (activity) {
		//NOTE: swingFormButtonListener should get called for all click,enter,exit,press,release.
		case XFAFormObject.ACTION_MOUSECLICK:
		case XFAFormObject.ACTION_MOUSEENTER:
		case XFAFormObject.ACTION_MOUSEEXIT:
		case XFAFormObject.ACTION_MOUSEPRESS:
		case XFAFormObject.ACTION_MOUSERELEASE:
			return new SwingFormButtonListener(activity, scriptType, script, acrorend);
		default:
		}
		return null;
	}
	
	/**
	 * A action when pressed in active area ?some others should now be ignored?
	 */
	public void A(Object raw, FormObject formObj, int eventType) {

        if(showMethods)
            System.out.println("DefaultActionHandler.A()");
        
		// new version
        PdfObject aData = null;
		if(eventType==MOUSERELEASED){
			//get the A action if we have activated the form (released)
			aData = formObj.getDictionary(PdfDictionary.A);
		}
		if(aData==null){
			aData = formObj.getDictionary(PdfDictionary.AA);
			if(aData!=null){
				if(eventType == MOUSEENTERED){
					aData = aData.getDictionary(PdfDictionary.E);
				}else if(eventType == MOUSEEXITED){
					aData = aData.getDictionary(PdfDictionary.X);
				}else if(eventType == MOUSEPRESSED){
					aData = aData.getDictionary(PdfDictionary.D);
				}else if(eventType == MOUSERELEASED){
					aData = aData.getDictionary(PdfDictionary.U);
				}
			}
		}

		//change cursor for each event
		actionFactory.setCursor(eventType);
		
		PdfArrayIterator Dest = formObj.getMixedArray(PdfDictionary.Dest);
		if (Dest!=null) { 
			if (eventType == MOUSECLICKED) {

	            //read all the values
				if (Dest.getTokenCount()>1) {
						
					//get page	and convert to target page
					String pageRef=Dest.getNextValueAsString(true);
					int pageNumber = decode_pdf.getPageFromObjectRef(pageRef);

					//get type of Dest
					//System.out.println("Next value as String="+Dest.getNextValueAsString(false)); //debug code to show actual value (note false so does not roll on)
					int type=Dest.getNextValueAsConstant(true);
					
					Rectangle position=null;

                    // - I have added all the keys for you and
                    //changed code below. If you run this on baseline,
                    //with new debug flag testActions on in DefaultAcroRender
                    // it will exit when it hits one
                    //not coded
					//@mark - todo with a couple of examples
					
					//type of Dest (see page 552 in 1.6Spec (Table 8.2) for full list)
                    switch(type){
                        case PdfDictionary.XYZ: //get X,y values and convert to rectangle which we store for later

                            //get x and y, (null will return 0)
                            float x=Dest.getNextValueAsFloat();
                            float y=Dest.getNextValueAsFloat();

                            //third value is zoom which is not implemented yet

                            //create Rectangle to scroll to
                            position=new Rectangle((int)x,(int)y,10,10);

                            break;
                        case PdfDictionary.Fit: //type sent in so that we scale to Fit.
                        	break;
                        	
                        case PdfDictionary.FitB: type = PdfDictionary.Fit; //scale to same as Fit so use Fit.
                        	break;
                        	
                        	/* [ page /FitH top ] - Display the page designated by page, with the vertical coordinate 
                        	 * top positioned at the top edge of the window and the contents of the page magnified 
                        	 * just enough to fit the entire width of the page within the window. A null value for 
                        	 * top specifies that the current value of that parameter is to be retained unchanged.
                        	 */
                        	
                        	/* [ page /FitV left ] - Display the page designated by page, with the horizontal 
                        	 * coordinate left positioned at the left edge of the window and the contents of 
                        	 * the page magnified just enough to fit the entire height of the page within the window. 
                        	 * A null value for left specifies that the current value of that parameter is to be 
                        	 * retained unchanged.
                        	 */
                        	
                        	/* [ page /FitR left bottom right top ] - Display the page designated by page, with its 
                        	 * contents magnified just enough to fit the rectangle specified by the coordinates left, 
                        	 * bottom, right, and topentirely within the window both horizontally and vertically. 
                        	 * If the required horizontal and vertical magnification factors are different, use 
                        	 * the smaller of the two, centering the rectangle within the window in the other 
                        	 * dimension. A null value for any of the parameters may result in unpredictable behavior.
                        	 */
                        default:

                    }
                    
					changeTo(null, pageNumber, position,type);
				}
            }
		}
		
        int subtype=formObj.getParameterConstant(PdfDictionary.Subtype);

		int popupFlag = formObj.getActionFlag();

		if (subtype == PdfDictionary.Sig) {

			additionalAction_Signature(formObj, eventType);

		} else if (popupFlag == FormObject.POPUP) {

            actionFactory.popup(raw,formObj,currentPdfFile);

		} else {
			// can get empty values
			if (aData == null)
				return;
			
			int command = aData.getNameAsConstant(PdfDictionary.S);

			// S is Name of action
			if (command != PdfDictionary.Unknown) {

				if (command == PdfDictionary.Named) {

					additionalAction_Named(eventType, aData);

                }else if(command==PdfDictionary.Goto || command==PdfDictionary.GoToR){

					additionalAction_Goto(formObj, eventType, command);

				} else if (command == PdfDictionary.ResetForm) {
					
					additionalAction_ResetForm();
					
				} else if (command == PdfDictionary.SubmitForm) {
					
					additionalAction_SubmitForm(aData);

				} else if (command == PdfDictionary.JavaScript) {


				} else if (command == PdfDictionary.Hide) {
					
					additionalAction_Hide(eventType, aData);

				} else if (command == PdfDictionary.URI) {

                    additionalAction_URI(eventType, aData.getTextStreamValue(PdfDictionary.URI));

				} else if (command == PdfDictionary.Launch) {

					//<start-thin><start-adobe>
					try {
						//get the F dictionary
				        PdfObject dict=aData.getDictionary(PdfDictionary.F);
				        
				        //System.out.println("dict="+dict+" "+dict.getObjectRefAsString());
				        
				        //then get the submit URL to use
				        if(dict!=null){
                            String target = dict.getTextStreamValue(PdfDictionary.F);

                            InputStream sourceFile = getClass().getResourceAsStream("/org/jpedal/res/"+target);

                            if(sourceFile==null){
                                JOptionPane.showMessageDialog(decode_pdf,"Unable to locate "+target);
                            }else{
                                //System.out.printl("name="+getClass().getResource("/org/jpedal/res/"+target).get);

                                //get name without path
                                int ptr=target.lastIndexOf("/");
                                if(ptr!=-1)
                                    target=target.substring(ptr+1);

                                File output=new File(ObjectStore.temp_dir+target);
                                output.deleteOnExit();

                                ObjectStore.copy(new BufferedInputStream(sourceFile),
                                        new BufferedOutputStream(new FileOutputStream(output)));

                                if(target.endsWith(".pdf")){

                                    try{
                                        
                                        SimpleViewer viewer=new SimpleViewer(Values.RUNNING_NORMAL);
                                        SimpleViewer.exitOnClose=false;
                                        viewer.setupViewer(ObjectStore.temp_dir+target);

                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                }else if(PdfDecoder.isRunningOnMac){
                                    target="open "+ObjectStore.temp_dir+target;

                                    // System.out.println("target="+target);

                                    Process p=Runtime.getRuntime().exec(target);
                                    /**String line="";

                                     BufferedReader input =
                                     new BufferedReader
                                     (new InputStreamReader(p.getInputStream()));
                                     while ((line = input.readLine()) != null) {
                                     System.out.println(line);
                                     }
                                     input.close();

                                     System.out.println("target="+target);
                                     /**/
                                }
                            }

                        }
					} catch (Exception e1) {
						e1.printStackTrace();						
					} catch (Error err) {
						err.printStackTrace();
					}
					
					//<end-adobe><end-thin>
					
                    LogWriter.writeFormLog("{stream} launch activate action NOT IMPLEMENTED", FormStream.debugUnimplemented);

				} else if (command == PdfDictionary.SetOCGState) {

					additionalAction_OCState(eventType, aData);

				} else if (command == PdfDictionary.Sound) {


				} else {
                    LogWriter.writeFormLog("{stream} UNKNOWN Command "+aData.getName(PdfDictionary.S)+" Action", FormStream.debugUnimplemented);
				}
			} else if(command!=-1){
                LogWriter.writeFormLog("{stream} Activate Action UNKNOWN command "+aData.getName(PdfDictionary.S)+" "+formObj.getObjectRefAsString(), FormStream.debugUnimplemented);
			}
		}
	}

	private void additionalAction_OCState(int eventType, PdfObject aData) {
		if (eventType == MOUSECLICKED) {

			PdfArrayIterator state = aData.getMixedArray(PdfDictionary.State);

			if (state != null && state.getTokenCount() > 0) {

				final PdfLayerList layers = decode_pdf.getLayers();

				int count = state.getTokenCount();

				final int action = state.getNextValueAsConstant(true);
				String ref;
				for (int jj = 1; jj < count; jj++) {
					ref = state.getNextValueAsString(true);

					final String layerName = layers.getNameFromRef(ref);

					// toggle layer status when clicked
					Runnable updateAComponent = new Runnable() {
						public void run() {
							// force refresh
							decode_pdf.invalidate();
							decode_pdf.updateUI();
							decode_pdf.validate();

							// update settings on display and in PdfDecoder
							boolean newState;
							if (action == PdfDictionary.Toggle)
								newState = !layers.isVisible(layerName);
							else if (action == PdfDictionary.OFF)
								newState = false;
                            else //must be ON
								newState = true;

							layers.setVisiblity(layerName, newState);

							// decode again with new settings
							try {
								decode_pdf.decodePage(-1);
							} catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
							}
						}
					};

					SwingUtilities.invokeLater(updateAComponent);
				}
			}
		}
	}

	private void additionalAction_Named(int eventType, PdfObject aData) {
		int name = aData.getNameAsConstant(PdfDictionary.N);

		if (name == PdfDictionary.Print) {
			additionalAction_Print(eventType);
		} else {


		}
	}
	
	private void additionalAction_URI(int eventType, String url) {
		
		if (showMethods)
			System.out.println("DefaultActionHandler.additionalAction_URI()");

		if (eventType == MOUSECLICKED) {
			try {
				BrowserLauncher.openURL(url);
			} catch (IOException e1) {
				actionFactory.showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
			}
		} else
			actionFactory.setCursor(eventType);
	}

	private void additionalAction_Hide(int eventType, PdfObject aData) {
		if (showMethods)
			System.out.println("DefaultActionHandler.additionalAction_Hide()");

			FieldsHideObject fieldsToHide = new FieldsHideObject();
	        
			getHideMap(aData, fieldsToHide);

			actionFactory.setFieldVisibility(fieldsToHide);
	}

	private void additionalAction_SubmitForm(PdfObject aData) {
		if(showMethods)
			System.out.println("DefaultActionHandler.additionalAction_SubmitForm()");
		
		boolean newExcludeList=false;
		String newSubmitURL=null;
		String[] newListOfFields=null;
		
		//get the F dictionary
        PdfObject dict=aData.getDictionary(PdfDictionary.F);
        //then get the submit URL to use
        if(dict!=null)
		newSubmitURL = dict.getTextStreamValue(PdfDictionary.F);
        
		//get the fields we need to change
		PdfArrayIterator fieldList = aData.getMixedArray(PdfDictionary.Fields);
		if (fieldList != null) {
			if (fieldList.getTokenCount() < 1)
				fieldList = null;

			if (fieldList != null) {
				// code goes here
				int fieldIndex = 0;
				newListOfFields = new String[fieldList.getTokenCount()];

				// go through list of fields and store so we can send
				String formObject;
				String tok, preName = null;
				StringBuffer names = new StringBuffer();
				while (fieldList.hasMoreTokens()) {
					formObject = fieldList.getNextValueAsString(true);

					if (formObject.indexOf(".x") != -1) {
						preName = formObject.substring(formObject.indexOf('.') + 1,
								formObject.indexOf(".x") + 1);
					}
					if (formObject.indexOf(" R") != -1) {

                       FormObject formObj=new FormObject(formObject);
                       currentPdfFile.readObject(formObj);


                        tok=formObj.getTextStreamValue(PdfDictionary.T);
                           if (preName != null) {
							names.append(preName);
						}
						names.append(tok);
						names.append(',');

					}
				}

				newListOfFields[fieldIndex++] = names.toString();
			}// end of code section
		}// END of Fields defining
		
		//if there was a list of fields read the corresponding Flags see pdf spec v1.6 p662
		if (newListOfFields != null) {
			// if list is null we ignore this flag anyway
			int flags = aData.getInt(PdfDictionary.Flags);

			if ((flags & 1) == 1) {
				// fields is an exclude list
				newExcludeList = true;
			}
		}// END of if exclude list ( Flags )
		
		// send our values to the actioning method
		actionFactory.submitURL(newListOfFields, newExcludeList, newSubmitURL);
	}

	private void additionalAction_ResetForm() {
		if (showMethods)
    		System.out.println("DefaultActionHandler.additionalAction_ResetForm()");

		actionFactory.reset();
	}

    private void additionalAction_Goto(FormObject formObj, int eventType, int type) {
		if (showMethods)
			System.out.println("DefaultActionHandler.additionalAction_Goto()");

		// S /Goto or /GoToR action is a goto remote file action,
		// F specifies the file (GoToR only)
		// D specifies the location or page

		if (eventType == MOUSECLICKED) {

			// new version - read Page Object to jump to
			String pageRef = "";

			PdfObject aData = formObj.getDictionary(PdfDictionary.A);
			if (aData != null) {

                //MArk where has the DEST come from??
                //@chris - its the D parameter on the A dictionary p616
                //I've called it Dest internally because it is a Dest.
				PdfArrayIterator D = aData.getMixedArray(PdfDictionary.Dest);

				if (D != null && D.getTokenCount() > 0)
					pageRef = D.getNextValueAsString(true);

			}
			/**/
			if (type == PdfDictionary.Goto) {
				
				int pageNumber = decode_pdf.getPageFromObjectRef(pageRef);

				//CHRIS todo potentially can recover the location of the goto using FitH and FitR values
				changeTo(null, pageNumber, null,type);
			} else if(type == PdfDictionary.GoToR){
//				if (eventType == MOUSECLICKED) {
//					//A /GoToR action is a goto remote file action,
//					//F specifies the file
//					//D specifies the location or page
//
//					String stpage = (String) aDataMap.get("D");
////					Map dataMap = (Map) currentPdfFile.resolveToMapOrString("F", aDataMap.get("F"));
//					Map dataMap = (Map) aDataMap.get("F");
//					String type = (String) dataMap.get("Type");
//
//					String file = (String) dataMap.get("F");
//					if (file.startsWith("(")) {
//						file = file.substring(1, file.length() - 1);
//					}
//
//					if (stpage.startsWith("(")) {
//						stpage = stpage.substring(1, stpage.length() - 1);
//					}
//
//					int page;
//					int index = stpage.indexOf("P.");
//					if (index != -1) {
//						stpage = stpage.substring(index + 2, stpage.length());
//						page = Integer.parseInt(stpage);
//					} else if (stpage.equals("F")) {
//						//use file only
//						page = 1;
//					} else {
//						page = 1;
//					}
//
//					if (type.equals("/Filespec")) {
//						if (file.startsWith("./")) {
//							file = new File(file.substring(2, file.length())).getAbsolutePath();
//						}
//						if (file.startsWith("../")) {
//							String tmp = new File("").getAbsolutePath();
//							file = tmp.substring(0, tmp.lastIndexOf('\\') + 1) + file.substring(3, file.length());
//						}
//
//						if (new File(file).exists()) {
//
//							//Open this file, on page 'page'
//							changeTo(file, page, null);
//
//							LogWriter.writeFormLog("{DefaultActionHamdler.A} Form has GoToR command, needs methods for opening new file on page specified", FormStream.debugUnimplemented);
//						} else {
//							actionFactory.showMessageDialog("The file specified " + file + " Does Not Exist!");
//						}
//					} else {
//						LogWriter.writeFormLog("{CustomMouseListener.mouseClicked} GoToRemote NON Filespec NOT IMPLEMENTED", FormStream.debugUnimplemented);
//					}
//
//					//				((JComponent)currentComp).setToolTipText(text);
//				}
			}else {
			}
		} else
			actionFactory.setCursor(eventType);

	}

	private void additionalAction_Print(int eventType) {
		if (showMethods)
			System.out.println("DefaultActionHandler.additionalAction_Print()");

		if (eventType == MOUSERELEASED)
			actionFactory.print();

	}

	/**
	 * display signature details in popup frame
	 * @param formObj
	 * @param eventType
	 */
	private void additionalAction_Signature(FormObject formObj, int eventType) {
		if (showMethods)
    		System.out.println("DefaultActionHandler.additionalAction_Signature()");

		if (eventType == MOUSECLICKED) {

            PdfObject sigObject=formObj.getDictionary(PdfDictionary.V);//.getDictionary(PdfDictionary.Sig);

			if (sigObject == null)
				return;

			actionFactory.showSig(sigObject);

		} else
			actionFactory.setCursor(eventType);
	}

	/**
	 * this calls the PdfDecoder to open a new page and change to the correct page and location on page,
	 * is any value is null, it means leave as is.
	 * @param type - the type of action
	 */
	public void changeTo(String file, int page, Object location, int type) {
		
		if (showMethods)
			System.out.println("DefaultActionHandler.changeTo()");

		// open file 'file'
		if (file != null) {
			try {
				decode_pdf.openPdfFile(file);
			} catch (Exception e) {
			}
		}

		// change to 'page'
		if (page != -1) {
			//@mark re decode page numbers 
			//we should use +1 as we reference pages from 1.
			if(decode_pdf.getPageCount()!=1 && decode_pdf.getlastPageDecoded()!=page){
				if (page > 0 && page < decode_pdf.getPageCount()+1) {
					try {
						decode_pdf.decodePage(page);
	
						decode_pdf.updatePageNumberDisplayed(page);
					} catch (Exception e) {
						e.printStackTrace();
					}
	
					/** reset as rotation may change! */
					decode_pdf.setPageParameters(-1, page);
	
				}
			}
		}

		actionFactory.setPageandPossition(location);
		
		if(type== PdfDictionary.Fit){

        	/**
        	 * Display the page designated by page, with its contents magnified just enough to 
        	 * fit the entire page within the window both horizontally and vertically. 
        	 * If the required horizontal and vertical magnification factors are different, 
        	 * use the smaller of the two, centering the page within the window in the other 
        	 * dimension.
        	 */
			//<start-adobe><start-thin>
            //now available via callback
        	Object swingGUI = this.decode_pdf.getExternalHandler(org.jpedal.external.Options.SwingContainer);

            //set to fit - please use full paths (we do not want in imports as it will break Adobe version)
            if(swingGUI!=null){
            	//set scaling box to 0 index, which is scale to window
                ((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).setSelectedComboIndex(org.jpedal.examples.simpleviewer.Commands.SCALING,0);
                ((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).zoom(true);
            }
            //<end-thin><end-adobe>

            //we need to zoom to fit, horizontally and vertically, I cant figure out how you zoom. chris.
		}else if(type == PdfDictionary.FitWidth){
			//<start-adobe><start-thin>
            //now available via callback
        	Object swingGUI = this.decode_pdf.getExternalHandler(org.jpedal.external.Options.SwingContainer);

            //set to fit - please use full paths (we do not want in imports as it will break Adobe version)
            if(swingGUI!=null){
            	//set scaling box to 0 index, which is scale to window
                ((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).setSelectedComboIndex(org.jpedal.examples.simpleviewer.Commands.SCALING,2);
                ((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).zoom(true);
            }
            //<end-thin><end-adobe>
		}else if(type == PdfDictionary.FitHeight){
			//<start-adobe><start-thin>
            //now available via callback
        	Object swingGUI = this.decode_pdf.getExternalHandler(org.jpedal.external.Options.SwingContainer);

            //set to fit - please use full paths (we do not want in imports as it will break Adobe version)
            if(swingGUI!=null){
            	//set scaling box to 0 index, which is scale to window
                ((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).setSelectedComboIndex(org.jpedal.examples.simpleviewer.Commands.SCALING,1);
                ((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).zoom(true);
            }
            //<end-thin><end-adobe>
		}
	}

    public PdfDecoder getPDFDecoder() {
        return decode_pdf;
    }

	/**
	 * E action when cursor enters active area
	 */
	public void E(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.E()");
		
	}

	/**
	 * X action when cursor exits active area
	 */
	public void X(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.X()");

	}

	/**
	 * D action when cursor button pressed inside active area
	 */
	public void D(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.D()");

	}

	/**
	 * U action when cursor button released inside active area
	 */
	public void U(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.U()");

	}

	/**
	 * Fo action on input focus
	 */
	public void Fo(Object e, FormObject formObj) {     //TODO called with focus gained
		if (showMethods)
			System.out.println("DefaultActionHandler.Fo()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * Bl action when input focus lost
	 */
	public void Bl(Object e, FormObject formObj) { // TODO called by focus lost
		if (showMethods)
			System.out.println("DefaultActionHandler.Bl()");

	}

	/**
	 * O called when a page is opened
	 */
	public void O(int pageNumber) {
		if (showMethods)
			System.out.println("DefaultActionHandler.O()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PO action when page containing is opened,
	 * actions O of pages AA dic, and OpenAction in document catalog should be done first
	 */
	public void PO(int pageNumber) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PO()");

		Map POaction = null;// hack as not yet implemented


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PC action when page is closed, action C from pages AA dic follows this
	 */
	public void PC(int pageNumber) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PC()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PV action on viewing containing page
	 */
	public void PV(int pageNumber) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PV()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PI action when no longer visible in viewer
	 */
	public void PI(int pageNumber) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PI()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * when user types a keystroke
	 * K action on - [javascript]
	 * keystroke in textfield or combobox
	 * modifys the list box selection
	 * (can access the keystroke for validity and reject or modify)
	 */
	public int K(Object ex, FormObject formObj, int actionID) {
		if (showMethods)
			System.out.println("DefaultActionHandler.K()");

		int result = 0;


		return result;
	}

	/**
	 * F the display formatting of the field (e.g 2 decimal places) [javascript]
	 */
	public void F(FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.F()");

	}

	/**
	 * V action when fields value is changed [javascript]
	 */
	public void V(Object ex, FormObject formObj, int actionID) {
		if (showMethods)
			System.out.println("DefaultActionHandler.V()");
		
		//set this fields value within the FormObject so javascript actions are correct
		//String fieldRef = formObj.getPDFRef();
		String fieldRef = formObj.getObjectRefAsString();
		acrorend.getCompData().storeDisplayValue(fieldRef);

	}

	/**
	 * C action when another field changes (recalculate this field) [javascript]
	 * <p/>
	 * NOT actually called as called from other other objects but here for completeness
	 */
	public void C(FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.C()");
		
	}

	private String removeBrackets(String text) {
		if (text.startsWith("(") || text.startsWith("[") || text.startsWith("{")) {

			if (text.endsWith(")"))
				return text.substring(1, text.length() - 1);
			else
				return text.substring(1, text.length() - 2);
		} else {
			return text;
		}
	}

	/**
	 * goes through the map and adds the required data to the hideMap and returns it
	 */
	private void getHideMap(PdfObject aData, final FieldsHideObject fieldToHide) {
		if (showMethods)
			System.out.println("DefaultActionHandler.getHideMap()");
		
		String[] fieldstoHide = fieldToHide.getFieldArray();
		boolean[] whethertoHide = fieldToHide.getHideArray();
		
		if (aData.getTextStreamValue(PdfDictionary.T) != null) {
			String fieldList = aData.getTextStreamValue(PdfDictionary.T);
			if(fieldList!=null){
				String[] fields;
				if (fieldstoHide.length>0){
					fields = new String[fieldstoHide.length + 1];
					System.arraycopy(fieldstoHide, 0, fields, 0, fieldstoHide.length);
					fields[fields.length - 1] = fieldList;
				} else {
					fields = new String[]{fieldList};
				}
				fieldstoHide = fields;
			}
		}
		
		boolean hideFlag = aData.getBoolean(PdfDictionary.H);
		
		boolean[] hideFlags;
		if (whethertoHide.length>0){
			hideFlags = new boolean[whethertoHide.length + 1];
			System.arraycopy(whethertoHide, 0, hideFlags, 0, whethertoHide.length);
			hideFlags[hideFlags.length - 1] = hideFlag;
		} else {
			hideFlags = new boolean[] { hideFlag };
		}
		whethertoHide = hideFlags;
		
		//put values back into fields to hide object
		//@mark - do you think this is a good way to store the fields and hide actions, or can you think of a better way?
		fieldToHide.setFieldArray(fieldstoHide);
		fieldToHide.setHideArray(whethertoHide);
		
		if (aData.getDictionary(PdfDictionary.Next)!=null) {
			PdfObject nextDic = aData.getDictionary(PdfDictionary.Next);
			getHideMap(nextDic, fieldToHide);
		}
	}

	public PdfLayerList getLayerHandler() {
		return decode_pdf.getLayers();

	}

	//<start-adobe><start-thin>
	public void setMouseHandler(SwingMouseHandler swingMouseHandler) {
		this.swingMouseHandler = swingMouseHandler;
	}
	
	public void updateCordsFromFormComponent(MouseEvent e, boolean mouseClicked) {
		
		if(swingMouseHandler!=null){
			swingMouseHandler.updateCordsFromFormComponent(e);
			swingMouseHandler.checkLinks(mouseClicked,decode_pdf.getIO());
		}
	}
	//<end-thin><end-adobe>
	
}
