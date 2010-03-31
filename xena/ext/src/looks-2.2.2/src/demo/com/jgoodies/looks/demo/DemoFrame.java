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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

/**
 * Builds the main frame in the Simple Looks Demo.
 * Demonstrates and tests different multi-platform issues by
 * showing a variety of Swing widgets in different configurations.<p>
 *
 * This class provides a couple of protected methods that create
 * components or a builder. The full JGoodies Looks Demo overrides
 * these methods to vend components or builders from the
 * JGoodies UI framework that better handle different platforms.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public class DemoFrame extends JFrame {

    protected static final Dimension PREFERRED_SIZE =
        LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(650, 510)
            : new Dimension(730, 560);


    private static final String COPYRIGHT =
        "\u00a9 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.";


    /** Describes optional settings of the JGoodies Looks. */
    private final Settings settings;

    /**
     * Constructs a <code>DemoFrame</code>, configures the UI,
     * and builds the content.
     */
    protected DemoFrame(Settings settings) {
        this.settings = settings;
        configureUI();
        build();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        Settings settings = createDefaultSettings();
        if (args.length > 0) {
            String lafShortName = args[0];
            String lafClassName;
            if ("Windows".equalsIgnoreCase(lafShortName)) {
                lafClassName = Options.JGOODIES_WINDOWS_NAME;
            } else if ("Plastic".equalsIgnoreCase(lafShortName)) {
                lafClassName = Options.PLASTIC_NAME;
            } else if ("Plastic3D".equalsIgnoreCase(lafShortName)) {
                lafClassName = Options.PLASTIC3D_NAME;
            } else if ("PlasticXP".equalsIgnoreCase(lafShortName)) {
                lafClassName = Options.PLASTICXP_NAME;
            } else {
                lafClassName = lafShortName;
            }
            System.out.println("L&f chosen: " + lafClassName);
            settings.setSelectedLookAndFeel(lafClassName);
        }
        DemoFrame instance = new DemoFrame(settings);
        instance.setSize(PREFERRED_SIZE);
        instance.locateOnScreen(instance);
        instance.setVisible(true);
    }

    private static Settings createDefaultSettings() {
        Settings settings = Settings.createDefault();

        // Configure the settings here.

        return settings;
    }


    /**
     * Configures the user interface; requests Swing settings and
     * JGoodies Looks options from the launcher.
     */
    private void configureUI() {
        // UIManager.put("ToolTip.hideAccelerator", Boolean.FALSE);

        Options.setDefaultIconSize(new Dimension(18, 18));

        Options.setUseNarrowButtons(settings.isUseNarrowButtons());

        // Global options
        Options.setTabIconsEnabled(settings.isTabIconsEnabled());
        UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY,
                settings.isPopupDropShadowEnabled());

        // Swing Settings
        LookAndFeel selectedLaf = settings.getSelectedLookAndFeel();
        if (selectedLaf instanceof PlasticLookAndFeel) {
            PlasticLookAndFeel.setPlasticTheme(settings.getSelectedTheme());
            PlasticLookAndFeel.setTabStyle(settings.getPlasticTabStyle());
            PlasticLookAndFeel.setHighContrastFocusColorsEnabled(
                settings.isPlasticHighContrastFocusEnabled());
        } else if (selectedLaf.getClass() == MetalLookAndFeel.class) {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        }

        // Work around caching in MetalRadioButtonUI
        JRadioButton radio = new JRadioButton();
        radio.getUI().uninstallUI(radio);
        JCheckBox checkBox = new JCheckBox();
        checkBox.getUI().uninstallUI(checkBox);

        try {
            UIManager.setLookAndFeel(selectedLaf);
        } catch (Exception e) {
            System.out.println("Can't change L&F: " + e);
        }

    }

    /**
     * Builds the <code>DemoFrame</code> using Options from the Launcher.
     */
    private void build() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setJMenuBar(
            createMenuBuilder().buildMenuBar(
                settings,
                createHelpActionListener(),
                createAboutActionListener()));
        setIconImage(readImageIcon("eye_16x16.gif").getImage());
    }


    /**
     * Creates and returns a builder that builds the menu.
     * This method is overriden by the full JGoodies Looks Demo to use
     * a more sophisticated menu builder that uses the JGoodies
     * UI Framework.
     *
     * @return the builder that builds the menu bar
     */
    protected MenuBarView createMenuBuilder() {
        return new MenuBarView();
    }

    /**
     * Builds and answers the content.
     */
    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildToolBar(), BorderLayout.NORTH);
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    // Tool Bar *************************************************************

    /**
     * Builds, configures and returns the toolbar. Requests
     * HeaderStyle, look-specific BorderStyles, and Plastic 3D Hint
     * from Launcher.
     */
    private Component buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(true);
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        // Swing
        toolBar.putClientProperty(
            Options.HEADER_STYLE_KEY,
            settings.getToolBarHeaderStyle());
        toolBar.putClientProperty(
            PlasticLookAndFeel.BORDER_STYLE_KEY,
            settings.getToolBarPlasticBorderStyle());
        toolBar.putClientProperty(
            WindowsLookAndFeel.BORDER_STYLE_KEY,
            settings.getToolBarWindowsBorderStyle());
        toolBar.putClientProperty(
            PlasticLookAndFeel.IS_3D_KEY,
            settings.getToolBar3DHint());

        AbstractButton button;

        toolBar.add(createToolBarButton("backward.gif", "Back"));
        button = createToolBarButton("forward.gif", "Next");
        button.setEnabled(false);
        toolBar.add(button);
        toolBar.add(createToolBarButton("home.gif", "Home"));
        toolBar.addSeparator();

        ActionListener openAction = new OpenFileActionListener();
        button = createToolBarButton("open.gif", "Open", openAction, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        button.addActionListener(openAction);
        toolBar.add(button);
        toolBar.add(createToolBarButton("print.gif", "Print"));
        toolBar.add(createToolBarButton("refresh.gif", "Update"));
        toolBar.addSeparator();

        ButtonGroup group = new ButtonGroup();
        button = createToolBarRadioButton("pie_mode.png", "Pie Chart");
        button.setSelectedIcon(readImageIcon("pie_mode_selected.gif"));
        group.add(button);
        button.setSelected(true);
        toolBar.add(button);

        button = createToolBarRadioButton("bar_mode.png", "Bar Chart");
        button.setSelectedIcon(readImageIcon("bar_mode_selected.gif"));
        group.add(button);
        toolBar.add(button);

        button = createToolBarRadioButton("table_mode.png", "Table");
        button.setSelectedIcon(readImageIcon("table_mode_selected.gif"));
        group.add(button);
        toolBar.add(button);
        toolBar.addSeparator();

        button = createToolBarButton("help.gif", "Open Help");
        button.addActionListener(createHelpActionListener());
        toolBar.add(button);

        return toolBar;
    }

    /**
     * Creates and returns a JButton configured for use in a JToolBar.<p>
     *
     * This is a simplified method that is overriden by the Looks Demo.
     * The full code uses the JGoodies UI framework's ToolBarButton
     * that better handles platform differences.
     */
    protected AbstractButton createToolBarButton(String iconName, String toolTipText) {
        JButton button = new JButton(readImageIcon(iconName));
        button.setToolTipText(toolTipText);
        button.setFocusable(false);
        return button;
    }

    private AbstractButton createToolBarButton(String iconName, String toolTipText, ActionListener action, KeyStroke keyStroke) {
        AbstractButton button = createToolBarButton(iconName, toolTipText);
        button.registerKeyboardAction(action, keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return button;
    }

    /**
     * Creates and returns a JToggleButton configured for use in a JToolBar.<p>
     *
     * This is a simplified method that is overriden by the Looks Demo.
     * The full code uses the JGoodies UI framework's ToolBarButton
     * that better handles platform differences.
     */
    protected AbstractButton createToolBarRadioButton(String iconName, String toolTipText) {
        JToggleButton button = new JToggleButton(readImageIcon(iconName));
        button.setToolTipText(toolTipText);
        button.setFocusable(false);
        return button;
    }

    // Tabbed Pane **********************************************************

    /**
     * Builds and answers the tabbed pane.
     */
    private Component buildMainPanel() {
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        //tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        addTabs(tabbedPane);

        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return tabbedPane;
    }

    private void addTabs(JTabbedPane tabbedPane) {
        tabbedPane.addTab("State",     new StateTab().build());
        tabbedPane.addTab("Align",     new AlignmentTab().build());
        tabbedPane.addTab("Tab",       new TabTestTab().build());
        tabbedPane.addTab("Split",     new SplitTab().build());
        tabbedPane.addTab("Combo",     new ComboBoxTab().build());
        tabbedPane.addTab("HTML",      new HtmlTab().build());
        tabbedPane.addTab("Dialog",    new DialogsTab().build(tabbedPane));
        tabbedPane.addTab("Desktop",   new DesktopTab().build());
        tabbedPane.addTab("Narrow",    new NarrowTab().build());
    }

    protected String getWindowTitle() {
        return "Simple Looks Demo";
    }


    // Helper Code **********************************************************************

    /**
     * Looks up and returns an icon for the specified filename suffix.
     */
    protected static ImageIcon readImageIcon(String filename) {
        URL url = DemoFrame.class.getResource("resources/images/" + filename);
        return new ImageIcon(url);
    }

    /**
     * Locates the given component on the screen's center.
     */
    protected void locateOnScreen(Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation(
            (screenSize.width  - paneSize.width)  / 2,
            (screenSize.height - paneSize.height) / 2);
    }

    /**
     * Creates and answers an ActionListener that opens the help viewer.
     */
    protected ActionListener createHelpActionListener() {
        return null;
    }

    /**
     * Creates and answers an ActionListener that opens the about dialog.
     */
    protected ActionListener createAboutActionListener() {
        return new AboutActionListener();
    }


    private final class AboutActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(
                DemoFrame.this,
                "The simple Looks Demo Application\n\n"
                    + COPYRIGHT + "\n\n");
        }
    }

    private final class OpenFileActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new JFileChooser().showOpenDialog(DemoFrame.this);
        }
    }


}