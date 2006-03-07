package au.gov.naa.digipres.xena.plugin.basic;
import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessIndicator;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

/**
 * Guesser for binary (non-character) files.
 *
 * @author not attributable
 */
public class BinaryGuesser extends Guesser {
	
	private Type type;
		
	/**
	 * @throws XenaException 
	 * 
	 */
	public BinaryGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(BinaryFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		InputStream in = source.getByteStream();
		int c = -1;
		int total = 0;
		while (total < 65536 && 0 <= (c = in.read())) {
			total++;
            //i have a better idea.
            // lets use the xml character validator.
            if (!XMLCharacterValidator.isValidCharacter((char)c)) {
                guess.setDataMatch(GuessIndicator.TRUE);
                break;   
            }
            
//			if (Character.isISOControl(c)) {
//				if (!(c == '\r' || c == '\n' || c == '\t' || c == '\f' || c == '\\')) {
//					guess.setDataMatch(GuessIndicator.TRUE);
//                    System.out.println("Found control char! it is:" + c +
//                                " in hex:" + Integer.toHexString(c) +
//                                " and the char renders as: [" + (char)c + "]" +
//                                " and it is at: " + total +
//                                " and this char valid returns: " + XMLCharacterValidator.isValidCharacter((char)c)) ;
//                    
//                    
//                    
//					break;
//				}
//			}
            
        }
        
        guess.setPriority(GuessPriority.LOW);
		return guess;
	}

    public String getName() {
        return "BinaryGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess bestGuess = new Guess();
		bestGuess.setDataMatch(true);
		return bestGuess;
	}

	@Override
	public Type getType()
	{
		return type;
	}
	
}
