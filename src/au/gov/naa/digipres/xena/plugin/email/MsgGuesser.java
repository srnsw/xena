package au.gov.naa.digipres.xena.plugin.email;
import java.io.IOException;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser  for the Microsoft MSG  file format.
 *
 * @author Chris Bitmead
 */
public class MsgGuesser extends Guesser {
	
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public MsgGuesser() throws XenaException
	{
		super();
	}

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(MsgFileType.class);
    }

    public Guess guess(XenaInputSource source) throws IOException, XenaException {
	    Guess guess = new Guess(type);
        FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();
		if (extension.equalsIgnoreCase("msg")) {
            guess.setExtensionMatch(true);
        }
		POIFSFileSystem fs = null;
		try {
		    fs = new POIFSFileSystem(source.getByteStream());
		    guess.setMagicNumber(true);
		} catch (IOException x) {
		    // an I/O error occurred, or the InputStream did not provide a compatible
		    // POIFS data structure
            guess.setPossible(false);
		}
        return guess;
	}
    
    public String getName() {
        return "MsgGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}

}
