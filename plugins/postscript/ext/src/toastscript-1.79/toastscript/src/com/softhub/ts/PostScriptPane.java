
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.*;
import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JPanel;
import com.softhub.ps.Interpreter;
import com.softhub.ps.device.DefaultPageDevice;
import com.softhub.ps.device.PageDevice;
import com.softhub.ps.device.PageEvent;
import com.softhub.ps.device.PageEventListener;
import com.softhub.ps.graphics.Drawable;
import com.softhub.ps.util.Console;
import com.softhub.ps.util.StreamAdapter;
import com.softhub.ts.event.NavigationEvent;
import com.softhub.ts.event.NavigationListener;
import com.softhub.ts.event.ViewEvent;
import com.softhub.ts.event.ViewEventListener;

public class PostScriptPane extends JPanel
	implements Viewable, Pageable, Printable, PageEventListener,
		       NavigationListener, ActionListener
{
	private final static float DEFAULT_PAGE_WIDTH = 576;
	private final static float DEFAULT_PAGE_HEIGHT = 720;
	private final static float DEFAULT_PAGE_SCALE = 1;

	private PageFlowLayout layout = new PageFlowLayout();
	private StreamAdapter consoleAdapter = new StreamAdapter();
	private StreamAdapter interpreterAdapter = new StreamAdapter();
	private Console console;
	private Interpreter interpreter;
	private PageDevice device;
	private PageFormat pageFormat = new PageFormat();
	private Vector pages = new Vector();
	private Vector listeners = new Vector();
	private int currentPageIndex;
	private float currentPageWidth = DEFAULT_PAGE_WIDTH;
	private float currentPageHeight = DEFAULT_PAGE_HEIGHT;
	private float currentPageScale = DEFAULT_PAGE_SCALE;
	private int currentPageOrientation;
	private boolean firstPage;

	public PostScriptPane() {
		try {
	    	jbInit();
		} catch (Exception ex) {
	    	ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setLayout(layout);
	}

	public void init() {
		try {
			device = createPageDevice();
			device.addPageEventListener(this);
			consoleAdapter.connect(interpreterAdapter);
			console = new Console(consoleAdapter.in, consoleAdapter.out);
			interpreter = new Interpreter(interpreterAdapter.in, interpreterAdapter.out);
			float size[] = {currentPageWidth, currentPageHeight};
		    device.setPageSize(size);
			device.setOrientation(currentPageOrientation);
			device.setScale(currentPageScale);
			interpreter.init(device);
			// set the initial page margins for printing
			Paper paper = pageFormat.getPaper();
			double w = paper.getWidth();
			double h = paper.getHeight();
			paper.setImageableArea(0, 0, w, h);
			pageFormat.setPaper(paper);
		} catch (Exception ex) {
	    	ex.printStackTrace();
		}
	}

	public void addViewEventListener(ViewEventListener listener) {
		listeners.addElement(listener);
	}

	public void removeViewEventListener(ViewEventListener listener) {
		listeners.removeElement(listener);
	}

	protected void fireViewEvent(ViewEvent evt) {
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			ViewEventListener listener = (ViewEventListener) e.nextElement();
			listener.viewChanged(evt);
		}
	}

	protected void fireViewChangeEvent() {
		fireViewEvent(new ViewEvent(this, ViewEvent.PAGE_CHANGE));
	}

	protected void fireViewAdjustEvent() {
		fireViewEvent(new ViewEvent(this, ViewEvent.PAGE_ADJUST));
	}

	protected void fireViewResizeEvent() {
		fireViewEvent(new ViewEvent(this, ViewEvent.PAGE_RESIZE));
	}

	protected PageDevice createPageDevice() {
		return new DefaultPageDevice();
	}

	public PagePane createPage() {
		PagePane pagePane = new PagePane();
		float width, height;
		if (currentPageOrientation == 0) {
			width = currentPageWidth;
			height = currentPageHeight;
		} else {
			width = currentPageHeight;
			height = currentPageWidth;
		}
		pagePane.updatePageSize(width, height, currentPageScale);
		add(pagePane, null);
		pages.addElement(pagePane);
		doLayout();
		return pagePane;
	}

	public void addPage(Drawable content) {
		PagePane pagePane = getCurrentPage();
		PageCanvas canvas = pagePane.getPageCanvas();
		if (canvas.getContent() != null) {
		    pagePane = createPage();
			canvas = pagePane.getPageCanvas();
		}
		canvas.setContent(content, getScale());
		fireViewResizeEvent();
	}

    public void removePage(PagePane pagePane) {
        pages.removeElement(pagePane);
        remove(pagePane);
        if (getPageCount() <= 0) {
            createPage();
        }
        fireViewResizeEvent();
    }

	public void removeAllPages() {
		Enumeration e = pages.elements();
		while (e.hasMoreElements()) {
		    remove((PagePane) e.nextElement());
		}
		pages.removeAllElements();
		currentPageIndex = 0;
		createPage();
		fireViewResizeEvent();
	}

    protected void resizePage(PageDevice device) {
		currentPageWidth = device.getPageWidth();
		currentPageHeight = device.getPageHeight();
		PagePane pagePane = getCurrentPage();
		currentPageScale = device.getScale();
		float width, height;
		if (currentPageOrientation == 0) {
			width = currentPageWidth;
			height = currentPageHeight;
		} else {
			width = currentPageHeight;
			height = currentPageWidth;
		}
		pagePane.updatePageSize(width, height, currentPageScale);
		doLayout();
		fireViewResizeEvent();
    }

	public void saveProfile(Profile profile) {
		profile.setFloat("page.width", currentPageWidth);
		profile.setFloat("page.height", currentPageHeight);
		profile.setFloat("page.scale", currentPageScale);
		profile.setInteger("page.orientation", currentPageOrientation);
	}

	public void restoreProfile(Profile profile) {
		currentPageWidth = profile.getFloat("page.width", DEFAULT_PAGE_WIDTH);
		currentPageHeight = profile.getFloat("page.height", DEFAULT_PAGE_HEIGHT);
		currentPageScale = profile.getFloat("page.scale", DEFAULT_PAGE_SCALE);
		currentPageOrientation = profile.getInteger("page.orientation", 0);
	}

	public Console getConsole() {
		return console;
	}

	public void actionPerformed(ActionEvent evt) {
		exec(evt.getActionCommand());
	}

	public void exec(String code) {
		consoleAdapter.out.println(code);
	}

	public void run(File file) {
		try {
			String path = file.getPath().replace('\\', '/');
			exec("(" + path + ") statusdict /jobserver get exec");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void save(OutputStream stream, String format) throws IOException {
		PagePane pagePane = getCurrentPage();
		PageCanvas canvas = pagePane.getPageCanvas();
		canvas.save(stream, format);
	}

	public void pageDeviceChanged(PageEvent evt) {
		PageDevice dev = evt.getPageDevice();
		switch (evt.getType()) {
		case PageEvent.BEGINJOB:
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			firstPage = true;
			break;
		case PageEvent.ENDJOB:
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		    break;
		case PageEvent.SHOWPAGE:
		case PageEvent.COPYPAGE:
            addPage(dev.getContent());
			if (firstPage) {
				showCurrentPage();
				firstPage = false;
			}
			break;
		case PageEvent.RESIZE:
			resizePage(dev);
 			break;
		case PageEvent.ERROR:
			console.setVisible(true);
			break;
		}
	}

	public void pageChange(NavigationEvent evt) {
		setPageIndex(evt.getPageIndex());
	}

	public int getNumberOfPages() {
		return getPageCount();
	}

	public PageFormat getPageFormat(int pageIndex) {
		return pageFormat;
	}

	public Printable getPrintable(int pageIndex) {
		return this;
	}

    public void printSetup() {
		PrinterJob job = PrinterJob.getPrinterJob();
		pageFormat = job.pageDialog(pageFormat);
    }

    public void print() {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(this, pageFormat);
//		job.setPageable(this);
		if (job.printDialog()) {
			try {
			    job.print();
			} catch (PrinterException ex) {
				ex.printStackTrace();
			}
		}
    }

	public int print(Graphics g, PageFormat format, int pageIndex)
		throws PrinterException
	{
		if (pageIndex < 0 || pageIndex >= getPageCount())
			return NO_SUCH_PAGE;
		PagePane pagePane = (PagePane) pages.elementAt(pageIndex);
		PageCanvas canvas = pagePane.getPageCanvas();
		canvas.print(g);
		return PAGE_EXISTS;
	}

	public void updatePages(Rectangle viewBounds) {
		int count = 1;
		currentPageIndex = 0;
		boolean active = false;
		Enumeration e = pages.elements();
		while (e.hasMoreElements()) {
			PagePane pagePane = (PagePane) e.nextElement();
			pagePane.setToolTipText("Page " + count++);
			active |= pagePane.updatePage(viewBounds);
			if (!active) {
				currentPageIndex++;
			}
		}
		int n = getPageCount();
		if (currentPageIndex >= n) {
			currentPageIndex = n-1;
		}
	}

	public void interrupt() {
		interpreter.interrupt(true);
	}

	public void setScale(float scale) {
		device.setScale(scale);
		Enumeration e = pages.elements();
		while (e.hasMoreElements()) {
		    PagePane pagePane = (PagePane) e.nextElement();
			pagePane.updatePageScale(scale);
		}
		currentPageScale = scale;
		validate();
	}

	public float getScale() {
		return device.getScale();
	}

	public void setOrientation(int orientation) {
		if (currentPageOrientation == orientation)
			return;
		exec("<</Orientation " + orientation + ">> setpagedevice");
		currentPageOrientation = orientation;
		Enumeration e = pages.elements();
		while (e.hasMoreElements()) {
		    PagePane pagePane = (PagePane) e.nextElement();
			float width = pagePane.getPageWidth();
			float height = pagePane.getPageHeight();
		    pagePane.updatePageSize(height, width, currentPageScale);
		}
		doLayout();
		fireViewResizeEvent();
	}

	public int getPageCount() {
		return pages.size();
	}

	public int getPageIndex() {
		return currentPageIndex;
	}

	private PagePane getCurrentPage() {
		return (PagePane) pages.elementAt(currentPageIndex);
	}

	public Point getPageOffset() {
		int i, x = 0, y = 0, vgap = layout.getGapV();
		for (i = 0; i < currentPageIndex; i++) {
			PagePane pane = (PagePane) pages.elementAt(i);
			Dimension d = pane.getSize();
			y += d.height + vgap;
		}
		return new Point(x, y);
	}

	public void showCurrentPage() {
		if (currentPageIndex > 0) {
			setPageIndex(currentPageIndex);
		}
	}

	public void showFirstPage() {
		if (currentPageIndex > 0) {
			setPageIndex(0);
		}
	}

	public void showLastPage() {
		int n = getPageCount() - 1;
		if (n >= 0) {
			setPageIndex(n);
		}
	}

	public void showNextPage() {
		int pageno = currentPageIndex + 1;
		if (pageno < getPageCount()) {
			setPageIndex(pageno);
		}
	}

	public void showPreviousPage() {
		if (currentPageIndex > 0) {
			setPageIndex(currentPageIndex-1);
		}
	}

	public void setPageIndex(int pageIndex) {
		int n = getPageCount();
		if (pageIndex < 0 || pageIndex >= n)
			throw new IllegalArgumentException("no such page: " + pageIndex);
		currentPageIndex = pageIndex;
		fireViewChangeEvent();
	}

	public void deleteCurrentPage() {
		int n = getPageCount();
		if (n > 0) {
			int pageno = currentPageIndex;
			if (0 < currentPageIndex && currentPageIndex < n) {
				currentPageIndex--;
			}
            removePage((PagePane) pages.elementAt(pageno));
		}
	}

	public void deleteAllPages() {
		if (getPageCount() > 0) {
		    removeAllPages();
		}
	}

	public String getVersion() {
		return interpreter.load("version").toString();
	}

}
