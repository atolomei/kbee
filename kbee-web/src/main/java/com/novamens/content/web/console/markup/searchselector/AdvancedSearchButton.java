package com.novamens.content.web.console.markup.searchselector;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.console.browser.TopPanelEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;


public class AdvancedSearchButton<T> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;

	private boolean is_up = false;
	private String key;
	
	public AdvancedSearchButton(String id, String key) {
		super(id);
		setOutputMarkupId(true);
		
		this.key=key;
		
		String top_preference = getPreference("toppanel");
		
		if (top_preference!=null && !"none".equals(top_preference))
			is_up=false;
		else
			is_up=true;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	protected String getPreference(String name) {
		return getSessionUser().getService(PreferencesService.class).getValue( key + "-browser", name);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WorkingIndicatorAjaxLinkV5<Void> ln = new WorkingIndicatorAjaxLinkV5<Void>("advancedsearch",  new StringResourceModel("advancedsearch", this, null).getString()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				is_up=!is_up;
				target.add(AdvancedSearchButton.this);
				fire(new TopPanelEvent(target));
			}
			
			@Override
			public String getWorkingLabel() {
				return new StringResourceModel("working", this, null).getString();
			}
		};
		
		add(ln);
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		icon.add( new AttributeModifier("class", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				if (is_up)
					return "far fa-angle-down";
				else
					return "far fa-angle-up";
			}
		}));
		
		add(icon);
				
	}
}
