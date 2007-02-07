/*
 * Created on 14/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.plugin.PluginLocator;
import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class AudioProperties extends PluginProperties
{
	public static final String AUDIO_PLUGIN_NAME = "Audio";
	public static final String FLAC_LOCATION_PROP_NAME = "Flac Executable Location";
	
	private List<XenaProperty> properties;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public AudioProperties()
	{
		
	}

	@Override
	public String getName()
	{
		return AUDIO_PLUGIN_NAME;
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
		
		// fLaC executable property
		XenaProperty flacLocationProperty = new XenaProperty(FLAC_LOCATION_PROP_NAME,
		                                                "Location of flac executable",
		                                                XenaProperty.PropertyType.FILE_TYPE,
		                                                this.getName())
                                                        {
            
            /**
             * Ensure selected executable file exists
             * 
             * @param newValue
             * @throws InvalidPropertyException
             * @throws PropertyMessageException 
             */
            @Override
            public void validate(String newValue) throws InvalidPropertyException, PropertyMessageException 
            {
                super.validate(newValue);
                
                File location = new File(newValue);
                // Put this back in when Java 6 is used!                
//                if (location == null || !location.exists() || !location.isFile() || !location.canExecute()) 
                
                if (location == null || !location.exists() || !location.isFile()) 
                {
                    throw new InvalidPropertyException("Invalid location for flac!");
                }
            }
        };
        
		this.getManager().loadProperty(flacLocationProperty);
		properties.add(flacLocationProperty);
    }

}
