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

/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Mon Jun 18 15:14:06 EST 2007 */
package au.gov.naa.digipres.xena.core;

import java.util.Date; /*
						 * $Id$
						 */
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import java.lang.reflect.Method;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * A simple application which uses the generated JReleaseInfo file as
 * argument. It creates a table with the properties in the JReleaseInfo
 * file. Properties are read with introspection.<br />
 * You can add this viewer to your java library jar-file, setting
 * the Main-Class Manifest-Attribute to this viewer and providing
 * a "double-click view" of relevant build-data.
 *
 */
class JReleaseInfoViewer extends JFrame {
	/** ContentPanel. */
	private JPanel pnlContent = null;

	/** ScrollPanel holding table. */
	private JScrollPane scrlPane = null;

	/** Table showing properties. */
	private JTable tblProps = null;

	/** Status line. */
	private JLabel lblStatus = null;

	/** JReleaseInfo class for introspection. */
	private Class c = null;

	/** Projectname in title */
	private String project = null;

	/** Version in title */
	private String version = null;

	/**
	 * Constructor.
	 * @param c Class 
	 */
	public JReleaseInfoViewer(Class c) {
		super();
		this.c = c;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		initialize();
	}

	/**
	 * Center the frame in the middle of the screen.
	 * @param frame of window
	 */
	private void centerFrame(JFrame frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation((screenSize.width / 2) - (frameSize.width / 2), (screenSize.height / 2) - (frameSize.height / 2));
	}

	/**
	 * This method initializes the frame.
	 *
	 * @return void
	 */
	private void initialize() {
		setSize(500, 200);
		centerFrame(this);

		scrlPane = new JScrollPane();

		tblProps = getTable();
		scrlPane.setViewportView(tblProps);

		// Status Bar
		lblStatus = new JLabel();
		lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblStatus.setText("JReleaseInfo Viewer by Open Source Competence Group, www.oscg.ch");

		// Frame
		pnlContent = new JPanel();
		pnlContent.setLayout(new BorderLayout());
		pnlContent.add(scrlPane, BorderLayout.CENTER);
		pnlContent.add(lblStatus, BorderLayout.SOUTH);

		this.setContentPane(pnlContent);

		// Prepare the title
		String title = "Release Information";
		if (project != null) {
			String prefix = project;
			if (version != null) {
				prefix = prefix + " " + version;
			}

			this.setTitle(prefix + " - " + title);
		} else {
			this.setTitle(title);
		}
	}

	/**
	 * Create a table with two columns.
	 * @return table
	 */
	private JTable getTable() {
		JTable tbl = null;
		try {
			Vector cols = new Vector();
			cols.add("Property");
			cols.add("Value");
			Vector rows = new Vector();

			Object obj = c.newInstance();
			Method[] methods = c.getDeclaredMethods();

			// Feature request from rgisler 20040430
			Map sortedProps = new TreeMap();

			for (int i = 0; i < methods.length; i++) {
				Vector v = new Vector();
				Method method = methods[i];
				String methodName = method.getName();

				// Feature request from rgisler 20040430
				if (methodName.equalsIgnoreCase("getProject")) {
					Object objP = method.invoke(obj, null);
					if (objP instanceof String) {
						project = (String) objP;
					}
				}

				if (methodName.equalsIgnoreCase("getVersion")) {
					Object objV = method.invoke(obj, null);
					if (objV instanceof String) {
						version = (String) objV;
					} else {
						version = objV.toString();
					}
				}

				if (methodName.startsWith("get")) {
					String field = methodName.substring(3);
					v.add(field);
					v.add(method.invoke(obj, null));
					sortedProps.put(field, v);
				} else if (methodName.startsWith("is")) {
					String field = methodName.substring(2);
					v.add(field);
					v.add(method.invoke(obj, null));
					sortedProps.put(field, v);
				}
			}

			rows.addAll(sortedProps.values());

			tbl = new JTable(rows, cols);
			tbl.getColumn("Property").setPreferredWidth(150);
			tbl.getColumn("Value").setPreferredWidth(350);
			tbl.setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tbl;
	}

	/**
	 * Process Window close event.
	 * @param e WindowEvent
	 */
	@Override
    protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}

}

/**
 * This class provides information gathered from the build environment.
 * 
 */
public class ReleaseInfo {

	/** buildDate (set during build process to 1182143646502L). */
	private static Date buildDate = new Date(1182143646502L);

	/**
	 * Get buildDate (set during build process to Mon Jun 18 15:14:06 EST 2007).
	 * @return Date buildDate
	 */
	public static final Date getBuildDate() {
		return buildDate;
	}

	/**
	 * Get versionNum (set during build process to 3).
	 * @return int versionNum
	 */
	public static final int getVersionNum() {
		return 3;
	}

	/** revisionNum (set during build process to 0). */
	private static Integer revisionNum = new Integer(0);

	/**
	 * Get revisionNum (set during build process to 0).
	 * @return Integer revisionNum
	 */
	public static final Integer getRevisionNum() {
		return revisionNum;
	}

	/**
	 * Get buildNumber (set during build process to 74).
	 * @return int buildNumber
	 */
	public static final int getBuildNumber() {
		return 74;
	}

	/** version (set during build process to "3.0"). */
	private static String version = new String("3.0");

	/**
	 * Get version (set during build process to "3.0").
	 * @return String version
	 */
	public static final String getVersion() {
		return version;
	}

	/** project (set during build process to "Xena"). */
	private static String project = new String("Xena");

	/**
	 * Get project (set during build process to "Xena").
	 * @return String project
	 */
	public static final String getProject() {
		return project;
	}

	public static void main(String[] args) throws Exception {
		JReleaseInfoViewer frame = new JReleaseInfoViewer(ReleaseInfo.class);
		frame.setVisible(true);
	}
}
