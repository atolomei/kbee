package com.novamens.content.web.security.markup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;
import kbee.web.event.wicket.ClickSetGroupEvent;

				
public class UsersBatchSetGlobalPermissionButton extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(UsersBatchSetGlobalPermissionButton.class.getName());
	
	public UsersBatchSetGlobalPermissionButton(BaseBrowser<Person> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(UsersBatchSetGlobalPermissionButton.this);
			}
		});
	}

	public void close(AjaxRequestTarget target) {
		target.add(getPage());
	}

	
	@Override
	public boolean isEnabled() {
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (get("set-global-permission-modal")==null) {
			add(new AjaxLink<Void>("link") {
				private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
					fire(new ClickSetGroupEvent(target));
				}
			});
		}
	}
	

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}


	protected UserProfile getSessionUserProfile() {
		return getContentDao().findUserProfileByUser(getSessionUser());
	}


	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getSessionUser() );
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
}
