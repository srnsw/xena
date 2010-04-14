/**************************************************************************
/* This class wraps return-code and Exceptions of a terminated process.
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

package org.im4java.process;

/**
   This class  wraps return-code and Exceptions of a terminated process. 

   @version $Revision$
   @author  $Author$
 */

public class ProcessEvent {
  
  ////////////////////////////////////////////////////////////////////////////
  
  /**
    The return-code of the process. Note that this field is only valid, if
    no exception occured.
  */
  
  private int iReturnCode=Integer.MIN_VALUE;
  
  ////////////////////////////////////////////////////////////////////////////
  
  /**
    If this field is not null, the process ended with this exception.
  */
  
  private Exception iException=null;

  ///////////////////////////////////////////////////////////////////////////////
  
  /**
   * Default constructor.
  */
  
  public ProcessEvent() {
  }

  ///////////////////////////////////////////////////////////////////////////////

  /**
    @param pReturnCode the iReturnCode to set
  */
  public void setReturnCode(int pReturnCode) {
    iReturnCode = pReturnCode;
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /**
    @return the iReturnCode
  */
  public int getReturnCode() {
    return iReturnCode;
  }

  /////////////////////////////////////////////////////////////////////////////
  
  /**
    @param pException the iException to set
  */
  public void setException(Exception pException) {
    iException = pException;
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /**
    @return the iException
  */
  public Exception getException() {
    return iException;
  }
}
