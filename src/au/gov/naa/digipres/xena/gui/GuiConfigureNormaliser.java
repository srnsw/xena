package au.gov.naa.digipres.xena.gui;
import java.awt.Component;
import java.io.IOException;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Abstract class for a class to manage the guis for normalisers which require
 * configuring through a GUI. Instances of this class tend to do more or less
 * the same things, so the need to inherit this class might perhaps be able to
 * be done away with at some future time.
 *
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
public abstract class GuiConfigureNormaliser {
	protected XMLReader normaliser;

	protected GuiConfigurePanel buttonPanel;

	XenaInputSource inputSource;

	public GuiConfigureNormaliser() {
	}

	abstract public void setScreenNum(int n);

	abstract public int getScreenNum();

	/**
	 * @param  normaliser  The normaliser associated with this class
	 */
	public void setNormaliser(XMLReader normaliser) {
		this.normaliser = normaliser;
	}

	public void setButtonPanel(GuiConfigurePanel panel) {
		this.buttonPanel = panel;
	}

	public XMLReader getNormaliser() {
		return normaliser;
	}

	public boolean isMulti() {
		return true;
	}

	public abstract Component getScreen() throws IOException, XenaException;

	abstract public Component start() throws IOException, XenaException;

	public abstract void finish(java.util.Set activated) throws IOException, XenaException;

	/**
	 * @return    The normaliser class associated with this class
	 */
	public abstract Class normaliserClass();

	public Type normaliserType() throws XenaException {
		return null;
	}

	protected GuiConfigurePanel getButtonPanel() {
		return buttonPanel;
	}

	public XenaInputSource getInputSource() {
		return inputSource;
	}

	public void setInputSource(XenaInputSource inputSource) {
		this.inputSource = inputSource;
	}

	/**
	 * Set whether or not it is ok for the user to now navigate to the next
	 * panel in the configuration wizard.
	 * @param v boolean
	 */
	abstract public void nextOk(boolean v);
}
