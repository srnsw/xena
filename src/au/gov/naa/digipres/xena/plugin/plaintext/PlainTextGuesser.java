package au.gov.naa.digipres.xena.plugin.plaintext;
import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for plaintext files.
 *
 * @author Chris Bitmead
 */
public class PlainTextGuesser extends Guesser {

    //made array of extensions so we can just add them willy - nilly.
    public static final String[] EXTENSIONS = { "txt", "log", "inf", "ini", 
    											 "css", "asp", "jsp", "js", 
    											 "java", "c", "cpp", "cs", 
    											 "dat", "bat" };
    
    public static final String[] STANDARD_CHARSETS = {"US-ASCII",
    												  "UTF-16", 
    												  "UTF-16BE", 
    												  "UTF-16LE",
    												  "UTF-8"};    
    
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess((FileType)TypeManager.singleton().lookup(PlainTextFileType.class));
		// If path ends with "/" it is really a directory, but the Sun
		// directory handler sets the mime type and returns plain text.
		
        // always return true! pretty much every file can be viewed as plain text, its just they might not look very good...
        guess.setPossible(true);
        
        String type = source.getMimeType();
        System.out.println(type);
		if (!source.getSystemId().endsWith("/") && 
			type != null && 
			type.equals("text/plain")) 
		{
			guess.setMimeMatch(true);
		}
        
        
        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull().toLowerCase();
        boolean extensionMatch = false;
        
        for (int i = 0; i < EXTENSIONS.length; i++) {
            if (extension.equals(EXTENSIONS[i])) {
                extensionMatch = true;
                break;
            }
        }
        guess.setExtensionMatch(extensionMatch);
        
        // Guess the charset. If the guessed charset is not one 
        // of the standard charsets, then it is not a PlainTextFile 
        //(it might be a NonStandardPlainTextFile).
		try {
		    String charset = CharsetDetector.mustGuessCharSet(source.getByteStream(), 2 ^ 16);
		    if (charset != null && arrayContainsValue(STANDARD_CHARSETS, charset)) {
		        guess.setDataMatch(true);
		    }
		} catch (IOException x) {
		    //throw new XenaException(x);
		    //TODO: aak - plaintext guesser - Check this stuff....
            // OK - Here's the deal. If something breaks during charset detection, lets just say
            // it is borked - and return a guess that is datamatch = false.
            // then the guesser manager will put something else up. also, if required, we can
            // still go and set the normaliser for to plain text regardless anyhow.
            
            guess.setDataMatch(false);
        }
		return guess;
	}
    
    public String getName() {
        return "PlainTextGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setPossible(true);
		guess.setMimeMatch(true);
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
    
}
