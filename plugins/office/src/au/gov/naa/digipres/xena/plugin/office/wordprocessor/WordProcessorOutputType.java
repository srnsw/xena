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
 * @author Jeff Stiff
 */

/*
 * Created on 23/05/2011 smeeheee
 * 
 */
package au.gov.naa.digipres.xena.plugin.office.wordprocessor;

/**
 * Class representing the output formats of a Word processor type document.
 * OutputType enumeration.
 *  
 * created 23/05/2011
 */
public class WordProcessorOutputType {
	//private enum outputTypes {
	public enum outputTypes {
		ODT("odt", "Open Office Document", "writer8"), PDF("pdf", "PDF Portable Document Format", "writer_pdf_Export"), HTML("html", "HTML Document",
		        "HTML (StarWriter)"), MSO2003XML("xml", "Microsoft Word 2003 XML", "MS Word 2003 XML"), MS02007XML("docx", "Microsoft Word 2007 XML",
		        "MS Word 2007 XML"), MSOXP("doc", "Microsoft Word 97/2000/XP", "MS Word 97"), RTF("rtf", "Rich Text Format", "Rich Text Format");
		// TODO: NOTE that these outputTypes are based on OOo 3.0 FilterLists

		private String fileExtensionValue;
		private String strName;
		private String outputFilterValue;

		outputTypes(String fileExtension, String strName, String outputFilter) {
			this.fileExtensionValue = fileExtension;
			this.strName = strName;
			this.outputFilterValue = outputFilter;
		}

		public String getFileExtensionValue() {
			return this.fileExtensionValue;
		}

		public String getOutputFilterValue() {
			return this.outputFilterValue;
		}

		public String getNameValue() {
			return this.strName;
		}

		public static outputTypes toOutputType(String str) {
			try {
				return valueOf(str.toUpperCase());
			} catch (Exception ex) {
				return ODT;
			}
		}
	}

	/**
	 * Returns the OutputType enum value from the given strName
	 * 
	 * @param strName - The name of the Output Type format
	 * @return Returns the first outputType with the matching name
	 */
	public static outputTypes getTypeFromName(String strName) {
		// TODO:  There must be a better way than this
		for (outputTypes out : outputTypes.values()) {
			if (out.getNameValue().equalsIgnoreCase(strName)) {
				return out;
			}
		}
		// Nothing found if we get to here, return the default
		return outputTypes.ODT;

	}

	/**
	 * Get the Open Office Converter filter for the given output type
	 * @param strOutputTypeName - The name of the outputType
	 * @return Returns the OpenOffice converter filter name as a string
	 */
	public static String getOfficeConverterName(String strOutputTypeName) {
		return getTypeFromName(strOutputTypeName).getOutputFilterValue();
	}

	/**
	 * Get the Open Office Converter filter for the given output type
	 * @param outType - The outputType being used
	 * @return Returns the OpenOffice converter filter name as a string
	 */
	public static String getOfficeConverterName(outputTypes outType) {
		return outType.getOutputFilterValue();
	}

	/**
	 * Get the file extension for the given output type
	 * @param outType - The outputType being used
	 * @return Returns the file extension for the destination file as a string
	 */
	public static String getOutputFileExtension(outputTypes outType) {
		return outType.getFileExtensionValue();
	}

	/**
	 * Get the file extension for the given output type
	 * @param strOutputTypeName - The name of the outputType
	 * @return Returns the file extension for the destination file as a string
	 */
	public static String getOutputFileExtension(String strOutputTypeName) {
		return getTypeFromName(strOutputTypeName).getFileExtensionValue();
	}

	/**
	 * Converts the string name of the OutputType to the enum OutputType
	 * 
	 * @param strOutType - The enum value name
	 * @return Returns the outputType
	 */
	public static outputTypes toOutputType(String strOutType) {
		return outputTypes.toOutputType(strOutType);
	}

}
