package au.gov.naa.digipres.xena.gui;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * Extension of JInternalFrame customised for use within Xena desktop frame.
 * Each outer view is displayed within the context of this class.
 *
 * Maybe this design wasn't such a good idea since it makes it harder for other
 * applications to embed Xena views in their window, since Xena assumes that
 * it will be displayed in an JInternalFrame. Probably we should sometime try
 * and stop inheriting JInternalFrame.
 * 
 * aak: No shit sherlock.
 * 
 */
public class InternalFrame extends JInternalFrame {
	/**
	 *  name of file save as xena data file
	 */
	public File savedFile;

	public String partId;
    
	/**
	 *  root element of xanadu data file
	 */
	//public Element element;

	public String extraText;

	/**
	 *  savedTransformConfigFile TBI
	 */
	public File savedTransformConfigFile;

	private XMLReader normaliser;

	protected MainFrame mainFrame;

	protected XenaView wrapperView;

	WindowMenuItem windowMenuItem;

	/**
	 *  Displays a file in an internal frame using the specified view
	 *
	 * @param  file                 the Xena file to view
	 * @param partId
	 * @param  mainFrame            Xena application main frame
	 * @param  view                 type of view for internal frame
	 * @param  normaliser           if we have just finished normalising, this is the normaliser used
	 * @param  extraText            extra text to append to the title bar
	 */
	public InternalFrame(File file, String partId, MainFrame mainFrame, XenaView view, XMLReader normaliser,
						 String extraText) throws
		XenaException {
		setSavedFile(file, partId);
		this.mainFrame = mainFrame;
		this.normaliser = normaliser;
		this.extraText = extraText;
		this.wrapperView = new XenaView() {
			public boolean canShowTag(String tag) {
				return false;
			}

			public String getViewName() {
				return "";

			}
		};
		getContentPane().add(wrapperView);
		windowMenuItem = new WindowMenuItem(this);
		windowMenuItem.setSelected(true);
		mainFrame.windowsButtonGroup.add(windowMenuItem);
		getTopMenu("Window").add(windowMenuItem);
		setLayer(0);
		setClosable(true);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		changeView(view);
		show();
		initListenersBasic();
	}

	public XenaView getView() {
		return (XenaView)wrapperView.getSubViews().get(0);
	}

	public XenaView getWrapperView() {
		return wrapperView;
	}

	/**
	 *  Displays a field in an internal frame using the specified view
	 *
	 * @param  view   type of view for internal frame
	 * @exception  XenaException
	 */
	protected void changeView(XenaView view) throws XenaException {
		wrapperView.setInternalFrame(this);
		wrapperView.setSubView(wrapperView, view);
		view.setInternalFrame(this);
		try {
			view.parse(); //XYZ
		} catch (IOException x) {
			throw new XenaException(x);
		} catch (SAXException x) {
			throw new XenaException(x);
		}
		makeMenu();
		windowMenuItem.changeView();
	}

	public void initListenersAndSubViews() throws XenaException {
		wrapperView.initListenersAndSubViews();
	}

	JMenu getTopMenu(String name) throws XenaException {
		return (JMenu)CustomManager.singleton().lookupByName(name).getMenuItem();
	}

	/**
	 * Set this frame to its appropriate size.
	 */
	public void setDefaultSize() {
		setSensibleSize();
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(ViewManager.class);
		try {
			if (prefs.getBoolean(ViewManager.MAXIMISE_NEW_VIEW, false)) {
				this.setMaximum(true);
			}
		} catch (PropertyVetoException ex) {
			// Nothing.
		}
	}

	/**
	 * Try and set the window to be a sensible size.
	 */
	public void setSensibleSize() {
		Dimension preferredDim = getPreferredSize();
		setSize(min(preferredDim.getWidth(), mainFrame.desktopPane.getWidth()),
				min(preferredDim.getHeight(), mainFrame.desktopPane.getHeight()));
	}

	public void initListeners() throws XenaException {
		initListenersBasic();
		initListenersAndSubViews();
	}

	/**
	 */
	public void initListenersBasic() {
		Iterator it = CustomManager.singleton().getAllByMenu().iterator();
		while (it.hasNext()) {
			CustomMenuItem custom = (CustomMenuItem)it.next();
			addInternalFrameListener(custom);
		}
		addInternalFrameListener(
			new InternalFrameListener() {
			public void internalFrameDeactivated(InternalFrameEvent e) {
			}

			public void internalFrameActivated(InternalFrameEvent e) {
				makeMenu();
				windowMenuItem.setSelected(true);
			}

			public void internalFrameDeiconified(InternalFrameEvent e) {
			}

			public void internalFrameIconified(InternalFrameEvent e) {
			}

			public void internalFrameClosing(InternalFrameEvent e) {
			}

			public void internalFrameOpened(InternalFrameEvent e) {
			}

			public void internalFrameClosed(InternalFrameEvent e) {
				prepareClose();
			}
		});
		final InternalFrame self = this;
		windowMenuItem.addActionListener(
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					self.setSelected(true);
					self.setIcon(false);
				} catch (PropertyVetoException ex) {
					System.out.println(ex);
				}
			}
		});
	}

	/**
	 *  ready the desktop for closing
	 */
	public void prepareClose() {
		wrapperView.doClose();
		mainFrame.windowsButtonGroup.remove(windowMenuItem);
		try {
			getTopMenu(MainFrame.WINDOW_MENU_STRING).remove(windowMenuItem);
			getTopMenu(MainFrame.VIEW_MENU_STRING).removeAll();
		} catch (XenaException x) {
			MainFrame.singleton().showError(x);
		}
	}

	/**
	 *  create menu for internal frame based on view
	 */
	public void makeMenu() {
		try {
			getTopMenu(MainFrame.VIEW_MENU_STRING).removeAll();
			makeMenu(getView(), getTopMenu(MainFrame.VIEW_MENU_STRING));
		} catch (XenaException x) {
			MainFrame.singleton().showError(x);
		}
	}

	/**
	 * @return    The windowMenuItem value
	 */
	protected WindowMenuItem getWindowMenuItem() {
		return windowMenuItem;
	}

	/**
	 *  Initialise listeners for this frame, listeners included with view for
	 *  datatype
	 *
	 * @exception  XenaException
	 */
	protected void makeMenu(XenaView view, JMenu addTo) {
		JMenu submenu = new JMenu(view.getViewName());
		addTo.add(submenu);
		view.makeMenu(submenu);

		Iterator it = view.getSubViews().iterator();
		while (it.hasNext()) {
			XenaView subview = (XenaView)it.next();
			makeMenu(subview, submenu);
		}
	}

	/**
	 *  method to compare a double to int
	 *
	 * @param  ad  a double
	 * @param  b   an int
	 * @return     the greater of the two values
	 */
	protected int min(double ad, int b) {
		BigDecimal bd = new BigDecimal(ad);
		bd.setScale(0, BigDecimal.ROUND_UP);
        int a = bd.intValue();
		if (a < b) {
			return a;
		} else {
			return b;
		}
	}

	public XMLReader getNormaliser() {
		return normaliser;
	}

	public File getSavedFile() {
		return savedFile;
	}

	public void setSavedFile(File savedFile, String partId) {
		this.savedFile = savedFile;
		this.partId = partId;
		String viewName = savedFile.toString();
		if (partId != null) {
			viewName += " : " + partId;
		}
		if (extraText != null) {
			viewName += " - " + extraText;
		}
		setTitle(viewName);
	}
}
