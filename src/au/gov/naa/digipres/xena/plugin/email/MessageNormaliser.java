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
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.ByteArrayInputSource;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.util.JdomUtil;

/**
 * Normaliser for individual email messages.
 *
 * @author Chris Bitmead
 */
public class MessageNormaliser extends AbstractNormaliser {
	public final static String DATE_FORMAT_STRING = "yyyyMMdd'T'HHmmssZ";
	
	final static String URI = "http://preservation.naa.gov.au/email/1.0";

	final static String PREFIX = "email";

	Message msg;
	
	Logger logger;


	MessageNormaliser(Message msg) {
		this.msg = msg;
		logger = Logger.getLogger(this.getClass().getName());
	}

	public String getName() {
		return "Message";
	}

	XMLReader lastNormaliser;

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
			ch.startElement(URI, "email", "email:email", empty);
			ch.startElement(URI, "headers", "email:headers", empty);
			Enumeration en = msg.getAllHeaders();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
			Pattern tzpat = Pattern.compile(".*([+-][0-9]{4})[^0-9]*");
			while (en.hasMoreElements()) {
				Header head = (Header)en.nextElement();
				AttributesImpl hatt = new AttributesImpl();
				// Reinstate this line when moving to Java 1.5.0
//				hatt.addAttribute(URI, "name", "email:name", "CDATA", head.getName());
				hatt.addAttribute(URI, "name", "name", "CDATA", head.getName());
				ch.startElement(URI, "header", "email:header", hatt);
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
				ch.endElement(URI, "header", "email:header");
			}
			ch.endElement(URI, "headers", "email:headers");

//			log.logMsg(XenaResultsLog.LOG_OK, input.getSystemId(), this, msg.getSubject(), null);
			logger.finest("Normalisation successful - " + 
			              "input: " + input.getSystemId() + ", " +
			              "subject: " + msg.getSubject());
			ch.startElement(URI, "parts", "email:parts", empty);
			Object content = msg.getContent();
			if (content instanceof Multipart) {
				Multipart mp = (Multipart)content;
				for (int j = 0; j < mp.getCount(); j++) {
					BodyPart bp = mp.getBodyPart(j);
					AttributesImpl partatt = new AttributesImpl();
					if (bp.getFileName() != null) {
						partatt.addAttribute(URI, "filename", "email:filename", "CDATA", bp.getFileName());
					}
					if (bp.getDescription() != null) {
						partatt.addAttribute(URI, "description", "email:description", "CDATA", bp.getDescription());
					}
					ch.startElement(URI, "part", "email:part", partatt);
					Type type = null;
					XMLReader norm = null;
					if (bp.getContent() instanceof Message) {
						Message msgatt = (Message)bp.getContent();
						norm = new MessageNormaliser(msgatt);
						type = PluginManager.singleton().getTypeManager().lookup(MsgFileType.class);
					}
//						lastNormaliser.setContentHandler(ch);
//						lastNormaliser.setProperty("http://xena/log", getProperty("http://xena/log"));
//						ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(lastNormaliser, lastInputSource,
//																							  log.getLevel() + 1);
//						((XMLFilter)wrap).setContentHandler(ch);
//						wrap.startDocument();
//						lastNormaliser.parse(new XenaInputSource("http://tmp", null));
//						wrap.endDocument();
//					} else {
					Element part = getPart(msgurl, bp, j + 1, (XenaInputSource)input, type, norm);

                    //TODO - aak 2005/10/06 removed level from wrapTheNormaliser call...
                    //ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(lastNormaliser, lastInputSource,log.getLevel() + 1);
                    ContentHandler wrap = PluginManager.singleton().getNormaliserManager().wrapTheNormaliser(lastNormaliser, lastInputSource);
                    
                    
					((XMLFilter)wrap).setContentHandler(ch);
					/*					if (bp instanceof xena.util.trim.TrimAttachment) {
					   ((XMLReader)wrap).setProperty("http://xena/file", ((xena.util.trim.TrimAttachment)bp).getFile());
					  } */
					wrap.startDocument();
					JdomUtil.writeElement(ch, part);
					wrap.endDocument();
//					}
					ch.endElement(URI, "part", "email:part");
				}
			} else if (content instanceof String || content instanceof InputStream) {
				Element part = getPart(msgurl, msg, 1, (XenaInputSource)input, null, null);
//              TODO - aak 2005/10/06 removed level from wrapTheNormaliser call...
                //ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(lastNormaliser, lastInputSource, log.getLevel() + 1);
                ContentHandler wrap = PluginManager.singleton().getNormaliserManager().wrapTheNormaliser(lastNormaliser, lastInputSource);
                assert ch != null;
				((XMLFilter)wrap).setContentHandler(ch);
				ch.startElement(URI, "part", "email:part", empty);
				wrap.startDocument();
				JdomUtil.writeElement(ch, part);
				wrap.endDocument();
				ch.endElement(URI, "part", "email:part");
			} else {
				throw new SAXException("Unknown email mime type");
			}
			ch.endElement(URI, "parts", "email:parts");
			ch.endElement(URI, "email", "email:email");
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

	Element getPart(URLName url, Part bp, int n, XenaInputSource parent, Type type, XMLReader norm) throws MessagingException, IOException,
		XenaException,
		JDOMException,
		SAXException {
		/*		Namespace ns = Namespace.getNamespace(PREFIX, URI);
		  Element part = new Element("part", ns);
		  if (bp.getFileName() != null) {
		   part.setAttribute("filename", bp.getFileName(), ns);
		  }
		  if (bp.getDescription() != null) {
		   part.setAttribute("description", bp.getDescription(), ns);
		  } */
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
		/*		if (bp instanceof xena.util.trim.TrimMessage) {
		   // If we don't do this special case we infinitely recurse, because
		   // The
		 type = (FileType)TypeManager.singleton().lookup("PlainText");
		  } else { */
		if (type == null) {
			type = PluginManager.singleton().getGuesserManager().mostLikelyType(xis);
		}
//		}
		Element el = null;
		try {
			if (norm == null) {
				norm = PluginManager.singleton().getNormaliserManager().lookup(type); // XXX XYZ
			}
			lastNormaliser = norm;
			xis.setType(type);
			norm.setContentHandler(getContentHandler());
//			ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(norm, xis, log.getLevel());
//			wrap.startDocument();
			el = JdomUtil.parseToElement(norm, xis);
//			wrap.endDocument();
//			part.addContent(el);
		} catch (Exception x) {
//			log.logMsg(XenaResultsLog.LOG_NONORMALISER, xis.getSystemId(), this,
//					   "Falling back to Binary Normalisation. file: " + bp.getFileName() + " subject: " + msg.getSubject(), x);
			logger.log(Level.FINER, 
			           "No Normaliser found, falling back to Binary Normalisation." +
			           "file: " + bp.getFileName() + " subject: " + msg.getSubject(),
			           x);
			el = getBinary(xis);
//			part.addContent(el);
		} 
		
		return el;
	}

	Element getBinary(XenaInputSource xis) throws IOException, JDOMException, SAXException, XenaException {
		Type binaryType = PluginManager.singleton().getTypeManager().lookup("Binary");
		List l = PluginManager.singleton().getNormaliserManager().lookupList(binaryType);
		if (1 <= l.size()) {
			Class cbn = (Class)l.get(0);
			XMLReader bn = lastNormaliser = (XMLReader)PluginManager.singleton().getNormaliserManager().lookupByClass(cbn);
			return JdomUtil.parseToElement(bn, xis);
		}
		return null;
	}
}
