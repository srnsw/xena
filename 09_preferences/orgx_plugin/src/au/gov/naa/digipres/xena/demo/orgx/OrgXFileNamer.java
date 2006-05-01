/*
 * Created on 16/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

public class OrgXFileNamer extends AbstractFileNamer {

    private InfoProvider myInfoProvider = null;
    
    private static char SEPARATOR_CHAR = '_';
    
    public String getName() {
        return "Org X FileNamer";
    }
    
    
    @Override
    public File makeNewXenaFile(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
        // auto generated method stub
        String userName = "unknown_user";
        String departmentCode = "UNK";
        
        if (myInfoProvider != null) {
            userName = myInfoProvider.getUserName();
            departmentCode = myInfoProvider.getDepartmentCode();    
        }
        
        String systemId = xis.getSystemId();
        int startOfFileName = systemId.lastIndexOf('/');
        String noSlashFileName = systemId.substring(startOfFileName == -1 ? 0 : startOfFileName);
        startOfFileName = noSlashFileName.lastIndexOf('\\');
        String fileName = noSlashFileName.substring(startOfFileName == -1 ? 0 : startOfFileName);
        
        int id = 0;

        DecimalFormat idFormatter = new DecimalFormat("0000");
        
        //now to make an insanely long file name with all this stuff...
        String outputFileName = noSlashFileName + SEPARATOR_CHAR + 
                                userName + SEPARATOR_CHAR +
                                departmentCode + SEPARATOR_CHAR +
                                idFormatter.format(id) +
                                "." + FileNamerManager.DEFAULT_EXTENSION;
        
        File outputFile = new File(destinationDir, outputFileName);
        while (outputFile.exists()) {
            outputFileName = noSlashFileName + SEPARATOR_CHAR + 
                            userName + SEPARATOR_CHAR +
                            departmentCode + SEPARATOR_CHAR + 
                            idFormatter.format(++id) +
                            "." + FileNamerManager.DEFAULT_EXTENSION;
            outputFile = new File(destinationDir, outputFileName);
        }
        return outputFile;
    }

    @Override
    public FileFilter makeFileFilter() {
        return FileNamerManager.DEFAULT_FILE_FILTER;
    }

    
    
    /**
     * @return Returns the myInfoProvider.
     */
    public InfoProvider getMyInfoProvider() {
        return myInfoProvider;
    }

    /**
     * @param myInfoProvider The new value to set myInfoProvider to.
     */
    public void setMyInfoProvider(InfoProvider myInfoProvider) {
        this.myInfoProvider = myInfoProvider;
    }

    
    
    
    
}
