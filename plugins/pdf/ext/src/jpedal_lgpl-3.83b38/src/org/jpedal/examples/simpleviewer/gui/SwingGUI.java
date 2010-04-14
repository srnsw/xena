
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
 * SwingGUI.java
 * ---------------
 */
package org.jpedal.examples.simpleviewer.gui;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.parser.DecodeStatus;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.generic.GUIButton;
import org.jpedal.examples.simpleviewer.gui.generic.GUICombo;
import org.jpedal.examples.simpleviewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.examples.simpleviewer.gui.popups.SwingProperties;
import org.jpedal.examples.simpleviewer.gui.swing.*;
import org.jpedal.examples.simpleviewer.utils.Printer;
import org.jpedal.examples.simpleviewer.utils.PropertiesFile;
import org.jpedal.exception.PdfException;
import org.jpedal.external.JPedalCustomDrawObject;
import org.jpedal.external.Options;
import org.jpedal.fonts.FontMappings;
import org.jpedal.gui.GUIFactory;
import org.jpedal.gui.ShowGUIMessage;
import org.jpedal.io.JAIHelper;
import org.jpedal.io.StatusBar;
import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.formData.GUIData;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;
import org.jpedal.utils.repositories.Vector_Int;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.Box.Filler;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * <br>Description: Swing GUI functions in Viewer
 *
 *
 */
public class SwingGUI extends GUI implements GUIFactory {

	boolean finishedDecoding=false;
	static final int startSize=30,expandedSize=190;
	String pageTitle,bookmarksTitle, signaturesTitle,layersTitle;
	boolean hasListener=false;
	private boolean isSetup=false;
	int lastTabSelected=-1;
	public boolean messageShown=false;

	//example code showing how we can add icons and then track
	public boolean addUniqueIconToFileAttachment=false;

	ButtonGroup layoutGroup = new ButtonGroup();

	ButtonGroup searchLayoutGroup = new ButtonGroup();

	ButtonGroup borderGroup = new ButtonGroup();

	/**listener on buttons, menus, combboxes to execute options (one instance on all objects)*/
	private CommandListener currentCommandListener;

	/**holds OPEN, INFO,etc*/
	private JToolBar topButtons = new JToolBar();

	/**holds back/forward buttons at bottom of page*/
	private JToolBar navButtons = new JToolBar();

	/**holds rotation, quality, scaling and status*/
	private JToolBar comboBoxBar = new JToolBar();

	/**holds all menu entries (File, View, Help)*/
	private JMenuBar currentMenu =new JMenuBar();

	/**tell user on first form change it can be saved*/
	private boolean firstTimeFormMessage=true;

	/** visual display of current cursor co-ords on page*/
	private JLabel coords=new JLabel();

	/**root element to hold display*/
	private Container frame=new JFrame();

	/** alternative internal JFrame*/
	private JDesktopPane desktopPane=new JDesktopPane();

	/**flag to disable functions*/
	boolean isSingle=true;

	/**displayed on left to hold thumbnails, bookmarks*/
	private JTabbedPane navOptionsPanel=new JTabbedPane();

	/**split display between PDF and thumbnails/bookmarks*/
	private JSplitPane displayPane;


	/**Scrollpane for pdf panel*/
	private JScrollPane scrollPane = new JScrollPane();

	private Font headFont=new Font("SansSerif",Font.BOLD,14);

	private Font textFont=new Font("Serif",Font.PLAIN,12);

	/**Interactive display object - needs to be added to PdfDecoder*/
	private StatusBar statusBar=new StatusBar(Color.orange);

	private JLabel pageCounter1;

	public JTextField pageCounter2 = new JTextField(4);

	private JLabel pageCounter3;

	private JLabel optimizationLabel;

	private JTree signaturesTree;

	private JPanel layersPanel=new JPanel();

	/**user dir in which program can write*/
	private String user_dir = System.getProperty( "user.dir" );

	/**stop user forcing open tab before any pages loaded*/
	private boolean tabsNotInitialised=true;
	private JToolBar navToolBar = new JToolBar();
	private JToolBar pagesToolBar = new JToolBar();


	//Optional Buttons for menu Search
	public GUIButton nextSearch,previousSearch;

	//layers tab
	PdfLayerList layersObject;

	//Progress bar on nav bar
	private final JProgressBar memoryBar = new JProgressBar();

	//Component to display cursor position on page
	JToolBar cursor = new JToolBar();

	//Buttons on the function bar
	private GUIButton openButton;
	private GUIButton printButton;
	private GUIButton searchButton;
	private GUIButton docPropButton;
	private GUIButton infoButton;

	//Menu items for gui
	private JMenu fileMenu;
	private JMenu openMenu;
	private JMenuItem open;
	private JMenuItem openUrl;
	private JMenuItem save;
	private JMenuItem reSaveAsForms;
	private JMenuItem find;
	private JMenuItem documentProperties;
	private JMenuItem print;
	private JMenuItem recentDocuments;
	private JMenuItem exit;
	private JMenu editMenu;
	private JMenuItem copy;
	private JMenuItem selectAll;
	private JMenuItem deselectAll;
	private JMenuItem preferences;
	private JMenu viewMenu;
	private JMenu goToMenu;
	private JMenuItem firstPage;
	private JMenuItem backPage;
	private JMenuItem forwardPage;
	private JMenuItem lastPage;
	private JMenuItem goTo;
	private JMenuItem previousDocument;
	private JMenuItem nextDocument;
	private JMenu pageLayoutMenu;
	private JMenuItem single;
	private JMenuItem continuous;
	private JMenuItem facing;
	private JMenuItem continuousFacing;
	private JMenuItem sideScroll;
	private JMenuItem fullscreen;
	private JMenu windowMenu;
	private JMenuItem cascade;
	private JMenuItem tile;
	private JMenu exportMenu;
	private JMenu pdfMenu;
	private JMenuItem onePerPage;
	private JMenuItem nup;
	private JMenuItem handouts;
	private JMenu contentMenu;
	private JMenuItem images;
	private JMenuItem text;
	private JMenuItem bitmap;
	private JMenu pageToolsMenu;
	private JMenuItem rotatePages;
	private JMenuItem deletePages;
	private JMenuItem addPage;
	private JMenuItem addHeaderFooter;
	private JMenuItem stampText;
	private JMenuItem stampImage;
	private JMenuItem crop;
	private JMenu helpMenu;
	private JMenuItem visitWebsite;
	private JMenuItem tipOfTheDay;
	private JMenuItem checkUpdates;
	private JMenuItem about;



	public SwingGUI(PdfDecoder decode_pdf, Values commonValues, GUIThumbnailPanel thumbnails, PropertiesFile properties) {

		this.decode_pdf = decode_pdf;
		this.commonValues = commonValues;
		this.thumbnails = thumbnails;
		this.properties = properties;

		if (commonValues.isContentExtractor()) {
			titleMessage = "IDRsolutions Extraction Solution " + PdfDecoder.version + ' ';
			showOutlines = false;
		}

        //pass in SwingGUI so we can call via callback
        decode_pdf.addExternalHandler((Object)this,Options.SwingContainer);


		/**
		 * setup display multiview display
		 */
		if (isSingle) {
			desktopPane.setBackground(frame.getBackground());
			desktopPane.setVisible(true);
			if(frame instanceof JFrame)
				((JFrame)frame).getContentPane().add((Component) desktopPane, BorderLayout.CENTER);
			else
				frame.add((Component) desktopPane, BorderLayout.CENTER);

		}
	}

	public JComponent getDisplayPane() {
		return displayPane;
	}

	public JDesktopPane getMultiViewerFrames(){
		return desktopPane;
	}

	public void setPdfDecoder(PdfDecoder decode_pdf){
		this.decode_pdf = decode_pdf;
	}

	public void closeMultiViewerWindow(String selectedFile) {
		JInternalFrame[] allFrames = desktopPane.getAllFrames();
		for (int i = 0; i < allFrames.length; i++) {
			JInternalFrame internalFrame = allFrames[i];
			if (internalFrame.getTitle().equals(selectedFile)) {
				try {
					internalFrame.setClosed(true);
				} catch (PropertyVetoException e) {
				}
				break;
			}
		}
	}

	/**
	 * adjusty x co-ordinate shown in display for user to include
	 * any page centering
	 */
	public int AdjustForAlignment(int cx) {

		if(decode_pdf.getPageAlignment()== Display.DISPLAY_CENTERED){
			int width=decode_pdf.getBounds().width;
			int pdfWidth=decode_pdf.getPDFWidth();

			if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE)
				pdfWidth=(int)decode_pdf.getMaximumSize().getWidth();

			if(width>pdfWidth)
				cx=cx-((width-pdfWidth)/(2));
		}

