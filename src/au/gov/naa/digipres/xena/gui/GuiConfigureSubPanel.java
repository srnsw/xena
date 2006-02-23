package au.gov.naa.digipres.xena.gui;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Each stage or panel in the GuiConfigure wizard setup should extend this
 * interface, when using the AbstractGuiConfigureNormaliser.
 * @see AbstractGuiConfigureNormaliser
 */
public interface GuiConfigureSubPanel {
	/**
	 * Initialise at the start. Grab default values from the normaliser object.
	 * @throws XenaException
	 */
	public void start() throws XenaException;

	/**
	 * Activate this screen as the current one.
	 * @throws XenaException
	 */
	public void activate() throws XenaException;

	/**
	 * Finish up and clean up. Store all selections within the normaliser object.
	 * @throws XenaException
	 */
	public void finish() throws XenaException;

}
