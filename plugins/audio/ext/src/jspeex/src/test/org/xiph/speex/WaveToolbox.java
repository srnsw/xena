/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2003 Wimba S.A., All Rights Reserved.                   *
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
 * Class: WaveToolbox.java                                                    *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: Oct 21, 2004                                                         *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Toolbox for dealing with wave files
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class WaveToolbox
{
  /**
   * Generates a wav header based on the given parameters.
   * Wave file structure is as follows:
   * <table>
   * <tr><th> Byte </th><th> Size </th><th> Value </th></tr>
   * <!--                RIFF Header                       -->
   * <tr><td>  0- 3 </td><td> 4 </td><td> "RIFF" </td></tr>
   * <tr><td>  4- 7 </td><td> 4 </td><td> Wave File Size (-8) </td></tr>
   * <tr><td>  8-11 </td><td> 4 </td><td> "WAVE" </td></tr>
   * <!--                Format Chunk                      -->
   * <tr><td> 12-15 </td><td> 4 </td><td> "fmt " = Format Chunk Header </td></tr>
   * <tr><td> 16-19 </td><td> 4 </td><td> Size Format Chunk = 16 usually </td></tr>
   * <tr><td> 20-21 </td><td> 2 </td><td> Format Tag = 0x01 for PCM </td></tr>
   * <tr><td> 22-23 </td><td> 2 </td><td> Number of Channels = 1 for mono </td></tr>
   * <tr><td> 24-27 </td><td> 4 </td><td> Sampling Frequency : ex. 8000 </td></tr>
   * <tr><td> 28-31 </td><td> 4 </td><td> Average bytes per second </td></tr>
   * <tr><td> 32-33 </td><td> 2 </td><td> Data Block Size (in bytes) </td></tr>
   * <tr><td> 34-35 </td><td> 2 </td><td> Bits per sample : 16 for PCM </td></tr>
   * <tr><td>  ... </td><td> 4+ </td><td> Extended Format Chunk </td></tr>
   * <!--            Fact Chunk (optional)                 -->
   * <!-- Cue, Instrument, Playlist, ... Chunks (optional) -->
   * <!--                 Data Chunk                       -->
   * <tr><td> ~36-39 </td><td> 4 </td><td> "data" = Data Chunk Header </td></tr>
   * <tr><td> ~40-43 </td><td> 4 </td><td> Size Data Chunk </td></tr>
   * <tr><td> ~44-... </td><td> 4 </td><td> Audio data </td></tr>
   * </table>
   * 
   * @param channels     the number of audio channels (1=mono, 2=stereo, ...).
   * @param sampleRate   the sampling frequency of the audio.
   * @param sampleCount  the number of audio samples.
   * @return a wav header based on the given parameters.
   */
  public static byte[] generateWaveHeader(final int channels,
                                          final int sampleRate,
                                          final int sampleCount)
  {
    int headerSize = 44;
    byte[] header = new byte[headerSize];
    writeString(header,  0, "RIFF");
    writeInt   (header,  4, 2*channels*sampleCount+headerSize-8);
    writeString(header,  8, "WAVE");
    writeString(header, 12, "fmt ");
    writeInt   (header, 16, 16);                    // Size of format chunk
    writeShort (header, 20, (short) 0x01);          // Format tag: PCM
    writeShort (header, 22, (short) channels);      // Number of channels
    writeInt   (header, 24, sampleRate);            // Sampling frequency
    writeInt   (header, 28, 2*sampleRate*channels); // Average bytes per second
    writeShort (header, 32, (short) 2*channels);    // Blocksize of data
    writeShort (header, 34, (short) 16);            // Bits per sample
    writeString(header, 36, "data");
    writeInt   (header, 40, 2*sampleCount*channels); // Data Size
    return header;
  }
  
  /**
   * Generate Gaussian White Noise.
   * @param sampleCount  the number of audio samples.
   * @param stddev       standard deviation of the gaussian white noise.
   * @return
   */
  public static int[] generateWhiteNoise(final int sampleCount,
                                         final int stddev)
  {
    if (sampleCount < 0) {
      return new int[0];
    }
    Random random = new Random();
    // Generate White Noise.
    int[] signal = new int[sampleCount];
    for (int i=0; i<sampleCount; i++) {
      signal[i] = (int)(random.nextGaussian() * stddev);
      if (signal[i] > 32767)
        signal[i] = 32767;
      if (signal[i] < -32768)
        signal[i] = -32768;
    }
    return signal;
  }
  
  /**
   * Generates a sine wave using the given parameters.
   * @param sampleCount  the number of audio samples.
   * @param amplitude    the amplitude of the sine wave.
   * @param periode      the periode (in samples) of the sine wave.
   * @return an integer array representing a sine wave.
   */
  private static int[] generateSine(final int sampleCount,
                                    final int amplitude,
                                    final int periode)
  {
    if (sampleCount < 0) {
      return new int[0];
    }
    double phase = 0;
    double frequency;
    if (periode == 0) {
      frequency = 1.0;
    }
    else {
      frequency = 1.0 / ((double) periode);
    }
    double phaseIncrement = 2.0 * Math.PI * frequency;
    int[] signal = new int[sampleCount];
    for (int i=0; i<sampleCount; i++) {
      signal[i] = (int) (amplitude * Math.sin(phase));
      // check saturation
      if (signal[i] > 32767)
        signal[i] = 32767;
      if (signal[i] < -32768)
        signal[i] = -32768;
      phase += phaseIncrement;
    }
    return signal;
  }

  /**
   * Generate a Wave File of White Noise with the given parameters.
   * @param filename
   * @param channels     the number of audio channels (1=mono, 2=stereo, ...).
   * @param sampleRate   the sampling frequency of the audio.
   * @param sampleCount  the number of audio samples.
   * @param stddev       standard deviation of the gaussian white noise.
   * @throws IOException
   */
  public static void generateWhiteNoiseWaveFile(final String filename,
                                                final int channels,
                                                final int sampleRate,
                                                final int sampleCount,
                                                final int stddev)
    throws IOException
  {
    FileOutputStream fos = new FileOutputStream(filename);
    fos.write(generateWaveHeader(channels, sampleRate, sampleCount));
    int[] signal = generateWhiteNoise(channels*sampleCount, stddev);
    byte[] data = new byte[2*channels*sampleCount];
    mapInt2Pcm16bit(signal, 0, data, 0, channels*sampleCount);
    fos.write(data);
    fos.flush();
    fos.close();
  }


  /**
   * Generate a Wave File of a Sine Signal with the given parameters.
   * @param filename
   * @param channels     the number of audio channels (1=mono, 2=stereo, ...).
   * @param sampleRate   the sampling frequency of the audio.
   * @param sampleCount  the number of audio samples.
   * @param amplitude    the amplitude of the sine wave.
   * @param periode      the periode (in samples) of the sine wave.
   * @throws IOException
   */
  public static void generateSineWaveFile(final String filename,
                                          final int channels,
                                          final int sampleRate,
                                          final int sampleCount,
                                          final int amplitude,
                                          final int periode)
    throws IOException
  {
    FileOutputStream fos = new FileOutputStream(filename);
    fos.write(generateWaveHeader(channels, sampleRate, sampleCount));
    int[] signal = generateSine(channels*sampleCount, amplitude, periode);
    byte[] data = new byte[2*channels*sampleCount];
    mapInt2Pcm16bit(signal, 0, data, 0, channels*sampleCount);
    fos.write(data);
    fos.flush();
    fos.close();
  }

  /**
   * Generate a Wave File of Silence of a given length.
   * @param filename
   * @param channels     the number of audio channels (1=mono, 2=stereo, ...).
   * @param sampleRate   the sampling frequency of the audio.
   * @param sampleCount  the number of audio samples.
   * @throws IOException
   */
  public static void generateSilenceWaveFile(final String filename,
                                             final int channels,
                                             final int sampleRate,
                                             final int sampleCount)
    throws IOException
  {
    FileOutputStream fos = new FileOutputStream(filename);
    fos.write(generateWaveHeader(channels, sampleRate, sampleCount));
    byte[] data = new byte[2*channels*sampleCount];
    fos.write(data);
    fos.flush();
    fos.close();
  }

  //-------------------------------------------------------------------------
  // General Array Static methods
  //-------------------------------------------------------------------------

  /**
   * Writes a Little-endian short.
   * @param data   the array into which the data should be written.
   * @param offset the offset from which to start writing in the array.
   * @param val    the value to write.
   */
  public static void writeShort(final byte[] data,
                                final int offset,
                                final int val)
  {
    data[offset]   = (byte) (0xff & val);
    data[offset+1] = (byte) (0xff & (val >>>  8));
  }

  /**
   * Writes a Little-endian int.
   * @param data   the array into which the data should be written.
   * @param offset the offset from which to start writing in the array.
   * @param val    the value to write.
   */
  public static void writeInt(final byte[] data,
                              final int offset,
                              final int val)
  {
    data[offset]   = (byte) (0xff & val);
    data[offset+1] = (byte) (0xff & (val >>>  8));
    data[offset+2] = (byte) (0xff & (val >>> 16));
    data[offset+3] = (byte) (0xff & (val >>> 24));
  }

  /**
   * Writes a Little-endian long.
   * @param data   the array into which the data should be written.
   * @param offset the offset from which to start writing in the array.
   * @param val    the value to write.
   */
  public static void writeLong(final byte[] data,
                               final int offset,
                               final long val)
  {
    data[offset]   = (byte) (0xff & val);
    data[offset+1] = (byte) (0xff & (val >>>  8));
    data[offset+2] = (byte) (0xff & (val >>> 16));
    data[offset+3] = (byte) (0xff & (val >>> 24));
    data[offset+4] = (byte) (0xff & (val >>> 32));
    data[offset+5] = (byte) (0xff & (val >>> 40));
    data[offset+6] = (byte) (0xff & (val >>> 48));
    data[offset+7] = (byte) (0xff & (val >>> 56));
  }

  /**
   * Writes a String.
   * @param data   the array into which the data should be written.
   * @param offset the offset from which to start writing in the array.
   * @param val    the value to write.
   */
  public static void writeString(final byte[] data,
                                 final int offset,
                                 final String val)
  {
    byte[] str = val.getBytes();
    System.arraycopy(str, 0, data, offset, str.length);
  }

  /**
   * Converts an integer PCM stream (in the form of an int array) into a
   * 16 bit linear PCM Little Endian stream (in the form of a byte array).
   * @param samples      int array of 16-bit linear audio samples.
   * @param offsetInput  offset in samples from which to start reading the input.
   * @param pcmBytes     byte array to receive the linear 16-bit PCM formated audio.
   * @param offsetOutput offset in bytes from which to start writing the output.
   * @param length       number of samples to convert.
   */
  public static void mapInt2Pcm16bit(final int[] samples,
                                     final int offsetInput,
                                     final byte[] pcmBytes,
                                     int offsetOutput,
                                     final int length)
  {
    if (samples.length - offsetInput < length) {
      throw new IllegalArgumentException(
          "Insufficient Samples to convert to bytes");
    }
    if (pcmBytes.length - offsetOutput < 2*length) {
      throw new IllegalArgumentException(
          "Insufficient byte buffer to convert the samples");
    }
    for (int i = 0; i < length; i++) {
      pcmBytes[offsetOutput++] = (byte) (samples[offsetInput+i] & 0xff);
      pcmBytes[offsetOutput++] = (byte) ((samples[offsetInput+i]>>8) & 0xff);
    }
  }

  /**
   * Converts a 16 bit linear PCM Little Endian stream (in the form of a byte array)
   * into an integer PCM stream (in the form of an int array).
   * Here are some important details about the encoding:
   * <ul>
   * <li> Java uses big endian for shorts and ints, and Windows uses little Endian.
   *      Therefore, shorts and ints must be read as sequences of bytes and
   *      combined with shifting operations.
   * </ul>
   * @param pcmBytes     byte array of linear 16-bit PCM formated audio.
   * @param offsetInput  offset in bytes from which to start reading the input.
   * @param samples      int array to receive the 16-bit linear audio samples.
   * @param offsetOutput offset in samples from which to start writing the output.
   * @param length       number of samples to convert.
   */
  public static void mapPcm16bit2Int(final byte[] pcmBytes,
                                     final int offsetInput,
                                     final int[] samples,
                                     final int offsetOutput,
                                     final int length)
  {
    if (pcmBytes.length - offsetInput < 2 * length) {
      throw new IllegalArgumentException(
          "Insufficient Samples to convert to integers");
    }
    if (samples.length - offsetOutput < length) {
      throw new IllegalArgumentException(
          "Insufficient integer buffer to convert the samples");
    }
    for (int i = 0; i < length; i++) {
      samples[offsetOutput+i] = (pcmBytes[offsetInput+2*i] & 0xff) |
                                (pcmBytes[offsetInput+2*i+1] << 8);
      // no & 0xff at the end to keep the sign
    }
  }
}