		return cx;
	}

	public String getBookmark(String bookmark) {
		return tree.getPage(bookmark);
	}

	public void reinitialiseTabs(boolean showVisible) {
		if(properties.getValue("ShowSidetabbar").toLowerCase().equals("true")){

			if(!isSingle)
				return;

			if(!showVisible)
				displayPane.setDividerLocation(startSize);

			lastTabSelected=-1;

			if(commonValues.isContentExtractor()){
				navOptionsPanel.removeAll();
				displayPane.setDividerLocation(0);
			}else if(!commonValues.isPDF()){
				navOptionsPanel.setVisible(false);
			}else{
				navOptionsPanel.setVisible(true);
				/**
				 * add/remove optional tabs
				 */
				if(!decode_pdf.hasOutline()){

					int outlineTab=-1;

					if(PdfDecoder.isRunningOnMac){
						String tabName="";
						//see if there is an outlines tab
						for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
							if(navOptionsPanel.getTitleAt(jj).equals(bookmarksTitle))
								outlineTab=jj;
						}
					}else{
						String tabName="";
						//see if there is an outlines tab
						for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
							if(navOptionsPanel.getIconAt(jj).toString().equals(bookmarksTitle))
								outlineTab=jj;
						}
					}

					if(outlineTab!=-1)
						navOptionsPanel.remove(outlineTab);

				}else if(properties.getValue("Bookmarkstab").toLowerCase().equals("Bookmarkstab")){
					int outlineTab=-1;
					if(PdfDecoder.isRunningOnMac){
						String tabName="";
						//see if there is an outlines tab
						for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
							if(navOptionsPanel.getTitleAt(jj).equals(bookmarksTitle))
								outlineTab=jj;
						}

						if(outlineTab==-1)
							navOptionsPanel.addTab(bookmarksTitle,(SwingOutline) tree);
					}else{
						String tabName="";
						//see if there is an outlines tab
						for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
							if(navOptionsPanel.getIconAt(jj).toString().equals(bookmarksTitle))
								outlineTab=jj;
						}

						if(outlineTab==-1){
							VTextIcon textIcon2 = new VTextIcon(navOptionsPanel, bookmarksTitle, VTextIcon.ROTATE_LEFT);
							navOptionsPanel.addTab(null, textIcon2, (SwingOutline) tree);
						}
					}
				}

				/** handle signatures pane*/
				AcroRenderer currentFormRenderer = decode_pdf.getFormRenderer();

				boolean useNewCode = true;
				if(useNewCode) {

					Iterator signatureObjects = currentFormRenderer.getSignatureObjects();

					//System.out.println("signatureObjects = "+signatureObjects);

					if(signatureObjects != null){

						DefaultMutableTreeNode root = new DefaultMutableTreeNode("Signatures");

						DefaultMutableTreeNode signed = new DefaultMutableTreeNode(
						"The following people have digitally counter-signed this document");

						DefaultMutableTreeNode blank = new DefaultMutableTreeNode("The following signature fields are not signed");


						while(signatureObjects.hasNext()){

							FormObject formObj = (FormObject) signatureObjects.next();

							//@simon - old and new
							//can we lose getSignatureObject in Interface and class afterwards please
							//Map OLDsigObject =currentFormRenderer.getSignatureObject(formObj.getObjectRefAsString());

							PdfObject sigObject=formObj.getDictionary(PdfDictionary.V);//.getDictionary(PdfDictionary.Sig);

							//System.out.println("formObj = "+formObj+" "+formObj.getObjectRefAsString());

							//System.out.println("sigObject = "+sigObject);

							decode_pdf.getIO().checkResolved(formObj);

							if(sigObject == null){

								if(!blank.isNodeChild(root))
									root.add(blank);

								DefaultMutableTreeNode blankNode = new DefaultMutableTreeNode(formObj.getTextStreamValue(PdfDictionary.T)+ " on page " + formObj.getPageNumber());
								blank.add(blankNode);

							} else {

								if(!signed.isNodeChild(root))
									root.add(signed);

								//String name = (String) OLDsigObject.get("Name");

								String name=sigObject.getTextStreamValue(PdfDictionary.Name);

								DefaultMutableTreeNode owner = new DefaultMutableTreeNode("Signed by " + name);
								signed.add(owner);

								DefaultMutableTreeNode type = new DefaultMutableTreeNode("Type");
								owner.add(type);

								String filter = null;//sigObject.getName(PdfDictionary.Filter);

								//@simon -new version to test
								PdfArrayIterator filters = sigObject.getMixedArray(PdfDictionary.Filter);
								if(filters!=null && filters.hasMoreTokens())
									filter=filters.getNextValueAsString(true);

								DefaultMutableTreeNode filterNode = new DefaultMutableTreeNode("Filter: " + filter);
								type.add(filterNode);

								String subFilter = sigObject.getName(PdfDictionary.SubFilter);

								DefaultMutableTreeNode subFilterNode = new DefaultMutableTreeNode("Sub Filter: " + subFilter);
								type.add(subFilterNode);

								DefaultMutableTreeNode details = new DefaultMutableTreeNode("Details");
								owner.add(details);


								//@simon - guess on my part....
                                String rawDate=sigObject.getTextStreamValue(PdfDictionary.M);
                                //if(rawDate!=null){
                                    
                                    StringBuffer date = new StringBuffer(rawDate);

                                    date.delete(0, 2);
                                    date.insert(4, '/');
                                    date.insert(7, '/');
                                    date.insert(10, ' ');
                                    date.insert(13, ':');
                                    date.insert(16, ':');
                                    date.insert(19, ' ');

                                    DefaultMutableTreeNode time = new DefaultMutableTreeNode("Time: " +date);
                                    details.add(time);
                                //}

								String reason=sigObject.getTextStreamValue(PdfDictionary.Reason);

								DefaultMutableTreeNode reasonNode = new DefaultMutableTreeNode("Reason: " + reason);
								details.add(reasonNode);

								String location=sigObject.getTextStreamValue(PdfDictionary.Location);

								DefaultMutableTreeNode locationNode = new DefaultMutableTreeNode("Location: " + location);
								details.add(locationNode);

								DefaultMutableTreeNode field = new DefaultMutableTreeNode("Field: " + formObj.getTextStreamValue(PdfDictionary.T)+ " on page " + formObj.getPageNumber());
								details.add(field);
							}
						}
						if(signaturesTree==null){
							signaturesTree = new JTree();

							SignaturesTreeCellRenderer treeCellRenderer = new SignaturesTreeCellRenderer();
							signaturesTree.setCellRenderer(treeCellRenderer);

						}
						((DefaultTreeModel)signaturesTree.getModel()).setRoot(root);

						checkTabShown(signaturesTitle);
					}else
						removeTab(signaturesTitle);

				} else {

					Iterator signatureObjects = currentFormRenderer.getSignatureObjects();

					if(signatureObjects != null){

						DefaultMutableTreeNode root = new DefaultMutableTreeNode("Signatures");

						DefaultMutableTreeNode signed = new DefaultMutableTreeNode(
						"The following people have digitally counter-signed this document");

						DefaultMutableTreeNode blank = new DefaultMutableTreeNode("The following signature fields are not signed");


						while(signatureObjects.hasNext()){
							FormObject formObj = (FormObject) signatureObjects.next();

							PdfObject sigObject=formObj.getDictionary(PdfDictionary.V);//.getDictionary(PdfDictionary.Sig);

							decode_pdf.getIO().checkResolved(formObj);

							if(sigObject == null){

								if(!blank.isNodeChild(root))
									root.add(blank);

								DefaultMutableTreeNode blankNode = new DefaultMutableTreeNode(formObj.getTextStreamValue(PdfDictionary.T)+ " on page " + formObj.getPageNumber());
								blank.add(blankNode);

							} else {

								if(!signed.isNodeChild(root))
									root.add(signed);

								//String name = (String) OLDsigObject.get("Name");

								String name=sigObject.getTextStreamValue(PdfDictionary.Name);

								//System.out.println("names = " + name + " " + newname);

								DefaultMutableTreeNode owner = new DefaultMutableTreeNode("Signed by " + name);
								signed.add(owner);

								DefaultMutableTreeNode type = new DefaultMutableTreeNode("Type");
								owner.add(type);

								//String filter = (String) OLDsigObject.get("Filter");

								String filter = null;//sigObject.getName(PdfDictionary.Filter);

								PdfArrayIterator filters = sigObject.getMixedArray(PdfDictionary.Filter);
								if(filters!=null && filters.hasMoreTokens())
									filter=filters.getNextValueAsString(true);

								//System.out.println("filters = " + filter + " " + newfilter);

								DefaultMutableTreeNode filterNode = new DefaultMutableTreeNode("Filter: " + filter.substring(1,filter.length()));
								type.add(filterNode);

								//String subFilter = (String) OLDsigObject.get("SubFilter");

								String subFilter = sigObject.getName(PdfDictionary.SubFilter);

								//System.out.println("newsubFilter = " + subFilter + " " + newsubFilter);

								DefaultMutableTreeNode subFilterNode = new DefaultMutableTreeNode("Sub Filter: " + subFilter.substring(1,subFilter.length()));
								type.add(subFilterNode);

								DefaultMutableTreeNode details = new DefaultMutableTreeNode("Details");
								owner.add(details);

								//StringBuffer date = new StringBuffer((String) OLDsigObject.get("M"));

								StringBuffer date = new StringBuffer(sigObject.getTextStreamValue(PdfDictionary.M));

								//System.out.println("date = " + date + " " + newdate);

								date.delete(0, 2);
								date.insert(4, '/');
								date.insert(7, '/');
								date.insert(10, ' ');
								date.insert(13, ':');
								date.insert(16, ':');
								date.insert(19, ' ');

								DefaultMutableTreeNode time = new DefaultMutableTreeNode("Time: " +date);
								details.add(time);

								//String reason = (String) OLDsigObject.get("Reason");
								String reason=sigObject.getTextStreamValue(PdfDictionary.Reason);

								//System.out.println("reason = " + reason + " " + newreason);

								DefaultMutableTreeNode reasonNode = new DefaultMutableTreeNode("Reason: " + reason);
								details.add(reasonNode);

								//String location = (String) OLDsigObject.get("Location");
								String location=sigObject.getTextStreamValue(PdfDictionary.Location);

								//System.out.println("newlocation = " + location + " " + newlocation);

								DefaultMutableTreeNode locationNode = new DefaultMutableTreeNode("Location: " + location);
								details.add(locationNode);

								DefaultMutableTreeNode field = new DefaultMutableTreeNode("Field: " + formObj.getTextStreamValue(PdfDictionary.T)+ " on page " + formObj.getPageNumber());
								details.add(field);

							}
						}
						if(signaturesTree==null){
							signaturesTree = new JTree();

							SignaturesTreeCellRenderer treeCellRenderer = new SignaturesTreeCellRenderer();
							signaturesTree.setCellRenderer(treeCellRenderer);

						}
						((DefaultTreeModel)signaturesTree.getModel()).setRoot(root);

						checkTabShown(signaturesTitle);

					} else
						removeTab(signaturesTitle);
				}
				//<link><a name="layers" />
				/**
				 * add a control Panel to enable/disable layers
				 */
				//layers object
				layersObject=decode_pdf.getLayers();

				if(layersObject != null && layersObject.getLayersCount()>0){ //some files have empty Layers objects

					layersPanel.removeAll(); //flush any previous items

					layersPanel.setLayout(new BorderLayout());

					checkTabShown(layersTitle);

					/**
					 * add metadata to tab (Map of key values) as a Tree
					 */
					DefaultMutableTreeNode top = new DefaultMutableTreeNode("Info");

					Map metaData=layersObject.getMetaData();

					Iterator metaDataKeys=metaData.keySet().iterator();
					Object nextKey, value;
					while(metaDataKeys.hasNext()){

						nextKey=metaDataKeys.next();
						value=metaData.get(nextKey);

						top.add(new DefaultMutableTreeNode(nextKey+"="+value));

					}


					//add collapsed Tree at Top
					final JTree infoTree = new JTree(top);
					infoTree.setToolTipText("Double click to see any metadata");
					infoTree.setRootVisible(true);
					infoTree.collapseRow(0);
					layersPanel.add(infoTree, BorderLayout.NORTH);

					/**
					 * Display list of layers which can be recursive
					 * layerNames can contain comments or sub-trees as Object[] or String name of Layer
					 */
					final Object[] layerNames=layersObject.getDisplayTree();
					if(layerNames!=null){

						final DefaultMutableTreeNode topLayer = new DefaultMutableTreeNode("Layers");

						final JTree layersTree = new JTree(topLayer);
						layersTree.setName("LayersTree");

						//Listener to redraw with altered layer
						layersTree.addTreeSelectionListener(new TreeSelectionListener() {

							public void valueChanged(TreeSelectionEvent e) {

								final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
								layersTree.getLastSelectedPathComponent();

								/* exit if nothing is selected */
								if (node == null)
									return;

								/* retrieve the name of Layer that was selected */
								final String name = (String)node.getUserObject();

								//if allowed toggle and update display
								if(layersObject.isLayerName(name) && !layersObject.isLocked(name)){

									//toggle layer status when clicked
									Runnable updateAComponent = new Runnable() {

										public void run() {
											decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
											//force refresh
											decode_pdf.invalidate();
											decode_pdf.updateUI();
											decode_pdf.validate();

											scrollPane.invalidate();
											scrollPane.updateUI();
											scrollPane.validate();

											//update settings on display and in PdfDecoder
											CheckNode checkNode=(CheckNode)node;

											if(!checkNode.isEnabled()){ //selection not allowed so display info message

												checkNode.setSelected(checkNode.isSelected());
												ShowGUIMessage.showstaticGUIMessage(new StringBuffer("This layer has been disabled because its parent layer is disabled"),"Parent Layer disabled");
											}else{
												boolean reversedStatus=!checkNode.isSelected();
												checkNode.setSelected(reversedStatus);
												layersObject.setVisiblity(name,reversedStatus);

												//may be radio buttons which disable others so sync values
												//before repaint
												syncTreeDisplay(topLayer,true);

												//decode again with new settings
												try {
													decode_pdf.decodePage(commonValues.getCurrentPage());
												} catch (Exception e) {
													e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
												}

											}
											//deselect so works if user clicks on same again to deselect
											layersTree.invalidate();
											layersTree.clearSelection();
											layersTree.repaint();
											decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
										}
									};

									SwingUtilities.invokeLater(updateAComponent);

								}
							}
						});

						//build tree from values
						addLayersToTree(layerNames, topLayer, true);

						layersTree.setRootVisible(true);
						layersTree.expandRow(0);
						layersTree.setCellRenderer(new CheckRenderer());
						layersTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

						layersPanel.add(layersTree, BorderLayout.CENTER);

					}

				} else
					removeTab(layersTitle);

				setBookmarks(false);
			}
		}
	}

	private void syncTreeDisplay(DefaultMutableTreeNode topLayer, boolean isEnabled) {

		int count=topLayer.getChildCount();

		boolean parentIsEnabled =isEnabled, isSelected;
		String childName="";
		TreeNode childNode;
		int ii=0;
		while(true){

			isEnabled= parentIsEnabled;
			isSelected=true;

			if(count==0)
				childNode=topLayer;
			else
				childNode=topLayer.getChildAt(ii);

			if(childNode instanceof CheckNode){

				CheckNode cc=(CheckNode)childNode;
				childName=(String)cc.getText();

				if(layersObject.isLayerName(childName)){

					if(isEnabled)
						isEnabled=!layersObject.isLocked(childName);

					isSelected=(layersObject.isVisible(childName));
					cc.setSelected(isSelected);
					cc.setEnabled(isEnabled);
				}
			}

			if(childNode.getChildCount()>0){

				Enumeration children=childNode.children();
				while(children.hasMoreElements())
					syncTreeDisplay((DefaultMutableTreeNode)children.nextElement(), (isEnabled && isSelected));
			}

			ii++;
			if(ii>=count)
				break;
		}
	}

	private void addLayersToTree(Object[] layerNames, DefaultMutableTreeNode topLayer, boolean isEnabled) {
		int layerCount=layerNames.length;

		String name;

		DefaultMutableTreeNode currentNode=topLayer;
		boolean parentEnabled=isEnabled,parentIsSelected=true;

		for(int ii=0;ii<layerCount;ii++){

			//work out type of node and handle
			if(layerNames[ii] instanceof Object[]){ //its a subtree
				addLayersToTree((Object[])layerNames[ii], currentNode,isEnabled && parentIsSelected);

				currentNode= (DefaultMutableTreeNode) currentNode.getParent();
				isEnabled=parentEnabled;
			}else{

				//store possible recursive settings
				parentEnabled=isEnabled;

				if(layerNames[ii] instanceof String)
					name=(String)layerNames[ii];
				else //its a byte[]
					name=new String((byte[])layerNames[ii]);

				if(!layersObject.isLayerName(name)){ //just text

					currentNode=new DefaultMutableTreeNode(name);
					topLayer.add(currentNode);

					parentIsSelected=true;

					//add a node
				}else{

					currentNode=new CheckNode(name);
					topLayer.add(currentNode);

					//see if showing and set box to match
					if(layersObject.isVisible(name)){
						((CheckNode)currentNode).setSelected(true);
						parentIsSelected=true;
					}else
						parentIsSelected=false;

					//check locks and allow Parents to disable children
					if(isEnabled)
						isEnabled=!layersObject.isLocked(name);

					((CheckNode)currentNode).setEnabled(isEnabled);
				}
			}
		}
	}

	private void checkTabShown(String title) {
		int outlineTab=-1;
		if(PdfDecoder.isRunningOnMac){

			//see if there is an outlines tab
			for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
				if(navOptionsPanel.getTitleAt(jj).equals(title))
					outlineTab=jj;
			}

			if(outlineTab==-1){
				if(title.equals(signaturesTitle)){
					if(signaturesTree==null){
						signaturesTree = new JTree();

						SignaturesTreeCellRenderer treeCellRenderer = new SignaturesTreeCellRenderer();
						signaturesTree.setCellRenderer(treeCellRenderer);

					}
					navOptionsPanel.addTab(signaturesTitle, signaturesTree);
					navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, signaturesTitle);

				}else if(title.equals(layersTitle)){

					JScrollPane scrollPane=new JScrollPane();
					scrollPane.getViewport().add(layersPanel);
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
					scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

					navOptionsPanel.addTab(layersTitle, scrollPane);
					navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, layersTitle);

				}
			}

		}else{
			//see if there is an outlines tab
			for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
				if(navOptionsPanel.getIconAt(jj).toString().equals(title))
					outlineTab=jj;
			}

			if(outlineTab==-1){


				VTextIcon textIcon2 = new VTextIcon(navOptionsPanel, signaturesTitle, VTextIcon.ROTATE_LEFT);
				navOptionsPanel.addTab(null, textIcon2, signaturesTree);
				//navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, signaturesTitle);

				VTextIcon textIcon = new VTextIcon(navOptionsPanel, layersTitle, VTextIcon.ROTATE_LEFT);

				JScrollPane scrollPane=new JScrollPane();
				scrollPane.getViewport().add(layersPanel);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

				navOptionsPanel.addTab(null, textIcon, scrollPane);
				//navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, layersTitle);
			}
		}
	}

	private void removeTab(String title) {

		int outlineTab=-1;

		if(PdfDecoder.isRunningOnMac){
			String tabName="";
			//see if there is an outlines tab
			for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
				if(navOptionsPanel.getTitleAt(jj).equals(title))
					outlineTab=jj;
			}
		}else{
			String tabName="";
			//see if there is an outlines tab
			for(int jj=0;jj<navOptionsPanel.getTabCount();jj++){
				if(navOptionsPanel.getIconAt(jj).toString().equals(title))
					outlineTab=jj;
			}
		}

		if(outlineTab!=-1)
			navOptionsPanel.remove(outlineTab);

	}

	public void stopThumbnails() {

		if(!isSingle)
			return;

		if(thumbnails.isShownOnscreen()){
			/** if running terminate first */
			thumbnails.terminateDrawing();

			thumbnails.removeAllListeners();

		}
	}

	public void reinitThumbnails() {

		isSetup=false;

	}

	/**reset so appears closed*/
	public void resetNavBar() {

		if(!isSingle)
			return;

		displayPane.setDividerLocation(startSize);
		tabsNotInitialised=true;

		//disable page view buttons until we know we have multiple pages
		if(!commonValues.isContentExtractor())
			setPageLayoutButtonsEnabled(false);

	}

	public void setBackNavigationButtonsEnabled(boolean flag) {

//		if(!isSingle)
//		return;

		back.setEnabled(flag);
		first.setEnabled(flag);
		fback.setEnabled(flag);

	}

	public void setForwardNavigationButtonsEnabled(boolean flag) {

//		if(!isSingle)
//		return;

		forward.setEnabled(flag);
		end.setEnabled(flag);
		fforward.setEnabled(flag);


	}

	public void setPageLayoutButtonsEnabled(boolean flag) {

		if(!isSingle)
			return;

		continuousButton.setEnabled(flag);
		continuousFacingButton.setEnabled(flag);
		facingButton.setEnabled(flag);
		
		if(JAIHelper.isJAIused())
			sideScrollButton.setEnabled(flag);


		Enumeration menuOptions=layoutGroup.getElements();

		//@kieran - added fix below. Can you recode without Enumeration
		//object please (several refs) so we can keep 1.4 compatability.

		//export menu is broken in standalone (works in IDE). Is this related?

		//we cannot assume there are values so trap to avoid exception
		if(menuOptions.hasMoreElements()){

			//first one is always ON
			((JMenuItem)menuOptions.nextElement()).setEnabled(true);

			//set other menu items
			while(menuOptions.hasMoreElements())
				((JMenuItem)menuOptions.nextElement()).setEnabled(flag);
		}

	}

	public void setSearchLayoutButtonsEnabled() {

		Enumeration menuOptions=searchLayoutGroup.getElements();

		//first one is always ON
		((JMenuItem)menuOptions.nextElement()).setEnabled(true);

		//set other menu items
		while(menuOptions.hasMoreElements()){
			((JMenuItem)menuOptions.nextElement()).setEnabled(true);
		}

	}

	public void alignLayoutMenuOption(int mode) {

		//reset rotation
		//rotation=0;
		//setSelectedComboIndex(Commands.ROTATION,0);

		int i=1;

		Enumeration menuOptions=layoutGroup.getElements();

		//cycle to correct value
		while(menuOptions.hasMoreElements() && i!=mode){
			menuOptions.nextElement();
			i++;
		}

		//choose item
		((JMenuItem)menuOptions.nextElement()).setSelected(true);
	}

	public void setDisplayMode(Integer mode) {

		if(mode.equals(GUIFactory.MULTIPAGE))
			isSingle=false;

	}

	public boolean isSingle() {
		return isSingle;
	}

	/**used when clicking on thumbnails to move onto new page*/
	private class PageChanger implements ActionListener {

		int page;
		public PageChanger(int i){
			i++;
			page=i;
		}

		public void actionPerformed(ActionEvent e) {
			if((!commonValues.isProcessing())&&(commonValues.getCurrentPage()!=page)){
				commonValues.setCurrentPage(page);

				statusBar.resetStatus("");

				//setScalingToDefault();

				//decode_pdf.setPageParameters(getScaling(), commonValues.getCurrentPage());

				decodePage(false);

			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#initLayoutMenus(javax.swing.JMenu, java.lang.String[], int[])
	 */
	public void initLayoutMenus(JMenu pageLayout, String[] descriptions, int[] value) {

        int count=value.length;
        for(int i=0;i<count;i++){
            JCheckBoxMenuItem pageView=new JCheckBoxMenuItem(descriptions[i]);
            pageView.setBorder(BorderFactory.createEmptyBorder());
            layoutGroup.add(pageView);
            if(i==0)
                pageView.setSelected(true);

            if(pageLayout!=null){

                switch(value[i]){
                case Display.SINGLE_PAGE :
                    single = pageView;
                    pageLayout.add(single);
                    break;
                case Display.CONTINUOUS :
                    continuous = pageView;
                    pageLayout.add(continuous);
                    break;
                case Display.FACING :
                    facing = pageView;
                    pageLayout.add(facing);
                    break;
                case Display.CONTINUOUS_FACING :
                    continuousFacing = pageView;
                    pageLayout.add(continuousFacing);
                    break;

                }

            }
        }

        if(!isSingle)
            return;

        //default is off
        setPageLayoutButtonsEnabled(false);


    }

	/**
	 * show fonts on system displayed
	 */
	private JScrollPane getFontsAliasesInfoBox(){

		JPanel details=new JPanel();

		JScrollPane scrollPane=new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400,300));
		scrollPane.getViewport().add(details);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		details.setOpaque(true);
		details.setBackground(Color.white);
		details.setEnabled(false);
		details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));

		/**
		 * list of all fonts fonts
		 */
		StringBuffer fullList=new StringBuffer();


		Iterator fonts= FontMappings.fontSubstitutionAliasTable.keySet().iterator();
		while(fonts.hasNext()){
			Object nextFont=fonts.next();
			fullList.append(nextFont);
			fullList.append(" ==> ");
			fullList.append(FontMappings.fontSubstitutionAliasTable.get(nextFont));
			fullList.append('\n');
		}


		String xmlText=fullList.toString();
		if(xmlText.length()>0){

			JTextArea xml=new JTextArea();
			xml.setLineWrap(false);
			xml.setText(xmlText);
			details.add(xml);
			xml.setCaretPosition(0);
			xml.setOpaque(false);

			details.add(Box.createRigidArea(new Dimension(0,5)));
		}

		return scrollPane;
	}

	//Font tree Display pane
	JScrollPane fontScrollPane=new JScrollPane();


	boolean sortFontsByDir = true;

	//<link><a name="fontdetails" />
	/**
	 * show fonts on system displayed
	 */
	private JPanel getFontsFoundInfoBox(){

		//Create font list display area
		JPanel fontDetails=new JPanel(new BorderLayout());
		fontDetails.setBackground(Color.WHITE);
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);

		fontScrollPane.setBackground(Color.WHITE);
		fontScrollPane.getViewport().setBackground(Color.WHITE);
		fontScrollPane.setPreferredSize(new Dimension(400,300));
		fontScrollPane.getViewport().add(fontDetails);
		fontScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		fontScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//This allows the title to be centered above the filter box
		JPanel filterTitlePane = new JPanel();
		filterTitlePane.setBackground(Color.WHITE);
		JLabel filterTitle = new JLabel("Filter Font List");
		filterTitlePane.add(filterTitle);

		//Create buttons
		ButtonGroup bg = new ButtonGroup();
		JRadioButton folder = new JRadioButton("Sort By Folder");
		folder.setBackground(Color.WHITE);
		JRadioButton name = new JRadioButton("Sort By Name");
		name.setBackground(Color.WHITE);
		final JTextField filter = new JTextField();

		//Ensure correct display mode selected
		if(sortFontsByDir==true)
			folder.setSelected(true);
		else
			name.setSelected(true);

		bg.add(folder);
		bg.add(name);
		JPanel buttons = new JPanel(new BorderLayout());
		buttons.setBackground(Color.WHITE);
		buttons.add(filterTitlePane, BorderLayout.NORTH);
		buttons.add(folder, BorderLayout.WEST);
		buttons.add(filter, BorderLayout.CENTER);
		buttons.add(name, BorderLayout.EAST);

		folder.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(!sortFontsByDir){
					DefaultMutableTreeNode fontlist = new DefaultMutableTreeNode("Fonts");
					sortFontsByDir = true;
					fontlist = populateAvailableFonts(fontlist, filter.getText());
					displayAvailableFonts(fontlist);
				}
			}
		});

		name.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(sortFontsByDir){
					DefaultMutableTreeNode fontlist = new DefaultMutableTreeNode("Fonts");
					sortFontsByDir = false;
					fontlist = populateAvailableFonts(fontlist, filter.getText());
					displayAvailableFonts(fontlist);
				}
			}
		});

		filter.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				DefaultMutableTreeNode fontlist = new DefaultMutableTreeNode("Fonts");
				populateAvailableFonts(fontlist, ((JTextField)e.getSource()).getText());
				displayAvailableFonts(fontlist);
			}
			public void keyTyped(KeyEvent e) {}
		});

		//Start tree here
		DefaultMutableTreeNode top =
			new DefaultMutableTreeNode("Fonts");

		//Populate font list and build tree
		top = populateAvailableFonts(top, null);
		JTree fontTree = new JTree(top);
		//Added to keep the tree left aligned when top parent is closed
		fontDetails.add(fontTree, BorderLayout.WEST);


		//Peice it all together
		panel.add(buttons, BorderLayout.NORTH);
		panel.add(fontScrollPane, BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(400,300));

		return panel;
	}

	private void displayAvailableFonts(DefaultMutableTreeNode fontlist){

		//Remove old font tree display panel
		fontScrollPane.getViewport().removeAll();

		//Create new font list display
		JPanel jp = new JPanel(new BorderLayout());
		jp.setBackground(Color.WHITE);
		jp.add(new JTree(fontlist), BorderLayout.WEST);

		//Show font tree
		fontScrollPane.getViewport().add(jp);
	}

	/**
	 * list of all fonts properties in sorted order
	 */
	private DefaultMutableTreeNode populateAvailableFonts(DefaultMutableTreeNode top, String filter){

		//get list
		if(FontMappings.fontSubstitutionTable!=null){
			Set fonts=FontMappings.fontSubstitutionTable.keySet();
			Iterator fontList=FontMappings.fontSubstitutionTable.keySet().iterator();

			int fontCount=fonts.size();
			ArrayList fontNames=new ArrayList(fontCount);

			while(fontList.hasNext())
				fontNames.add(fontList.next().toString());

			//sort
			Collections.sort(fontNames);

			//Sort and Display Fonts by Directory
			if(sortFontsByDir){

				Vector Location = new Vector();
				Vector LocationNode = new Vector();

				//build display
				for(int ii=0;ii<fontCount;ii++){
					Object nextFont=fontNames.get(ii);

					String current = ((String)FontMappings.fontSubstitutionLocation.get(nextFont));

					int ptr=current.lastIndexOf(System.getProperty("file.separator"));
					if(ptr==-1 && current.indexOf('/')!=-1)
						ptr=current.lastIndexOf('/');

					if(ptr!=-1)
						current = current.substring(0, ptr);

					if(filter==null || ((String)nextFont).toLowerCase().indexOf(filter.toLowerCase())!=-1){
						if(!Location.contains(current)){
							Location.add(current);
							DefaultMutableTreeNode loc = new DefaultMutableTreeNode(new DefaultMutableTreeNode(current));
							top.add(loc);
							LocationNode.add(loc);
						}

						DefaultMutableTreeNode FontTop = new DefaultMutableTreeNode(nextFont+" = "+FontMappings.fontSubstitutionLocation.get(nextFont));
						int pos = Location.indexOf(current);
						((DefaultMutableTreeNode)LocationNode.get(pos)).add(FontTop);


						//add details
						Map properties=(Map)FontMappings.fontPropertiesTable.get(nextFont);
						if(properties!=null){

							Iterator fontProperties= properties.keySet().iterator();
							while(fontProperties.hasNext()){
								Object key=fontProperties.next();
								Object value=properties.get(key);

								//JLabel fontString=new JLabel(key+" = "+value);
								//fontString.setFont(new Font("Lucida",Font.PLAIN,10));
								//details.add(fontString);
								DefaultMutableTreeNode FontDetails = new DefaultMutableTreeNode(key+" = "+value);
								FontTop.add(FontDetails);

							}
						}
					}
				}
			}else{//Show all fonts in one list

				//build display
				for(int ii=0;ii<fontCount;ii++){
					Object nextFont=fontNames.get(ii);

					if(filter==null || ((String)nextFont).toLowerCase().indexOf(filter.toLowerCase())!=-1){
						DefaultMutableTreeNode FontTop = new DefaultMutableTreeNode(nextFont+" = "+FontMappings.fontSubstitutionLocation.get(nextFont));
						top.add(FontTop);

						//add details
						Map properties=(Map)FontMappings.fontPropertiesTable.get(nextFont);
						if(properties!=null){

							Iterator fontProperties= properties.keySet().iterator();
							while(fontProperties.hasNext()){
								Object key=fontProperties.next();
								Object value=properties.get(key);

								//JLabel fontString=new JLabel(key+" = "+value);
								//fontString.setFont(new Font("Lucida",Font.PLAIN,10));
								//details.add(fontString);
								DefaultMutableTreeNode FontDetails = new DefaultMutableTreeNode(key+" = "+value);
								FontTop.add(FontDetails);

							}
						}
					}
				}
			}
		}
		return top;
	}


	/**
	 * show fonts displayed
	 */
	private JScrollPane getFontInfoBox(){

		JPanel details=new JPanel();

		JScrollPane scrollPane=new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400,300));
		scrollPane.getViewport().add(details);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		details.setOpaque(true);
		details.setBackground(Color.white);
		details.setEnabled(false);
		details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));

		/**
		 * list of fonts
		 */
		String xmlTxt=decode_pdf.getFontsInFile();
		String xmlText = "Font Substitution mode: ";

		switch(decode_pdf.getFontSubstitutionMode()){
		case(1):
			xmlText = xmlText + "using file name";
		break;
		case(2):
			xmlText = xmlText + "using PostScript name";
		break;
		case(3):
			xmlText = xmlText + "using family name";
		break;
		case(4):
			xmlText = xmlText + "using the full font name";
		break;
		default:
			xmlText = xmlText + "Unknown FontSubstitutionMode";
		break;
		}

		xmlText = xmlText + "\n";




		if(xmlTxt.length()>0){

			JTextArea xml=new JTextArea();
			JLabel mode = new JLabel();

			mode.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			mode.setText(xmlText);
			mode.setForeground(Color.BLUE);

			xml.setLineWrap(false);
			xml.setForeground(Color.BLACK);
			xml.setText("\n" + xmlTxt);


			details.add(mode);
			details.add(xml);

			xml.setCaretPosition(0);
			xml.setOpaque(false);

			//details.add(Box.createRigidArea(new Dimension(0,5)));
		}

		return scrollPane;
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getInfoBox()
	 */
	public void getInfoBox() {

		final JPanel details=new JPanel();
		details.setPreferredSize(new Dimension(400,260));
		details.setOpaque(false);
		details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

		//general details
		JLabel header1=new JLabel(Messages.getMessage("PdfViewerInfo.title"));
		header1.setOpaque(false);
		header1.setFont(headFont);
		header1.setAlignmentX(Component.CENTER_ALIGNMENT);
		details.add(header1);

		details.add(Box.createRigidArea(new Dimension(0,15)));

		String xmlText=Messages.getMessage("PdfViewerInfo1")+Messages.getMessage("PdfViewerInfo2");
		if(xmlText.length()>0){

			JTextArea xml=new JTextArea();
			xml.setOpaque(false);
			xml.setText(xmlText + "\n\nVersion: "+PdfDecoder.version + "\n\n" + "Java version " + System.getProperty("java.version"));
			xml.setLineWrap(true);
			xml.setWrapStyleWord(true);
			xml.setEditable(false);
			details.add(xml);
			xml.setAlignmentX(Component.CENTER_ALIGNMENT);

		}

		ImageIcon logo=new ImageIcon(getClass().getResource("/org/jpedal/examples/simpleviewer/res/logo.gif"));
		details.add(Box.createRigidArea(new Dimension(0,25)));
		JLabel idr=new JLabel(logo);
		idr.setAlignmentX(Component.CENTER_ALIGNMENT);
		details.add(idr);

		final JLabel url=new JLabel("<html><center>"+Messages.getMessage("PdfViewerJpedalLibrary.Text")
				+Messages.getMessage("PdfViewer.WebAddress"));
		url.setForeground(Color.blue);
		url.setHorizontalAlignment(JLabel.CENTER);
		url.setAlignmentX(Component.CENTER_ALIGNMENT);

		//@kieran - cursor
		url.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				details.setCursor(new Cursor(Cursor.HAND_CURSOR));
				url.setText("<html><center>"+Messages.getMessage("PdfViewerJpedalLibrary.Link")+
						Messages.getMessage("PdfViewerJpedalLibrary.Text")+
						Messages.getMessage("PdfViewer.WebAddress")+"</a></center>");
			}

			public void mouseExited(MouseEvent e) {
				details.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				url.setText("<html><center>"+Messages.getMessage("PdfViewerJpedalLibrary.Text")
						+Messages.getMessage("PdfViewer.WebAddress"));
			}

			public void mouseClicked(MouseEvent e) {
				try {
					BrowserLauncher.openURL(Messages.getMessage("PdfViewer.VisitWebsite"));
				} catch (IOException e1) {
					showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
				}
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});

		details.add(url);
		details.add(Box.createRigidArea(new Dimension(0,5)));

		details.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		showMessageDialog(details,Messages.getMessage("PdfViewerInfo3"),JOptionPane.PLAIN_MESSAGE);

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#resetRotationBox()
	 */
	public void resetRotationBox() {

		PdfPageData currentPageData=decode_pdf.getPdfPageData();

		//>>> DON'T UNCOMMENT THIS LINE, causes major rotation issues, only useful for debuging <<<
		if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
			rotation=currentPageData.getRotation(commonValues.getCurrentPage());
		//else
		//rotation=0;

		if(getSelectedComboIndex(Commands.ROTATION)!=(rotation/90)){
			setSelectedComboIndex(Commands.ROTATION, (rotation/90));
		}else if(!commonValues.isProcessing()){
			decode_pdf.repaint();
		}
	}


	/**
	 * show document properties
	 */
	private JScrollPane getPropertiesBox(String file, String path, String user_dir, long size, int pageCount,int currentPage) {

		PdfFileInformation currentFileInformation=decode_pdf.getFileInformationData();

		/**get the Pdf file information object to extract info from*/
		if(currentFileInformation!=null){

			JPanel details=new JPanel();
			details.setOpaque(true);
			details.setBackground(Color.white);
			details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

			JScrollPane scrollPane=new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(400,300));
			scrollPane.getViewport().add(details);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			//general details
			JLabel header1=new JLabel(Messages.getMessage("PdfViewerGeneral"));
			header1.setFont(headFont);
			header1.setOpaque(false);
			details.add(header1);

			JLabel g1=new JLabel(Messages.getMessage("PdfViewerFileName")+file);
			g1.setFont(textFont);
			g1.setOpaque(false);
			details.add(g1);

			JLabel g2=new JLabel(Messages.getMessage("PdfViewerFilePath")+path);
			g2.setFont(textFont);
			g2.setOpaque(false);
			details.add(g2);

			JLabel g3=new JLabel(Messages.getMessage("PdfViewerCurrentWorkingDir")+ ' ' +user_dir);
			g3.setFont(textFont);
			g3.setOpaque(false);
			details.add(g3);

			JLabel g4=new JLabel(Messages.getMessage("PdfViewerFileSize")+size+" K");
			g4.setFont(textFont);
			g4.setOpaque(false);
			details.add(g4);

			JLabel g5=new JLabel(Messages.getMessage("PdfViewerPageCount")+pageCount);
			g5.setOpaque(false);
			g5.setFont(textFont);
			details.add(g5);

			JLabel g6=new JLabel("PDF "+decode_pdf.getPDFVersion());
			g6.setOpaque(false);
			g6.setFont(textFont);
			details.add(g6);

			details.add(Box.createVerticalStrut(10));

			//general details
			JLabel header2=new JLabel(Messages.getMessage("PdfViewerProperties"));
			header2.setFont(headFont);
			header2.setOpaque(false);
			details.add(header2);

			//get the document properties
			String[] values=currentFileInformation.getFieldValues();
			String[] fields=currentFileInformation.getFieldNames();

			//add to list and display
			int count=fields.length;

			JLabel[] displayValues=new JLabel[count];

			for(int i=0;i<count;i++){
				if(values[i].length()>0){

					displayValues[i]=new JLabel(fields[i]+" = "+values[i]);
					displayValues[i].setFont(textFont);
					displayValues[i].setOpaque(false);
					details.add(displayValues[i]);
				}
			}

			details.add(Box.createVerticalStrut(10));

			/**
			 * get the Pdf file information object to extract info from
			 */
			PdfPageData currentPageSize=decode_pdf.getPdfPageData();

			if(currentPageSize!=null){

				//general details
				JLabel header3=new JLabel(Messages.getMessage("PdfViewerCoords.text"));
				header3.setFont(headFont);
				details.add(header3);

				JLabel g7=new JLabel(Messages.getMessage("PdfViewermediaBox.text")+currentPageSize.getMediaValue(currentPage));
				g7.setFont(textFont);
				details.add(g7);

				JLabel g8=new JLabel(Messages.getMessage("PdfViewercropBox.text")+currentPageSize.getCropValue(currentPage));
				g8.setFont(textFont);
				details.add(g8);

				JLabel g9=new JLabel(Messages.getMessage("PdfViewerLabel.Rotation")+currentPageSize.getRotation(currentPage));
				g3.setFont(textFont);
				details.add(g9);

			}

			details.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

			return scrollPane;

		}else{
			return new JScrollPane();
		}
	}


	/**
	 * page info option
	 */
	private JScrollPane getXMLInfoBox(String xmlText) {

		JPanel details=new JPanel();
		details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));

		details.setOpaque(true);
		details.setBackground(Color.white);

		JScrollPane scrollPane=new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400,300));
		scrollPane.getViewport().add(details);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JTextArea xml=new JTextArea();

		xml.setRows(5);
		xml.setColumns(15);
		xml.setLineWrap(true);
		xml.setText(xmlText);
		details.add(new JScrollPane(xml));
		xml.setCaretPosition(0);
		xml.setOpaque(true);
		xml.setBackground(Color.white);

		return scrollPane;

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showDocumentProperties(java.lang.String, java.lang.String, long, int, int)
	 */
	public void showDocumentProperties(String selectedFile, String inputDir, long size, int pageCount,int currentPage) {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBackground(Color.WHITE);




		if(selectedFile == null){

			showMessageDialog(Messages.getMessage("PdfVieweremptyFile.message"),Messages.getMessage("PdfViewerTooltip.pageSize"),JOptionPane.PLAIN_MESSAGE);
		}else{

			String filename=selectedFile;

			int ptr=filename.lastIndexOf('\\');
			if(ptr==-1)
				ptr=filename.lastIndexOf('/');

			String file=filename.substring(ptr+1,filename.length());


			String path=filename.substring(0,ptr+1);

			tabbedPane.add(getPropertiesBox(file, path,user_dir,size,pageCount,currentPage));
			tabbedPane.setTitleAt(0, Messages.getMessage("PdfViewerTab.Properties"));

			tabbedPane.add(getFontInfoBox());
			tabbedPane.setTitleAt(1, Messages.getMessage("PdfViewerTab.Fonts"));

			tabbedPane.add(getFontsFoundInfoBox());
			tabbedPane.setTitleAt(2, "Available");

			tabbedPane.add(getFontsAliasesInfoBox());
			tabbedPane.setTitleAt(3, "Aliases");

			int nextTab=4;

			/**
			 * add form details if applicable
			 */
			JScrollPane scroll=getFormList();

			if(scroll!=null){
				tabbedPane.add(scroll);
				tabbedPane.setTitleAt(nextTab, "Forms");
				nextTab++;
			}

			/**
			 * optional tab for new XML style info
			 */
			PdfFileInformation currentFileInformation=decode_pdf.getFileInformationData();
			String xmlText=currentFileInformation.getFileXMLMetaData();
			if(xmlText.length()>0){
				tabbedPane.add(getXMLInfoBox(xmlText));
				tabbedPane.setTitleAt(nextTab, "XML");
			}

			showMessageDialog(tabbedPane, Messages.getMessage("PdfViewerTab.DocumentProperties"), JOptionPane.PLAIN_MESSAGE);
		}
	}

	/**
	 * provide list of forms
	 */
	private JScrollPane getFormList() {

		JScrollPane scroll=null;

		//get the form renderer
		org.jpedal.objects.acroforms.rendering.AcroRenderer formRenderer=decode_pdf.getFormRenderer();

		if(formRenderer!=null){

			//get list of forms on page
			java.util.List formsOnPage=null;

			try {
				formsOnPage = formRenderer.getComponentNameList(commonValues.getCurrentPage());
			} catch (PdfException e) {

				LogWriter.writeLog("Exception "+e+" reading component list");
			}

			//allow for no forms
			if(formsOnPage!=null){

				int formCount=formsOnPage.size();

				JPanel formPanel=new JPanel();

				scroll=new JScrollPane();
				scroll.setPreferredSize(new Dimension(400,300));
				scroll.getViewport().add(formPanel);
				scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


				/**
				 * create a JPanel to list forms and popup details
				 */

				formPanel.setLayout(new BoxLayout(formPanel,BoxLayout.Y_AXIS));
				JLabel formHeader = new JLabel("This page contains "+formCount+" form objects");
				formHeader.setFont(headFont);
				formPanel.add(formHeader);

				formPanel.add(Box.createRigidArea(new Dimension(10,10)));

				/** sort form names in alphabetical order */
				Collections.sort(formsOnPage);

                //get FormRenderer and Data objects
                AcroRenderer renderer = decode_pdf.getFormRenderer();
                if(renderer==null)
                        return scroll;
                GUIData formData=renderer.getCompData();

				/**
				 * populate our list with details
				 */
				for (int i = 0; i < formCount; i++) {

					// get name of form
					String formName = (String) formsOnPage.get(i);

                    //swing component we map data into
					Component[] comp = (Component[]) formRenderer.getComponentsByName(formName);

                    //actual data read from PDF
                    Object rawFormData=formData.getRawForm(formName);


                    if (comp != null) {

                        //number of components - may be several child items
                        int count = comp.length;

                        //take value or first if array to check for types (will be same if children)
                        FormObject formObj=null;

                        //extract list of actual PDF references to display and get FormObject
                        String PDFrefs = "PDF ref=";

                        JLabel ref = new JLabel();
                        if(rawFormData instanceof FormObject){
                            formObj=(FormObject)rawFormData;
                            PDFrefs=PDFrefs+formObj.getObjectRefAsString();
                        }else{
                            Object[] allObjs=(Object[])rawFormData;
                            int objCount=allObjs.length;
                            for(int ii=0;ii<objCount;ii++){
                                formObj=(FormObject)allObjs[ii];
                                PDFrefs=PDFrefs+" "+formObj.getObjectRefAsString();
                            }
                        }
                        ref.setText(PDFrefs);

                        /** display the form component description */
                        int formComponentType = ((Integer) formData.getTypeValueByName(formName)).intValue();

                        String formDescription = formName;
                        JLabel header = new JLabel(formDescription);

                        JLabel type = new JLabel();
                        type.setText("Type="+
                                PdfDictionary.showAsConstant(formObj.getParameterConstant(PdfDictionary.Type))+
                                " Subtype="+PdfDictionary.showAsConstant(formObj.getParameterConstant(PdfDictionary.Subtype)));


                        /** get the current Swing component type */
                        String standardDetails = "java class=" + comp[0].getClass();

                        JLabel details = new JLabel(standardDetails);

                        header.setFont(headFont);
                        header.setForeground(Color.blue);

                        type.setFont(textFont);
                        type.setForeground(Color.blue);

                        details.setFont(textFont);
                        details.setForeground(Color.blue);

                        ref.setFont(textFont);
                        ref.setForeground(Color.blue);

                        formPanel.add(header);
                        formPanel.add(type);
                        formPanel.add(details);
                        formPanel.add(ref);

                        /** not currently used or setup
                        JButton more = new JButton("View Form Data");
                        more.setFont(textFont);
                        more.setForeground(Color.blue);

                        more.addActionListener(new ShowFormDataListener(formName));
                        formPanel.add(more);

                        formPanel.add(new JLabel(" "));

                         /**/
                    }
				}
			}
		}

		return scroll;
	}

	/**
	 * display form data in popup
	 *
	private class ShowFormDataListener implements ActionListener{

		private String formName;

		public ShowFormDataListener(String formName){
			this.formName=formName;
		}

		public void actionPerformed(ActionEvent e) {


			//will return Object or  Object[] if multiple items of same name
			Object formObjects=decode_pdf.getFormRenderer().getCompData().getRawForm(formName);
			if(formObjects instanceof Object[]){
				Object[] values=(Object[])formObjects;

				int count=values.length;

				JTabbedPane valueDisplay=new JTabbedPane();
				for(int jj=0;jj<count;jj++){

                    FormObject form=(FormObject)values[jj];

					if(values[jj]!=null){
						String data=form.toString();
						JTextArea text=new JTextArea();
						text.setText(data);
						text.setWrapStyleWord(true);

						JScrollPane scroll=new JScrollPane();
						scroll.setPreferredSize(new Dimension(400,300));
						scroll.getViewport().add(text);
						scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
						scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

						valueDisplay.add(form.getObjectRefAsString(),scroll);
					}
				}

				JOptionPane.showMessageDialog(getFrame(), valueDisplay,"Raw Form Data",JOptionPane.OK_OPTION);
			}else{
				String data=((FormObject)formObjects).toString();
				JTextArea text=new JTextArea();
				text.setText(data);
				text.setWrapStyleWord(true);

				JScrollPane scroll=new JScrollPane();
				scroll.setPreferredSize(new Dimension(400,300));
				scroll.getViewport().add(text);
				scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

				JOptionPane.showMessageDialog(getFrame(), scroll,"Raw Form Data",JOptionPane.OK_OPTION);
			}
		}

	}/**/


	GUISearchWindow searchFrame = null;
	boolean addSearchTab = false;
	boolean searchInMenu = false;

	/*
	 * Set Search Bar to be in the Left hand Tabbed pane
	 */
	public void searchInTab(GUISearchWindow searchFrame){
		this.searchFrame = searchFrame;
		if(PdfDecoder.isRunningOnMac){
			if(thumbnails.isShownOnscreen())
				navOptionsPanel.addTab("Search",searchFrame.getContentPanel());
		}else{
			VTextIcon textIcon2 = new VTextIcon(navOptionsPanel, "Search", VTextIcon.ROTATE_LEFT);
			navOptionsPanel.addTab(null, textIcon2, searchFrame.getContentPanel());
		}
		addSearchTab = true;
	}
	JTextField searchText = null;
	SearchList results = null;
	Commands currentCommands;
	/*
	 * Set Search Bar to be in the Top Button Bar
	 */
	public void searchInMenu(GUISearchWindow searchFrame){
		this.searchFrame = searchFrame;
		searchInMenu = true;
		searchFrame.find(decode_pdf, commonValues);
		searchText.setPreferredSize(new Dimension(150,20));
		topButtons.add(searchText);
		addButton(GUIFactory.BUTTONBAR, "Previous Search Result", "/org/jpedal/examples/simpleviewer/res/search_previous.gif", Commands.PREVIOUSRESULT);
		addButton(GUIFactory.BUTTONBAR, "Next Search Result", "/org/jpedal/examples/simpleviewer/res/search_next.gif", Commands.NEXTRESULT);

		nextSearch.setVisible(false);
		previousSearch.setVisible(false);
	}

	public void clearRecentDocuments() {
		currentCommands.clearRecentDocuments();
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#init(java.lang.String[], org.jpedal.examples.simpleviewer.Commands, org.jpedal.examples.simpleviewer.utils.Printer)
	 */
	public void init(String[] scalingValues,final Object currentCommands,Object currentPrinter) {


		/**
		 * Set up from properties
		 */
		try{

			//Set border config value and repaint
			String propValue = properties.getValue("borderType");
			if(propValue.length()>0)
			PdfDecoder.CURRENT_BORDER_STYLE = Integer.parseInt(properties.getValue("borderType"));

			//Set autoScroll default and add to properties file
			propValue = properties.getValue("autoScroll");
			if(propValue.length()>0)
			allowScrolling = Boolean.getBoolean(properties.getValue("autoScroll"));

			//Dpi is taken into effect when zoom is called
			propValue = properties.getValue("DPI");
			if(propValue.length()>0)
			decode_pdf.getDPIFactory().setDpi(Integer.parseInt(properties.getValue("DPI")));

			//@kieran Ensure valid value if not recognised
			propValue = properties.getValue("pageMode");
			if(propValue.length()>0){
				int pageMode = Integer.parseInt(properties.getValue("pageMode"));
				if(pageMode<Display.SINGLE_PAGE || pageMode>Display.CONTINUOUS_FACING)
					pageMode = Display.SINGLE_PAGE;
				//Default Page Layout
				decode_pdf.setPageMode(pageMode);
			}

			propValue = properties.getValue("maxmuliviewers");
			if(propValue.length()>0)
			commonValues.setMaxMiltiViewers(Integer.parseInt(properties.getValue("maxmultiviewers")));

			propValue = properties.getValue("showDownloadWindow");
			if(propValue.length()>0)
			useDownloadWindow = Boolean.valueOf(properties.getValue("showDownloadWindow")).booleanValue();

			propValue = properties.getValue("useHiResPrinting");
			if(propValue.length()>0)
			hiResPrinting = Boolean.valueOf(properties.getValue("useHiResPrinting")).booleanValue();

			//@kieran - in this code, it will break if we add new value for all users.
			//could we recode these all defensively so  change one below to
			String val= properties.getValue("highlightBoxColor"); //empty string to old users
			if(val.length()>0)
				PdfDecoder.highlightColor = new Color(Integer.parseInt(val));

			//how it is at moment
			//PdfDecoder.highlightColor = new Color(Integer.parseInt(properties.getValue("highlightBoxColor")));

			////////////////////////////
			propValue = properties.getValue("highlightTextColor");
			if(propValue.length()>0)
			PdfDecoder.backgroundColor = new Color(Integer.parseInt(properties.getValue("highlightTextColor")));

			propValue = properties.getValue("invertHighlights");
			if(propValue.length()>0)
			DynamicVectorRenderer.invertHighlight = Boolean.valueOf(properties.getValue("invertHighlights")).booleanValue();

			propValue = properties.getValue("highlightComposite");
			if(propValue.length()>0){
				float value = Float.parseFloat(properties.getValue("highlightComposite"));
				if(value>1)
					value = 1;
				if(value<0)
					value = 0;
				PdfDecoder.highlightComposite = value;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		this.currentCommands = (Commands) currentCommands;


		/**
		 * single listener to execute all commands
		 */
		currentCommandListener=new CommandListener((Commands) currentCommands);

		/**
		 * set a title
		 */
		setViewerTitle(Messages.getMessage("PdfViewer.titlebar") +"  " + PdfDecoder.version);

		/**arrange insets*/
		decode_pdf.setInset(inset,inset);

		//Add Background color to the panel to helpp break up view
		decode_pdf.setBackground(new Color(190,190,190));

		/**
		 * setup combo boxes
		 */

        //set new default if appropriate
        String choosenScaling=System.getProperty("org.jpedal.defaultViewerScaling");
        if(choosenScaling!=null){
            int total=scalingValues.length;
            for(int aa=0;aa<total;aa++){
                if(scalingValues[aa].equals(choosenScaling)){
                    defaultSelection=aa;
                    aa=total;
                }
            }
        }

		scalingBox=new SwingCombo(scalingValues);
		scalingBox.setBackground(Color.white);
		scalingBox.setEditable(true);
		scalingBox.setSelectedIndex(defaultSelection); //set default before we add a listener

		//if you enable, remember to change rotation and quality Comboboxes
		//scalingBox.setPreferredSize(new Dimension(85,25));

		rotationBox=new SwingCombo(rotationValues);
		rotationBox.setBackground(Color.white);
		rotationBox.setSelectedIndex(0); //set default before we add a listener

		if(isSingle){

			/**
			 * add the pdf display to show page
			 **/
			scrollPane.getViewport().add(decode_pdf);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.getVerticalScrollBar().setUnitIncrement(80);
			scrollPane.getHorizontalScrollBar().setUnitIncrement(80);

		}

		comboBoxBar.setBorder(BorderFactory.createEmptyBorder());
		comboBoxBar.setLayout(new FlowLayout(FlowLayout.LEADING));
		comboBoxBar.setFloatable(false);
		comboBoxBar.setFont(new Font("SansSerif", Font.PLAIN, 8));

		if(isSingle){
			/**
			 * Create a left-right split pane with tabs
			 * and add to main display
			 */
			navOptionsPanel.setTabPlacement(JTabbedPane.LEFT);
			navOptionsPanel.setOpaque(true);
			navOptionsPanel.setMinimumSize(new Dimension(0,100));
			navOptionsPanel.setName("NavPanel");

			pageTitle=Messages.getMessage("PdfViewerJPanel.thumbnails");
			bookmarksTitle=Messages.getMessage("PdfViewerJPanel.bookmarks");
			layersTitle=Messages.getMessage("PdfViewerJPanel.layers");
			signaturesTitle="Signatures";

			displayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navOptionsPanel, scrollPane);
			displayPane.setOneTouchExpandable(false);


			if(!commonValues.isContentExtractor()){
				if(PdfDecoder.isRunningOnMac){
					navOptionsPanel.addTab(pageTitle,(Component) thumbnails);
					navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, pageTitle);



					if(thumbnails.isShownOnscreen()){
						navOptionsPanel.addTab(bookmarksTitle,(SwingOutline)tree);
						navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, bookmarksTitle);
					}

				}else{

					if(thumbnails.isShownOnscreen()){
						VTextIcon textIcon1 = new VTextIcon(navOptionsPanel, pageTitle, VTextIcon.ROTATE_LEFT);
						navOptionsPanel.addTab(null, textIcon1, (Component) thumbnails);

						//navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, pageTitle);
					}


					VTextIcon textIcon2 = new VTextIcon(navOptionsPanel, bookmarksTitle, VTextIcon.ROTATE_LEFT);
					navOptionsPanel.addTab(null, textIcon2, (SwingOutline)tree);
					//navOptionsPanel.setTitleAt(navOptionsPanel.getTabCount()-1, bookmarksTitle);

				}

//				p.setTabDefaults(defaultValues);

				displayPane.setDividerLocation(startSize);
			}else
				displayPane.setDividerLocation(0);

			if(!hasListener){
				hasListener=true;
				navOptionsPanel.addMouseListener(new MouseListener(){

					public void focusLost(FocusEvent focusEvent) {
						//To change body of implemented methods use File | Settings | File Templates.
					}

					public void mouseClicked(MouseEvent mouseEvent) {
						handleTabbedPanes();
					}

					public void mousePressed(MouseEvent mouseEvent) {
						//To change body of implemented methods use File | Settings | File Templates.
					}

					public void mouseReleased(MouseEvent mouseEvent) {
						//To change body of implemented methods use File | Settings | File Templates.
					}

					public void mouseEntered(MouseEvent mouseEvent) {
						//To change body of implemented methods use File | Settings | File Templates.
					}

					public void mouseExited(MouseEvent mouseEvent) {
						//To change body of implemented methods use File | Settings | File Templates.
					}
				});

			}


		}

		/**
		 * setup global buttons
		 */
		//if(!commonValues.isContentExtractor()){
			first=new SwingButton();
			fback=new SwingButton();
			back=new SwingButton();
			forward=new SwingButton();
			fforward=new SwingButton();
			end=new SwingButton();

		//}

		snapshotButton=new SwingButton();


		singleButton=new SwingButton();
		continuousButton=new SwingButton();
		continuousFacingButton=new SwingButton();
		facingButton=new SwingButton();
		
		if(JAIHelper.isJAIused())
			sideScrollButton=new SwingButton();

		openButton=new SwingButton();
		printButton=new SwingButton();
		searchButton=new SwingButton();
		docPropButton=new SwingButton();
		infoButton=new SwingButton();

		previousSearch=new SwingButton();
		nextSearch=new SwingButton();

		/**
		 * set colours on display boxes and add listener to page number
		 */
		pageCounter2.setEditable(true);
		pageCounter2.setToolTipText(Messages.getMessage("PdfViewerTooltip.goto"));
		pageCounter2.setBorder(BorderFactory.createLineBorder(Color.black));

		pageCounter2.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {

				String value=pageCounter2.getText().trim();

				((Commands)currentCommands).gotoPage(value);
			}

		});
		pageCounter3=new JLabel(Messages.getMessage("PdfViewerOfLabel.text")+ ' ');
		pageCounter3.setOpaque(false);

		/**
		 * create a menu bar and add to display
		 */
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		if(frame instanceof JFrame)
			((JFrame)frame).getContentPane().add(top, BorderLayout.NORTH);
		else
			frame.add(top, BorderLayout.NORTH);

		/** nav bar at bottom to select pages and setup Toolbar on it*/

		//navToolBar.setLayout(new FlowLayout());
		navToolBar.setLayout(new BoxLayout(navToolBar,BoxLayout.LINE_AXIS));
		navToolBar.setFloatable(false);

		//pagesToolBar.setLayout(new FlowLayout());
		pagesToolBar.setFloatable(false);

		navButtons.setBorder(BorderFactory.createEmptyBorder());
		navButtons.setLayout(new BorderLayout());
		navButtons.setFloatable(false);
