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
 * Created on 28/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.tar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import au.gov.naa.digipres.xena.plugin.archive.ArchiveEntry;
import au.gov.naa.digipres.xena.plugin.archive.ArchiveHandler;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

public class TarHandler implements ArchiveHandler {
	private TarInputStream tarStream;

	public TarHandler(TarInputStream tarStream) {
		this.tarStream = tarStream;
	}

	public ArchiveEntry getNextEntry() throws IOException {
		boolean found = false;
		TarEntry tarEntry;
		do {
			tarEntry = tarStream.getNextEntry();
			if (tarEntry == null) {
				return null;
			}
			if (!tarEntry.isDirectory()) {
				found = true;
			}
		} while (found == false);

		String entryName = tarEntry.getName();
		if (entryName.lastIndexOf("/") != -1) {
			entryName = entryName.substring(entryName.lastIndexOf("/") + 1);
		} else if (entryName.indexOf("\\") != -1) {
			entryName = entryName.substring(entryName.lastIndexOf("\\") + 1);
		}

		File entryTempFile = File.createTempFile("archive_entry", entryName);
		entryTempFile.deleteOnExit();
		FileOutputStream tempFileOS = new FileOutputStream(entryTempFile);

		// 10k buffer
		byte[] readBuff = new byte[10 * 1024];
		int bytesRead = tarStream.read(readBuff);
		while (bytesRead > 0) {
			tempFileOS.write(readBuff, 0, bytesRead);
			bytesRead = tarStream.read(readBuff);
		}

		// Create ArchiveEntry object, using full path name of the entry
		ArchiveEntry archiveEntry = new ArchiveEntry(tarEntry.getName(), entryTempFile.getAbsolutePath());
		archiveEntry.setOriginalFileDate(tarEntry.getModTime());
		archiveEntry.setOriginalSize(tarEntry.getSize());
		return archiveEntry;

	}

}
