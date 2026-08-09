package kbee.web.security;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.tabs.TabbedPanel4;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.security.AclRow.PermissionValue;

@SuppressWarnings( "serial")
public class AclEditorPanel extends ObjectEditorPanel<Acl> {
	private static final long serialVersionUID = 1L;
				
	private static Logger logger = Logger.getLogger(AclEditorPanel.class.getName());
																	
	private boolean read_only;
	private IModel<List<AclRow>> rowsModel;
	private boolean updated = false;
	private String property;
	
	public class RowsModel implements IModel<List<AclRow>> {
		private List<AclRow> rows;
		public List<AclRow> getObject() {
			if (rows==null) {
				rows = getRows(getModelObject());
			}
			return rows;
		}
		public void setObject(List<AclRow> rows) {
			this.rows = rows;
		}
		public void detach() {
			for (AclRow row : rows) {
				row.detach();
			}
		}	
	}

	public AclEditorPanel(Editor<Acl> editor, List<Permission> permissions) {
		super("acl-editor");

		add(new Label("title", getTitle()));

		setProperty("acl-editor");
		setEditor(editor);
		
		setRows(getModelObject());
		
		add(new AclEditorTab("tabs", editor, getRowsModel(), permissions, false) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(AclEditorPanel.this);
				AclEditorPanel.this.updated = true;
			}
		});
	}
	
	public AclEditorPanel(Editor<Acl> editor) {
		this("acl-editor", editor);
	}
	
	protected void onHelp(AjaxRequestTarget target) {
	}
	
	public boolean helpInfo() {
		return false;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info") {
			public boolean isVisible() {
				return helpInfo();
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onHelp(target);
			}
		};

		add(helpLink);
		
		IModel<String> help = getHelpText();
		
		if (help!=null && help.getObject()!=null)
			add((new Label ("help", help)).setEscapeModelStrings(false));
		else
			add((new Label ("help", "")).setVisible(false));
	}
	
	public void setProperty(String name) {
		this.property = name;
	}
	
	public String getProperty() {
		return this.property;
	}
	
	protected IModel<String> getHelpText() {
		IModel<String> model = new StringResourceModel(getProperty()+".help", AclEditorPanel.this, null);
		try {
			model.getObject();
			return model;
		}
		catch (MissingResourceException e) {
			return null;
		}
	}
	
	public boolean isUsersEnabled() {
		return true;
	}
	
	public AclEditorPanel(String id, Editor<Acl> editor) {
		super(id);
		
		setProperty(id);
		setEditor(editor);
		setRows(getModelObject());
		setOutputMarkupId(true);
		
		add(new Label("title", getTitle()));

		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new ITab() {
			public IModel<String> getTitle() {
				return getTargetLabel();
			}
			public Panel getPanel(String id) {
				List<Permission> permissions = getPermissions();
				Panel tab = new AclEditorTab(id, AclEditorPanel.this.getEditor(), getRowsModel(), permissions, isReadOnly()) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						AclEditorPanel.this.updated = true;
					}
					@Override
					public boolean isUsersEnabled() {
						 return AclEditorPanel.this.isUsersEnabled();
					}
				};
				return tab;
			}
			public boolean isVisible() {
				return true;
			}
		});
		
		Set<String> titles = new HashSet<>();
		
		if (showWorkflowPermissions()) {
			for (Procedure procedure : getProcedures()) {
				if (procedure != null) {
					final boolean enabled = true;
					final String label =  procedure.getDisplayName();
					final String procedureid = String.valueOf(procedure.getId());
					final List<Permission> procedurepermissions = getPermissions(procedure);
					ContentTemplate template = ((ContentProcedure)procedure).getContentTemplate();
					final String title = titles.contains(label) ? label + " ("+template.getName()+")" : label + " Workflow";
					titles.add(title);
						
						tabs.add(new ITab() {
							public IModel<String> getTitle() {
								return new Model<String>(title);
							}
							public Panel getPanel(String id) {
								Locale locale=getSessionUser().getLocale();
								
								List<Permission>  permissions = new ArrayList<Permission>();
								
								String monitorpermissionname = procedureid+"-"+KbeePermission.MONITOR.toString();
								KbeePermission monitorpermission = KbeePermission.valueOf(monitorpermissionname);
								monitorpermission.setLabel(KbeePermission.MONITOR.getLabel(locale));
								permissions.add(monitorpermission);
								
								String terminatepermissionname = procedureid+"-"+KbeePermission.TERMINATE.toString();
								KbeePermission terminatepermission = KbeePermission.valueOf(terminatepermissionname);
								terminatepermission.setLabel(KbeePermission.TERMINATE.getLabel(locale));
								permissions.add(terminatepermission);
	
								permissions.addAll(procedurepermissions);
								Panel tab = new AclEditorTab(id, 
										AclEditorPanel.this.getEditor(), 
										getRowsModel(), 
										permissions, 
										isReadOnly()) {
									@Override
				 					public void onUpdate(AjaxRequestTarget target) {
										super.onUpdate(target);
										AclEditorPanel.this.updated = true;
									}
								};
								return tab;
							}
							public boolean isVisible() {
								return enabled;
							}
						});
				}
			};
		}
		
		add(new TabbedPanel4("tabs", tabs) {
			@Override
			protected String getTabContainerCssClass()	{
				return "nav nav-tabs nav-stacked";
			}
			@Override
			protected String getSelectedTabCssClass() {
				return "active";
			}
		});
	}
	
	public boolean usersEnabled() {
		return true;
	}
	
	public boolean isReadOnly() {
		return read_only;
	}

	public void setReadOnly(boolean b) {
		read_only=b;
	}
	
	public IModel<String> getTitle() {
		return getLabel("permissions");
	}
	
	@Override
	public void updateModel() {
		
		Acl acl = getModelObject();
		
		acl.getId();

		if (!updated) 
			return;
		
		User caller = getSessionUser();
		
		try {
			boolean delete = true;
			while (delete) {
				delete = false;
				for (AclEntry entry : acl.getEntries()) {
					Principal principal = (Principal)entry.getPrincipal();
					boolean found = false;
					for (AclRow row : getRows()) {
						if (row.getPrincipal().getId().equals(principal.getId())) {
							if (entry.isNegative() && row.denied()) {
									found = true;
									break;
							}
							else {
								if (!entry.isNegative() && row.grants()) {
									found = true;
									break;
								}
							}
						}
					}
					if (!found) {
						acl.removeEntry(caller, entry);
						delete = true;
						break;
					}
				}
			}
		}
		catch (SecurityException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		
		try {
			for (AclRow row : getRows()) {
				Principal principal = row.getPrincipal();
				if (row.denied()) {
					AclEntry entry = null;
					for (AclEntry e : acl.getEntries()) {
						if (row.getPrincipal().getId().equals(principal.getId()) && e.isNegative()) {
							entry = (AclEntry)e;
							break;
						}
					}
					if (entry == null) {
						entry = new KbeeAclEntry(acl, principal, true);
						acl.addEntry(null, entry);
					}
					List<Permission> permissions = new ArrayList<Permission>();
					for (Permission permission : row.getPermissions()) {
						if (PermissionValue.DENIED.equals(row.getValue(permission))) {
							permissions.add(permission);
						}
					}
					entry.setPermissions(permissions);
				}
				if (row.grants()) {
					AclEntry entry = null;
					for (AclEntry e : acl.getEntries()) {
						if (((Principal)e.getPrincipal()).getId().equals(principal.getId()) && !e.isNegative()) {
							entry = (AclEntry)e;
							break;
						}
					}
					if (entry == null) {
						entry = new KbeeAclEntry(acl, principal, false);
						acl.addEntry(null, entry);
					}
					List<Permission> permissions = new ArrayList<Permission>();
					for (Permission permission : row.getPermissions()) {
						if (PermissionValue.GRANT.equals(row.getValue(permission))) {
							permissions.add(permission);
						}
					}
					entry.setPermissions(permissions);
				}
			}
			setUpdatedPart("entries");
			setRows(acl);
		}
		catch (SecurityException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.rowsModel.detach();
		if (getModel()!=null)
			getModel().detach();
	}
	
	protected boolean showWorkflowPermissions() {
		return true;
	}
	
	protected List<Permission> getPermissions() {
		List<Permission> permissions = new ArrayList<>();
		permissions.add(KbeePermission.READ);
		permissions.add(KbeePermission.WRITE);
		permissions.add(KbeePermission.DELETE);
		permissions.add(KbeePermission.AUDIT_LOG);
		permissions.add(KbeePermission.PRIVATE);
		return permissions;
	}
	
	protected IModel<String> getTargetLabel() {
		return getLabel("target-label");
	}
	
	protected void setRows(Acl acl) {
		if (this.rowsModel==null) {
			this.rowsModel = new RowsModel();
		}
		else {
			rowsModel.setObject(getRows(getModelObject()));
		}
	}
	
	protected List<AclRow> getRows() {
		return rowsModel.getObject();
	}
	
	protected IModel<List<AclRow>> getRowsModel() {
		return rowsModel;
	}
	
	protected List<AclRow> getRows(Acl acl) {
		ArrayList<AclRow> rows = new ArrayList<>(); 
		if (acl==null) return rows;
		for (AclEntry entry : acl.getEntries()) {
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
	
	protected List<Procedure> getProcedures() {
		return getDomain().getService(WorkflowDomainService.class).getProcedures();
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();	
	}
	
	
	private List<Permission> getPermissions(Procedure procedure) {
		List<Permission> permissions = new ArrayList<Permission>();
		try {
			for (Task task : procedure.getTasks()) {
				if (task.getTrigger()!=null) {
					permissions.addAll(task.getTrigger().getPermissions());
				}
				for (EndCondition action : ((KbeeTask)task).getEndConditions()) {
					if (action instanceof ManualEndCondition && ((ManualEndCondition)action).getTrigger()!=null) {
						permissions.addAll(((ManualEndCondition)action).getTrigger().getPermissions());
					}
				}
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return permissions;
	}
}