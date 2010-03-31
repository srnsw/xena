// $Id$
package at.jta;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;

/********************************************************************************************************************************
 *
 * <p>Title: Class is 100% pur Java Registry handle (just can write/read string values)</p>
 *
 * <p>Description:  You can read, delete or create any key in the registry (when you have access rights).
 * But you just can read/delete and set string values! The java.dll doenst provide any other feature </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Taschek Joerg</p>
 *
 * @author Taschek Joerg
 * @version 2.0 22.03.2007 Methods are renamed and now called by the function they are implementing and the document is now in
 *              english, instead of german
 *******************************************************************************************************************************/
final public class Regor
{
  /**
   * the handle to the HKEY_CLASSES_ROOT registry root node
   */
  public static final int HKEY_CLASSES_ROOT = 0x80000000;
  /**
   * the handle to the HEKY_CURRENT_USER registry root node
   */
  public static final int HKEY_CURRENT_USER = 0x80000001;
  /**
   * the handle to the HKEY_LOCAL_MACHINE registry root node
   */
  public static final int HKEY_LOCAL_MACHINE = 0x80000002;


  public static final int ERROR_SUCCESS = 0;
  public static final int ERROR_FILE_NOT_FOUND = 2;
  public static final int ERROR_ACCESS_DENIED = 5;

  /* Constants used to interpret returns of native functions    */
  public static final int NATIVE_HANDLE = 0;
  public static final int ERROR_CODE = 1;
  public static final int SUBKEYS_NUMBER = 0;
  public static final int VALUES_NUMBER = 2;
  public static final int MAX_KEY_LENGTH = 3;
  public static final int MAX_VALUE_NAME_LENGTH = 4;


  /* Windows security masks */
  /**
   * Security Mask need by openKey - just for delete
   */
  public static final int DELETE = 0x10000;
  /**
   * Security Mask need by openKey - just for querying values
   */
  public static final int KEY_QUERY_VALUE = 1;
  /**
   * Security Mask need by openKey - just for setting values
   */
  public static final int KEY_SET_VALUE = 2;
  /**
   * Security Mask need by openKey - for creating sub keys
   */
  public static final int KEY_CREATE_SUB_KEY = 4;
  /**
   * Security Mask need by openKey - for enum sub keys
   */
  public static final int KEY_ENUMERATE_SUB_KEYS = 8;
  /**
   * Security Mask need by openKey - for key reading
   */
  public static final int KEY_READ = 0x20019;
  /**
   * Security Mask need by openKey - for writing keys
   */
  public static final int KEY_WRITE = 0x20006;
  /**
   * Security Mask need by openKey - highest access to do everything (default access by openkey without security mask)
   */
  public static final int KEY_ALL_ACCESS = 0xf003f;

  private Method openKey = null;
  private Method closeKey = null;
  private Method delKey = null;
  private Method createKey = null;
  private Method flushKey = null;
  private Method queryValue = null;
  private Method setValue = null;
  private Method delValue = null;
  private Method queryInfoKey = null;
  private Method enumKey = null;
  private Method enumValue = null;

  /******************************************************************************************************************************
   * Constructor to handle with windows registry
   * @throws RegistryErrorException throws an registryerrorException when its not able to get a handle to the registry methods
   * @throws NotSupportedOSException throws an notSupportedOSException if the registry is not used in windows
   *****************************************************************************************************************************/
  public Regor() throws RegistryErrorException
  {
    checkOS();
    initMethods();
  }

  /******************************************************************************************************************************
   * Method checks either if its windows or anohter system (if not windows an exception is thrown) - just needed for internal checks
   *****************************************************************************************************************************/
  private void checkOS()
  {
    String str = System.getProperty("os.name");
    if(str == null || str.toLowerCase().indexOf("windows") == -1)
      throw new NotSupportedOSException("Operating system: " + str + " is not supported!");
  }

