/*
 * MboxOutputStream.java
 * Copyright(C) 1999 Chris Burdess <dog@gnu.org>
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

package gnu.mail.providers.mbox;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A filter stream that can escape mbox From_ lines in message content.
 * This will only work reliably for messages with &lt;4096 bytes in any one
 * line.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class MboxOutputStream 
  extends FilterOutputStream 
{

  private static byte KET = 62;

  /**
   * The buffer where the current line is stored. 
   */
  protected byte buf[];

  /**
   * The number of valid bytes in the buffer. 
   */
  protected int count = 0;

  /**
   * Constructs an mbox From_-escaping output stream with a buffer size of
   * 1024 bytes.
   */
  public MboxOutputStream(OutputStream out) 
  {
    this(out, 4096);
  }

  /**
   * Constructs an mbox From_-escaping output stream with the specified
   * buffer size.
   * @param len the buffer size
   */
  public MboxOutputStream(OutputStream out, int len) 
  {
    super(out);
    buf = new byte[len];
  }

  /**
   * Flush the internal buffer.
   */
  protected void validateAndFlushBuffer() 
    throws IOException 
  {
    if (count > 0) 
    {
      boolean done = false;
      for (int i=0; i<count-5 && !done; i++) 
      {
        if (buf[i]=='F' &&
            buf[i+1]=='r' &&
            buf[i+2]=='o' &&
            buf[i+3]=='m' &&
            buf[i+4]==' ') 
        {
          byte[] b2 = new byte[buf.length+1];
          System.arraycopy(buf, 0, b2, 0, buf.length);
          b2[i] = KET;
          System.arraycopy(buf, i, b2, i+1, buf.length-i);
          buf = b2;
          count++;
          done = true;
        } 
        else if (buf[i]!=KET && buf[i]!='\n') 
        {
          done = true;
        }
      }
      out.write(buf, 0, count);
      count = 0;
    }
  }

  /**
   * Writes the specified byte to this output stream. 
   */
  public synchronized void write(int b) 
    throws IOException 
  {
    if (b=='\r')
      return;
    if (count>buf.length)
      validateAndFlushBuffer();
    buf[count++] = (byte)b;
    if (b=='\n')
      validateAndFlushBuffer();
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this output stream.
   */
  public synchronized void write(byte b[], int off, int len) 
    throws IOException 
  {
    // strip any CRs in the byte array
    for (int i=off; i<off+len; i++) 
    {
      if (b[i]=='\r') 
      {
        byte[] b2 = new byte[b.length-1];
        System.arraycopy(b, off, b2, off, len-1);
        System.arraycopy(b, i+1, b2, i, len-(i-off)-1);
        b = b2;
        len--;
        i--;
      }
    }
    // validate and flush a line at a time
    for (int i=off; i<off+len; i++) 
    {
      if (b[i]=='\n' || i-off>buf.length) 
      {
        int cl = (i-off>buf.length) ? buf.length : i-off;
        System.arraycopy(b, off, buf, count, cl);
        count += cl;
        validateAndFlushBuffer();
        len = len-(i-off);
        byte[] b2 = new byte[b.length];
        System.arraycopy(b, i, b2, off, len);
        b = b2;
        i = off;
      }
    }
    System.arraycopy(b, off, buf, count, len);
    count += len;
  }

  /**
   * Flushes this output stream.
   */
  public synchronized void flush() 
    throws IOException 
  {
    validateAndFlushBuffer();
    out.flush();
  }
  
}
