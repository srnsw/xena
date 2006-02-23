package au.gov.naa.digipres.xena.kernel.guesser;
import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 *  A base class for modules that guess the types of files.
 *
 * @see GuesserManager
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public abstract class OldGuesser {
	/**
	 *  Guess the type of an input Source.
	 *
	 * @param  source A source of data.
	 * @return The guess.
	 * @exception  IOException
	 */
    
    
    abstract public String getName();
    
	abstract public Guess guess(XenaInputSource source) throws IOException, XenaException;
}
