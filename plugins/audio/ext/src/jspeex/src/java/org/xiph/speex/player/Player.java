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
 * Class: Player.java                                                         *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: Jun 1, 2004                                                          *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

/**
 * JavaSound Player.
 * Here is the Finite State Machine describing it's state.
 * <pre>
 *          +----+
 *          |Init|
 *          +----+
 *             V
 *    ----->+----+
 *   /     >|Stop| \
 *  /     / +----+< \
 * /     /         \ V
 * | +-----+ ---> +----+
 * | |Pause|      |Play|
 * | +-----+ <--- +----+
 *  \     A        / A
 *   \     \+----+< /
 *    ------|Buff| /
 *          +----+
 * </pre>
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class Player 
  extends JPanel
  implements ActionListener
{
  /** Build Number */
  public static final String BUILD = "@BUILD@";
  /** Version Number */
  public static final String VERSION = "@VERSION@";
  /** Revision Number */
  public static final String REVISION = "$Revision$";

  // Possible States for the Finite State Machine
  /** Finite State Machine State: Initialised */
  protected static final int STATE_INIT      = 0;
  /** Finite State Machine State: Stopped */
  protected static final int STATE_STOPPED   = 1;
  /** Finite State Machine State: Playing */
  protected static final int STATE_PLAYING   = 2;
  /** Finite State Machine State: Paused */
  protected static final int STATE_PAUSED    = 3;
  /** Finite State Machine State: Buffering */
  protected static final int STATE_BUFFERING = 4;
  /** Finite State Machine State: Error */
  protected static final int STATE_ERROR     = 5;

  /** The Players Scroll Panel */
  protected JPanel playerScrollPane;
  /** The Players Button Panel */
  protected JPanel playerButtonPane;
  /** Play Button */
  protected JButton playButton;
  /** Pause Button */
  protected JButton pauseButton;
  /** Stop Button */
  protected JButton stopButton;
  /** Progress Bar */
  protected JSlider progressBar;
  protected Timer timer;

  /** Current State of the Finite State Machine */
  protected int state;
  /** Previous State of the Finite State Machine */
  protected int oldstate;
  protected String audioFilename;
  protected URL audioFile;
  protected int audioLength;

  protected Playback playback;

  //--------------------------------------------------------------------------
  // Initialization code
  //--------------------------------------------------------------------------

  /**
   * Command Line entrance.
   * @param args
   */
  public static void main(final String[] args)
  {
    String filename = null;
    if (args.length > 0) {
      filename = args[0];
    }
    final Player player = new Player(filename);
    player.init();
    JFrame frame = new JFrame("Player");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      public void windowDeiconified(WindowEvent e) { player.start(); }
      public void windowIconified(WindowEvent e) { player.stop(); }
    });
    frame.getContentPane().add("Center", player);
    frame.pack();
    frame.setVisible(true);
}

  /**
   * Build a Player.
   * @param file
   */
  public Player(final String file)
  {
    this.audioFilename = file;
    createGUI();
  }
  
  /**
   * Initialize the Player Component.
   */
  public void init()
  {
    state = STATE_STOPPED;
    if (!audioFilename.startsWith("http://") &&
        !audioFilename.startsWith("file:/"))
        audioFilename = "file:/" + audioFilename;
    try {
      audioFile = new URL(audioFilename);
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
    audioLength = 0;
    playback = new Playback();
  }

  /**
   * Start the Player Component.
   */
  public void start()
  {
  }
    
  /**
   * Stop the Player Component.
   */
  public void stop()
  {
    if (state != STATE_STOPPED) {
      stopButton.doClick();
    }
  }

  /**
   * Returns an InputSteam containing the Audio to playback.
   * @return an InputSteam containing the Audio to playback.
   * @throws IOException
   */
  protected InputStream getAudioStream()
    throws IOException
  {
    return new BufferedInputStream(audioFile.openStream());
  }
  
  //--------------------------------------------------------------------------
  // Running code
  //--------------------------------------------------------------------------

  /**
   * Playback thread
   */
  protected class Playback
    implements Runnable
  {
    protected InputStream audioStream;
    protected AudioInputStream audioInputStream;
    protected AudioFormat audioFormat;
    protected DataLine.Info info;
    protected SourceDataLine line;
    protected byte[] buffer;
    protected int written;
    protected int read;

    protected Thread thread;

    /**
     * Start the playback thread which fills the JavaSound playback buffer.
     */
    protected void start() {
      thread = new Thread(this);
      thread.setName("Playback");
      thread.start();
    }

    /**
     * Stop the playback thread and destroy all resources.
     */
    protected void stop() {
      // Stop the thread
      thread = null;
      // Close the line
      if (line != null) {
        line.stop();
        line.close();
        line = null;
      }
      // Close the audio InputStream
      try {
        audioInputStream.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      audioInputStream = null;
    }

    /**
     * Setup the JavaSound System to play the Audio.
     */
    protected void setupSound()
    {
      // We have to read in the sound file.
      try {
        audioStream = getAudioStream();
        if (audioStream instanceof AudioInputStream) {
          audioInputStream = (AudioInputStream) audioStream;
        }
        else {
          audioInputStream = AudioSystem.getAudioInputStream(audioStream);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      /*
        From the AudioInputStream, i.e. from the sound file,
        we fetch information about the format of the audio data.
        This information includes the sampling frequency, the number of channels
        and the size of the samples.
        This information is needed to ask JavaSound for a suitable output line
        for this audio file.
      */
      audioFormat = audioInputStream.getFormat();
      /*
        Asking for a line is a rather tricky thing.
        We have to construct an Info object that specifies the desired properties
        for the line.
        First, we have to say which kind of line we want. The possibilities are:
        SourceDataLine (for playback), Clip (for repeated playback) and
        TargetDataLine (for recording).
        Here, we want to do normal playback, so we ask for a SourceDataLine.
        Then, we have to pass an AudioFormat object, so that the Line knows which
        format the data passed to it will have.
        Furthermore, we can give JavaSound a hint about how big the internal
        buffer for the line should be. This isn't used here, signaling that we
        don't care about the exact size. JavaSound will use some default value
        for the buffer size.
      */
      info = new DataLine.Info(SourceDataLine.class, audioFormat);
      // If the audioFormat is not directly supported
      if (!AudioSystem.isLineSupported(info)) {
        AudioFormat sourceFormat = audioFormat;
        AudioFormat targetFormat = new AudioFormat(
              AudioFormat.Encoding.PCM_SIGNED,
              sourceFormat.getSampleRate(),
              16,
              sourceFormat.getChannels(),
              sourceFormat.getChannels() * 2,
              sourceFormat.getSampleRate(),
              false);
        audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        audioFormat = audioInputStream.getFormat();
        info = new DataLine.Info(SourceDataLine.class, audioFormat);
      }
      // We have to open the line for it to be ready to receive audio data.
      try {
        line = (SourceDataLine) AudioSystem.getLine(info);
        // We have to open the line for it to be ready to receive audio data.
        line.open(audioFormat);
      }
      catch (LineUnavailableException e) {
        e.printStackTrace();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      // setup copying buffer
      buffer = new byte[128000];
      written = 0;
      read = 0;
    }
    
    /**
     * The code that runs in the thread and fills the JavaSound playback buffer.
     * Implemented from Runnable interface.
     */
    public void run()
    {
      while (thread != null && state == STATE_PLAYING && read != -1) {
        if (written >= read) {
          try {
            read = audioInputStream.read(buffer, 0, buffer.length);
            written = 0;
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (read > written) {
          int temp = line.write(buffer, written, read-written);
          written += temp;
        }
      }
      if (state == STATE_PLAYING) {
        /*
        Wait until all data are played. This is only necessary because of the
        bug noted below. (If we do not wait, we would interrupt the playback by
        prematurely closing the line and exiting the VM.)
        */
        line.drain();
        // All data are played. We can close the shop.
        line.close();
        stopButton.doClick();
      }
    }
  } // End class Playback

  //--------------------------------------------------------------------------
  // Action processing code
  //--------------------------------------------------------------------------

  /**
   * Process Actions when button are pressed.
   * Implemented from ActionListener interface.
   * @param e
   */
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == timer) {
      progressBar.setValue(getProgress());
    }
    else {
      if ("Play".equals(e.getActionCommand())) {
        playIt();
      }
      else if ("Pause".equals(e.getActionCommand())) {
        if (state == STATE_PAUSED) {
          playIt();
        }
        else if (state == STATE_PLAYING) {
          pauseIt();
        }
      }
      else if ("Stop".equals(e.getActionCommand())) {
        stopIt();
      }
      else {
      }
    }
  }
  
  /**
   *
   */
  public synchronized void stopIt()
  {
    playButton.setEnabled(false);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    oldstate = state;
    state = STATE_STOPPED;
    playback.stop();
    timer.stop();
    progressBar.setValue(0);
    playButton.setEnabled(true);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
  }

  /**
   *
   */
  public synchronized void playIt()
  {
    playButton.setEnabled(false);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    oldstate = state;
    state = STATE_PLAYING;
    if (oldstate == STATE_STOPPED) {
      playback.setupSound();
    }
    if (playback.thread == null || !playback.thread.isAlive()) {
      playback.start();
    }
    playback.line.start();
    timer.start();
    playButton.setEnabled(false);
    pauseButton.setEnabled(true);
    stopButton.setEnabled(true);
  }

  /**
   * Pause
   */
  public synchronized void pauseIt()
  {
    playButton.setEnabled(false);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    oldstate = state;
    state = STATE_PAUSED;
    playback.line.stop();
    timer.stop();
    playButton.setEnabled(true);
    pauseButton.setEnabled(true);
    stopButton.setEnabled(true);
  }

  /**
   * Return the progress of the playback.
   * @return the progress of the playback.
   */
  protected int getProgress()
  {
    audioLength = 500000;
    if (state == STATE_PLAYING || state == STATE_PAUSED) {
      return playback.line.getFramePosition() * 1000 / audioLength;
    }
    else {
      return 0;
    }
  }
  
  //--------------------------------------------------------------------------
  // GUI code
  //--------------------------------------------------------------------------
  
  /**
   * Create GUI for the player.
   * The player panel that should look something like this:
   * <pre> 
   * +-----------------------+
   * |  ----|--------------  |  Scroll Panel
   * +-----------------------+
   * | +----+ +-----+ +----+ |
   * | |play| |pause| |stop| |  Button Panel
   * | +----+ +-----+ +----+ |
   * +-----------------------+
   * </pre>
   */
  protected void createGUI()
  {
    // Create a player panel:
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    playerScrollPane = new JPanel(new FlowLayout());
    playerButtonPane = new JPanel(new FlowLayout());
    createScrollPanel();
    createButtonPanel();
    add(playerScrollPane);
    add(playerButtonPane);
    setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.black));
  }
  
  /**
   * Create the ScrollPanel for the player.
   * The player scroll panel that should look something like this:
   * <pre> 
   * +-----------------------+
   * |  ----|--------------  |  Scroll Panel
   * +-----------------------+
   * </pre>
   */
  protected void createScrollPanel()
  {
    // Create the Progress Bar
    progressBar = new JSlider();
    progressBar.setMinimum(0);
    progressBar.setMaximum(1000);
    progressBar.setValue(0);
    progressBar.setPreferredSize(new Dimension(120, 16));
    progressBar.setBackground(Color.WHITE);
    progressBar.setEnabled(false);
    playerScrollPane.add(progressBar);
    playerScrollPane.setBackground(Color.WHITE);
    // Create the timer
    timer = new Timer(100, this);
  }
  
  /**
   * Create the ButtonPanel for the player.
   * The player button panel that should look something like this:
   * <pre> 
   * +-----------------------+
   * | +----+ +-----+ +----+ |
   * | |play| |pause| |stop| |  Button Panel
   * | +----+ +-----+ +----+ |
   * +-----------------------+
   * </pre>
   */
  protected void createButtonPanel()
  {
    playButton = buildButton("Play", "Play",
                             "/images/player_play.gif",
                             "/images/player_play2.gif",
                             "/images/player_play3.gif", this);
    playerButtonPane.add(playButton);
    pauseButton = buildButton("Pause", "Pause",
                              "/images/player_pause.gif",
                              "/images/player_pause2.gif",
                              "/images/player_pause3.gif", this);
    playerButtonPane.add(pauseButton);
    stopButton = buildButton("Stop", "Stop",
                             "/images/player_stop.gif",
                             "/images/player_stop2.gif",
                             "/images/player_stop3.gif", this);
    playerButtonPane.add(stopButton);
    playerButtonPane.setBackground(Color.WHITE);
    // Make the player panel the content pane.
    setOpaque(true);
  }
  
  /**
   * Build a Button.
   * @param actionCommand
   * @param toolTip
   * @param pathIconDefault
   * @param pathIconDisabled
   * @param pathIconRollover
   * @param listener
   * @return the Button that was built.
   */
  protected static JButton buildButton(final String actionCommand,
                                       final String toolTip,
                                       final String pathIconDefault,
                                       final String pathIconDisabled,
                                       final String pathIconRollover,
                                       final ActionListener listener)
  {
    ImageIcon IconDefault = createImageIcon(pathIconDefault);
    ImageIcon IconDisabled = createImageIcon(pathIconDisabled);
    ImageIcon IconRollover = createImageIcon(pathIconRollover);
    JButton button = new JButton();
    button.setActionCommand(actionCommand);
    button.setToolTipText(toolTip);
    button.setIcon(IconDefault);
    button.setDisabledIcon(IconDisabled);
    button.setRolloverIcon(IconRollover);
    button.setBorderPainted(false);
    button.setBackground(Color.WHITE);
    button.setPreferredSize(new Dimension(IconDefault.getIconWidth(),
                                          IconDefault.getIconHeight()));
    button.addActionListener(listener);
    return button;
  }
    
  /**
   * Returns an ImageIcon, or null if the path was invalid.
   * @param path
   * @return an ImageIcon, or null if the path was invalid.
   */
  protected static ImageIcon createImageIcon(final String path)
  {
    URL imgURL = Player.class.getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL);
    }
    else {
      System.err.println("Couldn't find file: " + path);
      System.err.println("path: " + Player.class.getResource("."));
      return null;
    }
  }
}
