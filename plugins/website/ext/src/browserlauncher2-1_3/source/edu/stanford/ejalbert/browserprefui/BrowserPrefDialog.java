/************************************************
    Copyright 2006 Jeff Chapman

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
package edu.stanford.ejalbert.browserprefui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 *
 * @author Jeff Chapman
 * @version 1.0
 */
public class BrowserPrefDialog
        extends JDialog {
    private JList browserList = new JList();
    private String selectedBrowser = null;
    private static final String UI_BUNDLE =
            "edu.stanford.ejalbert.browserprefui.BrowserPrefs";

    public BrowserPrefDialog(Dialog owner,
                             BrowserLauncher launcher)
            throws HeadlessException {
        super(owner, true);
        initDialog(launcher);
    }

    public BrowserPrefDialog(Frame owner,
                             BrowserLauncher launcher)
            throws HeadlessException {
        super(owner, true);
        initDialog(launcher);
    }

    public String getSelectedBrowser() {
        return selectedBrowser;
    }

    private void initDialog(BrowserLauncher launcher)
            throws MissingResourceException {
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        // get resource bundle
        ResourceBundle rbundle = ResourceBundle.getBundle(UI_BUNDLE);
        // set title
        this.setTitle(rbundle.getString("dialog.title"));
        // init list of browsers
        List browsers = launcher.getBrowserList();
        browserList.setListData(browsers.toArray());
        browserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // get selected browser from system prop
        String prefBrowser = System.getProperty(
            BrowserLauncher.BROWSER_SYSTEM_PROPERTY,
            null);
        if(prefBrowser != null) {
            browserList.setSelectedValue(prefBrowser, true);
        }
        initGui(rbundle);
    }

    private void okButtonClicked() {
        selectedBrowser = (String)browserList.getSelectedValue();
        dispose();
    }

    private void cancelButtonClicked() {
        dispose();
    }

    private void initGui(ResourceBundle rbundle)
            throws MissingResourceException {
        JButton okButton = new JButton(rbundle.getString("dialog.bttn.ok"));
        JButton cancelButton = new JButton(rbundle.getString("dialog.bttn.cancel"));
        JScrollPane browserListScroll = new JScrollPane(browserList);
        // init ok button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                okButtonClicked();
            }
        });
        // init cancel button
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                cancelButtonClicked();
            }
        });
        // create panels
        JPanel mainPanel = new JPanel(new BorderLayout(0,2));
        JPanel buttonsPanel = new JPanel();
        // format controls
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        mainPanel.add(browserListScroll, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel);
    }
}
