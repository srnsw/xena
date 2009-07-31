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
package edu.stanford.ejalbert.launching.windows;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import at.jta.RegistryErrorException;
import at.jta.Regor;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import net.sf.wraplog.AbstractLogger;
import edu.stanford.ejalbert.launching.utils.LaunchingUtils;

/**
 * Handles initialization, configuration, and calls to open a url.
 *
 * @author Markus Gebhard, Jeff Chapman, Chris Dance
 */
public class WindowsBrowserLaunching
        implements IBrowserLaunching {
    /**
     * windows configuration file -- info on commands and browsers
     */
    private static final String CONFIGFILE_WINDOWS =
            "/edu/stanford/ejalbert/launching/windows/windowsConfig.properties";
    /**
     * config file key for Windows Vista
     */
    public static final String WINKEY_WINVISTA = "windows.winVista";
    /**
     * config file key for Windows 2000
     */
    public static final String WINKEY_WIN2000 = "windows.win2000";
    /**
     * config file key for Windows 9x
     */
    public static final String WINKEY_WIN9X = "windows.win9x";
    /**
     * config file key for Windows NT
     */
    public static final String WINKEY_WINNT = "windows.winNT";
    /**
     * collects valid config keys for key validation
     */
    private static final String[] WIN_KEYS = {
                                             WINKEY_WIN2000,
                                             WINKEY_WIN9X,
                                             WINKEY_WINNT,
                                             WINKEY_WINVISTA};
    static {
        Arrays.sort(WIN_KEYS);
    }

    protected final AbstractLogger logger; // in ctor

    /**
     * Maps display name and exe name to {@link WindowsBrowser WindowsBrowser}
     * objects. Using name and exe as keys for backward compatiblity.
     */
    private Map browserNameAndExeMap = null;

    /**
     * List of {@link WindowsBrowser WindowsBrowser} objects that
     * will be used to determine which browsers are available
     * on the machine. The list is created from the windows
     * config file.
     */
    private List browsersToCheck = new ArrayList();

    /**
     * Arguments for starting the default browser.
     */
    private String commandsDefaultBrowser; // in initialize
    /**
     * Arguments for starting a specific browser.
     */
    private String commandsTargettedBrowser; // in initialize
    /**
     * The key for accessing information from the windows config
     * file for a particular version of windows.
     * @see WINKEY_WIN2000
     * @see WINKEY_WIN9X
     * @see WINKEY_WINNT
     */
    private final String windowsKey; // in ctor
    /**
     * new window policy to apply when opening a url. If true,
     * try to force url into a new browser instance/window.
     */
    private boolean forceNewWindow = false;

    /**
     * set from properties to determine if the registry
     * should be consulted for available browsers.
     * Vista does not allow universal access to the registry.
     */
    private boolean useRegistry = false;

    private String programFilesFolderTemplate;
    private String driveLetters;

    // constants defined for accessing and processing registry information
    //private static final int REGEDIT_TYPE_APPPATHS = 0;
    //private static final int REGEDIT_TYPE_UNINSTALL = 1;
    //private static String[] regeditQueries = {
    //                                         "\"HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\""};
    //"\"HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\""};

    /**
     * Checks that the windows key is valid.
     *
     * @param logger AbstractLogger
     * @param windowsKey String
     */
    public WindowsBrowserLaunching(AbstractLogger logger,
                                   String windowsKey) {
        if (windowsKey == null) {
            throw new IllegalArgumentException("windowsKey cannot be null");
        }
        if (Arrays.binarySearch(WIN_KEYS, windowsKey) < 0) {
            throw new IllegalArgumentException(windowsKey + " is invalid");
        }
        this.logger = logger;
        this.windowsKey = windowsKey;
        logger.info(windowsKey);
    }

    private String getArrayAsString(String[] array) {
        return Arrays.asList(array).toString();
    }

    /**
     * Returns the protocol for the url.
     *
     * @param urlString String
     * @return String
     * @throws MalformedURLException
     */
    private String getProtocol(String urlString)
            throws MalformedURLException {
        URL url = new URL(urlString);
        return url.getProtocol();
    }

    /**
     * Returns map of browser names and exe names to
     * {@link WindowsBrowser WindowsBrowser} objects.
     * <p>
     * This is the preferred method for accessing the browser name and exe map.
     * @return Map
     */
    private Map getBrowserMap() {
        // Handles lazy instantiation of available browser map.
        synchronized (WindowsBrowserLaunching.class) {
            if (browserNameAndExeMap == null) {
                browserNameAndExeMap = new HashMap();
                // pull additional browsers from system property??
                // ---------
                // create temporary list of browsers to check to track which
                // ones have been found
                // we will remove items from this temp list
                List tempBrowsersToCheck = new ArrayList(browsersToCheck);
                // first try the registry
                if (useRegistry) {
                    browserNameAndExeMap.putAll(
                            getAvailableBrowsers(tempBrowsersToCheck));
                }
                // if there are still browsers to find, try file path
                if (!tempBrowsersToCheck.isEmpty()) {
                    browserNameAndExeMap.putAll(
                            processFilePathsForBrowsers(tempBrowsersToCheck));
                }
            }
        }
        return browserNameAndExeMap;
    }

    /**
     * Use program files folder template from properties file and
     * the list of drive letters from that properties file
     * @return File
     */
    private File getProgramFilesPath() {
        File progFilesPath = null;
        if (driveLetters != null && programFilesFolderTemplate != null) {
            String[] drives = driveLetters.split(";");
            for (int idx = 0; idx < drives.length && progFilesPath == null; idx++) {
                String path = MessageFormat.format(
                        programFilesFolderTemplate,
                        new Object[] {drives[idx]});
                File pfPath = new File(path);
                logger.debug(path);
                logger.debug(pfPath.getPath());
                if (pfPath.exists()) {
                    progFilesPath = pfPath;
                }
            }
        }
        return progFilesPath;
    }

    /**
     * Secondary method for browser discovery.
     * <p>
     * Uses IE to get the path to the Program Files directory;
     * then gets a list of the sub dirs and checks them against
     * the remaining browsers.
     *
     * @param iePath String
     * @param browsersAvailable Map
     * @param tmpBrowsersToCheck List
     */
    private Map processFilePathsForBrowsers(
            List tmpBrowsersToCheck) {
        logger.debug("finding available browsers in program files path");
        logger.debug("browsers to check: " + tmpBrowsersToCheck);
        Map browsersAvailable = new HashMap();
        File progFilesPath = getProgramFilesPath();
        if (progFilesPath != null) {
            logger.debug("program files path: " + progFilesPath.getPath());
            File[] subDirs = progFilesPath.listFiles(new DirFileFilter());
            int subDirsCnt = subDirs != null ? subDirs.length : 0;
            // create and populate map of dir names to win browser objects
            Iterator iter = tmpBrowsersToCheck.iterator();
            Map dirNameToBrowser = new HashMap();
            while (iter.hasNext()) {
                WindowsBrowser wBrowser = (WindowsBrowser) iter.next();
                dirNameToBrowser.put(wBrowser.getSubDirName(), wBrowser);
            }
            // iterate over subdirs and compare to map entries
            for (int idx = 0; idx < subDirsCnt && !tmpBrowsersToCheck.isEmpty();
                           idx++) {
                if (dirNameToBrowser.containsKey(subDirs[idx].getName())) {
                    WindowsBrowser wBrowser = (WindowsBrowser) dirNameToBrowser.
                                              get(
                            subDirs[idx].getName());
                    // need to search folder and sub-folders for exe to find
                    // the full path
                    String exeName = wBrowser.getBrowserApplicationName() +
                                     ".exe";
                    File fullPathToExe = findExeFilePath(
                            subDirs[idx],
                            exeName);
                    if (fullPathToExe != null) {
                        logger.debug("Adding browser " +
                                     wBrowser.getBrowserDisplayName() +
                                     " to available list.");
                        wBrowser.setPathToExe(fullPathToExe.getPath());
                        logger.debug(wBrowser.getPathToExe());
                        // adding display and exe for backward compatibility and
                        // ease of use if someone passes in the name of an exe
                        browsersAvailable.put(wBrowser.getBrowserDisplayName(),
                                              wBrowser);
                        browsersAvailable.put(wBrowser.
                                              getBrowserApplicationName(),
                                              wBrowser);
                        tmpBrowsersToCheck.remove(wBrowser);
                    }
                }
            }
        }
        return browsersAvailable;
    }

    private File findExeFilePath(File path, String exeName) {
        File exePath = null;
        File exeFiles[] = path.listFiles(new ExeFileNameFilter());
        if (exeFiles != null && exeFiles.length > 0) {
            for (int idx = 0; idx < exeFiles.length && exePath == null; idx++) {
                if (exeFiles[idx].getName().equalsIgnoreCase(exeName)) {
                    // found the exe, get parent
                    exePath = exeFiles[idx].getParentFile();
                }
            }
        }
        // didn't find the exe
        if (exePath == null) {
            File[] subDirs = path.listFiles(new DirFileFilter());
            if (subDirs != null && subDirs.length > 0) {
                for (int idx = 0; idx < subDirs.length && exePath == null; idx++) {
                    exePath = findExeFilePath(subDirs[idx], exeName);
                }
            }
        }
        return exePath;
    }

    /**
     * Filter used to only select directories.
     */
    private static final class DirFileFilter
            implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }


    /**
     * Filter used to only find exe files.
     */
    private static final class ExeFileNameFilter
            implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".exe");
        }
    }


    private Map getExeNamesToBrowsers(List tempBrowsersToCheck) {
        Map exeNamesToBrowsers = new HashMap();
        Iterator iter = tempBrowsersToCheck.iterator();
        while (iter.hasNext()) {
            WindowsBrowser winBrowser = (WindowsBrowser) iter.next();
            String exeName = winBrowser.getBrowserApplicationName().
                             toLowerCase() + ".exe";
            exeNamesToBrowsers.put(exeName, winBrowser);
        }
        return exeNamesToBrowsers;
    }

    private WindowsBrowser getBrowserFromRegistryEntry(
            Regor regor,
            int key,
            String subKey,
            String exeKey,
            Map exesToBrowserObjs)
            throws RegistryErrorException {
        WindowsBrowser winBrowser = null;
        int key2 = regor.openKey(key, subKey);
        List values = regor.listValueNames(key2);
        //boolean fndPath = false;
        for (int x = 0;
                     values != null && x < values.size() && winBrowser == null;
                     x++) {
            byte[] buf = regor.readValue(
                    key2,
                    (String) values.get(x));
            String path = buf != null ? Regor.parseValue(buf) :
                          "";
            String lpath = path.toLowerCase();
            if (lpath.endsWith(exeKey)) {
                winBrowser = (WindowsBrowser)
                             exesToBrowserObjs.get(exeKey);
                // get path to exe and set it in winBrowser object
                StringTokenizer tokenizer =
                        new StringTokenizer(path, "\\", false);
                StringBuffer pathBuf = new StringBuffer();
                int tokCnt = tokenizer.countTokens();
                // we want to ignore the last token
                for (int idx = 1; idx < tokCnt; idx++) {
                    pathBuf.append(tokenizer.nextToken());
                    pathBuf.append('\\');
                }
                winBrowser.setPathToExe(pathBuf.toString());
            }
        }
        return winBrowser;
    }

    /**
     * Accesses the Windows registry to look for browser exes. The
     * browsers search for are in the browsersToCheck list. The returned
     * map will use display names and exe names as keys to the
     * {@link WindowsBrowser WindowsBrowser} objects.
     *
     * @param browsersToCheck List
     * @return Map
     */
    private Map getAvailableBrowsers(List tempBrowsersToCheck) {
        logger.debug("finding available browsers using registry");
        logger.debug("browsers to check: " + tempBrowsersToCheck);
        Map browsersAvailable = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        try {
            // create map of exe names to win browser objects
            Map exesToBrowserObjs = getExeNamesToBrowsers(tempBrowsersToCheck);
            // access and look in registry
            Regor regor = new Regor();
            String subKeyName =
                    "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths";
            int key = regor.openKey(Regor.HKEY_LOCAL_MACHINE,
                                    subKeyName);
            if (key > -1) {
                List keys = regor.listKeys(key);
                Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
                Iterator keysIter = exesToBrowserObjs.keySet().iterator();
                while (keysIter.hasNext()) {
                    String exeKey = (String) keysIter.next();
                    int index = Collections.binarySearch(
                            keys,
                            exeKey,
                            String.CASE_INSENSITIVE_ORDER);
                    if (index >= 0) {
                        WindowsBrowser winBrowser = getBrowserFromRegistryEntry(
                                regor,
                                key,
                                (String) keys.get(index),
                                exeKey,
                                exesToBrowserObjs);
                        if (winBrowser != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Adding browser " +
                                             winBrowser.
                                             getBrowserDisplayName() +
                                             " to available list.");
                                logger.debug(winBrowser.getPathToExe());
                            }
                            // adding display and exe for backward compatibility and
                            // ease of use if someone passes in the name of an exe
                            browsersAvailable.put(winBrowser.
                                                  getBrowserDisplayName(),
                                                  winBrowser);
                            browsersAvailable.put(winBrowser.
                                                  getBrowserApplicationName(),
                                                  winBrowser);
                            tempBrowsersToCheck.remove(winBrowser);
                        }
                    }
                }
            }
        }
        catch (RegistryErrorException ex) {
            logger.error("problem accessing registry", ex);
        }
        return browsersAvailable;
    }

    /**
     * Returns the windows arguments for launching a default browser.
     *
     * @param protocol String
     * @param urlString String
     * @return String[]
     */
    private String[] getCommandArgs(String protocol,
                                    String urlString) {
        String commandArgs = LaunchingUtils.replaceArgs(
                commandsDefaultBrowser,
                null,
                urlString);
        return commandArgs.split("[ ]");
    }

    /**
     * Returns the windows arguments for launching a specified browser.
     * <p>
     * Depending on the forceNewWindow boolean, the args may also contain the
     * args to force a new window.
     *
     * @param protocol String
     * @param winbrowser WindowsBrowser
     * @param urlString String
     * @param forceNewWindow boolean
     * @return String[]
     */
    private String getCommandArgs(String protocol,
                                  WindowsBrowser winbrowser,
                                  String urlString,
                                  boolean forceNewWindow) {
        String commandArgs = LaunchingUtils.replaceArgs(
                commandsTargettedBrowser,
                winbrowser.getBrowserApplicationName(),
                urlString);
        String args = "";
        if (forceNewWindow) {
            args = winbrowser.getForceNewWindowArgs();
        }
        commandArgs = commandArgs.replaceAll("<args>", args);
        int pathLoc = commandArgs.indexOf("<path>");
        if (pathLoc > 0) {
            StringBuffer buf = new StringBuffer();
            buf.append(commandArgs.substring(0, pathLoc));
            buf.append(winbrowser.getPathToExe());
            buf.append(commandArgs.substring(pathLoc + 6));
            commandArgs = buf.toString();
        }
        return commandArgs; //.split("[ ]");
    }

    /**
     * Attempts to open a url with the specified browser. This is
     * a utility method called by the openUrl methods.
     *
     * @param winBrowser WindowsBrowser
     * @param protocol String
     * @param urlString String
     * @return boolean
     * @throws BrowserLaunchingExecutionException
     */
    private boolean openUrlWithBrowser(WindowsBrowser winBrowser,
                                       String protocol,
                                       String urlString)
            throws BrowserLaunchingExecutionException {
        boolean success = false;
        try {
            logger.info(winBrowser.getBrowserDisplayName());
            logger.info(urlString);
            logger.info(protocol);
            String args = getCommandArgs(
                    protocol,
                    winBrowser,
                    urlString,
                    forceNewWindow);
            if (logger.isDebugEnabled()) {
                logger.debug(args);
            }
            Process process = Runtime.getRuntime().exec(args);
            // This avoids a memory leak on some versions of Java on Windows.
            // That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
            process.waitFor();
            // some browsers (mozilla, firefox) return 1 if you attempt to
            // open a url and an instance of that browser is already running
            // not clear why because the call is succeeding, ie the browser
            // opens the url.
            // If we don't say 1 is also a success, we get two browser
            // windows or tabs opened to the url.
            //
            // We could make this check smarter in the future if we run
            // into problems. the winBrowser object could handle the
            // check to make it browser specific.
            int exitValue = process.exitValue();
            success = exitValue == 0 || exitValue == 1;
        }
        // Runtimes may throw InterruptedException
        // want to catch every possible exception and wrap it
        catch (Exception e) {
            throw new BrowserLaunchingExecutionException(e);
        }
        return success;
    }

    /* ----------------- from IBrowserLaunching -------------------- */

    /**
     * Initializes the browser launcher from the windows config
     * file. It initializes the browsers to check list and
     * the command line args to use for version of windows
     * referenced by the windowsKey.
     *
     * @see windowsKey
     * @throws BrowserLaunchingInitializingException
     */
    public void initialize()
            throws BrowserLaunchingInitializingException {
        try {
            URL configUrl = getClass().getResource(CONFIGFILE_WINDOWS);
            if (configUrl == null) {
                throw new BrowserLaunchingInitializingException(
                        "unable to find config file: " + CONFIGFILE_WINDOWS);
            }
            Properties configProps = new Properties();
            configProps.load(configUrl.openStream());
            // get sep char
            String sepChar = configProps.getProperty(PROP_KEY_DELIMITER);
            // load different types of browsers
            Iterator keysIter = configProps.keySet().iterator();
            while (keysIter.hasNext()) {
                String key = (String) keysIter.next();
                if (key.startsWith(PROP_KEY_BROWSER_PREFIX)) {
                    WindowsBrowser winBrowser = new WindowsBrowser(
                            sepChar,
                            configProps.getProperty(key));
                    browsersToCheck.add(winBrowser);
                }
            }
            // load the type of windows based on the windows key
            String windowsConfigStr = configProps.getProperty(
                    windowsKey,
                    null);
            if (windowsConfigStr == null) {
                throw new BrowserLaunchingInitializingException(
                        windowsKey + " is not a valid property");
            }
            String[] winConfigItems = windowsConfigStr.split(sepChar);
            commandsDefaultBrowser = winConfigItems[0];
            commandsTargettedBrowser = winConfigItems[1];
            Boolean boolVal = new Boolean(winConfigItems[2]);
            useRegistry = boolVal.booleanValue();
            // check for override of useRegistry from system prop
            // need to explicitly check BOTH values to filter out
            // invalid prop values
            String propValue = System.getProperty(
                    IBrowserLaunching.WINDOWS_BROWSER_DISC_POLICY_PROPERTY,
                    null);
            if (IBrowserLaunching.WINDOWS_BROWSER_DISC_POLICY_DISK.equals(
                    propValue)) {
                useRegistry = false;
            }
            else if (IBrowserLaunching.WINDOWS_BROWSER_DISC_POLICY_REGISTRY.
                     equals(propValue)) {
                useRegistry = true;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Browser discovery policy property value=" +
                             (propValue == null ? "null" : propValue));
                logger.debug("useRegistry=" + Boolean.toString(useRegistry));
            }
            // get info for checking Program Files folder
            programFilesFolderTemplate = configProps.getProperty(
                    "program.files.template",
                    null);
            driveLetters = configProps.getProperty(
                    "drive.letters",
                    null);
            // set brwosersToCheck to a non-modifiable list
            browsersToCheck = Collections.unmodifiableList(browsersToCheck);
        }
        catch (IOException ioex) {
            throw new BrowserLaunchingInitializingException(ioex);
        }
    }

    /**
     * Opens a url using the default browser.
     *
     * @param urlString String
     * @throws UnsupportedOperatingSystemException
     * @throws BrowserLaunchingExecutionException
     * @throws BrowserLaunchingInitializingException
     */
    public void openUrl(String urlString)
            throws UnsupportedOperatingSystemException,
            BrowserLaunchingExecutionException,
            BrowserLaunchingInitializingException {
        try {
            logger.info(urlString);
            String protocol = getProtocol(urlString);
            logger.info(protocol);
            // try the system prop first
            boolean successfullSystemPropLaunch = false;
            String browserName = System.getProperty(
                    IBrowserLaunching.BROWSER_SYSTEM_PROPERTY,
                    null);
            if (browserName != null) {
                Map browserMap = getBrowserMap();
                WindowsBrowser winBrowser = (WindowsBrowser) browserMap.get(
                        browserName);
                if (winBrowser != null) {
                    logger.debug("using browser from system property");
                    successfullSystemPropLaunch = openUrlWithBrowser(
                            winBrowser,
                            protocol,
                            urlString);
                }
            }
            if (!successfullSystemPropLaunch) {
                String[] args = getCommandArgs(protocol,
                                               urlString);
                if (logger.isDebugEnabled()) {
                    logger.debug(getArrayAsString(args));
                }
                Process process = Runtime.getRuntime().exec(args);
                // This avoids a memory leak on some versions of Java on Windows.
                // That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
                process.waitFor();
                process.exitValue();
            }
        }
        catch (Exception e) {
            logger.error("fatal exception", e);
            throw new BrowserLaunchingExecutionException(e);
        }
    }

    /**
     * Opens a url using a specific browser.
     * <p>
     * If the specified browser is not available, the method will
     * fall through to calling the default openUrl method.
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
        if (IBrowserLaunching.BROWSER_DEFAULT.equals(browser) ||
            browser == null) {
            logger.info(
                    "default or null browser target; falling through to non-targetted openUrl");
            openUrl(urlString);
        }
        else {
            Map browserMap = getBrowserMap();
            WindowsBrowser winBrowser = (WindowsBrowser) browserMap.get(browser);
            if (winBrowser == null) {
                logger.info("the available browsers list does not contain: " +
                            browser);
                logger.info("falling through to non-targetted openUrl");
                openUrl(urlString);
            }
            else {
                String protocol = null;
                try {
                    protocol = getProtocol(urlString);
                }
                catch (MalformedURLException malrulex) {
                    throw new BrowserLaunchingExecutionException(malrulex);
                }
                boolean successfullLaunch = openUrlWithBrowser(
                        winBrowser,
                        protocol,
                        urlString);
                if (!successfullLaunch) {
                    logger.debug("falling through to non-targetted openUrl");
                    openUrl(urlString);
                }
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
            String protocol = null;
            try {
                protocol = getProtocol(urlString);
            }
            catch (MalformedURLException malrulex) {
                throw new BrowserLaunchingExecutionException(malrulex);
            }
            Map browserMap = getBrowserMap();
            boolean success = false;
            Iterator iter = browsers.iterator();
            while (iter.hasNext() && !success) {
                WindowsBrowser winBrowser = (WindowsBrowser) browserMap.get(
                        iter.next());
                if (winBrowser != null) {
                    success = openUrlWithBrowser(winBrowser,
                                                 protocol,
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
     * Returns a list of browsers to be used for browser targetting.
     * This list will always contain at least one item--the BROWSER_DEFAULT.
     *
     * @return List
     */
    public List getBrowserList() {
        Map browserMap = getBrowserMap();
        List browsers = new ArrayList();
        browsers.add(IBrowserLaunching.BROWSER_DEFAULT);
        // exes are present in the map as well as display names
        Iterator iter = browserMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            WindowsBrowser winBrowser = (WindowsBrowser) browserMap.get(key);
            if (key.equals(winBrowser.getBrowserDisplayName())) {
                browsers.add(winBrowser.getBrowserDisplayName());
            }
        }
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
     * Some browsers on Windows systems have command line options to
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
