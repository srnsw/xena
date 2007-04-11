/*
 * Created on 9/06/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * The direct audio normaliser is used for files that can be directly handled by the flac encoder. The source file
 * is streamed directly to the flac encoder, which will produce a temporary flac file, which is then encoded with 
 * base64 to produce our final output.
 * 
 * @author justinw5
 * created 11/04/2007
 * audio
 * Short desc of class:
 */
public class DirectAudioNormaliser extends AbstractNormaliser
{
	public final static String AUDIO_PREFIX = "audio";
	public final static String FLAC_TAG = "flac";
	public final static String AUDIO_URI = "http://preservation.naa.gov.au/audio/1.0";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/** Endianess value to use in conversion.
	 * If a conversion of the AudioInputStream is done,
     * this values is used as endianess in the target AudioFormat.
     * The default value can be altered by the command line
     * option "-B".
     */
	boolean	bBigEndian = false;

	/** Sample size value to use in conversion.
     * If a conversion of the AudioInputStream is done,
     * this values is used as sample size in the target
     * AudioFormat.
     * The default value can be altered by the command line
     * option "-S".
     */
	int	nSampleSizeInBits = 16;

	public DirectAudioNormaliser()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parse(InputSource input, NormaliserResults results)
			throws IOException, SAXException
	{
		try 
		{
            //TODO: The parse method should ONLY accept xena input sources. The Abstract normaliser should handle this appropriately.
            // ie - this method should be parse(XenaInputSource xis)
            if (!(input instanceof XenaInputSource)) 
            {
                throw new XenaException("Can only normalise XenaInputSource objects.");
            }
            
            XenaInputSource xis = (XenaInputSource)input;
            
            InputStream sourceIS;
            if (xis.getFile() == null)
            {
            	sourceIS = xis.getByteStream();
            }
            else
            {
            	sourceIS = new FileInputStream(xis.getFile());
            }
                                               
            // Temporarily using binary flac encoder until a Java version exists.
            // Although it may be better to always use a binary encoder for performance reasons...
            	            
            // Encode input file with binary flac encoder
            File tmpFlacFile = File.createTempFile("flacoutput", ".tmp");
                        
            PluginManager pluginManager = normaliserManager.getPluginManager();
			PropertiesManager propManager = pluginManager.getPropertiesManager();
			String flacEncoderProg = propManager.getPropertyValue(AudioProperties.AUDIO_PLUGIN_NAME,
			                                                      AudioProperties.FLAC_LOCATION_PROP_NAME);
            	            
            String callStr = flacEncoderProg
            				+ " -f"
            				+ " -o " + tmpFlacFile.getAbsolutePath() // output filename
            				+ " - "; // Forces read from stdin. Need to use this in case our input is not a file
            
            Process pr;
			final StringBuilder errorBuff = new StringBuilder();
            try
            {
	            pr = Runtime.getRuntime().exec(callStr);
				
				final InputStream eis = pr.getErrorStream();
				final InputStream ois = pr.getInputStream();
				
				
				Thread et = new Thread() 
				{
					public void run() 
					{
						try 
						{
							int c;
							while (0 <= (c = eis.read())) 
							{
								errorBuff.append((char)c);
							}
						} 
						catch (IOException x) 
						{
							// Nothing
						}
					}
				};
				et.start();
				
				Thread ot = new Thread() 
				{
					public void run() 
					{
						int c;
						try 
						{
							while (0 <= (c = ois.read())) 
							{
								System.err.print((char)c);
							}
						} 
						catch (IOException x) 
						{
							// Nothing
						}
					}
				};
				ot.start();
				
				OutputStream procOS = new BufferedOutputStream(pr.getOutputStream());
				
	        	// read 10k at a time
	        	byte[] buffer = new byte[10 * 1024];
				
				int bytesRead;
				while (0 < (bytesRead = sourceIS.read(buffer)))
				{
					procOS.write(buffer, 0, bytesRead);
				}
				procOS.flush();
				procOS.close();
				pr.waitFor();
            }
			catch (Exception flacEx)
			{
				throw new IOException("An error occured in the flac normaliser: " + flacEx);
			}
            
			if (pr.exitValue() == 1)
			{
				throw new IOException("An error occured in the flac normaliser: " + errorBuff);
			}
            
            // Base64-encode FLAC stream
            InputStream flacStream = new FileInputStream(tmpFlacFile);
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(AUDIO_URI, FLAC_TAG, AUDIO_PREFIX + ":" + FLAC_TAG, att);
			InputStreamEncoder.base64Encode(flacStream, ch);
			ch.endElement(AUDIO_URI, FLAC_TAG, AUDIO_PREFIX + ":" + FLAC_TAG);
		} 
		catch (XenaException x) 
		{
			throw new SAXException(x);
		}
	}

	@Override
	public String getName()
	{
		return "Direct Audio";
	}

}
