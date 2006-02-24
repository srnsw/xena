package au.gov.naa.digipres.xena.plugin.html.javatools.util;

/**
 * Any object that can be "Reseted" implements this. The meaning of "reset"
 * is rather ill-defined, but usually means to bring itself back to a starting
 * state, to clear its caches and/or to re-read configuration files.
 * @author Chris Bitmead
 */
public interface Resetable {
	public void reset() throws ResetException;
}
