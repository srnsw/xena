/*
 * SharedFileInputStream.java
 * Copyright (C) 2005 The Free Software Foundation
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

package javax.mail.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.mail.internet.SharedInputStream;

/**
 * A buffered input stream that reads data from an underlying file and is
 * shareable between multiple readers.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 * @since JavaMail 1.4
 */
public class SharedFileInputStream
  extends BufferedInputStream
  implements SharedInputStream
{

  /**
   * The underlying file.
   */
  protected RandomAccessFile in;

  /**
   * The normal size of the read buffer.
   */
  protected int bufsize;

  /**
   * The file offset of the first byte in the read buffer.
   */
  protected long bufpos;

  /**
   * The file offset of the start of data.
   */
  protected long start = 0;

  /**
   * The number of bytes in this stream.
   */
  protected long datalen;
  
  /**
   * The open streams count;
   */
  private final int[] openCount;
  
  /**
   * Constructor with file.
   * @param file the file
   */
  public SharedFileInputStream(File file)
    throws IOException
  {
    super(null);
    bufsize = buf.length;
    in = new RandomAccessFile(file, "r");
    datalen = in.length();
    openCount = new int[]{1};
  }

  /**
   * Constructor with path.
   * @param file the file path
   */
  public SharedFileInputStream(String file)
    throws IOException
  {
    super(null);
    bufsize = buf.length;
    in = new RandomAccessFile(file, "r");
    datalen = in.length();
    openCount = new int[]{1};
  }
  
  /**
   * Constructor with file and buffer size.
   * @param file the file
   * @param size the buffer size
   */
  public SharedFileInputStream(File file, int size)
    throws IOException
  {
    super(null, size);
    bufsize = size;
    in = new RandomAccessFile(file, "r");
    datalen = in.length();
    openCount = new int[]{1};
  }

  /**
   * Constructor with path and buffer size.
   * @param file the file path
   * @param size the buffer size
   */
  public SharedFileInputStream(String file, int size)
    throws IOException
  {
    super(null, size);
    bufsize = size;
    in = new RandomAccessFile(file, "r");
    datalen = in.length();
    openCount = new int[]{1};
  }

  private SharedFileInputStream(SharedFileInputStream parent,
                                long start, long datalen)
  {
    super(null, parent.bufsize);
    this.openCount = parent.openCount;
    this.in = parent.in;
    this.bufsize = parent.bufsize;
    this.start = start;
    this.datalen = datalen;
    bufpos = start;
  }

  public int read()
    throws IOException
  {
    in.seek(bufpos);
    int ret = in.read();
    if (ret != -1)
      bufpos++;
    return ret;
  }

  public int read(byte[] b, int off, int len)
    throws IOException
  {
    in.seek(bufpos);
    int ret = in.read(b, off, len);
    if (ret > 0)
      bufpos += ret;
    return ret;
  }

  public long skip(long n)
    throws IOException
  {
    long l1 = in.length();
    long l2 = start + datalen;
    long l3 = Math.min(l1, l2);
    long ret = (bufpos + n > l3) ? l3 - (bufpos + n) : n;
    bufpos += ret;
    return ret;
  }

  public int available()
    throws IOException
  {
    long l1 = in.length();
    long l2 = start + datalen;
    long l3 = Math.min(l1, l2);
    return (int) (l3 - bufpos);
  }

  public void mark(int limit)
  {
    super.mark(limit);
  }

  public void reset()
    throws IOException
  {
    super.reset();
  }

  public boolean markSupported()
  {
    return super.markSupported();
  }

  public void close()
    throws IOException
  {
    if (in != null) {
      synchronized(openCount) {
        if (openCount[0] > 0) {
          --openCount[0];
          if (openCount[0] == 0) {
            in.close();
          }
        }
      }
      buf = null;
      in = null;
    }
  }

  public long getPosition()
  {
    return bufpos - start;
  }

  public InputStream newStream(long start, long end)
  {
    synchronized(openCount) {
       ++openCount[0];
    }
    return new SharedFileInputStream(this, start, end - start);
  }

  /**
   * Forces the underlying file to close.
   */
  protected void finalize()
    throws Throwable
  {
    close();
  }
  
}
