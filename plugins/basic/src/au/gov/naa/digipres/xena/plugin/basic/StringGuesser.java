package au.gov.naa.digipres.xena.plugin.basic;
import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for the string file type.
 *
 * @author Chris Bitmead
 */
public class StringGuesser extends Guesser {
	
	private Type type;
	
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public StringGuesser() throws XenaException
	{
		super();
	}

	@Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(StringFileType.class);
    }

    

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		
		Guess guess = new Guess(type);
		
        guess.setPossible(true);
        guess.setPriority(GuessPriority.LOW);
        guess.setDataMatch(true);
        
        InputStream in = source.getByteStream();
		int c;
		while (0 <= (c = in.read())) {
			if (Character.isISOControl((char)c)) {
				if (!(c == '\r' || c == '\n')) {
					guess.setDataMatch(false);
					break;
				}
			}
		}
		return guess;
	}
    
    public String getName() {
        return "StringGuesser";
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
