/*
 * Created on 31/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa;

public class NaaTagNames {

    /**
     * @param args
     */


    public final static String NAA_PACKAGE = "NAA Package";
    
    final static String PACKAGE_URI ="http://preservation.naa.gov.au/package/1.0";
    final static String PACKAGE_PREFIX = "package";
    
    public final static String PACKAGE = "package";
    public final static String PACKAGE_PACKAGE = PACKAGE + ":" + PACKAGE;
    
    public final static String META = "meta";
    public final static String PACKAGE_META = PACKAGE + ":" + META;
    
    public final static String IDENTIFIER = "identifier";
    public final static String SOURCE = "source";
    
    public final static String IDENTIFIER_URI ="http://preservation.naa.gov.au/identifier/1.0";
    
    public final static String DC_URI = "http://purl.org/dc/elements/1.1/";
    
    public final static String DC_PREFIX = "dc";
    
    public final static String DCIDENTIFIER = DC_PREFIX + ":" + IDENTIFIER;
    
    public final static String DCSOURCE = DC_PREFIX + ":" + SOURCE;
    
    public final static String DCTERMS_URI = "http://purl.org/dc/terms/";
    public final static String DCTERMS_PREFIX = "dcterms";
    
    public final static String CREATED = "created";
    public final static String DCCREATED = DCTERMS_PREFIX + ":" + CREATED;
    
    public final static String NAA_URI = "http://preservation.naa.gov.au/naa/1.0";
    public final static String NAA_PREFIX = "naa";
    
    public final static String DATASOURCE = "datasource";
    public final static String NAA_DATASOURCE = NAA_PREFIX + ":" + DATASOURCE;
    
    public final static String DATASOURCES = "datasources";
    public final static String NAA_DATASOURCES = NAA_PREFIX + ":" + DATASOURCES;
    
    public final static String LASTMODIFIED = "last-modified";
    public final static String NAA_LASTMODIFIED = NAA_PREFIX + ":" + LASTMODIFIED;
    
    public final static String SOURCEID = "source-id";
    public final static String NAA_SOURCEID = NAA_PREFIX + ":" + SOURCEID;
    
    public final static String TYPE = "type";
    public final static String NAA_TYPE = NAA_PREFIX + ":" + TYPE;

    public final static String WRAPPER = "wrapper";
    public final static String NAA_WRAPPER = NAA_PREFIX + ":" + WRAPPER;

}
