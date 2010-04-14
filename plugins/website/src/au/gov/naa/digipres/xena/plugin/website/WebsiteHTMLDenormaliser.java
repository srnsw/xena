/**
 * This file is part of website.
 * 
 * website is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * website is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with website; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.website;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
import au.gov.naa.digipres.xena.util.XmlDeNormaliser;

/**
 * @author Justin Waddell
 *
 */
public class WebsiteHTMLDenormaliser extends XmlDeNormaliser {

	private Map<String, String> websiteFileIndex;
	private Map<String, String> linkRenameCache;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public WebsiteHTMLDenormaliser(Map<String, String> websiteFileIndex) {
		this.websiteFileIndex = websiteFileIndex;
		linkRenameCache = new HashMap<String, String>();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "HTML Denormaliser for websites";
	}

	/**
	 * Return the file extension which should be used for the file exported by this denormaliser.
	 * 
	 * This class extends the XmlDeNormaliser class which returns an extension of "xml". 
	 * We will thus just override with "html".
	 * 
	 * @return output file extension
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) {
		return "html";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.util.XmlDeNormaliser#getDoctypeIdentifier()
	 */
	@Override
	public String getDoctypePublicIdentifier() {
		return XHTML_DOCTYPE_PUBLIC_ID;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.util.XmlDeNormaliser#getDoctypeName()
	 */
	@Override
	public String getDoctypeName() {
		return XHTML_DOCTYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.util.XmlDeNormaliser#getDoctypeSystemIdentifier()
	 */
	@Override
	public String getDoctypeSystemIdentifier() {
		return XHTML_DOCTYPE_SYSTEM_ID;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		// Create a copy of the attributes, which we may need to modify
		AttributesImpl modifiedAtts = new AttributesImpl(atts);

		String originalLink = null;
		try {
			// Determine the name of the link attribute (if any) that we are interested in
			String linkAttributeName = null;
			if (localName.toLowerCase().equals("a") || localName.toLowerCase().equals("link")) {
				linkAttributeName = "href";
			} else if (localName.toLowerCase().equals("img")) {
				linkAttributeName = "src";
			}

			// If this element is a type that has a link, check to see if the link value should be modified.
			if (linkAttributeName != null) {
				for (int index = 0; index < modifiedAtts.getLength(); index++) {
					String attName = modifiedAtts.getLocalName(index);
					if (attName.toLowerCase().equals(linkAttributeName)) {
						originalLink = modifiedAtts.getValue(index);
						String modifiedLink = checkLinkName(originalLink);
						modifiedAtts.setValue(index, modifiedLink);
					}
				}
			}

		} catch (Exception ex) {
			// Do nothing - we will just log the error and use the unmodified link
			logger.log(Level.FINEST, "Problem checking the link for " + originalLink, ex);
		}

		// Output the element as normal, except use the modified attributes
		super.startElement(namespaceURI, localName, qName, modifiedAtts);
	}

	/**
	 * @param originalLink
	 * @return
	 * @throws IOException 
	 * @throws XenaException 
	 */
	private String checkLinkName(String originalLink) throws IOException, XenaException {
		String link = originalLink;

		// If we have already checked this link, just return the previous result
		if (linkRenameCache.containsKey(link)) {
			return linkRenameCache.get(link);
		}

		// Check for leading dots and slashes (eg ../../), and strip them away. 
		// They need to be removed as they will not exist in the websiteFileIndex in that form.
		String strippedPrefix = "";
		if (link.length() > 0) {
			char[] originalLinkChars = link.toCharArray();
			int currentIndex = 0;

			// Find the index of the first character that is not a dot or a slash
			while ((originalLinkChars[currentIndex] == '/' || originalLinkChars[currentIndex] == '.') && currentIndex < originalLinkChars.length) {
				currentIndex++;
			}

			// The link has leading dots and slashes, but does not consist entirely of dots and slashes, so strip them out
			if (currentIndex > 0 && currentIndex < originalLinkChars.length) {
				strippedPrefix = link.substring(0, currentIndex);
				link = link.substring(currentIndex);
			}
		}

		// If we have a normalised file for this link, check to see the name that this file
		// would be given when exported, and return this name.
		if (websiteFileIndex.containsKey(link)) {
			String normalisedFilename = websiteFileIndex.get(link);
			File normalisedFile = new File(sourceDirectory, normalisedFilename);
			XenaInputSource xis = new XenaInputSource(normalisedFile);

			// Get the deNormaliser for this XIS
			XMLFilter unwrapper = null;
			String tag;
			try {
				unwrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getUnwrapper(xis);
				tag = normaliserManager.unwrapGetTag(xis, unwrapper);
			} catch (XenaException xe) {
				// see if we can just get the tag regardless...
				tag = normaliserManager.getPluginManager().getMetaDataWrapperManager().getTag(xis);
			}
			AbstractDeNormaliser deNormaliser = normaliserManager.lookupDeNormaliser(tag);

			String exportFilename = normaliserManager.adjustOutputFileExtension(xis, deNormaliser, link);

			// Replace the dots and slashes that may have been removed earlier
			exportFilename = strippedPrefix + exportFilename;

			// Cache the (possibly) modified link
			linkRenameCache.put(originalLink, exportFilename);

			// Return the (possibly) modified link
			return exportFilename;
		}

		// If we don't know about this link, just return it unmodified
		return originalLink;
	}

}
