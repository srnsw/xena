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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.kernel.batchfilter;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Some kinds of input files gather in other input files. An example is TRIM
 * email directories, where the trim email file refers to other files in the
 * directory. This class can be overridden by plugins to filter out these
 * extra files from the regular batch processing so that they can be handled
 * specially by the appropriate plugin. This means that say, an MS-Word document
 * that belongs as an attachment to an email, will only be normalised once
 * within the email, instead of being normalised a second time as an individual
 * file.
 *
 * @see BatchFilterManager
 */
abstract public class BatchFilter {
	abstract public Map filter(Map files) throws XenaException;

	abstract public Map<XenaInputSource, NormaliserResults> getChildren(Collection<XenaInputSource> xisColl) throws XenaException;

	public abstract String getName();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	public static class FileAndType {
		File file;

		Type type;

		XMLReader normaliser;

		public FileAndType(File file, Type guess, XMLReader normaliser) {
			this.file = file;
			this.type = guess;
			this.normaliser = normaliser;
		}

		public File getFile() {
			return file;
		}

		public Type getType() {
			return type;
		}

		public XMLReader getNormaliser() {
			return normaliser;
		}

		/*
		 * public boolean equals(Object o) { if (o instanceof String) { return o.equals(file.getName()); } else if (o
		 * instanceof File) { return o.equals(o); } else if (o instanceof FileAndType) { return
		 * o.equals(((FileAndType)o).getFile()); } else { throw new RuntimeException("Unknown type:
		 * BatchFilter.FileAndType.equals"); } }
		 * 
		 * public int hashCode() { return file.getName().hashCode(); }
		 */
	}
}
