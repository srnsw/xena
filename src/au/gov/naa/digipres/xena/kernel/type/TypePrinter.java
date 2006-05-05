package au.gov.naa.digipres.xena.kernel.type;
import java.io.File;

import javax.print.Doc;

import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Class used to provide a mechanism to produce javax.print.Doc objects from a File, specific to each Xena Type.
 * This may eventually form part of a printing system to print out Xena files, but is currently not in use.
 * @author justinw5
 * created 5/05/2006
 * xena
 * Short desc of class:
 */
abstract public class TypePrinter {
    
    protected TypePrinterManager typePrinterManager;
    
	/**
     * @return Returns the typePrinterManager.
     */
    public TypePrinterManager getTypePrinterManager() {
        return typePrinterManager;
    }

    /**
     * @param typePrinterManager The new value to set typePrinterManager to.
     */
    public void setTypePrinterManager(TypePrinterManager typePrinterManager) {
        this.typePrinterManager = typePrinterManager;
    }

    /**
     * Return the specfic XenaFileType for this TypePrinter.
     * @return XenaFileType
     * @throws XenaException
     */
    abstract public XenaFileType getType() throws XenaException;

    /**
     * Return a Doc object representing the given File, assuming the file is the same type as that of this TypePrinter.
     * @param file - input File
     * @return Doc representing the given File
     * @throws XenaException
     */
	abstract public Doc getDoc(File file) throws XenaException;
}
