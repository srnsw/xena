/*
 * Copyright (c) 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.looks.plastic;

import java.awt.Color;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicInternalFrameUI;


/**
 * The JGoodies Plastic Look and Feel implementation of <code>InternalFrameUI</code>.<p>
 *
 * Uses a <code>PlasticInternalFrameTitlePane</code>
 * that in turn uses <code>PlasticTheme</code> colors.<p>
 *
 * Although this class is not intended for subclassing, there's no final marker.
 * If you extend this class, carefully obey all explicit and implicit
 * constraints made in this class and its superclasses.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
public class PlasticInternalFrameUI extends BasicInternalFrameUI {


	private static final String FRAME_TYPE	= "JInternalFrame.frameType";
 	public  static final String IS_PALETTE	= "JInternalFrame.isPalette";
	private static final String PALETTE_FRAME	= "palette";
	private static final String OPTION_DIALOG	= "optionDialog";
	private static final Border EMPTY_BORDER	= new EmptyBorder(0, 0, 0, 0);


  	private PlasticInternalFrameTitlePane	titlePane;
 	private PropertyChangeListener			paletteListener;
 	private PropertyChangeListener			contentPaneListener;


	public PlasticInternalFrameUI(JInternalFrame b) {
		super(b);
	}


	public static ComponentUI createUI(JComponent c) {
		return new PlasticInternalFrameUI((JInternalFrame) c);
	}


	public void installUI(JComponent c) {
		frame = (JInternalFrame) c;

		paletteListener		= new PaletteListener    (this);
		contentPaneListener = new ContentPaneListener(this);
		c.addPropertyChangeListener(paletteListener);
		c.addPropertyChangeListener(contentPaneListener);

		super.installUI(c);

		Object paletteProp = c.getClientProperty(IS_PALETTE);
		if (paletteProp != null) {
			setPalette(((Boolean) paletteProp).booleanValue());
		}

		Container content = frame.getContentPane();
		stripContentBorder(content);
	}


	public void uninstallUI(JComponent c) {
		frame = (JInternalFrame) c;

		c.removePropertyChangeListener(paletteListener);
		c.removePropertyChangeListener(contentPaneListener);

		Container cont = ((JInternalFrame) (c)).getContentPane();
		if (cont instanceof JComponent) {
			JComponent content = (JComponent) cont;
			if (content.getBorder() == EMPTY_BORDER) {
				content.setBorder(null);
			}
		}
		super.uninstallUI(c);
	}


    protected void installDefaults() {
    	super.installDefaults();

		/* Enable the content pane to inherit background color
		 * from its parent by setting its background color to null.
		 * Fixes bug#4268949, which has been fixed in 1.4, too. */
		JComponent contentPane = (JComponent) frame.getContentPane();
		if (contentPane != null) {
	          Color bg = contentPane.getBackground();
		  if (bg instanceof UIResource)
		    contentPane.setBackground(null);
		}
		frame.setBackground(UIManager.getLookAndFeelDefaults().getColor("control"));
    }


	protected void installKeyboardActions()	{
    }

	protected void uninstallKeyboardActions()	{
    }


	private void stripContentBorder(Object c) {
		if (c instanceof JComponent) {
			JComponent contentComp = (JComponent) c;
			Border contentBorder = contentComp.getBorder();
			if (contentBorder == null || contentBorder instanceof UIResource) {
				contentComp.setBorder(EMPTY_BORDER);
			}
		}
	}


	protected JComponent createNorthPane(JInternalFrame w) {
		titlePane = new PlasticInternalFrameTitlePane(w);
		return titlePane;
	}


	public void setPalette(boolean isPalette) {
		String key = isPalette ? "InternalFrame.paletteBorder" : "InternalFrame.border";
		LookAndFeel.installBorder(frame, key);
		titlePane.setPalette(isPalette);
	}


	private void setFrameType(String frameType) {
		String   key;
		boolean hasPalette = frameType.equals(PALETTE_FRAME);
		if (frameType.equals(OPTION_DIALOG)) {
			key = "InternalFrame.optionDialogBorder";
		} else if (hasPalette) {
			key = "InternalFrame.paletteBorder";
		} else {
			key = "InternalFrame.border";
		}
		LookAndFeel.installBorder(frame, key);
		titlePane.setPalette(hasPalette);
	}


	private static final class PaletteListener implements PropertyChangeListener {

		private final PlasticInternalFrameUI ui;

		private PaletteListener(PlasticInternalFrameUI ui) { this.ui = ui; }

		public void propertyChange(PropertyChangeEvent e) {
			String name  = e.getPropertyName();
			Object value = e.getNewValue();
			if (name.equals(FRAME_TYPE)) {
				if (value instanceof String) {
					ui.setFrameType((String) value);
				}
			} else if (name.equals(IS_PALETTE)) {
				ui.setPalette(Boolean.TRUE.equals(value));
			}
		}
	}

	private static final class ContentPaneListener implements PropertyChangeListener {

		private final PlasticInternalFrameUI ui;

		private ContentPaneListener(PlasticInternalFrameUI ui) { this.ui = ui; }

		public void propertyChange(PropertyChangeEvent e) {
			String name = e.getPropertyName();
			if (name.equals(JInternalFrame.CONTENT_PANE_PROPERTY)) {
				ui.stripContentBorder(e.getNewValue());
			}
		}
	}

}
