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

package au.gov.naa.digipres.xena.plugin.office.wordprocessor;

import au.gov.naa.digipres.xena.plugin.office.OfficeFileType;

/**
 * Type to represent a Microsoft Word file.
 *
 */
public class WordFileType extends OfficeFileType {

	public WordFileType() {
	}

	/*private enum outputTypes {
		ODT, PDF, XML, DEFAULT;
		public static outputTypes toOutputType(String str) {
			try {
				return valueOf(str.toUpperCase());
			} catch (Exception ex) {
				return DEFAULT;
			}
		}
	}*/
	private enum outputTypes {
		ODT("odt", "writer8"), PDF("pdf", "writer_pdf_Export"), XML("xml", "unknown1"), DEFAULT("odt", "writer8");

		/*	ls.add("Open Office Document"); // Added to head of list
			ls.add("HTML Document");
			ls.add("LaTeX");
			ls.add("Microsoft Offic 2003 XML");
			ls.add("Microsoft Office 97/2000/XP");
			ls.add("PDF Portable Document Format");
			ls.add("Rich Text Format");
		*/
		private String fileExtensionValue;
		private String outputFilterValue;

		outputTypes(String fileExtension, String outputFilter) {
			this.fileExtensionValue = fileExtension;
			this.outputFilterValue = outputFilter;
		}

		public String getFileExtensionValue() {
			return this.fileExtensionValue;
		}

		public String getOutputFilterValue() {
			return this.outputFilterValue;
		}

		public static outputTypes toOutputType(String str) {
			try {
				return valueOf(str.toUpperCase());
			} catch (Exception ex) {
				return DEFAULT;
			}
		}
	}

	@Override
	public String getName() {
		return "Microsoft Word";
	}

	@Override
	public String getMimeType() {
		return "application/vnd.ms-word";
	}

	@Override
	public String getOfficeConverterName() {
		return "writer8";
	}

	@Override
	public String getOfficeConverterName(String strOutputTypeName) {
		//return outputTypes.toOutputType(strOutputType).getOutputFilterValue();
		//WordProcessorOutputType wpot = new WordProcessorOutputType;
		//return WordProcessorOutputType.getOfficeConverterName(WordProcessorOutputType.getTypeFromName(strOutputType));
		return WordProcessorOutputType.getOfficeConverterName(strOutputTypeName);
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension() {
		return "doc";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.office.OfficeFileType#getTextConverterName()
	 */
	@Override
	public String getTextConverterName() {
		return "Text";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.office.OfficeFileType#getODFExtension()
	 */
	@Override
	public String getODFExtension() {
		return "odt";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.office.OfficeFileType#getODFExtension()
	 */
	@Override
	public String getODFExtension(String strOutputTypeName) {
		//return outputTypes.toOutputType(strOutputType).getFileExtensionValue();
		return WordProcessorOutputType.getOutputFileExtension(strOutputTypeName);
	}

}
