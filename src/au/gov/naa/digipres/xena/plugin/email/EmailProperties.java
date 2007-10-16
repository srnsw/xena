/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 14/02/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class EmailProperties extends PluginProperties {
	public static final String EMAIL_PLUGIN_NAME = "Email";
	public static final String READPST_LOCATION_PROP_NAME = "Readpst Executable Location";

	private List<XenaProperty> properties;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public EmailProperties() {

	}

	@Override
	public String getName() {
		return EMAIL_PLUGIN_NAME;
	}

	@Override
	public List<XenaProperty> getProperties() {
		return properties;
	}

	@Override
	public void initialiseProperties() {
		properties = new ArrayList<XenaProperty>();

		// fLaC executable property
		XenaProperty readpstLocationProperty =
		    new XenaProperty(READPST_LOCATION_PROP_NAME, "Location of readpst executable", XenaProperty.PropertyType.FILE_TYPE, this.getName()) {

			    /**
			     * Ensure selected executable file exists
			     * 
			     * @param newValue
			     * @throws InvalidPropertyException
			     * @throws PropertyMessageException 
			     */
			    @Override
			    public void validate(String newValue) throws InvalidPropertyException, PropertyMessageException {
				    super.validate(newValue);

				    File location = new File(newValue);

				    // Put this back in for Java 6!
				    // if (location == null || !location.exists() || !location.isFile() || !location.canExecute())

				    if (location == null || !location.exists() || !location.isFile()) {
					    throw new InvalidPropertyException("Invalid location for readpst!");
				    }
			    }
		    };

		this.getManager().loadProperty(readpstLocationProperty);
		properties.add(readpstLocationProperty);
	}

}
