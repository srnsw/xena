package au.gov.naa.digipres.xena.plugin.archive.gzip;
import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * 
 * @author justinw5
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class GZipGuesser extends Guesser 
{
    // GZip Format
    private static final byte[][] gzipMagic = {{ 0x1F, (byte)0x8B, 0x08 } };
    private static final String[] gzipExtensions = {"gz", "gzip", "tgz"};
    private static final String[] gzipMime = {"application/gzip"};
    
        
    private FileTypeDescriptor[] zipFileDescriptors = 
    {
    	new FileTypeDescriptor(gzipExtensions, gzipMagic, gzipMime)
    };
    
    private Type type;
    
    
    /**
     * @throws XenaException 
	 * 
	 */
	public GZipGuesser()
	{
		super();
	}
    

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException 
    {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(GZipFileType.class);
    }

	public Guess guess(XenaInputSource source) throws IOException, XenaException 
	{
		FileTypeDescriptor[] descriptorArr = getFileTypeDescriptors();
		
        Guess guess = new Guess(getType());
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
        byte[] first = new byte[10];
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

    public String getName() 
    {
        return "GZipGuesser";
    }
    
    protected FileTypeDescriptor[] getFileTypeDescriptors()
    {
    	return zipFileDescriptors;
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
