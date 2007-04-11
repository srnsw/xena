/*
 * Created on 9/06/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

public class AudioNormaliserHarness
{

//	private static final String TEST_FILE = "D:/xena_data/source/audio/acm waveform.unk";
//	private static final String TEST_FILE = "D:/xena_data/source/audio/acm waveform.wav";
//	private static final String TEST_FILE = "D:/xena_data/source/audio/shelly_kovco.mp3";
	private static final String TEST_FILE = "D:/xena_data/source/audio/test.flac";

	
	
	/**
	 * @throws IOException 
	 * @throws XenaException 
	 * 
	 */
	public AudioNormaliserHarness() throws IOException, XenaException
	{
		XenaInputSource xis = new XenaInputSource(new File(TEST_FILE));
		
		File outputFile = new File("D:\\xena_data\\destination", "audio" + System.currentTimeMillis() + ".xena");
		
		AbstractNormaliser normaliser = new DirectAudioNormaliser();
		
        // create our transform handler
        TransformerHandler transformerHandler = null;
        SAXTransformerFactory transformFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try 
        {
            transformerHandler = transformFactory.newTransformerHandler();
        } 
        catch (TransformerConfigurationException e) 
        {
        	System.out.println(e);
        }
        
        // TODO manage resorces better.

        OutputStream out = new FileOutputStream(outputFile);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
            StreamResult streamResult = new StreamResult(osw);
            transformerHandler.setResult(streamResult);
        } catch (UnsupportedEncodingException e) {
            if (out != null) {
                out.close();
            }
            throw new XenaException(
                    "Unsupported encoder for output stream writer.");
        }
        

        normaliser.setContentHandler(transformerHandler);
        
        // do the normalisation!
        try {
            normaliser.getContentHandler().startDocument();
            normaliser.parse(xis);
            normaliser.getContentHandler().endDocument();
        } catch (Exception x) {
        	System.out.println(x);
        } finally {
            // let go the output files and any streams that are using it.
            if (out != null) {
                out.flush();
                out.close();
            }
            outputFile = null;
            normaliser.setProperty("http://xena/file", null);
            normaliser.setContentHandler(null);
            transformerHandler = null;
            System.gc();
        }
        
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			new AudioNormaliserHarness();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XenaException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
