/*
 * MimeTypeParseException.java
 * Copyright (C) 2004 The Free Software Foundation
 * 
 * This file is part of GNU Java Activation Framework (JAF), a library.
 * 
 * GNU JAF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GNU JAF is distributed in the hope that it will be useful,
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
package javax.activation;

/**
 * Exception thrown to indicate a malformed MIME content type.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public class MimeTypeParseException
  extends Exception
{

  /**
   * Constructs a MimeTypeParseException with no detail message.
   */
  public MimeTypeParseException()
  {
  }
  
  /**
   * MimeTypeParseException with the specified detail message.
   * @param message the exception message
   */
  public MimeTypeParseException(String message)
  {
    super(message);
  }

  /**
   * Constructs a MimeTypeParseException with the specified detail message
   * and token in error.
   * @param message the exception message
   * @param token the token in error
   */
  MimeTypeParseException(String message, String token)
  {
    this(new StringBuffer(message)
         .append(':')
         .append(' ')
         .append(token)
         .toString());
  }
  
}

