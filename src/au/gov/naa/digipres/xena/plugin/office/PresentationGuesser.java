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

public class PresentationGuesser extends OfficeGuesser {
		
	private static byte[][] sxiMagic = {};
    private static final String[] sxiExtensions = {"sxi"};
    private static final String[] sxiMime = {"application/vnd.sun.xml.impress"};
    
    private static final String[] pptExtensions = {"ppt", "pot", "pps"};
    private static final String[] pptMime = {"application/ms-powerpoint"};
    
    private Type type;
    
    private FileTypeDescriptor[] fileTypeDescriptors = 
    {
    	new FileTypeDescriptor(pptExtensions, officeMagic, pptMime),
    	new FileTypeDescriptor(sxiExtensions, sxiMagic, sxiMime),
    };

    
    /**
     * @throws XenaException 
	 * 
	 */
	public PresentationGuesser()
	{
		super();
	}
    
    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(PresentationFileType.class);
    }

	public String getName() {
        return "PresentationGuesser";
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
        
}
