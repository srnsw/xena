/*
 * Copyright (c) 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.looks.demo;

import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

/**
 * Builds the menu bar and pull-down menus in the Simple Looks Demo.
 * Demonstrates and tests different multi-platform issues.<p>
 *
 * This class provides a couple of factory methods that create
 * menu items, check box menu items, and radio button menu items.
 * The full JGoodies Looks Demo overrides these methods to vend
 * components from the JGoodies UI framework that better handle
 * different platforms.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public class MenuBarView {

    private static final String HTML_TEXT =
        "<html><b>Bold</b>, <i>Italics</i>, <tt>Typewriter</tt></html>";


	/**
	 * Builds, configures, and returns the menubar. Requests HeaderStyle,
	 * look-specific BorderStyles, and Plastic 3D hint from Launcher.
	 */
	JMenuBar buildMenuBar(
        Settings settings,
        ActionListener helpActionListener,
        ActionListener aboutActionListener) {

		JMenuBar bar = new JMenuBar();
		bar.putClientProperty(Options.HEADER_STYLE_KEY,
							  settings.getMenuBarHeaderStyle());
		bar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY,
							  settings.getMenuBarPlasticBorderStyle());
		bar.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY,
							  settings.getMenuBarWindowsBorderStyle());
		bar.putClientProperty(PlasticLookAndFeel.IS_3D_KEY,
							  settings.getMenuBar3DHint());

		bar.add(buildFileMenu());
		bar.add(buildRadioMenu());
		bar.add(buildCheckMenu());
        bar.add(buildHtmlMenu());
        bar.add(buildAlignmentTestMenu());
		bar.add(buildHelpMenu(helpActionListener, aboutActionListener));
		return bar;
	}


	/**
	 * Builds and returns the file menu.
	 */
	private JMenu buildFileMenu() {
		JMenuItem item;

		JMenu menu = createMenu("File", 'F');

		// Build a submenu that has the noIcons hint set.
		JMenu submenu = createMenu("New", 'N');
		submenu.putClientProperty(Options.NO_ICONS_KEY, Boolean.TRUE);
		submenu.add(createMenuItem("Project\u2026", 'P'));
		submenu.add(createMenuItem("Folder\u2026", 'F'));
		submenu.add(createMenuItem("Document\u2026", 'D', KeyStroke.getKeyStroke("ctrl F8")));
		submenu.addSeparator();
		submenu.add(createMenuItem("No icon hint set", 'N', KeyStroke.getKeyStroke("ctrl F9")));

		menu.add(submenu);
		menu.addSeparator();
		item = createMenuItem("Close", 'C', KeyStroke.getKeyStroke("ctrl F4"));
		menu.add(item);
		item = createMenuItem("Close All", 'o', KeyStroke.getKeyStroke("ctrl shift F4"));
		menu.add(item);
		menu.addSeparator();
		item = createMenuItem("Save",
                              readImageIcon("save_edit.gif"),
                              'S',
                              KeyStroke.getKeyStroke("ctrl S"));
		item.setEnabled(false);
		menu.add(item);
		item = createMenuItem("Save As\u2026",
                readImageIcon("saveas_edit.gif"),
                'A');
		item.setDisplayedMnemonicIndex(5);
        menu.add(item);
		item = createMenuItem("Save All", 'e', KeyStroke.getKeyStroke("ctrl shift S"));
		item.setEnabled(false);
		menu.add(item);
		menu.addSeparator();
		item = createMenuItem("Print\u2026", readImageIcon("print.gif"), 'P',
                KeyStroke.getKeyStroke("ctrl P"));
		menu.add(item);
		menu.addSeparator();
		menu.add(createMenuItem("1 WinXPMenuItemUI.java",  '1'));
		menu.add(createMenuItem("2 WinXPUtils.java",       '2'));
		menu.add(createMenuItem("3 WinXPBorders.java",     '3'));
		menu.add(createMenuItem("4 WinXPLookAndFeel.java", '4'));
        if (!isQuitInOSMenu()) {
            menu.addSeparator();
            menu.add(createMenuItem("Exit", 'x'));
        }
		return menu;
	}


	/**
	 * Builds and returns a menu with different JRadioButtonMenuItems.
	 */
	private JMenu buildRadioMenu() {
		JRadioButtonMenuItem item;

		JMenu menu = createMenu("Radio", 'R');

		// Default icon
		ButtonGroup group1 = new ButtonGroup();
		item = createRadioItem(true, false);
		group1.add(item);
		menu.add(item);
		item = createRadioItem(true, true);
		group1.add(item);
		menu.add(item);

		menu.addSeparator();

		item = createRadioItem(false, false);
		menu.add(item);
		item = createRadioItem(false, true);
		menu.add(item);

		menu.addSeparator();

		// Custom icon
		ButtonGroup group2 = new ButtonGroup();
		item = createRadioItem(true, false);
		item.setIcon(readImageIcon("pie_mode.png"));
		group2.add(item);
		menu.add(item);
		item = createRadioItem(true, true);
		item.setIcon(readImageIcon("bar_mode.png"));
		group2.add(item);
		menu.add(item);

		menu.addSeparator();

		item = createRadioItem(false, false);
		item.setIcon(readImageIcon("alphab_sort.png"));
		//item.setDisabledIcon(Utils.getIcon("alphab_sort_gray.png"));
		menu.add(item);
		item = createRadioItem(false, true);
		item.setIcon(readImageIcon("size_sort.png"));
		//item.setDisabledIcon(readImageIcon("size_sort_gray.png"));
		menu.add(item);

		return menu;
	}


	/**
	 * Builds and returns a menu with different JCheckBoxMenuItems.
	 */
	private JMenu buildCheckMenu() {
		JCheckBoxMenuItem item;

		JMenu menu = createMenu("Check", 'C');

		// Default icon
		menu.add(createCheckItem(true, false));
		menu.add(createCheckItem(true, true));
		menu.addSeparator();
		menu.add(createCheckItem(false, false));
		menu.add(createCheckItem(false, true));

		menu.addSeparator();

		// Custom icon
		item = createCheckItem(true, false);
		item.setIcon(readImageIcon("check.gif"));
		item.setSelectedIcon(readImageIcon("check_selected.gif"));
		menu.add(item);
		item = createCheckItem(true, true);
		item.setIcon(readImageIcon("check.gif"));
		item.setSelectedIcon(readImageIcon("check_selected.gif"));
		menu.add(item);

		menu.addSeparator();

		item = createCheckItem(false, false);
		item.setIcon(readImageIcon("check.gif"));
		item.setSelectedIcon(readImageIcon("check_selected.gif"));
		menu.add(item);
		item = createCheckItem(false, true);
		item.setIcon(readImageIcon("check.gif"));
		item.setSelectedIcon(readImageIcon("check_selected.gif"));
		item.setDisabledSelectedIcon(readImageIcon("check_disabled_selected.gif"));
		menu.add(item);

		return menu;
	}


    /**
     * Builds and returns a menu with items that use a HTML text.
     */
    private JMenu buildHtmlMenu() {
        JMenu menu = createMenu("Styled", 'S');

        menu.add(createSubmenu(HTML_TEXT));
        menu.add(createMenuItem(HTML_TEXT));
        menu.addSeparator();
        menu.add(new JRadioButtonMenuItem(HTML_TEXT, false));
        menu.add(new JRadioButtonMenuItem(HTML_TEXT, true));
        menu.addSeparator();
        menu.add(new JCheckBoxMenuItem(HTML_TEXT, true));
        menu.add(new JCheckBoxMenuItem(HTML_TEXT, false));
        return menu;
    }


    /**
     * Builds and returns a menu with items that use a HTML text.
     */
    private JMenu buildAlignmentTestMenu() {
        JMenu menu = createMenu("Alignment", 'A');

        menu.add(createMenuItem("Menu item"));
        menu.add(createMenuItem("Menu item with icon", readImageIcon("refresh.gif")));
        menu.addSeparator();
        JMenu submenu = createSubmenu("Submenu");
        menu.add(submenu);

        submenu = createSubmenu("Submenu with icon");
        submenu.setIcon(readImageIcon("refresh.gif"));
        menu.add(submenu);

        return menu;
    }


	/**
	 * Builds and and returns the help menu.
	 */
	private JMenu buildHelpMenu(
        ActionListener helpActionListener,
        ActionListener aboutActionListener) {

		JMenu menu = createMenu("Help", 'H');

		JMenuItem item;
        item = createMenuItem("Help Contents", readImageIcon("help.gif"), 'H');
        if (helpActionListener != null) {
    		item.addActionListener(helpActionListener);
        }
        menu.add(item);
        if (!isAboutInOSMenu()) {
            menu.addSeparator();
            item = createMenuItem("About", 'a');
            item.addActionListener(aboutActionListener);
            menu.add(item);
        }

		return menu;
	}


    // Factory Methods ********************************************************

    protected JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }


    protected JMenuItem createMenuItem(String text) {
        return new JMenuItem(text);
    }


    protected JMenuItem createMenuItem(String text, char mnemonic) {
        return new JMenuItem(text, mnemonic);
    }


    protected JMenuItem createMenuItem(String text, char mnemonic, KeyStroke key) {
        JMenuItem menuItem = new JMenuItem(text, mnemonic);
        menuItem.setAccelerator(key);
        return menuItem;
    }


    protected JMenuItem createMenuItem(String text, Icon icon) {
        return new JMenuItem(text, icon);
    }


    protected JMenuItem createMenuItem(String text, Icon icon, char mnemonic) {
        JMenuItem menuItem = new JMenuItem(text, icon);
        menuItem.setMnemonic(mnemonic);
        return menuItem;
    }


    protected JMenuItem createMenuItem(String text, Icon icon, char mnemonic, KeyStroke key) {
        JMenuItem menuItem = createMenuItem(text, icon, mnemonic);
        menuItem.setAccelerator(key);
        return menuItem;
    }


    protected JRadioButtonMenuItem createRadioButtonMenuItem(String text, boolean selected) {
        return new JRadioButtonMenuItem(text, selected);
    }


    protected JCheckBoxMenuItem createCheckBoxMenuItem(String text, boolean selected) {
        return new JCheckBoxMenuItem(text, selected);
    }


    // Subclass will override the following methods ***************************

    /**
     * Checks and answers whether the quit action has been moved to an
     * operating system specific menu, e.g. the OS X application menu.
     *
     * @return true if the quit action is in an OS-specific menu
     */
    protected boolean isQuitInOSMenu() {
        return false;
    }


    /**
     * Checks and answers whether the about action has been moved to an
     * operating system specific menu, e.g. the OS X application menu.
     *
     * @return true if the about action is in an OS-specific menu
     */
    protected boolean isAboutInOSMenu() {
        return false;
    }


    // Higher Level Factory Methods *****************************************

	/**
	 * Creates and returns a JRadioButtonMenuItem
	 * with the given enablement and selection state.
	 */
	private JRadioButtonMenuItem createRadioItem(boolean enabled, boolean selected) {
		JRadioButtonMenuItem item = createRadioButtonMenuItem(
			getToggleLabel(enabled, selected),
			selected);
		item.setEnabled(enabled);
		item.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JRadioButtonMenuItem source = (JRadioButtonMenuItem) e.getSource();
				source.setText(getToggleLabel(source.isEnabled(), source.isSelected()));
			}
		});
		return item;
	}


	/**
	 * Creates and returns a JCheckBoxMenuItem
	 * with the given enablement and selection state.
	 */
	private JCheckBoxMenuItem createCheckItem(boolean enabled, boolean selected) {
		JCheckBoxMenuItem item = createCheckBoxMenuItem(
			getToggleLabel(enabled, selected),
			selected);
		item.setEnabled(enabled);
		item.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
				source.setText(getToggleLabel(source.isEnabled(), source.isSelected()));
			}
		});
		return item;
	}


	/**
	 *  Returns an appropriate label for the given enablement and selection state.
	 */
	protected String getToggleLabel(boolean enabled, boolean selected) {
		String prefix = enabled  ? "Enabled" : "Disabled";
		String suffix = selected ? "Selected" : "Deselected";
		return prefix + " and " + suffix;
	}


    // Helper Code ************************************************************

    /**
     * Looks up and returns an icon for the specified filename suffix.
     */
    private ImageIcon readImageIcon(String filename) {
        URL url = getClass().getResource("resources/images/" + filename);
        return new ImageIcon(url);
    }


    /**
     * Creates and returns a submenu labeled with the given text.
     */
    private JMenu createSubmenu(String text) {
        JMenu submenu = new JMenu(text);
        submenu.add(new JMenuItem("Item 1"));
        submenu.add(new JMenuItem("Item 2"));
        submenu.add(new JMenuItem("Item 3"));
        return submenu;
    }


}