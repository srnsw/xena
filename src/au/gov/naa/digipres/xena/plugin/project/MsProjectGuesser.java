package au.gov.naa.digipres.xena.plugin.project;
import java.io.IOException;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for MS Project files.
 *
 * @author Chris Bitmead
 */
public class MsProjectGuesser extends Guesser {
	
	private static final byte[] mppMagic =
		{(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0, (byte)0xA1, (byte)0xB1, 
		 0x1A, (byte)0xE1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
		 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x03, 
		 0x00, (byte)0xFE, (byte)0xFF, 0x09, 0x00, 0x06, 0x00, 0x00, 0x00, 
		 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
        Guess guess = new Guess((FileType)TypeManager.singleton().lookup(MsProjectFileType.class));
        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull();
        if (extension.equals("mpp")) {
            guess.setExtensionMatch(true);            
        }

        byte[] first = new byte[44];
        source.getByteStream().read(first);
        
        if (GuesserUtils.compareByteArrays(first, mppMagic)) 
        {
            guess.setMagicNumber(true);
        } 
        else 
        {
            guess.setMagicNumber(false);
            guess.setPossible(false);
        }
        
        POIFSFileSystem fs = null;
        try {
            fs = new POIFSFileSystem(source.getByteStream());
            guess.setDataMatch(true);
        } catch (IOException x) {
            // an I/O error occurred, or the InputStream did not provide a compatible
            // POIFS data structure
            guess.setPossible(false);
        }
        
        return guess;
    }
    
    public String getName() {
        return "ProjectGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setDataMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}
    
}
