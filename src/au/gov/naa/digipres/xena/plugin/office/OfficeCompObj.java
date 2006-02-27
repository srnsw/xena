/*
 * Created on 27/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


/**
 * This class handles reading the data found in the CompObj block
 * of an MPP file. The bits we can decypher allow us to determine
 * the file format, and a method is provided to determine if the
 * file is handled by Office or not.
 * 
 * This class is mostly cut and pasted from Tapster Rock's source files.
 * Needed to do this because most of their classes have only been given
 * package access... *sigh*
 */
public class OfficeCompObj
{
   /**
    * Constructor. Reads and processes the block data.
    *
    * @param is input stream
    * @throws IOException on read failure
    */
   public OfficeCompObj (InputStream is) throws IOException
   {
      int length;

      is.skip(28);

      length = readInt(is);
      m_applicationName = new String (readByteArray(is, length), 0, length-1);

      if (m_applicationName != null && m_applicationName.equals("Microsoft Project 4.0") == true)
      {
         m_fileFormat = "MSProject.MPP4";
         m_applicationID = "MSProject.Project.4";
      }
      else
      {
         length = readInt(is);
         m_fileFormat = new String (readByteArray(is, length), 0, length-1);
         length = readInt(is);
         m_applicationID = new String (readByteArray(is, length), 0, length-1);
      }            
   }
   
   /**
    * Returns true if this file is handled by OpenOffice.
    * At the moment it only returns false if it's a Project file.
    * @return
    */
   public boolean isOfficeFile()
   {
	   // TODO: Get a definitive list of all the file formats that OO can handle.
	   // Given the fantastic documentation that exists for both Microsoft's
	   // document format and for OO, this will obviously be a trivial job. 
       String format = getFileFormat();
       if (format.equals("MSProject.MPP9") ||
    	   format.equals("MSProject.MPP8"))
       {
    	   return false;
       }
       else
       {
    	   return true;
       }
   }

   /**
    * Accessor method to retrieve the application name.
    *
    * @return Name of the application
    */
   public String getApplicationName ()
   {
      return (m_applicationName);
   }

   /**
    * Accessor method to retrieve the application ID.
    *
    * @return Application ID
    */
   public String getApplicationID ()
   {
      return (m_applicationID);
   }

   /**
    * Accessor method to retrieve the file format.
    *
    * @return File format
    */
   public String getFileFormat ()
   {
      return (m_fileFormat);
   }

   /**
    * Application name.
    */
   private String m_applicationName;

   /**
    * Application identifier.
    */
   private String m_applicationID;

   /**
    * File format.
    */
   private String m_fileFormat;
   
   /**
    * This method reads a single byte from the input stream
    *
    * @param is the input stream
    * @return byte value
    * @throws IOException on file read error or EOF
    */
   protected int readByte (InputStream is)
      throws IOException
   {
      byte[] data = new byte[1];
      if (is.read(data) != data.length)
      {
         throw new EOFException ();
      }

      return getByte(data, 0);
   }

   /**
    * This method reads a two byte integer from the input stream
    *
    * @param is the input stream
    * @return integer value
    * @throws IOException on file read error or EOF
    */
   protected int readShort (InputStream is)
      throws IOException
   {
      byte[] data = new byte[2];
      if (is.read(data) != data.length)
      {
         throw new EOFException ();
      }

      return getShort(data, 0);
   }

   /**
    * This method reads a four byte integer from the input stream
    *
    * @param is the input stream
    * @return byte value
    * @throws IOException on file read error or EOF
    */
   protected int readInt (InputStream is)
      throws IOException
   {
      byte[] data = new byte[4];
      if (is.read(data) != data.length)
      {
         throw new EOFException ();
      }

      return getInt(data, 0);
   }

   /**
    * This method reads a byte array from the input stream
    *
    * @param is the input stream
    * @param size number of bytes to read
    * @return byte array
    * @throws IOException on file read error or EOF
    */
   protected byte[] readByteArray (InputStream is, int size)
      throws IOException
   {
      byte[] buffer = new byte[size];
      if (is.read(buffer) != buffer.length)
      {
         throw new EOFException ();
      }
      return (buffer);
   }

   /**
    * This method reads a single byte from the input array
    *
    * @param data byte array of data
    * @param offset offset of byte data in the array
    * @return byte value
    */
   public static final int getByte (byte[] data, int offset)
   {
      int result = data[offset] & 0x0F;
      result += (((data[offset] >> 4) & 0x0F) * 16);
      return (result);
   }
   
   /**
    * This method reads a four byte integer from the input array.
    *
    * @param data the input array
    * @param offset offset of integer data in the array
    * @return integer value
    */
   public static final int getInt (byte[] data, int offset)
   {
      int result = (data[offset] & 0x0F);
      result += (((data[offset] >> 4) & 0x0F) * 16);
      result += ((data[offset + 1] & 0x0F) * 256);
      result += (((data[offset + 1] >> 4) & 0x0F) * 4096);
      result += ((data[offset + 2] & 0x0F) * 65536);
      result += (((data[offset + 2] >> 4) & 0x0F) * 1048576);
      result += ((data[offset + 3] & 0x0F) * 16777216);
      result += (((data[offset + 3] >> 4) & 0x0F) * 268435456);
      return (result);
   }
   
   /**
    * This method reads a two byte integer from the input array.
    *
    * @param data the input array
    * @param offset offset of integer data in the array
    * @return integer value
    */
   public static final int getShort (byte[] data, int offset)
   {
      int result = (data[offset] & 0x0F);
      result += (((data[offset] >> 4) & 0x0F) * 16);
      result += ((data[offset + 1] & 0x0F) * 256);
      result += (((data[offset + 1] >> 4) & 0x0F) * 4096);
      return (result);
   }
   
}
