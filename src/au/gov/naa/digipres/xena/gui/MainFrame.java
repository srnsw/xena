package au.gov.naa.digipres.xena.gui;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.MenuElement;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.IconFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 *  Main frame for Xena application.
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public class MainFrame extends JFrame {

	public final static String XENA = "Xena";

	static MainFrame theSingleton = new MainFrame();

	static public String BACKGROUND_COLOR_PREF = "backgroundColor";

	public static final String WIDTH_PREF = "width";

	public static final String HEIGHT_PREF = "height";

	public static final String XPOS_PREF = "xpos";

	public static final String YPOS_PREF = "ypos";

	public static java.util.List<Object> ROOT_MENU;

	public static java.util.List<Object> FILE_MENU;

	public static java.util.List<Object> EDIT_MENU;

	public static java.util.List<Object> VIEW_MENU;

	public static java.util.List<Object> WINDOW_MENU;

	public static java.util.List<Object> HELP_MENU;

	public static java.util.List<Object> EDIT_PREFERENCES_MENU;

	public static java.util.List<Object> EDIT_PREFERENCES_PLUGINS_MENU;

	public static final String FILE_MENU_STRING = "File";

	public static final String EDIT_MENU_STRING = "Edit";

	public static final String VIEW_MENU_STRING = "View";

	public static final String WINDOW_MENU_STRING = "Window";

	public static final String HELP_MENU_STRING = "Help";

	public static final String PLUGIN_PROPS_MENU_STRING = "Plugin Properties";

	static
	{
		// Yes well, ok fair criticism that other than the (never-used) kawa
		// command line interpreter that you can call in the Main class,
		// this is the only place that kawa classes are used, in this case
		// the lisp style "CONS" List classes. Trouble is they are so darned
		// convenient for this stuff, and Lisp CONS lists are so darned cool.
		// But yes ok, not really a good enough reason for pulling in the kawa
		// library.
		
		// JRW Actually getting rid of kawa then!
		ROOT_MENU = new ArrayList<Object>();
		FILE_MENU = new ArrayList<Object>();
		FILE_MENU.add(FILE_MENU_STRING);
		EDIT_MENU = new ArrayList<Object>();
		EDIT_MENU.add(EDIT_MENU_STRING);
		VIEW_MENU = new ArrayList<Object>();
		VIEW_MENU.add(VIEW_MENU_STRING);
		WINDOW_MENU = new ArrayList<Object>();
		WINDOW_MENU.add(WINDOW_MENU_STRING);
		HELP_MENU = new ArrayList<Object>();
		HELP_MENU.add(HELP_MENU_STRING);
		EDIT_PREFERENCES_PLUGINS_MENU = new ArrayList<Object>();
		EDIT_PREFERENCES_PLUGINS_MENU.add(PLUGIN_PROPS_MENU_STRING);
		EDIT_PREFERENCES_PLUGINS_MENU.add(EDIT_MENU_STRING);
	}

	protected JPanel contentPane;

	protected JMenuBar menuBar = new JMenuBar();

	protected JToolBar toolBar = new JToolBar();

	protected JLabel statusBar = new JLabel();

	protected BorderLayout borderLayout = new BorderLayout();

	protected JDesktopPane desktopPane = new JDesktopPane();

	protected TitledBorder titledBorder1;

	protected FlowLayout flowLayout1 = new FlowLayout();

    //TODO: make getters and setters. or something.
	public ButtonGroup windowsButtonGroup = new ButtonGroup();

	protected java.util.List customs = new ArrayList();

	public String extensionSeparator() {
		return ".";
	}

	public MainFrame() {

	}

	public static MainFrame singleton() {
		return theSingleton;
	}

	static void setSingleton(MainFrame frame) {
		theSingleton = frame;
	}

	public FileType chooseFileType(File[] files, List guesses) throws XenaException {
		if (guesses.size() == 0) {
			throw new XenaException("No View Available");
		}
		String title;
		if (1 < files.length) {
			title = "Multiple Selection";
		} else {
			title = files[0].toString();
		}
		FileType type = (FileType)JOptionPane.showInputDialog(this, "Choose File Type of " + title, title, JOptionPane.QUESTION_MESSAGE, null, guesses.toArray(), null);
		return type;
    }

	public XMLReader chooseNormaliser(Type type) throws XenaException {
		List normalisers = NormaliserManager.singleton().lookupList(type);
		if (normalisers == null) {
			throw new XenaException("No Normaliser available for type: " + type);
		}
		Class rtn = null;
		if (normalisers.size() == 1) {
			rtn = (Class)normalisers.get(0);
		} else if (1 < normalisers.size()) {
			String title = "Choose Normaliser for Type " + type;
			rtn = (Class)JOptionPane.showInputDialog(this, title,
													 title,
													 JOptionPane.QUESTION_MESSAGE, null, normalisers.toArray(), null);
		}
		return (XMLReader)NormaliserManager.singleton().lookupByClass(rtn);
	}

	public InternalFrame getSelectedFrame() {
		return (InternalFrame)desktopPane.getSelectedFrame();
	}

	/**
	 *  Initialise this class, by calling init methods
	 */
	public void init() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
			myInit();
			initListeners();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initListeners() throws XenaException {
		helpInit();
		JInternalFrame[] frames = desktopPane.getAllFrames();
		for (int i = 0; i < frames.length; i++) {
			InternalFrame frame = (InternalFrame)frames[i];
			frame.initListeners();
		}
		if (0 < frames.length) {
			desktopPane.setSelectedFrame(frames[0]);
		}
	}

	public InternalFrame newFrame(File file, String partId, XenaView view, XMLReader normaliser, String extraText) throws
		XenaException {
		try {
            
			InternalFrame ifr = new InternalFrame(file, partId, this, view, normaliser, extraText);
			desktopPane.add(ifr, new Integer(1));
			// This needs to be here because we get NullPointerException trying
			// to maximize window not yet in a desktop.
			// ifr.setDefaultSize();
			try {
				ifr.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			return ifr;
		} catch (XenaException e) {
			JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	/**
	 * Show an error dialog box for an unexpected exception
	 * @param e Throwable
	 */
	public void showError(Throwable e) {
		Object[] buttons = {
			"OK", "Details"};
		String msg = e.getMessage();
		Throwable nxt = null;
		while (msg == null && (nxt = e.getCause()) != null) {
			msg = nxt.getMessage();
		}
		if (msg == null) {
			msg = e.toString();
		}
		JOptionPane pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, buttons);
		JDialog dialog = pane.createDialog(this, "Error");
		dialog.setVisible(true);
		if (pane.getValue() != null && pane.getValue().equals(buttons[1])) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream pw = new PrintStream(baos);
			e.printStackTrace(pw);
			JTextArea jtf = new JTextArea(new String(baos.toByteArray()));
			jtf.setRows(20);
			jtf.setColumns(60);
			JScrollPane scroll = new JScrollPane(jtf, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jtf.setEditable(false);

			Object[] buttons2 = {
				"OK"};
			dialog = new JDialog(this, "Error", true);
			dialog.getContentPane().add(scroll);
			packAndPosition(dialog);
			dialog.setVisible(true);
		}
	}

	/**
	 * Show an error dialog box for a known error.
	 * @param s String
	 */
	public void showError(String s) {
		JOptionPane.showMessageDialog(null, s, s, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Install the menu and toolbar call backs
	 */
	protected void initCustomListeners() {
		Iterator it = customs.iterator();
		while (it.hasNext()) {
			CustomMenuItem custom = (CustomMenuItem)it.next();
			JButton button;
			JMenuItem mi = custom.getMenuItem();
			if (mi != null) {
				mi.addActionListener(custom.getActionListener());
			}
			JButton jb = custom.getToolbarButton();
			if (jb != null) {
				jb.addActionListener(custom.getActionListener());
			}
		}
	}

	/**
	 * Add new custom menu.
	 * @param custom
	 */
	protected void addCustomMenu(final CustomMenuItem custom) {
		customs.add(custom);
		final MainFrame mf = this;
		
		java.util.List path = new ArrayList(custom.getPath());
		Collections.reverse(path);
		
		AbstractButton mi = addMenuTo(menuBar, path, custom);
	}

	protected AbstractButton getToolbarButton(String toolTip) {
		Component[] cs = toolBar.getComponents();
		for (int i = 0; i < cs.length; i++) {
			if (cs[i] instanceof AbstractButton) {
				if (toolTip.equals(((AbstractButton)cs[i]).getToolTipText())) {
					return (AbstractButton)cs[i];
				}
			}
		}
		return null;
	}

	protected void addCustomToolbar(final CustomMenuItem custom) {
		final MainFrame mf = this;
		JButton toolButton = custom.getToolbarButton();
		if (toolButton != null) {
			AbstractButton oldButton = getToolbarButton(toolButton.getToolTipText());
			if (oldButton != null) {
				toolBar.remove(oldButton);
			}
			toolBar.add(toolButton);
		}
	}

	protected MenuElement findMenuElement(MenuElement e, String name) {
		// This searches for sub-menus
		MenuElement[] el = e.getSubElements();
		for (int i = 0; i < el.length; i++) {
			MenuElement el2 = el[i];
			if (el2 instanceof JMenuItem && ((JMenuItem)el2).getText().equals(name)) {
				return el2;
			}
		}
		// This searches for sub-items
		if (e instanceof JMenu) {
			JMenu jm = (JMenu)e;
			Component cs[] = jm.getMenuComponents();
			for (int i = 0; i < cs.length; i++) {
				Component c = (Component)cs[i];
				if (c instanceof JMenuItem) {
					JMenuItem ji = (JMenuItem)c;
					if (ji.getText().equals(name)) {
						return ji;
					}
				}
			}
		}
		return null;
	}

	JMenu findOrAddMenu(MenuElement e, String name) {
		MenuElement m = findMenuElement(e, name);
		if (m != null) {
			if (m instanceof JMenu) {
				return (JMenu)m;
			} else {
				((JMenu)e).remove((Component)m);
			}
		}
		JMenu nm = new JMenu(name);
		if (e instanceof JMenuBar) {
			((JMenuBar)e).add(nm);
		} else {
			if (e instanceof JMenu) {
				((JMenu)e).add(nm);
			}
		}
		return nm;
	}

	protected AbstractButton addMenuTo(MenuElement menus, java.util.List path, CustomMenuItem custom) {
		if (0 < path.size()) {
			String name = (String)path.get(0);
			java.util.List rest = path.subList(1, path.size());
			MenuElement nextmenu = findOrAddMenu(menus, name);
			return addMenuTo(nextmenu, rest, custom);
		} else {
			custom.init();
			AbstractButton comp = custom.getMenuItem();
			// Dummy menus are null
			if (comp != null) {
				String name = comp.getText();
				MenuElement e = findMenuElement(menus, name);
				if (e != null) {
					if (menus instanceof JMenuBar) {
						((JMenuBar)menus).remove((Component)e);
					} else {
						((JMenu)menus).remove((Component)e);
					}
				}
				if (menus instanceof JMenuBar) {
					((JMenuBar)menus).add(comp);
				} else {
					((JMenu)menus).add(comp);
				}
			}
			return comp;
		}
	}

	/**
	 * Install the menu items and the toolbar options
	 */
	protected void addCustom() {
		Iterator it = CustomManager.singleton().getAllByMenu().iterator();
		while (it.hasNext()) {
			addCustomMenu((CustomMenuItem)it.next());
		}
		it = CustomManager.singleton().getAllByToolbar().iterator();
		while (it.hasNext()) {
			addCustomToolbar((CustomMenuItem)it.next());
		}
	}

	/**
	 *  Initialises the help system
	 */
	protected void helpInit() {
		//CSH.setHelpIDString(menuBar, "xena.menu.index");
		//CSH.setHelpIDString(toolBar, "xena.menu.toolbar");
	}

	protected void printButton_actionPerformed(ActionEvent e) {
		InternalFrame ifr = getSelectedFrame();
		XenaView xv = ifr.getView();
		xv.PrintView();

	}

	public JInternalFrame[] getAllFrames() {
		return desktopPane.getAllFrames();
	}

	/**
	 *  Initialises the main frame
	 */
	protected void myInit() {
		addCustom();
		initCustomListeners();
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(MainFrame.class);
		// Restore the most recent mainframe size and location
		int width = prefs.getInt(WIDTH_PREF, 600);
		int height = prefs.getInt(HEIGHT_PREF, 400);
		int xpos = prefs.getInt(XPOS_PREF, 0);
		int ypos = prefs.getInt(YPOS_PREF, 0);
		this.setSize(width, height);
		this.setLocation(xpos, ypos);
		// Restore the user pref background color
		String colstr = prefs.get(this.BACKGROUND_COLOR_PREF, null);
		if (colstr != null) {
			getDesktopPane().setBackground(Color.decode(colstr));
		}
	}

	/**
	 *  Component initialization for the main frame
	 *
	 * @exception  Exception
	 */
	protected void jbInit() throws Exception {
		ImageIcon wmicon =  IconFactory.getIconByName("xena-icon.png");
		this.setIconImage(wmicon.getImage());
		contentPane = (JPanel)this.getContentPane();
		titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "BNBBBBBB");
		contentPane.setLayout(borderLayout);
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(XenaException.class);
		this.setForeground(new Color(158, 217, 164));
		this.setSize(new Dimension(400, 300));
		this.setTitle("Xena: digital preservation application, " + prefs.get("version", "") + " " + prefs.get("date", ""));
		statusBar.setText(" ");
		desktopPane.setBackground(new Color(184, 249, 217));
		this.setJMenuBar(menuBar);
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		contentPane.add(desktopPane, BorderLayout.CENTER);
	}

	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	public JLabel getStatusBar() {
		return statusBar;
	}

	public InternalFrame showXena(File file, XMLReader normaliser) throws XenaException {
		String tag = NormaliserManager.singleton().getTag(file.toURI().toASCIIString());
        //notout
        //System.out.println("mainframe.showXena -> Got tag:" + tag);
		final XenaView view = ViewManager.singleton().getDefaultView(tag, XenaView.REGULAR_VIEW, 0);
		return showXena(file, normaliser, view);
	}

	/**
	 * Show the given Xena file on the screen with the default view.
	 * @param file Xena file to display
	 * @param normaliser Normaliser used for this file. May be null if not normalised recently.
	 * @return InternalFrame
	 * @throws XenaException
	 */
	public InternalFrame showXena(File file, XMLReader normaliser, XenaView view) throws XenaException {
		try {
			InternalFrame ifr = newFrame(file, null, view, normaliser, null);
			try {
				XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
				reader.setFeature("http://xml.org/sax/features/namespaces",true);
				reader.setFeature("http://xml.org/sax/features/namespace-prefixes",true);

				XenaInputSource is = new XenaInputSource(file);
				is.setEncoding("UTF-8");
				reader.setContentHandler(view.getContentHandler());
				reader.parse(is);
				is.close();
				view.closeContentHandler();
			} catch (Exception e) {
				throw new XenaException(e);
			}
			view.initListenersAndSubViews();
			view.parse();
			ifr.makeMenu();
			ifr.pack();
			//ifr.setDefaultSize();
			return ifr;
		} catch (IOException x) {
			throw new XenaException(x);
		} catch (SAXException x) {
			throw new XenaException(x);
		}
	}

	/**
	 * Given a dialog box, pack it and centre it on the MainFrame.
	 * Utility function that is used all over the place.
	 */
	public static void packAndPosition(JDialog dialog) {
		dialog.pack();
		if (MainFrame.singleton().isVisible()) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			java.awt.Point topLeft = MainFrame.singleton().getLocationOnScreen();
			dialog.setLocation((int)(topLeft.getX() + (MainFrame.singleton().getWidth() - dialog.getWidth()) / 2),
							   (int)(topLeft.getY() + (MainFrame.singleton().getHeight() - dialog.getHeight()) / 2));
		}
	}

}
