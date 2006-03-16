package au.gov.naa.digipres.xena.kernel.type;
import java.io.File;

import javax.print.Doc;

import au.gov.naa.digipres.xena.kernel.XenaException;

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

    abstract public XenaFileType getType() throws XenaException;

	abstract public Doc getDoc(File file) throws XenaException;
}
