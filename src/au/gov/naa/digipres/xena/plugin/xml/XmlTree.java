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

import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Widget to show XML as a tree.
 *
 */
public class XmlTree extends JTree {
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

	public void setElement(Element root) {
		TreeModel treeMod = buildTree(root);
		setModel(treeMod);
	}

	private DefaultTreeModel buildTree(Element root) {
		DefaultMutableTreeNode treeNode;
		treeNode = createTreeNode(root);
		return new DefaultTreeModel(treeNode);
	}

	public void addNode(java.util.List path) {
		// DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(root.getName());
		// root.setAllowsChildren(true);
		assert 0 < path.size();
		DefaultMutableTreeNode parent = root;
		XmlTreeView.MyContentHandler.Element last = null;
		Iterator it = path.iterator();
		while (it.hasNext()) {
			XmlTreeView.MyContentHandler.Element component = (XmlTreeView.MyContentHandler.Element) it.next();
			if (component.node == null) {
				assert component.qName != null;
				component.node = new DefaultMutableTreeNode(component.qName, false);
				parent.setAllowsChildren(true);
				model.insertNodeInto(component.node, parent, parent.getChildCount());
				// model.insertNodeInto(component.node, parent, 0);
			}
			parent = component.node;
			last = component;
		}
		int n = 1;
		// expandPath doesn't work for leaf nodes, so need this as a separate step.
		it = path.iterator();
		while (it.hasNext()) {
			XmlTreeView.MyContentHandler.Element component = (XmlTreeView.MyContentHandler.Element) it.next();

			if (n <= 1) {
				expandPath(new TreePath(model.getPathToRoot(component.node)));
			}
			n++;
		}
		if (last.atts != null) {
			for (int i = 0; i < last.atts.getLength(); i++) {
				String name = last.atts.getQName(i);
				String value = last.atts.getValue(i);
				last.node.setAllowsChildren(true);
				model.insertNodeInto(new DefaultMutableTreeNode("[Attribute]: " + name + "=\"" + value + "\""), last.node, last.node.getChildCount());
			}
		}
		if (last.data != null) {
			last.node.setAllowsChildren(true);
			model.insertNodeInto(new DefaultMutableTreeNode("[Data]: " + last.data.toString()), last.node, last.node.getChildCount());
		}
	}

	private DefaultMutableTreeNode createTreeNode(Element root) {
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(root.getName());
		treeNode.setAllowsChildren(true);
		java.util.List attribs = root.getAttributes();
		Iterator it = attribs.iterator();
		while (it.hasNext()) {
			Attribute attribNode = (Attribute) it.next();
			String name = attribNode.getName();
			String value = attribNode.getValue();
			treeNode.add(new DefaultMutableTreeNode("[Attribute]: " + name + "=\"" + value + "\""));
		}
		if (!root.getText().trim().equals("")) {
			treeNode.add(new DefaultMutableTreeNode("[Data]: " + root.getText()));
		}
		java.util.List children = root.getChildren();
		it = children.iterator();
		while (it.hasNext()) {
			Element node = (Element) it.next();
			treeNode.add(createTreeNode(node));
		}
		return treeNode;
	}

}
