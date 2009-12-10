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
 * SwingData.java
 * ---------------
 */
package org.jpedal.objects.acroforms.formData;

import org.jpedal.PdfDecoder;
import org.jpedal.external.CustomFormPrint;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.constants.ErrorCodes;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.overridingImplementations.FixImageIcon;
import org.jpedal.objects.acroforms.overridingImplementations.PdfSwingPopup;
import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.objects.acroforms.utils.FormUtils;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Strip;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.util.*;
import java.util.List;

/**
 * Swing specific implementation of Widget data
 * (all non-Swing variables defined in ComponentData)
 * 
 */
public class SwingData extends ComponentData implements GUIData {

	private static final boolean tryNewSortRoutine = true;

    CustomFormPrint customFormPrint=null;

	/**
	 * array to hold components
	 */
	private Component[] allFields;

	/**
	 * flag to show if checked
	 */
	private boolean[] testedForDuplicates;

	/**
	 * possible wrapper for some components such as JList which do not have proper scrolling
	 */
	private JScrollPane[] scroll;

	/**
	 * panel components attached to
	 */
	private JPanel panel;

	/** we store Buttongroups in this */
	private Map annotBgs = new HashMap();

	/**
	 * generic call used for triggering repaint and actions
	 */
	public void loseFocus() {
		/*if (panel != null) {
			if (SwingUtilities.isEventDispatchThread())
				panel.grabFocus();
			else {
				final Runnable doPaintComponent = new Runnable() {
					public void run() {
						panel.grabFocus();
					}
				};
				SwingUtilities.invokeLater(doPaintComponent);
			}
		}*/
	}

	/**
	 * set the type of object for each Form created
	 * @param fieldName
	 * @param type
	 */
	public void setFormType(String fieldName, Integer type) {

		typeValues.put(fieldName, type);

	}

	/**
	 * return components which match object name
	 * @return
	 */
	private Object[] getComponentsByName(String objectName, Object checkObj) {

		// allow for duplicates
		String duplicateComponents = (String) duplicates.get(objectName);

		int index = ((Integer) checkObj).intValue();

		boolean moreToProcess = true;
		int firstIndex = index;
		while (moreToProcess) {
			if (index + 1 < allFields.length && allFields[index + 1] != null) {

				String name = allFields[index + 1].getName();
				if(name==null){	//we now pass Annots through so need to allow for no name
					moreToProcess = false;
				} else if (FormUtils.removeStateToCheck(name, false).equals(objectName)) {
					index += 1;
				} else {
					moreToProcess = false;
				}
			} else
				moreToProcess = false;
		}

		int size = (index + 1) - firstIndex;
		Component[] compsToRet = new Component[size];

		for (int i = 0; i < size; i++, firstIndex++) {
			compsToRet[i] = allFields[firstIndex];
			if (firstIndex == index)
				break;
		}

		// recreate list and add in any duplicates
		if (duplicateComponents != null && duplicateComponents.indexOf(',') != -1) {

			StringTokenizer additionalComponents = new StringTokenizer(duplicateComponents, ",");

			int count = additionalComponents.countTokens();

			Component[] origComponentList = compsToRet;
			compsToRet = new Component[size + count];

			// add in original components
			System.arraycopy(origComponentList, 0, compsToRet, 0, size);

			// and duplicates
			for (int i = 0; i < count; i++) {
				int ii = Integer.parseInt(additionalComponents.nextToken());

				// System.out.println(ii+" "+count);
				compsToRet[i + size] = allFields[ii];
			}
		}

		return compsToRet;
	}

	/**
	 * get value using objectName or field pdf ref.
	 * Pdf ref is more acurate
	 */
	public Object getValue(Object objectName) {

		if (objectName == null)
			return "";

		Object checkObj;
		if (((String) objectName).indexOf("R") != -1) {
			checkObj = refToCompIndex.get(objectName);
		} else {
			checkObj = nameToCompIndex.get(objectName);
		}

		Object retValue = "";
		retValue = getFormValue(checkObj);

		return retValue;

	}
	
	/**valid flag used by Javascript to allow rollback
	 * &nbsp
	 * @param ref - name of the field to change the value of
	 * @param value - the value to change it to
	 * @param isValid - is a valid value or not
	 * @param isFormatted - is formatted properly
	 * @param reset - can be reset, ie if false we store for rollback if it is successfully formatted or validated
	 */
	public void setValue(String ref, Object value, boolean isValid, boolean isFormatted, boolean reset) {
		if(ComponentData.newSetValueCode){
			Object checkObj = super.setValue(ref, value, isValid, isFormatted, reset,getValue(ref));
			
			//set the display fields value
			if(checkObj!=null)
				setFormValue(value, checkObj);
		}else {
			// track so we can reset if needed
			if (!reset && isValid) {
				lastValidValue.put(ref, value);
			}
			// save raw version before we overwrite
			if (!reset && isFormatted) {
				lastUnformattedValue.put(ref, getValue(ref));
			}

			Object checkObj;
			if (ref.indexOf("R") != -1) {
				checkObj = refToCompIndex.get(ref);
			} else {
				checkObj = nameToCompIndex.get(ref);
			}
			
			//Fix null exception in /PDFdata/baseline_screens/forms/406302.pdf
			if(checkObj==null)
				return ;
			
			//set the display fields value
			setFormValue(value, checkObj);
			
			// Now set the formObject value so we keep track of the current field value within our FormObject
			String pdfRef = convertIDtoRef(((Integer)checkObj).intValue());
			FormObject form = ((FormObject)rawFormData.get(pdfRef));
			form.setValue((String) value);
		}
	}

	public String getComponentName(int currentComp, ArrayList nameList, String lastName) {

		String currentName;

		Component currentField = allFields[currentComp];

		if (currentField != null) {
			// ensure following fields don't get added if (e.g they are a group)

			currentName = FormUtils.removeStateToCheck(currentField.getName(), false);
			if (currentName != null && !lastName.equals(currentName)) {

				if (!testedForDuplicates[currentComp]) {
					// stop multiple matches
					testedForDuplicates[currentComp] = true;

					// track duplicates
					String previous = (String) duplicates.get(currentName);
					if (previous != null)
						duplicates.put(currentName, previous + ',' + currentComp);
					else
						duplicates.put(currentName, String.valueOf(currentComp));
				}

				// add to list
				nameList.add(currentName);
				lastName = currentName;
			}
		}
		return lastName;
	}

	public Object[] getComponentsByName(String objectName) {

		if (objectName == null)
			return allFields;

		Object checkObj = nameToCompIndex.get(objectName);
		if (checkObj == null)
			return null;

		if (checkObj instanceof Integer) {

			// return allFields[index];
			return getComponentsByName(objectName, checkObj);
		} else {
			LogWriter.writeLog("{stream} ERROR DefaultAcroRenderer.getComponentByName() Object NOT Integer and NOT null");

			return null;
		}
	}

	public Object getFormValue(Object checkObj) {

		Object retValue = "";

		if (checkObj != null) {
			int index = ((Integer) checkObj).intValue();

			if (allFields[index] instanceof JCheckBox)
				retValue = Boolean.valueOf(((JCheckBox) allFields[index]).isSelected());
			else if (allFields[index] instanceof JComboBox)
				retValue = ((JComboBox) allFields[index]).getSelectedItem();
			else if (allFields[index] instanceof JList)
				retValue = ((JList) allFields[index]).getSelectedValues();
			else if (allFields[index] instanceof JRadioButton)
				retValue = Boolean.valueOf(((JRadioButton) allFields[index]).isSelected());
			else if (allFields[index] instanceof JTextComponent)
				retValue = ((JTextComponent) allFields[index]).getText();
			else {

				retValue = "";
			}
		}
		return retValue;
	}

