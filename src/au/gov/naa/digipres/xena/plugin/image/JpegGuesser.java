/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class JpegGuesser extends Guesser {

    private static byte[] jpegmagic = {
        new Integer( -1).byteValue(),
        new Integer( -40).byteValue(),
        new Integer( -1).byteValue()
        };
    

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
    }
    
    public Guess guess(XenaInputSource source) throws XenaException, IOException {
        //Guess guess = new Guess((FileType)TypeManager.singleton().lookup(JpegFileType.class));

        Guess guess = new Guess(new JpegFileType());
        
        String type = source.getMimeType();
        byte[] first = new byte[4];
        source.getByteStream().read(first);
        String id = source.getSystemId().toLowerCase();
        

        if (type.equals("image/jpeg")) {
            guess.setMimeMatch(true);
        }
        if (id.endsWith(".jpg") || id.endsWith(".jpeg")) {
            guess.setExtensionMatch(true);
        }
        if (GuesserUtils.compareByteArrays(first, jpegmagic)) {
            guess.setMagicNumber(true);
            
            // TODO: A better way of checking for Data Match
            guess.setDataMatch(true);
        } else {
            guess.setMagicNumber(false);
            guess.setPossible(false);
        }
        
        return guess;
    }
    
    
    public String getName() {
        return "JpegGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	public Type getType()
	{
		return new JpegFileType();
	}

}
