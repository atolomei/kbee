package com.novamens.content.web.console.markup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.console.grid.LabelTagPanel;

public class SelectionTitleColumnPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	static final String PROPERTY_UNREAD = "unread";

	static private Logger logger = LogManager.getLogger(SelectionTitleColumnPanel.class.getName());

	@SuppressWarnings("serial")
	public SelectionTitleColumnPanel(String id, IModel<T> model) {
		super(id, model);
		
		AjaxLink<?> link = new AjaxLink<Void>("title-link") {
			public void onClick(AjaxRequestTarget target) {
				SelectionTitleColumnPanel.this.onClick(target, SelectionTitleColumnPanel.this.getModel());
			}
		};
			
		link.add(new Label("title", new Model<String>() { 
			public String getObject() { 
				return getTitle(); 
			};
		}));
				
		if (getCss()!=null) {
			((Label) link.get("title")).add(new AttributeModifier("class", getCss()));
		}
			
		link.add(new WebMarkupContainer("lock-icon") { 
			public boolean isVisible() {
				return false;
			};
		});
				
				
		 WebMarkupContainer newi = new WebMarkupContainer("new-icon") { 
			public boolean isVisible() {
				return false;
			};
		};
		
		link.add(newi);
				
		add(link);
				
		try {
			add(new LabelTagPanel<T>("labels", getModel()));
		} 
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			addOrReplace(new InvisiblePanel("labels"));
		}
			
	}
	
	public String getTitle() {
		try {
			return getModelObject().getTitle();
		} 
		catch (Exception e) {
			logger.warn(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return "err";
		}
	}
	
	protected void onClick(AjaxRequestTarget target, IModel<T> model) {
		
	}
	
	protected String getCss() {
		return null;
	}
}
