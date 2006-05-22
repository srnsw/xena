/*
 * Created on 17/08/2005
 * aak
 * 
 * ripped off from Dan Spasojevic's DPR code - DPRIcon
 * 
 * Makes sense to use a factory for the icons.
 * 
 */
package au.gov.naa.digipres.xena.kernel;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

public class IconFactory {

    protected static String dirName = "images/";
    
    protected static Map<String, ImageIcon> loadedIcons = new HashMap<String, ImageIcon>();
    
    private static Logger logger = Logger.getLogger(IconFactory.class.getName());
    
    public static void configureImageDirectory(String newDirName){
        IconFactory.dirName = newDirName;
        if (!IconFactory.dirName.endsWith("/")){
            IconFactory.dirName += "/";
        }
    }
    
    /**
     * Return the named icon using the class loader of this class. Do not store a reference to the icon.
     * @param iconName
     * @return
     */
    public static ImageIcon getIconByName(String iconName)
    {
    	return getIconByName(iconName, IconFactory.class.getClassLoader(), false);
    }
    
    /**
     * Return the named icon using the given class loader. Do not store a reference to the icon.
     * @param iconName
     * @param classLoader
     * @return
     */
    public static ImageIcon getIconByName(String iconName, ClassLoader classLoader)
    {
    	return getIconByName(iconName, classLoader, false);
    }
    
    /**
     * Return the named icon using the given class loader. If storeIcon is true, store a reference to the icon.
     * @param iconName
     * @param classLoader
     * @param storeIcon
     * @return
     */
    public static ImageIcon getIconByName(String iconName, ClassLoader classLoader, boolean storeIcon)
    {
    	
    	ImageIcon icon = new ImageIcon();
    	if (loadedIcons.containsKey(iconName))
    	{
    		icon = (ImageIcon) loadedIcons.get(iconName); 
    	}
    	else
    	{
	        URL iconURL = classLoader.getResource(iconName);
	        
	        if (iconURL != null)
	        {
	            icon = new ImageIcon(iconURL);
	            if (storeIcon)
	            {
	            	loadedIcons.put(iconName, icon);
	            }
	        } 
	        else 
	        {
	        	logger.finest("No icon found for icon path: [" + iconName+ "]");
	        }
    	}
        return icon;
    }
}
