package au.gov.naa.digipres.xena.plugin.image;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class PnmGuesser extends DefaultGuesser {

	public static final String PNM_GUESSER_NAME = "PNM Guesser";

	private static final byte[][] pnmMagic = { {'P', '1'}, {'P', '2'}, {'P', '3'}, {'P', '4'}, {'P', '5'}, {'P', '6'}};
	private static final String[] pnmExtensions = {"ppm", "pgm", "pbm", "pnm"};
	private static final String[] pnmMime =
	    {"image/x-portable-pixmap", "image/x-portable-graymap", "image/x-portable-bitmap", "image/x-portable-anymap"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	public PnmGuesser() {
		super();
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

	@Override
	public String getName() {
		return PNM_GUESSER_NAME;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(PnmFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(pnmExtensions, pnmMagic, pnmMime, type)};
		descriptorArr = tempFileDescriptors;
	}

}
