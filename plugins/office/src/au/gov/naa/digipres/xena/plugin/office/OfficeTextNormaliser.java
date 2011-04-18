/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Justin Waddell
 * @author Jeff Stiff
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;

/*
 * * Class to produce a text AIP from office documents.
 * 
 * @author justin
 */
public class OfficeTextNormaliser extends AbstractTextNormaliser {

	@Override
	public String getOutputFileExtension() {
		// This normaliser outputs a text version of the input document, so output is a .txt file
		return "txt";
	}

	@Override
	public boolean isConvertable() {
		return false;
	}

	@Override
	public String getName() {
		return "Office Text Normaliser";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException {
		XenaInputSource xis = (XenaInputSource) input;
		Type type = xis.getType();

		/*
		 * This is slightly broken --> if the type is null, then we have a problem. At least this way there is some way
		 * of ensure type != null If the normaliser has been specified though, we really should have the type as not
		 * null!
		 */
		if (type == null) {
			GuesserManager gm = normaliserManager.getPluginManager().getGuesserManager();
			type = gm.mostLikelyType(xis);
			xis.setType(type);
		}

		// Verify that we are actually getting an office type input source.
		OfficeFileType officeType;
		if (type instanceof OfficeFileType) {
			officeType = (OfficeFileType) type;
		} else {
			throw new IOException("Invalid FileType - must be an OfficeFileType. To override, the type should be set manually.");
		}

		File output = OpenOfficeConverter.convertInput(input, officeType, results, normaliserManager, true);

		try {
			// Simply write the contents of the converter's output file to the content handler
			ContentHandler contentHandler = getContentHandler();
			Reader reader = new FileReader(output);
			char[] buffer = new char[10 * 1024];
			int charsRead = reader.read(buffer);
			while (charsRead > 0) {
				contentHandler.characters(buffer, 0, charsRead);
				charsRead = reader.read(buffer);
			}
		} finally {
			output.delete();
		}
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

}
