package com.novamens.content.web.security.markup;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;
import kbee.web.error.ApplicationErrorPage;
			
public abstract class NewGroupButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(NewGroupButton.class.getName());
	
	final boolean is_root		  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_support	  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_domain_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	public NewGroupButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("new") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return is_domain_admin || is_support;
			}
			
			@Override
			public boolean isEnabled() {
				if (is_support && !is_root)
					return false;
				return true;
			}
			
			@Override
			public void onClick() {
				try {
					NewGroupButton.this.onClick();
				}
				catch (Exception e) {
					logger.error(" {} | {} | {} | {}", (getSessionUser()!=null?getSessionUser().getUserName():"null"), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					setResponsePage( new ApplicationErrorPage<Group>(new Model<String>(e.getClass().getSimpleName()), new Model<String>(getBrowser().getConsoleKey())));
				}
			};
		});
	}

	
	/**
	 * Must be overriden 
	 */
	protected void onClick() {
		
	}
	

	private User getSessionUser() {
		return  ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}

}







