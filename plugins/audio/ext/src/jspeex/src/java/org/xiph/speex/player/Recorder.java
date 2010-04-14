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
 * Class: Recorder.java                                                       *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: Jun 1, 2004                                                          *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.player;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.xiph.speex.spi.SpeexEncoding;

/**
 * JavaSound Recorder.
 *
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class Recorder
  extends Player
{
  /** Revision Number */
  public static final String REVISION = "$Revision$";

  /** Audio sampled at 8 kHz (telephone quality). */
  public static final String SAMPLERATE_8KHZ  =  "8000 Hz";
  /** Audio sampled at 11 kHz. */
  public static final String SAMPLERATE_11KHZ = "11025 Hz";
  /** Audio sampled at 16 kHz (wideband). */
  public static final String SAMPLERATE_16KHZ = "16000 Hz";
  /** Audio sampled at 22 kHz (FM radio quality). */
  public static final String SAMPLERATE_22KHZ = "22050 Hz";
  /** Audio sampled at 32 kHz (ultra-wideband). */
  public static final String SAMPLERATE_32KHZ = "32000 Hz";
  /** Audio sampled at 44 kHz (CD quality). */
  public static final String SAMPLERATE_44KHZ = "44100 Hz";
  /** Mono Audio (1 channel). */
  public static final String CHANNELS_MONO    = "mono";
  /** Stereo Audio (2 channels). */
  public static final String CHANNELS_STEREO  = "stereo";

  // Possible States for the Finite State Machine 
  /** Finite State Machine State: Recording */
  protected static final int STATE_RECORDING  = 6;
  /** Finite State Machine State: Recording Paused */
  protected static final int STATE_REC_PAUSED = 7;

  /** Record Button */
  protected JButton recordButton;

  protected Capture capture;
  
  protected byte[] audio;

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
    final Recorder recorder = new Recorder(filename);
    recorder.init();
    JFrame frame = new JFrame("Recorder");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      public void windowDeiconified(WindowEvent e) { recorder.start(); }
      public void windowIconified(WindowEvent e) { recorder.stop(); }
    });
    frame.getContentPane().add("Center", recorder);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Build a Recorder.
   * @param file
   */
  public Recorder(final String file)
  {
    super(file);
  }

  /**
   * Initialize the Player Component.
   */
  public void init()
  {
    super.init();
    capture = new Capture(); 
  }

  /**
   * Returns an InputSteam containing the Audio to playback.
   * @return an InputSteam containing the Audio to playback.
   * @throws IOException
   */
  protected InputStream getAudioStream()
    throws IOException
  {
    if (audio == null) {
      return new BufferedInputStream(audioFile.openStream());
    }
    else {
      return new ByteArrayInputStream(audio);
    }
  }

  //--------------------------------------------------------------------------
  // Running code
  //--------------------------------------------------------------------------

  /**
   * Capture thread
   */
  protected class Capture
    implements Runnable
  {
    protected ByteArrayOutputStream out;
    protected AudioInputStream audioInputStream;
    protected AudioFormat audioFormat;
    protected DataLine.Info info;
    protected AudioFileFormat.Type targetType;
    protected TargetDataLine line;
    protected byte[] buffer;
    
    protected Thread thread;

    /**
     * Start the playback thread which fills the JavaSound playback buffer.
     */
    protected void start() {
      thread = new Thread(this);
      thread.setName("Capture");
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
      // stop and close the output stream
      try {
        out.flush();
        out.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      // load bytes into the audio input stream for playback
      audio = out.toByteArray();
System.out.println("size="+audio.length);
    }

    /**
     * Setup the JavaSound System to play the Audio.
     */
    protected void setupSound()
    {
      // define the required attributes for our line, 
      // and make sure a compatible line is supported.
      audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                    44100.0F, 16, 1, 2, 44100.0F, false);
      /*
      Now, we are trying to get a TargetDataLine. The TargetDataLine is used
      later to read audio data from it.
      If requesting the line was successful, we are opening it (important!).
      */
      info = new DataLine.Info(TargetDataLine.class, audioFormat);
      try {
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
      }
      catch (LineUnavailableException e) {
        e.printStackTrace();
      }
      catch (SecurityException e) {
        // An applet requires the following permissions to record audio:
        // permission javax.sound.sampled.AudioPermission : record
        e.printStackTrace();
      }
      audioInputStream = new AudioInputStream(line);
      AudioFormat targetFormat = new AudioFormat(SpeexEncoding.SPEEX_Q6,
                                                 audioFormat.getSampleRate(),
                                                 -1, // sample size in bits
                                                 audioFormat.getChannels(),
                                                 -1, // frame size
                                                 -1, // frame rate
                                                 false); // little endian
      audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
      audioFormat = audioInputStream.getFormat();
      out = new ByteArrayOutputStream();
      // setup copying buffer
      buffer = new byte[128000];
    }
    
    /**
     * The code that runs in the thread and recovers the JavaSound capture
     * buffer.
     * Implemented from Runnable interface.
     */
    public void run()
    {
      int read = 0;
      while (thread != null && state == STATE_RECORDING && read != -1) {
        try {
          read = audioInputStream.read(buffer, 0, buffer.length);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        if (read > 0) {
          out.write(buffer, 0, read);
        }
      }
    }
  } // End class Capture

  //--------------------------------------------------------------------------
  // Action processing code
  //--------------------------------------------------------------------------

  /**
   * Process Actions when button are pressed.
   * Implemented from ActionListener interface.
   */
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == timer) {
      progressBar.setValue(getProgress());
    }
    else {
      if ("Play".equals(e.getActionCommand())) {
        playIt();
      }
      else if ("Record".equals(e.getActionCommand())) {
        recordIt();
      }
      else if ("Pause".equals(e.getActionCommand())) {
        if (state == STATE_PAUSED) {
          playIt();
        }
        else if (state == STATE_REC_PAUSED) {
          recordIt();
        }
        else {
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
    recordButton.setEnabled(false);
    playButton.setEnabled(false);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    oldstate = state;
    state = STATE_STOPPED;
    if (oldstate == STATE_PLAYING || oldstate == STATE_PAUSED) {
      playback.stop();
    }
    else if (oldstate == STATE_RECORDING || oldstate == STATE_REC_PAUSED) {
      capture.stop();
    }
    timer.stop();
    progressBar.setValue(0);
    recordButton.setEnabled(true);
    playButton.setEnabled(true);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
  }

  /**
   *
   */
  public synchronized void playIt()
  {
    recordButton.setEnabled(false);
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
    recordButton.setEnabled(false);
    playButton.setEnabled(false);
    pauseButton.setEnabled(true);
    stopButton.setEnabled(true);
  }

  /**
   *
   */
  public synchronized void pauseIt()
  {
    recordButton.setEnabled(false);
    playButton.setEnabled(false);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    oldstate = state;
    if (oldstate == STATE_PLAYING) {
      state = STATE_PAUSED;
      playback.line.stop();
      recordButton.setEnabled(false);
      playButton.setEnabled(true);
    }
    else if (oldstate == STATE_RECORDING) {
      state = STATE_REC_PAUSED;
      capture.line.stop();
      recordButton.setEnabled(true);
      playButton.setEnabled(false);
    }
    timer.stop();
    pauseButton.setEnabled(true);
    stopButton.setEnabled(true);
  }

  /**
   *
   */
  public synchronized void recordIt()
  {
    recordButton.setEnabled(false);
    playButton.setEnabled(false);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    oldstate = state;
    state = STATE_RECORDING;
    if (oldstate == STATE_STOPPED) {
      capture.setupSound();
    }
    if (capture.thread == null || !capture.thread.isAlive()) {
      capture.start();
    }
    capture.line.start();
    timer.start();
    recordButton.setEnabled(false);
    playButton.setEnabled(false);
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
      return super.getProgress();
    }
    else if (state == STATE_RECORDING || state == STATE_REC_PAUSED) {
      return capture.line.getFramePosition() * 1000 / audioLength;
    }
    else {
      return 0;
    }
  }
  //--------------------------------------------------------------------------
  // GUI code
  //--------------------------------------------------------------------------
  
  /**
   * Create the ButtonPanel for the recorder.
   * The recorder button panel that should look something like this:
   * <pre> 
   * +--------------------------------+
   * | +------+ +----+ +-----+ +----+ |
   * | |record| |play| |pause| |stop| |  Button Panel
   * | +------+ +----+ +-----+ +----+ |
   * +--------------------------------+
   * </pre>
   */
  protected void createButtonPanel()
  {
    recordButton = buildButton("Record", "Record",
                               "/images/player_record.gif",
                               "/images/player_record2.gif",
                               "/images/player_record3.gif", this);
    playerButtonPane.add(recordButton);
    super.createButtonPanel();
  }
}
