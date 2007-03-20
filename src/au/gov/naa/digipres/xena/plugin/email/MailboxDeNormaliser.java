/*
 * Created on 15/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email;

import java.io.File;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

public class MailboxDeNormaliser extends AbstractDeNormaliser
{
	private StringBuilder filenameBuilder;
	private TransformerHandler rootXMLWriter;
	private boolean inItem = false;
	private int messageCounter = 0;

	@Override
	public String getName()
	{
		return "Mailbox Denormaliser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getOutputFileExtension(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) throws XenaException
	{
		return "xml";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (inItem)
		{
			filenameBuilder.append(ch, start, length);
		}
		else
		{
			rootXMLWriter.characters(ch, start, length);
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException
	{
		// Initialise the writer for the root XML file
		
        // create our transform handler
        SAXTransformerFactory transformFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try 
        {
            rootXMLWriter = transformFactory.newTransformerHandler();
        	rootXMLWriter.setResult(streamResult);
        	rootXMLWriter.startDocument();
        } 
        catch (TransformerConfigurationException e) 
        {
            throw new SAXException("Unable to create transformerHandler due to transformer configuration exception.");
        }
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException
	{
        rootXMLWriter.endDocument();		
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		if (qName.equals(EmailToXenaEmailNormaliser.MAILBOX_PREFIX + ":" + EmailToXenaEmailNormaliser.MAILBOX_ITEM_TAG))
		{
			inItem = true;
			filenameBuilder = new StringBuilder();
		}
		else
		{
			rootXMLWriter.startElement(namespaceURI, localName, qName, atts);
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		if (qName.equals(EmailToXenaEmailNormaliser.MAILBOX_PREFIX + ":" + EmailToXenaEmailNormaliser.MAILBOX_ITEM_TAG))
		{
			String messageFilename = filenameBuilder.toString();
			File messageFile = new File(sourceDirectory, messageFilename);
			if (messageFile.exists() && messageFile.isFile())
			{
				messageCounter++;
				String messageExportFilename = messageCounter + "-" + outputFilename;
				
				try
				{
					normaliserManager.export(new XenaInputSource(messageFile), outputDirectory, messageExportFilename, true);
					
					// Write out link to exported message
					AttributesImpl atts = new AttributesImpl();
					rootXMLWriter.startElement(namespaceURI, localName, qName, atts);
					char[] exportFileChars = messageExportFilename.toCharArray();
					rootXMLWriter.characters(exportFileChars, 0, exportFileChars.length);
					rootXMLWriter.endElement(namespaceURI, localName, qName);
				}
				catch (Exception e)
				{
					throw new SAXException("Could not export normalised message - " + messageFilename);
				}
			}
		}
		else
		{
			rootXMLWriter.endElement(namespaceURI, localName, qName);
		}
	}


}
