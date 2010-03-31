/**************************************************************************
/* The base class for image-based commandline operations.
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

import java.util.LinkedList;
import java.util.List;

/**
   This class models the command-line of an image-command. 
   Objects of this class hold the arguments in a list and keep
   track of special "dynamic" operations.

   @version $Revision$
   @author  $Author$
*/

public  class Operation {

  //////////////////////////////////////////////////////////////////////////////

  /**
     Placeholder-string for images.
  */

  public final static String IMG_PLACEHOLDER = "?img?";

  //////////////////////////////////////////////////////////////////////////////

  /**
     Placeholder-string for dynamic operations.
  */

  final static String DOP_PLACEHOLDER = "?dop?";

  //////////////////////////////////////////////////////////////////////////////

  /**
     The list of command-line arguments.
  */

  LinkedList<String> iCmdArgs = null;

  //////////////////////////////////////////////////////////////////////////////

  /**
     DynamicOperations for this Operation.
  */

  private LinkedList<DynamicOperation> iDynamicOperations = null;

  //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
  */

  public Operation() {
    iCmdArgs           = new LinkedList<String>();
    iDynamicOperations = new LinkedList<DynamicOperation>();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Convert to String. Note that the arguments are not quoted!
  */

  public String toString() {
    StringBuffer buf = new StringBuffer();
    for (String arg:iCmdArgs) {
      buf.append(arg).append(" ");
    }
    return buf.toString();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Clone the (java) object. Note that ImageMagick has a -clone operator,
     therefore this class has a method clone() (inherited from  Core) which
     does not clone the java-object, but wraps the IM-clone operator!
  */

  public Operation cloneObject() {
    Operation op = new Operation();
    op.getCmdArgs().addAll(iCmdArgs);
    return op;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Add raw text to the list of arguments.
  */

  public Operation addRawArgs(String... pArgs) {
    for (String arg:pArgs) {
      iCmdArgs.add(arg);
    }
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Add raw text to the list of arguments.
  */

  public Operation addRawArgs(List<String> pArgs) {
    iCmdArgs.addAll(pArgs);
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Return the raw arguments.
  */

  public LinkedList<String> getCmdArgs() {
    return iCmdArgs;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Add image(s) to the operation.
  */

  public Operation addImage(String... pImages) {
    for (String img:pImages) {
      if (img != null) {
	iCmdArgs.add(img);
      }
    }
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Add an image-placeholder to an Operation.
  */

  public Operation addImage() {
    return addImage(1);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Add multiple image-placeholders to an Operation.
  */

  public Operation addImage(int n) {
    for (int i=0; i<n; ++i) {
      iCmdArgs.add(IMG_PLACEHOLDER);
    }
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Add an Operation.
  */

  public Operation addOperation(Operation pOperation) {
    return addRawArgs(pOperation.getCmdArgs());
  }


  ///////////////////////////////////////////////////////////////////////////////

  /**
   * Add a DynamicOperation to this Operation. We just save the DynamicOperation
   * in the internal list and add a placeholder for the operation.
   @param pOperation
   @return
  */

    public Operation addDynamicOperation(DynamicOperation pOperation) {
      iDynamicOperations.add(pOperation);
      iCmdArgs.add(DOP_PLACEHOLDER);
      return this;
    }


  ///////////////////////////////////////////////////////////////////////////////

    /**
     * Return the list of DynmicOperations.
     @return
    */

    public LinkedList<DynamicOperation> getDynamicOperations() {
      return iDynamicOperations;
    }
}
