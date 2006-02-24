package au.gov.naa.digipres.xena.plugin.email.trim;
import java.io.File;

import javax.mail.Part;

public interface TrimPart extends Part {
	public File getFile();
}
