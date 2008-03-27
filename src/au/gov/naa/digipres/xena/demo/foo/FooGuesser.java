package au.gov.naa.digipres.xena.demo.foo;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class FooGuesser extends Guesser {

	public static final byte[] FOO_MAGIC = {'~', 'b', 'e', 'g', 'i', 'n', 'F', 'o', 'o', '~'};
	private static final String EXTENSION = "foo";
	private static final String UTF8 = "UTF-8";
	private static final String ASCII = "US-ASCII";

	private Type type;

	public FooGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(FooFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource xis) throws XenaException, IOException {
		Guess guess = new Guess(type);
		// first up - we check the characters.
		// we will only look at the first 64k - if we have gone that far and
		// have had no bad chars, should be okay.
		String charset = CharsetDetector.mustGuessCharSet(xis.getByteStream(), 2 ^ 16);

		if (charset != null && (charset.equals(UTF8) || charset.equals(ASCII))) {
			guess.setDataMatch(true);
		} else {
			guess.setDataMatch(false);
			guess.setPossible(false);
			return guess;
		}

		// now check for our magic number, using the guesserutils compare byte
		// array method...
		byte[] first = new byte[FOO_MAGIC.length];
		xis.getByteStream().read(first);
		byte[] foo_magic_bytes = new byte[FOO_MAGIC.length];

		if (GuesserUtils.compareByteArrays(first, FOO_MAGIC)) {
			guess.setMagicNumber(true);
		} else {
			guess.setMagicNumber(false);
			guess.setPossible(false);
			return guess;
		}

		// check the extension - if it doesnt match leave the extension match at
		// it's default
		// value - 'unknown'.
		String id = xis.getSystemId().toLowerCase();
		if (id.endsWith(EXTENSION)) {
			guess.setExtensionMatch(true);
		}

		// and thats it! return our guess and rejoice!
		return guess;
	}

	@Override
	public String getName() {
		return "FooGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		guess.setExtensionMatch(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}
}
