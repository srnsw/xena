/*
 * TextViewer.java
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

import java.awt.Dimension;
import java.awt.TextArea;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.activation.CommandObject;
import javax.activation.DataHandler;

/**
 * Simple text display component.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0.2
 */
public class TextViewer extends TextArea
    implements CommandObject
{

    public TextViewer()
    {
        super("", 24, 80, 1);
        setEditable(false);
    }

    public Dimension getPreferredSize()
    {
        return getMinimumSize(24, 80);
    }

    public void setCommandContext(String verb, DataHandler dh)
        throws IOException
    {
        InputStream in = dh.getInputStream();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int len = in.read(buf); len != -1; len = in.read(buf))
            bytes.write(buf, 0, len);
        in.close();
        setText(bytes.toString());
    }
    
}
