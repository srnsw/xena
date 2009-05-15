/**
 * This file is part of html.
 * 
 * html is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * html is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with html; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.html.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import au.gov.naa.digipres.xena.javatools.ClassName;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * @author Justin Waddell
 *
 */
public class EntityResolverImpl implements EntityResolver {

	private NormaliserManager normaliserManager;

	public EntityResolverImpl(NormaliserManager normaliserManager) {
		this.normaliserManager = normaliserManager;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemIdParam) {
		String systemId = systemIdParam;
		int ind = systemId.lastIndexOf('/');
		if (0 < ind) {
			systemId = systemId.substring(ind + 1);
		}
		ClassLoader loader = normaliserManager.getPluginManager().getClassLoader();
		String resourceName = ClassName.joinPath(ClassName.classToPath(ClassName.packageComponent(getClass().getName())), systemId);
		return new InputSource(loader.getResourceAsStream(resourceName));
	}

}
