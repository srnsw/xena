/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Matthew Oliver
 */

package au.gov.naa.digipres.xena.plugin.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class MetadataProperties extends PluginProperties {
	public static final String METADATA_PLUGIN_NAME = "Metadata";
	public static final String EXIFTOOL_LOCATION_PROP_NAME = "ExifTool Executable Location";

	private List<XenaProperty> properties;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public MetadataProperties() {

	}

	@Override
	public String getName() {
		return METADATA_PLUGIN_NAME;
	}

	@Override
	public List<XenaProperty> getProperties() {
		return properties;
	}

	@Override
	public void initialiseProperties() {
		properties = new ArrayList<XenaProperty>();

		// ExifTool executable property
		XenaProperty exifToolLocationProperty =
		    new XenaProperty(EXIFTOOL_LOCATION_PROP_NAME, "Location of ExifTool executable", XenaProperty.PropertyType.FILE_TYPE, this.getName()) {

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
				    // Put this back in when Java 6 is used!
				    // if (location == null || !location.exists() || !location.isFile() || !location.canExecute())

				    if (location == null || !location.exists() || !location.isFile()) {
					    throw new InvalidPropertyException("Invalid location for ExifTool!");
				    }
			    }
		    };

		this.getManager().loadProperty(exifToolLocationProperty);
		properties.add(exifToolLocationProperty);
	}

}
