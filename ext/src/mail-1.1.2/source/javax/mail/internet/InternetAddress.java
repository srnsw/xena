/*
 * InternetAddress.java
 * Copyright (C) 2002, 2004 The Free Software Foundation
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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Session;

/**
 * An RFC 822 address.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class InternetAddress
  extends Address
  implements Cloneable
{

  /*
   * The type of address.
   */
  private static final String RFC822 = "rfc822";

  /**
   * The string representation of the address.
   */
  protected String address;

  /**
   * The personal name.
   */
  protected String personal;

  /**
   * The RFC 2047 encoded version of the personal name.
   */
  protected String encodedPersonal;
  
  /**
   * Constructor for an empty address.
   */
  public InternetAddress()
  {
  }

  /**
   * Constructor with an RFC 822 string representation of the address.
   * Note that this parses the address in non-strict mode: this is for
   * compatibility with implementations and not with the JavaMail
   * specification.
   * @param address the address in RFC 822 format
   * @exception AddressException if the parse failed
   */
  public InternetAddress(String address)
    throws AddressException
  {
    this(address, false);
  }

  /**
   * Constructor with an RFC 822 string representation of the address.
   * @param address the address in RFC 822 format
   * @param strict enforce RFC 822 syntax
   * @exception AddressException if the parse failed
   * @since JavaMail 1.3
   */
  public InternetAddress(String address, boolean strict)
    throws AddressException
  {
    InternetAddress[] addresses = parseHeader(address, strict);
    if (addresses.length != 1)
      {
        throw new AddressException("Illegal address", address);
      }
    this.address = addresses[0].address;
    this.personal = addresses[0].personal;
    this.encodedPersonal = addresses[0].encodedPersonal;
    if (strict)
      {
        validate(address, true, true);
      }
  }

  /**
   * Constructor with an address and personal name.
   * The address is assumed to be syntactically valid according to RFC 822.
   * @param address the address in RFC 822 format
   * @param personal the personal name
   */
  public InternetAddress(String address, String personal)
    throws UnsupportedEncodingException
  {
    this(address, personal, null);
  }

  /**
   * Construct with an address and personal name.
   * The address is assumed to be syntactically valid according to RFC 822.
   * @param address the address in RFC 822 format
   * @param personal the personal name
   * @param charset the charset for the personal name
   */
  public InternetAddress(String address, String personal, String charset)
    throws UnsupportedEncodingException
  {
    this.address = address;
    setPersonal(personal, charset);
  }

  /**
   * Returns a copy of this address.
   */
  public Object clone()
  {
    InternetAddress clone = new InternetAddress();
    clone.address = this.address;
    clone.personal = personal;
    clone.encodedPersonal = encodedPersonal;
    return clone;
  }

  /**
   * Returns the type of this address.
   * The type of an <code>InternetAddress</code> is "rfc822".
   */
  public String getType()
  {
    return RFC822;
  }

  /**
   * Indicates whether this address is an RFC 822 group address.
   * Group addresses are not mailing list addresses and are rarely used;
   * see RFC 822 for details.
   * @since JavaMail 1.3
   */
  public boolean isGroup()
  {
    int start = address.indexOf(':');
    if (start == -1)
      {
        return false;
      }
    int end = address.length() - 1;
    return (address.charAt(end) == ';');
  }

  /**
   * Returns the members of a group address. A group may have any number of
   * members. If this address is not a group, this method returns
   * <code>null</code>.
   * @exception AddressException if a parse error occurs
   * @since JavaMail 1.3
   */
  public InternetAddress[] getGroup(boolean strict)
    throws AddressException
  {
    int start = address.indexOf(':');
    int end = address.length() - 1;
    if (start == -1 || address.charAt(end) == ';')
      {
        return null;
      }
    return parseHeader(address.substring(start + 1, end), strict);
  }

  /**
   * Sets the email address.
   */
  public void setAddress(String address)
  {
    this.address = address;
  }

  /**
   * Sets the personal name.
   * If the name contains non US-ASCII characters, it will be encoded using
   * the specified charset as per RFC 2047.
   * @param name the personal name
   * @param charset the charset to be used for any encoding
   * @param UnsupportedEncodingException if charset encoding fails
   */
  public void setPersonal(String name, String charset)
    throws UnsupportedEncodingException
  {
    personal = name;
    if (name != null)
      {
        if (charset == null)
          {
            encodedPersonal = MimeUtility.encodeWord(name);
          }
        else
          {
            encodedPersonal = MimeUtility.encodeWord(name, charset, null);
          }
      }
    else
      {
        encodedPersonal = null;
      }
  }

  /**
   * Sets the personal name.
   * If the name contains non US-ASCII characters, it will be encoded using
   * the platform default charset.
   * @param name the personal name
   * @exception UnsupportedEncodingException if charset encoding fails
   */
  public void setPersonal(String name)
    throws UnsupportedEncodingException
  {
    setPersonal(name, null);
  }

  /**
   * Returns the email address.
   */
  public String getAddress()
  {
    return address;
  }

  /**
   * Returns the personal name.
   */
  public String getPersonal()
  {
    if (personal != null)
      {
        return personal;
      }
    if (encodedPersonal != null)
      {
        try
          {
            personal = MimeUtility.decodeText(encodedPersonal);
            return personal;
          }
        catch (Exception e)
          {
            return encodedPersonal;
          }
      }
    return null;
  }

  /**
   * Validate this address according to the syntax rules of RFC 822.
   * This implementation checks many but not all of the syntax rules.
   * @exception AddressException if the address is invalid
   * @since JavaMail 1.3
   */
  public void validate()
    throws AddressException
  {
    validate(address, true, true);
  }

  /**
   * Returns the RFC 822 / RFC 2047 string representation of this address.
   * The resulting string contains only US-ASCII characters,
   * and is therefore mail-safe.
   */
  public String toString()
  {
    if (encodedPersonal == null && personal != null)
      {
        try
          {
            encodedPersonal = MimeUtility.encodeWord(personal);
          }
        catch (UnsupportedEncodingException e)
          {
          }
      }
    
    StringBuffer buffer = new StringBuffer();
    if (encodedPersonal != null)
      {
        buffer.append(quote(encodedPersonal));
        buffer.append(' ');
        buffer.append('<');
        buffer.append(address);
        buffer.append('>');
      }
    else if (isGroupAddress(address) || isSimpleAddress(address))
      {
        buffer.append(address);
      }
    else
      {
        buffer.append('<');
        buffer.append(address);
        buffer.append('>');
      }
    return buffer.toString();
  }

  /**
   * Returns the RFC 822 string representation of this address.
   * The returned string may contain unencoded Unicode characters and may
   * therefore not be mail-safe.
   */
  public String toUnicodeString()
  {
    StringBuffer buffer = new StringBuffer();
    if (getPersonal() != null)
      {
        buffer.append(quote(personal));
        buffer.append(' ');
        buffer.append('<');
        buffer.append(address);
        buffer.append('>');
      }
    else if (isGroupAddress(address) || isSimpleAddress(address))
      {
        buffer.append(address);
      }
    else
      {
        buffer.append('<');
        buffer.append(address);
        buffer.append('>');
      }
    return buffer.toString();
  }

  /*
   * Indicates whether this address is simple.
   */
  private static boolean isSimpleAddress(String address)
  {
    return (address.indexOf('"') == -1) &&
     (address.indexOf('(') == -1) &&
     (address.indexOf(')') == -1) &&
     (address.indexOf(',') == -1) &&
     (address.indexOf(':') == -1) &&
     (address.indexOf(';') == -1) &&
     (address.indexOf('<') == -1) &&
     (address.indexOf('>') == -1) &&
     (address.indexOf('[') == -1) &&
     (address.indexOf('\\') == -1) &&
     (address.indexOf(']') == -1);
  }

  /*
   * Indicates whether this address is a group address (see RFC 822).
   */
  private static boolean isGroupAddress(String address)
  {
    int len = address.length();
    return (len > 0 &&
            address.indexOf(':') > 0 &&
            address.charAt(len - 1) == ';');
  }

  public boolean equals(Object other)
  {
    if (other instanceof InternetAddress)
      {
        String otherAddress = ((InternetAddress) other).getAddress();
        return (this == other || 
               (address != null && address.equalsIgnoreCase(otherAddress)));
      }
    return false;
  }

  public int hashCode()
  {
    return (address == null) ? 0 : address.hashCode();
  }

  /**
   * Converts the given array of InternetAddresses into a comma-separated
   * sequence of address strings.
   * The resulting string contains only US-ASCII characters,
   * and is therefore mail-safe.
   * @param addresses the InternetAddresses
   * @exception ClassCastException if any of the specified addresses is not
   * an InternetAddress
   */
  public static String toString(Address[] addresses)
  {
    return toString(addresses, 0);
  }

  /**
   * Converts the given array of InternetAddresses into a comma-separated
   * sequence of address strings.
   * The resulting string contains only US-ASCII characters,
   * and is therefore mail-safe.
   * @param addresses the InternetAddresses
   * @param used the number of character positions already used, in the
   * field into which the address string is to be inserted
   * @exception ClassCastException if any of the specified addresses is not
   * an InternetAddress
   */
  public static String toString(Address[] addresses, int used)
  {
    if (addresses == null || addresses.length == 0)
      {
        return null;
      }
    String crlf = "\r\n";
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < addresses.length; i++)
      {
        if (i != 0)
          {
            buffer.append(", ");
            used += 2;
          }
        String addressText = addresses[i].toString();
        int len = addressText.length();
        int fl = addressText.indexOf(crlf); // pos of first crlf
        if (fl < 0)
          {
            fl = addressText.length();
          }
        int ll = addressText.lastIndexOf(crlf); // pos of last crlf
        
        if ((used + fl) > 76)
          {
            buffer.append("\r\n\t");
            used = 8;
          }
        buffer.append(addressText);
        used = (ll > -1) ?(used + len) :(len - ll - 2);
      }
    return buffer.toString();
  }

  /**
   * Returns an InternetAddress object representing the current user.
   * This information is determined from the following locations, in order
   * of preference:
   * <ol>
   * <li>the session property <code>mail.from</code></li>
   * <li>the session properties <code>mail.user</code> or
   * <code>user.name</code>, and <code>mail.host</code></li>
   * <li>the system property <code>user.name</code> and the hostname of
   * localhost as determined by <code>InetAddress.getLocalHost</code></li>
   * </ol>
   * @param session the session
   */
  public static InternetAddress getLocalAddress(Session session)
  {
    String username = null;
    String hostname = null;
    String address = null;
    try
      {
        if (session == null)
          {
            username = System.getProperty("user.name");
            hostname = InetAddress.getLocalHost().getHostName();
          }
        else
          {
            address = session.getProperty("mail.from");
            if (address == null)
              {
                username = session.getProperty("mail.user");
                if (username == null)
                  {
                    username = session.getProperty("user.name");
                  }
                if (username == null)
                  {
                    username = System.getProperty("user.name");
                  }
                hostname = session.getProperty("mail.host");
                if (hostname == null)
                  {
                    InetAddress localhost = InetAddress.getLocalHost();
                    if (localhost != null)
                      {
                        hostname = localhost.getCanonicalHostName();
                      }
                  }
              }
          }
        if (address == null && username != null && hostname != null)
          {
            StringBuffer buffer = new StringBuffer();
            buffer.append(username);
            buffer.append('@');
            buffer.append(hostname);
            address = buffer.toString();
          }
        if (address != null)
          {
            return new InternetAddress(address);
          }
      }
    catch (AddressException e)
      {
      }
    catch (SecurityException e)
      {
      }
    catch (UnknownHostException e)
      {
      }
    return null;
  }

  /**
   * Parses the given comma-separated sequence of RFC 822 addresses into 
   * InternetAddresses.
   * @param addresslist the comma-separated addresses
   * @exception AddressException if the parse failed
   */
  public static InternetAddress[] parse(String addresslist)
    throws AddressException
  {
    return parse(addresslist, true);
  }

  /**
   * Parses the given comma-separated sequence of RFC 822 addresses into
   * InternetAddresses.
   * If <code>strict</code> is false, simple email addresses separated by 
   * spaces are also allowed. If <code>strict</code> is true, many (but not
   * all) of the RFC 822 syntax rules are enforced.
   * Even if <code>strict</code> is true, addresses composed of simple
   * names (with no "@domain" part) are allowed.
   * @param addresslist the comma-separated addresses
   * @param strict whether to enforce RFC 822 syntax
   * @exception AddressException if the parse failed
   */
  public static InternetAddress[] parse(String addresslist, boolean strict)
    throws AddressException
  {
    return parse(addresslist, strict ? STRICT : NONE);
  }

  /**
   * Parses the given comma-separated sequence of RFC 822 addresses into
   * InternetAddresses.
   * If <code>strict</code> is false, simple email addresses separated by 
   * spaces are also allowed. If <code>strict</code> is true, many (but not
   * all) of the RFC 822 syntax rules are enforced.
   * @param addresslist the comma-separated addresses
   * @param strict whether to enforce RFC 822 syntax
   * @exception AddressException if the parse failed
   * @since JavaMail 1.3
   */
  public static InternetAddress[] parseHeader(String addresslist,
                                              boolean strict)
    throws AddressException
  {
    return parse(addresslist, strict ? STRICT_OR_LAX : LAX);
  }

  private static final int NONE = 0x00;
  private static final int LAX = 0x01;
  private static final int STRICT = 0x02;
  private static final int STRICT_OR_LAX = 0x03;

  private static InternetAddress[] parse(String addresslist, int rules)
    throws AddressException
  {
    /*
     * address := mailbox / group ; one addressee, named list
     * group := phrase ":" [#mailbox] ";"
     * mailbox := addr-spec / phrase route-addr ; simple address,
     *                                          ; name & addr-spec
     * route-addr := "<" [route] addr-spec ">"
     * route := 1#("@" domain) ":" ; path-relative
     * addr-spec := local-part "@" domain ; global address
     * local-part := word *("." word) ; uninterpreted, case-preserved
     * domain := sub-domain *("." sub-domain)
     * sub-domain := domain-ref / domain-literal
     * domain-ref := atom ; symbolic reference
     */

    // NB I have been working on this parse for about 8 hours now.
    // It is very likely I am starting to lose the plot.
    // If anyone wants to work on it, I strongly recommend you write some
    // kind of tokenizer and attack it from that direction.

    boolean inGroup = false;
    boolean gotDelimiter = false;
    boolean inAddress = false;
    int len = addresslist.length();
    int pEnd = -1;
    int pStart = -1;
    int start = -1;
    int end = -1;
    ArrayList acc = new ArrayList();

    int pos;
    for (pos = 0; pos < len; pos++)
      {
        char c = addresslist.charAt(pos);
        switch (c)
          {
          case '\t':
          case '\n':
          case '\r':
          case ' ':
            break;
            
          case '<': // bra-ket delimited address
            inAddress = true;
            if (gotDelimiter)
              {
                throw new AddressException("Too many route-addr",
                                            addresslist, pos);
              }
            if (!inGroup)
              {
                start = pStart;
                if (start >= 0)
                  {
                    end = pos;
                  }
                pStart = pos + 1;
              }
            pos++;
            boolean inQuote = false;
            boolean gotKet = false;
            while (pos<len && !gotKet)
              {
                char c2 = addresslist.charAt(pos);
                switch (c2)
                  {
                  case '"':
                    inQuote = !inQuote;
                    break;
                  case '>':
                    if (!inQuote)
                      {
                        gotKet = true;
                        pos--;
                      }
                    break;
                  case '\\':
                    pos++;
                    break;
                  }
                pos++;
              }
            if (!gotKet && pos >= len)
              {
                if (inQuote)
                  {
                    throw new AddressException("Unmatched '\"'",
                                                addresslist, pos);
                  }
                throw new AddressException("Unmatched '<'", addresslist, pos);
              }
            gotDelimiter = true;
            pEnd = pos;
            break;
          case '>':
            throw new AddressException("Unmatched '>'", addresslist, pos);
            
          case '(': // paren delimited personal
            inAddress = true;
            if (pStart >= 0 && pEnd == -1)
              {
                pEnd = pos;
              }
            if (start == -1)
              {
                start = pos + 1;
              }
            pos++;
            int parenCount = 1;
            while (pos < len && parenCount > 0)
              {
                c = addresslist.charAt(pos);
                switch (c)
                  {
                  case '(':
                    parenCount++;
                    break;
                  case ')':
                    parenCount--;
                    break;
                  case '\\':
                    pos++;
                    break;
                  }
                pos++;
              }
            if (parenCount > 0)
              {
                throw new AddressException("Unmatched '('", addresslist, pos);
              }
            pos--;
            if (end == -1)
              {
                end = pos;
              }
            break;
          case ')':
            throw new AddressException("Unmatched ')'", addresslist, pos);
            
          case '"': // quote delimited personal
            inAddress = true;
            if (pStart == -1)
              {
                pStart = pos;
              }
            pos++;
            boolean gotQuote = false;
            while (pos < len && !gotQuote)
              {
                c = addresslist.charAt(pos);
                switch (c)
                  {
                  case '"':
                    gotQuote = true;
                    pos--;
                    break;
                  case '\\':
                    pos++;
                    break;
                  }
                pos++;
              }
            if (pos >= len)
              {
                throw new AddressException("Unmatched '\"'",
                                            addresslist, pos);
              }
            break;
            
          case '[':
            inAddress = true;
            pos++;
            boolean gotBracket = false;
            while (pos < len && !gotBracket)
              {
                c = addresslist.charAt(pos);
                switch (c)
                  {
                  case ']':
                    gotBracket = true;
                    pos--;
                    break;
                  case '\\':
                    pos++;
                    break;
                  }
                pos++;
              }
            if (pos >= len)
              {
                throw new AddressException("Unmatched '['",
                                            addresslist, pos);
              }
            break;
            
          case ',': // address delimiter
            if (pStart == -1)
              {
                gotDelimiter = false;
                inAddress = false;
                pEnd = -1;
                break;
              }
            if (inGroup)
              {
                break;
              }
            if (pEnd == -1)
              {
                pEnd = pos;
              }
              {
                String addressText = addresslist.substring(pStart, pEnd);
                addressText = addressText.trim();
                if (inAddress ||(rules | STRICT_OR_LAX) != 0)
                  {
                    if ((rules & STRICT) != 0 ||(rules & LAX) == 0)
                      {
                        validate(addressText, gotDelimiter, false);
                      }
                    InternetAddress address = new InternetAddress();
                    address.setAddress(addressText);
                    if (start >= 0)
                      {
                        String personal = addresslist.substring(start, end);
                        personal = personal.trim();
                        address.encodedPersonal = unquote(personal);
                        start = end = -1;
                      }
                    acc.add(address);
                  }
                else
                  {
                    StringTokenizer st = new StringTokenizer(addressText);
                    while (st.hasMoreTokens())
                      {
                        addressText = st.nextToken();
                        validate(addressText, false, false);
                        InternetAddress address = new InternetAddress();
                        address.setAddress(addressText);
                        acc.add(address);
                      }
                  }
              }
            gotDelimiter = false;
            inAddress = false;
            pStart = -1;
            pEnd = -1;
            break;
            
          case ':': // group indicator
            inAddress = true;
            if (inGroup)
              {
                throw new AddressException("Cannot have nested group",
                                            addresslist, pos);
              }
            inGroup = true;
            break;
          case ';': // group delimiter
            if (!inGroup)
              {
                throw new AddressException("Unexpected ';'",
                                            addresslist, pos);
              }
            inGroup = false;
            pEnd = pos + 1;
              {
                String addressText = addresslist.substring(pStart, pEnd);
                addressText = addressText.trim();
                InternetAddress address = new InternetAddress();
                address.setAddress(addressText);
                acc.add(address);
              }
            gotDelimiter = false;
            pStart = pEnd = -1;
            break;
            
          default:
            if (pStart == -1)
              {
                pStart = pos;
              }
            break;
          }
      }
    
    if (pStart > -1)
      {
        if (pEnd == -1)
          {
            pEnd = pos;
          }
        String addressText = addresslist.substring(pStart, pEnd);
        addressText = addressText.trim();
        if (inAddress ||(rules | STRICT_OR_LAX) != 0)
          {
            if ((rules & STRICT) != 0 ||(rules & LAX) == 0)
              {
                validate(addressText, gotDelimiter, false);
              }
            InternetAddress address = new InternetAddress();
            address.setAddress(addressText);
            if (start >= 0)
              {
                String personal = addresslist.substring(start, end);
                personal = personal.trim();
                address.encodedPersonal = unquote(personal);
              }
            acc.add(address);
          }
        else
          {
            StringTokenizer st = new StringTokenizer(addressText);
            while (st.hasMoreTokens())
              {
                addressText = st.nextToken();
                validate(addressText, false, false);
                InternetAddress address = new InternetAddress();
                address.setAddress(addressText);
                acc.add(address);
              }
          }
      }
    
    InternetAddress[] addresses = new InternetAddress[acc.size()];
    acc.toArray(addresses);
    return addresses;
  }

  private static void validate(String address, boolean gotDelimiter,
                                boolean strict)
    throws AddressException
  {
    // TODO What happens about addresses with quoted strings?
    int pos = 0;
    if (!strict || gotDelimiter)
    {
      int i = address.indexOf(',', pos);
      if (i < 0)
        {
          i = address.indexOf(':', pos);
        }
      while (i > -1)
        {
          if (address.charAt(pos) != '@')
            {
              throw new AddressException("Illegal route-addr", address);
            }
          if (address.charAt(i) != ':')
            {
              i = address.indexOf(',', pos);
              if (i < 0)
                {
                  i = address.indexOf(':', pos);
                }
            }
          else
            {
              pos = i + 1;
              i = -1;
            }
        }
    }
    
    // Get atomic parts
    String localName = address;
    String domain = null;
    int atIndex = address.indexOf('@', pos);
    if (atIndex > -1)
      {
        if (atIndex == pos)
          {
            throw new AddressException("Missing local name", address);
          }
        if (atIndex == address.length() - 1)
          {
            throw new AddressException("Missing domain", address);
          }
        localName = address.substring(pos, atIndex);
        domain = address.substring(atIndex + 1);
      }
    else if (strict)
      {
        throw new AddressException("Missing final @domain", address);
      }
    
    // Check atomic parts
    String illegalWS = "\t\n\r ";
    int len = 4; // illegalWS.length()
    for (int i = 0; i < len; i++)
      {
        if (address.indexOf(illegalWS.charAt(i)) > -1)
          {
            throw new AddressException("Illegal whitespace", address);
          }
      }
    String illegalName = "\"(),:;<>@[\\]";
    len = 12; // illegalName.length()
    for (int i = 0; i < len; i++)
      {
        if (localName.indexOf(illegalName.charAt(i)) > -1)
          {
            throw new AddressException("Illegal local name", address);
          }
      }
    if (domain != null)
      {
        for (int i = 0; i < len; i++)
          {
            if (domain.indexOf(illegalName.charAt(i)) > -1)
              {
                throw new AddressException("Illegal domain", address);
              }
          }
      }
  }

  /*
   * The list of characters that need quote-escaping.
   */
  private static final String needsQuoting = "()<>@,;:\\\".[]";

  /*
   * Quote-escapes the specified text.
   */
  private static String quote(String text)
  {
    int len = text.length();
    boolean needsQuotes = false;
    for (int i = 0; i < len; i++)
      {
        char c = text.charAt(i);
        if (c=='"' || c=='\\')
          {
            StringBuffer buffer = new StringBuffer(len + 3);
            buffer.append('"');
            for (int j = 0; j < len; j++)
              {
                c = text.charAt(j);
                if (c == '"' || c == '\\')
                  {
                    buffer.append('\\');
                  }
                buffer.append(c);
              }
            buffer.append('"');
            return buffer.toString();
          }
        if ((c < ' ' && c != '\r' && c != '\n' && c != '\t') ||
           (c >= '\177') ||
            needsQuoting.indexOf(c ) > -1)
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
        text = buffer.toString();
      }
    return text;
  }

  /*
   * Un-quote-escapes the specified text.
   */
  private static String unquote(String text)
  {
    int len = text.length();
    if (len > 2 && text.charAt(0) == '"' && text.charAt(len - 1) == '"')
      {
        text = text.substring(1, len - 1);
        if (text.indexOf('\\') > -1)
          {
            len -= 2;
            StringBuffer buffer = new StringBuffer(len);
            for (int i = 0; i < len; i++)
              {
                char c = text.charAt(i);
                if (c == '\\' && i <(len - 1))
                  {
                    c = text.charAt(++i);
                  }
                buffer.append(c);
              }
            text = buffer.toString();
          }
      }
    return text;
  }

}

