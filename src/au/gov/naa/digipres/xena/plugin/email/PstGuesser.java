package au.gov.naa.digipres.xena.plugin.email;
import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser to guess the Microsoft PST email  file format.
 *
 * @author Chris Bitmead
 */
public class PstGuesser extends Guesser {
	static byte[] pstmagic = {
		'!', 'B', 'D', 'N'};
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public PstGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(PstFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
        FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();
		if (extension.equalsIgnoreCase("pst")) {
            guess.setExtensionMatch(true);
		}
		
		byte[] first = new byte[4];
		source.getByteStream().read(first);
		if (compareMagic(first, pstmagic)) {
		    guess.setMagicNumber(true);
		} else {
		    guess.setPossible(false);
		}
		
        return guess;
        
	}

	static boolean compareMagic(byte[] b1, byte[] b2) {
		for (int i = 0; i < b2.length && i < b1.length; i++) {
			if (b2[i] != b1[i]) {
				return false;
			}
		}
		return true;
	}
    
    public String getName() {
        return "PstGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}

}
