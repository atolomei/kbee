package com.novamens.content.web.editor.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class ExpandableObjectEditorPanel<T> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;

	WebMarkupContainer membersBlock;
	private int quantity = 0;
	private boolean expanded = true;
	private boolean showQuantity;
	
	private IModel<String> title;
	
	public IModel<String> getTitle() {
		return title;
	}

	public void setTitle(IModel<String> title) {
		this.title=title;
	}
	
	protected Component getComponent(String id) {
		return membersBlock.get(id);
	}
	
	public ExpandableObjectEditorPanel(String id) {
		this(id, false);
	}
	
	public ExpandableObjectEditorPanel(String id, final boolean showQuantity) {
		this(id, showQuantity, false);
	}
	
	public ExpandableObjectEditorPanel(String id, final boolean showQuantity, final boolean isText) {
		super(id);
		
		this.showQuantity = showQuantity;
		
		AjaxLink<Void> expander = new AjaxLink<Void>("expander") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (isExpanded())
					colapse();
				else
					expand();
				onExpanderClick(target);
			}
		};
		
		Label expanderButton = new Label("expander-button", new Model<String>() {
			public String getObject() {
				return isExpanded()?"-":"+";
			}			
		}) {
			public boolean isVisible() {
				return !isExpanded();
			}
		};

		expander.add(new Label("title", new Model<String>() {
			@Override
			public String getObject() {
				return getTitle().getObject();
			}
		}));			
			
		expander.add(new Label("quantity", new Model<String>() {
			public String getObject() {
				return "("+String.valueOf(quantity)+")";
			}
		}) 	{
			public boolean isVisible() {
				return showQuantity();
		}});
			
		expander.add(expanderButton);
		add(expander);
			
									
		membersBlock = new WebMarkupContainer("members-block") {
			@Override
			public boolean isVisible() {
				return ExpandableObjectEditorPanel.this.isExpanded();
			}
		};

		if (isText)
			membersBlock.add(new AttributeModifier("class", "members-block textblock"));
		else
			membersBlock.add(new AttributeModifier("class", "members-block"));
			
		add(membersBlock);
		
		add(new AttributeModifier("class", new Model<String>("classexpander") {
			@Override
			public String getObject() {
				return ExpandableObjectEditorPanel.this.isExpanded()?"field-block expanded":"field-block colapsed";
			}
		}));
	}
	
	public int getQuantity() 		{return quantity;}
	public void setQuantity(int n) 	{quantity=n;}
	public void incrementQuantity() {quantity++;}
	public void decrementQuantity() {quantity--;}
	
	protected void colapse() {
		expanded=false; 
	}
	
	protected void expand() {
		expanded=true; 
	}

	protected boolean isExpanded() {
		return expanded; 
	}
	
	public void addToExpandablePanel(Component component) {
		membersBlock.add(component);
	}
	
	protected void onExpanderClick(AjaxRequestTarget target) {}
	
	private boolean showQuantity() {
		return showQuantity;
	}
}
