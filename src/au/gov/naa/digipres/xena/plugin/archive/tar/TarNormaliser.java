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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 28/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.tar;

import java.io.InputStream;

import au.gov.naa.digipres.xena.plugin.archive.ArchiveHandler;
import au.gov.naa.digipres.xena.plugin.archive.ArchiveNormaliser;

import com.ice.tar.TarInputStream;

/**
 * Normaliser for .zip and .jar files
 * 
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class TarNormaliser extends ArchiveNormaliser {

	@Override
	protected ArchiveHandler getArchiveHandler(InputStream archiveStream) {
		TarInputStream tarStream = new TarInputStream(archiveStream);
		return new TarHandler(tarStream);
	}

	@Override
	public String getName() {
		return "Tar";
	}

}
