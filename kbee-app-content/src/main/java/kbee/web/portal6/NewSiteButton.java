package kbee.web.portal6;


import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;

public class NewSiteButton extends ToolbarItem {

	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class)
			.isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();

	private static final long serialVersionUID = 1L;

	public NewSiteButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);

	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new SiteFactoryPanel("new-site"));
	}

}
