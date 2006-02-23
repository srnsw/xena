package au.gov.naa.digipres.xena.javatools;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;

public class ListEditor extends JPanel {
	BorderLayout borderLayout1 = new BorderLayout();
//	Box vbox;

	Box vboxButton;

	Box hboxButtonPanel;

	Box hbox;
//  JList jList1 = new JList();

	JButton deleteButton = new JButton();

	JButton insertButton = new JButton();

	JTextField editText = new JTextField();

	JScrollPane scrollPane = new JScrollPane();

	JList list = new JList(new DefaultListModel());

	JButton updateButton = new JButton();

	JButton upButton = new JButton();

	JButton downButton = new JButton();

	java.util.List modifiedActions = new java.util.ArrayList();

//	JButton currentButton;

	public ListEditor() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ListEditor(java.util.List items) {
		this();
		setItems(items);
	}

	public static void main(String[] args) {
		JDialog d = new JDialog();
		ListEditor ed = new ListEditor();
		ed.setEditable(false);
		ed.setEditable(true);
		//ed.jList1.add
		d.getContentPane().add(ed);
		d.pack();
		d.setVisible(true);
	}

	public void setEditable(boolean v) {
		this.remove(hboxButtonPanel);
		if (v) {
			this.add(hboxButtonPanel, BorderLayout.SOUTH);
		}
	}

	public void setItems(java.util.List items) {
		clear();
		if (items != null) {
			addItems(items);
		}
	}

	public JList getList() {
		return list;
	}

	public java.util.List getItems() {
		java.util.List rtn = new ArrayList();
		Enumeration en = ((DefaultListModel)list.getModel()).elements();
		while (en.hasMoreElements()) {
			rtn.add(en.nextElement());
		}
		return rtn;
	}

	public void clear() {
		DefaultListModel lm = (DefaultListModel)list.getModel();
		lm.clear();
	}

	public void addItems(java.util.List items) {
		DefaultListModel lm = (DefaultListModel)list.getModel();
		Iterator it = items.iterator();
		while (it.hasNext()) {
			lm.addElement(it.next());
		}
	}

	public void addModifiedListener(ListModifiedListener list) {
		modifiedActions.add(list);
	}

	public void clearSelection() {
		insertButton.setEnabled(false);
		updateButton.setEnabled(false);
		deleteButton.setEnabled(false);
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		list.clearSelection();
	}

	void notifyListeners() {
		Iterator it = modifiedActions.iterator();
		while (it.hasNext()) {
			ListModifiedListener list = (ListModifiedListener)it.next();
			list.modified();
		}
	}

	void jbInit() throws Exception {
//		vbox = Box.createVerticalBox();
		hboxButtonPanel = Box.createHorizontalBox();
		hbox = Box.createHorizontalBox();
		vboxButton = Box.createVerticalBox();
		this.setLayout(borderLayout1);
		deleteButton.setText("Delete");
		deleteButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteButton_actionPerformed(e);
			}
		});
		insertButton.setText("Insert");
		insertButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertButton_actionPerformed(e);
			}
		});
		list.addListSelectionListener(
			new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent ev) {
				if (!list.isSelectionEmpty()) {
					DefaultListModel lm = (DefaultListModel)list.getModel();
					String text = lm.elementAt(list.getSelectedIndex()).toString();
					editText.setText(text);
//						currentButton = updateButton;
					insertButton.setEnabled(true);
					updateButton.setEnabled(true);
					deleteButton.setEnabled(true);
					upButton.setEnabled(1 < lm.size() && 0 < list.getSelectedIndex());
					downButton.setEnabled(1 < lm.size() && list.getSelectedIndex() < lm.size() - 1);
				}
			}
		});
//    jList1.setMaximumSize(new Dimension(111, 111));
//    jList1.setPreferredSize(new Dimension(200, 100));
//    jScrollPane1.setMaximumSize(new Dimension(111, 111));
//		scrollPane.setPreferredSize(new Dimension(300, 100));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		updateButton.setText("Update");
		updateButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateButton_actionPerformed(e);
			}
		});
		upButton.setToolTipText("");
		upButton.setText("Up     ");
		upButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				upButton_actionPerformed(e);
			}
		});
		downButton.setText("Down");
		downButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downButton_actionPerformed(e);
			}
		});
		editText.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editText_actionPerformed(e);
			}
		});
		//		this.add(vbox,  BorderLayout.CENTER);
//		vbox.add(hbox, null);
		this.add(hbox, BorderLayout.CENTER);

		scrollPane.getViewport().add(list, null);
		hbox.add(scrollPane, null);
		hbox.add(vboxButton, null);
		vboxButton.add(upButton, null);
		vboxButton.add(downButton, null);
//    jScrollPane1.add(jList1, null);
		this.add(hboxButtonPanel, BorderLayout.SOUTH);
		hboxButtonPanel.add(deleteButton, null);
		hboxButtonPanel.add(updateButton, null);
		hboxButtonPanel.add(insertButton, null);
		hboxButtonPanel.add(editText, null);
//		currentButton = insertButton;
		DocumentListener myListener =
			new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				go();
			}

			public void removeUpdate(DocumentEvent e) {
				go();
			}

			public void changedUpdate(DocumentEvent e) {
				go();
			}

			void go() {
				boolean v = 0 < editText.getText().length();
				insertButton.setEnabled(v);
				updateButton.setEnabled(v && !list.isSelectionEmpty());
			}
		};
		editText.getDocument().addDocumentListener(myListener);
		clearSelection();
	}

	void insertButton_actionPerformed(ActionEvent e) {
		DefaultListModel lm = (DefaultListModel)list.getModel();
		lm.addElement(editText.getText());
		editText.setText("");
		notifyListeners();
		clearSelection();
	}

	void deleteButton_actionPerformed(ActionEvent e) {
		DefaultListModel lm = (DefaultListModel)list.getModel();
		lm.removeElementAt(list.getSelectedIndex());
		editText.setText("");
		notifyListeners();
		clearSelection();
	}

	void updateButton_actionPerformed(ActionEvent e) {
		DefaultListModel lm = (DefaultListModel)list.getModel();
		lm.setElementAt(editText.getText(), list.getSelectedIndex());
		editText.setText("");
		notifyListeners();
		clearSelection();
	}

	void upButton_actionPerformed(ActionEvent e) {
		DefaultListModel lm = (DefaultListModel)list.getModel();
		int index = list.getSelectedIndex();
		if (0 < index) {
			Object text = lm.elementAt(index);
			lm.removeElementAt(index);
			lm.insertElementAt(text, index - 1);
			list.setSelectedIndex(index - 1);
		}
	}

	void downButton_actionPerformed(ActionEvent e) {
		DefaultListModel lm = (DefaultListModel)list.getModel();
		int index = list.getSelectedIndex();
		if (0 <= index && index < (lm.size() - 1)) {
			Object text = lm.elementAt(index);
			lm.removeElementAt(index);
//			lm.insertElementAt(editText.getText(), index + 1);
			lm.insertElementAt(text, index + 1);
			list.setSelectedIndex(index + 1);
		}
	}

	void editText_actionPerformed(ActionEvent e) {
		insertButton_actionPerformed(e);
	}

	public interface ListModifiedListener {
		public void modified();
	}
}
