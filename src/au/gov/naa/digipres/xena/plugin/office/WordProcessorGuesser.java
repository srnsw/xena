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
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public class WordProcessorGuesser extends OfficeGuesser {

    private static byte[] rtfMagic = { 0x7B, 0x5c, 0x72, 0x74, 0x66, 0x31 };
    
    private static byte[] wriMagic = { 0x31, (byte)0xBE, 0x00, 0x00, 0x00, (byte)0xAB, 0x00, 0x00};
    
    public String getName() {
        return "WordGuesser";
    }

    public Guess guess(XenaInputSource xis) throws XenaException, IOException {

        Guess guess = new Guess((FileType)TypeManager.singleton().lookup(WordProcessorFileType.class));

        String type = xis.getMimeType();
        
        if (type != null &&
        	(type.equals("application/msword") || 
        	 type.equals("application/rtf") || 
             type.equals("text/rtf") || 
             type.equals("application/vnd.sun.xml.writer"))) 
        {
        	guess.setMimeMatch(true);
        }
        
        // MAGIC NUMBER
        // -> do not set to false because it could be a word processor format
        // that the normaliser can handle but we dont know the magic number for it...
        
        if (OfficeMagicNumber.checkForOfficeMagicNumber(xis) == true) {
            guess.setMagicNumber(true);
        }
        
        byte header[] = new byte[rtfMagic.length];
        xis.getByteStream().read(header);
        if (GuesserUtils.compareByteArrays(header, rtfMagic)) {
            guess.setMagicNumber(true);
        }

        header = new byte[wriMagic.length];
        if (GuesserUtils.compareByteArrays(header, wriMagic)) {
            guess.setMagicNumber(true);
        }
        
        // extension...
        FileName name = new FileName(xis.getSystemId());
        String extension = name.extenstionNotNull().toLowerCase();
        if (extension.equals("doc") || extension.equals("dot") || extension.equals("sxw") || extension.equals("rtf")) {
            guess.setExtensionMatch(true);
        }
        
        try
        {
        	if (isOfficeFile(xis))
        	{
        		guess.setDataMatch(true);
        	}
        }
        catch (IOException ex)
        {
        	// Not a POIFS, but could still be a non-POIFS word processor
        	// file of some sort, so do nothing
        }
        
        // If the office file does not have an extension, it will be given
        // the same rank by all the office guessers. So we'll rank them
        // in order of probability (ie docs are more common than xls etc) 
        // using priority. 
        // TODO: A better way of determining office file type needs to be found!
        guess.setPriority(GuessPriority.HIGH);
        
        return guess;
    }

    
}
