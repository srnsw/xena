/**
 * This file is part of pdf.
 * 
 * pdf is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * pdf is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with pdf; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.objects.PdfPageData;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractSearchableNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * @author Justin Waddell
 *
 */
public class PdfSearchableNormaliser extends AbstractSearchableNormaliser {

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractSearchableNormaliser#getOutputFileExtension()
	 */
	@Override
	public String getOutputFileExtension() {
		// Just the text will be extracted from the PDF document, so the extension is "txt"
		return "txt";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "PDF Searchable Normaliser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#parse(org.xml.sax.InputSource, au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults)
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException {
		XenaInputSource xis = (XenaInputSource) input;
		// JPedal only accepts an input file, so is the input source is a stream we will need to write it out.
		File originalFile;
		if (xis.getFile() == null) {
			originalFile = File.createTempFile("savedstream", ".pdf");
			originalFile.deleteOnExit();
			InputStream inStream = xis.getByteStream();
			FileOutputStream outStream = new FileOutputStream(originalFile);
			byte[] buffer = new byte[10 * 1024];
			int bytesRead = inStream.read(buffer);
			while (bytesRead > 0) {
				outStream.write(buffer, 0, bytesRead);
				bytesRead = inStream.read(buffer);
			}
			outStream.flush();
			outStream.close();
			inStream.close();
		} else {
			originalFile = xis.getFile();
		}

		// Initialise the PDF decoder, and pass it our PDF file.
		PdfDecoder decodePdf = null;
		try {
			// Initialise the PDF Decoder
			decodePdf = new PdfDecoder(false);

			// Both these are needed!
			PdfDecoder.useTextExtraction();
			decodePdf.setExtractionMode(PdfDecoder.TEXT); //extract just text		

			decodePdf.init(true);

			/**
			 * open the file (and read metadata including pages in  file)
			 */
			decodePdf.openPdfFile(originalFile.getAbsolutePath());

		} catch (PdfSecurityException se) {
			throw new IOException("Security exception when attempting to initialise extraction from PDF file " + originalFile.getAbsolutePath(), se);
		} catch (PdfException se) {
			throw new IOException("Exception when attempting to initialise extraction from PDF file " + originalFile.getAbsolutePath(), se);
		}

		/**
		 * extract data from pdf (if allowed). 
		 */
		if (!decodePdf.isExtractionAllowed()) {
			throw new IOException("Extraction is not allowed from PDF file " + originalFile.getAbsolutePath());
		} else if (decodePdf.isEncrypted() && !decodePdf.isPasswordSupplied()) {
			throw new IOException("Cannot extract from encrypted PDF file " + originalFile.getAbsolutePath());
		} else {
			//page range
			int start = 1, end = decodePdf.getPageCount();

			ContentHandler contentHandler = getContentHandler();
			/**
			 * extract data from pdf
			 */
			int page = -1;
			try {
				for (page = start; page < end + 1; page++) { //read pages

					//decode the page
					decodePdf.decodePage(page);

					/** create a grouping object to apply grouping to data*/
					PdfGroupingAlgorithms currentGrouping = decodePdf.getGroupingObject();

					/** use whole page size */
					PdfPageData currentPageData = decodePdf.getPdfPageData();

					int x1 = currentPageData.getMediaBoxX(page);
					int x2 = currentPageData.getMediaBoxWidth(page) + x1;

					int y2 = currentPageData.getMediaBoxY(page);
					int y1 = currentPageData.getMediaBoxHeight(page) + y2;

					/**Co-ordinates are x1,y1 (top left hand corner), x2,y2(bottom right) */

					/**The call to extract the text*/
					String text = currentGrouping.extractTextInRectangle(x1, y1, x2, y2, page, false, true);

					// Write out the page's text to our content handler
					if (text != null) {
						contentHandler.characters(text.toCharArray(), 0, text.length());

						// Insert a break between pages
						String pageBreak = "\n\n";
						contentHandler.characters(pageBreak.toCharArray(), 0, pageBreak.length());
					}

					//remove data once written out
					decodePdf.flushObjectValues(false);
				}
			} catch (Exception e) {
				decodePdf.closePdfFile();
				throw new IOException("Exception when extracting text from page " + page + " of PDF file " + originalFile.getAbsolutePath(), e);
			}

			/**
			 * flush data structures - not strictly required but included
			 * as example
			 */
			decodePdf.flushObjectValues(true); //flush any text data read

		}

		/**close the pdf file*/
		decodePdf.closePdfFile();

	}
}
