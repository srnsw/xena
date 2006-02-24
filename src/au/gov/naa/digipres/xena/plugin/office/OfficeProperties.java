/*
 * Created on 14/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class OfficeProperties extends PluginProperties
{
	public static final String OFFICE_PLUGIN_NAME = "Office";
	public static final String OOO_DIR_PROP_NAME = "Office Location";
	public static final String OOO_SLEEP_PROP_NAME = "Office Startup Sleep Time";
	
	private static final String OOO_SLEEP_DEFAULT_VALUE = "14000";
	
	private List<XenaProperty> properties;

	public OfficeProperties()
	{
		
	}

	@Override
	public String getName()
	{
		return OFFICE_PLUGIN_NAME;
	}

	@Override
	public List<XenaProperty> getProperties()
	{
		return properties;
	}

	@Override
	public void initialiseProperties()
	{
		properties = new ArrayList<XenaProperty>();
		
		// Office location
		XenaProperty locationProperty = new XenaProperty(OOO_DIR_PROP_NAME,
		                                                "Base directory of Open Office installation",
		                                                XenaProperty.PropertyType.DIR_TYPE,
		                                                this.getName());
		this.getManager().loadProperty(locationProperty);
		properties.add(locationProperty);
		
		
		// Office startup sleep time
		XenaProperty sleepProperty = new XenaProperty(OOO_SLEEP_PROP_NAME,
		                                                "Sleep time allowed for OpenOffice to start (milliseconds)",
		                                                XenaProperty.PropertyType.INT_TYPE,
		                                                this.getName());
		this.getManager().loadProperty(sleepProperty);
		
		// If first use, set default sleep time
		if (sleepProperty != null && sleepProperty.getValue().equals(""))
		{
			sleepProperty.setValue(OOO_SLEEP_DEFAULT_VALUE);
			this.getManager().saveProperty(sleepProperty);
		}
		
		properties.add(sleepProperty);
		
	}

}
