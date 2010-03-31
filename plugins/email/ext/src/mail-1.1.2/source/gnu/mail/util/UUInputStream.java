/*
 * UUInputStream.java
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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

import gnu.inet.util.LineInputStream;

/**
 * UU decoder.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class UUInputStream
  extends FilterInputStream
{

  /**
   * True if we are in the code body, between the begin and end lines.
   */
  boolean body;

  /**
   * Decoded bytes that have not yet been read.
   */
  ByteArrayOutputStream pending;

  public UUInputStream(InputStream in)
  {
    super(new LineInputStream(in));
    pending = new ByteArrayOutputStream();
  }

  public int read()
    throws IOException
  {
    byte[] buf = new byte[1];
    int len;
    do
     {
       len = read(buf, 0, 1);
     }
    while (len == 0);
    return (len == -1) ? len : (int) buf[0];
  }

  public int read(byte[] buf)
    throws IOException
  {
    return read(buf, 0, buf.length);
  }
  
  public int read(byte[] buf, int off, int len)
    throws IOException
  {
    // If there are pending decoded bytes, return these
    int plen = pending.size();
    if (plen == 0)
      {
        // Populate pending buffer
        LineInputStream lin = (LineInputStream) in;
        String line;
        if (!body)
          {
            // Read begin line
            line = lin.readLine();
            if (line == null || !line.startsWith("begin "))
              {
                throw new IOException("No `begin' line");
              }
            // mode and filename are currently ignored
            body = true;
          }
        do
          {
            line = lin.readLine();
          }
        while ("".equals(line));
        if (line == null)
          {
            throw new EOFException();
          }
        byte[] src = line.getBytes("US-ASCII");
        int i = 0;
        int n = decode(src[i]);
        if (n <= 0)
          {
            body = false;
            // Read end line
            line = lin.readLine();
            if (line == null || !line.equals("end"))
              {
                throw new IOException("No `end' line");
              }
            return -1;
          }
        for (++i; n > 0; i += 4, n -= 3)
          {
            int c;
            
            if (n >= 1)
              {
                c = decode(src[i]) << 2 | decode(src[i + 1]) >> 4;
                pending.write(c);
              }
            if (n >= 2)
              {
                c = decode(src[i + 1]) << 4 | decode(src[i + 2]) >> 2;
                pending.write(c);
              }            
            if (n >= 3)
              {
                c = decode(src[i + 2]) << 6 | decode(src[i + 3]);
                pending.write(c);
              }
          }
      }
    
    // Copy pending to buf    
    byte[] pbuf = pending.toByteArray();
    plen = pbuf.length;
    pending.reset();
    if (plen > len)
      {
        System.arraycopy(pbuf, 0, buf, 0, len);
        byte[] tmp = new byte[plen - len];
        System.arraycopy(pbuf, len, tmp, 0, tmp.length);
        pending.write(tmp);
        return len;
      }
    else
      {
        System.arraycopy(pbuf, 0, buf, 0, plen);
        return plen;
      }
  }

  /**
   * Decode a single character.
   */
  static int decode(byte c)
  {
    int c2 = (int) c;
    if (c2 < 0)
       {
         c2 += 256;
       }
    return (c2 - 32) & 077;
  }

}

