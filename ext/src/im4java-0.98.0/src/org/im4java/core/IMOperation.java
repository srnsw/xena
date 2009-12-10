/**************************************************************************
/* This class models the command-line of ImageMagick.
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


/**
    This class models the command-line of ImageMagick.
    It extends the class IMOps and adds some utility-methods
   (like appendVertically()) not
   found in ImageMagick, mainly for ease of use. Subclasses of IMOperation
   implement more specific operations (e.g.  ChannelMixer).

   <p>If you want to switch between GraphicsMagick and ImageMagick at
   runtime (using the system-property im4java.useGM=true), you have to
   limit yourself to the subset of options
   common to both implementations.</p>

   @version $Revision$
   @author  $Author$
*/

public class IMOperation extends IMOps {

  //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
  */

  public IMOperation() {
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Open a sub-operation (add a opening parenthesis).
  */

  public IMOperation openOperation() {
    return (IMOperation) addRawArgs("(");
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Close a sub-operation (add a closing parenthesis).
  */

  public IMOperation closeOperation() {
    return (IMOperation) addRawArgs(")");
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Add a IMOperation as a suboperation.
  */

  public IMOperation addSubOperation(Operation pSubOperation) {
    openOperation();
    addRawArgs(pSubOperation.getCmdArgs());
    return closeOperation(); 
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Append images horizontally (same as +append)
  */

  public IMOperation appendHorizontally() {
    p_append();
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Append images vertically (same as -append)
  */

  public IMOperation appendVertically() {
    append();
    return this;
  }
}
