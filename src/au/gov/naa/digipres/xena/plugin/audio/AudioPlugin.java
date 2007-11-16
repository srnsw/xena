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

package au.gov.naa.digipres.xena.plugin.audio;

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
public class AudioPlugin extends XenaPlugin {

	public static final String AUDIO_PLUGIN_NAME = "audio";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return AUDIO_PLUGIN_NAME;
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

		guesserList.add(new WavGuesser());
		guesserList.add(new AiffGuesser());
		guesserList.add(new MP3Guesser());
		guesserList.add(new FlacGuesser());
		guesserList.add(new PcmGuesser());

		return guesserList;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();

		// Direct Audio Normaliser
		DirectAudioNormaliser directNormaliser = new DirectAudioNormaliser();
		Set<Type> directNormaliserSet = new HashSet<Type>();
		directNormaliserSet.add(new WavType());
		directNormaliserSet.add(new AiffType());
		inputMap.put(directNormaliser, directNormaliserSet);

		// Denormaliser
		AudioDeNormaliser denormaliser = new AudioDeNormaliser();
		Set<Type> denormaliserSet = new HashSet<Type>();
		denormaliserSet.add(new XenaAudioFileType());
		inputMap.put(denormaliser, denormaliserSet);

		// Flac Normaliser
		FlacToXenaAudioNormaliser flacNormaliser = new FlacToXenaAudioNormaliser();
		Set<Type> flacNormaliserSet = new HashSet<Type>();
		flacNormaliserSet.add(new FlacType());
		inputMap.put(flacNormaliser, flacNormaliserSet);

		// Converted Audio Normaliser
		ConvertedAudioNormaliser convertedNormaliser = new ConvertedAudioNormaliser();
		Set<Type> convertedNormaliserSet = new HashSet<Type>();
		convertedNormaliserSet.add(new MP3Type());
		convertedNormaliserSet.add(new PcmType());
		inputMap.put(convertedNormaliser, convertedNormaliserSet);

		return inputMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();

		// Direct Audio Normaliser
		DirectAudioNormaliser directNormaliser = new DirectAudioNormaliser();
		Set<Type> directNormaliserSet = new HashSet<Type>();
		directNormaliserSet.add(new XenaAudioFileType());
		outputMap.put(directNormaliser, directNormaliserSet);

		// Denormaliser
		AudioDeNormaliser denormaliser = new AudioDeNormaliser();
		Set<Type> denormaliserSet = new HashSet<Type>();
		denormaliserSet.add(new FlacType());
		outputMap.put(denormaliser, denormaliserSet);

		// Flac Normaliser
		FlacToXenaAudioNormaliser flacNormaliser = new FlacToXenaAudioNormaliser();
		Set<Type> flacNormaliserSet = new HashSet<Type>();
		flacNormaliserSet.add(new XenaAudioFileType());
		outputMap.put(flacNormaliser, flacNormaliserSet);

		// Converted Audio Normaliser
		ConvertedAudioNormaliser convertedNormaliser = new ConvertedAudioNormaliser();
		Set<Type> convertedNormaliserSet = new HashSet<Type>();
		convertedNormaliserSet.add(new XenaAudioFileType());
		outputMap.put(convertedNormaliser, convertedNormaliserSet);

		return outputMap;
	}

	@Override
	public List<PluginProperties> getPluginPropertiesList() {
		List<PluginProperties> propertiesList = new ArrayList<PluginProperties>();
		propertiesList.add(new AudioProperties());
		return propertiesList;
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new XenaAudioFileType());
		typeList.add(new WavType());
		typeList.add(new MP3Type());
		typeList.add(new PcmType());
		typeList.add(new FlacType());
		typeList.add(new AiffType());

		return typeList;
	}

	@Override
	public List<XenaView> getViews() {
		List<XenaView> viewList = new ArrayList<XenaView>();
		viewList.add(new AudioPlayerView());
		return viewList;
	}

}
