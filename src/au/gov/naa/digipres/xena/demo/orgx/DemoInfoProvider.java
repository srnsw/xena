/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.util.Random;

public class DemoInfoProvider implements InfoProvider {

    private String userName = null;
    private String departmentCode = null;
    private String deparmentName = null;
    
    private String randomUserNames[] = {"Homer", "Karl", "Kenny", "Monty Burns", "Smithers"};
    private String randomDepartmentNames[] = {"Sector 7G", "Sector 7A", "Corporate", "Administration"};
    private String randomDepartmentCodes[] = {"S7G", "S7A", "COR", "ADM"};
    
    private Random random = new Random();
    
    /**
     * Return the username if it is set, or a random one from randomUserNames if it is not.
     */
    public String getUserName() {
        if (userName != null) {
            return userName;
        }
        return randomUserNames[random.nextInt(randomUserNames.length)];
    }

    /**
     * Set the userName.
     * @param userName the user name to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;        
    }


    /**
     * Return the departmentCode if it is set, or a random one from randomDepartmentCodes if it is not.
     * @return the department code.
     */
    public String getDepartmentCode() {
        if (departmentCode != null) {
            return userName;
        }
        return randomDepartmentCodes[random.nextInt(randomDepartmentCodes.length)];
    }

    /**
     * @param departmentCode The new value to set departmentCode to.
     */
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    /**
     * @return Returns the deparmentName.
     */
    public String getDeparmentName() {
        if (deparmentName!= null) {
            return deparmentName;
        }
        return randomDepartmentNames[random.nextInt(randomDepartmentNames.length)];
    }

    /**
     * Go to the DB, and lookup the correct name for the department...
     * @param dbConnectionURL
     */
    public String getDepartmentName(String dbConnectionURL) {
        System.out.println("Our db is located here: " + dbConnectionURL);
        return getDeparmentName();
    }
    
    
    /**
     * @param deparmentName The new value to set deparmentName to.
     */
    public void setDeparmentName(String deparmentName) {
        this.deparmentName = deparmentName;
    }

    
}
