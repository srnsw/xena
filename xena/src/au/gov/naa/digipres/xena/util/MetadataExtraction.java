package au.gov.naa.digipres.xena.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;

public class MetadataExtraction {

	public static Map<String, String> extractMetadataWithTika(XenaInputSource input) {
		Map<String, String> metadataMap = new HashMap<String, String>();

		// has a file out on disk for tika to parse. 
		boolean hasFile = true;
		if (input.getFile() == null) {
			hasFile = false;
		}

		File tmpFile = null;
		try {

			ParseContext context = new ParseContext();
			Detector detector = (new TikaConfig()).getMimeRepository();
			AutoDetectParser parser = new AutoDetectParser(detector);
			context.set(Parser.class, parser);

			Metadata metadata = new Metadata();
			InputStream inputStream;

			if (hasFile) {
				URL url = new URL(input.getSystemId());
				inputStream = TikaInputStream.get(input.getFile(), metadata);
			} else {
				tmpFile = File.createTempFile("tiki", null);
				tmpFile.deleteOnExit();
				FileOutputStream out = new FileOutputStream(tmpFile);
				inputStream = ((XenaInputSource) input).getByteStream();
				byte[] buff = new byte[2048];
				while (inputStream.read(buff) != -1) {
					out.write(buff);
				}
				inputStream.close();
				out.flush();
				out.close();

				inputStream = TikaInputStream.get(tmpFile.toURI(), metadata);
			}
			parser.parse(inputStream, new DefaultHandler(), metadata, context);

			for (String key : metadata.names()) {
				String data = metadata.get(key);
				if (data == null) {
					data = "";
				}
				metadataMap.put(key, data);
			}

		} catch (MimeTypeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if ((tmpFile != null) && (tmpFile.exists())) {
				tmpFile.delete();
			}
		}

		return metadataMap;
	}
}
