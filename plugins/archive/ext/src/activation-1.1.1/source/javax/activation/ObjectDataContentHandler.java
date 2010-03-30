/*
 * ObjectDataContentHandler.java
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Data content handler that uses an existing DCH and reified object.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
class ObjectDataContentHandler
  implements DataContentHandler
{

  private DataContentHandler dch;
  private Object object;
  private String mimeType;
  private DataFlavor[] flavors;
  
  public ObjectDataContentHandler(DataContentHandler dch, Object object,
                                  String mimeType)
  {
    this.dch = dch;
    this.object = object;
    this.mimeType = mimeType;
  }
  
  public Object getContent(DataSource ds)
  {
    return object;
  }
  
  public DataContentHandler getDCH()
  {
    return dch;
  }

  public Object getTransferData(DataFlavor flavor, DataSource ds)
    throws UnsupportedFlavorException, IOException
  {
    if (dch != null)
      {
        return dch.getTransferData(flavor, ds);
      }
    if (flavors == null)
      {
        getTransferDataFlavors();
      }
    if (flavor.equals(flavors[0]))
      {
        return object;
      }
    throw new UnsupportedFlavorException(flavor);
  }
  
  public DataFlavor[] getTransferDataFlavors()
  {
    if (flavors == null)
      {
        if (dch != null)
          {
            flavors = dch.getTransferDataFlavors();
          }
        else
          {
            flavors = new DataFlavor[1];
            flavors[0] = new ActivationDataFlavor(object.getClass(),
                                                  mimeType, mimeType);
          }
      }
    return flavors;
  }

  public void writeTo(Object object, String mimeType, OutputStream out)
    throws IOException
  {
    if (dch != null)
      {
        dch.writeTo(object, mimeType, out);
      }
    else
      {
        throw new UnsupportedDataTypeException("no object DCH for MIME type " + mimeType);
      }
  }
  
}

