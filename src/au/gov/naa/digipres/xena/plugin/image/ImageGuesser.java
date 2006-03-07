package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for Java supported image types other than the core JPEG and PNG
 * 
 * @author Chris Bitmead
 */
public class ImageGuesser extends Guesser {
    static byte[] gifmagic = { 'G', 'I', 'F' };

    static byte[] gifTail = { 0x00, 0x3b };

    static byte[] tiffmagic1 = { 'M', 'M' };

    static byte[] tiffmagic2 = { 'I', 'I' };

    static byte[] bmpmagic = { 'B', 'M' };
    
    private Type type;
    
    
    /**
     * @throws XenaException 
	 * 
	 */
	public ImageGuesser() throws XenaException
	{
		super();
    	type = TypeManager.singleton().lookup(ImageFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException 
	{
        Guess guess = new Guess(type);
        String type = source.getMimeType();
        

        //get the mime type...
        if (type != null &&
        	(type.equals("image/gif") || 
        	 type.equals("image/tiff") || 
        	 type.equals("image/bmp"))) {
            guess.setMimeMatch(true);
        }

        //Get the extension...
        String id = source.getSystemId().toLowerCase();
        if (id.endsWith(".gif") || id.endsWith(".tiff") || id.endsWith(".tif")
                || id.endsWith(".bmp")) {
            guess.setExtensionMatch(true);
        }

        //Get the magic number
        byte[] first = new byte[3];
        source.getByteStream().read(first);
        if (GuesserUtils.compareByteArrays(first, gifmagic)
                || GuesserUtils.compareByteArrays(first, tiffmagic1)
                || GuesserUtils.compareByteArrays(first, tiffmagic2)
                || GuesserUtils.compareByteArrays(first, bmpmagic)) {
            guess.setMagicNumber(true);
            
            // TODO: A better way of checking for data match
            guess.setDataMatch(true);

        } else {
            guess.setMagicNumber(false);
            guess.setPossible(false);
        }
        
        return guess;
    }

    public String getName() {
        return "ImageGuesser";
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