	/** sets the value for the displayed form */
	public void setFormValue(Object value, Object checkObj) {

		if (checkObj != null) {
			int index = ((Integer) checkObj).intValue();

			if (allFields[index] instanceof JCheckBox)
				((JCheckBox) allFields[index]).setSelected(Boolean.valueOf((String) value).booleanValue());
			else if (allFields[index] instanceof JComboBox)
				((JComboBox) allFields[index]).setSelectedItem(value);
			else if (allFields[index] instanceof JList)
				((JList) allFields[index]).setSelectedValue(value, false);
			else if (allFields[index] instanceof JRadioButton)
				((JRadioButton) allFields[index]).setText((String) value);
			else if (allFields[index] instanceof JTextComponent)
				((JTextComponent) allFields[index]).setText((String) value);
			else {
			}
		}
	}

	public void showForms() {

		if (allFields != null) {
			for (int i = 0; i < allFields.length; i++) {
				if (allFields[i] != null) {
					if (allFields[i] instanceof JButton) {
						allFields[i].setBackground(Color.blue);
					} else if (allFields[i] instanceof JTextComponent) {
						allFields[i].setBackground(Color.red);
					} else {
						allFields[i].setBackground(Color.green);
					}

					allFields[i].setForeground(Color.lightGray);
					allFields[i].setVisible(true);
					allFields[i].setEnabled(true);
					((JComponent) allFields[i]).setOpaque(true);
					if (allFields[i] instanceof AbstractButton) {
						if (!(allFields[i] instanceof JRadioButton)) {
							((AbstractButton) allFields[i]).setIcon(null);
						}
					} else if (allFields[i] instanceof JComboBox) {
						((JComboBox) allFields[i]).setEditable(false);
					}
				}
			}
		}
	}

	/**
	 * get actual widget using objectName as ref or null if none
	 * @param objectName
	 * @return
	 */
	public Object getWidget(Object objectName) {

		if (objectName == null)
			return null;
		else {

			Object checkObj;
			if (((String) objectName).indexOf("R") != -1) {
				checkObj = refToCompIndex.get(objectName);
			} else {
				checkObj = nameToCompIndex.get(objectName);
			}

			if (checkObj == null)
				return null;
			else {
				int index = ((Integer) checkObj).intValue();

				return allFields[index];
			}
		}
	}

	/**
	 * render component onto G2 for print of image creation
	 */
	private void renderComponent(Graphics2D g2, int currentComp, Component comp, int rotation, int accDisplay) {
		// if (showMethods)
		// System.out.println("DefaultAcroRenderer.renderComponent()");

		if (comp != null) {

			boolean editable = false;

			if (comp instanceof JComboBox) {
				if (((JComboBox) comp).isEditable()) {
					editable = true;
					((JComboBox) comp).setEditable(false);
				}

				/**fix for odd bug in Java when using Windows l & f - might need refining*/
				if (!UIManager.getLookAndFeel().isNativeLookAndFeel()) {

					if (((JComboBox) comp).getComponentCount() > 0)
						renderComponent(g2, currentComp, ((JComboBox) comp).getComponent(0), rotation, accDisplay);
				}else if(PdfDecoder.isRunningOnMac){ //hack for OS X (where have we heard that before)
					if (((JComboBox) comp).getComponentCount() > 0) {

						JComboBox combo = ((JComboBox) comp);

						Object selected = combo.getSelectedItem();
						if (selected != null) {

							JTextField text = new JTextField();

							text.setText(combo.getSelectedItem().toString());

							text.setBackground(combo.getBackground());
							text.setForeground(combo.getForeground());
							text.setFont(combo.getFont());

							text.setBorder(null);

							renderComponent(g2, currentComp, text, rotation, accDisplay);
						} else
							renderComponent(g2, currentComp, ((JComboBox) comp).getComponent(0), rotation, accDisplay);
					}
				}

			}

			scaleComponent(currentPage, 1, rotation, currentComp, comp, false,false);

			AffineTransform ax = g2.getTransform();
			g2.translate(comp.getBounds().x - insetW, comp.getBounds().y + cropOtherY[currentPage]);
			
			comp.paint(g2);
			
			g2.setTransform(ax);

			if (editable /* && comp instanceof JComboBox */) {
				((JComboBox) comp).setEditable(true);
			}
		}
	}

	boolean renderFormsWithJPedalFontRenderer = false;

	private Component[] popups = new Component[0];
	
	/** for if we add a popup to the panel, could be used for adding other objects*/
	private boolean forceRedraw = false;
	
