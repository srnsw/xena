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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.core.ReleaseInfo;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.IconFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesMenuListener;
import au.gov.naa.digipres.xena.util.TableSorter;
import au.gov.naa.digipres.xena.util.logging.LogFrame;
import au.gov.naa.digipres.xena.util.logging.LogFrameHandler;

import com.jgoodies.plaf.plastic.Plastic3DLookAndFeel;

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
	private static final String XENA_LITE_TITLE = "Xena 3.0 Lite";
	
	// Preferences keys
	private static final String LAST_DIR_VISITED_KEY = "dir/lastvisited";
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
	private JRadioButton guessTypeRadio;
	private JRadioButton binaryOnlyRadio;
	private JProgressBar progressBar;
	private JButton pauseButton;
	private JButton stopButton;
	private JButton cancelButton;
	private JButton normErrorsButton;
	private JButton newSessionButton;
	private JMenu pluginPropertiesMenu = new JMenu("Plugin Preferences");
	
	private NormalisationThread normalisationThread;
	
	private Preferences prefs;
	private Xena xenaInterface;	
	
	private Logger logger;
	private LogFrame logFrame;
	private FileHandler logFileHandler = null;
	
	private SplashScreen splashScreen;
    
	/**
	 * Basic constructor - calls logging and GUI initialisation methods, 
	 * and then makes the main frame visible
	 *
	 */
    public LiteMainFrame() 
    {
        super(XENA_LITE_TITLE);
        
        prefs = Preferences.userNodeForPackage(LiteMainFrame.class);
            	
        // Show splash screen
    	splashScreen = new SplashScreen(XENA_LITE_TITLE, getVersionString());
    	splashScreen.setVisible(true);
    	        
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
		
		// Hide splash screen
		logger.removeHandler(splashScreen.getLogHandler());
		splashScreen.setVisible(false);
		splashScreen.dispose();
		splashScreen = null;
        
    }
    
    private String getVersionString()
    {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");    	
    	
    	return "build " + 
    		ReleaseInfo.getVersionNum() + "." +
    		ReleaseInfo.getRevisionNum() + "." +
    		ReleaseInfo.getBuildNumber() + "/" +
    		formatter.format(ReleaseInfo.getBuildDate());
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
		
		// Splash screen logger
		logger.addHandler(splashScreen.getLogHandler());
		
		logger.finest("Logging initialised");
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
    	TitledBorder itemsBorder = new TitledBorder(new EtchedBorder(),"Items to Normalise");
    	itemsBorder.setTitleFont(itemsBorder.getTitleFont().deriveFont(13.0f));
    	normaliseItemsPanel.setBorder(itemsBorder);
   	
    	normaliseItemsLM = new NormalisationItemsListModel();
    	normaliseItemsList = new JList(normaliseItemsLM);
    	normaliseItemsList.setCellRenderer(new NormalisationItemsListRenderer());
    	normaliseItemsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	JScrollPane itemListSP = new JScrollPane(normaliseItemsList);
    	
    	
    	JPanel normaliseButtonPanel = new JPanel();
    	normaliseButtonPanel.setLayout(new GridLayout(3, 1, 10, 10));
    	
    	Font buttonFont = new JButton().getFont().deriveFont(13.0f);
    	JButton addFilesButton = new JButton("Add Files");
    	addFilesButton.setFont(buttonFont);
    	JButton addDirButton = new JButton("Add Directory");
    	addDirButton.setFont(buttonFont);
    	JButton removeButton = new JButton("Remove");
    	removeButton.setFont(buttonFont);
   	
    	normaliseButtonPanel.add(addFilesButton);
    	normaliseButtonPanel.add(addDirButton);
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
    	             new Insets(5, 5, 5, 8),
    	             0,
    	             0);

    	
    	// Setup normalise options panel
    	
    	JPanel binaryRadioPanel = new JPanel();
    	binaryRadioPanel.setLayout(new BoxLayout(binaryRadioPanel, BoxLayout.Y_AXIS));
    	guessTypeRadio = new JRadioButton("Guess type for all files");
    	guessTypeRadio.setFont(guessTypeRadio.getFont().deriveFont(12.0f));
    	binaryOnlyRadio = new JRadioButton("Binary normalisation only");
    	binaryOnlyRadio.setFont(binaryOnlyRadio.getFont().deriveFont(12.0f));
    	binaryRadioPanel.add(guessTypeRadio);
    	binaryRadioPanel.add(binaryOnlyRadio);
    	ButtonGroup binaryRadioGroup = new ButtonGroup();
    	binaryRadioGroup.add(guessTypeRadio);
    	binaryRadioGroup.add(binaryOnlyRadio);
    	guessTypeRadio.setSelected(true);
    	
    	JPanel normaliseOptionsPanel = 
    		new JPanel(new FlowLayout(FlowLayout.LEFT));
    	normaliseOptionsPanel.add(binaryRadioPanel);
    	TitledBorder optionsBorder = new TitledBorder(new EtchedBorder(),"Normalisation Options");
    	optionsBorder.setTitleFont(optionsBorder.getTitleFont().deriveFont(13.0f));
    	normaliseOptionsPanel.setBorder(optionsBorder);
       
    	
    	// Setup main button panel
    	
    	JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	JButton normaliseButton = new JButton("Normalise");
    	normaliseButton.setIcon(IconFactory.getIconByName("images/icons/green_r_arrow.png"));
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
		    	int mode = binaryOnlyRadio.isSelected() 
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

		logger.finest("Normalise Items initialised");
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
    	
    	Font buttonFont = new JButton().getFont().deriveFont(13.0f);
    	    	    
    	// Initialise Pause and Stop buttons
    	pauseButton = new JButton(PAUSE_BUTTON_TEXT);
    	pauseButton.setEnabled(false);  
    	pauseButton.setIcon(IconFactory.getIconByName("images/icons/pause.png"));
    	pauseButton.setFont(buttonFont);
    	stopButton = new JButton("Stop");
    	stopButton.setEnabled(false);
    	stopButton.setIcon(IconFactory.getIconByName("images/icons/stop.png"));
    	stopButton.setFont(buttonFont);
    	JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	leftButtonPanel.add(pauseButton);
    	leftButtonPanel.add(stopButton);
    	
    	// Initialise Cancel and Binary Normalise Errors buttons
    	cancelButton = new JButton("Cancel");
    	cancelButton.setIcon(IconFactory.getIconByName("images/icons/black_cross.png"));
    	cancelButton.setFont(buttonFont);
    	normErrorsButton = new JButton("Binary Normalise Failures");
    	normErrorsButton.setIcon(IconFactory.getIconByName("images/icons/binary.png"));
    	normErrorsButton.setFont(buttonFont);
    	JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	rightButtonPanel.add(normErrorsButton);
    	rightButtonPanel.add(cancelButton);   	
    	
    	// Layout buttons
    	JPanel resultsButtonPanel = new JPanel(new BorderLayout());
     	resultsButtonPanel.add(leftButtonPanel, BorderLayout.WEST);
    	resultsButtonPanel.add(rightButtonPanel, BorderLayout.EAST);

    	// Layout
    	JPanel tablePanel = new JPanel(new BorderLayout());
    	tablePanel.setBorder(new EmptyBorder(3, 3, 10, 3));
    	tablePanel.add(resultsTableSP, BorderLayout.CENTER);

    	mainResultsPanel = new JPanel(new BorderLayout());
    	TitledBorder titledBorder = 
    		new TitledBorder(new EmptyBorder(0, 3, 3, 3),
    		                 "Normalisation Results");
    	titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(13.0f));
    	mainResultsPanel.setBorder(titledBorder);
    	mainResultsPanel.add(tablePanel, BorderLayout.CENTER);
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
						int modelIndex = resultsSorter.modelIndex(resultsTable.getSelectedRow());
						displayResults(modelIndex);
					}
					catch (Exception ex)
					{
						handleXenaException(ex);
					}
				}
			}
    		
    	});
    	resultsTable.addKeyListener(new KeyAdapter(){

			/* (non-Javadoc)
			 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e)
			{				
				if (e.getKeyChar() == ' ')
				{
					try
					{
						int modelIndex = resultsSorter.modelIndex(resultsTable.getSelectedRow());
						displayResults(modelIndex);
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

    	logger.finest("Results Panel initialised");
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
    	
    	ImageIcon xenaImageIcon = IconFactory.getIconByName("images/xena-icon.png");
    	this.setIconImage(xenaImageIcon.getImage());
    	
    	// Setup menu
    	JMenuBar menuBar = new JMenuBar();
    	JMenu fileMenu = new JMenu("File");
    	fileMenu.setMnemonic('F');
    	JMenu toolsMenu = new JMenu("Tools");
    	toolsMenu.setMnemonic('T');
    	JMenu helpMenu = new JMenu("Help");
    	helpMenu.setMnemonic('H');
    	menuBar.add(fileMenu);
    	menuBar.add(toolsMenu);
    	menuBar.add(helpMenu);
    	JMenuItem exitItem = new JMenuItem("Exit", 'E');
    	exitItem.setIcon(IconFactory.getIconByName("images/icons/exit.png"));
    	fileMenu.add(exitItem);
		JMenuItem prefsItem = new JMenuItem(XENA_LITE_TITLE + " Preferences", 'X');
		prefsItem.setIcon(IconFactory.getIconByName("images/icons/spanner.png"));
		toolsMenu.add(prefsItem);
    	JMenuItem helpItem  = new JMenuItem("Help", 'H');
    	helpItem.setIcon(IconFactory.getIconByName("images/icons/help.png"));
        try {
    	helpItem.addActionListener(new CSH.DisplayHelpFromSource(getHelpBroker()));
    	helpMenu.add(helpItem);
        } catch (XenaException xe) {
            xe.printStackTrace();
        }
        JMenuItem aboutItem = new JMenuItem("About", 'A');
    	aboutItem.setIcon(IconFactory.getIconByName("images/icons/info.png"));
    	helpMenu.add(aboutItem);
    	JMenuItem aboutPluginsItem = new JMenuItem("About Plugins", 'P');
    	aboutPluginsItem.setIcon(IconFactory.getIconByName("images/icons/plug.png"));
    	helpMenu.add(aboutPluginsItem);
    	
    	// Initialise properties menu
    	initPluginPropertiesMenu();
    	pluginPropertiesMenu.setIcon(IconFactory.getIconByName("images/icons/plug_lightning.png"));
    	toolsMenu.add(pluginPropertiesMenu);
    	
    	this.setJMenuBar(menuBar);
    	
    	// Setup toolbar
    	JToolBar toolbar = new JToolBar();
    	newSessionButton = new JButton("New Session");
    	newSessionButton.setIcon(IconFactory.getIconByName("images/icons/window_new.png"));
    	newSessionButton.setFont(newSessionButton.getFont().deriveFont(12.0f));
    	JButton viewLogButton = new JButton("View Log");
    	viewLogButton.setIcon(IconFactory.getIconByName("images/icons/open_book.png"));
    	viewLogButton.setFont(viewLogButton.getFont().deriveFont(12.0f));
    	toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
    	toolbar.add(newSessionButton);
    	toolbar.add(viewLogButton);
    	
    	logger.finest("Toolbar initialised");
    	
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

        // Ensure window is not resized below 400x430
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent event)
			{
				LiteMainFrame.this.setSize((LiteMainFrame.this.getWidth() < 400) 
				                           ? 400 : LiteMainFrame.this.getWidth(), 
				                           (LiteMainFrame.this.getHeight() < 430) 
				                           ? 430 : LiteMainFrame.this.getHeight());
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
                
        prefsItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				showPreferencesDialog();
			}
        	
        });

        aboutItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				LiteAboutDialog.showAboutDialog(LiteMainFrame.this, XENA_LITE_TITLE, getVersionString());
			}
        	
        });

        aboutPluginsItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				try
				{
					AboutPluginsDialog.showPluginsDialog(LiteMainFrame.this, getXenaInterface(), XENA_LITE_TITLE + " Plugins");
				}
				catch (Exception ex)
				{
					handleXenaException(ex);
				}
			}
        	
        });
        
        viewLogButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				logFrame.setLocationRelativeTo(LiteMainFrame.this);
				logFrame.setVisible(true);
			}
        	
        });
                       
		logger.finest("Main GUI initialised");
	}
	
	/**
	 * Create Plugin Properties menu, and populate with a menu item for each
	 * plugin with properties to set
	 * 
	 * @return
	 * @throws IOException 
	 * @throws XenaException 
	 */
	private void initPluginPropertiesMenu()
	{
		if (pluginPropertiesMenu == null)
		{
			throw new IllegalStateException("Developer error - method should not be " + 
			                                "called until properties menu object has " +
			                                "been initialised");
		}
		
		pluginPropertiesMenu.removeAll();
		pluginPropertiesMenu.setMnemonic('P');
				
		// Add plugin properties
        try
        {
	 		PropertiesManager manager = 
				getXenaInterface().getPluginManager().getPropertiesManager();
			List<PluginProperties> pluginProperties = manager.getPluginProperties();
			
			for (PluginProperties pluginProp : pluginProperties)
			{
				JMenuItem propItem = new JMenuItem(pluginProp.getName() + "...");
				propItem.addActionListener(new PropertiesMenuListener(this, pluginProp));
				pluginPropertiesMenu.add(propItem);
			}
			
			logger.finest("Plugin properties menu initialised");
        }
        catch (Exception ex)
        {
        	// Not sure if we want to display this error or not... will for the moment
        	handleXenaException(ex);
        }
        
	}
	
	private HelpBroker getHelpBroker() throws XenaException
	{
		HelpBroker broker;
		String helpsetName = "xenalitehelp";
		
		ClassLoader loader = getClass().getClassLoader();
		URL url = HelpSet.findHelpSet(loader, "doc/litehelp/" + helpsetName);
        if (url != null) 
        {
        	HelpSet mainHS;
			try
			{
				mainHS = new HelpSet(loader, url);
				broker = mainHS.createHelpBroker();
			}
			catch (HelpSetException e)
			{
				throw new XenaException("Could not create help set " + helpsetName);
			}
        	
        }
        else
        {
        	logger.log(Level.FINER, "Help Set " + helpsetName + " not found");
        	throw new XenaException("Could not find help set " + helpsetName);
        }
            
		return broker;
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
		LitePreferencesDialog prefsDialog = new LitePreferencesDialog(this, XENA_LITE_TITLE + " Preferences");
		prefsDialog.setXenaDestDir(prefs.get(XENA_DEST_DIR_KEY, ""));
		prefsDialog.setXenaLogFile(prefs.get(XENA_LOG_FILE_KEY, ""));
		prefsDialog.setLocationRelativeTo(this);
		prefsDialog.setVisible(true);
		
		// We have returned from the dialog
		if (prefsDialog.isApproved())
		{
			if (!prefs.get(XENA_LOG_FILE_KEY, "").equals(prefsDialog.getXenaLogFile().trim()))
			{
				prefs.put(XENA_LOG_FILE_KEY, prefsDialog.getXenaLogFile());
				initLogFileHandler();
			}
			prefs.put(XENA_DEST_DIR_KEY, prefsDialog.getXenaDestDir());
		}
		logger.finest(XENA_LITE_TITLE + " preferences saved");
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
			              normaliseItemsLM.getSize() + " items");

			// Ensure that at least one file or directory has been selected
			if (normaliseItemsLM.getSize() == 0)
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
			                              	" in Tools->" +
			                              	XENA_LITE_TITLE + 
			                              	" Preferences.",
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
					                        new File(destDir),
					                        this);
	
				// Display the results panel
		    	mainPanel.removeAll();
		    	mainPanel.add(mainResultsPanel, BorderLayout.CENTER);
	    	}
	    	else
	    	{
	    		// Create the normalisation thread
		    	normalisationThread = 
					new NormalisationThread(mode,
					                        getXenaInterface(),
					                        tableModel,
					                        null,
					                        new File(destDir),
					                        this);
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
			pauseButton.setIcon(IconFactory.getIconByName("images/icons/pause.png"));
			stopButton.setEnabled(true);
			normErrorsButton.setEnabled(false);
			cancelButton.setEnabled(false);
			newSessionButton.setEnabled(false);
			
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
			pauseButton.setIcon(IconFactory.getIconByName("images/icons/green_r_arrow.png"));
			stopButton.setEnabled(true);
			normErrorsButton.setEnabled(false);
			cancelButton.setEnabled(true);
			newSessionButton.setEnabled(true);
			
			currentFileLabel.setText("Paused");
			break;
		case NormalisationThread.STOPPED:
			// Update buttons
			pauseButton.setEnabled(false);
			stopButton.setEnabled(false);
			cancelButton.setEnabled(true);
			newSessionButton.setEnabled(true);
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
		new NormalisationCompleteDialog(this, totalItems, normalisedItems, errorItems).setVisible(true);
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
		NormaliserResults results = tableModel.getNormaliserResults(selectedRow);
    	
    	// Display results frame
    	NormaliserResultsFrame resultsFrame = 
    		new NormaliserResultsFrame(results, getXenaInterface());
    	resultsFrame.setLocationRelativeTo(this);
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
    	// Check that the user really wants to restart
    	String[] msgArr = {"Are you sure you want to start a new session?",
    					   "This will not delete any output from this session."};
    	
    	int retVal = 
    		JOptionPane.showConfirmDialog(this, msgArr, "Confirm New Session", JOptionPane.OK_CANCEL_OPTION);
    	if (retVal == JOptionPane.OK_OPTION)
    	{
 	    	// Reset item list, normalisation options and status bar
	    	normaliseItemsLM.removeAllElements();
	    	guessTypeRadio.setSelected(true);
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
    				File file = new File(destDir + File.separator + destFile);
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
		logger.finest("Shutting down " + XENA_LITE_TITLE);
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
			xenaInterface = new Xena();
			xenaInterface.loadPlugins(getPluginsDirectory());
			logger.finest("Successfully loaded Xena Framework interface");
		}
		
		return xenaInterface;
	}
	
	/**
	 * Returns the xena lite plugins directory. This is set as being a directory named "plugins"
	 * which is a subdirectory of the directory containing the xena.jar file.
	 * First we assume that we are running xena lite from the directory containing the xena.jar file.
	 * If the plugins directory cannot be found, then the base directory could be different to the
	 * xena.jar directory. So first we get the URL of the litegui package. This URL
	 * will consist of the file system path to the jar file plus a path to the package directory. The
	 * directory containing the jar file can thus be extracted.
	 * @return
	 * @throws XenaException
	 */
	private File getPluginsDirectory() throws XenaException
	{
		File pluginsDir = new File("plugins");
		if (!pluginsDir.exists() || !pluginsDir.isDirectory())
		{
			boolean pluginsDirFound = false;
			String resourcePath = 
				this.getClass().getResource("/" + this.getClass().getPackage().getName().replace(".", "/")).getPath();
			
			if (resourcePath.indexOf("/") >= 0 && resourcePath.lastIndexOf("!") >= 0)
			{
				String jarPath = resourcePath.substring(resourcePath.indexOf("/")+1, resourcePath.lastIndexOf("!"));
				if (jarPath.lastIndexOf("/") >= 0)
				{
					pluginsDir = new File(jarPath.substring(0, jarPath.lastIndexOf("/")+1) + "plugins");
					if (pluginsDir.exists() && pluginsDir.isDirectory())
					{
						pluginsDirFound = true;
						
					}
				}
			}
			if (!pluginsDirFound)
			{
				throw new XenaException("Cannot find default plugins directory. " +
	            						"Try running Xena Lite from the same directory as xena.jar.");
			}
		}			
			
		File[] pluginFiles = pluginsDir.listFiles();
		boolean foundPlugin = false;
		for (int i = 0; i < pluginFiles.length; i++)
		{
			if (pluginFiles[i].getName().endsWith(".jar"))
			{
				foundPlugin = true;
				break;
			}
		}
		if (!foundPlugin)
		{
			JOptionPane.showMessageDialog(this,
			                              "No plugins found in plugin directory " + pluginsDir.getAbsolutePath(),
			                              "No Plugins Found",
			                              JOptionPane.WARNING_MESSAGE);
			logger.finer("No plugins found, proceding without plugins");
		}
		return pluginsDir;
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
		                              XENA_LITE_TITLE,
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
			Plastic3DLookAndFeel plaf = new Plastic3DLookAndFeel();
//			Plastic3DLookAndFeel.setMyCurrentTheme(new SkyBluerTahoma());
			UIManager.setLookAndFeel(plaf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		LiteMainFrame mf = new LiteMainFrame();
        mf.setVisible(true);
    }

            
}
