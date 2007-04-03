/*
 * Created on 28/03/2007
 * justinw5
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

public class TarHandler implements ArchiveHandler
{
	private TarInputStream tarStream;
	
	public TarHandler(TarInputStream tarStream)
	{
		this.tarStream = tarStream;
	}
	
	public ArchiveEntry getNextEntry() throws IOException
	{
		boolean found = false;
		TarEntry tarEntry;
		do
		{
			tarEntry = tarStream.getNextEntry();
			if (tarEntry == null)
			{
				return null;
			}
			if (!tarEntry.isDirectory())
			{
				found = true;
			}
		}
		while (found == false);
		
		
		String entryName = tarEntry.getName();
		if (entryName.lastIndexOf("/") != -1)
		{
			entryName = entryName.substring(entryName.lastIndexOf("/")+1);
		}
		else if (entryName.indexOf("\\") != -1)
		{
			entryName = entryName.substring(entryName.lastIndexOf("\\")+1);
		}
		
		File entryTempFile = File.createTempFile("archive_entry", entryName);
		entryTempFile.deleteOnExit();
		FileOutputStream tempFileOS = new FileOutputStream(entryTempFile);
		
		// 10k buffer
		byte[] readBuff = new byte[10 * 1024];
		int bytesRead = tarStream.read(readBuff);
		while (bytesRead > 0)
		{
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
