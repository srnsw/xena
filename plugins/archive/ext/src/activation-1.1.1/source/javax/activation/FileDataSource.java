/*
 * FileDataSource.java
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

import java.io.*;

/**
 * Data source encapsulating a file.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public class FileDataSource
  implements DataSource
{

  private File file;
  private FileTypeMap typeMap;
  
  /**
   * Constructor.
   * @param file the underlying file to use
   */
  public FileDataSource(File file)
  {
    this.file = file;
  }
  
  /**
   * Constructor.
   * @param name the path to the underlying file to use
   */
  public FileDataSource(String name)
  {
    this(new File(name));
  }
  
  public InputStream getInputStream()
    throws IOException
  {
    return new FileInputStream(file);
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    return new FileOutputStream(file);
  }
  
  public String getContentType()
  {
    if (typeMap == null)
      {
        FileTypeMap dftm = FileTypeMap.getDefaultFileTypeMap();
        return dftm.getContentType(file);
      }
    return typeMap.getContentType(file);
  }
  
  public String getName()
  {
    return file.getName();
  }
  
  /**
   * Returns the underlying file.
   */
  public File getFile()
  {
    return file;
  }

  /**
   * Sets the file type map to use to determine the content type of the file.
   * @param map the file type map
   */
  public void setFileTypeMap(FileTypeMap map)
  {
    typeMap = map;
  }
  
}

