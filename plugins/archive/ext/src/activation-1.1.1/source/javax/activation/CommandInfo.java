/*
 * CommandInfo.java
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

import java.beans.Beans;
import java.io.Externalizable;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Description of the result of a command request.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public class CommandInfo
{

  private String verb;
  private String className;
  
  /**
   * Constructor.
   * @param verb the command verb
   * @param className the command class name
   */
  public CommandInfo(String verb, String className)
  {
    this.verb = verb;
    this.className = className;
  }
  
  /**
   * Returns the command verb.
   */
  public String getCommandName()
  {
    return verb;
  }
  
  /**
   * Returns the command class name.
   */
  public String getCommandClass()
  {
    return className;
  }
  
  /**
   * Returns the instantiated bean.
   * If the bean implements <code>CommandObject</code>, its
   * <code>setCommandContext</code> method will be called.
   * @param dh the data handler describing the command data
   * @param loader the class loader used to instantiate the bean
   */
  public Object getCommandObject(DataHandler dh, ClassLoader loader)
    throws IOException, ClassNotFoundException
  {
    Object object = Beans.instantiate(loader, className);
    if (object != null)
      {
        if (object instanceof CommandObject)
          {
            CommandObject command = (CommandObject)object;
            command.setCommandContext(verb, dh);
          }
        else if (dh != null && (object instanceof Externalizable))
          {
            InputStream in = dh.getInputStream();
            if (in != null)
              {
                Externalizable externalizable = (Externalizable)object;
                externalizable.readExternal(new ObjectInputStream(in));
              }
          }
      }
    return object;
  }
  
}

