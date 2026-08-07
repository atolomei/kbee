package com.novamens.content.web.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class ExpandablePanel extends Panel {
	private static final long serialVersionUID = 1L;
	private boolean expanded = true;
	private IModel<String> title;
	
	public ExpandablePanel(String id, Panel panel, IModel<String> title) {
		super(id);
		
		setOutputMarkupId(true);
		setTitle(title);
		
		AjaxLink<Void> expander = new AjaxLink<Void>("expander") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (isExpanded())
					colapse(target);
				else
					expand(target);
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
				return "("+String.valueOf(getQuantity())+")";
			}
		})	{
			public boolean isVisible() {
				return showQuantity();
		}});
			
		expander.add(expanderButton);
		add(expander);
		
		WebMarkupContainer container = new WebMarkupContainer("panel-container") {
			public boolean isVisible() {
				return isExpanded();
			}
		};
		
		panel.setMarkupId("panel");
		container.add(panel);
		add(container);
	}
	
	public IModel<String> getTitle() {
		return title;
	}
	
	public void setTitle(IModel<String> title) {
		this.title = title;	
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	
	public boolean showQuantity() {
		return false;
	}
	
	public int getQuantity() {
		return 0;
	}
	
	public void colapse(AjaxRequestTarget target) {
		this.expanded = false;
		target.add(this);
	}
	
	public void expand(AjaxRequestTarget target) {
		this.expanded = true;
		target.add(this);
	}

}
