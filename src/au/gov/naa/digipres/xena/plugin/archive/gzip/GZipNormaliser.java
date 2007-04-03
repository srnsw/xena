/*
 * Created on 28/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.gzip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Normaliser for .zip and .jar files
 * 
 * @author justinw5
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class GZipNormaliser extends AbstractNormaliser
{
	

	@Override
	public String getName()
	{
		return "GZip";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException
	{
		ContentHandler contentHandler = getContentHandler();
		
		// Just decompress the gzip file to a temporary file, and normalise the decompressed file as normal
		
		// Get temp file. Life is much easier if the original source is a filename. Still need support the case where
		// the original is stream-based though...
		String tempFilename;
		XenaInputSource xis = (XenaInputSource)input;
		if (xis.getFile() == null)
		{
			tempFilename = ".tmp";
		}
		else
		{
			tempFilename = xis.getFile().getName();
			
			// Remove gzip extension (if it exists). .tgz file should produce a .tar output
			if (tempFilename.toLowerCase().endsWith(".gzip") || tempFilename.toLowerCase().endsWith(".gz"))
			{
				tempFilename = tempFilename.substring(0, tempFilename.lastIndexOf("."));
			}
			else if (tempFilename.toLowerCase().endsWith(".tgz"))
			{
				tempFilename = tempFilename.substring(0, tempFilename.lastIndexOf(".")) + ".tar";
			}
		}
		
		File extractedTempFile = File.createTempFile("gzip", tempFilename);
		extractedTempFile.deleteOnExit();
		FileOutputStream tempFileOS = new FileOutputStream(extractedTempFile);
		GZIPInputStream gzStream = new GZIPInputStream(input.getByteStream());
		
		// 10k buffer
		byte[] buffer = new byte[10 * 1024];
		
		// Extract compressed file
		int bytesRead = gzStream.read(buffer);
		while (bytesRead > 0)
		{
			tempFileOS.write(buffer, 0, bytesRead);
			bytesRead = gzStream.read(buffer);
		}
		
		// Close streams
		gzStream.close();
		tempFileOS.flush();
		tempFileOS.close();
		
		XenaInputSource extractedXis = new XenaInputSource(extractedTempFile);
		
		
		try
		{
			// Get the type and associated normaliser for this entry
			Type fileType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(extractedXis);				
			
			extractedXis.setType(fileType);
			AbstractNormaliser entryNormaliser = normaliserManager.lookup(fileType);
			
			
	        // Set up the normaliser and wrapper for this entry
	        entryNormaliser.setProperty("http://xena/url", extractedXis.getSystemId());
	        entryNormaliser.setContentHandler(contentHandler);            
	        entryNormaliser.setProperty("http://xena/file", this.getProperty("http://xena/file"));
	        entryNormaliser.setProperty("http://xena/normaliser", entryNormaliser);
	               
	        // Normalise the entry
	        entryNormaliser.parse(extractedXis, results);
		}
		catch (XenaException ex)
		{
			throw new SAXException("Problem normalising the compressed file contained within a GZIP archive", ex);
		}
	}

}
