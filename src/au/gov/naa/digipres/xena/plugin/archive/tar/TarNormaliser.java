/*
 * Created on 28/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.tar;

import java.io.InputStream;

import au.gov.naa.digipres.xena.plugin.archive.ArchiveHandler;
import au.gov.naa.digipres.xena.plugin.archive.ArchiveNormaliser;

import com.ice.tar.TarInputStream;

/**
 * Normaliser for .zip and .jar files
 * 
 * @author justinw5
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class TarNormaliser extends ArchiveNormaliser
{

	@Override
	protected ArchiveHandler getArchiveHandler(InputStream archiveStream)
	{
		TarInputStream tarStream = new TarInputStream(archiveStream);
		return new TarHandler(tarStream);		
	}

	@Override
	public String getName()
	{
		return "Tar";
	}

}
