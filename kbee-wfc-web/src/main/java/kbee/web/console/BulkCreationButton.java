package kbee.web.console;


import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;
import kbee.web.error.ApplicationErrorPage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class BulkCreationButton extends ToolbarItem {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BulkCreationButton.class.getName());


	final boolean is_root 		= ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_support 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_security	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_domain_admin = is_root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	public BulkCreationButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		add(new Link<Void>("button") {
			@Override
			public boolean isVisible() {
				return is_domain_admin || is_support || is_security || isUserAdmin();
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
					BulkCreationButton.this.onClick();
				}
				catch (Exception e) {
					logger.error(e);
					setResponsePage( new ApplicationErrorPage<User>(new Model<String>(e.getClass().getSimpleName()), new Model<String>(getBrowser().getConsoleKey())));
				}
			};
		});
	}

	protected void onClick() {
	}
	
	private boolean isUserAdmin() {
		return ServiceLocator.getService(UserService.class).isUserAdmin();
	}
}
