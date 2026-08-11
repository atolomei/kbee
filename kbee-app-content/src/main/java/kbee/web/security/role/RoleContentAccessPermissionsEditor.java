package kbee.web.security.role;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.EntitySet;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.security.AclRow;
import kbee.web.security.AclRow.PermissionValue;
			

@Deprecated
public class RoleContentAccessPermissionsEditor extends DomainObjectEditor<Role> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RoleContentAccessPermissionsEditor.class.getName());

	final boolean role_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	boolean updated = false;
	private AclRow row;
	
	public RoleContentAccessPermissionsEditor(IModel<Role> model) {
		this("editor", model, false);
	}
	
	public RoleContentAccessPermissionsEditor(String id, IModel<Role> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		setRow(getModelObject());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(KbeePermission.READ);
		permissions.add(KbeePermission.WRITE);
		permissions.add(KbeePermission.DELETE);
		permissions.add(KbeePermission.PRIVATE);
									
		Panel tab = new PermissionsTab<User>("content-permissions", 
				new ObjectModel<User>(getSessionUser()),
				RoleContentAccessPermissionsEditor.this, 
				row, permissions) {
			
			
			private static final long serialVersionUID = 1L;
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					RoleContentAccessPermissionsEditor.this.updated = true; 
				}
		};
		
		
		form.add(tab);
		
				
		add(form);
		
		form.add(new EditButtonsV5<Role>(this) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
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

	public void edit(AjaxRequestTarget target) {
		super.edit(target);
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
	}

	/**
	 * 
	 */
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeAbstractRole role = getRole();
				ServiceLocator.getService(SecurityContentMgmtService.class).update(role, getUpdatedParts());
				super.reset();
				target.add(RoleContentAccessPermissionsEditor.this.getPage());
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
	
	
	
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getDataSet() instanceof EntitySet) {
				classifiers.add(classifier);
			}
		}
		return classifiers;
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	private void setRow(Role role) {
		AclRow row = new AclRow(null); 
		if (role!=null) {
			for (Permission permission : ((KbeeAbstractRole)role).getPermissions()) {
				row.setValue(permission, PermissionValue.GRANT);
			}
			for (Permission permission : ((KbeeAbstractRole)role).getNegativePermissions()) {
				row.setValue(permission, PermissionValue.DENIED);
			}
		}	
		this.row = row;
	}

}
