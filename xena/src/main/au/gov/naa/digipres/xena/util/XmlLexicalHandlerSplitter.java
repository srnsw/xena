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

package au.gov.naa.digipres.xena.util;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Justin Waddell
 *
 */
public class XmlLexicalHandlerSplitter implements LexicalHandler {

	private Set<LexicalHandler> lexicalHandlers = new HashSet<LexicalHandler>();

	/**
	 * @param e
	 * @return
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean addLexicalHandler(LexicalHandler e) {
		return lexicalHandlers.add(e);
	}

	/**
	 * 
	 * @see java.util.Set#clear()
	 */
	public void clearHandlers() {
		lexicalHandlers.clear();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		for (LexicalHandler handler : lexicalHandlers) {
			handler.comment(ch, start, length);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
	public void endCDATA() throws SAXException {
		for (LexicalHandler handler : lexicalHandlers) {
			handler.endCDATA();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
	public void endDTD() throws SAXException {
		for (LexicalHandler handler : lexicalHandlers) {
			handler.endDTD();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
	public void endEntity(String name) throws SAXException {
		for (LexicalHandler handler : lexicalHandlers) {
			handler.endEntity(name);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
	public void startCDATA() throws SAXException {
		for (LexicalHandler handler : lexicalHandlers) {
			handler.startCDATA();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		for (LexicalHandler handler : lexicalHandlers) {
			handler.startDTD(name, publicId, systemId);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
	public void startEntity(String name) throws SAXException {
		for (LexicalHandler handler : lexicalHandlers) {
			handler.startEntity(name);
		}
	}

}
