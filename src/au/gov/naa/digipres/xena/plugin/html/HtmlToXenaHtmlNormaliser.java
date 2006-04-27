package au.gov.naa.digipres.xena.plugin.html;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.tidy.Tidy;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.javatools.ClassName;
import au.gov.naa.digipres.xena.kernel.ByteArrayInputSource;
import au.gov.naa.digipres.xena.kernel.PrintXml;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.util.AbstractJdomNormaliser;

/**
 * Normaliser to convert HTML files into XHTML files. We rely on a couple of
 * external libraries configure to work together in the way that seems to
 * have proven most useful - TAGSOUP and JTIDY. TAGSOUP is a simple and well-
 * written HTML parser that does a good job of creating matching tags and so on.
 * But it falls down in not addressing the intricacies of XHTML and conversion.
 * JTIDY is a complex and kludgy tool to convert HTML into XHTML. However it
 * tends to be buggy and has lots of tricky edge conditions.
 *
 * The fact is, converting HTML to XHMTL in a way that would allow it to continue
 * to render as it did originally is a very difficult if not impossible mission.
 * It may never be perfect, but if we spent a lot of time on it it may be made
 * a lot better than it is now.
 *
 * @author Chris Bitmead
 */
public class HtmlToXenaHtmlNormaliser extends AbstractJdomNormaliser {
	public HtmlToXenaHtmlNormaliser() {
	}

	public String getName() {
		return "HTML";
	}

	public Element normalise(InputSource input) throws IOException, SAXException {
		Element rtn = null;
		try {
			//rtn = normaliseTagSoupPlusJTidy(input);
//			rtn = normaliseJTidy(input);
			rtn = normaliseTagSoup(input);
		} catch (TransformerConfigurationException x2) {
			throw new SAXException(x2);
		} catch (XenaException x2) {
			throw new SAXException(x2);
		} catch (JDOMException x2) {
			throw new SAXException(x2);
		}
		List<Element> meta = new ArrayList<Element>();
		findTag(rtn, "meta", meta);
		Iterator it = meta.iterator();
		while (it.hasNext()) {
			Element el = (Element)it.next();
			String val = el.getAttributeValue("content");
			if (val != null) {
				int con = val.toLowerCase().indexOf("charset=");
				if (0 < con) {
					String nval = val.substring(0, con) + "charset=" + PrintXml.singleton().ENCODING;
					el.setAttribute("content", nval);
				}
			}
		}
		return rtn;
	}

	void findTag(Element root, String name, List<Element> result) {
		if (root.getName().equals(name)) {
			result.add(root);
		}
		Iterator it = root.getChildren().iterator();
		while (it.hasNext()) {
			Element el = (Element)it.next();
			findTag(el, name, result);
		}
	}

	public Element normaliseTagSoupPlusJTidy(final InputSource input) throws IOException, JDOMException, XenaException, SAXException,
		TransformerConfigurationException {
		XMLReader r = new org.ccil.cowan.tagsoup.Parser();
		String ignoreBogonsFeature = "http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons";
		r.setFeature(ignoreBogonsFeature, true);
		SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
		TransformerHandler writer = null;
		writer = tf.newTransformerHandler();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(out);
		writer.setResult(streamResult);
		r.setContentHandler(writer);
		r.parse(input);
		byte[] bytes = out.toByteArray();
		int i;
		for (i = 0; i < bytes.length && bytes[i] != '>'; i++) {
			// Nothing. JTidy can't cope with <?xml header
		}
		if (bytes[i] == '>') {
			i++;
		}
		XenaInputSource xis = new ByteArrayInputSource(out.toByteArray(), i, bytes.length - i, ((XenaInputSource)input).getType(),
													   ((XenaInputSource)input).getMimeType(), "UTF-8");
		return normaliseJTidy(xis);
	}

	public Element normaliseTagSoup(final InputSource input) throws IOException, JDOMException, XenaException, SAXException,
		TransformerConfigurationException {
		XMLReader r = new org.ccil.cowan.tagsoup.Parser();
		String ignoreBogonsFeature = "http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons";
		r.setFeature(ignoreBogonsFeature, true);
		SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
		TransformerHandler writer = null;
		writer = tf.newTransformerHandler();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(out);
		writer.setResult(streamResult);
		r.setContentHandler(writer);
		r.parse(input);
		byte[] bytes = out.toByteArray();
		int i;
		for (i = 0; i < bytes.length && bytes[i] != '>'; i++) {
			// Nothing. JTidy can't cope with <?xml header
		}
		if (bytes[i] == '>') {
			i++;
		}
		SAXBuilder sax = new SAXBuilder();
		sax.setValidation(false);
		sax.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) {
				int ind = systemId.lastIndexOf('/');
				if (0 < ind) {
					systemId = systemId.substring(ind + 1);
				}
				return new InputSource(normaliserManager.getPluginManager().getClassLoader().getResourceAsStream(
					ClassName.joinPath(ClassName.classToPath(ClassName.packageComponent(getClass().getName())), systemId)));
			}
		});
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Reader reader = new InputStreamReader(in, "UTF-8");
		return sax.build(reader).detachRootElement();
	}

	public static Tidy getTidy() {
		Tidy tidy = new Tidy();
		tidy.setXmlSpace(true);
		tidy.setTidyMark(false);
		tidy.setQuiet(false);
		tidy.setWraplen(0);
		tidy.setDropEmptyParas(false);
//		tidy.setXmlOut(true);
		tidy.setXHTML(true);
		tidy.setShowWarnings(false);
		tidy.setNumEntities(false);
//		tidy.setQuoteNbsp(false);

		return tidy;
	}

	/**
	 * I'm not using this function anymore because JTidy doesn't seem very
	 * intelligent in closing tags. e.g. <table><tr></tr><a href="b">AAAA</a>
	 * becomes.. <a href="b"></a>AAAA<table><tr></tr></table>.
	 * So now I always use tagsoup first, and jtidy later.
	 */
	private Element normaliseJTidy(InputSource input) throws IOException, JDOMException, XenaException {
		Tidy tidy = getTidy();
		tidy.setJavaCharset(input.getEncoding());
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		tidy.setErrout(new PrintWriter(err, true));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		tidy.parse(input.getByteStream(), out);

		if (0 < tidy.getParseErrors()) {
			throw new XenaException(new String(err.toByteArray()));
		}
		SAXBuilder sax = new SAXBuilder();
		sax.setValidation(false);
		sax.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) {
				int ind = systemId.lastIndexOf('/');
				if (0 < ind) {
					systemId = systemId.substring(ind + 1);
				}
				return new InputSource(normaliserManager.getPluginManager().getClassLoader().getResourceAsStream(
					
                        
                        ClassName.joinPath(ClassName.classToPath(ClassName.packageComponent(getClass().getName())), systemId)));
			}
		});
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Reader reader = new InputStreamReader(in, "UTF-8");
		return sax.build(reader).detachRootElement();
	}
}
