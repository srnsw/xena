/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public class PresentationGuesser extends Guesser {

    public String getName() {
        return "PresentationGuesser";
    }
    
    public Guess guess(XenaInputSource source) throws XenaException, IOException {
        Guess guess = new Guess((FileType)TypeManager.singleton().lookup(PresentationFileType.class));

        String type = source.getMimeType();
        
        if (type != null && 
        	(type.equals("application/ms-powerpoint") || 
        	 type.equals("application/vnd.sun.xml.impress"))) {
            guess.setMimeMatch(true);
        }

        // MAGIC NUMBER
        // -> do not set to false because it could be a word processor format
        // that the normaliser can handle but we dont know the magic number for it...
        
        if (OfficeMagicNumber.checkForOfficeMagicNumber(source) == true) {
            guess.setMagicNumber(true);
        }

        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull().toLowerCase();
        if (extension.equals("ppt") || extension.equals("pot") || extension.equals("pps") || extension.equals("sxi")) {
            guess.setExtensionMatch(true); 
        }
        return guess;
    }
        
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}
        
}
