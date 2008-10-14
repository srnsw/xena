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

package au.gov.naa.digipres.xena.plugin.office;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * @author Justin Waddell
 *
 */
public class OfficePlugin extends XenaPlugin {

	public static final String OFFICE_PLUGIN_NAME = "office";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return OFFICE_PLUGIN_NAME;
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
		guesserList.add(new SpreadsheetGuesser());
		guesserList.add(new WordProcessorGuesser());
		guesserList.add(new PresentationGuesser());
		guesserList.add(new SylkGuesser());
		guesserList.add(new WordPerfectGuesser());
		guesserList.add(new PptxGuesser());
		guesserList.add(new DocxGuesser());
		guesserList.add(new XlsxGuesser());
		return guesserList;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();

		// Normaliser
		OfficeToXenaOooNormaliser normaliser = new OfficeToXenaOooNormaliser();
		Set<Type> normaliserSet = new HashSet<Type>();
		normaliserSet.add(new WordProcessorFileType());
		normaliserSet.add(new SpreadsheetFileType());
		normaliserSet.add(new PresentationFileType());
		normaliserSet.add(new SylkFileType());
		normaliserSet.add(new WordPerfectFileType());
		normaliserSet.add(new XlsxFileType());
		normaliserSet.add(new DocxFileType());
		normaliserSet.add(new PptxFileType());
		inputMap.put(normaliser, normaliserSet);

		// Denormaliser
		XenaOfficeToFlatOooDeNormaliser denormaliser = new XenaOfficeToFlatOooDeNormaliser();
		Set<Type> denormaliserSet = new HashSet<Type>();
		denormaliserSet.add(new XenaOooFileType());
		inputMap.put(denormaliser, denormaliserSet);

		return inputMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();

		// Normaliser
		OfficeToXenaOooNormaliser normaliser = new OfficeToXenaOooNormaliser();
		Set<Type> normaliserSet = new HashSet<Type>();
		normaliserSet.add(new XenaOooFileType());
		outputMap.put(normaliser, normaliserSet);

		// Denormaliser
		XenaOfficeToFlatOooDeNormaliser denormaliser = new XenaOfficeToFlatOooDeNormaliser();
		Set<Type> denormaliserSet = new HashSet<Type>();
		denormaliserSet.add(new FlatOOoFileType());
		outputMap.put(denormaliser, denormaliserSet);

		return outputMap;
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new WordProcessorFileType());
		typeList.add(new SpreadsheetFileType());
		typeList.add(new PresentationFileType());
		typeList.add(new SylkFileType());
		typeList.add(new XenaOooFileType());
		typeList.add(new FlatOOoFileType());
		typeList.add(new WordPerfectFileType());
		typeList.add(new XlsxFileType());
		typeList.add(new DocxFileType());
		typeList.add(new PptxFileType());

		return typeList;
	}

	@Override
	public List<XenaView> getViews() {
		List<XenaView> viewList = new ArrayList<XenaView>();
		viewList.add(new OooView());
		return viewList;
	}

	@Override
	public List<PluginProperties> getPluginPropertiesList() {
		List<PluginProperties> propertiesList = new ArrayList<PluginProperties>();
		propertiesList.add(new OfficeProperties());
		return propertiesList;
	}

}
