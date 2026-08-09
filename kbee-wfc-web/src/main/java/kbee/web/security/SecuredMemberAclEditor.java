package kbee.web.security;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.security.IQLRule;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.security.AclRow.PermissionValue;

@SuppressWarnings("serial")
public class SecuredMemberAclEditor extends ObjectEditor<DataSetMember> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(SecuredMemberAclEditor.class.getName());
	
	final boolean role_admin = 
		ServiceLocator.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	final boolean role_model = role_admin || 
		ServiceLocator.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	final boolean role_dataset_members = role_model || role_admin || 
		ServiceLocator.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	
	private static String ShowWorkflowPermission =
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.security.workflow", "true");
	
	private IModel<Acl> aclmodel;
	private AclEditor acleditor;
	private boolean readOnly = false;
				
	public class AclEditor extends ObjectEditor<Acl> {
		public AclEditor() {
			super("editor", getAclModel());
		}
		@Override
		public boolean isEditionEnabled() {
			return SecuredMemberAclEditor.this.isEditionEnabled();
		}
		@Override
		public void setUpdatedPart(String updatedPart) {
			SecuredMemberAclEditor.this.setUpdatedPart(updatedPart);
		}
	}

	public SecuredMemberAclEditor(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public SecuredMemberAclEditor(String id, IModel<DataSetMember> model) {
		this(id, model, false);
	}
	
	public SecuredMemberAclEditor(String id, IModel<DataSetMember> model, boolean readOnly) {
		super(id, model);
		setOutputMarkupId(true);
		setEditionEnabled(false);
		this.readOnly = readOnly;
	}
	
	public ObjectEditor<Acl> getAclEditor() {
		return acleditor;
	}
	
	public IModel<Acl> getAclModel() {
		return aclmodel;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setAclModel(new IModel<Acl>() {
			public Acl getObject() {
				return (Acl)((SecuredMember)getModelObject()).getSecurityRule().getAcl();
			}
			public void setObject(Acl acl) {
			}
			public void detach() {
			}
		});
		
		setAclEditor(new AclEditor());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		try {
			AclEditorPanel pa = new AclEditorPanel(getAclEditor()) {
				@Override  
				public boolean usersEnabled() {
					return true;
				}
				@Override
				public IModel<String> getTitle() {
					return new Model<String>() {
						public String getObject() {
							return isInheritedAcl()
									? getLabel("inherited-permissions").getObject()
									: getLabel("local-permissions").getObject();
						}
					};
				}
				@Override
				public boolean isUsersEnabled() {
					return false;
				}
				@Override
				public boolean isReadOnly() {
					return readOnly;
				}
				@Override
				protected List<AclRow> getRows(Acl acl) {
					return SecuredMemberAclEditor.this.getRows(acl);
				}
				@Override
				protected IModel<String> getTargetLabel() {
					return new Model<String>(SecuredMemberAclEditor
						.this.getModelObject()
						.getDataSet()
						.getDisplayName());
				}
				@Override
				protected List<Permission> getPermissions() {
					return SecuredMemberAclEditor.this.getPermissions();
				}
				@Override
				protected boolean showWorkflowPermissions() {
					return SecuredMemberAclEditor.this.showWorkflowPermissions();
				}

			};
			
			form.add(pa);
		
		} 
		catch (Exception e) {
			logger.error(e);
			form.add( new ErrorPanel("acl-editor", e));
		}
		
		add(form);
		
		add(new EditButtonsV5<DataSetMember>(this) {
			@Override
			public boolean isVisible() {
					if (getModelObject().getDataSet().isReadonly())
						return isRoot();
					if (getModelObject().getState()==ObjectState.DELETED)
						return false;
					if (isReadOnly())
						return false;
					if (isSupportSessionUser() && !isRoot())
						return false;
					if (role_dataset_members)
						return true;
					if (!isWriteable(getModelObject()))
						return false;
					if (!isDeleteable(getModelObject()))
						return false;
					return true;
			}
			@Override
			public boolean isEnabled()  {
				return true;
			}
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
		});		
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				IQLRule rule = (IQLRule)((SecuredMember)getModelObject()).getSecurityRule();
				ServiceLocator.getService(SecurityContentMgmtService.class).update(rule, getUpdatedParts());
				getModelObject().getService(DOMObjectService.class).update("Permissions");
				target.add(get("form"));
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}
	}
	
	protected boolean isInheritedAcl() {
		return  !getModelObject().getParents().isEmpty() &&
			getAclModel().getObject().getEntries().isEmpty();
	}
	
	protected List<Permission> getPermissions() {
		List<Permission> permissions = new ArrayList<>();
		permissions.add(KbeePermission.READ);
		permissions.add(KbeePermission.WRITE);
		permissions.add(KbeePermission.DELETE);
		permissions.add(KbeePermission.CHILDS);
		return permissions;
	}
	
	protected ArrayList<AclRow> getRows(Acl acl) {
		ArrayList<AclRow> rows = new ArrayList<>(); 
		if (acl==null) return rows;
		List<AclEntry> entries = acl.getEntries();
		if (entries.isEmpty()) {
			KbeeAcl inheritedAcl = (KbeeAcl)((SecuredMember)getModelObject()).getInheritedAcl();
			entries = inheritedAcl.getEntries();
		}
		for (AclEntry entry : entries) {
			Principal principal = (Principal)entry.getPrincipal();
			boolean negative = entry.isNegative();
			AclRow principalrow = null;
			for (AclRow row : rows) {
				if (row.getPrincipal().getId().equals(principal.getId())) {
					principalrow = row;
					break;
				}
			}
			if (principalrow==null) {
				principalrow = new AclRow(principal);
				rows.add(principalrow);
			}
			Enumeration<Permission> entrypermissions = entry.permissions(); 
			while (entrypermissions.hasMoreElements()) {
				Permission entrypermission = (Permission)entrypermissions.nextElement();
				if (negative)
					principalrow.setValue(entrypermission, PermissionValue.DENIED);
				else
					principalrow.setValue(entrypermission, PermissionValue.GRANT);
			}
		}
		return rows;
	}
	
	protected boolean isRoot() {
		return ServiceLocator
			.getService(com.novamens.service.SecurityService.class)
			.isRoot(getSessionUser());
	}
	
	protected boolean isWriteable(DataSetMember member) {
		return ServiceLocator
			.getService(UserService.class)
			.isWriteable(member);
	}
	
	protected boolean isDeleteable(DataSetMember member) {
		return ServiceLocator
			.getService(UserService.class)
			.isDeleteable(member);
	}
	
	protected boolean isSupportSessionUser() {
		return ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean showWorkflowPermissions() {
		return "true".equals(ShowWorkflowPermission) || role_admin;
	}
	
	protected User getSessionUser() {
		return ServiceLocator
			.getService(SecurityService.class)
			.getSessionUser();
	}
	
	private void setAclModel(IModel<Acl> model) {
		this.aclmodel = model;
	}
	
	private void setAclEditor(AclEditor editor) {
		this.acleditor = editor;
	}
}