//		comboBar.setFont(new Font("SansSerif", Font.PLAIN, 8));
		navButtons.setPreferredSize(new Dimension(5,24));


		/**
		 *setup menu and create options
		 */
		top.add(currentMenu, BorderLayout.NORTH);


		/**
		 * create other tool bars and add to display
		 */
		topButtons.setBorder(BorderFactory.createEmptyBorder());
		topButtons.setLayout(new FlowLayout(FlowLayout.LEADING));
		topButtons.setFloatable(false);
		topButtons.setFont(new Font("SansSerif", Font.PLAIN, 8));


		top.add(topButtons, BorderLayout.CENTER);


		/**
		 * zoom,scale,rotation, status,cursor
		 */
		top.add(comboBoxBar, BorderLayout.SOUTH);


		if(frame instanceof JFrame)
			((JFrame)frame).getContentPane().add(navButtons, BorderLayout.SOUTH);
		else
			frame.add(navButtons, BorderLayout.SOUTH);


		if(displayPane!=null){ //null in MultiViewer
			if(frame instanceof JFrame)
				((JFrame)frame).getContentPane().add(displayPane, BorderLayout.CENTER);
			else
				frame.add(displayPane, BorderLayout.CENTER);


		}


		/**
		 * navigation toolbar for moving between pages
		 */
		createNavbar();

		/**
		 * Menu bar for using the majority of functions
		 */
		createMainMenu(true);


		//createSwingMenu(true);

		/**
		 * combo boxes on toolbar
		 * */
		addCombo(Messages.getMessage("PdfViewerToolbarScaling.text"), Messages.getMessage("PdfViewerToolbarTooltip.zoomin"), Commands.SCALING);


		addCombo(Messages.getMessage("PdfViewerToolbarRotation.text"), Messages.getMessage("PdfViewerToolbarTooltip.rotation"), Commands.ROTATION);


        //<start-wrap>
		/**
		 * sets up all the toolbar items
		 */
		addButton(GUIFactory.BUTTONBAR,Messages.getMessage("PdfViewerToolbarTooltip.openFile"),"/org/jpedal/examples/simpleviewer/res/open.gif",Commands.OPENFILE);
        //<end-wrap>


		if(searchFrame!=null && searchFrame.getStyle()==SwingSearchWindow.SEARCH_EXTERNAL_WINDOW)
				addButton(GUIFactory.BUTTONBAR,Messages.getMessage("PdfViewerToolbarTooltip.search"),"/org/jpedal/examples/simpleviewer/res/find.gif",Commands.FIND);

        //<start-wrap>
		addButton(GUIFactory.BUTTONBAR,Messages.getMessage("PdfViewerToolbarTooltip.properties"),"/org/jpedal/examples/simpleviewer/res/properties.gif",Commands.DOCINFO);
        //<end-wrap>

		addButton(GUIFactory.BUTTONBAR,Messages.getMessage("PdfViewerToolbarTooltip.about"),"/org/jpedal/examples/simpleviewer/res/about.gif",Commands.INFO);

        //<start-wrap>
		/**snapshot screen function*/
		addButton(GUIFactory.BUTTONBAR,Messages.getMessage("PdfViewerToolbarTooltip.snapshot"),"/org/jpedal/examples/simpleviewer/res/snapshot.gif",Commands.SNAPSHOT);
        //<end-wrap>


        //<start-wrap>
		addCursor();
        //<end-wrap>

