package au.gov.naa.digipres.xena.plugin.dataset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.ByteArrayInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XmlList;
import au.gov.naa.digipres.xena.kernel.decoder.Decoder;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Normalise CSV files to Xena dataset.
 *
 * @author Chris Bitmead
 */
public class CsvToXenaDatasetNormaliser extends AbstractNormaliser {
	final static String URI = "http://preservation.naa.gov.au/dataset/1.0";

	final static String PREFIX = "dataset";

	/*
	 *  Fields must be public for the default toXml() to work.
	 */
	public int fieldDelimiter = -1;

	public char quoteCharacter = '\"';

	public boolean firstRowFieldNames = false;

	public static int GUESS_NONE = 0;

	public static int GUESS_SOME = 1;

	public static int GUESS_ALL = 2;

	public int guessFields = GUESS_NONE;

	public int numberOfGuessRows = 10;

	public XmlList fieldCaptionList = new XmlList();
	public XmlList fileTypeList = new XmlList();
	public XmlList decoderList = new XmlList();
	public XmlList normaliserList = new XmlList();

	public boolean useQuoteCharacter = false;

	private char headerFieldDelimiter = ':';

	private boolean oneFieldHeader = false;

	private int oneFieldHeaderNumber = 3;

	public CsvToXenaDatasetNormaliser() {
        
	}

	static void ensureCapacity(ArrayList list, int sz) {
		list.ensureCapacity(sz);
		for (int i = list.size(); i < sz; i++) {
			list.add(i, null);
		}
	}

	public String getName() {
		return "CSV Legacy normaliser";
	}

	public void setQuoteCharacter(char quoteCharacter) {
		this.quoteCharacter = quoteCharacter;
		useQuoteCharacter = true;
	}

	public void setNormaliserList(XmlList normaliserList) {
		this.normaliserList = normaliserList;
	}

	public void setNumberOfGuessRows(int numberOfGuessRows) {
		this.numberOfGuessRows = numberOfGuessRows;
	}

	public void setFirstRowFieldNames(boolean firstRowFieldNames) {
		this.firstRowFieldNames = firstRowFieldNames;
	}

	public void setGuessFields(int guessFields) {
		this.guessFields = guessFields;
	}

	public void setDecoderList(XmlList decoderList) {
		this.decoderList = decoderList;
	}

	public void setFieldDelimiter(char fieldDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
	}

	public void setFileTypeList(XmlList fileTypeList) {
		this.fileTypeList = fileTypeList;
	}

	public void setCaptionList(XmlList fieldCaptionList) {
		this.fieldCaptionList = fieldCaptionList;
	}

	public void setFileType(int i, FileType n) {
		ensureCapacity(fileTypeList, i + 1);
		fileTypeList.set(i, n);
	}

	public void setDecoder(int i, Decoder n) {
		ensureCapacity(decoderList, i + 1);
		decoderList.set(i, n);
	}

	public void setNormaliser(int i, XMLReader n) {
		ensureCapacity(normaliserList, i + 1);
		normaliserList.set(i, n);
	}

	public void setFieldCaption(int i, String n) {
		ensureCapacity(fieldCaptionList, i + 1);
		fieldCaptionList.set(i, n);
	}

	public void setUseQuoteCharacter(boolean useQuoteCharacter) {
		this.useQuoteCharacter = useQuoteCharacter;
	}

	public void setHeaderFieldDelimiter(char headerFieldDelimiter) {
		this.headerFieldDelimiter = headerFieldDelimiter;
	}

	public void setOneFieldHeader(boolean oneFieldHeader) {
		this.oneFieldHeader = oneFieldHeader;
	}

	public void setOneFieldHeaderNumber(int oneFieldHeaderNumber) {
		this.oneFieldHeaderNumber = oneFieldHeaderNumber;
	}

	public XmlList getCaptionList() {
		return fieldCaptionList;
	}

	public XmlList getfileTypeList() {
		return fileTypeList;
	}

