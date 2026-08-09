package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.googlecode.wicket.jquery.core.panel.LabelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;



@SuppressWarnings("serial")
public abstract class AbstractLinkMenuItemPanelV5<T> extends AbstractMenuItemPanelV5<T> {

	private static final long serialVersionUID = 1L;
	
	static public final String CHECK = "far fa-check";
	static public final String CHECK_LIGHT = "fal fa-check";
	
	WebMarkupContainer context;
	WebMarkupContainer mc;
	WebMarkupContainer zzz;
	
	private IModel<String> html_title;

	AbstractLink link;
	
	
	boolean is_expanded  = false;
	
	public AbstractLinkMenuItemPanelV5(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	public AbstractLinkMenuItemPanelV5(String id, IModel<T> model, final String iconcss) {
		super(id, model, iconcss);
		setOutputMarkupId(true);
	}
		
	public AbstractLinkMenuItemPanelV5(String id, final String iconcss) {
		super(id, iconcss);
		setOutputMarkupId(true);
	}
	

	protected void addContextualPanel() {
		
			
		if (!isContextualHelp()) {
			mc.add(new InvisiblePanel("contextual-help"));
			add(new InvisiblePanel("contextual-help-detail-container"));
			return;
		}
		
		context = new WebMarkupContainer ("contextual-help");
		context.setOutputMarkupId(true);
		
		
		zzz = new WebMarkupContainer ("contextual-help-detail-container");
		zzz.setOutputMarkupId(true);
		add(zzz);
		
		AjaxLink<T> cl=new AjaxLink<T>("contextual-help-link", getModel()) {

			@Override
			public void onClick(AjaxRequestTarget target) {
			
				 if (is_expanded) {
					 zzz.addOrReplace(new InvisiblePanel("contextual-help-detail"));
				 }
					
				 else {
				Panel panel=AbstractLinkMenuItemPanelV5.this.getContextualDetailPanel();
				if (panel!=null) 
					zzz.addOrReplace(panel);
				 }
				 is_expanded = !is_expanded;
				 target.add(zzz);
				 target.add(context);
			}
			
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setEventPropagation(EventPropagation.STOP); 
			}
			
		};
		
		
		context.add(cl);
		mc.add(context);
		
		zzz.add(new InvisiblePanel("contextual-help-detail"));
		
		
			
	}
	

	
	protected boolean isContextualHelp() {
		return false;
		
	}
	
	
	protected Panel getContextualDetailPanel() {
		return new LabelPanel("contextual-help-detail", new Model<String>("method getContextualDetailPanel must be overriden"));
	}

	
	
 	
	
	
	public AbstractLink getLink() {
		return link;
	}
	
	protected void addComponents() {

		mc = new WebMarkupContainer("lcontainer");
		mc.setOutputMarkupId(true);
		add(mc);
		
		 link = getNewLink("item-link");
		
		
		if (getHTMLTitle()!=null)
			link.add(new AttributeModifier("title", getHTMLTitle()));
		
		
		WebMarkupContainer w = new WebMarkupContainer ("item-icon") {
			public boolean isVisible() {
				return  getIconCssClass()!=null;
			}
		};
		link.add(w);
		
		w.add(new AttributeModifier("class", new Model<String>() {
				@Override
				public String getObject() {
					return getIconCssClass()!=null?getIconCssClass():"";
				}
			}));

		Label label = new Label("item-label", new Model<String>() {
			public String getObject() {
				return AbstractLinkMenuItemPanelV5.this.getLabel();
			}
		});
		
		if (isEscapeModelString()) 
			label.setEscapeModelStrings(true);
		else
			label.setEscapeModelStrings(false);
		
		link.add(label);
		mc.add(link);
		
		addContextualPanel();
	}
	
	public void setHTMLTitle(IModel<String> s) {
		this.html_title=s;
	}
	
	public IModel<String> getHTMLTitle() {
		return this.html_title !=null ?	this.html_title : new Model<String>( getLabel());
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addComponents();
	}
	
	
	protected abstract AbstractLink getNewLink(String id);
	
	protected boolean isEscapeModelString() {
		return false;
	}
	
	protected IModel<String> getItemLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected IModel<String> getItemLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this, null);
		model.setParameters((Object[])parameter);
		return model;
	}
	
	protected String getItemLabelString(String key) {
		return getItemLabel(key).getObject();
	}
	
	protected String getItemLabelString(String key, String... parameter) {
		return getItemLabel(key, parameter).getObject();
	}
}

 