package kbee.web.source;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;
import kbee.web.error.ApplicationErrorPage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class ToolBarButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ToolBarButton.class.getName());

	final boolean is_root		  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_support	  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_domain_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	private String label;

	public ToolBarButton(BaseBrowser<?> browser, Align align, String label) {
		super(browser, align);
		this.label = label;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		Link<Void> link = new Link<Void>("button-link") {
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
					ToolBarButton.this.onClick();
				} catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Group>(new Model<String>(e.getClass().getSimpleName()), new Model<String>(getBrowser().getConsoleKey())));
				}
			}

			;
		};
		add(link);
		link.add(new Label("button-label", label));
	}

	/**
	 * Must be overriden 
	 */
	protected abstract void onClick();
}

