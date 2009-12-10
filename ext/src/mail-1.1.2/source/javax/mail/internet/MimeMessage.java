/*
 * MimeMessage.java
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;

import gnu.inet.util.GetSystemPropertyAction;
import gnu.mail.util.RFC2822OutputStream;

/**
 * A MIME mail message.
 * This may be a top-level part, or the content of a MIME body part with a
 * "message/rfc822" Content-Type.
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
public class MimeMessage
  extends Message
  implements MimePart
{

  /**
   * Additional recipient types specific to internet messages.
   */
  public static class RecipientType
    extends Message.RecipientType
  {

    /**
     * The "Newsgroups" (Usenet news) recipient type.
     */
    public static final RecipientType NEWSGROUPS =
      new RecipientType("Newsgroups");

    protected Object readResolve()
      throws ObjectStreamException
    {
      if (type.equals("Newsgroups"))
        {
          return NEWSGROUPS;
        }  
      return super.readResolve();
    }

    // super :-)
    protected RecipientType(String type)
    {
      super(type);
    }
    
  }

  /**
   * The data handler managing this message's content.
   */
  protected DataHandler dh;

  /**
   * The bytes of the content of this message, if the message can be stored
   * in memory.
   */
  protected byte content[];

  /**
   * A SharedInputStream containing the byte content of this message, if the
   * message cannot be stored in memory.
   */
  protected InputStream contentStream;

  /**
   * The message headers.
   */
  protected InternetHeaders headers;

  /**
   * The message flags.
   */
  protected Flags flags;

  /**
   * Indicates whether the message has been modified.
   * If false, any data in the content array is assumed to be valid and is
   * used directly in the <code>writeTo</code> method.
   * This field is set to true when an empty message is created or when the
   * <code>saveChanges</code> method is called.
   */
  protected boolean modified;

  /**
   * Indicates whether we do not need to call <code>saveChanges</code> on
   * the message.
   * This flag is set to false by the public constructor and set to true 
   * by the <code>saveChanges</code> method.
   * The <code>writeTo</code> method checks this flag and calls the 
   * <code>saveChanges</code> method as necessary.
   */
  protected boolean saved;

  /*
   * This is used to parse and format values for the RFC822 Date header.
   */
  private static MailDateFormat dateFormat = new MailDateFormat();

  // Header constants.
  static final String TO_NAME = "To";
  static final String CC_NAME = "Cc";
  static final String BCC_NAME = "Bcc";
  static final String NEWSGROUPS_NAME = "Newsgroups";
  static final String FROM_NAME = "From";
  static final String SENDER_NAME = "Sender";
  static final String REPLY_TO_NAME = "Reply-To";
  static final String SUBJECT_NAME = "Subject";
  static final String DATE_NAME = "Date";
  static final String MESSAGE_ID_NAME = "Message-ID";
  
  /**
   * Constructor for an empty message.
   */
  public MimeMessage(Session session)
  {
    super(session);
    headers = new InternetHeaders();
    flags = new Flags();
    modified = true;
  }

  /**
   * Constructor with an input stream contining an RFC 822 message.
   * When this method returns, the stream will be positioned at the end of
   * the data for the message.
   * @param session the session context
   * @param is the message input stream
   */
  public MimeMessage(Session session, InputStream is)
    throws MessagingException
  {
    super(session);
    flags = new Flags();
    parse(is);
    saved = true;
  }

  /**
   * Constructor with an existing message.
   * This performs a deep copy of the target message.
   * @param source the message to copy
   */
  public MimeMessage(MimeMessage source)
    throws MessagingException
  {
    super(source.session);
    // Use a byte array for temporary storage
    try
      {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        source.writeTo(bos);
        bos.close();
        ByteArrayInputStream bis =
          new ByteArrayInputStream(bos.toByteArray());
        parse(bis);
        bis.close();
        saved = true;
      }
    catch (IOException e)
      {
        throw new MessagingException("I/O error", e);
      }
  }

  /**
   * Constructor with a parent folder and message number.
   * @param folder the parent folder
   * @param msgnum the message number
   */
  protected MimeMessage(Folder folder, int msgnum)
  {
    super(folder, msgnum);
    flags = new Flags();
    saved = true;
  }

  /**
   * Constructor with a parent folder, message number, and RFC 822 input
   * stream.
   * When this method returns, the stream will be positioned at the end of
   * the data for the message.
   * @param folder the parent folder
   * @param is the message input stream
   * @param msgnum the message number of this message within the folder
   */
  protected MimeMessage(Folder folder, InputStream is, int msgnum)
    throws MessagingException
  {
    this(folder, msgnum);
    parse(is);
  }

  /**
   * Constructor with a parent folder, message number, headers and byte
   * content.
   * @param folder the parent folder
   * @param headers the headers
   * @param content the content byte array
   * @param msgnum the message number of this message within the folder
   */
  protected MimeMessage(Folder folder, InternetHeaders headers, 
                        byte[] content, int msgnum)
    throws MessagingException
  {
    this(folder, msgnum);
    this.headers = headers;
    this.content = content;
  }

  /**
   * Parses the given input stream, setting the headers and content fields 
   * appropriately.
   * This resets the <code>modified</code> flag.
   * @param is the message input stream
   */
  protected void parse(InputStream is)
    throws MessagingException
  {
    if (is instanceof SharedInputStream)
      {
        headers = createInternetHeaders(is);
        SharedInputStream sis = (SharedInputStream) is;
        contentStream = sis.newStream(sis.getPosition(), -1L);
      }
    else
      {
        // buffer it
        if (!(is instanceof ByteArrayInputStream) && 
            !(is instanceof BufferedInputStream))
          {
            is = new BufferedInputStream(is);
          }
        // headers
        headers = createInternetHeaders(is);
        // Read stream into byte array
        try
          {
            // TODO Make buffer size configurable
            int len = 1024;
            if (is instanceof ByteArrayInputStream)
              {
                len = is.available();
                content = new byte[len];
                is.read(content, 0, len);
              }
            else
              {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(len);
                byte[] b = new byte[len];
                for (int l = is.read(b); l != -1; l = is.read(b)) 
                  {
                    bos.write(b, 0, l);
                  }
                content = bos.toByteArray();
              }
          }
        catch (IOException e)
          {
            throw new MessagingException("I/O error", e);
          }
      }
    modified = false;
  }

  // -- From --
  
  /**
   * Returns the value of the RFC 822 From header field.
   * If this header field is absent, the Sender header field is used instead.
   */
  public Address[] getFrom()
    throws MessagingException
  {
    Address[] from = getInternetAddresses(FROM_NAME);
    if (from == null)
      {
        from = getInternetAddresses(SENDER_NAME);
      }
    return from;
  }

  /**
   * Sets the RFC 822 From header field.
   * @param address the sender of this message
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setFrom(Address address)
    throws MessagingException
  {
    if (address == null)
      {
        removeHeader(FROM_NAME);
      }
    else
      {
        setHeader(FROM_NAME, address.toString());
      }
  }

  /**
   * Sets the RFC 822 From header field using the value of the
   * <code>InternetAddress.getLocalAddress</code> method.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setFrom()
    throws MessagingException
  {
    InternetAddress localAddress = 
      InternetAddress.getLocalAddress(session);
    if (localAddress != null)
      {
        setFrom(localAddress);
      }
    else
      {
        throw new MessagingException("No local address");
      }
  }

  /**
   * Adds the specified addresses to From header field.
   * @param addresses the senders of this message
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void addFrom(Address[] addresses)
    throws MessagingException
  {
    addInternetAddresses(FROM_NAME, addresses);
  }

  /**
   * Returns the value of the RFC 822 Sender header field.
   * @since JavaMail 1.3
   */
  public Address getSender()
    throws MessagingException
  {
    Address[] sender = getInternetAddresses(SENDER_NAME);
    if (sender != null && sender.length > 0)
      {
        return sender[0];
      }
    else
      {
        return null;
      }
  }

  /**
   * Sets the RFC 822 Sender header field.
   * @param address the sender of this message
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @since JavaMail 1.3
   */
  public void setSender(Address address)
    throws MessagingException
  {
    Address[] addresses = new Address[] { address };
    addInternetAddresses(SENDER_NAME, addresses);
  }

  // -- To --
  
  /**
   * Returns the recipients of the given type.
   * @param type the recipient type
   */
  public Address[] getRecipients(Message.RecipientType type)
    throws MessagingException
  {
    if (type == RecipientType.NEWSGROUPS)
      {
        // Can't use getInternetAddresses here
        // and it's not worth a getNewsAddresses method
        String header = getHeader(NEWSGROUPS_NAME, ",");
        return (header != null) ? NewsAddress.parse(header) : null;
      }
    return getInternetAddresses(getHeader(type));
  }

  /**
   * Returns all the recipients.
   * This returns the TO, CC, BCC, and NEWSGROUPS recipients.
   */
  public Address[] getAllRecipients()
    throws MessagingException
  {
    Address[] recipients = super.getAllRecipients();
    Address[] newsgroups = getRecipients(RecipientType.NEWSGROUPS);
    if (newsgroups == null)
      {
        return recipients;
      }
    else if (recipients == null)
      {
        return newsgroups;
      }
    else
      {
        Address[] both = new Address[recipients.length + newsgroups.length];
        System.arraycopy(recipients, 0, both, 0, recipients.length);
        System.arraycopy(newsgroups, 0, both, recipients.length, 
                          newsgroups.length);
        return both;
      }
  }

  /**
   * Sets the recipients of the given type.
   * @param type the recipient type
   * @param addresses the addresses, or null to remove recipients of this
   * type
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setRecipients(Message.RecipientType type, Address[] addresses)
    throws MessagingException
  {
    if (type == RecipientType.NEWSGROUPS)
      {
        if (addresses == null || addresses.length == 0)
          {
            removeHeader(NEWSGROUPS_NAME);
          }
        else
          {
            setHeader(NEWSGROUPS_NAME, NewsAddress.toString(addresses));
          }
      }
    else
      {
        setInternetAddresses(getHeader(type), addresses);
      }
  }

  /**
   * Sets the recipients of the given type.
   * @param type the recipient type
   * @param addresses the addresses, or null to remove recpients of this
   * type
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setRecipients(Message.RecipientType type, String addresses)
    throws MessagingException
  {
    if (type == RecipientType.NEWSGROUPS)
      {
        if (addresses == null || addresses.length() == 0)
          {
            removeHeader(NEWSGROUPS_NAME);
          }
        else
          {
            setHeader(NEWSGROUPS_NAME, addresses);
          }
      }
    else
      {
        setInternetAddresses(getHeader(type),
                              InternetAddress.parse(addresses));
      }
  }

  /**
   * Adds the given addresses to the recipients of the specified type.
   * @param type the recipient type
   * @param addresses the addresses
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void addRecipients(Message.RecipientType type, Address[] addresses)
    throws MessagingException
  {
    if (type == RecipientType.NEWSGROUPS)
      {
        String value = NewsAddress.toString(addresses);
        if (value != null)
          {
            addHeader(NEWSGROUPS_NAME, value);
          }
      }
    else
      {
        addInternetAddresses(getHeader(type), addresses);
      }
  }

  /**
   * Adds the given addresses to the recipients of the specified type.
   * @param type the recipient type
   * @param addresses the addresses
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void addRecipients(Message.RecipientType type, String addresses)
    throws MessagingException
  {
    if (type == RecipientType.NEWSGROUPS)
      {
        if (addresses != null && addresses.length() != 0)
          {
            addHeader(NEWSGROUPS_NAME, addresses);
          }
      }
    else
      {
        addInternetAddresses(getHeader(type),
                              InternetAddress.parse(addresses));
      }
  }

  /**
   * Returns the value of the RFC 822 Reply-To header field.
   * If the header is absent, the value of the <code>getFrom</code> method
   * is returned.
   */
  public Address[] getReplyTo()
    throws MessagingException
  {
    Address[] replyTo = getInternetAddresses(REPLY_TO_NAME);
    if (replyTo == null)
      {
        replyTo = getFrom();
      }
    return replyTo;
  }

  /**
   * Sets the RFC 822 Reply-To header field.
   * @param addresses the addresses, or <code>null</code> to remove this
   * header
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setReplyTo(Address[] addresses)
    throws MessagingException
  {
    setInternetAddresses(REPLY_TO_NAME, addresses);
  }

  // convenience method
  private Address[] getInternetAddresses(String name)
    throws MessagingException
  {
    String value = getHeader(name, ",");
    // Use InternetAddress.parseHeader since 1.3
    String s = session.getProperty("mail.mime.address.strict");
    boolean strict = (s == null) || Boolean.valueOf(s).booleanValue();
    return (value != null) ? InternetAddress.parseHeader(value, strict) : null;
  }

  // convenience method
  private void setInternetAddresses(String name, Address[] addresses)
    throws MessagingException
  {
    String line = InternetAddress.toString(addresses);
    if (line == null)
      {
        removeHeader(line);
      }
    else
      {
        setHeader(name, line);
      }
  }

  // convenience method
  private void addInternetAddresses(String name, Address[] addresses)
    throws MessagingException
  {
    String line = InternetAddress.toString(addresses);
    if (line != null)
      {
        addHeader(name, line);
      }
  }

  /*
   * Convenience method to return the header name for a given recipient
   * type. This should be faster than keeping a hash of recipient types to
   * names.
   */
  private String getHeader(Message.RecipientType type)
    throws MessagingException
  {
    if (type == Message.RecipientType.TO)
      {
        return TO_NAME;
      }
    if (type == Message.RecipientType.CC)
      {
        return CC_NAME;
      }
    if (type == Message.RecipientType.BCC)
      {
        return BCC_NAME;
      }
    if (type == RecipientType.NEWSGROUPS)
      {
        return NEWSGROUPS_NAME;
      }
    throw new MessagingException("Invalid recipient type");
  }

  /**
   * Returns the value of the Subject header field.
   * <p>
   * If the subject is encoded as per RFC 2047, it is decoded and converted 
   * into Unicode.
   */
  public String getSubject()
    throws MessagingException
  {
    String subject = getHeader(SUBJECT_NAME, null);
    if (subject == null)
      {
        return null;
      }
    try
      {
        subject = MimeUtility.decodeText(subject);
      }
    catch (UnsupportedEncodingException e)
      {
      }
    return subject;
  }

  /**
   * Sets the Subject header field.
   * <p>
   * If the subject contains non US-ASCII characters, it will be encoded 
   * using the platform default charset.
   * @param subject the subject
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setSubject(String subject)
    throws MessagingException
  {
    setSubject(subject, null);
  }

  /**
   * Sets the Subject header field.
   * <p>
   * If the subject contains non US-ASCII characters, it will be encoded 
   * using the specified charset.
   * @param subject the subject
   * @param charset the charset used for any encoding
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setSubject(String subject, String charset)
    throws MessagingException
  {
    if (subject == null)
      {
        removeHeader(SUBJECT_NAME);
      }
    try
      {
        setHeader(SUBJECT_NAME,
                   MimeUtility.encodeText(subject, charset, null));
      }
    catch (UnsupportedEncodingException e)
      {
        throw new MessagingException("Encoding error", e);
      }
  }

  /**
   * Returns the value of the RFC 822 Date field.
   * This is the date on which this message was sent.
   */
  public Date getSentDate()
    throws MessagingException
  {
    String value = getHeader(DATE_NAME, null);
    if (value != null)
      {
        try
          {
            return dateFormat.parse(value);
          }
        catch (ParseException e)
          {
          }
      }
    return null;
  }

  /**
   * Sets the RFC 822 Date header field.
   * @param date the sent date, or <code>null</code> to remove this header
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setSentDate(Date date)
    throws MessagingException
  {
    if (date == null)
      {
        removeHeader(DATE_NAME);
      }
    else
      {
        setHeader(DATE_NAME, dateFormat.format(date));
      }
  }

  /**
   * Returns the date on which this message was received.
   * This returns null if the received date cannot be obtained.
   */
  public Date getReceivedDate()
    throws MessagingException
  {
    // hence...
    return null;
  }

  /**
   * Returns the size of the content of this message in bytes, or -1 if the
   * size cannot be determined.
   * <p>
   * Note that this number may not be an exact measure, but if not -1, it
   * will be suitable for display to the user.
   */
  public int getSize()
    throws MessagingException
  {
    if (content != null)
      {
        return content.length;
      }
    if (contentStream != null)
      {
        try
          {
            int available = contentStream.available();
            if (available > 0)
              {
                return available;
              }
          }
        catch (IOException e)
          {
          }
      }
    return -1;
  }

  /**
   * Returns the number of lines in the content of this message, or -1 if
   * this number cannot be determined.
   * <p>
   * Note that this number may not be an exact measure, but if not -1, it
   * will be suitable for display to the user.
   */
  public int getLineCount()
    throws MessagingException
  {
    return -1;
  }

  /**
   * Returns the value of the RFC 822 Content-Type header field, or
   * "text/plain" if the header is not available.
   */
  public String getContentType()
    throws MessagingException
  {
    String contentType = getHeader(MimeBodyPart.CONTENT_TYPE_NAME, null);
    if (contentType == null)
      {
        return MimeBodyPart.TEXT_PLAIN;
      }
    return contentType;
  }

  /**
   * Indicates whether this message is of the specified MIME type.
   * <p>
   * If the subtype of <code>mimeType</code> is the special character '*',
   * the subtype is ignored during the comparison.
   * @see MimeBodyPart#isMimeType
   */
  public boolean isMimeType(String mimeType)
    throws MessagingException
  {
    return (new ContentType(getContentType()).match(mimeType));
  }

  /**
   * Returns the value of the RFC 822 Content-Disposition header field, or
   * <code>null</code> if the header is not available.
   * @see MimeBodyPart#getDisposition
   */
  public String getDisposition()
    throws MessagingException
  {
    String disposition = 
      getHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME, null);
    if (disposition != null)
      {
        return new ContentDisposition(disposition).getDisposition();
      }
    return null;
  }

  /**
   * Sets the Content-Disposition header field of this message.
   * @param disposition the disposition value to set, or null to remove
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setDisposition
   */
  public void setDisposition(String disposition)
    throws MessagingException
  {
    if (disposition == null)
      {
        removeHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME);
      }
    else
      {
        String value = getHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME, null);
        if (value != null)
          {
            ContentDisposition cd = new ContentDisposition(value);
            cd.setDisposition(disposition);
            disposition = cd.toString();
          }
        setHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME, disposition);
      }
  }

  /**
   * Returns the value of the Content-Transfer-Encoding header field.
   * @see MimeBodyPart#getEncoding
   */
  public String getEncoding()
    throws MessagingException
  {
    String encoding = 
      getHeader(MimeBodyPart.CONTENT_TRANSFER_ENCODING_NAME, null);
    if (encoding != null)
      {
        encoding = encoding.trim();
        if (encoding.equalsIgnoreCase("7bit") || 
            encoding.equalsIgnoreCase("8bit") || 
            encoding.equalsIgnoreCase("quoted-printable") ||
            encoding.equalsIgnoreCase("base64"))
          {
            return encoding;
          }
        HeaderTokenizer ht = new HeaderTokenizer(encoding,
                                                  HeaderTokenizer.MIME);
        for (boolean done = false; !done; )
          {
            HeaderTokenizer.Token token = ht.next();
            switch (token.getType())
              {
              case HeaderTokenizer.Token.EOF:
                done = true;
                break;
              case HeaderTokenizer.Token.ATOM:
                return token.getValue();
              }
          }
        return encoding;
      }
    return null;
  }

  /**
   * Returns the value of the Content-ID header field.
   * @see MimeBodyPart#getContentID
   */
  public String getContentID()
    throws MessagingException
  {
    return getHeader(MimeBodyPart.CONTENT_ID_NAME, null);
  }

  /**
   * Sets the Content-ID header field of this message.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setContentID(String cid)
    throws MessagingException
  {
    if (cid == null)
      {
        removeHeader(MimeBodyPart.CONTENT_ID_NAME);
      }
    else
      {
        setHeader(MimeBodyPart.CONTENT_ID_NAME, cid);
      }
  }

  /**
   * Returns the value of the Content-MD5 header field.
   * @see MimeBodyPart#getContentMD5
   */
  public String getContentMD5()
    throws MessagingException
  {
    return getHeader(MimeBodyPart.CONTENT_MD5_NAME, null);
  }

  /**
   * Sets the Content-MD5 header field of this message.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setContentMD5
   */
  public void setContentMD5(String md5)
    throws MessagingException
  {
    setHeader(MimeBodyPart.CONTENT_MD5_NAME, md5);
  }

  /**
   * Returns the Content-Description header field of this message.
   * <p>
   * If the Content-Description field is encoded as per RFC 2047,
   * it is decoded and converted into Unicode.
   * @see MimeBodyPart#getDescription
   */
  public String getDescription()
    throws MessagingException
  {
    String header = getHeader(MimeBodyPart.CONTENT_DESCRIPTION_NAME, null);
    if (header != null)
      {
        try
          {
            return MimeUtility.decodeText(header);
          }
        catch (UnsupportedEncodingException e)
          {
            return header;
          }
      }
    return null;
  }

  /**
   * Sets the Content-Description header field for this message.
   * <p>
   * If the description contains non US-ASCII characters, it will be encoded
   * using the platform default charset.
   * @param description the content description
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setDescription
   */
  public void setDescription(String description)
    throws MessagingException
  {
    setDescription(description, null);
  }

  /**
   * Sets the Content-Description header field for this message.
   * <p>
   * If the description contains non US-ASCII characters, it will be encoded
   * using the specified charset.
   * @param description the content description
   * @param charset the charset used for any encoding
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setDescription
   */
  public void setDescription(String description, String charset)
    throws MessagingException
  {
    if (description != null)
      {
        try
          {
            setHeader(MimeBodyPart.CONTENT_DESCRIPTION_NAME,
                       MimeUtility.encodeText(description, charset, null));
          }
        catch (UnsupportedEncodingException e)
          {
            throw new MessagingException("Encode error", e);
          }
      }
    else
      {
        removeHeader(MimeBodyPart.CONTENT_DESCRIPTION_NAME);
      }
  }

  /**
   * Returns the languages specified in the Content-Language header field 
   * of this message, as defined by RFC 1766. This method returns
   * <code>null</code> if this header is not available.
   * @see MimeBodyPart#getContentLanguage
   */
  public String[] getContentLanguage()
    throws MessagingException
  {
    String header = getHeader(MimeBodyPart.CONTENT_LANGUAGE_NAME, null);
    if (header != null)
      {
        HeaderTokenizer ht = new HeaderTokenizer(header, HeaderTokenizer.MIME);
        ArrayList acc = new ArrayList();
        for (boolean done = false; !done; )
          {
            HeaderTokenizer.Token token = ht.next();
            switch (token.getType())
              {
              case HeaderTokenizer.Token.EOF:
                done = true;
                break;
              case HeaderTokenizer.Token.ATOM:
                acc.add(token.getValue());
                break;
              }
          } 
        if (acc.size() > 0)
          {
            String[] languages = new String[acc.size()];
            acc.toArray(languages);
            return languages;
          }
      }
    return null;
  }

  /**
   * Sets the Content-Language header of this message.
   * @param languages the array of language tags
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setContentLanguage
   */
  public void setContentLanguage(String[] languages)
    throws MessagingException
  {
    if (languages != null && languages.length > 0)
      {
        StringBuffer buffer = new StringBuffer();
        buffer.append(languages[0]);
        for (int i = 1; i < languages.length; i++)
          {
            buffer.append(',');
            buffer.append(languages[i]);
          }
        setHeader(MimeBodyPart.CONTENT_LANGUAGE_NAME, buffer.toString());
      }
    else
      {
        setHeader(MimeBodyPart.CONTENT_LANGUAGE_NAME, null);
      }
  }

  /**
   * Returns the value of the Message-ID header field.
   */
  public String getMessageID()
    throws MessagingException
  {
    return getHeader(MESSAGE_ID_NAME, null);
  }

  /**
   * Returns the filename associated with this message.
   * <p>
   * This method returns the value of the "filename" parameter from the
   * Content-Disposition header field of this message.
   * If the latter is not available, it returns the value of the "name"
   * parameter from the Content-Type header field.
   * @see MimeBodyPart#getFileName
   */
  public String getFileName()
    throws MessagingException
  {
    String filename = null;
    String header = getHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME, null);
    if (header != null)
      {
        ContentDisposition cd = new ContentDisposition(header);
        filename = cd.getParameter("filename");
      }
    if (filename == null)
      {
        header = getHeader(MimeBodyPart.CONTENT_TYPE_NAME, null);
        if (header != null)
          {
            ContentType contentType = new ContentType(header);
            filename = contentType.getParameter("name");
          }
      }
    PrivilegedAction a =
      new GetSystemPropertyAction("mail.mime.decodefilename");
    if ("true".equals(AccessController.doPrivileged(a)))
      {
        try
          {
            filename = MimeUtility.decodeText(filename);
          }
        catch (UnsupportedEncodingException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
    return filename;
  }

  /**
   * Sets the filename associated with this part.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setFileName
   */
  public void setFileName(String filename)
    throws MessagingException
  {
    PrivilegedAction a =
      new GetSystemPropertyAction("mail.mime.encodefilename");
    if ("true".equals(AccessController.doPrivileged(a)))
      {
        try
          {
            filename = MimeUtility.encodeText(filename);
          }
        catch (UnsupportedEncodingException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
    String header = getHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME, null);
    if (header == null)
      {
        header = "attachment";
      }
    ContentDisposition cd = new ContentDisposition(header);
    cd.setParameter("filename", filename);
    setHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME, cd.toString());

    // We will also set the "name" parameter of the Content-Type field
    // to preserve compatibility with nonconformant MUAs
    header = getHeader(MimeBodyPart.CONTENT_TYPE_NAME, null);
    if (header == null)
      {
        DataHandler dh0 = getDataHandler();
        if (dh0 != null)
          header = dh0.getContentType();
        else
          header = "text/plain";
      }
    ContentType contentType = new ContentType(header);
    contentType.setParameter("name", filename);
    setHeader(MimeBodyPart.CONTENT_TYPE_NAME, contentType.toString());
  }

  /**
   * Returns a decoded input stream for this message's content.
   * @exception IOException if an error occurs in the data handler layer
   * @see MimeBodyPart#getInputStream
   */
  public InputStream getInputStream()
    throws IOException, MessagingException
  {
    return getDataHandler().getInputStream();
  }

  /**
   * Returns the unencoded bytes of the content.
   * @see MimeBodyPart#getContentStream
   */
  protected InputStream getContentStream()
    throws MessagingException
  {
    if (contentStream != null)
      {
        return ((SharedInputStream) contentStream).newStream(0L, -1L);
      }
    if (content != null)
      {
        return new ByteArrayInputStream(content);
      }
    else
      {
        throw new MessagingException("No content");
      }
  }

  /**
   * Returns the unencoded bytes of the content without applying any content
   * transfer encoding.
   * @see MimeBodyPart#getRawInputStream
   */
  public InputStream getRawInputStream()
    throws MessagingException
  {
    return getContentStream();
  }

  /**
   * Returns a data handler for accessing this message's content.
   */
  public synchronized DataHandler getDataHandler()
    throws MessagingException
  {
    if (dh == null)
      {
        dh = new DataHandler(new MimePartDataSource(this));
      }
    return dh;
  }

  /**
   * Returns this message's content as a Java object.
   * @exception IOException if an error occurred in the data handler layer
   */
  public Object getContent()
    throws IOException, MessagingException
  {
    return getDataHandler().getContent();
  }

  /**
   * Sets the content of this part using the specified data handler.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setDataHandler
   */
  public void setDataHandler(DataHandler datahandler)
    throws MessagingException
  {
    dh = datahandler;
    // The Content-Type and Content-Transfer-Encoding headers may need to be
    // recalculated by the new DataHandler - see updateHeaders()
    removeHeader(MimeBodyPart.CONTENT_TYPE_NAME);
    removeHeader(MimeBodyPart.CONTENT_TRANSFER_ENCODING_NAME);
    removeHeader(MESSAGE_ID_NAME);
  }

  /**
   * Sets the content of this message using the specified Java object and
   * MIME type. Note that a data content handler for the MIME type must be
   * installed and accept objects of the type given.
   * @param o the content object
   * @param type the MIME type of the object
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setContent
   */
  public void setContent(Object o, String type)
    throws MessagingException
  {
    setDataHandler(new DataHandler(o, type));
  }

  /**
   * Sets the content of this message using the specified text, and with a
   * MIME type of "text/plain".
   * <p>
   * If the string contains non US-ASCII characters, it will be encoded 
   * using the platform default charset.
   * @param text the text content
   * @see MimeBodyPart#setText(String)
   */
  public void setText(String text)
    throws MessagingException
  {
    setText(text, null, "plain");
  }

  /**
   * Sets the content of this message using the specified text, and with a
   * MIME type of "text/plain".
   * <p>
   * If the string contains non US-ASCII characters, it will be encoded 
   * using the specified charset.
   * @param text the text content
   * @param charset the charset used for any encoding
   * @see MimeBodyPart#setText(String,String)
   */
  public void setText(String text, String charset)
    throws MessagingException
  {
    setText(text, charset, "plain");
  }
  
  /**
   * Sets the content of this message using the specified text, and with a
   * text MIME type of the specified subtype.
   * <p>
   * If the string contains non US-ASCII characters, it will be encoded 
   * using the specified charset.
   * @param text the text content
   * @param charset the charset used for any encoding
   * @param subtype the MIME text subtype (e.g. "plain", "html")
   * @see MimeBodyPart#setText(String,String,String)
   * @since JavaMail 1.4
   */
  public void setText(String text, String charset, String subtype)
    throws MessagingException
  {
    if (charset == null)
      {
        // According to the API doc for getText(String), we may have to scan
        // the characters to determine the charset.
        // However this should work just as well and is hopefully relatively
        // cheap.
        charset =
          MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());
      }
    if (subtype == null || "".equals(subtype))
      subtype = "plain";
    StringBuffer buffer = new StringBuffer();
    buffer.append("text/").append(subtype).append("; charset=");
    buffer.append(MimeUtility.quote(charset, HeaderTokenizer.MIME));
    setContent(text, buffer.toString());
  }

  /**
   * Sets the content of this message to be the specified multipart.
   * @param mp the multipart content
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#setContent(Multipart)
   */
  public void setContent(Multipart mp)
    throws MessagingException
  {
    setDataHandler(new DataHandler(mp, mp.getContentType()));
    // Ensure component hierarchy
    mp.setParent(this);
  }

  /**
   * Returns a new message suitable for a reply to this message.
   * The new message will have its headers set appropriately for sending,
   * but no content.
   * @param replyToAll the reply should be sent to all the recipients of
   * this message
   */
  public Message reply(boolean replyToAll)
    throws MessagingException
  {
    MimeMessage message = createMimeMessage(session);
    String subject = getHeader(SUBJECT_NAME, null);
    if (subject != null)
      {
        if (!subject.startsWith("Re: "))
          {
            subject = "Re: " + subject;
          }
        message.setHeader(SUBJECT_NAME, subject);
      }
    Address[] addresses = getReplyTo();
    message.setRecipients(Message.RecipientType.TO, addresses);
    if (replyToAll)
      {
        // We use a Set to store the addresses in order to ensure no address
        // duplication.
        HashSet set = new HashSet();
        set.addAll(Arrays.asList(addresses));
        
        InternetAddress localAddress =
          InternetAddress.getLocalAddress(session);
        if (localAddress != null)
          {
            set.add(localAddress);
          }
        String alternates = session.getProperty("mail.alternates");
        if (alternates != null)
          {
            set.addAll( Arrays.asList( InternetAddress.parse(alternates,
                                                              false)));
          }
        
        set.addAll(Arrays.asList(getRecipients(Message.RecipientType.TO)));
        addresses = new Address[set.size()];
        set.toArray(addresses);
        
        boolean replyAllCC = 
          new Boolean(session.getProperty("mail.replyallcc")).booleanValue();
        if (addresses.length > 0)
          {
            if (replyAllCC)
              {
                message.addRecipients(Message.RecipientType.CC, addresses);
              }
            else
              {
                message.addRecipients(Message.RecipientType.TO, addresses);
              }
          }
      
        set.clear();
        set.addAll(Arrays.asList(getRecipients(Message.RecipientType.CC)));
        addresses = new Address[set.size()];
        set.toArray(addresses);
        
        if (addresses != null && addresses.length > 0)
          {
            message.addRecipients(Message.RecipientType.CC, addresses);
          }
      
        addresses = getRecipients(RecipientType.NEWSGROUPS);
        if (addresses != null && addresses.length > 0)
          {
            message.setRecipients(RecipientType.NEWSGROUPS, addresses);
          }
      }
    
    // Set In-Reply-To(will be replaced by References for NNTP)
    String mid = getHeader(MESSAGE_ID_NAME, null);
    if (mid != null)
      {
        message.setHeader("In-Reply-To", mid);
      }
    try
      {
        setFlag(Flags.Flag.ANSWERED, true);
      }
    catch (MessagingException e)
      {
      }
    return message;
  }

  /**
   * Writes this message to the specified stream in RFC 822 format.
   * @exception IOException if an error occurs writing to the stream or in
   * the data handler layer
   */
  public void writeTo(OutputStream os)
    throws IOException, MessagingException
  {
    writeTo(os, null);
  }

  /**
   * Writes this message to the specified stream in RFC 822 format, without
   * the specified headers.
   * @exception IOException if an error occurs writing to the stream or in
   * the data handler layer
   */
  public void writeTo(OutputStream os, String[] ignoreList)
    throws IOException, MessagingException
  {
    if (!saved)
      {
        saveChanges();
      }

    String charset = "US-ASCII"; // MIME default charset
    byte[] sep = new byte[] { 0x0d, 0x0a };

    // Write the headers
    for (Enumeration e = getNonMatchingHeaderLines(ignoreList);
         e.hasMoreElements(); )
      {
        String line = (String) e.nextElement();
        StringTokenizer st = new StringTokenizer(line, "\r\n");
        int count = 0;
        while (st.hasMoreTokens())
          {
            String line2 = st.nextToken();
            if (count > 0 && line2.charAt(0) != '\t')
              {
                // Folded line must start with tab
                os.write(0x09);
              }
            /*
             * RFC 2822, section 2.1 states that each line should be no more
             * than 998 characters.
             * Ensure that any headers we emit have no lines longer than
             * this by folding the line.
             */
            int max = (count > 0) ? 997 : 998;
            while (line2.length() > max)
              {
                String left = line2.substring(0, max);
                byte[] bytes = left.getBytes(charset);
                os.write(bytes);
                os.write(sep);
                os.write(0x09);
                line2 = line2.substring(max);
                max = 997; // make space for the tab
              }
            byte[] bytes = line2.getBytes(charset);
            os.write(bytes);
            os.write(sep);
            count++;
          }
      }
    os.write(sep);
    os.flush();

    if (modified || content == null && contentStream == null)
      {
        // use datahandler
        os = MimeUtility.encode(os, getEncoding());
        getDataHandler().writeTo(os);
        os.flush();
      }
    else
      {
        // write content directly
        if (contentStream != null)
          {
            InputStream is =
             ((SharedInputStream) contentStream).newStream(0L, -1L);
            // TODO make buffer size configurable
            int len = 8192;
            byte[] bytes = new byte[len];
            while ((len = is.read(bytes)) > -1) 
              {
                os.write(bytes, 0, len);
              }
            is.close();
          }
        else
          {
            os.write(content);
          }
        os.flush();
      }
  }

  static int fc = 1;

  /**
   * Returns all the values for the specified header name.
   * Note that headers may be encoded as per RFC 2047 if they 
   * contain non-US-ASCII characters: these should be decoded.
   * @param name the header name
   */
  public String[] getHeader(String name)
    throws MessagingException
  {
    return headers.getHeader(name);
  }

  /**
   * Returns all the values for the specified header name as a single
   * string, with headers separated by the given delimiter.
   * If the delimiter is <code>null</code>, only the first header is
   * returned.
   * @param name the header name
   * @param delimiter the delimiter
   */
  public String getHeader(String name, String delimiter)
    throws MessagingException
  {
    return headers.getHeader(name, delimiter);
  }

  /**
   * Sets the specified header.
   * @param name the header name
   * @param value the header value
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setHeader(String name, String value)
    throws MessagingException
  {
    headers.setHeader(name, value);
  }

  /**
   * Adds the specified header.
   * @param name the header name
   * @param value the header value
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void addHeader(String name, String value)
    throws MessagingException
  {
    headers.addHeader(name, value);
  }

  /**
   * Removes all headers with the specified name.
   * @param name the header name
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void removeHeader(String name)
    throws MessagingException
  {
    headers.removeHeader(name);
  }

  /**
   * Returns all the headers.
   * @return an Enumeration of Header objects
   */
  public Enumeration getAllHeaders()
    throws MessagingException
  {
    return headers.getAllHeaders();
  }

  /**
   * Returns all the headers with any of the given names.
   * @return an Enumeration of Header objects
   */
  public Enumeration getMatchingHeaders(String[] names)
    throws MessagingException
  {
    return headers.getMatchingHeaders(names);
  }

  /**
   * Returns all the headers without any of the given names.
   * @return an Enumeration of Header objects
   */
  public Enumeration getNonMatchingHeaders(String[] names)
    throws MessagingException
  {
    return headers.getNonMatchingHeaders(names);
  }

  /**
   * Adds an RFC 822 header-line to this message.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void addHeaderLine(String line)
    throws MessagingException
  {
    headers.addHeaderLine(line);
  }

  /**
   * Returns all the header-lines.
   * @return an Enumeration of Strings
   */
  public Enumeration getAllHeaderLines()
    throws MessagingException
  {
    return headers.getAllHeaderLines();
  }

  /**
   * Returns all the header-lines with any of the given names.
   * @return an Enumeration of Strings
   */
  public Enumeration getMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    return headers.getMatchingHeaderLines(names);
  }

  /**
   * Returns all the header-lines without any of the given names.
   * @return an Enumeration of Strings
   */
  public Enumeration getNonMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    return headers.getNonMatchingHeaderLines(names);
  }

  /**
   * Returns the flags for this message.
   */
  public Flags getFlags()
    throws MessagingException
  {
    return (Flags) flags.clone();
  }

  /**
   * Indicates whether the specified flag is set in this message.
   * @param flag the flag
   */
  public boolean isSet(Flags.Flag flag)
    throws MessagingException
  {
    return flags.contains(flag);
  }

  /**
   * Sets the flags for this message.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void setFlags(Flags flag, boolean set)
    throws MessagingException
  {
    if (set)
      {
        flags.add(flag);
      }
    else
      {
        flags.remove(flag);
      }
  }

  /**
   * Saves any changes to this message.
   * Header fields in the message are updated appropriately to be consistent
   * with the message contents.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   */
  public void saveChanges()
    throws MessagingException
  {
    modified = true;
    saved = true;
    updateHeaders();
  }

  /**
   * Updates the headers of this part, based on the content.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder
   * @see MimeBodyPart#updateHeaders
   */
  protected void updateHeaders()
    throws MessagingException
  {
    // This code is from MimeBodyPart
    if (getDataHandler() != null)
      {
        try
          {
            String contentType = dh.getContentType();
            ContentType ct = new ContentType(contentType);
            if (ct.match("multipart/*"))
              {
                MimeMultipart mmp = (MimeMultipart) dh.getContent();
                mmp.updateHeaders();
              } 
            else if (ct.match("message/rfc822"))
              {
              }
            else
              {
                // Update Content-Transfer-Encoding
                if (getHeader(MimeBodyPart.CONTENT_TRANSFER_ENCODING_NAME)
                    == null)
                  {
                    setHeader(MimeBodyPart.CONTENT_TRANSFER_ENCODING_NAME,
                               MimeUtility.getEncoding(dh));
                  }
              }

            // Update Content-Type if nonexistent,
            // and Content-Type "name" with Content-Disposition "filename"
            // parameter(see setFilename())
            if (getHeader(MimeBodyPart.CONTENT_TYPE_NAME) == null)
              {
                String disposition =
                  getHeader(MimeBodyPart.CONTENT_DISPOSITION_NAME, null);
                if (disposition != null)
                  {
                    ContentDisposition cd =
                      new ContentDisposition(disposition);
                    String filename = cd.getParameter("filename");
                    if (filename != null)
                      {
                        ct.setParameter("name", filename);
                        contentType = ct.toString();
                      }
                  }
                setHeader(MimeBodyPart.CONTENT_TYPE_NAME, contentType);
              }
          }
        catch (IOException e)
          {
            throw new MessagingException("I/O error", e);
          }
      }
    
    // Below is MimeMessage-specific.
    // set mime version
    setHeader("Mime-Version", "1.0");
    // set new message-id if necessary
    updateMessageId();
  }
  
  /**
   * Creates the headers from the given input stream.
   * @param is the input stream to read the headers from
   */
  protected InternetHeaders createInternetHeaders(InputStream is)
    throws MessagingException
  {
    return new InternetHeaders(is);
  }

  /**
   * Updates the Message-ID header. This method is called by
   * <code>updateHeaders</code>, and should set the Message-Id header to a
   * suitably unique value if overridden.
   * @since JavaMail 1.4
   */
  protected void updateMessageId()
    throws MessagingException
  {
    String mid = getHeader(MESSAGE_ID_NAME, null);
    if (mid == null)
      {
        StringBuffer buffer = new StringBuffer();
        buffer.append('<');
        buffer.append(MimeUtility.getUniqueMessageIDValue(session));
        buffer.append('>');
        mid = buffer.toString();
        setHeader(MESSAGE_ID_NAME, mid);
      }
  }

  /**
   * Creates a new MIME message.
   * Used by the <code>reply</code> method to determine the MimeMessage
   * subclass, if any, to use.
   * @since JavaMail 1.4
   */
  protected MimeMessage createMimeMessage(Session session)
    throws MessagingException
  {
    return new MimeMessage(session);
  }

}

