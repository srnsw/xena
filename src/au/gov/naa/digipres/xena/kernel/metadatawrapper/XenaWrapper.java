/*
 * Created on 28/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

public interface XenaWrapper {

    /*
     */
    public String getOpeningTag();
    
    public String getSourceId(XenaInputSource input) throws XenaException;
        
    public String getSourceName(XenaInputSource input) throws XenaException;
}
