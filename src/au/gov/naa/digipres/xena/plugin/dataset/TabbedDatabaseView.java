package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.util.JdomUtil;
import au.gov.naa.digipres.xena.util.JdomXenaView;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * View for Xena database that displays each table as a tab in the view.
 *
 * @author Chris Bitmead
 */
public class TabbedDatabaseView extends JdomXenaView {
	JPopupMenu popup;

//	MyMenu popupItems = new MyMenu(this);
//
//	MyMenu customItems = new MyMenu(this);
//
//	MyMenu menus[];

	java.util.Map windowMap = new HashMap();

	java.util.List windows = new ArrayList();

	java.util.Map elementMap = new HashMap();

	JTabbedPane tabbedPane = new JTabbedPane();

	BorderLayout borderLayout1 = new BorderLayout();

	public TabbedDatabaseView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateViewFromElement() throws XenaException {
		Namespace ns = Namespace.getNamespace(CsvToXenaDatasetNormaliser.PREFIX, CsvToXenaDatasetNormaliser.URI);
		java.util.List datasets = getElement().getChildren("dataset", ns);
		Iterator it = datasets.iterator();
		String sname = null;
		int c = 1;
		while (it.hasNext()) {
			Element dataset = (Element)it.next();
			Element definitions = dataset.getChild("definitions", ns);
			Element name = null;
			if (definitions != null) {
				definitions.getChild("name", ns);
			}
			if (name == null) {
				sname = Integer.toString(c) + OrdinalPostfix.postfix(c);
			} else {
				sname = name.getText();
			}
			XenaView subView = viewManager.getDefaultView(dataset.getQualifiedName(), XenaView.REGULAR_VIEW, getLevel() + 1);
			try {
				JdomUtil.writeDocument(subView.getContentHandler(), dataset);
				subView.parse();
			} catch (JDOMException x) {
				throw new XenaException(x);
			} catch (SAXException x) {
				throw new XenaException(x);
			} catch (IOException x) {
				throw new XenaException(x);
			}
			addTab(sname, subView);
			if (windowMap.get(sname) != null) {
				throw new XenaException("Column names are not Unique");
			}
			windowMap.put(sname, subView);
			elementMap.put(sname, dataset);
			c++;
		}
	}

	public String getViewName() {
		return "Tabbed View";
	}

	public void initListeners() throws XenaException {
		addPopupListener(popup, tabbedPane);
//		XenaMenu.initListenersAll(menus);
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(PluginManager.singleton().getTypeManager().lookupXenaFileType(XenaDatabaseFileType.class).getTag());
	}

//	public void makeMenu(JMenu menu) {
//		customItems.makeMenu(menu);
//	}

	void addTab(String name, XenaView comp) throws XenaException {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		tabbedPane.addTab(name, panel);
		setSubView(panel, comp);
		windows.add(name);
	}

	void removeAllTabs() {
		tabbedPane.removeAll();
		windows = new ArrayList();
		clearSubViews();
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
//		menus = new MyMenu[] {
//			popupItems, customItems};
//		popup = new JPopupMenu();
//		popupItems.makeMenu(popup);
		this.add(tabbedPane, BorderLayout.CENTER);
	}

//	class MyMenu extends XenaMenu {
//		JMenuItem reorderButton;
//
//		TabbedDatabaseView view;
//
//		MyMenu(TabbedDatabaseView view) {
//			this.view = view;
//			reorderButton = new JMenuItem("Reorder Tabs");
//		}
//
//		public void sync() {
//		}
//
//		public void makeMenu(Container component) {
//			component.add(reorderButton);
//		}
//
//		public void initListeners() {
//			reorderButton.addActionListener(
//				new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					final JDialog dialog = new JDialog();
//					BorderLayout borderLayout1 = new BorderLayout();
//					JPanel panel = new JPanel();
//					panel.setLayout(borderLayout1);
//					dialog.getContentPane().add(panel);
//					final ListEditor le = new ListEditor();
//					le.setEditable(false);
//					le.setItems(windows);
//					final Map windowMap2 = windowMap;
//					final Map elementMap2 = elementMap;
//					panel.add(le, BorderLayout.NORTH);
//					JButton ok = new JButton("OK");
//					ok.addActionListener(
//						new ActionListener() {
//						public void actionPerformed(ActionEvent e) {
//							Map tmpWinMap = windowMap2;
//							Map tmpElementMap = elementMap2;
//							removeAllTabs();
//							java.util.List order = le.getItems();
//							Iterator it = order.iterator();
//							while (it.hasNext()) {
//								String winName = (String)it.next();
//								XenaView comp = (XenaView)tmpWinMap.get(winName);
//								try {
//									addTab(winName, comp);
//								} catch (XenaException x) {
//									MainFrame.singleton().showError(x);
//								}
//							}
//							dialog.dispose();
//							tabbedPane.updateUI();
//						}
//					});
//					panel.add(ok, BorderLayout.SOUTH);
//					dialog.pack();
//					dialog.setVisible(true);
//				}
//			});
//		}
//	}
}
