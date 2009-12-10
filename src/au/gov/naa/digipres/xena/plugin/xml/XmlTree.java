/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.xml;

import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Widget to show XML as a tree.
 *
 */
public class XmlTree extends JTree {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DefaultMutableTreeNode root;

	DefaultTreeModel model;

	public XmlTree() {
		super();
		setModel(model);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(false);
		setEditable(false);
		// this.expandPath(new TreePath(model.getPathToRoot(root)));
		setRootVisible(false);
	}

	public void clear() {
		root = new DefaultMutableTreeNode("/");
		model = new DefaultTreeModel(root);
		setModel(model);
	}

	public void addNode(List<XmlTreeView.XmlTreeHandler.Element> path) {
		assert 0 < path.size();
		DefaultMutableTreeNode parent = root;
		XmlTreeView.XmlTreeHandler.Element last = null;

		for (XmlTreeView.XmlTreeHandler.Element component : path) {
			if (component.node == null) {
				assert component.qName != null;
				component.node = new DefaultMutableTreeNode(component.qName, false);
				parent.setAllowsChildren(true);
				model.insertNodeInto(component.node, parent, parent.getChildCount());
			}
			parent = component.node;
			last = component;
		}

		// expandPath doesn't work for leaf nodes, so need this as a separate step.
		int n = 1;
		for (XmlTreeView.XmlTreeHandler.Element component : path) {
			if (n <= 1) {
				expandPath(new TreePath(model.getPathToRoot(component.node)));
			}
			n++;
		}

		if (last != null && last.atts != null) {
			for (int i = 0; i < last.atts.getLength(); i++) {
				String name = last.atts.getQName(i);
				String value = last.atts.getValue(i);
				last.node.setAllowsChildren(true);
				model.insertNodeInto(new DefaultMutableTreeNode("[Attribute]: " + name + "=\"" + value + "\""), last.node, last.node.getChildCount());
			}
		}

		if (last != null && last.data != null) {
			last.node.setAllowsChildren(true);
			model.insertNodeInto(new DefaultMutableTreeNode("[Data]: " + last.data.toString()), last.node, last.node.getChildCount());
		}
	}

}
