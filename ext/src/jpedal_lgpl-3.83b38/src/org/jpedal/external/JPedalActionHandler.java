package org.jpedal.external;

import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;

public interface JPedalActionHandler {
	public void actionPerformed(SwingGUI currentGUI, Commands commands);
}
