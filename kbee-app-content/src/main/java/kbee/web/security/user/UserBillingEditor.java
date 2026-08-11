package kbee.web.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

public class UserBillingEditor extends DomainObjectEditor<UserProfile> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserEditor.class.getName());
	
	
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_external = !is_root && !is_domain_admin && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.EXTERNAL_USER.getId());

	/**
	 * 
	 * 
	 * 
	 */
	public UserBillingEditor(String id, IModel<UserProfile> model, final boolean ismyaccount) {
		super(id, model);

		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		BooleanSwitchField isc=new BooleanSwitchField("isclient", new PropertyModel<Boolean>(this, "client")) {
			@Override
			public boolean isEnabled() {
				return isRoot();
			}
		};
		isc.setBorder(true);
		form.add(isc);

		
		add(form);

		EditButtonsV5<UserProfile> buttons = new EditButtonsV5<UserProfile>(this) {
			
			@Override
			public boolean isVisible() {
				return is_root;
			}
			
			@Override
			public boolean isEnabled() {
				return is_root;
			}
		};
		
		add(buttons);
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());

				// AjaxWicketEvet to refresh screen
				//
				fire(new EditEvent<UserProfile>(target, getModel()));
			}
		} catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
		}
	}

	public void setClient(boolean b) {
		((KbeeUserProfile) getModelObject()).setClientProfile(true);
	}
	
	public boolean isClient() {
		return getModelObject().isClientProfile();
	}

	
}
