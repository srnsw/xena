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
 * Created on 26/04/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

public class SourceURIParser {

	/**
	 * Return the relative system Id for a given XenaInputSource. This assumes that 
	 * the base path has been set in the meta data wrapper by which all local system
	 * id should be resolved from.
	 * 
	 * TODO: aak - SourceURIParser - better description required please...
	 * 
	 * @param xis
	 * @param pluginManager
	 * @return String - the relative path and name of the file relative to the base path set in
	 * the meta data wrapper, or, if that is not able to be resolved (ie base path is not set or
	 * the XIS URI has nothing to do with the base path) then the entire URI of the XIS.
	 * @throws SAXException - In the event that the URI from the XIS cannot be encoded.
	 */
	public static String getRelativeSystemId(XenaInputSource xis, PluginManager pluginManager) throws SAXException {
		String xisRelativeSystemId = "";
		try {
			java.net.URI uri = new java.net.URI(xis.getSystemId());
			if (uri.getScheme() != null && "file".equals(uri.getScheme())) {
				File inputSourceFile = new File(uri);
				String relativePath = null;
				File baseDir;
				/*
				 * Get the path location.
				 * 
				 * First off, see if we can get a path from the filter manager, and get a relative path. 
				 * If no success, then we set the path to be the full path name.
				 * 
				 */
				if (pluginManager.getMetaDataWrapperManager().getBasePathName() != null) {
					try {
						baseDir = new File(pluginManager.getMetaDataWrapperManager().getBasePathName());
						relativePath = FileName.relativeTo(baseDir, inputSourceFile);
					} catch (IOException iox) {
						// Nothing to do here as we have another go at setting the base path further down
					}
				}
				if (relativePath == null) {
					relativePath = inputSourceFile.getName();
				}
				String encodedPath = null;
				try {
					encodedPath = au.gov.naa.digipres.xena.util.UrlEncoder.encode(relativePath);
				} catch (UnsupportedEncodingException x) {
					throw new SAXException(x);
				}
				xisRelativeSystemId = "file:/" + encodedPath;
			} else {
				xisRelativeSystemId = xis.getSystemId();
			}
		} catch (URISyntaxException xe) {
			xisRelativeSystemId = xis.getSystemId();
		}
		return xisRelativeSystemId;
	}

	/**
	 * Return only the filename component of the source uri.
	 * Basically everything after the last '/' and '\'.
	 * 
	 * TODO: aak - SourceURIParser - better description required please...
	 * 
	 * @param xis
	 * @return
	 */
	public static String getFileNameComponent(XenaInputSource xis) {
		String systemId = xis.getSystemId();

		int backslashIndex = systemId.lastIndexOf("\\");
		int slashIndex = systemId.lastIndexOf("/");
		int lastIndex = backslashIndex > slashIndex ? backslashIndex : slashIndex;
		return systemId.substring(lastIndex + 1);
	}

}
