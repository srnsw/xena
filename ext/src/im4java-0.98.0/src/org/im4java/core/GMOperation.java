/**************************************************************************
/* This class models the command-line of GraphicsMagick.
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
   This class models the command-line of GraphicsMagick. 
   It extends the class GMOps and adds some utility-methods
   (like appendVertically()) not found in GraphicsMagick, 
   mainly for ease of use.

   <p>If you want to switch between GraphicsMagick and ImageMagick at
   runtime (using the system-property im4java.useGM=true),
   you have to limit yourself to the subset of options
   common to both implementations.</p>

   @version $Revision$
   @author  $Author$
*/

public class GMOperation extends GMOps {

  //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
  */

  public GMOperation() {
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Append images horizontally (same as +append)
  */

  public GMOperation appendHorizontally() {
    p_append();
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Append images vertically (same as -append)
  */

  public GMOperation appendVertically() {
    append();
    return this;
  }
}
