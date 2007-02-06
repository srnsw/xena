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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

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

public class AudioNormaliser extends AbstractNormaliser
{
	
	final static String FLAC_PREFIX = "flac";

	final static String FLAC_URI = "http://preservation.naa.gov.au/flac/1.0";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * RFC suggests max of 76 characters per line
	 */
	public static final int MAX_BASE64_RFC_LINE_LENGTH = 76;

	/**
	 * Base64 turns 3 characters into 4...
	 */
	public static final int CHUNK_SIZE = (MAX_BASE64_RFC_LINE_LENGTH * 3) / 4;
	
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

	public AudioNormaliser()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parse(InputSource input, NormaliserResults results)
			throws IOException, SAXException
	{
		try {
            //TODO: The parse method should ONLY accept xena input sources. The Abstract normaliser should handle this appropriately.
            // ie - this method should be parse(XenaInputSource xis)
            if (!(input instanceof XenaInputSource)) {
                throw new XenaException("Can only normalise XenaInputSource objects.");
            }
            
            XenaInputSource xis = (XenaInputSource)input;
            
            // This is where the difficult bit goes! :)            

            // Convert source audio stream to raw format
            AudioInputStream audioIS;
            
            if (xis.getFile() == null)
            {
            	audioIS = AudioSystem.getAudioInputStream(xis.getByteStream());
            }
            else
            {
            	audioIS = AudioSystem.getAudioInputStream(xis.getFile());
            }
                                               
			AudioFormat	sourceFormat = audioIS.getFormat();
			
			InputStream flacStream;
			if (sourceFormat.getEncoding().toString().equals("FLAC"))
			{
				flacStream = audioIS;
			}
			else
			{
				AudioFormat	targetFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					sourceFormat.getSampleRate(),
					nSampleSizeInBits,
					sourceFormat.getChannels(),
					sourceFormat.getChannels() * (nSampleSizeInBits / 8),
					sourceFormat.getSampleRate(),
					bBigEndian);
	            
	            AudioInputStream rawIS = AudioSystem.getAudioInputStream(targetFormat, audioIS);
	            AudioFormat rawFormat = rawIS.getFormat();
	            System.out.println("Channels: " + rawFormat.getChannels()
	                               + "\nbig endian: " + rawFormat.isBigEndian()
	                               + "\nsample rate: " + rawFormat.getSampleRate()
	                               + "\nbps: " +rawFormat.getSampleRate() * rawFormat.getSampleSizeInBits());
	            
	            String endianStr = rawFormat.isBigEndian() ? "big" : "little";
	            
	            // Temporarily using binary flac encoder until a Java version exists.
	            	            
	            // Encode input file with binary flac encoder
	            File tmpFlacFile = File.createTempFile("flacoutput", ".tmp");
	                        
	            PluginManager pluginManager = normaliserManager.getPluginManager();
				PropertiesManager propManager = pluginManager.getPropertiesManager();
				String flacEncoderProg = propManager.getPropertyValue(AudioProperties.AUDIO_PLUGIN_NAME,
				                                                      AudioProperties.FLAC_LOCATION_PROP_NAME);
	            
	            System.out.println(rawFormat);
	            
	            String signStr;
	            Encoding encodingType = rawFormat.getEncoding();
	            if (encodingType.equals(Encoding.PCM_SIGNED))
	            {
	            	signStr = "signed";
	            }
	            else if (encodingType.equals(Encoding.PCM_UNSIGNED))
	            {
	            	signStr = "unsigned";
	            }
	            else
	            {
	            	throw new IOException("Invalid raw encoding type: " + encodingType);
	            }
	            
	            String callStr = flacEncoderProg
	            				+ " -f"
	            				+ " --endian=" + endianStr
	            				+ " --channels=" + rawFormat.getChannels()
	            				+ " --sample-rate=" + rawFormat.getSampleRate()
	            				+ " --sign=" + signStr
	            				+ " --bps=" + rawFormat.getSampleSizeInBits()
	            				+ " -o " + tmpFlacFile.getAbsolutePath() // output filename
	            				+ " - "; // Forces read from stdin
	            
	            Process pr;
				final StringBuilder errorBuff = new StringBuilder();
	            try
	            {
		            pr = Runtime.getRuntime().exec(callStr);
					
					final InputStream eis = pr.getErrorStream();
					final InputStream ois = pr.getInputStream();
					
					
					Thread et = new Thread() {
						public void run() {
							try {
								int c;
								while (0 <= (c = eis.read())) {
									errorBuff.append((char)c);
								}
							} catch (IOException x) {
								// Nothing
							}
						}
					};
					et.start();
					Thread ot = new Thread() {
						public void run() {
							int c;
							try {
								while (0 <= (c = ois.read())) {
									System.err.print((char)c);
								}
							} catch (IOException x) {
								// Nothing
							}
						}
					};
					ot.start();
					
					OutputStream procOS = new BufferedOutputStream(pr.getOutputStream());
					
		        	// read 10k at a time
		        	byte[] buffer = new byte[1000];
					
					int bytesRead;
					while (0 < (bytesRead = rawIS.read(buffer)))
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
	                        
	            flacStream = new FileInputStream(tmpFlacFile);
			}
            
            // Base64-encode FLAC stream
			String prefix = FLAC_PREFIX;
			String uri = FLAC_URI;
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(uri, prefix, prefix + ":" + prefix, att);

			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();

			// 80 characters makes nice looking output
			byte[] buf = new byte[CHUNK_SIZE];
			int c;
			while (0 <= (c = flacStream.read(buf))) {
				byte[] tbuf = buf;
				if (c < buf.length) {
					tbuf = new byte[c];
					System.arraycopy(buf, 0, tbuf, 0, c);
				}
				char[] chs = encoder.encode(tbuf).trim().toCharArray();
				ch.characters(chs, 0, chs.length);
			}
			ch.endElement(uri, prefix, prefix + ":" + prefix);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
		catch (UnsupportedAudioFileException e)
		{
			throw new IOException("Xena does not handle this particular audio format. " + e);
		}
	}

	@Override
	public String getName()
	{
		return "Audio";
	}

}
