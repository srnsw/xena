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
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 7/12/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.util.logging;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Logging handler which will display the given messages in a LogFrame
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class LogFrameHandler extends Handler {
	private LogFrame logFrame;
	private boolean handlerClosed = false;

	/**
	 * Creates and initialises a new LogFrameHandler. Messages
	 * will be added to the given LogFrame.
	 * The message formatting will be handled by a SimpleFormatter.
	 * @param logFrame
	 */
	public LogFrameHandler(LogFrame logFrame) {
		super();
		this.logFrame = logFrame;
	}

	@Override
	/**
	 * Append the given LogRecord to the LogFrame
	 */
	public void publish(LogRecord record) {
		if (!handlerClosed) {
			logFrame.addText("[" + new Date(record.getMillis()) + "] - " + record.getMessage() + "\n");
		}
	}

	@Override
	public void flush() {
		logFrame.validate();
		logFrame.repaint();
	}

	@Override
	public void close() throws SecurityException {
		logFrame = null;
		handlerClosed = true;
	}

}
