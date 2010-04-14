/************************************************
    Copyright 2007 Jeff Chapman

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
package edu.stanford.ejalbert.launching.utils;

/**
 *
 * @author not attributable
 * @version 1.0
 */
public class LaunchingUtils {
    private static final String REPLACE_BROWSER = "<browser>";
    private static final String REPLACE_URL = "<url>";

    public static String replaceArgs(String commands,
                                     String browserArg,
                                     String urlArg) {
        // replace <browser> placeholder if browserArg passed
        if(browserArg != null) {
            commands = commands.replaceAll(REPLACE_BROWSER, browserArg);
        }
        // replace <url> placeholder if urlArg passed
        if(urlArg != null) {
            int urlPos = commands.indexOf(REPLACE_URL);
            StringBuffer buf = new StringBuffer();
            while (urlPos > 0) {
                buf.append(commands.substring(0, urlPos));
                buf.append(urlArg);
                buf.append(commands.substring(urlPos + REPLACE_URL.length()));
                commands = buf.toString();
                urlPos = commands.indexOf(REPLACE_URL);
                buf.setLength(0);
            }
        }
        return commands;
    }
}
