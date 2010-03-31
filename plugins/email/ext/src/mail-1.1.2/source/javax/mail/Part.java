/*
 * Part.java
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

package javax.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;

/**
 * A part consists of a set of attributes and a content. Some of the
 * attributes provide metadata describing the content and its encoding,
 * others may describe how to process the part.
 * The Part interface is the common base interface for Messages and BodyParts.
 * <p>
 * The content of a part is available in various forms:
 * <ul>
 * <li>As a data handler, using the <code>getDataHandler</code> method.
 * <li>As an input stream, using the <code>getInputStream</code> method.
 * <li>As a Java object, using the <code>getContent</code> method.
 * </ul>
 * The <code>writeTo</code> method can be used to write the part to a
 * byte stream in mail-safe form suitable for transmission. 
 * <p>
 * In MIME terms, Part models an Entity (RFC 2045, Section 2.4).
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public interface Part
{

  /**
   * This part should be presented as an attachment.
   */
  String ATTACHMENT = "attachment";

  /**
   * This part should be presented inline.
   */
  String INLINE = "inline";

  /**
   * Returns the size of the content of this part in bytes, or -1 if the
   * size cannot be determined.
   * <p>
   * Note that the size may not be an exact measure of the content size,
   * but will be suitable for display in a user interface to give the 
   * user an idea of the size of this part.
   */
  int getSize()
    throws MessagingException;

  /**
   * Returns the number of lines in the content of this part, or -1 if the
   * number cannot be determined. 
   * Note that this number may not be an exact measure.
   */
  int getLineCount()
    throws MessagingException;

  /**
   * Returns the content-type of the content of this part, or
   * <code>null</code> if the content-type could not be determined.
   * <p>
   * The MIME typing system is used to name content-types.
   */
  String getContentType()
    throws MessagingException;

  /**
   * Is this part of the specified MIME type?
   * This method compares only the primary type and subtype.
   * The parameters of the content types are ignored.
   * <p>
   * If the subtype of <code>mimeType</code> is the special character '*', 
   * then the subtype is ignored during the comparison.
   */
  boolean isMimeType(String mimeType)
    throws MessagingException;

  /**
   * Returns the disposition of this part.
   * The disposition describes how the part should be presented to the user
   * (see RFC 2183).
   * Return values are not case sensitive.
   */
  String getDisposition()
    throws MessagingException;

  /**
   * Sets the disposition of this part.
   * @param disposition the disposition of this part
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of this header
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void setDisposition(String disposition)
    throws MessagingException;

  /**
   * Returns the description of this part.
   */
  String getDescription()
    throws MessagingException;

  /**
   * Sets the description of this part.
   * @param description the description of this part
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of this header
   * @exception IllegalStateException if this Part is obtained from 
   * a READ_ONLY folder
   */
  void setDescription(String description)
    throws MessagingException;

  /**
   * Returns the filename associated with this part, if available.
   */
  String getFileName()
    throws MessagingException;

  /**
   * Sets the filename associated with this part.
   * @param filename the filename to associate with this part
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of this header
   * @exception IllegalStateException if this Part is obtained from 
   * a READ_ONLY folder
   */
  void setFileName(String filename)
    throws MessagingException;

  /**
   * Returns an input stream for reading the content of this part.
   * Any mail-specific transfer encodings will be decoded by the
   * implementation.
   * @exception IOException when a data handler error occurs
   */
  InputStream getInputStream()
    throws IOException, MessagingException;

  /**
   * Returns a data handler for the content of this part. 
   */
  DataHandler getDataHandler()
    throws MessagingException;

  /**
   * Returns the content of this part as a Java object.
   * The type of the returned object is of course dependent on the content 
   * itself. For instance, the object returned for "text/plain" content 
   * is usually a String object. The object returned for a "multipart"
   * content is always a Multipart subclass. For content-types that are 
   * unknown to the data handler system, an input stream is returned.
   * @exception IOException when a data handler error occurs
   */
  Object getContent()
    throws IOException, MessagingException;

  /**
   * Sets the content of this part using the specified data handler.
   * @param dh the data handler for the content
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void setDataHandler(DataHandler dh)
    throws MessagingException;

  /**
   * Sets the content of this part using the specified object. The type of
   * the supplied argument must be known to the data handler system.
   * @param obj a Java object
   * @param type the MIME content-type of this object
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void setContent(Object obj, String type)
    throws MessagingException;

  /**
   * Sets the textual content of this part, using a MIME type of
   * <code>text/plain</code>.
   * @param text the textual content
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void setText(String text)
    throws MessagingException;

  /**
   * Sets the multipart content of this part.
   * @param mp the multipart content
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void setContent(Multipart mp)
    throws MessagingException;

  /**
   * Writes this part to the specified byte stream.
   * @exception IOException if an error occurs writing to the stream 
   * or if an error occurs in the data handler system.
   * @exception MessagingException if an error occurs fetching the data 
   * to be written
   */
  void writeTo(OutputStream os)
    throws IOException, MessagingException;

  /**
   * Returns all the values for the specified header name, or
   * <code>null</code> if no such headers are available.
   * @param name the header name
   */
  String[] getHeader(String name)
    throws MessagingException;

  /**
   * Sets the value for the specified header name.
   * @param name the header name
   * @param value the new value
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of this header
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void setHeader(String name, String value)
    throws MessagingException;

  /**
   * Adds the specified value to the existing values for this header name.
   * @param name the header name
   * @param value the value to add
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of this header
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void addHeader(String name, String value)
    throws MessagingException;

  /**
   * Removes all headers of the specified name.
   * @param name the header name
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of this header
   * @exception IllegalStateException if this part is obtained from 
   * a READ_ONLY folder
   */
  void removeHeader(String name)
    throws MessagingException;

  /**
   * Returns all the headers from this part.
   * @return an enumeration of Header
   */
  Enumeration getAllHeaders()
    throws MessagingException;

  /**
   * Returns the matching headers from this part.
   * @param names the header names to match
   * @return an enumeration of Header
   */
  Enumeration getMatchingHeaders(String[] names)
    throws MessagingException;

  /**
   * Returns the non-matching headers from this part.
   * @param names the header names to ignore
   * @return an enumeration of Header
   */
  Enumeration getNonMatchingHeaders(String[] names)
    throws MessagingException;
  
}
