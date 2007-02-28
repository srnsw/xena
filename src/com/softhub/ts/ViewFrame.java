
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

import com.softhub.ps.util.Console;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class ViewFrame extends JFrame {

    private JPanel contentPane;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu();
    private JMenuItem exitMenuItem = new JMenuItem();
    private JMenu helpMenu = new JMenu();
    private JMenuItem aboutMenuItem = new JMenuItem();
    private JToolBar toolBar = new JToolBar();
    private JButton fileOpenButton = new JButton();
    private JButton fileSaveButton = new JButton();
    private JButton printButton = new JButton();
    private JButton deleteButton = new JButton();
    private JButton prevButton = new JButton();
    private JButton nextButton = new JButton();
    private JButton showPageButton = new JButton();
    private JButton helpButton = new JButton();
    private ImageIcon openImage;
    private ImageIcon saveImage;
    private ImageIcon printImage;
    private ImageIcon deleteImage;
    private ImageIcon prevImage;
    private ImageIcon nextImage;
    private ImageIcon showPageImage;
    private ImageIcon helpImage;
    private JLabel statusBar = new JLabel();
	private PageTray pageTray = new PageTray();
	private URLDialog urlDialog;
	private GotoDialog gotoDialog;
    private BorderLayout contentLayout = new BorderLayout();
	private File propertiesFile;
	private Profile profile;
    private JMenuItem openMenuItem = new JMenuItem();
    private JMenuItem openURLMenuItem = new JMenuItem();
    private JMenu viewMenu = new JMenu();
    private JMenuItem gotoPageMenuItem = new JMenuItem();
    private JMenuItem firstPageMenuItem = new JMenuItem();
    private JMenuItem lastPageMenuItem = new JMenuItem();
    private JMenuItem previousPageMenuItem = new JMenuItem();
    private JMenuItem nextPageMenuItem = new JMenuItem();
    private JMenu controlMenu = new JMenu();
    private JMenuItem consoleMenuItem = new JMenuItem();
    private JMenuItem interruptMenuItem = new JMenuItem();
    private JMenuItem showPageMenuItem = new JMenuItem();
    private JMenuItem deleteAllPagesMenuItem = new JMenuItem();
    private JMenuItem deletePageMenuItem = new JMenuItem();
	private JFileChooser fileChooser = new JFileChooser();
	private JMenuItem saveAsMenuItem = new JMenuItem();
    private JMenuItem printMenuItem = new JMenuItem();
    private JMenu formatMenu = new JMenu();
    private JMenuItem formatLetterItem = new JMenuItem();
    private JMenuItem formatLegalItem = new JMenuItem();
    private JMenuItem formatA3Item = new JMenuItem();
    private JMenuItem formatA4Item = new JMenuItem();
    private JMenuItem formatA5Item = new JMenuItem();
    private JMenuItem formatB3Item = new JMenuItem();
    private JMenuItem formatB4Item = new JMenuItem();
    private JMenuItem formatB5Item = new JMenuItem();
    private JMenuItem printSetupItem = new JMenuItem();
    private JMenu orientationMenu = new JMenu();
    private JMenuItem normalOrientationItem = new JMenuItem();
    private JMenuItem landscapeOrientationItem = new JMenuItem();
	private Console console;

    public ViewFrame() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception  {
		try {
		    restoreProfile();
		} catch (Exception ex) {}
		openImage = new ImageIcon(ViewFrame.class.getResource("open.gif"));
		saveImage = new ImageIcon(ViewFrame.class.getResource("save.gif"));
		printImage = new ImageIcon(ViewFrame.class.getResource("print.gif"));
		deleteImage = new ImageIcon(ViewFrame.class.getResource("delete.gif"));
		prevImage = new ImageIcon(ViewFrame.class.getResource("leftarrow.gif"));
		nextImage = new ImageIcon(ViewFrame.class.getResource("rightarrow.gif"));
		showPageImage = new ImageIcon(ViewFrame.class.getResource("showpage.gif"));
		helpImage = new ImageIcon(ViewFrame.class.getResource("help.gif"));
        //setIconImage(Toolkit.getDefaultToolkit().createImage(ViewFrame.class.getResource("[Your Icon]")));
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(contentLayout);
        this.setTitle("ToastScript");
        statusBar.setText(" ");
        fileMenu.setText("File");
        exitMenuItem.setText("Exit");
        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(81, java.awt.event.KeyEvent.CTRL_MASK, false));
        exitMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                exitMenuItemAction(e);
            }
        });
        helpMenu.setText("Help");
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                aboutMenuItemAction(e);
            }
        });
        fileOpenButton.setIcon(openImage);
        fileOpenButton.setMargin(new Insets(2, 2, 2, 2));
        fileOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openAction(e);
            }
        });
        fileOpenButton.setMaximumSize(new Dimension(28, 28));
        fileOpenButton.setMinimumSize(new Dimension(28, 28));
        fileOpenButton.setPreferredSize(new Dimension(28, 28));
        fileOpenButton.setToolTipText("Open File");
        fileOpenButton.setBorderPainted(false);
        fileOpenButton.setFocusPainted(false);
        fileSaveButton.setIcon(saveImage);
        fileSaveButton.setMargin(new Insets(2, 2, 2, 2));
        fileSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAsAction(e);
            }
        });
        fileSaveButton.setMaximumSize(new Dimension(28, 28));
        fileSaveButton.setMinimumSize(new Dimension(28, 28));
        fileSaveButton.setPreferredSize(new Dimension(28, 28));
        fileSaveButton.setToolTipText("Close File");
        fileSaveButton.setBorderPainted(false);
        fileSaveButton.setFocusPainted(false);
        prevButton.setIcon(prevImage);
        prevButton.setMargin(new Insets(2, 2, 2, 2));
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                previousPageMenuItemAction(e);
            }
        });
        prevButton.setMaximumSize(new Dimension(28, 28));
        prevButton.setMinimumSize(new Dimension(28, 28));
        prevButton.setPreferredSize(new Dimension(28, 28));
        prevButton.setToolTipText("Previous");
        prevButton.setBorderPainted(false);
        prevButton.setFocusPainted(false);
        nextButton.setIcon(nextImage);
        nextButton.setMargin(new Insets(2, 2, 2, 2));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextPageMenuItemAction(e);
            }
        });
        nextButton.setMaximumSize(new Dimension(28, 28));
        nextButton.setMinimumSize(new Dimension(28, 28));
        nextButton.setPreferredSize(new Dimension(28, 28));
        nextButton.setToolTipText("Next");
        nextButton.setBorderPainted(false);
        nextButton.setFocusPainted(false);
        helpButton.setIcon(helpImage);
        helpButton.setMargin(new Insets(2, 2, 2, 2));
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                aboutMenuItemAction(e);
            }
        });
		helpButton.setMaximumSize(new Dimension(28, 28));
		helpButton.setMinimumSize(new Dimension(28, 28));
		helpButton.setPreferredSize(new Dimension(28, 28));
		helpButton.setToolTipText("Help");
		helpButton.setBorderPainted(false);
		helpButton.setFocusPainted(false);
		toolBar.setBorder(BorderFactory.createEtchedBorder());
		openMenuItem.setText("Open...");
		openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(48, java.awt.event.KeyEvent.CTRL_MASK, false));
		openMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction(e);
			}
		});
        openURLMenuItem.setText("Open URL...");
        openURLMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(85, java.awt.event.KeyEvent.CTRL_MASK, false));
        openURLMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openURLMenuAction(e);
            }
        });
        viewMenu.setText("View");
        gotoPageMenuItem.setText("Goto Page...");
        gotoPageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gotoPageAction(e);
            }
        });
        firstPageMenuItem.setText("First Page");
        firstPageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firstPageMenuItemAction(e);
            }
        });
        lastPageMenuItem.setText("Last Page");
        lastPageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lastPageMenuItemAction(e);
            }
        });
        previousPageMenuItem.setText("Previous Page");
        previousPageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                previousPageMenuItemAction(e);
            }
        });
        nextPageMenuItem.setText("Next Page");
        nextPageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextPageMenuItemAction(e);
            }
        });
        controlMenu.setText("Control");
        consoleMenuItem.setText("Show Console");
        consoleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showConsoleAction(e);
            }
        });
        interruptMenuItem.setText("Interrupt");
        interruptMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(73, java.awt.event.KeyEvent.CTRL_MASK, false));
        interruptMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                interruptMenuAction(e);
            }
        });
        showPageMenuItem.setText("Show Page");
        showPageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPageAction(e);
            }
        });
        deleteAllPagesMenuItem.setText("Delete All Pages");
        deleteAllPagesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteAllPagesMenuItemAction(e);
            }
        });
        deletePageMenuItem.setText("Delete Page");
        deletePageMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(68, java.awt.event.KeyEvent.CTRL_MASK, false));
        deletePageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deletePageMenuItemAction(e);
            }
        });
        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(83, java.awt.event.KeyEvent.CTRL_MASK, false));
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAsAction(e);
            }
        });
        pageTray.setMinimumSize(new Dimension(50, 50));
        pageTray.setPreferredSize(new Dimension(600, 800));
        printMenuItem.setText("Print...");
        printMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(80, java.awt.event.KeyEvent.CTRL_MASK, false));
        printMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printMenuAction(e);
            }
        });
        formatMenu.setText("Format");
        formatLetterItem.setText("Letter");
        formatLetterItem.addActionListener(new FormatListener("letter"));
        formatLegalItem.setText("Legal");
        formatLegalItem.addActionListener(new FormatListener("legal"));
        formatA3Item.setText("A3");
        formatA3Item.addActionListener(new FormatListener("a3"));
        formatA4Item.setText("A4");
        formatA4Item.addActionListener(new FormatListener("a4"));
        formatA5Item.setText("A5");
        formatA5Item.addActionListener(new FormatListener("a5"));
        formatB3Item.setText("B3");
        formatB3Item.addActionListener(new FormatListener("b3"));
        formatB4Item.setText("B4");
        formatB4Item.addActionListener(new FormatListener("b4"));
        formatB5Item.setText("B5");
        formatB5Item.addActionListener(new FormatListener("b5"));
        printSetupItem.setText("Page Setup...");
        printSetupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printSetupAction(e);
            }
        });
        printButton.setMaximumSize(new Dimension(28, 28));
        printButton.setMinimumSize(new Dimension(28, 28));
        printButton.setPreferredSize(new Dimension(28, 28));
        printButton.setToolTipText("Print");
        printButton.setBorderPainted(false);
        printButton.setFocusPainted(false);
        printButton.setIcon(printImage);
        printButton.setMargin(new Insets(2, 2, 2, 2));
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printMenuAction(e);
            }
        });
		deleteButton.setMaximumSize(new Dimension(28, 28));
		deleteButton.setMinimumSize(new Dimension(28, 28));
		deleteButton.setPreferredSize(new Dimension(28, 28));
		deleteButton.setToolTipText("Delete All Pages");
		deleteButton.setBorderPainted(false);
		deleteButton.setFocusPainted(false);
		deleteButton.setIcon(deleteImage);
		deleteButton.setMargin(new Insets(2, 2, 2, 2));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteAllPagesMenuItemAction(e);
			}
		});
		showPageButton.setMaximumSize(new Dimension(28, 28));
		showPageButton.setMinimumSize(new Dimension(28, 28));
		showPageButton.setPreferredSize(new Dimension(28, 28));
		showPageButton.setToolTipText("Show Page");
		showPageButton.setBorderPainted(false);
		showPageButton.setFocusPainted(false);
		showPageButton.setIcon(showPageImage);
		showPageButton.setMargin(new Insets(2, 2, 2, 2));
		showPageButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPageAction(e);
			}
		});
        orientationMenu.setText("Orientation");
        normalOrientationItem.setText("normal");
        normalOrientationItem.addActionListener(new OrientationListener("normal"));
        landscapeOrientationItem.setText("landscape");
        landscapeOrientationItem.addActionListener(new OrientationListener("landscape"));
        toolBar.add(fileOpenButton);
		toolBar.add(fileSaveButton);
		toolBar.add(printButton);
		toolBar.add(showPageButton);
		toolBar.add(deleteButton);
		toolBar.add(prevButton);
		toolBar.add(nextButton);
		toolBar.add(helpButton);
		fileMenu.add(openMenuItem);
		fileMenu.add(openURLMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(saveAsMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(printSetupItem);
		fileMenu.add(printMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		helpMenu.add(aboutMenuItem);
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(controlMenu);
		menuBar.add(helpMenu);
		this.setJMenuBar(menuBar);
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		contentPane.add(pageTray, BorderLayout.CENTER);
        viewMenu.add(formatMenu);
        viewMenu.add(orientationMenu);
		viewMenu.addSeparator();
		viewMenu.add(gotoPageMenuItem);
		viewMenu.add(firstPageMenuItem);
		viewMenu.add(lastPageMenuItem);
		viewMenu.addSeparator();
		viewMenu.add(previousPageMenuItem);
		viewMenu.add(nextPageMenuItem);
		controlMenu.add(consoleMenuItem);
		controlMenu.addSeparator();
		controlMenu.add(interruptMenuItem);
		controlMenu.add(showPageMenuItem);
		controlMenu.add(deletePageMenuItem);
		controlMenu.add(deleteAllPagesMenuItem);
		formatMenu.add(formatLetterItem);
		formatMenu.add(formatLegalItem);
		formatMenu.addSeparator();
		formatMenu.add(formatA3Item);
		formatMenu.add(formatA4Item);
		formatMenu.add(formatA5Item);
		formatMenu.addSeparator();
		formatMenu.add(formatB3Item);
		formatMenu.add(formatB4Item);
		formatMenu.add(formatB5Item);
        orientationMenu.add(normalOrientationItem);
        orientationMenu.add(landscapeOrientationItem);
		init();
    }

	public void init() {
		pageTray.restoreProfile(profile);
		pageTray.init();
		console = getPostScriptPane().getConsole();
		console.addActionListener(new ConsoleListener());
		console.pack();
    }

    private void prepareChooser() {
		String fileName = profile.getString("file", null);
		if (fileName != null) {
			try {
				File file = new File(fileName);
				fileChooser.setCurrentDirectory(file);
			} catch (Exception ex) {}
		}
    }

    void openAction(ActionEvent evt) {
		prepareChooser();
		fileChooser.setDialogTitle("Open PostScript File");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			profile.setString("file", file.getPath());
			getPostScriptPane().run(file);
		}
    }
  
    
   public void openAction(ActionEvent evt, String path) {
    	File file = new File(path);
    	profile.setString("file", path);
    	getPostScriptPane().run(file);
		
    }
   

    void saveAsAction(ActionEvent evt) {
		prepareChooser();
		fileChooser.setDialogTitle("Save As JPEG");
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			boolean writeImage = true;
			File file = fileChooser.getSelectedFile();
			if (file.exists()) {
				writeImage = alert("Alert", "Overwrite");
			}
			profile.setString("file", file.getPath());
			if (writeImage) {
				FileOutputStream stream = null;
				try {
					stream = new FileOutputStream(file);
					getPostScriptPane().save(stream, "jpeg");
				} catch (IOException ex) {
					System.err.println("export failed: " + ex);
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException ex) {}
					}
				}
			}
		}
    }

    void gotoPageAction(ActionEvent evt) {
		if (gotoDialog == null) {
		    gotoDialog = new GotoDialog(this, "Goto Page", false);
		    PostScriptPane pane = getPostScriptPane();
			pane.addViewEventListener(gotoDialog);
		    gotoDialog.addNavigationListener(pane);
			gotoDialog.setPageNumber(pane.getPageIndex() + 1);
		}
		postDialog(gotoDialog);
    }

	void openURLMenuAction(ActionEvent evt) {
		if (urlDialog == null) {
		    urlDialog = new URLDialog(this, "URL", false);
			urlDialog.addActionListener(getPostScriptPane());
		}
		postDialog(urlDialog);
	}

	void firstPageMenuItemAction(ActionEvent evt) {
		getPostScriptPane().showFirstPage();
	}

	void lastPageMenuItemAction(ActionEvent evt) {
		getPostScriptPane().showLastPage();
	}

    void previousPageMenuItemAction(ActionEvent evt) {
		getPostScriptPane().showPreviousPage();
    }

    void nextPageMenuItemAction(ActionEvent evt) {
		getPostScriptPane().showNextPage();
    }

    void deletePageMenuItemAction(ActionEvent evt) {
		getPostScriptPane().deleteCurrentPage();
    }

    void deleteAllPagesMenuItemAction(ActionEvent evt) {
		getPostScriptPane().deleteAllPages();
    }

    void interruptMenuAction(ActionEvent evt) {
		getPostScriptPane().interrupt();
    }

    void showPageAction(ActionEvent evt) {
		getPostScriptPane().exec("showpage");
    }

    void exitMenuItemAction(ActionEvent evt) {
		saveProfile();
        System.exit(0);
    }

    void printSetupAction(ActionEvent evt) {
		getPostScriptPane().printSetup();
   }

    void printMenuAction(ActionEvent evt) {
		getPostScriptPane().print();
    }

    void showConsoleAction(ActionEvent evt) {
		PostScriptPane pane = pageTray.getPostScriptPane();
		String cmd = evt.getActionCommand();
		if ("Hide Console".equalsIgnoreCase(cmd)) {
			console.setVisible(false);
		} else {
			console.setVisible(true);
		}
    }

    void consoleAction(ActionEvent evt) {
		PostScriptPane pane = pageTray.getPostScriptPane();
		String cmd = evt.getActionCommand();
		if (Console.HIDE.equalsIgnoreCase(cmd)) {
		    consoleMenuItem.setText("Show Console");
		} else {
		    consoleMenuItem.setText("Hide Console");
		}
    }

    void aboutMenuItemAction(ActionEvent evt) {
		postDialog(new AboutBox(this, getPostScriptPane()));
    }

	protected PostScriptPane getPostScriptPane() {
		return pageTray.getPostScriptPane();
	}

    protected void processWindowEvent(WindowEvent evt) {
        super.processWindowEvent(evt);
        if (evt.getID() == WindowEvent.WINDOW_CLOSING) {
            exitMenuItemAction(null);
        }
    }

	protected void restoreProfile() {
        pageTray.setMinimumSize(new Dimension(80, 80));
        pageTray.setPreferredSize(new Dimension(120, 120));
		try {
		    propertiesFile = new File("toastscript.properties");
		    profile = new PropertyProfile(propertiesFile);
		} catch (Exception ex) {
		    profile = new PropertyProfile();
		}
		int width = profile.getInteger("frame.width", 400);
		int height = profile.getInteger("frame.height", 400);
        this.setSize(new Dimension(width, height));
		int x = profile.getInteger("frame.x", -1);
		int y = profile.getInteger("frame.y", -1);
		if (x < 0 || y < 0) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (height > screenSize.height) {
				height = screenSize.height;
			}
			if (width > screenSize.width) {
				width = screenSize.width;
			}
			x = (screenSize.width - width) / 2;
			y = (screenSize.height - height) / 2;
		}
        this.setLocation(x, y);
	}

	protected void saveProfile() {
		Dimension d = getSize();
		profile.setInteger("frame.width", d.width);
		profile.setInteger("frame.height", d.height);
		Point pt = getLocation();
		profile.setInteger("frame.x", pt.x);
		profile.setInteger("frame.y", pt.y);
		pageTray.saveProfile(profile);
		try {
			profile.save(propertiesFile, "ToastScript Properties");
		} catch (Exception ex) {}
	}

    protected void postDialog(JDialog dialog) {
        Dimension dlgSize = dialog.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        int x = (frmSize.width - dlgSize.width) / 2 + loc.x;
        int y = (frmSize.height - dlgSize.height) / 2 + loc.y;
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }

	boolean modalResult;

	protected boolean alert(String title, String approveText) {
		modalResult = false;
		final JDialog alert = new JDialog(this, title, true);
		JButton ok = new JButton(approveText);
		JButton cancel = new JButton("Cancel");
		Container contentPane = alert.getContentPane();
		contentPane.setLayout(new FlowLayout());
		contentPane.add(ok);
		contentPane.add(cancel);
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				alert.setVisible(false);
				modalResult = true;
			}
		});
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				alert.setVisible(false);
				modalResult = false;
			}
		});
		alert.pack();
		postDialog(alert);
		return modalResult;
	}

	class FormatListener implements ActionListener {

		private String format;

		FormatListener(String format) {
			this.format = format;
		}

		public void actionPerformed(ActionEvent evt) {
		    getPostScriptPane().exec(format);
		}

	}

	class OrientationListener implements ActionListener {

		private String orientation;

		OrientationListener(String orientation) {
			this.orientation = orientation;
		}

		public void actionPerformed(ActionEvent evt) {
			int orient = 0;
			if (!orientation.equals("normal")) {
				orient = 1;
			}
		    getPostScriptPane().setOrientation(orient);
		}

	}

	class ConsoleListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			consoleAction(evt);
		}

	}

}
