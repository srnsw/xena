package au.gov.naa.digipres.xena.kernel;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.XMLReader;

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
 * @author Chris Bitmead
 */
abstract public class BatchFilter {
	abstract public Map filter(Map files) throws XenaException;
	
	abstract public Map<XenaInputSource, NormaliserResults> 
		getChildren(Collection<XenaInputSource> xisColl) throws XenaException;

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

		/*		public boolean equals(Object o) {
		   if (o instanceof String) {
			return o.equals(file.getName());
		   } else if (o instanceof File) {
			return o.equals(o);
		   } else if (o instanceof FileAndType) {
			return o.equals(((FileAndType)o).getFile());
		   } else {
			throw new RuntimeException("Unknown type: BatchFilter.FileAndType.equals");
		   }
		  }

		  public int hashCode() {
		   return file.getName().hashCode();
		  } */
	}
}
