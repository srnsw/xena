/************************************************
    Copyright 2004,2005,2006 Jeff Chapman

    This file is part of BrowserLauncher2.

    BrowserLauncher2 is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    BrowserLauncher2 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with BrowserLauncher2; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ************************************************/
// $Id$
package edu.stanford.ejalbert.testing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exceptionhandler.BrowserLauncherErrorHandler;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import edu.stanford.ejalbert.browserprefui.BrowserPrefAction;

/**
 * Standalone gui that allows for testing the broserlauncher code and provides
 * a sample implementation.
 *
 * @author Jeff Chapman
 */
public class BrowserLauncherTestApp
        extends JFrame {
    private static final String debugResources =
            "edu.stanford.ejalbert.resources.Debugging";
    private TestAppLogger logger; // in ctor
    private JComboBox browserBox = new JComboBox();
    private JLabel loggingLevelTxtFld = new JLabel();
    private JTextField urlTextField = new JTextField();
    private BrowserLauncher launcher; // in ctor
    private JTextArea debugTextArea = new JTextArea();
    private JTextField browserListField = new JTextField();
    private ResourceBundle bundle; // in ctor
    private JCheckBox windowPolicyCBox = new JCheckBox();

    public BrowserLauncherTestApp() {
        super();
        try {
            bundle = ResourceBundle.getBundle(debugResources);
            logger = initDebugLogging();
            loggingLevelTxtFld.setText(logger.getLevelText());
            super.setTitle(bundle.getString("label.app.title"));
            populateDebugInfo(bundle, debugTextArea);
            launcher = new BrowserLauncher(
                    logger,
                    new TestAppErrorHandler(debugTextArea));
            ComboBoxModel cbModel = new DefaultComboBoxModel(launcher.
                    getBrowserList().toArray());
            browserBox.setModel(cbModel);
            windowPolicyCBox.setSelected(launcher.getNewWindowPolicy());
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private TestAppLogger initDebugLogging() {
        String[] levelLabels =
                bundle.getString("logging.level.labels").split(";");
        debugTextArea.setEditable(false);
        debugTextArea.setLineWrap(true);
        debugTextArea.setWrapStyleWord(true);
        debugTextArea.setText("");
        return new TestAppLogger(debugTextArea,
                                 levelLabels,
                                 bundle.getString("logging.dateformat"));
    }

    public static void main(String[] args) {
        BrowserLauncherTestApp app = new BrowserLauncherTestApp();
        app.pack();
        app.setVisible(true);
    }

    private void windowPolicyItemStateChange(ItemEvent e) {
        launcher.setNewWindowPolicy(e.getStateChange() == ItemEvent.SELECTED);
    }

    private void populateDebugInfo(ResourceBundle bundle,
                                   JTextArea debugTextArea) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        // display first message
        printWriter.println(bundle.getString("debug.mssg"));
        printWriter.println();
        // get property values to display
        StringTokenizer tokenizer =
                new StringTokenizer(bundle.getString("debug.propnames"),
                                    ";",
                                    false);
        int pipeSymbol;
        String token, display, property;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            pipeSymbol = token.indexOf('|');
            display = token.substring(0, pipeSymbol);
            property = token.substring(pipeSymbol + 1);
            printWriter.print(display);
            printWriter.println(System.getProperty(property));
        }
        printWriter.close();
        debugTextArea.append(stringWriter.toString());
    }

    private void jbInit()
            throws Exception {
        // button and action for setting the browser preference
        BrowserPrefAction browserPrefAction = new BrowserPrefAction(
                bundle.getString("bttn.set.preference"),
                launcher,
                this);
        JButton prefBrowserBttn = new JButton(browserPrefAction);

        JButton browseButton = new JButton(bundle.getString("bttn.browse"));
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseButton_actionPerformed(e);
            }
        });
        JLabel enterUrlLabel = new JLabel(bundle.getString("label.url"));
        urlTextField.setText(bundle.getString("url.default"));
        urlTextField.setColumns(25);
        JPanel urlPanel = new JPanel(new BorderLayout());
        urlPanel.add(enterUrlLabel, BorderLayout.LINE_START);
        urlPanel.add(urlTextField, BorderLayout.CENTER);
        urlPanel.add(browseButton, BorderLayout.LINE_END);

        JScrollPane debugTextScrollPane = new JScrollPane(debugTextArea);
        //debugTextScrollPane.getViewport().add(debugTextArea);

        JLabel debugLevelLabel = new JLabel(bundle.getString(
                "label.logging.level"));
        JButton loggingLevelBttn = new JButton(bundle.getString(
                "bttn.set.logging"));
        loggingLevelBttn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loggingLevelBttn_actionPerformed(e);
            }
        });
        JButton copyButton = new JButton(bundle.getString("bttn.copy"));
        copyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyButton_actionPerformed(e);
            }
        });
        JPanel debugTextBttnPanel = new JPanel();
        BoxLayout bttnBoxLayout = new BoxLayout(
                debugTextBttnPanel,
                BoxLayout.X_AXIS);
        debugTextBttnPanel.setLayout(bttnBoxLayout);
        debugTextBttnPanel.add(Box.createHorizontalStrut(2));
        debugTextBttnPanel.add(browserBox);
        debugTextBttnPanel.add(Box.createHorizontalStrut(2));
        debugTextBttnPanel.add(debugLevelLabel);
        debugTextBttnPanel.add(Box.createHorizontalStrut(3));
        debugTextBttnPanel.add(loggingLevelTxtFld);
        debugTextBttnPanel.add(Box.createHorizontalStrut(5));
        debugTextBttnPanel.add(Box.createHorizontalGlue());
        debugTextBttnPanel.add(loggingLevelBttn);
        debugTextBttnPanel.add(Box.createHorizontalStrut(3));
        debugTextBttnPanel.add(copyButton);
        debugTextBttnPanel.add(Box.createHorizontalStrut(2));

        windowPolicyCBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                windowPolicyItemStateChange(e);
            }
        });
        windowPolicyCBox.setText(bundle.getString("label.window.policy"));
        JLabel browserListLabel = new JLabel(bundle.getString(
                "label.browser.list"));
        JPanel browserListPanel = new JPanel();
        BoxLayout browserListBoxLayout = new BoxLayout(
                browserListPanel,
                BoxLayout.X_AXIS);
        browserListPanel.setLayout(browserListBoxLayout);
        browserListPanel.add(browserListLabel);
        browserListPanel.add(Box.createHorizontalStrut(2));
        browserListPanel.add(browserListField);
        browserListPanel.add(Box.createHorizontalStrut(2));
        browserListPanel.add(windowPolicyCBox);
        browserListPanel.add(Box.createHorizontalStrut(2));
        browserListPanel.add(prefBrowserBttn);

        JPanel configPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        configPanel.add(browserListPanel);
        configPanel.add(debugTextBttnPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 3));
        mainPanel.add(debugTextScrollPane,
                      java.awt.BorderLayout.CENTER);
        mainPanel.add(urlPanel,
                      java.awt.BorderLayout.NORTH);
        mainPanel.add(configPanel,
                      java.awt.BorderLayout.SOUTH);

        this.getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(browseButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void updateDebugTextArea(Exception exception,
                                            JTextArea debugTextArea) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        printWriter.println();
        exception.printStackTrace(printWriter);
        printWriter.println();
        printWriter.close();
        debugTextArea.append(stringWriter.toString());
    }

    private void browseButton_actionPerformed(ActionEvent e) {
        if (logger.isInfoEnabled()) {
            logger.info("browse button clicked");
        }
        try {
            String urlString = urlTextField.getText();
            if (urlString == null || urlString.trim().length() == 0) {
                throw new MalformedURLException("You must specify a url.");
            }
            new URL(urlString); // may throw MalformedURLException
            BrowserLauncherErrorHandler errorHandler = new TestAppErrorHandler(
                    debugTextArea);
            // use browser list if browserListField has data
            String browserItems = browserListField.getText();
            if (browserItems != null && browserItems.length() > 0) {
                logger.debug("using browser list");
                String[] browserArray = browserItems.split("[ ]+");
                List browserList = Arrays.asList(browserArray);
                logger.debug(browserList.toString());
                launcher.openURLinBrowser(browserList,
                                          urlString);
            }
            else {
                String targetBrowser = browserBox.getSelectedItem().toString();
                logger.debug(targetBrowser);
                launcher.openURLinBrowser(targetBrowser,
                                          urlString);
            }
        }
        catch (Exception ex) {
            // capture exception
            BrowserLauncherTestApp.updateDebugTextArea(ex, debugTextArea);
            // show message to user
            JOptionPane.showMessageDialog(this,
                                          ex.getMessage(),
                                          "Error Message",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyButton_actionPerformed(ActionEvent e) {
        if (logger.isInfoEnabled()) {
            logger.info("copy button clicked");
        }
        debugTextArea.selectAll();
        debugTextArea.copy();
        debugTextArea.select(0, 0);
    }

    private void loggingLevelBttn_actionPerformed(ActionEvent e) {
        String[] levels = logger.getLevelOptions();
        int levelIndex = logger.getLevel();
        String level = (String) JOptionPane.showInputDialog(
                this,
                bundle.getString("logging.level.select.message"),
                bundle.getString("logging.level.select.title"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                levels,
                levels[levelIndex]);
        if (level != null && level.length() > 0) {
            levelIndex = -1;
            for (int idx = 0, max = levels.length;
                                    idx < max && levelIndex == -1; idx++) {
                if (level.equals(levels[idx])) {
                    levelIndex = idx;
                }

            }
            logger.setLevel(levelIndex);
            loggingLevelTxtFld.setText(logger.getLevelText());
        }
    }

    private static class TestAppErrorHandler
            implements BrowserLauncherErrorHandler {
        private JTextArea debugTextArea; // in ctor

        TestAppErrorHandler(JTextArea debugTextArea) {
            this.debugTextArea = debugTextArea;
        }

        public void handleException(Exception ex) {
            // capture exception
            BrowserLauncherTestApp.updateDebugTextArea(ex, debugTextArea);
            // show message to user
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                          ex.getMessage(),
                                          "Error Message",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
}
