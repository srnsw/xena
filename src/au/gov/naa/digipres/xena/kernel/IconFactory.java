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
    
    protected static Map loadedIcons = new HashMap();
    
    public static void configureImageDirectory(String newDirName){
        IconFactory.dirName = newDirName;
        if (!IconFactory.dirName.endsWith("/")){
            IconFactory.dirName += "/";
        }
    }
    
    public static ImageIcon getIconByName(String iconName){
        //notout
        //System.out.println("trying to get icon: "+iconName);
        //System.out.println("Loading icon in xena kernel:" + iconName);
        ImageIcon icon = (ImageIcon) loadedIcons.get(iconName);  
        URL iconURL = null;
        if (icon == null){
            iconURL = ClassLoader.getSystemResource(iconName);
            if (iconURL == null){
                iconURL = ClassLoader.getSystemResource(dirName + iconName);
            }
            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(iconName);
            }
            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(dirName + iconName);
            }
        }
        
        if (iconURL != null){
            icon = new ImageIcon(iconURL);
            //notout
            //System.out.println("Icon found and stored.");
            loadedIcons.put(iconName, icon);
        } else {
            System.out.println("No icon found for URL: [" + iconURL+ "]");
            icon = new  ImageIcon();
            //throw new NullPointerException();
        }
        return icon;
    }
    
    /*
    public static ImageIcon getIconByName(String iconName, Class theClassUsingIcon) {
        ImageIcon icon = (ImageIcon) loadedIcons.get(iconName);
        URL iconURL = null;
        
        if (icon == null){
            iconURL = ClassLoader.getSystemResource(iconName);
            if (iconURL == null){
                iconURL = ClassLoader.getSystemResource(dirName + iconName);
            }
            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(iconName);
            }
            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(dirName + iconName);
            }
        }
        return icon;
    }
    */
    
}
