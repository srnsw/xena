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

// SAX classes.
// JAXP 1.1
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.util.UrlEncoder;

/**
 * Wrap the XML with NAA approved meta-data.
 *
 */
public class NaaInnerWrapNormaliser extends XMLFilterImpl {
	private SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private AbstractMetaDataWrapper parent;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private String packageURI = "";

	public NaaInnerWrapNormaliser(AbstractMetaDataWrapper parent) {
		super();
		this.parent = parent;
	}

	@Override
    public void startDocument() throws org.xml.sax.SAXException {
		String fileName;
		char[] id;

		XMLReader normaliser = (XMLReader) getProperty("http://xena/normaliser");
		if (normaliser == null) {
			throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
		}
		boolean isBinary = normaliser.getClass().getName().equals("au.gov.naa.digipres.xena.plugin.basic.BinaryToXenaBinaryNormaliser");
		XenaInputSource xis = (XenaInputSource) getProperty("http://xena/input");
		super.startDocument();
		File outfile = ((File) getProperty("http://xena/file"));

		ContentHandler th = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		th.startElement(packageURI, NaaTagNames.PACKAGE, NaaTagNames.PACKAGE_PACKAGE, att);

		// Add metadata tags
		th.startElement(packageURI, NaaTagNames.META, NaaTagNames.PACKAGE_META, att);

		/*
		 * Add the NAA Package wrapper string.
		 */
		th.startElement(NaaTagNames.NAA_URI, NaaTagNames.WRAPPER, NaaTagNames.NAA_WRAPPER, att);
		th.characters(NaaTagNames.NAA_PACKAGE.toCharArray(), 0, NaaTagNames.NAA_PACKAGE.toCharArray().length);
		th.endElement(NaaTagNames.NAA_URI, NaaTagNames.WRAPPER, NaaTagNames.NAA_WRAPPER);

		/*
		 * Add the date that the package was created by Xena.
		 */
		th.startElement(NaaTagNames.DCTERMS_URI, NaaTagNames.CREATED, NaaTagNames.DCCREATED, att);
		char[] sDate = isoDateFormat.format(new java.util.Date(System.currentTimeMillis())).toCharArray();
		th.characters(sDate, 0, sDate.length);
		th.endElement(NaaTagNames.DCTERMS_URI, NaaTagNames.CREATED, NaaTagNames.DCCREATED);

		if (xis.getFile() != null || outfile != null) {

			if (outfile != null) {

				/*
				 * Add the identifier for the package.
				 */
				th.startElement(NaaTagNames.DC_URI, NaaTagNames.IDENTIFIER, NaaTagNames.DCIDENTIFIER, att);

				fileName = xis.getOutputFileName().substring(0, xis.getOutputFileName().lastIndexOf('.'));
				id = fileName.toCharArray();

				th.characters(id, 0, id.length);
				th.endElement(NaaTagNames.DC_URI, NaaTagNames.IDENTIFIER, NaaTagNames.DCIDENTIFIER);

			}

			/*
			 * Add out data sources meta information.
			 */
			th.startElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCES, NaaTagNames.NAA_DATASOURCES, att);

			/*
			 * This is indented to indicate this block of code is responsible for doing the datasources. TODO: The
			 * following code should be commented to indicate what meta data is being written.
			 */
			{
				List<XenaInputSource> xenaInputSourceList = new ArrayList<XenaInputSource>();
				if (xis instanceof MultiInputSource) {
					Iterator it = ((MultiInputSource) xis).getSystemIds().iterator();
					while (it.hasNext()) {
						String url = (String) it.next();
						xenaInputSourceList.add(new XenaInputSource(url, null));
					}
				} else {
					xenaInputSourceList.add(xis);
				}
				Iterator it = xenaInputSourceList.iterator();
				while (it.hasNext()) {
					XenaInputSource source = (XenaInputSource) it.next();
					th.startElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCE, NaaTagNames.NAA_DATASOURCE, att);

					XenaInputSource relsource = null;
					try {
						java.net.URI uri = new java.net.URI(source.getSystemId());
						if (uri.getScheme().equals("file")) {
							File file = new File(uri);
							final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
							char[] lastModStr = sdf.format(new Date(file.lastModified())).toCharArray();
							th.startElement(NaaTagNames.NAA_URI, NaaTagNames.LASTMODIFIED, NaaTagNames.NAA_LASTMODIFIED, att);
							th.characters(lastModStr, 0, lastModStr.length);
							th.endElement(NaaTagNames.NAA_URI, NaaTagNames.LASTMODIFIED, NaaTagNames.NAA_LASTMODIFIED);

							// TODO nested try / catch. nasty. can we refactor this somewhat?
							// this needs to be done as a matter of urgency...

							/*
							 * Get the path location.
							 * 
							 * First off, see if we can get a path from the filter manager, and get a relative path. If
							 * that doesnt work, try to get a legacy base path, and a relative path from that. If still
							 * no success, then we set the path to just be the file name.
							 * 
							 */
							String relativePath = null;
							File baseDir;

							if (parent.getMetaDataWrapperManager().getPluginManager().getMetaDataWrapperManager().getBasePathName() != null) {
								try {
									baseDir =
									    new File(parent.getMetaDataWrapperManager().getPluginManager().getMetaDataWrapperManager().getBasePathName());
									if (baseDir != null) {
										relativePath = FileName.relativeTo(baseDir, file);
									}
								} catch (IOException iox) {
									relativePath = null;
								}
							}

							// Commented out as this causes an exception in Xena Lite... cannot set a single
							// base path in Xena Lite as files could come from different drives, network shares etc
							// if (relativePath == null) {
							// try {
							// baseDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.SOURCE_DIR_STRING);
							// if (baseDir != null) {
							// relativePath = FileName.relativeTo(baseDir, file);
							// }
							// } catch (IOException iox) {
							// //sysout
							// logger.log(Level.FINER, "Could not get base path from Legacy Xena code: " + iox);
							// relativePath = null;
							// } catch (XenaException xe) {
							// //sysout
							// logger.log(Level.FINER, "Could not get base path from Legacy Xena code: " + xe);
							// relativePath = null;
							// }
							// }
							if (relativePath == null) {
								relativePath = file.getName();
							}
							String encodedPath = null;
							try {
								encodedPath = UrlEncoder.encode(relativePath);
							} catch (UnsupportedEncodingException x) {
								throw new SAXException(x);
							}
							relsource = new XenaInputSource(new java.net.URI("file:/" + encodedPath).toASCIIString(), null);
						} else {
							relsource = source;
						}
					} catch (java.net.URISyntaxException x) {
						x.printStackTrace();
						// Nothing
					}

					th.startElement(NaaTagNames.DC_URI, NaaTagNames.SOURCE, NaaTagNames.DCSOURCE, att);
					char[] src = relsource.getSystemId().toCharArray();
					th.characters(src, 0, src.length);
					th.endElement(NaaTagNames.DC_URI, NaaTagNames.SOURCE, NaaTagNames.DCSOURCE);

					// This appears to be redundant, as it stores exactly the same thing as the dc:identifier element.
					// Also, the API ID does not belong in a "source" section of the XML.
					// if (!isBinary) {
					// //File file = xis.getUltimateFile();
					// if (xis.getOutputFileName() != null) {
					// //TODO - this really should be throwing an exception right here.
					// fileName = xis.getOutputFileName().substring(0, xis.getOutputFileName().lastIndexOf('.'));
					// id = fileName.toCharArray();
					// th.startElement(NaaTagNames.NAA_URI, NaaTagNames.SOURCEID, NaaTagNames.NAA_SOURCEID,att);
					// th.characters(id, 0, id.length);
					// th.endElement(NaaTagNames.NAA_URI, NaaTagNames.SOURCEID, NaaTagNames.NAA_SOURCEID);
					// }
					// }

					if (isBinary) {
						char[] typename = "binary data".toCharArray();
						th.startElement(NaaTagNames.NAA_URI, NaaTagNames.TYPE, NaaTagNames.NAA_TYPE, att);
						th.characters(typename, 0, typename.length);
						th.endElement(NaaTagNames.NAA_URI, NaaTagNames.TYPE, NaaTagNames.NAA_TYPE);
					}

					th.endElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCE, NaaTagNames.NAA_DATASOURCE);
				}
			}

			th.endElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCES, NaaTagNames.NAA_DATASOURCES);
		}

		th.endElement(packageURI, NaaTagNames.META, NaaTagNames.PACKAGE_META);

		/*
		 * Add our package content.
		 */
		th.startElement(packageURI, "content", "package:content", att);

	}

	@Override
    public void endDocument() throws org.xml.sax.SAXException {
		XenaInputSource xis = (XenaInputSource) getProperty("http://xena/input");
		File outfile = ((File) getProperty("http://xena/file"));
		ContentHandler th = getContentHandler();

		th.endElement(packageURI, "content", "package:content");
		th.endElement(packageURI, NaaTagNames.PACKAGE, NaaTagNames.PACKAGE_PACKAGE);

		/*
		 * We are all done.
		 */
		super.endDocument();
	}

	/**
	 * @return the packageURI
	 */
	public String getPackageURI() {
		return packageURI;
	}

	/**
	 * @param packageURI the packageURI to set
	 */
	public void setPackageURI(String packageURI) {
		this.packageURI = packageURI;
	}

}
