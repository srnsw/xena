package au.gov.naa.digipres.xena.plugin.basic;
import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for the string file type.
 *
 * @author Chris Bitmead
 */
public class StringGuesser extends Guesser {
	
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess((FileType)TypeManager.singleton().lookup(StringFileType.class));
		
        guess.setPossible(true);
        guess.setPriority(GuessPriority.LOW);
        guess.setDataMatch(true);
        
        InputStream in = source.getByteStream();
		int c;
		while (0 <= (c = in.read())) {
			if (Character.isISOControl((char)c)) {
				if (!(c == '\r' || c == '\n')) {
					guess.setDataMatch(false);
					break;
				}
			}
		}
		return guess;
	}
    
    public String getName() {
        return "StringGuesser";
    }

	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess bestGuess = new Guess();
		bestGuess.setDataMatch(true);
		bestGuess.setPossible(true);
		bestGuess.setPriority(GuessPriority.LOW);
		return bestGuess;
	}

}
