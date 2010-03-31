/**************************************************************************
/* This class wraps the ImageMagick command-set.
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
   This class wraps the ImageMagick command-set. There should be no
   need to use this class, since for all IM-commands there are
   class-wrappers available (e.g. ConvertCmd) which are more specific
   and provide extended functionality.

   @version $Revision$
   @author  $Author$
*/

public class ImageMagickCmd extends ImageCommand {

 //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor. Set the system-property im4java.useGM to true to switch
     at runtime to GraphicsMagick.
  */

  public  ImageMagickCmd(String pCommand) {
    super();
    if (!Boolean.getBoolean("im4java.useGM")) {
      setCommand(pCommand);
    } else {
      setCommand("gm",pCommand);
    }
  }
}
