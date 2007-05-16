/*
 * Created on 15/05/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.macbinary;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class MacBinaryGuesser extends Guesser
{
	private static final String[] binExtensions = {"bin"};
	private static final String[] binMime = {"application/macbinary"};
	private static final byte[][] binMagic = {{  }};
	
    private FileTypeDescriptor[] binFileDescriptors = 
    {
    	new FileTypeDescriptor(binExtensions, binMagic, binMime),
   };

    private Type type;
       
    /**
     * @throws XenaException 
	 * 
	 */
	public MacBinaryGuesser()
	{
		super();
	}    

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException 
    {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(MacBinaryFileType.class);
    }

	public Guess guess(XenaInputSource source) throws IOException, XenaException 
	{
        Guess guess = new Guess(getType());
        String type = source.getMimeType();

        //get the mime type...
        if (type != null && !type.equals(""))
        {
        	for (int i = 0; i < binFileDescriptors.length; i++)
        	{
	        	if (binFileDescriptors[i].mimeTypeMatch(type))
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
	        for (int i = 0; i < binFileDescriptors.length; i++)
	        {
	        	if (binFileDescriptors[i].extensionMatch(extension))
	        	{
	        		extMatch = true;
	        		break;
	        	}
	        }
        }
	    guess.setExtensionMatch(extMatch);

	    // MacBinary has a weak magic number (first byte is 0) so while we won't be able to definitively confirm
	    // that this file is a MacBinary file, we can confirm that it is NOT a MacBinary file by checking certain bytes in the header
        byte[] header = new byte[128];
        source.getByteStream().read(header);
        if (header[0] != 0 ||
        	header[74] != 0 ||
        	header[82] != 0)
        {
        	guess.setMagicNumber(false);
        }
	    
        
        return guess;
    }

    public String getName() 
    {
        return "MacBinary Guesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMimeMatch(true);
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
