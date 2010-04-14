package au.gov.naa.digipres.xena.plugin.psd.test;

import java.util.Iterator;
import java.util.Vector;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class PSDPluginLoadTester {

	public static void main(String[] argv) {
		
		Xena xena = new Xena();
		
		Vector<String> pluginList = new Vector<String>();
		pluginList.add("au/gov/naa/digipres/xena/plugin/psd");

		try {
			xena.loadPlugins(pluginList);

			System.out.println("Types");
			for (Object element : xena.getPluginManager().getTypeManager().allTypes()) {
				Type newType = (Type) element;
				System.out.println(newType.toString());
}
			System.out.println("----------------------------->>>><<<<<--------------------");

			System.out.println("Guessers...");
			for (Iterator iter = xena.getPluginManager().getGuesserManager().getGuessers().iterator(); 
				iter.hasNext();) {
					Guesser newPsdGuesser = (Guesser) iter.next();
					System.out.println(newPsdGuesser.getName());
			}
			System.out.println("---------------------------->>>><<<<<--------------------");

			/*System.out.println("Normalisers...");
			for (Iterator iter = xena.getPluginManager().getNormaliserManager().getAll().iterator(); 
				iter.hasNext();) {
					AbstractNormaliser normaliser = (AbstractNormaliser) iter.next();
					System.out.println(normaliser.getName());
			}
			System.out.println("---------------------------->>>><<<<<--------------------");
		*/	
			System.out.println("Meta data wrappers...");
	        for (Iterator iter = xena.getPluginManager().getMetaDataWrapperManager().getMetaDataWrapperNames().iterator(); iter.hasNext();) {
	            String metaDataWrapperName = (String)iter.next();
	            //at this stage, we dont know if it is a denormaliser or normaliser...
	                System.out.println(metaDataWrapperName);
	        }
	        
	        System.out.println("Active wrapper:");
	        System.out.println(xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getName());
	        System.out.println("---------------------------->>>><<<<<--------------------");
	  

		} catch (XenaException xe) {
			System.err.println("Unable to load plugins!");
			xe.printStackTrace();
		}
	}
}
