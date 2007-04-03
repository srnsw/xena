/*
 * Created on 27/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive;

import java.io.IOException;

/**
 * Interface representing a class which Xena can use to retrieve entries from archives.
 * 
 * @author justinw5
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public interface ArchiveHandler
{

	public ArchiveEntry getNextEntry() throws IOException;

	
}
