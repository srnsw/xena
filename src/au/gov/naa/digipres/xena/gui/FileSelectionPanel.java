package au.gov.naa.digipres.xena.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import kiwi.ui.FilesystemTreeView;
import kiwi.ui.model.ITreeNode;
import kiwi.ui.model.TreeModelTreeAdapter;
import au.gov.naa.digipres.xena.javatools.BrowseFile;

/**
 * A general purpose gui component that allows selecting files from a left
 * panel and moving them to a right panel.
 *
 * @author Chris Bitmead
 */
public class FileSelectionPanel extends JPanel {
	protected JPanel panel1 = new JPanel();

	protected BorderLayout borderLayout1 = new BorderLayout();

	protected TitledBorder titledBorder3;

	protected JPanel jPanel14 = new JPanel();

	protected JTextField regexTextField = new JTextField();

	protected BorderLayout borderLayout11 = new BorderLayout();

	protected TitledBorder titledBorder4;

	protected TitledBorder titledBorder1;

	protected JTextField jTextField1 = new JTextField();

	protected JScrollPane jScrollPane1 = new JScrollPane();

	protected JPanel jPanel10 = new JPanel();

	protected JPanel jPanel9 = new JPanel();

	protected JPanel jPanel8 = new JPanel();

	protected Box jPanel6 = Box.createVerticalBox();

	protected JSplitPane jSplitPane1 = new JSplitPane();

	protected JPanel jPanel5 = new JPanel();

	protected JPanel jPanel4 = new JPanel();

	protected JPanel jPanel3 = new JPanel();

	protected JPanel rightPanel = new JPanel();

	protected JPanel jPanel1 = new JPanel();

	protected JScrollPane jScrollPane3 = new JScrollPane();

	protected FlowLayout flowLayout1 = new FlowLayout();

	protected BorderLayout borderLayout8 = new BorderLayout();

	protected BorderLayout borderLayout6 = new BorderLayout();

	protected BorderLayout borderLayout5 = new BorderLayout();

	protected BorderLayout borderLayout4 = new BorderLayout();

	protected BorderLayout borderLayout3 = new BorderLayout();

	protected BorderLayout borderLayout2 = new BorderLayout();

	protected FilesystemTreeView selectTree = new FilesystemTreeView();

	protected BorderLayout borderLayout10 = new BorderLayout();

	protected BrowseFile browseRoot = new BrowseFile();

	protected Box box1 = Box.createVerticalBox();

	protected TitledBorder titledBorder2;

	protected DefaultListModel listModel = new DefaultListModel();

	protected JList fileList = new JList(listModel);

	protected JButton upButton = new JButton();

	protected JButton downButton = new JButton();

	protected JButton leftButton = new JButton();

	protected JButton rightButton = new JButton();

	protected boolean allowDirectory = true;

	public FileSelectionPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws java.io.IOException {
		JDialog bd = new JDialog();
		FileSelectionPanel pan = new FileSelectionPanel();
		bd.getContentPane().add(pan);
		bd.pack();
		bd.setModal(true);
		bd.setVisible(true);
		System.exit(0);
	}

	public void setAllowDirectory(boolean allowDirectory) {
		this.allowDirectory = allowDirectory;
	}

	public void setFiles(File[] files) {
		listModel.clear();
		for (int i = 0; i < files.length; i++) {
			String seg = fileToSegment(files[i]);
			listModel.addElement(seg);
		}
	}

	public File[] getFiles() {
		File[] rtn = new File[listModel.size()];
		Object[] arr = listModel.toArray();
		for (int i = 0; i < arr.length; i++) {
			rtn[i] = segmentToFile((String)arr[i]);
		}
		return rtn;
	}

	public boolean isAllowDirectory() {
		return allowDirectory;
	}

	public JPanel getRightPanel() {
		return rightPanel;
	}

	public void moveOne(File path) {
		TreeModelTreeAdapter smodel = (TreeModelTreeAdapter)selectTree.getJTree().getModel();
		ITreeNode snode = (ITreeNode)smodel.getRoot();
		snode = findNode(snode, path);
		TreePath tpath = smodel.getPathForNode(snode);
		selectTree.getJTree().addSelectionPath(tpath);

		String name = pathToName(tpath);
		listModel.removeElement(name);
	}

	String pathToName(TreePath tp) {
		StringBuffer fileName = new StringBuffer("");
		for (int j = 1; j < tp.getPathCount(); j++) {
			ITreeNode nd = (ITreeNode)tp.getPathComponent(j);
			File file = (File)nd.getObject();
			fileName.append(file.getName());
			if (file.isDirectory()) {
				fileName.append(System.getProperty("file.separator"));
			}
		}
		return fileName.toString();
	}

