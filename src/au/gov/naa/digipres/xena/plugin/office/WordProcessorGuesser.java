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
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public class WordProcessorGuesser extends Guesser {

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
