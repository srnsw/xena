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

package au.gov.naa.digipres.xena.plugin.naa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.XMLFilter;

import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.XenaBinaryFileType;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.AviType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.FlashType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.MacWordType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.MovType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.MpegType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.ThumbsDBType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.UnsupportedType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.UnsupportedTypeGuesser;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.UnsupportedTypeNormaliser;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.VisioType;
import au.gov.naa.digipres.xena.plugin.naa.unsupported.WmvType;

/**
 * @author Justin Waddell
 *
 */
public class NaaPlugin extends XenaPlugin {

	public static final String NAA_PLUGIN_NAME = "naa";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return NAA_PLUGIN_NAME;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public List<AbstractFileNamer> getFileNamers() {
		List<AbstractFileNamer> fileNamerList = new ArrayList<AbstractFileNamer>();
		fileNamerList.add(new NaaFileNamer());
		return fileNamerList;
	}

	@Override
	public List<Guesser> getGuessers() {
		List<Guesser> guesserList = new ArrayList<Guesser>();
		guesserList.add(new UnsupportedTypeGuesser());
		return guesserList;
	}

	@Override
	public Map<AbstractMetaDataWrapper, XMLFilter> getMetaDataWrappers() {
		Map<AbstractMetaDataWrapper, XMLFilter> wrapperMap = new LinkedHashMap<AbstractMetaDataWrapper, XMLFilter>();
		wrapperMap.put(new NaaSignedAipWrapNormaliser(), new NaaSignedAipUnwrapFilter());
		wrapperMap.put(new NaaPackageWrapNormaliser(), new NaaPackageUnwrapFilter());
		return wrapperMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();
		Set<Type> unsupportedSet = new HashSet<Type>();
		unsupportedSet.add(new AviType());
		unsupportedSet.add(new FlashType());
		unsupportedSet.add(new MacWordType());
		unsupportedSet.add(new MovType());
		unsupportedSet.add(new MpegType());
		unsupportedSet.add(new ThumbsDBType());
		unsupportedSet.add(new VisioType());
		unsupportedSet.add(new UnsupportedType());
		unsupportedSet.add(new WmvType());
		inputMap.put(new UnsupportedTypeNormaliser(), unsupportedSet);

		return inputMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();
		Set<Type> unsupportedSet = new HashSet<Type>();
		unsupportedSet.add(new XenaBinaryFileType());
		outputMap.put(new UnsupportedTypeNormaliser(), unsupportedSet);
		return outputMap;
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new UnsupportedType());
		typeList.add(new NaaPackageFileType());
		typeList.add(new AviType());
		typeList.add(new FlashType());
		typeList.add(new MacWordType());
		typeList.add(new MovType());
		typeList.add(new MpegType());
		typeList.add(new ThumbsDBType());
		typeList.add(new VisioType());
		typeList.add(new WmvType());

		return typeList;
	}

	@Override
	public List<XenaView> getViews() {
		List<XenaView> viewList = new ArrayList<XenaView>();
		viewList.add(new NaaPackageView());
		return viewList;
	}

}
