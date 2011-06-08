/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 28/10/2005 andrek24
 */
package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import au.gov.naa.digipres.xena.kernel.XenaListener;

/**
 * The Export result object represents the result of a Xena export.
 * It includes information about the output file, input file, and whether
 * the export was actually successful.
 * created 17/04/2006
 * xena
 * Short desc of class:
 */
public class ExportResult implements XenaListener {

	private String inputSysId;
	private String sourceSysId;
	private String outputFileName;
	private String outputDirectoryName;
	private boolean exportSuccessful;

	private List<String> warnings;
	private final List<ExportResult> childExportResultList = new ArrayList<ExportResult>();

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
		warnings = new Vector<String>();
	}

	public ExportResult(String inputFileName, String outputDirectoryName) {
		inputSysId = inputFileName;
		this.outputDirectoryName = outputDirectoryName;
		exportSuccessful = false;
		warnings = new Vector<String>();
	}

	@Override
	public String toString() {
		StringBuilder rtn = new StringBuilder("input file name: " + inputSysId + "\nsuccess: " + exportSuccessful);

		if (exportSuccessful) {
			rtn.append("\noutput dir: " + outputDirectoryName + "\noutput file name: " + outputFileName);
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
		inputSysId = inputFileName;
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
	 * Return a handle to the output file
	 * @return
	 */
	public File getOutputFile() {
		if (outputFileName == null) {
			return null;
		}
		return new File(outputDirectoryName, outputFileName);
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
		sourceSysId = originalSysId;
	}

	/**
	 * @return the childExportResultList
	 */
	public List<ExportResult> getChildExportResultList() {
		return childExportResultList;
	}

	/**
	 * Add all of the ExportResults contained in the given list to childExportResultList
	 * @param exportResultList
	 */
	public void addChildResults(List<ExportResult> exportResultList) {
		if (exportResultList != null) {
			childExportResultList.addAll(exportResultList);
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.XenaListener#warning(java.lang.String)
	 */
	@Override
	public void warning(String warning) {
		warnings.add(warning);
	}

	public List<String> getWarnings() {
		return warnings;
	}

	/**
	 * Checks whether the result contains any warnings.
	 * @return
	 */
	public boolean hasWarnings() {
		return (warnings.size() > 0);
	}

}
