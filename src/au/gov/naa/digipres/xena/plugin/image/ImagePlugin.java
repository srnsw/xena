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

package au.gov.naa.digipres.xena.plugin.image;

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
import au.gov.naa.digipres.xena.plugin.image.legacy.CURFileType;
import au.gov.naa.digipres.xena.plugin.image.legacy.CURGuesser;
import au.gov.naa.digipres.xena.plugin.image.legacy.LegacyImageToXenaPngNormaliser;
import au.gov.naa.digipres.xena.plugin.image.legacy.PSDFileType;
import au.gov.naa.digipres.xena.plugin.image.legacy.PSDGuesser;
import au.gov.naa.digipres.xena.plugin.image.legacy.RASFileType;
import au.gov.naa.digipres.xena.plugin.image.legacy.RASGuesser;
import au.gov.naa.digipres.xena.plugin.image.legacy.XPMFileType;
import au.gov.naa.digipres.xena.plugin.image.legacy.XPMGuesser;
import au.gov.naa.digipres.xena.plugin.image.pcx.PcxFileType;
import au.gov.naa.digipres.xena.plugin.image.pcx.PcxGuesser;
import au.gov.naa.digipres.xena.plugin.image.pcx.PcxToXenaPngNormaliser;
import au.gov.naa.digipres.xena.plugin.image.tiff.TiffFileType;
import au.gov.naa.digipres.xena.plugin.image.tiff.TiffGuesser;
import au.gov.naa.digipres.xena.plugin.image.tiff.TiffToXenaPngNormaliser;

/**
 * @author Justin Waddell
 *
 */
public class ImagePlugin extends XenaPlugin {

	public static final String IMAGE_PLUGIN_NAME = "image";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return IMAGE_PLUGIN_NAME;
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
		guesserList.add(new GifGuesser());
		guesserList.add(new BmpGuesser());
		guesserList.add(new XPMGuesser());
		guesserList.add(new RASGuesser());
		guesserList.add(new CURGuesser());
		guesserList.add(new PSDGuesser());
		guesserList.add(new TiffGuesser());
		guesserList.add(new SvgGuesser());
		guesserList.add(new PcxGuesser());
		guesserList.add(new PNGGuesser());
		guesserList.add(new JpegGuesser());
		return guesserList;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();

		// PNG
		PngToXenaPngNormaliser pngNormaliser = new PngToXenaPngNormaliser();
		Set<Type> pngNormaliserSet = new HashSet<Type>();
		pngNormaliserSet.add(new PngFileType());
		inputMap.put(pngNormaliser, pngNormaliserSet);

		// JPEG
		JpegToXenaJpegNormaliser jpegNormaliser = new JpegToXenaJpegNormaliser();
		Set<Type> jpegNormaliserSet = new HashSet<Type>();
		jpegNormaliserSet.add(new JpegFileType());
		inputMap.put(jpegNormaliser, jpegNormaliserSet);

		// Image (BMP and GIF)
		ImageToXenaPngNormaliser imageNormaliser = new ImageToXenaPngNormaliser();
		Set<Type> imageNormaliserSet = new HashSet<Type>();
		imageNormaliserSet.add(new GifFileType());
		imageNormaliserSet.add(new BmpFileType());
		inputMap.put(imageNormaliser, imageNormaliserSet);

		// Legacy Image
		LegacyImageToXenaPngNormaliser legacyImageNormaliser = new LegacyImageToXenaPngNormaliser();
		Set<Type> legacyImageNormaliserSet = new HashSet<Type>();
		legacyImageNormaliserSet.add(new XPMFileType());
		legacyImageNormaliserSet.add(new RASFileType());
		legacyImageNormaliserSet.add(new CURFileType());
		legacyImageNormaliserSet.add(new PSDFileType());
		inputMap.put(legacyImageNormaliser, legacyImageNormaliserSet);

		// PCX
		PcxToXenaPngNormaliser pcxNormaliser = new PcxToXenaPngNormaliser();
		Set<Type> pcxNormaliserSet = new HashSet<Type>();
		pcxNormaliserSet.add(new PcxFileType());
		inputMap.put(pcxNormaliser, pcxNormaliserSet);

		// SVG
		SvgNormaliser svgNormaliser = new SvgNormaliser();
		Set<Type> svgNormaliserSet = new HashSet<Type>();
		svgNormaliserSet.add(new SvgFileType());
		inputMap.put(svgNormaliser, svgNormaliserSet);

		// TIFF
		TiffToXenaPngNormaliser tiffNormaliser = new TiffToXenaPngNormaliser();
		Set<Type> tiffNormaliserSet = new HashSet<Type>();
		tiffNormaliserSet.add(new TiffFileType());
		inputMap.put(tiffNormaliser, tiffNormaliserSet);

		// PNG Denormaliser
		XenaPngToPngDeNormaliser pngDenormaliser = new XenaPngToPngDeNormaliser();
		Set<Type> pngDenormaliserSet = new HashSet<Type>();
		pngDenormaliserSet.add(new XenaPngFileType());
		inputMap.put(pngDenormaliser, pngDenormaliserSet);

