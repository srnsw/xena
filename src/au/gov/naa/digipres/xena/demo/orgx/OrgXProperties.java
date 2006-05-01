/*
 * Created on 26/04/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty.PropertyType;

public class OrgXProperties extends PluginProperties
{
	public static final String ORG_X_PLUGIN_NAME = "OrgX";
	public static final String ORG_X_HEADER_FILE_PROP_NAME = "OrgX Header File";
	public static final String ORG_X_DEPARTMENT_PROP_NAME = "OrgX Department Name";
	public static final String ORG_X_USER_PROP_NAME = "User Name";
	public static final String ORG_X_USE_TIMESTAMP_PROP_NAME = "Insert Timestamp";
	
	private List<XenaProperty> properties;

	public OrgXProperties()
	{
		super();
	}

	@Override
	public String getName()
	{
		return ORG_X_PLUGIN_NAME;
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
		
		// User name property
		XenaProperty userProperty = new XenaProperty(ORG_X_USER_PROP_NAME, 
		                                             "Name of current user", 
		                                             PropertyType.STRING_TYPE, 
		                                             ORG_X_PLUGIN_NAME);
		this.getManager().loadProperty(userProperty);
		properties.add(userProperty);
				
		// Timestamp property
		XenaProperty timestampProperty = new XenaProperty(ORG_X_USE_TIMESTAMP_PROP_NAME, 
		                                             "Insert Timestamp", 
		                                             PropertyType.BOOLEAN_TYPE, 
		                                             ORG_X_PLUGIN_NAME);
		this.getManager().loadProperty(timestampProperty);
		properties.add(timestampProperty);

		// Department name property
		XenaProperty departmentProperty = new XenaProperty(ORG_X_DEPARTMENT_PROP_NAME, 
		                                             "OrgX Department Name", 
		                                             PropertyType.SINGLE_OPTION_TYPE, 
		                                             ORG_X_PLUGIN_NAME);
		List<Object> departmentOptions = new ArrayList<Object>();
		departmentOptions.add("Finance");
		departmentOptions.add("Library");
		departmentOptions.add("HR");
		departmentOptions.add("Consulting");
		departmentOptions.add("Maintenance");
		departmentOptions.add("Executive");
		departmentProperty.setListOptions(departmentOptions);
		this.getManager().loadProperty(departmentProperty);
		properties.add(departmentProperty);
		
		
		// Header file property
		XenaProperty headerProperty = new XenaProperty(ORG_X_HEADER_FILE_PROP_NAME, 
		                                             "Header text file location", 
		                                             PropertyType.FILE_TYPE, 
		                                             ORG_X_PLUGIN_NAME)
	    {
			/* (non-Javadoc)
			 * @see au.gov.naa.digipres.xena.kernel.properties.XenaProperty#validate(java.lang.String)
			 */
			@Override
			public void validate(String newValue) throws InvalidPropertyException, PropertyMessageException
			{
				super.validate(newValue);
				File headerFile = new File(newValue);
				if (!headerFile.exists())
				{
					throw new InvalidPropertyException("Selected header file does not exist: " + headerFile);
				}
				if (headerFile.isDirectory())
				{
					throw new InvalidPropertyException("Selected header file is a directory: " + headerFile);
				}
			}
			
	    };
	    
		this.getManager().loadProperty(headerProperty);
		properties.add(headerProperty);


	}

}
