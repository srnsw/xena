package au.gov.naa.digipres.xena.plugin.naa;
import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.util.TagContentFinder;

/**
 * Wraps the XML according to NAA policy. Firstly, an inner package with NAA
 * meta data, then outside that a checksum.
 *
 * @author Chris Bitmead
 */
public class NaaSignedAipWrapNormaliser extends AbstractMetaDataWrapper {

    
    public final static String NAA_PACKAGE_WRAPPER_NAME = "NaaSignedAipWrapper";
    
	NaaInnerWrapNormaliser innerWrapNormaliser = new NaaInnerWrapNormaliser(this);
	NaaOuterWrapNormaliser outerWrapNormaliser = new NaaOuterWrapNormaliser();
    
	public String toString() {
		return "NAA Signed AIP Wrapper";
	}

    @Override
    public String getName() {
        return NAA_PACKAGE_WRAPPER_NAME;
    }
    
	public void setContentHandler(ContentHandler handler) 
	{
		super.setContentHandler(innerWrapNormaliser);		
		innerWrapNormaliser.setParent(this);
		innerWrapNormaliser.setPackageURI(NaaTagNames.PACKAGE_URI);
		
		if (this.isEmbedded())
		{
			innerWrapNormaliser.setContentHandler(handler);
		}
		else
		{
			innerWrapNormaliser.setContentHandler(outerWrapNormaliser);
			outerWrapNormaliser.setParent(innerWrapNormaliser);
			outerWrapNormaliser.setContentHandler(handler);
		}
		
	}

	public ContentHandler getContentHandler() {
		return innerWrapNormaliser.getContentHandler();
	}

    public String getOpeningTag(){
        return NaaTagNames.WRAPPER_SIGNED_AIP;
    }
    
    public String getSourceId(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, NaaTagNames.DCIDENTIFIER);
    }
    
    public String getSourceName(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, NaaTagNames.DCSOURCE);
    }
    	   
}
