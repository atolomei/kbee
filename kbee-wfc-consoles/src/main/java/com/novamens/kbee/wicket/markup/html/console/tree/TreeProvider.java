package com.novamens.kbee.wicket.markup.html.console.tree;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

import com.novamens.content.tree.TreePath;
import com.novamens.wicket.model.ObjectModel;

public abstract class TreeProvider <T> implements ITreeProvider<T> {
	private static final long serialVersionUID = 1L;
	 
	public IModel<T> model(T object) {
		return new ObjectModel<T>(object);
	}
	
	public T getNode(Object object, TreePath path) {
		return null;
	}
	
	public void detach() {
	}
}