/*
 * Created on 14/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;
import au.gov.naa.digipres.xena.plugin.office.ConfigOpenOffice.AlreadyDoneException;

public class OfficeProperties extends PluginProperties
{
	public static final String OFFICE_PLUGIN_NAME = "Office";
	public static final String OOO_DIR_PROP_NAME = "Office Location";
	public static final String OOO_SLEEP_PROP_NAME = "Office Startup Sleep Time";
	
	private static final String OOO_SLEEP_DEFAULT_VALUE = "14000";
	
	private List<XenaProperty> properties;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

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
             * @throws PropertyMessageException 
             */
            @Override
            public void validate(String newValue) throws InvalidPropertyException, PropertyMessageException {
                
                if (newValue == null) {
                    throw new InvalidPropertyException("New value for property " + getName() + " is null");
                }
                if (newValue instanceof String) {
                    String valueString = getName();
                    if (valueString.length() == 0) {
                        throw new InvalidPropertyException("New value for property " + getName() + " is null");
                    }
                } else {
                    throw new InvalidPropertyException("New value for property " + getName() + " is not a string!");    
                }
                
                ConfigOpenOffice conf = new ConfigOpenOffice();
                
                File location = new File(newValue);
                if (location == null || !location.exists() || !location.isDirectory()) {
                    throw new InvalidPropertyException("Invalid location for open office!");
                }
                conf.setInstallDir(location);
                
                try
				{
					conf.modify();
				}
				catch (IOException e)
				{
					throw new InvalidPropertyException("An error occurred when trying to modify the OpenOffice configuration files", e);
				}
				catch (AlreadyDoneException e)
				{
					// Log but do nothing
					logger.finest("OpenOffice already configured, no changes made to OpenOffice configuration files");
					
				}
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
