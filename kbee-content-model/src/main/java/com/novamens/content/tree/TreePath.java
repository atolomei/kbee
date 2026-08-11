package com.novamens.content.tree;

import java.util.List;

public interface TreePath {
	public String asString();
	public TreePath plus(TreeNode node);
	public TreeNode getChild(TreeNode node);
	public TreeNode getNode();
	public List<TreeNode> getNodes();
	public TreeNode getRoot();
	public boolean isDescendant(TreePath path);
}