package au.gov.naa.digipres.xena.plugin.xml;
import java.io.BufferedReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser to guess a file of a XML of a random type.
 *
 * @author Chris Bitmead
 */
public class XmlGuesser extends Guesser {
	
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public XmlGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(XmlFileType.class);
	}

	public Guess guess(XenaInputSource source) throws java.io.IOException, XenaException {
		Guess guess = new Guess(type);

		// Check extension
		if (source.getSystemId().toLowerCase().endsWith(".xml")) 
		{
			guess.setExtensionMatch(true);
		} 
		
		// Check Magic Number/Data Match
		BufferedReader rd = new BufferedReader(source.getCharacterStream());
		String line = rd.readLine();
		
		// Get the first non-blank line. If the first characters
		// are "<?xml" then we have matched magic number and data
		while (line != null)
		{
			line = line.trim();
			if (line.equals(""))
			{
				line = rd.readLine();
			}
			else
			{
				if (line.toLowerCase().startsWith("<?xml"))
				{
					guess.setMagicNumber(true);
					guess.setDataMatch(true);
				}
				break;
			}
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
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		guess.setDataMatch(true);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}
    
}
