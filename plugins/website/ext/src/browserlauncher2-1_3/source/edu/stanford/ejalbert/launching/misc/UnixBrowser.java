/************************************************
    Copyright 2004,2006 Jeff Chapman

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

import edu.stanford.ejalbert.launching.BrowserDescription;
import net.sf.wraplog.AbstractLogger;

/**
 * Augments the standard browser description with information
 * specific to a Unix type browser.
 */
public interface UnixBrowser
        extends BrowserDescription {
    /**
     * Returns the command line arguments for addressing an already
     * open browser.
     *
     * @param urlString String
     * @return String[]
     */
    public String[] getArgsForOpenBrowser(String url);

    /**
     * Returns the command line arguments for starting a new browser
     * instance.
     *
     * @param urlString String
     * @return String[]
     */
    public String[] getArgsForStartingBrowser(String url);

    public String[] getArgsForForcingNewBrowserWindow(String url);


    /**
     * Returns true if the browser is available on the user's system..
     *
     * @param logger AbstractLogger
     * @return boolean
     */
    public boolean isBrowserAvailable(AbstractLogger logger);
}
