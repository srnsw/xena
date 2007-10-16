/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.html.javatools.util;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;

/**
 *  A class for logging context specific messages to a file. Logged messages
 *  include the context (typically a class or subsystem), a date-time stamp a
 *  severity and supplied text. Entries are of the form: <P>
 *
 *  <code>yyyy.MM.dd HH:mm:ss <severity> <context name>: <message> </code> <P>
 *
 *  If the system property "debugOn" is set to TRUE the messages with a severity
 *  of DEBUG will be logged. If the property is missing or is FALSE then DEBUG
 *  messages will be ignored.
 *
 * @created    29 August 2001
 */

public class FileLog implements Log {
	static FileLog single;
	// Encapsulates the log file
	private PrintWriter log = null;

	// If debugOn is false then entries with DEBUG severity are ignored
	private int debugLevel;

	/**
	 *  Constructs a new log where entries are stored in a file.
	 *
	 * @param  path        Description of Parameter
	 * @param  debugLevel  Description of Parameter
	 */

	private FileLog(String path, int debugLevel) {
		init(path, debugLevel);
	}

	private FileLog() {
		String fileName = null;
		int level = INFO;
		try {
			Props props = Props.singleton("log");
			fileName = props.getProperty("logFileName");
			String debugLevelString = props.getProperty("debugLevel");
			if (debugLevelString != null) {
				for (int i = 0; i < names.length; i++) {
					if (debugLevelString.equals(names[i])) {
						level = i;
						break;
					}
				}
			}
		} catch (IOException e) {
			// Nothing. Will use defaults.
		}
		init(fileName, level);
	}

	public static FileLog singleton() {
		if (single == null) {
			synchronized (FileLog.class) {
				if (single == null) {
					single = new FileLog();
				}
			}
		}
		return single;
	}

	public void init(String path, int debugLevel) {
		this.debugLevel = debugLevel;
		String errorMsg = "Could not open " + path + " for write. " + "Messages will appear on standard out";
		try {
			if (path == null) {
				error("FileLog", errorMsg);
			} else {
				log = new PrintWriter(new FileOutputStream(path, true), true);
			}
		} catch (Exception e) {
			error("FileLog", errorMsg);
		}

	}

	public synchronized void log(int severity, String sID, String sMessage) {
		write(severity, sID, sMessage);
	}

	public synchronized void debug(String sID, String sMessage) {
		write(Log.DEBUG, sID, sMessage);
	}

	public synchronized void info(String sID, String sMessage) {
		write(Log.INFO, sID, sMessage);
	}

	public synchronized void warning(String sID, String sMessage) {
		write(Log.WARNING, sID, sMessage);
	}

	public synchronized void error(String sID, String sMessage) {
		write(Log.ERROR, sID, sMessage);
	}

	public synchronized void severe(String sID, String sMessage) {
		write(Log.SEVERE, sID, sMessage);
	}

	@Override
    public void finalize() {
		if (log != null) {
			log.flush();
			log.close();
		}
	}

	/**
	 *  Adds a log entry with a severity of <code>INFO</code>.
	 *
	 * @param  severity  Description of Parameter
	 * @param  sID       Description of Parameter
	 * @param  sMessage  Description of Parameter
	 */

	/**
	 *  Adds a log entry with a severity of <code>INFO</code>.
	 *
	 *  Adds a log entry with a severity of <code>INFO</code>. Adds a log entry
	 *  with a severity of <code>INFO</code>. Adds a log entry with a severity of
	 *  <code>INFO</code>. Adds a log entry with the specified severity.
	 *
	 * @param  severity  Description of Parameter
	 * @param  sID       Description of Parameter
	 * @param  sMessage  Description of Parameter
	 */

	synchronized void write(int severity, String sID, String sMessage) {
		if (severity < debugLevel) {
			return;
		}
		PrintWriter w;
		if (log == null) {
			w = new PrintWriter(System.out);
		} else {
			w = log;
		}
		// Write the message to a log file if it exists or standard out if not
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		w.println(sdf.format(date) + " (" + Thread.currentThread().getName() + ":" + names[severity] + ") " + sID + ": " + sMessage);
		w.flush();
	}
}
