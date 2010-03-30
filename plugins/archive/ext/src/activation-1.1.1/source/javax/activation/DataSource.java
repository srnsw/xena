/*
 * DataSource.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface by which MIME data can be retrieved and stored.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public interface DataSource
{
  
  /**
   * Returns an input stream from which the data can be read.
   */
  InputStream getInputStream()
    throws IOException;
  
  /**
   * Returns an output stream to which the data can be written.
   */
  OutputStream getOutputStream()
    throws IOException;
  
  /**
   * Returns the MIME content type of the data.
   */
  String getContentType();
  
  /**
   * Returns the underlying name of this object.
   */
  String getName();
  
}

