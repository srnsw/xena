package au.gov.naa.digipres.xena.kernel;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Standardise the storing of Xena XML files.
 */

public class PrintXml {
	static PrintXml single = new PrintXml();

	protected PrintXml() {
	}

	public static PrintXml singleton() {
		return single;
	}

	public void printXmlWithHeader(Element e, OutputStream out) throws IOException {
		// Need to create Document otherwise no XML declaration is output.
		Document doc = new Document(e);
		printXml(doc, out);
		doc.detachRootElement();
	}

	public String getCharset() {
		return ENCODING;
	}

	public final String ENCODING = "UTF-8";

	/**
	 * Don't put newlines or indentation in the stored XML. If you do that we
	 * can't tell any more what spaces or newlines are data and what are just
	 * pretty printing. This makes the XML display module nearly impossible
	 * because every time you load it adds yet another set of newlines and
	 * indentations. In short, it seems that nobody should ever store XML with
	 * pretty printing. Pretty printing is for display only.
	 * @param doc
	 * @param os
	 * @throws IOException
	 */
	public void printXml(Element doc, OutputStream os) throws IOException {
		XMLOutputter outputter = new XMLOutputter(getFormatter());
		OutputStreamWriter out = new OutputStreamWriter(os, ENCODING);
		outputter.output(doc, out);
	}

	protected Format getFormatter() {
		Format format = Format.getRawFormat();
		// The doco says this doesn't affect anything, but it does affect the
		format.setLineSeparator("\n");
		return format;
	}

	public void printXmlWithDeclaration(Element doc, OutputStream os) throws IOException {
		PrintWriter pw = new PrintWriter(os);
		pw.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		pw.close();
		XMLOutputter outputter = new XMLOutputter(getFormatter());
		OutputStreamWriter out = new OutputStreamWriter(os, ENCODING);
		outputter.output(doc, out);
	}

	public void printXml(Document doc, OutputStream os) throws IOException {
		XMLOutputter outputter = new XMLOutputter(getFormatter());
		OutputStreamWriter out = new OutputStreamWriter(os, ENCODING);
		outputter.output(doc, out);
	}
}
