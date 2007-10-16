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

package au.gov.naa.digipres.xena.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jdom.input.SAXHandler;
import org.xml.sax.SAXException;

/**
 * Allows denormalisation using JDOM. This should be avoided because SAX is
 * more efficient and works with bigger files. However JDOM can be easier to
 * deal with.
 */
public abstract class AbstractJdomDeNormaliser extends SAXHandler implements TransformerHandler {
	protected Result result;

	// Constructors

	public AbstractJdomDeNormaliser() {
	}

	// Methods
	public abstract void denormalise(OutputStream outputStream) throws IOException;

	@Override
    public void endDocument() throws SAXException {
		StreamResult sr = (StreamResult) result;
		try {
			denormalise(sr.getOutputStream());
		} catch (IOException ex) {
			throw new SAXException(ex);
		}
	}

	public void setResult(Result result) throws IllegalArgumentException {
		this.result = result;
	}

	public void setSystemId(String systemID) {
	}

	public String getSystemId() {
		return null;
	}

	public Transformer getTransformer() {
		return null;
	}

}
