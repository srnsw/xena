/*
 * Created on 13/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.plaintext;

import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class PlainTextProperties extends PluginProperties
{
	private List<XenaProperty> properties;
	
	public PlainTextProperties()
	{
		
	}
	
	public void initialiseProperties()
	{
		properties = new ArrayList<XenaProperty>();
		
		// Tab size property
		XenaProperty tabSizeProperty = new XenaProperty("Tab Size",
		                                                "Number of spaces per tab character",
		                                                XenaProperty.PropertyType.INT_TYPE,
		                                                this.getName());
		this.getManager().loadProperty(tabSizeProperty);
		properties.add(tabSizeProperty);

		// Charset property
		// TODO: This probably should be a list
		XenaProperty charsetProperty = new XenaProperty("Charset",
		                                                "Default charset for export output",
		                                                XenaProperty.PropertyType.STRING_TYPE,
		                                                this.getName());
		
		this.getManager().loadProperty(charsetProperty);
		properties.add(charsetProperty);
		
	
	}

	public String getName()
	{
		return "PlainText";
	}

	public List<XenaProperty> getProperties()
	{
		return properties;
	}

}
