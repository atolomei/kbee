package com.novamens.kbee.wicket.markup.html.console.tree;

import java.io.Serializable;

import com.novamens.content.tree.TreePath;

public interface TreeNode<T> extends Serializable {
	public String getPath();
	public TreePath getTreePath();
	public T getObject();
	public String getDisplayName();
}
