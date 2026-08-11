package kbee.web.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import com.novamens.content.notes.Billboard;
import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;
import kbee.web.error.ApplicationErrorPage;

public class NewDomainButton extends ToolbarItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger( NewDomainButton.class.getName());
	
	final boolean is_root		  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_support	  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_domain_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_factory_admin	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());	
			
	public NewDomainButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("new") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return is_domain_admin || is_support || is_factory_admin;
			}
			
			@Override
			public boolean isEnabled() {
				return is_domain_admin || is_factory_admin;
			}
			
			@Override
			public void onClick() {
				try {
					setResponsePage(new DomainCreationPage());
				}
				catch (Exception e) {
					logger.error(" {} | {} | {} | {}", (getSessionUser()!=null?getSessionUser().getUserName():"null"), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
					setResponsePage( new ApplicationErrorPage<Billboard>(new Model<String>(e.getClass().getSimpleName()), new Model<String>(getBrowser().getConsoleKey())));
				}
			};
		});
	}

	private User getSessionUser() {
		return  ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}


}
