/**************************************************************************
/* This class wraps exceptions during command execution.
/*
/* Copyright (c) 2009 by Bernhard Bablok (mail@bablokb.de)
/*
/* This program is free software; you can redistribute it and/or modify
/* it under the terms of the GNU Library General Public License as published
/* by  the Free Software Foundation; either version 2 of the License or
/* (at your option) any later version.
/*
/* This program is distributed in the hope that it will be useful, but
/* WITHOUT ANY WARRANTY; without even the implied warranty of
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/* GNU Library General Public License for more details.
/*
/* You should have received a copy of the GNU Library General Public License
/* along with this program; see the file COPYING.LIB.  If not, write to
/* the Free Software Foundation Inc., 59 Temple Place - Suite 330,
/* Boston, MA  02111-1307 USA
/**************************************************************************/

package org.im4java.core;

import java.util.ArrayList;

/**
   This class wraps exceptions during image-attribute retrivial.

   @version $Revision$
   @author  $Author$
 */

@SuppressWarnings("serial")
public class CommandException extends IM4JavaException {

	//////////////////////////////////////////////////////////////////////////////

	/**
    The stderr-output of the command.
	 */

	private ArrayList<String> iErrorText = new ArrayList<String>();

	//////////////////////////////////////////////////////////////////////////////

	/**
     Constructor.
	 */

	public  CommandException() {
		super();
	}

	//////////////////////////////////////////////////////////////////////////////

	/**
     Constructor.
	 */

	public  CommandException(String pMessage) {
		super(pMessage);
	}

	//////////////////////////////////////////////////////////////////////////////

	/**
     Constructor.
	 */

	public  CommandException(String pMessage, Throwable pCause) {
		super(pMessage,pCause);
	}

	//////////////////////////////////////////////////////////////////////////////

	/**
     Constructor.
	 */

	public  CommandException(Throwable pCause) {
		super(pCause);
	}

	//////////////////////////////////////////////////////////////////////////////

	/**
     Return the error-text object.
	 */

	public ArrayList<String> getErrorText() {
		return iErrorText;
	}


///////////////////////////////////////////////////////////////////////////////

	/**
	 * Set the error text of this exception.
       
       @param pErrorText	
	 */

	public void setErrorText(ArrayList<String> pErrorText) {
		iErrorText = pErrorText;
	}
}
