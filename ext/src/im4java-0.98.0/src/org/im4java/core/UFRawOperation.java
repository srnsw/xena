/**************************************************************************
/* This class models the command-line of ufraw/ufraw-batch.
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
   This class models the command-line of ufraw/ufraw-batch.

   @version $Revision$
   @author  $Author$
*/

public class UFRawOperation extends UFRawOps {

  //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
  */

  public UFRawOperation() {
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     --exposure= with String-argument only accepts "auto".
  */

  public UFRawOps exposure(String pAuto) {
    if (pAuto.equals("auto")) {
      return super.exposure(pAuto);
    } else {
      throw new IllegalArgumentException();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     --black-point= with String-argument only accepts "auto".
  */

  public UFRawOps blackPoint(String pAuto) {
    if (pAuto.equals("auto")) {
      return super.blackPoint(pAuto);
    } else {
      throw new IllegalArgumentException();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Method to set all crop-values at once.
  */

  public UFRawOperation crop(Integer pLeft, Integer pRight, Integer pTop, 
                                                              Integer pBottom) {
    if (pLeft != null) {
      cropLeft(pLeft);
    }
    if (pRight != null) {
      cropRight(pRight);
    }
    if (pTop != null) {
      cropTop(pTop);
    }
    if (pBottom != null) {
      cropBottom(pBottom);
    }
    return this;
  }
}
