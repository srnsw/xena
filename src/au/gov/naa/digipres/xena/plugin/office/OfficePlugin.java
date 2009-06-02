/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
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
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.plugin.office.presentation.OdpFileType;
import au.gov.naa.digipres.xena.plugin.office.presentation.OdpGuesser;
import au.gov.naa.digipres.xena.plugin.office.presentation.PowerpointFileType;
import au.gov.naa.digipres.xena.plugin.office.presentation.PowerpointGuesser;
import au.gov.naa.digipres.xena.plugin.office.presentation.PptxFileType;
import au.gov.naa.digipres.xena.plugin.office.presentation.PptxGuesser;
import au.gov.naa.digipres.xena.plugin.office.presentation.SxiFileType;
import au.gov.naa.digipres.xena.plugin.office.presentation.SxiGuesser;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.ExcelFileType;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.ExcelGuesser;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.OdsFileType;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.OdsGuesser;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.SxcFileType;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.SxcGuesser;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.SylkFileType;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.SylkGuesser;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.XlsxFileType;
import au.gov.naa.digipres.xena.plugin.office.spreadsheet.XlsxGuesser;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.DocxFileType;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.DocxGuesser;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.OdtFileType;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.OdtGuesser;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.RtfFileType;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.RtfGuesser;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.SxwFileType;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.SxwGuesser;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.WordFileType;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.WordGuesser;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.WordPerfectFileType;
import au.gov.naa.digipres.xena.plugin.office.wordprocessor.WordPerfectGuesser;

/*
 * *
 * 
 * @author Justin Waddell
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
		guesserList.add(new ExcelGuesser());
		guesserList.add(new WordGuesser());
		guesserList.add(new PowerpointGuesser());
		guesserList.add(new SxiGuesser());
		guesserList.add(new SxcGuesser());
		guesserList.add(new SxwGuesser());
		guesserList.add(new OdpGuesser());
		guesserList.add(new OdsGuesser());
		guesserList.add(new OdtGuesser());
		guesserList.add(new RtfGuesser());
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
		normaliserSet.add(new WordFileType());
		normaliserSet.add(new ExcelFileType());
		normaliserSet.add(new PowerpointFileType());
		normaliserSet.add(new SxiFileType());
		normaliserSet.add(new SxcFileType());
		normaliserSet.add(new SxwFileType());
		normaliserSet.add(new OdpFileType());
		normaliserSet.add(new OdtFileType());
		normaliserSet.add(new OdsFileType());
		normaliserSet.add(new RtfFileType());
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
	public Map<Type, AbstractNormaliser> getTextNormaliserMap() {

		Map<Type, AbstractNormaliser> textNormaliserMap = new HashMap<Type, AbstractNormaliser>();
		textNormaliserMap.put(new WordFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new ExcelFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new SylkFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new XenaOooFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new FlatOOoFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new WordPerfectFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new XlsxFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new DocxFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new SxcFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new SxwFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new OdtFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new OdsFileType(), new OfficeTextNormaliser());
		textNormaliserMap.put(new RtfFileType(), new OfficeTextNormaliser());

		return textNormaliserMap;
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new WordFileType());
		typeList.add(new ExcelFileType());
		typeList.add(new PowerpointFileType());
		typeList.add(new SylkFileType());
		typeList.add(new XenaOooFileType());
		typeList.add(new FlatOOoFileType());
		typeList.add(new WordPerfectFileType());
		typeList.add(new XlsxFileType());
		typeList.add(new DocxFileType());
		typeList.add(new PptxFileType());
		typeList.add(new SxiFileType());
		typeList.add(new SxcFileType());
		typeList.add(new SxwFileType());
		typeList.add(new OdpFileType());
		typeList.add(new OdtFileType());
		typeList.add(new OdsFileType());
		typeList.add(new RtfFileType());

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
