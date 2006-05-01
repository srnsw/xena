package au.gov.naa.digipres.xena.demo.foo.test;


import java.util.Iterator;
import java.util.Vector;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

public class PluginLoadTester {


	public static void main(String[] argv) {
		Xena xena = new Xena();
		// our foo jar will already be on the class path, so load it by name...
		Vector<String> pluginList = new Vector<String>();
		pluginList.add("au/gov/naa/digipres/xena/demo/foo");
		
		try {
			xena.loadPlugins(pluginList);
		} catch (XenaException xe) {
			xe.printStackTrace();
			return;
		}
	
        	System.out.println("Types...");
        	for (Iterator iter = xena.getPluginManager().getTypeManager().allTypes().iterator(); iter.hasNext();) {
            		Type newType = (Type) iter.next();
            		System.out.println(newType.toString());
        	}
        	System.out.println("----------------------------->>>><<<<<--------------------");

		System.out.println("Guessers...");
        	for (Iterator iter = xena.getPluginManager().getGuesserManager().getGuessers().iterator(); iter.hasNext();) {
            		Guesser newGuesser = (Guesser) iter.next();
			System.out.println(newGuesser.getName());
        	}
        	System.out.println("---------------------------->>>><<<<<--------------------");

            System.out.println("Normalisers...");
            for (Iterator iter = xena.getPluginManager().getNormaliserManager().getNormalisers().iterator(); iter.hasNext();) {
                    Object object = iter.next();
                    if (object instanceof AbstractNormaliser) {
                        AbstractNormaliser normaliser = (AbstractNormaliser)object;
                        System.out.println(normaliser.getName());
                    }
            }
            System.out.println("---------------------------->>>><<<<<--------------------");
            
            System.out.println("De-Normalisers...");
            for (Iterator iter = xena.getPluginManager().getNormaliserManager().getDeNormalisers().iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof AbstractDeNormaliser) {
                    AbstractDeNormaliser normaliser = (AbstractDeNormaliser)object;
                    System.out.println(normaliser.getName());
                }
        }
        System.out.println("---------------------------->>>><<<<<--------------------");
	}

}