	protected void rightButton_actionPerformed(ActionEvent e) {
		File[] files = selectTree.getSelectedFiles();
		TreePath[] paths = selectTree.getJTree().getSelectionPaths();
		// For each selected item...
		for (int i = 0; i < paths.length; i++) {
			TreePath tp = paths[i];
			File fileObject = (File)((ITreeNode)tp.getLastPathComponent()).getObject();
			if (allowDirectory || !fileObject.isDirectory()) {
				// For each segment in the path...
				String fileName = pathToName(tp);
				if (!listModel.contains(fileName)) {
					listModel.addElement(fileName);
				}
			}
		}
	}

	protected void moveNode(TreeModel model, TreePath from, int index, MutableTreeNode to) {
		for (int j = 0; j < from.getPathCount(); j++) {
			ITreeNode nd = (ITreeNode)from.getPathComponent(j);
			System.out.println("ND: " + nd.getObject());
		}
	}

	protected File fileForPath(TreePath path) {
		ITreeNode node = (ITreeNode)path.getLastPathComponent();
		return ((node == null) ? null : (File)(node.getObject()));
	}

	protected void leftButton_actionPerformed(ActionEvent e) {
		TreeModelTreeAdapter smodel = (TreeModelTreeAdapter)selectTree.getJTree().getModel();
		Object[] paths = fileList.getSelectedValues();
		if (paths != null) {
			for (int j = 0; j < paths.length; j++) {
				String path = (String)paths[j];
				File file = segmentToFile(path);
				moveOne(file);
			}
		}
	}

	protected File segmentToFile(String segment) {
		return new File(browseRoot.getText() + System.getProperty("file.separator") + segment);
	}

	protected String fileToSegment(File file) {
		String rtn = null;
		File root = new File(browseRoot.getText());
		while (!file.equals(root)) {
			String oldrtn = rtn;
			rtn = file.getName();
			if (oldrtn != null) {
				rtn += System.getProperty("file.separator") + oldrtn;
			}
			file = file.getParentFile();
		}
		return rtn;
	}

	protected ITreeNode findNode(Object tn, File f) {
		TreeModel model = selectTree.getJTree().getModel();
		for (int i = 0; i < model.getChildCount(tn); i++) {
			ITreeNode itn = (ITreeNode)model.getChild(tn, i);
			if (itn.getObject().equals(f)) {
				return itn;
			}
		}
		return null;
	}

