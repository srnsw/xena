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
    
    public static ImageIcon getIconByName(String iconName)
    {
    	return getIconByName(iconName, IconFactory.class.getClassLoader());
    }
    
    public static ImageIcon getIconByName(String iconName, ClassLoader classLoader)
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
	            loadedIcons.put(iconName, icon);
	        } 
	        else 
	        {
	        	logger.finest("No icon found for icon path: [" + iconName+ "]");
	        }
    	}
        return icon;
    }
}
