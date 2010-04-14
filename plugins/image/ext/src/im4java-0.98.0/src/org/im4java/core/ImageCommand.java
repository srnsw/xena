/**************************************************************************
/* This class implements the processing of image-commands.
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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.imageio.ImageIO;


import org.im4java.process.ErrorConsumer;
import org.im4java.process.ProcessStarter;
import org.im4java.process.StandardStream;

/**
   This class implements the processing of image operations. It replaces
   placeholders within the argument-stack and passes all arguments to the
   generic run-method of ProcessStarter.

   @version $Revision$
   @author  $Author$
*/

public class ImageCommand extends ProcessStarter implements ErrorConsumer {

  //////////////////////////////////////////////////////////////////////////////

  /**
     The command (plus initial arguments) to execute.
  */

  private LinkedList<String> iCommands;

  //////////////////////////////////////////////////////////////////////////////

  /**
     List of stderr-output.
  */

  private ArrayList<String> iErrorText;

  //////////////////////////////////////////////////////////////////////////////

  /**
     List of temporary files (input).
  */

  private LinkedList<String> iTmpFiles;

  //////////////////////////////////////////////////////////////////////////////

  /**
     Temporary output file.
  */

  private String iTmpOutputFile;

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Constructor.
   */

  public ImageCommand() {
    super();
    iCommands = new LinkedList<String>();
    iTmpFiles = new LinkedList<String>();
    setOutputConsumer(StandardStream.STDOUT);
    setErrorConsumer(this);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Constructor setting the commands.
   */

  public ImageCommand(String... pCommands) {
    this();
    setCommand(pCommands);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Set the command.
   */

  public void setCommand(String... pCommands) {
    for (String cmd:pCommands) {
      iCommands.add(cmd);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Clear the command.
   */

  public void clearCommand() {
    iCommands.clear();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Execute the command (replace given placeholders).
     * @throws IM4JavaException 
     */

  public void run(Operation pOperation, Object... images) 
    throws IOException, InterruptedException, IM4JavaException {

    // prepare list of arguments
    LinkedList<String> args = new LinkedList<String>(pOperation.getCmdArgs());
    args.addAll(0,iCommands);
    resolveImages(args,images);
    resolveDynamicOperations(pOperation,args,images);

    int rc=run(args);
    removeTmpFiles();
    if (rc > 0) {
      CommandException ce = new CommandException();
      ce.setErrorText(iErrorText);
      throw ce;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Resolve images passed as arguments.
  */

  private void resolveImages(LinkedList<String> pArgs,Object... pImages) 
                                                            throws IOException {
    ListIterator<String> argIterator = pArgs.listIterator();
    int i = 0;
    for (Object obj:pImages) {
      // find the next placeholder
      while (argIterator.hasNext()) {
	if (argIterator.next().equals(Operation.IMG_PLACEHOLDER)) {
	  break;
	}
      }
      if (obj instanceof String) {
	argIterator.set((String) obj);
      } else if (obj instanceof BufferedImage) {
	if (i<pImages.length) {
	  // write BufferedImage to temporary file
	  // and replace the placeholder with the temporary file
	  String tmpFile = convert2TmpFile((BufferedImage) obj);
	  argIterator.set(tmpFile);
	  iTmpFiles.add(tmpFile);
	} else {
	  // special case: BufferedImage is last image, so just create name
	  iTmpOutputFile=getTmpFile();
	  argIterator.set(iTmpOutputFile);
	}
      } else {
	throw new IllegalArgumentException(obj.getClass().getName() +
					   " is an unsupported image-type");
      }
      i++;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Resolve DynamicOperations.
     @throws IM4JavaException 
  */

  private void resolveDynamicOperations(Operation pOp, LinkedList<String> pArgs,
                                     Object... pImages) throws IM4JavaException {
    ListIterator<String> argIterator = pArgs.listIterator();
    ListIterator<DynamicOperation> dynOps = 
      pOp.getDynamicOperations().listIterator();

    // iterate over all DynamicOperations
    while (dynOps.hasNext()) {
      DynamicOperation dynOp = dynOps.next();
      Operation op = dynOp.resolveOperation(pImages);

      // find the next placeholder
      while (argIterator.hasNext()) {
	if (argIterator.next().equals(Operation.DOP_PLACEHOLDER)) {
	  break;
	}
      }

      if (op == null) {
	// no operation
	argIterator.remove();		  
      } else {
	List<String> args = dynOp.resolveOperation(pImages).getCmdArgs();
	if (args == null) {
	  // empty operation, remove placeholder
	  argIterator.remove();
	} else {
	  // remove placeholder and add replacement
	  argIterator.remove();
	  for (String arg:args) {
	    argIterator.add(arg);
	  }
	}
      }
    }  // while (dynOps.hasNext())
  }
  
  //////////////////////////////////////////////////////////////////////////////
    
  /**
     This method just saves the stderr-output into an internal field.
     
     @see org.im4java.process.ErrorConsumer#consumeError(java.io.InputStream)
  */
    
  public void consumeError(InputStream pInputStream) throws IOException {
    InputStreamReader esr = new InputStreamReader(pInputStream);
    BufferedReader reader = new BufferedReader(esr);
    String line;
    if (iErrorText == null) {
      iErrorText= new ArrayList<String>();
    }
    while ((line=reader.readLine()) != null) {
      iErrorText.add(line);
    }
    reader.close();
    esr.close();
  }
  
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Create a temporary file.
   */

  private String getTmpFile() throws IOException {
    File tmpFile = File.createTempFile("im4java-",".png");
    tmpFile.deleteOnExit();
    return tmpFile.getAbsolutePath();
  }
  
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Write a BufferedImage to a temporary file.
   */

  private String convert2TmpFile(BufferedImage pBufferedImage)
                                                             throws IOException {
    String tmpFile = getTmpFile();
    ImageIO.write(pBufferedImage,"PNG",new File(tmpFile));
    return tmpFile;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Remove all temporary files.
   */

  private void removeTmpFiles() {
    try {
      for (String file:iTmpFiles) {
	(new File(file)).delete();
      }
    } catch (Exception e) {
      // ignore, since if we can't delete the file, we can't do anything about it
    }
  }
}
