/*
 * Created on 9/09/2005
 * andrek24
 * 
 * Example application to demonstrate normalisation using the Xena API.
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
            
            // load the plugins named in the list
            xena.loadPlugins(new File("d://workspace//xena//dist//plugins//plaintext.jar"));
            
            // create a new XenaInputSource xis based on d:/data/simple.txt file.
            XenaInputSource xis = null;
            xis = new XenaInputSource(new File("D:/xena_data/source/simple.txt"));
            
            // create the destination dir – d:/xena_data/destination
            File destDir = new File("d:/xena_data/destination");
            
            // normalise a file using best guess.
            NormaliserResults results = xena.normalise(xis, destDir);
            
            if (results != null) {
                System.out.println("Input normalised: " + results.isNormalised());
                //ignore the rest of our results for the moment
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    

