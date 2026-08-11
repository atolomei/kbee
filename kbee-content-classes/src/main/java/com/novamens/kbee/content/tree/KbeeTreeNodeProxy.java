package com.novamens.kbee.content.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.Tree;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreePath;
import com.novamens.security.acl.Permission;

public class KbeeTreeNodeProxy implements TreeNode, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Boolean hasChilds;
	private Boolean writeable;
	private TreeNode node;
	private List<TreeNode> childs;
	private Tree tree;
	
	KbeeTreeNodeProxy(Tree tree, TreeNode node) {
		this.tree = tree;
		if (node==null) {
			node = null;
		}
		this.node  = node;
	}
	
	public String getId() {
		return getNode().getId();
	}
	
	@Override
	public TreePath getPath() {
		return getNode()!=null ? getNode().getPath() : null;
	}

	public String getDisplayName() {
		return node!=null ? node.getDisplayName() : "null";
	}
	
	public boolean hasChilds() {
		if (hasChilds==null) {
			hasChilds = !getChilds().isEmpty();
		}
		return hasChilds;
	}
	
	public boolean isDescendant(TreeNode node) {
		return getNode().isDescendant(node);
	}
	
	public List<TreeNode> getChilds() {
		if (node==null) return new ArrayList<>();
		if (childs==null) {
			childs = getTree().getChilds(getNode());
		}
		else {
			return childs;
		}
		return childs;
	}
	
	public boolean includeAcl() {
		return getNode().includeAcl();
	}
	
	public TreeNode getNode() {
		return node;
	}
	
	public Tree getTree() {
		return tree;
	}
	
	public DataSetMember getObject() {
		return getNode().getObject();
	}
	
	public boolean isWriteable() {
		if (writeable==null) {
			writeable = getNode().isWriteable();
		}
		return writeable;
	}
	
	public boolean hasPermission(Permission permission) {
		return getNode().hasPermission(permission);
		//if (writeable==null) {
		//	writeable = getNode().hasPermission(permission);
		//}
		//return writeable;
	}
}