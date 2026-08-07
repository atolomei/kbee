package com.novamens.content.web.resource.markup.model;

import javax.swing.tree.TreeNode;

import org.apache.wicket.model.IModel;

import com.novamens.content.document.TreeFile;

public class TreeFileNodeModel implements IModel<TreeNode> {

	private static final long serialVersionUID = 1L;

	IModel<TreeFile> tfmodel;
	
	public TreeFileNodeModel(IModel<TreeFile> tfm) {
		tfmodel=tfm;
	}
	
	@Override
	public void detach() {
		tfmodel.detach();
	}
	
	@Override
	public TreeNode getObject() {
		return tfmodel.getObject();
	}

}
