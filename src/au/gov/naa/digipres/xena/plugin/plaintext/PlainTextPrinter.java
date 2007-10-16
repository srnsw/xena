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

package au.gov.naa.digipres.xena.plugin.plaintext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.SimpleDoc;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.TypePrinter;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Print out plaintext documents.
 *
 */
public class PlainTextPrinter extends TypePrinter {
	@Override
    public Doc getDoc(File file) throws XenaException {
		DocFlavor myFormat = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		XenaPlainTextToPlainTextDeNormaliser den = new XenaPlainTextToPlainTextDeNormaliser();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(bos);
		den.setResult(new StreamResult(osw));
		try {
			typePrinterManager.getPluginManager().getNormaliserManager().unwrap(file.toURI().toASCIIString(), den);
			bos.write('\f');
			Doc myDoc = new SimpleDoc(bos.toByteArray(), myFormat, null);
			return myDoc;
		} catch (IOException x) {
			throw new XenaException(x);
		} catch (SAXException x) {
			throw new XenaException(x);
		} catch (ParserConfigurationException x) {
			throw new XenaException(x);
		}
	}

	@Override
    public XenaFileType getType() throws XenaException {
		return typePrinterManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPlainTextFileType.class);
	}
}
