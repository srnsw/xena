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

package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * 
 * AbstractDeNormaliser is an empty implementation of TransformerHandler
 * that is able to be overridden to De-Normalise Xena files. It contains a
 * several denoramliser specific abstract methods that must be implemented
 * by any new denormaliser.
 * 
 * 
 * 
 */
public abstract class AbstractDeNormaliser implements TransformerHandler {

	protected StreamResult streamResult;
	protected Result result;
	protected NormaliserManager normaliserManager;
	protected File outputDirectory;
	protected String outputFilename;
	protected File sourceDirectory;

	/**
	 * Return the file extension which should be used for the file exported by this denormaliser.
	 * This default method just returns the extension associated with the type associated with this
	 * denormaliser, but concrete classes may need to determine the extension from the file being
	 * denormalised, as some denormalisers can produce multiple file types (eg an office normalised
	 * file could be a word processor file, a spreadsheet or a presentation).
	 * 
	 * @return output file extension
	 */
	public String getOutputFileExtension(XenaInputSource xis) throws XenaException {
		return normaliserManager.getOutputType(this.getClass()).fileExtension();
	}

	public void setResult(Result result) {
		if (result instanceof StreamResult) {
			this.result = result;
			this.streamResult = (StreamResult) result;
		}
	}

	/**
	 * AAK This method is called from normaliserManager to set the Stream Result
	 * for this abstract denormaliser. This seems to break good object oriented
	 * coding, since StreamResult extends Result, however, the NormaliserManager
	 * export method requires that there be a stream result rather than just a
	 * result. As such, the abstract denormaliser
	 * 
	 * @param streamResult
	 */
	public void setStreamResult(StreamResult streamResult) {
		this.streamResult = streamResult;
		this.result = streamResult;
	}

	public StreamResult getStreamResult() {
		return streamResult;
	}

	/**
	 * Return a human readable name for this normaliser.
	 * 
	 * @return String
	 */
	abstract public String getName();

	@Override
    public String toString() {
		return getName();
	}

	/**
	 * @return Returns the normaliserManager.
	 */
	public NormaliserManager getNormaliserManager() {
		return normaliserManager;
	}

	/**
	 * @param normaliserManager
	 *            The new value to set normaliserManager to.
	 */
	public void setNormaliserManager(NormaliserManager normaliserManager) {
		this.normaliserManager = normaliserManager;
	}

	public void setSystemId(String systemID) {
	}

	public String getSystemId() {
		return null;
	}

	public Transformer getTransformer() {
		return null;
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}

	public void processingInstruction(String target, String data) throws SAXException {
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void startDTD(String name, String publicId, String systemId) throws SAXException {
	}

	public void endDTD() throws SAXException {
	}

	public void startEntity(String name) throws SAXException {
	}

	public void endEntity(String name) throws SAXException {
	}

	public void startCDATA() throws SAXException {
	}

	public void endCDATA() throws SAXException {
	}

	public void comment(char[] ch, int start, int length) throws SAXException {
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
	}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
	}

	/**
	 * @return the outputDirectory
	 */
	public File getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * @param outputDirectory the outputDirectory to set
	 */
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/**
	 * @return the outputFilename
	 */
	public String getOutputFilename() {
		return outputFilename;
	}

	/**
	 * @param outputFilename the outputFilename to set
	 */
	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}

	/**
	 * @return the sourceDirectory
	 */
	public File getSourceDirectory() {
		return sourceDirectory;
	}

	/**
	 * @param sourceDirectory the sourceDirectory to set
	 */
	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

}
