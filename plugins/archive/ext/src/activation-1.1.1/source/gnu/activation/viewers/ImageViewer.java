/*
 * ImageViewer.java
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
package gnu.activation.viewers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.activation.CommandObject;
import javax.activation.DataHandler;

/**
 * Simple image display component.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0.2
 */
public class ImageViewer extends Component
  implements CommandObject
{

  private Image image;

  /**
   * Returns the preferred size for this component (the image size).
   */
  public Dimension getPreferredSize()
    {
      Dimension ps = new Dimension(0, 0);
      if (image != null)
        {
          ps.width = image.getWidth(this);
          ps.height = image.getHeight(this);
        }
      return ps;
    }

  public void setCommandContext(String verb, DataHandler dh)
    throws IOException
  {
    // Read image into a byte array
    InputStream in = dh.getInputStream();
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    for (int len = in.read(buf); len != -1; len = in.read(buf))
      bytes.write(buf, 0, len);
    in.close();
    // Create and prepare the image
    Toolkit toolkit = getToolkit();
    Image img = toolkit.createImage(bytes.toByteArray());
    try
      {
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(img, 0);
        tracker.waitForID(0);
      }
    catch (InterruptedException e)
      {
      }
    toolkit.prepareImage(img, -1, -1, this);
  }

  /**
   * Image bits arrive.
   */
  public boolean imageUpdate(Image image, int flags, int x, int y,
                             int width, int height)
  {
    if ((flags & ALLBITS) != 0)
      {
        this.image = image;
        invalidate();
        repaint();
        return false;
      }
    return ((flags & ERROR) == 0);
  }

  /**
   * Scale the image into this component's bounds.
   */
  public void paint(Graphics g)
  {
    if (image != null)
      {
        Dimension is = new Dimension(image.getWidth(this),
                                     image.getHeight(this));
        if (is.width > -1 && is.height > -1)
          {
            Dimension cs = getSize();
            g.drawImage(image, 0, 0, cs.width, cs.height, 
                        0, 0, is.width, is.height, this);
          }
      }
  }

}
