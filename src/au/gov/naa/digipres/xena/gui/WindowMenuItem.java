package au.gov.naa.digipres.xena.gui;
import javax.swing.JRadioButtonMenuItem;


/**
 * Enables the addition of a  menu item associated with an internal frame
 * @author     Chris Bitmead
 * @created    2 July 2002
 */

public class WindowMenuItem extends JRadioButtonMenuItem {
	InternalFrame ifr;

	/**
	 * @param  ifr  Description of Parameter
	 */
	public WindowMenuItem(InternalFrame ifr) {
		this.ifr = ifr;
	}

	/**
	 * In case different views have different title. Don't think this is important right now,
	 * but good to have.
	 */
	public void changeView() {
		this.setText(ifr.getTitle());
	}
}
