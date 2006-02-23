package au.gov.naa.digipres.xena.gui;
import gnu.getopt.Getopt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.JavaVersionChecker;
import au.gov.naa.digipres.xena.javatools.Props;
import au.gov.naa.digipres.xena.kernel.IconFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Main class for Xena application. We only put stuff in here that is used only
 * during startup, or else it has to be here. The look and feel stuff has to be
 * here because we need to set the look and feel prior to messing with the gui
 * stuff for it to take full effect.
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public class Main {
	static public String LOOK_AND_FEEL_PREF = "lookAndFeel";

	/**
	 * Load the user-selected look and feel
	 */
	public static void loadLookAndFeel() {
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(Main.class);
		try {
			String lafClassName = prefs.get(LOOK_AND_FEEL_PREF, null);
			if (lafClassName != null) {
				UIManager.setLookAndFeel(lafClassName);
			}
		} catch (UnsupportedLookAndFeelException ex) {
			System.out.println(ex);
		} catch (IllegalAccessException ex) {
			System.out.println(ex);
		} catch (ClassNotFoundException ex) {
			System.out.println(ex);
		} catch (InstantiationException ex) {
			System.out.println(ex);
		}
	}

	public static void setLookAndFeel() {
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(Main.class);
		try {
			String lafClassName = prefs.get(LOOK_AND_FEEL_PREF, null);
			if (lafClassName != null) {
				UIManager.setLookAndFeel(lafClassName);
			}
		} catch (UnsupportedLookAndFeelException ex) {
			System.out.println(ex);
		} catch (IllegalAccessException ex) {
			System.out.println(ex);
		} catch (ClassNotFoundException ex) {
			System.out.println(ex);
		} catch (InstantiationException ex) {
			System.out.println(ex);
		}
	}

	/**
	/**
	 *  Construct the application
	 *  <pre>
	 *  Arguments:
	 *  file... Load the files into Xena
	 *  -p pluginname Try and load the plugin with this name from the classpath
	 *  -i args... Start the kawa command line interpreter, passing the rest of the arguments to it
	 *
	 * @param  args Command line arguments
	 * @exception  ClassNotFoundException
	 */
	public Main(String[] args) throws ClassNotFoundException {
		loadLookAndFeel();
		JDialog splashDialog = showSplashDialog();
		Collection<String> plugins = new ArrayList<String>();
		
		// JRW - plugin directory/ies from command line
		Collection<String> pluginDirs = new ArrayList<String>();
		
		Getopt g = new Getopt("xena", args, "p:d:");
		//
		int c;
		String arg;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'p':

				// Import the given plugin from CLASSPATH
				arg = g.getOptarg();
				plugins.add(arg);
				break;
			case 'd':
				// JRW - plugin directories
				pluginDirs.add(g.getOptarg());
				break;
			case '?':
				break;
				// getopt() already printed an error
			default:
				System.out.print("getopt() returned " + c + "\n");
			}
		}
		Props props = null;
		try {
			GuiPluginManager.singleton().loadPlugins(plugins);
			
			// JRW - plugin directories
			for (String dirName : pluginDirs)
			{
				GuiPluginManager.singleton().loadPlugins(new File(dirName));
			}
						
			splashDialog.dispose();
			splashDialog = null; // Allow garbage collect.

			guiStart();
			/**
			 * Load files.
			 */
			for (int i = g.getOptind(); i < args.length; i++) {
				File file = new File(args[i]);
				if (!file.exists()) {
					MainFrame.singleton().showError("File: " + args[i] + " does not exist");
				} else {
					XenaInputSource input = new XenaInputSource(file);
					FileType type = GuesserManager.singleton().mostLikelyType(input);
					if (type == null) {
						MainFrame.singleton().showError("File: " + args[i] + " cannot guess file type");
					} else if (type instanceof XenaFileType) {
						MainFrame.singleton().showXena(file, null);
					} else {
						MainFrame.singleton().showError("Not a Xena file: " + args[i]);
					}
				}
			}
			
		} catch (IOException x) {
			x.printStackTrace();
			MainFrame.singleton().showError(x);
			System.exit(1);
		} catch (XenaException x) {
			x.printStackTrace();
			MainFrame.singleton().showError(x);
			System.exit(1);
		}
	}

	/**
	 *  Main method
	 *
	 * @param  args  The command line arguments
	 */
	public static void main(String[] args) {
		String minVer = "1.4.2";
		JavaVersionChecker chk = new JavaVersionChecker();
		if (!chk.checkMinimum(minVer)) {
			JOptionPane.showMessageDialog(null,
										  "Error: Running Java " + chk.getVersion() + " but we need version: " + minVer +
										  " Go to http://java.sun.com/ and get a later version",
										  "Wrong Java Version", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		try {
			new Main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Show the Xena logo splash screen while the plugins are loading.
	 * @return JDialog
	 */
	protected JDialog showSplashDialog() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		JDialog splashDialog = new JDialog((Frame)null, "Xena");
		splashDialog.setUndecorated(true);
		JLabel splashLabel = new JLabel();
		//ImageIcon splashIcon = new ImageIcon(getClass().getResource("xena-splash.png"));
		//ImageIcon splashIcon = new ImageIcon("xena-splash.png");
        ImageIcon splashIcon = IconFactory.getIconByName("xena-splash.png");
        
        splashLabel.setIcon(splashIcon);
		splashDialog.getContentPane().add(splashLabel);
		splashDialog.pack();
		Dimension splashSize = splashDialog.getSize();
		splashDialog.setLocation((screenSize.width - splashSize.width) / 2, (screenSize.height - splashSize.height) / 2);
		splashDialog.setVisible(true);
		return splashDialog;
	}

	/**
	 * Start up the Xena gui.
	 *
	 * @exception  IOException
	 */
	protected void guiStart() throws IOException {
		EventQueue waitQueue = new WaitCursorEventQueue(200);
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQueue);

		MainFrame frame = MainFrame.singleton();
		frame.init();
		MainFrame.singleton().setVisible(true);
	}

	/**
	 * This class was adapted from some "How-To" Java book, designed to make
	 * the hourglass cursor come up after a certain period of busy work in Java.
	 * This is because manually setting the hourglass and setting it back in
	 * every single place in the code is very tricky and error prone. However
	 * this code doesn't seem 100% reliable. Not sure why that is, maybe because
	 * of thread scheduling or something.
	 *
	 * @author Chris Bitmead
	 */
	public class WaitCursorEventQueue extends EventQueue {

		public WaitCursorEventQueue(int delay) {
			this.delay = delay;
			waitTimer = new WaitCursorTimer();
			waitTimer.setDaemon(true);
			waitTimer.start();
		}

		protected void dispatchEvent(AWTEvent event) {
			waitTimer.startTimer(event.getSource());
			try {
				super.dispatchEvent(event);
			} finally {
				waitTimer.stopTimer();
			}
		}

		private int delay;

		private WaitCursorTimer waitTimer;

		private class WaitCursorTimer extends Thread {

			synchronized void startTimer(Object source) {
				this.source = source;
				notify();
			}

			synchronized void stopTimer() {
				if (parent == null) {
					interrupt();
				} else {
					parent.setCursor(null);
					parent = null;
				}
			}

			public synchronized void run() {
				while (true) {
					try {
						//wait for notification from startTimer()
						wait();

						//wait for event processing to reach the threshold, or
						//interruption from stopTimer()
						wait(delay);

						if (source instanceof Component) {
							parent =
								SwingUtilities.getRoot((Component)source);
						} else if (source instanceof MenuComponent) {
							MenuContainer mParent =
								((MenuComponent)source).getParent();
							if (mParent instanceof Component) {
								parent = SwingUtilities.getRoot(
									(Component)mParent);
							}
						}

						if (parent != null && parent.isShowing()) {
							parent.setCursor(
								Cursor.getPredefinedCursor(
									Cursor.WAIT_CURSOR));
						}
					} catch (InterruptedException ie) {}
				}
			}

			private Object source;

			private Component parent;
		}
	}

}
