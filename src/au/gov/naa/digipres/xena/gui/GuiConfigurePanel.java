package au.gov.naa.digipres.xena.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.PrintXml;
import au.gov.naa.digipres.xena.kernel.ToXml;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * Base
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GuiConfigurePanel extends JPanel {
	protected JPanel contentPanel = new JPanel();

	protected Border border1;

	protected Border border2;

	protected BorderLayout borderLayout2 = new BorderLayout();

	protected BorderLayout borderLayout1 = new BorderLayout();

	protected JPanel buttonPanel = new JPanel();

	protected JButton cancelButton = new JButton();

	protected JButton finishButton = new JButton();

	protected JButton nextButton = new JButton();

	protected JButton backButton = new JButton();

	protected Component content = null;

	protected GuiConfigureNormaliser guiConfigureNormaliser;

	protected JDialog dialog;

	protected boolean success = false;

	protected XMLReader normaliser;

	protected JMenuBar menuBar = new JMenuBar();

	protected JMenu configurationMenu = new JMenu();

	protected JMenuItem loadMenuItem = new JMenuItem();

	protected JMenuItem saveMenuItem = new JMenuItem();

	protected XenaInputSource inputSource;

	protected Set activated = new HashSet();

	public GuiConfigurePanel(JDialog dialog, XMLReader normaliser, XenaInputSource xis,
							 GuiConfigureNormaliser guiConfigureNormaliser) {
		this.normaliser = normaliser;
		this.inputSource = xis;
		this.dialog = dialog;
		this.guiConfigureNormaliser = guiConfigureNormaliser;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		guiConfigureNormaliser.setButtonPanel(this);
		guiConfigureNormaliser.setNormaliser(normaliser);
		guiConfigureNormaliser.setInputSource(xis);
		try {
			setContent(guiConfigureNormaliser.start(), false);
		} catch (IOException ex) {
			MainFrame.singleton().showError(ex);
		} catch (XenaException ex) {
			MainFrame.singleton().showError(ex);
		}
	}

	public void setContent(Component c, boolean finishUp) throws XenaException {
		if (content != null) {
			if (finishUp && content instanceof GuiConfigureSubPanel) {
				((GuiConfigureSubPanel)content).finish();
			}
			contentPanel.remove(content);
		}
		content = c;
		contentPanel.add(content, BorderLayout.CENTER);
		if (content instanceof GuiConfigureSubPanel) {
			((GuiConfigureSubPanel)content).activate();
			activated.add(content);
		}
		MainFrame.packAndPosition(dialog);
	}

	public void setTitle(String title) {
		dialog.setTitle(title);
	}

	public boolean getSuccess() {
		return success;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public void nextOk(boolean ok) {
		nextButton.setEnabled(ok);
	}

	public void backOk(boolean ok) {
		backButton.setEnabled(ok);
	}

	public void finishOk(boolean ok) {
		finishButton.setEnabled(ok);
	}

	protected XMLReader getNormaliser() {
		return normaliser;
	}

	protected void jbInit() throws Exception {
		border1 = BorderFactory.createLineBorder(Color.black, 2);
		border2 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		this.setLayout(borderLayout1);
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});
		finishButton.setText("Finish");
		finishButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishButton_actionPerformed(e);
			}
		});
		loadMenuItem.setText("Load");
		loadMenuItem.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadMenuItem_actionPerformed(e);
			}
		});
		saveMenuItem.setText("Save");
		saveMenuItem.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveMenuItem_actionPerformed(e);
			}
		});
		configurationMenu.setText("Configuration");
		this.setBorder(border2);
		contentPanel.setLayout(borderLayout2);
		this.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setBorder(border1);
		this.add(buttonPanel, BorderLayout.SOUTH);
		if (guiConfigureNormaliser.isMulti()) {
			nextButton.setToolTipText("");
			nextButton.setText("Next >");
			nextButton.addActionListener(
				new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					nextButton_actionPerformed(e);
				}
			});
			backButton.setText("< Back");
			backButton.addActionListener(
				new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					backButton_actionPerformed(e);
				}
			});
			buttonPanel.add(backButton, null);
			buttonPanel.add(nextButton, null);
		}
		buttonPanel.add(finishButton, null);
		buttonPanel.add(cancelButton, null);
		menuBar.add(configurationMenu);
		configurationMenu.add(loadMenuItem);
		configurationMenu.add(saveMenuItem);
	}

	protected void backButton_actionPerformed(ActionEvent e) {
		int oldScreen = guiConfigureNormaliser.getScreenNum();
		try {
			guiConfigureNormaliser.setScreenNum(guiConfigureNormaliser.
												getScreenNum() - 1);
			setContent(guiConfigureNormaliser.getScreen(), true);
		} catch (Exception ex) {
			guiConfigureNormaliser.setScreenNum(oldScreen);
			try {
				setContent(guiConfigureNormaliser.getScreen(), false);
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}

			MainFrame.singleton().showError(ex);
		}
	}

	protected void nextButton_actionPerformed(ActionEvent e) {
		int oldScreen = guiConfigureNormaliser.getScreenNum();
		try {
			guiConfigureNormaliser.setScreenNum(guiConfigureNormaliser.
												getScreenNum() + 1);
			setContent(guiConfigureNormaliser.getScreen(), true);
		} catch (Exception ex) {
			guiConfigureNormaliser.setScreenNum(oldScreen);
			try {
				setContent(guiConfigureNormaliser.getScreen(), false);
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
			MainFrame.singleton().showError(ex);
		}
	}

	protected void finishButton_actionPerformed(ActionEvent e) {
		try {
			guiConfigureNormaliser.finish(activated);
		} catch (IOException ex) {
			MainFrame.singleton().showError(ex);
		} catch (XenaException ex) {
			MainFrame.singleton().showError(ex);
		}
		dialog.dispose();
		success = true;
	}

	protected void cancelButton_actionPerformed(ActionEvent e) {
		success = false;
		dialog.dispose();
	}

	protected void saveMenuItem_actionPerformed(ActionEvent e) {
		try {
			guiConfigureNormaliser.finish(activated);
			XenaFileChooser chooser = new XenaFileChooser(XenaFileChooser.DEFAULT_CONFIG_DIRECTORY);
			if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = chooser.getSelectedFile();
			if (file != null) {
				Element xml = ToXml.toXml(normaliser);
				Element el = ToXml.toXmlFile(inputSource.getSystemId(), xml);
				FileOutputStream w = new FileOutputStream(file);
				PrintXml.singleton().printXmlWithHeader(el, w);
				w.close();
			}
		} catch (Exception ex) {
			MainFrame.singleton().showError(ex);
		}
	}

	protected void loadMenuItem_actionPerformed(ActionEvent e) {
		try {
			XenaFileChooser chooser = new XenaFileChooser(XenaFileChooser.DEFAULT_CONFIG_DIRECTORY);
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = chooser.getSelectedFile();
			if (file != null) {
				SAXBuilder sax = new SAXBuilder();
				InputStream is = new FileInputStream(file);
				Element el = sax.build(is).detachRootElement();
				Element xml = ToXml.fromXmlFile(el);
				ToXml.fromXmlObject(normaliser, xml);
				setContent(guiConfigureNormaliser.start(), false);
			}
		} catch (JDOMException ex) {
			MainFrame.singleton().showError(ex);
		} catch (IOException ex) {
			MainFrame.singleton().showError(ex);
		} catch (XenaException ex) {
			MainFrame.singleton().showError(ex);
		}
	}
}
