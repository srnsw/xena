/*
 * StatusEvent.java
 * Copyright(C) 1999 Chris Burdess <dog@gnu.org>
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
 * 
 * GNU JavaMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */

package gnu.mail.treeutil;

import java.util.*;

/**
 * A status message.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0.2
 */
public class StatusEvent
	extends EventObject 
{

	public static final int OPERATION_START = 0;

	public static final int OPERATION_UPDATE = 1;

	public static final int OPERATION_END = 2;

	public static final int UNKNOWN = -1;

	protected int type;

	protected String operation;

	protected int minimum = UNKNOWN;
	
	protected int maximum = UNKNOWN;
	
	protected int value = UNKNOWN;
	
	/**
	 * Creates a new status event with the specified type and operation.
	 */
	public StatusEvent(Object source, int type, String operation) 
	{
		super(source);
		switch (type) 
		{
		  case OPERATION_START:
		  case OPERATION_UPDATE:
		  case OPERATION_END:
			this.type = type;
			break;
		  default:
			throw new IllegalArgumentException("Illegal event type: "+type);
		}
		this.operation = operation;
	}
	
	/**
	 * Creates a new status event representing an update of the specified operation.
	 */
	public StatusEvent(Object source, int type, String operation, int minimum, int maximum, int value) 
	{
		super(source);
		switch (type) 
		{
		  case OPERATION_START:
		  case OPERATION_UPDATE:
		  case OPERATION_END:
			this.type = type;
			break;
		  default:
			throw new IllegalArgumentException("Illegal event type: "+type);
		}
		this.operation = operation;
		this.minimum = minimum;
		this.maximum = maximum;
		this.value = value;
	}

	/**
	 * Returns the type of event(OPERATION_START, OPERATION_UPDATE, or OPERATION_END).
	 */
	public int getType()
	{
		return type; 
	}

	/**
	 * Returns a string describing the operation being performed.
	 */
	public String getOperation()
	{
		return operation; 
	}

	/**
	 * Returns the start point of the operation.
	 */
	public int getMinimum()
	{
		return minimum; 
	}

	/**
	 * Returns the end point of the operation.
	 */
	public int getMaximum()
	{
		return maximum; 
	}

	/**
	 * Returns the current point in the operation.
	 */
	public int getValue()
	{
		return value;
	}

}
