package com.novamens.content.web.nav.markup;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class NavBarSettingsMenu extends Panel {

	private static final long serialVersionUID = 1L;
								
	final boolean role_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model    = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	// we (temporarily) let information_model roles to manage dataset values because some older domains may no have this role
	//
	final boolean role_dataset_members 	= role_model || role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_security			= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_root				= ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_support 			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	public NavBarSettingsMenu(String id) {
		super(id);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						// DomainPage
						PageParameters pa= new PageParameters();
					    pa.add("id", getDomain().getId().toString());
						setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-generalsettings-page"));

						
						
					}
					@Override
					public String getLabel() {
						return (new ResourceModel("mainmenu.settings")).getObject();
					}
					@Override
					public boolean isEnabled() {
						return role_admin || role_support;
					}
				};
			}
		});

		add(menu);
	}

	protected Domain getDomain() {
		return  ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	protected String getUserPreference(String key) {
		KbeeUser user = getUser();
		if (user!=null) {
			return user.getService(PreferencesService.class).getValue( "settings", key);
		}	
		return null;
	}
	

}
