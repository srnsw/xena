package au.gov.naa.digipres.xena.plugin.psd.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

public class TestPsdHarness {

	public static void main(String[] args) throws XenaException, IOException {

		Xena xena = new Xena();
		String fileSource = "PSDtest1.psd";

		//load the plugins named in the list
		xena.loadPlugins(new File("dist/Psd.jar"));

		//SYSOUT
		System.out.println("Plugins loaded");
		
		/*
		 * fileSource is used as the input sources 
		 * normaliser will guess the type and normalise the file.
		 * 
		 * Outpout file name followed by the guesses that xena provides
		 */

		//SYSOUT
		System.out.println("\n------------>>>>>>>>> STARTING TEST <<<<<<<<<<<-----------------\n");
		System.out.println("    >>>>>>>>------- File Name: " + fileSource+ " -----<<<<<<<<\n");

		//  create a new XenaInputSource xis.
		XenaInputSource xis = new XenaInputSource(new File(	"c:/xena/TestFiles/source/" + fileSource));

		// create the normalized file in destination dir – d:\Examples\destination
		File destDir = new File("c:/xena/TestFiles/destination");

		// normalise a file using best guess.
		NormaliserResults results = xena.normalise(xis, destDir);
		String objName = "c:/xena/TestFiles/destination/"+ results.getOutputFileName().toString();

		System.out.println(" Most Likely Type : " + xena.getMostLikelyType(xis)	+ "\n" + xena.getGuesses(xis));

		//display results
		if (results != null) {
			System.out.println(" Input normalised : " + results.isNormalised()
					+ "\n");

			// display the source file name and normalized file name	 
			System.out.println(" Normalized File Name : "
					+ results.getOutputFileName().toString() + "\n");
		}
		System.out.println("-------------->>>>>>>>>> TEST ENDS <<<<<<<<<<-------------------");

		//create the view factory
		NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(xena);

		//create our frame
		JFrame frame = new JFrame("XenaTester View");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		//create our view file
		File viewFile = new File(objName);

		//get our view
		JPanel view = null;

		try {
			view = novf.getView(viewFile, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//add it to our frame and display it!
		frame.setBounds(200, 250, 300, 200);
		frame.getContentPane().add(view);
		frame.pack();
		frame.setVisible(true);
	}

}