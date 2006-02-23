package au.gov.naa.digipres.xena.javatools;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;

public class MultiList extends JDialog {
	private BorderLayout borderLayout1 = new BorderLayout();

	private DefaultListModel model = new DefaultListModel();

	private JPanel jPanel1 = new JPanel();

	private JPanel jPanel2 = new JPanel();

	private JPanel jPanel3 = new JPanel();

	private JButton okButton = new JButton();

	private JButton cancelButton = new JButton();

	private BorderLayout borderLayout2 = new BorderLayout();

	private JPanel jPanel4 = new JPanel();

	private JScrollPane jScrollPane1 = new JScrollPane();

	private JList list = new JList();

	Object[] result = null;

	private BorderLayout borderLayout3 = new BorderLayout();

	private Border border;

	public MultiList(Dialog parent, Object[] objs, String message, String title) {
		super(parent, title, true);
		init(objs, message);
	}

	public MultiList(Frame parent, Object[] objs, String message, String title) {
		super(parent, title, true);
		init(objs, message);
	}

	public void init(Object[] objs, String title) {
		try {
			jbInit();
			for (int i = 0; i < objs.length; i++) {
				model.addElement(objs[i]);
			}
			border = new TitledBorder(BorderFactory.createLineBorder(Color.white, 1), title);
			jPanel4.setBorder(border);
//			titleLabel.setText("  " + title);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setSelected(Object[] objs) {
		int[] indexes = new int[objs.length];
		for (int i = 0; i < objs.length; i++) {
			indexes[i] = model.indexOf(objs[i]);
		}
		list.setSelectedIndices(indexes);
	}

	void jbInit() throws Exception {
		list.setModel(model);
		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButton_actionPerformed(e);
			}
		});
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});
		jPanel1.setLayout(borderLayout2);
		jPanel4.setLayout(borderLayout3);
		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jPanel4, BorderLayout.CENTER);
		jPanel4.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(list, null);
		this.getContentPane().add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(jPanel3, null);
		jPanel3.add(cancelButton, null);
		jPanel3.add(okButton, null);
		pack();
	}

	/*	public static Object[] showDialog(Frame parent, Object[] objs, String title) {
	  JDialog d = new JDialog( parent, title, true);
	  MultiList lst = new MultiList();
	  JPanel outerPanel
	  d.getContentPane().add(lst);
	 }*/

	public static void main(String[] args) {
		Object[] objs = {
			"Foo", "Bar", "Baz"};
		MultiList ml = new MultiList((Frame)null, objs, "Select Some", "Selection");
		ml.setVisible(true);
//		objs = showDialog(null, objs, "Select some");
		objs = ml.getResult();
		if (objs == null) {
			System.out.println("--CANCEL--");
		} else {
			for (int i = 0; i < objs.length; i++) {
				System.out.println(objs[i]);
			}
		}
		System.exit(1);
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		result = null;
		dispose();
	}

	void okButton_actionPerformed(ActionEvent e) {
		result = list.getSelectedValues();
		dispose();
	}

	public Object[] getResult() {
		return result;
	}

	public JList getList() {
		return list;
	}
}
