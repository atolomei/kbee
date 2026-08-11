package kbee.web.security.role;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classifier;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public abstract class NewRoleButton extends ToolbarItem {

	private static final long serialVersionUID = 1L;
	
	final boolean is_support= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	public NewRoleButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new RoleFactoryPanel("new-role") {
			@Override
			public boolean isEnabled() {
				if (is_support && !is_root)
					return false;
				return true;
			}
			@Override
			protected void onCreate(int type, IModel<Classifier> model) {
				NewRoleButton.this.onCreate(type, model);
			}
		});
	}
	
	abstract protected void onCreate(int type, IModel<Classifier> model);
}
