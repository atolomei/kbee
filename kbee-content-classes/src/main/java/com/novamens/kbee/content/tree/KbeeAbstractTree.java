package com.novamens.kbee.content.tree;

import java.io.Serializable;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.tree.Tree;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreePath;
import com.novamens.content.tree.TreeService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.model.KbeeValueSet;
import com.novamens.service.ServiceLocator;

public abstract class KbeeAbstractTree implements Tree, Serializable {
	private static final long serialVersionUID = 1L;
	
	Serializable dataSetId;
	
	public KbeeAbstractTree(DataSet dataSet) {
		Assert.isTrue(dataSet.isHierachical(), "no hierachical");
		this.dataSetId = dataSet.getId();
	}
	
	public TreeService getTreeService() {
		return ServiceLocator.getService(TreeService.class);
	}
	
	public DataSet getDataSet() {
		KbeeValueSet dataSet = new KbeeValueSet();
		dataSet.setId(dataSetId);
		dataSet.setHierachical(true);
		return dataSet; 
	}
	
	public TreeNode getNode(DataSetMember member, TreePath path) {
		for (TreeNode node : getTreeService().getNodes(member)) {
			if (path==null || node.getPath().isDescendant(path)) {
				return new KbeeTreeNodeProxy(this, node);
			}
		}
		return null;
	}
	
	protected List<TreeNode> getNodes(DataSetMember member) {
		return getTreeService().getNodes(member);
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}