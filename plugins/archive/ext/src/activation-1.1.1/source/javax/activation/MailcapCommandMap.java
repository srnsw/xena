/*
 * MailcapCommandMap.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a command map using a <code>mailcap</code> file (RFC
 * 1524). Mailcap files are searched for in the following places:
 * <ol>
 * <li>Programmatically added entries to this interface</li>
 * <li>the file <tt>.mailcap</tt> in the user's home directory</li>
 * <li>the file <i>&lt;java.home&gt;</i><tt>/lib/mailcap</tt></li>
 * <li>the resource <tt>META-INF/mailcap</tt></li>
 * <li>the resource <tt>META-INF/mailcap.default</tt> in the JAF
 * distribution</li>
 * </ol>
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public class MailcapCommandMap
    extends CommandMap
{

  private static final int PROG = 0;
  private static final int HOME = 1;
  private static final int SYS = 2;
  private static final int JAR = 3;
  private static final int DEF = 4;
  private static boolean debug = false;
  private static final int NORMAL = 0;
  private static final int FALLBACK = 1;
  
  static 
  {
    try
      {
        String d = System.getProperty("javax.activation.debug");
        debug = Boolean.valueOf(d).booleanValue();
      }
    catch (SecurityException e)
      {
      }
  }
  
  private Map[][] mailcaps;
  
  /**
   * Default constructor.
   */
  public MailcapCommandMap()
  {
    init(null);
  }
  
  /**
   * Constructor specifying a filename.
   * @param fileName the name of the file to read mailcap entries from
   */
  public MailcapCommandMap(String fileName)
    throws IOException
  {
    Reader in = null;
    try
      {
        in = new FileReader(fileName);
      }
    catch (IOException e)
      {
      }
    init(in);
    if (in != null)
      {
        try
          {
            in.close();
          }
        catch (IOException e)
          {
          }
      }
  }
  
  /**
   * Constructor specifying an input stream.
   * @param is the input stream to read mailcap entries from
   */
  public MailcapCommandMap(InputStream is)
  {
    init(new InputStreamReader(is));
  }

  private void init(Reader in)
  {
    mailcaps = new Map[5][2];
    for (int i = 0; i < 5; i++)
      {
        for (int j = 0; j < 2; j++)
          {
            mailcaps[i][j] = new LinkedHashMap();
          }
      }
    if (in != null)
      {
        if (debug)
          {
            System.out.println("MailcapCommandMap: load PROG");
          }
        try
          {
            parse(PROG, in);
          }
        catch (IOException e)
          {
          }
      }
    
    if (debug)
      {
        System.out.println("MailcapCommandMap: load HOME");
      }
    try
      {
        String home = System.getProperty("user.home");
        if (home != null)
          {
            parseFile(HOME, new StringBuffer(home)
                      .append(File.separatorChar)
                      .append(".mailcap")
                      .toString());
          }
      }
    catch (SecurityException e)
      {
      }
    
    if (debug)
      {
        System.out.println("MailcapCommandMap: load SYS");
      }
    try
      {
        parseFile(SYS, 
                  new StringBuffer(System.getProperty("java.home"))
                  .append(File.separatorChar)                                                     .append("lib")
                  .append(File.separatorChar)
                  .append("mailcap")
                  .toString());
      }
    catch (SecurityException e)
      {
      }
    
    if (debug)
      {
        System.out.println("MailcapCommandMap: load JAR");
      }
    List systemResources = getSystemResources("META-INF/mailcap");
    int len = systemResources.size();
    if (len > 0)
      {
        for (int i = 0; i < len ; i++)
          {
            Reader urlIn = null;
            URL url = (URL) systemResources.get(i);
            try
              {
                if (debug)
                  {
                    System.out.println("\t" + url.toString());
                  }
                urlIn = new InputStreamReader(url.openStream());
                parse(JAR, urlIn);
              }
            catch (IOException e)
              {
                if (debug)
                  {
                    System.out.println(e.getClass().getName() + ": " +
                                       e.getMessage());
                  }
              }
            finally
              {
                if (urlIn != null)
                  {
                    try
                      {
                        urlIn.close();
                      }
                    catch (IOException e)
                      {
                      }
                  }
              }
          }
      }
    else
      {
        parseResource(JAR, "/META-INF/mailcap");
      }
    
    if (debug)
      {
        System.out.println("MailcapCommandMap: load DEF");
      }
    parseResource(DEF, "/META-INF/mailcap.default");
  }
  
  /**
   * Returns the list of preferred commands for a given MIME type.
   * @param mimeType the MIME type
   */
  public synchronized CommandInfo[] getPreferredCommands(String mimeType)
  {
    List cmdList = new ArrayList();
    List verbList = new ArrayList();
    for (int i = 0; i < 2; i++)
      {
        for (int j = 0; j < 5; j++)
          {
            Map map = getCommands(mailcaps[j][i], mimeType);
            if (map != null)
              {
                for (Iterator k = map.entrySet().iterator(); k.hasNext(); )
                  {
                    Map.Entry entry = (Map.Entry) k.next();
                    String verb = (String) entry.getKey();
                    if (!verbList.contains(verb))
                      {
                        List classNames = (List) entry.getValue();
                        String className = (String) classNames.get(0);
                        CommandInfo cmd = new CommandInfo(verb, className);
                        cmdList.add(cmd);
                        verbList.add(verb);
                      }
                  }
              }
          }
      }
    CommandInfo[] cmds = new CommandInfo[cmdList.size()];
    cmdList.toArray(cmds);
    return cmds;
  }
  
  /**
   * Returns all commands for the given MIME type.
   * @param mimeType the MIME type
   */
  public synchronized CommandInfo[] getAllCommands(String mimeType)
  {
    List cmdList = new ArrayList();
    for (int i = 0; i < 2; i++)
      {
        for (int j = 0; j < 5; j++)
          {
            Map map = getCommands(mailcaps[j][i], mimeType);
            if (map != null)
              {
                for (Iterator k = map.entrySet().iterator(); k.hasNext(); )
                  {
                    Map.Entry entry = (Map.Entry) k.next();
                    String verb = (String) entry.getKey();
                    List classNames = (List) entry.getValue();
                    int len = classNames.size();
                    for (int l = 0; l < len; l++)
                      {
                        String className = (String) classNames.get(l);
                        CommandInfo cmd = new CommandInfo(verb, className);
                        cmdList.add(cmd);
                      }
                  }
              }
          }
      }
    CommandInfo[] cmds = new CommandInfo[cmdList.size()];
    cmdList.toArray(cmds);
    return cmds;
  }

  /**
   * Returns the command with the specified name for the given MIME type.
   * @param mimeType the MIME type
   * @param cmdName the command verb
   */
  public synchronized CommandInfo getCommand(String mimeType,
                                             String cmdName)
  {
    for (int i = 0; i < 2; i++)
      {
        for (int j = 0; j < 5; j++)
          {
            Map map = getCommands(mailcaps[j][i], mimeType);
            if (map != null)
              {
                List classNames = (List) map.get(cmdName);
                if (classNames == null)
                  {
                    classNames = (List) map.get("x-java-" + cmdName);
                  }
                if (classNames != null)
                  {
                    String className = (String) classNames.get(0);
                    return new CommandInfo(cmdName, className);
                  }
              }
          }
      }
    return null;
  }

  /**
   * Adds entries programmatically to the registry.
   * @param mail_cap a mailcap string
   */
  public synchronized void addMailcap(String mail_cap)
  {
    if (debug)
      {
        System.out.println("MailcapCommandMap: add to PROG");
      }
    try
      {
        parse(PROG, new StringReader(mail_cap));
      }
    catch (IOException e)
      {
      }
  }

  /**
   * Returns the DCH for the specified MIME type.
   * @param mimeType the MIME type
   */
  public synchronized DataContentHandler
    createDataContentHandler(String mimeType)
  {
    if (debug)
      {
        System.out.println("MailcapCommandMap: " +
                           "createDataContentHandler for " + mimeType);
      }
    for (int i = 0; i < 2; i++)
      {
        for (int j = 0; j < 5; j++)
          {
            if (debug)
              {
                System.out.println("  search DB #" + i);
              }
            Map map = getCommands(mailcaps[j][i], mimeType);
            if (map != null)
              {
                List classNames = (List) map.get("content-handler");
                if (classNames == null)
                  {
                    classNames = (List) map.get("x-java-content-handler");
                  }
                if (classNames != null)
                  {
                    String className = (String) classNames.get(0);
                    if (debug)
                      {
                        System.out.println("  In " + nameOf(j) +
                                           ", content-handler=" + className);
                      }
                    try
                      {
                        Class clazz = Class.forName(className);
                        return (DataContentHandler)clazz.newInstance();
                      }
                    catch (IllegalAccessException e)
                      {
                        if (debug)
                          {
                            e.printStackTrace();
                          }
                      }
                    catch (ClassNotFoundException e)
                      {
                        if (debug)
                      {
                        e.printStackTrace();
                      }
                      }
                    catch (InstantiationException e)
                      {
                        if (debug)
                          {
                            e.printStackTrace();
                          }
                      }
                  }
              }
          }
      }
    return null;
  }
    
  /**
   * Get the native commands for the given MIME type.
   * Returns an array of strings where each string is
   * an entire mailcap file entry.  The application
   * will need to parse the entry to extract the actual
   * command as well as any attributes it needs. See
   * <a href="http://www.ietf.org/rfc/rfc1524.txt">RFC 1524</a>
   * for details of the mailcap entry syntax.  Only mailcap
   * entries that specify a view command for the specified
   * MIME type are returned.
   * @return array of native command entries
   * @since JAF 1.1
   */
  public String[] getNativeCommands(String mimeType)
  {
    List acc = new ArrayList();
    for (int i = 0; i < 2; i++)
      {
        for (int j = 0; j < 5; j++)
          {
            addNativeCommands(acc, mailcaps[j][i], mimeType);
          }
      }
    String[] ret = new String[acc.size()];
    acc.toArray(ret);
    return ret;
  }

  private void addNativeCommands(List acc, Map mailcap, String mimeType)
  {
    for (Iterator j = mailcap.entrySet().iterator(); j.hasNext(); )
      {
        Map.Entry m_entry = (Map.Entry) j.next();
        String entryMimeType = (String) m_entry.getKey();
        if (!entryMimeType.equals(mimeType))
          {
            continue;
          }
        Map commands = (Map) m_entry.getValue();
        String viewCommand = (String) commands.get("view-command");
        if (viewCommand == null)
          {
            continue;
          }
        StringBuffer buf = new StringBuffer();
        buf.append(mimeType);
        buf.append(';');
        buf.append(' ');
        buf.append(viewCommand);
        for (Iterator k = commands.entrySet().iterator(); k.hasNext(); )
          {
            Map.Entry c_entry = (Map.Entry) k.next();
            String verb = (String) c_entry.getKey();
            List classNames = (List) c_entry.getValue();
            if (!"view-command".equals(verb))
              {
                for (Iterator l = classNames.iterator(); l.hasNext(); )
                  {
                    String command = (String) l.next();
                    buf.append(';');
                    buf.append(' ');
                    buf.append(verb);
                    buf.append('=');
                    buf.append(command);
                  }
              }
          }
        if (buf.length() > 0)
          {
            acc.add(buf.toString());
          }
      }
  }
  
  private static String nameOf(int mailcap)
  {
    switch (mailcap)
      {
      case PROG:
        return "PROG";
      case HOME:
        return "HOME";
      case SYS:
        return "SYS";
      case JAR:
        return "JAR";
      case DEF:
        return "DEF";
      default:
        return "ERR";
      }   
  }

  private void parseFile(int index, String filename)
  {
    Reader in = null;
    try
      {
        if (debug)
          {
            System.out.println("\t" + filename);
          }
        in = new FileReader(filename);
        parse(index, in);
      }
    catch (IOException e)
      {
        if (debug)
          {
            System.out.println(e.getClass().getName() + ": " +
                               e.getMessage());
          }
      }
    finally
      {
        if (in != null)
          {
            try
              {
                in.close();
              }
            catch (IOException e)
              {
              }
          }
      }
  }
  
  private void parseResource(int index, String name)
  {
    Reader in = null;
    try
      {
        InputStream is = getClass().getResourceAsStream(name);
        if (is != null)
          {
            if (debug)
              {
                System.out.println("\t" + name);
              }
            in = new InputStreamReader(is);
            parse(index, in);
          }
      }
    catch (IOException e)
      {
        if (debug)
          {
            System.out.println(e.getClass().getName() + ": " +
                               e.getMessage());
          }
      }
    finally
      {
        if (in != null)
          {
            try
              {
                in.close();
              }
            catch (IOException e)
              {
              }
          }
      }
  }
  
  private void parse(int index, Reader in)
    throws IOException
  {
    BufferedReader br = new BufferedReader(in);
    StringBuffer buf = null;
    for (String line = br.readLine(); line != null; line = br.readLine())
      {
        line = line.trim();
        int len = line.length();
        if (len == 0 || line.charAt(0) == '#')
          {
            continue; // Comment
          }
        if (line.charAt(len - 1) == '\\')
          {
            if (buf == null)
              {
                buf = new StringBuffer();
              }
            buf.append(line.substring(0, len - 1));
          }
        else if (buf != null)
          {
            buf.append(line);
            parseEntry(index, buf.toString());
            buf = null;
          }
        else
          {
            parseEntry(index, line);
          }
      }
  }
  
  private void parseEntry(int index, String line)
  {
    // Tokenize entry into fields
    char[] chars = line.toCharArray();
    int len = chars.length;
    boolean inQuotedString = false;
    boolean fallback = false;
    StringBuffer buffer = new StringBuffer();
    List fields = new ArrayList();
    for (int i = 0; i < len; i++)
      {
        char c = chars[i];
        if (c == '\\')
          {
            c = chars[++i]; // qchar
          }
        if (c == ';' && !inQuotedString)
          {
            String field = buffer.toString().trim();
            if ("x-java-fallback-entry".equals(field))
              {
                fallback = true;
              }
            fields.add(field);
            buffer.setLength(0);
          }
        else
          {
            if (c == '"')
              {
                inQuotedString = !inQuotedString;
              }
            buffer.append(c);
          }
      }
    String field = buffer.toString().trim();
    if ("x-java-fallback-entry".equals(field))
      {
        fallback = true;
      }
    fields.add(field);
    
    len = fields.size();
    if (len < 2)
      {
        if (debug)
          {
            System.err.println("Invalid mailcap entry: " + line);
          }
        return;
      }
    
    Map mailcap = fallback ? mailcaps[index][FALLBACK] :
      mailcaps[index][NORMAL];
    String mimeType = (String) fields.get(0);
    addField(mailcap, mimeType, "view-command", (String) fields.get(1));
    for (int i = 2; i < len; i++)
      {
        addField(mailcap, mimeType, null, (String) fields.get(i));
      }
  }
    
  private void addField(Map mailcap, String mimeType, String verb,
                        String command)
  {
    if (verb == null)
      {
        int ei = command.indexOf('=');
        if (ei != -1)
          {
            verb = command.substring(0, ei);
            command = command.substring(ei + 1);
          }
      }
    if (command.length() == 0 || verb == null || verb.length() == 0)
      {
        return; // Invalid field or flag
      }
      
    Map commands = (Map) mailcap.get(mimeType);
    if (commands == null)
      {
        commands = new LinkedHashMap();
        mailcap.put(mimeType, commands);
      }
    List classNames = (List) commands.get(verb);
    if (classNames == null)
      {
        classNames = new ArrayList();
        commands.put(verb, classNames);
      }
    classNames.add(command);
  }
  
  private Map getCommands(Map mailcap, String mimeType)
  {
    int si = mimeType.indexOf('/');
    String genericMimeType = new StringBuffer(mimeType.substring(0, si))
      .append('/')
      .append('*')
      .toString();
    Map specific = (Map) mailcap.get(mimeType);
    Map generic = (Map) mailcap.get(genericMimeType);
    if (generic == null)
      {
        return specific;
      }
    if (specific == null)
      {
        return generic;
      }
    Map combined = new LinkedHashMap();
    combined.putAll(specific);
    for (Iterator i = generic.keySet().iterator(); i.hasNext(); )
      {
        String verb = (String) i.next();
        List genericClassNames = (List) generic.get(verb);
        List classNames = (List) combined.get(verb);
        if (classNames == null)
          {
            combined.put(verb, genericClassNames);
          }
        else
          {
            classNames.addAll(genericClassNames);
          }
      }
    return combined;
  }

  // -- Utility methods --
  
  private List getSystemResources(String name)
  {
    List acc = new ArrayList();
    try
      {
        for (Enumeration i = ClassLoader.getSystemResources(name);
             i.hasMoreElements(); )
          {
            acc.add(i.nextElement());
          }
      }
    catch (IOException e)
      {
      }
    return acc;
  }
  
}

