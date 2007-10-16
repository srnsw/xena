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

/*
 * Created on 28/10/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.normalise;

/**
 * The Export result object represents the result of a Xena export.
 * It includes information about the output file, input file, and whether
 * the export was actually successful.
 * created 17/04/2006
 * xena
 * Short desc of class:
 */
public class ExportResult {

	private String inputSysId;
	private String sourceSysId;
	private String outputFileName;
	private String outputDirectoryName;
	private boolean exportSuccessful;

	/**
	 * @return Returns the exportSuccessful.
	 */
	public boolean isExportSuccessful() {
		return exportSuccessful;
	}

	/**
	 * @param exportSuccessful The new value to set exportSuccessful to.
	 */
	public void setExportSuccessful(boolean exportSuccessful) {
		this.exportSuccessful = exportSuccessful;
	}

	public ExportResult() {
		exportSuccessful = false;
	}

	public ExportResult(String inputFileName, String outputDirectoryName) {
		this.inputSysId = inputFileName;
		this.outputDirectoryName = outputDirectoryName;
		exportSuccessful = false;
	}

	@Override
    public String toString() {
		StringBuilder rtn = new StringBuilder("input file name: " + inputSysId + " success: " + exportSuccessful);

		if (exportSuccessful) {
			rtn.append(" output dir: " + outputDirectoryName + " output file name: " + outputFileName);
		}
		return new String(rtn);
	}

	/**
	 * @return Returns the inputFileName.
	 */
	public String getInputSysId() {
		return inputSysId;
	}

	/**
	 * @param inputFileName The new value to set inputFileName to.
	 */
	public void setInputSysId(String inputFileName) {
		this.inputSysId = inputFileName;
	}

	/**
	 * @return Returns the outputDirectoryName.
	 */
	public String getOutputDirectoryName() {
		return outputDirectoryName;
	}

	/**
	 * @param outputDirectoryName The new value to set outputDirectoryName to.
	 */
	public void setOutputDirectoryName(String outputDirectoryName) {
		this.outputDirectoryName = outputDirectoryName;
	}

	/**
	 * @return Returns the outputFileName.
	 */
	public String getOutputFileName() {
		return outputFileName;
	}

	/**
	 * @param outputFileName The new value to set outputFileName to.
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	/**
	 * @return Returns the originalSysId.
	 */
	public String getSourceSysId() {
		return sourceSysId;
	}

	/**
	 * @param originalSysId The new value to set originalSysId to.
	 */
	public void setSourceSysId(String originalSysId) {
		this.sourceSysId = originalSysId;
	}

}
