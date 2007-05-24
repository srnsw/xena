/*
 * Created on 28/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.macbinary;

import glguerin.io.FileForker;
import glguerin.io.imp.gen.PlainForker;
import glguerin.macbinary.MacBinaryHeader;
import glguerin.macbinary.MacBinaryReceiver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
public class MacBinaryNormaliser extends AbstractNormaliser
{
	

	@Override
	public String getName()
	{
		return "MacBinary";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException
	{
		ContentHandler contentHandler = getContentHandler();
		
		// Setup  FileForker (a version of File to handle Mac-like resource forking)
		FileForker.SetFactoryClass(PlainForker.class);
		FileForker forker = FileForker.MakeOne();
		
		// Setup MacBinary receiver, which will extract the archived file to the temp directory
		MacBinaryReceiver receiver = new MacBinaryReceiver();
		receiver.useForker(forker);
		
		// We want to normalise the file inside this archive, using its original file name. However the name of the
		// archived file cannot be determined until after we have started extracting, when the MacBinaryReceiver
		// will automatically create a file with the correct name in the specified directory. An error is thrown if
		// this file already exists, but we can't check for its existence before we start the extraction process. So
		// we'll use a unique subdirectory in the temp directory, and delete both this directory and file when we
		// are finished.
		
		// Ensure we're not overwriting an existing file
		File tempDir = new File(System.getProperty("java.io.tmpdir"), String.valueOf(System.currentTimeMillis()));
		if (!tempDir.exists())
		{
			tempDir.mkdirs();
		}
		tempDir.deleteOnExit();
		receiver.setFile(tempDir);
		receiver.reset();
		
		// Pass source bytes to receiver
		InputStream sourceStream = input.getByteStream();
		byte[] buffer = new byte[10 * 1024];
		int bytesRead = sourceStream.read(buffer);
		while (bytesRead > 0)
		{
			receiver.processBytes(buffer, 0, bytesRead);
			bytesRead = sourceStream.read(buffer);
		}
		
		MacBinaryHeader header = receiver.getHeader();
		File outputFile = new File(receiver.getPathname().getPath());
		outputFile.deleteOnExit();
		outputFile.setLastModified(header.getTimeModified());
		
		XenaInputSource extractedXis = new XenaInputSource(outputFile);
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
