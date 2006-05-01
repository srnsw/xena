/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.File;

public interface InfoProvider {
    
    public String getUserName();
    
    public String getDepartmentCode();
    
    public String getDepartmentName();

	public boolean isInsertTimestamp();

	public File getHeaderFile();
    
}
