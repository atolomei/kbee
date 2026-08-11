package kbee.web.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;
import kbee.web.console.BaseBrowser;
import kbee.web.error.ApplicationErrorPage;

@SuppressWarnings("serial")
public abstract class NewUserButton extends ToolbarItem {
					
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(NewUserButton.class.getName());
	
	final boolean is_root = ServiceLocator
		.getService(SecurityService.class)
		.isRoot();
	final boolean is_support = ServiceLocator.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_security = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_federated_security	= ServiceLocator.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.FEDERATED_SECURITY.getId());
	final boolean is_domain_admin = is_root || 
		ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	public NewUserButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("new") {
			@Override
			public boolean isVisible() {
				return is_domain_admin || 
					is_support || 
					is_security; 
					//(is_federated_security && isUserAdmin());
			}
			@Override
			public boolean isEnabled() {
				if (is_support && !is_root)
					return false;
				return true;
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					NewUserButton.this.onClick(target);
				}
				catch (Exception e) {
					logger.error(e);
					setResponsePage( new ApplicationErrorPage<User>(
						new Model<String>(e.getClass().getSimpleName()), 
						new Model<String>(getBrowser().getConsoleKey())));
				}
			};
		});
	}

	protected void onClick(AjaxRequestTarget target) {
	}
	
//	private boolean isUserAdmin() {
//		return ServiceLocator.getService(UserService.class).isUserAdmin();
//	}
}
