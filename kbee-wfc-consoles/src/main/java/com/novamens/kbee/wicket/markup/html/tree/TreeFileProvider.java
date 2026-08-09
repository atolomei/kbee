package com.novamens.kbee.wicket.markup.html.tree;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

import com.novamens.content.document.TreeFile;
import com.novamens.wicket.model.ObjectModel;


/**
 * 
 */
public class TreeFileProvider implements ITreeProvider<TreeFile> {

	private static final long serialVersionUID = 1L;
	
	public IModel<TreeFile> root_model;
	
	public TreeFileProvider(IModel<TreeFile> root_model) {
		this.root_model=root_model;
	}
	
	@Override
	public void detach() {
		root_model.detach();
	}

	@Override
	public Iterator<? extends TreeFile> getRoots() {
		if (this.root_model.getObject().getChildren()==null)
			return null;
		return this.root_model.getObject().getChildren().iterator();
	}

	@Override
	public boolean hasChildren(TreeFile node) {
		return node.getChildCount()>0;
	}

	@Override
	public Iterator<? extends TreeFile> getChildren(TreeFile node) {
		return node.getChildren().iterator();
	}

	@Override
	public IModel<TreeFile> model(TreeFile object) {
		return new ObjectModel<TreeFile>(object);
	}

}
