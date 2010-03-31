/**************************************************************************
/* This class wraps the IM command display.
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

import java.io.IOException;

/**
   This class wraps the IM command display.

   @version $Revision$
   @author  $Author$
*/

public class DisplayCmd extends ImageCommand {

 //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
  */

  public  DisplayCmd() {
    super();
    if (!Boolean.getBoolean("im4java.useGM")) {
      setCommand("display");
    } else {
      setCommand("gm","display");
    }
  }

 //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor with option to use GraphicsMagick.
  */

  public  DisplayCmd(boolean useGM) {
    super();
    if (useGM) {
      setCommand("gm","display");
    } else {
      setCommand("display");
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Convinience method to show the image passed as an argument.
  */

  public static void show(String pImageName)   
                     throws IOException, InterruptedException, IM4JavaException {

	Operation displayOp = new Operation();
	displayOp.addImage(pImageName);
	DisplayCmd disp = new DisplayCmd();
	disp.run(displayOp);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Execute the command (replace given placeholders).
  */

  public void run(Operation pOperation, Object... images) 
                    throws IOException, InterruptedException, IM4JavaException {
    try {
      super.run(pOperation,images);
    } catch (CommandException ce) {
      // display always returns rc > 0
    }
  }
}
