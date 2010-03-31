package au.gov.naa.digipres.xena.plugin.audio;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class OggGuesser extends DefaultGuesser {

	public static final String GUESSER_NAME = "OggGuessor";
	private static final byte[][] oggMagic = {{'O', 'g', 'g', 'S'}};
	private static final String[] oggExtensions = {"ogg", "oga", "ogx", "spx"};
	private static final String[] oggMime = {"audio/ogg", "audio/vorbis", "application/ogg"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	public OggGuesser() {
		super();
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

	@Override
	public String getName() {
		return GUESSER_NAME;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(OggType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(oggExtensions, oggMagic, oggMime, type)};
		descriptorArr = tempFileDescriptors;
	}

}
