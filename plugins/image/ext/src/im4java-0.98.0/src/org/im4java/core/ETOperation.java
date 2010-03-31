/**************************************************************************
/* This class models the command-line of exiftool.
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
   This class models the command-line of exiftool. It extends the
   auto-generated class ETOps with a number of methods for tag retrival
   and tag manipulation. Since it is not efficient to translate the
   original exiftool commandline-options directly to java-methods,
   the methods of this class don't follow the usual 1:1 relationship
   between method-names and commandline option-names.

   @version $Revision$
   @author  $Author$
*/

public class ETOperation extends ETOps {

  //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
  */

  public ETOperation() {
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Query all tags with the given tag-names. This method translates to
     the -TAG commandline options.
     See the exiftool documentation for details.
  */

  public ETOperation getTags(String... pTags) {
    for (String tag:pTags) {
      iCmdArgs.add("-"+tag);
    }
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Omit all tags with the given tag-names. This method translates to
     the --TAG commandline options.
     See the exiftool documentation for details.
  */

  public ETOperation omitTags(String... pTags) {
    for (String tag:pTags) {
      iCmdArgs.add("--"+tag);
    }
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Set all tags with the given tag-names. This method translates to
     the -TAG[+-][&lt;]=Value commandline options.
     See the exiftool documentation for details.

     @param pTagExpressions A list in the form Tag[+-]=[&lt;][Value],...
  */

  public ETOperation setTags(String... pTagExpressions) {
    for (String exp:pTagExpressions) {
      iCmdArgs.add("-"+exp);
    }
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Set tag with the content read from a file.
     This method translates to the -TAG<=Filename commandline option.
     See the exiftool documentation for details.

     @param pTag      A tag name
     @param pFilename A filename or filename-template
  */

  public ETOperation setTag(String pTag, String pFilename) {
    iCmdArgs.add("-"+pTag+"<="+pFilename);
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Delete all tags with the given tag-names. This method translates to
     the -TAG= commandline options. Using the setTags()-methods with
     omitted values has the same effect.
     See the exiftool documentation for details.
  */

  public ETOperation delTags(String... pTags) {
    for (String tag:pTags) {
      iCmdArgs.add("-"+tag+"=");
    }
    return this;
  }

}
