/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author 
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.plugin.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.im4java.core.ConvertCmd;
import org.im4java.core.Operation;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.util.FileUtils;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

public class ImageMagicNormaliser extends AbstractNormaliser {

	final static String IMG_PREFIX = BasicImageNormaliser.PNG_PREFIX;
	final static String PNG_PREFIX = BasicImageNormaliser.PNG_PREFIX;
	final static String PNG_TAG = BasicImageNormaliser.PNG_TAG;
	final static String PNG_URI = BasicImageNormaliser.PNG_URI;

	final static String MULTIPAGE_PREFIX = "multipage";
	final static String MULTIPAGE_URI = "http://preservation.naa.gov.au/multipage/1.0";
	final static String METADATA_TAG = "metadata";

	private File tmpImageDir;

	@Override
	public String getName() {
		return "Image Magick Normaliser";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException {
		try {

			if (!(input instanceof XenaInputSource)) {
				throw new XenaException("Can only normalise XenaInputSource objects.");
			}

			XenaInputSource xis = (XenaInputSource) input;

			// Get the Image Magick location property
			PluginManager pluginManager = normaliserManager.getPluginManager();
			PropertiesManager propManager = pluginManager.getPropertiesManager();
			String imageMagickPath = propManager.getPropertyValue(ImageProperties.IMAGE_PLUGIN_NAME, ImageProperties.IMAGEMAGIC_LOCATION_PROP_NAME);

			// Use image magick to convert to PNG. 
			List<File> images = imageMagickConvert(xis.getFile(), imageMagickPath, results.getDestinationDirString(), migrateOnly);
			if (!migrateOnly) {

				// Continue normalisation

				// If we have multiple images, we will link them in our normalised file using Multipage
				if (images.size() > 1) {
					ContentHandler ch = getContentHandler();
					AttributesImpl att = new AttributesImpl();
					ch.startElement(MULTIPAGE_URI, "multipage", MULTIPAGE_PREFIX + ":multipage", att);

					for (int i = 0; i < images.size(); i++) {
						ch.startElement(MULTIPAGE_URI, "page", MULTIPAGE_PREFIX + ":page", att);
						outputImage(images.get(i));
						ch.endElement(MULTIPAGE_URI, "page", MULTIPAGE_PREFIX + ":page");

						// Add the input file checksum as a normaliser property so it can be picked up when we write the metadata. 
						addExportedChecksum(generateChecksum(images.get(i)));

					}
					ch.endElement(PNG_URI, "multipage", MULTIPAGE_PREFIX + ":multipage");
				} else {
					// Just a single image file
					outputImage(images.get(0));

					// Add the input file checksum as a normaliser property so it can be picked up when we write the metadata. 
					setExportedChecksum(generateChecksum(images.get(0)));
				}
			}
			//Cleanup temp directory
			cleanupTempDir();

		} catch (XenaException e) {
			throw new SAXException(e);
		}
	}

	private List<File> imageMagickConvert(File tiffFile, String binaryPath, String destinationDir, boolean migrateOnly) throws SAXException {
		try {
			List<File> result = new ArrayList<File>();

			//Image magic automatically creates split images, we don't know how many so we dump them into an _EMPTY_ temp directory.
			// the names should be "<outputfilename-x>.png.

			tmpImageDir = File.createTempFile("imagedir", "dir");
			tmpImageDir.delete();
			tmpImageDir.mkdir();

			final String outputFileName = "out.png";
			File outfile = new File(tmpImageDir, outputFileName);
			Operation op = new Operation();

			op.addImage(tiffFile.getAbsolutePath());
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
				if (migrateOnly) {
					FileUtils.fileCopy(outfile, destinationDir + File.separator + tiffFile.getName() + "-" + outfile.getName(), false);
				}
			} else {
				// More then one file generated.
				for (File file : tmpImageDir.listFiles()) {
					result.add(file);
					if (migrateOnly) {
						FileUtils.fileCopy(file, destinationDir + File.separator + tiffFile.getName() + "-" + file.getName(), false);
					}
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

	void outputImage(File image) throws SAXException, IOException {

		// Create our Xena normalised file
		AttributesImpl att = new AttributesImpl();
		att.addAttribute(PNG_URI, BasicImageNormaliser.DESCRIPTION_TAG_NAME, IMG_PREFIX + ":" + BasicImageNormaliser.DESCRIPTION_TAG_NAME, "CDATA",
		                 BasicImageNormaliser.PNG_DESCRIPTION_CONTENT);
		ContentHandler ch = getContentHandler();
		InputStream is = new FileInputStream(image);
		ch.startElement(PNG_URI, PNG_TAG, PNG_PREFIX + ":" + PNG_TAG, att);

		// Output the image data to our Xena file
		InputStreamEncoder.base64Encode(is, ch);

		ch.endElement(PNG_URI, PNG_TAG, PNG_PREFIX + ":" + PNG_TAG);
		is.close();
	}

	@Override
	public boolean isConvertible() {
		return true;
	}

	@Override
	public String getOutputFileExtension() {
		return BasicImageNormaliser.PNG_EXTENSION;
	}
}
