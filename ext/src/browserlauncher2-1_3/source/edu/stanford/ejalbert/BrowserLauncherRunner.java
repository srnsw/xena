/************************************************
    Copyright 2005,2006 Jeff Chapman

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
package edu.stanford.ejalbert;

import edu.stanford.ejalbert.exceptionhandler.BrowserLauncherErrorHandler;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import net.sf.wraplog.AbstractLogger;
import java.util.List;

/**
 * This is a convenience class to facilitate executing the browser launch in
 * a separate thread. This class is used from within BrowserLauncher
 * when handling calls to open a url.
 *
 * @author Jeff Chapman
 */
class BrowserLauncherRunner
        implements Runnable {
    private final List targetBrowsers; // in ctor
    private final String targetBrowser; // in ctor
    private final String url; // in ctor
    private final BrowserLauncherErrorHandler errorHandler; // in ctor
    private final IBrowserLaunching launcher; // in ctor
    private final AbstractLogger logger; // in ctor

    /**
     * Takes the items necessary for launching a browser and handling any
     * exceptions.
     *
     * @param launcher IBrowserLaunching
     * @param url String
     * @param logger AbstractLogger
     * @param errorHandler BrowserLauncherErrorHandler
     */
    BrowserLauncherRunner(IBrowserLaunching launcher,
                          String url,
                          AbstractLogger logger,
                          BrowserLauncherErrorHandler errorHandler) {
        this(launcher, null, null, url, logger, errorHandler);
    }

    BrowserLauncherRunner(IBrowserLaunching launcher,
                          String browserName,
                          String url,
                          AbstractLogger logger,
                          BrowserLauncherErrorHandler errorHandler) {
        this(launcher, browserName, null, url, logger, errorHandler);
    }

    BrowserLauncherRunner(IBrowserLaunching launcher,
                          List browserList,
                          String url,
                          AbstractLogger logger,
                          BrowserLauncherErrorHandler errorHandler) {
        this(launcher, null, browserList, url, logger, errorHandler);
    }

    /**
     * Takes the items necessary for launching a browser and handling any
     * exceptions.
     *
     * @param launcher IBrowserLaunching
     * @param browserName String
     * @param url String
     * @param logger AbstractLogger
     * @param errorHandler BrowserLauncherErrorHandler
     */
    private BrowserLauncherRunner(IBrowserLaunching launcher,
                                  String browserName,
                                  List browserList,
                                  String url,
                                  AbstractLogger logger,
                                  BrowserLauncherErrorHandler errorHandler) {
        if (launcher == null) {
            throw new IllegalArgumentException("launcher cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null.");
        }
        if (errorHandler == null) {
            throw new IllegalArgumentException("errorHandler cannot be null.");
        }
        if (logger == null) {
            throw new IllegalArgumentException("logger cannot be null");
        }
        this.targetBrowsers = browserList;
        this.launcher = launcher;
        this.url = url;
        this.targetBrowser = browserName;
        this.errorHandler = errorHandler;
        this.logger = logger;
    }

    /* ------------------- from Runnable -------------------- */

    /**
     * When an object implementing interface <code>Runnable</code> is used to
     * create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * This method will make the call to open the browser and display the
     * url. If an exception occurs, it will be passed to the instance of
     * BrowserLauncherErrorHandler that has been passed into the constructor.
     */
    public void run() {
        try {
            if (targetBrowser != null) {
                launcher.openUrl(targetBrowser,
                                 url);
            }
            else if(targetBrowsers != null) {
                launcher.openUrl(targetBrowsers,
                                 url);
            }
            else {
                launcher.openUrl(url);
            }
        }
        catch (Exception ex) {
            logger.error("fatal error opening url", ex);
            errorHandler.handleException(ex);
        }
    }
}
