/*
 * Created on 6/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel;

import java.io.File;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

public class LegacyXenaCode {

    public LegacyXenaCode() {
    }
    
    

    /**
     * Return one of Xena's main working directories. Which one depends on the
     * argument passed.
     * 
     * @param name
     *            preference name
     * @return File the directory
     */
    public static File getBaseDirectory(String name) throws XenaException {
        final JarPreferences prefs = (JarPreferences) JarPreferences
                .userNodeForPackage(NormaliserManager.class);
        String dirS = prefs.get(name, null);
        if (dirS == null || dirS.equals("")) {
            return null;
        }
        File file = new File(dirS);
        if (!file.isDirectory()) {
            throw new XenaException("Directory is invalid: " + file + " for: "
                    + name);
        }
        return file;
    }
    
    
    
    

}
