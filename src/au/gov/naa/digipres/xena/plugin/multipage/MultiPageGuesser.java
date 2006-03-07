package au.gov.naa.digipres.xena.plugin.multipage;
import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for a number of files suitable for input into the MultiPageNormaliser.
 *
 * @author Chris Bitmead
 */
public class MultiPageGuesser extends Guesser {
	
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public MultiPageGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(MultiPageFileType.class);
	}


	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
        
        if (source instanceof MultiInputSource) {
            // we want to push it up the rankings a little bit...
			
            // AAK 01/2005 - the only we reason we want to 'push it up the rankings a little bit'
            // is because the guesser architecture is busted. Will have to be reviewed in the
            // future me thinks...
            
            guess.setDataMatch(true);
            guess.setExtensionMatch(true);
            
		}
		return guess;
	}

    
    public String getName() {
        return "MultiPageGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		guess.setDataMatch(true);
		return guess;
	}


	@Override
	public Type getType()
	{
		return type;
	}
   
}
