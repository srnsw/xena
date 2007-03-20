package au.gov.naa.digipres.xena.plugin.email;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.URLName;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.ByteArrayInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.JdomUtil;

/**
 * Normaliser for individual email messages.
 *
 * @author Chris Bitmead
 */
public class MessageNormaliser extends AbstractNormaliser {
	public final static String DATE_FORMAT_STRING = "yyyyMMdd'T'HHmmssZ";
	public final static String EMAIL_URI = "http://preservation.naa.gov.au/email/1.0";
	public final static String EMAIL_PREFIX = "email";
	public final static String PART_TAG = "part";
	public final static String FILENAME_ATTRIBUTE = "filename";

	private Message msg;
	
	private Logger logger;

	
    
    
	MessageNormaliser(Message msg, NormaliserManager normaliserManager) {
		this.msg = msg;
		logger = Logger.getLogger(this.getClass().getName());
        this.normaliserManager = normaliserManager;
	}

	public String getName() {
		return "Message";
	}

	AbstractNormaliser lastNormaliser;

	XenaInputSource lastInputSource;

	/**
	 * Note: mail.jar must be earlier in the classpath than the GNU mail providers
	 * otherwise wierd things happen with attachments not being recognized.
	 * @param input
	 * @throws java.io.IOException
	 * @throws org.xml.sax.SAXException
	 */
	public void parse(InputSource input, NormaliserResults results) 
	throws java.io.IOException, org.xml.sax.SAXException {
		try {
			URLName msgurl = new URLName(input.getSystemId());
			AttributesImpl empty = new AttributesImpl();
			ContentHandler ch = getContentHandler();
			ch.startElement(EMAIL_URI, EMAIL_PREFIX, EMAIL_PREFIX + ":email", empty);
			ch.startElement(EMAIL_URI, "headers", EMAIL_PREFIX + ":headers", empty);
			Enumeration en = msg.getAllHeaders();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
			Pattern tzpat = Pattern.compile(".*([+-][0-9]{4})[^0-9]*");
			while (en.hasMoreElements()) {
				Header head = (Header)en.nextElement();
				AttributesImpl hatt = new AttributesImpl();
				// Reinstate this line when moving to Java 1.5.0
//				hatt.addAttribute(URI, "name", "email:name", "CDATA", head.getName());
				hatt.addAttribute(EMAIL_URI, "name", "name", "CDATA", head.getName());
				ch.startElement(EMAIL_URI, "header", EMAIL_PREFIX + ":header", hatt);
				String hstring = null;
				if (head.getName().equals("Date") || head.getName().equals("Sent-Date")) {
					Matcher mat = tzpat.matcher(head.getValue());
					sdf.setTimeZone(TimeZone.getDefault());
					if (mat.matches()) {
						TimeZone tz = TimeZone.getTimeZone("GMT" + mat.group(1));
						sdf.setTimeZone(tz);
					}
					hstring = sdf.format(msg.getSentDate());
				} else if (head.getName().equals("Received-Date")) {
					Matcher mat = tzpat.matcher(head.getValue());
					sdf.setTimeZone(TimeZone.getDefault());
					if (mat.matches()) {
						TimeZone tz = TimeZone.getTimeZone("GMT" + mat.group(1));
						sdf.setTimeZone(tz);
					}
					hstring = sdf.format(msg.getReceivedDate());
				} else {
					hstring = head.getValue();
				}
				char[] hvalue = hstring.toCharArray();
				ch.characters(hvalue, 0, hvalue.length);
				ch.endElement(EMAIL_URI, "header", EMAIL_PREFIX + ":header");
			}
			ch.endElement(EMAIL_URI, "headers", EMAIL_PREFIX + ":headers");

			logger.finest("Normalisation successful - " + 
			              "input: " + input.getSystemId() + ", " +
			              "subject: " + msg.getSubject());
			ch.startElement(EMAIL_URI, "parts", EMAIL_PREFIX + ":parts", empty);
			Object content = msg.getContent();
			if (content instanceof Multipart) {
				Multipart mp = (Multipart)content;
				for (int j = 0; j < mp.getCount(); j++) {
					BodyPart bp = mp.getBodyPart(j);
					AttributesImpl partatt = new AttributesImpl();
					if (bp.getFileName() != null) {
						partatt.addAttribute(EMAIL_URI, FILENAME_ATTRIBUTE, EMAIL_PREFIX + ":" + FILENAME_ATTRIBUTE, "CDATA", bp.getFileName());
					}
					if (bp.getDescription() != null) {
						partatt.addAttribute(EMAIL_URI, "description", EMAIL_PREFIX + ":description", "CDATA", bp.getDescription());
					}
					ch.startElement(EMAIL_URI, "part", EMAIL_PREFIX + ":" + PART_TAG, partatt);
					Type type = null;
					AbstractNormaliser normaliser = null;
					if (bp.getContent() instanceof Message) {
						Message msgatt = (Message)bp.getContent();
						normaliser = new MessageNormaliser(msgatt,normaliserManager);
						type = normaliserManager.getPluginManager().getTypeManager().lookup(MsgFileType.class);
					}
					Element part = getPart(msgurl, bp, j + 1, (XenaInputSource)input, type, normaliser);

                    //TODO - aak 2005/10/06 removed level from wrapTheNormaliser call...
                    //ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(lastNormaliser, lastInputSource,log.getLevel() + 1);
					
					AbstractMetaDataWrapper wrap = normaliserManager.wrapEmbeddedNormaliser(lastNormaliser, lastInputSource, ch);
					
					/*					if (bp instanceof xena.util.trim.TrimAttachment) {
					   ((XMLReader)wrap).setProperty("http://xena/file", ((xena.util.trim.TrimAttachment)bp).getFile());
					  } */
					wrap.startDocument();
					JdomUtil.writeElement(wrap, part);
					wrap.endDocument();
//					}
					ch.endElement(EMAIL_URI, "part", EMAIL_PREFIX + ":" + PART_TAG);
				}
			} else if (content instanceof String || content instanceof InputStream) {
				Element part = getPart(msgurl, msg, 1, (XenaInputSource)input, null, null);
//              TODO - aak 2005/10/06 removed level from wrapTheNormaliser call...
                //ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(lastNormaliser, lastInputSource, log.getLevel() + 1);
				
				AbstractMetaDataWrapper wrap = normaliserManager.wrapEmbeddedNormaliser(lastNormaliser, lastInputSource, ch);
				
				ch.startElement(EMAIL_URI, "part", EMAIL_PREFIX + ":part", empty);
				wrap.startDocument();
				JdomUtil.writeElement(wrap, part);
				wrap.endDocument();
				ch.endElement(EMAIL_URI, "part", EMAIL_PREFIX + ":part");
			} else {
				throw new SAXException("Unknown email mime type");
			}
			ch.endElement(EMAIL_URI, "parts", EMAIL_PREFIX + ":parts");
			ch.endElement(EMAIL_URI, EMAIL_PREFIX, EMAIL_PREFIX + ":email");
		} catch (MessagingException x) {
			throw new SAXException(x);
		} catch (MalformedURLException x) {
			throw new SAXException(x);
		} catch (IOException x) {
			throw new SAXException(x);
		} catch (JDOMException x) {
			throw new SAXException(x);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}

	Element getPart(URLName url, Part bp, int n, XenaInputSource parent, Type type, AbstractNormaliser normaliser) throws MessagingException, IOException,
		XenaException,
		JDOMException,
		SAXException 
	{
		String nuri = url.toString();
		if (!(bp instanceof Message) || bp.getInputStream() != null) {
			if (bp.getFileName() != null) {
				nuri += "/" + bp.getFileName();
			} else {
				nuri += "/" + Integer.toString(n);
			}
		}
		XenaInputSource xis = null;
		
		// There is a requirement for the meta-data to contain the real source location of the attachment,
		// thus this hack specially for Trim where attachments are separate files.
		if (bp.getContent() instanceof Message) {
			xis = lastInputSource = new XenaInputSource(nuri, type);
		} else if (bp instanceof au.gov.naa.digipres.xena.plugin.email.trim.TrimAttachment) {
			xis = lastInputSource = new XenaInputSource(((au.gov.naa.digipres.xena.plugin.email.trim.TrimPart)bp).getFile(), null);
		} else {
			xis = lastInputSource = new ByteArrayInputSource(bp.getInputStream(), null);
			xis.setSystemId(nuri);
		}
		
		xis.setParent(parent);
		
		if (bp.getContentType() != null) {
			xis.setMimeType(bp.getContentType());
		}
		
		if (type == null) {
			type = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(xis);
		}
		
		Element el = null;
		try {
			if (normaliser == null) {
				normaliser = normaliserManager.lookup(type); // XXX XYZ
			}
			lastNormaliser = normaliser;
			xis.setType(type);
			normaliser.setContentHandler(getContentHandler());
			el = JdomUtil.parseToElement(normaliser, xis);
		} catch (Exception x) {
			logger.log(Level.FINER, 
			           "No Normaliser found, falling back to Binary Normalisation." +
			           "file: " + bp.getFileName() + " subject: " + msg.getSubject(),
			           x);
			el = getBinary(xis);
		} 
		
		return el;
	}

	Element getBinary(XenaInputSource xis) throws IOException, JDOMException, SAXException, XenaException {
		Type binaryType = normaliserManager.getPluginManager().getTypeManager().lookup("Binary");
		List l = normaliserManager.lookupList(binaryType);
		if (1 <= l.size()) {
			Class cbn = (Class)l.get(0);
			XMLReader bn = lastNormaliser = (AbstractNormaliser)normaliserManager.lookupByClass(cbn);
			return JdomUtil.parseToElement(bn, xis);
		}
		return null;
	}
}
