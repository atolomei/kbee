package com.novamens.content.web.security.markup;

import org.apache.wicket.Page;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.page.ApplicationMenuSection;

@SuppressWarnings("serial")
public class GroupsPage extends ConsolePage<Group> {

	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	
	public GroupsPage(PageParameters parameters) {
		setPagePreferences();
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
	}
	
	public GroupsPage() {
		setPagePreferences();
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
	}
	
	public GroupsPage(Query query) {
		super(query);
		setPagePreferences();
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
	}

	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}
	
	@Override
	protected String getTipCategory() {
		return Tip.SECURITY;
	}

	@Override
	public Console<Group> newConsole(Query query) {
		return new GroupsConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return GroupsPage.this.getConsolePage(query, index);
			}
		};
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new GroupsPage(query);
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected boolean isExpressVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	@Override
	public boolean hasPermissions() {
		return (isExpressVersion() && is_root)  || is_domain_admin || is_root || is_security || is_support; 
	}
	
	private void setPagePreferences() {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "security",  getClass().getSimpleName());
	}

}
