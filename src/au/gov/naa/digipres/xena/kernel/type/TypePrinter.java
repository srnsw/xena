package au.gov.naa.digipres.xena.kernel.type;
import java.io.File;

import javax.print.Doc;

import au.gov.naa.digipres.xena.kernel.XenaException;

abstract public class TypePrinter {
	abstract public XenaFileType getType() throws XenaException;

	abstract public Doc getDoc(File file) throws XenaException;
}
