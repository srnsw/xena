/************************************************
    Copyright 2004,2005,2006,2007 Markus Gebhard, Jeff Chapman

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
package edu.stanford.ejalbert.launching;

import java.util.List;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * Main interface to the Browser Launching methods.
 *
 * @author Markus Gebhard
 */
public interface IBrowserLaunching {
    /**
     * Key to system property containing name of users
     * preferred browser.
     */
    public static final String BROWSER_SYSTEM_PROPERTY =
            "edu.stanford.ejalbert.preferred.browser";
    /**
     * Key to system property that controls how browsers are discovered
     * when running on a Windows O/S.
     * <p>
     * The values are registry and disk.
     */
    public static final String WINDOWS_BROWSER_DISC_POLICY_PROPERTY =
            "win.browser.disc.policy";
    /**
     * Value associated with WINDOWS_BROWSER_DISC_POLICY_PROPERTY.
     */
    public static final String WINDOWS_BROWSER_DISC_POLICY_DISK = "disk";
    /**
     * Value associated with WINDOWS_BROWSER_DISC_POLICY_PROPERTY.
     */
    public static final String WINDOWS_BROWSER_DISC_POLICY_REGISTRY = "registry";
    /**
     * property file key for delimiter character used in other properties.
     */
    public static final String PROP_KEY_DELIMITER = "delimchar";
    /**
     * prefix used for property file keys that define a browser
     */
    public static final String PROP_KEY_BROWSER_PREFIX = "browser.";
    /**
     * http protocol
     */
    public static final String PROTOCOL_HTTP = "http";
    /**
     * file protocol
     */
    public static final String PROTOCOL_FILE = "file";
    /**
     * mailto protocol
     */
    public static final String PROTOCOL_MAILTO = "mailto";
    /**
     * Identifier for the system's default browser.
     */
    public static final String BROWSER_DEFAULT = "Default";

    /**
     * Performs any initialization needed for the particular O/S.
     *
     * @throws BrowserLaunchingInitializingException
     */
    public void initialize()
            throws BrowserLaunchingInitializingException;

    /**
     * Opens the passed url in the system's default browser.
     *
     * @param urlString String
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
    public void openUrl(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException;

    /**
     * Allows user to target a specific browser. The names of
     * potential browsers can be accessed via the
     * {@link #getBrowserList() getBrowserList} method.
     * <p>
     * If the call to the requested browser fails, the code will
     * fail over to the default browser.
     *
     * @param browser String
     * @param urlString String
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
    public void openUrl(String browser,
                        String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException;

    /**
     * Allows user to target several browsers. The names of
     * potential browsers can be accessed via the
     * {@link #getBrowserList() getBrowserList} method.
     * <p>
     * The browsers from the list will be tried in order
     * (first to last) until one of the calls succeeds. If
     * all the calls to the requested browsers fail, the code
     * will fail over to the default browser.
     *
     * @param browsers List
     * @param urlString String
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
    public void openUrl(List browsers,
                        String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException;

    /**
     * Returns a list of browsers to be used for browser targetting.
     * This list will always contain at least one item:
     * {@link #BROWSER_DEFAULT BROWSER_DEFAULT}.
     *
     * @return List
     */
    public List getBrowserList();

    /**
     * Returns the policy used for opening a url in a browser.
     * <p>
     * If the policy is true, an attempt will be made to force the
     * url to be opened in a new instance (window) of the
     * browser.
     * <p>
     * If the policy is false, the url may open in a new window or
     * a new tab.
     * <p>
     * Results will vary based on the O/S and browser being targetted.
     *
     * @return boolean
     */
    public boolean getNewWindowPolicy();

    /**
     * Sets the policy used for opening a url in a browser.
     *
     * @param forceNewWindow boolean
     */
    public void setNewWindowPolicy(boolean forceNewWindow);
}
