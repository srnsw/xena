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

/*
 * Created on 11/01/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.image.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.plugin.image.JpegGuesser;

public class JpegGuesserTester {

	public static void main(String[] argv) {

		JpegGuesser guesser = new JpegGuesser();
		XenaInputSource xis = null;

		List<String> fileNameList = new ArrayList<String>();

		fileNameList.add("d:\\test_data\\dino.jpg");
		fileNameList.add("d:\\test_data\\dino.jpg");
		fileNameList.add("d:\\test_data\\dino.jpg");
		fileNameList.add("d:\\test_data\\dino.jpg");

		for (Iterator iter = fileNameList.iterator(); iter.hasNext();) {
			String fileName = (String) iter.next();

			try {
				xis = new XenaInputSource(new File(fileName));
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(1);
			}

			try {
				Guess guess = guesser.guess(xis);

				System.out.println(guess.toString());

			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (XenaException xe) {
				xe.printStackTrace();
			}
		}

	}

}
