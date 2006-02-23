/*
 * Created on 10/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.util.List;

public class XenaProperty
{
	public enum PropertyType
	{
		STRING_TYPE, INT_TYPE, FILE_TYPE, DIR_TYPE, 
		SINGLE_OPTION_TYPE, MULTI_OPTION_TYPE, BOOLEAN_TYPE
	}
	
	private String name;
	private String description;
	private String value = "";
	private List<Object> listOptions;
	private PropertyType type;
	private String pluginName;
	
	
	
	/**
	 * @param description
	 * @param name
	 * @param type
	 */
	public XenaProperty(String name, 
						String description, 
						PropertyType type,
						String pluginName)
	{
		this.description = description;
		this.name = name;
		this.type = type;
		this.pluginName = pluginName;
	}



	/**
	 * Perform basic validation. Subclasses should call this method in
	 * addition to any specific validation which may be required.
	 * 
	 * @param newValue
	 * @throws InvalidPropertyException
	 */
	public void validate(String newValue) throws InvalidPropertyException
	{
		if (newValue == null)
		{
			throw new InvalidPropertyException("New value for property " + name + 
											   " is null");
		}
		
		switch(type)
		{
		case BOOLEAN_TYPE:
			if (!newValue.equals(Boolean.TRUE.toString()) &&
				!newValue.equals(Boolean.FALSE.toString()))
			{
				throw new InvalidPropertyException("New value for property " + name + 
				                                   " is not a valid boolean value");
			}
			break;
		case INT_TYPE:
			try
			{
				Integer.parseInt(newValue);
			}
			catch (NumberFormatException ex)
			{
				throw new InvalidPropertyException("New value for property " + name +
				                                   " is not a valid number",
				                                   ex);
			}
			break;
		}
	}
	
	

	/**
	 * @return Returns the description.
	 */
	public String getDescription()
	{
		return description;
	}


	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}


	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 * @return Returns the value.
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * @return Returns the listOptions.
	 */
	public List<Object> getListOptions()
	{
		return listOptions;
	}

	/**
	 * @param listOptions The listOptions to set.
	 */
	public void setListOptions(List<Object> listOptions)
	{
		this.listOptions = listOptions;
	}




	/**
	 * @return Returns the type.
	 */
	public PropertyType getType()
	{
		return type;
	}




	/**
	 * @param type The type to set.
	 */
	public void setType(PropertyType type)
	{
		this.type = type;
	}



	/**
	 * @return Returns the pluginName.
	 */
	public String getPluginName()
	{
		return pluginName;
	}



	/**
	 * @param pluginName The pluginName to set.
	 */
	public void setPluginName(String pluginName)
	{
		this.pluginName = pluginName;
	}


}
