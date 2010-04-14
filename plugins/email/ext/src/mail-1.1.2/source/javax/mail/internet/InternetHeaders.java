/*
 * InternetHeaders.java
 * Copyright (C) 2002, 2005 The Free Software Foundation
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.Header;
import javax.mail.MessagingException;

import gnu.inet.util.CRLFInputStream;
import gnu.inet.util.LineInputStream;

/**
 * A collection of RFC 822 headers.
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
public class InternetHeaders
{

  /*
   * The header class that stores raw header lines.
   */
  protected static class InternetHeader
    extends Header
  {
    
    /**
     * The name.
     * @since JavaMail 1.4
     */
    protected String name;

    /**
     * The value.
     * @since JavaMail 1.4
     */
    protected String line;
    
    public InternetHeader(String line)
    {
      super(null, null);
      int i = line.indexOf(':');
      name = (i < 0) ? line.trim() : line.substring(0, i).trim();
      this.line = line;
    }
    
    public InternetHeader(String name, String value)
    {
      super(null, null);
      this.name = name;
      if (value != null)
        {
          StringBuffer buffer = new StringBuffer();
          buffer.append(name);
          buffer.append(':');
          buffer.append(' ');
          buffer.append(value);
          line = buffer.toString();
        }
    }

    public String getName()
    {
      return name;
    }

    public String getValue()
    {
      int i = line.indexOf(':');
      if (i < 0)
        {
          return line;
        }
      
      int pos, len = line.length();
      for (pos = i + 1; pos < len; pos++)
        {
          char c = line.charAt(pos);
          if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
            {
              break;
            }
        }
      
      return line.substring(pos);
    }

    void setValue(String value)
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append(name);
      buffer.append(':');
      buffer.append(' ');
      buffer.append(value);
      line = buffer.toString();
    }

    boolean nameEquals(String other)
    {
      return name.equalsIgnoreCase(other);
    }
    
  }

  /*
   * The enumeration used to filter headers for the InternetHeaders object.
   */
  static class HeaderEnumeration
    implements Iterator, Enumeration
  {

    private Iterator source;
    private String[] names;
    private boolean stringForm;
    private boolean matching;
    private InternetHeader nextHeader;
    
    HeaderEnumeration(Iterator source, String[] names,
                       boolean stringForm, boolean matching)
    {
      this.source = source;
      this.names = names;
      this.stringForm = stringForm;
      this.matching = matching;
    }
    
    /**
     * Enumeration syntax
     */
    public boolean hasMoreElements()
    {
      return hasNext();
    }

    /**
     * Iterator syntax
     */
    public boolean hasNext()
    {
      if (nextHeader == null)
        {
          nextHeader = getNext();
        }
      return (nextHeader != null);
    }
    
    /**
     * Enumeration syntax
     */
    public Object nextElement()
    {
      return next();
    }

    /**
     * Iterator syntax
     */
    public Object next()
    {
      if (nextHeader == null)
        {
          nextHeader = getNext();
        }
      if (nextHeader == null)
        {
          throw new NoSuchElementException();
        }
      
      InternetHeader header = nextHeader;
      nextHeader = null;
      
      if (stringForm)
        {
          return header.line;
        }
      return header;
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
    
    private InternetHeader getNext()
    {
      while (source.hasNext()) 
        {
          InternetHeader header = (InternetHeader) source.next();
          if (header.line == null)
            {
              continue;
            }
          
          if (names == null)
            {
              return (matching) ? null : header;
            }
          
          for (int i = 0; i < names.length; i++)
            {
              if (!header.nameEquals(names[i]))
                {
                  continue;
                }
              
              if (matching)
                {
                  return header;
                }
              
              return getNext();
            }
          
          if (!matching)
            {
              return header;
            }
        }
      return null;
    }
  
  }
  
  /**
   * The list of headers.
   * @since JavaMail 1.4
   */
  protected List headers = new ArrayList(20);

  /**
   * Constructor for an empty InternetHeaders.
   */
  public InternetHeaders()
  {
    headers.add(new InternetHeader("Return-Path", null));
    headers.add(new InternetHeader("Received", null));
    headers.add(new InternetHeader("Message-Id", null));
    headers.add(new InternetHeader("Resent-Date", null));
    headers.add(new InternetHeader("Date", null));
    headers.add(new InternetHeader("Resent-From", null));
    headers.add(new InternetHeader("From", null));
    headers.add(new InternetHeader("Reply-To", null));
    headers.add(new InternetHeader("To", null));
    headers.add(new InternetHeader("Subject", null));
    headers.add(new InternetHeader("Cc", null));
    headers.add(new InternetHeader("In-Reply-To", null));
    headers.add(new InternetHeader("Resent-Message-Id", null));
    headers.add(new InternetHeader("Errors-To", null));
    headers.add(new InternetHeader("Mime-Version", null));
    headers.add(new InternetHeader("Content-Type", null));
    headers.add(new InternetHeader("Content-Transfer-Encoding", null));
    headers.add(new InternetHeader("Content-MD5", null));
    headers.add(new InternetHeader("Content-Length", null));
    headers.add(new InternetHeader("Status", null));
  }

  /**
   * Constructor with an RFC 822 message stream.
   * The stream is parsed up to the blank line separating the headers from
   * the body, and is left positioned at the start of the body.
   * @param is an RFC 822 input stream
   */
  public InternetHeaders(InputStream is)
    throws MessagingException
  {
    load(is);
  }

  /**
   * Parses the specified RFC 822 message stream, storing the headers in
   * this InternetHeaders.
   * The stream is parsed up to the blank line separating the headers from
   * the body, and is left positioned at the start of the body.
   * Note that the headers are added into this InternetHeaders object:
   * any existing headers in this object are not affected.
   * @param is an RFC 822 input stream
   */
  public void load(InputStream is)
    throws MessagingException
  {
    LineInputStream in = new LineInputStream(is);
    try
      {
        for (String line = in.readLine(); line != null; line = in.readLine()) 
          {
            line = trim(line);
            if (line.length() == 0)
              {
                break;
              }
            addHeaderLine(line);
          }
      }
    catch (IOException e)
      {
        throw new MessagingException("I/O error", e);
      }
  }

  /**
   * Returns all the values for the specified header.
   * @param name the header name
   */
  public String[] getHeader(String name)
  {
    ArrayList acc = new ArrayList(headers.size());
    for (Iterator i = headers.iterator(); i.hasNext(); ) 
      {
        InternetHeader header = (InternetHeader) i.next();
        if (header.nameEquals(name) && header.line != null)
          {
            acc.add(header.getValue());
          }
      }
    int size = acc.size();
    if (size == 0)
      {
        return null;
      }
    String[] h = new String[size];
    acc.toArray(h);
    return h;
  }

  /**
   * Returns all the headers for this header name as a single string,
   * with headers separated by the given delimiter.
   * If the delimiter is <code>null</code>, only the first header is returned.
   * @param name the header name
   * @param delimiter the delimiter
   */
  public String getHeader(String name, String delimiter)
  {
    String[] h = getHeader(name);
    if (h == null)
      {
        return null;
      }
    
    if (delimiter == null || h.length == 1)
      {
        return h[0];
      }

    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < h.length; i++)
      {
        if (i > 0)
          {
            buffer.append(delimiter);
          }
        buffer.append(h[i]);
      }
    return buffer.toString();
  }

  /**
   * Sets the specified header.
   * If existing header lines match the given name, they are all replaced by
   * the specified value.
   * @param name the header name
   * @param value the header value
   */
  public void setHeader(String name, String value)
  {
    boolean first = true;
    for (int i = 0; i < headers.size(); i++)
    {
      InternetHeader header = (InternetHeader) headers.get(i);
      if (header.nameEquals(name))
        {
          if (first)
            {
              header.setValue(value);
              first = false;
            }
          else
            {
              headers.remove(i);
              i--;
            }
        }
    }
    if (first)
      {
        addHeader(name, value);
      }
  }

  /**
   * Adds the specified header.
   * @param name the header name
   * @param value the header value
   */
  public void addHeader(String name, String value)
  {
    synchronized (headers)
      {
        int len = headers.size();
        for (int i = len - 1; i >= 0; i--)
          {
            InternetHeader header = (InternetHeader) headers.get(i);
            if (header.nameEquals(name))
              {
                headers.add(i + 1, new InternetHeader(name, value));
                return;
              }
            if (header.nameEquals(":"))
              {
                len = i;
              }
          }
        headers.add(len, new InternetHeader(name, value));
      }
  }

  /**
   * Removes all headers matching the given name.
   * @param name the header name
   */
  public void removeHeader(String name)
  {
    synchronized (headers)
      {
        int len = headers.size();
        for (int i = 0; i < len; i++)
          {
            InternetHeader header = (InternetHeader) headers.get(i);
            if (header.nameEquals(name))
              {
                header.line = null;
              }
          }
      }
  }

  /**
   * Returns all the headers.
   * @return an Enumeration of Header objects
   */
  public Enumeration getAllHeaders()
  {
    return new HeaderEnumeration(headers.iterator(), null, false, false);
  }

  /**
   * Returns all the headers with any of the given names.
   * @param names the names to match
   * @return an Enumeration of Header objects
   */
  public Enumeration getMatchingHeaders(String[] names)
  {
    return new HeaderEnumeration(headers.iterator(), names, false, true);
  }

  /**
   * Returns all the headers without any of the given names.
   * @param names the names not to match
   * @return an Enumeration of Header objects
   */
  public Enumeration getNonMatchingHeaders(String[] names)
  {
    return new HeaderEnumeration(headers.iterator(), names, false, false);
  }

  /**
   * Adds an RFC 822 header-line to this InternetHeaders.
   * If the line starts with a space or tab (a continuation line for a
   * folded header), adds it to the last header line in the list.
   * @param line the raw RFC 822 header-line
   */
  public void addHeaderLine(String line)
  {
    try
      {
        char c = line.charAt(0);
        if (c == ' ' || c == '\t') // continuation character
          {
            int len = headers.size();
            InternetHeader header = (InternetHeader) headers.get(len - 1);
            StringBuffer buffer = new StringBuffer();
            buffer.append(header.line);
            buffer.append("\r\n");
            buffer.append(line);
            header.line = buffer.toString();
          }
        else
          {
            synchronized (headers)
              {
                headers.add(new InternetHeader(line));
              }
          }
      }
    catch (StringIndexOutOfBoundsException e)
      {
      }
    catch (NoSuchElementException e)
      {
      }
  }

  /**
   * Returns all the header-lines.
   * @return an Enumeration of Strings
   */
  public Enumeration getAllHeaderLines()
  {
    return new HeaderEnumeration(headers.iterator(), null, true, false);
  }

  /**
   * Returns all the header-lines with any of the given names.
   * @return an Enumeration of Strings
   */
  public Enumeration getMatchingHeaderLines(String[] names)
  {
    return new HeaderEnumeration(headers.iterator(), names, true, true);
  }

  /**
   * Returns all the header-lines without any of the given names.
   * @return an Enumeration of Strings
   */
  public Enumeration getNonMatchingHeaderLines(String[] names)
  {
    return new HeaderEnumeration(headers.iterator(), names, true, false);
  }

  private static String trim(String line)
  {
    int len = line.length();
    if (len > 0 && line.charAt(len - 1) == '\r')
      {
        line = line.substring(0, len - 1);
      }
    return line;
  }
  
}

