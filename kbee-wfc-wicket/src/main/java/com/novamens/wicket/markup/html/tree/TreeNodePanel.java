package com.novamens.wicket.markup.html.tree;

import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.event.TreeNodeLabelClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.model.ModelPanel;
 
/**
 * 	 TreNode
 * 
 *   Label
 *   Children 
 */
public class TreeNodePanel<T extends TreeNode> extends  ModelPanel<T> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TreeNodePanel.class.getName());
	
 	private static final long serialVersionUID = 1L;
	
	IModel<T> model;
	int index = 0;
	private boolean isExpanded = false;

	public TreeNodePanel(String id, IModel<T> model) {
		super(id, model);
		setModel(model);
		setOutputMarkupId(true);
		addListeners();
	}

	public TreeNodePanel(String id, IModel<T> model, int index) {
		super(id, model);
		setModel(model);
		this.index=index;
		setOutputMarkupId(true);
		addListeners();
	}
	
	public boolean isLastChild() {
		if (getModel().getObject().getParent()==null)
			return true;
		return getModel().getObject().getParent().getChildCount()<=(index+1);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		TreeNodeLabelPanel<T> label=new TreeNodeLabelPanel<T>("label", getModel(), this.index);
		add(label);

		if (getModel().getObject().isLeaf())
			setExpanded(false);
		
		TreeNodeChildrenPanel<T> childs = new TreeNodeChildrenPanel<T>("children", getModel()) {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return isExpanded();
				}
		};
			
		childs.add(new AttributeModifier("class", new Model<String>() {
 			private static final long serialVersionUID = 1L;
			public String getObject() {
			if(TreeNodePanel.this.isLastChild())
				return "last-child";
			return"middle-child";
			}
		}));
		add(childs);
		
	}

	
	
	public boolean isExpanded() {
		return isExpanded;
	}

	protected void setExpanded(boolean b) {
		 isExpanded=b;
	}

	@Override
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


	protected void addListeners() {
	 	
		add(new WicketEventListener<TreeNodeLabelClickEvent<T>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean handle(com.novamens.event.Event event) {
			
				if (!super.handle(event))
					 return false;
					 
				 if (!(event instanceof TreeNodeLabelClickEvent))
					 return false;
				 
				 @SuppressWarnings("unchecked")
				TreeNodeLabelClickEvent<T> e=(TreeNodeLabelClickEvent<T>) event;
				 
				 logger.debug( String.valueOf(getModelObject().hashCode()) +" " + (getModelObject().equals(e.getModelObject())?"yes":"no"));
				 
				 return (getModelObject().equals(e.getModelObject()));
			}
			
			
			@Override
			public void onEvent(TreeNodeLabelClickEvent<T> event) {
				
				if ( event.getModelObject().equals(getModelObject())
					) {
					setExpanded(!isExpanded());
					logger.debug("Action: " +(isExpanded()?"expand": "collapse")+" "+ String.valueOf(getModel().getObject().hashCode()));
					event.getRequestTarget().add(TreeNodePanel.this);
				}
				else
					logger.debug("No action");
			}
		});
	}


}
