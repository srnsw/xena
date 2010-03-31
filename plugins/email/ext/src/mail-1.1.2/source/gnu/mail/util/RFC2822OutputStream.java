/*
 * RFC2822OutputStream.java
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** 
 * An output stream that ensures that lines of characters in the body are
 * limited to 998 octets(excluding CRLF).
 *
 * This is required by RFC 2822, section 2.3.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class RFC2822OutputStream
  extends FilterOutputStream
{

  /**
   * The CR octet.
   */
  public static final int CR = 13;

  /**
   * The LF octet.
   */
  public static final int LF = 10;

  /**
   * The number of bytes in the line.
   */
  protected int count;

  /**
   * Constructs an RFC2822 output stream
   * connected to the specified output stream.
   * @param out the underlying OutputStream
   */
  public RFC2822OutputStream(OutputStream out)
  {
    super(out);
    count = 0;
  }

  /**
   * Writes a character to the underlying stream.
   * @exception IOException if an I/O error occurred
   */
  public void write(int ch)
    throws IOException
  {
    if (ch == CR || ch == LF)
    {
      out.write(ch);
      count = 0;
    }
    else
    {
      if (count > 998)
      {
        out.write(LF);
        count = 0;
      }
      out.write(ch);
      count++;
    }
  }

  /**
   * Writes a byte array to the underlying stream.
   * @exception IOException if an I/O error occurred
   */
  public void write(byte[] b)
    throws IOException
  {
    write(b, 0, b.length);
  }

  /**
   * Writes a portion of a byte array to the underlying stream.
   * @exception IOException if an I/O error occurred
   */
  public void write(byte[] b, int off, int len)
    throws IOException
  {
    int d = off;
    len += off;
    int i;
    for (i = off; i < len; i++)
    {
      count++;
      if (b[i] == CR || b[i] == LF)
      {
        out.write(b, d, i + 1 - d);
        d = i + 1;
        count = 0;
      }
      else
      {
        if (count > 998)
        {
          out.write(b, d, count);
          out.write(LF);
          d = i + 1;
          count = 0;
        }
      }
    }
    int leftToWrite = i - d;
    if (leftToWrite > 0)
      out.write(b, d, leftToWrite);
  }

}
