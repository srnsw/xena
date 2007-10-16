/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 27/02/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.project;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class handles reading the data found in the CompObj block
 * of an MPP file. The bits we can decypher allow us to determine
 * the file format.
 */
public class ProjectCompObj {
	/**
	* Constructor. Reads and processes the block data.
	*
	* @param is input stream
	* @throws IOException on read failure
	*/
	public ProjectCompObj(InputStream is) throws IOException {
		int length;

		is.skip(28);

		length = readInt(is);
		m_applicationName = length > 0 ? new String(readByteArray(is, length), 0, length - 1) : "";

		if (m_applicationName != null && m_applicationName.equals("Microsoft Project 4.0") == true) {
			m_fileFormat = "MSProject.MPP4";
			m_applicationID = "MSProject.Project.4";
		} else {
			length = readInt(is);
			m_fileFormat = length > 0 ? new String(readByteArray(is, length), 0, length - 1) : "";
			length = readInt(is);
			m_applicationID = length > 0 ? new String(readByteArray(is, length), 0, length - 1) : "";
		}
	}

	public boolean isProjectFile() {
		String format = getFileFormat();
		if (format.equals("MSProject.MPP9") || format.equals("MSProject.MPP8")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Accessor method to retrieve the application name.
	 *
	 * @return Name of the application
	 */
	public String getApplicationName() {
		return (m_applicationName);
	}

	/**
	 * Accessor method to retrieve the application ID.
	 *
	 * @return Application ID
	 */
	public String getApplicationID() {
		return (m_applicationID);
	}

	/**
	 * Accessor method to retrieve the file format.
	 *
	 * @return File format
	 */
	public String getFileFormat() {
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
	protected int readByte(InputStream is) throws IOException {
		byte[] data = new byte[1];
		if (is.read(data) != data.length) {
			throw new EOFException();
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
	protected int readShort(InputStream is) throws IOException {
		byte[] data = new byte[2];
		if (is.read(data) != data.length) {
			throw new EOFException();
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
	protected int readInt(InputStream is) throws IOException {
		byte[] data = new byte[4];
		if (is.read(data) != data.length) {
			throw new EOFException();
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
	protected byte[] readByteArray(InputStream is, int size) throws IOException {
		byte[] buffer = new byte[size];
		if (is.read(buffer) != buffer.length) {
			throw new EOFException();
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
	public static final int getByte(byte[] data, int offset) {
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
	public static final int getInt(byte[] data, int offset) {
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
	public static final int getShort(byte[] data, int offset) {
		int result = (data[offset] & 0x0F);
		result += (((data[offset] >> 4) & 0x0F) * 16);
		result += ((data[offset + 1] & 0x0F) * 256);
		result += (((data[offset + 1] >> 4) & 0x0F) * 4096);
		return (result);
	}

}
