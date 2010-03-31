/*
 * Created on 27/04/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.File;

import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;

public class PropertiesInfoProvider implements InfoProvider
{
	private PropertiesManager propManager;

	public PropertiesInfoProvider(PropertiesManager propManager)
	{
		this.propManager = propManager;
	}

	public String getUserName()
	{
		return propManager.getPropertyValue(OrgXProperties.ORG_X_PLUGIN_NAME, OrgXProperties.ORG_X_USER_PROP_NAME);
	}

	public String getDepartmentCode()
	{
		return propManager.getPropertyValue(OrgXProperties.ORG_X_PLUGIN_NAME, OrgXProperties.ORG_X_DEPARTMENT_PROP_NAME);
	}

	public String getDepartmentName()
	{
		return propManager.getPropertyValue(OrgXProperties.ORG_X_PLUGIN_NAME, OrgXProperties.ORG_X_DEPARTMENT_PROP_NAME);
	}

	public boolean isInsertTimestamp()
	{
		return new Boolean(propManager.getPropertyValue(OrgXProperties.ORG_X_PLUGIN_NAME, OrgXProperties.ORG_X_USE_TIMESTAMP_PROP_NAME));
	}

	public File getHeaderFile()
	{
		return new File(propManager.getPropertyValue(OrgXProperties.ORG_X_PLUGIN_NAME, OrgXProperties.ORG_X_HEADER_FILE_PROP_NAME));
	}

}
