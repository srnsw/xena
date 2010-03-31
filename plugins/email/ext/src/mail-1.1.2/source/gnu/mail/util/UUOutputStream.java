/*
 * UUOutputStream.java
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
 * UU encoding output stream.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class UUOutputStream
  extends FilterOutputStream
{

  static final byte[] TABLE = {
    '`', '!', '"', '#', '$', '%', '&', '\'',
    '(', ')', '*', '+', ',', '-', '.', '/',
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', ':', ';', '<', '=', '>', '?',
    '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
    'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
    'X', 'Y', 'Z', '[', '\\', ']', '^', '_'
  };

  static final int MAX_LINE_LENGTH = 45;

  byte[] line;
  String filename;
  int mode;
  boolean beginLineDone;

  /**
   * Default constructor.
   * This writes a UU encoded file with the filename "file".
   */
  public UUOutputStream(OutputStream out)
  {
    this(out, null, 0600);
  }

  /**
   * Constructor with filename.
   * @param filename the filename to encode into the UU file.
   */
  public UUOutputStream(OutputStream out, String filename)
  {
    this(out, filename, 0600);
  }
  
  /**
   * Constructor with filename and mode.
   * @param filename the filename to encode into the UU file.
   * @param mode the file mode to encode
   */
  public UUOutputStream(OutputStream out, String filename, int mode)
  {
    super(out);
    if (filename == null)
      {
        filename = "file";
      }
    this.filename = filename;
    this.mode = mode;
    line = new byte[0];
    beginLineDone = false;
  }

  void writeBeginLine()
    throws IOException
  { 
    // Output begin line
    String beginLine = "begin " + Integer.toString(mode, 8) +
      " " + filename + "\n";
    out.write(beginLine.getBytes("US-ASCII"));
    beginLineDone = true;
  }

  public void close()
    throws IOException
  {
    flush(line, 0, line.length);
    out.write(encode('\0'));
    out.write('\n');
    // Output end line
    out.write(new byte[] {'e', 'n', 'd', '\n'});
    out.close();
  }

  public void write(int c)
    throws IOException
  {
    byte[] buf = new byte[1];
    buf[0] = (byte) c;
    write(buf, 0, 1);
  }

  public void write(byte[] buf)
    throws IOException
  {
    write(buf, 0, buf.length);
  }
	
  public void write(byte[] buf, int off, int len)
    throws IOException
  {
    // Append bytes to line
    byte[] tmp = new byte[line.length + (len - off)];
    System.arraycopy(line, 0, tmp, 0, line.length);
    System.arraycopy(buf, off, tmp, line.length, len - off);
    line = tmp;
    
    // Flush line in chunks of MAX_LINE_LENGTH
    int loff = 0;
    for (; (line.length - loff) > MAX_LINE_LENGTH; loff += MAX_LINE_LENGTH)
      {
        flush(line, loff, MAX_LINE_LENGTH);
      }

    // Truncate line
    tmp = new byte[line.length - loff];
    System.arraycopy(line, loff, tmp, 0, tmp.length);
    line = tmp;
  }

  void flush(byte[] buf, int off, int len)
    throws IOException
  {
    if (!beginLineDone)
      {
        writeBeginLine();
      }
    // Write line length byte
    out.write(encode((byte) len));
    // Read 3 bytes, write 4 bytes
    for (; len > 2; len -= 3, off += 3)
      {
        out.write(encode(buf[off] >> 2));
        out.write(encode(((buf[off] << 4) & 060) |
                         ((buf[off + 1] >> 4) & 017)));
        out.write(encode(((buf[off + 1] << 2) & 074) |
                         ((buf[off + 2] >> 6) & 03)));
        out.write(encode(buf[off + 2] & 077));
      }
    // Last len bytes
    if (len != 0)
      {
        byte c1 = buf[off];
        byte c2 = '\0';
        if (len != 1)
          {
            c2 = buf[off + 1];
          }
        
        out.write(encode(c1 >> 2));
        out.write(encode(((c1 << 4) & 060) |
                         ((c2 >> 4) & 017)));
        if (len == 1)
          {
            out.write(encode('\0'));
          }
        else
          {
            out.write(encode((c2 << 2) & 074));
          }
        out.write(encode('\0'));
      }
    // EOL
    out.write(0x0a);
  }

  static byte encode(int c)
  {
    return encode((byte) c);
  }

  /**
   * Encode a single character.
   */
  static byte encode(byte c)
  {
    int c2 = (int) c;
    if (c2 < 0)
      {
        c2 += 256;
      }
    return TABLE[c2 & 077];
  }
	
}

