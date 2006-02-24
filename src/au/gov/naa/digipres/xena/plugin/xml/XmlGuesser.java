package au.gov.naa.digipres.xena.plugin.xml;
import java.io.BufferedReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser to guess a file of a XML of a random type.
 *
 * @author Chris Bitmead
 */
public class XmlGuesser extends Guesser {
	public Guess guess(XenaInputSource source) throws java.io.IOException, XenaException {
		BufferedReader rd = new BufferedReader(source.getCharacterStream());
		String line = rd.readLine();
		Guess guess = new Guess((FileType)TypeManager.singleton().lookup(XmlFileType.class));
		if (source.getSystemId().toLowerCase().endsWith(".xml")) {
			guess.setExtensionMatch(true);
		} else if (line != null && 0 <= line.indexOf("<?xml ")) {
			guess.setDataMatch(true);
		}
		return guess;
	}
    
    public String getName() {
        return "XMLGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setDataMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}
    
}
