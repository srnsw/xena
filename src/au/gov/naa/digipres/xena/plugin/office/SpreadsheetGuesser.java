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
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public class SpreadsheetGuesser extends OfficeGuesser {

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
         
         try
         {
         	if (isOfficeFile(source))
         	{
         		guess.setDataMatch(true);
         	}
         }
         catch (IOException ex)
         {
         	// Not a POIFS, but could still be a non-POIFS spreadsheet
         	// file of some sort, so do nothing
         }

         // If the office file does not have an extension, it will be given
         // the same rank by all the office guessers. So we'll rank them
         // in order of probability (ie docs are more common than xls etc) 
         // using priority. 
         // TODO: A better way of determining office file type needs to be found!
         guess.setPriority(GuessPriority.DEFAULT);

         return guess;
        
    }
        
}
