package org.jpedal.examples.simpleviewer.javabean;

import java.io.File;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.examples.simpleviewer.Values;

public class SimpleViewerBean extends JPanel {
	private SimpleViewer viewer;
	
	private File document = null;
	private Integer pageNumber = null;
	private Integer rotation = null;
	private Integer zoom = null;

	private Boolean isMenuBarVisible = null;
	private Boolean isToolBarVisible = null;
	private Boolean isDisplayOptionsBarVisible = null;
	private Boolean isSideTabBarVisible = null;
	private Boolean isNavigationBarVisible = null;
	
	public SimpleViewerBean() {
        viewer = new SimpleViewer(this, SimpleViewer.PREFERENCES_BEAN);
        viewer.setupViewer();
	}
	
    public SimpleViewer getViewer() {
    	return viewer;
    }
    
    // Document ////////
	public void setDocument(final File document) {
		this.document = document;
		
		excuteCommand(Commands.OPENFILE, new String[] { 
				String.valueOf(document) });
		
		if(pageNumber != null) {
			excuteCommand(Commands.GOTO, new String[] { 
				String.valueOf(pageNumber) });
		}
		
		if(rotation != null) {
			excuteCommand(Commands.ROTATION, new String[] { 
				String.valueOf(rotation) });
		}
		
		if(zoom != null) {
			excuteCommand(Commands.SCALING, new String[] { 
				String.valueOf(zoom) });
		} else {
			excuteCommand(Commands.SCALING, new String[] { 
					String.valueOf(100) });
		}
		
		if(isMenuBarVisible != null) {
			setMenuBar(isMenuBarVisible.booleanValue());
		}
		
		if(isToolBarVisible != null) {
			setToolBar(isToolBarVisible.booleanValue());
		}
		
		if(isDisplayOptionsBarVisible != null) {
			setDisplayOptionsBar(isDisplayOptionsBarVisible.booleanValue());
		}
		
		if(isSideTabBarVisible != null) {
			setSideTabBar(isSideTabBarVisible.booleanValue());
		}
		
		if(isNavigationBarVisible != null) {
			setNavigationBar(isNavigationBarVisible.booleanValue());
		}
	}
	
	// Page Number ////////
	public int getPageNumber() {
		if(pageNumber == null)
			return 1;
		else
			return pageNumber.intValue();
	}
	
	public void setPageNumber(final int pageNumber) {
		this.pageNumber = new Integer(pageNumber);
		
		if(document != null) {
			excuteCommand(Commands.GOTO, new String[] { 
				String.valueOf(pageNumber) });
		}
	}

	// Rotation ////////
	public int getRotation() {
		if(rotation == null)
			return 0;
		else
			return rotation.intValue();
	}

	public void setRotation(final int rotation) {
		this.rotation = new Integer(rotation);

		if(document != null) {
			excuteCommand(Commands.ROTATION, new String[] { 
				String.valueOf(rotation) });
		}
	}
	
	// Zoom ////////
	public int getZoom() {
		if(zoom == null)
			return 100;
		else
			return zoom.intValue();
	}

	public void setZoom(int zoom) {
		this.zoom = new Integer(zoom);
		
		if(document != null) {
			excuteCommand(Commands.SCALING, new String[] { 
				String.valueOf(zoom) });
		}
	}

	//setToolBar, setDisplayOptionsBar, setSideTabBar, setNavigationBar, 
	public void setMenuBar(boolean visible) {
		this.isMenuBarVisible = new Boolean(visible);
		
		//if(document != null)
			viewer.executeCommand(Commands.UPDATEGUILAYOUT, new Object[] {new String("ShowMenubar"), Boolean.valueOf(visible)});
	}
	
	public boolean getMenuBar() {
		if(isMenuBarVisible == null)
			return true;
		else
			return isMenuBarVisible.booleanValue();
	}
	
	public void setToolBar(boolean visible) {
		this.isToolBarVisible = new Boolean(visible);
		
		//@kieran
        //I did not write this class so not familiar with it
        //Did you write or or Simon?
        //is a null document goint to cause any issues in MAtisse?
		//if(document != null)
			viewer.executeCommand(Commands.UPDATEGUILAYOUT, new Object[] {new String("ShowButtons"), Boolean.valueOf(visible)});
	}
	
	public boolean getToolBar() {
		if(isToolBarVisible == null)
			return true;
		else
			return isToolBarVisible.booleanValue();
	}
	
	public void setDisplayOptionsBar(boolean visible) {
		this.isDisplayOptionsBarVisible = new Boolean(visible);
		
		//if(document != null)
			viewer.executeCommand(Commands.UPDATEGUILAYOUT, new Object[] {new String("ShowDisplayoptions"), Boolean.valueOf(visible)});
	}
	
	public boolean getDisplayOptionsBar() {
		if(isDisplayOptionsBarVisible == null)
			return true;
		else
			return isDisplayOptionsBarVisible.booleanValue();
	}
	
	public void setSideTabBar(boolean visible) {
		this.isSideTabBarVisible = new Boolean(visible);
		
		//if(document != null)
			viewer.executeCommand(Commands.UPDATEGUILAYOUT, new Object[] {new String("ShowSidetabbar"), Boolean.valueOf(visible)});
	}
	
	public boolean getSideTabBar() {
		if(isSideTabBarVisible == null)
			return true;
		else
			return isSideTabBarVisible.booleanValue();
	}
	
	public void setNavigationBar(boolean visible) {
		this.isNavigationBarVisible = new Boolean(visible);
		
		//if(document != null)
			viewer.executeCommand(Commands.UPDATEGUILAYOUT, new Object[] {new String("ShowNavigationbar"), Boolean.valueOf(visible)});
	}
	
	public boolean getNavigationBar() {
		if(isNavigationBarVisible == null)
			return true;
		else
			return isNavigationBarVisible.booleanValue();
	}
	
	private void excuteCommand(final int command, final Object[] input) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				viewer.executeCommand(command, input);
				
				while(Values.isProcessing()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				repaint();
			}
		});
	}
	
//	// Page Layout ////////
//	private String pageLayout = "Single";
//	
//	public String getPageLayout() {
//		return pageLayout;
//	}
//
//	public void setPageLayout(String pageLayout) {
//		this.pageLayout = pageLayout;
//	}
}