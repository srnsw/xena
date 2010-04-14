/*
 * Multipart.java
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
import java.io.OutputStream;
import java.util.Vector;
import javax.activation.DataSource;

/**
 * A multipart is a container for multiple body parts. 
 * <p>
 * Some messaging systems provide different subtypes of multiparts.
 * For example, MIME specifies a set of subtypes that include 
 * "alternative", "mixed", "related", "parallel", "signed", etc.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class Multipart
{

  /**
   * Vector of body parts.
   */
  protected Vector parts;

  /**
   * The content-type of this multipart object.
   * It defaults to "multipart/mixed".
   */
  protected String contentType;

  /**
   * The part containing this multipart, if known.
   */
  protected Part parent;

  /**
   * Constructor for an empty multipart of type "multipart/mixed".
   */
  protected Multipart()
  {
    contentType = "multipart/mixed";
    parent = null;
  }

  /**
   * Configures this multipart from the given data source.
   * <p>
   * The method adds the body parts in the data source to this multipart,
   * and sets the content-type.
   * @param mp a multipart data source
   */
  protected void setMultipartDataSource(MultipartDataSource mp)
    throws MessagingException
  {
    contentType = mp.getContentType();
    int count = mp.getCount();
    for (int i = 0; i < count; i++)
      {
        addBodyPart(mp.getBodyPart(i));
      }
  }

  /**
   * Returns the content-type of this multipart.
   */
  public String getContentType()
  {
    return contentType;
  }

  /**
   * Returns the number of enclosed body parts.
   */
  public int getCount()
    throws MessagingException
  {
    return (parts == null) ? 0 : parts.size();
  }

  /**
   * Get the specified body part.
   * The body parts in this container are numbered starting at 0.
   * @param index the index of the desired body part
   * @exception IndexOutOfBoundsException if the given index is out of range
   */
  public BodyPart getBodyPart(int index)
    throws MessagingException
  {
    if (parts == null)
      {
        throw new IndexOutOfBoundsException();
      }
    return (BodyPart) parts.get(index);
  }

  /**
   * Removes the specified body part from this multipart.
   * @param part the body part to remove
   * @return true if a body part was removed, false otherwise
   * @exception MessagingException if the multipart has not been configured
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   */
  public boolean removeBodyPart(BodyPart part)
    throws MessagingException
  {
    if (parts == null)
      {
        throw new MessagingException("No such BodyPart");
      }
    synchronized (parts)
      {
        boolean success = parts.remove(part);
        if (success)
          {
            part.setParent(null);
          }
        return success;
      }
  }

  /**
   * Removes the body part at the specified index.
   * The body parts in this container are numbered starting at 0.
   * @param index index of the part to remove
   * @exception IndexOutOfBoundsException if the given index is out of range
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   */
  public void removeBodyPart(int index)
    throws MessagingException
  {
    if (parts == null)
      {
        throw new IndexOutOfBoundsException("No such BodyPart");
      }
    synchronized (parts)
      {
        BodyPart part = (BodyPart) parts.get(index);
        parts.remove(index);
        part.setParent(null);
      }
  }

  /**
   * Adds a body part to this multipart. 
   * @param part the body part to be appended
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   */
  public synchronized void addBodyPart(BodyPart part)
    throws MessagingException
  {
    if (parts == null)
      {
        parts = new Vector();
      }
    synchronized (parts)
      {
        parts.add(part);
        part.setParent(this);
      }
  }

  /**
   * Inserts a body part at the specified index.
   * The body parts in this container are numbered starting at 0.
   * @param part the body part to be inserted
   * @param index where to insert the part
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   */
  public synchronized void addBodyPart(BodyPart part, int index)
    throws MessagingException
  {
    if (parts == null)
      {
        parts = new Vector();
      }
    synchronized (parts)
      {
        parts.add(index, part);
        part.setParent(this);
      }
  }

  /**
   * Writes this multipart to the specified byte stream.
   */
  public abstract void writeTo(OutputStream os)
    throws IOException, MessagingException;

  /**
   * Returns the part containing this multipart, or <code>null</code> if
   * not known.
   */
  public Part getParent()
  {
    return parent;
  }

  /**
   * Sets the parent of this multipart.
   */
  public void setParent(Part part)
  {
    parent = part;
  }
  
}
