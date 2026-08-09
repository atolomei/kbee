package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;

import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;

public abstract class AddButtonToolbarItem extends ToolbarItem {

	private static final long serialVersionUID = 1L;

	
	public AddButtonToolbarItem(BaseBrowser<?> browser, Align align) {
			this(browser, align, null);
	}
	
	public AddButtonToolbarItem(BaseBrowser<?> browser, Align align, IModel<String> label) {
		super(browser, align);
		
		setOutputMarkupId(true);
		
		this.label=label;
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(AddButtonToolbarItem.this);
			}
		});
	
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("link")==null) {
			addLink();
		}
	}
	
	protected IModel<String> getIconCss() {
		return null;
	}
	
	protected void addLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddButtonToolbarItem.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return AddButtonToolbarItem.this.isEnabled();
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"  " + (getIconCss()!=null?getIconCss().getObject():"fal fa-plus") +"\"></i> ";
						if (AddButtonToolbarItem.this.getLabel()!=null &&  AddButtonToolbarItem.this.getLabel().getObject()!=null) {
							Label label = (Label) AddButtonToolbarItem.this.get("link:label");
									if (label!=null && label.isVisible()) {
											s1 += "<span class=\""+getLabelCss()+"\">" + getLabel().getObject()+" </span>";
									}
						}
						s1 += "';";
						s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw spinning\" ></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
			
		};
		
		Label label = new Label("label", getLabel());
		label.setEscapeModelStrings(false);

		link.add(label);
		add(link);
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		
		if (getIconCss()!=null)
			icon.add(new AttributeModifier("class", getIconCss()));
		else
			icon.setVisible(false);
		
		link.add(icon);
		
	}


	protected String getLabelCss() {
		return "label";
	}

	
	private IModel<String> label;
	private String label_property;
	
	protected void  setLabel(IModel<String> label) {
		this.label=label;
	}
	
	
	protected IModel<String> getLabel() {
		if (label!=null) 
			return label;
		if (label_property!=null)
			return new StringResourceModel(label_property, this, null);
		return new StringResourceModel("label", this, null);
	}

	@Override
	public boolean isEnabled() {
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	public abstract void onClick(AjaxRequestTarget target);
	
	
	public String getBeforeClick() {
		return null;
	}
	
	public String getWorkingLabel() {
		return null;
	}

	
	
}
