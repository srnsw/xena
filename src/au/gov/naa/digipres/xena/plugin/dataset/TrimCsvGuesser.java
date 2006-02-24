package au.gov.naa.digipres.xena.plugin.dataset;
import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public class TrimCsvGuesser extends Guesser {
		
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
	    Guess guess = new Guess((FileType)TypeManager.singleton().lookup(TrimCsvFileType.class));
        String magic = "Tower Software - ASCII Dump";
		char[] ch = new char[magic.length()];
		int sz = source.getCharacterStream().read(ch);
		if (0 <= sz) {
			String str = new String(ch, 0, sz);
			if (str.equals(magic)) {
			    guess.setDataMatch(true);
                guess.setMagicNumber(true);
            } else {
                guess.setPossible(false);
            }
        }
        //TODO: probably should put in our file name check here too...
        
        
        guess.setPossible(false);
        
		return guess;
	}
    
    public String getName() {
        return "TrimCSVGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

}
