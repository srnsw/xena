package au.gov.naa.digipres.xena.kernel.guesser;
import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.BinaryFileType;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

/**
 * Guesser for binary (non-character) files.
 *
 * @author not attributable
 */
public class BinaryGuesser extends Guesser {
	
    private static byte[][] zipMagic = {{ 0x50, 0x4B, 0x03, 0x04}};
    private static final String[] zipExtensions = {"zip"};
    private static final String[] zipMime = {"application/zip"};

    private Type type;
		
    private FileTypeDescriptor[] fileTypeDescriptors = 
    {
    	new FileTypeDescriptor(zipExtensions, zipMagic, zipMime),
    };

    /**
	 * @throws XenaException 
	 * 
	 */
	public BinaryGuesser() throws XenaException
	{
		super();
	}

	@Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(BinaryFileType.class);
    }

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		InputStream in = source.getByteStream();
		int c = -1;
		int total = 0;
		while (total < 65536 && 0 <= (c = in.read())) {
			total++;
            //i have a better idea.
            // lets use the xml character validator.
            if (!XMLCharacterValidator.isValidCharacter((char)c)) {
                guess.setDataMatch(GuessIndicator.TRUE);
                break;   
            }
            
//			if (Character.isISOControl(c)) {
//				if (!(c == '\r' || c == '\n' || c == '\t' || c == '\f' || c == '\\')) {
//					guess.setDataMatch(GuessIndicator.TRUE);
//                    System.out.println("Found control char! it is:" + c +
//                                " in hex:" + Integer.toHexString(c) +
//                                " and the char renders as: [" + (char)c + "]" +
//                                " and it is at: " + total +
//                                " and this char valid returns: " + XMLCharacterValidator.isValidCharacter((char)c)) ;
//                    
//                    
//                    
//					break;
//				}
//			}
            
        }
        
        // MAGIC NUMBER
        
        byte[] first = new byte[4];
        source.getByteStream().read(first);
        
        for (int i = 0; i < fileTypeDescriptors.length; i++)
        {
        	if (fileTypeDescriptors[i].magicNumberMatch(first))
        	{
                guess.setMagicNumber(true);
	        	break;
        	}
        }
        
        // extension...
        //Get the extension...
        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull();
        
        if (!extension.equals(""))
        {
	        for (int i = 0; i < fileTypeDescriptors.length; i++)
	        {
	        	if (fileTypeDescriptors[i].extensionMatch(extension))
	        	{
	        		guess.setExtensionMatch(true);
	        		break;
	        	}
	        }
        }	    
		
        guess.setPriority(GuessPriority.LOW);
		return guess;
	}

    public String getName() {
        return "BinaryGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess bestGuess = new Guess();
		bestGuess.setDataMatch(true);
		bestGuess.setExtensionMatch(true);
		return bestGuess;
	}

	@Override
	public Type getType()
	{
		return type;
	}
	
}
