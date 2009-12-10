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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 *
 * @author Jeff Chapman
 * @version 1.0
 */
public class BrowserPrefAction
        extends AbstractAction {
    private final BrowserLauncher browserLauncher; // in ctor
    private final JFrame appFrame; // in ctor

    public BrowserPrefAction(String name,
                             BrowserLauncher browserLauncher,
                             JFrame appFrame) {
        super(name);
        if(browserLauncher == null) {
            throw new IllegalArgumentException("browserLauncher cannot be null");
        }
        this.browserLauncher = browserLauncher;
        this.appFrame = appFrame;
    }

    public BrowserPrefAction(String name,
                             Icon icon,
                             BrowserLauncher browserLauncher,
                             JFrame appFrame) {
        super(name, icon);
        if(browserLauncher == null) {
            throw new IllegalArgumentException("browserLauncher cannot be null");
        }
        this.browserLauncher = browserLauncher;
        this.appFrame = appFrame;
    }

    /* --------------------------- from Action --------------------------- */

    /**
     * Launches a browser preferences dialog and sets the system
     * property BrowserLauncher.BROWSER_SYSTEM_PROPERTY with
     * the requested browser.
     * <p>
     * Browser prefs dialog will be placed in the Swing thread queue
     * to enable action performed method to return immediately.
     *
     * @param e ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        final ActionEvent event = e;
        Runnable runner = new Runnable() {
            public void run() {
                try {
                    BrowserPrefDialog dlg = new BrowserPrefDialog(
                            appFrame,
                            browserLauncher);
                    dlg.setLocationRelativeTo(appFrame);
                    dlg.pack();
                    dlg.setSize(275,200);
                    dlg.setVisible(true);
                    String prefBrowser = dlg.getSelectedBrowser();
                    if(prefBrowser != null) {
                        System.setProperty(
                                BrowserLauncher.BROWSER_SYSTEM_PROPERTY,
                                prefBrowser);
                    }
                }
                catch (Exception ex) {
                    browserLauncher.getLogger().error("problem getting/setting browser pref", ex);
                }
            }
        };
        SwingUtilities.invokeLater(runner);
    }
}