	/**
	 * draw the forms onto display for print of image. Note different routine to
	 * handle forms also displayed at present
	 */
	public void renderFormsOntoG2(Object raw, int pageIndex, float scaling,
                                  int rotation, int accDisplay, Map componentsToIgnore,
                                  FormFactory formFactory) {

        this.componentsToIgnore=componentsToIgnore;


        //only passed in on print so also used as flag
        boolean isPrinting=formFactory!=null;

		Graphics2D g2 = (Graphics2D) raw;

		AffineTransform defaultAf = g2.getTransform();

		// setup scaling
		AffineTransform aff = g2.getTransform();
		aff.scale(1, -1);
		aff.translate(0, -pageHeight - insetH);
		g2.setTransform(aff);

		// remove so don't appear rescaled on screen
		// if((currentPage==pageIndex)&&(panel!=null))
//		this.removeDisplayComponentsFromScreen(panel);//removed to stop forms disappearing on printing

		try {
				
			Component[] formComps = allFields;

			/** needs to go onto a panel to be drawn */
			JPanel dummyPanel = new JPanel();
			
			//get unsorted components and iterate over forms
            Iterator formsUnsortedIterator = formsUnordered[pageIndex].iterator();
            while(formsUnsortedIterator.hasNext()){
            	
            	//get ref from list and convert to index.
            	Object nextVal = formsUnsortedIterator.next();
            	int currentComp = ((Integer)refToCompIndex.get(nextVal)).intValue();
            	
            	
				if (formComps != null && currentComp != -1) {

					// disable indent while we print
					int tempIndent = indent;
					indent = 0;

					Component comp = formComps[currentComp];

					if (comp != null && comp.isVisible()) {

						//wrap JList in ScrollPane to ensure displayed if size set to smaller than list
						// (ie like ComboBox)
						//@note - fixed by moving the selected item to the top of the list. 
						// this works for the file acro_func_baseline1.pdf
						//and now works on file fieldertest2.pdf and on formsmixed.pdf
						//but does not render correct in tests i THINK, UNCONFIRMED
						// leaves grayed out boxes in renderer.

						float boundHeight = boundingBoxs[currentComp][3]-boundingBoxs[currentComp][1];
						int swingHeight = comp.getPreferredSize().height;
						/**
						 * check if the component is a jlist, and if it is, then is their a selected item, 
						 * if their is then is the bounding box smaller than the jlist actual size
						 * then and only then do we need to change the way its printed
						 */
						if (renderFormsWithJPedalFontRenderer) {

							// get correct key to lookup form data
							String ref = this.convertIDtoRef(currentComp);


							//System.out.println(currentComp+" "+comp.getLocation()+" "+comp);
							Object rawForm = this.getRawForm(ref);

							if (rawForm instanceof FormObject) {
								FormObject form = (FormObject) rawForm;
								System.out.println(ref+" "+form.getTextFont()+" "+form.getTextString());
							}
                        }else if(isFormNotPrinted(currentComp)){
						}else if(comp instanceof JList && ((JList)comp).getSelectedIndex()!=-1 && boundHeight<swingHeight){

							JList comp2 = (JList) comp;

							dummyPanel.add(comp);

//								JList tmp = comp2;//((JList)((JScrollPane)comp).getViewport().getComponent(0));
							ListModel model = comp2.getModel();
							Object[] array = new Object[model.getSize()];

							int selectedIndex = comp2.getSelectedIndex();
							int c = 0;
							array[c++] = model.getElementAt(selectedIndex);

							for (int i = 0; i < array.length; i++) {
								if (i != selectedIndex)
									array[c++] = model.getElementAt(i);
							}

							comp2.setListData(array);
							comp2.setSelectedIndex(0);

							try {
								renderComponent(g2, currentComp,comp2,rotation,accDisplay);
								dummyPanel.remove(comp2);
							} catch (Exception cc) {

							}
                            
						} else { //if printing improve quality on AP images
							FormObject form=null;

							boolean customPrintoverRide=false;
							if(customFormPrint!=null)
								customPrintoverRide=customFormPrint.print(g2, currentComp, comp, this);

							if(isPrinting){

								if(!customPrintoverRide){

									try{
										// get correct key to lookup form data
										String ref = this.convertIDtoRef(currentComp);


										//System.out.println(currentComp+" "+comp.getLocation()+" "+comp);
										Object rawForm = this.getRawForm(ref);

										if (rawForm instanceof FormObject) {

                                            form=(FormObject)rawForm;

                                            if (form.isAppearancesUsed()){

                                                int subtype=form.getParameterConstant(PdfDictionary.Subtype);

                                                //hi res version to print
                                                //formFactory.setAPScaling(4);

                                                if(subtype==PdfDictionary.Sig){
                                                    comp= (Component) formFactory.signature(form);
                                                }/**else if(subtype==PdfDictionary.Tx){
                                                }else if(subtype==PdfDictionary.Btn){
                                                    boolean[] flags = form.getFieldFlags();

                                                    boolean isPushButton = false, isRadio = false, hasNoToggleToOff = false, radioinUnison = false;
                                                    if (flags != null) {
                                                        isPushButton = flags[FormObject.PUSHBUTTON_ID];
                                                        isRadio = flags[FormObject.RADIO_ID];
                                                        hasNoToggleToOff = flags[FormObject.NOTOGGLETOOFF_ID];
                                                        radioinUnison = flags[FormObject.RADIOINUNISON_ID];
                                                    }

                                                    if (isPushButton) {

                                                        comp = (Component) formFactory.pushBut(form);

                                                    }else if(isRadio){
                                                        comp = (Component)formFactory.radioBut(form);
                                                    }else {
                                                        comp = (Component) formFactory.checkBoxBut(form);
                                                    }
                                                }else{
                                                    System.out.println(subtype);
                                                }   /**/
                                               // formFactory.setAPScaling(1);//restore default

                                            }
                                        }
									}catch(Exception e){
										e.printStackTrace();
									}
								}
							}

							if(!customPrintoverRide){


								dummyPanel.add(comp);

								try {
									renderComponent(g2, currentComp,comp,rotation,accDisplay);
									dummyPanel.remove(comp);
								} catch (Exception cc) {

								}
							}
						}
					}
                    
					currentComp++;

					if (currentComp == pageMap.length)
						break;
					
					indent = tempIndent; // put back indent
				}
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

		g2.setTransform(defaultAf);

		// put componenents back
		if (currentPage == pageIndex && panel != null) {
			// createDisplayComponentsForPage(pageIndex,this.panel,this.displayScaling,this.rotation);
			// panel.invalidate();
			// panel.repaint();

			resetScaledLocation(displayScaling, rotation, indent);
		}

	}



    private void setField(Component nextComp,int formPage,float scaling, int rotation) {

		// add fieldname to map for action events
		String curCompName = FormUtils.removeStateToCheck(nextComp.getName(), false);

		if (curCompName != null && !lastNameAdded.equals(curCompName)) {
			nameToCompIndex.put(curCompName, new Integer(nextFreeField));
			lastNameAdded = curCompName;
		}

		// setup and add component to selection
		if (nextComp != null) {

			// set location and size
			Rectangle rect = nextComp.getBounds();
			if (rect != null) {

				boundingBoxs[nextFreeField][0] = rect.x;
				boundingBoxs[nextFreeField][1] = rect.y;
				boundingBoxs[nextFreeField][2] = rect.width + rect.x;
				boundingBoxs[nextFreeField][3] = rect.height + rect.y;

			}

			allFields[nextFreeField] = nextComp;
			scroll[nextFreeField] = null;

			fontSize[nextFreeField] = fontSizes[nextFreeField];

			// flag as unused
			firstTimeDisplayed[nextFreeField] = true;

			// make visible
			scaleComponent(formPage, scaling, rotation, nextFreeField, nextComp, true,false);

		}

		pageMap[nextFreeField] = formPage;

		nextFreeField++;

	}

	/**
	 * alter font and size to match scaling. Note we pass in compoent so we can
	 * have multiple copies (needed if printing page displayed).
	 */
	private void scaleComponent(int curPage, float scale, int rotate, int ind,
			Component curComp, boolean redraw,boolean popups) {

		// if (showMethods)
		// System.out.println("DefaultAcroRenderer.scaleComponent()");

		if (curComp == null)
			return;
		
		/**
		 * work out if visible in Layer @rog - layer change
		 */
		if (layers != null) {
			
			/**
			 * get matching component
			 */
			// get correct key to lookup form data
			String ref = this.convertIDtoRef(ind);
	
	
			Object rawForm = this.getRawForm(ref);
			FormObject form=null;
			String layerName = null;
			if (rawForm != null) {
				if(rawForm instanceof Object[]){ //assume multiple all have same name
					Object[] items = (Object[]) rawForm;
					form = (FormObject) (items[0]);
					layerName = form.getLayerName();
				} else {
					form=(FormObject) rawForm;
					layerName = (form).getLayerName();
				}
			}
		
		
			
			// do not display
			if (layerName != null && layers.isLayerName(layerName)) {

				boolean isVisible = layers.isVisible(layerName);
				curComp.setVisible(isVisible);
			}
		}
		// ////////////////////////

		// OLD routine
		/**int x = 0, y = 0, w = 0, h = 0;

		int cropOtherX = (pageData.getMediaBoxWidth(currentPage) - pageData.getCropBoxWidth(currentPage) - pageData.getCropBoxX(currentPage));

		if (rotation == 0) {

			// old working routine
//			int x = (int)((boundingBoxs[i][0])*scaling)+insetW-pageData.getCropBoxX(currentPage);
//			int y = (int)((pageData.getMediaBoxHeight(currentPage)-boundingBoxs[i][3]-cropOtherY)*scaling)+insetH;
			// int w = (int)((boundingBoxs[i][2]-boundingBoxs[i][0])*scaling);
			// int h = (int)((boundingBoxs[i][3]-boundingBoxs[i][1])*scaling);

			int crx = pageData.getCropBoxX(currentPage);
			// new hopefully more accurate routine
			float x100 = (boundingBoxs[index][0]) - (crx) + insetW;

		
//			 * if we are drawing the forms to "extract image" or "print",
//			 * we don't translate g2 by insets we translate by crop x,y
//			 * so add on crop values
//			 * we should also only be using 0 rotation
			if (!repaint)
				x100 += crx;

			float y100 = (pageData.getMediaBoxHeight(currentPage) - boundingBoxs[index][3] - cropOtherY[currentPage]) + insetH;
			float w100 = (boundingBoxs[index][2] - boundingBoxs[index][0]);
			float h100 = (boundingBoxs[index][3] - boundingBoxs[index][1]);

			x = (int) (((x100 - insetW) * scaling) + insetW);
			y = (int) (((y100 - insetH) * scaling) + insetH);
			w = (int) (w100 * scaling);
			h = (int) (h100 * scaling);

		} else if (rotation == 90) {

			// old working routine
//			int x = (int)((boundingBoxs[i][1]-pageData.getCropBoxY(currentPage))*scaling)+insetW;
			// int y = (int)((boundingBoxs[i][0])*scaling)+insetH;
			// int w = (int)((boundingBoxs[i][3]-boundingBoxs[i][1])*scaling);
			// int h = (int)((boundingBoxs[i][2]-boundingBoxs[i][0])*scaling);

			// new hopefully better routine
			float x100 = (boundingBoxs[index][1] - pageData.getCropBoxY(currentPage)) + insetW;
			float y100 = (boundingBoxs[index][0] - pageData.getCropBoxX(currentPage)) + insetH;
			float w100 = (boundingBoxs[index][3] - boundingBoxs[index][1]);
			float h100 = (boundingBoxs[index][2] - boundingBoxs[index][0]);

			x = (int) (((x100 - insetH) * scaling) + insetH);
			y = (int) (((y100 - insetW) * scaling) + insetW);
			w = (int) (w100 * scaling);
			h = (int) (h100 * scaling);

		} else if (rotation == 180) {
			// old working routine
//			int x = (int)((pageData.getMediaBoxWidth(currentPage)-boundingBoxs[i][2]-cropOtherX)*scaling)+insetW;
//			int y = (int)((boundingBoxs[i][1]-pageData.getCropBoxY(currentPage))*scaling)+insetH;
			// int w = (int)((boundingBoxs[i][2]-boundingBoxs[i][0])*scaling);
			// int h = (int)((boundingBoxs[i][3]-boundingBoxs[i][1])*scaling);

			// new hopefully better routine
			int x100 = (int) (pageData.getMediaBoxWidth(currentPage) - boundingBoxs[index][2] - cropOtherX) + insetW;
			int y100 = (int) (boundingBoxs[index][1] - pageData.getCropBoxY(currentPage)) + insetH;
			int w100 = (int) (boundingBoxs[index][2] - boundingBoxs[index][0]);
			int h100 = (int) (boundingBoxs[index][3] - boundingBoxs[index][1]);

			x = (int) (((x100 - insetW) * scaling) + insetW);
			y = (int) (((y100 - insetH) * scaling) + insetH);
			w = (int) (w100 * scaling);
			h = (int) (h100 * scaling);

		} else if (rotation == 270) {

			// old working routine
//			int x = (int)((pageData.getMediaBoxHeight(currentPage)-boundingBoxs[i][3]-cropOtherY)*scaling)+insetW;
//			int y = (int)((pageData.getMediaBoxWidth(currentPage)-boundingBoxs[i][2]-cropOtherX)*scaling)+insetH;
			// int w = (int)((boundingBoxs[i][3]-boundingBoxs[i][1])*scaling);
			// int h = (int)((boundingBoxs[i][2]-boundingBoxs[i][0])*scaling);

			// new hopefully improved routine
			float x100 = (pageData.getMediaBoxHeight(currentPage) - boundingBoxs[index][3] - cropOtherY[currentPage]) + insetW;
			float y100 = (pageData.getMediaBoxWidth(currentPage) - boundingBoxs[index][2] - cropOtherX) + insetH;
			float w100 = (boundingBoxs[index][3] - boundingBoxs[index][1]);
			float h100 = (boundingBoxs[index][2] - boundingBoxs[index][0]);

			x = (int) (((x100 - insetH) * scaling) + insetH);
			y = (int) (((y100 - insetW) * scaling) + insetW);
			w = (int) (w100 * scaling);
			h = (int) (h100 * scaling);

		}
		System.out.println("old bounds="+x+" "+y+" "+w+" "+h);
		*/
		//NEW routine
		float[] box;
		if(popups)
			box = popupBounds[ind];
		else
			box = boundingBoxs[ind];
		int[] bounds = cropComponent(box,curPage,scale,rotate,ind,redraw);
		//END routines
		
		/**
		 * rescale the font size
		 */
		// if (debug)
		// System.out.println("check font size=" + comp);
		Font resetFont = curComp.getFont();
		if (!popups && resetFont != null) {
			int rawSize = fontSize[ind];
			
			if (rawSize == -1)
				rawSize = 0;//change -1 to best fit so that text is more visible

			if (rawSize == 0) {// best fit
				// work out best size for bounding box of object
				int height = (int) (boundingBoxs[ind][3] - boundingBoxs[ind][1]);
				int width = (int) (boundingBoxs[ind][2] - boundingBoxs[ind][0]);
				if (rotate == 90 || rotate == 270) {
					int tmp = height;
					height = width;
					width = tmp;
				}

				height *= 0.85;

				rawSize = height;

				if (curComp instanceof JTextComponent) {
					int len = ((JTextComponent) curComp).getText().length();
					if ((len * height) / 2 > width) {
						if (len > 0)
							width /= len;
						rawSize = width;
					}
				} else if (curComp instanceof JButton) {
					String text = ((JButton) curComp).getText();
					if (text != null) {
						int len = text.length();
						if ((len * height) / 2 > width) {
							if (len > 0)
								width /= len;
							rawSize = width;
						}
					}
				} else {
					// System.out.println("else="+width);
				}

				// rawSize = height;
			}

			int size = (int) (rawSize * scale);
			if (size < 1) {
				size = 1;
			}
			
			// if (debug)
			// System.out.println(size + "<<<<<<resetfont=" + resetFont);

			Font newFont = new Font(resetFont.getFontName(),resetFont.getStyle(),size);
			// resetFont.getAttributes().put(java.awt.font.TextAttribute.SIZE,size);
			// if (debug)
			// System.out.println("newfont=" + newFont);
			
			curComp.setFont(newFont);
		}
		
		// factor in offset if multiple pages displayed
		if ((xReached != null)) {
			bounds[0] = bounds[0] + xReached[curPage];
			bounds[1] = bounds[1] + yReached[curPage];
		}
//		if ((xReached != null)) {
//			x = x + xReached[currentPage];
//			y = y + yReached[currentPage];
//		}
		
		curComp.setBounds(indent + bounds[0], bounds[1], bounds[2], bounds[3]);
//		comp.setBounds(indent + x, y, w, h);
		
		/**
		 * rescale the icons if any
		 */
		if (curComp != null && curComp instanceof AbstractButton) {
			AbstractButton but = ((AbstractButton) curComp);

			Icon curIcon = but.getIcon();

			int combinedRotation=rotate;
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation);

			curIcon = but.getPressedIcon();
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation);

			curIcon = but.getSelectedIcon();
			if (curIcon instanceof FixImageIcon) 
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation);

			curIcon = but.getRolloverIcon();
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation);

