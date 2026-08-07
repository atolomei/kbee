package com.novamens.content.web.security.markup;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public abstract class NewRuleButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;
	
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	public NewRuleButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new RuleFactoryPanel("new-rule") {
			@Override
			protected void onCreate(int type) {
				NewRuleButton.this.onCreate(type);
			}
			@Override
			public boolean isEnabled() {
				if (is_support && !is_root)
					return false;
				return true;
			}
		});
	}
	
	abstract protected void onCreate(int type);
}
