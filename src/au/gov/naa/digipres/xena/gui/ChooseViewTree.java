package au.gov.naa.digipres.xena.gui;
import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import au.gov.naa.digipres.xena.kernel.view.XenaView;


/**
 * Dialog panel used for selecting a view for each element in the view hierarchy.
 *
 * @author Chris Bitmead
 * @version 1.0
 */
public class ChooseViewTree extends JPanel {
	protected BorderLayout borderLayout1 = new BorderLayout();

	protected JScrollPane jScrollPane1 = new JScrollPane();

	protected DefaultMutableTreeNode root = new DefaultMutableTreeNode("top");

	protected DefaultTreeModel model = new DefaultTreeModel(root);

	protected JTree tree = new JTree(model);

	protected InternalFrame ifr;

	public ChooseViewTree() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void set(InternalFrame ifr) {
		this.ifr = ifr;
		// This clears the tree.
		tree.setSelectionRow(0);
		refresh();
	}

	public void refresh() {
		root = new DefaultMutableTreeNode("top");
		model.setRoot(root);
		Iterator it = ifr.getWrapperView().getSubViews().iterator();
		while (it.hasNext()) {
			XenaView subView = (XenaView)it.next();
			add((DefaultMutableTreeNode)model.getRoot(), subView);
		}
	}

	public XenaView getSelectedView() {
		XenaView rtn = null;
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			rtn = (XenaView)node.getUserObject();
		}
		return rtn;
	}

	protected void add(DefaultMutableTreeNode node, XenaView view) {
		DefaultMutableTreeNode n = new DefaultMutableTreeNode(view);
		int c = node.getChildCount();
		model.insertNodeInto(n, node, node.getChildCount());
		tree.makeVisible(new TreePath(model.getPathToRoot(n)));
		Iterator it = view.getSubViews().iterator();
		while (it.hasNext()) {
			XenaView subView = (XenaView)it.next();
			add(n, subView);
		}
	}

	protected void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		tree.setRootVisible(false);
		this.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(tree, null);
	}

	public JTree getTree() {
		return tree;
	}
}
