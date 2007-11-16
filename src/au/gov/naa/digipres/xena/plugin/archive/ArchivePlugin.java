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
 */

package au.gov.naa.digipres.xena.plugin.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.plugin.archive.gzip.GZipFileType;
import au.gov.naa.digipres.xena.plugin.archive.gzip.GZipGuesser;
import au.gov.naa.digipres.xena.plugin.archive.gzip.GZipNormaliser;
import au.gov.naa.digipres.xena.plugin.archive.macbinary.MacBinaryFileType;
import au.gov.naa.digipres.xena.plugin.archive.macbinary.MacBinaryGuesser;
import au.gov.naa.digipres.xena.plugin.archive.macbinary.MacBinaryNormaliser;
import au.gov.naa.digipres.xena.plugin.archive.tar.TarFileType;
import au.gov.naa.digipres.xena.plugin.archive.tar.TarGuesser;
import au.gov.naa.digipres.xena.plugin.archive.tar.TarNormaliser;
import au.gov.naa.digipres.xena.plugin.archive.zip.ZipFileType;
import au.gov.naa.digipres.xena.plugin.archive.zip.ZipGuesser;
import au.gov.naa.digipres.xena.plugin.archive.zip.ZipNormaliser;

/**
 * @author Justin Waddell
 *
 */
public class ArchivePlugin extends XenaPlugin {

	public static final String ARCHIVE_PLUGIN_NAME = "archive";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return ARCHIVE_PLUGIN_NAME;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public List<Guesser> getGuessers() {
		List<Guesser> guesserList = new ArrayList<Guesser>();

		guesserList.add(new ZipGuesser());
		guesserList.add(new TarGuesser());
		guesserList.add(new GZipGuesser());
		guesserList.add(new MacBinaryGuesser());

		return guesserList;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();

		// Zip
		ZipNormaliser zipNormaliser = new ZipNormaliser();
		Set<Type> zipInputSet = new HashSet<Type>();
		zipInputSet.add(new ZipFileType());
		inputMap.put(zipNormaliser, zipInputSet);

		// GZip
		GZipNormaliser gZipNormaliser = new GZipNormaliser();
		Set<Type> gZipInputSet = new HashSet<Type>();
		gZipInputSet.add(new GZipFileType());
		inputMap.put(gZipNormaliser, gZipInputSet);

		// MacBinary
		MacBinaryNormaliser macBinaryNormaliser = new MacBinaryNormaliser();
		Set<Type> macBinaryInputSet = new HashSet<Type>();
		macBinaryInputSet.add(new MacBinaryFileType());
		inputMap.put(macBinaryNormaliser, macBinaryInputSet);

		// Tar
		TarNormaliser tarNormaliser = new TarNormaliser();
		Set<Type> tarInputSet = new HashSet<Type>();
		tarInputSet.add(new TarFileType());
		inputMap.put(tarNormaliser, tarInputSet);

		// Denormaliser
		ArchiveDeNormaliser denormaliser = new ArchiveDeNormaliser();
		Set<Type> deNormaliserInputSet = new HashSet<Type>();
		deNormaliserInputSet.add(new XenaArchiveFileType());
		inputMap.put(denormaliser, deNormaliserInputSet);

		return inputMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();

		// Zip
		ZipNormaliser zipNormaliser = new ZipNormaliser();
		Set<Type> zipOutputSet = new HashSet<Type>();
		zipOutputSet.add(new XenaArchiveFileType());
		outputMap.put(zipNormaliser, zipOutputSet);

		// GZip
		GZipNormaliser gZipNormaliser = new GZipNormaliser();
		Set<Type> gZipOutputSet = new HashSet<Type>();
		gZipOutputSet.add(new XenaArchiveFileType());
		outputMap.put(gZipNormaliser, gZipOutputSet);

		// Tar
		TarNormaliser tarNormaliser = new TarNormaliser();
		Set<Type> tarOutputSet = new HashSet<Type>();
		tarOutputSet.add(new XenaArchiveFileType());
		outputMap.put(tarNormaliser, tarOutputSet);

		// MacBinary
		MacBinaryNormaliser macBinaryNormaliser = new MacBinaryNormaliser();
		Set<Type> macBinaryOutputSet = new HashSet<Type>();
		macBinaryOutputSet.add(new XenaArchiveFileType());
		outputMap.put(macBinaryNormaliser, macBinaryOutputSet);

		// Denormaliser
		ArchiveDeNormaliser denormaliser = new ArchiveDeNormaliser();
		Set<Type> deNormaliserOutputSet = new HashSet<Type>();
		deNormaliserOutputSet.add(new ZipFileType());
		outputMap.put(denormaliser, deNormaliserOutputSet);

		return outputMap;
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new XenaArchiveFileType());
		typeList.add(new ZipFileType());
		typeList.add(new GZipFileType());
		typeList.add(new MacBinaryFileType());
		typeList.add(new TarFileType());

		return typeList;
	}

	@Override
	public List<XenaView> getViews() {
		List<XenaView> viewList = new ArrayList<XenaView>();
		viewList.add(new ArchiveView());
		return viewList;
	}

}
