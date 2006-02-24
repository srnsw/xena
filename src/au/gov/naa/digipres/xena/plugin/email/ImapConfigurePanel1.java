package au.gov.naa.digipres.xena.plugin.email;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Panel 1 to configure the EmailToXenaEmailNormaliser for IMAP emails.
 *
 * @author Chris Bitmead
 */
public class ImapConfigurePanel1 extends JPanel implements GuiConfigureSubPanel {
	AbstractGuiConfigureNormaliser configure;

	private DefaultListModel folderListModel = new DefaultListModel();

	JScrollPane jScrollPane1 = new JScrollPane();

	JList folderList = new JList();

	BorderLayout borderLayout1 = new BorderLayout();

	Store store;

	public ImapConfigurePanel1(AbstractGuiConfigureNormaliser configure) {
		this.configure = configure;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() throws XenaException {
	}

	Store getStore() throws XenaException, MessagingException {
		if (store == null) {
			EmailToXenaEmailNormaliser n = (EmailToXenaEmailNormaliser)configure.getNormaliser();
			store = n.getStore(configure.getInputSource().getType(), configure.getInputSource());
		}
		return store;
	}

	public void activate() throws XenaException {
		EmailToXenaEmailNormaliser n = (EmailToXenaEmailNormaliser)configure.getNormaliser();
		try {
			java.util.List folders = n.allFolders(getStore());
//			Method m = Folder.class.getMethod("getFullName", new Class[0]);
//			Method m = Object.class.getMethod("toString", new Class[0]);
//			Set selected = new AttrHashSet(n.allFolders(getStore()), m);
			Set selected = new HashSet(n.getFoldersOrAll(getStore()));
//			DefaultListModel model = (DefaultListModel)folderList.getModel();
			folderListModel.removeAllElements();
			Iterator it = folders.iterator();
			java.util.List indexes = new ArrayList();
			for (int i = 0; it.hasNext(); i++) {
				String foldername = (String)it.next();
				Folder fld = store.getFolder(foldername);
				if ((fld.getType() & Folder.HOLDS_MESSAGES) != 0) {
					folderListModel.addElement(fld);
				}
				if (selected.contains(fld.getFullName())) {
					indexes.add(new Integer(i));
				}
			}
			int[] select = new int[indexes.size()];
			it = indexes.iterator();
			for (int i = 0; it.hasNext(); i++) {
				Integer in = (Integer)it.next();
				select[i] = in.intValue();
			}
			folderList.setSelectedIndices(select);
		} catch (MessagingException x) {
			MainFrame.singleton().showError(x);
		}
	}

	public void finish() throws XenaException {
		EmailToXenaEmailNormaliser n = (EmailToXenaEmailNormaliser)configure.getNormaliser();
		java.util.List lst = new ArrayList();
		Object[] sel = folderList.getSelectedValues();
		for (int i = 0; i < sel.length; i++) {
			Folder fld = (Folder)sel[i];
			lst.add(fld.getFullName());
		}
		n.setFolders(lst);
	}

	public ImapConfigurePanel1() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		folderList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				configure.nextOk(folderList.getSelectedValue() != null);
			}
		});
		folderList.setFixedCellHeight( -1);
		folderList.setModel(folderListModel);
		folderList.setVisibleRowCount(20);
		this.setDebugGraphicsOptions(0);
		this.setLayout(borderLayout1);
		this.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(folderList, null);
	}
}
