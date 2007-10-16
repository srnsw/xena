/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 02-Jul-2003
 * 
 * Simple demo to show JPedal being used as a simple GUI viewer
 */
package au.gov.naa.digipres.xena.plugin.pdf;

/**standard Java stuff*/
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jpedal.PdfDecoder;
import org.jpedal.fonts.PdfFont;
import org.jpedal.objects.PdfAnnots;
import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.PdfPageData;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * Simple demo to show JPedal being used as a simple GUI viewer.
 *
 * 2003-07-14	Add simple isProcessing boolean lock to stop multiple accesses.
 * 						Threaded code on buttons to improve response
 * 2003-09-24	Allow for null password being passed when user selects cancel
 * 2003-11-13	Add in JavaPrint code to proof concept
 * 2003-11-14	Add in ability to link rendering intent to creator
 * 2003-12-12	Improved printing code
 * 2004-01-05	Printing code updated but not fully operational.
 * 				If scaling selected when no page opened, error no longer generated.
 * 2004-01-13	Make sure user told if page takes over 1.5 seconds to decode.
 * 				Tidy up printing
 * 				Clean up code.
 * 2004-01-15	Tweak open file code
 * 2004-01-24	Print code updated:-
 *  						shows how to alter print border
 * 						Landscape printing now works
 * 						Background printing
 * 2004-02-28	Make sure screen redraws if nothing selected
 * 2004-03-17	Updated buttons and altered forward/back routines.
 * 2004-04-19	Menus to show fonts and Document properties.
 * 2004-04-22	No longer redraws page if open dialog selected and then cancel option.
 * 2004-04-24	Code cleaned up. Info option renamed to about and moved to help menu
 * 2004-04-25	Add listener to display screen co-ords
 * 2004-04-30	Improve display of Properties in page
 * 2004-04-05	Add support for links in PDF.
 * 2004-06-24	Add Outline code to display outlines and option to use draft mode
 * 2004-06-30	Add rotation to code
 * 2004-09-13	Improve locking if page decoded. Add left nav bar with outlines and thumbnails. Add 1.3 printing.
 * 2004-09-14	Add goto function. Add common font mapping
 * 2004-09-23	Rotation reset if scaling or page changed.
 * 2004-09-24	Rewrite threading of thumbnail viewer to improve repsonsiveness
 * 2004-09-27	Reset page parameters when moving between pages with thumbnail option
 * 2004-09-27	Reset page parameters when choosing bookmark
 * 2004-10-05	Improve threading performance
 * 2004-10-05	Threading rewritten to improve image refresh and simplify code
 * 2004-10-14	Buttons do not do anything until file opened
 * 2004-11-20   Chris Bitmead - made a number of changes to get it working in Xena.
 ****/
public class PdfViewer extends JPanel {

	long start = System.currentTimeMillis();

	/**PdfDecoder object which does all the decoding and rendering*/
	private PdfDecoder decode_pdf;

	/**holds the annotations data for the page*/
	private PdfAnnots pageAnnotations;

	JButton[] pageButton;

	boolean[] buttonDrawn;

	boolean[] isLandscape;

	int[] pageHeight;

	/**weight and height for thumbnails*/
	private int thumbH = 100, thumbW = 70;

	private JTree tree;

	/**used by tree to convert page title into page number*/
	private HashMap pageLookupTable = new HashMap();

	/**used by tree to find point to scroll to*/
	private HashMap pointLookupTable = new HashMap();

	/**directory to load files from*/
	private String inputDir = System.getProperty("user.dir");

	/**current page number*/
	private int currentPage = 1;

	/**store page rotation*/
	private int rotation = 0;

	/**store page size*/
	private int max_x, max_y, min_x, min_y;

	/**used to track changes when dragging rectangle around*/
	private int m_x1, m_y1, m_x2, m_y2, old_m_x2 = -1, old_m_y2 = -1;

	/**current cursor position*/
	int cx, cy;

	/**flag to stop mutliple prints*/
	private static int printingThreads = 0;

	/**Swing thread to decode in background - we have one thread we use for various tasks*/
	private SwingWorker worker = null;

	// show co-ords on page
	private JLabel coords = new JLabel("");

	/**number of pages in current pdf*/
	private int pageCount = 1;

	/**page scaling to use 1=100%*/
	private float scaling = 1;

	// private String selectedFile = null;

	/**boolean lock to stop multiple access*/
	private boolean isProcessing = false;

	private final String[] scalingValues =
	    {"Scale to Height", "Scale to Width", "25", "50", "75", "100", "125", "150", "200", "250", "500", "750", "1000"};

	/**scaling values as floats to save conversion*/
	private final float[] scalingFloatValues = {0, 0, .25f, .5f, .75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 5.0f, 7.5f, 10.0f};

	/**default scaling on the combobox scalingValues*/
	private final int defaultSelection = 5;

	/**scaling factors on the page*/
	private JComboBox scalingBox = new JComboBox(scalingValues);

	/**Scrollpane for pdf panel*/
	JScrollPane scrollPane = new JScrollPane();

	/**scaling values as floats to save conversion*/
	private final String[] rotationValues = {"0", "90", "180", "270"};

	/**scaling factors on the page*/
	private JComboBox rotationBox = new JComboBox(rotationValues);

	/**page counter to display*/
	private JTextField pageCounter1 = new JTextField("Page");

	private JTextField pageCounter2 = new JTextField(4);

	private JTextField pageCounter3 = new JTextField("of   Pages");

	/**main display panel we add all components onto*/
	public JFrame mainFrame;

	JPanel thumbnailPanel = new JPanel();

	private int inset = 25;

	/**size of file for display*/
	private long size;

	/**tells user if we enter a link*/
	private String message = "";

	/**used to trace file change in threads*/
	private int currentFileCount = 0;

	/**list for types - assumes present in org/jpedal/examples/simpleviewer/annots*
	 * "OTHER" MUST BE FIRST ITEM
	 * Try adding Link to the list to see links
	 */
	private String[] annotTypes = {"Other", "Text", "FileAttachment"};

	JScrollPane bookmarkScrollPane = new JScrollPane();
	JScrollPane thumbnailScrollPane = new JScrollPane();

	JTabbedPane navOptionsPanel = new JTabbedPane();

	JSplitPane displayPane;

	/**show if outlines drawn*/
	private boolean hasOutlinesDrawn = false;

	// <start-13>
	/**does background drawing of thumbnails*/
	private ThumbPainter painter = new ThumbPainter();
	// <end-13>

	/**can switch on or off thumbnails - DOES NOT WORK ON JAVA 1.3*/
	// <start-13>
	private boolean showThumbnails = true;

	/**<end-13>
	     private boolean showThumbnails=false;
	     /**/

