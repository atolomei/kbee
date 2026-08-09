package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.grid.GridDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;

import kbee.web.console.BaseBrowser;

public class GridDisplayModeCompactButton extends ToolbarItem {
	
	private static final long serialVersionUID = 1L;

	public GridDisplayModeCompactButton (BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("link")==null) {
			addLink();
		}
	}
	
	protected void addLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				GridDisplayModeCompactButton.this.onClick(target);
			}
			
			@Override
			public boolean isEnabled() {
				return GridDisplayModeCompactButton.this.isEnabled();
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
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<em><span class=\"far fa-th\"/></em>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 250);";
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
		

		WebMarkupContainer icon = new WebMarkupContainer("icon");
		
		icon.add( new AttributeModifier("class", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return "far fa-th";
				 
			}
		}));
		link.add(icon);
				
		
		add(link);
	}
	

	@Override
	public boolean isEnabled() {
		GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
		return (panel.getGridDisplayMode()!=GridDisplayMode.COMPACT);
	}

	public void onClick(AjaxRequestTarget target) {
		GridPanel<?> panel = (GridPanel<?>) GridDisplayModeCompactButton.this.getBrowser().getPanel(GridPanel.class);
		if (panel!=null && panel.getGridDisplayMode()!=GridDisplayMode.COMPACT) {
				panel.setGridDisplayMode(GridDisplayMode.COMPACT);
				target.add(getBrowser());
		}
	};

}
