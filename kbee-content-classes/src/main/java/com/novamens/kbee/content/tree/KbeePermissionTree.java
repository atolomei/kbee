package com.novamens.kbee.content.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.TreeNode;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.security.acl.Permission;

public class KbeePermissionTree extends KbeeAbstractTree {
	private static final long serialVersionUID = 1L;
	
	private List<TreeNode> writeablenodes;
	private Permission permission;
	
	public KbeePermissionTree(DataSet  dataSet, Permission permission) {
		super(dataSet);
		this.permission = permission;
	}
	
	public List<TreeNode> getRoots() {
		List<TreeNode> roots = new ArrayList<>();
		List<TreeNode> control =  new ArrayList<>();
		for (TreeNode node : getWriteableNodes()) {
			TreeNode root = node.getPath().getRoot();
			if (!control.contains(root)) {
				roots.add(new KbeeTreeNodeProxy(this, root));
				control.add(root);
			}
		}
		roots.sort(new Comparator<TreeNode>() {
			@Override
			public int compare(TreeNode n1, TreeNode n2) {
				return n1.getDisplayName().compareToIgnoreCase(n2.getDisplayName());
			}	
		});
		return roots;
	}
	
	public List<TreeNode> getChilds(TreeNode node) {
		List<TreeNode> childs = new ArrayList<>();
		List<TreeNode> control =  new ArrayList<>();
		for (TreeNode writeablenode : getWriteableNodes()) {
			if (writeablenode.isDescendant(node)) {
				TreeNode child =  writeablenode.getPath().getChild(node);
				if (!control.contains(child)) {
					childs.add(new KbeeTreeNodeProxy(this, child));
					control.add(child);
				}	
			}
		}
		return childs;
	}
	
	public List<TreeNode> getWriteableNodes() {
		if (writeablenodes==null) {
			writeablenodes = searchNodes();
		}
		return writeablenodes;
	}

	private List<TreeNode> searchNodes() {
		List<TreeNode> nodes = new ArrayList<>();
		Query query = new PermissionEnabledNodesQuery(getQueryIndex(), getDataSet(), permission);
		ResultSet resulSet = query.execute();
		while (resulSet.hasNext()) {
			DataSetMember member = (DataSetMember)resulSet.next().getObject();
			for (TreeNode nodeToAdd : getNodes(member)) {
				boolean add = true;
				for (TreeNode node : nodes) {
					if (nodeToAdd.isDescendant(node)) {
						nodes.remove(node);
						break;
					}
					if (node.isDescendant(nodeToAdd)) {
						add=false;
					}
				}
				if (add) nodes.add(nodeToAdd);
			};
		}
		return nodes;
	}
}
