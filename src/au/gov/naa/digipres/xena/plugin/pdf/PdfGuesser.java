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

package au.gov.naa.digipres.xena.plugin.pdf;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser to guess PDF files.
 *
 */
public class PdfGuesser extends Guesser {
	static byte[] pdfmagic = {'%', 'P', 'D', 'F'};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public PdfGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(PdfFileType.class);
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess rtn = new Guess(type);
		String type = source.getMimeType();
		byte[] first = new byte[pdfmagic.length];

		source.getByteStream().read(first);
		String id = source.getSystemId().toLowerCase();
		if (type != null && type.equals("application/pdf")) {
			rtn.setMimeMatch(true);
		}
		if (id.endsWith(".pdf")) {
			rtn.setExtensionMatch(true);
		}
		if (GuesserUtils.compareByteArrays(first, pdfmagic)) {
			rtn.setMagicNumber(true);
			rtn.setDataMatch(true);
		} else {
			rtn.setDataMatch(false);
			rtn.setPossible(false);
		}
		return rtn;
	}

	@Override
    public String getName() {
		return "PDFGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setDataMatch(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

}
