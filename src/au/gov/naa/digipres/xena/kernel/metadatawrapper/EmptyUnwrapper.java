package au.gov.naa.digipres.xena.kernel.metadatawrapper;
// SAX classes.
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 * Of course, there is no xml to strip off, so, we are in a bit of a bind if we are using this class.
 * 
 *
 * @author aak
 */
public class EmptyUnwrapper extends XMLFilterImpl {
	
}
