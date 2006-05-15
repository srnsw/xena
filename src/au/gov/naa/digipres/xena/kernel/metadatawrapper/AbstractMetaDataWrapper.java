/*
 * Created on 14/03/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
/**
 * 
 * @author andrek24
 * created 24/04/2006
 * xena
 * Short desc of class:
 * 
 * 
 * @see org.xml.sax.XMLFilterImpl
 */
public abstract class AbstractMetaDataWrapper extends XMLFilterImpl {

    protected MetaDataWrapperManager metaDataWrapperManager;
    
    public void setMetaDataWrapperManager(MetaDataWrapperManager metaDataWrapperManager){
        this.metaDataWrapperManager = metaDataWrapperManager;
    }

    public MetaDataWrapperManager getMetaDataWrapperManager() {
        return metaDataWrapperManager;
    }
    
    public AbstractMetaDataWrapper() {
        super();
    }
    
    public AbstractMetaDataWrapper(MetaDataWrapperManager metaDataWrapperManager){
        super();
        this.metaDataWrapperManager = metaDataWrapperManager;
    }
    
    public abstract String getName();
    
    public abstract String getOpeningTag();
    
    public abstract String getSourceId(XenaInputSource input) throws XenaException;
        
    public abstract String getSourceName(XenaInputSource input) throws XenaException;

}
