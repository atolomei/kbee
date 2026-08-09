package com.novamens.kbee.wicket.markup.html.event;

import javax.persistence.Transient;
import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;



public class TreeNodeLabelClickEvent<T extends TreeNode> extends AbstractWicketAjaxEvent implements WicketAjaxEvent {
				
	
	@Transient
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TreeNodeLabelClickEvent.class.getName());

	private IModel<T> model;
	
	public TreeNodeLabelClickEvent() {
		super(null);
	}
	
	public TreeNodeLabelClickEvent(AjaxRequestTarget target, IModel<T> model) {
		super(target);
		setModel(model);
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public TreeNode getModelObject() {
		return this.model.getObject();
	}
	
	
	
}
