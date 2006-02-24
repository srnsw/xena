package au.gov.naa.digipres.xena.plugin.html;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for HTML files.
 *
 * @author Chris Bitmead
 */
public class HtmlGuesser extends Guesser {
	
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess((FileType)TypeManager.singleton().lookup(HtmlFileType.class));
		String type = source.getMimeType();
		if (type != null && type.equals("text/html")) {
            guess.setMimeMatch(true);
		}
        
        

        FileName name = new FileName(source.getSystemId());
        String extension = name.extenstionNotNull();
        if (extension.equals("html") || extension.equals("htm") ) {
            guess.setExtensionMatch(true);
        }
        
        
        
        //okay lets have a look inside...
        // Imagine we have a huge binary file containing all nulls.
		// Without this limitation, the program will run out of memory...
		byte[] buf = new byte[1024 * 1024];
		InputStream sourceStream = source.getByteStream();
        int sz = sourceStream.read(buf);
		ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, sz);
		BufferedReader br = new BufferedReader(new java.io.InputStreamReader(bais));
		String line;
        // read the first 
		for (int i = 0; i < 100 && (line = br.readLine()) != null; i++) {
		    if (0 <= line.toLowerCase().indexOf("<html")) {
		        guess.setDataMatch(true);
		        break;
            }
		}
        sourceStream.close();
        
        
        
        
		return guess;
	}
    
    public String getName() {
        return "HTMLGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setDataMatch(true);
		return guess;
	}

}
