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
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public class PresentationGuesser extends OfficeGuesser {
		
	private static byte[][] sxiMagic = {};
    private static final String[] sxiExtensions = {"sxi"};
    private static final String[] sxiMime = {"application/vnd.sun.xml.impress"};
    
    private static final String[] pptExtensions = {"ppt", "pot", "pps"};
    private static final String[] pptMime = {"application/ms-powerpoint"};
    
    private FileTypeDescriptor[] fileTypeDescriptors = 
    {
    	new FileTypeDescriptor(pptExtensions, officeMagic, pptMime),
    	new FileTypeDescriptor(sxiExtensions, sxiMagic, sxiMime),
    };

    public String getName() {
        return "PresentationGuesser";
    }
    
    public Guess guess(XenaInputSource source) throws XenaException, IOException {
        FileType fileType = 
        	(FileType)TypeManager.singleton().lookup(PresentationFileType.class);

    	Guess guess = guess(source, fileType);
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
        
}
