package au.gov.naa.digipres.xena.plugin.email;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
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
 * Guesser for MBOX email files.
 *
 * @author Chris Bitmead
 */
public class MboxGuesser extends Guesser {
	
	static final String FROM_TEXT = "From ";
	private Type type;

	
	/**
	 * @throws XenaException 
	 * 
	 */
	public MboxGuesser() throws XenaException
	{
		super();
	}

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(MboxDirFileType.class);
    }

    public Guess guess(XenaInputSource source) throws IOException, XenaException {
        Guess guess = new Guess(type);
        
        if (source instanceof MultiInputSource) {
            guess.setPossible(true);
        }
        
        InputStream is = source.getByteStream();
		Reader reader = new InputStreamReader(is);
		char[] from = new char[FROM_TEXT.length() + 1];
		reader.read(from);
		String froms = new String(from);
		if (froms.substring(0, FROM_TEXT.length()).equalsIgnoreCase(FROM_TEXT) || froms.substring(1).equalsIgnoreCase(FROM_TEXT)) {
			guess.setDataMatch(true);
			guess.setPriority(GuessPriority.HIGH);
		}
        return guess;
	}
    
    public String getName() {
        return "MBoxGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setPossible(true);
		guess.setDataMatch(true);
		guess.setPriority(GuessPriority.HIGH);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}

}
