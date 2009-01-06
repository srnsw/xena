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
package au.gov.naa.digipres.xena.plugin.postscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.gov.naa.digipres.xena.core.ReleaseInfo;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/***
 * 
 * @author Matthew Oliver
 *
 */
public class PostscriptPlugin extends XenaPlugin {
	
	public static final String POSTSCRIPT_PLUGIN_NAME = "postscript";
	
	@Override
	public String getName() {
		return POSTSCRIPT_PLUGIN_NAME;
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
		guesserList.add(new PostscriptGuesser());
		return guesserList;
	}
	
	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();

		// Postscript
		PostscriptNormaliser psNormaliser = new PostscriptNormaliser();
		Set<Type> psNormaliserSet = new HashSet<Type>();
		psNormaliserSet.add(new PostscriptFileType());
		inputMap.put(psNormaliser, psNormaliserSet);

		// Postscript Denormaliser
		XenaToPostscriptDeNormaliser psDenormaliser = new XenaToPostscriptDeNormaliser();
		Set<Type> psDenormaliserSet = new HashSet<Type>();
		psDenormaliserSet.add(new XenaPostscriptFileType());
		inputMap.put(psDenormaliser, psDenormaliserSet);
		
		return inputMap;
	}
	
	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();

		// Postscript
		PostscriptNormaliser psNormaliser = new PostscriptNormaliser();
		Set<Type> psNormaliserSet = new HashSet<Type>();
		psNormaliserSet.add(new XenaPostscriptFileType());
		outputMap.put(psNormaliser, psNormaliserSet);


		// Postscript Denormaliser
		XenaToPostscriptDeNormaliser psDenormaliser = new XenaToPostscriptDeNormaliser();
		Set<Type> psDenormaliserSet = new HashSet<Type>();
		psDenormaliserSet.add(new PostscriptFileType());
		outputMap.put(psDenormaliser, psDenormaliserSet);

		return outputMap;
	}
	
	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new XenaPostscriptFileType());
		typeList.add(new PostscriptFileType());

		return typeList;
	}
	
	@Override
	public List<XenaView> getViews() {
		List<XenaView> viewList = new ArrayList<XenaView>();
		viewList.add(new PSView());
		return viewList;
	}
}
