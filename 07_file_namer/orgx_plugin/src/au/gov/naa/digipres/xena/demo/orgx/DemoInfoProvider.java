/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.File;
import java.util.Random;

public class DemoInfoProvider implements InfoProvider {

	private String randomUserNames[] = {"Homer", "Karl", "Kenny", "Monty Burns", "Smithers"};
	private String randomDepartmentCodes[] = {"S7G", "S7A", "COR", "ADM"};

	private Random random = new Random();

	/**
	 * Return the username if it is set, or a random one from randomUserNames if it is not.
	 */
	public String getUserName() {
		return randomUserNames[random.nextInt(randomUserNames.length)];
	}

	/**
	 * Return the departmentCode if it is set, or a random one from randomDepartmentCodes if it is not.
	 * @return the department code.
	 */
	public String getDepartmentCode() {
		return randomDepartmentCodes[random.nextInt(randomDepartmentCodes.length)];
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.demo.orgx.InfoProvider#getHeaderFile()
	 */
	public File getHeaderFile() {
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.demo.orgx.InfoProvider#isInsertTimestamp()
	 */
	public boolean isInsertTimestamp() {
		return false;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.demo.orgx.InfoProvider#getDepartmentName()
	 */
	public String getDepartmentName() {
		return null;
	}

}
