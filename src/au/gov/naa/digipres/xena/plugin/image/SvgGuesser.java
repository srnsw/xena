package au.gov.naa.digipres.xena.plugin.image;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.Reader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser to guess a SVG file.
 *
 * @author Justin Waddell
 */
public class SvgGuesser extends Guesser {
	
	// Read in a maximum of 64k characters when checking for XML tag
	private static final int MAX_CHARS_READ = 64 * 1024;

	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public SvgGuesser()
	{
		super();
	}
    
    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(SvgFileType.class);
    }

	public Guess guess(XenaInputSource source) throws java.io.IOException, XenaException {
		Guess guess = new Guess(type);

		// Check extension
		if (source.getSystemId().toLowerCase().endsWith(".svg")) 
		{
			guess.setExtensionMatch(true);
		} 
		
		// Check Magic Number/Data Match
        Reader isReader = source.getCharacterStream();
        char[] charArr = new char[MAX_CHARS_READ];
        isReader.read(charArr, 0, MAX_CHARS_READ);
        
		BufferedReader rd = new BufferedReader(new CharArrayReader(charArr));
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
        return "SVGGuesser";
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