	public PdfViewer(JFrame m) {
		this.mainFrame = m;

		/**debugging code to create a log
		   LogWriter.setupLogFile(true,1,"","v",false);
		   LogWriter.log_name =  "/mnt/shared/log.txt"; */
		/**/

		/**
		 * init instance of PdfDecoder as a GUI object by using true
		 */
		decode_pdf = new PdfDecoder(); // USE THIS FOR THE VIEWER ONLY

		/** */
		// ensure borfer round display - not flush with panel edge
		decode_pdf.setInset(inset, inset);

		/**
		 * ANNOTATIONS code 1
		 *
		 * use for annotations, loading icons and enabling display of annotations
		 * this enables general annotations with an icon for each type.
		 * See below for more specific function.
		 */
		decode_pdf.createPageHostspots(annotTypes, "org/jpedal/examples/simpleviewer/annots/");

		/**
		 * ANNOTATIONS code 2
		 *
		 * this code allows you to create a unique set on icons for any type of annotations, with
		 * an icons for every annotation, not just types.
		 */
		// createUniqueAnnontationIcons();

		/**comment out this line for faster performance but slighlty poorer images*/
		// decode_pdf.useDraftScaling();

		/**
		 * FONT EXAMPLE CODE showing JPedal's functionality to set values for
		 * non-embedded fonts.
		 *
		 * This allows sophisticated substitution of non-embedded fonts.
		 *
		 * Most font mapping is done as the fonts are read, so these calls must
		 * be made BEFORE the openFile() call.
		 */

		/**
		 * FONT EXAMPLE - Replace global default for non-embedded fonts.
		 *
		 * You can replace Lucida as the standard font used for all non-embedded and substituted fonts
		 * by using is code.
		 * Java fonts are case sensitive, but JPedal resolves this, so you could
		 * use Webdings, webdings or webDings for Java font Webdings
		 *
		   try{
		    //choice of example font to stand-out (useful in checking results to ensure no font missed.
		    //In general use Helvetica or similar is recommended
		    decode_pdf.setDefaultDisplayFont("Webdings");
		   }catch(PdfFontException e){ //if its not available catch error and show valid list

		    System.out.println(e.getMessage());

		    //get list of fonts you can use
		 String[] fontList =GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		 System.out.println("Fonts available are:-");
		 System.out.println("=====================\n");
		 int count = fontList.length;
		 for (int i = 0; i < count; i++) {
		  Font f=new Font(fontList[i],1,10);
		  System.out.println(fontList[i]+" (Postscript="+f.getPSName()+")");

		 }
		 System.exit(1);

		   }/***/

		/**
		 * IMPORTANT note on fonts for EXAMPLES
		 *
		 * USEFUL TIP : The SimpleViewer displays a list of fonts used on the
		 * current PDF page with the File > Fonts menu option.
		 *
		 * PDF allows the use of weights for fonts so Arial,Bold is a weight of
		 * Arial. This value is not case sensitive so JPedal would regard
		 * arial,bold and aRiaL,BoLd as the same.
		 *
		 * Java supports a set of Font families internally (which may have
		 * weights), while JPedals substitution facility uses physical True Type
		 * fonts so it is resolving each font weight separately. So mapping
		 * works differently, depending on which is being used.
		 *
		 * If you are using a font, which is named as arial,bold you can use
		 * either arial,bold or arial (and JPedal will then try to select the
		 * bold weight if a Java font is used).
		 *
		 * So for a font such as Arial,Bold JPedal will test for an external
		 * truetype font substitution (ie arialMT.ttf) mapped to Arial,Bold. BUT
		 * if the substitute font is a Java font an additional test will be made
		 * for a match against Arial if there is no match on Arial,Bold.
		 *
		 * If you want to map all Arial to equivalents to a Java font such as
		 * Times New Roman, just map Arial to Times New Roman (only works for
		 * inbuilt java fonts). Note if you map Arial,Bold to a Java font such
		 * as Times New Roman, you will get Times New Roman in a bold weight, if
		 * available. You cannot set a weight for the Java font.
		 *
		 * If you wish to substitute Arial but not Arial,Bold you should
		 * explicitly map Arial,Bold to Arial,Bold as well.
		 *
		 * The reason for the difference is that when using Javas inbuilt fonts
		 * JPedal can resolve the Font Family and will try to work out the
		 * weight internally. When substituting Truetype fonts, these only
		 * contain ONE weight so JPedal is resolving the Font and any weight as
		 * a separate font . Different weights will require separate files.
		 *
		 * Open Source version does not support all font capabilities.
		 */

		/**
		 * FONT EXAMPLE - Use Standard Java fonts for substitution
		 *
		 * This code tells JPedal to substitute fonts which are not embedded.
		 *
		 * The Name is not case-sensitive.
		 *
		 * Spaces are important so TimesNewRoman and Times New Roman are
		 * degarded as 2 fonts.
		 *
		 * If you have 2 copies of arialMT.ttf in the scanned directories, the
		 * last one will be used.
		 *
		 *
		 * If you wish to use one of Javas fonts for display (for example, Times
		 * New Roman is a close match for myCompanyFont in the PDF, you can the
		 * code below
		 *
		 * String[] aliases={"Times New Roman"};//,"helvetica","arial"};
		 * decode_pdf.setSubstitutedFontAliases("myCompanyFont",aliases);
		 *
		 * Here is is used to map Javas Times New Roman (and all weights) to
		 * TimesNewRoman.
		 */
		String[] nameInPDF = {"TimesNewRoman"}; // ,"helvetica","arial"};
		decode_pdf.setSubstitutedFontAliases("Times New Roman", nameInPDF);

		/**
		 * add alternate names for 14 fonts so Arial and Helvetica are synonomous - works under
		 * windows. Fonts are not always there for Mac/Linux
		 */
		PdfFont.setStandardFontMappings();

		/**
		 * setup  screen
		 */
		/*
		 * mainFrame.setTitle( "JPedal GUI Open Source version ( version " + PdfDecoder.version + " )");
		 */
		/**/

		this.setLayout(new BorderLayout());

		RepaintManager rm = RepaintManager.currentManager(mainFrame);
		rm.setDoubleBufferingEnabled(true);

		/**add the pdf display to show pages*/
		scrollPane.getViewport().add(decode_pdf);
		scrollPane.getVerticalScrollBar().setUnitIncrement(80);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(80);

		/**create scrollpanes*/
		bookmarkScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		bookmarkScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		thumbnailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		thumbnailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		/**Create a left-right split pane with tabs
		 * and add to main display
		 */
		navOptionsPanel.setTabPlacement(JTabbedPane.TOP);
		// <start-13>
		// navOptionsPanel.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		// <end-13>
		if (!showThumbnails) {
			navOptionsPanel.setVisible(false);
		}
		displayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navOptionsPanel, scrollPane);

		displayPane.setDividerLocation(170);
		this.add(displayPane, BorderLayout.CENTER);

		/**create the icons and menus for program*/
		createMenuAndToolBar();

		/**
		 * set display to occupy half screen size and display
		 */
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = d.width / 2, height = d.height / 2;

		// mainFrame.setSize(width, height);

		/**move over if we will display thumbnails*/
		if (showThumbnails) {
			width = width + 200;
		}

