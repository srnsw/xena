package au.gov.naa.digipres.xena.plugin.image;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class PnmFileType extends FileType {

	public static final String PNM_NAME = "PNM";
	public static final String PNM_MIME = "image/x-portable-pixmap";

	@Override
	public String getName() {
		return PNM_NAME;
	}

	@Override
	public String getMimeType() {
		return PNM_MIME;
	}

}
