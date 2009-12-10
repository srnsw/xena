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
* SwingSearchWindow.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.generic.GUISearchWindow;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.DefaultSearchListener;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.grouping.SearchListener;
import org.jpedal.grouping.SearchType;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;
import org.jpedal.utils.Strip;
import org.jpedal.utils.SwingWorker;
import org.jpedal.utils.repositories.Vector_Rectangle;
                                    
/**provides interactive search Window and search capabilities*/
public class SwingSearchWindow extends JFrame implements GUISearchWindow{

	public static int SEARCH_EXTERNAL_WINDOW = 0;
	public static int SEARCH_TABBED_PANE = 1;
	public static int SEARCH_MENU_BAR = 2;

	int style = 0;

	/**flag to stop multiple listeners*/
	private boolean isSetup=false;

	String defaultMessage="Search PDF Here";

	JTextField searchText=null;
	JCheckBox searchAll;
	JTextField searchCount;
	DefaultListModel listModel;
	SearchList results;
	JLabel label = null;

	private JPanel advancedPanel;
	private JComboBox searchType;
	private JCheckBox wholeWordsOnlyBox, caseSensitiveBox, multiLineBox;
	
	ActionListener AL=null;
	ListSelectionListener LSL = null;
	WindowListener WL;
	KeyListener KL;

	/**swing thread to search in background*/
	SwingWorker searcher=null;

	/**flag to show searching taking place*/
	public boolean isSearch=false;

	JButton searchButton=null;

	/**number fo search items*/
	private int itemFoundCount=0;

	/**used when fiding text to highlight on page*/
	Map textPages=new HashMap();
	Map textRectangles=new HashMap();

	final JPanel nav=new JPanel();

	Values commonValues;
	SwingGUI currentGUI;
	PdfDecoder decode_pdf;

	/**deletes message when user starts typing*/
	private boolean deleteOnClick;

	public SwingSearchWindow(SwingGUI currentGUI) {
		this.currentGUI=currentGUI;
		this.setName("searchFrame");
	}


	public Component getContentPanel(){
		return getContentPane();
	}

