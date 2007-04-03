/*
 * Created on 28/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.zip;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

import au.gov.naa.digipres.xena.plugin.archive.ArchiveHandler;
import au.gov.naa.digipres.xena.plugin.archive.ArchiveNormaliser;

/**
 * Normaliser for .zip and .jar files
 * 
 * @author justinw5
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class ZipNormaliser extends ArchiveNormaliser
{

	@Override
	protected ArchiveHandler getArchiveHandler(InputStream archiveStream)
	{
		ZipInputStream zipStream = new ZipInputStream(archiveStream);
		return new ZipHandler(zipStream);		
	}

	@Override
	public String getName()
	{
		return "Zip";
	}

}
