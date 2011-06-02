/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Terry O'Neill
 */

package au.gov.naa.digipres.xena.kernel.normalise;

/**
 * A message and associated type
 */
public class StatusMessage implements Comparable<StatusMessage> {
	public static final int INFO = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	
	private int type;
	private String message;
	
	public StatusMessage() {
	}
	
	public StatusMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
    public int compareTo(StatusMessage statusMessage) {
    	if (type != statusMessage.type) {
    		return statusMessage.getType() - type;
    	} else {
    		return message.compareTo(statusMessage.getMessage());
    	}
    }
    
    public String toString() {
    	String result = "";
    	if (type == ERROR) {
    		result += "Error: ";
    	} else if (type == WARNING) {
    		result += "Warning: ";
    	}
    	result += message;
    	return result;
    }
}
