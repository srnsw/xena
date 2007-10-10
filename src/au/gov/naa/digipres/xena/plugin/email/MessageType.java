/*
 * Created on 8/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class MessageType extends FileType
{
	private static final String EMAIL_MESSAGE_TYPE_NAME = 
		"Email Message";

	public MessageType()
	{
		super();
	}

	@Override
	public String getName()
	{
		return EMAIL_MESSAGE_TYPE_NAME;
	}


    public String getMimeType() {
        return "message/email";
    }
    
	
}
