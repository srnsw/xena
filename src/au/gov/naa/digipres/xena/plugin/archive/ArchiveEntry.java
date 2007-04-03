/*
 * Created on 27/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive;

import java.util.Date;

/**
 * 
 * @author justinw5
 * created 29/03/2007
 * archive
 * Short desc of class:
 */
public class ArchiveEntry
{
	private String name;
	
	// Filename associated with this ArchiveEntry. Could either be the temporary file in its original form, or the normalised xena file.
	private String filename;
	
	private Date originalFileDate;
	private long originalSize;
	
	/**
	 * @param name
	 * @param file
	 */
	public ArchiveEntry(String name, String filename)
	{
		this.name = name;
		this.filename = filename;
	}

	/**
	 * @return the file
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * @param file the file to set
	 */
	public void setFilename(String file)
	{
		this.filename = file;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the originalDateCreated
	 */
	public Date getOriginalFileDate()
	{
		return originalFileDate;
	}

	/**
	 * @param originalDateCreated the originalDateCreated to set
	 */
	public void setOriginalFileDate(Date originalDateCreated)
	{
		this.originalFileDate = originalDateCreated;
	}

	/**
	 * @return the originalSize
	 */
	public long getOriginalSize()
	{
		return originalSize;
	}

	/**
	 * @param originalSize the originalSize to set
	 */
	public void setOriginalSize(long originalSize)
	{
		this.originalSize = originalSize;
	}
	
	
}