		// JPEG Denormaliser
		XenaJpegToJpegDeNormaliser jpegDenormaliser = new XenaJpegToJpegDeNormaliser();
		Set<Type> jpegDenormaliserSet = new HashSet<Type>();
		jpegDenormaliserSet.add(new XenaJpegFileType());
		inputMap.put(jpegDenormaliser, jpegDenormaliserSet);

		// SVG Denormaliser
		SvgDeNormaliser svgDenormaliser = new SvgDeNormaliser();
		Set<Type> svgDenormaliserSet = new HashSet<Type>();
		svgDenormaliserSet.add(new XenaSvgFileType());
		inputMap.put(svgDenormaliser, svgDenormaliserSet);

		return inputMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();

		// PNG
		PngToXenaPngNormaliser pngNormaliser = new PngToXenaPngNormaliser();
		Set<Type> pngNormaliserSet = new HashSet<Type>();
		pngNormaliserSet.add(new XenaPngFileType());
		outputMap.put(pngNormaliser, pngNormaliserSet);

		// JPEG
		JpegToXenaJpegNormaliser jpegNormaliser = new JpegToXenaJpegNormaliser();
		Set<Type> jpegNormaliserSet = new HashSet<Type>();
		jpegNormaliserSet.add(new XenaJpegFileType());
		outputMap.put(jpegNormaliser, jpegNormaliserSet);

		// Image (BMP and GIF)
		ImageToXenaPngNormaliser imageNormaliser = new ImageToXenaPngNormaliser();
		Set<Type> imageNormaliserSet = new HashSet<Type>();
		imageNormaliserSet.add(new XenaPngFileType());
		outputMap.put(imageNormaliser, imageNormaliserSet);

		// Legacy Image
		LegacyImageToXenaPngNormaliser legacyImageNormaliser = new LegacyImageToXenaPngNormaliser();
		Set<Type> legacyImageNormaliserSet = new HashSet<Type>();
		legacyImageNormaliserSet.add(new XenaPngFileType());
		outputMap.put(legacyImageNormaliser, legacyImageNormaliserSet);

		// PCX
		PcxToXenaPngNormaliser pcxNormaliser = new PcxToXenaPngNormaliser();
		Set<Type> pcxNormaliserSet = new HashSet<Type>();
		pcxNormaliserSet.add(new XenaPngFileType());
		outputMap.put(pcxNormaliser, pcxNormaliserSet);

		// SVG
		SvgNormaliser svgNormaliser = new SvgNormaliser();
		Set<Type> svgNormaliserSet = new HashSet<Type>();
		svgNormaliserSet.add(new XenaSvgFileType());
		outputMap.put(svgNormaliser, svgNormaliserSet);

		// TIFF
		TiffToXenaPngNormaliser tiffNormaliser = new TiffToXenaPngNormaliser();
		Set<Type> tiffNormaliserSet = new HashSet<Type>();
		tiffNormaliserSet.add(new XenaPngFileType());
		outputMap.put(tiffNormaliser, tiffNormaliserSet);

		// PNG Denormaliser
		XenaPngToPngDeNormaliser pngDenormaliser = new XenaPngToPngDeNormaliser();
		Set<Type> pngDenormaliserSet = new HashSet<Type>();
		pngDenormaliserSet.add(new PngFileType());
		outputMap.put(pngDenormaliser, pngDenormaliserSet);

		// JPEG Denormaliser
		XenaJpegToJpegDeNormaliser jpegDenormaliser = new XenaJpegToJpegDeNormaliser();
		Set<Type> jpegDenormaliserSet = new HashSet<Type>();
		jpegDenormaliserSet.add(new JpegFileType());
		outputMap.put(jpegDenormaliser, jpegDenormaliserSet);

		// SVG Denormaliser
		SvgDeNormaliser svgDenormaliser = new SvgDeNormaliser();
		Set<Type> svgDenormaliserSet = new HashSet<Type>();
		svgDenormaliserSet.add(new SvgFileType());
		outputMap.put(svgDenormaliser, svgDenormaliserSet);

		return outputMap;
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new XenaPngFileType());
		typeList.add(new PngFileType());
		typeList.add(new JpegFileType());
		typeList.add(new XenaJpegFileType());
		typeList.add(new SvgFileType());
		typeList.add(new XenaSvgFileType());
		typeList.add(new TiffFileType());
		typeList.add(new GifFileType());
		typeList.add(new BmpFileType());
		typeList.add(new XPMFileType());
		typeList.add(new CURFileType());
		typeList.add(new PSDFileType());
		typeList.add(new RASFileType());
		typeList.add(new PcxFileType());

		return typeList;
	}

	@Override
	public List<XenaView> getViews() {
		List<XenaView> viewList = new ArrayList<XenaView>();
		viewList.add(new PngView());
		viewList.add(new SvgView());
		return viewList;
	}

}
