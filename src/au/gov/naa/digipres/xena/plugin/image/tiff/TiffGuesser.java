package au.gov.naa.digipres.xena.plugin.image.tiff;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for Java supported image types other than the core JPEG and PNG
 * 
 * @author Justin Waddell
 */
public class TiffGuesser extends Guesser
{
	static byte[] tiffmagic1 = { 0x4D, 0x4D, 0x00, 0x2A };
	static byte[] tiffmagic2 = { 0x49, 0x49, 0x2A, 0x00 };

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public TiffGuesser()
	{
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException
	{
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(TiffFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException
	{
		Guess guess = new Guess(type);
		String type = source.getMimeType();

		//get the mime type...
		if (type.equals("image/tiff"))
		{
			guess.setMimeMatch(true);
		}

		//Get the extension...
		String id = source.getSystemId().toLowerCase();
		if (id.endsWith(".tiff") || id.endsWith(".tif"))
		{
			guess.setExtensionMatch(true);
		}

		//Get the magic number
		byte[] first = new byte[4];
		source.getByteStream().read(first);
		if (GuesserUtils.compareByteArrays(first, tiffmagic1)|| GuesserUtils.compareByteArrays(first, tiffmagic2))
		{
			guess.setMagicNumber(true);

			// TODO: A better way of checking for data match
			guess.setDataMatch(true);

		}
		else
		{
			guess.setMagicNumber(false);
			guess.setPossible(false);
		}

		return guess;
	}

	public String getName()
	{
		return "TiffGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}

}
