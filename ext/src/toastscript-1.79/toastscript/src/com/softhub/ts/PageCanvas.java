
package com.softhub.ts;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.swing.JPanel;
import com.softhub.ps.graphics.Drawable;

public class PageCanvas extends JPanel {

	private Painter painter;
	private Drawable content;
    private BufferedImage image;
	private float scale = 1;
	private float contentScale = 1;
	private boolean printing;

	public PageCanvas() {
		try {
		    jbInit();
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setBackground(Color.white);
	}

	public void setContent(Drawable content, float contentScale) {
		this.content = content;
		this.contentScale = contentScale;
	}

    public Object getContent() {
		return content;
    }

    public void setScale(float scale) {
		this.scale = scale;
		image = null;
    }

    public float getScale() {
		return scale;
    }

	public void activate(boolean active) {
		if (active) {
			if (image != null) {
				int w = image.getWidth(this);
				int h = image.getHeight(this);
				Dimension d = getSize();
				if (w != d.width || h != d.height) {
					image = null;
				}
			}
		} else {
			image = null;
		}
		if (image == null && painter != null) {
			content.interrupt();
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (content != null) {
			Graphics2D g2d = (Graphics2D) g;
			if (printing) {
				AffineTransform xform = g2d.getTransform();
				if (!xform.isIdentity()) {
					// Why are we called twice? First call is ident matrix.
				    float dpi = getToolkit().getScreenResolution();
				    float factor = 72 / dpi;
				    g2d.scale(factor, factor);
				    draw(g2d);
				}
			} else {
			    drawImage(g2d);
			}
		}
	}

	public void print(Graphics g) {
		try {
			printing = true;
		    super.print(g);
		} finally {
			printing = false;
		}
	}

	public void save(OutputStream stream, String format) throws IOException {
		BufferedImage image = createImage(BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		Dimension d = getSize();
		g.setClip(0, 0, d.width, d.height);
		g.setColor(Color.white);
		g.fillRect(0, 0, d.width, d.height);
		draw(g);
		g.dispose();
		Iterator iterator = ImageIO.getImageWritersByFormatName(format);
		if (iterator.hasNext()) {
			ImageWriter encoder = (ImageWriter) iterator.next();
			JPEGImageWriteParam param = new JPEGImageWriteParam(null);
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(0.9f);
			encoder.setOutput(ImageIO.createImageOutputStream(stream));
			IIOImage iioImage = new IIOImage(image, null, null);
			encoder.write(null, iioImage, param);
		} else {
			throw new RuntimeException(format + " not supported");
		}
	}

	protected void draw(Graphics2D g) {
		if (content == null)
			return;
		float factor = scale / contentScale;
		g.setClip(getBounds());
		g.scale(factor, factor);
		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON
		);
        content.draw(g);
	}

	protected void drawImage(Graphics2D g) {
        if (image == null) {
			image = createImage();
		    if (painter == null) {
				Graphics2D g2d = image.createGraphics();
		        painter = new Painter(g2d);
		    }
        }
        g.drawImage(image, null, 0, 0);
	}

    protected BufferedImage createImage() {
		return createImage(BufferedImage.TYPE_INT_ARGB);
    }

    protected BufferedImage createImage(int type) {
        Dimension d = getSize();
        return new BufferedImage(d.width, d.height, type);
    }

	class Painter implements Runnable {

		private Graphics2D graphics;

		Painter(Graphics2D graphics) {
		    this.graphics = graphics;
			Thread thread = new Thread(this);
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		}

		public void run() {
			draw(graphics);
			graphics.dispose();
			painter = null;
			repaint();
		}

	}

}
