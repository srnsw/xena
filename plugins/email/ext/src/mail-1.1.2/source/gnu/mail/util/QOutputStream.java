/*
 * QOutputStream.java
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
/*
 * QOutputStream.java
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides RFC 2047 "B" transfer decoding.
 * See section 4.2:
 * <p>
 * The "Q" encoding is similar to the "Quoted-Printable" content-
 * transfer-encoding defined in RFC 2045.  It is designed to allow text
 * containing mostly ASCII characters to be decipherable on an ASCII
 * terminal without decoding.
 * <ol>
 * <li>Any 8-bit value may be represented by a "=" followed by two
 * hexadecimal digits.  For example, if the character set in use
 * were ISO-8859-1, the "=" character would thus be encoded as
 * "=3D", and a SPACE by "=20". (Upper case should be used for
 * hexadecimal digits "A" through "F".)
 * <li>The 8-bit hexadecimal value 20(e.g., ISO-8859-1 SPACE) may be
 * represented as "_"(underscore, ASCII 95.). (This character may
 * not pass through some internetwork mail gateways, but its use
 * will greatly enhance readability of "Q" encoded data with mail
 * readers that do not support this encoding.)  Note that the "_"
 * always represents hexadecimal 20, even if the SPACE character
 * occupies a different code position in the character set in use.
 * <li>8-bit values which correspond to printable ASCII characters other
 * than "=", "?", and "_"(underscore), MAY be represented as those
 * characters. (But see section 5 for restrictions.)  In
 * particular, SPACE and TAB MUST NOT be represented as themselves
 * within encoded words.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class QOutputStream
  extends QPOutputStream
{

  private static final int SPACE = 32;
  private static final int UNDERSCORE = 95;

  private static String TEXT_SPECIALS = "=_?";
  private static String WORD_SPECIALS = "=_?\"#$%&'(),.:;<>@[\\]^`{|}~";

  private String specials;
  
  /**
   * Constructor.
   * The <code>word</code> parameter is used to indicate whether the bytes
   * passed to this stream are considered to be RFC 822 "word" tokens or
   * "text" tokens.
   * @param out the underlying output stream
   * @param word word mode if true, text mode otherwise
   */
  public QOutputStream(OutputStream out, boolean word)
  {
    super(out, 0x7fffffff);
    specials = word ? WORD_SPECIALS : TEXT_SPECIALS;
  }

  /**
   * Write a character to the stream.
   */
  public void write(int c)
    throws IOException
  {
    c &= 0xff;
    if (c==SPACE)
      output(UNDERSCORE, false);
    else
    {
      if (c<32 || c>=127 || specials.indexOf(c)>=0)
        output(c, true);
      else
        output(c, false);
    }
  }

  public static int encodedLength(byte[] bytes, boolean word)
  {
    int len = 0;
    String specials = word ? WORD_SPECIALS : TEXT_SPECIALS;
    for(int i = 0; i<bytes.length; i++)
    {
      int c = bytes[i]&0xff;
      if (c<32 || c>=127 || specials.indexOf(c)>=0)
        len += 3;
      else
        len++;
    }
    return len;
  }

}
