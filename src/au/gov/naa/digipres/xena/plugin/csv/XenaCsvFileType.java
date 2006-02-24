/*
 * Created on 21/02/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.csv;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

public class XenaCsvFileType extends XenaFileType {

    public String getTag() {
        return "csv:csv";
    }

    public String getNamespaceUri() {
        return CsvToXenaCsvNormaliser.URI;
    }
    

}
