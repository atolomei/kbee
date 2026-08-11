package com.novamens.kbee.content.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreePath;

public class KbeeTreePath implements TreePath, Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<TreeNode> nodes = new ArrayList<>();
	private String path = "";
	
	public List<TreeNode> getNodes() {
		return nodes;
	}
	
	public String asString() {
		return path;
	}
	
	public boolean isDescendant(TreePath path) {
		return asString().startsWith(path.asString());
	}
	
	public void setNodes(List<TreeNode> nodes) {
		this.nodes = nodes;
		path = "";
		for (TreeNode node : nodes) {
			if (!"".equals(path)) path += "/";
			path += node.getId();
		}
	}
	
	public TreeNode getChild(TreeNode parent) {
		int p = 0;
		for (TreeNode node : nodes) {
			if (node.equals(parent)) {
				break;
			}
			else {
				p++;
			}
		}
		return p<nodes.size()-1 ? nodes.get(p+1) : null;
	}
	
	public TreeNode getNode() {
		return !nodes.isEmpty() ? nodes.get(nodes.size()-1) : null;
	}
	
	public TreeNode getRoot() {
		return !nodes.isEmpty() ? nodes.get(0) : null;
	}
	
	public TreePath plus(TreeNode node) {
		KbeeTreePath path = new KbeeTreePath();
		List<TreeNode> nodes = new ArrayList<>();
		nodes.addAll(this.nodes);
		nodes.add(node);
		path.setNodes(nodes);
		return path;
	}
}