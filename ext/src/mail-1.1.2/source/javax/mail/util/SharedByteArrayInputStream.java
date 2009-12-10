/*
 * SharedByteArrayInputStream.java
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.mail.internet.SharedInputStream;

/**
 * A byte array input stream that is shareable between multiple readers.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 * @since JavaMail 1.4
 */
public class SharedByteArrayInputStream
  extends ByteArrayInputStream
  implements SharedInputStream
{

  private int off;

  /**
   * Constructor with a byte array.
   * @param buf the byte array
   */
  public SharedByteArrayInputStream(byte[] buf)
  {
    super(buf);
    off = 0;
  }
  
  /**
   * Constructor with a byte array, offset and length.
   * @param buf the byte array
   * @param offset the offset at which to start
   * @param len the number of bytes to consider
   */
  public SharedByteArrayInputStream(byte[] buf, int off, int len)
  {
    super(buf, off, len);
    this.off = off;
  }

  /**
   * Returns the current position in the stream.
   */
  public long getPosition()
  {
    return (long) (pos - off);
  }

  /**
   * Returns a new shared input stream, representing the subset of this
   * stream's data from <code>start</code> to <code>end</code>.
   * @param start the starting offset within the stream
   * @param end the end offset within the stream (exclusive)
   */
  public InputStream newStream(long start, long end)
  {
    int len = (int) (end - start);
    return new SharedByteArrayInputStream(buf, off + (int) start, len);
  }
  
}
