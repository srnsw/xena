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

/*
 * Created on 9/09/2005 andrek24
 * 
 * Example application to demonstrate normalisation using the Xena API.
 * 
 * Load plugins from a hard coded plugins folder, create a xena input source based on a hard coded file name, then use
 * Xena to normalise the file. Print the success or otherwise of the normalisation process. There is no error handling
 * with this demonstration tool.
 * 
 */
package au.gov.naa.digipres.xena.core.test;

import java.io.File;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

public class Harness {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Xena xena = new Xena();
		try {
			// load all the plugins we in the plugins directory
			xena.loadPlugins(new File("d://workspace//xena//dist//plugins//"));

			// create a new XenaInputSource xis based on d:/data/simple.txt file.
			XenaInputSource xis = new XenaInputSource(new File("D:/xena_data/source/simple.txt"));

			// create the destination dir � d:/xena_data/destination
			File destDir = new File("d:/xena_data/destination");

			// normalise a file using best guess.
			NormaliserResults results = xena.normalise(xis, destDir);

			if (results != null) {
				System.out.println("Input normalised: " + results.isNormalised());
				// ignore the rest of our results for the moment
			}
		} catch (Exception e) {
			// Just print a stack trace if any errors
			e.printStackTrace();
		}
	}
}
