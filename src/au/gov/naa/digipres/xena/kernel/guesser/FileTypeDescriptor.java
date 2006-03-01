/*
 * Created on 28/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

public class FileTypeDescriptor
{
	private byte[][] magicNumberArr;
	private String[] mimeTypeArr;
	private String[] extensionArr;
	
	/**
	 * @param extension
	 * @param number
	 * @param type
	 */
	public FileTypeDescriptor(String[] extension, byte[][] numberArr, String[] typeArr)
	{
		this.extensionArr = extension;
		magicNumberArr = numberArr;
		mimeTypeArr = typeArr;
	}
	
	public boolean extensionMatch(String extension)
	{
		return stringInArr(extension, extensionArr);
	}
	
	public boolean mimeTypeMatch(String mimeType)
	{
		return stringInArr(mimeType, mimeTypeArr);
	}
	
	public boolean magicNumberMatch(byte[] magicNumber)
	{
		return bytesInArr(magicNumber, magicNumberArr);
	}
	
	/**
	 * @return Returns the extensionArr.
	 */
	public String[] getExtensionArr()
	{
		return extensionArr;
	}
	/**
	 * @param extension The extensionArr to set.
	 */
	public void setExtensionArr(String[] extension)
	{
		this.extensionArr = extension;
	}
	/**
	 * @return Returns the magicNumberArr.
	 */
	public byte[][] getMagicNumberArr()
	{
		return magicNumberArr;
	}
	/**
	 * @param magicNumberArr The magicNumberArr to set.
	 */
	public void setMagicNumberArr(byte[][] magicNumberArr)
	{
		this.magicNumberArr = magicNumberArr;
	}
	/**
	 * @return Returns the mimeType.
	 */
	public String[] getMimeTypeArr()
	{
		return mimeTypeArr;
	}
	/**
	 * @param mimeType The mimeType to set.
	 */
	public void setMimeTypeArr(String[] mimeType)
	{
		this.mimeTypeArr = mimeType;
	}
	
    private static boolean stringInArr(String str, String[] strArr)
    {
    	boolean found = false;
    	if (str != null)
    	{
	    	for (int i = 0; i < strArr.length; i++)
	    	{
	    		if (str.equalsIgnoreCase(strArr[i]))
	    		{
	    			found = true;
	    			break;
	    		}
	    	}
    	}
    	return found;
    }
    
    private static boolean bytesInArr(byte[] bytes, byte[][] byteArr)
    {
    	boolean found = false;
    	if (bytes != null)
    	{
	    	for (int i = 0; i < byteArr.length; i++)
	    	{
	    		if (GuesserUtils.compareByteArrays(bytes, byteArr[i]))
	    		{
	    			found = true;
	    			break;
	    		}
	    	}
    	}
    	return found;
    }
	
}
