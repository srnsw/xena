package au.gov.naa.digipres.xena.demo.foo.test;


import java.util.Iterator;
import java.util.Vector;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
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
            System.err.println("Unable to load plugins!");
            xe.printStackTrace();
        }
        
        System.out.println("Types");
        for (Iterator iter = xena.getPluginManager().getTypeManager().allTypes().iterator(); iter.hasNext();) {
            Type newType = (Type) iter.next();
            System.out.println(newType.toString());
        }
        System.out.println("----------------------------->>>><<<<<--------------------");
        
        System.out.println("Guessers...");
        for (Iterator iter = xena.getPluginManager().getGuesserManager().getGuessers().iterator(); iter.hasNext();) {
            Guesser foo = (Guesser) iter.next();
            System.out.println(foo.getName());
        }
        System.out.println("---------------------------->>>><<<<<--------------------");            
        
        
        System.out.println("Normalisers...");
        for (Iterator iter = xena.getPluginManager().getNormaliserManager().getAll().iterator(); iter.hasNext();) {
            Object normaliser = iter.next();
            //at this stage, we dont know if it is a denormaliser or normaliser...
            if (normaliser instanceof AbstractNormaliser) {
                System.out.println(((AbstractNormaliser)normaliser).getName());
            } else if (normaliser instanceof AbstractDeNormaliser) {
                System.out.println(((AbstractDeNormaliser)normaliser).getName());
            }
        }
        System.out.println("---------------------------->>>><<<<<--------------------");
    }
    
}