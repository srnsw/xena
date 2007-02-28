package au.gov.naa.digipres.xena.plugin.postscript.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Test Postscript normsaliser and viewer
 * 
 * @author Kamaj Jayakantha de Mel and Quang Phuc Tran(Eric)
 * 
 */

public class testPostScriptHarness {

	/**
	 * Input Postscript file is specified via open dialog is used as the input sources.
	 * Normaliser will guess the type and normalise the file. 
	 * Outpout file name followed by the guesses that xena provides
	 * 		
	 * @param args
	 * @throws XenaException, IOException
	 */
	public static void main(String[] args) throws XenaException, IOException {

		Xena xena = new Xena();

		// Create Open dialog and get the path of the input Postscript file
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.showOpenDialog(chooser);
		String fileName = chooser.getSelectedFile().getPath();

		System.out.println("loading plugins >>>> ");
		try{
		// Load the plugins named in the list
		xena.loadPlugins(new File("dist/PostScript.jar"));
		}
		catch(Exception e){
			System.out.println("Unable to load plugin");
		}
		System.out.println("Plugins loaded.");

		

		System.out.println("\n------------>>>>>>>>> STARTING TEST <<<<<<<<<<<-----------------\n");
		System.out.println(">>>>>>>>------- File Name: " + fileName	+ " -----<<<<<<<<\n");

		XenaInputSource xis = new XenaInputSource(new File(fileName));

		// Create the normalized file in destination
		File destDir = new File("c:/xena/TestFiles/destination");

		// Normalise a file using best guess.
		NormaliserResults results = xena.normalise(xis, destDir);
		String objName = "c:/xena/TestFiles/destination/"+ results.getOutputFileName().toString();
		
		
		System.out.println(" Most Likely Type : " + xena.getMostLikelyType(xis)+ "\n" + xena.getGuesses(xis));

		// Display normalise results
		if (results != null) {
			System.out.println(" Input normalised : " + results.isNormalised()+ "\n");

			// Display the source file name and normalized file name
			System.out.println(" Normalized File Name : "
					+ results.getOutputFileName().toString() + "\n");
		}
		System.out.println("-------------->>>>>>>>>> TEST ENDS <<<<<<<<<<-------------------");

		// Create the view factory
		NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(xena);

		// Create our frame
		JFrame frame = new JFrame("XenaTester View");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {

				System.exit(0);
			}
		});

		// Create our view file
		File viewFile = new File(objName);

		// Get our view
		JPanel view = null;

		try {
			view = novf.getView(viewFile, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Add it to our frame and display it!
		frame.setBounds(200, 250, 300, 200);
		frame.getContentPane().add(view);
		frame.pack();
		frame.setVisible(true);
	}

}
