package au.gov.naa.digipres.xena.plugin.basic;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for integer types.
 *
 * @author Chris Bitmead
 */
public class IntegerGuesser extends Guesser {
	
	private Type type;
	
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public IntegerGuesser() throws XenaException
	{
		super();
	}

    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(IntegerFileType.class);
    }
    
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		
        InputStream is = source.getByteStream();
		Reader rd = new InputStreamReader(is);
        
        guess.setPriority(GuessPriority.LOW);
        
        guess.setPossible(true);
        guess.setDataMatch(true);
		int c;
		while (0 <= (c = rd.read())) {
			if (!Character.isDigit((char)c)) {
                guess.setDataMatch(false);
				guess.setPossible(false);
				break;
			}
		}
		return guess;
	}
    
    public String getName() {
        return "IntegerGuesser";
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

	@Override
	public Type getType()
	{
		return type;
	}
	
}