			curIcon = but.getRolloverSelectedIcon();
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation);

		}

		if (redraw) {
			// comp.invalidate();
			// comp.repaint();
		}

		// @kieran
		/**
		final boolean testKieran=true;
		if(testKieran){
			
			comp.setFont(new JFRFont("Arial", Font.ITALIC,12, "\\WINDOWS\\Fonts"));
//			comp.setFont(new Font("Arial", Font.ITALIC,12));
			comp.setForeground(Color.CYAN);

		}
		/**/
	}
	
	private int[] cropComponent(float[] box,int curPage,float s,int r,int i,boolean redraw){
		/**/ // OLD routine
		int x = 0, y = 0, w = 0, h = 0;

		int cropOtherX = (pageData.getMediaBoxWidth(curPage) - pageData.getCropBoxWidth(curPage) - pageData.getCropBoxX(curPage));

		if (r == 0) {

			// old working routine
//			int x = (int)((box[i][0])*scaling)+insetW-pageData.getCropBoxX(currentPage);
//			int y = (int)((pageData.getMediaBoxHeight(currentPage)-box[i][3]-cropOtherY)*scaling)+insetH;
			// int w = (int)((box[i][2]-box[i][0])*scaling);
			// int h = (int)((box[i][3]-box[i][1])*scaling);

			int crx = pageData.getCropBoxX(curPage);
			// new hopefully more accurate routine
			float x100 = (box[0]) - (crx) + insetW;

//			if we are drawing the forms to "extract image" or "print",
//			we don't translate g2 by insets we translate by crop x,y
//			so add on crop values
//			we should also only be using 0 rotation
			
			if (!redraw)
				x100 += crx;

			float y100 = (pageData.getMediaBoxHeight(curPage) - box[3] - cropOtherY[curPage]) + insetH;
			float w100 = (box[2] - box[0]);
			float h100 = (box[3] - box[1]);

			x = (int) (((x100 - insetW) * s) + insetW);
			y = (int) (((y100 - insetH) * s) + insetH);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		} else if (r == 90) {

			// old working routine
//			int x = (int)((box[i][1]-pageData.getCropBoxY(currentPage))*scaling)+insetW;
			// int y = (int)((box[i][0])*scaling)+insetH;
			// int w = (int)((box[i][3]-box[i][1])*scaling);
			// int h = (int)((box[i][2]-box[i][0])*scaling);

			// new hopefully better routine
			float x100 = (box[1] - pageData.getCropBoxY(curPage)) + insetW;
			float y100 = (box[0] - pageData.getCropBoxX(curPage)) + insetH;
			float w100 = (box[3] - box[1]);
			float h100 = (box[2] - box[0]);

			x = (int) (((x100 - insetH) * s) + insetH);
			y = (int) (((y100 - insetW) * s) + insetW);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		} else if (r == 180) {
			// old working routine
//			int x = (int)((pageData.getMediaBoxWidth(currentPage)-box[i][2]-cropOtherX)*scaling)+insetW;
//			int y = (int)((box[i][1]-pageData.getCropBoxY(currentPage))*scaling)+insetH;
			// int w = (int)((box[i][2]-box[i][0])*scaling);
			// int h = (int)((box[i][3]-box[i][1])*scaling);

			// new hopefully better routine
			int x100 = (int) (pageData.getMediaBoxWidth(curPage) - box[2] - cropOtherX) + insetW;
			int y100 = (int) (box[1] - pageData.getCropBoxY(curPage)) + insetH;
			int w100 = (int) (box[2] - box[0]);
			int h100 = (int) (box[3] - box[1]);

			x = (int) (((x100 - insetW) * s) + insetW);
			y = (int) (((y100 - insetH) * s) + insetH);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		} else if (r == 270) {

			// old working routine
//			int x = (int)((pageData.getMediaBoxHeight(currentPage)-box[i][3]-cropOtherY)*scaling)+insetW;
//			int y = (int)((pageData.getMediaBoxWidth(currentPage)-box[i][2]-cropOtherX)*scaling)+insetH;
			// int w = (int)((box[i][3]-box[i][1])*scaling);
			// int h = (int)((box[i][2]-box[i][0])*scaling);

			// new hopefully improved routine
			float x100 = (pageData.getMediaBoxHeight(curPage) - box[3] - cropOtherY[curPage]) + insetW;
			float y100 = (pageData.getMediaBoxWidth(curPage) - box[2] - cropOtherX) + insetH;
			float w100 = (box[3] - box[1]);
			float h100 = (box[2] - box[0]);

			x = (int) (((x100 - insetH) * s) + insetH);
			y = (int) (((y100 - insetW) * s) + insetW);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		}
		
		return new int[]{x,y,w,h};
	}

	/**
	 * used to flush/resize data structures on new document/page
	 * @param formCount
	 * @param pageCount
	 * @param keepValues
	 */
	public void resetComponents(int formCount, int pageCount, boolean keepValues) {

		// System.out.println("count="+formCount);

		super.resetComponents(formCount, pageCount, keepValues);

		if (!keepValues) {
			scroll = new JScrollPane[formCount + 1];
			allFields = new Component[formCount + 1];
			popups = new Component[0];
			testedForDuplicates = new boolean[formCount + 1];
		} else if (pageMap != null) {
			JScrollPane[] tmpScroll = scroll;
			Component[] tmpFields = allFields;
			boolean[] tmptestedForDuplicates = testedForDuplicates;

			allFields = new Component[formCount + 1];
			testedForDuplicates = new boolean[formCount + 1];

			scroll = new JScrollPane[formCount + 1];

			int origSize = tmpFields.length;

			// populate
			for (int i = 0; i < formCount + 1; i++) {

				if (i == origSize)
					break;

				allFields[i] = tmpFields[i];
				testedForDuplicates[i] = tmptestedForDuplicates[i];
				scroll[i] = tmpScroll[i];
			}
		}

		// clean out store of buttonGroups
		annotBgs.clear();
	}

	/**
	 * used to remove all components from display
	 */
	public void removeAllComponentsFromScreen() {
		
		if (panel != null) {
			if (SwingUtilities.isEventDispatchThread())
				panel.removeAll();
			else {
				final Runnable doPaintComponent = new Runnable() {
					public void run() {
						panel.removeAll();
					}
				};
				SwingUtilities.invokeLater(doPaintComponent);
			}
		}

	}

	/**
	 * pass in object components drawn onto
	 * @param rootComp
	 */
	public void setRootDisplayComponent(final Object rootComp) {
		if (SwingUtilities.isEventDispatchThread())
			panel = (JPanel) rootComp;
		else {
			final Runnable doPaintComponent = new Runnable() {
				public void run() {
					panel = (JPanel) rootComp;
				}
			};
			SwingUtilities.invokeLater(doPaintComponent);
		}

	}

	/**
	 * used to add any additional radio/checkboxes on decode
	 * @param page
	 */
	public void completeFields(int page) {
	//commented out for now as it should be fixed when extra dictionarys are read into library.
	
//		Object[] bgRefs = annotBgs.keySet().toArray();
//		for (int i = 0; i < bgRefs.length; i++) {
//			ButtonGroup bg = (ButtonGroup) annotBgs.get(bgRefs[i]);
//			
//			Enumeration list = bg.getElements();
//			List emptyButs = new ArrayList(bg.getButtonCount());
//			FixImageIcon selIcon = null;
//	        while(list.hasMoreElements()){
//	        	JToggleButton but = (JToggleButton)list.nextElement();
//	            if(but.getSelectedIcon()==null){
//	            	//if we dont have a button icon, store the field to add it later
//	            	emptyButs.add(but);
//	            }else {
//	            	//we now have an icon so store it, so we can add it to the other buttons in the group.
//	            	selIcon = (FixImageIcon)but.getSelectedIcon();
//	            }
//	        }
//	        
//	        if(selIcon!=null){
//	        	//add the icon to the other buttons found
//	        	for (int b = 0; b < emptyButs.size(); b++) {
//					((JToggleButton)emptyButs.get(b)).setSelectedIcon(selIcon);
//				}
//	        }
//		}
	}

	/**
	 * store and complete setup of component
	 * @param formObject
     * @param formNum
     * @param formType
     * @param rawField
     * @param currentPdfFile
     */
	public void completeField(final FormObject formObject,
                              int formNum, Integer formType,
                                  Object rawField, PdfObjectReader currentPdfFile) {

        if (rawField == null)
            return;


        
        final int formPage = formObject.getPageNumber();

        // cast back to ULC or Swing or SWT
        // and change this class to suit
        Component retComponent = (Component) rawField;

        String fieldName = formObject.getTextStreamValue(PdfDictionary.T);
        if(!FormObject.newfieldnameRead){
	        String parent = formObject.getParentRef();

	        // if no name, or parent has one recursively scan tree for one in Parent
	        boolean isMultiple=false;

	        while (parent != null) {

	            FormObject parentObj =new FormObject(parent,true);
	            currentPdfFile.readObject(parentObj);

	            String newName = parentObj.getTextStreamValue(PdfDictionary.T);
	            if (fieldName == null && newName != null)
	                fieldName = newName;
	            else if (newName != null){
	                //we pass in kids data so stop name.name
	                if(!fieldName.equals(newName)) {
	                    fieldName = newName + "." + fieldName;
	                    isMultiple=true;
	                }
	            }
	            if (newName == null)
	                break;

	            parent = parentObj.getParentRef();
	        }

	        //set the field name to be the Fully Qualified Name
	        if(isMultiple)
	                formObject.setFieldName(fieldName);
		}

        /**
         * set values for Component
         */

        // append state to name so we can retrieve later if needed
        String name = fieldName;
        if (name != null) {// we have some empty values as well as null
            String stateToCheck = formObject.getStateTocheck();
            if (stateToCheck != null && stateToCheck.length() > 0)
                name = name + "-(" + stateToCheck + ')';
            retComponent.setName(name);
        }

        Rectangle rect = formObject.getBoundingRectangle();
        if (rect != null)
            retComponent.setBounds(rect);

        String defaultValue = formObject.getDefaultValue();
        if (formObject.getValuesMap() != null)
            defaultValue = (String) formObject.getValuesMap().get(Strip.checkRemoveLeadingSlach(defaultValue));
        else
            defaultValue = Strip.checkRemoveLeadingSlach(defaultValue);

        fontSizes[formNum] = formObject.getTextSize();
        defaultValues[formNum] = defaultValue;

        refToCompIndex.put(formObject.getObjectRefAsString(), new Integer(nextFreeField));
        convertFormIDtoRef.put(new Integer(nextFreeField), formObject.getObjectRefAsString());

        // set the type
        if(formType.equals(org.jpedal.objects.acroforms.creation.FormFactory.UNKNOWN))
            typeValues.put(fieldName, org.jpedal.objects.acroforms.creation.FormFactory.ANNOTATION);
        else
            typeValues.put(fieldName, formType);

        	/** //moved routine 
        	//@chrisBG - I have moved this code which sort Button Groups to end
            //so does it once whne page decoded, not every time we add a new icon
            //can you look at it and let me know what you think (and if it seems to
            //fix Costena. I have added related comments with @chrisBG
            //
            //I used your clever idea to set label to store info but now put Object ref
            //in it not page number.

            //order all ButtonGroups
            Iterator groups=this.annotBgs.values().iterator();
            ButtonGroup annotBg=null;
            while(groups.hasNext()){

                //work through each buttongroup
                annotBg= (ButtonGroup) groups.next();

                //format button group displays
                if (annotBg.getButtonCount() > 1) {

                    // we do so lets sort it
                    AbstractButton[] sortedButtons = FormUtils.sortGroupSmallestFirst(annotBg,this);

                    // and find the one thats selected and add them to display
                    for (int j = 0; j < annotBg.getButtonCount(); j++) {

                        //label actually holds ref
                        String ref=sortedButtons[j].getLabel();

                        FormObject formObject= (FormObject) this.rawFormData.get(ref);
                        int formPage=formObject.getPageNumber();
                        if (formPage==page) {

                            String currentState = formObject.getCurrentState();
                            String onState = formObject.getOnState();
                            if ((currentState != null && currentState.equals(FormUtils.removeStateToCheck(sortedButtons[j].getName(), true)))
                                    || (onState != null && onState.equals(FormUtils.removeStateToCheck(sortedButtons[j].getName(), true)))) {
                                sortedButtons[j].setSelected(true);
                            }

                            // put into array
                            //already done earlier
//                            setField(sortedButtons[j], page, scaling, rotation);

                        } else {
                            if (additionFieldsMap.get(sortedButtons[j].getLabel()) != null) {
                                ArrayList list = (ArrayList) additionFieldsMap.get(sortedButtons[j].getLabel());
                                list.add(sortedButtons[j]);
                                additionFieldsMap.put(sortedButtons[j].getLabel(), list);
                            } else {
                                ArrayList list = new ArrayList();
                                list.add(sortedButtons[j]);
                                additionFieldsMap.put(sortedButtons[j].getLabel(), list);
                            }
                        }
                    }
                }
            }

            //@chrisBG - original code left by Mark which may be related
        	//END moved routine*/
        	
                //@chrisBG - original code left by Mark - should never be called now
                //but left so we can look at

            // do we have a button group
            /*if (bg.getButtonCount() > 1) {

                // we do so lets sort it
                AbstractButton[] sortedButtons = FormUtils.sortGroupSmallestFirst(bg,this);

                // and find the one thats selected and add them to display
                for (int j = 0; j < bg.getButtonCount(); j++) {
                    if (sortedButtons[j].getLabel().equals(String.valueOf(formPage))) {
                        String currentState = formObject.getCurrentState();
                        String onState = formObject.getOnState();
                        if ((currentState != null && currentState.equals(FormUtils.removeStateToCheck(sortedButtons[j].getName(), true)))
                                || (onState != null && onState.equals(FormUtils.removeStateToCheck(sortedButtons[j].getName(), true)))) {
                            sortedButtons[j].setSelected(true);
                        }

                        // put into array
                        setField(sortedButtons[j], formPage, scaling, rotation);

                    } else {
                        if (additionFieldsMap.get(sortedButtons[j].getLabel()) != null) {
                            ArrayList list = (ArrayList) additionFieldsMap.get(sortedButtons[j].getLabel());
                            list.add(sortedButtons[j]);
                            additionFieldsMap.put(sortedButtons[j].getLabel(), list);
                        } else {
                            ArrayList list = new ArrayList();
                            list.add(sortedButtons[j]);
                            additionFieldsMap.put(sortedButtons[j].getLabel(), list);
                        }
                    }
                }
            } else */if (retComponent != null) { // other form objects

                    //@chrisBG - original code left by Mark and replicated below - can we delete (we may need to copy if)
                if (formObject.getFieldFlags()[FormObject.NOTOGGLETOOFF_ID]) {
                    if (retComponent instanceof AbstractButton) {
                        AbstractButton but = (AbstractButton) retComponent;
                        but.setBounds(formObject.getBoundingRectangle());
                        but.setText(String.valueOf(formObject.getPageNumber()));
                        new ButtonGroup().add(but); // Add to button group
                    } else {
                    }
                }

                // allow for kids set for Annots in Acroform
                // On first we create the button and then link in later items
                // no sorting at present - may need refining
                //String oldAnnotLink = formObject.getAnnotParent();

                    String parentRef = formObject.getStringKey(PdfDictionary.Parent);
                    if (parentRef != null) {

                    // see if one set ad if not set
                    ButtonGroup annotBg=null;
                    Object currentBg = annotBgs.get(parentRef);

                    if(currentBg==null){ //first item in Group so create new ButtonGroup if more than 1 kid

                        //check count by scanning back up tree for parents and counting kids
                        int kidCount=-1;
                            //parentRef=formObject.getObjectRefAsString();

                        while(true){
                                FormObject parentObj=new FormObject(parentRef,null);
                            currentPdfFile.readObject(parentObj);

                                byte[][] kids=parentObj.getKeyArray(PdfDictionary.Kids);
                            if(kids!=null)
                                kidCount=kids.length;

                            if(kidCount!=-1 || parentObj.getParentRef()==null)
                                break;

                                parentRef=parentObj.getParentRef();
                        }

                        if(kidCount>1){
                            annotBg = new ButtonGroup();
                            annotBgs.put(parentRef, annotBg);
                        }
                    } else { // use existing

                        annotBg = (ButtonGroup) currentBg;
                    }

                    if(annotBg!=null){
                    	if (formObject.getFieldFlags()[FormObject.NOTOGGLETOOFF_ID]) {

                            //@chris - case 5850 bounds are set here. Do we overwrite later or is it separate problem.
                            //Any ideas
	                        try {
	                            if (retComponent instanceof AbstractButton) {
	                                AbstractButton but = (AbstractButton) retComponent;
	                                but.setBounds(formObject.getBoundingRectangle());
                                    but.setText(formObject.getObjectRefAsString());
	                                annotBg.add(but); // Add to button group
	                            }

	                        } catch (Exception e) {
	                        }
                    	}
                	}
                }

                //if /Open set to false should not be visible at start
                if(formObject.getBoolean(PdfDictionary.Open)==false &&
                        formObject.getParameterConstant(PdfDictionary.Subtype)==PdfDictionary.Popup){
                    retComponent.setVisible(false);

                }

                // put into array
                setField(retComponent, formPage, scaling, rotation);

            }

    }

	/**
	 * alter location and bounds so form objects show correctly scaled
	 */
	public void resetScaledLocation(float scaling, int rotation, int indent) {

		//if (showMethods)
		  //  System.out.println("2.resetScaledLocation scaling=" + scaling + " indent=" + indent + " rotation=" + rotation);

		this.indent = indent;
		this.displayScaling = scaling;
		this.rotation = rotation;

		//System.out.println("Reset scaling "+scaling+" "+lastScaling+" "+startID+" "+this.pageHeight);
		/**
         debug=true;
         /**/

		// we get a spurious call in linux resulting in an exception
		if (trackPagesRendered == null)
			return;

		// only if necessary
		if (forceRedraw || scaling != lastScaling || rotation != oldRotation || indent != oldIndent) {

			oldRotation = rotation;
			lastScaling = scaling;
			oldIndent = indent;

			int currentComp;

			// fix rescale issue on Testfeld
			if (startPage <
					trackPagesRendered.length) {
				currentComp = trackPagesRendered[startPage];// startID;
			} else {
				currentComp = 0;
			}

			// reset all locations
			if ((allFields != null) && (currentPage > 0) && (currentComp != -1) && (pageMap.length > currentComp)) {

				//just put on page, allowing for no values (last one alsways empty as array 1 too big
				// while(pageMap[currentComp]==currentPage){
				while (currentComp<pageMap.length && currentComp>-1 &&  
						((pageMap[currentComp] >= startPage) && (pageMap[currentComp] < endPage) 
								&& (allFields[currentComp] != null))) {

					// System.out.println("added"+currentComp);
					//while(currentComp<pageMap.length){//potential fix to help rotation
					if (panel != null){// && !(allFields[currentComp] instanceof JList))

						if (SwingUtilities.isEventDispatchThread()) {
							if (scroll[currentComp] == null)
								panel.remove(allFields[currentComp]);
							else
								panel.remove(scroll[currentComp]);

							scaleComponent(pageMap[currentComp], scaling, rotation, currentComp, allFields[currentComp], true,false);

						} else {
							final int id = currentComp;
							final float s = scaling;
							final int r = rotation;
							final Runnable doPaintComponent = new Runnable() {
								public void run() {
									if (scroll[id] == null)
										panel.remove(allFields[id]);
									else
										panel.remove(scroll[id]);

									scaleComponent(pageMap[id], s, r, id, allFields[id], true,false);

								}
							};
							SwingUtilities.invokeLater(doPaintComponent);
						}
					}

					if (panel != null) {

						/** possible problem with rotation files, 
						 * just test if rotated 90 or 270 and get appropriate height or width, 
						 * that would represent the height when viewed at correct orientation
						 */
						float boundHeight = boundingBoxs[currentComp][3]-boundingBoxs[currentComp][1];
						int swingHeight = allFields[currentComp].getPreferredSize().height;

						if(allFields[currentComp] instanceof JList && boundHeight<swingHeight){

							JList comp = (JList) allFields[currentComp];

							if (scroll[currentComp] != null)
								scroll[currentComp].remove(comp);

							scroll[currentComp] = new JScrollPane(comp);

							scroll[currentComp].setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
							scroll[currentComp].setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


							scroll[currentComp].setLocation(comp.getLocation());

							scroll[currentComp].setPreferredSize(comp.getPreferredSize());
							scroll[currentComp].setSize(comp.getSize());

							// ensure visible (do it before we add)
							int index = comp.getSelectedIndex();
							if (index > -1)
								comp.ensureIndexIsVisible(index);

							if (SwingUtilities.isEventDispatchThread())
								panel.add(scroll[currentComp]);
							else {
								final int id = currentComp;
								final Runnable doPaintComponent = new Runnable() {
									public void run() {
										panel.add(scroll[id]);
									}
								};
								SwingUtilities.invokeLater(doPaintComponent);
							}

						} else {

							if (SwingUtilities.isEventDispatchThread())
								panel.add(allFields[currentComp]);
							else {
								final int id = currentComp;
								final Runnable doPaintComponent = new Runnable() {
									public void run() {
										panel.add(allFields[id]);
									}
								};
								SwingUtilities.invokeLater(doPaintComponent);
							}
						}
					}
					currentComp++;
				}
			}
			
		}
	}

	/**
	 * put components onto screen display
	 * @param startPage
	 * @param endPage
	 */
	public void displayComponents(int startPage, int endPage) {

		
		if (panel == null)
			return;

		this.startPage = startPage;
		this.endPage = endPage;

		/**    MIGHT be needed for multi display
         boolean multiPageDisplay=(startPage!=endPage);

         //remove all invisible forms
         if(multiPageDisplay){

         int start=1;
         int end=startPage;
         //from start to first page
         //removePageRangeFromDisplay(start, end, panel); //end not included in range

         //from end to last page
         int last=1+trackPagesRendered.length;
         //removePageRangeFromDisplay(end, last, panel);
         }
         /**/

		for (int page = startPage; page < endPage; page++) {

			int currentComp = getStartComponentCountForPage(page);
			//just put on page, allowing for no values (last one always empty as array 1 too big)

			// allow for empty form
			if (pageMap == null || pageMap.length <= currentComp)
				return;

			// display components
			if (currentComp!=-1 && currentComp != -999 && startPage>0 && endPage>0) {
				while (pageMap[currentComp] >= startPage && pageMap[currentComp] < endPage) {

					if (allFields[currentComp] != null) {
                        if (SwingUtilities.isEventDispatchThread()) {
							
							scaleComponent(pageMap[currentComp], scaling, rotation, currentComp, allFields[currentComp], true,false);
							panel.add(allFields[currentComp]);

						} else {
							final int id = currentComp;
							final Runnable doPaintComponent = new Runnable() {
								public void run() {
									scaleComponent(pageMap[id], scaling, rotation, id, allFields[id], true,false);
									panel.add(allFields[id]);
								}
							};
							SwingUtilities.invokeLater(doPaintComponent);
						}

						firstTimeDisplayed[currentComp] = false;
					}

					currentComp++;

					if (currentComp == pageMap.length)
						break;
				}
			}
		}
	}

	/**
	 * tell user about Javascript validation error
	 * @param code
	 * @param args
	 */
	public void reportError(int code, Object[] args) {

		// tell user
		if (code == ErrorCodes.JSInvalidFormat) {
			JOptionPane.showMessageDialog(panel,"The values entered does not match the format of the field ["+args[0]+" ]",
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);
		} else if (code == ErrorCodes.JSInvalidDateFormat)
			JOptionPane.showMessageDialog(panel,"Invalid date/time: please ensure that the date/time exists. Field ["+args[0]+" ] should match format "+args[1],
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);
		else if (code == ErrorCodes.JSInvalidRangeFormat) {
			StringBuffer message=new StringBuffer("Invalid value: must be greater than ");
			if (args[1].equals("true"))
				message.append("or equal to ");

			message.append(args[2]);
			message.append("\nand less than ");

			if ((args[3]).equals("true"))
				message.append("or equal to ");

			message.append(args[4]);

			message.append('.');
			JOptionPane.showMessageDialog(panel, message.toString(),
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);
		} else
			JOptionPane.showMessageDialog(panel,"The values entered does not match the format of the field",
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);

	}

	/**
	 * return list of form names for page
	 * @param pageNumber
	 * @return
	 */
	public List getComponentNameList(int pageNumber) {

		if (trackPagesRendered == null)
			return null;

		if ((pageNumber != -1) && (trackPagesRendered[pageNumber] == -1))
			return null; //now we can interrupt decode page this is more appropriate
		// throw new PdfException("[PDF] Page "+pageNumber+" not decoded");

		int currentComp;
		if (pageNumber == -1)
			currentComp = 0;
		else
			currentComp = trackPagesRendered[pageNumber];

		ArrayList nameList = new ArrayList();

		// go through all fields on page and add to list
		String lastName = "";
		String currentName = "";
		while ((pageNumber == -1) || (pageMap[currentComp] == pageNumber)) {
			lastName = getComponentName(currentComp, nameList, lastName);
			currentComp++;
			if (currentComp == pageMap.length)
				break;
		}

		return nameList;
	}

	/**
	 * not used by Swing
	 * @param offset
	 */
	public void setOffset(int offset) {

	}

	/** repaints the specified form or all forms if null is sent in */
	public void invalidate(String name) {
		if (name == null) {
			for (int i = 0; i < allFields.length; i++) {
				allFields[i].repaint();
			}
		} else {
			Object[] forms = getComponentsByName(name);
			
			if(forms==null)
				return;
			
			for (int i = 0; i < forms.length; i++) {
				allFields[i].repaint();
			}
		}
	}

	public void storeDisplayValue(String fieldRef) {
		int index = ((Integer)refToCompIndex.get(fieldRef)).intValue();
		
		if (allFields[index] instanceof JComboBox){
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			Object value = ((JComboBox) allFields[index]).getSelectedItem();
			form.setSelectedItem((String)value);
			
		}else if (allFields[index] instanceof JList){
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			int[] values = ((JList) allFields[index]).getSelectedIndices();
			form.setTopIndex(values);
			
		}else if (allFields[index] instanceof JRadioButton){
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			JRadioButton but = ((JRadioButton) allFields[index]);
			if(but.isSelected()){
				form.setChildOnState(FormUtils.removeStateToCheck(but.getName(), true));
			}
			
		}else if (allFields[index] instanceof JCheckBox){
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			JCheckBox but = ((JCheckBox) allFields[index]);
			if(but.isSelected()){
				form.setCurrentState(FormUtils.removeStateToCheck(but.getName(), true));
			}
			
		}else if (allFields[index] instanceof JTextComponent){
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			String value = ((JTextComponent) allFields[index]).getText();
			form.setTextValue(value);
			
		}else if (allFields[index] instanceof JButton){
//			FormObject form = (FormObject) rawFormData.get(fieldRef);
		}else {
            
		}
	}
	
	/** finds the display field of the defined form reference and changes its visibility as needed */
	public void setCompVisible(String ref, boolean visible) {
		Object checkObj;
		if (ref.indexOf("R") != -1) {
			checkObj = refToCompIndex.get(ref);
		}else {
			checkObj = nameToCompIndex.get(ref);
		}
		
		if(checkObj == null)
			return;
		
		int index = ((Integer) checkObj).intValue();
		allFields[index].setVisible(visible);
	}
    
    private BufferedImage invertImage(BufferedImage image) {
        if (image == null)
            return null;

        BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        byte reverse[] = new byte[256];
        for (int j = 0; j < 200; j++) {
            reverse[j] = (byte) (256 - j);
        }
        ByteLookupTable blut = new ByteLookupTable(0, reverse);
        LookupOp lop = new LookupOp(blut, null);
        lop.filter(image, ret);

        return ret;
    }

    /**
     * create a pressed look of the <b>image</b> and return it
     */
    private BufferedImage createPressedLook(Image image) {

        if(image==null)
        return null;
        
        BufferedImage pressedImage = new BufferedImage(image.getWidth(null) + 2, image.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) pressedImage.getGraphics();
        g.drawImage(image, 1, 1, null);
        g.dispose();
        return pressedImage;
    }
	
    /** defined in page 102 of javascript for acrobat api */
    public int alert(String cMsg,int nIcon,int nType,String cTitle,Object oDoc,Object oCheckbox){
    	//setup what type of answer options the user has with nType
    	int optionType;
    	switch(nType){
    	case 1: optionType = JOptionPane.OK_CANCEL_OPTION; break;
    	case 2: optionType = JOptionPane.YES_NO_OPTION; break;
    	case 3: optionType = JOptionPane.YES_NO_CANCEL_OPTION; break;
    	default: optionType = JOptionPane.DEFAULT_OPTION; break;//0
    	}

    	//setup what type of message this is with nIcon
    	int messageType;
    	switch(nIcon){
    	case 1: messageType = JOptionPane.WARNING_MESSAGE; break;
    	case 2: messageType = JOptionPane.QUESTION_MESSAGE; break;
    	case 3: messageType = JOptionPane.INFORMATION_MESSAGE; break;
    	default: messageType = JOptionPane.ERROR_MESSAGE; break;//0
    	}

    	//add line breaks to message so it doesnt extend to wide
    	cMsg = cMsg.replaceAll("\\. ", "\\.\n");

    	//show the dialog
		int answer = JOptionPane.showConfirmDialog((Component)oDoc, cMsg, cTitle, optionType, messageType);
		/**/

    	switch(nType){
    	case 1: //ok/cancel
    		/*returns 1 - OK, 2 - Cancel, 3 - No, 4 - Yes*/
    		if(answer==0){
    			return 1; //OK
    		}else {
    			return 2; //Cancel
    		}
    	case 2: //yes/no
    	case 3://yes/no/cancel
    		switch(answer){
    		case 0: return 4; //Yes
    		case 1: return 3; //No
    		default: return 2; //Cancel
    		}
    	default: //ok
    		if(answer == 0){
    			return 1;//OK
    		}else {
    			return 2;//Cancel
    		}
    	}
    }
	public void popup(FormObject formObj, PdfObjectReader currentPdfFile) {
        if(ActionHandler.drawPopups){
	        //popup needs to be stored for each field, as method A() is static,
	        //and we need a seperate popup for each field.
	        JComponent popup;
	
	        if(formObj.isPopupBuilt()){
	            popup = (JComponent)formObj.getPopupObj();
	
	        }else {
	            PdfObject popupObj=(PdfObject)formObj.getDictionary(PdfDictionary.Popup);;
	            currentPdfFile.checkResolved(popupObj);
	            
	    		popup = new PdfSwingPopup(formObj,popupObj);
	    		
	    		float[][] tmpf = new float[popupBounds.length+1][4];
	            System.arraycopy(popupBounds, 0, tmpf, 0, popupBounds.length);
	            tmpf[popupBounds.length] = checkPopupBoundsOnPage(popupObj.getFloatArray(PdfDictionary.Rect));
	            popupBounds = tmpf;
	    		
	            JComponent[] tmp = new JComponent[popups.length+1];
	            System.arraycopy(popups, 0, tmp, 0, popups.length);
	            tmp[popups.length] = popup;
	            popups = tmp;
	            
	            formObj.setPopupBuilt(popup);
	            
	            //draw the popup on screen for the first time
	            popup.setVisible(popupObj.getBoolean(PdfDictionary.Open));
	            
	            forceRedraw  = true;
	    		resetScaledLocation(scaling, rotation, indent);
	    		panel.repaint();
	        }
	        
	        if (popup.isVisible()) {
				popup.setVisible(false);
	        } else {
	        	popup.setVisible(true);
	        }
        }
	}
    
	/**
     * flag forms as needing redraw
     */
    public void invalidateForms() {
        lastScaling=-lastScaling;
    }

    /** sets the text color for the specified swing component */
	public void setTextColor(String ref, Color textColor) {
		Object checkObj;
		if (ref.indexOf("R") != -1) {
			checkObj = refToCompIndex.get(ref);
		} else {
			checkObj = nameToCompIndex.get(ref);
		}
		
		//Fix null exception in /PDFdata/baseline_screens/forms/406302.pdf
		if(checkObj==null)
			return ;
		
		//set the text color
		int index = ((Integer) checkObj).intValue();
		allFields[index].setForeground(textColor);
	}

    public void setCustomPrintInterface(CustomFormPrint customFormPrint) {
        this.customFormPrint=customFormPrint;
    }

	public int getFieldType(Object swingComp) {
		if(swingComp instanceof JTextField || swingComp instanceof JTextArea || swingComp instanceof JPasswordField){
			return TEXT_TYPE;
        }else if(swingComp instanceof JRadioButton || swingComp instanceof JCheckBox || swingComp instanceof JButton){
			return BUTTON_TYPE;
		}else if(swingComp instanceof JList || swingComp instanceof JComboBox){
			return LIST_TYPE;
		}else {
			return UNKNOWN_TYPE;
		}
	}

}
