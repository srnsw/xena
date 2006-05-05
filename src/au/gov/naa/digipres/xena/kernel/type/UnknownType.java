/*
 * Created on 26/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.type;

/**
 * A placeholder for when Xena cannot determine the input Type.
 * 
 * @author justinw5
 * created 5/05/2006
 * xena
 * Short desc of class:
 */
public class UnknownType extends Type {

	@Override
    public String getName() {
        return "Unknown";
    }

	@Override
    public String getMimeType() {
        return "unknown/unknown";
    }
    
}
