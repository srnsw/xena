/*
 * Created on 27/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.IOException;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;

public abstract class OfficeGuesser extends Guesser
{
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}
	
	public boolean isOfficeFile(XenaInputSource xis) throws IOException
	{
    	POIFSFileSystem fs = new POIFSFileSystem(xis.getByteStream());
        DirectoryEntry root = fs.getRoot ();

        //
        // Retrieve the CompObj data and validate the file format
        //
        OfficeCompObj compObj = 
        	new OfficeCompObj (new DocumentInputStream ((DocumentEntry)root.getEntry("\1CompObj")));
        
        return compObj.isOfficeFile();
	}
}
