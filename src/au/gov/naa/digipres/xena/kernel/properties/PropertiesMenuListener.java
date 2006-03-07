/*
 * Created on 6/03/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PropertiesMenuListener implements ActionListener
{
	private PluginProperties pluginProp;
	private Window parent;
	
	public PropertiesMenuListener(Window parent, PluginProperties pluginProp)
	{
		this.pluginProp = pluginProp;
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e)
	{
		PropertiesDialog dialog = 
			pluginProp.getPropertiesDialog(parent);
		dialog.pack();
		dialog.setLocation(parent.getX() + 50, parent.getY() + 50);
		dialog.setVisible(true);
		
		// Have finished with dialog
		dialog.dispose();
	}
}
