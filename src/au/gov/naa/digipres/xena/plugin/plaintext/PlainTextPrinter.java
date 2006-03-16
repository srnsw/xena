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

import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.TypePrinter;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Print out plaintext documents.
 *
 * @author Chris Bitmead
 */
public class PlainTextPrinter extends TypePrinter {
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

	public XenaFileType getType() throws XenaException {
		return typePrinterManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPlainTextFileType.class);
	}
}
