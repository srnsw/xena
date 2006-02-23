/*
 * Created on 29/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel;

import org.xml.sax.SAXException;

public class FoundException extends SAXException {
    private static final long serialVersionUID = 1L;
    private String name;
    public FoundException(String name) {
        super("Found");
        this.name = name;
    }
    public String getName(){
        return name;
    }
    
}