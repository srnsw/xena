package au.gov.naa.digipres.xena.plugin.plaintext;
import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for non-standard plaintext files, ie plain text files that have
 * been encoded using a non-standard charset.
 * The standard charsets are defined in PlainTextGuesser.
 * TODO: define the standard charsets in properties
 *
 * @author Justin Waddell
 */
public class NonStandardPlainTextGuesser extends Guesser {
    
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public NonStandardPlainTextGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(NonStandardPlainTextFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
                
        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull().toLowerCase();
        boolean extensionMatch = false;
        
        for (int i = 0; i < PlainTextGuesser.EXTENSIONS.length; i++) {
            if (extension.equals(PlainTextGuesser.EXTENSIONS[i])) {
                extensionMatch = true;
                break;
            }
        }
        guess.setExtensionMatch(extensionMatch);
        
        // If the guessed charset is null or one of the standard charsets, 
        // then set Data Likely to false, as only recognised non-standard 
        // charsets are of interest here.
        // Otherwise, do nothing as even binary files tend to have a charset
        // guessed for them...
		try {
		    String charset = 
		    	CharsetDetector.guessCharSet(source.getByteStream(), 2 ^ 16);
		    
		    if (charset == null ||
		    	arrayContainsValue(PlainTextGuesser.STANDARD_CHARSETS, charset)) {
		        guess.setDataMatch(false);
		    }
		} catch (IOException x) {
		    throw new XenaException(x);
		}
		
		return guess;
	}
    
    public String getName() {
        return "NonStandardPlainTextGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		return guess;
	}

	private boolean arrayContainsValue(String[] array, String value)
	{
		boolean found = false;
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(value))
			{
				found = true;
				break;
			}
		}
		return found;
	}

	@Override
	public Type getType()
	{
		return type;
	}
	
}
