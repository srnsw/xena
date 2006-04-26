package au.gov.naa.digipres.xena.kernel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;

import au.gov.naa.digipres.xena.javatools.JarPreferences;

/**
 * A class to handle a plugin entry.
 * TODO: fix this description.
 * 
 * @author Andy
 * @create sep 2005
 */
class PluginEntry {

    private final PluginManager manager;
    String name;
    boolean loaded;  //TODO: IS THIS DEPRECATED? (loaded attribute of plugin entry)
    ArrayList dependancyList;
    
    
    public PluginEntry(PluginManager manager, String name)  throws XenaException {
        this.manager = manager;
        this.name = name;
        loaded = false;
        dependancyList = new ArrayList();
        
        //okay, time to get our dependencies from our jar file. this also serves to see 
        //if our plugin entry can be created.
        JarPreferences root = (JarPreferences)JarPreferences.userRoot();
        try {
            // Check if the preferences file exists
            if (!root.jarNodeExists(name, this.manager.getDeserClassLoader())) {
                throw new XenaException("Plugin: " + name + " does not contain properties");
            }
        } catch (BackingStoreException ex) {
            throw new XenaException(ex);
        }
        JarPreferences preferences = (JarPreferences)root.node(name, this.manager.getDeserClassLoader());
        dependancyList = (ArrayList)preferences.getList("dependancies", dependancyList);
        
    }

    
    public String toString(){
        StringBuffer returnBuffer = new StringBuffer("");
        returnBuffer.append("Name:" + name);
        returnBuffer.append(System.getProperty("line.separator"));
        returnBuffer.append("laoded:" + loaded);
        returnBuffer.append(System.getProperty("line.separator"));
        returnBuffer.append("dependancy list:");
        returnBuffer.append(System.getProperty("line.separator"));
        if (dependancyList.size() == 0) {
            returnBuffer.append("EMTPY");
            returnBuffer.append(System.getProperty("line.separator"));
        } else {
            for (Iterator iter = dependancyList.iterator(); iter.hasNext();){
                returnBuffer.append(iter.next().toString());
                returnBuffer.append(System.getProperty("line.separator"));
            }
        }
        return new String(returnBuffer);
    }
    
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the loaded.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * @param loaded The loaded to set.
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * @return Returns the dependancyList.
     */
    public ArrayList getDependancyList() {
        return dependancyList;
    }
}