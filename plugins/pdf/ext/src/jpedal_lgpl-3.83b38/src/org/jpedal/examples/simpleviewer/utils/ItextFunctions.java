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
* ItextFunctions.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.utils;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.ProgressMonitor;
import javax.swing.text.JTextComponent;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.popups.AddHeaderFooterToPDFPages;
import org.jpedal.examples.simpleviewer.gui.popups.CropPDFPages;
import org.jpedal.examples.simpleviewer.gui.popups.DeletePDFPages;
import org.jpedal.examples.simpleviewer.gui.popups.EncryptPDFDocument;
import org.jpedal.examples.simpleviewer.gui.popups.ExtractPDFPagesNup;
import org.jpedal.examples.simpleviewer.gui.popups.InsertBlankPDFPage;
import org.jpedal.examples.simpleviewer.gui.popups.RotatePDFPages;
import org.jpedal.examples.simpleviewer.gui.popups.SavePDF;
import org.jpedal.examples.simpleviewer.gui.popups.StampImageToPDFPages;
import org.jpedal.examples.simpleviewer.gui.popups.StampTextToPDFPages;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;

/** central location to place external code using itext library */
public class ItextFunctions {

	public final static int ROTATECLOCKWISE = 0;
	public final static int ROTATECOUNTERCLOCKWISE = 1;
	public final static int ROTATE180 = 2;

	public final static int ORDER_ACCROS = 3;
	public final static int ORDER_DOWN = 4;
	public final static int ORDER_STACK = 5;
	
	public final static int REPEAT_NONE = 6;
	public final static int REPEAT_AUTO = 7;
	public final static int REPEAT_SPECIFIED = 8;
	
	private final String separator = System.getProperty("file.separator");

	private String fileName = "";

	private GUIFactory currentGUI;

	private String selectedFile;

	/**copy of PdfDecoder*/
	private PdfDecoder dPDF;

	public ItextFunctions(SwingGUI currentGUI, String selectedFile,
			PdfDecoder decode_pdf) {
		
		String fileName = new File(selectedFile).getName();
		if(fileName.lastIndexOf('.') != -1)
			fileName = fileName.substring(0,fileName.lastIndexOf('.'));
		
		this.fileName =fileName.replaceAll("%20"," ");
		
		this.currentGUI = currentGUI;
		this.selectedFile = selectedFile;
		this.dPDF = decode_pdf;
	}

