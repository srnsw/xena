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
* MultiViewTransferHandler.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;

public class MultiViewTransferHandler extends BaseTransferHandler {

	private int fileCount = 0;
	
	public MultiViewTransferHandler(Values commonValues, GUIThumbnailPanel thumbnails, SwingGUI currentGUI, Commands currentCommands) {
		super(commonValues, thumbnails, currentGUI, currentCommands);
	}

	public boolean importData(JComponent src, Transferable transferable) {
		try {
			Object dragImport = getImport(transferable);

			if (dragImport instanceof String) {
				String url = (String) dragImport;
				System.out.println(url);
				String testURL = url.toLowerCase();
				if (testURL.startsWith("http:/")) {
					currentCommands.openTransferedFile(testURL);
					return true;
				} else if (testURL.startsWith("file:/")) {
					String[] urls = url.split("file:/");
					
					List files = new LinkedList();
					for (int i = 0; i < urls.length; i++) {
						String file = urls[i];
						if(file.length() > 0){
							File file2 = new File(new URL("file:/"+file).getFile());
							System.out.println(file2);
							files.add(file2);
						}
					}
					
					return openFiles(files);
				}
			} else if (dragImport instanceof List) {
				List files = (List) dragImport;
				
				return openFiles(files);
			}
		} catch (Exception e) {
		}
		
		return false;
	}

	private boolean openFiles(List files) {
		fileCount = 0;
		List flattenedFiles = getFlattenedFiles(files, new ArrayList());
		
		if (fileCount == commonValues.getMaxMiltiViewers()) {
			currentGUI.showMessageDialog("You have choosen to import more files than your current set " + 
					"maximum (" + commonValues.getMaxMiltiViewers() + ").  Only the first " + 
					commonValues.getMaxMiltiViewers() + " files will be imported.\nYou can change this value " +
							"in View | Preferences", 
					"Maximum number of files reached", JOptionPane.INFORMATION_MESSAGE);
		}
		
		List[] filterdFiles = filterFiles(flattenedFiles);
		final List allowedFiles = filterdFiles[0];
		List disAllowedFiles = filterdFiles[1];
		
		int noOfDisAllowedFiles = disAllowedFiles.size();
		int noOfAllowedFiles = allowedFiles.size();
		
		if(noOfDisAllowedFiles > 0) {
			String unOpenableFiles = "";
			for (Iterator it = disAllowedFiles.iterator(); it.hasNext();) {
				String file = (String) it.next();
				String fileName = new File(file).getName();
				unOpenableFiles += fileName + "\n";
			}
			
			int result = currentGUI.showConfirmDialog("You have selected " + flattenedFiles.size() + 
					" files to open.  The following file(s) cannot be opened\nas they are not valid PDFs " +
					"or images.\n" + unOpenableFiles + "\nWould you like to open the remaining " + 
					noOfAllowedFiles + " files?", "File Import", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (result == JOptionPane.NO_OPTION) {
				return false;
			}
		} 
		
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				for (Iterator it = allowedFiles.iterator(); it.hasNext();) {
					final String file = (String) it.next();
					
					try {
						currentCommands.openTransferedFile(file);
					} catch (Exception e) {
						
						int result;
						if (allowedFiles.size() == 1) {
							currentGUI.showMessageDialog(Messages.getMessage("PdfViewerOpenerror"), commonValues.getSelectedFile(), JOptionPane.ERROR_MESSAGE);
							result = JOptionPane.NO_OPTION;
						} else {
							result = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerOpenerror")+". Continue opening remaining files?", commonValues.getSelectedFile(), 
									JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							
						}

						currentGUI.closeMultiViewerWindow(commonValues.getSelectedFile());
						
						if(result == JOptionPane.NO_OPTION) {
							return null;
						}
					}
				}
				return null;
			}
		};
		worker.start();
		
		
//				while (currentCommands.openingTransferedFiles()) {
//					Thread.sleep(250);
//				}
//				
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						JInternalFrame[] allFrames = currentGUI.getMultiViewerFrames().getAllFrames();
//
//						for (int i = allFrames.length - 1; i >= 0; i--) {
//							JInternalFrame pdf = allFrames[i];
//
//							pdf.updateUI();
//							pdf.repaint();
//							try {
//								pdf.setSelected(true);
//							} catch (PropertyVetoException e) {
//								e.printStackTrace();
//							}
//						}
//						currentGUI.getMultiViewerFrames().repaint();
//					}
//				});
		
		return true;
	}

	private List[] filterFiles(List flattenedFiles) {
		List allowedFiles = new LinkedList();
		List disAllowedFiles = new LinkedList();

		for (Iterator it = flattenedFiles.iterator(); it.hasNext();) {
			String file = ((String) it.next());
			String testFile = file.toLowerCase();

			boolean isValid = ((testFile.endsWith(".pdf")) || (testFile.endsWith(".fdf")) || 
					(testFile.endsWith(".tif")) || (testFile.endsWith(".tiff")) || 
					(testFile.endsWith(".png")) || (testFile.endsWith(".jpg")) || 
					(testFile.endsWith(".jpeg")));

			if (isValid) {
				allowedFiles.add(file);
			} else {
				disAllowedFiles.add(file);
			}
		}
		
		return new List[] { allowedFiles, disAllowedFiles };
	}

	private List getFlattenedFiles(List files, List flattenedFiles) {
		for (Iterator it = files.iterator(); it.hasNext();) {
			if (fileCount == commonValues.getMaxMiltiViewers()) {
				return flattenedFiles;
			}

			File file = (File) it.next();
//			System.out.println(file);
			if(file.isDirectory()){
				getFlattenedFiles(Arrays.asList(file.listFiles()), flattenedFiles);
			} else {
				flattenedFiles.add(file.getAbsolutePath());

				fileCount++;
			}
		}
		
		return flattenedFiles;
	}

//	protected void openTransferedFile(String file) {
//		String testFile = file.toLowerCase();
//		
//		boolean isValid = ((testFile.endsWith(".pdf"))
//				|| (testFile.endsWith(".fdf")) || (testFile.endsWith(".tif"))
//				|| (testFile.endsWith(".tiff")) || (testFile.endsWith(".png"))
//				|| (testFile.endsWith(".jpg")) || (testFile.endsWith(".jpeg")));
//	
//		if (isValid) {
//			currentCommands.openTransferedFile(file);
//		} else {
//			currentGUI.showMessageDialog("You may only import a valid PDF or image");
//		}
//	}	
}
