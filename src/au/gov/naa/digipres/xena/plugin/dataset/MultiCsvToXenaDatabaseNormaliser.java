package au.gov.naa.digipres.xena.plugin.dataset;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XmlList;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Convert a collection of CSV files into a Xena database instance.
 *
 * @author Chris Bitmead
 */
public class MultiCsvToXenaDatabaseNormaliser extends AbstractNormaliser {
	XmlList normalisers = new XmlList();

	public String getName() {
		return "Multi CSV";
	}

	public CsvToXenaDatasetNormaliser getNormaliser(int i) {
		CsvToXenaDatasetNormaliser normaliser = null;
		if (i < normalisers.size()) {
			normaliser = (CsvToXenaDatasetNormaliser)normalisers.get(i);
		} else {
			normaliser = (CsvToXenaDatasetNormaliser)normaliserManager.lookupByClass(CsvToXenaDatasetNormaliser.class);
			normalisers.add(i, normaliser);
		}
		return normaliser;
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws java.io.IOException, org.xml.sax.SAXException {
		try {
			AttributesImpl att = new AttributesImpl();
			ContentHandler ch = getContentHandler();
			ch.startElement(MultiDatasetToXenaDatabaseNormaliser.URI, "database",
							MultiDatasetToXenaDatabaseNormaliser.PREFIX + ":" + "database", att);
			MultiInputSource minput = (MultiInputSource)input;
			for (int i = 0; i < minput.size(); i++) {
				XMLReader normaliser = getNormaliser(i);
				normaliser.setContentHandler(ch);
				normaliser.parse(new XenaInputSource(minput.getSystemId(i), normaliserManager.getPluginManager().getTypeManager().lookup(CsvFileType.class)));
			}
			ch.endElement(MultiDatasetToXenaDatabaseNormaliser.URI, "database",
						  MultiDatasetToXenaDatabaseNormaliser.PREFIX + ":" + "database");
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}
}
