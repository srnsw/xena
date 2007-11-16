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

package au.gov.naa.digipres.xena.plugin.email;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser to guess TRIM .mbx files.
 *
 * @version 1.0
 */
public class TrimGuesser extends Guesser {
	static String FROM_TEXT = "From:";
	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public TrimGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(TrimFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);

		// if (source.getSystemId().startsWith("file:/") && source.getSystemId().endsWith("/")) {
		// File dir = null;
		// try {
		// dir = new File(new URI(source.getSystemId()));
		// } catch (URISyntaxException x) {
		// throw new IOException("Invalid URI: " + source.getSystemId());
		// }
		// File[] list = dir.listFiles(new FilenameFilter() {
		// public boolean accept(File dir, String name) {
		// return name.toLowerCase().endsWith(".mbx");
		// }
		// });
		// if (0 < list.length) {
		// guess.setExtensionMatch(true);
		// }
		// }

		if (source.getSystemId().toLowerCase().endsWith(".mbx")) {
			guess.setExtensionMatch(true);
		}
		return guess;
	}

	@Override
	public String getName() {
		return "TrimGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

}
