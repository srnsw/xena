/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;
import java.io.InputStream;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

import com.sun.media.jai.codec.SeekableStream;

public class PNGGuesser extends Guesser {
    

    static byte[] pngmagic = {
        new Integer(0x89).byteValue(), 'P', 'N', 'G'};
    
    private Type type;
        
    /**
     * @throws XenaException 
	 * 
	 */
	public PNGGuesser() throws XenaException
	{
		super();
	}

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(PngFileType.class);
    }

    public Guess guess(XenaInputSource xis) throws XenaException, IOException 
    {
    	
        Guess guess = new Guess(type);

        // Extension and MIME
        String type = xis.getMimeType();
        String id = xis.getSystemId().toLowerCase();        
        if (type.equals("image/png")) {
            guess.setMimeMatch(true);
        }
        if (id.endsWith(".png")) {
            guess.setExtensionMatch(true);
        }
        
        // Magic Number
        byte[] first = new byte[4];
        xis.getByteStream().read(first);
        boolean magicMatch = GuesserUtils.compareByteArrays(first, pngmagic);
        guess.setMagicNumber(magicMatch);
                 
        // Need to find a good way to check for a data match, other than just trying to render it...
        // Or just using magic number match, as we do at the moment...
        guess.setDataMatch(magicMatch);       
        
        return guess;
    }

    public String getName() {
        return "PNGGuesser";
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
