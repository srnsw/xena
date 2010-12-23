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
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 29/09/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadata.AbstractMetaData;
import au.gov.naa.digipres.xena.kernel.metadata.XenaMetaData;
import au.gov.naa.digipres.xena.plugin.naa.NaaTagNames;
import au.gov.naa.digipres.xena.util.TagContentFinder;
import au.gov.naa.digipres.xena.util.UrlEncoder;

public class DefaultWrapper extends AbstractMetaDataWrapper {

	private ContentHandler checksumHandler;
	private ByteArrayOutputStream checksumBAOS;
	private MessageDigest digest;
	private OutputStreamWriter checksumOSW;
	private boolean startedChecksumming = false;
	private SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private final static String DEFAULTWRAPPER = "Default Package Wrapper";

	@Override
	public String getName() {
		return DEFAULTWRAPPER;
	}

	@Override
	public String toString() {
		return "Xena Default XML Wrapper";
	}

	@Override
	public String getOpeningTag() {
		return TagNames.XENA;
	}

	@Override
	public String getSourceId(XenaInputSource input) throws XenaException {
		return TagContentFinder.getTagContents(input, XenaMetaData.INPUT_SOURCE_URI_TAG);
	}

	@Override
	public String getSourceName(XenaInputSource input) throws XenaException {
		return TagContentFinder.getTagContents(input, XenaMetaData.INPUT_SOURCE_URI_TAG);
	}

