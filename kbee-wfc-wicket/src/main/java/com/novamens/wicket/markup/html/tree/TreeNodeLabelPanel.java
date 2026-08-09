package com.novamens.wicket.markup.html.tree;


import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;

import com.novamens.kbee.wicket.markup.html.event.TreeNodeLabelClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;

public class TreeNodeLabelPanel<T extends TreeNode> extends ModelPanel<T> {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TreeNodeLabelPanel.class.getName());
	
	private int index = 0;
	IModel<T> model;

	// private boolean hasExpander;
	// private boolean hasImage;
	//private boolean expanded=false;


	/**
	 *  expander. cssexpander
	 *  menu
	 *  label
	 * 
	 * @param id
	 * @param model
	 */
	
	public TreeNodeLabelPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
		setModel(model);
	}
	
	public TreeNodeLabelPanel(String id, IModel<T> model, int index) {
		super(id, model);
		setOutputMarkupId(true);
		setModel(model);
		this.index=index;
	}
	
	
	

	
	public int getIndex() {

		if (getModel().getObject().getParent()==null)
			return 0;
		
		// return this.index;
		
		return getModel().getObject().getParent().getIndex(getModel().getObject());
		
		// ---
		//
		// int n=getModel().getObject().getParent().getIndex(getModel().getObject());
		//
		// logger.debug("n " + String.valueOf(n)+ " index " + this.index);
		// return this.index;
		//
		//
		
	}
	
	public IModel<String> getLabel() {
		
		if (getModel()==null)
			return new Model<String>("null model");
		
		if (getModel().getObject()==null)
			return new Model<String>("null model object");
		
		return new Model<String>("Node_"+ String.valueOf( getIndex() ) + " childs:"+String.valueOf(getModel().getObject().getChildCount()));
	}
	
	
	@SuppressWarnings({ "rawtypes", "serial" })
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer labc = new WebMarkupContainer("label-container");
		add(labc);
		
		labc.add(new AttributeModifier("class", new Model<String>() {

			private static final long serialVersionUID = 1L;

			public String getObject() {
				if (getModel().getObject().getParent()==null)
					return "tree-node root-node";
				int count=getModel().getObject().getParent().getChildCount();
	 			if (count>(index+1))
						return "tree-node middle-node";
				return "tree-node last-node";
			}
		}));
		
 
		WorkingIndicatorAjaxLinkV5 expander = new WorkingIndicatorAjaxLinkV5("expander") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new TreeNodeLabelClickEvent<T>(target, TreeNodeLabelPanel.this.getModel()));
			}

			@Override
			public MarkupContainer setDefaultModel(IModel model) {
				return null;
			}
		};
		
		labc.add(expander);
		
		WebMarkupContainer ei = new WebMarkupContainer("expander-icon");
		
		ei.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return isExpanded()?"far fa-minus":"far fa-plus";
			}
		}));
		
		
		expander.add(ei);
		
		Label label = new Label("label", getLabel());
		labc.add(label);
		
		//---	
		// Menu
		// Image
		// Link
		//---
	}

	//public void setExpanded(boolean b) {
	//	this.expanded=b;
	//}

	@SuppressWarnings("unchecked")
	public boolean isExpanded() {
		// return this.expanded;
		return ((TreeNodePanel<T>) getParent()).isExpanded();
	}


	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	
	public IModel<T> getModel() {
		return model;
	}

	public void setModel(IModel<T> model) {
		this.model = model;
	}

}
