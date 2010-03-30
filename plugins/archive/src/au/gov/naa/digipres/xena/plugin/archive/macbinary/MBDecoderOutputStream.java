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
 * @author Matthew Oliver
 */
package au.gov.naa.digipres.xena.plugin.archive.macbinary;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class MBDecoderOutputStream extends FilterOutputStream {

	private int byteIndex;
	private int dataForkLength;
	private int fileNameLength;
	private String fileName;
	private String fileType;
	private int fileCreationDate;
	private int fileLastModifiedDate;
	
	public final int FILE_NAME_LENGTH_OFFSET = 1;
	public final int FILE_NAME_START_OFFSET = 2;
	public final int FILE_NAME_END_OFFSET = 64;
	public final int FILE_TYPE_START_OFFSET = 65;
	public final int FILE_TYPE_END_OFFSET = 68;
	public final int DATA_FORK_LENGTH_START_OFFSET = 83;
	public final int DATA_FORK_LENGTH_END_OFFSET = 86;
	public final int FILE_CREATED_DATE_START_OFFSET = 91;
	public final int FILE_CREATED_DATE_END_OFFSET = 94;
	public final int FILE_LAST_MODIFIED_DATE_START_OFFSET = 95;
	public final int FILE_LAST_MODIFIED_DATE_END_OFFSET = 98;
	public final int DATA_FORK_START = 128;
	
	public MBDecoderOutputStream(OutputStream arg0) {
		super(arg0);
		
		byteIndex = 0;
		dataForkLength = 0;
		fileNameLength = 0;
		fileName = "";
		fileType = "";
		fileCreationDate = 0;
		fileLastModifiedDate = 0;
	}
	
	@Override
	public void write(int currentByte) throws IOException {
		//First grab the folder name length
		if (byteIndex == FILE_NAME_LENGTH_OFFSET) {
			fileNameLength = (currentByte & 0xff);
		}
		
		//If we are in the Folder Name offset range then append the current Char (byte) to the folder name variable.
		else if (byteIndex <= FILE_NAME_END_OFFSET && byteIndex >= FILE_NAME_START_OFFSET) {
			if (fileNameLength > 0 && byteIndex <= FILE_NAME_START_OFFSET + fileNameLength) {
				fileName += (char)(currentByte & 0xff);
			}
		}
		
		//If we are in the Folder type offset range then append the current Char (byte) to the folder type variable.
		else if (byteIndex <= FILE_TYPE_END_OFFSET && byteIndex >= FILE_TYPE_END_OFFSET) {
			fileType += (char)(currentByte & 0xff);
		}
		
		//If we are in the data fork length offset range then do some left bit shifting to grab int version of the 32 bit word.
		else if (byteIndex <= DATA_FORK_LENGTH_END_OFFSET && byteIndex >= DATA_FORK_LENGTH_START_OFFSET) {
			//Get the number of bytes in to the range and convert to the number of bits we will need to shift by.
			int shift = (DATA_FORK_LENGTH_END_OFFSET - byteIndex) * 8;
			
			//Cause be using binary manipulation we need to use OR (|) and the shifted value to stitch the word together.
			dataForkLength = dataForkLength | (currentByte & 0xff) << shift;
		}
		
		//Grab the files creation date..
		else if (byteIndex <= FILE_CREATED_DATE_END_OFFSET && byteIndex >= FILE_CREATED_DATE_START_OFFSET) {
			//Get the number of bytes in to the range and convert to the number of bits we will need to shift by.
			int shift = (FILE_CREATED_DATE_END_OFFSET - byteIndex) * 8;
			
			//Cause be using binary manipulation we need to use OR (|) and the shifted value to stitch the word together.
			fileCreationDate = fileCreationDate | (currentByte & 0xff) << shift;
		}
		
		//Grab the files last modified date..
		else if (byteIndex <= FILE_LAST_MODIFIED_DATE_END_OFFSET && byteIndex >= FILE_LAST_MODIFIED_DATE_START_OFFSET) {
			//Get the number of bytes in to the range and convert to the number of bits we will need to shift by.
			int shift = (FILE_LAST_MODIFIED_DATE_END_OFFSET - byteIndex) * 8;
			
			//Cause be using binary manipulation we need to use OR (|) and the shifted value to stitch the word together.
			fileLastModifiedDate = fileLastModifiedDate | (currentByte & 0xff) << shift;
		}
		
		//Now that the header has been passed if we lay inside the data fork range then simply write that data out as it is
		//the actual file we want.
		else if (byteIndex < (DATA_FORK_START + dataForkLength) && byteIndex >= DATA_FORK_START) {
			out.write(currentByte);
		}
		
		byteIndex++;
	}
	
	private long macToJavaTime(int macSeconds) {
		
	    // We need to calculate the number of milliseconds between the Mac epoch and Java epoch.
	    // Mac Epoch starts at 01 Jan 1904, and Java at 01 Jan 1970. 
		long macJavaEpochDifferenceInDays = 24107;
		long hours = 24;
		long minutes = 60;
		long seconds = 60;
		long millis = 1000;
	    long macDeltaInMillis = macJavaEpochDifferenceInDays * hours * minutes * seconds * millis;
	    
	    // Create a Calendar for date calcs
	    Calendar cal = Calendar.getInstance();
		
        long javaTimeInMillis = (macSeconds & 0x0FFFFFFFFL) * millis - macDeltaInMillis - cal.getTimeZone().getRawOffset();

        synchronized ( cal )
        {
            cal.setTimeInMillis(javaTimeInMillis);

            // To go from local time to GMT, subtract the DST offset.
            javaTimeInMillis -= cal.get( Calendar.DST_OFFSET );
        }
    
        return javaTimeInMillis;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// If the write is for content past the end of the data fork, ignore
		if (byteIndex >= (128 + dataForkLength)) {
			byteIndex+= len;
		}
		// If the write is entirely within the data fork, write it directly
		else if (byteIndex >= 128
				&& (byteIndex + len) <= (128 + dataForkLength)) {
			out.write(b, off, len);
			byteIndex += len;
		}
		// Otherwise, do the write a byte at a time to get the logic above
		else {
			for (int i = 0; i < len; i++) {
				write(b[off + i]);
			}
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public long getCreationDate() {
    	return macToJavaTime(fileCreationDate);
    }

	public long getLastModifiedDate() {
    	return macToJavaTime(fileLastModifiedDate);
    }
	
}