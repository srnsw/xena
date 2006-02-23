/*
 * Created on 9/09/2005
 * andrek24
 * 
 * Example application to demonstrate normalisation using the Xena API.
 * 
 */
package au.gov.naa.digipres.xena.core.test;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

public class Harness {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Hello world!!!!");
        Xena xena = new Xena();


        // create a list of plugin names
        Vector<String> pluginList = new Vector<String>();
        pluginList.add("au/gov/naa/digipres/xena/plugin/basic");
        pluginList.add("au/gov/naa/digipres/xena/plugin/plaintext");
        pluginList.add("au/gov/naa/digipres/xena/plugin/naa");
        pluginList.add("au/gov/naa/digipres/xena/plugin/image");

        // load the plugins named in the list
        xena.loadPlugins(pluginList);
        // create a new XenaInputSource xis based on d:/data/simple.txt file.
        XenaInputSource xis = null;
        try {
            xis = new XenaInputSource(new File("D:/xena_data/source/simple.txt"));
        } catch (IOException ie) {
            System.out.println("Unable to create XenaInputSource. Exiting.");
            xis = null;
            ie.printStackTrace();
            return;
        }

        // create the destination dir – d:/xena_data/destination
        File destDir = null;
        destDir = new File("d:/xena_data/destination");
        if (!destDir.mkdir()){
            if (destDir.exists() && destDir.isDirectory() ){
                System.out.println("Using existing destination folder.");
            } else {
                System.out.println("Error creating destination folder. Exiting.");
                return;
            }
        }
        
        // create a normalise result object.
        NormaliserResults results = null;
        // normalise a file using best guess.
        try {
            results = xena.normalise(xis, destDir);
        } catch (XenaException xe) {
            //show reason for failure.
            xe.printStackTrace(System.out);
        }
        if (results != null) {
            System.out.println("Input normalised: " + results.isNormalised());
            //ignore the rest of our results for the moment
        }
        
        // get a specific normaliser - binary
        AbstractNormaliser binaryNormaliser = null;
        try {
            binaryNormaliser = xena.getNormaliser("Binary");
        } catch (XenaException xe) {
            //show reason for failure.
            xe.printStackTrace(System.out);
        }
        if (binaryNormaliser != null) {
            // normalise a file using this normaliser.
            try {
                results = xena.normalise(xis, binaryNormaliser, destDir);
            } 
            catch (XenaException xe) {
                xe.printStackTrace(System.out);
            }
            System.out.println("Input normalised: " + results.isNormalised());
            //ignore the rest of our results for the moment :)
            
            // ALL DONE!
        }
    }
}
    

