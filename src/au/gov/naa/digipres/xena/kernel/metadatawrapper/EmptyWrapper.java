/*
 * Created on 9/03/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;


import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

public class EmptyWrapper extends AbstractMetaDataWrapper {
    
    public static String EMPTY_WRAPPER_NAME = "Emtpy meta data Wrapper";
    
    public String getName(){
        return EMPTY_WRAPPER_NAME;
    }
    
    public String getOpeningTag() {
        return "";
    }

    public String getSourceId(XenaInputSource input) throws XenaException {
        return "";
    }

    public String getSourceName(XenaInputSource input) throws XenaException {
        return "";
    }
    
}
