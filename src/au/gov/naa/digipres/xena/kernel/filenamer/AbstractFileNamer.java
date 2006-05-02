package au.gov.naa.digipres.xena.kernel.filenamer;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

/**
 * This is an abstract class to represent a Xena FileNamer. It's primary purpose is to create output files using 
 * a naming scheme that can be implemented by a specific institution.
 *
 * @see FileNamerManager
 * @author aak
 */
public abstract class AbstractFileNamer {

    /**
     * <p>This flag is used by concrete instances of this class during the creation of output files.</p>
     * 
     * <b>WARNING! This flag is not binding and may not necessarily be interrogated by the concrete implementation
     * of the makeNewXenaFile method!</b>
     */
    protected boolean overwrite;
    
    /**
     * This is the fileNamerManager, through which we can get a reference to the pluginManager and any other
     * component managers, as required.
     */
    protected FileNamerManager fileNamerManager;
   
    /**
     * Constructor for the abstract fileNamer. The default constructor is empty.
     */
    public AbstractFileNamer(){
    }

    /** 
     * Return the name of this fileNamer.
     * @return String - the name of the file namer.
     */
    public abstract String getName();

    /**
     * Make a new Xena File using the destination directory as set in the FileNamerManager. This is the preferred method
     * for creating new Xena files.
     * 
     * This method actually calls
     * <code>makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir)</code>
     * and simply passes it's arguments to it, along with the destination directory obtained from the fileNamerManager.
     *
     * @param xis - Input Xena Input source
     * @param normaliser - The normaliser that is being used during normalisation.
     * @return File - the new Xena File.
     * @throws XenaException 0 in the case of the Xena file not being able to be created.
     * @see AbstractFileNamer.makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir)
     */
    public File makeNewXenaFile(XenaInputSource xis, AbstractNormaliser normaliser) throws XenaException {
        return makeNewXenaFile(xis, normaliser, fileNamerManager.getDestinationDir());
    }
    
    /**
     * This abstract method creates a new Xena file. This must be implemented by any concrete file namers. The concrete
     * implmentation must create a file, based on some or all of the inputs (or none if you like!). A XenaException should
     * be thrown in the case of the file not being able to be created. Additionally, the flag 'overwrite' may be interrogated
     * or ignored depending on the implementation of the specific fileNamer.
     * 
     * @param input
     * @param normaliser
     * @param destinationDir
     * @return File - the new Xena file.
     * @throws XenaException - in the event of an error creating the file.
     */
    public abstract File makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir) throws XenaException;
    
    /**
     * Make fileFilter that a program calling Xena can use to filter files
     * @return FileFilter - a fileFilter to be used by Xena to ensure that 
     */
	public abstract FileFilter makeFileFilter();

    /**
     * @return Returns the overwrite flag
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * @param overwrite The overwrite to set.
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * @return Returns the fileNamerManager.
     */
    public FileNamerManager getFileNamerManager() {
        return fileNamerManager;
    }

    /**
     * @param fileNamerManager The new value to set fileNamerManager to.
     */
    public void setFileNamerManager(FileNamerManager fileNamerManager) {
        this.fileNamerManager = fileNamerManager;
    }
    
}
