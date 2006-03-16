package au.gov.naa.digipres.xena.plugin.multipage;
import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import javax.swing.JPanel;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.gui.XmlDivertor;
import au.gov.naa.digipres.xena.helper.ChunkedView;
import au.gov.naa.digipres.xena.helper.XmlContentHandlerSplitter;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * Display a Xena multipage instance page by page with First, Prev, Next and
 * Last buttons.
 *
 * @author Chris Bitmead
 */
public class MultiPageView extends ChunkedView {

	BorderLayout borderLayout1 = new BorderLayout();

	/*	JToolBar jToolBar1 = new JToolBar();
	 JButton previousPageButton = new JButton();

	 JButton firstPageButton = new JButton();

	 JLabel pageLabel = new JLabel();

	 JTextField pageTextField = new JTextField();

	 JButton nextPageButton = new JButton();

	 JButton lastPageButton = new JButton();

	 java.util.List nodes;  */

	Component currentPage;

	//int currentPageIndex = 0;

//	int numPages;

//	JTextField totalPagesTextField = new JTextField();

//	JLabel ofLabel = new JLabel();

	private JPanel displayPanel = new JPanel();

	private BorderLayout borderLayout2 = new BorderLayout();

	public MultiPageView() {
		// For some reason the original linked list dosen't serialize.
		try {
			jbInit2();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getTmpFileContentHandler());
		final XenaView oldview = getSubView(displayPanel);
		ContentHandler ch = new XmlDivertor(this, displayPanel) {

			StringBuffer buf = null;

			int p = 0;

			/*			public void divertXml(String name) throws SAXException {
			 super.divertXml(name);
			   } */

			public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
				if (qName.equals("multipage:page")) {
					if (p == currentChunk) {
						this.setDivertNextTag();
					}
					p++;
				}
				super.startElement(namespaceURI, localName, qName, atts);
			}

			public void endDocument() {
				setTotalChunks(p);
				XenaView newview = getSubView(displayPanel);
				if (oldview != null) {
					copyAttributes(oldview, newview);
				}
				try {
					initSubViews();
				} catch (XenaException x) {
					MainFrame.singleton().showError(x);
				}
			}
		};
		splitter.addContentHandler(ch);
		return splitter;
	}

	/*	public void updateViewFromElement() throws XenaException {
	  Namespace ns = Namespace.getNamespace(MultiPageNormaliser.PREFIX, MultiPageNormaliser.URI);
	  Iterator it = getElement().getChildren("page", ns).iterator();
	  this.nodes = new java.util.ArrayList();
	  while (it.hasNext()) {
	   Element page = (Element)it.next();
	   nodes.add(page.getChildren().iterator().next());
	  }
	  numPages = nodes.size();
	  totalPagesTextField.setText(Integer.toString(numPages) + " ");
	  if (0 < numPages) {
	   changeToPage(0);
	  }
	 } */

	public String getViewName() {
		return "Multi-Page View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaMultiPageFileType.class).getTag());
	}

	public void initListeners() {
		/*		previousPageButton.addActionListener(
		   new java.awt.event.ActionListener() {
		   public void actionPerformed(ActionEvent e) {
		 previousPageButton_actionPerformed(e);
		   }
		  });
		  firstPageButton.addActionListener(
		   new java.awt.event.ActionListener() {
		   public void actionPerformed(ActionEvent e) {
		 firstPageButton_actionPerformed(e);
		   }
		  });
		  nextPageButton.addActionListener(
		   new java.awt.event.ActionListener() {
		   public void actionPerformed(ActionEvent e) {
		 nextPageButton_actionPerformed(e);
		   }
		  });
		  lastPageButton.addActionListener(
		   new java.awt.event.ActionListener() {
		   public void actionPerformed(ActionEvent e) {
		 lastPageButton_actionPerformed(e);
		   }
		  });
		  pageTextField.addActionListener(
		   new java.awt.event.ActionListener() {
		   public void actionPerformed(ActionEvent e) {
		 pageTextField_actionPerformed(e);
		   }
		  }); */
	}

	void jbInit2() throws Exception {
		this.setLayout(borderLayout1);
		pageLabel.setText(" Page: ");
		/*		totalPagesTextField.setEditable(false);
		  previousPageButton.setText(" Prev ");
		  firstPageButton.setText(" First ");
		  pageLabel.setText(" Page: ");
		  pageTextField.setColumns(4);
		  nextPageButton.setText(" Next ");
		  lastPageButton.setText(" Last ");
		  ofLabel.setText(" of "); */
		displayPanel.setLayout(borderLayout2);
		/*		this.add(jToolBar1, BorderLayout.NORTH);
		  jToolBar1.add(firstPageButton, null);
		  jToolBar1.add(previousPageButton, null);
		  jToolBar1.add(pageLabel, null);
		  jToolBar1.add(pageTextField, null);
		  jToolBar1.add(ofLabel, null);
		  jToolBar1.add(totalPagesTextField, null);
		  jToolBar1.add(nextPageButton, null);
		  jToolBar1.add(lastPageButton, null); */
		this.add(displayPanel, BorderLayout.CENTER);
	}

	/**
	 * We can for example retain the same zoom factor between images using this
	 * function. Functions that start with "getXenaExternal" are treated
	 * specially and we copy these special attributes from the old to the new
	 * view.
	 * @param oldv
	 * @param newv
	 */
	void copyAttributes(XenaView oldv, XenaView newv) {
		if (oldv != null) {
			Method[] methods = oldv.getClass().getMethods();
			for (int i = 0; i < methods.length; i++) {
				String name = methods[i].getName();
//				System.out.println(name);
				if (name.startsWith("getXenaExternal")) {
					String rest = name.substring("getXenaExternal".length());
					String setterName = "setXenaExternal" + rest;
					try {
						Method setter = newv.getClass().getMethod(setterName, new Class[] {methods[i].getReturnType()});
						try {
							Object res = methods[i].invoke(oldv, new Class[] {});
							setter.invoke(newv, new Object[] {res});
						} catch (InvocationTargetException x) {
							MainFrame.singleton().showError(x);
						} catch (IllegalArgumentException x) {
							MainFrame.singleton().showError(x);
						} catch (IllegalAccessException x) {
							MainFrame.singleton().showError(x);
						}
					} catch (SecurityException x) {
						MainFrame.singleton().showError(x);
					} catch (NoSuchMethodException x) {
						// Nothing - forget it. Incompatable views.
					}
				}
			}
			Iterator it = newv.getSubViews().iterator();
			while (it.hasNext()) {
				XenaView view = (XenaView)it.next();
				copyAttributes(oldv, view);
			}
			it = oldv.getSubViews().iterator();
			while (it.hasNext()) {
				XenaView view = (XenaView)it.next();
				copyAttributes(view, newv);
			}
		}
	}

	public boolean displayChunkPanel() {
		return true;
	}

	/*	boolean changeToPage(int n) {
	 try {
	  if (n < 0) {
	   return false;
	  }
	  Element nthNode = (Element)nodes.get(n);
	  if (nthNode == null) {
	   return false;
	  } else {
	   String xmlTag = nthNode.getQualifiedName();
	   XenaView view = ViewManager.singleton().getDefaultView(xmlTag, XenaView.REGULAR_VIEW, getLevel() + 1);
	   XenaView oldview = getSubView(displayPanel);

	   try {
	 JdomUtil.writeDocument(view.getContentHandler(), nthNode);
	 view.parse();
	   } catch (JDOMException x) {
	 throw new XenaException(x);
	   } catch (SAXException x) {
	 throw new XenaException(x);
	   } catch (IOException x) {
	 throw new XenaException(x);
	   }

//				view.setElement(nthNode);
	   setSubView(displayPanel, view);
	   initSubViews();
	   copyAttributes(oldview, view);
	   pageTextField.setText(Integer.toString(n + 1));
	  }
	  this.invalidate();
	  this.validate();
	  currentPageIndex = n;
	  firstPageButton.setEnabled(currentPageIndex != 0);
	  lastPageButton.setEnabled(currentPageIndex != numPages - 1);
	  previousPageButton.setEnabled(currentPageIndex != 0);
	  nextPageButton.setEnabled(currentPageIndex != numPages - 1);
	 } catch (XenaException e) {
	  e.printStackTrace();
	 }
	 return true;
	  }

	  void nextPageButton_actionPerformed(ActionEvent e) {
	 changeToPage(currentPageIndex + 1);
	  }

	  void previousPageButton_actionPerformed(ActionEvent e) {
	 changeToPage(currentPageIndex - 1);
	  }

	  void firstPageButton_actionPerformed(ActionEvent e) {
	 changeToPage(0);
	  }

	  void lastPageButton_actionPerformed(ActionEvent e) {
	 changeToPage(numPages - 1);
	  }

	  void pageTextField_actionPerformed(ActionEvent e) {
	 try {
	  int num = Integer.parseInt(pageTextField.getText()) - 1;
	  if (num < 0 || numPages <= num) {
	   MainFrame.singleton().showError("Number out of Range: " + pageTextField.getText());
	   pageTextField.setText(Integer.toString(currentPageIndex + 1));
	  } else {
	   changeToPage(num);
	  }
	 } catch (NumberFormatException x) {
	  MainFrame.singleton().showError("Not a valid Number: " + pageTextField.getText());
	  pageTextField.setText(Integer.toString(currentPageIndex + 1));
	 }
	  } */
}