	/**
	 * find text on page
	 */
	public void find(final PdfDecoder dec, final Values values){
		
		this.decode_pdf = dec;
		this.commonValues = values;
		
//		System.out.println("clicked pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));
		
		

		/**
		 * pop up new window to search text (initialise if required
		 */
		if(isSetup){ //global variable so do NOT reinitialise
			searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound")+ ' ' +itemFoundCount);
			searchText.selectAll();
			searchText.grabFocus();
		}else{
			isSetup=true;
			
			setTitle(Messages.getMessage("PdfViewerSearchGUITitle.DefaultMessage"));
			
			defaultMessage=Messages.getMessage("PdfViewerSearchGUI.DefaultMessage");

			searchText=new JTextField(defaultMessage);
			searchText.setName("searchText");

			searchButton=new JButton(Messages.getMessage("PdfViewerSearch.Button"));

			advancedPanel = new JPanel(new GridBagLayout());
			
			searchType = new JComboBox(new String[] {"Match Exact word or phrase", 
					"Match Any of the words"});
			
			wholeWordsOnlyBox = new JCheckBox("Whole words only");
			wholeWordsOnlyBox.setName("wholeWords");
			
			caseSensitiveBox = new JCheckBox("Case-Sensitive");
			caseSensitiveBox.setName("caseSensitive");
			
			multiLineBox = new JCheckBox("Include split line results");
			multiLineBox.setName("multiLine");
			
			searchType.setName("combo");
			
			GridBagConstraints c = new GridBagConstraints();
			
			advancedPanel.setPreferredSize(new Dimension(advancedPanel.getPreferredSize().width, 150));
			c.gridx = 0;
			c.gridy = 0;
			
			c.anchor = GridBagConstraints.PAGE_START;
			c.fill = GridBagConstraints.HORIZONTAL; 
			
			c.weightx = 1;
			c.weighty = 0;
			advancedPanel.add(new JLabel("Return results containing:"), c);
			
			c.insets = new Insets(5,0,0,0);
			c.gridy = 1;
			advancedPanel.add(searchType, c);
			
			c.gridy = 2;
			advancedPanel.add(new JLabel("Use these additional criteria:"), c);
			
			c.insets = new Insets(0,0,0,0);
			c.gridy = 3;
			advancedPanel.add(wholeWordsOnlyBox, c);
			
			c.gridy = 4;
			advancedPanel.add(caseSensitiveBox, c);
			
			c.weighty = 1;
			c.gridy = 5;
			advancedPanel.add(multiLineBox, c);
			
			advancedPanel.setVisible(false);
			
			nav.setLayout(new BorderLayout());

			WL = new WindowListener(){
				public void windowOpened(WindowEvent arg0) {}

				//flush objects on close
				public void windowClosing(WindowEvent arg0) {

                    removeSearchWindow(true);
                }

				public void windowClosed(WindowEvent arg0) {}

				public void windowIconified(WindowEvent arg0) {}

				public void windowDeiconified(WindowEvent arg0) {}

				public void windowActivated(WindowEvent arg0) {}

				public void windowDeactivated(WindowEvent arg0) {}
			};

			this.addWindowListener(WL);

			nav.add(searchButton,BorderLayout.EAST);

			nav.add(searchText,BorderLayout.CENTER);

			searchAll=new JCheckBox();
			searchAll.setSelected(true);
			searchAll.setText(Messages.getMessage("PdfViewerSearch.CheckBox"));
			
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add(searchAll, BorderLayout.NORTH);
			
			label = new JLabel("<html><center> " + "Show Advanced");
			label.setForeground(Color.blue);
			label.setName("advSearch");
			
			label.addMouseListener(new MouseListener() {
				boolean isVisible = false;
				
				String text = "Show Advanced";
				
			    public void mouseEntered(MouseEvent e) {
			        nav.setCursor(new Cursor(Cursor.HAND_CURSOR));
			        label.setText("<html><center><a href=" + text + ">" + text + "</a></center>");
			    }
			
			    public void mouseExited(MouseEvent e) {
			        nav.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			        label.setText("<html><center>" + text);
			    }
			
			    public void mouseClicked(MouseEvent e) {
					if (isVisible) {
						text = "Show Advanced";
						label.setText("<html><center><a href=" + text + ">" + text + "</a></center>");
						advancedPanel.setVisible(false);
					} else {
						text = "Hide Advanced";
						label.setText("<html><center><a href=" + text + ">" + text + "</a></center>");
						advancedPanel.setVisible(true);
					}
					
					isVisible = !isVisible;
				}
			
			    public void mousePressed(MouseEvent e) {}
			    public void mouseReleased(MouseEvent e) {}
			});

			label.setBorder(BorderFactory.createEmptyBorder(3, 4, 4, 4));
			topPanel.add(label, BorderLayout.SOUTH);
//			nav.
			
			nav.add(topPanel,BorderLayout.NORTH);
			itemFoundCount=0;
			textPages.clear();
			textRectangles.clear();
			listModel = null;

			searchCount=new JTextField(Messages.getMessage("PdfViewerSearch.ItemsFound")+ ' ' +itemFoundCount);
			searchCount.setEditable(false);
			nav.add(searchCount,BorderLayout.SOUTH);

			listModel = new DefaultListModel();
			results=new SearchList(listModel,textPages);
			results.setName("results");
			
            //<link><a name="search" />
            /**
             * highlight text on item selected
             */
			LSL = new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					/** 
					 * Only do something on mouse button up,
					 * prevents this code being called twice
					 * on mouse click
					 */
					if (!e.getValueIsAdjusting()) {
						
						if(!commonValues.isProcessing()){//{if (!event.getValueIsAdjusting()) {

							float scaling=currentGUI.getScaling();
							int inset=currentGUI.getPDFDisplayInset();

							int id=results.getSelectedIndex();

							decode_pdf.setSearchHighlightAreas(null);
//							System.out.println("clicked pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));

							if(id!=-1){

								Integer key=new Integer(id);
								Object newPage=textPages.get(key);

								if(newPage!=null){
									int nextPage=((Integer)newPage).intValue();


									//move to new page
									if(commonValues.getCurrentPage()!=nextPage){

										commonValues.setCurrentPage(nextPage);

										currentGUI.resetStatusMessage(Messages.getMessage("PdfViewer.LoadingPage")+ ' ' +commonValues.getCurrentPage());

										/**reset as rotation may change!*/
										decode_pdf.setPageParameters(scaling, commonValues.getCurrentPage());

										//decode the page
										currentGUI.decodePage(false);

										decode_pdf.invalidate();
									}

									while(commonValues.isProcessing()){
										//Ensure page has been processed else highlight may be incorrect
									}

									

									/**
									 * Highlight all search results on page.
									 * Duplicating moab functionality for test purposes
									 */
									boolean moabDebug = false;
									if(moabDebug){
										
										Rectangle[] debugMoab;
										Vector_Rectangle debugVector = new Vector_Rectangle();
										
										for(int k=0; k!=results.getModel().getSize(); k++){
											Object page=textPages.get(new Integer(k));

											if(page!=null){

												int currentPage = ((Integer)page).intValue();
												if(currentPage==nextPage){
													Object highlight= textRectangles.get(key);

													if(highlight instanceof Rectangle){
														debugVector.addElement((Rectangle)highlight);
													}
													if(highlight instanceof Rectangle[]){
														Rectangle[] areas = (Rectangle[])highlight;
														for(int i=0; i!=areas.length; i++){
															debugVector.addElement(areas[i]);
														}
													}
												}
											}
										}

										debugVector.trim();
										debugMoab = debugVector.get();
										decode_pdf.setSearchHighlightAreas(debugMoab);
										
									}else{

										Object highlight= textRectangles.get(key);

										if(highlight instanceof Rectangle){
											decode_pdf.scrollRectToHighlight((Rectangle)highlight);

											//add text highlight
											decode_pdf.setSearchHighlightArea((Rectangle)highlight);
										}
										if(highlight instanceof Rectangle[]){
											decode_pdf.scrollRectToHighlight(((Rectangle[])highlight)[0]);

											//add text highlight
											decode_pdf.setSearchHighlightAreas(((Rectangle[])highlight));
										}
										
									}
									
									decode_pdf.invalidate();
									decode_pdf.repaint();

								}
							}
						}

						//When page changes make sure only relevant navigation buttons are displayed
						if(commonValues.getCurrentPage()==1)
							currentGUI.setBackNavigationButtonsEnabled(false);
						else
							currentGUI.setBackNavigationButtonsEnabled(true);

						if(commonValues.getCurrentPage()==decode_pdf.getPageCount())
							currentGUI.setForwardNavigationButtonsEnabled(false);
						else
							currentGUI.setForwardNavigationButtonsEnabled(true);


					}else{
						results.repaint();

					}
				}
			};
			
