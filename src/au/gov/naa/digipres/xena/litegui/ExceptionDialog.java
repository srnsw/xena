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
 * Created on 5/10/2004
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 */
public class ExceptionDialog extends JDialog {

	private javax.swing.JPanel jContentPane = null;

	private JOptionPane jOptionPane = null;

	private ExceptionPanel exceptionPanel;

	private final String title;

	/**
	 * Show a dialog displaying an exception.
	 * @param frame the component to show the dialog over
	 * @param e the exception to display
	 * @param title the title of the dialog
	 * @param explanation   the explanation of the exception
	 */
	public static void showExceptionDialog(Component component, Throwable t, String title, String explanation) {
		Frame frame = JOptionPane.getFrameForComponent(component);
		ExceptionDialog dialog = new ExceptionDialog(frame, t, title, explanation);
		dialog.setVisible(true);
	}

	/**
	 * Show a dialog displaying an exception.
	 * @param frame the component to show the dialog over
	 * @param title the title of the dialog
	 * @param explanation   the explanation of the exception
	 */
	public static void showExceptionDialog(Component component, String error, String title, String explanation) {
		showExceptionDialog(component, error, title, explanation, javax.swing.JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 
	 * @param component parent component
	 * @param error error detail
	 * @param title message box title
	 * @param explanation 
	 * @param type message box type (use JOptionPane types)
	 */
	public static void showExceptionDialog(Component component, String error, String title, String explanation, int type) {
		Frame frame = JOptionPane.getFrameForComponent(component);
		ExceptionDialog dialog = new ExceptionDialog(frame, error, title, explanation, type);
		dialog.setLocationRelativeTo(component);
		dialog.setVisible(true);
	}

	/**
	 * This is the default constructor
	 */
	protected ExceptionDialog() {
		this(null, (Exception) null, "", "");
	}

	protected ExceptionDialog(Frame frame, Throwable t, String title, String explanation) {
		super(frame, true);
		this.title = title;

		exceptionPanel = new ExceptionPanel(t, explanation);
		initialize(javax.swing.JOptionPane.ERROR_MESSAGE);
	}

	protected ExceptionDialog(Frame frame, String error, String title, String explanation, int type) {
		super(frame, true);
		this.title = title;
		exceptionPanel = new ExceptionPanel(error, explanation);
		initialize(type);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(int type) {
		this.setTitle(title); 
		this.setSize(562, 203);
		this.setContentPane(getJContentPane(type));
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane(int type) {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJOptionPane(type), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jOptionPane	
	 * 	
	 * @return javax.swing.JOptionPane	
	 */
	private JOptionPane getJOptionPane(int type) {
		if (jOptionPane == null) {
			jOptionPane = new JOptionPane();
			jOptionPane.setMessage(exceptionPanel);
			jOptionPane.setMessageType(type);
			jOptionPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					if ((e.getPropertyName().equals("value"))) { //$NON-NLS-1$
						jOptionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						setVisible(false);
					}
				}
			});
		}
		return jOptionPane;
	}
} // @jve:decl-index=0:visual-constraint="29,25"
