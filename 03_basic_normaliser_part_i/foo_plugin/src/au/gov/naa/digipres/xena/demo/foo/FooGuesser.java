
package au.gov.naa.digipres.xena.demo.foo;

import java.io.IOException;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;



public class FooGuesser extends Guesser {


	private static final byte[] fooMagic = {'~', 'b', 'e', 'g','i','n','F','o','o','~'};
	private static final String extension = "foo";
	private static final String ASCII = "US-ASCII";
	private static final String UTF8 = "UTF-8";   

	public Guess guess(XenaInputSource xis) throws XenaException, IOException {
		Guess guess = new Guess((FileType)PluginManager.singleton().getTypeManager().lookup(FooFileType.class));
		// first up - we check the characters.
		// we will only look at the first 64k - if we have gone that far and have had no bad chars, should be okay.
		String charset = CharsetDetector.mustGuessCharSet(xis.getByteStream(), 2 ^ 16);
		
		if (charset != null && ( charset.equals(UTF8) || charset.equals(ASCII) )) {
			guess.setDataMatch(true);
		} else {
			guess.setDataMatch(false);
			guess.setPossible(false);
			return guess;
		}
		
		// now check for our magic number, using the guesserutils compare byte array method...
		byte[] first = new byte[fooMagic.length];
        	xis.getByteStream().read(first);
        	if (GuesserUtils.compareByteArrays(first, fooMagic)) {
			guess.setMagicNumber(true);
        	} else {
            		guess.setMagicNumber(false);
            		guess.setPossible(false);
			return guess;
        	}
		
		//check the extension - if it doesnt match leave the extension match at it's default
		// value - 'unknown'.
        	String id = xis.getSystemId().toLowerCase();
        	if (id.endsWith("foo")) {
            		guess.setExtensionMatch(true);
        	}
		
		//and thats it! return our guess and rejoice!
		return guess;
	}
    
	public String getName() {
		return "FooGuesser";
	}

	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		guess.setExtensionMatch(true);
		return guess;
    	}
}