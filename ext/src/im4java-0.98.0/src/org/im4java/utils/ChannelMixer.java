/**************************************************************************
/* This class is a wrapper to a channel-mixer.
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
   This class is a wrapper to a channel mixer. Note that ImageMagick does
   not natively define a channel-mixer operation, but you can implement
   a channel-mixer with standard ImageMagick operations.

   @version $Revision$
   @author  $Author$
*/

public class ChannelMixer extends IMOperation {

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates a  yellow-filter.
  */

  public static final ChannelMixer YELLOW = new ChannelMixer(.60, .28, .12); 

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an orange-filter.
  */

  public static final ChannelMixer ORANGE = new ChannelMixer(.78, .22, 0);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates a red-filter.
  */

  public static final ChannelMixer RED = new ChannelMixer(.90, .10, 0);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates a green-filter.
  */

  public static final ChannelMixer GREEN = new ChannelMixer(.10, .70, .20);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an agfapan-25 film.
  */

  public static final ChannelMixer AGFAPAN_25 = new ChannelMixer(.25, .39, .36);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an agfapan-100 film.
  */

  public static final ChannelMixer
      AGFAPAN_100 = new ChannelMixer(.21, .40, .39);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an agfapan-400 film.
  */

  public static final ChannelMixer
      AGFAPAN_400 = new ChannelMixer(.20, .41, .39);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an agfa-200x film.
  */

  public static final ChannelMixer
      AGFA_200X = new ChannelMixer(.18, .41, .41);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an Ilford Delta400 film.
  */

  public static final ChannelMixer
      ILFORD_DELTA100 = new ChannelMixer(.21, .42, .37);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates a  film.
  */

  public static final ChannelMixer
      ILFORD_DELTA400 = new ChannelMixer(.22, .42, .36);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an Ilford Delta400-Pro film.
  */

  public static final ChannelMixer
      ILFORD_DELTA400_PRO = new ChannelMixer(.31, .36, .33);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an Ilford PANF film.
  */

  public static final ChannelMixer
      ILFORD_PANF = new ChannelMixer(.33, .36, .31);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an Ilford FP4 film.
  */

  public static final ChannelMixer ILFORD_FP4 = new ChannelMixer(.28, .41, .31);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an Ilford HP5 film.
  */

  public static final ChannelMixer ILFORD_HP5 = new ChannelMixer(.23, .37, .40);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an Ilford SFX film.
  */

  public static final ChannelMixer ILFORD_SFX = new ChannelMixer(.36, .31, .33);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an Ilford XP2 film.
  */

  public static final ChannelMixer  ILFORD_XP2 = new ChannelMixer(.21, .42, .37);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates a Kodak TMAX 100 film.
  */

  public static final ChannelMixer
      KODAK_TMAX100 = new ChannelMixer(.24, .37, .39);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates a Kodak TMAX 400 film.
  */

  public static final ChannelMixer
      KODAK_TMAX400 = new ChannelMixer(.27, .36, .37);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates a Kodak Tri-X film.
  */

  public static final ChannelMixer KODAK_TRIX = new ChannelMixer(.25, .35, .40);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer simulates an infrared film.
  */

  public static final ChannelMixer INFRARED = new ChannelMixer(1,1,-1);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer extracts the red-channel.
  */

  public static final ChannelMixer RED_CHANNEL = new ChannelMixer(1,0,0);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer extracts the green-channel.
  */

  public static final ChannelMixer GREEN_CHANNEL = new ChannelMixer(0,1,0);

  //////////////////////////////////////////////////////////////////////////////

  /**
     This channel-mixer extracts the blue-channel.
  */

  public static final ChannelMixer BLUE_CHANNEL = new ChannelMixer(0,0,1);

  //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
  */

  public ChannelMixer(double red, double green, double blue) {
    addRawArgs("-recolor",String.format("%g,%g,%g,%g,%g,%g,%g,%g,%g",
                                red,green,blue,
                                red,green,blue,
                                red,green,blue));
  }

}