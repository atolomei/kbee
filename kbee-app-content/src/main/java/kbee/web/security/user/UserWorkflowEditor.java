package kbee.web.security.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import org.apache.wicket.model.PropertyModel;

import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class UserWorkflowEditor extends DomainObjectEditor<UserProfile> {
	private static final long serialVersionUID = 1L;
				
	private static Logger logger = Logger.getLogger(UserWorkflowEditor.class.getName());

	final boolean is_root = ServiceLocator
		.getService(SecurityService.class)
		.isRoot(); 
	final boolean role_admin = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());
	
	private Boolean is_active;
	
	public UserWorkflowEditor(String id, IModel<UserProfile> model, final boolean ismyaccount) {
		super(id, model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);

		setActive(Boolean.valueOf(model.getObject().getUser().isActive()));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		add(form);
								
		form.add(new BooleanField("active", new PropertyModel<Boolean>(this, "active")));
		
		add(new EditButtonsV5<UserProfile>(this) {
			@Override
			public boolean isVisible() {
				if (UserWorkflowEditor.this.getModel().getObject().getEntity().getState()==ObjectState.DELETED)
					return false;
				if  (getModelObject().getUser().getUserName().startsWith("root@"))
					return getSessionUser().getUserName().startsWith("root@");
				if (!role_security)
					return false;
				return true;
			}
		});
	}
	
	public Boolean isActive() {
		return this.is_active;
	}
	
	public void setActive(Boolean b) {
		this.is_active=b;
	}
	

	

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModelObject().getUser().setActive(isActive());
				List<String> list = new ArrayList<String>();
				list.add("Workflow Status: " + (getModelObject().getUser().isActive()?" Active " : " Inactive"));
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject().getUser(), list);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
}
