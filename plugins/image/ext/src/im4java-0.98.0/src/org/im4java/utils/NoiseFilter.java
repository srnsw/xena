/**************************************************************************
/* Base class for noise-remover variants.
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

package org.im4java.utils;

import org.im4java.core.*;

/**
   This class is the base class of all   noise-remover classes.

   @version $Revision$
   @author  $Author$
*/

public class NoiseFilter extends IMOperation {

 //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor: creates a clone of the image and apply the operation
     (typically -despeckle, -blur or -noise).
  */

  public NoiseFilter(IMOperation pOperation) {
    openOperation();
    clone(0);
    addOperation(pOperation);
    closeOperation();
  }

 //////////////////////////////////////////////////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////

  public static class Edge extends NoiseFilter {

    ///////////////////////////////////////////////////////////////////////////

    /**
       Constructor.
    */

      public Edge(IMOperation pOperation, double pRadius) {

	// blurred version as a clone
	super(pOperation);

	// create the mask (also a clone)
	openOperation();
	clone(0);
	edge(pRadius);
	negate();
	closeOperation();

        // compose original and blurred version using the mask
	compose("Over");
	composite();
      }
  }

 //////////////////////////////////////////////////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////

  public static class Threshold extends NoiseFilter {

    ///////////////////////////////////////////////////////////////////////////

    /**
       Constructor (pass blackpoint and whitepoint as percentages)
    */

      public Threshold(IMOperation pOperation, double pBlackPoint,
                                                          double pWhitePoint) {

	// blurred version as a clone
	super(pOperation);

	// create the mask (also a clone)
	openOperation();
	clone(0);
	blackThreshold(pBlackPoint);
	whiteThreshold(pWhitePoint);
	negate();
	closeOperation();

        // compose original and blurred version using the mask
	compose("Over");
	composite();
      }
  }

 //////////////////////////////////////////////////////////////////////////////
 //////////////////////////////////////////////////////////////////////////////

  public static class Level extends NoiseFilter {

    ///////////////////////////////////////////////////////////////////////////

    /**
       Constructor (pass blackpoint and whitepoint as percentages)
    */

      public Level(IMOperation pOperation, double pBlackPoint,
                                           double pWhitePoint, double pGamma) {

	// blurred version as a clone
	super(pOperation);

	// create the mask (also a clone)
	openOperation();
	clone(0);
	level(pBlackPoint,pWhitePoint,true,pGamma);
	negate();
	closeOperation();

        // compose original and blurred version using the mask
	compose("Over");
	composite();
      }
  }
}
