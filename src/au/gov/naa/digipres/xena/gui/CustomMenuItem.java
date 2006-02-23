package au.gov.naa.digipres.xena.gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Custom menu item. Extend this class to place something in a menu.
 */
public abstract class CustomMenuItem implements InternalFrameListener, ActionListener, Serializable {
	/**
	 * Affects the ordering of toolbar buttons.
	 */
	protected int toolbarRanking = 0;

	/**
	 * Affects the ordering of menu items.
	 */
	protected int menuRanking = 0;

	/**
	 * The full menu hierarchy where to place this item.
	 */
	protected List path;

	/**
	 * The toolbar button.
	 */
	protected JButton toolbarButton;

	/**
	 * The menu item.
	 */
	protected JMenuItem menuItem;

	public CustomMenuItem() {
	}

	public JButton getToolbarButton() {
		return toolbarButton;
	}

	public ActionListener getActionListener() {
		return this;
	}

	public List getPath() {
		return path;
	}

	public JMenuItem getMenuItem() {
		return menuItem;
	}

	public int getMenuRanking() {
		return menuRanking;
	}

	public int getToolbarRanking() {
		return toolbarRanking;
	}

	public void init() {
	}

	/**
	 * Override this to do something when an InternalFrame is activated.
	 */
	public void activated() {
	}

	/**
	 * Override this to do something when an InternalFrame is deactivated.
	 */
	public void deactivated() {
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
		deactivated();
	}

	public void internalFrameActivated(InternalFrameEvent e) {
		activated();
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
	}

	public void setMenuRanking(int menuRanking) {
		this.menuRanking = menuRanking;
	}

	public void setToolbarRanking(int toolbarRanking) {
		this.toolbarRanking = toolbarRanking;
	}

	/**
	 * Override this to do perform the custom function.
	 */
	public void actionPerformed(ActionEvent e) {
	}

	public void setMenuItem(JMenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public void setToolbarButton(JButton toolbarButton) {
		this.toolbarButton = toolbarButton;
	}

	public void setPath(List path) {
		this.path = path;
	}
}
