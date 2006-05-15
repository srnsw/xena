package au.gov.naa.digipres.xena.plugin.multipage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XmlList;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Normaliser to convert a number of files into a Xena multipage instance.
 *
 * @author Chris Bitmead
 */
public class MultiPageNormaliser extends AbstractNormaliser {
	final static String PREFIX = "multipage";

	final static String URI = "http://preservation.naa.gov.au/multipage/1.0";

	public String getName() {
		return "Multi-page";
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws SAXException, java.io.IOException {
		try {
			MultiInputSource mis = (MultiInputSource)input;
			File[] bfiles = new File[mis.getSystemIds().size()];
			Iterator it = mis.getSystemIds().iterator();
			int j = 0;
			while (it.hasNext()) {
				String nm = (String)it.next();
				File file = null;
				try {
					file = new File(new URI(nm));
				} catch (URISyntaxException ex) {
					throw new SAXException(ex);
				}
				bfiles[j++] = file;
			}
			XmlList newSelectedFiles = new XmlList();
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(URI, "multipage", PREFIX + ":multipage", att);
			for (int i = 0; i < bfiles.length; i++) {
				File file = bfiles[i];
				if (file.isFile()) {
					XenaInputSource source = new XenaInputSource(file);
					FileType subType = null;
					subType =  normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(source);
					ch.startElement(URI, "page", PREFIX + ":page", att);
					XMLReader subnorm = null;
					try {
						subnorm = (XMLReader)normaliserManager.lookup(subType);
					} catch (XenaException x) {
						throw new SAXException(x);
					}
					subnorm.setProperty("http://xena/log", getProperty("http://xena/log"));
					XenaInputSource xis = new XenaInputSource(file, subType);
					subnorm.setContentHandler(ch);

					AbstractMetaDataWrapper wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getWrapNormaliser();
                    normaliserManager.parse(subnorm, xis, wrapper);
					
//					subnorm.parse(xis);
					newSelectedFiles.add(file);
					ch.endElement(URI, "page", PREFIX + ":page");
				}
			}
			ch.endElement(URI, "multipage", PREFIX + ":multipage");
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}
}
