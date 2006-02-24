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

public class SpreadsheetGuesser extends Guesser {

    public String getName() {
        return "SpreadsheetGuesser";
    }
    
    public Guess guess(XenaInputSource source) throws XenaException, IOException {
        Guess guess = new Guess((FileType)TypeManager.singleton().lookup(SpreadsheetFileType.class));

        String type = source.getMimeType();

        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull().toLowerCase();

        if (OfficeMagicNumber.checkForOfficeMagicNumber(source)) {
            guess.setMagicNumber(true);
        }
        
         if (extension.equals("xls") || 
             extension.equals("xlt") || 
             extension.equals("sxc")) {
            guess.setExtensionMatch(true);
         }
         if (type != null && 
        	 (type.equals("application/ms-excel") || 
        	  type.equals("application/vnd.sun.xml.calc"))) {
             guess.setMimeMatch(true);
         }
         
        return guess;
        
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}
    
}