	protected void regexTextField_actionPerformed(ActionEvent e) {
		try {
			Pattern pattern = Pattern.compile(regexTextField.getText(), Pattern.CASE_INSENSITIVE);
			markRegexp((ITreeNode)selectTree.getJTree().getModel().getRoot(), pattern);
		} catch (PatternSyntaxException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void markRegexp(ITreeNode select, Pattern pattern) {
		TreeModelTreeAdapter smodel = (TreeModelTreeAdapter)selectTree.getJTree().getModel();
		File file = (File)select.getObject();
		if (file.isDirectory()) {
			for (int i = 0; i < smodel.getChildCount(select); i++) {
				ITreeNode im = (ITreeNode)smodel.getChild(select, i);
				markRegexp(im, pattern);
			}
		} else {
			Matcher matcher = pattern.matcher(file.getName());
			if (matcher.matches()) {
				TreePath path = smodel.getPathForNode(select);
				selectTree.getJTree().makeVisible(path);
				selectTree.getJTree().addSelectionPath(path);
			}
		}
	}

	protected void browseRoot_actionPerformed(ActionEvent e) {
		File newFile = browseRoot.getFile();
		File oldFile = (File)((ITreeNode)selectTree.getJTree().getModel().getRoot()).getObject();
		if (!newFile.equals(oldFile)) {
			if (JOptionPane.showConfirmDialog(this, "Changing the root will clear all previous selections", "Confirm",
											  JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
				try {
					selectTree.setRoot(newFile);
					listModel.removeAllElements();
				} catch (IllegalArgumentException ex) {
					JOptionPane.showMessageDialog(this, newFile.toString() + " is not a Directory", "Error", JOptionPane.ERROR_MESSAGE);
					browseRoot.setText(oldFile.toString());
				}
			} else {
				browseRoot.setText(oldFile.toString());
			}
		}
	}

	public void setDirectory(File dir) {
		selectTree.setRoot(dir);
		browseRoot.setFile(dir);
		listModel.removeAllElements();
	}

	protected void upButton_actionPerformed(ActionEvent e) {
		Object[] paths = fileList.getSelectedValues();
		if (paths != null) {
			for (int j = 0; j < paths.length; j++) {
				String path = (String)paths[j];
				int index = listModel.indexOf(path);
				if (index != 0) {
					listModel.remove(index);
					listModel.insertElementAt(path, index - 1);
				}
			}
			int[] pathList = new int[paths.length];
			for (int i = 0; i < paths.length; i++) {
				pathList[i] = listModel.indexOf(paths[i]);
			}
			fileList.setSelectedIndices(pathList);
		}
	}

	void downButton_actionPerformed(ActionEvent e) {
		Object[] paths = fileList.getSelectedValues();
		if (paths != null) {
			for (int j = paths.length - 1; 0 <= j; j--) {
				String path = (String)paths[j];
				int index = listModel.indexOf(path);
				if (index != listModel.size() - 1) {
					listModel.remove(index);
					listModel.insertElementAt(path, index + 1);
				}
			}
			int[] pathList = new int[paths.length];
			for (int i = 0; i < paths.length; i++) {
				pathList[i] = listModel.indexOf(paths[i]);
			}
			fileList.setSelectedIndices(pathList);
		}
	}

	protected void jbInit() throws Exception {
		titledBorder3 = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 2), "Output Directory");
		titledBorder4 = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 2), "Regular Expression");
		titledBorder1 = new TitledBorder("Regular Expression Selection");
		titledBorder2 = new TitledBorder("Change Root Path");
		panel1.setLayout(borderLayout1);
		jTextField1.setBorder(titledBorder1);
		jTextField1.setText("jTextField1");
		jPanel9.setLayout(borderLayout8);
		jPanel8.setLayout(borderLayout6);
		jPanel5.setLayout(flowLayout1);
		jPanel4.setLayout(borderLayout5);
		jPanel3.setLayout(borderLayout2);
		rightPanel.setLayout(borderLayout3);
		jPanel1.setLayout(borderLayout4);
		browseRoot.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		browseRoot.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseRoot_actionPerformed(e);
			}
		});
		selectTree.setRoot(new java.io.File(browseRoot.getText()));
		selectTree.setMultipleSelectionsAllowed(true);
		jPanel10.setLayout(borderLayout10);
		browseRoot.setBorder(titledBorder2);
		regexTextField.setBorder(null);
		jPanel14.setLayout(borderLayout11);
		jPanel14.setBorder(titledBorder4);
		upButton.setText("^^");
		upButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				upButton_actionPerformed(e);
			}
		});
		downButton.setText("vv");
		downButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downButton_actionPerformed(e);
			}
		});
		leftButton.setToolTipText("Remove From Group");
		leftButton.setText("<<");
		leftButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				leftButton_actionPerformed(e);
			}
		});
		rightButton.setToolTipText("Add to Group");
		rightButton.setText(">>");
		rightButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rightButton_actionPerformed(e);
			}
		});
		this.add(panel1);
		panel1.add(jPanel10, BorderLayout.CENTER);
		jPanel9.add(jScrollPane3, BorderLayout.CENTER);
		jScrollPane3.getViewport().add(fileList, null);
		rightPanel.add(jPanel4, BorderLayout.WEST);
		jPanel4.add(jPanel5, BorderLayout.CENTER);
		jPanel5.add(jPanel6, null);
		rightPanel.add(jPanel9, BorderLayout.CENTER);
		jPanel6.add(leftButton, null);
		jPanel6.add(rightButton, null);
		jPanel6.add(upButton, null);
		jPanel6.add(downButton, null);
		jSplitPane1.add(jPanel1, JSplitPane.LEFT);
		jPanel1.add(jPanel3, BorderLayout.CENTER);
		jSplitPane1.add(rightPanel, JSplitPane.RIGHT);
		jPanel3.add(selectTree, BorderLayout.CENTER);
		jPanel3.add(box1, BorderLayout.SOUTH);
		box1.add(browseRoot, null);
		box1.add(jPanel14, null);
		jPanel14.add(regexTextField, BorderLayout.CENTER);
		this.add(jPanel8);
		jPanel8.add(jSplitPane1, BorderLayout.CENTER);
		regexTextField.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regexTextField_actionPerformed(e);
			}
		});
	}

	class GroupFileNode extends DefaultMutableTreeNode {
		public GroupFileNode(File file) {
			super(file, file.isDirectory());
		}

		public File getFile() {
			return (File)this.getUserObject();
		}

		public String toString() {
			return getFile().getName();
		}
	}
}
