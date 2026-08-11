package com.novamens.content.tree;

import java.util.List;

import com.novamens.content.model.DataSetMember;

public interface Tree {
	public List<TreeNode> getRoots();
	public List<TreeNode> getChilds(TreeNode node);
	public TreeNode getNode(DataSetMember member, TreePath path);
}