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

package au.gov.naa.digipres.xena.plugin.multipage;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Normaliser to convert a number of files into a Xena multipage instance.
 *
 */
public class MultiPageNormaliser extends AbstractNormaliser {
	public final static String MULTIPAGE_PREFIX = "multipage";
	public final static String PAGE_TAG = "page";

	final static String URI = "http://preservation.naa.gov.au/multipage/1.0";

	@Override
	public String getName() {
		return "Multi-page";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results) throws SAXException, java.io.IOException {
		try {
			MultiInputSource mis = (MultiInputSource) input;
			File[] bfiles = new File[mis.getSystemIds().size()];
			int j = 0;

			for (String nm : mis.getSystemIds()) {
				File file = null;
				try {
					file = new File(new URI(nm));
				} catch (URISyntaxException ex) {
					throw new SAXException(ex);
				}
				bfiles[j++] = file;
			}

			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(URI, "multipage", MULTIPAGE_PREFIX + ":multipage", att);
			for (File file : bfiles) {
				if (file.isFile()) {
					XenaInputSource source = new XenaInputSource(file);
					Type subType = null;
					subType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(source);
					ch.startElement(URI, PAGE_TAG, MULTIPAGE_PREFIX + ":" + PAGE_TAG, att);
					AbstractNormaliser subnorm = null;
					try {
						subnorm = normaliserManager.lookup(subType);
					} catch (XenaException x) {
						throw new SAXException(x);
					}
					subnorm.setProperty("http://xena/log", getProperty("http://xena/log"));
					XenaInputSource xis = new XenaInputSource(file, subType);
					subnorm.setContentHandler(ch);

					AbstractMetaDataWrapper wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getWrapNormaliser();
					normaliserManager.parse(subnorm, xis, wrapper, results);

					ch.endElement(URI, PAGE_TAG, MULTIPAGE_PREFIX + ":" + PAGE_TAG);
				}
			}
			ch.endElement(URI, "multipage", MULTIPAGE_PREFIX + ":multipage");
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}
}
