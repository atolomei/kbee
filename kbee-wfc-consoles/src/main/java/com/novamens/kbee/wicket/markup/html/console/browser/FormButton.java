package com.novamens.kbee.wicket.markup.html.console.browser;


import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.StringResourceModel;

import kbee.web.console.BaseBrowser;

public class FormButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	public FormButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("link")==null) {
			addLink();
		}
	}
	
	protected void addLink() {
		
		AjaxLink<Void> lnk = new AjaxLink<Void>("link") {
			
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				FormButton.this.onClick(target);
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
					
					// fa fa-list-alt
					
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-filter\"/>"+"';";
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
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw  spinning\"></i>'";
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
		
		add(lnk);
	}
	
	@Override
	protected String getAnchorTitle() {
		return new StringResourceModel("report-parameters", FormButton.this, null).getObject();
	}
	
	protected void onClick(AjaxRequestTarget target) {}
}
