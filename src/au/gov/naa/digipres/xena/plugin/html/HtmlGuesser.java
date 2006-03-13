package au.gov.naa.digipres.xena.plugin.html;
import java.io.BufferedReader;
import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for HTML files.
 *
 * @author Chris Bitmead
 */
public class HtmlGuesser extends Guesser {
	
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public HtmlGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(HtmlFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		String type = source.getMimeType();
		if (type != null && type.equals("text/html")) {
            guess.setMimeMatch(true);
		}
        
        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull();
        if (extension.equalsIgnoreCase("html") || extension.equalsIgnoreCase("htm") ) {
            guess.setExtensionMatch(true);
        }
        
		// Check Magic Number/Data Match
		BufferedReader rd = new BufferedReader(source.getCharacterStream());
		String line = rd.readLine();
		
		// HTML files are very flexible, and could have a lot of data
		// before the opening html tag (and may not have the html tag
		// at all... not much we can do about that though). So check 
		// the first 100 lines for "<html".
		int count = 0;
		while (line != null && count < 100)
		{
			if (line.toLowerCase().indexOf("<html") >= 0 ||
				line.toUpperCase().indexOf("<!DOCTYPE HTML") >= 0)
			{
				guess.setDataMatch(true);
				
				// If match is on first non-blank line, then we pretty much
				// have an HTML magic number...
				if (count == 0)
				{
					guess.setMagicNumber(true);
				}
				break;
			}
			if (!line.trim().equals(""))
			{
				count++;
			}
			line = rd.readLine();
		}
        
		return guess;
	}
    
    public String getName() {
        return "HTMLGuesser";
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

	@Override
	public Type getType()
	{
		return type;
	}

}
