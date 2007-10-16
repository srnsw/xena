/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

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
 */
public class NaaSignedAipWrapNormaliser extends AbstractMetaDataWrapper {

	public final static String NAA_PACKAGE_WRAPPER_NAME = "NaaSignedAipWrapper";

	NaaInnerWrapNormaliser innerWrapNormaliser = new NaaInnerWrapNormaliser(this);
	NaaOuterWrapNormaliser outerWrapNormaliser = new NaaOuterWrapNormaliser();

	@Override
    public String toString() {
		return "NAA Signed AIP Wrapper";
	}

	@Override
	public String getName() {
		return NAA_PACKAGE_WRAPPER_NAME;
	}

	@Override
    public void setContentHandler(ContentHandler handler) {
		super.setContentHandler(innerWrapNormaliser);
		innerWrapNormaliser.setParent(this);
		innerWrapNormaliser.setPackageURI(NaaTagNames.PACKAGE_URI);

		if (this.isEmbedded()) {
			innerWrapNormaliser.setContentHandler(handler);
		} else {
			innerWrapNormaliser.setContentHandler(outerWrapNormaliser);
			outerWrapNormaliser.setParent(innerWrapNormaliser);
			outerWrapNormaliser.setContentHandler(handler);
		}

	}

	@Override
    public ContentHandler getContentHandler() {
		return innerWrapNormaliser.getContentHandler();
	}

	@Override
    public String getOpeningTag() {
		return NaaTagNames.WRAPPER_SIGNED_AIP;
	}

	@Override
    public String getSourceId(XenaInputSource input) throws XenaException {
		return TagContentFinder.getTagContents(input, NaaTagNames.DCIDENTIFIER);
	}

	@Override
    public String getSourceName(XenaInputSource input) throws XenaException {
		return TagContentFinder.getTagContents(input, NaaTagNames.DCSOURCE);
	}

}
