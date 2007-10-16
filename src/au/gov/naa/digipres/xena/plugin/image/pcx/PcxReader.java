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
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.image.pcx;

/*
 * PcxReader: This is a class which provides a method for reading PCX-Files. The PCX-Format is a Image-File-Format which
 * was developed by ZSoft.
 * 
 * PcxReader, Version 1.10 [05/02/2003] Copyright (c) 2000-2003 by Matthias Burg All rights reserved eMail:
 * Matthias@burgsoft.de Internet: www.burgsoft.de
 * 
 * The PcxReader is Freeware. You can use and copy it without any fee. The author is not responsible for any damages
 * which are caused by this software. You find further information about the Java-Technology on the Sun- Website
 * (Internet: www.sun.com).
 * 
 * At the moment PcxReader supports the following File-Formats: - PCX Files with 1 Bit black & white Colors (since
 * Version 1.1) - PCX Version 3.0 with 8 Bit (=256) Colors (since Version 1.0) - PCX Version 3.0 with 24 Bit (=16.7 Mio)
 * Colors (since Version 1.0)
 * 
 * The PcxReader needs an opened InputStream with the PCX-Data as Argument. The return-value is the loaded Image. You
 * can use the PcxReader in a Java-Application as well as in a Java-Applet
 * 
 * If you have questions or tips for the PcxReader, please write an eMail.
 */

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PcxReader {
	/*
	 * This is the main-class of the PcxReader. It reads the PCX-Data from a stream and converts it into an Image-Class.
	 */

	public static final int NORMAL = 1;
	public static final int RLE = 2;

	private static int runCount = 0;
	private static int runValue = 0;

	public static Image decodeImage(InputStream in) throws IOException {
		int pcxheight, pcxwidth;
		Image picture = null;

		runCount = 0;
		runValue = 0;

		// Header-Data
		int manufacturer;
		int version;
		int encoding;
		int bits_per_pixel;
		int xmin, ymin;
		int xmax, ymax;
		int hres;
		int vres;
		int[] palette16RGB = new int[16];
		int reserved;
		int colour_planes;
		int bytes_per_line;
		int palette_type;
		byte[] filler = new byte[58];

		/*
		 * In the beginning the Image-Data is read as in the PCX- specification.
		 */
		manufacturer = in.read();
		version = in.read();
		encoding = in.read();
		bits_per_pixel = in.read();

		xmin = in.read() + in.read() * 256;
		ymin = in.read() + in.read() * 256;
		xmax = in.read() + in.read() * 256;
		ymax = in.read() + in.read() * 256;
		hres = in.read() + in.read() * 256;
		vres = in.read() + in.read() * 256;

		for (int i = 0; i < palette16RGB.length; i++) {
			int paletteRed = in.read();
			int paletteGreen = in.read();
			int paletteBlue = in.read();
			palette16RGB[i] = new Color(paletteRed, paletteGreen, paletteBlue).getRGB();
		}

		reserved = in.read();
		colour_planes = in.read();
		bytes_per_line = in.read() + in.read() * 256;
		palette_type = (short) (in.read() + in.read() * 256);
		in.read(filler);

		pcxwidth = 1 + xmax - xmin;
		pcxheight = 1 + ymax - ymin;
		if (pcxwidth % 2 == 1) {
			/*
			 * The width of an PCX-Image must be even. That is why the width is increased when it was odd before.
			 */
			pcxwidth++;
		}

		int scanLineLength = colour_planes * bytes_per_line;

		// notout
		// System.out.println("max colours: " + (1 << (bits_per_pixel * colour_planes)));
		// System.out.println("Bits per pixel: " + bits_per_pixel);
		// System.out.println("width: " + pcxwidth +
		// ", height: " + pcxheight +
		// ", bytesperline: " + bytes_per_line +
		// ", colorplanes: " + colour_planes +
		// ", scanLineLength: " + scanLineLength);

		int imagePixels;

		if (bits_per_pixel == 8 && colour_planes == 1) {
			/*
			 * If the PCX-file has 256 colors there is a color-palete at the end of the file. This is 768b bytes long
			 * and contains the red- green- and blue-values of the colors.
			 */
			byte[] pal = new byte[768];
			int[] intPal = new int[768];

			imagePixels = (pcxwidth * pcxheight);
			int[] imageData = new int[imagePixels];

			for (int i = 0; i < pcxheight; i++) {
				int[] scanLineBuffer = new int[scanLineLength];
				decodeScanLine(scanLineBuffer, in);
				for (int j = 0; j < pcxwidth; j++) {
					imageData[i * pcxwidth + j] = scanLineBuffer[j];
				}
			}

			if (in.available() > 769) {
				while (in.available() > 769) {
					in.read();
				}
			}

			// Bit of a hack here... the scan line reader appears to sometimes reads one byte too many.
			// But if we have 768 or 769 bytes left it's more than likely we have a VGA palette.
			// Otherwise, assuming each byte of the data is a greyscale value
			// (ie assigning that value to each RGB component) seems to work.
			int availableBytes = in.available();
			if (availableBytes < 768 || availableBytes > 769) {
				// Assume greyscale
				for (int y = 0; y < 256; y++) {
					intPal[3 * y] = intPal[3 * y + 1] = intPal[3 * y + 2] = y;
				}
			} else {
				if (availableBytes == 769) {
					// Read palette marker
					in.read();
				}

				in.read(pal);
				in.close();

				for (int y = 0; y < 767; y++) {
					intPal[y] = (pal[y]);
					if (intPal[y] < 0) {
						intPal[y] += 256;
					}
				}
			}

			/*
			 * Now the PcxReader converts the imagedata into the format of a MemoryImageSource. Using the same imageData
			 * array to save memory.
			 */

			for (int i = 0; i < imagePixels; i++) {
				int paletteEntry = (imageData[i]);
				if (paletteEntry < 0)
					paletteEntry += 256;
				imageData[i] = new Color(intPal[paletteEntry * 3], intPal[paletteEntry * 3 + 1], intPal[paletteEntry * 3 + 2]).getRGB();
			}

			ImageProducer prod = new MemoryImageSource(pcxwidth, pcxheight, imageData, 0, pcxwidth);
			picture = Toolkit.getDefaultToolkit().createImage(prod);

			// Might help with garbage collection?
			imageData = null;

		} else if (bits_per_pixel == 8 && colour_planes == 3) {
			/*
			 * If the picture has 24 bit colors, there are 3 times many bytes as many pixels.
			 */

			imagePixels = (pcxwidth * pcxheight);
			int[] imageData = new int[imagePixels * 3];

			for (int i = 0; i < pcxheight; i++) {
				int[] scanLineBuffer = new int[scanLineLength];
				decodeScanLine(scanLineBuffer, in);
				for (int j = 0; j < pcxwidth * 3; j++) {
					imageData[i * pcxwidth * 3 + j] = scanLineBuffer[j];
				}
			}

			in.close();

			int RGBImageData[] = new int[imagePixels];
			for (int i = 0; i < pcxheight; i++) {
				for (int j = 0; j < pcxwidth; j++) {
					int red = imageData[i * 3 * pcxwidth + j];
					int green = imageData[((i * 3) + 1) * pcxwidth + j];
					int blue = imageData[((i * 3) + 2) * pcxwidth + j];
					RGBImageData[i * pcxwidth + j] = new Color(red, green, blue).getRGB();
				}
			}

			ImageProducer prod = new MemoryImageSource(pcxwidth, pcxheight, RGBImageData, 0, pcxwidth);
			picture = Toolkit.getDefaultToolkit().createImage(prod);

			// Might help with garbage collection?
			RGBImageData = null;
			imageData = null;

		} else if (bits_per_pixel == 1 && colour_planes == 1) {
			/*
			 * This is a new feature in Version 1.1 (May 2003) Now the PCX Reader is also able to read b&w images.
			 */
			imagePixels = (bytes_per_line * pcxheight);
			int[] imageData = new int[imagePixels];

			for (int i = 0; i < pcxheight; i++) {
				int[] scanLineBuffer = new int[scanLineLength];
				decodeScanLine(scanLineBuffer, in);
				for (int j = 0; j < bytes_per_line; j++) {
					imageData[i * bytes_per_line + j] = scanLineBuffer[j];
				}
			}

			in.close();

			int RGBImageData[] = new int[pcxwidth * pcxheight];
			int width = 0;
			int height = 0;
			for (int i = 0; i < imagePixels; i++) {
				int k = 128;
				while (k > 0) {
					if ((imageData[i] & k) == 0) {
						RGBImageData[(pcxwidth * height) + width] = new Color(0, 0, 0).getRGB();
					} else {
						RGBImageData[(pcxwidth * height) + width] = new Color(255, 255, 255).getRGB();
					}
					k = k / 2;
					if (++width == pcxwidth) {
						k = 0;
						width = 0;
						height++;

						/*
						 * PCX lines must terminate on bit 16 Only whole bytes can be read
						 */
						if (i % 2 == 0) {
							i++;
						}
					}
				}
			}
			ImageProducer prod = new MemoryImageSource(pcxwidth, pcxheight, RGBImageData, 0, pcxwidth);
			picture = Toolkit.getDefaultToolkit().createImage(prod);

			// Might help with garbage collection?
			RGBImageData = null;
			imageData = null;

		} else if (bits_per_pixel == 1 && colour_planes == 4) {
			// EGA 16 colour palette

			imagePixels = (pcxwidth * pcxheight);
			int[][] imageData = new int[imagePixels][3];

			// For each scan line
			for (int line = 0; line < pcxheight; line++) {
				int[] scanLineBuffer = new int[scanLineLength];
				decodeScanLine(scanLineBuffer, in);
				int planePixelCount;

				// For each colour plane (not including itensity)
				for (int planeIndex = 0; planeIndex < 3; planeIndex++) {
					planePixelCount = 0;

					// For each byte in this colour plane
					for (int planeByte = 0; planeByte < bytes_per_line; planeByte++) {
						int currByte = scanLineBuffer[planeIndex * bytes_per_line + planeByte];

						// Each bit in this byte indicates if the current pixel occurs in this colour plane
						for (int bitCount = 7; bitCount >= 0 && planePixelCount < pcxwidth; bitCount--) {
							if ((currByte & (1 << bitCount)) != 0) {
								// Default value is 128 (this is doubled if the pixel is in the intensity plane)
								imageData[line * pcxwidth + planePixelCount][planeIndex] = 128;
							} else {
								imageData[line * pcxwidth + planePixelCount][planeIndex] = 0;
							}
							planePixelCount++;
						}
					}
				}

				// Intensity
				planePixelCount = 0;

				// For each byte in this colour plane
				for (int planeByte = 0; planeByte < bytes_per_line; planeByte++) {
					int currByte = scanLineBuffer[3 * bytes_per_line + planeByte];
					for (int bitCount = 7; bitCount >= 0 && planePixelCount < pcxwidth; bitCount--) {
						if ((currByte & (1 << bitCount)) != 0) {
							for (int planeIndex = 0; planeIndex < 3; planeIndex++) {
								// Double RGB value if pixel is in intensity plane
								imageData[line * pcxwidth + planePixelCount][planeIndex] *= 2;

								// Ensure value is not over 255
								if (imageData[line * pcxwidth + planePixelCount][planeIndex] > 255) {
									imageData[line * pcxwidth + planePixelCount][planeIndex] = 255;
								}
							}
						}
						planePixelCount++;
					}
				}
			}

			in.close();

			int rgbImageData[] = new int[pcxwidth * pcxheight];
			for (int i = 0; i < imagePixels; i++) {
				rgbImageData[i] = new Color(imageData[i][0], imageData[i][1], imageData[i][2]).getRGB();
			}
			ImageProducer prod = new MemoryImageSource(pcxwidth, pcxheight, rgbImageData, 0, pcxwidth);
			picture = Toolkit.getDefaultToolkit().createImage(prod);

			// Might help with garbage collection?
			rgbImageData = null;
			imageData = null;

		} else if (bits_per_pixel == 4 && colour_planes == 1) {
			// 16-colour palette in header
			imagePixels = (pcxwidth * pcxheight);
			int rgbImageData[] = new int[imagePixels];
			for (int i = 0; i < pcxheight; i++) {
				int[] scanLineBuffer = new int[scanLineLength];
				decodeScanLine(scanLineBuffer, in);
				int scanLineIndex = 0;
				for (int j = 0; j < pcxwidth; j += 2) {
					int paletteIndices = scanLineBuffer[scanLineIndex];

					// First index
					int paletteIndex = paletteIndices >> 4;
					rgbImageData[i * pcxwidth + j] = palette16RGB[paletteIndex];

					// Second index
					paletteIndex = paletteIndices & 0x0F;
					rgbImageData[i * pcxwidth + j + 1] = palette16RGB[paletteIndex];

					scanLineIndex++;
				}
			}
			ImageProducer prod = new MemoryImageSource(pcxwidth, pcxheight, rgbImageData, 0, pcxwidth);
			picture = Toolkit.getDefaultToolkit().createImage(prod);

			// Might help with garbage collection?
			rgbImageData = null;
		} else {
			throw new IOException("PCX Images with " + colour_planes + " colour planes and " + bits_per_pixel
			                      + "bits per pixel are not currently supported");
		}

		return picture;
	}

	private static int decodeScanLine(int[] scanLineBuffer, InputStream in) throws IOException {
		int index = 0;
		int total = 0;
		int currByte;

		do {
			/* Write the pixel run to the buffer */
			for (total += runCount; /* Update total */
			runCount > 0 && index < scanLineBuffer.length; /* Don't read past buffer */
			runCount--, index++)
				scanLineBuffer[index] = runValue; /* Assign value to buffer */

			if (runCount > 0) /* Encoded run ran past end of scan line */
			{
				total -= runCount; /* Subtract count not written to buffer */
				return total; /* Return number of pixels decoded */
			}

			/*
			 * * Get the next encoded run packet. * * Read a byte of data. If the two MSBs are 1 then this byte * holds
			 * a count value (0 to 63) and the following byte is the * data value to be repeated. If the two MSBs are 0
			 * then the * count is one and the byte is the data value itself.
			 */
			currByte = in.read(); /* Get next byte */

			if ((currByte & 0xC0) == 0xC0) /* Two-byte code */
			{
				runCount = currByte & 0x3F; /* Get run count */
				runValue = in.read(); /* Get pixel value */
			} else /* One byte code */
			{
				runCount = 1; /* Run count is one */
				runValue = currByte; /* Pixel value */
			}
		} while (index < scanLineBuffer.length); /* Read until the end of the buffer */

		return total; /* Return number of pixels decoded */

	}

	private static void displayPalette(int[] rgbPalette) {
		JPanel palettePanel = new JPanel(new GridLayout(1, rgbPalette.length));
		for (int i = 0; i < rgbPalette.length; i++) {
			JLabel colourLabel = new JLabel("    ");
			colourLabel.setBackground(new Color(rgbPalette[i]));
			colourLabel.setOpaque(true);
			palettePanel.add(colourLabel);
		}
		JFrame paletteFrame = new JFrame("Palette");
		paletteFrame.add(palettePanel);
		paletteFrame.pack();
		paletteFrame.setVisible(true);
	}

}
