/*
 * Created on 14/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
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
		                                                this.getName())
                                                        {
            
            /**
             * Perform basic validation. Subclasses should call this method in
             * addition to any specific validation which may be required.
             * 
             * @param newValue
             * @throws InvalidPropertyException
             */
            @Override
            public void validate(String newValue) throws InvalidPropertyException {
                
                if (newValue == null) {
                    throw new InvalidPropertyException("New value for property " + name + " is null");
                }
                if (newValue instanceof String) {
                    String valueString = (String) name;
                    if (valueString.length() == 0) {
                        throw new InvalidPropertyException("New value for property " + name + " is null");
                    }
                } else {
                    throw new InvalidPropertyException("New value for property " + name + " is not a string!");    
                }
                
                ConfigOpenOffice conf = new ConfigOpenOffice();
                
                File location = new File(newValue);
                if (location == null || !location.exists() || !location.isDirectory()) {
                    throw new InvalidPropertyException("Invalid location for open office!");
                }
                conf.setInstallDir(location);
                conf.modify();
            }
        };
        
        
        
        
        
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
