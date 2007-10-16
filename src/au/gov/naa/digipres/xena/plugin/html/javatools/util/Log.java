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

/**
 */
public interface Log {

	/** An informational message for programmers eyes only.*/
	public static final int DEBUG = 0;

	/** Informational message only to highlight an event of interest. */
	public static final int INFO = 1;

	/** An unexpected benign event which indicates a possible future error. */
	public static final int WARNING = 2;

	/** A recoverable error condition. */
	public static final int ERROR = 3;

	/** A fatal event which prevents the system or sub/system from continuing. */
	public static final int SEVERE = 4;

	// Display names for severity. For example names[ERROR] returns "ERROR"
	static final String[] names = {"DEBUG", "INFO", "WARNING", "ERROR", "SEVERE"};

	// public void write(String sID, String sMessage);

	// public void write(int severity, String sID, String sMessage)
	void log(int severity, String sID, String sMessage);

	void debug(String sID, String sMessage);

	void info(String sID, String sMessage);

	void warning(String sID, String sMessage);

	void error(String sID, String sMessage);

	void severe(String sID, String sMessage);
}
