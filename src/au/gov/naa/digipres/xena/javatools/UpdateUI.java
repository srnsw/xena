package au.gov.naa.digipres.xena.javatools;
import javax.swing.*;
import java.awt.*;

public class UpdateUI {
	public static void updateUI(Object c) {
		if (c instanceof JComponent) {
			((JComponent) c).updateUI();
		}
		if (c instanceof Container) {
			Component[] cs = ((Container) c).getComponents();
			for (int i = 0; i < cs.length; i++) {
				updateUI(cs[i]);
			}
		}
		if (c instanceof MenuElement) {
			MenuElement[] mes = ((MenuElement) c).getSubElements();
			for (int i = 0; i < mes.length; i++) {
				updateUI(mes[i]);
			}
		}
	}
}
