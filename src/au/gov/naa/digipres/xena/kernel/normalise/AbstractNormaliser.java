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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.core.Xena;

/**
 * Normalisers may find it convenient to use this abstract class.
 */
abstract public class AbstractNormaliser implements XMLReader {

	protected NormaliserManager normaliserManager;

	protected Map properties = new HashMap();

	org.xml.sax.ContentHandler contentHandler;

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

	public org.xml.sax.ContentHandler getContentHandler() {
		return contentHandler;
	}

	public void setEntityResolver(EntityResolver resolver) {
	}

	public void setDTDHandler(DTDHandler handler) {
	}

	public abstract void parse(InputSource input, NormaliserResults results) throws IOException, SAXException;

	public void parse(InputSource input) throws IOException, SAXException {
		parse(input, new NormaliserResults());
	}

	public void parse(String systemId) throws java.io.IOException, org.xml.sax.SAXException {
		parse(new InputSource(systemId), new NormaliserResults());
	}

	public void parse(String systemId, NormaliserResults results) throws java.io.IOException, org.xml.sax.SAXException {
		parse(new InputSource(systemId), results);
	}

	public ErrorHandler getErrorHandler() {
		throw new java.lang.UnsupportedOperationException("Method getErrorHandler() not yet implemented.");
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

	public void setFeature(String name, boolean value) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
		throw new java.lang.UnsupportedOperationException("Method setFeature() not yet implemented.");
	}

	public boolean getFeature(String name) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
		throw new java.lang.UnsupportedOperationException("Method getFeature() not yet implemented.");
	}

	public Object getProperty(String name) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
		Object rtn = properties.get(name);
		return rtn;
	}

	public void setContentHandler(org.xml.sax.ContentHandler handler) {
		contentHandler = handler;
	}

	public void setErrorHandler(ErrorHandler handler) {
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
}