		// mainFrame.setLocation((d.width - width) / 2, (d.height - height) / 2);

	}

	/**
	 * example code which sets up an individual icon for each annotation to display - only use
	 * if you require each annotation to have its own icon<p>
	 * To use this you ideally need to parse the annotations first -there is a method allowing you to
	 * extract just the annotations from the data.
	 */
	private void createUniqueAnnontationIcons() {

		int pages = 20; // hard-code. IF you want to use
		// decode_pdf.getPageCount(); , you will need to use this method AFTER the openFile call!!!

		int max = 20; // you will need to adapt to suit

		// this code will either do all or just 1 - comment out to suit
		// for (int types = 0; types < this.annotTypes.length; types++) { //all annots
		{
			int types = 1; // just 1 type

			for (int p = 1; p < pages + 1; p++) {

				Image[] annotIcons = new Image[max];

				for (int i = 0; i < max; i++) {

					// create a unique graphic
					annotIcons[i] = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2 = (Graphics2D) annotIcons[i].getGraphics();
					g2.setColor(Color.black);
					g2.fill(new Rectangle(0, 0, 32, 32));
					g2.setColor(Color.red);
					g2.draw(new Rectangle(0, 0, 32, 32));
					g2.setColor(Color.white);
					g2.drawString((p + "/" + i + annotTypes[types]), 0, 10);

				}

				// add set of icons to display
				decode_pdf.addUserIconsForAnnotations(p, annotTypes[types], annotIcons);

			}
		}
	}

	/**
	 * scan sublist
	 */
	private void readChildNodes(Node rootNode, DefaultMutableTreeNode topNode) {
		NodeList children = rootNode.getChildNodes();
		int childCount = children.getLength();

		for (int i = 0; i < childCount; i++) {

			Node child = children.item(i);

			Element currentElement = (Element) child;

			String title = currentElement.getAttribute("title");
			String page = currentElement.getAttribute("page");
			String rawDest = currentElement.getAttribute("Dest");

			/**create the lookup table*/
			pageLookupTable.put(title, page);

			/**create the point lookup table*/
			if ((rawDest != null) && (rawDest.indexOf("/XYZ") != -1)) {

				rawDest = rawDest.substring(rawDest.indexOf("/XYZ") + 4);

				StringTokenizer values = new StringTokenizer(rawDest, "[] ");

				// ignore the first, read next 2
				// values.nextToken();
				String x = values.nextToken();
				if (x.equals("null")) {
					x = "0";
				}
				String y = values.nextToken();
				if (y.equals("null")) {
					y = "0";
				}

				pointLookupTable.put(title, new Point((int) Float.parseFloat(x), (int) Float.parseFloat(y)));
			}

			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(title);

			/**add the nodes or initialise to top level*/
			topNode.add(childNode);

			if (child.hasChildNodes()) {
				readChildNodes(child, childNode);
			}

		}
	}

	/**
	 *  put the outline data into a display panel which we can pop up
	 * for the user - outlines, thumbnails
	 */
	private void createOutlinePanels() {

		boolean hasNavBars = false;

		/**
		 * set up first 10 thumbnails by default. Rest created as needed.
		 */
		// add if statement or comment out this section to remove thumbnails
		if (showThumbnails) {
			hasNavBars = true;

			int pages = decode_pdf.getPageCount();

			// create dispaly for thumbnails
			thumbnailScrollPane.getViewport().add(thumbnailPanel);
			thumbnailPanel.setLayout(new GridLayout(pages, 1, 0, 10));
			thumbnailPanel.scrollRectToVisible(new Rectangle(0, 0, 1, 1));

			// if(showThumbnails)
			// <start-13>
			thumbnailPanel.addComponentListener(painter);
			// <end-13>
			navOptionsPanel.add(thumbnailScrollPane, "Thumbnails");
			thumbnailScrollPane.getVerticalScrollBar().setUnitIncrement(80);

			// create empty thumbnails and add to display

			// empty thumbnails for unloaded pages
			BufferedImage blankPortrait = createBlankThumbnail(thumbW, thumbH);
			BufferedImage blankLandscape = createBlankThumbnail(thumbH, thumbW);
			ImageIcon landscape = new ImageIcon(blankLandscape.getScaledInstance(-1, 70, BufferedImage.SCALE_SMOOTH));
			ImageIcon portrait = new ImageIcon(blankPortrait.getScaledInstance(-1, 100, BufferedImage.SCALE_SMOOTH));

			// page data so we can choose portrait or landscape
			PdfPageData pageData = decode_pdf.getPdfPageData();

			isLandscape = new boolean[pages];
			pageHeight = new int[pages];
			pageButton = new JButton[pages];
			buttonDrawn = new boolean[pages];
			Font textFont = new Font("Serif", Font.PLAIN, 12);

			for (int i = 0; i < pages; i++) {

				int page = i + 1;

				// create blank image with correct orientation
				final int pw, ph;
				int cropWidth = pageData.getCropBoxWidth(page);
				int cropHeight = pageData.getCropBoxHeight(page);
				int rotation = pageData.getRotation(page);
				ImageIcon usedLandscape, usedPortrait;

				if ((rotation == 0) | (rotation == 180)) {
					ph = (pageData.getMediaBoxHeight(page));
					pw = (pageData.getMediaBoxWidth(page)); // %%
					usedLandscape = landscape;
					usedPortrait = portrait;
				} else {
					ph = (pageData.getMediaBoxWidth(page));
					pw = (pageData.getMediaBoxHeight(page)); // %%
					usedLandscape = portrait;
					usedPortrait = landscape;
				}

				if (cropWidth > cropHeight) {

					pageButton[i] = new JButton("Page " + page, usedLandscape);
					isLandscape[i] = true;
					pageHeight[i] = ph; // w;%%
				} else {
					pageButton[i] = new JButton("Page " + page, usedPortrait);
					isLandscape[i] = false;
					pageHeight[i] = ph;
				}

				pageButton[i].setVerticalTextPosition(AbstractButton.BOTTOM);
				pageButton[i].setHorizontalTextPosition(AbstractButton.CENTER);
				if ((i == 0) && (pages > 1)) {
					pageButton[0].setBorder(BorderFactory.createLineBorder(Color.red));
				} else {
					pageButton[i].setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				}

				pageButton[i].setFont(textFont);
				thumbnailPanel.add(pageButton[i], BorderLayout.CENTER);

				// add listener so clicking on button changes to page
				pageButton[i].addActionListener(new PageChanger(i));

			}
		}

		/**
		 * add any outline
		 */
		if (decode_pdf.hasOutline()) {

			hasNavBars = true;

			DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");

			/**graphical display*/
			Node rootNode = decode_pdf.getOutlineAsXML().getFirstChild();

			if (rootNode != null) {
				readChildNodes(rootNode, top);
			}

			tree = new JTree(top);
			tree.setRootVisible(false);
			// tree.setsetExpandsSelectedPaths(true);

			// create dispaly for bookmarks
			bookmarkScrollPane.getViewport().add(tree);
			navOptionsPanel.add(bookmarkScrollPane, "Bookmarks");

			// Listen for when the selection changes - looks up dests at present
			tree.addTreeSelectionListener(new TreeSelectionListener() {

				/** Required by TreeSelectionListener interface. */
				public void valueChanged(TreeSelectionEvent e) {

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

					if (node == null) {
						return;
					}

					Object nodeInfo = node.getUserObject();

					/**get title and open page if valid*/
					String title = (String) node.getUserObject();
					String page = (String) pageLookupTable.get(title);

					if ((page != null) && (page.length() > 0)) {
						int pageToDisplay = Integer.parseInt(page);

						if ((!isProcessing) && (currentPage != pageToDisplay)) {
							currentPage = pageToDisplay;
							/**reset as rotation may change!*/
							scalingBox.setSelectedIndex(defaultSelection); // set to 100%
							decode_pdf.setPageParameters(1, currentPage);
							decodePage(false);
						}

						Point p = (Point) pointLookupTable.get(title);
						if (p != null) {
							decode_pdf.ensurePointIsVisible(p);
						}

					}
				}
			});

			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		}

		/**
		 * resize to show if there are nav bars
		 */
		if (hasNavBars) {
			if (!showThumbnails) {
				navOptionsPanel.setVisible(true);
				displayPane.setDividerLocation(170);
			}
		}
	}

	/**
	 *setup a thumbnail button in outlines
	 */
	private void addThumbnail(BufferedImage page, int i, boolean highLightThumbnail) {

		i--; // convert from page to array

		if (page != null) {
			/**add a border*/
			Graphics2D g2 = (Graphics2D) page.getGraphics();
			g2.setColor(Color.black);
			g2.draw(new Rectangle(0, 0, page.getWidth() - 1, page.getHeight() - 1));

			/**scale and refresh button*/
			ImageIcon pageIcon = new ImageIcon(page.getScaledInstance(-1, page.getHeight(), BufferedImage.SCALE_FAST));

			pageButton[i].setIcon(pageIcon);

			buttonDrawn[i] = true;

		}
	}

	/**
	 * remove outlines and flag for redraw
	 */
	private void removeOutlinePanels() {
		/**
		 * reset left hand nav bar
		 */
		thumbnailPanel.removeAll();
		bookmarkScrollPane.setMinimumSize(new Dimension(50, mainFrame.getHeight()));
		if (!showThumbnails) {
			navOptionsPanel.setVisible(false);
		}
		navOptionsPanel.removeAll();

		/**flag for redraw*/
		hasOutlinesDrawn = false;
	}

	/**
	 * create a blank tile with a cross to use as a thumbnail for unloaded page
	 */
	private BufferedImage createBlankThumbnail(int w, int h) {
		BufferedImage blank = new BufferedImage(w + 1, h + 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) blank.getGraphics();
		g2.setColor(Color.white);
		g2.fill(new Rectangle(0, 0, w, h));
		g2.setColor(Color.black);
		g2.draw(new Rectangle(0, 0, w, h));
		g2.drawLine(0, 0, w, h);
		g2.drawLine(0, h, w, 0);
		return blank;
	}

	class ProgressListener implements ActionListener {

		int count = 0;

		public void actionPerformed(ActionEvent evt) {

			setTitle("Processing page Count= " + (count >> 3));

			count++;
		}

	}

	class PageChanger implements ActionListener {

		int page;

		public PageChanger(int i) {
			i++;
			page = i;
		}

		public void actionPerformed(ActionEvent e) {
			if ((!isProcessing) && (currentPage != page)) {
				currentPage = page;
				scalingBox.setSelectedIndex(defaultSelection); // set to 100%
				decode_pdf.setPageParameters(1, currentPage);

				decodePage(false);

			}
		}
	}

	/**listener used to update display*/
	private class mouse_mover implements MouseMotionListener {

		public mouse_mover() {
		}

		public void mouseDragged(MouseEvent event) {

			int x = event.getX() - inset;
			int y = event.getY() - inset;

			// get raw co-ords and convert to correct scaled units
			if (rotation == 90) {

				m_y2 = (int) (x / scaling);
				m_x2 = (int) (y / scaling);

			} else if ((rotation == 180)) {

				m_x2 = max_x - (int) (x / scaling);
				m_y2 = (int) ((y) / scaling);

			} else if ((rotation == 270)) {

				m_y2 = max_y - (int) (x / scaling);
				m_x2 = max_x - (int) ((y) / scaling);

			} else {

				m_x2 = (int) (x / scaling);
				m_y2 = max_y - (int) (y / scaling);

			}

			// scroll if user hits side
			Rectangle visible_test = new Rectangle(event.getX(), event.getY(), 20, 20);
			if (!decode_pdf.getVisibleRect().contains(visible_test)) {
				decode_pdf.scrollRectToVisible(visible_test);
			}

			// redraw rectangle of dragged box onscreen if it has changed significantly
			if ((old_m_x2 != -1) | (old_m_y2 != -1) | (Math.abs(m_x2 - old_m_x2) > 5) | (Math.abs(m_y2 - old_m_y2) > 5)) {

				// allow for user to go up
				int top_x = m_x1;
				if (m_x1 > m_x2) {
					top_x = m_x2;
				}
				int top_y = m_y1;
				if (m_y1 > m_y2) {
					top_y = m_y2;
				}
				int w = Math.abs(m_x2 - m_x1);
				int h = Math.abs(m_y2 - m_y1);

				// add an outline rectangle to the display
				Rectangle currentRectangle = new Rectangle(top_x, top_y, w, h);

				decode_pdf.updateCursorBoxOnScreen(currentRectangle, Color.blue);

				// tell JPedal to highlight text in this area (you can add other areas to array)
				Rectangle[] highlightedAreas = new Rectangle[2];
				highlightedAreas[0] = currentRectangle;
				// highlightedAreas[1]=new Rectangle(10,10,100,100); - try me to see extra highlights
				decode_pdf.setHighlightedAreas(highlightedAreas);

				// reset trasckin
				old_m_x2 = m_x2;
				old_m_y2 = m_y2;

			}

			updateCords(event);
			checkLinks(false);

		}

		public void mouseMoved(MouseEvent event) {
			updateCords(event);
			checkLinks(false);
		}

	}

	/**
	 * picks up clicks so we can draw an outline on screen
	 */
	private class mouse_clicker extends MouseAdapter {

		// user has pressed mouse button so we want to use this
		// as one point of outline
		public void mousePressed(MouseEvent event) {

			// get co-ordinates of top point of outine rectangle

			int x = event.getX() - inset;
			int y = event.getY() - inset;

			if (rotation == 90) {

				m_y1 = (int) (x / scaling);
				m_x1 = (int) (y / scaling);

			} else if ((rotation == 180)) {

				m_x1 = max_x - (int) (x / scaling);
				m_y1 = (int) (y / scaling);

			} else if ((rotation == 270)) {

				m_y1 = max_y - (int) (x / scaling);
				m_x1 = max_x - (int) (y / scaling);

			} else {

				m_x1 = (int) (x / scaling);
				m_y1 = max_y - (int) (y / scaling);

			}

			updateCords(event);

		}

		// show the description in the text box or update screen
		public void mouseClicked(MouseEvent event) {
			checkLinks(true);
		}

		// user has stopped clicking so we want to remove the outline rectangle
		public void mouseReleased(MouseEvent event) {

			/** remove any outline and reset variables used to track change */
			decode_pdf.updateCursorBoxOnScreen(null, null); // remove box
			decode_pdf.setHighlightedAreas(null); // remove highlighted text

			decode_pdf.repaintArea(new Rectangle(m_x1, m_y2, m_x2 - m_x1, (m_y1 - m_y2)), max_y); // redraw

			old_m_x2 = -1;
			old_m_y2 = -1;

			updateCords(event);

		}
	}

	/**checks the link areas on the page for mouse entering. Provides
	 * option to behave differently on mouse click. Note code will not check
	 * multiple links only first match.
	 * */
	public void checkLinks(boolean mouseClicked) {

		message = "";

		// get hotspots for the page
		Rectangle[] hotSpots = decode_pdf.getPageHotspots();

		if (hotSpots != null) {
			int count = hotSpots.length;
			int matchFound = -1;

			// look for first match
			for (int i = 0; i < count; i++) {
				if ((hotSpots[i] != null) && (hotSpots[i].contains(cx, cy))) {
					matchFound = i;
					i = count;
				}
			}

			/**action for moved over of clicked*/
			if (matchFound != -1) {

				if (mouseClicked) {

					// get values in Annotation
					Map annotDetails = this.pageAnnotations.getAnnotRawData(matchFound);

					Map annotAction = (Map) annotDetails.get("A");

					String subtype = pageAnnotations.getAnnotSubType(matchFound);

					if ((subtype.equals("Link")) && (annotAction != null)) {
						Iterator keys = annotAction.keySet().iterator();

						// just build a display
						JPanel details = new JPanel();
						// <start-13>
						details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
						// <end-13>
						Font textFont = new Font("Serif", Font.PLAIN, 12);

						while (keys.hasNext()) {
							String nextKey = (String) keys.next();
							details.add(new JLabel(nextKey + " : " + annotDetails.get(nextKey)));
						}

						JOptionPane.showMessageDialog(mainFrame, details, "Annotation Properties", JOptionPane.PLAIN_MESSAGE);

					} else if (subtype.equals("Text")) {

						String title = pageAnnotations.getField(matchFound, "T");
						if (title == null) {
							title = "No title";
						}
						String contents = pageAnnotations.getField(matchFound, "Contents");
						if (contents == null) {
							contents = "No Contents";
						}
						JOptionPane.showMessageDialog(mainFrame, new TextArea(contents), title, JOptionPane.PLAIN_MESSAGE);

					} else if (subtype.equals("FileAttachment")) { // saves file (Adobe default is to open the file,
																	// but Java does not have a simpel open command.

						// drill down to file held as binary stream
						Map fileDetails = (Map) annotDetails.get("FS");
						if (fileDetails != null) {
							fileDetails = (Map) fileDetails.get("EF");
						}
						if (fileDetails != null) {
							fileDetails = (Map) fileDetails.get("F");
						}

						if (fileDetails != null) {
							byte[] file = (byte[]) fileDetails.get("DecodedStream");

							if (file == null) {
								JOptionPane.showMessageDialog(mainFrame, "No file embedded");
							} else {
								/**
								 * create the file chooser to select the file name
								 */
								JFileChooser chooser = new JFileChooser(inputDir);
								chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

								int state = chooser.showSaveDialog(mainFrame);
								if (state == 0) {
									File fileTarget = chooser.getSelectedFile();
									FileOutputStream fos;
									try {
										fos = new FileOutputStream(fileTarget);
										fos.write(file);
										fos.close();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}

					} else { // type not yet implemented so just display details

						JPanel details = new JPanel();
						details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
						Iterator keys = annotDetails.keySet().iterator();
						while (keys.hasNext()) {
							String nextKey = (String) keys.next();
							details.add(new JLabel(nextKey + " : " + annotDetails.get(nextKey)));
						}

						JOptionPane.showMessageDialog(mainFrame, details, "Unimplemented subtype " + subtype, JOptionPane.PLAIN_MESSAGE);
					}
				} else {
					message = " Entered link " + matchFound;
				}
			}
		}
	}

	/**method used to display current page co-ordinates*/
	public void updateCords(MouseEvent event) {
		// update box showing co-ords on screen

		if (rotation == 90) {

			cy = (int) (event.getX() / scaling);
			cx = (int) (event.getY() / scaling);

		} else if ((rotation == 180)) {

			cx = max_x - (int) (event.getX() / scaling);
			cy = (int) ((event.getY()) / scaling);

		} else if ((rotation == 270)) {

			cy = max_y - (int) (event.getX() / scaling);
			cx = max_x - (int) ((event.getY()) / scaling);

		} else {

			cx = (int) (event.getX() / scaling);
			cy = max_y - (int) (event.getY() / scaling);

		}

		// add any inset
		cx = min_x + cx - (int) (inset / scaling);
		cy = cy + (int) (inset / scaling);

		coords.setText("  X: " + cx + " Y: " + cy + " " + " " + message);

		// scroll if user hits side and shift key not pressed
		if (!event.isShiftDown()) {
			Rectangle visible_test = new Rectangle(event.getX() - 50, event.getY() - 50, 100, 100);
			if (!decode_pdf.getVisibleRect().contains(visible_test)) {
				decode_pdf.scrollRectToVisible(visible_test);
			}
		}
	}

	// <start-13>
	/** class to paint thumbnails */
	private class ThumbPainter extends ComponentAdapter {

		/** used to track user stopping movement */
		Timer trapMultipleMoves = trapMultipleMoves = new Timer(250, new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				if (worker != null) {
					worker.interrupt();
				}

				/** if running wait to finish */
				while (isProcessing) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// should never be called
						e.printStackTrace();
					}
				}

				// create the thread to just do the thumbnails
				worker = new SwingWorker() {
					@Override
                    public Object construct() {

						try {
							if (Thread.interrupted()) {
								throw new InterruptedException();
							}

							Rectangle rect = thumbnailPanel.getVisibleRect();
							int pages = decode_pdf.getPageCount();

							for (int i = 0; i < pages; i++) {

								if (Thread.interrupted()) {
									throw new InterruptedException();
								}

								if ((i > 0) && (!buttonDrawn[i]) && (pageButton[i] != null) && (rect.intersects(pageButton[i].getBounds()))) {

									int h = thumbH;
									if (isLandscape[i]) {

										h = thumbW;
									}
									float scaleFactor = (float) h / (float) pageHeight[i];

									BufferedImage page = decode_pdf.getPageAsThumbnail(i + 1, h);

									if (Thread.interrupted()) {
										throw new InterruptedException();
									}

									addThumbnail(page, i + 1, false);
								}
							}

						} catch (Exception e) {
							// stopped thumbnails
						}

						return null;
					}
				};

				worker.start();
			}
		});

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
		 */
		public void componentMoved(ComponentEvent e) {

			// allow us to disable on scroll
			if (trapMultipleMoves.isRunning()) {
				trapMultipleMoves.stop();
			}

			trapMultipleMoves.setRepeats(false);
			trapMultipleMoves.start();

		}
	}

	// <end-13>

	/**create a drop down menu and icons at top.
	 * Also add actions to commands
	 */
	private void createMenuAndToolBar() {

		/**
		 * set colours on display boxes and add listener to page number
		 */
		pageCounter1.setEditable(false);
		pageCounter2.setEditable(true);
		pageCounter2.setToolTipText("To go to a page - Type in page number and press return");
		pageCounter2.setBorder(BorderFactory.createLineBorder(Color.black));
		pageCounter2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				String value = (String) pageCounter2.getText().trim();
				int newPage;

				// allow for bum values
				try {
					newPage = Integer.parseInt(value);

					if ((newPage > decode_pdf.getPageCount()) | (newPage < 1)) {
						JOptionPane.showMessageDialog(mainFrame, "Page " + value + " is not in range. Document has " + decode_pdf.getPageCount()
						                                         + " pages.");
						newPage = currentPage;
						pageCounter2.setText("" + currentPage);
					}

				} catch (Exception e) {
					JOptionPane.showMessageDialog(mainFrame, "Value >" + value + "< is not valid. Please use a number (ie 4)");
					newPage = currentPage;
					pageCounter2.setText("" + currentPage);
				}

				// open new page
				if ((!isProcessing) && (currentPage != newPage)) {
					currentPage = newPage;
					decodePage(false);
				}
			}

		});
		pageCounter3.setEditable(false);

		scalingBox.setBackground(Color.white);
		scalingBox.setSelectedIndex(3); // set default before we add a listener

		rotationBox.setBackground(Color.white);
		rotationBox.setSelectedIndex(0); // set default before we add a listener

		/**
		 * create a menu bar and add to display
		 */
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		this.add(top, BorderLayout.NORTH);

		// track and display screen co-ordinates and support links
		decode_pdf.addMouseMotionListener(new mouse_mover());
		decode_pdf.addMouseListener(new mouse_clicker());

		/**
		 * add values to menu and set actions
		 */
		JMenuBar currentMenu = new JMenuBar();
		JMenu file = new JMenu("File");
		// JMenu help = new JMenu("Help");
		currentMenu.add(file);
		// currentMenu.add(help);
		top.add(currentMenu, BorderLayout.NORTH);

		/**open file option*/
		/*
		 * JMenuItem Open = new JMenuItem("Open"); Open.setToolTipText("Open a file"); Open.addActionListener(new
		 * ActionListener() { public void actionPerformed(ActionEvent e) { if (printingThreads > 0) {
		 * JOptionPane.showMessageDialog(mainFrame, "Please wait for printing to finish"); } else if (isProcessing) {
		 * JOptionPane.showMessageDialog(mainFrame, "Please wait for page to display"); } else { // selectFile(); }
		 *  }
		 * 
		 * }); file.add(Open);
		 */
		/**show info option*/
		/*
		 * JMenuItem info = new JMenuItem("About"); info.setToolTipText("Message about library");
		 * info.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
		 * 
		 * JOptionPane.showMessageDialog( mainFrame, "Simple demo of JPedal as a GUI component - see source for advice
		 * on enhancements and options"); } }); help.add(info);
		 */

		/**show fonts used option*/
		JMenuItem fonts = new JMenuItem("Fonts");
		fonts.setToolTipText("Fonts used on displayed page");
		fonts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JOptionPane.showMessageDialog(mainFrame, PdfStreamDecoder.getFontsInFile(), "List of Fonts used on Page", JOptionPane.PLAIN_MESSAGE);
			}
		});
		file.add(fonts);

		/**show Document info*/
		JMenuItem props = new JMenuItem("Document Properties");
		props.setToolTipText("Document Properties");
		props.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PdfFileInformation currentFileInformation = decode_pdf.getFileInformationData();

				/**get the Pdf file information object to extract info from*/
				if (currentFileInformation != null) {

					JPanel details = new JPanel();
					// <start-13>
					details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));
					// <end-13>

					Font textFont = new Font("Serif", Font.PLAIN, 12);
					Font headFont = new Font("SansSerif", Font.BOLD, 14);

					// general details
					JLabel header1 = new JLabel("General");
					header1.setFont(headFont);
					details.add(header1);

					/*
					 * JLabel g1 = new JLabel("File name : " + selectedFile); g1.setFont(textFont); details.add(g1);
					 */

					JLabel g2 = new JLabel("File path : " + inputDir);
					g2.setFont(textFont);
					details.add(g2);

					JLabel g3 = new JLabel("File size : " + size + " K");
					g3.setFont(textFont);
					details.add(g3);

					JLabel g4 = new JLabel("Page Count : " + pageCount);
					g4.setFont(textFont);
					details.add(g4);

					details.add(Box.createRigidArea(new Dimension(0, 5)));

					// general details
					JLabel header2 = new JLabel("Properties");
					header2.setFont(headFont);
					details.add(header2);

					// get the document properties
					String[] values = currentFileInformation.getFieldValues();
					String[] fields = currentFileInformation.getFieldNames();

					// add to list and display
					int count = fields.length;

					JLabel[] displayValues = new JLabel[count];

					for (int i = 0; i < count; i++) {
						if (values[i].length() > 0) {
							displayValues[i] = new JLabel(fields[i] + " = " + values[i]);
							displayValues[i].setFont(textFont);
							details.add(displayValues[i]);
						}
					}

					details.add(Box.createRigidArea(new Dimension(0, 5)));

					String xmlText = currentFileInformation.getFileXMLMetaData();
					if (xmlText.length() > 0) {

						JLabel header3 = new JLabel("XML metadata");
						header3.setFont(headFont);
						details.add(header3);

						JTextArea xml = new JTextArea();
						xml.setRows(5);
						xml.setColumns(15);
						xml.setLineWrap(true);
						xml.setText(xmlText);
						details.add(new JScrollPane(xml));
						xml.setCaretPosition(0);

						details.add(Box.createRigidArea(new Dimension(0, 5)));
					}

					JOptionPane.showMessageDialog(mainFrame, details, "Document Properties", JOptionPane.PLAIN_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(mainFrame, "No file data available", "Document Properties", JOptionPane.PLAIN_MESSAGE);
				}

			}
		});
		file.add(props);

		/**show Document info*/
		JMenuItem psize = new JMenuItem("Page Size");
		psize.setToolTipText("Current page size");
		psize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PdfPageData currentPageSize = decode_pdf.getPdfPageData();

				/**get the Pdf file information object to extract info from*/
				if (currentPageSize != null) {

					JPanel details = new JPanel();
					// <start-13>
					details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));
					// <end-13>

					Font textFont = new Font("Serif", Font.PLAIN, 12);
					Font headFont = new Font("SansSerif", Font.BOLD, 14);

					// general details
					JLabel header1 = new JLabel("Media Box");
					header1.setFont(headFont);
					details.add(header1);

					JLabel g1 = new JLabel("Media Box: " + currentPageSize.getMediaValue(currentPage));
					g1.setFont(textFont);
					details.add(g1);

					JLabel g2 = new JLabel("Crop Box : " + currentPageSize.getCropValue(currentPage));
					g2.setFont(textFont);
					details.add(g2);

					JLabel g3 = new JLabel("Rotation : " + currentPageSize.getRotation(currentPage));
					g3.setFont(textFont);
					details.add(g3);

					details.add(Box.createRigidArea(new Dimension(0, 5)));

					JOptionPane.showMessageDialog(mainFrame, details, "Current Page Size", JOptionPane.PLAIN_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(mainFrame, "No file data available", "Current Page Size", JOptionPane.PLAIN_MESSAGE);
				}

			}
		});
		file.add(psize);

		/**Print option*/
		JMenuItem print = new JMenuItem("Print");
		file.add(print);
		print.setToolTipText("Print using JavaAPI");
		print.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				/**the Java tutorial from Sun has some nice examples of
				 * setting up printing under Java and some performance tips
				 */

				// provides atomic flag on printing so we don't exit until all done
				printingThreads++;

				/**printing in thread to improve background printing
				 * - comment out if not required*/
				Thread worker = new Thread() {
					public void run() {

						try {

							// setup print job and objects
							PrinterJob printJob = PrinterJob.getPrinterJob();
							PageFormat pf = printJob.defaultPage();

							// Set PageOrientation to best use page layout
							int orientation = decode_pdf.getPDFWidth() < decode_pdf.getPDFHeight() ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE;
							pf.setOrientation(orientation);

							/**
							 * Create default page format A4 - you may wish to change this for your printer
							 */
							Paper paper = new Paper();
							paper.setSize(595, 842);
							// Set margins
							paper.setImageableArea(43, 43, 509, 756);
							// paper.setImageableArea(0, 0, 595, 842); no margins if printer supports it
							pf.setPaper(paper);

							// very useful for debugging! (shows the imageable area as a green box bordered by a
							// rectangle)
							// decode_pdf.showImageableArea();

							/**SERVERSIDE printing
							 *IF you wish to print using a server, do the following

							 1. Delete the loop
							 ==================

							 if(printJob.printDialog()){
							 }

							 2. Use this alternative code (1.4 only)
							 ============================

							         //Set page orientation on printer
							 PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
							 if (pf.getOrientation() == PageFormat.PORTRAIT)
							 attributeSet.add(OrientationRequested.PORTRAIT);
							               else
							 attributeSet.add(OrientationRequested.LANDSCAPE);

							 decode_pdf.setPagePrintRange(startPage,endPage);
							 for(int page=startPage;page<endPage+1;page++)
							 decode_pdf.setPageFormat(page,pf); //change to suit - sets format for each page
							               printJob.setPageable(decode_pdf);
							               printJob.print(attributeSet);

							 */

							// allow user to edit settings and select printing
							printJob.setPrintable(decode_pdf, pf);

							if (printJob.printDialog()) {

								// <start-13>
								// Set page orientation on printer in 1.4
								PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
								if (pf.getOrientation() == PageFormat.PORTRAIT) {
									attributeSet.add(OrientationRequested.PORTRAIT);
								} else {
									attributeSet.add(OrientationRequested.LANDSCAPE);
								}
								// <end-13>

								/**tell users about printing*/
								JTextArea message =
								    new JTextArea(
								                  "Print Details\n=============\n"
								                          + "\nPaper size\nwidth="
								                          + paper.getWidth()
								                          + " height="
								                          + paper.getHeight()
								                          + "\nMargins="
								                          + paper.getImageableX()
								                          + " "
								                          + paper.getImageableY()
								                          + " "
								                          + paper.getImageableWidth()
								                          + " "
								                          + paper.getImageableHeight()
								                          + "\n\nUseful tips"
								                          + "\n===========\n"
								                          + "\nYour printer may use different values"
								                          + "\nPages will be scaled if larger than page to fit"
								                          + "\nPLEASE look at SimpleViewer sample code for example code for printing\nincluding servderside and without a print dialog  \n\n");
								message.setColumns(30);
								message.setWrapStyleWord(true);
								JOptionPane.showMessageDialog(mainFrame, message);

								// <start-13>
								/**
								 * title changes to give user something to see under timer
								 * control
								 */
								ActionListener listener = new ProgressListener();
								Timer t = new Timer(250, listener);
								t.start(); // start it
								// <end-13>

								// Print PDF document
								// <start-13>
								printJob.print(attributeSet);

								// <end-13>
								// <start-13>
								/**
								          //<end-13>
								          printJob.print();
								 /***/

								// <start-13>
								t.stop();
								setTitle(null);
								// <end-13>
							}

						} catch (Exception ee) {
							LogWriter.writeLog("Exception " + ee + " printing");
						} catch (Error err) {
							LogWriter.writeLog("Error " + err + " printing");
						}

						printingThreads--;

						// redraw to clean up
						decode_pdf.invalidate();
						decode_pdf.updateUI();
						mainFrame.repaint();
						// internalFrame.invalidate();
						// internalFrame.updateUI();
						// internalFrame.repaint();
						JOptionPane.showMessageDialog(mainFrame, "finished printing");
					}
				};

				// start printing in background (comment out if not required)
				worker.start();

			}
		});
		/**
		 * create tool bars and add to display
		 */
		JToolBar currentBar1 = new JToolBar();
		currentBar1.setBorder(BorderFactory.createEmptyBorder());
		currentBar1.setLayout(new FlowLayout(FlowLayout.LEADING));
		currentBar1.setFloatable(false);
		currentBar1.setFont(new Font("SansSerif", Font.PLAIN, 8));

		JToolBar currentBar2 = new JToolBar();
		currentBar2.setBorder(BorderFactory.createEmptyBorder());
		currentBar2.setLayout(new FlowLayout(FlowLayout.LEADING));
		currentBar2.setFloatable(false);
		currentBar2.setFont(new Font("SansSerif", Font.PLAIN, 8));

		/**open icon*/
		URL current_image = getClass().getClassLoader().getResource("org/jpedal/examples/simpleviewer/res/open.gif");
		// JButton open = new JButton("open");
		// open.setIcon(new ImageIcon(current_image));
		// open.setToolTipText("open");
		// currentBar1.add(open);
		// open.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		/**if you require the ability to open files while printing you should
		 * create a new PdfDecoder instance each time
		 */
		/*
		 * if (printingThreads > 0) { JOptionPane.showMessageDialog(mainFrame, "Please wait for printing to finish"); }
		 * else if (isProcessing) { JOptionPane.showMessageDialog(mainFrame, "Please wait for page to display"); } else { //
		 * selectFile(); } } });
		 */
		currentBar1.add(Box.createHorizontalGlue());

		/**back to page 1*/
		URL startImage = getClass().getClassLoader().getResource("org/jpedal/examples/simpleviewer/res/start.gif");
		JButton start = new JButton();
		start.setIcon(new ImageIcon(startImage));
		start.setToolTipText("Rewind to page 1");
		currentBar1.add(start);
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (selectedFile != null) /**%%*/
				// {
				back(currentPage - 1);
				// }
			}
		});

		/**back 10 icon*/
		URL fbackImage = getClass().getClassLoader().getResource("org/jpedal/examples/simpleviewer/res/fback.gif");
		JButton fback = new JButton("-10");
		fback.setIcon(new ImageIcon(fbackImage));
		fback.setToolTipText("Rewind 10 pages");
		currentBar1.add(fback);
		fback.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (selectedFile != null) /**%%*/
				// {
				back(10);
				// }
			}
		});

		/**back icon*/
		URL backImage = getClass().getClassLoader().getResource("org/jpedal/examples/simpleviewer/res/back.gif");
		JButton back = new JButton("-1");
		back.setIcon(new ImageIcon(backImage));
		back.setToolTipText("Rewind one page");
		currentBar1.add(back);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (selectedFile != null) /**%%*/
				// {
				back(1);
				// }
			}
		});

		/**put page count in middle of forward and back*/
		currentBar1.add(pageCounter1);
		currentBar1.add(pageCounter2);
		currentBar1.add(pageCounter3);

		/**forward icon*/
		URL fowardImage = getClass().getClassLoader().getResource("org/jpedal/examples/simpleviewer/res/forward.gif");
		JButton forward = new JButton("+1");
		forward.setIcon(new ImageIcon(fowardImage));
		forward.setToolTipText("forward 1 page");
		currentBar1.add(forward);
		forward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (selectedFile != null) /**%%*/
				// {
				forward(1);
				// }
			}
		});

		/**fast forward icon*/
		URL ffowardImage = getClass().getClassLoader().getResource("org/jpedal/examples/simpleviewer/res/fforward.gif");
		JButton fforward = new JButton("+10");
		fforward.setIcon(new ImageIcon(ffowardImage));
		fforward.setToolTipText("Fast forward 10 pages");
		currentBar1.add(fforward);
		fforward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (selectedFile != null) /**%%*/
				// {
				forward(10);
				// }
			}
		});

		/**goto last page*/
		URL endImage = getClass().getClassLoader().getResource("org/jpedal/examples/simpleviewer/res/end.gif");
		JButton end = new JButton();
		end.setIcon(new ImageIcon(endImage));
		end.setToolTipText("Fast forward to last page");
		currentBar1.add(end);
		end.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (selectedFile != null) /**%%*/
				// {
				forward(pageCount - currentPage);
				// }
			}
		});

		/**add a gap in display*/
		currentBar1.add(Box.createHorizontalGlue());

		/**zoom in*/
		JLabel plus = new JLabel("Scaling");
		currentBar2.add(plus);
		plus.setToolTipText("zoom in");
		currentBar2.add(scalingBox);
		scalingBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (!isProcessing) {
					// if (selectedFile != null) {
					zoom();
					// }
				}
			}
		});

		/**add a gap in display*/
		currentBar2.add(Box.createHorizontalGlue());

		/**page rotation option*/
		JLabel rotation = new JLabel("Rotation");
		rotation.setToolTipText("Rotation in degrees");
		currentBar2.add(rotation);
		currentBar2.add(rotationBox);
		rotationBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (selectedFile != null) { /**%%*/
				decode_pdf.setPageRotation(Integer.parseInt((String) rotationBox.getSelectedItem()));
				decode_pdf.repaint();
				// }
				/**%%*/
			}
		});

		// and initialise the display

		/**add cursor location*/
		JToolBar cursor = new JToolBar();
		cursor.setBorder(BorderFactory.createEmptyBorder());
		cursor.setLayout(new FlowLayout(FlowLayout.LEADING));
		cursor.setFloatable(false);
		cursor.setFont(new Font("SansSerif", Font.ITALIC, 10));
		cursor.add(new JLabel("Cursor at:"));
		currentBar2.add(Box.createHorizontalGlue());
		cursor.add(this.coords);

		currentBar2.add(cursor);

		/**add toolbars to display*/
		top.add(currentBar2, BorderLayout.SOUTH);
		top.add(currentBar1, BorderLayout.CENTER);

	}

	/**
	 * set title or over-ride with message
	 */
	private void setTitle(final String title) {

		/*
		 * if (title != null) { mainFrame.setTitle(title); } else { mainFrame.setTitle("JPedal GUI Open Source version (
		 * version " + PdfDecoder.version + " ) " ); }
		 */
	}

	/**
	 *
	 */
	public void openFile(byte[] selectedFile) {

		/** reset default values */
		scaling = (float) 1.0;
		scalingBox.setSelectedIndex(5); // set to 100%

		decode_pdf.closePdfFile();

		/** ensure all data flushed from PdfDecoder before we decode the file */
		// decode_pdf.flushObjectValues(true);
		try {

			/** opens the pdf and reads metadata */
			decode_pdf.openPdfArray(selectedFile);

			boolean fileCanBeOpened = true;

			/** flag up if encryption present */

			/** popup window if needed */
			if ((decode_pdf.isEncrypted()) && (!decode_pdf.isFileViewable())) {
				fileCanBeOpened = false;

				// <start-13>
				/**
				 * //<end-13>JOptionPane.showMessageDialog(mainFrame,"Please
				 * use Java 1.4 to display encrypted files"); //<start-13>
				 */

				String password = JOptionPane.showInputDialog(mainFrame, "Please enter a password is required to display encrypted files");

				/** try and reopen with new password */
				if (password != null) {
					decode_pdf.setEncryptionPassword(password);

					if (decode_pdf.isFileViewable()) {
						fileCanBeOpened = true;
					}

				}
				// mainFrame.repaint();
				// <end-13>
			}

			if (!fileCanBeOpened) {
				// <start-13>
				JOptionPane.showMessageDialog(mainFrame, "A valid password is required to display encrypted files");
				// <end-13>

			} else {

				// update count
				currentFileCount++;

				removeOutlinePanels();

				/** reset values */
				currentPage = 1;
				pageCount = decode_pdf.getPageCount();
				// actual number of pages
				decode_pdf.setPageParameters(1, currentPage); // must be called
				// AFTER
				// setExtractionMode
				// to work

				// resize (ensure at least certain size)
				/*
				 * if (!showThumbnails) { int w = decode_pdf.getPDFWidth(), h = decode_pdf .getPDFHeight(); if (w < 100) {
				 * w = 100; } if (h < 100) { h = 100; }
				 * 
				 * mainFrame.setSize(w, h); }
				 */

				// add a border
				decode_pdf.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));

				/** turn off border in printing */
				decode_pdf.disableBorderForPrinting();

				/**
				 * update the display, including any rotation
				 */
				pageCounter2.setForeground(Color.black);
				pageCounter2.setText(" " + currentPage);
				pageCounter3.setText(" of " + pageCount);

				resetRotationBox();

				decodePage(true);

			}

		} catch (Exception e) {
			System.err.println("Exception " + e + " opening file");
			JOptionPane.showMessageDialog(mainFrame,
			                              "This file generated an exception and cannot continue. Please send file to IDRsolutions for analysis");
			System.exit(1);
			isProcessing = false;
		}
	}

	/**
	 * align rotation combo box to default for page
	 */
	private void resetRotationBox() {
		PdfPageData currentPageData = decode_pdf.getPdfPageData();
		rotation = currentPageData.getRotation(currentPage);
		rotationBox.setSelectedIndex(rotation / 90);
	}

	/** zoom into page */
	public void zoom() {

		if (decode_pdf != null) {

			/** update value and GUI */
			int index = scalingBox.getSelectedIndex();
			if (index < 2) { // handle scroll to width/height

				float width = scrollPane.getVisibleRect().width - inset - inset;
				float height = scrollPane.getVisibleRect().height - inset - inset;
				float x_factor = 0, y_factor = 0;
				x_factor = width / max_x;
				y_factor = height / max_y;

				if (index == 0) {
					scaling = y_factor;
				} else if (index == 1) {
					scaling = x_factor;
				}
			} else {
				scaling = scalingFloatValues[index];
			}

			/**update our components*/
			resetRotationBox();

			if (decode_pdf != null) { // allow for clicking on it before page opened
				decode_pdf.setPageParameters(scaling, currentPage);
			}

			// decode_pdf.invalidate();
			decode_pdf.repaint();
			// mainFrame.validate();
		}

	}

	/**move forward one page*/
	public void forward(int count) {
		if (!isProcessing) { // lock to stop multiple accesses

			/**if in range update count and decode next page. Decoded pages are cached so will redisplay
			 * almost instantly*/
			if (currentPage + count <= pageCount) {
				currentPage += count;

				/**reset as rotation may change!*/
				decode_pdf.setPageParameters(1, currentPage);
				scalingBox.setSelectedIndex(defaultSelection); // set to 100%
				// pass vales to PdfDecoder

				// decode the page
				decodePage(false);

			}
		} else {
			JOptionPane.showMessageDialog(mainFrame, "Please wait for page to display");
		}
	}

	private void decodePage(final boolean resizePanel) {

		decode_pdf.clearScreen();

		/**if running terminate first*/
		if ((isProcessing) | (worker != null)) {
			worker.interrupt();
		}

		isProcessing = true;

		worker = new SwingWorker() {
			public Object construct() {

				// <start-13>
				/**
				 * title changes to give user something to see under timer
				 * control
				 */
				ActionListener listener = new ProgressListener();
				Timer t = new Timer(250, listener);
				t.start(); // start it
				// <end-13>

				try {

					/**
					 * add outline if appropriate in a scrollbar on the left to
					 * replicate L & F or Acrobat
					 */
					if (!hasOutlinesDrawn) {
						hasOutlinesDrawn = true;
						createOutlinePanels();
					}

					/**
					 * make sure screen fits display nicely
					 */
					/*
					 * if ((resizePanel) && (showThumbnails)) { //resize (ensure at least certain size) int w =
					 * decode_pdf.getPDFWidth(), h = decode_pdf .getPDFHeight(); if (w < 100) { w = 100; } if (h < 100) {
					 * h = 100; }
					 * 
					 * int fx = mainFrame.getLocationOnScreen().x; int fy = mainFrame.getLocationOnScreen().y; int fw =
					 * mainFrame.getWidth(); int fh = mainFrame.getHeight(); Dimension d = Toolkit.getDefaultToolkit()
					 * .getScreenSize(); if (d.width < (fx + w + 200)) { w = d.width - fx - 250; } if (d.height < (fy +
					 * h)) { h = d.height - fy - 50; } mainFrame.setSize(w + 200, h); }
					 */

					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					/**
					 * decode the page
					 */
					try {
						decode_pdf.decodePage(currentPage);

						// read values for page display
						PdfPageData page_data = decode_pdf.getPdfPageData();

						max_x = page_data.getMediaBoxWidth(currentPage) + page_data.getMediaBoxX(currentPage);
						max_y = page_data.getMediaBoxHeight(currentPage) + page_data.getMediaBoxY(currentPage);

						resetRotationBox();

						// read annotations data
						pageAnnotations = decode_pdf.getPdfAnnotsData(null);

					} catch (Exception e) {
						System.err.println("Exception " + e + " decoding page");
						e.printStackTrace();
					}

					pageCounter2.setForeground(Color.black);
					pageCounter2.setText(" " + currentPage);
					pageCounter3.setText(" of " + pageCount);

					if ((showThumbnails)) {

						// if not drawn get page and flag
						if ((buttonDrawn[currentPage - 1] == false)) {

							int h = thumbH;
							if (isLandscape[currentPage - 1]) {
								h = thumbW;
							}

							BufferedImage page = decode_pdf.getPageAsThumbnail(currentPage, h);

							addThumbnail(page, currentPage, true);
						}
					} else {
						isProcessing = false;
					}

					// make sure fully drawn
					// decode_pdf.repaint();

					// <start-13>
					t.stop();
					setTitle(null); // restore title
					// <end-13>

					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					if (showThumbnails) {

						int count = decode_pdf.getPageCount();

						for (int i1 = 0; i1 < count; i1++) {
							if ((i1 != currentPage - 1)) {
								pageButton[i1].setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
							}

						}

						// set button and scroll to
						if ((count > 1) && (currentPage > 0)) {

							pageButton[currentPage - 1].setBorder(BorderFactory.createLineBorder(Color.red));

						}

						// update thumbnail pane if needed
						if (showThumbnails) {
							Rectangle rect = thumbnailPanel.getVisibleRect();

							if (!rect.contains(pageButton[currentPage - 1].getBounds())) {

								SwingUtilities.invokeAndWait(new Runnable() {

									public void run() {
										// decode_pdf.repaint();
										thumbnailPanel.scrollRectToVisible(pageButton[currentPage - 1].getBounds());
										// thumbnailPanel.repaint();

									}
								});

							}
						}

						isProcessing = false;
						/**draw thumbnails in background*/

						int pages = decode_pdf.getPageCount();

						for (int j = -5; j < 5; j++) {

							int i = j + currentPage;

							if (Thread.interrupted()) {
								throw new InterruptedException();
							}

							if (i >= pages) {
								j = 5;
							} else if ((i > 0) && (!buttonDrawn[i]) && (pageButton[i] != null)) {

								int h = thumbH;
								if (isLandscape[i]) {
									h = thumbW;
								}

								BufferedImage page = decode_pdf.getPageAsThumbnail(i + 1, h);

								if (Thread.interrupted()) {
									throw new InterruptedException();
								}

								addThumbnail(page, i + 1, false);

								if (Thread.interrupted()) {
									throw new InterruptedException();
								}

							}
						}
					}

				} catch (Exception e) {
					// System.err.println("Stopped " + e + " thread decoding page");
					// e.printStackTrace();
					// <start-13>
					t.stop();
					setTitle(null); // restore title
					// <end-13>

					isProcessing = false;

				}

				return null;
			}
		};

		worker.start();
	}

	/** move back one page */
	public void back(int count) {

		if (!isProcessing) { // lock to stop multiple accesses

			/**
			 * if in range update count and decode next page. Decoded pages are
			 * cached so will redisplay almost instantly
			 */
			if (currentPage - count >= 1) {
				currentPage = currentPage - count;

				/** reset as rotation may change! */
				decode_pdf.setPageParameters(1, currentPage);
				scalingBox.setSelectedIndex(defaultSelection); // set to 100%
				// pass vales to PdfDecoder
				decodePage(false);

			}
		} else {
			JOptionPane.showMessageDialog(mainFrame, "Please wait for page to display");
		}
	}
}
