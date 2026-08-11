package com.novamens.kbee.content.tree;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.TreeNode;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;

public class KbeeTree extends KbeeAbstractTree {
	private static final long serialVersionUID = 1L;

	public KbeeTree(DataSet dataSet) {
		super(dataSet);
	}
	
	public List<TreeNode> getRoots() {
		List<TreeNode> roots = new ArrayList<>();
		Query query = new RootsQuery(getQueryIndex(), getDataSet());
		ResultSet resulSet = query.execute();
		while (resulSet.hasNext()) {
			SearchResult result = resulSet.next();
			if (result!=null) {
				DataSetMember member = (DataSetMember)result.getObject();
				if (member!=null) {
					TreeNode node = getNode(member, new KbeeTreePath());
					roots.add(node);
				}
			}
		}
		return roots;
	}
	
	public List<TreeNode> getChilds(TreeNode node) {
		List<TreeNode> childs = new ArrayList<>();
		Query query = new ChildsQuery(getQueryIndex(), node.getObject());
		ResultSet resulSet = query.execute();
		while (resulSet.hasNext()) {
			DataSetMember member = (DataSetMember)resulSet.next().getObject();
			TreeNode child = getNode(member, node.getPath());
			if (child!=null) {
				childs.add(new KbeeTreeNodeProxy(this, child));
			}
			else {
				node.getDisplayName();
			}
		}
		return childs;
	}
}