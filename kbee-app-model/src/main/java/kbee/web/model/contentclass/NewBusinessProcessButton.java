package kbee.web.model.contentclass;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.workflow.Procedure;

@SuppressWarnings("serial")
public abstract class NewBusinessProcessButton extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	//final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_admin	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public NewBusinessProcessButton(String id) {
		super(id);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new BusinessProcessFactoryPanel("new-business-process") {
			@Override
			public boolean isEnabled() {
				if (is_root)
					return true;
				if (is_admin)
					return true;
				return false;
			}
			protected void onCreate(Procedure procedure,AjaxRequestTarget target) {
				NewBusinessProcessButton.this.onCreate(procedure, target);	
			}
		});
	}
	
	protected abstract void onCreate(Procedure procedure, AjaxRequestTarget target);
}