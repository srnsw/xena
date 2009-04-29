/**
 * This file is part of image.
 * 
 * image is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * image is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with image; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

/**
 * @author Justin Waddell
 *
 */
public class ImageProperties extends PluginProperties {

	private List<XenaProperty> properties;
	public static final String IMAGE_PLUGIN_NAME = "Image";
	public static final String TESSERACT_LOCATION_PROP_NAME = "Tesseract Executable Location";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.properties.PluginProperties#getName()
	 */
	@Override
	public String getName() {
		return IMAGE_PLUGIN_NAME;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.properties.PluginProperties#getProperties()
	 */
	@Override
	public List<XenaProperty> getProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.properties.PluginProperties#initialiseProperties()
	 */
	@Override
	public void initialiseProperties() {
		properties = new ArrayList<XenaProperty>();

		// tesseract executable property
		XenaProperty tesseractLocationProperty =
		    new XenaProperty(TESSERACT_LOCATION_PROP_NAME, "Location of tesseract executable", XenaProperty.PropertyType.FILE_TYPE, getName()) {

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

				    if (!location.exists() || !location.isFile()) {
					    throw new InvalidPropertyException("Invalid location for tesseract!");
				    }
			    }
		    };

		getManager().loadProperty(tesseractLocationProperty);
		properties.add(tesseractLocationProperty);

	}

}
