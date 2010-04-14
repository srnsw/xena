/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2004 Wimba S.A., All Rights Reserved.                   *
 *                                                                            *
 * COPYRIGHT:                                                                 *
 *      This software is the property of Wimba S.A.                           *
 *      This software is redistributed under the Xiph.org variant of          *
 *      the BSD license.                                                      *
 *      Redistribution and use in source and binary forms, with or without    *
 *      modification, are permitted provided that the following conditions    *
 *      are met:                                                              *
 *      - Redistributions of source code must retain the above copyright      *
 *      notice, this list of conditions and the following disclaimer.         *
 *      - Redistributions in binary form must reproduce the above copyright   *
 *      notice, this list of conditions and the following disclaimer in the   *
 *      documentation and/or other materials provided with the distribution.  *
 *      - Neither the name of Wimba, the Xiph.org Foundation nor the names of *
 *      its contributors may be used to endorse or promote products derived   *
 *      from this software without specific prior written permission.         *
 *                                                                            *
 * WARRANTIES:                                                                *
 *      This software is made available by the authors in the hope            *
 *      that it will be useful, but without any warranty.                     *
 *      Wimba S.A. is not liable for any consequence related to the           *
 *      use of the provided software.                                         *
 *                                                                            *
 * Class: PlayerApplet.java                                                   *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: Jun 8, 2004                                                          *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.player;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

/**
 * JavaSound Player Applet.
 * This is simply a Player Panel placed inside an applet.
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class PlayerApplet
  extends JApplet
{
  private Player player;
  private String filename;
  
  /**
   * Initialize Applet.
   * Called by the browser or applet viewer to inform this applet that it has
   * been loaded into the system. It is always called before the first time
   * that the <code>start</code> method is called.
   */
  public void init()
  {
    System.out.println("****** Player Applet starting, copyright Wimba 2004");
    System.out.println("****** Version: " + Player.VERSION +
                       ", Revision: " + Player.REVISION +
                       ", build: " + Player.BUILD);
    filename = getParameter("file");
    // Some initialising should be done on the event-dispatching thread.
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          initGUI();
        }
      });
    }
    catch (Exception e) {
      System.err.println("couldn't successfully initialise from event thread");
    }
    // The rest of the initialising is done from the applet launching thread.
    player.init();
  }

  /**
   * Initialize Applet, but run from the event-dispatching thread.
   */
  public void initGUI()
  {
    player = new Player(filename);
    setContentPane(player);
  }

  /**
   * Called by the browser or applet viewer to inform this applet that it
   * should start its execution. It is called after the <code>init</code>
   * method and each time the applet is revisited in a Web page.
   */
  public void start() {
    player.start();
  }

  /**
   * Called by the browser or applet viewer to inform this applet that it
   * should stop its execution. It is called when the Web page that contains
   * this applet has been replaced by another page, and also just before the
   * applet is to be destroyed.
   */
  public void stop() {
    player.stop();
  }
  
  /**
   * Called by the browser or applet viewer to inform this applet that it is
   * being reclaimed and that it should destroy any resources that it has
   * allocated. The <code>stop</code> method will always be called before
   * <code>destroy</code>.
   */
  public void destroy()
  {
    player = null;
  }
}
