/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 14/02/2006 justinw5
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class OfficeProperties extends PluginProperties {
	public static final String OFFICE_PLUGIN_NAME = "Office";
	public static final String OOO_DIR_PROP_NAME = "Office Location";
	public static final String OOO_SLEEP_PROP_NAME = "Office Startup Sleep Time";

	private static final String OOO_SLEEP_DEFAULT_VALUE = "5";

	private List<XenaProperty> properties;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public OfficeProperties() {

	}

	@Override
	public String getName() {
		return OFFICE_PLUGIN_NAME;
	}

	@Override
	public List<XenaProperty> getProperties() {
		return properties;
	}

	@Override
	public void initialiseProperties() {
		properties = new ArrayList<XenaProperty>();

		// Office location
		XenaProperty locationProperty =
		    new XenaProperty(OOO_DIR_PROP_NAME, "Base directory of OpenOffice.org installation", XenaProperty.PropertyType.DIR_TYPE, getName()) {

			    /**
			     * Validates that the chosen value for the OpenOffice.org base directory is not null or empty,
			     * and the path represents an existing directory.
			     * 
			     * @param newValue
			     * @throws InvalidPropertyException
			     * @throws PropertyMessageException 
			     */
			    @Override
			    public void validate(String newValue) throws InvalidPropertyException, PropertyMessageException {
				    super.validate(newValue);
				    if (newValue == null || newValue.length() == 0) {
					    throw new InvalidPropertyException("New value for property " + getName() + " is null or empty");
				    }

				    File location = new File(newValue);
				    if (!location.exists() || !location.isDirectory()) {
					    throw new InvalidPropertyException("Invalid base directory for open office!");
				    }
			    }
		    };

		getManager().loadProperty(locationProperty);
		properties.add(locationProperty);

		// Office startup sleep time
		XenaProperty sleepProperty =
		    new XenaProperty(OOO_SLEEP_PROP_NAME, "Sleep time allowed for OpenOffice.org to start (seconds)", XenaProperty.PropertyType.INT_TYPE,
		                     getName()) {

			    /**
			     * Validates that the chosen value for the OpenOffice.org sleep time is a valid number from 0 to 300 (5 minutes)
			     * 
			     * @param newValue
			     * @throws InvalidPropertyException
			     * @throws PropertyMessageException 
			     */
			    @Override
			    public void validate(String newValue) throws InvalidPropertyException, PropertyMessageException {
				    super.validate(newValue);
				    if (newValue == null || newValue.length() == 0) {
					    throw new InvalidPropertyException("New value for property " + getName() + " is null or empty");
				    }

				    try {
					    int newSleepTime = Integer.parseInt(newValue);
					    if (newSleepTime < 0 || newSleepTime > 300) {
						    throw new InvalidPropertyException("New value for property " + name + " must be between 0 and 300.");
					    }
				    } catch (NumberFormatException e) {
					    throw new InvalidPropertyException("New value for property " + name + " is not a valid number", e);
				    }
			    }

		    };

		getManager().loadProperty(sleepProperty);

		// If first use, set default sleep time
		if (sleepProperty.getValue().equals("")) {
			sleepProperty.setValue(OOO_SLEEP_DEFAULT_VALUE);
			getManager().saveProperty(sleepProperty);
		}

		properties.add(sleepProperty);
	}

}
