/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;


import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class SylkGuesser extends OfficeGuesser {

	private static final String SYLK_TYPE_STRING = "Symbolic Link (SYLK)";
	
	private static byte[][] sylkMagic = {};
    private static final String[] sylkExtensions = {"slk", "sylk"};
    private static final String[] sylkMime = {"application/excel"};

    private Type type;
    
    private FileTypeDescriptor[] fileTypeDescriptors = 
    {
    	new FileTypeDescriptor(sylkExtensions, sylkMagic, sylkMime)
    };


    /**
     * @throws XenaException 
	 * 
	 */
	public SylkGuesser() throws XenaException
	{
		super();
	}

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(SylkFileType.class);
    }

	public String getName() {
        return "SylkGuesser";
    }
    
    public Guess guess(XenaInputSource source) throws XenaException, IOException {
 
        Guess guess = guess(source, type);
        guess.setPriority(GuessPriority.LOW);

        return guess;
        
    }

	/**
	 * @return Returns the fileTypeDescriptors.
	 */
	public FileTypeDescriptor[] getFileTypeDescriptors()
	{
		return fileTypeDescriptors;
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	protected String getOfficeTypeString()
	{
		return SYLK_TYPE_STRING;
	}
        
}
