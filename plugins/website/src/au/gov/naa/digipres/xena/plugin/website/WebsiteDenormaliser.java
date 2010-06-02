/**
 * This file is part of website.
 * 
 * website is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * website is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with website; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.website;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataUnwrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.ExportResult;
import au.gov.naa.digipres.xena.util.XmlDeNormaliser;

/**
 * @author Justin Waddell
 *
 */
public class WebsiteDenormaliser extends AbstractDeNormaliser {

	private static final String HTML_DENORMALISER_NAME = "Xena HTML Denormaliser";

	private Map<String, String> websiteFileIndex;

	@Override
	public String getName() {
		return "Archive Denormaliser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#writesToRootExportFile()
	 */
	@Override
	public boolean writesToRootExportFile() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getOutputFileExtension(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startDocument()
	 */
	@Override
	public void startDocument() {
		// Ensure childExportResultList is empty
		childExportResultList.clear();

		websiteFileIndex = new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {

		if (websiteFileIndex == null || websiteFileIndex.isEmpty()) {
			// The primary website xena file has not been parsed correctly - throw an exception
			throw new SAXException("Could not export the website - no entries found.");
		}

		// Initialise the WebsiteHTMLDenormaliser
		WebsiteHTMLDenormaliser websiteHTMLDenormaliser = new WebsiteHTMLDenormaliser(websiteFileIndex);
		websiteHTMLDenormaliser.setNormaliserManager(normaliserManager);
		websiteHTMLDenormaliser.setSourceDirectory(sourceDirectory);

		// Strip the extension from the output filename
		int extensionIndex = outputFilename.lastIndexOf(".");
		String websiteDirName = extensionIndex != -1 ? outputFilename.substring(0, extensionIndex) : outputFilename;

		// Create the output directory for the website
		File websiteOutputDir = new File(outputDirectory, websiteDirName);
		websiteOutputDir.mkdirs();

		for (String entryOriginalPath : websiteFileIndex.keySet()) {
			String entryNormalisedFilename = websiteFileIndex.get(entryOriginalPath);
			File entryNormalisedFile = new File(sourceDirectory, entryNormalisedFilename);

			if (entryNormalisedFile.exists() && entryNormalisedFile.isFile()) {
				try {
					XenaInputSource xis = new XenaInputSource(entryNormalisedFile);

					// Get the deNormaliser for this XIS
					AbstractMetaDataUnwrapper unwrapper = null;
					String tag;
					try {
						unwrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getUnwrapper(xis);
						tag = normaliserManager.unwrapGetTag(xis, unwrapper);
					} catch (XenaException xe) {
						// see if we can just get the tag regardless...
						tag = normaliserManager.getPluginManager().getMetaDataWrapperManager().getTag(xis);
					}
					AbstractDeNormaliser deNormaliser = normaliserManager.lookupDeNormaliser(tag);
					if (deNormaliser == null) {
						// Just use basic XML denormaliser
						deNormaliser = new XmlDeNormaliser();
					} else if (deNormaliser.getName().equals(HTML_DENORMALISER_NAME)) {
						// If the deNormaliser is the HTMLDeNormaliser, we need to replace it with our own custom denormaliser.
						// This is because we need to modify the links inside the exported file in order to handle exported
						// files that have a different extension to what they had originally.
						deNormaliser = websiteHTMLDenormaliser;
					}

					String entryOutputFilename = normaliserManager.adjustOutputFileExtension(xis, deNormaliser, entryOriginalPath);

					// Export this entry
					ExportResult fileExportResult = normaliserManager.export(xis, websiteOutputDir, entryOutputFilename, true, deNormaliser);
					childExportResultList.add(fileExportResult);

				} catch (Exception ex) {
					throw new SAXException("Problem exporting website file " + entryOriginalPath, ex);
				}
			} else {
				// The normalised file for one of the website's files could not be found.
				throw new SAXException("Could not find the normalised file " + entryNormalisedFile.getAbsolutePath());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		if (qName.equals(WebsiteNormaliser.WEBSITE_PREFIX + ":" + WebsiteNormaliser.FILE_TAG)) {

			String elementOutputFilename = atts.getValue(WebsiteNormaliser.WEBSITE_PREFIX + ":" + WebsiteNormaliser.FILE_OUTPUT_FILENAME);
			String originalPath = atts.getValue(WebsiteNormaliser.WEBSITE_PREFIX + ":" + WebsiteNormaliser.FILE_ORIGINAL_PATH_ATTRIBUTE);

			websiteFileIndex.put(originalPath, elementOutputFilename);

		}
	}

}
