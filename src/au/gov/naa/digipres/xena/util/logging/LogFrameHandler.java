/*
 * Created on 7/12/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.util.logging;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Logging handler which will display the given messages in a LogFrame
 * @author justinw5
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class LogFrameHandler extends Handler
{
	private LogFrame logFrame;
	private boolean handlerClosed = false;
	
	/**
	 * Creates and initialises a new LogFrameHandler. Messages
	 * will be added to the given LogFrame.
	 * The message formatting will be handled by a SimpleFormatter.
	 * @param logFrame
	 */
	public LogFrameHandler(LogFrame logFrame)
	{
		super();
		this.logFrame = logFrame;
	}

	@Override
	/**
	 * Append the given LogRecord to the LogFrame
	 */
	public void publish(LogRecord record)
	{
		if (!handlerClosed)
		{
			logFrame.addText("[" + new Date(record.getMillis()) + "] - " + record.getMessage() + "\n");
		}
	}

	@Override
	public void flush()
	{
		logFrame.validate();
		logFrame.repaint();
	}

	@Override
	public void close() throws SecurityException
	{
		logFrame = null;
		handlerClosed = true;
	}

}
