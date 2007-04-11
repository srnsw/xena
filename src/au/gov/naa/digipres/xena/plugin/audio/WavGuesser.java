/*
 * Created on 21/06/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class WavGuesser extends Guesser
{
    private static final byte[][] wavMagic = {{ 'R', 'I', 'F', 'F' }};
    private static final String[] wavExtensions = {"wav"};
    private static final String[] wavMime = {"audio/wav",
    										 "audio/wave"};
    
    private FileTypeDescriptor[] descriptorArr = 
    {
    	new FileTypeDescriptor(wavExtensions, wavMagic, wavMime)
    };
    
    private Type type;
    
    
    /**
     * @throws XenaException 
	 * 
	 */
	public WavGuesser()
	{
		super();
	}

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(WavType.class);
    }

    public Guess guess(XenaInputSource source) throws IOException, XenaException {
        Guess guess = new Guess(type);
        String type = source.getMimeType();
        
        //get the mime type...
        if (type != null && !type.equals(""))
        {
	        for (int i = 0; i < descriptorArr.length; i++)
	        {
	        	if (descriptorArr[i].mimeTypeMatch(type))
	        	{
	        	    guess.setMimeMatch(true);
	        		break;
	        	}
	        }
        }

        //Get the extension...
        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull();
        
        boolean extMatch = false;
        if (!extension.equals(""))
        {
	        for (int i = 0; i < descriptorArr.length; i++)
	        {
	        	if (descriptorArr[i].extensionMatch(extension))
	        	{
	        		extMatch = true;
	        		break;
	        	}
	        }
        }
	    guess.setExtensionMatch(extMatch);

        // Get the magic number.
        byte[] first = new byte[4];
        source.getByteStream().read(first);
        boolean magicMatch = false;
        
        for (int i = 0; i < descriptorArr.length; i++)
        {
        	if (descriptorArr[i].magicNumberMatch(first))
        	{
         		magicMatch = true;
	        	break;
        	}
        }
        guess.setMagicNumber(magicMatch);
       
        return guess;
    }
    
    public String getName() {
        return "WAVGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}
    

}