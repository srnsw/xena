/*
 * Created on 28/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.normalise;

/**
 * The Export result object represents the result of a Xena export.
 * It includes information about the output file, input file, and whether
 * the export was actually successful.
 * @author andrek24
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

    public ExportResult(){
        exportSuccessful = false;
    }
            
    public ExportResult(String inputFileName, String outputDirectoryName) {
        this.inputSysId = inputFileName;
        this.outputDirectoryName = outputDirectoryName;
        exportSuccessful = false;
    }

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