  /******************************************************************************************************************************
   * Reading every valueName (not only the string value) out of the registry handle (for maximum value index and maxValueNameLength
   * use the getChildInformation method
   * @param key the handle to the parent key obtained from openKey
   * @param valueNameIndex the index of the valueName name - starting from 0 going to the maximum count from the getChildInformation
   * stored in array index 2
   * @param maxValueNameLength maximum length of valueName name (used because for memory allocating in the java.dll - if you obtain
   * the size from getChildInformation increase the [4] int array by 1)
   * @return byte[] either the name of the valueName or null if not found or an error occurs or if the maxValueNameLength is to short
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public byte[] enumValueName(int key, int valueNameIndex, int maxValueNameLength) throws RegistryErrorException
  {
    try
    {
      return (byte[])enumValue.invoke(null, new Object[] {new Integer(key), new Integer(valueNameIndex), new Integer(maxValueNameLength)});
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /*****************************************************************************************************************************
   * Returns every valueName (not only the String value names)
   * @param key either one of the root nodes or a key obtained from openKey
   * @param subkey a string to a subkey - if  the subkey is empty or null the information will be obtained from the given key
   * @return List on success and found a filled list with strings will be returned - on error or nothing found null will be returned
   * @throws RegistryErrorException
   ****************************************************************************************************************************/
  public List listValueNames(int key, String subkey) throws RegistryErrorException
  {
    int handle = -1;
    try{
      handle = openKey(key, subkey, KEY_READ); //just reading priv
      if(handle != -1)
      {
        int info[] = getChildInformation(handle); //obtain the informations
        if(info != null && info[0] != -1)
        {
          List ret = new ArrayList();
          for(int x = 0; x != info[2]; x++)
          {
            String tmp = parseValue(enumValueName(handle,x,info[4] + 1));
            if(tmp != null) //just if not null, maybe there are no valueNames
              ret.add(tmp);
          }
          return ret.isEmpty() ? null : ret;
        }
      }
    }
    catch(RegistryErrorException ex)
    {
      throw ex;
    }
    catch(Exception ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    finally{
      closeKey(handle);
    }
    return null;
  }

  /*****************************************************************************************************************************
   * Returns every valueName (not only the String value names)
   * @param key either one of the root nodes or a key obtained from openKey
   * @return List on success and found a filled list with strings will be returned - on error or nothing found null will be returned
   * @throws RegistryErrorException
   ****************************************************************************************************************************/
  public List listValueNames(int key) throws RegistryErrorException
  {
    return listValueNames(key,null);
  }

  /******************************************************************************************************************************
   * Reading the subkey name out of the registry (to obtain the count and the maxKeyNameLength use <code>getChildInformation</code>
   * method
   * @param key the handle to the key obtained by openKey
   * @param subkeyIndex index from the subkey from which you want to obtain the name (start with 0 - the maximum count you get from
   * getChildInformation method in array [0])
   * @param maxKeyNameLength the maximum length of a subkey name (used because for memory allocating in the java.dll - if you obtain
   * the size from getChildInformation increase the [3] int array by 1 )
   * @return byte[] on error or not found or the maxKeyNameLength is to short it will returns null, on success the name of the subkey
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public byte[] enumKeys(int key, int subkeyIndex, int maxKeyNameLength) throws RegistryErrorException
  {
    try
    {
      return (byte[])enumKey.invoke(null, new Object[] {new Integer(key), new Integer(subkeyIndex), new Integer(maxKeyNameLength)});
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Returns all subkeys from the given key and subkey
   * @param key either one of the root nodes or a key obtained from openKey
   * @param subkey a string to a subkey - if  the subkey is empty or null the information will be obtained from the given key
   * @return List on success and found a filled list with strings will be returned - on error or nothing found null will be returned
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public List listKeys(int key, String subkey) throws RegistryErrorException
  {
    int handle = -1;
    try{
      handle = openKey(key, subkey, KEY_READ); //just reading priv
      if(handle != -1)
      {
        int info[] = getChildInformation(handle); //obtain the informations
        if(info != null && info[0] != -1)
        {
          List ret = new ArrayList();
          for(int x = 0; x != info[0]; x++)
          {
            String tmp = parseValue(enumKeys(handle,x,info[3] + 1));
            if(tmp != null) //just if not null, maybe there are no valueNames
              ret.add(tmp);
          }
          return ret.isEmpty() ? null : ret;
        }
      }
    }
    catch(RegistryErrorException ex)
    {
      throw ex;
    }
    catch(Exception ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    finally{
      closeKey(handle);
    }
    return null;
  }

  /*****************************************************************************************************************************
   * Returns all subkeys from the given key
   * @param key either one of the root nodes or a key obtained from openKey
   * @return List on success and found a filled list with strings will be returned - on error or nothing found null will be returned
   * @throws RegistryErrorException
   ****************************************************************************************************************************/
  public List listKeys(int key) throws RegistryErrorException
  {
    return listKeys(key,null);
  }


  /******************************************************************************************************************************
   * Reads information about the current opened key (use it when you want to enumKey or enumValueName to determine the maximum
   * key length and the count of keys)
   * @param key the key which you obtained from openKey
   * @return int[0] the count of the subkeys,[2] count of valuenames,
   * [3] the maximum length of a subkey! the maximum length of valuename is stored in[4]
   * (for other operations you should increase the [3] or [4] value by 1 because of the terminating \0 in C - because you handle
   * with the java.dll)
   * if nothing found or illegal key, the values are -1 of the array (at index 1 the value would be 6 the other -1)
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public int[] getChildInformation(int key) throws RegistryErrorException
  {
    try
    {
      return (int[])queryInfoKey.invoke(null, new Object[] {new Integer(key)});
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Method deletes the specified String value
   * @param key the key obtained by openKey
   * @param valueName name of String value you want to delete (if the string is empty or null the default entry will be
   * deleted)
   * @return int
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public int delValue(int key, String valueName) throws RegistryErrorException
  {
    try
    {
      Integer ret = (Integer)delValue.invoke(null, new Object[] {new Integer(key), getString(valueName)});
      if(ret != null)
        return ret.intValue();
      else
        return -1;
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Method set the specified string value
   * Methode setzt (oder erstellt) einen Wert auf eine Zeichenfolge
   * Will man den defaulteintrag ändern, so muss man valueName "" übergeben
   * @param key obtained by openKey
   * @param valueName the string value name in the registry you want to set
   * @param value the new value you want to set
   * @return on success, return is ERROR_SUCCESS if not -1 or sth else will be returned
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public int setValue(int key,String valueName, String value) throws RegistryErrorException
  {
    try
    {
      Integer ret = (Integer)setValue.invoke(null, new Object[] {new Integer(key), getString(valueName), getString(value)});
      if(ret != null)
        return ret.intValue();
      else
        return -1;
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Reads the value of an string value
   * @param key obtained from openKey
   * @param valueName the string value which you want to read (if you want to obtain the default entry the valueName should be
   * empty or NULL)
   * @return byte[] if found the data in the string value will be returned (to get a string use the class method parseValue(byte[]))
   * on error NULL will be returned
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public byte[] readValue(int key, String valueName) throws RegistryErrorException
  {
    try
    {
      byte ret[] = (byte[])queryValue.invoke(null, new Object[] {new Integer(key), getString(valueName)});
      return ret;
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Flush method - dont know what the method exactly does just implemented because i found it in the java sun source
   * @param key obtained the key from openKey
   * @return on success, ERROR_SUCESS will be returned! on error -1 or sth else
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public int flushKey(int key) throws RegistryErrorException
  {
    try
    {
      Integer ret = (Integer)flushKey.invoke(null, new Object[] {new Integer(key)});
      if(ret != null)
        return ret.intValue();
      else
        return -1;
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * deletes a key/subkey from the registry
   * @param key the parent key obtained by openKey
   * @param subkey the key name you want to delete
   * @return int ERROR_SUCCESS wenn erfolgreich
   * @throws RegistryErrorException if subkey is empty or null or any other exception occurs
   *****************************************************************************************************************************/
  public int delKey(int key, String subkey) throws RegistryErrorException
  {
    if(subkey == null || subkey.length() == 0)
      throw new RegistryErrorException("subkey cannot be null");
    try
    {
      Integer ret = (Integer)delKey.invoke(null, new Object[] {new Integer(key), getString(subkey)});
      if(ret != null)
        return ret.intValue();
      else
        return -1;
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Create new key/subkey in the registry with the specified name
   * Attentition: if the key is successfully returned, you should close and open the key again, because the obtained key
   * doesnt have a high access level (so maybe creating or deleting a key/value wouldn´t be successful)
   * @param key handle to parent key obtained from openKey
   * @param subkey name of the key/subkey you want to create
   * @return on success the handle to the new key will be returned, otherwhise it will be -1
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public int createKey(int key, String subkey) throws RegistryErrorException
  {
    try
    {
      int result[] = (int[])createKey.invoke(null, new Object[] {new Integer(key), getString(subkey)});
      if(result[ERROR_CODE] == ERROR_SUCCESS)
        return result[NATIVE_HANDLE];
      else
        return -1;
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }



  /*****************************************************************************************************************************
   * Close an obtained key for right usage
   * @param key the key handle
   * @return int on error it will be -1
   * @throws RegistryErrorException
   ****************************************************************************************************************************/
  public int closeKey(int key) throws RegistryErrorException
  {
    try
    {
      Integer ret = (Integer)closeKey.invoke(null, new Object[] {new Integer(key)});
      if(ret != null)
        return ret.intValue();
      else
        return -1;
    }
    catch (InvocationTargetException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalArgumentException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
    catch (IllegalAccessException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Opens a registry key
   * @param key one of the registry root nodes - either HKEY_CLASSES_ROOT, HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE
   * @param subkey the name of the key/subkey like SOFTWARE or HARDWARE - for subkeys use the \\ as delimiter f.e. : SOFTWARE\\MICROSOFT
   * if subkey name is "" or null it returns the handle to the root node
   * @param security_mask the security mask to handle with the opened key (see security mask doc at the begin for detailed information)
   * @return int on error -1 (when not found or not allowed) otherwhise the handle to the obtained key
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public int openKey(int key, String subkey, int security_mask) throws RegistryErrorException
  {
    try
    {
      int[] result = (int[])openKey.invoke(null, new Object[]{new Integer(key),getString(subkey),new Integer(security_mask)});
      if(result == null || result[ERROR_CODE] != ERROR_SUCCESS)
        return -1;
      else
        return result[NATIVE_HANDLE];
    }
    catch (InvocationTargetException ex1)
    {
      throw new RegistryErrorException(ex1.getMessage());
    }
    catch (IllegalArgumentException ex1)
    {
      throw new RegistryErrorException(ex1.getMessage());
    }
    catch (IllegalAccessException ex1)
    {
      throw new RegistryErrorException(ex1.getMessage());
    }
  }

  /******************************************************************************************************************************
   * Opens a registry key
   * @param key one of the registry root nodes - either HKEY_CLASSES_ROOT, HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE
   * @param subkey the name of the key/subkey like SOFTWARE or HARDWARE - for subkeys use the \\ as delimiter f.e. : SOFTWARE\\MICROSOFT
   * if subkey name is "" or null it returns the handle to the root node
   * @return int -1 if not found or not allowed (attention here this methods allways uses the KEY_ALL_ACCESS security mask)
   * on success the handle to key will be returned
   * @throws RegistryErrorException
   *****************************************************************************************************************************/
  public int openKey(int key, String subkey) throws RegistryErrorException
  {
    return openKey(key,subkey,KEY_ALL_ACCESS);
  }

  /******************************************************************************************************************************
   * Intern method which adds the trailing \0 for the handle with java.dll
   * @param str String
   * @return byte[]
   *****************************************************************************************************************************/
  private byte[] getString(String str)
  {
    if(str == null)
      str = "";
    return (str += "\0").getBytes();
  }

  /******************************************************************************************************************************
   * Method removes the trailing \0 which is returned from the java.dll (just if the last sign is a \0)
   * @param buf the byte[] buffer which every read method returns
   * @return String a parsed string without the trailing \0
   *****************************************************************************************************************************/
  public static String parseValue(byte buf[])
  {
    if(buf == null)
      return null;
    String ret = new String(buf);
    if(ret.charAt(ret.length() - 1) == '\0')
      return ret.substring(0,ret.length() - 1);
    return ret;
  }


  /******************************************************************************************************************************
   * intern method which obtain the methods via reflection from the java.util.prefs.WindowPreferences (tested with java 1.4, 1.5
   * and java 1.6)
   * @throws RegistryErrorException exception is thrown if any method is not found or if the class is not found
   *****************************************************************************************************************************/
  private void initMethods() throws RegistryErrorException
  {
    Class clazz = null;
    try
    {
      clazz = Class.forName("java.util.prefs.WindowsPreferences"); //da der Zugriff anders nicht erlaubt wird

      Method ms[] = clazz.getDeclaredMethods();
      if(ms == null)
        throw new RegistryErrorException("Cannot access java.util.prefs.WindowsPreferences class!");
      //geht die Methoden durch und speichert diese in den Variablen ab
      for(int x = 0; x != ms.length; x++)
      {
        if(ms[x] != null)
        {
          if(ms[x].getName().equals("WindowsRegOpenKey"))
          {
            openKey = ms[x];
            openKey.setAccessible(true); //set Access for private
          }
          else if(ms[x].getName().equals("WindowsRegCloseKey"))
          {
            closeKey = ms[x];
            closeKey.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegCreateKeyEx"))
          {
            createKey = ms[x];
            createKey.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegDeleteKey"))
          {
            delKey = ms[x];
            delKey.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegFlushKey"))
          {
            flushKey = ms[x];
            flushKey.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegQueryValueEx"))
          {
            queryValue = ms[x];
            queryValue.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegSetValueEx"))
          {
            setValue = ms[x];
            setValue.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegDeleteValue"))
          {
            delValue = ms[x];
            delValue.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegQueryInfoKey"))
          {
            queryInfoKey = ms[x];
            queryInfoKey.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegEnumKeyEx"))
          {
            enumKey = ms[x];
            enumKey.setAccessible(true);
          }
          else if(ms[x].getName().equals("WindowsRegEnumValue"))
          {
            enumValue = ms[x];
            enumValue.setAccessible(true);
          }
        }
      }
    }
    catch (ClassNotFoundException ex)
    {
      throw new RegistryErrorException(ex.getMessage());
    }
  }


  /******************************************************************************************************************************
   * main for testing and some examples are stored here
   * @param args String[]
   * @throws Exception
   *****************************************************************************************************************************/
  public static void main(String[] args) throws Exception
  {
    Regor regor = new Regor();
    //opening dhe LOCAL_MACHINE entry and software\microsoft - the delimiter is the \\
    int key = regor.openKey(HKEY_LOCAL_MACHINE,"Software\\Microsoft"), key2 = -1;
    //listing the subkeys
    List l = regor.listKeys(key);
    System.out.println("SOME KEYS....");
    for(int x = 0; l != null && x != l.size(); x++) //printing out the keys
      System.out.println(x + " == " + l.get(x));
    if(l.size() > 0) //if keys found, use first key to get valueNames
      key2 = regor.openKey(key,(String)l.get(0));
    l = regor.listValueNames(key2); //read the valueNames
    System.out.println("SOME VALUENAMES.....");
    for(int x = 0; l != null && x != l.size(); x++) //printing it
      System.out.println(x + " == " + l.get(x));
    System.out.println("SOME STRING VALUES....");
    for(int x = 0; l != null && x != l.size(); x++) //getting the String value from the valueNames
    {
      byte buf[] = regor.readValue(key2,(String)l.get(x)); //get the information - if is not a string value, null will be returned
      System.out.println(x + ": " + l.get(x) + " == " + Regor.parseValue(buf)); //parses the byte buffer to String
    }
    //example to access the default valueName - either null or ""
    System.out.println("default entry == " + Regor.parseValue(regor.readValue(key,null)));
    //accessing a root node
    l = regor.listKeys(HKEY_LOCAL_MACHINE);
    System.out.println("KEYS FROM LOCAL_MACHINE....");
    for(int x = 0; l != null && x != l.size(); x++) //printing out the keys
      System.out.println(x + " == " + l.get(x));
    regor.closeKey(key2);
    regor.closeKey(key);
  }
}
