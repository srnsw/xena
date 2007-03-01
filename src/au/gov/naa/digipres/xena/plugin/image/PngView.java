package au.gov.naa.digipres.xena.plugin.image;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

/**
 * View  for displaying both Xena PNG as well as Xena JPEG instances.
 *
 * @author Chris Bitmead
 */
public class PngView extends XenaView {
	
	private File imgFile;
	
	public static class State {
		private double widthZoomFactor;

		private double heightZoomFactor;

		private boolean fitToWidth;

		private boolean fitToHeight;

		private boolean fitToSize;

		private String mesg;

		public State() {
			reset();
		}

		public double getZoomFactor() {
			if (widthZoomFactor == heightZoomFactor) {
				return widthZoomFactor;
			} else {
				return -1.0;
			}
		}

		void reset() {
			fitToWidth = fitToHeight = fitToSize = false;
			mesg = null;
			widthZoomFactor = 1.0F;
			heightZoomFactor = 1.0F;
		}

		public void setZoomFactor(double zoomFactor) {
			reset();
			this.widthZoomFactor = zoomFactor;
			this.heightZoomFactor = zoomFactor;
		}

		public void setFitToWidth() {
			reset();
			fitToWidth = true;
			mesg = "Fit to Width";
		}

		public boolean isFitToWidth() {
			return fitToWidth && !(fitToHeight || fitToSize);
		}

		public boolean isFitToHeight() {
			return fitToHeight && !(fitToWidth || fitToSize);
		}

		public boolean isFitToBoth() {
			return fitToWidth && fitToHeight && !fitToSize;
		}

		public boolean isFitToSize() {
			return fitToSize;
		}

		public void setFitToHeight() {
			reset();
			fitToHeight = true;
			mesg = "Fit to Height";
		}

		public void setFitToBoth() {
			reset();
			fitToWidth = true;
			fitToHeight = true;
			mesg = "Fit to Both";
		}

		public void setFitToSize() {
			reset();
			fitToWidth = true;
			fitToHeight = true;
			fitToSize = true;
			mesg = "Fit to Size";
		}

		void set(PngView view) {
			double w = widthZoomFactor;
			double h = heightZoomFactor;
			if (fitToWidth) {
				w = view.getWidthFit();
			}
			if (fitToHeight) {
				h = view.getHeightFit();
			}
			if (fitToSize) {
				if (w < h) {
					h = w;
				} else {
					w = h;
				}
			}
			view.label.setZoomFactor(w, h, mesg);
		}
	}

	State state = new State();

	static double FUDGE_FACTOR = 20.0;

	JScrollPane scrollPane = new JScrollPane();

	MyLabel label = new MyLabel();

	MyMenu popupItems = new MyMenu();
//
//	MyMenu customItems = new MyMenu();
//
//	MyMenu menus[];

	JPopupMenu popup = new JPopupMenu();

	float scale;

	JLabel statusBar = new JLabel();

	public PngView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*	public void updateViewFromElement() throws XenaException {
	  try {
	   sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
	   byte[] bytes = decoder.decodeBuffer(getElement().getText());
	   ImageIcon icon = new ImageIcon(bytes);
	   label.setIcon(icon);
	   label.setZoomFactor(1.0F, 1.0F);
	  } catch (IOException e) {
	   throw new XenaException(e);
	  }
	 }
	 */

	public ContentHandler getContentHandler() throws XenaException 
	{
		FileOutputStream xenaTempOS = null;
        try
		{
    		imgFile = File.createTempFile("imgview", ".tmp");
    		imgFile.deleteOnExit();
            xenaTempOS = new FileOutputStream(imgFile);
		}
		catch (IOException e)
		{
			throw new XenaException("Problem creating temporary xena output file", e);
		}
		
		BinaryDeNormaliser base64Handler = new BinaryDeNormaliser()
		{
			/* (non-Javadoc)
			 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
			 */
			@Override
			public void endDocument() throws SAXException
			{
		        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
				label.setIcon(icon);
				label.setZoomFactor(1.0F, 1.0F);
			}
		};
		
 		StreamResult result = new StreamResult(xenaTempOS);
 		base64Handler.setResult(result);
		return base64Handler;
	}

