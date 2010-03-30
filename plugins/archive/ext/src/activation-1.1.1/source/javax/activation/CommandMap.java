/*
 * CommandMap.java
 * Copyright (C) 2004, 2005 The Free Software Foundation
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
 * Registry of command objects available to the system.
 * 
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public abstract class CommandMap
{

  /* Class scope */
  
  private static CommandMap defaultCommandMap;
  
  /**
   * Returns the default command map.
   * This returns a MailcapCommandMap if no value has been set using
   * <code>setDefaultCommandMap</code>.
   */
  public static CommandMap getDefaultCommandMap()
  {
    if (defaultCommandMap == null)
      {
        defaultCommandMap = new MailcapCommandMap();
      }
    return defaultCommandMap;
  }
  
  /**
   * Sets the default command map.
   * @param commandMap the new default command map
   */
  public static void setDefaultCommandMap(CommandMap commandMap)
  {
    SecurityManager security = System.getSecurityManager();
    if (security != null)
      {
        try
          {
            security.checkSetFactory();
          }
        catch (SecurityException e)
          {
            if (commandMap != null && CommandMap.class.getClassLoader() !=
                commandMap.getClass().getClassLoader())
              {
                throw e;
              }
          }
      }
    defaultCommandMap = commandMap;
  }

  /* Instance scope */
  
  /**
   * Returns the list of preferred commands for a MIME type.
   * @param mimeType the MIME type
   */
  public abstract CommandInfo[] getPreferredCommands(String mimeType);
  
  /**
   * Returns the complete list of commands for a MIME type.
   * @param mimeType the MIME type
   */
  public abstract CommandInfo[] getAllCommands(String mimeType);
  
  /**
   * Returns the command corresponding to the specified MIME type and
   * command name.
   * @param mimeType the MIME type
   * @param cmdName the command name
   */
  public abstract CommandInfo getCommand(String mimeType, String cmdName);
  
  /**
   * Returns a DataContentHandler corresponding to the MIME type.
   * @param mimeType the MIME type
   */
  public abstract DataContentHandler createDataContentHandler(String mimeType);
  
  /**
   * Get all the MIME types known to this command map.
   * If the command map doesn't support this operation, null is returned.
   * @return array of MIME types as strings, or null if not supported
   * @since JAF 1.1
   */
  public String[] getMimeTypes()
  {
    return null;
  }
  
  /**
   * Get the preferred command list from a MIME Type. The actual semantics
   * are determined by the implementation of the CommandMap.
   * <p>
   * The <code>DataSource</code> provides extra information, such as
   * the file name, that a CommandMap implementation may use to further
   * refine the list of commands that are returned.  The implementation
   * in this class simply calls the <code>getPreferredCommands</code>
   * method that ignores this argument.
   * @param mimeType the MIME type
   * @param ds a DataSource for the data
   * @return the CommandInfo classes that represent the command Beans.
   * @since JAF 1.1
   */
  public CommandInfo[] getPreferredCommands(String mimeType,
                                            DataSource ds)
  {
    return getPreferredCommands(mimeType);
  }
  
  /**
   * Get all the available commands for this type. This method
   * should return all the possible commands for this MIME type.
   * <p>
   * The <code>DataSource</code> provides extra information, such as
   * the file name, that a CommandMap implementation may use to further
   * refine the list of commands that are returned.  The implementation
   * in this class simply calls the <code>getAllCommands</code>
   * method that ignores this argument.
   * @param mimeType the MIME type
   * @param ds a DataSource for the data
   * @return the CommandInfo objects representing all the commands.
   * @since JAF 1.1
   */
  public CommandInfo[] getAllCommands(String mimeType, DataSource ds)
  {
    return getAllCommands(mimeType);
  }
  
  /**
   * Get the default command corresponding to the MIME type.
   * <p>
   * The <code>DataSource</code> provides extra information, such as
   * the file name, that a CommandMap implementation may use to further
   * refine the command that is chosen.  The implementation
   * in this class simply calls the <code>getCommand</code>
   * method that ignores this argument.
   * @param mimeType the MIME type
   * @param cmdName the command name
   * @param ds a DataSource for the data
   * @return the CommandInfo corresponding to the command.
   * @since JAF 1.1
   */
  public CommandInfo getCommand(String mimeType, String cmdName,
                                DataSource ds)
  {
    return getCommand(mimeType, cmdName);
  }
  
  /**
   * Locate a DataContentHandler that corresponds to the MIME type.
   * The mechanism and semantics for determining this are determined
   * by the implementation of the particular CommandMap.
   * <p>
   * The <code>DataSource</code> provides extra information, such as
   * the file name, that a CommandMap implementation may use to further
   * refine the choice of DataContentHandler.  The implementation
   * in this class simply calls the <code>createDataContentHandler</code>
   * method that ignores this argument.
   * @param mimeType the MIME type
   * @param ds a DataSource for the data
   * @return the DataContentHandler for the MIME type
   * @since JAF 1.1
   */
  public DataContentHandler createDataContentHandler(String mimeType,
                                                     DataSource ds)
  {
    return createDataContentHandler(mimeType);
  }  

}

