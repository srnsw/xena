/*
 * MimeUtility.java
 * Copyright (C) 2002, 2004, 2005 The Free Software Foundation
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

package javax.mail.internet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Session;

import gnu.inet.util.LineInputStream;
import gnu.mail.util.Base64InputStream;
import gnu.mail.util.Base64OutputStream;
import gnu.mail.util.BOutputStream;
import gnu.mail.util.QInputStream;
import gnu.mail.util.QOutputStream;
import gnu.mail.util.QPInputStream;
import gnu.mail.util.QPOutputStream;
import gnu.mail.util.UUInputStream;
import gnu.mail.util.UUOutputStream;

/**
 * This is a utility class providing micellaneous MIME-related functionality.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class MimeUtility
{

  /*
   * Uninstantiable.
   */
  private MimeUtility()
  {
  }
  
  /**
   * Returns the Content-Transfer-Encoding that should be applied to the input
   * stream of this data source, to make it mail safe.
   * <ul>
   * <li>If the primary type of this datasource is "text" and if all the bytes
   * in its input stream are US-ASCII, then the encoding is "7bit". If more
   * than half of the bytes are non-US-ASCII, then the encoding is "base64".
   * If less than half of the bytes are non-US-ASCII, then the encoding is
   * "quoted-printable".
   * <li>If the primary type of this datasource is not "text", then if all the
   * bytes of its input stream are US-ASCII, the encoding is "7bit". If
   * there is even one non-US-ASCII character, the encoding is "base64".
   * @param ds the data source
   * @return "7bit", "quoted-printable" or "base64"
   */
  public static String getEncoding(DataSource ds)
  {
    String encoding = "base64";
    InputStream is = null;
    try
      {
        is = ds.getInputStream();
        ContentType ct = new ContentType(ds.getContentType());
        boolean text = ct.match("text/*");
        switch (asciiStatus(is, ALL, text))
          {
          case ALL_ASCII:
            encoding = "7bit";
            break;
          case MAJORITY_ASCII:
            if (text)
              {
                encoding = "quoted-printable";
              }
            break;
          }
      }
    catch (Exception e)
      {
      }
    try
      {
        is.close();
      }
    catch (IOException e)
      {
      }
    return encoding;
  }
  
  /**
   * Returns the Content-Transfer-Encoding that needs to be applied to the
   * given content in order to make it mail safe.
   * This is the same as the <code>getEncoding(DataSource)</code> method
   * except that instead of reading the data from an input stream it uses
   * the <code>writeTo</code> method to examine the data, which can be more
   * efficient.
   */
  public static String getEncoding(DataHandler dh)
  {
    String encoding = "base64";
    if (dh.getName() != null)
      {
        return getEncoding(dh.getDataSource());
      }
    try
      {
        ContentType ct = new ContentType(dh.getContentType());
        boolean text = ct.match("text/*");
        
        AsciiOutputStream aos =
          new AsciiOutputStream(!text, encodeeolStrict() && !text);
        try
          {
            dh.writeTo(aos);
          }
        catch (IOException e)
          {
          }
        switch (aos.status())
          {
          case ALL_ASCII:
            encoding = "7bit";
            break;
          case MAJORITY_ASCII:
            if (text)
              {
                encoding = "quoted-printable";
              }
            break;
          }
      }
    catch (Exception e)
      {
      }
    return encoding;
  }

  /**
   * Decodes the given input stream.
   * All the encodings defined in RFC 2045 are supported here, including
   * "base64", "quoted-printable", "7bit", "8bit", and "binary".
   * "uuencode" is also supported.
   * @param is the input stream
   * @param encoding the encoding
   * @return the decoded input stream
   */
  public static InputStream decode(InputStream is, String encoding)
    throws MessagingException
  {
    if (encoding.equalsIgnoreCase("base64"))
      {
        return new Base64InputStream(is);
      }
    if (encoding.equalsIgnoreCase("quoted-printable"))
      {
        return new QPInputStream(is);
      }
    if (encoding.equalsIgnoreCase("uuencode") || 
        encoding.equalsIgnoreCase("x-uuencode"))
      {
        return new UUInputStream(is);
      }
    if (encoding.equalsIgnoreCase("binary") ||
        encoding.equalsIgnoreCase("7bit") ||
        encoding.equalsIgnoreCase("8bit"))
      {
        return is;
      }
    throw new MessagingException("Unknown encoding: " + encoding);
  }

  /**
   * Encodes the given output stream.
   * All the encodings defined in RFC 2045 are supported here, including
   * "base64", "quoted-printable", "7bit", "8bit" and "binary".
   * "uuencode" is also supported.
   * @param os the output stream
   * @param encoding the encoding
   * @return an output stream that applies the specified encoding
   */
  public static OutputStream encode(OutputStream os, String encoding)
    throws MessagingException
  {
    if (encoding == null)
      {
        return os;
      }
    if (encoding.equalsIgnoreCase("base64"))
      {
        return new Base64OutputStream(os);
      }
    if (encoding.equalsIgnoreCase("quoted-printable"))
      {
        return new QPOutputStream(os);
      }
    if (encoding.equalsIgnoreCase("uuencode") ||
        encoding.equalsIgnoreCase("x-uuencode"))
      {
        return new UUOutputStream(os);
      }
    if (encoding.equalsIgnoreCase("binary") || 
        encoding.equalsIgnoreCase("7bit") || 
        encoding.equalsIgnoreCase("8bit"))
      {
        return os;
      }
    throw new MessagingException("Unknown encoding: " + encoding);
  }

  /**
   * Encodes the given output stream.
   * All the encodings defined in RFC 2045 are supported here, including
   * "base64", "quoted-printable", "7bit", "8bit" and "binary".
   * "uuencode" is also supported.
   * @param os the output stream
   * @param encoding the encoding
   * @param filename the name for the file being encoded (this is only used
   * with the uuencode encoding)
   * @return an output stream that applies the specified encoding
   */
  public static OutputStream encode(OutputStream os, String encoding,
                                    String filename)
    throws MessagingException
  {
    if (encoding == null)
      {
        return os;
      }
    if (encoding.equalsIgnoreCase("base64"))
      {
        return new Base64OutputStream(os);
      }
    if (encoding.equalsIgnoreCase("quoted-printable"))
      {
        return new QPOutputStream(os);
      }
    if (encoding.equalsIgnoreCase("uuencode") ||
        encoding.equalsIgnoreCase("x-uuencode"))
      {
        return new UUOutputStream(os, filename);
      }
    if (encoding.equalsIgnoreCase("binary") || 
        encoding.equalsIgnoreCase("7bit") || 
        encoding.equalsIgnoreCase("8bit"))
      {
        return os;
      }
    throw new MessagingException("Unknown encoding: " + encoding);
  }

  /**
   * Encodes an RFC 822 "text" token into mail-safe form according to
   * RFC 2047.
   * @param text the Unicode string
   * @param UnsupportedEncodingException if the encoding fails
   */
  public static String encodeText(String text)
    throws UnsupportedEncodingException
  {
    return encodeText(text, null, null);
  }

  /**
   * Encodes an RFC 822 "text" token into mail-safe form according to
   * RFC 2047.
   * @param text the Unicode string
   * @param charset the charset, or null to use the platform default charset
   * @param encoding the encoding to be used ("B" or "Q") 
   */
  public static String encodeText(String text, String charset, String encoding)
    throws UnsupportedEncodingException
  {
    return encodeWord(text, charset, encoding, false);
  }

  /**
   * Decodes headers that are defined as '*text' in RFC 822.
   * @param etext the possibly encoded value
   * @exception UnsupportedEncodingException if the charset conversion failed
   */
  public static String decodeText(String etext)
    throws UnsupportedEncodingException
  {
    String delimiters = "\t\n\r ";
    if (etext.indexOf("=?") == -1)
      {
        return etext;
      }
    StringTokenizer st = new StringTokenizer(etext, delimiters, true);
    StringBuffer buffer = new StringBuffer();
    StringBuffer extra = new StringBuffer();
    boolean decoded = false;
    while (st.hasMoreTokens()) 
      {
        String token = st.nextToken();
        char c = token.charAt(0);
        if (delimiters.indexOf(c) > -1)
          {
            extra.append(c);
          }
        else
          {
            try
              {
                token = decodeWord(token);
                if (!decoded && extra.length() > 0)
                  {
                    buffer.append(extra);
                  }
                decoded = true;
              }
            catch (ParseException e)
              {
                if (!decodetextStrict())
                  {
                    token = decodeInnerText(token);
                  }
                if (extra.length() > 0)
                  {
                    buffer.append(extra);
                  }
                decoded = false;
              }
            buffer.append(token);
            extra.setLength(0);
          }
      }
    return buffer.toString();
  }

  /**
   * Encodes an RFC 822 "word" token into mail-safe form according to
   * RFC 2047.
   * @param text the Unicode string
   * @exception UnsupportedEncodingException if the encoding fails
   */
  public static String encodeWord(String text)
    throws UnsupportedEncodingException
  {
    return encodeWord(text, null, null);
  }

  /**
   * Encodes an RFC 822 "word" token into mail-safe form according to
   * RFC 2047.
   * @param text the Unicode string
   * @param charset the charset, or null to use the platform default charset
   * @param encoding the encoding to be used ("B" or "Q")
   * @exception UnsupportedEncodingException if the encoding fails
   */
  public static String encodeWord(String text, String charset,
                                  String encoding)
    throws UnsupportedEncodingException
  {
    return encodeWord(text, charset, encoding, true);
  }

  private static String encodeWord(String text, String charset, 
                                    String encoding, boolean word)
    throws UnsupportedEncodingException
  {
    if (asciiStatus(text.getBytes()) == ALL_ASCII)
      {
        return text;
      }
    String javaCharset;
    if (charset == null)
      {
        javaCharset = getDefaultJavaCharset();
        charset = mimeCharset(javaCharset);
      }
    else
      {
        javaCharset = javaCharset(charset);
      }
    if (encoding == null)
      {
        byte[] bytes = text.getBytes(javaCharset);
        if (asciiStatus(bytes) != MINORITY_ASCII)
          {
            encoding = "Q";
          }
        else
          {
            encoding = "B";
          }
      }
    boolean bEncoding;
    if (encoding.equalsIgnoreCase("B"))
      {
        bEncoding = true;
      }
    else if (encoding.equalsIgnoreCase("Q"))
      {
        bEncoding = false;
      }
    else
      {
        throw new UnsupportedEncodingException("Unknown transfer encoding: " +
                                                encoding);
      }
    
    StringBuffer encodingBuffer = new StringBuffer();
    encodingBuffer.append("=?");
    encodingBuffer.append(charset);
    encodingBuffer.append("?");
    encodingBuffer.append(encoding);
    encodingBuffer.append("?");
    
    StringBuffer buffer = new StringBuffer();
    encodeBuffer(buffer,
                  text, 
                  javaCharset, 
                  bEncoding, 
                  68 - charset.length(), 
                  encodingBuffer.toString(), 
                  true,
                  word);
    return buffer.toString();
  }

  private static void encodeBuffer(StringBuffer buffer,
                                   String text, 
                                   String charset, 
                                   boolean bEncoding, 
                                   int max, 
                                   String encoding,
                                   boolean keepTogether, 
                                   boolean word)
    throws UnsupportedEncodingException
  {
    byte[] bytes = text.getBytes(charset);
    int elen;
    if (bEncoding)
      {
        elen = BOutputStream.encodedLength(bytes);
      }
    else
      {
        elen = QOutputStream.encodedLength(bytes, word);
      }
    int len = text.length();
    if (elen > max && len > 1)
      {
        encodeBuffer(buffer,
                     text.substring(0, len / 2), 
                     charset, 
                     bEncoding, 
                     max, 
                     encoding, 
                     keepTogether, 
                     word);
        encodeBuffer(buffer,
                     text.substring(len / 2, len),
                     charset,
                     bEncoding,
                     max,
                     encoding,
                     false,
                     word);
      }
    else
      {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream os = null;
        if (bEncoding)
          {
            os = new BOutputStream(bos);
          }
        else
          {
            os = new QOutputStream(bos, word);
          }
        try
          {
            os.write(bytes);
            os.close();
          }
        catch (IOException e)
          {
          }
        bytes = bos.toByteArray();
        if (!keepTogether)
          {
            buffer.append("\r\n ");
          }
        buffer.append(encoding);
        for (int i = 0; i < bytes.length; i++)
          {
            buffer.append((char) bytes[i]);
          }
        
        buffer.append("?=");
      }
  }
  
  /**
   * Decodes the specified string using the RFC 2047 rules for parsing an
   * "encoded-word".
   * @param eword the possibly encoded value
   * @exception ParseException if the string is not an encoded-word
   * @exception UnsupportedEncodingException if the decoding failed
   */
  public static String decodeWord(String text)
    throws ParseException, UnsupportedEncodingException
  {
    if (!text.startsWith("=?"))
      {
        throw new ParseException();
      }
    int start = 2;
    int end = text.indexOf('?', start);
    if (end < 0)
      {
        throw new ParseException();
      }
    String charset = text.substring(start, end);
    // Allow for RFC2231 language
    int si = charset.indexOf('*');
    if (si != -1)
      {
        charset = charset.substring(0, si);
      }
    charset = javaCharset(charset);
    start = end + 1;
    end = text.indexOf('?', start);
    if (end < 0)
      {
        throw new ParseException();
      }
    String encoding = text.substring(start, end);
    start = end + 1;
    end = text.indexOf("?=", start);
    if (end < 0)
      {
        throw new ParseException();
      }
    text = text.substring(start, end);
    try
      {
        // The characters in the remaining string must all be 7-bit clean.
        // Therefore it is safe just to copy them verbatim into a byte array.
        char[] chars = text.toCharArray();
        int len = chars.length;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++)
          {
            bytes[i] = (byte) chars[i];
          }
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        InputStream is;
        if (encoding.equalsIgnoreCase("B"))
          {
            is = new Base64InputStream(bis);
          }
        else if (encoding.equalsIgnoreCase("Q"))
          {
            is = new QInputStream(bis);
          }
        else
          {
            throw new UnsupportedEncodingException("Unknown encoding: " +
                                                    encoding);
          }
        len = bis.available();
        bytes = new byte[len];
        len = is.read(bytes, 0, len);
        String ret = new String(bytes, 0, len, charset);
        if (text.length() > end + 2)
          {
            String extra = text.substring(end + 2);
            if (!decodetextStrict())
              {
                extra = decodeInnerText(extra);
              }
            ret = ret + extra;
          }
        return ret;
      }
    catch (IOException e)
      {
        throw new ParseException();
      }
    catch (IllegalArgumentException e)
      {
        throw new UnsupportedEncodingException();
      }
  }

  /**
   * Indicates that we should consider a lone CR or LF in a body part
   * that's not a MIME text type to indicate that the body part
   * needs to be encoded.
   * @since JavaMail 1.3
   */
  private static boolean encodeeolStrict()
  {
    try
      {
        String encodeeolStrict =
          System.getProperty("mail.mime.encodeeol.strict", "false");
        return Boolean.valueOf(encodeeolStrict).booleanValue();
      }
    catch (SecurityException e)
      {
        return false;
      }
  }

  /**
   * Indicates if text in the middle of words should be decoded.
   * @since JavaMail 1.3
   */
  private static boolean decodetextStrict()
  {
    try
      {
        String decodetextStrict =
          System.getProperty("mail.mime.decodetext.strict", "true");
        return Boolean.valueOf(decodetextStrict).booleanValue();
      }
    catch (SecurityException e)
      {
        return true;
      }
  }

  /**
   * Decodes text in the middle of the specified text.
   * @since JavaMail 1.3
   */
  private static String decodeInnerText(String text)
    throws UnsupportedEncodingException
  {
    final String LD = "=?", RD = "?=";
    int pos = 0;
    StringBuffer buffer = new StringBuffer();
    for (int start = text.indexOf(LD, pos); start != -1;
        start = text.indexOf(LD, pos))
      {
        int end = text.indexOf(RD, start + 2);
        if (end == -1)
          {
            break;
          }
        buffer.append(text.substring(pos, start));
        pos = end + 2;
        String encoded = text.substring(start, pos);
        try
          {
            buffer.append(decodeWord(encoded));
          }
        catch (ParseException e)
          {
            buffer.append(encoded);
          }
      }
    if (buffer.length() > 0)
      {
        if (pos < text.length())
          {
            buffer.append(text.substring(pos));
          }
        return buffer.toString();
      }
    return text;
  }

  /**
   * Quotes the specified word, if it contains any characters from the
   * given "specials" list.
   * <p>
   * The HeaderTokenizer class defines two "specials" lists, 
   * MIME and RFC 822.
   * @param word the word to be quoted
   * @param specials the set of special characters
   */
  public static String quote(String text, String specials)
  {
    int len = text.length();
    boolean needsQuotes = false;
    for (int i = 0; i < len; i++)
      {
        char c = text.charAt(i);
        if (c == '\n' || c == '\r' || c == '"' || c == '\\')
          {
            StringBuffer buffer = new StringBuffer(len + 3);
            buffer.append('"');
            for (int j = 0; j < len; j++)
              {
                char c2 = text.charAt(j);
                if (c2 == '"' || c2 == '\\' || c2 == '\r' || c2 == '\n')
                  {
                    buffer.append('\\');
                  }
                buffer.append(c2);
              }
            
            buffer.append('"');
            return buffer.toString();
          }
        if (c < ' ' || c > '\177' || specials.indexOf(c) >= 0)
          {
            needsQuotes = true;
          }
      }
    
    if (needsQuotes)
      {
        StringBuffer buffer = new StringBuffer(len + 2);
        buffer.append('"');
        buffer.append(text);
        buffer.append('"');
        return buffer.toString();
      }
    return text;
  }

  // -- Java and MIME charset conversions --

  /*
   * Map of MIME charset names to Java charset names.
   */
  private static HashMap mimeCharsets;

  /*
   * Map of Java charset names to MIME charset names.
   */
  private static HashMap javaCharsets;

  /*
   * Indicates if we are using Java 1.2 - if so, we return "Java" charsets
   * instead of MIME charsets.
   */
  private static boolean java12;

  /*
   * Load the charset conversion tables.
   */
  static 
  {
    String mappings = "/META-INF/javamail.charset.map";
    InputStream in = (MimeUtility.class).getResourceAsStream(mappings);
    if (in != null)
      {
        mimeCharsets = new HashMap(10);
        javaCharsets = new HashMap(20);
        LineInputStream lin = new LineInputStream(in);
        parse(mimeCharsets, lin);
        parse(javaCharsets, lin);
      }
    try
      {
        String version = System.getProperty("java.version");
        java12 = (version.startsWith("1.2") ||
                  version.startsWith("1.1"));
      }
    catch (SecurityException e)
      {
        // TODO
      }
  }

  /*
   * Parse a charset map stream.
   */
  private static void parse(HashMap mappings, LineInputStream lin)
  {
    try
      {
        while (true)
          {
            String line = lin.readLine();
            if (line == null ||
               (line.startsWith("--") && line.endsWith("--")))
              {
                return;
              }
            
            if (line.trim().length() != 0 && !line.startsWith("#"))
              {
                StringTokenizer st = new StringTokenizer(line, "\t ");
                try
                  {
                    String key = st.nextToken();
                    String value = st.nextToken();
                    mappings.put(key.toLowerCase(), value);
                  }
                catch (NoSuchElementException e2)
                  {
                  }
              }
          }
      }
    catch (IOException e)
      {
        e.printStackTrace();
      }
  }

  /**
   * Converts a MIME charset name into a Java charset name.
   * @param charset the MIME charset name
   */
  public static String javaCharset(String charset)
  {
    if (mimeCharsets == null || charset == null)
      {
        return charset;
      }
    String jc = (String) mimeCharsets.get(charset.toLowerCase());
    if (jc != null)
      {
        if (java12)
          {
            return jc;
          }
        else
          {
            String mc = (String) javaCharsets.get(jc.toLowerCase());
            return (mc != null) ? mc : charset;
          }
      }
    return charset;
  }

  /**
   * Converts a Java charset name into a MIME charset name.
   * @param charset the Java charset name
   */
  public static String mimeCharset(String charset)
  {
    if (javaCharsets == null || charset == null)
      {
        return charset;
      }
    String mc = (String) javaCharsets.get(charset.toLowerCase());
    return (mc != null) ? mc : charset;
  }

  // -- Java default charset --
  
  /*
   * Local cache for the system default Java charset.
   * @see #getDefaultJavaCharset
   */
  private static String defaultJavaCharset;

  /**
   * Returns the default Java charset.
   */
  public static String getDefaultJavaCharset()
  {
    if (defaultJavaCharset == null)
      {
        try
          {
            // Use mail.mime.charset as of JavaMail 1.3
            defaultJavaCharset = System.getProperty("mail.mime.charset");
            if (defaultJavaCharset == null)
              {
                defaultJavaCharset = System.getProperty("file.encoding",
                                                         "UTF-8");
              }
          }
        catch (SecurityException e)
          {
            // InputStreamReader has access to the platform default encoding.
            // We create a dummy input stream to feed it with, just to get
            // this encoding value.
            InputStreamReader isr = 
              new InputStreamReader(new InputStream() { public int read() { return 0; } });
            defaultJavaCharset = isr.getEncoding();
            
            // If all else fails use UTF-8
            if (defaultJavaCharset == null)
              {
                defaultJavaCharset = "UTF-8";
              }
          }
      }
    return javaCharset(defaultJavaCharset);
  }
  
  // -- Calculating multipart boundaries --
  
  private static int part = 0;

  /*
   * Returns a suitably unique boundary value.
   */
  static String getUniqueBoundaryValue()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("----=_Part_");
    buffer.append(part++);
    buffer.append("_");
    buffer.append(Math.abs(buffer.hashCode()));
    buffer.append('.');
    buffer.append(System.currentTimeMillis());
    return buffer.toString();
  }

  /*
   * Returns a suitably unique Message-ID value.
   */
  static String getUniqueMessageIDValue(Session session)
  {
    InternetAddress localAddress = InternetAddress.getLocalAddress(session);
    String address = (localAddress != null) ? localAddress.getAddress() :
      "javamailuser@localhost";

    StringBuffer buffer = new StringBuffer();
    buffer.append(Math.abs(getUniqueBoundaryValue().hashCode()));
    buffer.append('.');
    buffer.append(System.currentTimeMillis());
    buffer.append('.');
    buffer.append("JavaMail.");
    buffer.append(address);
    return buffer.toString();
  }

  // These methods provide checks on whether collections of bytes contain
  // all-ASCII, majority-ASCII, or minority-ASCII bytes.
  
  // Constants
  public static final int ALL = -1;
  static final int ALL_ASCII = 1;
  static final int MAJORITY_ASCII = 2;
  static final int MINORITY_ASCII = 3;

  static int asciiStatus(byte[] bytes)
  {
    int asciiCount = 0;
    int nonAsciiCount = 0;
    for (int i = 0; i < bytes.length; i++)
      {
        if (isAscii((int) bytes[i]))
          {
            asciiCount++;
          }
        else
          {
            nonAsciiCount++;
          }
      }
    
    if (nonAsciiCount == 0)
      {
        return ALL_ASCII;
      }
    return (asciiCount <= nonAsciiCount) ? MINORITY_ASCII : MAJORITY_ASCII;
  }

  static int asciiStatus(InputStream is, int len, boolean text)
  {
    int asciiCount = 0;
    int nonAsciiCount = 0;
    int blockLen = 4096;
    int lineLen = 0;
    boolean islong = false;
    byte[] bytes = null;
    if (len != 0)
      {
        blockLen = (len != ALL) ? Math.min(len, 4096) : 4096;
        bytes = new byte[blockLen];
      }
    while (len != 0) 
      {
        int readLen;
        try
          {
            readLen = is.read(bytes, 0, blockLen);
            if (readLen < 0)
              {
                break;
              }
            for (int i = 0; i < readLen; i++)
              {
                int c = bytes[i] & 0xff;
                if (c == 13 || c == 10)
                  {
                    lineLen = 0;
                  }
                else
                  {
                    lineLen++;
                    if (lineLen > 998)
                      {
                        islong = true;
                      }
                  }
                if (isAscii(c))
                  {
                    asciiCount++;
                  }
                else
                  {
                    if (text)
                      {
                        return MINORITY_ASCII;
                      }
                    nonAsciiCount++;
                  }
              }
            
          }
        catch (IOException e)
          {
            break;
          }
        if (len != -1)
          {
            len -= readLen;
          }
      }
    if (len == 0 && text)
      {
        return MINORITY_ASCII;
      }
    if (nonAsciiCount == 0)
      {
        return !islong ? ALL_ASCII : MAJORITY_ASCII;
      }
    return (asciiCount <= nonAsciiCount) ? MINORITY_ASCII : MAJORITY_ASCII;
  }

  private static final boolean isAscii(int c)
  {
    if (c < 0)
      {
        c += 0xff;
      }
    return (c < 128 && c > 31) || c == 13 || c == 10 || c == 9;
  }

  /*
   * This is used by the getEncoding(DataHandler) method to ascertain which
   * encoding scheme to use. It embodies the same algorithm as the
   * asciiStatus methods above.
   */
  static class AsciiOutputStream extends OutputStream
  {

    static final int LF = 0x0a;
    static final int CR = 0x0d;
    
    private boolean strict;
    private boolean eolStrict;
    private int asciiCount = 0;
    private int nonAsciiCount = 0;
    private int ret;
    private int len;
    private int last = -1;
    private boolean islong = false;
    private boolean eolCheckFailed = false;
    
    public AsciiOutputStream(boolean strict, boolean eolStrict)
    {
      this.strict = strict;
      this.eolStrict = eolStrict;
    }
    
    public void write(int c)
      throws IOException
    {
      check(c);
    }
    
    public void write(byte[] bytes)
      throws IOException
    {
      write(bytes, 0, bytes.length);
    }
    
    public void write(byte[] bytes, int offset, int length)
      throws IOException
    {
      length += offset;
      for (int i = offset; i < length; i++)
        {
          check(bytes[i]);
        }
      
    }
    
    private final void check(int c)
      throws IOException
    {
      c &= 0xff;
      if (eolStrict)
        {
          if (last == CR && c != LF || last != CR && c == LF)
            {
              eolCheckFailed = true;
            }
        }
      if (c == CR || c == LF)
        {
          len = 0;
        }
      else
        {
          len++;
          if (len > 998)
            {
              islong = true;
            }
        }
      if (c > 127)
        {
          nonAsciiCount++;
          if (strict)
            {
              ret = MINORITY_ASCII;
              throw new EOFException();
            }
        }
      else
        {
          asciiCount++;
        }
      last = c;
    }
    
    int status()
    {
      if (ret != 0)
        {
          return ret;
        }
      if (eolCheckFailed)
        {
          return MINORITY_ASCII;
        }
      if (nonAsciiCount == 0)
        {
          return !islong ? ALL_ASCII : MAJORITY_ASCII;
        }
      return (asciiCount <= nonAsciiCount) ? MAJORITY_ASCII : MINORITY_ASCII;
    }
    
  }

  /**
   * Folds the specified string such that each line is no longer than 76
   * characters, whitespace permitting.
   * @param used the number of characters used in the line already
   * @param s the string to fold
   * @since JavaMail 1.4
   */
  public static String fold(int used, String s)
  {
    int len = s.length();
    int k = Math.min(76 - used, len);
    if (k == len)
      return s;
    StringBuffer buf = new StringBuffer();
    int i;
    do
      {
        i = whitespaceIndexOf(s, k, -1, len);
        if (i == -1)
          i = whitespaceIndexOf(s, k, 1, len);
        if (i != -1)
          {
            buf.append(s.substring(0, i));
            buf.append('\n');
            s = s.substring(i);
            len -= i;
          }
        k = Math.min(76, len);
      }
    while (i != -1);
    buf.append(s);
    return buf.toString();
  }

  private static int whitespaceIndexOf(String s, int offset, int step, int len)
  {
    for (int i = offset; i > 0 && i < len; i += step)
      {
        char c = s.charAt(i);
        if (c == ' ' || c == '\t')
          return i;
      }
    return -1;
  }

  /**
   * Unfolds a folded header.
   * @param s the header to unfold
   * @since JavaMail 1.4
   */
  public static String unfold(String s)
  {
    StringBuffer buf = null;
    int start = 0, len = s.length();
    for (int end = start; end < len; end++)
      {
        char c = s.charAt(end);
        if (c == '\n' && end < (len - 1))
          {
            char d = s.charAt(end + 1);
            if (d == ' ' || d == '\t')
              {
                String head = s.substring(start, end);
                if (buf == null)
                  buf = new StringBuffer();
                buf.append(head);
                start = end + 1;
              }
          }
      }
    if (buf == null)
      return s;
    buf.append(s.substring(start));
    return buf.toString();
  }

}

