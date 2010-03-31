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
 * Class: JSpeexEncoderTask.java                                              *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: Jun 4, 2004                                                          *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.ant;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.OggSpeexWriter;
import org.xiph.speex.PcmWaveWriter;
import org.xiph.speex.RawWriter;
import org.xiph.speex.SpeexDecoder;
import org.xiph.speex.SpeexEncoder;

/**
 * Ant <code>Task</code> to Encode an audio file from PCM Wave to Speex.
 * Here is an usage example:
 * <pre>
 * <taskdef name="speexenc" classname="org.xiph.speex.ant.JSpeexEncoderTask"/>
 * <target name="encode" description="Encode" >
 *   <speexenc quality="8" complexity="3" nframes="1"
 *             vbr="true" vad="false" dtx="false"
 *             verbose="true" failOnError="true">
 *     <fileset dir="audio">
 *       <include name="*.wav"/>
 *     </fileset>
 *   </speexenc>
 * </target>
 * </pre>
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class JSpeexEncoderTask
  extends Task
{
  /** Version of the Speex Encoder */
  public static final String VERSION = "Java Speex Encoder Task v0.9.4 ($Revision$)";
  /** Copyright display String */
  public static final String COPYRIGHT = "Copyright (C) 2002-2004 Wimba S.A.";
  
  /** Print level for messages : Print debug information */
  public static final int DEBUG = 0;
  /** Print level for messages : Print basic information */
  public static final int INFO  = 1;
  /** Print level for messages : Print only warnings and errors */
  public static final int WARN  = 2;
  /** Print level for messages : Print only errors */
  public static final int ERROR = 3;

  /** File format for input or output audio file: Raw */
  public static final int FILE_FORMAT_RAW  = 0;
  /** File format for input or output audio file: Ogg */
  public static final int FILE_FORMAT_OGG  = 1;
  /** File format for input or output audio file: Wave */
  public static final int FILE_FORMAT_WAVE = 2;

  /** Source file to decode */
  private File srcFile;
  /** List of source files to decode */
  private final Vector srcFileset = new Vector();
  /** Destination file of decoded audio */
  private File destFile;
  /** Directory to place destination files */
  private File destDir;
  /** */
  private boolean failOnError = true;

  /** Print level for messages */
  private int printlevel = INFO;
  /** Tells the task to suppress all but the most important output */
  private boolean quiet;
  /** Tells the task to output as much information as possible */ 
  private boolean verbose;
  /** Defines File format for input audio file (Raw, Ogg or Wave). */
  protected int srcFormat  = FILE_FORMAT_OGG;
  /** Defines File format for output audio file (Raw or Wave). */
  protected int destFormat = FILE_FORMAT_WAVE;

  /** Whether the mode is manualy set or automatically determined. */
  protected boolean modeset = false;
  /** Defines the encoder mode (0=NB, 1=WB and 2-UWB). */
  protected int mode       = -1;
  /** Defines the encoder quality setting (integer from 0 to 10). */
  protected int quality    = 8;
  /** Defines the encoders algorithmic complexity. */
  protected int complexity = 3;
  /** Defines the number of frames per speex packet. */
  protected int nframes    = 1;
  /** Defines the desired bitrate for the encoded audio. */
  protected int bitrate    = -1;
  /** Defines the sampling rate of the audio input. */
  protected int sampleRate = -1;
  /** Defines the number of channels of the audio input (1=mono, 2=stereo). */
  protected int channels   = 1;
  /** Defines the encoder VBR quality setting (float from 0 to 10). */
  protected float vbr_quality = -1;
  /** Defines whether or not to use VBR (Variable Bit Rate). */
  protected boolean vbr    = false;
  /** Defines whether or not to use VAD (Voice Activity Detection). */
  protected boolean vad    = false;
  /** Defines whether or not to use DTX (Discontinuous Transmission). */
  protected boolean dtx    = false;
  
  //-------------------------------------------------------------------------
  // Ant Task
  //-------------------------------------------------------------------------

  /**
   * The method executing the task.
   * @throws BuildException
   */
  public void execute()
    throws BuildException
  {
    if (srcFile == null && srcFileset.size() == 0) {
      throw new BuildException("There must be a file attribute or a fileset child element");
    }
    boolean hadError = false;
    if (srcFile != null) {
      if (destFile == null) {
        destFile = buildDestFile(srcFile);
      }
      try {
        setupTask(srcFile, destFile);
        encode(srcFile, destFile);
      }
      catch (IOException e) {
        log(e.getMessage());
      }
    }
    for (int i=0; i<srcFileset.size(); i++) {
      FileSet fs = (FileSet)srcFileset.elementAt(i);
      DirectoryScanner ds = fs.getDirectoryScanner(getProject());
      File dir = fs.getDir(getProject());
      String[] srcs = ds.getIncludedFiles();
      for (int j = 0; j < srcs.length; j++) {
        File srcFileI = new File(dir, srcs[j]);
        File destFileI = buildDestFile(srcFileI);
        try {
          setupTask(srcFileI, destFileI);
          encode(srcFileI, destFileI);
        }
        catch (IOException e) {
          log(e.getMessage());
        }
      }
    }
    if (hadError && failOnError)
      throw new BuildException("Decoding failed.");
  }

  /**
   * Builds and returns the destination file.
   * @param srcFile
   * @return the destination file.
   */
  private File buildDestFile(final File srcFile)
  {
    String srcFilename = srcFile.getName();
    String destFilename;
    if (srcFilename.toLowerCase().endsWith(".wav")) {
      destFilename = srcFilename.substring(0, srcFilename.length()-4);
    }
    else {
      destFilename = srcFilename;
    }
    destFilename = destFilename + ".spx";
    if (destDir == null) {
      return new File(srcFile.getParent(), destFilename);
    }
    else {
      return new File(destDir, destFilename);
    }
  }
  
  /**
   * Setup some task variables.
   * @param srcPath the Speex encoded source file.
   * @param destPath the destination file.
   */
  private void setupTask(final File srcPath, final File destPath)
  {
    if (!modeset) {
      mode = -1;
    }
    if (srcPath.toString().toLowerCase().endsWith(".wav")) {
      srcFormat = FILE_FORMAT_WAVE;
    }
    else {
      srcFormat = FILE_FORMAT_RAW;
    }
    if (destPath.toString().toLowerCase().endsWith(".spx")) {
      destFormat = FILE_FORMAT_OGG;
    }
    else if (destPath.toString().toLowerCase().endsWith(".wav")) {
      destFormat = FILE_FORMAT_WAVE;
    }
    else {
      destFormat = FILE_FORMAT_RAW;
    }
  }

  //-------------------------------------------------------------------------
  // Getters and Setters
  //-------------------------------------------------------------------------
  
  /**
   * Handles the <code>fileset</code> child element.
   * @param set
   */
  public void addFileset(final FileSet set)
  {
    srcFileset.addElement(set);
  }
  
  /**
   * Handles the <code>srcfile</code> attribute.
   * @param file the attribute value converted to a File.
   */
  public void setSrcfile(final File file)
  {
    this.srcFile = file;
  }
  
  /**
   * Handles the <code>destfile</code> attribute.
   * @param file the attribute value converted to a File.
   */
  public void setDestfile(final File file)
  {
    this.destFile = file;
  }

  /**
   * Handles the <code>destdir</code> attribute.
   * @param dir the attribute value converted to a File.
   */
  public void setDestdir(final File dir)
  {
    this.destDir = dir;
  }

  /**
   * Handles the <code>failonerror</code> attribute.
   * @param failOnError the attribute value converted to a boolean.
   */
  public void setFailonerror(final boolean failOnError)
  {
    this.failOnError = failOnError;
  }

  /**
   * Handles the <code>quiet</code> attribute.
   * @param quiet the attribute value converted to a boolean.
   */
  public void setQuiet(final boolean quiet)
  {
    this.quiet = quiet;
    this.printlevel = WARN;
  }

  /**
   * Handles the <code>verbose</code> attribute.
   * @param verbose the attribute value converted to a boolean.
   */
  public void setVerbose(final boolean verbose)
  {
    this.verbose = verbose;
    this.printlevel = DEBUG;
  }

  /**
   * Handles the <code>quality</code> attribute.
   * @param quality the attribute value converted to a float.
   */
  public void setQuality(float quality)
  {
    if (quality < 0)
      quality = 0;
    if (quality > 10)
      quality = 10;
    this.quality = (int) quality;
    this.vbr_quality = quality;
  }

  /**
   * Handles the <code>complexity</code> attribute.
   * @param complexity the attribute value converted to an integer.
   */
  public void setComplexity(final int complexity)
  {
    this.complexity = complexity;
  }

  /**
   * Handles the <code>nframes</code> attribute.
   * @param nframes the attribute value converted to an integer.
   */
  public void setNframes(final int nframes)
  {
    this.nframes = nframes;
  }

  /**
   * Handles the <code>vbr</code> attribute.
   * @param vbr the attribute value converted to a boolean.
   */
  public void setVbr(final boolean vbr)
  {
    this.vbr = vbr;
  }

  /**
   * Handles the <code>vad</code> attribute.
   * @param vad the attribute value converted to a boolean.
   */
  public void setVad(final boolean vad)
  {
    this.vad = vad;
  }

  /**
   * Handles the <code>dtx</code> attribute.
   * @param dtx the attribute value converted to a boolean.
   */
  public void setDtx(final boolean dtx)
  {
    this.dtx = dtx;
  }

  /**
   * Handles the <code>mode</code> attribute.
   * @param mode the attribute value converted to a String.
   */
  public void setMode(final String mode)
  {
    modeset = true;
    if ("ultrawideband".equalsIgnoreCase(mode) ||
        "uwb".equalsIgnoreCase(mode) ||
        "2".equals(mode))
      this.mode = 2;
    else if ("wideband".equalsIgnoreCase(mode) ||
             "wb".equalsIgnoreCase(mode) ||
             "1".equals(mode))
      this.mode = 1;
    else if ("narrowband".equalsIgnoreCase(mode) ||
             "nb".equalsIgnoreCase(mode) ||
             "0".equals(mode))
      this.mode = 0;
    else {
      this.mode = -1;
    }
  }

  //-------------------------------------------------------------------------
  // Encoder
  //-------------------------------------------------------------------------

  /**
   * Prints the version.
   */
  public void version()
  {
    log(VERSION);
    log("using " + SpeexDecoder.VERSION);
    log(COPYRIGHT);
  }

  /**
   * Encodes a PCM file to Speex. 
   * @param srcPath
   * @param destPath
   * @exception IOException
   */
  public void encode(final File srcPath, final File destPath)
    throws IOException
  {
    byte[] temp    = new byte[2560]; // stereo UWB requires one to read 2560b
    final int HEADERSIZE = 8;
    final String RIFF      = "RIFF";
    final String WAVE      = "WAVE";
    final String FORMAT    = "fmt ";
    final String DATA      = "data";
    final int WAVE_FORMAT_PCM = 0x0001;
    // Display info
    if (printlevel <= INFO) version();
    if (printlevel <= DEBUG) System.out.println("");
    if (printlevel <= DEBUG) System.out.println("Input File: " + srcPath);
    // Open the input stream
    DataInputStream dis = new DataInputStream(new FileInputStream(srcPath));
    // Prepare input stream
    if (srcFormat == FILE_FORMAT_WAVE) {
      // read the WAVE header
      dis.readFully(temp, 0, HEADERSIZE+4);
      // make sure its a WAVE header
      if (!RIFF.equals(new String(temp, 0, 4)) &&
          !WAVE.equals(new String(temp, 8, 4))) {
        System.err.println("Not a WAVE file");
        return;
      }
      // Read other header chunks
      dis.readFully(temp, 0, HEADERSIZE);
      String chunk = new String(temp, 0, 4);
      int size = readInt(temp, 4);
      while (!chunk.equals(DATA)) {
        dis.readFully(temp, 0, size);
        if (chunk.equals(FORMAT)) {
          /*
          typedef struct waveformat_extended_tag {
          WORD wFormatTag; // format type
          WORD nChannels; // number of channels (i.e. mono, stereo...)
          DWORD nSamplesPerSec; // sample rate
          DWORD nAvgBytesPerSec; // for buffer estimation
          WORD nBlockAlign; // block size of data
          WORD wBitsPerSample; // Number of bits per sample of mono data
          WORD cbSize; // The count in bytes of the extra size 
          } WAVEFORMATEX;
          */
          if (readShort(temp, 0) != WAVE_FORMAT_PCM) {
            System.err.println("Not a PCM file");
            return;
          }
          channels = readShort(temp, 2);
          sampleRate = readInt(temp, 4);
          if (readShort(temp, 14) != 16) {
            System.err.println("Not a 16 bit file " + readShort(temp, 18));
            return;
          }
          // Display audio info
          if (printlevel <= DEBUG) {
            System.out.println("File Format: PCM wave");
            System.out.println("Sample Rate: " + sampleRate);
            System.out.println("Channels: " + channels);
          }
        }
        dis.readFully(temp, 0, HEADERSIZE);
        chunk = new String(temp, 0, 4);
        size = readInt(temp, 4);
      }
      if (printlevel <= DEBUG) System.out.println("Data size: " + size);
    }
    else {
      if (sampleRate < 0) {
        switch (mode) {
        case 0:
          sampleRate = 8000;
          break;
        case 1:
          sampleRate = 16000;
          break;
        case 2:
          sampleRate = 32000;
          break;
        default:
          sampleRate = 8000;
          break;
        }
      }
      // Display audio info
      if (printlevel <= DEBUG) {
        System.out.println("File format: Raw audio");
        System.out.println("Sample rate: " + sampleRate);
        System.out.println("Channels: " + channels);
        System.out.println("Data size: " + srcPath.length());
      }
    }

    // Set the mode if it has not yet been determined
    if (mode < 0) {
      if (sampleRate < 100) // Sample Rate has probably been given in kHz
        sampleRate *= 1000;
      if (sampleRate < 12000)
        mode = 0; // Narrowband
      else if (sampleRate < 24000)
        mode = 1; // Wideband
      else
        mode = 2; // Ultra-wideband
    }
    // Construct a new encoder
    SpeexEncoder speexEncoder = new SpeexEncoder();
    speexEncoder.init(mode, quality, sampleRate, channels);
    if (complexity > 0) {
      speexEncoder.getEncoder().setComplexity(complexity);
    }
    if (bitrate > 0) {
      speexEncoder.getEncoder().setBitRate(bitrate);
    }
    if (vbr) {
      speexEncoder.getEncoder().setVbr(vbr);
      if (vbr_quality > 0) {
        speexEncoder.getEncoder().setVbrQuality(vbr_quality);
      }
    }
    if (vad) {
      speexEncoder.getEncoder().setVad(vad);
    }
    if (dtx) {
      speexEncoder.getEncoder().setDtx(dtx);
    }

    // Display info
    if (printlevel <= DEBUG) {
      System.out.println("");
      System.out.println("Output File: " + destPath);
      System.out.println("File format: Ogg Speex");
      System.out.println("Encoder mode: " + (mode==0 ? "Narrowband" : (mode==1 ? "Wideband" : "UltraWideband")));
      System.out.println("Quality: " + (vbr ? vbr_quality : quality));
      System.out.println("Complexity: " + complexity);
      System.out.println("Frames per packet: " + nframes);
      System.out.println("Varible bitrate: " + vbr);
      System.out.println("Voice activity detection: " + vad);
      System.out.println("Discontinouous Transmission: " + dtx);
    }
    // Open the file writer
    AudioFileWriter writer;
    if (destFormat == FILE_FORMAT_OGG) {
      writer = new OggSpeexWriter(mode, sampleRate, channels, nframes, vbr);
    }
    else if (destFormat == FILE_FORMAT_WAVE) {
      nframes = PcmWaveWriter.WAVE_FRAME_SIZES[mode-1][channels-1][quality];
      writer = new PcmWaveWriter(mode, quality, sampleRate, channels, nframes, vbr);
    }
    else {
      writer = new RawWriter();
    }
    writer.open(destPath);
    writer.writeHeader("Encoded with: " + VERSION);
    int pcmPacketSize = 2 * channels * speexEncoder.getFrameSize();
    try {
      // read until we get to EOF
      while (true) {
        dis.readFully(temp, 0, nframes*pcmPacketSize);
        for (int i=0; i<nframes; i++)
          speexEncoder.processData(temp, i*pcmPacketSize, pcmPacketSize);
        int encsize = speexEncoder.getProcessedData(temp, 0);
        if (encsize > 0) {
          writer.writePacket(temp, 0, encsize);
        }
      }
    }
    catch (EOFException e) {}
    writer.close(); 
  }
  
  /**
   * Converts Little Endian (Windows) bytes to an int (Java uses Big Endian).
   * @param data the data to read.
   * @param offset the offset from which to start reading.
   * @return the integer value of the reassembled bytes.
   */
  protected static int readInt(final byte[] data, final int offset)
  {
    return (data[offset] & 0xff) |
           ((data[offset+1] & 0xff) <<  8) |
           ((data[offset+2] & 0xff) << 16) |
           (data[offset+3] << 24); // no 0xff on the last one to keep the sign
  }

  /**
   * Converts Little Endian (Windows) bytes to an short (Java uses Big Endian).
   * @param data the data to read.
   * @param offset the offset from which to start reading.
   * @return the integer value of the reassembled bytes.
   */
  protected static int readShort(final byte[] data, final int offset)
  {
    return (data[offset] & 0xff) |
           (data[offset+1] << 8); // no 0xff on the last one to keep the sign
  }
}
