package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for Java supported image types other than the core JPEG and PNG
 * 
 * @author Chris Bitmead
 */
public class LegacyImageGuesser extends Guesser {
	// Leaving these in existing Image functionality for now
//	// GIF Format
//    private static final byte[][] gifMagic = {{ 'G', 'I', 'F' }};
//    private static final String[] gifExtensions = {".gif"};
//    private static final String[] gifMime = {"image/gif"};
//
//    // TIFF Format
//    private static final byte[][] tiffMagic = {{ 'M', 'M' }, 
//    										   { 'I', 'I' }};
//    private static final String[] tiffExtensions = {".tif", ".tiff"};
//    private static final String[] tiffMime = {"image/tiff"};
//
//    // BMP Format
//    private static final byte[][] bmpMagic = {{ 'B', 'M' }};
//    private static final String[] bmpExtensions = {".bmp"};
//    private static final String[] bmpMime = {"image/bmp"};
    
    // Macintosh PICT Format
    // PICT does not have a magic number, which means JIMI can't 
    // recognise it without the file extension (which is unavailable
    // when data is passed in a stream).
    // Unlikely to be getting many PICTs anyway...
//    private static final byte[][] pictMagic = {{}};
//    private static final String[] pictExtensions = {".pict", ".pct"};
//    private static final String[] pictMime = {};

    // Photoshop PSD Format
    private static final byte[][] psdMagic = {{ '8', 'B', 'P', 'S' }};
    private static final String[] psdExtensions = {"psd"};
    private static final String[] psdMime = {"image/photoshop",
    										 "image/x-photoshop",
    										 "image/psd"};
    
//    // TARGA TGA Format
      // TGA does not have a magic number, which means JIMI can't 
      // recognise it without the file extension (which is unavailable
      // when data is passed in a stream).
      // Unlikely to be getting many TGAs anyway...
//    private static final byte[][] tgaMagic = {{}};
//    private static final String[] tgaExtensions = {".tga", ".targa"};
//    private static final String[] tgaMime = {"image/tga",
//    										 "image/x-tga",
//    										 "image/targa",
//    										 "image/x-targa"};
// 
    // Windows Icon Format
    // For ICO files JIMI attempts to create an array of colour objects
    // for each possible colour... so for 32-bit images it tries to create
    // a > 2 billion object array... which understandably causes an 
    // out of memory error. So ICO files are disabled for now.
//    private static final byte[][] icoMagic = {{0x00, 0x00, 0x01, 0x00}};
//    private static final String[] icoExtensions = {".ico"};
//    private static final String[] icoMime = {"image/ico", "image/x-icon"};

    // Windows Cursor Format
    private static final byte[][] curMagic = {{0x00, 0x00, 0x02, 0x00}};
    private static final String[] curExtensions = {"cur"};
    private static final String[] curMime = {"image/x-win-bitmap"};
    
    // Sun Raster Format
    private static final byte[][] rasMagic = {{0x59, (byte)0xA6, 0x6A, (byte)0x95}};
    private static final String[] rasExtensions = {"ras", "rs", "sun"};
    private static final String[] rasMime = {"image/ras"};

    // XBM Format
    // XBM does not have a magic number, which means JIMI can't 
    // recognise it without the file extension (which is unavailable
    // when data is passed in a stream).
    // Unlikely to be getting many XBMs anyway...
//    private static final byte[][] xbmMagic = {{}};
//    private static final String[] xbmExtensions = {".xbm"};
//    private static final String[] xbmMime = {"image/x-xbitmap"};

    // XPM Format
    private static final byte[][] xpmMagic = {{0x2F, 0x2A, 0x20, 0x58, 0x50, 
    										   0x4D, 0x20, 0x2A, 0x2F, 0x0A}};
    private static final String[] xpmExtensions = {"xpm"};
    private static final String[] xpmMime = {"image/x-xpixmap",
    										 "image/xpm",
    										 "image/x-xpm"};

    // PCX Format
    // PCX has a 1-character magic number (0x0A) but this is not used as
    // it is picking up non-PCX files (eg inline email attachments)
    private static final byte[][] pcxMagic = {};
    private static final String[] pcxExtensions = {"pcx"};
    private static final String[] pcxMime = {"image/pcx",
    										 "image/x-pc-paintbrush",
    										 "image/x-pcx"};

    // Unlikely to be ever used
//    // DCX Format
//    private static final byte[][] dcxMagic = {{(byte)0xB1, 0x68, (byte)0xDE, 0x3A}};
//    private static final String[] dcxExtensions = {".dcx"};
//    private static final String[] dcxMime = {"image/dcx",
//    										 "image/x-dcx",
//    										 "image/vnd.swiftview-pcx"};
    
    private FileTypeDescriptor[] descriptorArr = 
    {
//    	new ImageDescriptor(gifExtensions, gifMagic, gifMime),
//    	new ImageDescriptor(tiffExtensions, tiffMagic, tiffMime),
//    	new ImageDescriptor(bmpExtensions, bmpMagic, bmpMime),
//    	new ImageDescriptor(pictExtensions, pictMagic, pictMime),
    	new FileTypeDescriptor(psdExtensions, psdMagic, psdMime),
//    	new ImageDescriptor(tgaExtensions, tgaMagic, tgaMime),
//    	new ImageDescriptor(icoExtensions, icoMagic, icoMime),
    	new FileTypeDescriptor(curExtensions, curMagic, curMime),
    	new FileTypeDescriptor(rasExtensions, rasMagic, rasMime),
//    	new ImageDescriptor(xbmExtensions, xbmMagic, xbmMime),
    	new FileTypeDescriptor(xpmExtensions, xpmMagic, xpmMime),
    	new FileTypeDescriptor(pcxExtensions, pcxMagic, pcxMime),
//    	new ImageDescriptor(dcxExtensions, dcxMagic, dcxMime)
    };
    
    private Type type;
    
    
    /**
     * @throws XenaException 
	 * 
	 */
	public LegacyImageGuesser() throws XenaException
	{
		super();
    	type = TypeManager.singleton().lookup(LegacyImageFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException 
	{
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

        // Get the magic number. Do not set to 'false' if magic number
	    // not found, as PCX does not have a magic number and setting to
	    // false would give the guesser such a low result that it could
	    // never be the best guess.
        byte[] first = new byte[10];
        source.getByteStream().read(first);
        
        for (int i = 0; i < descriptorArr.length; i++)
        {
        	if (descriptorArr[i].magicNumberMatch(first))
        	{
                guess.setMagicNumber(true);
        		
//				// Checking if image is renderable
//				try {
//					// Need to get full, unread stream again
//					InputStream is = source.getByteStream();
//					Jimi.getImage(is, Jimi.SYNCHRONOUS);
//					guess.setDataMatch(true);
//				} 
//				catch (Exception e) {
//				    guess.setPossible(false);
//				    guess.setDataMatch(false);
//				}
				
	        	break;
        	}
        }
        
        return guess;
    }

    public String getName() {
        return "Legacy ImageGuesser";
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
