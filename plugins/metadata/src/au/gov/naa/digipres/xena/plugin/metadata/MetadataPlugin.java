package au.gov.naa.digipres.xena.plugin.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import au.gov.naa.digipres.xena.kernel.metadata.AbstractMetaData;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;

public class MetadataPlugin extends XenaPlugin {

	public static final String METADATA_PLUGIN_NAME = "metadata";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return METADATA_PLUGIN_NAME;
	}

	@Override
	public List<PluginProperties> getPluginPropertiesList() {
		List<PluginProperties> propertiesList = new ArrayList<PluginProperties>();
		propertiesList.add(new MetadataProperties());
		return propertiesList;
	}

	@Override
	public List<AbstractMetaData> getMetaDataObjects() {
		List<AbstractMetaData> metadataList = new Vector<AbstractMetaData>();
		metadataList.add(new ExiftoolMetaData());
		return metadataList;
	}

}