	public int getFieldDelimiter(XenaInputSource input) throws XenaException {
		CsvGuesser guesser = new CsvGuesser();
		if (fieldDelimiter < 0) {
			try {
				guesser.guess((XenaInputSource)input);
			} catch (XenaException x) {
				x.printStackTrace();
			} catch (IOException x) {
				x.printStackTrace();
			}
			fieldDelimiter = guesser.getGuessedDelimiter();
		}

		return fieldDelimiter;
	}

	public XmlList getDecoderList() {
		return decoderList;
	}

	public Decoder getDecoder(int i) {
		Decoder rtn = null;
		if (i < decoderList.size()) {
			rtn = (Decoder)decoderList.get(i);
		} else {
			rtn = (Decoder)normaliserManager.getPluginManager().getDecoderManager().getAllDecoders().get(0);
		}
		return rtn;
	}

	public int getGuessFields() {
		return guessFields;
	}

	public int getNumberOfGuessRows() {
		return numberOfGuessRows;
	}

	public XmlList getNormaliserList() {
		return normaliserList;
	}

	public char getQuoteCharacter() {
		return quoteCharacter;
	}

	public boolean isFirstRowFieldNames() {
		return firstRowFieldNames;
	}

	public boolean isUseQuoteCharacter() {
		return useQuoteCharacter;
	}

	public void getPossibleFileTypes(List nextRecord, ArrayList rtn) throws IOException, SAXException, XenaException {
		for (int i = 0; i < nextRecord.size(); i++) {
			ByteArrayInputSource input2 = new ByteArrayInputSource(nextRecord.get(i).toString(), null);
			XenaInputSource dsource = getDecoder(i).decode(input2);
			try {
				List types =  normaliserManager.getPluginManager().getGuesserManager().getPossibleTypes(dsource);
				ensureCapacity(rtn, i + 1);
				List set = (List)rtn.get(i);
				if (set == null) {
					rtn.set(i, new ArrayList(types));
				} else {
					Iterator it = set.iterator();
					while (it.hasNext()) {
						Type type = (Type)it.next();
						if (!types.contains(type)) {
							it.remove();
//							set.remove(type);
						}
					}
				}
			} finally {
				dsource.close();
			}
		}
	}

	public List getPossibleFileTypes(XenaInputSource input, int numberOfGuessRows) throws IOException, SAXException, XenaException {
		ArrayList rtn = null;
		if (guessFields != GUESS_NONE) {
			CsvTokenizer tokenizer = getTokenizer((XenaInputSource)input);
			rtn = new ArrayList();
			List nextRecord = null;
			for (int recNo = 0; recNo != numberOfGuessRows && (nextRecord = tokenizer.nextRecord()) != null; recNo++) {
				getPossibleFileTypes(nextRecord, rtn);
			}
		}
		return rtn;
	}

	/**
	 *  Guesses the most specific type available.
	 *
	 * @param  input            source of data stream
	 * @return                  The fileTypes value
	 * @exception  IOException  Description of Exception
	 */
	public List getFileTypes(XenaInputSource input) throws IOException, SAXException, XenaException {
		List guesses = null;
		if (guessFields != GUESS_NONE) {
			guesses = getPossibleFileTypes(input, -1);
		}
		return getBestFileTypes(guesses);
	}

	/**
	 * @deprecated
	 */
	public List getFileTypes(List record) throws IOException, SAXException, XenaException {
		ArrayList guesses = new ArrayList();
		if (guessFields != GUESS_NONE) {
			getPossibleFileTypes(record, guesses);
		}
		return getBestFileTypes(guesses);
	}

	protected List getBestFileTypes(List guesses) throws IOException {
		List rtn = null;
		if (guessFields != GUESS_NONE) {
			rtn = new ArrayList(guesses.size());
			Iterator it = guesses.iterator();
			while (it.hasNext()) {
				List set = (List)it.next();
				rtn.add(set.get(0));
			}
		} else {
			rtn = fileTypeList;
		}
		return rtn;
	}

	public CsvTokenizer getTokenizer(XenaInputSource is) throws IOException, SAXException, XenaException {
		CsvTokenizer tokenizer = new CsvTokenizer(is.getByteStream());
//		assert 0 <= fieldDelimiter;
		tokenizer.setFieldDelimiter((char)this.getFieldDelimiter(is));
		tokenizer.setUseQuoteCharacter(useQuoteCharacter);
		tokenizer.setQuoteCharacter(quoteCharacter);
		tokenizer.setFirstRowFieldNames(firstRowFieldNames || oneFieldHeader);
		return tokenizer;
	}

