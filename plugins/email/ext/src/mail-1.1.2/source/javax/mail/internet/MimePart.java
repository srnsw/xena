/*
 * MimePart.java
 * Copyright (C) 2002 The Free Software Foundation
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

import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.Part;

/**
 * A MIME part is an Entity as defined by MIME (RFC2045, Section 2.4).
 * <p>
 * The string representation of RFC822 and MIME header fields must contain
 * only US-ASCII characters. Non US-ASCII characters must be encoded as per
 * the rules in RFC 2047. This class does not enforce those rules; the
 * caller is expected to use <code>MimeUtility</code> to ensure that header
 * values are correctly encoded.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public interface MimePart
  extends Part
{

  /**
   * Returns the values of all header fields for the specified name,
   * returned as a single String with the values separated by the given
   * delimiter.
   * If the delimiter is null, only the first value is returned.
   * @param header_name the header name
   */
  String getHeader(String header_name, String delimiter)
    throws MessagingException;

  /**
   * Adds an RFC822 header-line.
   * @exception IllegalWriteException if the underlying implementation does not
   * support modification
   * @exception IllegalStateException if this part is obtained from a READ_ONLY
   * folder
   */
  void addHeaderLine(String line)
    throws MessagingException;

  /**
   * Returns all the header-lines.
   * @return an Enumeration of Strings
   */
  Enumeration getAllHeaderLines()
    throws MessagingException;

  /**
   * Returns all the header-lines with any of the given names.
   * @return an Enumeration of Strings
   */
  Enumeration getMatchingHeaderLines(String[] names)
    throws MessagingException;

  /**
   * Returns all the header-lines without any of the given names.
   * @return an Enumeration of Strings
   */
  Enumeration getNonMatchingHeaderLines(String[] names)
    throws MessagingException;

  /**
   * Returns the value of the Content-Transfer-Encoding header field of
   * this part.
   */
  String getEncoding()
    throws MessagingException;

  /**
   * Returns the value of the Content-ID header field of this part. 
   */
  String getContentID()
    throws MessagingException;

  /**
   * Returns the value of the Content-MD5 header field of this part.
   */
  String getContentMD5()
    throws MessagingException;

  /**
   * Sets the Content-MD5 header value for this part.
   * @exception IllegalWriteException if the underlying implementation does not
   * support modification
   * @param IllegalStateException if this part is obtained from a READ_ONLY 
   * folder
   */
  void setContentMD5(String md5)
    throws MessagingException;

  /**
   * Returns the languages specified in the Content-Language header of this
   * part, as defined by RFC 1766. This method returns <code>null</code> if
   * this header is not available.
   */
  String[] getContentLanguage()
    throws MessagingException;

  /**
   * Sets the Content-Language header of this part.
   * @param languages the array of language tags
   * @exception IllegalWriteException if the underlying implementation does not
   * support modification
   * @exception IllegalStateException if this Part is obtained from a READ_ONLY
   * folder
   */
  void setContentLanguage(String[] languages)
    throws MessagingException;

  /**
   * Sets the content of this message using the specified text, and with a
   * MIME type of "text/plain".
   * <p>
   * If the string contains non US-ASCII characters, it will be encoded 
   * using the platform default charset.
   * @param text the text content
   */
  void setText(String text)
    throws MessagingException;

  /**
   * Sets the content of this message using the specified text, and with a
   * MIME type of "text/plain".
   * <p>
   * If the string contains non US-ASCII characters, it will be encoded 
   * using the specified charset.
   * @param text the text content
   * @param charset the charset to use for any encoding
   */
  void setText(String text, String charset)
    throws MessagingException;
  
  /**
   * Sets the content of this message using the specified text, and with a
   * text MIME type of the specified subtype.
   * <p>
   * If the string contains non US-ASCII characters, it will be encoded 
   * using the specified charset.
   * @param text the text content
   * @param charset the charset to use for any encoding
   * @param subtype the MIME text subtype (e.g. "plain", "html")
   * @since JavaMail 1.4
   */
  void setText(String text, String charset, String subtype)
    throws MessagingException;
  
}

