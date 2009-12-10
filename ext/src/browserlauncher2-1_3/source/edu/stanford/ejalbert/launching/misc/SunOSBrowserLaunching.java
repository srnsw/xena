/************************************************
    Copyright 2005,2006 Olivier Hochreutiner, Jeff Chapman

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

import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import net.sf.wraplog.AbstractLogger;

/**
 * Launches a default browser on SunOS Unix systems using the sdtwebclient
 * command.
 *
 * @author Olivier Hochreutiner
 */
public class SunOSBrowserLaunching
        extends UnixNetscapeBrowserLaunching {
    /**
     * config file for SunOS.
     */
    public static final String CONFIGFILE_SUNOS =
            "/edu/stanford/ejalbert/launching/misc/sunOSConfig.properties";

    /**
     * Passes the logger and config file for SunOS to its
     * super class.
     *
     * @param logger AbstractLogger
     */
    public SunOSBrowserLaunching(AbstractLogger logger) {
        super(logger, CONFIGFILE_SUNOS);
    }

    /**
     * Opens a url using the default browser. It uses sdtwebclient
     * to launch the default browser. The sdtwebclient executable
     * is mapped to
     * {@link IBrowserLaunching.BROWSER_DEFAULT IBrowserLaunching.BROWSER_DEFAULT}.
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
            // check system property which may contain user's preferred browser
            String browserId = System.getProperty(
                    IBrowserLaunching.BROWSER_SYSTEM_PROPERTY,
                    null);
            StandardUnixBrowser defBrowser = getBrowser(
                    IBrowserLaunching.BROWSER_DEFAULT);
            if (browserId != null) {
                logger.info(
                        "browser pref defined in system prop. Failing over to super.openUrl() method");
                super.openUrl(urlString);
            }
            // we should always have a default browser defined for
            // SunOS but if not, fail over to super class method
            else if (defBrowser == null) {
                logger.info(
                        "no default browser defined. Failing over to super.openUrl() method");
                super.openUrl(urlString);
            }
            else {
                logger.info(defBrowser.getBrowserDisplayName());
                Process process = Runtime.getRuntime().exec(
                        defBrowser.getArgsForStartingBrowser(urlString));
                process.waitFor();
            }
        }
        catch (Exception e) {
            throw new BrowserLaunchingExecutionException(e);
        }
    }
}
