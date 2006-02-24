/*
 * Created on 6/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.html;

import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class IconFactory {

    protected static String dirName = "images/";

    protected static HashMap loadedIcons = new HashMap();

    protected static IconFactory theFactory = new IconFactory();

    private IconFactory() {

    }

    public static void configureImageDirectory(String newDirName) {
        IconFactory.dirName = newDirName;
        if (!IconFactory.dirName.endsWith("/")) {
            IconFactory.dirName += "/";
        }
    }

    public static ImageIcon getIconByName(String iconName) {
        //sysout
        //System.out.println("HTML plugin icon factory, trying to load:" + dirName + iconName);
        ImageIcon icon = (ImageIcon) loadedIcons.get(iconName);
        if (icon == null) {
            URL iconURL = ClassLoader.getSystemResource(iconName);
            if (iconURL == null) {
                iconURL = ClassLoader.getSystemResource(dirName + iconName);
            }

            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(dirName + iconName);
            }

            if (iconURL == null) {
                iconURL = IconFactory.class.getResource(iconName);
            }

            if (iconURL != null) {
                icon = new ImageIcon(iconURL);
                loadedIcons.put(iconName, icon);
            } else {
                System.err.println("Could not find icon! (Icon name:" + iconName + ")");
                icon = new ImageIcon();
                
            }
        }
        return icon;
    }
}
