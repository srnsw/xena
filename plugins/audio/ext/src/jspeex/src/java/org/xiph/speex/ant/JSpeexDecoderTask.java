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
 * Class: JSpeexDecoderTask.java                                              *
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
import java.util.Random;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.OggCrc;
import org.xiph.speex.PcmWaveWriter;
import org.xiph.speex.RawWriter;
import org.xiph.speex.SbEncoder;
import org.xiph.speex.SpeexDecoder;

/**
 * Ant <code>Task</code> to Decode an audio file from Speex to PCM Wave.
 * Here is an usage example:
 * <pre>
 * <taskdef name="speexdec" classname="org.xiph.speex.ant.JSpeexDecoderTask"/>
 * <target name="decode" description="Decode" >
 *   <speexdec enhanced="true" verbose="true" failOnError="true">
 *     <fileset dir="audio">
 *       <include name="*.spx"/>
 *     </fileset>
 *   </speexdec>
 * </target>
 * </pre>
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class JSpeexDecoderTask
  extends Task
{
  /** Version of the Speex Encoder */
  public static final String VERSION = "Java Speex Decoder Task v0.9.4 ($Revision$)";
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

  /** Random number generator for packet loss simulation. */
  protected static Random random = new Random();
  /** Speex Decoder */
  protected SpeexDecoder speexDecoder;

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
  private int srcFormat;
  /** Defines File format for output audio file (Raw or Wave). */
  private int destFormat;
  /** Defines whether or not the perceptual enhancement is used. */
  private boolean enhanced  = true;
  /** If input is raw, defines the decoder mode (0=NB, 1=WB and 2-UWB). */
  private int mode          = 0;
  /** If input is raw, defines the quality setting used by the encoder. */
  private int quality       = 8;
  /** If input is raw, defines the number of frmaes per packet. */
  private int nframes       = 1;
  /** If input is raw, defines the sample rate of the audio. */
  private int sampleRate    = -1;
  /** */
  private float vbr_quality = -1;
  /** */
  private boolean vbr       = false;
  /** If input is raw, defines th number of channels (1=mono, 2=stereo). */
  private int channels      = 1;
  /** The percentage of packets to lose in the packet loss simulation. */
  private int loss          = 0;
  
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
        decode(srcFile, destFile);
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
          decode(srcFileI, destFileI);
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
    if (srcFilename.toLowerCase().endsWith(".spx")) {
      destFilename = srcFilename.substring(0, srcFilename.length()-4);
    }
    else {
      destFilename = srcFilename;
    }
    destFilename = destFilename + ".wav";
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
    // Setup source and destination formats
    if (srcPath.toString().toLowerCase().endsWith(".spx")) {
      srcFormat = FILE_FORMAT_OGG;
    }
    else if (srcPath.toString().toLowerCase().endsWith(".wav")) {
      srcFormat = FILE_FORMAT_WAVE;
    }
    else {
      srcFormat = FILE_FORMAT_RAW;
    }
    if (destPath == null ||
        destPath.toString().toLowerCase().endsWith(".wav")) {
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
   * Handles the <code>enhanced</code> attribute.
   * @param enhanced the attribute value converted to a boolean.
   */
  public void setEnhanced(final boolean enhanced)
  {
    this.enhanced = enhanced;
  }

  //-------------------------------------------------------------------------
  // Decoder
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
   * Decodes a spx file to wave.
   * @param srcPath the Speex encoded source file.
   * @param destPath the destination file.
   * @exception IOException
   */
  public void decode(final File srcPath, final File destPath)
    throws IOException
  {
    byte[] header    = new byte[2048];
    byte[] payload   = new byte[65536];
    byte[] decdat    = new byte[44100*2*2];
    final int    WAV_HEADERSIZE    = 8;
    final short  WAVE_FORMAT_SPEEX = (short) 0xa109;
    final String RIFF           = "RIFF";
    final String WAVE           = "WAVE";
    final String FORMAT         = "fmt ";
    final String DATA           = "data";
    final int    OGG_HEADERSIZE = 27;
    final int    OGG_SEGOFFSET  = 26;
    final String OGGID          = "OggS";
    int segments=0;
    int curseg=0;
    int bodybytes=0;
    int decsize=0; 
    int packetNo=0;
    // Display info
    if (printlevel <= INFO) version();
    if (printlevel <= DEBUG) log("");
    if (printlevel <= DEBUG) log("Input File: " + srcPath);
    // construct a new decoder
    speexDecoder = new SpeexDecoder();
    // open the input stream
    DataInputStream dis =  new DataInputStream(new FileInputStream(srcPath));

    AudioFileWriter writer = null;
    int origchksum;
    int chksum;
    try {
      // read until we get to EOF
      while (true) {
        if (srcFormat == FILE_FORMAT_OGG) {
          // read the OGG header
          dis.readFully(header, 0, OGG_HEADERSIZE);
          origchksum = readInt(header, 22);
          header[22] = 0;
          header[23] = 0;
          header[24] = 0;
          header[25] = 0;
          chksum=OggCrc.checksum(0, header, 0, OGG_HEADERSIZE);

          // make sure its a OGG header
          if (!OGGID.equals(new String(header, 0, 4))) {
            log("missing ogg id!");
            return;
          }

          /* how many segments are there? */
          segments = header[OGG_SEGOFFSET] & 0xFF;
          dis.readFully(header, OGG_HEADERSIZE, segments);
          chksum=OggCrc.checksum(chksum, header, OGG_HEADERSIZE, segments);

          /* decode each segment, writing output to wav */
          for (curseg=0; curseg < segments; curseg++) {
            /* get the number of bytes in the segment */
            bodybytes = header[OGG_HEADERSIZE+curseg] & 0xFF;
            if (bodybytes==255) {
              log("sorry, don't handle 255 sizes!"); 
              return;
            }
            dis.readFully(payload, 0, bodybytes);
            chksum=OggCrc.checksum(chksum, payload, 0, bodybytes);
            
            /* decode the segment */
            /* if first packet, read the Speex header */
            if (packetNo == 0) {
              if (readSpeexHeader(payload, 0, bodybytes)) {
                if (printlevel <= DEBUG) {
                  log("File Format: Ogg Speex");
                  log("Sample Rate: " + sampleRate);
                  log("Channels: " + channels);
                  log("Encoder mode: " + (mode==0 ? "Narrowband" : (mode==1 ? "Wideband" : "UltraWideband")));
                  log("Frames per packet: " + nframes);
                }
                /* once Speex header read, initialize the wave writer with output format */
                if (destFormat == FILE_FORMAT_WAVE) {
                  writer = new PcmWaveWriter(speexDecoder.getSampleRate(),
                                             speexDecoder.getChannels());
                  if (printlevel <= DEBUG) {
                    log("");
                    log("Output File: " + destPath);
                    log("File Format: PCM Wave");
                    log("Perceptual Enhancement: " + enhanced);
                  }
                }
                else {
                  writer = new RawWriter();
                  if (printlevel <= DEBUG) {
                    log("");
                    log("Output File: " + destPath);
                    log("File Format: Raw Audio");
                    log("Perceptual Enhancement: " + enhanced);
                  }
                }
                writer.open(destPath);  
                writer.writeHeader(null);
                packetNo++;
              }
              else {
                packetNo = 0;
              }
            }
            else if (packetNo == 1) { // Ogg Comment packet
                packetNo++;
            }
            else {
              if (loss>0 && random.nextInt(100)<loss) {
                speexDecoder.processData(null, 0, bodybytes);
                for (int i=1; i<nframes; i++) {
                  speexDecoder.processData(true);
                }
              }
              else {
                speexDecoder.processData(payload, 0, bodybytes);
                for (int i=1; i<nframes; i++) {
                  speexDecoder.processData(false);
                }
              }
              /* get the amount of decoded data */
              if ((decsize = speexDecoder.getProcessedData(decdat, 0)) > 0) {
                writer.writePacket(decdat, 0, decsize);
              }
              packetNo++;
            }
          }
          if (chksum != origchksum)
            throw new IOException("Ogg CheckSums do not match");
        }
        else  { // Wave or Raw Speex
          /* if first packet, initialise everything */
          if (packetNo == 0) {
            if (srcFormat == FILE_FORMAT_WAVE) {
              // read the WAVE header
              dis.readFully(header, 0, WAV_HEADERSIZE+4);
              // make sure its a WAVE header
              if (!RIFF.equals(new String(header, 0, 4)) &&
                  !WAVE.equals(new String(header, 8, 4))) {
                log("Not a WAVE file");
                return;
              }
              // Read other header chunks
              dis.readFully(header, 0, WAV_HEADERSIZE);
              String chunk = new String(header, 0, 4);
              int size = readInt(header, 4);
              while (!chunk.equals(DATA)) {
                dis.readFully(header, 0, size);
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
                  if (readShort(header, 0) != WAVE_FORMAT_SPEEX) {
                    log("Not a Wave Speex file");
                    return;
                  }
                  channels = readShort(header, 2);
                  sampleRate = readInt(header, 4);
                  bodybytes = readShort(header, 12);
                  /*
                  The extra data in the wave format are
                  18 : ACM major version number
                  19 : ACM minor version number
                  20-100 : Speex header
                  100-... : Comment ?
                  */
                  if (readShort(header, 16) < 82) {
                    log("Possibly corrupt Speex Wave file.");
                    return;
                  }
                  readSpeexHeader(header, 20, 80);
                  // Display audio info
                  if (printlevel <= DEBUG) {
                    log("File Format: Wave Speex");
                    log("Sample Rate: " + sampleRate);
                    log("Channels: " + channels);
                    log("Encoder mode: " + (mode==0 ? "Narrowband" : (mode==1 ? "Wideband" : "UltraWideband")));
                    log("Frames per packet: " + nframes);
                  }
                }
                dis.readFully(header, 0, WAV_HEADERSIZE);
                chunk = new String(header, 0, 4);
                size = readInt(header, 4);
              }
              if (printlevel <= DEBUG) log("Data size: " + size);
            }
            else {
              if (printlevel <= DEBUG) {
                log("File Format: Raw Speex");
                log("Sample Rate: " + sampleRate);
                log("Channels: " + channels);
                log("Encoder mode: " + (mode==0 ? "Narrowband" : (mode==1 ? "Wideband" : "UltraWideband")));
                log("Frames per packet: " + nframes);
              }
              /* initialize the Speex decoder */
              speexDecoder.init(mode, sampleRate, channels, enhanced);
              if (!vbr) {
                switch (mode) {
                  case 0:
                    bodybytes = NbEncoder.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[quality]];
                    break;
                  case 1:
                    bodybytes = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                    bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                    break;
                  case 2:
                    bodybytes = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                    bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                    bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[quality]];
                    break;
                  default:
                }
                bodybytes = (bodybytes + 7) >> 3;
              }
              else {
                // We have read the stream to find out more
                bodybytes = 0;
              }
            }
            /* initialize the wave writer with output format */
            if (destFormat == FILE_FORMAT_WAVE) {
              writer = new PcmWaveWriter(sampleRate, channels);
              if (printlevel <= DEBUG) {
                log("");
                log("Output File: " + destPath);
                log("File Format: PCM Wave");
                log("Perceptual Enhancement: " + enhanced);
              }
            }
            else {
              writer = new RawWriter();
              if (printlevel <= DEBUG) {
                log("");
                log("Output File: " + destPath);
                log("File Format: Raw Audio");
                log("Perceptual Enhancement: " + enhanced);
              }
            }
            writer.open(destPath);
            writer.writeHeader(null);
            packetNo++;
          }
          else {
            dis.readFully(payload, 0, bodybytes);
            if (loss>0 && random.nextInt(100)<loss) {
              speexDecoder.processData(null, 0, bodybytes);
              for (int i=1; i<nframes; i++) {
                speexDecoder.processData(true);
              }
            }
            else {
              speexDecoder.processData(payload, 0, bodybytes);
              for (int i=1; i<nframes; i++) {
                speexDecoder.processData(false);
              }
            }
            /* get the amount of decoded data */
            if ((decsize = speexDecoder.getProcessedData(decdat, 0)) > 0) {
              writer.writePacket(decdat, 0, decsize);
            }
            packetNo++;
          }
        }
      }
    }
    catch (EOFException eof) {}
    /* close the output file */
    writer.close();
  }

  /**
   * Reads the header packet.
   * <pre>
   *  0 -  7: speex_string: "Speex   "
   *  8 - 27: speex_version: "speex-1.0"
   * 28 - 31: speex_version_id: 1
   * 32 - 35: header_size: 80
   * 36 - 39: rate
   * 40 - 43: mode: 0=narrowband, 1=wb, 2=uwb
   * 44 - 47: mode_bitstream_version: 4
   * 48 - 51: nb_channels
   * 52 - 55: bitrate: -1
   * 56 - 59: frame_size: 160
   * 60 - 63: vbr
   * 64 - 67: frames_per_packet
   * 68 - 71: extra_headers: 0
   * 72 - 75: reserved1
   * 76 - 79: reserved2
   * </pre>
   * @param packet
   * @param offset
   * @param bytes
   * @return true if the Speex header was successfully parsed, false otherwise.
   */
  private boolean readSpeexHeader(final byte[] packet,
                                  final int offset,
                                  final int bytes)
  {
    if (bytes!=80) {
      log("Oooops");
      return false;
    }
    if (!"Speex   ".equals(new String(packet, offset, 8))) {
      return false;
    }
    mode       = packet[40+offset] & 0xFF;
    sampleRate = readInt(packet, offset+36);
    channels   = readInt(packet, offset+48);
    nframes    = readInt(packet, offset+64);
    return speexDecoder.init(mode, sampleRate, channels, enhanced);
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
