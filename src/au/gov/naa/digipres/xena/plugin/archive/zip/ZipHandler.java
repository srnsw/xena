/*
 * Created on 28/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import au.gov.naa.digipres.xena.plugin.archive.ArchiveEntry;
import au.gov.naa.digipres.xena.plugin.archive.ArchiveHandler;

public class ZipHandler implements ArchiveHandler
{
	private ZipInputStream zipStream;
	
	public ZipHandler(ZipInputStream zipStream)
	{
		this.zipStream = zipStream;
	}
	
	public ArchiveEntry getNextEntry() throws IOException
	{
		boolean found = false;
		ZipEntry zipEntry;
		do
		{
			zipEntry = zipStream.getNextEntry();
			if (zipEntry == null)
			{
				return null;
			}
			if (!zipEntry.isDirectory())
			{
				found = true;
			}
		}
		while (found == false);
		
		
		String entryName = zipEntry.getName();
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
		int bytesRead = zipStream.read(readBuff);
		while (bytesRead > 0)
		{
			tempFileOS.write(readBuff, 0, bytesRead);
			bytesRead = zipStream.read(readBuff);
		}
		
		// Create ArchiveEntry object, using full path name of the entry
		ArchiveEntry archiveEntry = new ArchiveEntry(zipEntry.getName(), entryTempFile.getAbsolutePath());
		archiveEntry.setOriginalFileDate(new Date(zipEntry.getTime()));
		archiveEntry.setOriginalSize(zipEntry.getSize());
		return archiveEntry;
		
	}

}