			results.addListSelectionListener(LSL);
			results.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			
			//setup searching
			//if(AL==null){
			AL = new ActionListener(){
				public void actionPerformed(ActionEvent e) {

					if(!isSearch){

						try {
							searchText();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}else{
						searcher.interrupt();
						isSearch=false;
						searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
					}
				}
			};

			searchButton.addActionListener(AL);
			//}

			searchText.selectAll();
			deleteOnClick=true;

			KL = new KeyListener(){
				public void keyTyped(KeyEvent e) {
					if(searchText.getText().length() == 0){
						currentGUI.nextSearch.setVisible(false);
						currentGUI.previousSearch.setVisible(false);
					}

					//clear when user types
					if(deleteOnClick){
						deleteOnClick=false;
						searchText.setText("");
					}
					int id = e.getID();
					if (id == KeyEvent.KEY_TYPED) {
						char key=e.getKeyChar();

						if(key=='\n'){
							
							if(!decode_pdf.isOpen())
								JOptionPane.showMessageDialog(null, "File must be open before you can search.");
							else{
								try {
									currentGUI.nextSearch.setVisible(true);
									currentGUI.previousSearch.setVisible(true);
									
									currentGUI.nextSearch.setEnabled(false);
									currentGUI.previousSearch.setEnabled(false);

									isSearch=false;
									searchText();
								} catch (Exception e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}

				public void keyPressed(KeyEvent arg0) {}

				public void keyReleased(KeyEvent arg0) {}
			};

			searchText.addKeyListener(KL);
			if(style==SEARCH_EXTERNAL_WINDOW || style==SEARCH_TABBED_PANE){
				//build frame
				JScrollPane scrollPane=new JScrollPane();
				scrollPane.getViewport().add(results);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.getVerticalScrollBar().setUnitIncrement(80);
				scrollPane.getHorizontalScrollBar().setUnitIncrement(80);

				getContentPane().setLayout(new BorderLayout());
				getContentPane().add(scrollPane,BorderLayout.CENTER);
				getContentPane().add(nav,BorderLayout.NORTH);
				getContentPane().add(advancedPanel, BorderLayout.SOUTH);
				
				//position and size
				Container frame = currentGUI.getFrame();
				if(commonValues.getModeOfOperation() == Values.RUNNING_APPLET){
					if (currentGUI.getFrame() instanceof JFrame)
						frame = ((JFrame)currentGUI.getFrame()).getContentPane();
				}

				int w=230;

				int h=frame.getHeight();
				int x1=frame.getLocationOnScreen().x;
				int x=frame.getWidth()+x1;
				int y=frame.getLocationOnScreen().y;
				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

				int width = d.width;
				if(x+w>width && style==SEARCH_EXTERNAL_WINDOW){
					x=width-w;
					frame.setSize(x-x1,frame.getHeight());
				}

				setSize(w,h);
				setLocation(x,y);
				
				searchAll.setFocusable(false);
				
				searchText.grabFocus();
			}else{
				//Whole Panel not used, take what is needed
				currentGUI.setSearchText(searchText);
			}
		}
		if(style==SEARCH_EXTERNAL_WINDOW)
			setVisible(true);
	}


	public void removeSearchWindow(boolean justHide) {

		//System.out.println("remove search window");

		setVisible(false);

		setVisible(false);

		if(searcher!=null)
			searcher.interrupt();

		if(isSetup && !justHide){
			if(listModel!=null)
				listModel.clear();//removeAllElements();

			//searchText.setText(defaultMessage);
			//searchAll=null;
			//if(nav!=null)
			//    nav.removeAll();

			itemFoundCount=0;
			isSearch=false;

		}

        //lose any highlights and force redraw with non-existent box
        if(decode_pdf!=null){
            decode_pdf.setMouseHighlightArea(null);
            decode_pdf.repaint();
        }
    }

	private void searchText() throws Exception {

		/** if running terminate first */
		if ((searcher != null))
			searcher.interrupt();

		searchButton.setText(Messages.getMessage("PdfViewerSearchButton.Stop"));
		searchButton.invalidate();
		searchButton.repaint();
		isSearch=true;

		searchCount.setText(Messages.getMessage("PdfViewerSearch.Scanning1"));
		searchCount.repaint();

		searcher = new SwingWorker() {
			public Object construct() {

				try {
					
//					System.out.println("seareching pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));
					
					listModel.removeAllElements();
					results.repaint();

					int listCount = 0;
					textPages.clear();

					textRectangles.clear();
					itemFoundCount = 0;
					decode_pdf.setMouseHighlightAreas(null);

					// get text
					String textToFind = searchText.getText().trim();
					String[] terms;
					if(searchType.getSelectedIndex() == 0){ // find exact word or phrase
						terms = new String[] { textToFind };
					} else { // match any of the words
						terms = textToFind.split(" ");
						for (int i = 0; i < terms.length; i++) {
							terms[i] = terms[i].trim();
						}
					}
					
					//System.out.println("textToFind = "+textToFind);

					// get page sizes
					PdfPageData pageSize = decode_pdf.getPdfPageData();

					int x1, y1, x2, y2;

					// page range
					int startPage = 1;
					int endPage = commonValues.getPageCount() + 1;
					
					if (!searchAll.isSelected()) {
						startPage = commonValues.getCurrentPage();
						endPage = startPage + 1;
					}

					// search all pages
					for (int page = startPage; page < endPage; page++) {
						if (Thread.interrupted()) {
							throw new InterruptedException();
						}
						
						/** common extraction code */
						PdfGroupingAlgorithms currentGrouping;

						/** create a grouping object to apply grouping to data */
						try {
							if (page == commonValues.getCurrentPage())
								currentGrouping = decode_pdf.getGroupingObject();
							else {
								decode_pdf.decodePageInBackground(page);
								currentGrouping = decode_pdf.getBackgroundGroupingObject();
							}

							// tell JPedal we want teasers
                            currentGrouping.generateTeasers();

                            //allow us to add options
                            currentGrouping.setIncludeHTML(true);

                            // set size
							x1 = pageSize.getMediaBoxX(page);
							x2 = pageSize.getMediaBoxWidth(page);
							y1 = pageSize.getMediaBoxY(page);
							y2 = pageSize.getMediaBoxHeight(page);

							final SearchListener listener = new DefaultSearchListener(); 

							int searchType = SearchType.DEFAULT;
							
							if(wholeWordsOnlyBox.isSelected())
								searchType |= SearchType.WHOLE_WORDS_ONLY;
							
							if(caseSensitiveBox.isSelected())
								searchType |= SearchType.CASE_SENSITIVE;
							
							if(multiLineBox.isSelected())
								searchType |= SearchType.MUTLI_LINE_RESULTS;
							
							SortedMap highlightsWithTeasers = currentGrouping.findMultipleTermsInRectangleWithMatchingTeasers(x1, y1, x2, y2, pageSize.getRotation(page), page, terms, searchType, listener);
							
							if (Thread.interrupted()) {
								throw new InterruptedException();
							}
							
							if (!highlightsWithTeasers.isEmpty()) {

								// update count display
								itemFoundCount = itemFoundCount + highlightsWithTeasers.size();

								Iterator iter = highlightsWithTeasers.entrySet().iterator();
								while (iter.hasNext()) {
									Map.Entry e = (Map.Entry) iter.next();

									/*highlight is a rectangle or a rectangle[]*/
									Object highlight = e.getKey();
									
									final String teaser = (String) e.getValue();
									
									Runnable setTextRun = new Runnable() {
										public void run() {

                                            //if highights ensure displayed by wrapping in tags
                                            if(teaser.indexOf("<b>")==-1)
                                                listModel.addElement(teaser);
                                            else
                                                listModel.addElement("<html>"+teaser+"</html>");
                                        }
									};
									SwingUtilities.invokeAndWait(setTextRun);

									Integer key = new Integer(listCount);
									listCount++;
									textRectangles.put(key, highlight);
									textPages.put(key, new Integer(page));
								}
							}

							// new value or 16 pages elapsed
							if ((!highlightsWithTeasers.isEmpty()) | ((page % 16) == 0)) {
								searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount + ' '
										+ Messages.getMessage("PdfViewerSearch.Scanning") + page);
								searchCount.invalidate();
								searchCount.repaint();
							}
						} catch (PdfException e1) {
						}
					}


					searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount + "  "
							+ Messages.getMessage("PdfViewerSearch.Done"));
					results.invalidate();
					results.repaint();
					results.setSelectedIndex(0);
					results.setLength(listModel.capacity());
					currentGUI.setResults(results);

					// reset search button
					isSearch = false;

					currentGUI.nextSearch.setEnabled(true);
					currentGUI.previousSearch.setEnabled(true);

					searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));


                }catch(InterruptedException ee){

                    //Exception caused so use alert user and allow search
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							JOptionPane.showMessageDialog(null, "Search stopped by user.");
							currentGUI.nextSearch.setEnabled(true);
							currentGUI.previousSearch.setEnabled(true);
						}
					});
                } catch (Exception e) {
					//Exception caused so use alert user and allow search
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							JOptionPane.showMessageDialog(null, "An error occured during search. Some results may be missing.\n\nPlease send the file to IDRSolutions for investigation.");
							currentGUI.nextSearch.setEnabled(true);
							currentGUI.previousSearch.setEnabled(true);
						}
					});

				}
				return null;
			}
		};

		searcher.start();
	}

	public int getListLength(){
		return listModel.capacity();
	}

	public void grabFocusInInput() {
		searchText.grabFocus();

	}

	public boolean isSearchVisible() {
		return this.isVisible();
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public JTextField getSearchText() {
		return searchText;
	}

	public Map getTextRectangles() {
		return textRectangles;
	}

	public SearchList getResults() {
		return results;
	}
	
}