	@Override
	public void startDocument() throws SAXException {
		// try {
		XMLReader normaliser = (XMLReader) getProperty("http://xena/normaliser");
		if (normaliser == null) {
			throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
		}

		XenaInputSource xis = (XenaInputSource) getProperty("http://xena/input");
		AbstractMetaData xenaMetaData = (AbstractMetaData) getProperty("http://xena/meta");
		File outfile = ((File) getProperty("http://xena/file"));
		xenaMetaData.setProperty("http://xena/normaliser", getProperty("http://xena/normaliser"));
		xenaMetaData.setProperty("http://xena/input", getProperty("http://xena/input"));

		String fileName;
		char[] id;

		boolean isBinary = normaliser.getClass().getName().equals("au.gov.naa.digipres.xena.plugin.basic.BinaryToXenaBinaryNormaliser");
		super.startDocument();

		// File outfile = ((File)getProperty("http://xena/file"));
		// if (outfile == null) {
		// throw new XenaException("Output file was null!");
		// }
		// if (xis.getFile() == null) {
		// throw new XenaException("XIS input file was null!");
		// }
		ContentHandler th = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		th.startElement(TagNames.XENA_URI, TagNames.XENA, TagNames.XENA, att);

		th.startElement(TagNames.XENA_URI, TagNames.META, TagNames.XENA_META, att);
		xenaMetaData.setContentHandler(this);
		try {
			xenaMetaData.parse(xis);
		} catch (IOException e) {
			throw new SAXException(e);
		}
		th.endElement(TagNames.XENA_URI, TagNames.META, TagNames.XENA_META);

		th.startElement(TagNames.WRAPPER_URI, TagNames.SIGNED_AIP, TagNames.WRAPPER_SIGNED_AIP, att);
		th.startElement(TagNames.WRAPPER_URI, TagNames.AIP, TagNames.WRAPPER_AIP, att);

		// Write the package information
		th.startElement(TagNames.PACKAGE_URI, TagNames.PACKAGE, TagNames.PACKAGE_PACKAGE, att);

		// Add metadata tags
		th.startElement(TagNames.PACKAGE_URI, TagNames.META, TagNames.PACKAGE_META, att);

		/*
		 * Add the NAA Package wrapper string.
		 */
		th.startElement(TagNames.NAA_URI, TagNames.WRAPPER, TagNames.NAA_WRAPPER, att);
		th.characters(TagNames.NAA_PACKAGE.toCharArray(), 0, TagNames.NAA_PACKAGE.toCharArray().length);
		th.endElement(TagNames.NAA_URI, TagNames.WRAPPER, TagNames.NAA_WRAPPER);

		/*
		 * Add the date that the package was created by Xena.
		 */
		th.startElement(TagNames.DCTERMS_URI, TagNames.CREATED, TagNames.DCCREATED, att);
		char[] sDate = isoDateFormat.format(new java.util.Date(System.currentTimeMillis())).toCharArray();
		th.characters(sDate, 0, sDate.length);
		th.endElement(TagNames.DCTERMS_URI, TagNames.CREATED, TagNames.DCCREATED);

		if (xis.getFile() != null || outfile != null) {

			if (outfile != null) {

				/*
				 * Add the identifier for the package.
				 */
				th.startElement(TagNames.DC_URI, TagNames.IDENTIFIER, TagNames.DCIDENTIFIER, att);

				fileName = xis.getOutputFileName().substring(0, xis.getOutputFileName().lastIndexOf('.'));
				id = fileName.toCharArray();

				th.characters(id, 0, id.length);
				th.endElement(TagNames.DC_URI, TagNames.IDENTIFIER, TagNames.DCIDENTIFIER);

			}

			/*
			 * Add out data sources meta information.
			 */
			th.startElement(TagNames.NAA_URI, TagNames.DATASOURCES, TagNames.NAA_DATASOURCES, att);

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
					th.startElement(TagNames.NAA_URI, TagNames.DATASOURCE, TagNames.NAA_DATASOURCE, att);

					XenaInputSource relsource = null;
					try {
						java.net.URI uri = new java.net.URI(source.getSystemId());
						if (uri.getScheme().equals("file")) {
							File file = new File(uri);
							final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
							char[] lastModStr = sdf.format(new Date(file.lastModified())).toCharArray();
							th.startElement(TagNames.NAA_URI, TagNames.LASTMODIFIED, TagNames.NAA_LASTMODIFIED, att);
							th.characters(lastModStr, 0, lastModStr.length);
							th.endElement(TagNames.NAA_URI, TagNames.LASTMODIFIED, TagNames.NAA_LASTMODIFIED);

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

							if (getMetaDataWrapperManager().getPluginManager().getMetaDataWrapperManager().getBasePathName() != null) {
								try {
									baseDir = new File(getMetaDataWrapperManager().getPluginManager().getMetaDataWrapperManager().getBasePathName());
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

					th.startElement(TagNames.DC_URI, TagNames.SOURCE, TagNames.DCSOURCE, att);
					char[] src = relsource.getSystemId().toCharArray();
					th.characters(src, 0, src.length);
					th.endElement(TagNames.DC_URI, TagNames.SOURCE, TagNames.DCSOURCE);

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
						th.startElement(TagNames.NAA_URI, TagNames.TYPE, TagNames.NAA_TYPE, att);
						th.characters(typename, 0, typename.length);
						th.endElement(TagNames.NAA_URI, TagNames.TYPE, TagNames.NAA_TYPE);
					}

					th.endElement(TagNames.NAA_URI, TagNames.DATASOURCE, TagNames.NAA_DATASOURCE);
				}
			}

			th.endElement(TagNames.NAA_URI, TagNames.DATASOURCES, TagNames.NAA_DATASOURCES);
		}

		th.endElement(TagNames.PACKAGE_URI, TagNames.META, TagNames.PACKAGE_META);

		/*
		 * Add our package content.
		 */
		// We use this classes startElement so the checksumming can be set up. 
		startElement(TagNames.PACKAGE_URI, TagNames.CONTENT, TagNames.PACKAGE_CONTENT, att);

		// } catch (XenaException x) {
		// throw new SAXException(x);
		// }
	}

	@Override
	public void endDocument() throws org.xml.sax.SAXException {
		/*
		 * THIS DOESNT WORK FOR EMBEDDED OBJECTS! Not sure why it was here at all really...
		 */
		// XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
		// File outfile = ((File)getProperty("http://xena/file"));
		// //int level = ((Integer)getProperty("http://xena/level"));
		// if (xis.getFile() != null || outfile != null) {
		// ContentHandler th = getContentHandler();
		// th.endElement(null, CONTENT_TAG, CONTENT_TAG);
		// th.endElement(null, OPENING_TAG, OPENING_TAG);
		// }
		ContentHandler th = getContentHandler();
		th.endElement(TagNames.WRAPPER_URI, NaaTagNames.AIP, NaaTagNames.WRAPPER_AIP);
		th.endElement(TagNames.WRAPPER_URI, NaaTagNames.SIGNED_AIP, NaaTagNames.WRAPPER_SIGNED_AIP);
		th.endElement(TagNames.XENA_URI, TagNames.XENA, TagNames.XENA);

		super.endDocument();
	}

	public void finishNormaliserXMLSection() throws org.xml.sax.SAXException {
		ContentHandler th = getContentHandler();

		// call this methods endElement so the checksum can finish correctly
		//		th.endElement(TagNames.PACKAGE_URI, TagNames.CONTENT, TagNames.PACKAGE_CONTENT);
		//		th.endElement(TagNames.PACKAGE_URI, TagNames.PACKAGE, TagNames.PACKAGE_PACKAGE);
		endElement(TagNames.PACKAGE_URI, TagNames.CONTENT, TagNames.PACKAGE_CONTENT);
		endElement(TagNames.PACKAGE_URI, TagNames.PACKAGE, TagNames.PACKAGE_PACKAGE);

	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);

		if (startedChecksumming) {
			checksumHandler.characters(ch, start, length);

			// Update checksum creator with new bytes from the call to characters
			try {
				checksumOSW.flush();
				checksumBAOS.flush();
				digest.update(checksumBAOS.toByteArray());
				checksumBAOS.reset();
			} catch (IOException iex) {
				throw new SAXException("Problem updating checksum", iex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);

		if ((uri.equals(TagNames.PACKAGE_URI) && (localName.equals(TagNames.CONTENT)) && (qName.equals(TagNames.PACKAGE_CONTENT)))) {
			startedChecksumming = false;

			//			checksumHandler.endDocument();
			setProperty("http://xena/digest", convertToHex(digest.digest()));

			try {
				if (checksumBAOS != null) {
					checksumBAOS.close();
				}
				if (checksumOSW != null) {
					checksumOSW.close();
				}
			} catch (IOException e) {
				throw new SAXException("Could not close checksum streams", e);
			}
		}

		if (startedChecksumming) {
			checksumHandler.endElement(uri, localName, qName);

			// Update checksum creator with new bytes from the call to endElement
			try {
				checksumOSW.flush();
				checksumBAOS.flush();
				digest.update(checksumBAOS.toByteArray());
				checksumBAOS.reset();
			} catch (IOException iex) {
				throw new SAXException("Problem updating checksum", iex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String,
	 * org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		super.startElement(uri, localName, qName, atts);

		if (startedChecksumming) {
			checksumHandler.startElement(uri, localName, qName, atts);

			// Update checksum creator with new bytes from the call to startElement
			try {
				checksumOSW.flush();
				checksumBAOS.flush();
				digest.update(checksumBAOS.toByteArray());
				checksumBAOS.reset();
			} catch (IOException iex) {
				throw new SAXException("Problem updating checksum", iex);
			}
		}

		if ((uri.equals(TagNames.PACKAGE_URI) && (localName.equals(TagNames.CONTENT)) && (qName.equals(TagNames.PACKAGE_CONTENT)))) {
			startedChecksumming = true;

			// Setup checksum stream and checksum producer
			// We need to generate the checksum of the Xena file here as this is the main ContentHandler. 
			// Then we pass it the default metadata object to include in the default metadata. 
			try {
				checksumBAOS = new ByteArrayOutputStream();
				checksumHandler = createChecksumHandler(checksumBAOS);
				digest = MessageDigest.getInstance(TagNames.DEFAULT_CHECKSUM_ALGORITHM);
			} catch (Exception e) {
				throw new SAXException("Could not create checksum handler", e);
			}
		}

	}

	private ContentHandler createChecksumHandler(ByteArrayOutputStream baos) throws IOException, TransformerException {
		// create our transform handler
		TransformerHandler transformerHandler = null;
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		transformerHandler = transformFactory.newTransformerHandler();

		checksumOSW = new OutputStreamWriter(baos, "UTF-8");
		StreamResult streamResult = new StreamResult(checksumOSW);
		transformerHandler.setResult(streamResult);
		transformerHandler.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		return transformerHandler;
	}

	private static String convertToHex(byte[] byteArray) {
		/*
		 * ------------------------------------------------------ Converts byte array to printable hexadecimal string.
		 * eg convert created checksum to file form. ------------------------------------------------------
		 */
		String s; // work string for single byte translation
		String hexString = ""; // the output string being built

		for (byte element : byteArray) {
			s = Integer.toHexString(element & 0xFF); // mask removes 'ffff' prefix from -ive numbers
			if (s.length() == 1) {
				s = "0" + s;
			}
			hexString = hexString + s;
		}
		return hexString;
	}

}
