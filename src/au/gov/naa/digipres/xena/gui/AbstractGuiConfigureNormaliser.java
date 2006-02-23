package au.gov.naa.digipres.xena.gui;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * A GuiConfigureNormaliser base class that implements what most instances will
 * want to do. Nothing is forcing you to use this class (yet!), but it is strongly
 * suggested that you try and fit in with this paradigm. When you use this class,
 * each panel in your configuration needs to extend GuiConfigureSubPanel.
 * @see GuiConfigureSubPanel
 */
public abstract class AbstractGuiConfigureNormaliser extends GuiConfigureNormaliser {
	protected List panels = new ArrayList();

	protected List titles = new ArrayList();

	protected List nextOk = new ArrayList();

	private int screenNum;

	public void setScreenNum(int n) {
		screenNum = n;
		setAvailable(screenNum);
	}

	public int getScreenNum() {
		return screenNum;
	}

	public void finish(Set activated) throws XenaException {
		Iterator it = panels.iterator();
		while (it.hasNext()) {
			GuiConfigureSubPanel panel = (GuiConfigureSubPanel)it.next();
			// Better if we don't "finish" when saving configure for non-activated panels.
			if (activated.contains(panel)) {
				panel.finish();
			}
		}
	}

	public Component start() throws XenaException {
		// Initialize the panels in reverse order so that the first panel is
		// initialized last. That way, if it changes the nextOk, it will be
		// the one in effect. We have to set the screenNum because nextOk
		// uses it and panel.start might call it.
		for (screenNum = panels.size() - 1; 0 <= screenNum; screenNum--) {
			GuiConfigureSubPanel panel = (GuiConfigureSubPanel)panels.get(screenNum);
			panel.start();
		}
		screenNum = 0;
		setAvailable(screenNum);
		return (Component)panels.get(screenNum);
	}


	public void nextOk(boolean v) {
		nextOk.set(screenNum, new Boolean(v));
		setAvailable(screenNum);
	}

	public Component getScreen() {
		return (Component)panels.get(screenNum);
	}

	/**
	 * Add a panel to the sequence in the wizard configuration.
	 * @param title title of this panel
	 * @param panel the panel to add
	 */
	protected void addPanel(String title, GuiConfigureSubPanel panel) {
		titles.add(title);
		panels.add(panel);
		nextOk.add(new Boolean(false));
	}

	/**
	 * Set the buttons to be greyed out or not greyed out as applicable.
	 * @param screenNum int
	 */
	protected void setAvailable(int screenNum) {
		this.buttonPanel.nextOk(((Boolean)nextOk.get(screenNum)).booleanValue() && !(screenNum == panels.size() - 1));
		this.buttonPanel.finishOk(((Boolean)nextOk.get(screenNum)).booleanValue() && screenNum == panels.size() - 1);
		this.buttonPanel.backOk(screenNum > 0);
		this.buttonPanel.setTitle((inputSource == null || inputSource.getSystemId() == null ? "" : inputSource.getSystemId() + ": ") +
								  (String)titles.get(screenNum));
	}
}
