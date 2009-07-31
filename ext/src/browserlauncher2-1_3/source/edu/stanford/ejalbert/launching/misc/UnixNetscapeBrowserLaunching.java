/************************************************
    Copyright 2004,2005,2006 Markus Gebhard, Jeff Chapman

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
package edu.stanford.ejalbert.launching.misc;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import net.sf.wraplog.AbstractLogger;

/**
 * Tries several browsers (mozilla, netscape, firefox, opera, and konqueror).
 * Most users will have at least one of these installed. The types are
 * defined in /edu/stanford/ejalbert/launching/misc/linuxUnixConfig.properties.
 *
 * @author Markus Gebhard, Jeff Chapman
 */
public class UnixNetscapeBrowserLaunching
        implements IBrowserLaunching {
    /**
     * config file for linux/unix
     */
    public static final String CONFIGFILE_LINUX_UNIX =
            "/edu/stanford/ejalbert/launching/misc/linuxUnixConfig.properties";
    /**
     * map of supported unix/linux browsers. The map contains
     * displayName => StandardUnixBrowser mappings.
     */
    private Map unixBrowsers = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    protected final AbstractLogger logger; // in ctor
    /**
     * name of config file passed into constructor
     */
    private final String configFileName; // in ctor
    /**
     * new window policy to apply when opening a url. If true,
     * try to force url into a new browser instance/window.
     */
    private boolean forceNewWindow = false;

    /**
     * Sets the logger and config file name.
     *
     * @param logger AbstractLogger
     * @param configFile String
     */
    public UnixNetscapeBrowserLaunching(AbstractLogger logger,
                                        String configFile) {
        if (configFile == null) {
            throw new IllegalArgumentException("config file cannot be null");
        }
        this.logger = logger;
        this.configFileName = configFile;
    }

    /**
     * Provides access the browsers map for extending classes.
     *
     * @param key String
     * @return StandardUnixBrowser
     */
    protected StandardUnixBrowser getBrowser(String key) {
        return (StandardUnixBrowser) unixBrowsers.get(key);
    }

    /**
     * Attempts to open a url with the specified browser. This is
     * a utility method called by the openUrl methods.
     *
     * @param unixBrowser UnixBrowser
     * @param urlString String
     * @return boolean
     * @throws BrowserLaunchingExecutionException
     */
    protected boolean openUrlWithBrowser(UnixBrowser unixBrowser,
                                         String urlString)
            throws BrowserLaunchingExecutionException {
        boolean success = false;
        logger.info(unixBrowser.getBrowserDisplayName());
        logger.info(urlString);
        try {
            int exitCode = -1;
            Process process = null;
            String[] args;
            // try to open in a new tab/current instance
            // skip this attempt if force new window is set to true
            if (!forceNewWindow) {
                args = unixBrowser.getArgsForOpenBrowser(urlString);
                if (logger.isDebugEnabled()) {
                    logger.debug(Arrays.asList(args).toString());
                }
                process = Runtime.getRuntime().exec(args);
                exitCode = process.waitFor();
            }
            // try call to force a new window if requested
            if (forceNewWindow && exitCode != 0) {
                args = unixBrowser.getArgsForForcingNewBrowserWindow(urlString);
                if (logger.isDebugEnabled()) {
                    logger.debug(Arrays.asList(args).toString());
                }
                process = Runtime.getRuntime().exec(args);
                exitCode = process.waitFor();
            }
            // open in a new window
            if (exitCode != 0) {
                args = unixBrowser.getArgsForStartingBrowser(urlString);
                if (logger.isDebugEnabled()) {
                    logger.debug(Arrays.asList(args).toString());
                }
                process = Runtime.getRuntime().exec(args);
                exitCode = process.waitFor();
            }
            if (exitCode == 0) {
                success = true;
            }
        }
        // Runtimes may throw InterruptedException
        // want to catch every possible exception and wrap it
        catch (Exception e) {
            throw new BrowserLaunchingExecutionException(e);
        }
        return success;
    }

    /* ---------------------- from IBrowserLaunching ----------------------- */

    /**
     * Uses the which command to find out which browsers are available.
     * The available browsers are put into the unixBrowsers map
     * using displayName => StandardUnixBrowser mappings.
     *
     * @todo what do we do if there are no browsers available?
     * @throws BrowserLaunchingInitializingException
     */
    public void initialize()
            throws BrowserLaunchingInitializingException {
        try {
            URL configUrl = getClass().getResource(configFileName);
            if (configUrl == null) {
                throw new BrowserLaunchingInitializingException(
                        "unable to find config file: " + configFileName);
            }
            StringBuffer potentialBrowserNames = new StringBuffer();
            Properties configProps = new Properties();
            configProps.load(configUrl.openStream());
            String sepChar = configProps.getProperty(PROP_KEY_DELIMITER);
            Iterator keysIter = configProps.keySet().iterator();
            while (keysIter.hasNext()) {
                String key = (String) keysIter.next();
                if (key.startsWith(PROP_KEY_BROWSER_PREFIX)) {
                    StandardUnixBrowser browser = new StandardUnixBrowser(
                            sepChar,
                            configProps.getProperty(key));
                    if (browser.isBrowserAvailable(logger)) {
                        unixBrowsers.put(browser.getBrowserDisplayName(),
                                         browser);
                    }
                    else {
                        if (potentialBrowserNames.length() > 0) {
                            potentialBrowserNames.append("; ");
                        }
                        potentialBrowserNames.append(
                                browser.getBrowserDisplayName());
                    }
                }
            }
            if (unixBrowsers.size() == 0) {
                // no browser installed
                throw new BrowserLaunchingInitializingException(
                        "one of the supported browsers must be installed: "
                        + potentialBrowserNames);
            }
            logger.info(unixBrowsers.keySet().toString());
            unixBrowsers = Collections.unmodifiableMap(unixBrowsers);
        }
        catch (IOException ioex) {
            throw new BrowserLaunchingInitializingException(ioex);
        }
    }

    /**
     * Opens a url in one of the available browsers.
     *
     * @param urlString String
     * @throws BrowserLaunchingExecutionException
     */
    public void openUrl(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        try {
            logger.info(urlString);
            boolean success = false;
            // get list of browsers to try
            List unixBrowsersList = new ArrayList(unixBrowsers.values());
            // check system property which may contain user's preferred browser
            String browserId = System.getProperty(
                    IBrowserLaunching.BROWSER_SYSTEM_PROPERTY,
                    null);
            if (browserId != null) {
                UnixBrowser unixBrowser =
                        (UnixBrowser) unixBrowsers.get(browserId);
                if (unixBrowser != null) {
                    // if user has preferred browser, place at start of list
                    unixBrowsersList.add(0, unixBrowser);
                }
            }
            // iterate over browsers until one works
            Iterator iter = unixBrowsersList.iterator();
            UnixBrowser browser;
            Process process;
            while (iter.hasNext() && !success) {
                browser = (UnixBrowser) iter.next();
                success = openUrlWithBrowser(browser,
                                             urlString);
            }
        }
        catch (Exception e) {
            throw new BrowserLaunchingExecutionException(e);
        }
    }

    /**
     * Opens a url in the specified browser. If the call to the
     * specified browser fails, the method falls through to the
     * non-targetted version.
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
            BrowserLaunchingInitializingException {
        UnixBrowser unixBrowser = (UnixBrowser) unixBrowsers.get(browser);
        if (unixBrowser == null ||
            IBrowserLaunching.BROWSER_DEFAULT.equals(browser)) {
            logger.debug("falling through to non-targetted openUrl");
            openUrl(urlString);
        }
        else {
            boolean success = openUrlWithBrowser(unixBrowser,
                                                 urlString);
            if (!success) {
                logger.debug(
                        "open browser failure, trying non-targetted openUrl");
                openUrl(urlString);
            }
        }
    }

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
            BrowserLaunchingInitializingException {
        if (browsers == null || browsers.isEmpty()) {
            logger.debug("falling through to non-targetted openUrl");
            openUrl(urlString);
        }
        else {
            boolean success = false;
            Iterator iter = browsers.iterator();
            while (iter.hasNext() && !success) {
                UnixBrowser unixBrowser = (UnixBrowser) unixBrowsers.get(
                        iter.next());
                if (unixBrowser != null) {
                    success = openUrlWithBrowser(unixBrowser,
                                                 urlString);
                }
            }
            if (!success) {
                logger.debug(
                        "none of listed browsers succeeded; falling through to non-targetted openUrl");
                openUrl(urlString);
            }
        }
    }

    /**
     * Returns a list of browsers to be used for browser
     * targetting. This list will always contain at least
     * one item--the BROWSER_DEFAULT.
     *
     * @return List
     */
    public List getBrowserList() {
        List browsers = new ArrayList();
        // add Default if not present
        if (!unixBrowsers.containsKey(IBrowserLaunching.BROWSER_DEFAULT)) {
            browsers.add(IBrowserLaunching.BROWSER_DEFAULT);
        }
        browsers.addAll(unixBrowsers.keySet());
        return browsers;
    }

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
     * Most browsers on Unix/Linux systems have command line options to
     * support this feature.
     *
     * @return boolean
     */
    public boolean getNewWindowPolicy() {
        return forceNewWindow;
    }

    /**
     * Sets the policy used for opening a url in a browser.
     *
     * @param forceNewWindow boolean
     */
    public void setNewWindowPolicy(boolean forceNewWindow) {
        this.forceNewWindow = forceNewWindow;
    }
}
