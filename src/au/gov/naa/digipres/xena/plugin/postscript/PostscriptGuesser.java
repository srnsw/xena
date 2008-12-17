package au.gov.naa.digipres.xena.plugin.postscript;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser will identify PostScript files PostScript guesser extends from Guesser
 * 
 * @see au.gov.naa.digipres.xena.kernel.normalise
 * 
 * @author Kamaj Jayakantha de Mel 
 *
 * @since 14-Feb-2007
 * @version 1.0
 * 
 */

public class PostscriptGuesser extends DefaultGuesser {

	private static final byte[][] psMagic = {{ '%', '!' }};
	private static final String[] psExtensions = {"ps"};
	private static final String[] psMime = {"application/postscript"};
	
	private FileTypeDescriptor[] descriptorArr = { new FileTypeDescriptor(psExtensions, psMagic, psMime)};

	private Type type;

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
	 * Initialize the Guesser
	 * @param	GuesserManager
	 * @throws 	XenaException
	 */
	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(PostscriptFileType.class);
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}
}
