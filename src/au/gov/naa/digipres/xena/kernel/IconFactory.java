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

import javax.swing.ImageIcon;

public class IconFactory {

    protected static String dirName = "images/";
    
    protected static Map<String, ImageIcon> loadedIcons = new HashMap<String, ImageIcon>();
    
    public static void configureImageDirectory(String newDirName){
        IconFactory.dirName = newDirName;
        if (!IconFactory.dirName.endsWith("/")){
            IconFactory.dirName += "/";
        }
    }
    
    public static ImageIcon getIconByName(String iconName){
    	ImageIcon icon = new ImageIcon();
    	if (loadedIcons.containsKey(iconName))
    	{
    		icon = (ImageIcon) loadedIcons.get(iconName); 
    	}
    	else
    	{
	        URL iconURL = ClassLoader.getSystemResource(iconName);
	        
	        if (iconURL == null){
                iconURL = ClassLoader.getSystemResource(dirName + iconName);
            }
            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(iconName);
            }
            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(dirName + iconName);
            }
	        
	        if (iconURL != null)
	        {
	            icon = new ImageIcon(iconURL);
	            loadedIcons.put(iconName, icon);
	        } 
	        else 
	        {
	        	//sysout
	            System.out.println("No icon found for icon path: [" + iconName+ "]");
	        }
    	}
        return icon;
    }
}
