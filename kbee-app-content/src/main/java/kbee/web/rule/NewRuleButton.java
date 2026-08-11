package kbee.web.rule;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;
import kbee.web.console.BaseBrowser;
import kbee.web.error.ApplicationErrorPage;

@SuppressWarnings("serial")
public abstract class NewRuleButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	static Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(NewRuleButton.class.getName()));
	
	final boolean is_root		  = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_support	  = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_domain_admin = is_root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	public NewRuleButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("new-rule") {
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
					NewRuleButton.this.onClick();
				}
				catch (Exception e) {
					logger.error(e);
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
}

