package au.gov.naa.digipres.xena.plugin.pdf;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent PDF files.
 *
 * @author Chris Bitmead
 */
public class PdfFileType extends FileType {
    public String getName() {
        return "PDF";
    }

    public String fileExtension() {
        return "pdf";
    }

    public String getMimeType() {
        return "application/pdf";
    }
}
