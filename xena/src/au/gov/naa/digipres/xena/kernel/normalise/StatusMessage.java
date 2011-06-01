package au.gov.naa.digipres.xena.kernel.normalise;

/**
 * A message and associated type
 * 
 * @author toneill
 *
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
