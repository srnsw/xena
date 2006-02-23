/*
 * Created on 28/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.XMLFilter;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.XenaWrapper;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.UnknownType;

public class NormaliserResults {

    private String xenaVersion = Xena.getVersion();
    private boolean normalised = false;
    private AbstractNormaliser normaliser;
    private String inputSystemId;
    private Date inputLastModified;
    private Type inputType;
    private String outputFileName;
    private String destinationDirString;
    private FileNamer fileNamer;
    private XMLFilter wrapper;
    private String id;
    private String normaliserVersion;
    private boolean isChild;
    private String parentSystemId;
    
    
    private List<String> errorList = new ArrayList<String>();
    private List<Exception> exceptionList = new ArrayList<Exception>();
    
    public NormaliserResults() {
        normalised = false;
        normaliser = null;
        inputSystemId = null;
        inputType = new UnknownType();
        fileNamer = null;
        wrapper = null;
        id = null;
    }
    
    public NormaliserResults(XenaInputSource xis) {
        normalised = false;
        normaliser = null;
        inputSystemId = xis.getSystemId();
        inputLastModified = xis.getLastModified();
        inputType = new UnknownType();
        fileNamer = null;
        wrapper = null;
        id = null;
    }

    public NormaliserResults(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir, FileNamer fileNamer, XMLFilter wrapper) {
        normalised = false;
        this.normaliser = normaliser;
        this.normaliserVersion = normaliser.getVersion();
        this.inputSystemId = xis.getSystemId();
        this.inputType = xis.getType();
        this.inputLastModified = xis.getLastModified();
        this.destinationDirString = destinationDir.getAbsolutePath();
        this.fileNamer = fileNamer;
        this.wrapper = wrapper;
        this.id = null;
    }
    
    public String toString() {
        if (normalised) {
            return inputSystemId + " normalised to: " + outputFileName + " with normaliser: \"" + normaliser.getName() + "\" to the folder: " + destinationDirString + " id is:" + id;
        } else if (exceptionList.size() != 0) {
            return "The following exceptions were registered: " + getErrorMessages();
        } else {
            return inputSystemId + " NOT normalised, no apparant reason.";
        }
    }

    /**
     * @return Returns the inputSystemId.
     */
    public String getInputSystemId() {
        return inputSystemId;
    }

    /**
     * @param inputSystemId The inputSystemId to set.
     */
    public void setInputSystemId(String inputSystemId) {
        this.inputSystemId = inputSystemId;
    }

    /**
     * @return Returns the inputType.
     */
    public Type getInputType() {
        return inputType;
    }

    /**
     * @param inputType The inputType to set.
     */
    public void setInputType(Type inputType) {
        this.inputType = inputType;
    }

    /**
     * @return Returns the normalised flag.
     */
    public boolean isNormalised() {
        return normalised;
    }

    /**
     * @param normalised Set the normalised flag.
     */
    public void setNormalised(boolean normalised) {
        this.normalised = normalised;
    }

    /**
     * @return Returns the normaliser.
     */
    public AbstractNormaliser getNormaliser() {
        return normaliser;
    }

    /**
     * @param normaliser The normaliser to set.
     */
    public void setNormaliser(AbstractNormaliser normaliser) {
        this.normaliser = normaliser;
    }

    /**
     * @return Returns the outputFileNamer.
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * @param outputFileNamer The outputFileNamer to set.
     */
    public void setOutputFileName(String outputFileNamer) {
        this.outputFileName = outputFileNamer;
    }

    /**
     * @return Returns the destinationDirString.
     */
    public String getDestinationDirString() {
        return destinationDirString;
    }

    /**
     * @param destinationDirString The destinationDirString to set.
     */
    public void setDestinationDirString(String destinationDirString) {
        this.destinationDirString = destinationDirString;
    }
    
    public void addException(Exception e) {
        exceptionList.add(e);
    }

    public String getErrorMessages(){
        // find all our exception messages
        StringBuffer exceptions = new StringBuffer("");
        for (Iterator iter = exceptionList.iterator(); iter.hasNext();) {
            Exception e = (Exception) iter.next();
            if (exceptions.length() != 0) {
                exceptions.append(", ");
            }
            exceptions.append(e.getMessage());
        }
        
        //find all our error messages
        StringBuffer errors = new StringBuffer("");
        for (Iterator iter = errorList.iterator(); iter.hasNext();) {
            String errorMesg = (String) iter.next();
            if (errors.length() != 0) {
                errors.append(", ");
            }
            errors.append(errorMesg);
        }
        StringBuffer returnStringBuffer = new StringBuffer();

        if (exceptions.length() != 0) {
            returnStringBuffer.append("The following exceptions were logged: " + exceptions+ ". ");
        }
        if (errors.length() != 0) {
            returnStringBuffer.append("The following errors were logged: " + errors + ".");
        }
        return new String(returnStringBuffer);
    }
    
    /**
     * @return Returns the errorList.
     */
    public List<String> getErrorList() {
        return errorList;
    }

    /**
     * @return Returns the exceptionList.
     */
    public List<Exception> getExceptionList() {
        return exceptionList;
    }

    /**
     * @return Returns the fileNamer.
     */
    public FileNamer getFileNamer() {
        return fileNamer;
    }

    /**
     * @return Returns the wrapper.
     */
    public XMLFilter getWrapper() {
        return wrapper;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    public void initialiseId(File outputFile) {
        if ((wrapper == null) || (normalised == false)){
            return;
        }
        if (wrapper instanceof XenaWrapper) {
            XenaWrapper xenaWrapper = (XenaWrapper)wrapper;
            try {
                id = xenaWrapper.getSourceId(new XenaInputSource(outputFile));
            } catch (XenaException xe) {
                id = null;
                exceptionList.add(xe);
                errorList.add("Could not get the ID from the normalised file.");
            } catch (FileNotFoundException fnfe) {
                id = null;
                exceptionList.add(fnfe);
                errorList.add("Could not open the normalised file to get the ID.");
            }
        }
        
    }
    
    
    /**
     * @return Returns the normaliserVersion.
     */
    public String getNormaliserVersion() {
        return normaliserVersion;
    }

    /**
     * @return Returns the xenaVersion.
     */
    public String getXenaVersion() {
        return xenaVersion;
    }

    /**
     * @return Returns the inputLastModified.
     */
    public Date getInputLastModified() {
        return inputLastModified;
    }

    /**
     * @return Returns the isChild.
     */
    public boolean isChild() {
        return isChild;
    }

    /**
     * @param isChild The new value to set isChild to.
     */
    public void setChild(boolean isChild) {
        this.isChild = isChild;
    }

    /**
     * @return Returns the parentSystemId.
     */
    public String getParentSystemId() {
        return parentSystemId;
    }

    /**
     * @param parentSystemId The new value to set parentSystemId to.
     */
    public void setParentSystemId(String parentSystemId) {
        this.parentSystemId = parentSystemId;
    }
    
    
    
}
