package au.gov.naa.digipres.xena.plugin.naa;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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
public class NaaPackageWrapNormaliser extends AbstractMetaDataWrapper {

    
    
	//final Namespace nameSpace = Namespace.getNamespace(NaaTagNames.PACKAGE_PREFIX, NaaTagNames.PACKAGE_URI);
	NaaInnerWrapNormaliser innerWrapNormaliser = new NaaInnerWrapNormaliser(this);
	ChecksumContentHandler checksumContentHandler = new ChecksumContentHandler();
	NaaOuterWrapNormaliser outerWrapNormaliser = new NaaOuterWrapNormaliser();

	
    
	public String toString() {
		return "NAA Package Wrapper";
	}

	public void setContentHandler(ContentHandler handler) {
		super.setContentHandler(innerWrapNormaliser);
		int level = 0;
		try {
			// JRW - really annoying in debug so making it slightly better
			Integer levelObj = (Integer)getProperty("http://xena/level");
			if (levelObj != null)
			{
				level = levelObj.intValue();
			}
        } catch (SAXNotSupportedException x) {
            //sysout - print stack trace in case of exception getting 'level' property.
			x.printStackTrace();
		} catch (SAXNotRecognizedException x) {
			//sysout - print stack trace in case of exception getting 'level' property.
            x.printStackTrace();
		}
		if (level == 0) {
			innerWrapNormaliser.setParent(this);
			innerWrapNormaliser.setContentHandler(checksumContentHandler);
			checksumContentHandler.setParent(innerWrapNormaliser);
			checksumContentHandler.setContentHandler(outerWrapNormaliser);
			outerWrapNormaliser.setParent(checksumContentHandler);
			outerWrapNormaliser.setContentHandler(handler);
			outerWrapNormaliser.setMD5(checksumContentHandler.getMD5());
		} else {
			innerWrapNormaliser.setParent(this);
			innerWrapNormaliser.setContentHandler(handler);
		}
	}

	public ContentHandler getContentHandler() {
		return outerWrapNormaliser.getContentHandler();
	}

    public String getOpeningTag(){
        return NaaTagNames.PACKAGE_PACKAGE;
    }
    
    public String getSourceId(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, NaaTagNames.DCIDENTIFIER);
    }
    
    public String getSourceName(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, NaaTagNames.DCSOURCE);
    }
    
}
