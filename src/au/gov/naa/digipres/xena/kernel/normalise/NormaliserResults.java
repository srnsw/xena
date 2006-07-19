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
import java.util.List;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperPlugin;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.UnknownType;

/**
 * @author Andrew Keeling
 * @author Justin Waddell
 * 
 * <p>
 * created 31/03/2006
 * </p>
 * <p>
 * xena
 * </p>
 * <p>
 * This class encapsulates the results of the normalisation process for a Xena
 * Input source.
 * </p>
 * <p>
 * When a XenaInputSource is to be normalised, a number of things may happen to
 * it:
 * <ul>
 * <li>It is normalised properly.</li>
 * <li>There is a problem during normalisation.</li>
 * <li>It is found to be a 'child' of another XenaInputSource</li>
 * </ul>
 * 
 * <p>
 * When a XenaInputSource is normalised correctly, in the
 * <code>normalised</code> flag will be set to <code>true</code>. In this
 * case, the following information is set in the results object:
 * <ul>
 * <li>normaliser information</li>
 * <li>the version of Xena</li>
 * <li>the type of the XenaInputSource</li>
 * <li>FileNamer</li>
 * <li>MetaDataWrapper</li>
 * <li>Xena ID</li>
 * </ul>
 * </p>
 * <p>
 * If an error occurs during normalising, then the normalised flag should be set
 * to false. In this case, if an exception has been thrown or an error condition
 * has arisen, these should be appended to the <code>errorList</code> or
 * <code>exceptionList</code> as appropriate. If this normaliser results
 * object corresponds to a XenaInputSource that will be embedded in the output
 * of another XenaInputSource when it is normalised, then this will be reflected
 * in the results object, by setting the <code>isChild</code> flag to true,
 * and setting the <code>parentSystemId</code>.
 * 
 * 
 * 
 */

public class NormaliserResults {

    private String xenaVersion = Xena.getVersion();

    private boolean normalised = false;

    private String normaliserName;

    private String inputSystemId;

    private Date inputLastModified;

    private Type inputType;

    private String outputFileName;

    private String destinationDirString;

    private AbstractFileNamer fileNamer;

    private String wrapperName;

    private String id;
    
    private String normaliserClassName = null;

    private String normaliserVersion;

    private boolean isChild;

    private String parentSystemId;

    private List<String> errorList = new ArrayList<String>();

    private List<Exception> exceptionList = new ArrayList<Exception>();

    private List<NormaliserResults> childAIPResults = new ArrayList<NormaliserResults>();

    private List<NormaliserResults> dataObjectComponentResults = new ArrayList<NormaliserResults>();

    /**
     * Default Constructor - initialise values to null, unknown, or false.
     * 
     */
    public NormaliserResults() {
        normalised = false;
        normaliserName = null;
        inputSystemId = null;
        inputType = new UnknownType();
        fileNamer = null;
        wrapperName = null;
        id = null;
    }

    /**
     * Constructor with XenaInputSource. Initialise results to default values.
     */
    public NormaliserResults(XenaInputSource xis) {
        normalised = false;
        normaliserName = null;
        inputSystemId = xis.getSystemId();
        inputLastModified = xis.getLastModified();
        inputType = new UnknownType();
        fileNamer = null;
        wrapperName = null;
        id = null;
    }

    /**
     * Construtcor containing values to set results to. Fields still initialised
     * as required.
     * 
     * @param xis
     * @param normaliser
     * @param destinationDir
     * @param fileNamer
     * @param wrapper
     */
    public NormaliserResults(XenaInputSource xis,
            AbstractNormaliser normaliser, File destinationDir,
            AbstractFileNamer fileNamer, AbstractMetaDataWrapper wrapper) {
        normalised = false;
        this.normaliserName = normaliser.getName();
        this.normaliserVersion = normaliser.getVersion();
        this.normaliserClassName = normaliser.getClass().getName();
        this.inputSystemId = xis.getSystemId();
        this.inputType = xis.getType();
        this.inputLastModified = xis.getLastModified();
        this.destinationDirString = destinationDir.getAbsolutePath();
        this.fileNamer = fileNamer;
        this.wrapperName = wrapper.getName();
        this.id = null;
    }

