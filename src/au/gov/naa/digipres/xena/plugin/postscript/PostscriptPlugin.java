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
		viewList.add(new PostscriptViewer());
		return viewList;
	}
}
