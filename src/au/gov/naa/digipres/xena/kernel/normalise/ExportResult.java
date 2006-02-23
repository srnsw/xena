/*
 * Created on 28/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.normalise;

public class ExportResult {

    private String inputFileName;
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
        this.inputFileName = inputFileName;
        this.outputDirectoryName = outputDirectoryName;
        exportSuccessful = false;
    }

    public String toString() {
        StringBuilder rtn = new StringBuilder("input file name: " + inputFileName + " success: " + exportSuccessful);
        
        if (exportSuccessful) {
            rtn.append(" output dir: " + outputDirectoryName + " output file name: " + outputFileName);
        }
        return new String(rtn);
    }
    
    /**
     * @return Returns the inputFileName.
     */
    public String getInputFileName() {
        return inputFileName;
    }

    /**
     * @param inputFileName The new value to set inputFileName to.
     */
    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
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
    

}