    //<link><a name="saveform" />
    /** uses itext to save out form data with any changes user has made */
	public void saveFormsData(String file) {
		try {
			org.jpedal.objects.acroforms.rendering.AcroRenderer formRenderer = dPDF.getFormRenderer();

			if (formRenderer == null)
				return;

			PdfReader reader = new PdfReader(selectedFile);
			PdfStamper stamp = new PdfStamper(reader,
					new FileOutputStream(file));
			AcroFields form = stamp.getAcroFields();

			List names = formRenderer.getComponentNameList();

			/**
			 * work through all components writing out values
			 */
			for (int i = 0; i < names.size(); i++) {

				String name = (String) names.get(i);
				Component[] comps = (Component[]) formRenderer.getComponentsByName(name);

				int type = form.getFieldType(name);
				String value = "";
				switch (type) {
				case AcroFields.FIELD_TYPE_CHECKBOX:
//					TODO @itext checkbox selection save 
					if (comps.length == 1) {
						JCheckBox cb = (JCheckBox) comps[0];
						value = cb.getName();
						if (value != null) {
							int ptr = value.indexOf("-(");
							if (ptr != -1) {
								value = value.substring(ptr + 2,
										value.length() - 1);
							}
						}
						
						if (value.length() == 0)
							value = "On";

						if (cb.isSelected()){
							form.setField(name, value);
						}else {
							form.setField(name, "Off");
						}

					} else {
						for (int j = 0; j < comps.length; j++) {
							JCheckBox cb = (JCheckBox) comps[j];
							if (cb.isSelected()) {

								value = cb.getName();
								if (value != null) {
									int ptr = value.indexOf("-(");
									if (ptr != -1) {
										value = value.substring(ptr + 2, value
												.length() - 1);
										
//										name is wrong it should be the piece of field data that needs changing.
										//TODO itext
										form.setField(name, value);
									}
								}

								break;
							}
						}
					}

					break;
				case AcroFields.FIELD_TYPE_COMBO:
					JComboBox combobox = (JComboBox) comps[0];
					value = (String) combobox.getSelectedItem();

					/**
					 * allow for user adding new value to Combo to emulate
					 * Acrobat * String currentText = (String)
					 * combobox.getEditor().getItem();
					 * 
					 * if(!currentText.equals("")) value = currentText;
					 */

					if (value == null)
						value = "";
					form.setField(name, value);

					break;
				case AcroFields.FIELD_TYPE_LIST:
					JList list = (JList) comps[0];
					value = (String) list.getSelectedValue();
					if (value == null)
						value = "";
					form.setField(name, value);

					break;
				case AcroFields.FIELD_TYPE_NONE:

					break;
				case AcroFields.FIELD_TYPE_PUSHBUTTON:

					break;
				case AcroFields.FIELD_TYPE_RADIOBUTTON:

					for (int j = 0; j < comps.length; j++) {
						JRadioButton radioButton = (JRadioButton) comps[j];
						if (radioButton.isSelected()) {

							value = radioButton.getName();
							if (value != null) {
								int ptr = value.indexOf("-(");
								if (ptr != -1) {
									value = value.substring(ptr + 2, value
											.length() - 1);
									form.setField(name, value);
								}
							}

							break;
						}
					}

					break;
				case AcroFields.FIELD_TYPE_SIGNATURE:

					break;

				case AcroFields.FIELD_TYPE_TEXT:
					JTextComponent tc = (JTextComponent) comps[0];
					value = tc.getText();
					form.setField(name, value);

					// ArrayList objArrayList = form.getFieldItem(name).widgets;
					// PdfDictionary dic = (PdfDictionary)objArrayList.get(0);
					// PdfDictionary action
					// =(PdfDictionary)PdfReader.getPdfObject(dic.get(PdfName.MK));
					//
					// if (action == null) {
					// PdfDictionary d = new PdfDictionary(PdfName.MK);
					// dic.put(PdfName.MK, d);
					//
					// Color color = tc.getBackground();
					// PdfArray f = new PdfArray(new int[] { color.getRed(),
					// color.getGreen(), color.getBlue() });
					// d.put(PdfName.BG, f);
					// }

					// moderatly useful debug code
					// Item dd = form.getFieldItem(name);
					//					
					// ArrayList objArrayList = dd.widgets;
					// Iterator iter1 = objArrayList.iterator(),iter2;
					// String strName;
					// PdfDictionary objPdfDict = null;
					// PdfName objName = null;
					// PdfObject objObject = null;
					// while(iter1.hasNext())
					// {
					// objPdfDict = (PdfDictionary)iter1.next();
					// System.out.println("PdfDictionary Object: " +
					// objPdfDict.toString());
					// Set objSet = objPdfDict.getKeys();
					// for(iter2 = objSet.iterator(); iter2.hasNext();)
					// {
					// objName = (PdfName)iter2.next();
					// objObject = objPdfDict.get(objName);
					// if(objName.toString().indexOf("MK")!=-1)
					// System.out.println("here");
					// System.out.println("objName: " + objName.toString() + " -
					// objObject:" + objObject.toString() + " - Type: " +
					// objObject.type());
					// if(objObject.isDictionary())
					// {
					// Set objSet2 = ((PdfDictionary)objObject).getKeys();
					// PdfObject objObject2;
					// PdfName objName2;
					// for(Iterator iter3 = objSet2.iterator();
					// iter3.hasNext();)
					// {
					// objName2 = (PdfName)iter3.next();
					// objObject2 = ((PdfDictionary)objObject).get(objName2);
					// System.out.println("objName2: " + objName2.toString() + "
					// -objObject2: " + objObject2.toString() + " - Type: " +
					// objObject2.type());
					// }
					// }
					// }
					// }

					break;
				default:
					break;
				}
			}
			stamp.close();

		} catch (ClassCastException e1) {
			System.out
					.println("Expected component does not match actual component");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void extractPagesToNewPDF(SavePDF current_selection) {

		final boolean exportIntoMultiplePages = current_selection.getExportType();

		final int[] pgsToExport = current_selection.getExportPages();

		if (pgsToExport == null)
			return;

		final int noOfPages = pgsToExport.length;

		// get user choice
		final String output_dir = current_selection.getRootDir() + separator + fileName + separator + "PDFs" + separator;

		File testDirExists = new File(output_dir);
		if (!testDirExists.exists())
			testDirExists.mkdirs();

		final ProgressMonitor status = new ProgressMonitor(currentGUI
				.getFrame(), Messages
				.getMessage("PdfViewerMessage.GeneratingPdfs"), "", 0,
				noOfPages);

		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				if (exportIntoMultiplePages) {

					boolean yesToAll = false;

					for (int i = 0; i < noOfPages; i++) {
						int page = pgsToExport[i];

						if (status.isCanceled()) {
							currentGUI
									.showMessageDialog(Messages.getMessage("PdfViewerError.UserStoppedExport")
											+ i
											+ ' '
                                            + Messages.getMessage("PdfViewerError.ReportNumberOfPagesExported"));

							return null;
						}
						try {

							PdfReader reader = new PdfReader(selectedFile);

							File fileToSave = new File(output_dir + fileName + "_pg_" + page + ".pdf");

							if (fileToSave.exists() && !yesToAll) {
								if (pgsToExport.length > 1) {
									int n = currentGUI.showOverwriteDialog(
											fileToSave.getAbsolutePath(), true);

									if (n == 0) {
										// clicked yes so just carry on for this
										// once
									} else if (n == 1) {
										// clicked yes to all, so set flag
										yesToAll = true;
									} else if (n == 2) {
										// clicked no, so loop round again
										status.setProgress(page);
										continue;
									} else {

										currentGUI
												.showMessageDialog(Messages.getMessage("PdfViewerError.UserStoppedExport")
														+ i
														+ ' '
                                                        + Messages.getMessage("PdfViewerError.ReportNumberOfPagesExported"));

										status.close();
										return null;
									}
								} else {
									int n = currentGUI.showOverwriteDialog(fileToSave.getAbsolutePath(), false);

									if (n == 0) {
										// clicked yes so just carry on
									} else {
										// clicked no, so exit
										return null;
									}
								}
							}

							Document document = new Document();
							PdfCopy writer = new PdfCopy(document,new FileOutputStream(fileToSave));

							document.open();

							PdfImportedPage pip = writer.getImportedPage(reader, page);
							writer.addPage(pip);

							PRAcroForm form = reader.getAcroForm();
							if (form != null) {
								writer.copyAcroForm(reader);
							}

							document.close();
						} catch (Exception de) {
							de.printStackTrace();
						}

						status.setProgress(i + 1);
					}
				} else {
					try {

						PdfReader reader = new PdfReader(selectedFile);

						File fileToSave = new File(output_dir + "export_" + fileName + ".pdf");

						if (fileToSave.exists()) {
							int n = currentGUI.showOverwriteDialog(fileToSave.getAbsolutePath(), false);

							if (n == 0) {
								// clicked yes so just carry on
							} else {
								// clicked no, so exit
								return null;
							}
						}

						Document document = new Document();
						PdfCopy copy = new PdfCopy(document,new FileOutputStream(fileToSave.getAbsolutePath()));
						document.open();
						PdfImportedPage pip;
						for (int i = 0; i < noOfPages; i++) {
							int page = pgsToExport[i];

							pip = copy.getImportedPage(reader, page);
							copy.addPage(pip);
						}

						PRAcroForm form = reader.getAcroForm();

						if (form != null) {
							copy.copyAcroForm(reader);
						}

						List bookmarks = SimpleBookmark.getBookmark(reader);
						copy.setOutlines(bookmarks);

						document.close();

					} catch (Exception de) {
						de.printStackTrace();
					}
				}
				status.close();

				currentGUI.showMessageDialog(Messages
						.getMessage("PdfViewerMessage.PagesSavedAsPdfTo")
						+ ' ' + output_dir);

				return null;
			}
		};

		worker.start();

	}

