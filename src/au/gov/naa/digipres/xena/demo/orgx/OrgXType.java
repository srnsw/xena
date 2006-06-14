/*
 * Created on 4/05/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

public class OrgXType extends XenaFileType {

    @Override
    public String getTag() {
        return OrgXMetaDataWrapper.ORGX_OPENING_TAG;
    }

    @Override
    public String getNamespaceUri() {
        return null;
    }

}
