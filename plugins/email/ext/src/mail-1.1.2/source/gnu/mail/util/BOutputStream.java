/*
 * BOutputStream.java
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

import java.io.OutputStream;

/**
 * Provides RFC 2047 "B" transfer encoding.
 * See section 4.1.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class BOutputStream
  extends Base64OutputStream
{

  public BOutputStream(OutputStream out)
  {
    super(out, 0x7fffffff);
  }

  public static int encodedLength(byte[] bytes)
  {
    return ((bytes.length+2)/3)*4;
  }
  
}
