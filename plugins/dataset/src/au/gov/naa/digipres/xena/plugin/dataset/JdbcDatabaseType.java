package au.gov.naa.digipres.xena.plugin.dataset;
import au.gov.naa.digipres.xena.kernel.type.MiscType;

/**
 * Type representing a JDBC data source
 *
 * @author Chris Bitmead
 */
public class JdbcDatabaseType extends MiscType {
	public String getName() {
		return "JDBC Database";
	}
    
}
