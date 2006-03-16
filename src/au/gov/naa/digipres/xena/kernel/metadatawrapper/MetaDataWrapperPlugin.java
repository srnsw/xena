/*
 * Created on 28/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import org.xml.sax.XMLFilter;

import au.gov.naa.digipres.xena.kernel.XenaException;

public class MetaDataWrapperPlugin {
    private String name;
    private Class wrapperClass;
    private Class unwrapperClass;
    private String topTag;
    private MetaDataWrapperManager metaDataWrapperManager;
    
    public MetaDataWrapperPlugin(){
    }
    
    public MetaDataWrapperPlugin(String name, AbstractMetaDataWrapper wrapper, XMLFilter unwrapper, String topTag, MetaDataWrapperManager metaDataWrapperManager){
        this.name = name;
        this.wrapperClass = wrapper.getClass();
        this.unwrapperClass = unwrapper.getClass();
        this.topTag = topTag;
        this.metaDataWrapperManager = metaDataWrapperManager;
    }
    
    public MetaDataWrapperPlugin(String name, Class wrapperClass, Class unwrapperClass, String topTag, MetaDataWrapperManager metaDataWrapperManager) {
        this.name = name;
        this.wrapperClass = wrapperClass;
        this.unwrapperClass = unwrapperClass;
        this.topTag = topTag;
        this.metaDataWrapperManager = metaDataWrapperManager;
    }
    
    public String toString() {
        if (name != null)
            return name;
        return  "New filter";
        
    }
    
    public boolean equals(Object obj){
        if ( obj == null ){
            return false;
        }
        if (obj.getClass() != this.getClass() ){
            return false;
        }
        
        MetaDataWrapperPlugin other = (MetaDataWrapperPlugin)obj;
        if (this.name != other.getName()){
            return false;
        }
        if (this.wrapperClass != other.getWrapperClass()){
            return false;
        }
        if (this.unwrapperClass.getClass() != other.getUnwrapperClass()){
            return false;
        }
        return true;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @returns the unwrapper class
     */
    public Class getUnwrapperClass() {
        return unwrapperClass;
    }
    
    /**
     * @return Returns an instance of the unwrapper.
     */
    public XMLFilter getUnwrapper() throws XenaException {
        try {
            Object object = unwrapperClass.newInstance();
            if (object instanceof XMLFilter) {
                return (XMLFilter)object;                
            }
            throw new XenaException("Could not create unwrapper!");
        } catch (InstantiationException ie) {
            throw new XenaException(ie);
        } catch (IllegalAccessException iae) {
            throw new XenaException(iae);
        }
    }

    /**
     * set the unwrapper class
     * @param unwrapperClass
     */
    public void setUnwrapper(Class unwrapperClass) {
        this.unwrapperClass = unwrapperClass;
    }
    
    /**
     * Set the unwrapper using an instance of a class.
     * @param unwrapper The new value to set unwrapper to.
     */
    public void setUnwrapper(XMLFilter unwrapper) {
        this.unwrapperClass = unwrapper.getClass();
    }

    /**
     * @return Returns the wrapper.
     */
    public Class getWrapperClass() {
        return wrapperClass;
    }
    
    /**
     * @return Returns an instance of the wrapper class.
     */
    public AbstractMetaDataWrapper getWrapper() throws XenaException {
        try {
            Object object = wrapperClass.newInstance();
            if (object instanceof AbstractMetaDataWrapper) {
                AbstractMetaDataWrapper wrapper = (AbstractMetaDataWrapper)object;
                wrapper.setMetaDataWrapperManager(metaDataWrapperManager);
                return wrapper;
            }
            throw new XenaException("Could not create unwrapper!");
        } catch (InstantiationException ie) {
            throw new XenaException(ie);
        } catch (IllegalAccessException iae) {
            throw new XenaException(iae);
        }
    }

    public void setWrapper(Class wrapperClass) {
        this.wrapperClass = wrapperClass;
    }
    
    /**
     * @param wrapper The new value to set wrapper to.
     */
    public void setWrapper(AbstractMetaDataWrapper wrapper) {
        this.wrapperClass = wrapper.getClass();
    }

    /**
     * @return Returns the topTag.
     */
    public String getTopTag() {
        return topTag;
    }

    /**
     * @param topTag The new value to set topTag to.
     */
    public void setTopTag(String topTag) {
        this.topTag = topTag;
    }

    /**
     * @return Returns the metaDataWrapperManager.
     */
    public MetaDataWrapperManager getMetaDataWrapperManager() {
        return metaDataWrapperManager;
    }

    /**
     * @param metaDataWrapperManager The new value to set metaDataWrapperManager to.
     */
    public void setMetaDataWrapperManager(
            MetaDataWrapperManager metaDataWrapperManager) {
        this.metaDataWrapperManager = metaDataWrapperManager;
    }
    
    
}