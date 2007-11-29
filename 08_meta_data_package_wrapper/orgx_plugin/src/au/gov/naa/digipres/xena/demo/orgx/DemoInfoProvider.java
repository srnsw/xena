/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.File;
import java.util.Random;

public class DemoInfoProvider implements InfoProvider {

	private String userName;
	private String departmentCode;
	private String departmentName;

	private String randomUserNames[] = {"Homer", "Karl", "Kenny", "Monty Burns", "Smithers"};
	private String randomDepartmentNames[] = {"Sector 7G", "Sector 7A", "Corporate", "Administration"};
	private String randomDepartmentCodes[] = {"S7G", "S7A", "COR", "ADM"};

	private Random random = new Random();

	/**
	 * Return the username if it is set, or a random one from randomUserNames if it is not.
	 */
	public String getUserName() {
		if (userName == null) {
			userName = randomUserNames[random.nextInt(randomUserNames.length)];
		}
		return userName;
	}

	/**
	 * Return the departmentCode if it is set, or a random one from randomDepartmentCodes if it is not.
	 * @return the department code.
	 */
	public String getDepartmentCode() {
		if (departmentCode == null) {
			departmentCode = randomDepartmentCodes[random.nextInt(randomDepartmentCodes.length)];
		}
		return departmentCode;
	}

	/**
	 * @return Returns the deparmentName.
	 */
	public String getDepartmentName() {
		if (departmentName == null) {
			departmentName = randomDepartmentNames[random.nextInt(randomDepartmentNames.length)];
		}
		return departmentName;
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

}