//		p.setButtonDefaults(defaultValues);

		//<link><a name="newbutton" />
		/**
		 * external/itext button option example adding new option to Export menu
		 * an icon is set wtih location on classpath
		 * "/org/jpedal/examples/simpleviewer/res/newfunction.gif"
		 * Make sure it exists at location and is copied into jar if recompiled
		 */
		//currentGUI.addButton(currentGUI.BUTTONBAR,tooltip,"/org/jpedal/examples/simpleviewer/res/newfunction.gif",Commands.NEWFUNCTION);

		/**
		 * external/itext menu option example adding new option to Export menu
		 * Tooltip text can be externalised in Messages.getMessage("PdfViewerTooltip.NEWFUNCTION")
		 * and text added into files in res package
		 */


		if(searchFrame!=null && searchFrame.getStyle()==SwingSearchWindow.SEARCH_MENU_BAR)
			searchInMenu(searchFrame);

		/**status object on toolbar showing 0 -100 % completion */
		initStatus();

//		p.setDisplayDefaults(defaultValues);

		//Ensure all gui sections are displayed correctly
		//Issues found when removing some sections
		getFrame().invalidate();
		getFrame().validate();
		getFrame().repaint();

		/**
		 * set display to occupy half screen size and display, add listener and
		 * make sure appears in centre
		 */
		if(commonValues.getModeOfOperation()!=Values.RUNNING_APPLET){

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int width = d.width / 2, height = d.height / 2;
			if(width<minimumScreenWidth)
				width=minimumScreenWidth;

			//allow user to alter size
			String customWindowSize=System.getProperty("org.jpedal.startWindowSize");
			if(customWindowSize!=null){

				StringTokenizer values=new StringTokenizer(customWindowSize,"x");

				System.out.println(values.countTokens());
				if(values.countTokens()!=2)
					throw new RuntimeException("Unable to use value for org.jpedal.startWindowSize="+customWindowSize+"\nValue should be in format org.jpedal.startWindowSize=200x300");

				try{
					width=Integer.parseInt(values.nextToken().trim());
					height=Integer.parseInt(values.nextToken().trim());

				}catch(Exception ee){
					throw new RuntimeException("Unable to use value for org.jpedal.startWindowSize="+customWindowSize+"\nValue should be in format org.jpedal.startWindowSize=200x300");
				}
			}

            /**
			 * Load properties
			 */
			try{
                loadProperties();
            }catch(Exception e){
                e.printStackTrace();
            }

			if (frame instanceof JFrame) {
				((JFrame)frame).setSize(width, height);
				((JFrame)frame).setLocationRelativeTo(null); //centre on screen
				((JFrame)frame).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				((JFrame)frame).addWindowListener(new FrameCloser((Commands) currentCommands, this,decode_pdf,(Printer)currentPrinter,thumbnails,commonValues,properties));
				((JFrame)frame).setVisible(true);
			}
		}

		/**Ensure Document is redrawn when frame is resized and scaling set to width, height or window*/
		frame.addComponentListener(new ComponentListener(){
			public void componentHidden(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentResized(ComponentEvent e) {
				if(decode_pdf.getParent()!=null && getSelectedComboIndex(Commands.SCALING)<3)
					zoom(false);
			}
			public void componentShown(ComponentEvent e) {}
		});
	}

	
	public PdfDecoder getPdfDecoder(){
		return decode_pdf;
	}



	private void handleTabbedPanes() {

		if(tabsNotInitialised)
			return;

		/**
		 * expand size if not already at size
		 */
		int currentSize=displayPane.getDividerLocation();
		int tabSelected=navOptionsPanel.getSelectedIndex();

		if(tabSelected==-1)
			return;

		if(currentSize==startSize){
			/**
			 * workout selected tab
			 */
			String tabName="";
			if(PdfDecoder.isRunningOnMac){
				tabName=navOptionsPanel.getTitleAt(tabSelected);
			}else
				tabName=navOptionsPanel.getIconAt(tabSelected).toString();

			//if(tabName.equals(pageTitle)){
			//add if statement or comment out this section to remove thumbnails
			setupThumbnailPanel();

			//}else if(tabName.equals(bookmarksTitle)){
			setBookmarks(true);
			//}

//			if(searchFrame!=null)
//			searchFrame.find();

			displayPane.setDividerLocation(expandedSize);
		}else if(tabSelected==lastTabSelected)
			displayPane.setDividerLocation(startSize);

		lastTabSelected=tabSelected;
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#addCursor()
	 */
	public void addCursor(){

		/**add cursor location*/
		cursor.setBorder(BorderFactory.createEmptyBorder());
		cursor.setLayout(new FlowLayout(FlowLayout.LEADING));
		cursor.setFloatable(false);
		cursor.setFont(new Font("SansSerif", Font.ITALIC, 10));
		cursor.add(new JLabel(Messages.getMessage("PdfViewerToolbarCursorLoc.text")));

		cursor.add(initCoordBox());

		cursor.setPreferredSize(new Dimension(200,32));

		/**setup cursor*/
		topButtons.add(cursor);

	}

	/**setup keyboard shortcuts*/
	private void setKeyAccelerators(int ID,JMenuItem menuItem){

		int systemMask = java.awt.Event.CTRL_MASK;
		if(decode_pdf.isRunningOnMac){
			systemMask = java.awt.Event.META_MASK;
		}


		switch(ID){

		case Commands.FIND:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,systemMask));
			break;

		case Commands.SAVE:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
					systemMask));
			break;
		case Commands.PRINT:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P,
					systemMask));
			break;
		case Commands.EXIT:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q,
					systemMask));
			break;
		case Commands.DOCINFO:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D,
					systemMask));
			break;
		case Commands.OPENFILE:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
					systemMask));
			break;
		case Commands.OPENURL:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U,
					systemMask));
			break;
		case Commands.PREVIOUSDOCUMENT:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,java.awt.event.KeyEvent.ALT_MASK | java.awt.event.KeyEvent.SHIFT_MASK));
			break;
		case Commands.NEXTDOCUMENT:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,java.awt.event.KeyEvent.ALT_MASK | java.awt.event.KeyEvent.SHIFT_MASK));
			break;
		case Commands.FIRSTPAGE:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME,systemMask));
			break;
		case Commands.BACKPAGE:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,systemMask));
			break;
		case Commands.FORWARDPAGE:
			menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,systemMask));
			break;
		case Commands.LASTPAGE:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END,systemMask));
			break;
		case Commands.GOTO:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,systemMask | java.awt.event.KeyEvent.SHIFT_MASK));
			break;
		case Commands.BITMAP:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,java.awt.event.KeyEvent.ALT_MASK));
			break;
		case Commands.COPY:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,systemMask));
			break;
		case Commands.SELECTALL:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,systemMask));
			break;
		case Commands.DESELECTALL:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,systemMask+java.awt.event.KeyEvent.SHIFT_DOWN_MASK));
			break;
		case Commands.PREFERENCES:
			menuItem.setAccelerator( KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K,systemMask));
			break;

		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#addButton(int, java.lang.String, java.lang.String, int)
	 */
	public void addButton(int line,String toolTip,String path,final int ID) {

		GUIButton newButton = new SwingButton();

		/**specific buttons*/
		switch(ID){



		case Commands.FIRSTPAGE:
			newButton=first;
			break;
		case Commands.FBACKPAGE:
			newButton=fback;
			break;
		case Commands.BACKPAGE:
			newButton=back;
			break;
		case Commands.FORWARDPAGE:
			newButton=forward;
			break;
		case Commands.FFORWARDPAGE:
			newButton=fforward;
			break;
		case Commands.LASTPAGE:
			newButton=end;
			break;
		case Commands.SNAPSHOT:
			newButton=snapshotButton;
			break;
		case Commands.SINGLE:
			newButton=singleButton;
			break;
		case Commands.CONTINUOUS:
			newButton=continuousButton;
			break;
		case Commands.CONTINUOUS_FACING:
			newButton=continuousFacingButton;
			break;
		case Commands.FACING:
			newButton=facingButton;
			break;
		case Commands.SIDESCROLL:
			if(JAIHelper.isJAIused())
				newButton=sideScrollButton;
			else
				return;
			break;
		case Commands.PREVIOUSRESULT:
			newButton=previousSearch;
			break;
		case Commands.NEXTRESULT:
			newButton=nextSearch;
			break;
		case Commands.OPENFILE:
			newButton=openButton;
			newButton.setName("open");
			break;
		case Commands.PRINT:
			newButton=printButton;
			newButton.setName("print");
			break;
		case Commands.FIND:
			newButton=searchButton;
			newButton.setName("search");
			break;
		case Commands.DOCINFO:
			newButton=docPropButton;
			break;
		case Commands.INFO:
			newButton=infoButton;
			break;
		}

		//@kieran : This may be a good idea. See how you feel when time to commit.
		((SwingButton)newButton).addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {
				((SwingButton)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			public void mouseExited(MouseEvent e) {
				((SwingButton)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});

		newButton.init(path, ID,toolTip);

		//add listener
		((AbstractButton) newButton).addActionListener(currentCommandListener);

		//add to toolbar
		if(line==BUTTONBAR){
			topButtons.add((AbstractButton) newButton);
			//topButtons.add(Box.createHorizontalGlue());
		}else if(line==NAVBAR){
			navToolBar.add((AbstractButton)newButton);
		}else if(line==PAGES){
			pagesToolBar.add((AbstractButton)newButton,BorderLayout.CENTER);
		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#addMenuItem(javax.swing.JMenu, java.lang.String, java.lang.String, int)
	 */
	public void addMenuItem(JMenu parentMenu,String text,String toolTip,final int ID) {

		SwingMenuItem menuItem = new SwingMenuItem(text);
		if(toolTip.length()>0)
			menuItem.setToolTipText(toolTip);
		menuItem.setID(ID);
		setKeyAccelerators(ID,menuItem);

		//add listener
		menuItem.addActionListener(currentCommandListener);

		switch(ID){
		case Commands.OPENFILE :
			open = menuItem;
			parentMenu.add(open);
			break;
		case Commands.OPENURL :
			openUrl = menuItem;
			parentMenu.add(openUrl);
			break;
		case Commands.SAVE :
			save = menuItem;
			parentMenu.add(save);
			break;
		case Commands.SAVEFORM :
			reSaveAsForms = menuItem;
			parentMenu.add(reSaveAsForms);
			break;
		case Commands.FIND :
			find = menuItem;
			parentMenu.add(find);
			break;
		case Commands.DOCINFO :
			documentProperties = menuItem;
			parentMenu.add(documentProperties);
			break;
		case Commands.PRINT :
			print = menuItem;
			parentMenu.add(print);
			break;
		case Commands.EXIT :
			exit = menuItem;
            //@chris - memoryleak (so Fest can click it)
            exit.setName("exit");
			parentMenu.add(exit);
			break;
		case Commands.COPY :
			copy = menuItem;
			parentMenu.add(copy);
			break;
		case Commands.SELECTALL :
			selectAll = menuItem;
			parentMenu.add(selectAll);
			break;
		case Commands.DESELECTALL :
			deselectAll = menuItem;
			parentMenu.add(deselectAll);
			break;
		case Commands.PREFERENCES :
			preferences = menuItem;
			parentMenu.add(preferences);
			break;
		case Commands.FIRSTPAGE :
			firstPage = menuItem;
			parentMenu.add(firstPage);
			break;
		case Commands.BACKPAGE :
			backPage = menuItem;
			parentMenu.add(backPage);
			break;
		case Commands.FORWARDPAGE :
			forwardPage = menuItem;
			parentMenu.add(forwardPage);
			break;
		case Commands.LASTPAGE :
			lastPage = menuItem;
			parentMenu.add(lastPage);
			break;
		case Commands.GOTO :
			goTo =menuItem;
			parentMenu.add(goTo);
			break;
		case Commands.PREVIOUSDOCUMENT :
			previousDocument = menuItem;
			parentMenu.add(previousDocument);
			break;
		case Commands.NEXTDOCUMENT :
			nextDocument = menuItem;
			parentMenu.add(nextDocument);
			break;
		case Commands.FULLSCREEN :
			fullscreen = menuItem;
			parentMenu.add(fullscreen);
			break;
		case Commands.CASCADE :
			cascade = menuItem;
			parentMenu.add(cascade);
			break;
		case Commands.TILE :
			tile =menuItem;
			parentMenu.add(tile);
			break;
		case Commands.PDF :
			onePerPage = menuItem;
			parentMenu.add(onePerPage);
			break;
		case Commands.NUP :
			nup = menuItem;
			parentMenu.add(nup);
			break;
		case Commands.HANDOUTS :
			handouts = menuItem;
			parentMenu.add(handouts);
			break;
		case Commands.IMAGES :
			images = menuItem;
			parentMenu.add(images);
			break;
		case Commands.TEXT :
			this.text = menuItem;
			parentMenu.add(this.text);
			break;
		case Commands.BITMAP :
			bitmap = menuItem;
			parentMenu.add(bitmap); break;
		case Commands.ROTATE :
			rotatePages = menuItem;
			parentMenu.add(rotatePages); break;
		case Commands.DELETE :
			deletePages = menuItem;
			parentMenu.add(deletePages);
			break;
		case Commands.ADD :
			addPage = menuItem;
			parentMenu.add(addPage);
			break;
		case Commands.ADDHEADERFOOTER :
			addHeaderFooter = menuItem;
			parentMenu.add(addHeaderFooter);
			break;
		case Commands.STAMPTEXT :
			stampText = menuItem;
			parentMenu.add(stampText);
			break;
		case Commands.STAMPIMAGE :
			stampImage = menuItem;
			parentMenu.add(stampImage);
			break;
		case Commands.SETCROP :
			crop = menuItem;
			parentMenu.add(crop);
			break;
		case Commands.VISITWEBSITE :
			visitWebsite = menuItem;
			parentMenu.add(visitWebsite);
			break;
		case Commands.TIP :
			tipOfTheDay = menuItem;
			parentMenu.add(tipOfTheDay);
			break;
		case Commands.UPDATE :
			checkUpdates = menuItem;
			parentMenu.add(checkUpdates);
			break;
		case Commands.INFO :
			about = menuItem;
			parentMenu.add(about);
			break;


		default : parentMenu.add(menuItem);
		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#addCombo(java.lang.String, java.lang.String, int)
	 */
	public void addCombo(String title,String tooltip,int ID){

		GUICombo combo=null;
		switch (ID){
		case Commands.SCALING:
			combo=scalingBox;
			break;
		case Commands.ROTATION:
			combo=rotationBox;
			break;

		}

		combo.setID(ID);

		optimizationLabel = new JLabel(title);
		if(tooltip.length()>0)
			combo.setToolTipText(tooltip);


        //<start-wrap>
        /**
        //<end-wrap>
		topButtons.add(optimizationLabel);
		topButtons.add((SwingCombo) combo);
        /**/

        //<start-wrap>
        comboBoxBar.add(optimizationLabel);
        comboBoxBar.add((SwingCombo) combo);
        //<end-wrap>

		//add listener
		((SwingCombo)combo).addActionListener(currentCommandListener);

	}


	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setViewerTitle(java.lang.String)
	 */
	public void setViewerTitle(String title) {

        //<start-wrap>
        /**
        //<end-wrap>
        // hard-coded for file
		title=org.jpedal.examples.simpleviewer.SimpleViewer.file;
        /**/

		if(title!=null){

             title="LGPL "+title;
             /**/
			if(frame instanceof JFrame)
				((JFrame)frame).setTitle(title);
		}else{

			String finalMessage="";

			if(titleMessage==null)
				finalMessage=(Messages.getMessage("PdfViewer.titlebar")+ PdfDecoder.version + ' ' + commonValues.getSelectedFile());
			else
				finalMessage=titleMessage+ commonValues.getSelectedFile();


             finalMessage="LGPL "+finalMessage;
             /**/
			if(commonValues.isFormsChanged())
				finalMessage="* "+finalMessage;

			if(frame instanceof JFrame)
				((JFrame)frame).setTitle(finalMessage);

		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#resetComboBoxes(boolean)
	 */
	public void resetComboBoxes(boolean value) {
		scalingBox.setEnabled(value);
		rotationBox.setEnabled(value);

	}


	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#createPane(javax.swing.JTextPane, java.lang.String, boolean)
	 */
	public final JScrollPane createPane(JTextPane text_pane,String content, boolean useXML) throws BadLocationException {

		text_pane.setEditable(true);
		text_pane.setFont(new Font("Lucida", Font.PLAIN, 14));

		text_pane.setToolTipText(Messages.getMessage("PdfViewerTooltip.text"));
		Document doc = text_pane.getDocument();
		text_pane.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), Messages.getMessage("PdfViewerTitle.text")));
		text_pane.setForeground(Color.black);

		SimpleAttributeSet token_attribute = new SimpleAttributeSet();
		SimpleAttributeSet text_attribute = new SimpleAttributeSet();
		SimpleAttributeSet plain_attribute = new SimpleAttributeSet();
		StyleConstants.setForeground(token_attribute, Color.blue);
		StyleConstants.setForeground(text_attribute, Color.black);
		StyleConstants.setForeground(plain_attribute, Color.black);
		int pointer=0;

		/**put content in and color XML*/
		if((useXML)&&(content!=null)){
			//tokenise and write out data
			StringTokenizer data_As_tokens = new StringTokenizer(content,"<>", true);

			while (data_As_tokens.hasMoreTokens()) {
				String next_item = data_As_tokens.nextToken();

				if ((next_item.equals("<"))&&((data_As_tokens.hasMoreTokens()))) {

					String current_token = next_item + data_As_tokens.nextToken()+ data_As_tokens.nextToken();

					doc.insertString(pointer, current_token,token_attribute);
					pointer = pointer + current_token.length();

				} else {
					doc.insertString(pointer, next_item, text_attribute);
					pointer = pointer + next_item.length();
				}
			}
		}else
			doc.insertString(pointer,content, plain_attribute);

		//wrap in scrollpane
		JScrollPane text_scroll = new JScrollPane();
		text_scroll.getViewport().add( text_pane );
		text_scroll.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		text_scroll.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		return text_scroll;
	}


	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getSelectedComboIndex(int)
	 */
	public int getSelectedComboIndex(int ID) {

		switch (ID){
		case Commands.SCALING:
			return scalingBox.getSelectedIndex();
		case Commands.ROTATION:
			return rotationBox.getSelectedIndex();
		default:
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setSelectedComboIndex(int, int)
	 */
	public void setSelectedComboIndex(int ID,int index) {
		switch (ID){
		case Commands.SCALING:
			scalingBox.setSelectedIndex(index);
			break;
		case Commands.ROTATION:
			rotationBox.setSelectedIndex(index);
			break;

		}

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setSelectedComboItem(int, java.lang.String)
	 */
	public void setSelectedComboItem(int ID,String index) {
		switch (ID){
		case Commands.SCALING:
			scalingBox.setSelectedItem(index);
			break;
		case Commands.ROTATION:
			rotationBox.setSelectedItem(index);
			break;

		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getSelectedComboItem(int)
	 */
	public Object getSelectedComboItem(int ID) {

		switch (ID){
		case Commands.SCALING:
			return scalingBox.getSelectedItem();
		case Commands.ROTATION:
			return rotationBox.getSelectedItem();
		default:
			return null;

		}
	}


	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#zoom()
	 */
	public void zoom(boolean Rotated) {

		float width,height;

		if(isSingle){

			width = scrollPane.getViewport().getWidth()-inset-inset;
			height = scrollPane.getViewport().getHeight()-inset-inset;

		}else{
			width=desktopPane.getWidth();
			height=desktopPane.getHeight();
		}

		if(decode_pdf!=null){

			//get current location and factor out scaling so we can put back at same page
			//final float x= (decode_pdf.getVisibleRect().x/scaling);
			//final float y= (decode_pdf.getVisibleRect().y/scaling);

			//System.out.println(x+" "+y+" "+scaling+" "+decode_pdf.getVisibleRect());
			/** update value and GUI */
			int index=getSelectedComboIndex(Commands.SCALING);
			if(index==-1){
				String numberValue=(String)getSelectedComboItem(Commands.SCALING);
				float zoom=-1;
				if((numberValue!=null)&&(numberValue.length()>0)){
					try{
						zoom= Float.parseFloat(numberValue);
					}catch(Exception e){
						zoom=-1;
						//its got characters in it so get first valid number string
						int length=numberValue.length();
						int ii=0;
						while(ii<length){
							char c=numberValue.charAt(ii);
							if(((c>='0')&&(c<='9'))|(c=='.'))
								ii++;
							else
								break;
						}

						if(ii>0)
							numberValue=numberValue.substring(0,ii);

						//try again if we reset above
						if(zoom==-1){
							try{
								zoom= Float.parseFloat(numberValue);
							}catch(Exception e1){zoom=-1;}
						}
					}
					if(zoom>1000){
						zoom=1000;
					}
				}

				//if nothing off either attempt, use window value
				if(zoom==-1){
					//its not set so use To window value
					index=defaultSelection;
					setSelectedComboIndex(Commands.SCALING, index);
				}else{
					scaling=decode_pdf.getDPIFactory().adjustScaling(zoom/100);

					setSelectedComboItem(Commands.SCALING, String.valueOf(zoom));
				}
			}

			if(index!=-1){
				if(index<3){ //handle scroll to width/height/window
					PdfPageData pageData = decode_pdf.getPdfPageData();
					int cw,ch,raw_rotation=pageData.getRotation(commonValues.getCurrentPage());
					if(rotation==90 || rotation==270){
						cw = pageData.getCropBoxHeight(commonValues.getCurrentPage());
						ch = pageData.getCropBoxWidth(commonValues.getCurrentPage());
					}else{
						cw = pageData.getCropBoxWidth(commonValues.getCurrentPage());
						ch = pageData.getCropBoxHeight(commonValues.getCurrentPage());
					}

					if(isSingle){

						if(displayPane!=null)
							width = width-displayPane.getDividerSize();

					}

					float x_factor=0,y_factor=0;
					x_factor = width / cw;
					y_factor = height / ch;

					if(index==0){//window
						if(x_factor<y_factor)
							scaling = x_factor;
						else
							scaling = y_factor;
					}else if(index==1)//height
						scaling = y_factor;
					else if(index==2)//width
						scaling = x_factor;
				}else{
					scaling=decode_pdf.getDPIFactory().adjustScaling(scalingFloatValues[index]);
				}
			}

			//this check for 0 to avoid error  and replace with 1
			//PdfPageData pagedata = decode_pdf.getPdfPageData();
			//if((pagedata.getCropBoxHeight(commonValues.getCurrentPage())*scaling<100) &&//keep the page bigger than 100 pixels high
			//        (pagedata.getCropBoxWidth(commonValues.getCurrentPage())*scaling<100) && commonValues.isPDF()){//keep the page bigger than 100 pixels wide
			//    scaling=1;
			//    setSelectedComboItem(Commands.SCALING,"100");
			//}

			// THIS section commented out so altering scalingbox does NOT reset rotation
			//if(!scalingBox.getSelectedIndex()<3){
			/**update our components*/
			//resetRotationBox();
			//}
			
			//Ensure page rotation is taken into account
			//int pageRot = decode_pdf.getPdfPageData().getRotation(commonValues.getCurrentPage());
			//allow for clicking on it before page opened
			decode_pdf.setPageParameters(scaling, commonValues.getCurrentPage(),rotation);
			
			//Ensure the page is displayed in the correct rotation
			setRotation();

			//move to correct page
			//setPageNumber();
			//decode_pdf.setDisplayView(decode_pdf.getDisplayView(),Display.DISPLAY_CENTERED);

			//open new page
			//if((!commonValues.isProcessing())&&(commonValues.getCurrentPage()!=newPage)){

			//commonValues.setCurrentPage(newPage);
			//decodePage(false);
			//currentGUI.zoom();
			//}

			//ensure at same page location

			Runnable updateAComponent = new Runnable() {
				public void run() {
					//
					decode_pdf.invalidate();
					decode_pdf.updateUI();
					decode_pdf.validate();

					scrollPane.invalidate();
					scrollPane.updateUI();
					scrollPane.validate();

					//move to correct page
					//scrollToPage is handled via the page change code so no need to do it here
//					if(commonValues.isPDF())
//					scrollToPage(commonValues.getCurrentPage());
					//scrollPane.getViewport().scrollRectToVisible(new Rectangle((int)(x*scaling)-1,(int)(y*scaling),1,1));
					//System.out.println("Scroll to page="+y+" "+(y*scaling)+" "+scaling);

				}
			};
			//boolean callAsThread=SwingUtilities.isEventDispatchThread();
			//if (callAsThread)
			//	scroll
			SwingUtilities.invokeLater(updateAComponent);
//			else{

//			//move to correct page
//			if(commonValues.isPDF())
//			scrollToPage(commonValues.getCurrentPage());

//			scrollPane.updateUI();

//			}
			//decode_pdf.invalidate();
			//scrollPane.updateUI();
			//decode_pdf.repaint();
			//scrollPane.repaint();
			//frame.validate();


		}


	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#rotate()
	 */
	public void rotate() {
		rotation=Integer.parseInt((String) getSelectedComboItem(Commands.ROTATION));
		zoom(true);
		decode_pdf.updateUI();

	}

	public void scrollToPage(int page){

		commonValues.setCurrentPage(page);

		if(commonValues.getCurrentPage()>0){

			int yCord =0;
			int xCord =0;

			if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE){
				if(decode_pdf.getDisplayView()==Display.SIDE_SCROLL){
					yCord = decode_pdf.getY();
					xCord = decode_pdf.getXCordForPage(commonValues.getCurrentPage(),scaling);
				}else{
					yCord = decode_pdf.getYCordForPage(commonValues.getCurrentPage(),scaling);
					xCord = decode_pdf.getXDisplacement();
				}
			}
			//System.out.println("Before="+decode_pdf.getVisibleRect()+" "+decode_pdf.getPreferredSize());

			PdfPageData pageData = decode_pdf.getPdfPageData();

			int ch = (int)(pageData.getCropBoxHeight(commonValues.getCurrentPage())*scaling);
			int cw = (int)(pageData.getCropBoxWidth(commonValues.getCurrentPage())*scaling);

			int centerH = xCord + ((cw-scrollPane.getHorizontalScrollBar().getVisibleAmount())/2);
			int centerV = yCord + (ch-scrollPane.getVerticalScrollBar().getVisibleAmount())/2;

			scrollPane.getHorizontalScrollBar().setValue(centerH);
			scrollPane.getVerticalScrollBar().setValue(centerV);



//			decode_pdf.scrollRectToVisible(new Rectangle(0,(int) (yCord),(int)r.width-1,(int)r.height-1));
//			decode_pdf.scrollRectToVisible(new Rectangle(0,(int) (yCord),(int)r.width-1,(int)r.height-1));

			//System.out.println("After="+decode_pdf.getVisibleRect()+" "+decode_pdf.getPreferredSize());

			//System.out.println("Scroll to page="+commonValues.getCurrentPage()+" "+yCord+" "+(yCord*scaling)+" "+scaling);
		}

		if(decode_pdf.getPageCount()>1 && !commonValues.isContentExtractor())
			setPageLayoutButtonsEnabled(true);

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#decodePage(boolean)
	 */
	public void decodePage(final boolean resizePanel){

		//Remove Image extraction outlines when page is changed
		decode_pdf.setHighlightedImage(null);
		
		resetRotationBox();
		
		/** if running terminate first */
		if(thumbnails.isShownOnscreen())
			thumbnails.terminateDrawing();

		if(thumbnails.isShownOnscreen())
			setupThumbnailPanel();

		if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE){
			pageCounter2.setForeground(Color.black);
			pageCounter2.setText(" " + commonValues.getCurrentPage());
			pageCounter3.setText(Messages.getMessage("PdfViewerOfLabel.text") + ' ' + commonValues.getPageCount());
		}

		//allow user to now open tabs
		tabsNotInitialised=false;

		boolean isContentExtractor=commonValues.isContentExtractor();

		decode_pdf.unsetScaling();

		/**ensure text and color extracted. If you do not need color, take out
		 * line for faster decode
		 */
		if(isContentExtractor)
			decode_pdf.setExtractionMode(PdfDecoder.TEXT);
		else
			decode_pdf.setExtractionMode(PdfDecoder.TEXT+PdfDecoder.TEXTCOLOR);


		//remove any search highlight
		decode_pdf.setMouseHighlightArea(null);
		decode_pdf.setMouseHighlightAreas(null);

		setRectangle(null);


		//decode_pdf.clearScreen();


		//stop user changing scaling while decode in progress
		resetComboBoxes(false);
		if(!commonValues.isContentExtractor())
			setPageLayoutButtonsEnabled(false);

		if(!commonValues.isContentExtractor())
			commonValues.setProcessing(true);

		SwingWorker worker = new SwingWorker() {

			public Object construct() {
				decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				try {

					statusBar.updateStatus("Decoding Page",0);

					/**
					 * make sure screen fits display nicely
					 */
					//if ((resizePanel) && (thumbnails.isShownOnscreen()))
					//	zoom();

					if (Thread.interrupted())
						throw new InterruptedException();

					/**
					 * decode the page
					 */
					try {
						decode_pdf.decodePage(commonValues.getCurrentPage());

						if(!decode_pdf.getPageDecodeStatus(DecodeStatus.ImagesProcessed)){

							String status = (Messages.getMessage("PdfViewer.ImageDisplayError")+
									Messages.getMessage("PdfViewer.ImageDisplayError1")+
									Messages.getMessage("PdfViewer.ImageDisplayError2")+
									Messages.getMessage("PdfViewer.ImageDisplayError3")+
									Messages.getMessage("PdfViewer.ImageDisplayError4")+
									Messages.getMessage("PdfViewer.ImageDisplayError5")+
									Messages.getMessage("PdfViewer.ImageDisplayError6")+
									Messages.getMessage("PdfViewer.ImageDisplayError7"));

							showMessageDialog(status);
						}

                        /**
                         * tell user we could decode files with profiles if not already used
                        */
                        if(decode_pdf.getPageDecodeStatus(DecodeStatus.YCCKImages) &&
                                System.getProperty("org.jpedal.useICC")==null){

							String status = (Messages.getMessage("PdfViewer.ImageYCCKError")+
									Messages.getMessage("PdfViewer.ImageYCCKError1")+
									Messages.getMessage("PdfViewer.ImageYCCKError2")+
									Messages.getMessage("PdfViewer.ImageYCCKError3")+
									Messages.getMessage("PdfViewer.ImageYCCKError4")+
									Messages.getMessage("PdfViewer.ImageYCCKError5"));

							showMessageDialog(status);
						}

                        //@chris - this is the message I get in FormClickTest. Do we have a testing variable
                        //I could use to disable?
						if(decode_pdf.getPageDecodeStatus(DecodeStatus.NonEmbeddedCIDFonts)){

							String status = ("This page contains non-embedded CID fonts \n" +
									decode_pdf.getPageDecodeStatusReport(DecodeStatus.NonEmbeddedCIDFonts)+
									"\nwhich may need mapping to display correctly.\n" +
							"See http://www.jpedal.org/support_FontSub.php");

							showMessageDialog(status);
						}
						//read values for page display
						PdfPageData page_data = decode_pdf.getPdfPageData();

						mediaW  = page_data.getMediaBoxWidth(commonValues.getCurrentPage());
						mediaH = page_data.getMediaBoxHeight(commonValues.getCurrentPage());
						mediaX = page_data.getMediaBoxX(commonValues.getCurrentPage());
						mediaY = page_data.getMediaBoxY(commonValues.getCurrentPage());

						cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
						cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
						cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
						cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());

//						resetRotationBox();


						//create custom annot icons
						if(addUniqueIconToFileAttachment){
							/**
							 * ANNOTATIONS code to create unique icons
							 *
							 * this code allows you to create a unique set on icons for any type of annotations, with
							 * an icons for every annotation, not just types.
							 */
							FormFactory formfactory = decode_pdf.getFormRenderer().getFormFactory();

							//swing needs it to be done with invokeLater
							if(formfactory.getType()== FormFactory.SWING){
								final Runnable doPaintComponent2 = new Runnable() {
									public void run() {

										createUniqueAnnotationIcons();

										//validate();
									}
								};
								SwingUtilities.invokeLater(doPaintComponent2);

							}else{
								createUniqueAnnotationIcons();
							}


						}

						statusBar.updateStatus("Displaying Page",0);

					} catch (Exception e) {
						System.err.println(Messages.getMessage("PdfViewerError.Exception")+ ' ' + e +
								' ' +Messages.getMessage("PdfViewerError.DecodePage"));
						e.printStackTrace();
						commonValues.setProcessing(false);
					}


					//tell user if we had a memory error on decodePage
					String status=decode_pdf.getPageDecodeReport();
					if((status.indexOf("java.lang.OutOfMemoryError")!=-1)&& PdfDecoder.showErrorMessages){
						status = (Messages.getMessage("PdfViewer.OutOfMemoryDisplayError")+
								Messages.getMessage("PdfViewer.OutOfMemoryDisplayError1")+
								Messages.getMessage("PdfViewer.OutOfMemoryDisplayError2")+
								Messages.getMessage("PdfViewer.OutOfMemoryDisplayError3")+
								Messages.getMessage("PdfViewer.OutOfMemoryDisplayError4")+
								Messages.getMessage("PdfViewer.OutOfMemoryDisplayError5"));

						showMessageDialog(status);

					}

					/**
					 *  add this page as thumbnail - we don't need to decode twice
					 */

					if(decode_pdf.getPageCount()>0 && thumbnails!=null && decode_pdf.getDisplayView()==Display.SINGLE_PAGE && isSingle )
						thumbnails.addDisplayedPageAsThumbnail(commonValues.getCurrentPage(),null);

					commonValues.setProcessing(false);

					//make sure fully drawn
					//decode_pdf.repaint();

					setViewerTitle(null); //restore title

					currentCommands.setPageProperties(getSelectedComboItem(Commands.ROTATION), getSelectedComboItem(Commands.SCALING));

					if (decode_pdf.getPageCount()>0 && thumbnails.isShownOnscreen() && decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
						thumbnails.generateOtherVisibleThumbnails(commonValues.getCurrentPage());

				} catch (Exception e) {
					e.printStackTrace();
					setViewerTitle(null); //restore title
				}

				selectBookmark();

				statusBar.setProgress(100);

				//reanable user changing scaling
				resetComboBoxes(true);
				if(decode_pdf.getPageCount()>1 && !commonValues.isContentExtractor())
					setPageLayoutButtonsEnabled(true);

				addFormsListeners();

				//add a border
				decode_pdf.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));

				/** turn off border in printing */
				decode_pdf.disableBorderForPrinting();

				//resize (ensure at least certain size)
				//zoom(flase) is called twice so remove this call
				//zoom(false);

				//<link><a name="draw" />

				// sample code to add shapes and text on current page - should be called AFTER page decoded for display
				// (can appear on multiple pages for printing)
				//

				// in this example, we create a rectangle, a filled rectangle and draw some text.

				//initialise objects arrays - we will put 4 shapes on the page
				// (using PDF co-ordinates with origin bottom left corner)
				/*int count=4; //adding shapes to page

                // Due to the way some pdf's are created it is necessery to take the offset of a page
                // into account when addding custom objects to the page. Variables mX and mY represent
                // that offset and need to be taken in to account when placing any additional object
                // on a page.

                int mX = decode_pdf.getPdfPageData().getMediaBoxX(1);
                int mY = decode_pdf.getPdfPageData().getMediaBoxY(1);
                int[] type=new int[count];
                Color[] colors=new Color[count];
                Object[] obj=new Object[count];

                //example stroked shape
                type[0]= org.jpedal.render.DynamicVectorRenderer.STROKEDSHAPE;
                colors[0]=Color.RED;
                obj[0]=new Rectangle(35+mX,35+mY,510,50); //ALSO sets location. Any shape can be used

                //example filled shape
                type[1]= org.jpedal.render.DynamicVectorRenderer.FILLEDSHAPE;
                colors[1]=Color.GREEN;
                obj[1]=new Rectangle(40+mX,40+mY,500,40); //ALSO sets location. Any shape can be used

                //example text object
                type[2]= org.jpedal.render.DynamicVectorRenderer.STRING;
                org.jpedal.render.TextObject textObject=new org.jpedal.render.TextObject(); //composite object so we can pass in parameters
                textObject.x=40+mX;
                textObject.y=40+mY;
                textObject.text="Example text on page "+commonValues.getCurrentPage();
                textObject.font=new Font("Serif",Font.PLAIN,48);
                colors[2]=Color.BLUE;
                obj[2]=textObject; //ALSO sets location

                //example custom (from version 3.40)
                type[3]=org.jpedal.render.DynamicVectorRenderer.CUSTOM;

                JPedalCustomDrawObject exampleObj=new ExampleCustomDrawObject();
                exampleObj.setMedX(mX);
                exampleObj.setMedY(mY);

                obj[3]=exampleObj;

                //pass into JPEDAL after page decoded - will be removed automatically on new page/open file
                //BUT PRINTING retains values until manually removed
                try{
                    decode_pdf.drawAdditionalObjectsOverPage(commonValues.getCurrentPage(),type,colors,obj);
                }catch(PdfException e){
                    e.printStackTrace();
                }
                /**/

				//<link><a name="remove_additional_obj" />
				//this code will remove ALL items already drawn on page
				//try{
				//    decode_pdf.flushAdditionalObjectsOnPage(commonValues.getCurrentPage());
				//}catch(PdfException e){
				//    e.printStackTrace();
				//    //ShowGUIMessage.showGUIMessage( "", new JLabel(e.getMessage()),"Exception adding object to display");
				//}


				//<link><a name="print" />

				//Example to PRINT (needs to be create beforehand)
				//objects can be the same as from draw

				/* for(int pages=1;pages<decode_pdf.getPageCount()+1;pages++){ //note +1 for last page!!!
                    int count = 4;

                     // Due to the way some pdf's are created it is necessery to take the offset of a page
                     // into account when addding custom objects to the page. Variables mX and mY represent
                     // that offset and need to be taken in to account when placing any additional object
                     // on a page.

                    int mX = decode_pdf.getPdfPageData().getMediaBoxX(1);
                    int mY = decode_pdf.getPdfPageData().getMediaBoxY(1);
                    int[] typePrint=new int[count];
                    Color[] colorsPrint=new Color[count];
                    Object[] objPrint=new Object[count];

					//example custom (from version 3.40)
                    typePrint[0]=org.jpedal.render.DynamicVectorRenderer.CUSTOM;

                    JPedalCustomDrawObject examplePrintObj=new ExampleCustomDrawObject();
                    examplePrintObj.setMedX(mX);
                    examplePrintObj.setMedY(mY);

                    objPrint[0]=examplePrintObj;

                    //example stroked shape
                    typePrint[1]= org.jpedal.render.DynamicVectorRenderer.STROKEDSHAPE;
                    colorsPrint[1]=Color.RED;
                    objPrint[1]=new Rectangle(35+mX,35+mY,510,50); //ALSO sets location. Any shape can be used

                    //example filled shape
                    typePrint[2]= org.jpedal.render.DynamicVectorRenderer.FILLEDSHAPE;
                    colorsPrint[2]=Color.GREEN;
                    objPrint[2]=new Rectangle(40+mX,40+mY,500,40); //ALSO sets location. Any shape can be used

                    //example text object
                    typePrint[3]= org.jpedal.render.DynamicVectorRenderer.STRING;
                    org.jpedal.render.TextObject textPrintObject=new org.jpedal.render.TextObject(); //composite object so we can pass in parameters
                    textPrintObject.x=40+mX;
                    textPrintObject.y=40+mY;
                    textPrintObject.text="Print Ex text on page "+pages;
                    textPrintObject.font=new Font("Serif",Font.PLAIN,48);
                    colorsPrint[3]=Color.BLUE;
                    objPrint[3]=textPrintObject; //ALSO sets location

                    //pass into JPEDAL after page decoded - will be removed automatically on new page/open file
                    //BUT PRINTING retains values until manually removed
                    try{
                        decode_pdf.printAdditionalObjectsOverPage(pages,typePrint ,colorsPrint, objPrint);
                    }catch(PdfException e){
                        e.printStackTrace();
                    }

                }
				/**/


				//<link><a name="global_print" />
				//global printout
				/*	int count = 1;

                 // Due to the way some pdf's are created it is necessery to take the offset of a page
                 // into account when addding custom objects to the page. Variables medX and medY represent
                 // that offset and need to be taken in to account when placing any additional object
                 // on a page.

				int medX = decode_pdf.getPdfPageData().getMediaBoxX(1);
                int medY = decode_pdf.getPdfPageData().getMediaBoxY(1);
                int[] typePrint=new int[count];
                Color[] colorsPrint=new Color[count];
                Object[] objPrint=new Object[count];
                //example custom (from version 3.40)
                typePrint[0]=org.jpedal.render.DynamicVectorRenderer.CUSTOM;

                JPedalCustomDrawObject exampleGlobalPrintObj=new ExampleCustomDrawObject(JPedalCustomDrawObject.ALLPAGES);
                exampleGlobalPrintObj.setMedX(medX);
                exampleGlobalPrintObj.setMedY(medY);

                //JPedalCustomDrawObject examplePrintObj=new ExampleCustomDrawObject();

                objPrint[0]=exampleGlobalPrintObj;

                //pass into JPEDAL after page decoded - will be removed automatically on new page/open file
                //BUT PRINTING retains values until manually removed
                try{
                    decode_pdf.printAdditionalObjectsOverAllPages(typePrint ,colorsPrint, objPrint);
                }catch(PdfException e){
                    e.printStackTrace();
                }/**/


				if(displayPane!=null)
					reinitialiseTabs(displayPane.getDividerLocation() > startSize);

				finishedDecoding=true;

				//scrollPane.updateUI();

				zoom(false);

				decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				return null;
			}
		};

		worker.start();

		//zoom(false);
		/**/
	}

//	<link><a name="listen" />

	/**this method adds listeners to GUI widgets to track changes*/
	public void addFormsListeners(){

		//rest forms changed flag to show no changes
		commonValues.setFormsChanged(false);

		/**see if flag set - not default behaviour*/
		boolean showMessage=false;
		String formsFlag=System.getProperty("org.jpedal.listenforms");
		if(formsFlag!=null)
			showMessage=true;

		//get the form renderer which also contains the processed form data.
		//if you want simple form data, also look at the ExtractFormDataAsObject.java example
		org.jpedal.objects.acroforms.rendering.AcroRenderer formRenderer=decode_pdf.getFormRenderer();

		if(formRenderer==null)
			return;

		//get list of forms on page
		java.util.List formsOnPage=null;

		/**
		 * Or you can also use
		 * formRenderer.getDisplayComponentsForPage(commonValues.getCurrentPage());
		 * to get all components directly - we have already checked formRenderer not null
		 */
		try {
			formsOnPage = formRenderer.getComponentNameList(commonValues.getCurrentPage());
		} catch (PdfException e) {

			LogWriter.writeLog("Exception "+e+" reading component list");
		}

		//allow for no forms
		if(formsOnPage==null){

			if(showMessage)
				showMessageDialog(Messages.getMessage("PdfViewer.NoFields"));

			return;
		}

		int formCount=formsOnPage.size();

		JPanel formPanel=new JPanel();
		/**
		 * create a JPanel to list forms and tell user a box example
		 **/
		if(showMessage){
			formPanel.setLayout(new BoxLayout(formPanel,BoxLayout.Y_AXIS));
			JLabel formHeader = new JLabel("This page contains "+formCount+" form objects");
			formHeader.setFont(headFont);
			formPanel.add(formHeader);

			formPanel.add(Box.createRigidArea(new Dimension(10,10)));
			JTextPane instructions = new JTextPane();
			instructions.setPreferredSize(new Dimension(450,180));
			instructions.setEditable(false);
			instructions.setText("This provides a simple example of Forms handling. We have"+
					" added a listener to each form so clicking on it shows the form name.\n\n"+
					"Code is in addExampleListeners() in org.examples.simpleviewer.SimpleViewer\n\n"+
					"This could be easily be extended to interface with a database directly "+
					"or collect results on an action and write back using itext.\n\n"+
					"Forms have been converted into Swing components and are directly accessible"+
					" (as is the original data).\n\n"+
			"If you don't like the standard SwingSet you can replace with your own set.");
			instructions.setFont(textFont);
			formPanel.add(instructions);
			formPanel.add(Box.createRigidArea(new Dimension(10,10)));
		}

		/**
		 * access all forms in turn and add a listener
		 */
		for(int i=0;i<formCount;i++){

			//get name of form
			String formName=(String) formsOnPage.get(i);

			//get actual component - do not display it separately -
			//at the moment this will not work on group objects (ie radio buttons and checkboxes)
			Component[] comp=(Component[])formRenderer.getComponentsByName(formName);

			/**
			 * add listeners on first decode - not needed if we revisit page
			 *
			 * DO NOT remove listeners from Components as used internally to control appearance
			 */
			Integer pageKey=new Integer(i);
			if(comp!=null && pagesDecoded.get(pageKey)==null){

				//simple device to prevent multiple listeners
				pagesDecoded.put(pageKey,"x");

				//loop through all components returned
				int count=comp.length;
				for(int index=0;index<count;index++){

					//add details to screen display, group objects have the same name so add them only once
					if((showMessage)&&(index==0)){
						JLabel type = new JLabel();
						JLabel label = new JLabel("Form name="+formName);
						String labelS = "type="+comp[index].getClass();
						if(count>1){
							labelS = "Group of "+count+" Objects, type="+comp[index].getClass();
							type.setForeground(Color.red);
						}
						type.setText(labelS);
						label.setFont(headFont);
						type.setFont(textFont);
						formPanel.add(label);
						formPanel.add(type);

						formPanel.add(new JLabel(" "));
					}

					//add listeners to show proof of concept - this
					//could equally be inserting into database

					//combo boxes
					FormActionListener changeList=new FormActionListener(formName+index,frame,showMessage);
					if(comp[index] instanceof JComboBox){
						((JComboBox)comp[index]).addActionListener(changeList);
					}else if(comp[index] instanceof JCheckBox){
						((JCheckBox)comp[index]).addActionListener(changeList);
					}else if(comp[index] instanceof JRadioButton){
						((JRadioButton)comp[index]).addActionListener(changeList);
					}else if(comp[index] instanceof JTextField){
						((JTextField)comp[index]).addActionListener(changeList);
					}
				}
			}
		}

		/**
		 * pop-up to show forms on page
		 **/
		if(showMessage){
			final JDialog displayFrame =  new JDialog((JFrame)null,true);
			if(commonValues.getModeOfOperation()!=Values.RUNNING_APPLET){
				displayFrame.setLocationRelativeTo(null);
				displayFrame.setLocation(frame.getLocationOnScreen().x+10,frame.getLocationOnScreen().y+10);
			}

			JScrollPane scroll=new JScrollPane();
			scroll.getViewport().add(formPanel);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			displayFrame.setSize(500,500);
			displayFrame.setTitle("List of forms on this page");
			displayFrame.getContentPane().setLayout(new BorderLayout());
			displayFrame.getContentPane().add(scroll,BorderLayout.CENTER);

			JPanel buttonBar=new JPanel();
			buttonBar.setLayout(new BorderLayout());
			displayFrame.getContentPane().add(buttonBar,BorderLayout.SOUTH);

			// close option just removes display
			JButton no=new JButton(Messages.getMessage("PdfViewerButton.Close"));
			no.setFont(new Font("SansSerif", Font.PLAIN, 12));
			buttonBar.add(no,BorderLayout.EAST);
			no.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					displayFrame.dispose();
				}});

			/**show the popup*/
			displayFrame.setVisible(true);
		}

	}

	/**
	 *  put the outline data into a display panel which we can pop up
	 * for the user - outlines, thumbnails
	 */
	private void createOutlinePanels() {

		//boolean hasNavBars=false;

		/**
		 * set up first 10 thumbnails by default. Rest created as needed.
		 */
		//add if statement or comment out this section to remove thumbnails
		setupThumbnailPanel();

		/**
		 * add any outline
		 */
		setBookmarks(false);

		/**
		 * resize to show if there are nav bars
		 *
        if(hasNavBars){
            if(!thumbnails.isShownOnscreen()){
                if( !commonValues.isContentExtractor())
                navOptionsPanel.setVisible(true);
                displayPane.setDividerLocation(divLocation);
                //displayPane.invalidate();
                //displayPane.repaint();

            }
        }*/
	}

//	<start-thin>
public void setupThumbnailPanel() {

		decode_pdf.addExternalHandler(thumbnails, Options.ThumbnailHandler);

		if(isSetup)
			return;

		isSetup=true;

		if(!commonValues.isContentExtractor() && thumbnails.isShownOnscreen()){

			int pages=decode_pdf.getPageCount();

			//setup and add to display

			thumbnails.setupThumbnails(pages,textFont, Messages.getMessage("PdfViewerPageLabel.text"),decode_pdf.getPdfPageData());

			//add listener so clicking on button changes to page - has to be in SimpleViewer so it can update it
			Object[] buttons=thumbnails.getButtons();
			for(int i=0;i<pages;i++)
				((JButton)buttons[i]).addActionListener(new PageChanger(i));

			//add global listener
			thumbnails.addComponentListener();

		}
	}
//	<end-thin>

	public void setBookmarks(boolean alwaysGenerate) {

		//ignore if not opened
		int currentSize=displayPane.getDividerLocation();

		if((currentSize==startSize)&& !alwaysGenerate)
			return;

		org.w3c.dom.Document doc=decode_pdf.getOutlineAsXML();

		Node rootNode= null;
		if(doc!=null)
			rootNode= doc.getFirstChild();

		if(rootNode!=null){

			tree.reset(rootNode);

			//Listen for when the selection changes - looks up dests at present
			((JTree) tree.getTree()).addTreeSelectionListener(new TreeSelectionListener(){

				/** Required by TreeSelectionListener interface. */
				public void valueChanged(TreeSelectionEvent e) {

					if(tree.isIgnoreAlteredBookmark())
						return;

					DefaultMutableTreeNode node = tree.getLastSelectedPathComponent();

					if (node == null)
						return;

					/**get title and open page if valid*/
					String title=(String)node.getUserObject();

					JTree jtree = ((JTree) tree.getTree());

					DefaultTreeModel treeModel = (DefaultTreeModel) jtree.getModel();

					List flattenedTree = new ArrayList();

					/** flatten out the tree so we can find the index of the selected node */
					getFlattenedTreeNodes((TreeNode) treeModel.getRoot(), flattenedTree);
					flattenedTree.remove(0); // remove the root node as we don't account for this

					int index = flattenedTree.indexOf(node);

					String page = tree.getPageViaNodeNumber(index);

                    if((page==null)||(page.length()==0))
                    page=tree.getPage(title);

                    if((page!=null)&&(page.length()>0)){
						int pageToDisplay=Integer.parseInt(page);

						if((!commonValues.isProcessing())&&(commonValues.getCurrentPage()!=pageToDisplay)){
							commonValues.setCurrentPage(pageToDisplay);
							/**reset as rotation may change!*/

							decode_pdf.setPageParameters(getScaling(), commonValues.getCurrentPage());
							decodePage(false);
						}

						Point p= tree.getPoint(title);
						if(p!=null)
							decode_pdf.ensurePointIsVisible(p);

					}else{
						showMessageDialog(Messages.getMessage("PdfViewerError.NoBookmarkLink")+title);
						System.out.println("No dest page set for "+title);
					}
				}
			});

		}else{
			tree.reset(null);
		}
	}

	private void getFlattenedTreeNodes(TreeNode theNode, List items) {
		// add the item
		items.add(theNode);

		// recursion
		for (Enumeration theChildren = theNode.children(); theChildren.hasMoreElements();) {
			getFlattenedTreeNodes((TreeNode) theChildren.nextElement(), items);
		}
	}

	private void selectBookmark() {
		if(decode_pdf.hasOutline()&&(tree!=null))
			tree.selectBookmark();

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#initStatus()
	 */
	public void initStatus() {
		decode_pdf.setStatusBarObject(statusBar);
		resetStatus();

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#resetStatus()
	 */
	public void resetStatus() {
		//set status bar child color
		statusBar.setColorForSubroutines(Color.blue);

        //<start-wrap>
		//and initialise the display
		comboBoxBar.add(statusBar.getStatusObject());
        //<end-wrap>

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#initThumbnails(int, org.jpedal.utils.repositories.Vector_Int)
	 */
	public void initThumbnails(int itemSelectedCount, Vector_Int pageUsed) {

		navOptionsPanel.removeAll();
		if(thumbnails.isShownOnscreen())
			thumbnails.setupThumbnails(itemSelectedCount-1,pageUsed.get(),commonValues.getPageCount());

		if(PdfDecoder.isRunningOnMac){
			navOptionsPanel.add((Component) thumbnails,"Extracted items");
		}else{
			VTextIcon textIcon2 = new VTextIcon(navOptionsPanel, "Extracted items", VTextIcon.ROTATE_LEFT);
			navOptionsPanel.addTab(null, textIcon2, (Component) thumbnails);
		}

		displayPane.setDividerLocation(150);

	}


	class FormActionListener implements ActionListener{

		private Container c;
		private String formName;
		boolean showMessage;

		public FormActionListener(String formName, Container c,boolean showMessage) {

			this.c=c;
			this.formName=formName;
			this.showMessage=showMessage;

		}

		public void actionPerformed(ActionEvent arg0) {

			Object comp =arg0.getSource();
			Object value=null;
			if(comp instanceof JComboBox)
				value=((JComboBox)comp).getSelectedItem();
			else if(comp instanceof JCheckBox)
				value= String.valueOf(((JCheckBox) comp).isSelected());
			else if(comp instanceof JRadioButton)
				value= String.valueOf(((JRadioButton) comp).isSelected());
			else if(comp instanceof JTextField)
				value= ((JTextField) comp).getText();

			{
				String propValue = properties.getValue("showsaveformsmessage");
				boolean showSaveFormsMessage = false;

				if(propValue.length()>0)
					showSaveFormsMessage = propValue.equals("true");

				if(showSaveFormsMessage && firstTimeFormMessage && commonValues.isFormsChanged()==false){
					firstTimeFormMessage=false;

					JPanel panel =new JPanel();
					panel.setLayout(new GridBagLayout());
					final GridBagConstraints p = new GridBagConstraints();

					p.anchor=GridBagConstraints.WEST;
					p.gridx = 0;
					p.gridy = 0;
					String str=(Messages.getMessage("PdfViewerFormsWarning.ChangedFormsValue"));
					if(!commonValues.isItextOnClasspath())
						str=(Messages.getMessage("PdfViewerFormsWarning.ChangedFormsValueNoItext"));

					JCheckBox cb=new JCheckBox();
					cb.setText(Messages.getMessage("PdfViewerFormsWarning.CheckBox"));
					Font font = cb.getFont();

					JTextArea ta=new JTextArea(str);
					ta.setOpaque(false);
					ta.setFont(font);

					p.ipady=20;
					panel.add(ta, p);

					p.ipady=0;
					p.gridy = 1;
					panel.add(cb,p);

						JOptionPane.showMessageDialog(c,panel);


					if(cb.isSelected())
						properties.setValue("showsaveformsmessage","false");

				}
			}
			commonValues.setFormsChanged(true);
			setViewerTitle(null);

			if(showMessage)
				JOptionPane.showMessageDialog(c,"FormName >>"+formName+"<<. Value changed to "+value);

		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setCoordText(java.lang.String)
	 */
	public void setCoordText(String string) {
		coords.setText(string);
	}

	private JLabel initCoordBox() {

		coords.setBackground(Color.white);
		coords.setOpaque(true);
		coords.setBorder(BorderFactory.createLineBorder(Color.black,1));
		coords.setText("  X: "+ " Y: " + ' ' + ' ');
		coords.setPreferredSize(new Dimension(120,20));
		return coords;

	}

	//When page changes make sure only relevant navigation buttons are displayed
	public void hideRedundentNavButtons(){

		int maxPages = decode_pdf.getPageCount();
		if(commonValues.isMultiTiff()){
			maxPages = commonValues.getPageCount();
		}

		if(commonValues.getCurrentPage()==1)
			setBackNavigationButtonsEnabled(false);
		else
			setBackNavigationButtonsEnabled(true);

		if(commonValues.getCurrentPage()==maxPages)
			setForwardNavigationButtonsEnabled(false);
		else
			setForwardNavigationButtonsEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setPageNumber()
	 */
	public void setPageNumber() {
		pageCounter2.setForeground(Color.black);
		pageCounter2.setText(" " + commonValues.getCurrentPage());
		pageCounter3.setText(Messages.getMessage("PdfViewerOfLabel.text") + ' ' + commonValues.getPageCount()); //$NON-NLS-1$
		hideRedundentNavButtons();
	}
	
	public int getPageNumber(){
		return commonValues.getCurrentPage();
	}

	private void createNavbar() {
		Vector v = new Vector();

		Timer memoryMonitor = new Timer(500, new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				int free = (int) (Runtime.getRuntime().freeMemory() / (1024 * 1024));
				int total = (int) (Runtime.getRuntime().totalMemory() / (1024 * 1024));

				//this broke the image saving when it was run every time
				if(finishedDecoding){
					finishedDecoding=false;
				}

				//System.out.println((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000);
				memoryBar.setMaximum(total);
				memoryBar.setValue(total-free);
				memoryBar.setStringPainted(true);
				memoryBar.setString((total-free)+"M of "+total+ 'M');
			}
		});
		memoryMonitor.start();

		navButtons.add(memoryBar,BorderLayout.WEST);

		navButtons.add(Box.createHorizontalGlue());

		pageCounter1 = new JLabel(Messages.getMessage("PdfViewerPageLabel.text"));
		pageCounter1.setOpaque(false);

		navToolBar.add(Box.createHorizontalGlue());
		/**
		 * navigation toolbar for moving between pages
		 */
		addButton(NAVBAR,Messages.getMessage("PdfViewerNavBar.RewindToStart"),"/org/jpedal/examples/simpleviewer/res/start.gif",Commands.FIRSTPAGE);


		addButton(NAVBAR,Messages.getMessage("PdfViewerNavBar.Rewind10"),"/org/jpedal/examples/simpleviewer/res/fback.gif",Commands.FBACKPAGE);


		addButton(NAVBAR,Messages.getMessage("PdfViewerNavBar.Rewind1"),"/org/jpedal/examples/simpleviewer/res/back.gif",Commands.BACKPAGE);

		/**put page count in middle of forward and back*/
		navToolBar.add(pageCounter1);
		pageCounter2.setMaximumSize(new Dimension(5,50));
		navToolBar.add(pageCounter2);
		navToolBar.add(pageCounter3);

		addButton(NAVBAR,Messages.getMessage("PdfViewerNavBar.Forward1"),"/org/jpedal/examples/simpleviewer/res/forward.gif",Commands.FORWARDPAGE);

		addButton(NAVBAR,Messages.getMessage("PdfViewerNavBar.Forward10"),"/org/jpedal/examples/simpleviewer/res/fforward.gif",Commands.FFORWARDPAGE);

		addButton(NAVBAR,Messages.getMessage("PdfViewerNavBar.ForwardLast"),"/org/jpedal/examples/simpleviewer/res/end.gif",Commands.LASTPAGE);


		navToolBar.add(Box.createHorizontalGlue());


		boolean runningGPL = false;

		Dimension size;
		if (runningGPL) {
			size = new Dimension(110, 0);
			Filler filler = new Box.Filler(size, size, size);
			navButtons.add(filler, BorderLayout.EAST);
		} else {
			navButtons.add(pagesToolBar, BorderLayout.EAST);
			size = pagesToolBar.getPreferredSize();
		}
		memoryBar.setPreferredSize(size);

		boolean[] defaultValues = new boolean[v.size()];
		for(int i=0; i!=v.size(); i++){
			if(v.get(i).equals(Boolean.TRUE)){
				defaultValues[i] = true;
			}else{
				defaultValues[i] = false;
			}
		}

//		p.setNavDefaults(defaultValues);

		navButtons.add(navToolBar,BorderLayout.CENTER);

	}

	public void setPage(int page){
		commonValues.setCurrentPage(page);
		pageCounter2.setText(String.valueOf(page));
		//Page changed so save this page as last viewed
		setThumbnails();
		hideRedundentNavButtons();
	}

	public void resetPageNav() {
		pageCounter2.setText("");
		pageCounter3.setText("");
	}

	public void setRotation(){
		PdfPageData currentPageData=decode_pdf.getPdfPageData();
		//rotation=currentPageData.getRotation(commonValues.getCurrentPage());

		//Broke files with when moving from rotated page to non rotated.
		//The pages help previous rotation
		//rotation = (rotation + (getSelectedComboIndex(Commands.ROTATION)*90));

		if(rotation > 360)
			rotation = rotation - 360;

		if(getSelectedComboIndex(Commands.ROTATION)!=(rotation/90)){
			setSelectedComboIndex(Commands.ROTATION, (rotation/90));
		}else if(!commonValues.isProcessing()){
			decode_pdf.repaint();
		}
	}

	public void setRotationFromExternal(int rot){
		rotation = rot;
		rotationBox.setSelectedIndex(rotation/90);
		if(!commonValues.isProcessing()){
			decode_pdf.repaint();
		}
	}

	public void setScalingFromExternal(String scale){
		scaling = Float.parseFloat(scale);;
		scalingBox.setSelectedItem(scale);
		if(!commonValues.isProcessing()){
			decode_pdf.repaint();
		}
	}

	public void createMainMenu(boolean includeAll){

        //<start-wrap>

		fileMenu = new JMenu(Messages.getMessage("PdfViewerFileMenu.text"));

		addToMainMenu(fileMenu);

		/**
		 * add open options
		 **/

		openMenu = new JMenu(Messages.getMessage("PdfViewerFileMenuOpen.text"));
		fileMenu.add(openMenu);

		addMenuItem(openMenu,Messages.getMessage("PdfViewerFileMenuOpen.text"),Messages.getMessage("PdfViewerFileMenuTooltip.open"),Commands.OPENFILE);

		addMenuItem(openMenu,Messages.getMessage("PdfViewerFileMenuOpenurl.text"),Messages.getMessage("PdfViewerFileMenuTooltip.openurl"),Commands.OPENURL);




		fileMenu.addSeparator();
		addMenuItem(fileMenu,Messages.getMessage("PdfViewerFileMenuSave.text"),
				Messages.getMessage("PdfViewerFileMenuTooltip.save"),Commands.SAVE);

        //not set if I just run from jar as no IText....
		if(includeAll && commonValues.isItextOnClasspath())
			addMenuItem(fileMenu,
					Messages.getMessage("PdfViewerFileMenuResaveForms.text"),
					Messages.getMessage("PdfViewerFileMenuTooltip.saveForms"),
					Commands.SAVEFORM);


		// Remember to finish this off
		addMenuItem(fileMenu, Messages.getMessage("PdfViewerFileMenuFind.text"), Messages.getMessage("PdfViewerFileMenuTooltip.find"), Commands.FIND);

		// =====================



		fileMenu.addSeparator();
		addMenuItem(fileMenu,Messages.getMessage("PdfViewerFileMenuDocProperties.text"),
				Messages.getMessage("PdfViewerFileMenuTooltip.props"),Commands.DOCINFO);


		fileMenu.addSeparator();
		addMenuItem(fileMenu,Messages.getMessage("PdfViewerFileMenuPrint.text"),
				Messages.getMessage("PdfViewerFileMenuTooltip.print"),Commands.PRINT);

		if(properties.getValue("Recentdocuments").equals("true")){
			fileMenu.addSeparator();
			currentCommands.recentDocumentsOption(fileMenu);
		}

		fileMenu.addSeparator();
		addMenuItem(fileMenu,Messages.getMessage("PdfViewerFileMenuExit.text"),
				Messages.getMessage("PdfViewerFileMenuTooltip.exit"),Commands.EXIT);




		//EDIT MENU
		editMenu = new JMenu(Messages.getMessage("PdfViewerEditMenu.text"));
		addToMainMenu(editMenu);



		addMenuItem(editMenu,Messages.getMessage("PdfViewerEditMenuCopy.text"),
				Messages.getMessage("PdfViewerEditMenuTooltip.Copy"),Commands.COPY);

		addMenuItem(editMenu,Messages.getMessage("PdfViewerEditMenuSelectall.text"),
				Messages.getMessage("PdfViewerEditMenuTooltip.Selectall"),Commands.SELECTALL);

		addMenuItem(editMenu,Messages.getMessage("PdfViewerEditMenuDeselectall.text"),
				Messages.getMessage("PdfViewerEditMenuTooltip.Deselectall"),Commands.DESELECTALL);


		editMenu.addSeparator();
		addMenuItem(editMenu, Messages.getMessage("PdfViewerEditMenuPreferences.text"),
				Messages.getMessage("PdfViewerEditMenuTooltip.Preferences"), Commands.PREFERENCES);




		viewMenu = new JMenu(Messages.getMessage("PdfViewerViewMenu.text"));
		addToMainMenu(viewMenu);

		goToMenu = new JMenu(Messages.getMessage("GoToViewMenuGoto.text"));
		viewMenu.add(goToMenu);

		addMenuItem(goToMenu,Messages.getMessage("GoToViewMenuGoto.FirstPage"),"",Commands.FIRSTPAGE);

		addMenuItem(goToMenu,Messages.getMessage("GoToViewMenuGoto.BackPage"),"",Commands.BACKPAGE);

		addMenuItem(goToMenu,Messages.getMessage("GoToViewMenuGoto.ForwardPage"),"",Commands.FORWARDPAGE);

		addMenuItem(goToMenu,Messages.getMessage("GoToViewMenuGoto.LastPage"),"",Commands.LASTPAGE);

		addMenuItem(goToMenu,Messages.getMessage("GoToViewMenuGoto.GoTo"),"",Commands.GOTO);


		goToMenu.addSeparator();


		addMenuItem(goToMenu,Messages.getMessage("GoToViewMenuGoto.PreviousDoucment"),"",Commands.PREVIOUSDOCUMENT);

		addMenuItem(goToMenu,Messages.getMessage("GoToViewMenuGoto.NextDoucment"),"",Commands.NEXTDOCUMENT);


		/**
		 * add page layout
		 **/
		//if(properties.getValue("PageLayoutMenu").toLowerCase().equals("true")){


		pageLayoutMenu = new JMenu(Messages.getMessage("PageLayoutViewMenu.PageLayout"));
		viewMenu.add(pageLayoutMenu);

        //<end-wrap>
        
		String[] descriptions={Messages.getMessage("PageLayoutViewMenu.SinglePage"),Messages.getMessage("PageLayoutViewMenu.Continuous"),Messages.getMessage("PageLayoutViewMenu.Facing"),Messages.getMessage("PageLayoutViewMenu.ContinousFacing"),Messages.getMessage("PageLayoutViewMenu.SideScroll")};
		int[] value={Display.SINGLE_PAGE, Display.CONTINUOUS,Display.FACING,Display.CONTINUOUS_FACING, Display.SIDE_SCROLL};

		if( isSingle())
			initLayoutMenus(pageLayoutMenu, descriptions, value);

        //<start-wrap>

		// addMenuItem(view,Messages.getMessage("PdfViewerViewMenuAutoscroll.text"),Messages.getMessage("PdfViewerViewMenuTooltip.autoscroll"),Commands.AUTOSCROLL);
		
		if(properties.getValue("Fullscreen").equals("true")){
			//put line underneath
			viewMenu.addSeparator();

			//full page mode
			addMenuItem(viewMenu,Messages.getMessage("PdfViewerViewMenuFullScreenMode.text"),Messages.getMessage("PdfViewerViewMenuTooltip.fullScreenMode"),Commands.FULLSCREEN);
		}



		if (! isSingle()) {
			windowMenu = new JMenu(Messages.getMessage("PdfViewerWindowMenu.text"));
			addToMainMenu(windowMenu);

			addMenuItem(windowMenu, Messages.getMessage("PdfViewerWindowMenuCascade.text"), "",	Commands.CASCADE);

			addMenuItem(windowMenu, Messages.getMessage("PdfViewerWindowMenuTile.text"), "", Commands.TILE);

		}

		/**
		 * add export menus
		 **/
		if(commonValues.isItextOnClasspath()){
			exportMenu = new JMenu(Messages.getMessage("PdfViewerExportMenu.text"));
			addToMainMenu(exportMenu);

			//<link><a name="newmenu" />
			/**
			 * external/itext menu option example adding new option to Export menu
			 */
			// addMenuItem(export,"NEW",tooltip,Commands.NEWFUNCTION);
			/**
			 * external/itext menu option example adding new option to Export menu
			 * Tooltip text can be externalised in Messages.getMessage("PdfViewerTooltip.NEWFUNCTION")
			 * and text added into files in res package
			 */


			pdfMenu = new JMenu(Messages.getMessage("PdfViewerExportMenuPDF.text"));
			exportMenu.add(pdfMenu);

			addMenuItem(pdfMenu,Messages.getMessage("PdfViewerExportMenuOnePerPage.text"),"",Commands.PDF);

			addMenuItem(pdfMenu,Messages.getMessage("PdfViewerExportMenuNUp.text"),"",Commands.NUP);

			addMenuItem(pdfMenu,Messages.getMessage("PdfViewerExportMenuHandouts.text"),"",Commands.HANDOUTS);



			contentMenu=new JMenu(Messages.getMessage("PdfViewerExportMenuContent.text"));
			exportMenu.add(contentMenu);

			addMenuItem(contentMenu,Messages.getMessage("PdfViewerExportMenuImages.text"),"",Commands.IMAGES);

			addMenuItem(contentMenu,Messages.getMessage("PdfViewerExportMenuText.text"),"",Commands.TEXT);


			addMenuItem(exportMenu,"Bitmap",Messages.getMessage("PdfViewerExportMenuBitmap.text"),Commands.BITMAP);
		}

		if(commonValues.isItextOnClasspath()){
			pageToolsMenu = new JMenu(Messages.getMessage("PdfViewerPageToolsMenu.text"));
			addToMainMenu(pageToolsMenu);

			addMenuItem(pageToolsMenu,Messages.getMessage("PdfViewerPageToolsMenuRotate.text"),"",Commands.ROTATE);
			addMenuItem(pageToolsMenu,Messages.getMessage("PdfViewerPageToolsMenuDelete.text"),"",Commands.DELETE);
			addMenuItem(pageToolsMenu,Messages.getMessage("PdfViewerPageToolsMenuAddPage.text"),"",Commands.ADD);
			addMenuItem(pageToolsMenu,Messages.getMessage("PdfViewerPageToolsMenuAddHeaderFooter.text"),"",Commands.ADDHEADERFOOTER);
			addMenuItem(pageToolsMenu,Messages.getMessage("PdfViewerPageToolsMenuStampText.text"),"",Commands.STAMPTEXT);
			addMenuItem(pageToolsMenu,Messages.getMessage("PdfViewerPageToolsMenuStampImage.text"),"",Commands.STAMPIMAGE);
			addMenuItem(pageToolsMenu,Messages.getMessage("PdfViewerPageToolsMenuSetCrop.text"),"",Commands.SETCROP);

		}


		helpMenu = new JMenu(Messages.getMessage("PdfViewerHelpMenu.text"));
		addToMainMenu(helpMenu);

		addMenuItem(helpMenu,Messages.getMessage("PdfViewerHelpMenu.VisitWebsite"),"",Commands.VISITWEBSITE);
		addMenuItem(helpMenu,Messages.getMessage("PdfViewerHelpMenuTip.text"),"",Commands.TIP);
		addMenuItem(helpMenu,Messages.getMessage("PdfViewerHelpMenuUpdates.text"),"",Commands.UPDATE);
		addMenuItem(helpMenu,Messages.getMessage("PdfViewerHelpMenuabout.text"),Messages.getMessage("PdfViewerHelpMenuTooltip.about"),Commands.INFO);


        //<end-wrap>

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#addToMainMenu(javax.swing.JMenu)
	 */
	public void addToMainMenu(JMenu fileMenuList) {
		currentMenu.add(fileMenuList);
	}


	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getFrame()
	 */
	public Container getFrame() {
		return frame;
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#getTopButtonBar()
	 */
	public JToolBar getTopButtonBar() {
		return topButtons;
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showMessageDialog(java.lang.Object)
	 */
	public void showMessageDialog(Object message1){
		JOptionPane.showMessageDialog(frame,message1);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showMessageDialog(java.lang.Object, java.lang.String, int)
	 */
	public void showMessageDialog(Object message,String title,int type){
		JOptionPane.showMessageDialog(frame,message,title,type);
	}


	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showInputDialog(java.lang.Object, java.lang.String, int)
	 */
	public String showInputDialog(Object message, String title, int type) {
		return JOptionPane.showInputDialog(frame, message, title, type);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showInputDialog(java.lang.String)
	 */
	public String showInputDialog(String message) {

		return 	JOptionPane.showInputDialog(frame,message);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showOptionDialog(java.lang.Object, java.lang.String, int, int, java.lang.Object, java.lang.Object[], java.lang.Object)
	 */
	public int showOptionDialog(Object displayValue, String message, int option, int type, Object icon, Object[] options, Object initial) {

		return JOptionPane.showOptionDialog(frame, displayValue,message,option,type, (Icon)icon, options,initial);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showConfirmDialog(java.lang.String, java.lang.String, int)
	 */
	public int showConfirmDialog(String message, String message2, int option) {

		return JOptionPane.showConfirmDialog(frame, message,message2,option);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showOverwriteDialog(String file,boolean yesToAllPresent)
	 */
	public int showOverwriteDialog(String file,boolean yesToAllPresent) {

		int n = -1;

		if(yesToAllPresent){

			final Object[] buttonRowObjects = new Object[] {
					Messages.getMessage("PdfViewerConfirmButton.Yes"),
					Messages.getMessage("PdfViewerConfirmButton.YesToAll"),
					Messages.getMessage("PdfViewerConfirmButton.No"),
					Messages.getMessage("PdfViewerConfirmButton.Cancel")
			};

			n = JOptionPane.showOptionDialog(frame,
					file+ '\n' +Messages.getMessage("PdfViewerMessage.FileAlreadyExists")
					+ '\n' +Messages.getMessage("PdfViewerMessage.ConfirmResave"),
					Messages.getMessage("PdfViewerMessage.Overwrite"),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					buttonRowObjects,
					buttonRowObjects[0]);

		}else{
			n = JOptionPane.showOptionDialog(frame,
					file+ '\n' +Messages.getMessage("PdfViewerMessage.FileAlreadyExists")
					+ '\n' +Messages.getMessage("PdfViewerMessage.ConfirmResave"),
					Messages.getMessage("PdfViewerMessage.Overwrite"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,null,null);
		}

		return n;
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showMessageDialog(javax.swing.JTextArea)
	 */
	public void showMessageDialog(JTextArea info) {
		JOptionPane.showMessageDialog(frame, info);

	}

	public void showItextPopup() {

		JEditorPane p = new JEditorPane(
				"text/html",
				"Itext is not on the classpath.<BR>"
				+ "JPedal includes code to take advantage of itext and<BR>"
				+ "provide additional functionality with options<BR>"
				+ "to spilt pdf files, and resave forms data<BR>"
				+ "\nItext website - <a href=http://www.lowagie.com/iText/>http://www.lowagie.com/iText/</a>");
		p.setEditable(false);
		p.setOpaque(false);
		p.addHyperlinkListener( new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					try {
						BrowserLauncher.openURL("http://www.lowagie.com/iText/");
					} catch (IOException e1) {
						showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
					}
				}
			}
		});

		showMessageDialog(p);

		// Hack for 13 to make sure the message box is large enough to hold the message
		/**
        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(p);
        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        optionPane.setOptionType(JOptionPane.DEFAULT_OPTION);

        JDialog dialog = optionPane.createDialog(frame, "iText");
        dialog.pack();
        dialog.setSize(400,200);
        dialog.setVisible(true);
        /**/

	}

	public void showFirstTimePopup(){

		try{
			final JPanel a = new JPanel();
			a.setLayout(new BorderLayout());
			JLabel lab=new JLabel(new ImageIcon(getClass().getResource("/org/jpedal/objects/acroforms/ceo.jpg")));

			//lab.setBorder(BorderFactory.createRaisedBevelBorder());
			a.add(lab,BorderLayout.NORTH);
			final JLabel message=new JLabel("<html><center>"+Messages.getMessage("PdfViewerJpedalLibrary.Text")
					+Messages.getMessage("PdfViewer.WebAddress"));

			message.setHorizontalAlignment(JLabel.CENTER);
			message.setForeground(Color.blue);
			message.setFont(new Font("Lucida",Font.PLAIN,16));

			message.addMouseListener(new MouseListener() {
				public void mouseEntered(MouseEvent e) {
					a.setCursor(new Cursor(Cursor.HAND_CURSOR));


					message.setText("<html><center>"+Messages.getMessage("PdfViewerJpedalLibrary.Link")+
							Messages.getMessage("PdfViewerJpedalLibrary.Text")+
							Messages.getMessage("PdfViewer.WebAddress")+"</a></center>");

				}

				public void mouseExited(MouseEvent e) {
					a.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					message.setText("<html><center>"+Messages.getMessage("PdfViewerJpedalLibrary.Text")
							+Messages.getMessage("PdfViewer.WebAddress"));
				}

				public void mouseClicked(MouseEvent e) {
					try {
						BrowserLauncher.openURL(Messages.getMessage("PdfViewer.VisitWebsite"));
					} catch (IOException e1) {
						showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
					}
				}

				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
			});


			a.add(message,BorderLayout.CENTER);

			a.setPreferredSize(new Dimension(300,240));
			Object[] options = { Messages.getMessage("PdfViewerButton.RunSoftware") };
			int n =
				JOptionPane.showOptionDialog(
						frame,
						a,
						Messages.getMessage("PdfViewerTitle.RunningFirstTime"),
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.PLAIN_MESSAGE,
						null,
						options,
						options[0]);
		}catch(Exception e){
			//JOptionPane.showMessageDialog(null, "caught an exception "+e);
			System.err.println(Messages.getMessage("PdfViewerFirstRunDialog.Error"));
		}catch(Error e){
			//JOptionPane.showMessageDialog(null, "caught an error "+e);
			System.err.println(Messages.getMessage("PdfViewerFirstRunDialog.Error"));
		}
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#showConfirmDialog(java.lang.Object, java.lang.String, int, int)
	 */
	public int showConfirmDialog(Object message, String title, int optionType, int messageType) {
		return JOptionPane.showConfirmDialog(frame, message, title, optionType, messageType);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#updateStatusMessage(java.lang.String)
	 */
	public void updateStatusMessage(String message) {
		statusBar.updateStatus(message,0);


	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#resetStatusMessage(java.lang.String)
	 */
	public void resetStatusMessage(String message) {
		statusBar.resetStatus(message);

	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setStatusProgress(int)
	 */
	public void setStatusProgress(int size) {
		statusBar.setProgress(size);
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#isPDFOutlineVisible()
	 */
	public boolean isPDFOutlineVisible() {
		return navOptionsPanel.isVisible();
	}

	/* (non-Javadoc)
	 * @see org.jpedal.examples.simpleviewer.gui.swing.GUIFactory#setPDFOutlineVisible(boolean)
	 */
	public void setPDFOutlineVisible(boolean visible) {
		navOptionsPanel.setVisible(visible);
	}

	public void setSplitDividerLocation(int size) {
		displayPane.setDividerLocation(size);
	}


	public void setQualityBoxVisible(boolean visible){
	}

	private void setThumbnails() {
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				if(thumbnails.isShownOnscreen()) {
					setupThumbnailPanel();

					if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
						thumbnails.generateOtherVisibleThumbnails(commonValues.getCurrentPage());
				}

				return null;
			}
		};
		worker.start();
	}

	public void setSearchText(JTextField searchText) {
		this.searchText = searchText;
	}

	public void setResults(SearchList results) {
		this.results = results;
	}

	public SearchList getResults() {
		return results;
	}

	public JToolBar getComboBar() {
		return navButtons;
	}

	public ButtonGroup getSearchLayoutGroup() {
		return searchLayoutGroup;
	}

	public void setSearchFrame(GUISearchWindow searchFrame) {
		this.searchFrame = searchFrame;
	}

//	<link><a name="exampledraw" />
	/**
	 * example of a custom draw object
	 */
	private static class ExampleCustomDrawObject implements JPedalCustomDrawObject {

		private boolean isVisible=true;

		private int page = 0;

		public int medX = 0;
		public int medY = 0;


		public ExampleCustomDrawObject(){

		}

		public ExampleCustomDrawObject(Integer option){

			if(option.equals(JPedalCustomDrawObject.ALLPAGES))
				page=-1;
			else throw new RuntimeException("Only valid setting is JPedalCustomDrawObject.ALLPAGES");
		}

		public int getPage(){
			return page;
		}


		public void print(Graphics2D g2, int x) {

			//custom code or just pass through
			if(page==x || page ==-1 || page==0)
				paint(g2);
		}

		public void paint(Graphics2D g2) {
			if(isVisible){

				//your code here

				//if you alter something, put it back
				Paint paint=g2.getPaint();

				//loud shape we can see
				g2.setPaint(Color.orange);
				g2.fillRect(100+medX,100+medY,100,100); // PDF co-ordinates due to transform

				g2.setPaint(Color.RED);
				g2.drawRect(100+medX,100+medY,100,100); // PDF co-ordinates due to transform

				//put back values
				g2.setPaint(paint);
			}
		}

        /**example onto rotated page
        public void paint(Graphics2D g2) {
                if(isVisible){

                    //your code here

                    AffineTransform aff=g2.getTransform();


                    //allow for 90 degrees - detect of G2
                    double[] matrix=new double[6];
                    aff.getMatrix(matrix);

                    //System.out.println("0="+matrix[0]+" 1="+matrix[1]+" 2="+matrix[2]+" 3="+matrix[3]+" 4="+matrix[4]+" 5="+matrix[5]);
                    if(matrix[1]>0 && matrix[2]>0){ //90

                        g2.transform(AffineTransform.getScaleInstance(-1, 1));
                        g2.transform(AffineTransform.getRotateInstance(90 *Math.PI/180));

                        //BOTH X and Y POSITIVE!!!!
                    g2.drawString("hello world", 60,60);
                    }else if(matrix[0]<0 && matrix[3]>0){ //180 degrees  (origin now top right)
                        g2.transform(AffineTransform.getScaleInstance(-1, 1));

                        g2.drawString("hello world", -560,60);//subtract cropW from first number to use standard values

                    }else if(matrix[1]<0 && matrix[2]<0){ //270

                        g2.transform(AffineTransform.getScaleInstance(-1, 1));
                        g2.transform(AffineTransform.getRotateInstance(-90 *Math.PI/180));

                        //BOTH X and Y NEGATIVE!!!!
                        g2.drawString("hello world", -560,-60); //subtract CropW and CropH if you want standard values
                    }else{ //0 degress
                        g2.transform(AffineTransform.getScaleInstance(1, -1));
                        // X ONLY POSITIVE!!!!
                        g2.drawString("hello world", 60,-60);
                    }

                    //restore!!!
                    g2.setTransform(aff);
                }
            }
        /**/


		public void setVisible(boolean isVisible) {
			this.isVisible=isVisible;
		}

		public void setMedX(int medX) {
			this.medX = medX;
		}

		public void setMedY(int medY) {
			this.medY = medY;
		}
	}

	public void removeSearchWindow(boolean justHide) {
		searchFrame.removeSearchWindow(justHide);
	}

	public void showPreferencesDialog() {
//		JFrame frame = new JFrame("Preferences");
//		frame.getContentPane().setLayout(new BorderLayout());
//		frame.getContentPane().add("Center", p);
//		frame.pack();
//		frame.setLocation(100, 100);
//		frame.setVisible(true);

		SwingProperties p = new SwingProperties();
		p.setParent(frame);

		//p.setHideGuiPartsDefaults(defaultValues);

		p.showPreferenceWindow(this);

	}

	public void setFrame(Container frame) {
		this.frame = frame;

	}

	public void getRSSBox() {
		final JPanel panel = new JPanel();

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel("Click on the link below to load a web browser and sign up to our RSS feed.");
		label.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		labelPanel.add(label);
		labelPanel.add(Box.createHorizontalGlue());

		top.add(labelPanel);

		JPanel linkPanel = new JPanel();
		linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.X_AXIS));
		linkPanel.add(Box.createHorizontalGlue());
		linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel url=new JLabel("<html><center>"+"http://www.jpedal.org/jpedal.rss");
		url.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		url.setForeground(Color.blue);
		url.setHorizontalAlignment(JLabel.CENTER);

		//@kieran - cursor
		url.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.HAND_CURSOR));
				url.setText("<html><center><a>http://www.jpedal.org/jpedal.rss</a></center>");
			}

			public void mouseExited(MouseEvent e) {
				panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				url.setText("<html><center>http://www.jpedal.org/jpedal.rss");
			}

			public void mouseClicked(MouseEvent e) {
				try {
					BrowserLauncher.openURL("http://www.jpedal.org/jpedal.rss");
				} catch (IOException e1) {

					JPanel errorPanel = new JPanel();
					errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));

					JLabel errorMessage = new JLabel("Your web browser could not be successfully loaded.  " +
					"Please copy and paste the URL below, manually into your web browser.");
					errorMessage.setAlignmentX(JLabel.LEFT_ALIGNMENT);
					errorMessage.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

					JTextArea textArea = new JTextArea("http://www.jpedal.org/jpedal.rss");
					textArea.setEditable(false);
					textArea.setRows(5);
					textArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
					textArea.setAlignmentX(JTextArea.LEFT_ALIGNMENT);

					errorPanel.add(errorMessage);
					errorPanel.add(textArea);

					showMessageDialog(errorPanel,"Error loading web browser",JOptionPane.PLAIN_MESSAGE);

				}
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});

		linkPanel.add(url);
		linkPanel.add(Box.createHorizontalGlue());
		top.add(linkPanel);

		JLabel image = new JLabel(new ImageIcon(getClass().getResource("/org/jpedal/examples/simpleviewer/res/rss.png")));
		image.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		JPanel imagePanel = new JPanel();
		imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
		imagePanel.add(Box.createHorizontalGlue());
		imagePanel.add(image);
		imagePanel.add(Box.createHorizontalGlue());

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(top);
		panel.add(imagePanel);

		showMessageDialog(panel,"Subscribe to JPedal RSS Feed",JOptionPane.PLAIN_MESSAGE);
	}

	private void loadProperties(){
		Component[] c = comboBoxBar.getComponents();

//		default value used to load props
		boolean set = false;
		String propValue = "";

		//Disable entire section
		propValue = properties.getValue("ShowMenubar");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			currentMenu.setEnabled(set);
			currentMenu.setVisible(set);
			properties.setValue("ShowMenubar", String.valueOf(set));

			propValue = properties.getValue("ShowButtons");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			topButtons.setEnabled(set);
			topButtons.setVisible(set);
			properties.setValue("ShowButtons", String.valueOf(set));

			propValue = properties.getValue("ShowDisplayoptions");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			comboBoxBar.setEnabled(set);
			comboBoxBar.setVisible(set);
			properties.setValue("ShowDisplayoptions", String.valueOf(set));

			propValue = properties.getValue("ShowNavigationbar");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			navButtons.setEnabled(set);
			navButtons.setVisible(set);
			properties.setValue("ShowNavigationbar", String.valueOf(set));

			if(displayPane!=null){
				propValue = properties.getValue("ShowSidetabbar");
				set = propValue.length()>0 && propValue.toLowerCase().equals("true");
				if(!set)
					displayPane.setDividerSize(0);
				else
					displayPane.setDividerSize(5);
				displayPane.getLeftComponent().setEnabled(set);
				displayPane.getLeftComponent().setVisible(set);
				properties.setValue("ShowSidetabbar", String.valueOf(set));
			}

		/**
		 * Items on nav pane
		 */
			propValue = properties.getValue("Firstbottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			first.setEnabled(set);
			first.setVisible(set);

			propValue = properties.getValue("Back10bottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			fback.setEnabled(set);
			fback.setVisible(set);

			propValue = properties.getValue("Backbottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			back.setEnabled(set);
			back.setVisible(set);

			propValue = properties.getValue("Gotobottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			pageCounter1.setEnabled(set);
			pageCounter1.setVisible(set);

			pageCounter2.setEnabled(set);
			pageCounter2.setVisible(set);

			pageCounter3.setEnabled(set);
			pageCounter3.setVisible(set);

			propValue = properties.getValue("Forwardbottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			forward.setEnabled(set);
			forward.setVisible(set);

			propValue = properties.getValue("Forward10bottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			fforward.setEnabled(set);
			fforward.setVisible(set);

			propValue = properties.getValue("Lastbottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			end.setEnabled(set);
			end.setVisible(set);

			propValue = properties.getValue("Singlebottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			singleButton.setEnabled(set);
			singleButton.setVisible(set);

			propValue = properties.getValue("Continuousbottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			continuousButton.setEnabled(set);
			continuousButton.setVisible(set);

			propValue = properties.getValue("Continuousfacingbottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			continuousFacingButton.setEnabled(set);
			continuousFacingButton.setVisible(set);

			propValue = properties.getValue("Facingbottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			facingButton.setEnabled(set);
			facingButton.setVisible(set);

			if(JAIHelper.isJAIused()){
				propValue = properties.getValue("SideScrollbottom");
				set = propValue.length()>0 && propValue.toLowerCase().equals("true");
				sideScrollButton.setEnabled(set);
				sideScrollButton.setVisible(set);
			}
			
        	propValue = properties.getValue("Memorybottom");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			memoryBar.setEnabled(set);
			memoryBar.setVisible(set);



        /**
		 * Items on option pane
		 */
			propValue = properties.getValue("Scalingdisplay");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			for(int i=0; i!=c.length; i++){
				if(c[i] instanceof JLabel){
					if(((JLabel)c[i]).getText().equals(Messages.getMessage("PdfViewerToolbarScaling.text"))){
						c[i].setEnabled(set);
						scalingBox.setEnabled(set);
						c[i].setVisible(set);
						scalingBox.setVisible(set);
						properties.setValue("Scalingdisplay", String.valueOf(set));
					}
				}
			}

			propValue = properties.getValue("Rotationdisplay");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			for(int i=0; i!=c.length; i++){
				if(c[i] instanceof JLabel){
					if(((JLabel)c[i]).getText().equals(Messages.getMessage("PdfViewerToolbarRotation.text"))){
						c[i].setEnabled(set);
						rotationBox.setEnabled(set);
						c[i].setVisible(set);
						rotationBox.setVisible(set);
						properties.setValue("Rotationdisplay", String.valueOf(set));
					}
				}
			}

			propValue = properties.getValue("Imageopdisplay");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			for(int i=0; i!=c.length; i++){
				if(c[i] instanceof JLabel){
					if(((JLabel)c[i]).getText().equals(Messages.getMessage("PdfViewerToolbarImageOp.text"))){
						c[i].setVisible(set);
						qualityBox.setVisible(set);
						c[i].setEnabled(set);
						qualityBox.setEnabled(set);
						properties.setValue("Imageopdisplay", String.valueOf(set));
					}
				}
			}

			propValue = properties.getValue("Progressdisplay");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			statusBar.setEnabled(set);
        //<start-wrap>
			statusBar.setVisible(set);
        //<end-wrap>
			properties.setValue("Progressdisplay", String.valueOf(set));

		/**
		 * Items on button bar
		 */
			propValue = properties.getValue("Openfilebutton");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			openButton.setEnabled(set);
			openButton.setVisible(set);

			propValue = properties.getValue("Printbutton");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			printButton.setEnabled(set);
			printButton.setVisible(set);

			propValue = properties.getValue("Searchbutton");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			searchButton.setEnabled(set);
			searchButton.setVisible(set);

			propValue = properties.getValue("Propertiesbutton");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			docPropButton.setEnabled(set);
			docPropButton.setVisible(set);

			propValue = properties.getValue("Aboutbutton");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			infoButton.setEnabled(set);
			infoButton.setVisible(set);

			propValue = properties.getValue("Snapshotbutton");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			snapshotButton.setEnabled(set);
			snapshotButton.setVisible(set);

			propValue = properties.getValue("CursorButton");
		set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			cursor.setEnabled(set);
			cursor.setVisible(set);

		/**
		 * Items on signature tab
		 */
			propValue = properties.getValue("Pagetab");
		set = (properties.getValue("Pagetab").toLowerCase().equals("true") && navOptionsPanel.getTabCount()!=0);
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(pageTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}

			propValue = properties.getValue("Bookmarkstab");
		set = (properties.getValue("Bookmarkstab").toLowerCase().equals("true") && navOptionsPanel.getTabCount()!=0);
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(bookmarksTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}

			propValue = properties.getValue("Layerstab");
		set = (properties.getValue("Layerstab").toLowerCase().equals("true") && navOptionsPanel.getTabCount()!=0);
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(layersTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}


			propValue = properties.getValue("Signaturestab");
		set = (properties.getValue("Signaturestab").toLowerCase().equals("true") && navOptionsPanel.getTabCount()!=0);
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(signaturesTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}

        /**
		 * Items from the menu item
		 */
        if(fileMenu!=null){ //all of these will be null in 'Wrapper' mode so ignore

			propValue = properties.getValue("FileMenu");
		    set = propValue.length()>0 && propValue.toLowerCase().equals("true");
		    fileMenu.setEnabled(set);
		    fileMenu.setVisible(set);


            propValue = properties.getValue("OpenMenu");
            set = propValue.length()>0 && propValue.toLowerCase().equals("true");
            openMenu.setEnabled(set);
            openMenu.setVisible(set);

            propValue = properties.getValue("Open");
            set = propValue.length()>0 && propValue.toLowerCase().equals("true");
            open.setEnabled(set);
            open.setVisible(set);


		propValue = properties.getValue("Openurl");
        set = propValue.length()>0 && propValue.toLowerCase().equals("true");
        openUrl.setEnabled(set);
        openUrl.setVisible(set);

        propValue = properties.getValue("Save");
        set = propValue.length()>0 && propValue.toLowerCase().equals("true");
        save.setEnabled(set);
        save.setVisible(set);



        propValue = properties.getValue("Find");set = propValue.length()>0 && propValue.toLowerCase().equals("true");find.setEnabled(set); find.setVisible(set);

        propValue = properties.getValue("Documentproperties");set = propValue.length()>0 && propValue.toLowerCase().equals("true");documentProperties.setEnabled(set); documentProperties.setVisible(set);

        propValue = properties.getValue("Print");set = propValue.length()>0 && propValue.toLowerCase().equals("true");print.setEnabled(set); print.setVisible(set);

        propValue = properties.getValue("Recentdocuments"); set = propValue.length()>0 && propValue.toLowerCase().equals("true");currentCommands.enableRecentDocuments(set);

		propValue = properties.getValue("Exit");set = propValue.length()>0 && propValue.toLowerCase().equals("true");exit.setEnabled(set); exit.setVisible(set);


		propValue = properties.getValue("EditMenu");set = propValue.length()>0 && propValue.toLowerCase().equals("true");editMenu.setEnabled(set); editMenu.setVisible(set);

		propValue = properties.getValue("Copy");set = propValue.length()>0 && propValue.toLowerCase().equals("true");copy.setEnabled(set); copy.setVisible(set);

		propValue = properties.getValue("Selectall");set = propValue.length()>0 && propValue.toLowerCase().equals("true");selectAll.setEnabled(set); selectAll.setVisible(set);

		propValue = properties.getValue("Deselectall");set = propValue.length()>0 && propValue.toLowerCase().equals("true");deselectAll.setEnabled(set); deselectAll.setVisible(set);

		propValue = properties.getValue("Preferences");set = propValue.length()>0 && propValue.toLowerCase().equals("true");preferences.setEnabled(set); preferences.setVisible(set);


		propValue = properties.getValue("ViewMenu");set = propValue.length()>0 && propValue.toLowerCase().equals("true");viewMenu.setEnabled(set); viewMenu.setVisible(set);

		propValue = properties.getValue("GotoMenu");set = propValue.length()>0 && propValue.toLowerCase().equals("true");goToMenu.setEnabled(set); goToMenu.setVisible(set);

		propValue = properties.getValue("Firstpage");set = propValue.length()>0 && propValue.toLowerCase().equals("true");firstPage.setEnabled(set); firstPage.setVisible(set);

		propValue = properties.getValue("Backpage");set = propValue.length()>0 && propValue.toLowerCase().equals("true");backPage.setEnabled(set); backPage.setVisible(set);

		propValue = properties.getValue("Forwardpage");set = propValue.length()>0 && propValue.toLowerCase().equals("true");forwardPage.setEnabled(set); forwardPage.setVisible(set);

		propValue = properties.getValue("Lastpage");set = propValue.length()>0 && propValue.toLowerCase().equals("true");lastPage.setEnabled(set); lastPage.setVisible(set);

		propValue = properties.getValue("Goto");set = propValue.length()>0 && propValue.toLowerCase().equals("true");goTo.setEnabled(set); goTo.setVisible(set);

		propValue = properties.getValue("Previousdocument");set = propValue.length()>0 && propValue.toLowerCase().equals("true");previousDocument.setEnabled(set); previousDocument.setVisible(set);

		propValue = properties.getValue("Nextdocument");set = propValue.length()>0 && propValue.toLowerCase().equals("true");nextDocument.setEnabled(set); nextDocument.setVisible(set);


		propValue = properties.getValue("PagelayoutMenu");set = propValue.length()>0 && propValue.toLowerCase().equals("true");pageLayoutMenu.setEnabled(set); pageLayoutMenu.setVisible(set);

		if(single!=null){
			propValue = properties.getValue("Single");
			set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			single.setEnabled(set); 
			single.setVisible(set);
		}
		
		if(continuous!=null){
			propValue = properties.getValue("Continuous");
			set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			continuous.setEnabled(set); 
			continuous.setVisible(set);
		}
		
		if(facing!=null){
			propValue = properties.getValue("Facing");
			set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			facing.setEnabled(set); 
			facing.setVisible(set);
		}
		
		if(continuousFacing!=null){
			propValue = properties.getValue("Continuousfacing");
			set = propValue.length()>0 && propValue.toLowerCase().equals("true");
			continuousFacing.setEnabled(set); 
			continuousFacing.setVisible(set);
		}
		
        if(sideScroll!=null){

		    propValue = properties.getValue("SideScroll");
            set = propValue.length()>0 && propValue.toLowerCase().equals("true");
            sideScroll.setEnabled(set);
            sideScroll.setVisible(set);
       }

       if(fullscreen!=null){

		    propValue = properties.getValue("Fullscreen");
            set = propValue.length()>0 && propValue.toLowerCase().equals("true");
            fullscreen.setEnabled(set);
            fullscreen.setVisible(set);
        }

		if(windowMenu!=null){

			propValue = properties.getValue("WindowMenu");set = propValue.length()>0 && propValue.toLowerCase().equals("true");windowMenu.setEnabled(set); windowMenu.setVisible(set);

			propValue = properties.getValue("Cascade");set = propValue.length()>0 && propValue.toLowerCase().equals("true");cascade.setEnabled(set); cascade.setVisible(set);

			propValue = properties.getValue("Tile");set = propValue.length()>0 && propValue.toLowerCase().equals("true");tile.setEnabled(set); tile.setVisible(set);
		}

		if(commonValues.isItextOnClasspath()){

			propValue = properties.getValue("ContentMenu");set = propValue.length()>0 && propValue.toLowerCase().equals("true");contentMenu.setEnabled(set); contentMenu.setVisible(set);

			propValue = properties.getValue("Images");set = propValue.length()>0 && propValue.toLowerCase().equals("true");images.setEnabled(set); images.setVisible(set);

			propValue = properties.getValue("Text");set = propValue.length()>0 && propValue.toLowerCase().equals("true");text.setEnabled(set); text.setVisible(set);

			propValue = properties.getValue("Bitmap");set = propValue.length()>0 && propValue.toLowerCase().equals("true");bitmap.setEnabled(set); bitmap.setVisible(set);

		}

		propValue = properties.getValue("HelpMenu");set = propValue.length()>0 && propValue.toLowerCase().equals("true");helpMenu.setEnabled(set); helpMenu.setVisible(set);

		propValue = properties.getValue("Visitwebsite");set = propValue.length()>0 && propValue.toLowerCase().equals("true");visitWebsite.setEnabled(set); visitWebsite.setVisible(set);

		propValue = properties.getValue("Tipoftheday");set = propValue.length()>0 && propValue.toLowerCase().equals("true");tipOfTheDay.setEnabled(set); tipOfTheDay.setVisible(set);

		propValue = properties.getValue("Checkupdates");set = propValue.length()>0 && propValue.toLowerCase().equals("true");checkUpdates.setEnabled(set); checkUpdates.setVisible(set);

		propValue = properties.getValue("About");set = propValue.length()>0 && propValue.toLowerCase().equals("true");about.setEnabled(set); about.setVisible(set);


        }

	}

	public void alterProperty(String value, boolean set){
		Component[] c = comboBoxBar.getComponents();

		//Disable entire section
		if(value.equals("ShowMenubar")){
			currentMenu.setEnabled(set);
			currentMenu.setVisible(set);
			properties.setValue("ShowMenubar", String.valueOf(set));
		}
		if(value.equals("ShowButtons")){
			topButtons.setEnabled(set);
			topButtons.setVisible(set);
			properties.setValue("ShowButtons", String.valueOf(set));
		}
		if(value.equals("ShowDisplayoptions")){
			comboBoxBar.setEnabled(set);
			comboBoxBar.setVisible(set);
			properties.setValue("ShowDisplayoptions", String.valueOf(set));
		}
		if(value.equals("ShowNavigationbar")){
			navButtons.setEnabled(set);
			navButtons.setVisible(set);
			properties.setValue("ShowNavigationbar", String.valueOf(set));
		}
		if(value.equals("ShowSidetabbar")){
			if(!set)
				displayPane.setDividerSize(0);
			else
				displayPane.setDividerSize(5);
			displayPane.getLeftComponent().setEnabled(set);
			displayPane.getLeftComponent().setVisible(set);
			properties.setValue("ShowSidetabbar", String.valueOf(set));
		}

		/**
		 * Items on nav pane
		 */
		if(value.equals("Firstbottom")){
			first.setEnabled(set);
			first.setVisible(set);
		}
		if(value.equals("Back10bottom")){
			fback.setEnabled(set);
			fback.setVisible(set);
		}
		if(value.equals("Backbottom")){
			back.setEnabled(set);
			back.setVisible(set);
		}
		if(value.equals("Gotobottom")){
			pageCounter1.setEnabled(set);
			pageCounter1.setVisible(set);

			pageCounter2.setEnabled(set);
			pageCounter2.setVisible(set);

			pageCounter3.setEnabled(set);
			pageCounter3.setVisible(set);
		}
		if(value.equals("Forwardbottom")){
			forward.setEnabled(set);
			forward.setVisible(set);
		}
		if(value.equals("Forward10bottom")){
			fforward.setEnabled(set);
			fforward.setVisible(set);
		}
		if(value.equals("Lastbottom")){
			end.setEnabled(set);
			end.setVisible(set);
		}
		if(value.equals("Singlebottom")){
			singleButton.setEnabled(set);
			singleButton.setVisible(set);
		}
		if(value.equals("Continuousbottom")){
			continuousButton.setEnabled(set);
			continuousButton.setVisible(set);
		}
		if(value.equals("Continuousfacingbottom")){
			continuousFacingButton.setEnabled(set);
			continuousFacingButton.setVisible(set);
		}
		if(value.equals("Facingbottom")){
			facingButton.setEnabled(set);
			facingButton.setVisible(set);
		}
		if(value.equals("SideScrollbottom")){
			if(JAIHelper.isJAIused()){
				sideScrollButton.setEnabled(set);
				sideScrollButton.setVisible(set);
			}
		}
		if(value.equals("Memorybottom")){
			memoryBar.setEnabled(set);
			memoryBar.setVisible(set);
		}


		/**
		 * Items on option pane
		 */
		if(value.equals("Scalingdisplay")){
			for(int i=0; i!=c.length; i++){
				if(c[i] instanceof JLabel){
					if(((JLabel)c[i]).getText().equals(Messages.getMessage("PdfViewerToolbarScaling.text"))){
						c[i].setEnabled(set);
						scalingBox.setEnabled(set);
						c[i].setVisible(set);
						scalingBox.setVisible(set);
						properties.setValue("Scalingdisplay", String.valueOf(set));
					}
				}
			}
		}
		if(value.equals("Rotationdisplay")){
			for(int i=0; i!=c.length; i++){
				if(c[i] instanceof JLabel){
					if(((JLabel)c[i]).getText().equals(Messages.getMessage("PdfViewerToolbarRotation.text"))){
						c[i].setEnabled(set);
						rotationBox.setEnabled(set);
						c[i].setVisible(set);
						rotationBox.setVisible(set);
						properties.setValue("Rotationdisplay", String.valueOf(set));
					}
				}
			}
		}
		if(value.equals("Imageopdisplay")){
			for(int i=0; i!=c.length; i++){
				if(c[i] instanceof JLabel){
					if(((JLabel)c[i]).getText().equals(Messages.getMessage("PdfViewerToolbarImageOp.text"))){
						c[i].setVisible(set);
						qualityBox.setVisible(set);
						c[i].setEnabled(set);
						qualityBox.setEnabled(set);
						properties.setValue("Imageopdisplay", String.valueOf(set));
					}
				}
			}
		}
		if(value.equals("Progressdisplay")){
			statusBar.setEnabled(set);
            //<start-wrap>
			statusBar.setVisible(set);
            //<end-wrap>
			properties.setValue("Progressdisplay", String.valueOf(set));
		}

		/**
		 * Items on button bar
		 */
		if(value.equals("Openfilebutton")){
			openButton.setEnabled(set);
			openButton.setVisible(set);
		}
		if(value.equals("Printbutton")){
			printButton.setEnabled(set);
			printButton.setVisible(set);
		}
		if(value.equals("Searchbutton")){
			searchButton.setEnabled(set);
			searchButton.setVisible(set);
		}
		if(value.equals("Propertiesbutton")){
			docPropButton.setEnabled(set);
			docPropButton.setVisible(set);
		}
		if(value.equals("Aboutbutton")){
			infoButton.setEnabled(set);
			infoButton.setVisible(set);
		}
		if(value.equals("Snapshotbutton")){
			snapshotButton.setEnabled(set);
			snapshotButton.setVisible(set);
		}
		if(value.equals("CursorButton")){
			cursor.setEnabled(set);
			cursor.setVisible(set);
		}

		/**
		 * Items on signature tab
		 */
		if(value.equals("Pagetab") && navOptionsPanel.getTabCount()!=0){
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(pageTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}
		}
		if(value.equals("Bookmarkstab") && navOptionsPanel.getTabCount()!=0){
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(bookmarksTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}
		}
		if(value.equals("Layerstab") && navOptionsPanel.getTabCount()!=0){
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(layersTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}
		}
		if(value.equals("Signaturestab") && navOptionsPanel.getTabCount()!=0){
			for(int i=0; i<navOptionsPanel.getTabCount(); i++){

				if(navOptionsPanel.getTitleAt(i).equals(signaturesTitle) && !set){
					navOptionsPanel.remove(i);
				}
			}
		}

		/**
		 * Items from the menu item
		 */
		if(value.equals("FileMenu")){fileMenu.setEnabled(set); fileMenu.setVisible(set);}
		if(value.equals("OpenMenu")){openMenu.setEnabled(set); openMenu.setVisible(set);}
		if(value.equals("Open")){open.setEnabled(set); open.setVisible(set);}
		if(value.equals("Openurl")){openUrl.setEnabled(set); openUrl.setVisible(set);}

		if(value.equals("Save")){save.setEnabled(set); save.setVisible(set);}

        //added check to code (as it may not have been initialised)
		if(value.equals("Resaveasforms") && reSaveAsForms!=null){ //will not be initialised if Itext not on path
            reSaveAsForms.setEnabled(set);
            reSaveAsForms.setVisible(set);
        }

		if(value.equals("Find")){find.setEnabled(set); find.setVisible(set);}
		if(value.equals("Documentproperties")){documentProperties.setEnabled(set); documentProperties.setVisible(set);}
		if(value.equals("Print")){print.setEnabled(set); print.setVisible(set);}
		if(value.equals("Recentdocuments")){currentCommands.enableRecentDocuments(set);}
		if(value.equals("Exit")){exit.setEnabled(set); exit.setVisible(set);}

		if(value.equals("EditMenu")){editMenu.setEnabled(set); editMenu.setVisible(set);}
		if(value.equals("Copy")){copy.setEnabled(set); copy.setVisible(set);}
		if(value.equals("Selectall")){selectAll.setEnabled(set); selectAll.setVisible(set);}
		if(value.equals("Deselectall")){deselectAll.setEnabled(set); deselectAll.setVisible(set);}
		if(value.equals("Preferences")){preferences.setEnabled(set); preferences.setVisible(set);}

		if(value.equals("ViewMenu")){viewMenu.setEnabled(set); viewMenu.setVisible(set);}
		if(value.equals("GotoMenu")){goToMenu.setEnabled(set); goToMenu.setVisible(set);}
		if(value.equals("Firstpage")){firstPage.setEnabled(set); firstPage.setVisible(set);}
		if(value.equals("Backpage")){backPage.setEnabled(set); backPage.setVisible(set);}
		if(value.equals("Forwardpage")){forwardPage.setEnabled(set); forwardPage.setVisible(set);}
		if(value.equals("Lastpage")){lastPage.setEnabled(set); lastPage.setVisible(set);}
		if(value.equals("Goto")){goTo.setEnabled(set); goTo.setVisible(set);}
		if(value.equals("Previousdocument")){previousDocument.setEnabled(set); previousDocument.setVisible(set);}
		if(value.equals("Nextdocument")){nextDocument.setEnabled(set); nextDocument.setVisible(set);}

		if(value.equals("PagelayoutMenu")){pageLayoutMenu.setEnabled(set); pageLayoutMenu.setVisible(set);}
		if(value.equals("Single")){single.setEnabled(set); single.setVisible(set);}
		if(value.equals("Continuous")){continuous.setEnabled(set); continuous.setVisible(set);}
		if(value.equals("Facing")){facing.setEnabled(set); facing.setVisible(set);}
		if(value.equals("Continuousfacing")){continuousFacing.setEnabled(set); continuousFacing.setVisible(set);}

		//@kieran Remove when ready
		if(sideScroll!=null)
		if(value.equals("SideScroll")){sideScroll.setEnabled(set); sideScroll.setVisible(set);}

		if(value.equals("Fullscreen")){fullscreen.setEnabled(set); fullscreen.setVisible(set);}

		if(windowMenu!=null){
			if(value.equals("WindowMenu")){windowMenu.setEnabled(set); windowMenu.setVisible(set);}
			if(value.equals("Cascade")){cascade.setEnabled(set); cascade.setVisible(set);}
			if(value.equals("Tile")){tile.setEnabled(set); tile.setVisible(set);}
		}
		if(commonValues.isItextOnClasspath()){
			if(value.equals("ExportMenu")){exportMenu.setEnabled(set); exportMenu.setVisible(set);}
			if(value.equals("PdfMenu")){pdfMenu.setEnabled(set); pdfMenu.setVisible(set);}
			if(value.equals("Oneperpage")){onePerPage.setEnabled(set); onePerPage.setVisible(set);}
			if(value.equals("Nup")){nup.setEnabled(set); nup.setVisible(set);}
			if(value.equals("Handouts")){handouts.setEnabled(set); handouts.setVisible(set);}

			if(value.equals("ContentMenu")){contentMenu.setEnabled(set); contentMenu.setVisible(set);}
			if(value.equals("Images")){images.setEnabled(set); images.setVisible(set);}
			if(value.equals("Text")){text.setEnabled(set); text.setVisible(set);}

			if(value.equals("Bitmap")){bitmap.setEnabled(set); bitmap.setVisible(set);}

			if(value.equals("PagetoolsMenu")){pageToolsMenu.setEnabled(set); pageToolsMenu.setVisible(set);}
			if(value.equals("Rotatepages")){rotatePages.setEnabled(set); rotatePages.setVisible(set);}
			if(value.equals("Deletepages")){deletePages.setEnabled(set); deletePages.setVisible(set);}
			if(value.equals("Addpage")){addPage.setEnabled(set); addPage.setVisible(set);}
			if(value.equals("Addheaderfooter")){addHeaderFooter.setEnabled(set); addHeaderFooter.setVisible(set);}
			if(value.equals("Stamptext")){stampText.setEnabled(set); stampText.setVisible(set);}
			if(value.equals("Stampimage")){stampImage.setEnabled(set); stampImage.setVisible(set);}
			if(value.equals("Crop")){crop.setEnabled(set); crop.setVisible(set);}
		}
		if(value.equals("HelpMenu")){helpMenu.setEnabled(set); helpMenu.setVisible(set);}
		if(value.equals("Visitwebsite")){visitWebsite.setEnabled(set); visitWebsite.setVisible(set);}
		if(value.equals("Tipoftheday")){tipOfTheDay.setEnabled(set); tipOfTheDay.setVisible(set);}
		if(value.equals("Checkupdates")){checkUpdates.setEnabled(set); checkUpdates.setVisible(set);}
		if(value.equals("About")){about.setEnabled(set); about.setVisible(set);}

	}

	public void getHelpBox() {
		final JPanel panel = new JPanel();

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel("<html><p>This Help action is completely customizable in the SimpleViewer.  Click on the link" +
				"</p><p>below to load up our Support Wiki which contains tutorials on how you can " +
				"</p><p>easily customize this Help button, and all other aspects of the SimpleViewer" +
		"</p><p></p><p>The Support Wiki also contains exhaustive material covering all aspects of JPedal</p>");
		label.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		labelPanel.add(label);
		labelPanel.add(Box.createHorizontalGlue());

		top.add(labelPanel);

		JPanel linkPanel = new JPanel();
		linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.X_AXIS));
		linkPanel.add(Box.createHorizontalGlue());
		linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel url=new JLabel("<html><center>"+"http://www.jpedal.org/support.php");
		url.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		url.setForeground(Color.blue);
		url.setHorizontalAlignment(JLabel.CENTER);

		//@kieran - cursor
		url.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.HAND_CURSOR));
				url.setText("<html><center><a>http://www.jpedal.org/support.php</a></center>");
			}

			public void mouseExited(MouseEvent e) {
				panel.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				url.setText("<html><center>http://www.jpedal.org/support.php");
			}

			public void mouseClicked(MouseEvent e) {
				try {
					BrowserLauncher.openURL("http://www.jpedal.org/support.php");
				} catch (IOException e1) {

					JPanel errorPanel = new JPanel();
					errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));

					JLabel errorMessage = new JLabel("Your web browser could not be successfully loaded.  " +
					"Please copy and paste the URL below, manually into your web browser.");
					errorMessage.setAlignmentX(JLabel.LEFT_ALIGNMENT);
					errorMessage.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

					JTextArea textArea = new JTextArea("http://www.jpedal.org/support.php");
					textArea.setEditable(false);
					textArea.setRows(5);
					textArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
					textArea.setAlignmentX(JTextArea.LEFT_ALIGNMENT);

					errorPanel.add(errorMessage);
					errorPanel.add(textArea);

					showMessageDialog(errorPanel,"Error loading web browser",JOptionPane.PLAIN_MESSAGE);

				}
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});

		linkPanel.add(url);
		linkPanel.add(Box.createHorizontalGlue());
		top.add(linkPanel);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(top);

		showMessageDialog(panel,"JPedal Help",JOptionPane.PLAIN_MESSAGE);
	}

	public void dispose(){

		super.dispose();

		pageTitle=null;
		bookmarksTitle=null;

		signaturesTitle=null;
		layersTitle=null;

		layoutGroup=null;

		searchLayoutGroup=null;

		borderGroup=null;

		currentCommandListener=null;

		if(topButtons!=null)
			topButtons.removeAll();		
		topButtons =null;

		if(navButtons!=null)
			navButtons.removeAll();
		navButtons =null;

		if(comboBoxBar!=null)
			comboBoxBar.removeAll();
		comboBoxBar=null;

		if(currentMenu!=null)
			currentMenu.removeAll();
		currentMenu =null;

		if(coords!=null)
			coords.removeAll();
		coords=null;

		if(frame!=null)
			frame.removeAll();
		frame=null;

		if(desktopPane!=null)
			desktopPane.removeAll();
		desktopPane=null;

		if(navOptionsPanel!=null)
			navOptionsPanel.removeAll();
		navOptionsPanel=null;

		if(scrollPane!=null)
			scrollPane.removeAll();
		scrollPane =null;

		headFont=null;

		textFont=null;

		statusBar=null;

		pageCounter2 =null;

		pageCounter3=null;

		optimizationLabel=null;

		if(signaturesTree!=null){
			signaturesTree.setCellRenderer(null);
			signaturesTree.removeAll();
		}
		signaturesTree=null;

		if(layersPanel!=null)
			layersPanel.removeAll();
		layersPanel=null;

		user_dir =null;

		if(navToolBar!=null)
			navToolBar.removeAll();
		navToolBar =null;

		if(pagesToolBar!=null)
			pagesToolBar.removeAll();
		pagesToolBar =null;

		nextSearch=null;

		previousSearch=null;

		layersObject=null;
	}

	/**
	 * get Map containing Form Objects setup for Unique Annotations
	 * @return Map
	 */
	public Map getHotspots() {

		return objs;
	}

	public Point convertPDFto2D(int cx, int cy) {


		float scaling = getScaling();
		int inset = getPDFDisplayInset();
		int rotation = getRotation();

		if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE){
			cx=0;
			cy=0;
		} else if(rotation==90){
			int tmp=(cx-this.cropY);
			cx = (cy-this.cropX);
			cy =tmp;	
		}else if((rotation==180)){
			cx =-cx-(this.cropW+this.cropX);
			cy =(cy-this.cropY);
		}else if((rotation==270)){
			int tmp=-(this.cropH+this.cropY)-cx;
			cx =(this.cropW+this.cropX)+cy;
			cy =tmp;
		}else{
			cx = (cx-this.cropX);
			cy =(this.cropH+this.cropY)-cy;
		}

		cx=(int)((cx)*scaling);
		cy=(int)((cy)*scaling);

		if(decode_pdf.getPageAlignment()== Display.DISPLAY_CENTERED){
			int width=decode_pdf.getBounds().width;
			int pdfWidth=decode_pdf.getPDFWidth();

			if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE)
				pdfWidth=(int)decode_pdf.getMaximumSize().getWidth();

			if(width>pdfWidth)
				cx=cx+((width-pdfWidth)/(2));
		}

		cx=cx+inset;
		cy=cy+inset;

		return new Point(cx,cy);

	}

	/* used by JS to set values that dont need to save the new forms values */
	public boolean getFormsDirtyFlag() {
		return commonValues.isFormsChanged();
	}

	/* used by JS to set values that dont need to save the new forms values */
	public void setFormsDirtyFlag(boolean dirty) {
		commonValues.setFormsChanged(dirty);
	}

	public int getCurrentPage() {
		return commonValues.getCurrentPage();
	}
}