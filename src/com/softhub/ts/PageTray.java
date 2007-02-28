
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import com.softhub.ts.event.TrayControlEvent;
import com.softhub.ts.event.TrayControlListener;
import com.softhub.ts.event.ViewEvent;
import com.softhub.ts.event.ViewEventListener;

public class PageTray extends JPanel
	implements ViewEventListener, TrayConstants, TrayControlListener, AdjustmentListener
{
	private BorderLayout pageTrayLayout = new BorderLayout();
	private PageLayout portPaneLayout = new PageLayout();
	private FlowLayout trayControlLayout = new FlowLayout(FlowLayout.LEFT, 2, 2);
	private TrayScrollLayout scrollLayout = new TrayScrollLayout();
    private JScrollPane scrollPane = new JScrollPane();
	private PostScriptPane postScriptPane = new PostScriptPane();
	private NavigationPane navigationPane = new NavigationPane();
	private TrayControl trayControlPane = new TrayControl();
	private JPanel controlPane = new JPanel();
	private JViewport viewport = new JViewport();
	private JPanel portPane = new JPanel();
	private JPanel separatorPane = new JPanel();

	public PageTray() {
		try {
		    jbInit();
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(pageTrayLayout);
		controlPane.setLayout(trayControlLayout);
		scrollPane.setLayout(scrollLayout);
		portPane.setLayout(portPaneLayout);
		portPane.setPreferredSize(new Dimension(80, 80));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //scrollPane.setViewportBorder(BorderFactory.createEtchedBorder());
        navigationPane.setPreferredSize(new Dimension(125, 16));
        portPane.add(postScriptPane, PageLayout.CENTER);
		viewport.add(portPane, BorderLayout.CENTER);
		//controlPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		controlPane.setPreferredSize(new Dimension(10, 19));
		trayControlPane.setPreferredSize(new Dimension(110, 16));
		separatorPane.setPreferredSize(new Dimension(3, 16));
		controlPane.add(trayControlPane, null);
		controlPane.add(separatorPane, null);
		controlPane.add(navigationPane, null);
		scrollPane.add(controlPane, CONTROL_BAR);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setViewport(viewport);
		scrollLayout.syncWithScrollPane(scrollPane);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
		this.add(scrollPane, BorderLayout.CENTER);
		trayControlPane.addTrayControlListener(this);
		navigationPane.addNavigationListener(postScriptPane);
		postScriptPane.addViewEventListener(trayControlPane);
		postScriptPane.addViewEventListener(navigationPane);
		postScriptPane.addViewEventListener(this);
	}

	public void init() {
        postScriptPane.createPage();
		postScriptPane.init();
	}

	public void doLayout() {
		Dimension d = postScriptPane.getSize();
		int hgap = portPaneLayout.getGapH() * 2;
		int vgap = portPaneLayout.getGapV() * 2;
		Dimension t = new Dimension(d.width + hgap, d.height + vgap);
		portPane.setPreferredSize(t);
		super.doLayout();
	}

	public void saveProfile(Profile profile) {
		postScriptPane.saveProfile(profile);
	}

	public void restoreProfile(Profile profile) {
		postScriptPane.restoreProfile(profile);
	}

	public PostScriptPane getPostScriptPane() {
		return postScriptPane;
	}

    public void adjustmentValueChanged(AdjustmentEvent evt) {
		postScriptPane.updatePages(viewport.getViewRect());
		postScriptPane.fireViewAdjustEvent();
    }

	public void trayChange(TrayControlEvent evt) {
		postScriptPane.setScale(evt.getScale());
	}

	public void viewChanged(ViewEvent evt) {
		Viewable page = (Viewable) evt.getSource();
		switch (evt.getEventType()) {
		case ViewEvent.PAGE_RESIZE:
		    resizePage(page);
		    break;
		case ViewEvent.PAGE_CHANGE:
		    changePage(page);
		    break;
		}
	}

	private void resizePage(Viewable page) {
		doLayout();
		scrollLayout.syncWithScrollPane(scrollPane);
		scrollPane.setViewport(viewport);
		page.updatePages(viewport.getViewRect());
	}

	private void changePage(Viewable page) {
		viewport.doLayout();
		Point pt = page.getPageOffset();
		viewport.setViewPosition(pt);
		page.updatePages(viewport.getViewRect());
	}

}
