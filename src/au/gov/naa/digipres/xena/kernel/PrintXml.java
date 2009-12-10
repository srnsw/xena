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

package au.gov.naa.digipres.xena.kernel;

import java.io.IOException;
import java.io.OutputStream;

import nu.xom.Document;
import nu.xom.Serializer;

/**
 * Standardise the storing of Xena XML files.
 */

public class PrintXml {
	static PrintXml single = new PrintXml();

	protected PrintXml() {
		// Nothing to do
	}

	public static PrintXml singleton() {
		return single;
	}

	public String getCharset() {
		return ENCODING;
	}

	public final String ENCODING = "UTF-8";

	public void printXml(Document doc, OutputStream os, boolean writeDeclaration) throws IOException {
		Serializer outputter = new Serializer(os, ENCODING);
		outputter.write(doc, writeDeclaration);
	}
}
