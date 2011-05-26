/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
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
 * @author Matthew Oliver
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.TagNames;
import au.gov.naa.digipres.xena.util.Checksum;
import au.gov.naa.digipres.xena.util.FileUtils;

/**
 * Normalisers may find it convenient to use this abstract class.
 */
abstract public class AbstractNormaliser implements XMLReader {

	protected NormaliserManager normaliserManager;
	protected Map<String, Object> properties = new HashMap<String, Object>();
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	ContentHandler contentHandler;
	LexicalHandler lexicalHandler;

	public abstract String getOutputFileExtension();

	/**
	 * @return Returns true if the normaliser will convert the file to an open format.
	 */
	public abstract boolean isConvertible();

	/**
	 * Return the version of Xena for this normaliser.
	 * @return
	 */
	public String getVersion() {
		return Xena.getVersion();
	}

	/**
	 * @return Returns the normaliserManager.
	 */
	public NormaliserManager getNormaliserManager() {
		return normaliserManager;
	}

	/**
	 * @param normaliserManager The new value to set normaliserManager to.
	 */
	public void setNormaliserManager(NormaliserManager normaliserManager) {
		this.normaliserManager = normaliserManager;
	}

	public void setEntityResolver(EntityResolver resolver) {
		// Nothing to do
	}

	public void setDTDHandler(DTDHandler handler) {
		// Nothing to do
	}

	/**
	 * Parse the input source and normalise it. This version of the method doesn't not migrate the method. 
	 * @param input The input source.
	 * @param results 
	 * @throws IOException
	 * @throws SAXException
	 */
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		parse(input, results, false);
	}

	public abstract void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException;

	public void parse(InputSource input) throws IOException, SAXException {
		parse(input, new NormaliserResults(), false);
	}

	public void parse(InputSource input, boolean migrateOnly) throws IOException, SAXException {
		parse(input, new NormaliserResults(), migrateOnly);
	}

	public void parse(String systemId) throws java.io.IOException, org.xml.sax.SAXException {
		parse(new InputSource(systemId), new NormaliserResults(), false);
	}

	public void parse(String systemId, NormaliserResults results) throws java.io.IOException, org.xml.sax.SAXException {
		parse(new InputSource(systemId), results, false);
	}

	public DTDHandler getDTDHandler() {
		throw new java.lang.UnsupportedOperationException("Method getDTDHandler() not yet implemented.");
	}

	public EntityResolver getEntityResolver() {
		throw new java.lang.UnsupportedOperationException("Method getEntityResolver() not yet implemented.");
	}

	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	public void setFeature(String name, boolean value) {
		// We are not going to let external code (eg XOM) change the way our normaliser works, so just log the attempt
		logger.finest("Attempted to set feature " + name + " to " + value + ". At present, this is ignored.");
	}

	public boolean getFeature(String name) {
		throw new java.lang.UnsupportedOperationException("Method getFeature() not yet implemented.");
	}

	public Object getProperty(String name) {
		Object rtn = properties.get(name);
		return rtn;
	}

	public org.xml.sax.ContentHandler getContentHandler() {
		return contentHandler;
	}

	public void setContentHandler(org.xml.sax.ContentHandler handler) {
		contentHandler = handler;
	}

	/**
	 * @return the lexicalHandler
	 */
	public LexicalHandler getLexicalHandler() {
		return lexicalHandler;
	}

	/**
	 * @param lexicalHandler the lexicalHandler to set
	 */
	public void setLexicalHandler(LexicalHandler lexicalHandler) {
		this.lexicalHandler = lexicalHandler;
	}

	public ErrorHandler getErrorHandler() {
		// Just return a default handler that does nothing
		return new DefaultHandler();
	}

	public void setErrorHandler(ErrorHandler handler) {
		// Nothing to do
	}

	/**
	 * Return a human readable name for this normaliser.
	 * @return String
	 */
	abstract public String getName();

	@Override
	public String toString() {
		return getName();
	}

	protected String generateChecksum(File file) throws IOException {
		return Checksum.getChecksum(TagNames.DEFAULT_CHECKSUM_ALGORITHM, file);
	}

	protected String generateChecksum(InputStream stream) throws IOException {
		return Checksum.getChecksum(TagNames.DEFAULT_CHECKSUM_ALGORITHM, stream);
	}

	/**
	 * Set the exported checksum
	 * @param checksum
	 */
	protected void setExportedChecksum(String checksum) {
		if (checksum == null) {
			checksum = "";
		}
		setProperty("http://xena/exported_digest", checksum);
	}

	/**
	 * Adds an exported checksum, for the cases when there is more then one exported file.
	 * Generates a comma separated list. If a checksum hasn't been set then it calls setExportedChecksum. 
	 * @param checksum
	 */
	protected void addExportedChecksum(String checksum) {
		String currentValue = (String) getProperty("http://xena/exported_digest");
		if ((currentValue == null) || (currentValue.equals(""))) {
			setExportedChecksum(checksum);
			return;
		}

		setProperty("http://xena/exported_digest", currentValue + ", " + checksum);
	}

	protected static String convertToHex(byte[] byteArray) {
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

	/**
	 * Set an exported checksum comment.This is required if a comment related to specifics as to how the exported checksum was generated, if some extra metadata/comments
	 * are required. 
	 * Such as if the export was generated using Linux line endings rather then Windows or Mac line endings. 
	 * @param comment
	 */
	protected void setExportedChecksumComment(String comment) {
		if (comment == null) {
			comment = "";
		}
		setProperty("http://xena/exported_digest_comment", comment);
	}

	/**
	 * This method will attempt to use the NormaliserManager to export this file and generate the Checksum. This should only be used as a last resort 
	 * as we need to export a non-finished Xena file. 
	 * This was created to fix the problem with files such as SVG's which are not binary normalised, so an export from there Xena-ified form is required to create a 
	 * valid  export checksum.
	 * @param stream The Xena file. 
	 * @return The exported checksum or null.
	 */
	protected String exportThenGenerateChecksum(XenaInputSource xis) {
		try {
			String outFileName = "out.tmp";
			File tmpfolder = File.createTempFile("exported", "cksum");
			tmpfolder.delete();
			tmpfolder.mkdir();

			try {
				normaliserManager.export(xis, tmpfolder, outFileName, true);
			} catch (SAXException e) {
				// This may fail as the Xena file proabably isn't a complete one yet, so we don't want to end to processing on this exception. 
				e.printStackTrace();
			} catch (XenaException e) {
				// This may fail as the Xena file proabably isn't a complete one yet, so we don't want to end to processing on this exception. 
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			File exportedFile = new File(tmpfolder, outFileName);
			if (exportedFile.exists()) {
				String checksum = generateChecksum(exportedFile);
				exportedFile.delete();
				tmpfolder.delete();
				return checksum;
			} else {
				FileUtils.deleteDirAndContents(tmpfolder);
				return null;
			}
		} catch (IOException ioex) {
			return null;
		}
	}
}