	public void nup(int pageCount,PdfPageData currentPageData, ExtractPDFPagesNup extractPage){
		
		try{
			
			int[] pgsToEdit = extractPage.getPages();
			
			if(pgsToEdit == null)
				return;
			
			//get user choice
			final String output_dir = extractPage.getRootDir() + separator + fileName + separator + "PDFs" + separator;

			File testDirExists = new File(output_dir);
			if (!testDirExists.exists())
				testDirExists.mkdirs();
			
			List pagesToEdit = new ArrayList();
			for(int i=0;i<pgsToEdit.length;i++)
				pagesToEdit.add(new Integer(pgsToEdit[i]));
			
			PdfReader reader = new PdfReader(selectedFile);

			File fileToSave = new File(output_dir + "export_" + fileName + ".pdf");
			
			if (fileToSave.exists()) {
				int n = currentGUI.showOverwriteDialog(fileToSave.getAbsolutePath(), false);

				if (n == 0) {
					// clicked yes so just carry on
				} else {
					// clicked no, so exit
					return;
				}
			}
			
			int rows = extractPage.getLayoutRows();
			int coloumns = extractPage.getLayoutColumns();
			
			int paperWidth = extractPage.getPaperWidth();
			int paperHeight = extractPage.getPaperHeight();
			
			Rectangle pageSize = new Rectangle(paperWidth,paperHeight);
			
			String orientation = extractPage.getPaperOrientation();
			
			Rectangle newSize = null;
			if(orientation.equals(Messages.getMessage("PdfViewerNUPOption.Auto"))){
				if(coloumns > rows)
					newSize = new Rectangle(pageSize.height(), pageSize.width());	
					else
					newSize = new Rectangle(pageSize.width(), pageSize.height());
			}else if(orientation.equals("Portrait")){
				newSize = new Rectangle(pageSize.width(), pageSize.height());
			}else if(orientation.equals("Landscape")){
				newSize = new Rectangle(pageSize.height(), pageSize.width());
			}
			
			String scale=extractPage.getScale();
			
			float leftRightMargin = extractPage.getLeftRightMargin();
			float topBottomMargin = extractPage.getTopBottomMargin();
			float horizontalSpacing = extractPage.getHorizontalSpacing();
			float verticalSpacing = extractPage.getVerticalSpacing();
			
			Rectangle unitSize = null;
			if(scale.equals("Auto")){
				float totalHorizontalSpacing = (coloumns - 1) * horizontalSpacing;
				
				int totalWidth = (int) (newSize.width() - leftRightMargin * 2 - totalHorizontalSpacing);
				int unitWidth = totalWidth / coloumns;
				
				float totalVerticalSpacing = (rows - 1) * verticalSpacing;
				
				int totalHeight = (int) (newSize.height() - topBottomMargin * 2 - totalVerticalSpacing);
				int unitHeight = totalHeight / rows;
				
				unitSize = new Rectangle(unitWidth, unitHeight);
				
			}else if(scale.equals("Use Original Size")){
				unitSize = null;
			}else if(scale.equals("Specified")){
				unitSize = new Rectangle(extractPage.getScaleWidth(), extractPage.getScaleHeight());
			}
			
			int order = extractPage.getPageOrdering();
			
			int pagesPerPage = rows * coloumns;
			
			int repeats = 1;
			if (extractPage.getRepeat() == REPEAT_AUTO)
				repeats = coloumns * rows;
			else if (extractPage.getRepeat() == REPEAT_SPECIFIED)
				repeats = extractPage.getCopies();
			
			Document document = new Document(newSize, 0, 0, 0, 0);
			
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
			
			document.open();

			PdfContentByte cb = writer.getDirectContent();
			PdfImportedPage importedPage;
			float offsetX = 0, offsetY = 0, factor;
			int actualPage = 0, page = 0;
			Rectangle currentSize;
			
			boolean isProportional = extractPage.isScaleProportional();
			
			for (int i = 1; i <= pageCount; i++) {
				if (pagesToEdit.contains(new Integer(i))) {
					for (int j = 0; j < repeats; j++) {
						
						int currentUnit = page % pagesPerPage;
						
						if (currentUnit == 0) {
							document.newPage();
							actualPage++;
						}
						
						currentSize = reader.getPageSizeWithRotation(i);
						if(unitSize == null)
							unitSize = currentSize;
						
						int currentColoumn = 0, currentRow = 0;
						if(order == ORDER_DOWN){
							currentColoumn = currentUnit / rows;
							currentRow = currentUnit % rows;
							
							offsetX = unitSize.width() * currentColoumn;
							offsetY = newSize.height() - (unitSize.height() * (currentRow + 1));
							
						}else if(order == ORDER_ACCROS){
							currentColoumn = currentUnit % coloumns;
							currentRow = currentUnit / coloumns;
							
							offsetX = unitSize.width() * currentColoumn;
							offsetY = newSize.height() - (unitSize.height() * ((currentUnit / coloumns) + 1));
							
						}  
						
						factor = Math.min(unitSize.width() / currentSize.width(), unitSize.height() / currentSize.height());
						
						float widthFactor = factor, heightFactor = factor;
						if(!isProportional){
							widthFactor = unitSize.width() / currentSize.width();
							heightFactor = unitSize.height() / currentSize.height();
						}else{
							offsetX += ((unitSize.width() - (currentSize.width() * factor)) / 2f);
							offsetY += ((unitSize.height() - (currentSize.height() * factor)) / 2f);
						}
						
						offsetX += (horizontalSpacing * currentColoumn) + leftRightMargin;
						offsetY -= ((verticalSpacing * currentRow) + topBottomMargin);
						
						importedPage = writer.getImportedPage(reader, i);
						
						double rotation = currentSize.getRotation() * Math.PI / 180;
						
						/**
						 * see 
						 * http://itextdocs.lowagie.com/tutorial/directcontent/coordinates/index.html 
						 * for information about transformation matrices, and the coordinate system
						 */
						
						int mediaBoxX = -currentPageData.getMediaBoxX(i);
						int mediaBoxY = -currentPageData.getMediaBoxY(i);
						
						float a,b,c,d,e,f;
						switch (currentSize.getRotation()) {
						case 0:
							a = widthFactor;
							b = 0;
							c = 0;
							d = heightFactor;
							e = offsetX + (mediaBoxX * widthFactor);
							f = offsetY + (mediaBoxY * heightFactor);
							
							cb.addTemplate(importedPage, a, b, c, d, e, f);
							
							break;
						case 90:
							a = 0;
							b = (float) (Math.sin(rotation) * -heightFactor);
							c = (float) (Math.sin(rotation) * widthFactor);
							d = 0;
							e = offsetX + (mediaBoxY * widthFactor);
							f = ((currentSize.height() * heightFactor) + offsetY) - (mediaBoxX * heightFactor);
							
							cb.addTemplate(importedPage, a, b, c, d, e, f);
							
							break;
						case 180:
							a = (float) (Math.cos(rotation) * widthFactor);
							b = 0;
							c = 0;
							d = (float) (Math.cos(rotation) * heightFactor);
							e = (offsetX + (currentSize.width() * widthFactor)) - (mediaBoxX * widthFactor);
							f = ((currentSize.height() * heightFactor) + offsetY) - (mediaBoxY * heightFactor);
							
							cb.addTemplate(importedPage, a, b, c, d, e, f);
							
							break;
						case 270:
							a = 0;
							b = (float) (Math.sin(rotation) * -heightFactor);
							c = (float) (Math.sin(rotation) * widthFactor);
							d = 0;
							e = (offsetX + (currentSize.width() * widthFactor)) - (mediaBoxY * widthFactor);
							f = offsetY + (mediaBoxX * heightFactor);
							
							cb.addTemplate(importedPage, a, b, c, d, e, f);
							
							break;
						}
						
						
						page++;
					}
				}
			}
			
			document.close();
			
			currentGUI.showMessageDialog(Messages
					.getMessage("PdfViewerMessage.PagesSavedAsPdfTo")
					+ ' ' + output_dir);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public void handouts(String file) {
		try {
			File src = new File(selectedFile);

			File dest = new File(file);

			int pages = 4;

			float x1 = 30f;
			float x2 = 280f;
			float x3 = 320f;
			float x4 = 565f;

			float[] y1 = new float[pages];
			float[] y2 = new float[pages];

			float height = (778f - (20f * (pages - 1))) / pages;
			y1[0] = 812f;
			y2[0] = 812f - height;

			for (int i = 1; i < pages; i++) {
				y1[i] = y2[i - 1] - 20f;
				y2[i] = y1[i] - height;
			}

			// we create a reader for a certain document
			PdfReader reader = new PdfReader(src.getAbsolutePath());
			// we retrieve the total number of pages
			int n = reader.getNumberOfPages();

			// step 1: creation of a document-object
			Document document = new Document(PageSize.A4);
			// step 2: we create a writer that listens to the document
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(dest));
			// step 3: we open the document
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			PdfImportedPage page;
			int rotation;
			int i = 0;
			int p = 0;
			// step 4: we add content
			while (i < n) {
				i++;
				Rectangle rect = reader.getPageSizeWithRotation(i);
				float factorx = (x2 - x1) / rect.width();
				float factory = (y1[p] - y2[p]) / rect.height();
				float factor = (factorx < factory ? factorx : factory);
				float dx = (factorx == factor ? 0f : ((x2 - x1) - rect.width()
						* factor) / 2f);
				float dy = (factory == factor ? 0f : ((y1[p] - y2[p]) - rect
						.height()
						* factor) / 2f);
				page = writer.getImportedPage(reader, i);
				rotation = reader.getPageRotation(i);
				if (rotation == 90 || rotation == 270) {
					cb.addTemplate(page, 0, -factor, factor, 0, x1 + dx, y2[p]
							+ dy + rect.height() * factor);
				} else {
					cb.addTemplate(page, factor, 0, 0, factor, x1 + dx, y2[p]
							+ dy);
				}
				cb.setRGBColorStroke(0xC0, 0xC0, 0xC0);
				cb.rectangle(x3 - 5f, y2[p] - 5f, x4 - x3 + 10f, y1[p] - y2[p]
						+ 10f);
				for (float l = y1[p] - 19; l > y2[p]; l -= 16) {
					cb.moveTo(x3, l);
					cb.lineTo(x4, l);
				}
				cb.rectangle(x1 + dx, y2[p] + dy, rect.width() * factor, rect
						.height()
						* factor);
				cb.stroke();

				p++;
				if (p == pages) {
					p = 0;
					document.newPage();
				}
			}
			// step 5: we close the document
			document.close();
		} catch (Exception e) {

			System.err.println(e.getMessage());
		}
	}

	public void add(int pageCount, PdfPageData currentPageData,
			InsertBlankPDFPage addPage) {
		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null,new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		int pageToInsertBefore = addPage.getInsertBefore();

		boolean insertAsLastPage = false;
		if (pageToInsertBefore == -1)
			return;
		else if (pageToInsertBefore == -2)
			insertAsLastPage = true;

		try {
			PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(
					selectedFile));

			if (insertAsLastPage)
				stamp.insertPage(pageCount + 1, reader
						.getPageSizeWithRotation(pageCount));
			else
				stamp.insertPage(pageToInsertBefore, reader
						.getPageSizeWithRotation(pageToInsertBefore));

			stamp.close();
		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	public void rotate(int pageCount, PdfPageData currentPageData,
			RotatePDFPages current_selection) {
		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null,new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		try {
			int[] pgsToRotate = current_selection.getRotatedPages();

			if (pgsToRotate == null)
				return;

			int check = -1;

			if (pgsToRotate.length == 1) {
				check = currentGUI.showConfirmDialog(Messages
						.getMessage("PdfViewerMessage.ConfirmRotatePages"),
						Messages.getMessage("PdfViewerMessage.Confirm"),
						JOptionPane.YES_NO_OPTION);
			} else {
				check = currentGUI.showConfirmDialog(Messages
						.getMessage("PdfViewerMessage.ConfirmRotatePages"),
						Messages.getMessage("PdfViewerMessage.Confirm"),
						JOptionPane.YES_NO_OPTION);
			}

			if (check != 0)
				return;

			if (pgsToRotate == null)
				return;

			List pagesToRotate = new ArrayList();
			for (int i = 0; i < pgsToRotate.length; i++)
				pagesToRotate.add(new Integer(pgsToRotate[i]));

			int direction = current_selection.getDirection();

			PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			for (int page = 1; page <= pageCount; page++) {
				if (pagesToRotate.contains(new Integer(page))) {
					// int currentRotation =
					// Integer.parseInt(reader.getPageN(page).get(PdfName.ROTATE).toString());

					int currentRotation = currentPageData.getRotation(page);

					if (direction == ROTATECLOCKWISE)
						reader.getPageN(page).put(PdfName.ROTATE,
								new PdfNumber((currentRotation + 90) % 360));
					else if (direction == ROTATECOUNTERCLOCKWISE)
						reader.getPageN(page).put(PdfName.ROTATE,
								new PdfNumber((currentRotation - 90) % 360));
					else if (direction == ROTATE180)
						reader.getPageN(page).put(PdfName.ROTATE,
								new PdfNumber((currentRotation + 180) % 360));
					else
						throw new Exception("invalid desired rotation");
				}

			}

			PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(
					selectedFile));
			stamp.close();

		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	public void setCrop(int pageCount, PdfPageData currentPageData,
			CropPDFPages cropPage) {
		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null, new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		try {

			int[] pgsToEdit = cropPage.getPages();

			if (pgsToEdit == null)
				return;

			List pagesToEdit = new ArrayList();
			for (int i = 0; i < pgsToEdit.length; i++)
				pagesToEdit.add(new Integer(pgsToEdit[i]));

			PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			boolean applyToCurrent = cropPage.applyToCurrentCrop();

			for (int page = 1; page <= pageCount; page++) {
				if (pagesToEdit.contains(new Integer(page))) {

					float currentLeftCrop = currentPageData.getCropBoxX(page);
					float currentBottomCrop = currentPageData.getCropBoxY(page);
					float currentRightCrop = currentPageData
							.getCropBoxWidth(page)
							+ currentLeftCrop;
					float currentTopCrop = currentPageData
							.getCropBoxHeight(page)
							+ currentBottomCrop;

					float[] newCrop = cropPage.getCrop();

					if (applyToCurrent) {
						newCrop[0] = currentLeftCrop + newCrop[0];
						newCrop[1] = currentBottomCrop + newCrop[1];
						newCrop[2] = currentRightCrop - newCrop[2];
						newCrop[3] = currentTopCrop - newCrop[3];
					} else {
						newCrop[2] = reader.getPageSize(page).width()
								- newCrop[2];
						newCrop[3] = reader.getPageSize(page).height()
								- newCrop[3];
					}

					reader.getPageN(page).put(PdfName.CROPBOX,
							new PdfArray(newCrop));
				}
			}

			PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(
					selectedFile));
			stamp.close();

		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	public void delete(int pageCount, PdfPageData currentPageData,
			DeletePDFPages deletedPages) {
		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null, new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		try {
			int[] pgsToDelete = deletedPages.getDeletedPages();

			if (pgsToDelete == null)
				return;

			int check = -1;

			if (pgsToDelete.length == 1) {
				check = currentGUI.showConfirmDialog(Messages
						.getMessage("PdfViewerMessage.ConfirmDeletePage"),
						Messages.getMessage("PdfViewerMessage.Confirm"),
						JOptionPane.YES_NO_OPTION);
			} else {
				check = currentGUI.showConfirmDialog(Messages
						.getMessage("PdfViewerMessage.ConfirmDeletePage"),
						Messages.getMessage("PdfViewerMessage.Confirm"),
						JOptionPane.YES_NO_OPTION);
			}

			if (check != 0)
				return;

			if (pgsToDelete == null)
				return;

			List pagesToDelete = new ArrayList();
			for (int i = 0; i < pgsToDelete.length; i++)
				pagesToDelete.add(new Integer(pgsToDelete[i]));

			PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			List bookmarks = SimpleBookmark.getBookmark(reader);

			// int[][] xx = new int[pgsToDelete.length][1];
			// for(int i=0; i<pgsToDelete.length;i++){
			// xx[i][0] = pgsToDelete[i];
			// }
			//			
			// PageRanges pr = new PageRanges(xx);
			// int[] toRemove = linearize(pr.getMembers());
			//			
			// SimpleBookmark.eliminatePages(bookmarks,toRemove);
			SimpleBookmark.shiftPageNumbers(bookmarks, -1, new int[] { 5, 5 });

			// if(1==1)
			// return;
			
			/**
			 * check document will have at leat 1 page
			 */
			boolean pageAdded=false;

			for (int page = 1; page <= pageCount; page++) {
				if (!pagesToDelete.contains(new Integer(page))) {
					pageAdded=true;
					page = pageCount;
				}
			}
			
			if(!pageAdded){
				currentGUI.showMessageDialog(Messages
						.getMessage("PdfViewerError.PageWillNotDelete"));
				return ;
			}

			Document document = new Document();
			PdfCopy writer = new PdfCopy(document, new FileOutputStream(
					selectedFile));

			document.open();
			
			for (int page = 1; page <= pageCount; page++) {
				if (!pagesToDelete.contains(new Integer(page))) {
					PdfImportedPage pip = writer.getImportedPage(reader, page);

					writer.addPage(pip);
					pageAdded=true;
				}
			}

			writer.setOutlines(bookmarks);

			document.close();

		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	public void stampImage(int pageCount, PdfPageData currentPageData,
			final StampImageToPDFPages stampImage) {
		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null, new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		try {

			int[] pgsToEdit = stampImage.getPages();

			if (pgsToEdit == null)
				return;

			File fileToTest = new File(stampImage.getImageLocation());
			if (!fileToTest.exists()) {
				currentGUI.showMessageDialog(Messages
						.getMessage("PdfViewerError.ImageDoesNotExist"));
				return;
			}

			List pagesToEdit = new ArrayList();
			for (int i = 0; i < pgsToEdit.length; i++)
				pagesToEdit.add(new Integer(pgsToEdit[i]));

			final PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			int n = reader.getNumberOfPages();

			PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(
					selectedFile));

			Image img = Image.getInstance(fileToTest.getAbsolutePath());

			int chosenWidthScale = stampImage.getWidthScale();
			int chosenHeightScale = stampImage.getHeightScale();

			img.scalePercent(chosenWidthScale, chosenHeightScale);

			String chosenPlacement = stampImage.getPlacement();

			int chosenRotation = stampImage.getRotation();
			img.setRotationDegrees(chosenRotation);

			String chosenHorizontalPosition = stampImage
					.getHorizontalPosition();
			String chosenVerticalPosition = stampImage.getVerticalPosition();

			float chosenHorizontalOffset = stampImage.getHorizontalOffset();
			float chosenVerticalOffset = stampImage.getVerticalOffset();

			for (int page = 0; page <= n; page++) {
				if (pagesToEdit.contains(new Integer(page))) {

					PdfContentByte cb;
					if (chosenPlacement.equals("Overlay"))
						cb = stamp.getOverContent(page);
					else
						cb = stamp.getUnderContent(page);

					int currentRotation = currentPageData.getRotation(page);
					Rectangle pageSize;
					if (currentRotation == 90 || currentRotation == 270)
						pageSize = reader.getPageSize(page).rotate();
					else
						pageSize = reader.getPageSize(page);

					float startx, starty;
					if (chosenVerticalPosition.equals("From the top")) {
						starty = pageSize.height()
								- ((img.height() * (chosenHeightScale / 100)) / 2);
					} else if (chosenVerticalPosition.equals("Centered")) {
						starty = (pageSize.height() / 2)
								- ((img.height() * (chosenHeightScale / 100)) / 2);
					} else {
						starty = 0;
					}

					if (chosenHorizontalPosition.equals("From the left")) {
						startx = 0;
					} else if (chosenHorizontalPosition.equals("Centered")) {
						startx = (pageSize.width() / 2)
								- ((img.width() * (chosenWidthScale / 100)) / 2);
					} else {
						startx = pageSize.width()
								- ((img.width() * (chosenWidthScale / 100)) / 2);
					}

					img.setAbsolutePosition(startx + chosenHorizontalOffset,
							starty + chosenVerticalOffset);

					cb.addImage(img);
				}
			}

			stamp.close();

		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	public void stampText(int pageCount, PdfPageData currentPageData,
			final StampTextToPDFPages stampText) {
		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null, new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		try {

			int[] pgsToEdit = stampText.getPages();

			if (pgsToEdit == null)
				return;

			List pagesToEdit = new ArrayList();
			for (int i = 0; i < pgsToEdit.length; i++)
				pagesToEdit.add(new Integer(pgsToEdit[i]));

			final PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(
					selectedFile));

			for (int page = 1; page <= pageCount; page++) {
				if (pagesToEdit.contains(new Integer(page))) {

					String chosenText = stampText.getText();

					if (chosenText.length() != 0) {

						String chosenFont = stampText.getFontName();
						Color chosenFontColor = stampText.getFontColor();
						int chosenFontSize = stampText.getFontSize();

						int chosenRotation = stampText.getRotation();
						String chosenPlacement = stampText.getPlacement();

						String chosenHorizontalPosition = stampText
								.getHorizontalPosition();
						String chosenVerticalPosition = stampText
								.getVerticalPosition();

						float chosenHorizontalOffset = stampText
								.getHorizontalOffset();
						float chosenVerticalOffset = stampText
								.getVerticalOffset();

						BaseFont font = BaseFont.createFont(chosenFont,
								BaseFont.WINANSI, false);

						PdfContentByte cb;
						if (chosenPlacement.equals("Overlay"))
							cb = stamp.getOverContent(page);
						else
							cb = stamp.getUnderContent(page);

						cb.beginText();
						cb.setColorFill(chosenFontColor);
						cb.setFontAndSize(font, chosenFontSize);

						int currentRotation = currentPageData.getRotation(page);
						Rectangle pageSize;
						if (currentRotation == 90 || currentRotation == 270)
							pageSize = reader.getPageSize(page).rotate();
						else
							pageSize = reader.getPageSize(page);

						float startx;
						float starty;

						if (chosenVerticalPosition.equals("From the top")) {
							starty = pageSize.height();
						} else if (chosenVerticalPosition.equals("Centered")) {
							starty = pageSize.height() / 2;
						} else {
							starty = 0;
						}

						if (chosenHorizontalPosition.equals("From the left")) {
							startx = 0;
						} else if (chosenHorizontalPosition.equals("Centered")) {
							startx = pageSize.width() / 2;
						} else {
							startx = pageSize.width();
						}

						cb.showTextAligned(Element.ALIGN_CENTER, chosenText,
								startx + chosenHorizontalOffset, starty
										+ chosenVerticalOffset, chosenRotation);
						cb.endText();
					}
				}
			}

			stamp.close();

		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	public void addHeaderFooter(int pageCount, PdfPageData currentPageData,
			final AddHeaderFooterToPDFPages addHeaderFooter) {
		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null, new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		try {

			int[] pgsToEdit = addHeaderFooter.getPages();

			if (pgsToEdit == null)
				return;

			List pagesToEdit = new ArrayList();
			for (int i = 0; i < pgsToEdit.length; i++)
				pagesToEdit.add(new Integer(pgsToEdit[i]));

			final PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(
					selectedFile));

			String chosenFont = addHeaderFooter.getFontName();
			Color chosenFontColor = addHeaderFooter.getFontColor();
			int chosenFontSize = addHeaderFooter.getFontSize();

			float chosenLeftRightMargin = addHeaderFooter.getLeftRightMargin();
			float chosenTopBottomMargin = addHeaderFooter.getTopBottomMargin();

			String text[] = new String[6];
			text[0] = addHeaderFooter.getLeftHeader();
			text[1] = addHeaderFooter.getCenterHeader();
			text[2] = addHeaderFooter.getRightHeader();
			text[3] = addHeaderFooter.getLeftFooter();
			text[4] = addHeaderFooter.getCenterFooter();
			text[5] = addHeaderFooter.getRightFooter();

			Date date = new Date();
			String shortDate = DateFormat.getDateInstance(DateFormat.SHORT)
					.format(date);
			String longDate = DateFormat.getDateInstance(DateFormat.LONG)
					.format(date);

			SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
			String time12 = formatter.format(date);

			formatter = new SimpleDateFormat("HH.mm.ss");
			String time24 = formatter.format(date);

			String fileName = new File(selectedFile).getName();

			BaseFont font = BaseFont.createFont(chosenFont, BaseFont.WINANSI,
					false);

			for (int page = 1; page <= pageCount; page++) {
				if (pagesToEdit.contains(new Integer(page))) {
					String[] textCopy = new String[text.length];
					System.arraycopy(text, 0, textCopy, 0, text.length);

					for (int i = 0; i < 6; i++) {
						textCopy[i] = textCopy[i].replaceAll("<d>", shortDate);
						textCopy[i] = textCopy[i].replaceAll("<D>", longDate);
						textCopy[i] = textCopy[i].replaceAll("<t>", time12);
						textCopy[i] = textCopy[i].replaceAll("<T>", time24);
						textCopy[i] = textCopy[i].replaceAll("<f>", fileName);
						textCopy[i] = textCopy[i].replaceAll("<F>",
								selectedFile);
						textCopy[i] = textCopy[i].replaceAll("<p>", String.valueOf(page));
						textCopy[i] = textCopy[i].replaceAll("<P>", String.valueOf(pageCount));
					}

					PdfContentByte cb = stamp.getOverContent(page);

					cb.beginText();
					cb.setColorFill(chosenFontColor);
					cb.setFontAndSize(font, chosenFontSize);

					Rectangle pageSize = reader.getPageSizeWithRotation(page);

					cb.showTextAligned(Element.ALIGN_LEFT, textCopy[0],
							chosenLeftRightMargin, pageSize.height()
									- chosenTopBottomMargin, 0);
					cb.showTextAligned(Element.ALIGN_CENTER, textCopy[1],
							pageSize.width() / 2, pageSize.height()
									- chosenTopBottomMargin, 0);
					cb.showTextAligned(Element.ALIGN_RIGHT, textCopy[2],
							pageSize.width() - chosenLeftRightMargin, pageSize
									.height()
									- chosenTopBottomMargin, 0);

					cb.showTextAligned(Element.ALIGN_LEFT, textCopy[3],
							chosenLeftRightMargin, chosenTopBottomMargin, 0);
					cb.showTextAligned(Element.ALIGN_CENTER, textCopy[4],
							pageSize.width() / 2, chosenTopBottomMargin, 0);
					cb.showTextAligned(Element.ALIGN_RIGHT, textCopy[5],
							pageSize.width() - chosenLeftRightMargin,
							chosenTopBottomMargin, 0);

					cb.endText();
				}
			}

			stamp.close();

		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	public void encrypt(int pageCount, PdfPageData currentPageData,
			EncryptPDFDocument encryptPage) {
		String p = encryptPage.getPermissions();
		int encryptionLevel = encryptPage.getEncryptionLevel();
		String userPassword = encryptPage.getUserPassword();
		String masterPassword = encryptPage.getMasterPassword();

		int permit[] = { PdfWriter.AllowPrinting,
				PdfWriter.AllowModifyContents, PdfWriter.AllowCopy,
				PdfWriter.AllowModifyAnnotations, PdfWriter.AllowFillIn };

		int permissions = 0;
		for (int i = 0; i < p.length(); ++i) {
			permissions |= (p.charAt(i) == '0' ? 0 : permit[i]);
		}

		File tempFile = null;

		try {
			tempFile = File.createTempFile("temp", null, new File(ObjectStore.temp_dir));

			ObjectStore.copy(selectedFile, tempFile.getAbsolutePath());
		} catch (Exception e) {
			return;
		}

		try {
			PdfReader reader = new PdfReader(tempFile.getAbsolutePath());

			PdfEncryptor.encrypt(reader, new FileOutputStream(selectedFile),
					userPassword.getBytes(), masterPassword.getBytes(),
					permissions, encryptionLevel == 0);

		} catch (Exception e) {

			ObjectStore.copy(tempFile.getAbsolutePath(), selectedFile);

			e.printStackTrace();

		} finally {
			tempFile.delete();
		}
	}

	private static int[] linearize(final int[][] input) {
		// if you already know that all sub arrays of input are of the same
		// size,
		// you do not need to determine the size iteratively
		int size = 0;
		for (int k = 0; k < input.length; ++k) {
			size += input[k].length;
		}

		final int[] output = new int[size];
		int offset = 0;
		for (int k = 0; k < input.length; ++k) {
			System.arraycopy(input[k], 0, output, offset, input[k].length);
			offset += input[k].length;
		}

		return output;
	}

}