	public char getHeaderFieldDelimiter() {
		return headerFieldDelimiter;
	}

	public boolean isOneFieldHeader() {
		return oneFieldHeader;
	}

	public int getOneFieldHeaderNumber() {
		return oneFieldHeaderNumber;
	}

	public static int max(int a, int b) {
		if (a < b) {
			return b;
		}
		return a;
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws IOException, SAXException {
		int maxNumberColumns = 0;
		ContentHandler ch = this.getContentHandler();
		
		CsvTokenizer tokenizer;
		try
		{
			tokenizer = getTokenizer((XenaInputSource)input);
		}
		catch (XenaException e)
		{
			throw new SAXException("Problem getting tokenizer", e);
		}
		/*		CsvGuesser guesser = new CsvGuesser();
		  if (fieldDelimiter < 0) {
		   try {
			guesser.guess((XenaInputSource)input);
		   } catch (XenaException x) {
			throw new SAXException(x);
		   }
		   fieldDelimiter = guesser.getGuessedDelimiter();
		  } */
		AttributesImpl empty = new AttributesImpl();
		ch.startElement(URI, "dataset", "dataset:dataset", empty);
		int recNo = 0;
		List nextRecord = null;
		List firstNames = getNames(tokenizer.getHeader());
		int columns = max(max(fileTypeList.size(), firstNames.size()), fieldCaptionList.size());
		if (0 < firstNames.size() || 0 < fileTypeList.size() || 0 < fieldCaptionList.size()) {
			ch.startElement(URI, "definitions", "dataset:definitions", empty);
			ch.startElement(URI, "field-definitions", "dataset:field-definitions", empty);
			for (int i = 0; i < columns; i++) {
				XenaFileType type = getOutputType(i);
				String sname = getName(firstNames, i);
				String scaption = getFieldCaption(i);
				if (type != null || sname != null || scaption != null) {
					AttributesImpl defatt = new AttributesImpl();
					defatt.addAttribute(URI, "id", "dataset:id", "ID", "f" + String.valueOf(i + 1));
					if (type != null) {
						defatt.addAttribute(URI, "type", "dataset:type", "CDATA", type.getTag());
					}
					ch.startElement(URI, "field-definition", "dataset:field-definition", defatt);
					if (sname != null) {
						ch.startElement(URI, "field-name", "dataset:field-name", empty);
						char[] name = sname.toCharArray();
						ch.characters(name, 0, name.length);
						ch.endElement(URI, "field-name", "dataset:field-name");
					}
					if (scaption != null) {
						ch.startElement(URI, "field-caption", "dataset:field-caption", empty);
						char[] caption = scaption.toCharArray();
						ch.characters(caption, 0, caption.length);
						ch.endElement(URI, "field-caption", "dataset:field-caption");
					}
					ch.endElement(URI, "field-definition", "dataset:field-definition");
				}
			}
			ch.endElement(URI, "field-definitions", "dataset:field-definitions");
			ch.endElement(URI, "definitions", "dataset:definitions");
		}
		XenaFileType xenaStringType = null;
		try {
			xenaStringType = normaliserManager.getPluginManager().getTypeManager().lookupXenaTag("string:string");
		} catch (XenaException x) {
			throw new SAXException(x);
		}
		ch.startPrefixMapping("string", xenaStringType.getNamespaceUri());

		for (int i = 0; i < normaliserList.size(); i++) {
			XenaFileType type = getOutputType(i);
			if (type != null && type.getNamespaceUri() != null) {
				ch.startPrefixMapping(type.getTag(), type.getNamespaceUri());
			}
		}
		ch.startElement(URI, "records", "dataset:records", empty);
		while ((nextRecord = tokenizer.nextRecord()) != null) {
			ch.startElement(URI, "record", "dataset:record", empty);
			int i;
			if (maxNumberColumns < nextRecord.size()) {
				maxNumberColumns = nextRecord.size();
			}
			for (i = 0; i < nextRecord.size(); i++) {
				AttributesImpl fieldatt = new AttributesImpl();
				fieldatt.addAttribute(URI, "idref", "dataset:idref", "IDREF", "f" + String.valueOf(i + 1));
				ch.startElement(URI, "field", "dataset:field", fieldatt);
				AbstractNormaliser fieldNormaliser = getNormaliser(i);
				if (fieldNormaliser == null) {
					Type type = getFileType(i);
					if (type == null) {
						try {
							type = normaliserManager.getPluginManager().getTypeManager().lookup("String");
						} catch (XenaException x) {
							throw new SAXException(x);
						}
					}
					try {
						fieldNormaliser = getNormaliserManager().lookup(type);
					} catch (XenaException x) {
						throw new SAXException(x);
					}
				}

//				FileType ft = (FileType)TypeManager.singleton().lookup(typeString);
				FileType ft = getFileType(i);
				XenaInputSource newis = new ByteArrayInputSource(nextRecord.get(i).toString(), ft);
				XenaInputSource dsource = getDecoder(i).decode(newis);
				fieldNormaliser.setContentHandler(ch);
				try {
                    AbstractMetaDataWrapper wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
                    normaliserManager.parse(fieldNormaliser, dsource, wrapper, results);
				} catch (XenaException x) {
					throw new SAXException(x);
				} finally {
					dsource.close();
				}
				ch.endElement(URI, "field", "dataset:field");
			}
			if (columns < i) {
				columns = i;
			}
			recNo++;
			ch.endElement(URI, "record", "dataset:record");
		}
		ch.endElement(URI, "records", "dataset:records");
		for (int i = 0; i < normaliserList.size(); i++) {
			XenaFileType type = getOutputType(i);
			if (type != null && type.getNamespaceUri() != null) {
				ch.endPrefixMapping(type.getTag());
			}
		}
		ch.endPrefixMapping(xenaStringType.getTag());
		ch.endElement(URI, "dataset", "dataset:dataset");
	}

	List getNames(List header) throws SAXException {
		if (header == null) {
			return new LinkedList();
		} else if (oneFieldHeader) {
			try {
				String headerField = (String)header.get(oneFieldHeaderNumber - 1);
				List rtn = new ArrayList();
				StringTokenizer st = new StringTokenizer(headerField, new String(new char[] {headerFieldDelimiter}));
				while (st.hasMoreTokens()) {
					rtn.add(st.nextToken());
				}
				return rtn;
			} catch (IndexOutOfBoundsException ex) {
				throw new SAXException("One Field Header Index out of Bounds", ex);
			}
		} else {
			return header;
		}
	}

	FileType getFileType(int i) throws SAXException {
		FileType rtn = null;
		if (i < fileTypeList.size()) {
			rtn = (FileType)fileTypeList.get(i);
		} else {
			rtn = null;
		}
		return rtn;
	}

	String getName(List names, int i) {
		String rtn = null;
		if (names != null && i < names.size()) {
			rtn = names.get(i).toString();
		} else {
			rtn = null;
		}
		return rtn;
	}

	String getFieldCaption(int i) {
		String rtn = null;
		if (i < fieldCaptionList.size()) {
			rtn = (String)fieldCaptionList.get(i);
		} else {
			rtn = null;
		}
		return rtn;
	}

	AbstractNormaliser getNormaliser(int i) throws SAXException {
		AbstractNormaliser rtn = null;
		if (i < normaliserList.size()) {
			rtn = (AbstractNormaliser)normaliserList.get(i);
		}
		if (rtn == null) {
			Type type = getFileType(i);
			try {
				rtn = normaliserManager.lookup(type);
			} catch (XenaException x) {
				// No Normaliser found
				rtn = null;
			}
		}
		return rtn;
	}

	XenaFileType getOutputType(int i) throws SAXException {
		XMLReader reader = getNormaliser(i);
		if (reader == null) {
			return null;
		} else {
			return (XenaFileType)normaliserManager.getOutputType(reader.getClass());
		}
	}
}
