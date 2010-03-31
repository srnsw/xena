package au.gov.naa.digipres.xena.plugin.psd;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * The guesser will identify PSD files
 *   
 * the PSD gueser extends from Guesser         
 * @see au.gov.naa.digipres.xena.kernel.normalise; 
 * 
 * @author Kamaj Jayakantha de Mel
 * @since 09-Feb-2007
 * 
 * @version 1.0
 * 
 */

public class PsdGuesser extends Guesser {

	private static final String EXTENSION = "psd";
	public static final byte[] PSD_SIGNATURE = { '8', 'B', 'P', 'S' };
	private Type type;

	/**
	 *  Gives The Best Posible Guess
	 *  @return	guess
	 */
	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		guess.setExtensionMatch(true);
		guess.setMimeMatch(true);
		guess.setPossible(true);
		return guess;
	}

	/**
	 *@return	String: Guesser Name 
	 */
	@Override
	public String getName() {
		return "PSDGuesser";
	}

	/**
	 * @return	type
	 */
	@Override
	public Type getType() {
		return type;
	}

	/**
	 * @return Guess
	 */
	@Override
	public Guess guess(XenaInputSource xenaInputSource) throws XenaException,IOException {
	
		Guess guess = new Guess(type);
        String mime = xenaInputSource.getMimeType();
       
        byte[] fileSignature = new byte[PSD_SIGNATURE.length];
        
        xenaInputSource.getByteStream().read(fileSignature);
      
        String fileID = xenaInputSource.getSystemId().toLowerCase();
        
        if ((mime != null) && mime.equals("image/photoshop") ) {
        	guess.setMimeMatch(true);
        }
        if (fileID.endsWith(PsdGuesser.EXTENSION)) {
            guess.setExtensionMatch(true);
            guess.setPossible(true);
        }
        if (GuesserUtils.compareByteArrays(fileSignature, PSD_SIGNATURE)) {
            guess.setMagicNumber(true);
            guess.setDataMatch(true);
        } else {
        	 guess.setDataMatch(false);
            guess.setPossible(false);
        }
        return guess;
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(PsdFileType.class);

	}

}
