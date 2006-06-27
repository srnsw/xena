/*
 * Created on 21/06/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import au.gov.naa.digipres.xena.kernel.type.Type;

public class WavType extends Type
{

	public WavType()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return "WAV";
	}

	@Override
	public String getMimeType()
	{
		return "audio/wav";
	}

}
