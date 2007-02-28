package au.gov.naa.digipres.xena.plugin.postscript;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser will identify PostScript files PostScript gueser extends from Guesser
 * 
 * @see au.gov.naa.digipres.xena.kernel.normalise
 * 
 * @author Kamaj Jayakantha de Mel 
 *
 * @since 14-Feb-2007
 * @version 1.0
 * 
 */

public class PostscriptGuesser extends Guesser {

	/**
	 * Postscript file extension
	 */
	private static final String POSTSCRIPT_FILE_EXTENSION = "ps";

	/**
	 * Postscript file magic signature
	 */
	public static final byte[] POSTSCRIPT_FILE_SIGNATURE = { '%', '!' };

	/**
	 * Length of the list of postcript file signature
	 */
	public static final int POSTSCRIPT_FILE_SIGNATURE_LENGTH = POSTSCRIPT_FILE_SIGNATURE.length;

	private Type type;

	/**
	 * Gives The Best Posible Guess
	 * @return	guess
	 */
	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		guess.setExtensionMatch(true);
		guess.setMimeMatch(true);
		guess.setPossible(true);
		guess.setCertain(true);
		return guess;
	}

	/**
	 * Get Guesser Name
	 * @return	String: GuesserName
	 */
	@Override
	public String getName() {
		return "PostscriptGuesser";
	}

	/**
	 * Get Type
	 * @return	type : type
	 */
	@Override
	public Type getType() {
		return type;
	}

	/**
	 * Return the guess object with file extension, mime type and file signature
	 * @param	XenaInputSource
	 * @return 	Guess
	 * @throws 	XenaException, IOException
	 */
	@Override
	public Guess guess(XenaInputSource xenaInputSource) throws XenaException,IOException {

		// Create Guess object
		Guess guess = new Guess(type);
		// Get the mime type of input file
		String mimeType = xenaInputSource.getMimeType();

		// Declare an array of postscript signature
		byte[] postScriptFileSignature = new byte[POSTSCRIPT_FILE_SIGNATURE_LENGTH];

		xenaInputSource.getByteStream().read(postScriptFileSignature);

		String fileID = xenaInputSource.getSystemId().toLowerCase();

		// Check mime type
		if ((mimeType != null) && mimeType.equals("application/postscript")) {
			guess.setMimeMatch(true);
		}

		// Check file extension
		if (fileID.endsWith(PostscriptGuesser.POSTSCRIPT_FILE_EXTENSION)) {
			guess.setExtensionMatch(true);
			guess.setPossible(true);
		}
		// Check file signature
		if (GuesserUtils.compareByteArrays(postScriptFileSignature,
				POSTSCRIPT_FILE_SIGNATURE)) {
			guess.setMagicNumber(true);
			guess.setDataMatch(true);
			guess.setCertain(true);
			
		} else {
			guess.setDataMatch(false);
			guess.setPossible(false);
		}
		return guess;
	}
	
	/**
	 * Initialize the Guesser
	 * @param	GuesserManager
	 * @throws 	XenaException
	 */
	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(PostscriptFileType.class);
	}
}
