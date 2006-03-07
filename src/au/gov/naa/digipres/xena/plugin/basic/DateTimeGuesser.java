package au.gov.naa.digipres.xena.plugin.basic;
import java.io.IOException;
import java.io.Reader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser class for  date/time values.
 *
 * @author Chris Bitmead
 */
public class DateTimeGuesser extends Guesser {
	
	private Type type;
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public DateTimeGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(DateTimeFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
        Guess guess = new Guess(type);
		Reader is = source.getCharacterStream();
		int dashCount = 0;
		int slashCount = 0;
		int colonCount = 0;
		int digitCount = 0;
		int c;
		int MAX_CHARACTERS_TO_EXAMINE = 40;
		for (; 0 < MAX_CHARACTERS_TO_EXAMINE && 0 <= (c = is.read()); MAX_CHARACTERS_TO_EXAMINE--) {
			if (c == '-') {
				dashCount++;
			} else if (c == '/') {
				slashCount++;
			} else if (c == ':') {
				colonCount++;
			} else if (Character.isDigit((char)c)) {
				digitCount++;
			}
		}
		if ((dashCount == 2 || slashCount == 2 || colonCount == 2) && 4 <= digitCount) {
		    guess.setDataMatch(true);
		}
        guess.setPossible(true);
        guess.setPriority(GuessPriority.LOW);
        
        return guess;
	}

    public String getName() {
        return "DateTimeGuesser";
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
