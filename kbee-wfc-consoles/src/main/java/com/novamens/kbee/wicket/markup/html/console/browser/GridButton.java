package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.event.GeneralAjaxWicketEvent;

import kbee.web.console.BaseBrowser;
import kbee.web.console.Console;

@SuppressWarnings("serial")
public class GridButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;
	
	public GridButton(Console<?> console, Align align) {
		super(console, align);
	}
	
	public GridButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		super.setOutputMarkupId(true);
		addLink();
	}
	
	protected void addLink() {
		AjaxLink<Void> lnk = new AjaxLink<Void>("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new GeneralAjaxWicketEvent(target, "grid-browser"));
				target.add(GridButton.this);
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
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"fal fa-table-rows\"></i>"+"';";
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
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\""+com.novamens.wicket.markup.html.form.Form.SPINNING + " fa-fw \" ></i>";
						s +="';";
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
		
		lnk.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return "grid".equals(getBrowser().getBrowserType()) || "list".equals(getBrowser().getBrowserType()) ? "btn-mini enabled" : "btn-mini";
			}
		}));
		
		lnk.add(new AttributeModifier("title", getLabel("label")));
	}	
}
