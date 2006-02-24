/*
 * Created on 1/11/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesDialog;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.util.TableSorter;
import au.gov.naa.digipres.xena.util.logging.LogFrame;
import au.gov.naa.digipres.xena.util.logging.LogFrameHandler;

/**
 * Main Frame for Xena Lite application. Usage in a nutshell:
 * The user adds files or directories
 * to the list of items to be normalised, and then clicks the Normalise
 * button to perform normalisation. Results are displayed in a table.
 * 
 * @author justinw5
 * created 6/12/2005
 * xena
 * Short desc of class:
 */
public class LiteMainFrame extends JFrame
	implements NormalisationStateChangeListener
{
	private static final String XENA_LITE_VERSION = "0.2";
	private static final String XENA_LITE_TITLE = 
		"Xena Lite (version " + XENA_LITE_VERSION + ")";

	// Preferences keys
	private static final String LAST_DIR_VISITED_KEY = "dir/lastvisited";
	private static final String PLUGIN_DIR_KEY = "dir/plugin";
	private static final String XENA_DEST_DIR_KEY = "dir/xenadest";
	private static final String XENA_LOG_FILE_KEY = "dir/xenalog";
	
	// Logging properties
	private static final String XENA_DEFAULT_LOG_PATTERN = "%t/xenalite%g.log";
	private static final String ROOT_LOGGING_PACKAGE = "au.gov.naa.digipres.xena";
	
	private static final String PAUSE_BUTTON_TEXT = "Pause";
	private static final String RESUME_BUTTON_TEXT = "Resume";
	
	// GUI items
	private NormalisationItemsListModel normaliseItemsLM;
	private JList normaliseItemsList;
	private JTable resultsTable;
	private NormalisationResultsTableModel tableModel;
	private TableSorter resultsSorter;
	private JPanel mainNormalisePanel;
	private JPanel mainResultsPanel;
	private JPanel mainPanel;
	private JPanel statusBarPanel;
	private JLabel statusLabel;
	private JLabel currentFileLabel;
	private JCheckBox binaryOnlyCB;
	private JProgressBar progressBar;
	private JButton pauseButton;
	private JButton stopButton;
	private JButton cancelButton;
	private JButton normErrorsButton;
	private JMenu propertiesMenu = new JMenu("Properties");
	
	private NormalisationThread normalisationThread;
	
	private Preferences prefs;
	private Xena xenaInterface;	
	
	private Logger logger;
	private LogFrame logFrame;
	private FileHandler logFileHandler = null;
    
	/**
	 * Basic constructor - calls logging and GUI initialisation methods, 
	 * and then makes the main frame visible
	 *
	 */
    public LiteMainFrame() 
    {
        super(XENA_LITE_TITLE);
        
        prefs = Preferences.userNodeForPackage(LiteMainFrame.class);
        
        initLogging();
        initNormaliseItemsPanel();
        initResultsPanel();
        
        try
		{
			initGUI();
		}
		catch (Exception e)
		{
			handleXenaException(e);
		}
        
    }
    
    /**
     * Initialises logging for the application. Three handlers are 
     * added - a ConsoleHandler (logs to System.err), a FileHandler
     * (logs to the file(s) specified in XENA_DEFAULT_LOG_PATTERN)
     * and a LogFrameHandler, which logs to a frame which can be
     * viewed from within the application. The logger variable
     * can then be used to log to all 3 handlers at once.
     *
     */
	private void initLogging()
	{
		// Main logger object
		logger = Logger.getLogger(ROOT_LOGGING_PACKAGE);
		logger.setLevel(Level.ALL);
		
		// Console handler initialisation
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		logger.addHandler(consoleHandler);
		
		// Add FileHandler
		initLogFileHandler();
				
		// LogFrameHandler initialisation
		logFrame = new LogFrame(XENA_LITE_TITLE + " Log");
		LogFrameHandler lfHandler = new LogFrameHandler(logFrame);
		logger.addHandler(lfHandler);
		lfHandler.setLevel(Level.ALL);
		
		logger.finest("Xena Lite logging initialised");
	}
	
	/**
	 * Creates a file handler and adds it to the handlers for
	 * the current logger. If the logFileHandler is not null,
	 * then one has already been added to the logger, and thus
	 * it needs to be removed before adding a new file handler.
	 *
	 */
	private void initLogFileHandler()
	{
		if (logFileHandler != null)
		{
			logger.removeHandler(logFileHandler);
			logFileHandler.flush();
			logFileHandler.close();
		}
		
		try
		{		
			String logFilePattern = prefs.get(XENA_LOG_FILE_KEY,
			                                  XENA_DEFAULT_LOG_PATTERN);
			logFileHandler = new FileHandler(logFilePattern, 1000000, 2, true);
			logFileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(logFileHandler);
		}
		catch (Exception e)
		{
			logger.log(Level.FINER, "Could not start logging File Handler", e);
		}
		
	}

	/**
	 * Initialises the "Choose Normalise Items" panel, which is
	 * the screen first presented to the user on application startup.
	 * This method creates: 
	 * <LI> The JList to display the items to be
	 * normalised, and buttons to add and remove files and directories
	 * from this list.
	 * <LI> A panel to display normalisation options (currently "Binary
	 * Normalisation Only" is the sole option).
	 * <LI> A button to do the Normalisation.
	 *
	 */
	private void initNormaliseItemsPanel()
	{
    	// Setup normalise items panel
	   	
    	JPanel normaliseItemsPanel = new JPanel(new GridBagLayout());
    	normaliseItemsPanel.setBorder(new TitledBorder(new EtchedBorder(),
        "Items to Normalise"));
    	
    	normaliseItemsLM = new NormalisationItemsListModel();
    	normaliseItemsList = new JList(normaliseItemsLM);
    	normaliseItemsList.setCellRenderer(new NormalisationItemsListRenderer());
    	normaliseItemsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	JScrollPane itemListSP = new JScrollPane(normaliseItemsList);
    	
    	
    	JPanel normaliseButtonPanel = new JPanel();
    	normaliseButtonPanel.setLayout(new BoxLayout(normaliseButtonPanel,
    	                                             BoxLayout.Y_AXIS));
    	
    	JButton addFilesButton = new JButton("Add Files");
    	JButton addDirButton = new JButton("Add Directory");
    	JButton removeButton = new JButton("Remove");
    	
    	normaliseButtonPanel.add(Box.createVerticalGlue());
    	normaliseButtonPanel.add(addFilesButton);
    	normaliseButtonPanel.add(Box.createVerticalStrut(10));
    	normaliseButtonPanel.add(addDirButton);
    	normaliseButtonPanel.add(Box.createVerticalStrut(10));
    	normaliseButtonPanel.add(removeButton);
    	
    	addToGridBag(normaliseItemsPanel, 
    	             itemListSP,
    	             0,
    	             0,
    	             GridBagConstraints.RELATIVE,
    	             GridBagConstraints.REMAINDER,
    	             1.0, 
    	             1.0, 
    	             GridBagConstraints.NORTHWEST,
    	             GridBagConstraints.BOTH,
    	             new Insets(5, 5, 5, 5),
    	             0,
    	             0);
    	
    	addToGridBag(normaliseItemsPanel, 
    	             normaliseButtonPanel,
    	             1,
    	             0,
    	             GridBagConstraints.REMAINDER,
    	             GridBagConstraints.REMAINDER,
    	             0.0, 
    	             0.0, 
    	             GridBagConstraints.NORTH,
    	             GridBagConstraints.NONE,
    	             new Insets(5, 5, 5, 5),
    	             0,
    	             0);

    	
    	// Setup normalise options panel
    	
    	JPanel normaliseOptionsPanel = 
    		new JPanel(new FlowLayout(FlowLayout.LEFT));
    	binaryOnlyCB = new JCheckBox("Binary normalisation only");
    	normaliseOptionsPanel.add(binaryOnlyCB);

    	normaliseOptionsPanel.setBorder(new TitledBorder(new EtchedBorder(),
    	                                                 "Normalisation Options"));
       
    	
    	// Setup main button panel
    	
    	JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	JButton normaliseButton = new JButton("Normalise");
    	normaliseButton.setFont(normaliseButton.getFont().deriveFont(18.0f));
    	bottomButtonPanel.add(normaliseButton);
    	
    	// Setup main normalise panel

    	mainNormalisePanel = new JPanel(new GridBagLayout());
    	    	
    	addToGridBag(mainNormalisePanel,
    	             normaliseItemsPanel,
       	             0,
    	             0,
    	             GridBagConstraints.REMAINDER,
    	             1,
    	             1.0, 
    	             1.0, 
    	             GridBagConstraints.NORTH,
    	             GridBagConstraints.BOTH,
    	             new Insets(5, 5, 5, 5),
    	             0,
    	             0);

    	addToGridBag(mainNormalisePanel,
    	             normaliseOptionsPanel,
       	             0,
    	             1,
    	             GridBagConstraints.REMAINDER,
    	             GridBagConstraints.RELATIVE,
    	             1.0, 
    	             0.0, 
    	             GridBagConstraints.CENTER,
    	             GridBagConstraints.BOTH,
    	             new Insets(5, 5, 5, 5),
    	             0,
    	             10);
    	
    	addToGridBag(mainNormalisePanel,
    	             bottomButtonPanel,
       	             0,
    	             2,
    	             GridBagConstraints.REMAINDER,
    	             GridBagConstraints.REMAINDER,
    	             1.0, 
    	             0.0, 
    	             GridBagConstraints.SOUTH,
    	             GridBagConstraints.BOTH,
    	             new Insets(5, 5, 5, 5),
    	             0,
    	             0);
    	
    	// Action Listeners
    	
    	normaliseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				// Check if binary only option has been selected
		    	int mode = binaryOnlyCB.isSelected() 
	    			? NormalisationThread.BINARY_MODE
	    		    : NormalisationThread.STANDARD_MODE;
				doNormalisation(mode);
			}
    		
    	});
    	
    	addFilesButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				doAddItems(true);
			}
    		
    	});

    	addDirButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				doAddItems(false);
			}
    		
    	});

    	removeButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				doRemoveItems();
			}
    		
    	});

		logger.finest("Xena Lite Normalise Items initialised");
	}
    
    /**
     * Initialises the "Normalisation Results" panel. A table, with
     * an associated TableModel and TableSorter, is used to display 
     * the results. Double-clicking an item in the table will bring
     * up a window with a more detailed view and more options for
     * that particular NormalisationResults object. 
     * Four buttons are also created, a Pause button, a Stop button, 
     * a Cancel button and a Binary Normalise Errors button.
     * The Pause and Stop buttons control the Normalisation thread, which can
     * be paused or stopped completely after the completion of the current item.
     * When the pause button is clicked, it is renamed to a Resume button.
     * The Cancel button returns the user to the Normalisation Items screen,
     * in the same state which the user left it, ie with all items still
     * listed. If the user then clicked "Normalise" again, this would create
     * duplicate normalised items in the same output directory, and thus
     * all normalised objects are deleted before returning to the "Normalise
     * Items" screen. The user is presented with a confirm dialog before
     * this is carried out.
     * The Binary Normalise Errors button performs a binary normalisation
     * for all items which were not successfully normalised.
     *
     */
    private void initResultsPanel()
	{
    	// Initialise display table, with model, sorter and scrollpane
    	tableModel = new NormalisationResultsTableModel();
    	resultsSorter = new TableSorter(tableModel);
    	resultsTable = new JTable(resultsSorter);
    	resultsSorter.setTableHeader(resultsTable.getTableHeader());
    	JScrollPane resultsTableSP = new JScrollPane(resultsTable);
    	    	    
    	// Initialise Pause and Stop buttons
    	pauseButton = new JButton(PAUSE_BUTTON_TEXT);
    	pauseButton.setEnabled(false);    	
    	stopButton = new JButton("Stop");
    	stopButton.setEnabled(false);
    	JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	leftButtonPanel.add(pauseButton);
    	leftButtonPanel.add(stopButton);
    	
    	// Initialise Cancel and Binary Normalise Errors buttons
    	cancelButton = new JButton("Cancel");
    	normErrorsButton = new JButton("Binary Normalise Errors");
    	JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	rightButtonPanel.add(normErrorsButton);
    	rightButtonPanel.add(cancelButton);   	
    	
    	// Layout buttons
    	JPanel resultsButtonPanel = new JPanel(new BorderLayout());
    	resultsButtonPanel.add(leftButtonPanel, BorderLayout.WEST);
    	resultsButtonPanel.add(rightButtonPanel, BorderLayout.EAST);

    	// Layout main panel
    	mainResultsPanel = new JPanel(new BorderLayout());
    	TitledBorder titledBorder = 
    		new TitledBorder(new EmptyBorder(0, 3, 3, 3),
    		                 "Normalisation Results");
    	titledBorder.setTitleFont(new Font("Arial",Font.BOLD, 14));
    	mainResultsPanel.setBorder(titledBorder);
    	mainResultsPanel.add(resultsTableSP, BorderLayout.CENTER);
    	mainResultsPanel.add(resultsButtonPanel, BorderLayout.SOUTH);
    	
    	// Action Listeners
    	resultsTable.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent e)
			{
				if (e.getModifiers() == MouseEvent.BUTTON1_MASK &&
					e.getClickCount() == 2)
				{
					try
					{
						displayResults(resultsTable.getSelectedRow());
					}
					catch (Exception ex)
					{
						handleXenaException(ex);
					}
				}
			}
    		
    	});
    	
    	pauseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				// Check if we are pausing or resuming
				int newState = 
					pauseButton.getText().equals(PAUSE_BUTTON_TEXT) 
						? NormalisationThread.PAUSED
						: NormalisationThread.RUNNING;
				changeNormalisationState(newState);
			}
    		
    	});
    	    	                                  
    	stopButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				changeNormalisationState(NormalisationThread.STOPPED);
			}
    		
    	});
    	
    	cancelButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				doCancel();
			}
    		
    	});
    	
    	normErrorsButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				doNormalisation(NormalisationThread.BINARY_ERRORS_MODE);
			}
    		
    	});

    	logger.finest("Xena Lite Results Panel initialised");
	}

    /**
     * Initialises the overall GUI for the application. The Menu,
     * Toolbar and Status Bar are set up in this method.
     * @throws IOException 
     * @throws XenaException 
     *
     */
	private void initGUI() throws XenaException, IOException
	{
    	this.setSize(800, 600);
    	this.setLocation(120, 120);
    	
    	// Setup menu
    	JMenuBar menuBar = new JMenuBar();
    	JMenu fileMenu = new JMenu("File");
    	fileMenu.setMnemonic('F');
    	JMenu toolsMenu = new JMenu("Tools");
    	toolsMenu.setMnemonic('T');
    	menuBar.add(fileMenu);
    	menuBar.add(toolsMenu);
    	JMenuItem exitItem = new JMenuItem("Exit");
    	exitItem.setMnemonic('E');
    	fileMenu.add(exitItem);
    	
    	// Initialise properties menu
    	initPropertiesMenu();
    	toolsMenu.add(propertiesMenu);
    	
    	this.setJMenuBar(menuBar);
    	
    	// Setup toolbar
    	JToolBar toolbar = new JToolBar();
    	JButton newSessionButton = new JButton("New Session");
    	JButton viewLogButton = new JButton("View Log");
    	toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
    	toolbar.add(newSessionButton);
    	toolbar.add(viewLogButton);
    	
    	// Setup status bar
    	statusBarPanel = new JPanel(new BorderLayout());
    	statusBarPanel.add(new JLabel(" "), BorderLayout.CENTER);
    	statusBarPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    	    	
    	// Add to content pane
    	mainPanel = new JPanel(new BorderLayout());
    	mainPanel.setBorder(new EtchedBorder());
    	mainPanel.add(mainNormalisePanel, BorderLayout.CENTER);
    	this.getContentPane().add(toolbar, BorderLayout.NORTH);
    	this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    	this.getContentPane().add(statusBarPanel, BorderLayout.SOUTH);
    	
    	// Action listeners
    	
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                doShutdown();
            }
        });

        // Ensure window is not resized below 400x400
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent event)
			{
				LiteMainFrame.this.setSize((LiteMainFrame.this.getWidth() < 400) 
				                           ? 400 : LiteMainFrame.this.getWidth(), 
				                           (LiteMainFrame.this.getHeight() < 400) 
				                           ? 400 : LiteMainFrame.this.getHeight());
			}
		});
	
        newSessionButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				startNewSession();
			}
        	
        });
        
        exitItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				doShutdown();
			}
        	
        });
                
        viewLogButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				logFrame.setLocation(LiteMainFrame.this.getX() + 50,
				                     LiteMainFrame.this.getY() + 50);
				logFrame.setVisible(true);
			}
        	
        });
                       
		logger.finest("Xena Lite Main GUI initialised");
	}
	
	/**
	 * Set properties menu items. This will include a menu item for
	 * each plugin that has preferences to set, plus any items
	 * for Xena Lite itself.
	 * @return
	 * @throws IOException 
	 * @throws XenaException 
	 */
	private void initPropertiesMenu()
	{
		if (propertiesMenu == null)
		{
			throw new IllegalStateException("Developer error - method should not be " + 
			                                "called until properties menu object has " +
			                                "been initialised");
		}
		
		propertiesMenu.removeAll();
		propertiesMenu.setMnemonic('P');
		
		JMenuItem prefsItem = new JMenuItem("Xena Lite Preferences");
		propertiesMenu.add(prefsItem);
        prefsItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				showPreferencesDialog();
			}
        	
        });
		
		// Add plugin properties
        try
        {
	 		PropertiesManager manager = 
				getXenaInterface().getPluginManager().getPropertiesManager();
			List<PluginProperties> pluginProperties = manager.getPluginProperties();
			
			for (PluginProperties pluginProp : pluginProperties)
			{
				JMenuItem propItem = new JMenuItem(pluginProp.getName() + "...");
				propItem.addActionListener(new PropertiesMenuListener(pluginProp));
				propertiesMenu.add(propItem);
			}
        }
        catch (Exception ex)
        {
        	// Not sure if we want to display this error or not... will for the moment
        	handleXenaException(ex);
        }
        
	}
	

	
	

	/**
	 * Convenience method for adding a component to a container
	 * which is using a GridBagLayout
	 * 
	 * @param container
	 * @param component
	 * @param gridx
	 * @param gridy
	 * @param gridwidth
	 * @param gridheight
	 * @param weightx
	 * @param weighty
	 * @param anchor
	 * @param fill
	 * @param insets
	 * @param ipadx
	 * @param ipady
	 */
    private void addToGridBag(Container container,
    						  Component component,
    						  int gridx,
    						  int gridy,
    						  int gridwidth,
    						  int gridheight,
    						  double weightx,
    						  double weighty,
    						  int anchor,
    						  int fill,    						  
    						  Insets insets,
    						  int ipadx,
    						  int ipady)
    {
    	GridBagConstraints gbc = new GridBagConstraints(gridx, gridy,
														gridwidth, gridheight,
														weightx, weighty,
														anchor, fill, 
														insets, 
														ipadx,
														ipady);
    	container.add(component, gbc);
    }
    
	/**
	 * Show "Edit Preferences" dialog. Existing preferences will be
	 * loaded using Java preferences, and saved after the dialog has
	 * been (successfully) closed.
	 *
	 */
	private void showPreferencesDialog()
	{
		LitePreferencesDialog prefsDialog = new LitePreferencesDialog(this);
		prefsDialog.setPluginDir(prefs.get(PLUGIN_DIR_KEY, ""));
		prefsDialog.setXenaDestDir(prefs.get(XENA_DEST_DIR_KEY, ""));
		prefsDialog.setXenaLogFile(prefs.get(XENA_LOG_FILE_KEY, ""));
		prefsDialog.setLocation(this.getX()+25, this.getY()+25);
		prefsDialog.setVisible(true);
		
		// We have returned from the dialog
		if (prefsDialog.isApproved())
		{
			if (!prefs.get(PLUGIN_DIR_KEY, "").equals(prefsDialog.getPluginDir().trim()))
			{
				prefs.put(PLUGIN_DIR_KEY, prefsDialog.getXenaLogFile());
				initPropertiesMenu();
			}
			if (!prefs.get(XENA_LOG_FILE_KEY, "").equals(prefsDialog.getXenaLogFile().trim()))
			{
				prefs.put(XENA_LOG_FILE_KEY, prefsDialog.getXenaLogFile());
				initLogFileHandler();
			}
			prefs.put(XENA_DEST_DIR_KEY, prefsDialog.getXenaDestDir());
		}
		logger.finest("Xena Lite preferences saved");
	}

	/**
	 * Starts the NormalisationThread to carry out normalisation
	 * of the selected objects, initialises the progress bar
	 * and status label, and switches to the Results panel.
	 * 
	 * The method can be called in one of three modes:
	 * <LI><B>STANDARD_MODE</B> will use a guesser to guess the
	 * correct type of each file;
	 * <LI><B>BINARY_MODE</B> will use the binary normaliser 
	 * for each file;
	 * <LI><B>BINARY_ERRORS_MODE</B> will use the binary normaliser
	 * to normalise any files that were not normalised in a previous
	 * attempt.
	 *
	 */
	private void doNormalisation(int mode)
    {
		if (mode != NormalisationThread.BINARY_ERRORS_MODE)
		{
			logger.finest("Beginning normalisation process for " + 
			              normaliseItemsLM.size() + " items");

			// Ensure that at least one file or directory has been selected
			if (normaliseItemsLM.size() == 0)
			{
				JOptionPane.showMessageDialog(this,
				                              "Please add files and/or directories.",
				                              "No Normalisation Items",
				                              JOptionPane.INFORMATION_MESSAGE);
				logger.finest("Attempted to normalise with no items");
				return;
			}
		}
		else
		{
			logger.finest("Beginning error normalisation process");
		}
		
		// Ensure destination directory has been set
    	String destDir = prefs.get(XENA_DEST_DIR_KEY, "");
		if (destDir.trim().equals(""))
		{
			JOptionPane.showMessageDialog(this,
			                              "Please set the destination directory" +
			                              " in Tools->Preferences.",
			                              "Destination Directory Not Set",
			                              JOptionPane.INFORMATION_MESSAGE);
			logger.finest("Attempted to normalise with no destination directory");
			return;
		}
				    	
    	try
		{
    		// Initialise status bar
	    	progressBar = new JProgressBar();
	    	progressBar.setForeground(Color.GREEN);
	    	progressBar.setMinimum(0);
	    	statusLabel = new JLabel();
	    	currentFileLabel = new JLabel();
	    	currentFileLabel.setHorizontalAlignment(JLabel.CENTER);
	    	
	    	// Refresh status bar
	    	statusBarPanel.removeAll();
	    	statusBarPanel.add(statusLabel, BorderLayout.WEST);
	    	statusBarPanel.add(currentFileLabel, BorderLayout.CENTER);
	    	statusBarPanel.add(progressBar, BorderLayout.EAST);

	    	if (mode != NormalisationThread.BINARY_ERRORS_MODE)
	    	{
	    	
		    	// Create the normalisation thread
		    	normalisationThread = 
					new NormalisationThread(mode,
					                        getXenaInterface(),
					                        tableModel,
					                        normaliseItemsLM.getNormalisationItems(),
					                        new File(destDir));
	
				// Display the results panel
		    	mainPanel.removeAll();
		    	mainPanel.add(mainResultsPanel, BorderLayout.CENTER);

				logger.finest("Switched to results panel");
	    	}
	    	else
	    	{
//	    		 Create the normalisation thread
		    	normalisationThread = 
					new NormalisationThread(mode,
					                        getXenaInterface(),
					                        tableModel,
					                        null,
					                        new File(destDir));
	    	}

	    	// Add this object as a listener of the NormalisationThread,
	    	// so that the buttons on the Results panel can be enabled
	    	// and disabled appropriately
	    	normalisationThread.add(this);
	    	
	    	// Start the normalisation process
			normalisationThread.start();
			
	    	this.validate();
	    	this.repaint();
		}
		catch (Exception e)
		{
			handleXenaException(e);
		}
    	
    }
	    
	/**
	 * Implementation of a NormalisationStateChangeListener,
	 * which is called whenever the NormalisationThread indicates
	 * that it has changed its running state. There are three states -
	 * RUNNING, PAUSED and STOPPED, and changing to any of these
	 * states causes buttons on the results panel to be enabled or
	 * disabled appropriately.
	 * The status bar components are also updated based on the
	 * values of the total items, error count, current file etc.
	 */
	public void normalisationStateChanged(int newState,
										  int totalItems,
										  int normalisedItems,
										  int errorItems,
										  String currentFile)
	{
		String statusText = (normalisedItems + errorItems) + 
							" of " + totalItems +
							" completed (" + errorItems + 
							" error(s))";
		switch(newState)
		{
		case NormalisationThread.RUNNING:
			// Update buttons
			pauseButton.setText(PAUSE_BUTTON_TEXT);
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			normErrorsButton.setEnabled(false);
			cancelButton.setEnabled(false);
			
			// Update progress bar
			progressBar.setMaximum(totalItems);
			progressBar.setValue(normalisedItems + errorItems);
			
			// Update status label
			statusLabel.setText(statusText);								
			if (errorItems > 0)
			{
				statusLabel.setForeground(Color.RED);
			}
			
			// Update current file label
			currentFileLabel.setText("Normalising " + currentFile);
			
			break;
		case NormalisationThread.PAUSED:
			// Update buttons
			pauseButton.setText(RESUME_BUTTON_TEXT);
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			normErrorsButton.setEnabled(false);
			cancelButton.setEnabled(true);
			
			currentFileLabel.setText("Paused");
			break;
		case NormalisationThread.STOPPED:
			// Update buttons
			pauseButton.setEnabled(false);
			stopButton.setEnabled(false);
			cancelButton.setEnabled(true);
			if (errorItems > 0)
			{
				normErrorsButton.setEnabled(true);
			}
			else
			{
				normErrorsButton.setEnabled(false);
			}
			
			// Update status label
			statusLabel.setText(statusText);								
			if (errorItems > 0)
			{
				statusLabel.setForeground(Color.RED);
			}
			currentFileLabel.setText("");
			
			statusBarPanel.remove(progressBar);
			
			this.validate();
			this.repaint();
			
			displayConfirmationMessage("Normalisation Complete",
			                           totalItems, 
			                           normalisedItems, 
			                           errorItems);
			break;
		}
	}
	
	public void normalisationError(String message, Exception e)
	{
		handleXenaException(e);
	}

	/**
	 * Displays a confirmation message with details of the number
	 * of normalised items and errors
	 * 
	 * @param title
	 * @param totalItems
	 * @param normalisedItems
	 * @param errorItems
	 */
	private void displayConfirmationMessage(String title,
											int totalItems, 
											int normalisedItems, 
											int errorItems)
	{
		Color darkGreen = new Color(0, 140, 0);
		JPanel totalItemsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JLabel totalText = new JLabel("Total Items:");
		JLabel totalVal = new JLabel("        " + totalItems);
		totalText.setHorizontalAlignment(JLabel.LEFT);
		totalVal.setHorizontalAlignment(JLabel.RIGHT);
		totalItemsPanel.add(totalText);
		totalItemsPanel.add(totalVal);
		totalItemsPanel.add(Box.createHorizontalStrut(70));
		
		JPanel normItemsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JLabel normText = new JLabel("Normalised:");
		JLabel normVal = new JLabel("        " + normalisedItems);
		normText.setForeground(darkGreen);
		normVal.setForeground(darkGreen);
		normText.setHorizontalAlignment(JLabel.LEFT);
		normVal.setHorizontalAlignment(JLabel.RIGHT);
		normItemsPanel.add(normText);
		normItemsPanel.add(normVal);
		normItemsPanel.add(Box.createHorizontalStrut(70));

		JPanel errorItemsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JLabel errorText = new JLabel("Errors:");
		JLabel errorVal = new JLabel("        " + errorItems);
		errorText.setForeground(Color.RED);
		errorVal.setForeground(Color.RED);
		errorText.setHorizontalAlignment(JLabel.LEFT);
		errorVal.setHorizontalAlignment(JLabel.RIGHT);
		errorItemsPanel.add(errorText);
		errorItemsPanel.add(errorVal);
		errorItemsPanel.add(Box.createHorizontalStrut(70));
		
		JPanel mainConfPanel = new JPanel(new GridLayout(3, 1));
		mainConfPanel.setBorder(new EtchedBorder());
		mainConfPanel.setOpaque(true);
		mainConfPanel.add(totalItemsPanel);
		mainConfPanel.add(normItemsPanel);
		mainConfPanel.add(errorItemsPanel);
		
		JOptionPane.showMessageDialog(this,
		                              mainConfPanel,
		                              title,
		                              JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Clicking a button on the results panel will call this
	 * method, which will indicate to the NormalisationThread
	 * that it needs to take a certain action when next appropriate.
	 * @param newState
	 */
	private void changeNormalisationState(int newState)
	{
		normalisationThread.setThreadState(newState);
	}

	/**
	 * Displays the selected results row in a NormaliserResultsFrame.
	 * The results object is retrieved from the table model using a 
	 * special column index RESULTS_OBJECT_INDEX. This is because the
	 * NormalisationResultsTableModel can not be accessed directly as
	 * the request must go through the TableSorter so that the correct
	 * row is still selected when the table has been sorted. 
	 * 
	 * @param selectedRow
	 * @throws XenaException
	 * @throws IOException
	 */
	private void displayResults(int selectedRow) 
    	throws XenaException, IOException
    {
		logger.finest("Displaying results for row " + selectedRow);
		
		// Retrieve results object using special column index.
		// The request is passed through the TableSorter (which will
		// convert the selected (sorted) row into the correct data row),
		// to the NormalisationResultsTableModel.
    	NormaliserResults results = 
    		(NormaliserResults)resultsSorter.getValueAt(selectedRow,
    		    NormalisationResultsTableModel.RESULTS_OBJECT_INDEX);
    	
    	// Display results frame
    	NormaliserResultsFrame resultsFrame = 
    		new NormaliserResultsFrame(results, getXenaInterface());
    	resultsFrame.setLocation(this.getX() + 50, this.getY() + 50);
    	resultsFrame.setVisible(true);    	
    }
    
	/**
	 * Clears the Normalisation Items List and normalisation
	 * results table, resets the normalisation options, and 
	 * displays the Normalisation Items screen. 
	 *
	 */
    private void startNewSession()
    {
    	// Reset item list, normalisation options and status bar
    	normaliseItemsLM.removeAllElements();
    	binaryOnlyCB.setSelected(false);
    	statusBarPanel.removeAll();
    	statusBarPanel.add(new JLabel(" "), BorderLayout.CENTER);
    	
    	// Clear normalisation results table
		tableModel.clear();
		tableModel.fireTableDataChanged();
    	
    	// Display normalisation items screen
    	mainPanel.removeAll();
    	mainPanel.add(mainNormalisePanel, BorderLayout.CENTER);
    	this.validate();
    	this.repaint();
    	
		logger.finest("Started new normalisation session");   	
    }
    
    /**
     * The Cancel action deletes all the normalised objects currently
     * listed in the results table, and returns to the normalise
     * items screen. The user is first asked to confirm this action.
     *
     */
    private void doCancel()
    {
    	// Confirm file deletion
    	String[] msgArr = {"Using the Cancel button will cause the current set " +
    					   "of normalised output files to be deleted.",
    					   "Are you sure you want to do this?"};   	
    	int retVal = JOptionPane.showConfirmDialog(this,
    	                                           msgArr,
    	                                           "Confirm File Deletion",
    	                                           JOptionPane.YES_NO_OPTION,
    	                                           JOptionPane.WARNING_MESSAGE);
    	
    	if (retVal == JOptionPane.YES_OPTION)
    	{
    		// File deletion has been confirmed
    		
    		List<NormaliserResults> resultsList = 
    			tableModel.getAllNormaliserResults();
    		
    		// For all results objects displayed in the table
    		for (NormaliserResults results : resultsList)
    		{
    			// Delete output file
    			String destDir = results.getDestinationDirString();
    			String destFile = results.getOutputFileName();		
    			if (destDir != null && !destDir.trim().equals("") &&
    				destFile != null && !destFile.trim().equals(""))
    			{
    				File file = new File(destDir + "\\" + destFile);
    				file.delete();
    				
    				logger.finest("Deleted file " + file);
    			}   								  
    		}
    		
    		// Clear results table
    		tableModel.clear();
    		tableModel.fireTableDataChanged();
    		
    		// Reset status bar
        	statusBarPanel.removeAll();
        	statusBarPanel.add(new JLabel(" "), BorderLayout.CENTER);

        	// Display normalisation items screen
        	mainPanel.removeAll();
        	mainPanel.add(mainNormalisePanel, BorderLayout.CENTER);
        	this.validate();
        	this.repaint();
        	
        	logger.finest("Cancel action completed");
    	}
    }

    /**
     * Shut down application
     *
     */
	private void doShutdown()
	{
		logger.finest("Shutting down Xena Lite");
		System.exit(0);
	}

	/**
	 * Add items to the Normalisation Items List. If useFileMode is true,
	 * then the file chooser is set to FILES_ONLY, otherwise the file chooser
	 * is set to DIRECTORIES_ONLY.
	 * 
	 * @param useFileMode True if adding files, false if adding directories
	 */
	private void doAddItems(boolean useFileMode)
	{
		/* 
		 * Initial directory is last visited directory. If this has not been
		 * set, then the Xena Source Directory is used. If this is not set,
		 * then the default (root) directory is used.
		 */
		JFileChooser fileChooser = 
			new JFileChooser(prefs.get(LAST_DIR_VISITED_KEY, ""));
		
		// Set selection mode of file chooser
		if (useFileMode)
		{
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		else
		{
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		
		int retVal = fileChooser.showOpenDialog(this);
		
		// We have returned from the file chooser
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			if (useFileMode)
			{
				File[] selectedFiles = fileChooser.getSelectedFiles();
				for (File file : selectedFiles)
				{
					normaliseItemsLM.addElement(file);
				}
			}
			else
			{
				normaliseItemsLM.addElement(fileChooser.getSelectedFile());
			}
			
			prefs.put(LAST_DIR_VISITED_KEY, 
			          fileChooser.getCurrentDirectory().getPath());
		}
	}
	
	/**
	 * Remove an item or items from the Normalise Items List
	 *
	 */
	private void doRemoveItems()
	{
		int[] selectedIndices = 
			normaliseItemsList.getSelectedIndices();
		
		for (int i = selectedIndices.length-1; i >= 0; i--)
		{
			normaliseItemsLM.remove(selectedIndices[i]);
		}
	}

	/**
	 * Initialises the Xena interface (currently loads plugins) if required
	 * @return
	 * @throws XenaException
	 * @throws IOException
	 */
	private Xena getXenaInterface() throws XenaException, IOException
	{
		if (xenaInterface == null)
		{
			String pluginDir = prefs.get(PLUGIN_DIR_KEY, 
			                             "");
			if (!pluginDir.trim().equals(""))
			{
				xenaInterface = new Xena();
				xenaInterface.loadPlugins(new File(pluginDir));
			}
			else
			{
				throw new XenaException("Xena Plugin directory not set! " +
				                        "Please specify in Tools->Preferences.");
			}
			logger.finest("Successfully loaded Xena interface");
		}
		
		return xenaInterface;
	}
	
	/**
	 * Displays a message dialog containing the given exception
	 * @param ex
	 */
	private void handleXenaException(Exception ex)
	{	
		logger.log(Level.FINER, ex.toString(), ex);
		JOptionPane.showMessageDialog(this, 
		                              ex.getMessage(),
		                              "Xena Lite",
		                              JOptionPane.ERROR_MESSAGE);
	}
	
	/**
     * Entry point for the Xena Lite application
     * @param args
     */
    public static void main(String[] args) {
		// Set look and feel to the look and feel of the current OS
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		LiteMainFrame mf = new LiteMainFrame();
        mf.setVisible(true);
    }
    
    
    private class PropertiesMenuListener implements ActionListener
    {
    	private PluginProperties pluginProp;
    	
    	public PropertiesMenuListener(PluginProperties pluginProp)
    	{
    		this.pluginProp = pluginProp;
    	}

		public void actionPerformed(ActionEvent e)
		{
			PropertiesDialog dialog = 
				pluginProp.getPropertiesDialog(LiteMainFrame.this);
			dialog.pack();
			dialog.setLocation(LiteMainFrame.this.getX() + 50,
			                   LiteMainFrame.this.getY() + 50);
			dialog.setVisible(true);
			
			// Have finished with dialog
			dialog.dispose();
		}
    }
    
}
