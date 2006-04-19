/*
 * Created on 19/04/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.image.pcx;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser;

public class PcxGuesser extends LegacyImageGuesser
{
	private Type pcxType;

    // PCX Format
    private static final byte[][] pcxMagic = {{0x0A, 0x00, 0x01},
    										  {0x0A, 0x02, 0x01},
    										  {0x0A, 0x03, 0x01},
    										  {0x0A, 0x04, 0x01},
    										  {0x0A, 0x05, 0x01}};
    private static final String[] pcxExtensions = {"pcx"};
    private static final String[] pcxMime = {"image/pcx",
    										 "image/x-pc-paintbrush",
    										 "image/x-pcx"};
	
	public PcxGuesser()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#initGuesser(au.gov.naa.digipres.xena.kernel.guesser.GuesserManager)
	 */
	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException
	{
		this.guesserManager = guesserManager;
		pcxType = getTypeManager().lookup(PcxFileType.class);
	}



	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors()
	{
		FileTypeDescriptor[] descArr = {new FileTypeDescriptor(pcxExtensions, pcxMagic, pcxMime)};
		return descArr;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#getName()
	 */
	@Override
	public String getName()
	{
		return "PCXGuesser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#getType()
	 */
	@Override
	public Type getType()
	{
		return pcxType;
	}
	
}
