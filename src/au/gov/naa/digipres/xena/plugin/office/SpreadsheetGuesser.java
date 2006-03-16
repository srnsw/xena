/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;


import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public class SpreadsheetGuesser extends OfficeGuesser {

	private static byte[][] sxcMagic = {{ 0x50, 0x4B, 0x03, 0x04, 0x14, 0x00 }};
    private static final String[] sxcExtensions = {"sxc"};
    private static final String[] sxcMime = {"application/vnd.sun.xml.calc"};
    
    private static final String[] xlExtensions = {"xls", "xlt"};
    private static final String[] xlMime = {"application/ms-excel"};
    
    private Type type;
    
    private FileTypeDescriptor[] fileTypeDescriptors = 
    {
    	new FileTypeDescriptor(xlExtensions, officeMagic, xlMime),
    	new FileTypeDescriptor(sxcExtensions, sxcMagic, sxcMime),
    };


    /**
     * @throws XenaException 
	 * 
	 */
	public SpreadsheetGuesser() throws XenaException
	{
		super();
	}

    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(SpreadsheetFileType.class);
    }

	public String getName() {
        return "SpreadsheetGuesser";
    }
    
    public Guess guess(XenaInputSource source) throws XenaException, IOException {
 
        Guess guess = guess(source, type);
        guess.setPriority(GuessPriority.DEFAULT);

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
        
}