    /**
     * Return a verbose description of the current normaliser results.
     * 
     * @return String representation of these results.
     */
    public String getResultsDetails() {
        if (normalised) {
            return "Normalisation successful."
                    + System.getProperty("line.separator")
                    + "The input source name " + inputSystemId
                    + System.getProperty("line.separator") + "normalised to: "
                    + outputFileName + System.getProperty("line.separator")
                    + "with normaliser: \"" + normaliserName + "\""
                    + System.getProperty("line.separator") + "to the folder: "
                    + destinationDirString
                    + System.getProperty("line.separator")
                    + "and the Xena id is: " + id;
        } else if (exceptionList.size() != 0) {
            return "The following exceptions were registered: "
                    + getErrorDetails();
        } else {
            if (inputSystemId != null) {
                return inputSystemId
                        + " is NOT normalised, and no exceptions have been registered.";
            }
        }
        return "This results object is not initialised yet.";
    }
    
    

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getOutputFileName();
	}

	/**
     * @return Returns the inputSystemId.
     */
    public String getInputSystemId() {
        return inputSystemId;
    }

    /**
     * @param inputSystemId
     *            The inputSystemId to set.
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
     * @param inputType
     *            The inputType to set.
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
     * @param normalised
     *            Set the normalised flag.
     */
    public void setNormalised(boolean normalised) {
        this.normalised = normalised;
    }

    /**
     * @return Returns the normaliser.
     */
    public String getNormaliserName() {
        return normaliserName;
    }

    /**
     * @param normaliser
     *            The normaliser to set.
     */
    public void setNormaliser(AbstractNormaliser normaliser) {
        this.normaliserName = normaliser.getName();
        this.normaliserVersion = normaliser.getVersion();
        this.normaliserClassName = normaliser.getClass().getName();
    }

    public void setNormaliserName(String normaliserName) {
        this.normaliserName = normaliserName;
    }
    
    /**
     * @return Returns the outputFileNamer.
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * @param outputFileNamer
     *            The outputFileName to set.
     */
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    /**
     * @return Returns the destinationDirString.
     */
    public String getDestinationDirString() {
        return destinationDirString;
    }

    /**
     * @param destinationDirString
     *            The destinationDirString to set.
     */
    public void setDestinationDirString(String destinationDirString) {
        this.destinationDirString = destinationDirString;
    }

    public void addException(Exception e) {
        exceptionList.add(e);
    }

    public String getErrorDetails() {
        // find all our exception messages
        StringBuffer exceptions = new StringBuffer();
        for (Exception e : exceptionList) {
            exceptions.append(e.getMessage() + "\n");

            StackTraceElement[] steArr = e.getStackTrace();
            if (steArr.length > 0) {
                exceptions.append("Trace:\n");
                for (int i = 0; i < steArr.length; i++) {
                    exceptions.append(steArr[i].toString() + "\n");
                }
            }
        }

        // find all our error messages
        StringBuffer errors = new StringBuffer();
        for (String errorMesg : errorList) {
            errors.append(errorMesg);
        }
        StringBuffer returnStringBuffer = new StringBuffer();

        if (exceptions.length() != 0) {
            returnStringBuffer.append(exceptions + "\n");
        }
        if (errors.length() != 0) {
            returnStringBuffer.append(errors);
        }
        return new String(returnStringBuffer);
    }

    // Returns the message for the first exception or error, or an empty string
    // if no errors have occurred.
    public String getErrorMessage() {
        String message = "";
        if (!exceptionList.isEmpty()) {
            message = "Exception: " + exceptionList.get(0).getMessage();
        } else if (!errorList.isEmpty()) {
            message = "Error: " + errorList.get(0);
        }
        return message;
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
    public AbstractFileNamer getFileNamer() {
        return fileNamer;
    }

    /**
     * @return Returns the wrapper.
     */
    public String getWrapperName() {
        return wrapperName;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the id of a given file.
     * to find the Id.
     * 
     * @param outputFile
     * 
     * @deprecated
     */
    @Deprecated
    public void initialiseId(File outputFile, MetaDataWrapperManager metaDataWrapperManager) {
        if ((wrapperName == null) || (normalised == false)) {
            return;
        }
        MetaDataWrapperPlugin wrapperPlugin = metaDataWrapperManager.getMetaDataWrapperPluginByName(wrapperName);
        if (wrapperPlugin != null) {
            try {
                AbstractMetaDataWrapper xenaWrapper = wrapperPlugin.getWrapper();
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
        } else {
            id = null;
            errorList.add("Could not get the ID from the normalised file.");
        }
    }

    public void setNormaliserVersion(String normaliserVersion) {
        this.normaliserVersion = normaliserVersion;
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
     * @param isChild
     *            The new value to set isChild to.
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
     * @param parentSystemId
     *            The new value to set parentSystemId to.
     */
    public void setParentSystemId(String parentSystemId) {
        this.parentSystemId = parentSystemId;
    }

    /**
     * @return Returns the childAIPResults.
     */
    public List<NormaliserResults> getChildAIPResults() {
        return childAIPResults;
    }

    /**
     * @return Returns the dataObjectComponentResults.
     */
    public List<NormaliserResults> getDataObjectComponentResults() {
        return dataObjectComponentResults;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#add(E)
     */
    public boolean addChildAIPResult(NormaliserResults o) {
        return childAIPResults.add(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#add(E)
     */
    public boolean addDataObjectComponentResult(NormaliserResults o) {
        return dataObjectComponentResults.add(o);
    }

	/**
	 * @return Returns the normaliserClassName.
	 */
	public String getNormaliserClassName()
	{
		return normaliserClassName;
	}

	/**
	 * @param normaliserClassName The normaliserClassName to set.
	 */
	public void setNormaliserClassName(String normaliserClassName)
	{
		this.normaliserClassName = normaliserClassName;
	}

}
