/*
 * Created on 8/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email.trim;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class TrimAttachmentType extends FileType
{
	private static final String TRIM_ATTACHMENT_TYPE_NAME = 
		"TRIM Email Attachment";

	public TrimAttachmentType()
	{
		super();
	}

	@Override
	public String getName()
	{
		return TRIM_ATTACHMENT_TYPE_NAME;
	}

	
}
