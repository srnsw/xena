/**
 * This file is part of xena.
 * 
 * xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author Justin Waddell
 *
 */
public abstract class AbstractMetaDataUnwrapper extends XMLFilterImpl implements LexicalHandler {

	protected LexicalHandler outputLexicalHandler;

	/**
	 * @return the lexicalHandler
	 */
	public LexicalHandler getLexicalHandler() {
		return outputLexicalHandler;
	}

	/**
	 * @param lexicalHandler the lexicalHandler to set
	 */
	public void setLexicalHandler(LexicalHandler lexicalHandler) {
		outputLexicalHandler = lexicalHandler;
	}

	/*
	 ****************************
	 * LEXICAL HANDLER METHODS
	 ****************************
	 */

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		outputLexicalHandler.comment(ch, start, length);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
	public void endCDATA() throws SAXException {
		outputLexicalHandler.endCDATA();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
	public void endDTD() throws SAXException {
		outputLexicalHandler.endDTD();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
	public void endEntity(String name) throws SAXException {
		outputLexicalHandler.endEntity(name);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
	public void startCDATA() throws SAXException {
		outputLexicalHandler.startCDATA();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		outputLexicalHandler.startDTD(name, publicId, systemId);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
	public void startEntity(String name) throws SAXException {
		outputLexicalHandler.startEntity(name);
	}

}
