/*
 * QPOutputStream.java
 * Copyright(C) 2000 Andrew Selkirk
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
 * 
 * GNU JavaMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */

package gnu.mail.util;

// Imports
import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * Quoted Printable Encoding stream.
 *
 * @author Andrew Selkirk
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.0
 * @see java.io.FilterOutputStream
 **/
public class QPOutputStream
extends FilterOutputStream
{

  /**
   * Char array used in decimal to hexidecimal conversion.
   */
  private static final char[] hex = {'0','1','2','3','4','5','6',
				     '7','8','9','A','B','C','D',
				     'E','F'};

  /**
   * Current byte position in output.
   */
  private int count;

  /**
   * Number of bytes per line.
   */
  private int bytesPerLine;

  /**
   * Flag when a space is seen.
   */
  private boolean gotSpace;

  /**
   * Flag when a CR is seen.
   */
  private boolean gotCR;


  //-------------------------------------------------------------
  // Initialization ---------------------------------------------
  //-------------------------------------------------------------

  /**
   * Create a new Quoted Printable Encoding stream.
   * @param stream Output stream
   * @param length Number of bytes per line
   */
  public QPOutputStream(OutputStream stream, int length) 
  {
    super(stream);
    this.bytesPerLine = length;
    this.count = 0;
    this.gotSpace = false;
    this.gotCR = false;
  } // QPEncoderStream()

  /**
   * Create a new Quoted Printable Encoding stream with
   * the default 76 bytes per line.
   * @param stream Output stream
   */
  public QPOutputStream(OutputStream stream) 
  {
    this(stream, 76);
  } // QPEWncoderStream()


  //-------------------------------------------------------------
  // Methods ----------------------------------------------------
  //-------------------------------------------------------------

  /**
   * Flush encoding buffer.
   * @exception IOException IO Exception occurred
   */
  public void flush() throws IOException 
  {
    if (gotSpace)
    {
      output(0x20, false);
      gotSpace = false;
    }
    out.flush();
  } // flush()

  /**
   * Write bytes to encoding stream.
   * @param bytes Byte array to read values from
   * @param offset Offset to start reading bytes from
   * @param length Number of bytes to read
   * @exception IOException IO Exception occurred
   */
  public void write(byte[] bytes, int offset, int length)
  throws IOException 
  {

    // Variables
    int index;

    // Process Each Byte
    for (index = offset; index < length; index++) 
    {
      write(bytes[index]);
    } // for

  } // write()

  /**
   * Write bytes to stream.
   * @param bytes Byte array to write to stream
   * @exception IOException IO Exception occurred
   */
  public void write(byte[] bytes) throws IOException 
  {
    write(bytes, 0, bytes.length);
  } // write()

  /**
   * Write a byte to the stream.
   * @param b Byte to write to the stream
   * @exception IOException IO Exception occurred
   */
  public void write(int b) throws IOException 
  {
    b &= 0xff;
    if (gotSpace)
    {
      if (b=='\n' || b=='\r')
        output(' ', true);
      else
        output(' ', false);
      gotSpace = false;
    }
    if (b==' ')
      gotSpace = true;
    else if (b=='\r')
    {
      gotCR = true;
      outputCRLF();
    }
    else if (b=='\n')
    {
      if (gotCR)
        gotCR = false;
      else
        outputCRLF();
    }
    else
    {
      if (b<' ' || b>=127 || b=='=')
        output(b, true);
      else
        output(b, false);
    }
  } // write()

  /**
   * Close stream.
   * @exception IOException IO Exception occurred
   */
  public void close() throws IOException 
  {
    out.close();
  } // close()

  /**
   * ????
   * @param b ??
   * @param value ??
   * @exception IOException IO Exception occurred
   */
  protected void output(int b, boolean value)
    throws IOException 
  {
    if (value)
    {
      if ((count += 3) > bytesPerLine)
      {
        out.write('=');
        out.write('\r');
        out.write('\n');
        count = 3;
      }
      out.write('=');
      out.write(hex[b >> 4]);
      out.write(hex[b & 0xf]);
    }
    else
    {
      if (++count > bytesPerLine)
      {
        out.write('=');
        out.write('\r');
        out.write('\n');
        count = 1;
      }
      out.write(b);
    }
  } // output()

  /**
   * Write CRLF byte series to stream.
   * @exception IOException IO Exception occurred
   */
  private void outputCRLF() throws IOException 
  {
    out.write('\r');
    out.write('\n');
    count = 0;
  } // outputCRLF()


} // QPEncoderStream
