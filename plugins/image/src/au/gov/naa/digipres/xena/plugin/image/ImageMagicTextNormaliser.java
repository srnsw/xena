/**
* This file is part of Xena - Image plugin.
*
* Xena is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Xena is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Network Manifest Checker.  If not, see <http://www.gnu.org/licenses/>.
* 
*/
package au.gov.naa.digipres.xena.plugin.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.plugin.image.tiff.TiffFileType;
import au.gov.naa.digipres.xena.plugin.image.tiff.TiffTextNormaliser;

/**
 * @author Matthew Oliver
 *
 */

public class ImageMagicTextNormaliser extends AbstractTextNormaliser {
	public static final String IMAGE_MAGIC_NORMALISER_NAME = "Image Magic Text Normaliser";
	private File tmpImageDir;

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser#getOutputFileExtension()
	 */
	@Override
	public String getOutputFileExtension() {
		return AbstractTextNormaliser.DEFAULT_TEXT_OUTPUT_FILE_EXTENSION;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return IMAGE_MAGIC_NORMALISER_NAME;
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#parse(org.xml.sax.InputSource, au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults)
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException {
		try {

			// NOTE: As this is a text normaliser we don't bother about the migrate only functionality. 

			if (!(input instanceof XenaInputSource)) {
				throw new XenaException("Can only normalise XenaInputSource objects.");
			}

			XenaInputSource xis = (XenaInputSource) input;

			// Get the Image Magick location property
			PluginManager pluginManager = normaliserManager.getPluginManager();
			PropertiesManager propManager = pluginManager.getPropertiesManager();
			String imageMagickPath = propManager.getPropertyValue(ImageProperties.IMAGE_PLUGIN_NAME, ImageProperties.IMAGEMAGIC_LOCATION_PROP_NAME);

			// if the xis is a stream not a file then lets write it out for image magick
			File imageFile = xis.getFile();
			if (imageFile == null) {
				imageFile = File.createTempFile("savedstram", xis.getFileNameExtension().toLowerCase());
				imageFile.deleteOnExit();

				InputStream inStream = xis.getByteStream();
				FileOutputStream outStream = new FileOutputStream(imageFile);
				byte[] buffer = new byte[10 * 1024];
				int bytesRead = inStream.read(buffer);
				while (bytesRead > 0) {
					outStream.write(buffer, 0, bytesRead);
					bytesRead = inStream.read(buffer);
				}
				outStream.flush();
				outStream.close();
				inStream.close();
			}

			// Use image magick to convert to tif. 
			List<File> images = imageMagickConvert(imageFile, imageMagickPath);

			// Create a TiffTextNormaliser to generate the text version
			TiffTextNormaliser textNormaliser = new TiffTextNormaliser();
			textNormaliser.setNormaliserManager(normaliserManager);
			textNormaliser.setContentHandler(getContentHandler());

			// If we have multiple images, we will link them in our normalised file using Multipage
			if (images.size() > 1) {
				XenaInputSource tifXis = new XenaInputSource(images.get(0), new TiffFileType());
				tifXis.setSystemId(xis.getSystemId());

				textNormaliser.parse(tifXis, results);
			} else {
				XenaInputSource tifXis = new XenaInputSource(images.get(0), new TiffFileType());
				tifXis.setSystemId(xis.getSystemId());

				textNormaliser.parse(tifXis, results);
			}

			//Cleanup temp directory
			cleanupTempDir();

		} catch (XenaException e) {
			throw new SAXException(e);
		}
	}

	private List<File> imageMagickConvert(File tiffFile, String binaryPath) throws SAXException {
		try {
			List<File> result = new ArrayList<File>();

			//Image magic automatically creates split images, we don't know how many so we dump them into an _EMPTY_ temp directory.
			// the names should be "<outputfilename-x>.png.

			tmpImageDir = File.createTempFile("imagedir", "dir");
			tmpImageDir.delete();
			tmpImageDir.mkdir();

			final String outputFileName = "out.tif";
			File outfile = new File(tmpImageDir, outputFileName);
			IMOperation op = new IMOperation();

			op.addImage(tiffFile.getAbsolutePath());
			op.alpha("off");
			op.addImage(outfile.getAbsolutePath());

			ConvertCmd convert = new ConvertCmd();

			// If we have a binaryPath then modify the command used, otherwise use default (PATH).
			if ((binaryPath != null) && (!binaryPath.equals(""))) {
				// Change the command.
				convert.clearCommand();
				convert.setCommand(binaryPath);
			}

			convert.run(op);

			// Get the files generated
			if (outfile.exists()) {
				// Only one file generated.
				result.add(outfile);
			} else {
				// More then one file generated.
				for (File file : tmpImageDir.listFiles()) {
					result.add(file);
				}

				Comparator<File> compare = new Comparator<File>() {

					private int getFileIndex(String filename, int index) {
						String numPart = "";
						for (int i = index; i < filename.length(); i++) {
							if (Character.isDigit(filename.charAt(i))) {
								numPart += filename.charAt(i);
							}
						}

						return Integer.parseInt(numPart);
					}

					@Override
					public int compare(File arg0, File arg1) {
						String part = outputFileName.substring(0, outputFileName.indexOf("."));
						String fname0 = arg0.getAbsolutePath();
						String fname1 = arg1.getAbsolutePath();

						int val0 = getFileIndex(fname0, fname0.lastIndexOf(part));
						int val1 = getFileIndex(fname1, fname1.lastIndexOf(part));

						return val0 - val1;
					}
				};

				Collections.sort(result, compare);
			}

			return result;

		} catch (Exception e) {
			e.printStackTrace();
			cleanupTempDir();
			throw new SAXException(e);
		}
	}

	private void cleanupTempDir() {
		if (tmpImageDir.exists()) {
			if (tmpImageDir.isDirectory()) {
				File[] files = tmpImageDir.listFiles();
				for (File file : files) {
					file.delete();
				}
			}
			tmpImageDir.delete();
		}
	}

	@Override
	public boolean isConvertible() {
		return false;
	}
}
