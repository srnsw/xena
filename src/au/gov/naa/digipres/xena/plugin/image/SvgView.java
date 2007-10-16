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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 15/05/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.image;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.util.JdomXenaView;

public class SvgView extends JdomXenaView {
	JSVGCanvas svgComp;
	// JSVGComponent svgComp;
	// JSVGScrollPane sp;
	JScrollPane sp;

	public SvgView() {
		super();
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());
		svgComp = new JSVGCanvas();
		// svgComp = new JSVGComponent();
		// sp = new JSVGScrollPane(svgComp);
		sp = new JScrollPane(svgComp);
		this.add(sp, BorderLayout.CENTER);
		svgComp.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
			@Override
            public void gvtBuildCompleted(GVTTreeBuilderEvent evt) {
				// Dimension2D size = svgComp.getSVGDocumentSize();
				// svgComp.setMySize(new Dimension((int)size.getWidth(), (int)size.getHeight()));
				// svgComp.revalidate();
				// sp.revalidate();
				revalidate();
				// setSize(new Dimension((int)size.getWidth(), (int)size.getHeight()));
			}
		});
	}

	@Override
	public String getViewName() {
		return "SVG";
	}

	@Override
	public boolean canShowTag(String tag) throws XenaException {
		return tag.equalsIgnoreCase("svg");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.gov.naa.digipres.xena.util.JdomXenaView#updateViewFromElement()
	 */
	@Override
	public void updateViewFromElement() throws XenaException {
		// The SAX code uses the class loader of the current thread to load the SAX driver,
		// and because this will be the main Xena thread it won't have a
		// reference to the SAX driver. So we'll create a thread, set its ClassLoader
		// to the plugin ClassLoader, and then parse the XML file.
		Thread parseSvgThread = new Thread() {
			/*
			 * (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				// Using a temp file so that namespace links within the XML file are parsed correctly
				svgComp.setURI(getTmpFile().getFile().toURI().toString());
			}
		};

		PluginManager pluginManager = this.getViewManager().getPluginManager();
		parseSvgThread.setContextClassLoader(pluginManager.getDeserClassLoader());

		parseSvgThread.start();

	}

}
