package kbee.web.security.role;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.kbee.content.security.KbeeAbstractRole;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;

import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class UserAdminEditor extends ObjectEditor<Role> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserAdminEditor.class.getName());

	final boolean role_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	boolean enable;

	public UserAdminEditor(IModel<Role> model) {
		this("editor", model, false);
	}
	
	public UserAdminEditor(String id, IModel<Role> model, boolean isnew) {
		super(id, model);
		
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		enable = ((EntityRole)getModelObject()).enableUserAdmin();
		
		form.add(new BooleanField("enableUserAdmin") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				enable = getValue();
				target.add(form);
			}
		});
		
		form.add(new AssignableRolesEditor() {
			@Override
			public boolean isEnabled() {
				return enable;
			}
		});
				
		add(form);
		
		add(new EditButtonsV5<Role>(this) {
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled()  {
				return true;
			}
		});		
	}

	public void onClose(AjaxRequestTarget target) {
	}

	
	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
			try {
				ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModelObject());
			}
			catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.error(e);
				}
				else {
					logger.error(e);
				}	
			}
			onClose(target);
		}
		
		onCancel(target);
	}

	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeAbstractRole role = getRole();
				if (role.getAlias()==null)
					role.setAlias(role.getName().toLowerCase().trim().replace(" ", "-"));
				ServiceLocator.getService(SecurityContentMgmtService.class).update(role, getUpdatedParts());
				super.reset();
				target.add(UserAdminEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
		}
	}
	
	
	public KbeeAbstractRole getRole() {
		return (KbeeAbstractRole) getModelObject();
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onUpdate(AjaxRequestTarget target) {
		
	}
}