	public String getViewName() {
		return "Image View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPngFileType.class).getTag()) ||
			tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaJpegFileType.class).getTag());
	}

	public void initListeners() {
		addPopupListener(popup, label);
//		XenaMenu.initListenersAll(menus);
	}

//	public void makeMenu(JMenu menu) {
//		customItems.makeMenu(menu);
//	}

	private void jbInit() throws Exception {
//		menus = new MyMenu[] {
//			popupItems, customItems};
		popupItems.makeMenu(popup);
		this.setLayout(new BorderLayout());
		scrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		statusBar.setText(" ");
		scrollPane.getViewport().add(label);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.SOUTH);
	}

	public State getXenaExternalState() {
		return state;
	}

	public void setXenaExternalState(State v) {
		state = v;
		v.set(this);
	}

	public class MyLabel extends JPanel {
		ImageIcon image = new ImageIcon();

		double widthZoomFactor = 1.0F;

		double heightZoomFactor = 1.0F;

		public MyLabel() {
		}

		public void setZoomFactor(double w, double h) {
			setZoomFactor(w, h, null);
		}

		public void setZoomFactor(double w, double h, String mesg) {
			this.widthZoomFactor = w;
			this.heightZoomFactor = h;
			String s = "Zoom Factor: ";
			if (w == h) {
				s += widthZoomFactor * 100.0F + "%";
			} else {
				s += "Width: " + widthZoomFactor * 100.0F + "% Height: " + heightZoomFactor * 100.0F + "%";
			}
			if (mesg != null) {
				s += " " + mesg;
			}
			statusBar.setText(s);
			popupItems.sync();
			updateUI();
//			PngView.this.validate();
		}

		public void setIcon(ImageIcon image) {
			this.image = image;
		}

		public double getZoomFactor() {
			if (widthZoomFactor == heightZoomFactor) {
				return widthZoomFactor;
			} else {
				return -1.0F;
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(getZoomedImageWidth(), getZoomedImageHeight());
		}

		public void paint(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image.getImage(), 0, 0, getZoomedImageWidth(), getZoomedImageHeight(), this);
		}

		int getZoomedImageWidth() {
			return (int)(image.getIconWidth() * widthZoomFactor);
		}

		int getZoomedImageHeight() {
			return (int)(image.getIconHeight() * heightZoomFactor);
		}

		int getImageWidth() {
			return image.getIconWidth();
		}

		int getImageHeight() {
			return image.getIconHeight();
		}
	}

	class MyMenu {
		/**
		 *  We seem to need a fudge Factor to make it fit exactly.
		 */

		public JRadioButtonMenuItem twentyFive = new JRadioButtonMenuItem("25%");

		public JRadioButtonMenuItem fifty = new JRadioButtonMenuItem("50%");

		public JRadioButtonMenuItem oneHundred = new JRadioButtonMenuItem("100%");

		public JRadioButtonMenuItem twoHundred = new JRadioButtonMenuItem("200%");

		public JRadioButtonMenuItem fourHundred = new JRadioButtonMenuItem("400%");

		public JRadioButtonMenuItem eightHundred = new JRadioButtonMenuItem("800%");

		public JRadioButtonMenuItem custom = new JRadioButtonMenuItem("Custom");

		public JMenu zoomMenu = new JMenu("Zoom");

		public JMenu fitMenu = new JMenu("Fit");

		public JRadioButtonMenuItem fit = new JRadioButtonMenuItem("Fit To Size");

		public JRadioButtonMenuItem fitWidth = new JRadioButtonMenuItem("Fit To Width");

		public JRadioButtonMenuItem fitHeight = new JRadioButtonMenuItem("Fit To Height");

		public JRadioButtonMenuItem fitAll = new JRadioButtonMenuItem("Fit To Both");

		MyMenu() {
			ButtonGroup group = new ButtonGroup();
			oneHundred.setSelected(true);
			group.add(twentyFive);
			group.add(fifty);
			group.add(oneHundred);
			group.add(twoHundred);
			group.add(fourHundred);
			group.add(eightHundred);
			group.add(custom);
			group.add(fit);
			group.add(fitWidth);
			group.add(fitHeight);
			group.add(fitAll);
			zoomMenu.add(twentyFive);
			zoomMenu.add(fifty);
			zoomMenu.add(oneHundred);
			zoomMenu.add(twoHundred);
			zoomMenu.add(fourHundred);
			zoomMenu.add(eightHundred);
			zoomMenu.add(custom);
			fitMenu.add(fit);
			fitMenu.add(fitWidth);
			fitMenu.add(fitHeight);
			fitMenu.add(fitAll);
			
			initListeners();
		}

		public void sync() {
			if (state.isFitToSize()) {
				fit.setSelected(true);
			} else if (state.isFitToWidth()) {
				fitWidth.setSelected(true);
			} else if (state.isFitToHeight()) {
				fitHeight.setSelected(true);
			} else if (state.isFitToBoth()) {
				fitAll.setSelected(true);
			} else if (state.getZoomFactor() == 0.25F) {
				twentyFive.setSelected(true);
			} else if (state.getZoomFactor() == 0.5F) {
				fifty.setSelected(true);
			} else if (state.getZoomFactor() == 1.0F) {
				oneHundred.setSelected(true);
			} else if (state.getZoomFactor() == 2.0F) {
				twoHundred.setSelected(true);
			} else if (state.getZoomFactor() == 4.0F) {
				fourHundred.setSelected(true);
			} else if (state.getZoomFactor() == 8.0F) {
				eightHundred.setSelected(true);
			} 
			else {
				custom.setSelected(true);
			}
		}

		public void makeMenu(Container component) {
			component.add(zoomMenu);
			component.add(fitMenu);
		}

		public void initListeners() {
			twentyFive.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(0.25F);
					state.set(PngView.this);
//					label.setZoomFactor(0.25F, 0.25F);
				}
			});
			fifty.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(0.5F);
					state.set(PngView.this);
				}
			});
			oneHundred.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(1.0F);
					state.set(PngView.this);
				}
			});
			twoHundred.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(2.0F);
					state.set(PngView.this);
				}
			});
			fourHundred.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(4.0F);
					state.set(PngView.this);
				}
			});
			eightHundred.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(8.0F);
					state.set(PngView.this);
				}
			});
			custom.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double preValue = label.getZoomFactor();
					if (preValue < 0.0F) {
						preValue = 1.0F;
					}
					String pzoom = (String)
						JOptionPane.showInputDialog(null,
													"Zoom Percentage",
													"Zoom Percentage",
													JOptionPane.PLAIN_MESSAGE,
													null,
													null,
													Double.toString(preValue * 100));
					if (pzoom != null) {
						try {
							double zoom = Double.parseDouble(pzoom) / 100;
							state.setZoomFactor(zoom);
							state.set(PngView.this);
						} catch (Exception ex) {
							
						}
					}
				}
			});
			fit.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setFitToSize();
					state.set(PngView.this);
				}
			});
			fitWidth.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setFitToWidth();
					state.set(PngView.this);
				}
			});
			fitHeight.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setFitToHeight();
					state.set(PngView.this);
				}
			});
			fitAll.addActionListener(
				new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setFitToBoth();
					state.set(PngView.this);
				}
			});
		}
	}

	double getHeightFit() {
		return (((double)getHeight()) - FUDGE_FACTOR) / label.getImageHeight();
	}

	double getWidthFit() {
		return (((double)getWidth()) - FUDGE_FACTOR) / label.getImageWidth();
	}

	public State getState() {
		return state;
	}
}
