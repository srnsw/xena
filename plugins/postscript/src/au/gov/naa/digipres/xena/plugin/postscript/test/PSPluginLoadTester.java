package au.gov.naa.digipres.xena.plugin.postscript.test;

import java.util.Iterator;
import java.util.Vector;
import java.io.File;
import java.io.IOException;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * @author Kamaj Jayakantha de Mel 
 */

public class PSPluginLoadTester {

	public static void main(String[] argv) {

		Xena xena = new Xena();

		Vector<String> pluginList = new Vector<String>();
		File pluginDir = new File("dist");
		//pluginList.add("au/gov/naa/digipres/xena/plugin/postscript");
		//pluginList.add("/home/matt/workspace/postscript/dist/postscript.jar");
		
		try {
			//xena.loadPlugins(pluginList);
			try {
				xena.loadPlugins(pluginDir);
				}catch (IOException ioe) {
					throw new XenaException(ioe);
				}
			
			System.out.println("Types");
			for (Object element : xena.getPluginManager().getTypeManager().allTypes()) {
				Type newType = (Type) element;
				System.out.println(newType.toString());
			}

			System.out.println("----------------------------->>>><<<<<--------------------");
			System.out.println("Guessers...");
			for (Iterator iter = xena.getPluginManager().getGuesserManager().getGuessers().iterator(); iter.hasNext();) {
				Guesser newPsdGuesser = (Guesser) iter.next();
				System.out.println(newPsdGuesser.getName());
			}

			System.out.println("---------------------------->>>><<<<<--------------------");
			System.out.println("Viewer...");
			for (Iterator iter = xena.getPluginManager().getViewManager().getAllViews().iterator(); iter.hasNext();) {
				XenaView viewer = (XenaView) iter.next();
				System.out.println(viewer.getViewName());
			}
			
			System.out.println("---------------------------->>>><<<<<--------------------");
			System.out.println("Normalisers...");
			for (Iterator iter = xena.getPluginManager().getNormaliserManager().getAll().iterator(); iter.hasNext();) {
				
				Object norm = iter.next();
				try {
					AbstractNormaliser normaliser = (AbstractNormaliser) norm;
					System.out.println(normaliser.getName());
					
				} catch (ClassCastException ex) {
					AbstractDeNormaliser normaliser = (AbstractDeNormaliser) norm;
					System.out.println(normaliser.getName());
				}
				
			}
			
			System.out.println("---------------------------->>>><<<<<--------------------");
			System.out.println("Meta data wrappers...");
			for (Iterator iter = xena.getPluginManager().getMetaDataWrapperManager().getMetaDataWrapperNames().iterator(); iter.hasNext();) {
				String metaDataWrapperName = (String) iter.next();
				System.out.println(metaDataWrapperName);
			}

			System.out.println("Active wrapper:");
			System.out.println(xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getName());
			System.out.println("---------------------------->>>><<<<<--------------------");

		}
		catch (XenaException xe) {
			System.err.println("Unable to load plugins!");
			xe.printStackTrace();
		}
	}
}
