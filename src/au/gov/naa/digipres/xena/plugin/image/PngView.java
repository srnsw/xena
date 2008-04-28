/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.image;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

/**
 * View  for displaying both Xena PNG as well as Xena JPEG instances.
 *
 */
public class PngView extends XenaView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File imgFile;

	public static class State {
		public final static int NO_FIT_STATE = 0;
		public final static int FIT_TO_HEIGHT_STATE = 1;
		public final static int FIT_TO_WIDTH_STATE = 2;
		public final static int FIT_TO_SIZE_STATE = 3;

		private double widthZoomFactor;
		private double heightZoomFactor;

		private int fitState = FIT_TO_SIZE_STATE;

		private String mesg;

		public State() {
			reset();
		}

		public int getFitState() {
			return fitState;
		}

		public double getZoomFactor() {
			if (widthZoomFactor == heightZoomFactor) {
				return widthZoomFactor;
			}
			return -1.0;
		}

		void reset() {
			fitState = FIT_TO_SIZE_STATE;
			mesg = null;
			widthZoomFactor = 1.0F;
			heightZoomFactor = 1.0F;
		}

		public void setZoomFactor(double zoomFactor) {
			reset();
			fitState = NO_FIT_STATE;
			widthZoomFactor = zoomFactor;
			heightZoomFactor = zoomFactor;
		}

		public void setFitToWidth() {
			reset();
			fitState = FIT_TO_WIDTH_STATE;
			mesg = "Fit to Width";
		}

		public void setFitToHeight() {
			reset();
			fitState = FIT_TO_HEIGHT_STATE;
			mesg = "Fit to Height";
		}

		public void setFitToSize() {
			reset();
			fitState = FIT_TO_SIZE_STATE;
			mesg = "Fit to Size";
		}

		void set(PngView view) {
			double w = widthZoomFactor;
			double h = heightZoomFactor;

			// If we are fitting the image to the window dimension we need to calculate the zoom factor.
			// We never want to scale more than 100%
			switch (fitState) {
			case FIT_TO_HEIGHT_STATE:
				h = view.getHeightFit();
				if (h > 1.0) {
					h = 1.0;
				}
				w = h;
				break;
			case FIT_TO_WIDTH_STATE:
				w = view.getWidthFit();
				if (w > 1.0) {
					w = 1.0;
				}
				h = w;
				break;
			case FIT_TO_SIZE_STATE:
				w = view.getWidthFit();
				h = view.getHeightFit();
				if (w < h) {
					h = w;
				} else {
					w = h;
				}
				if (w > 1.0) {
					w = 1.0;
					h = 1.0;
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
	// MyMenu customItems = new MyMenu();
	//
	// MyMenu menus[];

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

	/*
	 * public void updateViewFromElement() throws XenaException { try { sun.misc.BASE64Decoder decoder = new
	 * sun.misc.BASE64Decoder(); byte[] bytes = decoder.decodeBuffer(getElement().getText()); ImageIcon icon = new
	 * ImageIcon(bytes); label.setIcon(icon); label.setZoomFactor(1.0F, 1.0F); } catch (IOException e) { throw new
	 * XenaException(e); } }
	 */

	@Override
	public ContentHandler getContentHandler() throws XenaException {
		FileOutputStream xenaTempOS = null;
		try {
			imgFile = File.createTempFile("imgview", ".tmp");
			imgFile.deleteOnExit();
			xenaTempOS = new FileOutputStream(imgFile);
		} catch (IOException e) {
			throw new XenaException("Problem creating temporary xena output file", e);
		}

		BinaryDeNormaliser base64Handler = new BinaryDeNormaliser() {
			/*
			 * (non-Javadoc)
			 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
			 */
			@Override
			public void endDocument() {
				ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
				label.setIcon(icon);
				label.setZoomFactor(1.0F, 1.0F);
			}
		};

		StreamResult result = new StreamResult(xenaTempOS);
		base64Handler.setResult(result);
		return base64Handler;
	}

	@Override
	public String getViewName() {
		return "Image View";
	}

	@Override
	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPngFileType.class).getTag())
		       || tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaJpegFileType.class).getTag());
	}

	@Override
	public void initListeners() {
		addPopupListener(popup, label);
		// XenaMenu.initListenersAll(menus);
	}

	// public void makeMenu(JMenu menu) {
	// customItems.makeMenu(menu);
	// }

	private void jbInit() throws Exception {
		// menus = new MyMenu[] {
		// popupItems, customItems};
		popupItems.makeMenu(popup);
		setLayout(new BorderLayout());
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
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ImageIcon image = new ImageIcon();

		double widthZoomFactor = 1.0F;

		double heightZoomFactor = 1.0F;

		public MyLabel() {
			// Add component listener so we can automatically Fit To Size the image when the panel is first made visible.
			// Update the view when the panel is resized
			addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					state.set(PngView.this);
				}

				@Override
				public void componentShown(ComponentEvent e) {
					state.setFitToSize();
					state.set(PngView.this);
				}

			});

		}

		public void setZoomFactor(double w, double h) {
			setZoomFactor(w, h, null);
		}

		public void setZoomFactor(double w, double h, String mesg) {
			widthZoomFactor = w;
			heightZoomFactor = h;
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
			// PngView.this.validate();
		}

		public void setIcon(ImageIcon image) {
			this.image = image;
		}

		public double getZoomFactor() {
			if (widthZoomFactor == heightZoomFactor) {
				return widthZoomFactor;
			}
			return -1.0F;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(getZoomedImageWidth(), getZoomedImageHeight());
		}

		@Override
		public void paint(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image.getImage(), 0, 0, getZoomedImageWidth(), getZoomedImageHeight(), this);
		}

		int getZoomedImageWidth() {
			return (int) (image.getIconWidth() * widthZoomFactor);
		}

		int getZoomedImageHeight() {
			return (int) (image.getIconHeight() * heightZoomFactor);
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

			initListeners();
		}

		public void sync() {
			if (state.getFitState() == State.FIT_TO_SIZE_STATE) {
				fit.setSelected(true);
			} else if (state.getFitState() == State.FIT_TO_WIDTH_STATE) {
				fitWidth.setSelected(true);
			} else if (state.getFitState() == State.FIT_TO_HEIGHT_STATE) {
				fitHeight.setSelected(true);
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
			} else {
				custom.setSelected(true);
			}
		}

		public void makeMenu(Container component) {
			component.add(zoomMenu);
			component.add(fitMenu);
		}

		public void initListeners() {
			twentyFive.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(0.25F);
					state.set(PngView.this);
				}
			});
			fifty.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(0.5F);
					state.set(PngView.this);
				}
			});
			oneHundred.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(1.0F);
					state.set(PngView.this);
				}
			});
			twoHundred.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(2.0F);
					state.set(PngView.this);
				}
			});
			fourHundred.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(4.0F);
					state.set(PngView.this);
				}
			});
			eightHundred.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setZoomFactor(8.0F);
					state.set(PngView.this);
				}
			});
			custom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double preValue = label.getZoomFactor();
					if (preValue < 0.0F) {
						preValue = 1.0F;
					}
					String pzoom =
					    (String) JOptionPane.showInputDialog(null, "Zoom Percentage", "Zoom Percentage", JOptionPane.PLAIN_MESSAGE, null, null,
					                                         Double.toString(preValue * 100));
					if (pzoom != null) {
						try {
							double zoom = Double.parseDouble(pzoom) / 100;
							state.setZoomFactor(zoom);
							state.set(PngView.this);
						} catch (Exception ex) {
							// Do nothing
						}
					}
				}
			});
			fit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setFitToSize();
					state.set(PngView.this);
				}
			});
			fitWidth.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setFitToWidth();
					state.set(PngView.this);
				}
			});
			fitHeight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					state.setFitToHeight();
					state.set(PngView.this);
				}
			});
		}
	}

	double getHeightFit() {
		return (getHeight() - FUDGE_FACTOR) / label.getImageHeight();
	}

	double getWidthFit() {
		return (getWidth() - FUDGE_FACTOR) / label.getImageWidth();
	}

	public State getState() {
		return state;
	}
}
