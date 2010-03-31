/*
 * Base64OutputStream.java
 * Copyright(C) 2002 The Free Software Foundation
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

import java.io.*;

/**
 * A Base64 content transfer encoding filter stream.
 * <p>
 * From RFC 2045, section 6.8:
 * <p>
 * The Base64 Content-Transfer-Encoding is designed to represent
 * arbitrary sequences of octets in a form that need not be humanly
 * readable.  The encoding and decoding algorithms are simple, but the
 * encoded data are consistently only about 33 percent larger than the
 * unencoded data.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class Base64OutputStream
  extends FilterOutputStream
{

  private byte buffer[];
  private int buflen;
  private int count;
  private int lineLength;
  
  private static final char src[] =
  {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
    'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 
    'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 
    'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', 
    '8', '9', '+', '/'
  };
  
  private static final int LF = 10, CR = 13, EQ = 61;
  
  /**
   * Default constructor.
   * This constructs a Base64OutputStream with a line length of 76.
   */
  public Base64OutputStream(OutputStream out)
  {
    this(out, 76);
  }
  
  /**
   * Constructor.
   * @param out the underlying output stream to encode
   * @param lineLength the line length
   */
  public Base64OutputStream(OutputStream out, int lineLength)
  {
    super(out);
    buffer = new byte[3];
    this.lineLength = lineLength;
  }

  /**
   * Writes the specified byte to this output stream.
   */
  public void write(int c)
    throws IOException
  {
    buffer[buflen++] = (byte)c;
    if (buflen==3) {
      encode();
      buflen = 0;
    }
  }
  
  /**
   * Writes <code>b.length</code> bytes from the specified byte array 
   * to this output stream.
   */
  public void write(byte b[])
    throws IOException
  {
    write(b, 0, b.length);
  }
  
  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this output stream.
   */
  public void write(byte b[], int off, int len)
    throws IOException
  {
    for (int i=0; i<len; i++)
      write(b[off+i]);
  }
  
  /**
   * Flushes this output stream and forces any buffered output bytes to be
   * written out.
   */
  public void flush()
    throws IOException
  {
    if (buflen>0) {
      encode();
      buflen = 0;
    }
    out.flush();
  }
  
  /**
   * Closes this output stream and releases any system resources 
   * associated with this stream.
   */
  public void close()
    throws IOException
  {
    flush();
    out.close();
  }
  
  private void encode()
    throws IOException
  {
    if ((count+4)>lineLength)
    {
      out.write(CR);
      out.write(LF);
      count = 0;
    }
    if (buflen==1)
    {
      byte b = buffer[0];
      int i = 0;
      boolean flag = false;
      out.write(src[b>>>2 & 0x3f]);
      out.write(src[(b<<4 & 0x30) +(i>>>4 & 0xf)]);
      out.write(EQ);
      out.write(EQ);
    }
    else if (buflen==2)
    {
      byte b1 = buffer[0], b2 = buffer[1];
      int i = 0;
      out.write(src[b1>>>2 & 0x3f]);
      out.write(src[(b1<<4 & 0x30) +(b2>>>4 & 0xf)]);
      out.write(src[(b2<<2 & 0x3c) +(i>>>6 & 0x3)]);
      out.write(EQ);
    }
    else
    {
      byte b1 = buffer[0], b2 = buffer[1], b3 = buffer[2];
      out.write(src[b1>>>2 & 0x3f]);
      out.write(src[(b1<<4 & 0x30) +(b2>>>4 & 0xf)]);
      out.write(src[(b2<<2 & 0x3c) +(b3>>>6 & 0x3)]);
      out.write(src[b3 & 0x3f]);
    }
    count += 4;
  }

}
