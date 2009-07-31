/************************************************
    Copyright 2004,2005,2006,2007 Jeff Chapman

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.wraplog.AbstractLogger;
import edu.stanford.ejalbert.launching.utils.LaunchingUtils;

/**
 * Contains information on a unix browser.
 */
class StandardUnixBrowser
        implements UnixBrowser {
    /**
     * name of browser for user display
     */
    private final String browserName; // in ctor
    /**
     * name of browser executable used to invoke it
     */
    private final String browserArgName; // in ctor
    /**
     * arguments used to address an already open browser.
     */
    private final String argsForOpenBrowser; // in ctor
    /**
     * arguments used for starting a new instance of a browser.
     */
    private final String argsForStartBrowser; // in ctor

    private final String argsForForcedBrowserWindow; // in ctor

    /**
     * Splits the config string using the configSep character.
     * The resulting config items are used to set the
     * browser display name, the browser executable name, and
     * the arguments for starting a new browser instance and
     * addressing an already open browser.
     *
     * @param configSep String
     * @param configStr String
     */
    StandardUnixBrowser(String configSep,
                        String configStr) {
        String[] configItems = configStr.split(configSep, -2);
        this.browserName = configItems[0];
        this.browserArgName = configItems[1];
        this.argsForStartBrowser = configItems[2];
        this.argsForOpenBrowser = configItems[3];
        if(configItems.length == 5) {
            this.argsForForcedBrowserWindow = configItems[4];
        }
        else {
            this.argsForForcedBrowserWindow = configItems[2];
        }
    }

    /**
     * Returns debug information.
     *
     * @return String
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("display name=");
        buf.append(browserName);
        buf.append(" executable name=");
        buf.append(browserArgName);
        buf.append(" argsForStartBrowser=");
        buf.append(argsForStartBrowser);
        buf.append(" argsForOpenBrowser=");
        buf.append(argsForOpenBrowser);
        return buf.toString();
    }

    /**
     * Replaces the placeholders <browser> and <url> in the
     * argsString and splits the resulting string around
     * space characters.
     *
     * @param argsString String
     * @param urlString String
     * @return String[]
     */
    private String[] getCommandLineArgs(String argsString,
                                        String urlString) {
        argsString = LaunchingUtils.replaceArgs(argsString,
                                                browserArgName,
                                                urlString);
        return argsString.split("[ ]");
    }

    /* --------------------- from BrowserDescription --------------------- */

    /**
     * Returns the display name for the browser.
     *
     * @return String
     */
    public String getBrowserDisplayName() {
        return browserName;
    }

    /**
     * Returns the executable name for the browser.
     *
     * @return String
     */
    public String getBrowserApplicationName() {
        return browserArgName;
    }

    /* ------------------------- from UnixBrowser ------------------------ */

    /**
     * Returns the command line arguments for addressing an already
     * open browser.
     *
     * @param urlString String
     * @return String[]
     */
    public String[] getArgsForOpenBrowser(String urlString) {
        String argsStartString = argsForOpenBrowser != null &&
                                 argsForOpenBrowser.length() > 0 ?
                                 argsForOpenBrowser : argsForStartBrowser;
        return getCommandLineArgs(argsStartString, urlString);
    }

    /**
     * Returns the command line arguments for starting a new browser
     * instance.
     *
     * @param urlString String
     * @return String[]
     */
    public String[] getArgsForStartingBrowser(String urlString) {
        return getCommandLineArgs(argsForStartBrowser, urlString);
    }

    public String[] getArgsForForcingNewBrowserWindow(String urlString) {
        return getCommandLineArgs(argsForForcedBrowserWindow, urlString);
    }

    /**
     * Returns true if the browser is available, ie which command finds it.
     *
     * @param logger AbstractLogger
     * @return boolean
     */
    public boolean isBrowserAvailable(AbstractLogger logger) {
        boolean isAvailable = false;
        try {
            Process process = Runtime.getRuntime().exec(new String[] {"which",
                    browserArgName});
            InputStream errStream = process.getErrorStream();
            InputStream inStream = process.getInputStream();
            BufferedReader errIn
                    = new BufferedReader(new InputStreamReader(errStream));
            BufferedReader in
                    = new BufferedReader(new InputStreamReader(inStream));
            String whichOutput = in.readLine();
            String whichErrOutput = errIn.readLine();
            in.close();
            errIn.close();
            if(whichOutput != null) {
                logger.debug(whichOutput);
            }
            if(whichErrOutput != null) {
                logger.debug(whichErrOutput);
            }
            isAvailable = whichOutput != null &&
                          whichOutput.startsWith("/");
        }
        catch (IOException ex) {
            logger.error("io error executing which command", ex);
        }
        return isAvailable;
    }
}
