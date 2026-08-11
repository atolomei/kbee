package kbee.web.security.role;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.security.Role;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.User;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.tabs.AbstractModelTab;
import com.novamens.wicket.markup.html.tabs.TabbedPanel4;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.web.security.AclRow;
import kbee.web.security.AclRow.PermissionValue;
import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class PermissionsPanel extends ObjectEditorPanel<Role> {
	private static final long serialVersionUID = 1L;
				
	private static Logger logger = LogManager.getLogger(PermissionsPanel.class.getName());
	
	private AclRow row;
	private boolean updated = false;
	private boolean isWorkflow = false;
	
	public PermissionsPanel(String id, boolean isWorkflow) {
		super(id);
		this.isWorkflow=isWorkflow;
		setOutputMarkupId(true);
		
	}
	
	@Override
	public void updateModel() {
		
		if (!updated) 
			return;
		
		KbeeAbstractRole role = (KbeeAbstractRole)getModelObject();
		
		List<Permission> permissions = new ArrayList<Permission>();
		List<Permission> negativepermissions = new ArrayList<Permission>();
		for (Permission permission : row.getPermissions()) {
			PermissionValue value = row.getValue(permission);
			if (value.equals(PermissionValue.GRANT)) {
				permissions.add(permission);
			}
			else {
				negativepermissions.add(permission);
			}
		}
		
		boolean permissionsupdated = false;
		List<Permission> rolepermissions = role.getPermissions();
		if (rolepermissions.size()==permissions.size()) {
			for (Permission permission : rolepermissions) {
				if (!permissions.contains(permission)) {
					permissionsupdated = true;
					break;
				}
			}
		}
		else {
			permissionsupdated = true;
		}
		
		if (permissionsupdated) {
			setUpdatedPart("permissions");
			role.setPermissions(permissions);
		}
		
		permissionsupdated = false;
		List<Permission> rolenegativepermissions = role.getNegativePermissions();
		if (rolenegativepermissions.size()==negativepermissions.size()) {
			for (Permission permission : rolenegativepermissions) {
				if (!negativepermissions.contains(permission)) {
					permissionsupdated = true;
					break;
				}
			}
		}
		else {
			permissionsupdated = true;
		}
		
		if (permissionsupdated) {
			setUpdatedPart("permissions");
			role.setNegativePermissions(negativepermissions);
		}
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("tabs")==null) {
			setRow(getModelObject());
			addComponents();
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (getModel()!=null)
			getModel().detach();
	}
	
	protected void addComponents() {
	
		List<ITab> tabs = new ArrayList<ITab>();
		
		if (!this.isWorkflow) {
			tabs.add(new ITab() {
				public IModel<String> getTitle() {
					return new StringResourceModel("content", PermissionsPanel.this, null);
				}
				public Panel getPanel(String id) {
					List<Permission> permissions = new ArrayList<Permission>();
					permissions.add(KbeePermission.READ);
					permissions.add(KbeePermission.WRITE);
					permissions.add(KbeePermission.DELETE);
					permissions.add(KbeePermission.AUDIT_LOG);
					//permissions.add(KbeePermission.PRIVATE);
					
					Panel tab = new PermissionsTab<User>(id, new ObjectModel<User>(getSessionUser()),  PermissionsPanel.this.getEditor(), row, permissions) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							super.onUpdate(target);
							PermissionsPanel.this.updated = true;
						}
					};
					return tab;
				}
				public boolean isVisible() {
					return true;
				}
			});
		
			
		
		
		}
		
		else {
			
		
		List<ProcessLauncher> list = getLaunchers();

		list.sort(new Comparator<ProcessLauncher>() {
			@Override
			public int compare(ProcessLauncher o1, ProcessLauncher o2) {
				try {
					String label_1 =  o1.getContentTemplate().getDisplayName()+" -> "+o1.getLabel();
					String label_2 =  o2.getContentTemplate().getDisplayName()+" -> "+o2.getLabel();
					return label_1.compareToIgnoreCase(label_2);
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
			
		});
		
		for (ProcessLauncher launcher : list) {

						Procedure procedure = launcher.getProcedure();
						if (procedure != null) {
								final boolean enabled = launcher.isEnabled() || launcher.isLibrary();
								
								
								StringBuilder str = new StringBuilder();
								if (launcher.isEnabled())
									str.append( new StringResourceModel("mytasks", PermissionsPanel.this, null).getObject());
								
								if (launcher.isLibrary()) {
									if(str.length()>0)
										str.append(", ");
									str.append(new StringResourceModel("library",  PermissionsPanel.this, null).getObject());
								}
								
								String s= (str.length()>0? " <span class=\"ago\">("+str.toString()+")</span>" :"");
								
								final String procedureid = String.valueOf(procedure.getId());
								final List<Permission> procedurepermissions = getPermissions(procedure);
								
								final String label =  "<span class=\"predicate\"> " + launcher.getContentTemplate().getDisplayName()+" </span> -> "+launcher.getLabel();

								
								final String title = label + s;
								
								
								tabs.add(new AbstractModelTab<ProcessLauncher>( new ObjectModel<ProcessLauncher>(launcher), new Model<String>(title)) {
									
									
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
										Panel tab = new PermissionsTab<ProcessLauncher>(id,  getModel(), PermissionsPanel.this.getEditor(), row, permissions) {
											@Override
						 					public void onUpdate(AjaxRequestTarget target) {
												super.onUpdate(target);
												PermissionsPanel.this.updated = true;
											}
											@Override
											protected void onClick() {
											    getModel().getObject().getProcedure();
											    IModel<Procedure> mp = new ObjectModel<Procedure>(getModel().getObject().getProcedure());
											    
											    String p_id= getModel().getObject().getProcedure().getId().toString();
											    String l_id =getModel().getObject().getId().toString();
											    PageParameters pa= new PageParameters();
											    pa.add("id", p_id);
											    pa.add("laucher", l_id);

											    WebPage page = ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-procedure-page", pa);
											    setResponsePage(page);
											    
											}
										};
										return tab;
									}
									public boolean isVisible() {
										return enabled;
									}
								});
						}
						else
							logger.error("Launcher has a null Procedure " + launcher.getLabel());
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
	
	private List<Permission> getPermissions(Procedure procedure) {
		List<Permission> permissions = new ArrayList<Permission>();
		for (Task task : procedure.getTasks()) {
			if (task.getTrigger()!=null) {
				for (Permission permission : task.getTrigger().getPermissions()) {
					if (!permissions.contains(permission)) {
						permissions.add(permission);
					}
				}
			}
			if (task instanceof KbeeTask) {
				if (((KbeeTask)task).getEndConditions()!=null)
				for (EndCondition endcondition : ((KbeeTask)task).getEndConditions()) {
					if (((ManualEndCondition)endcondition).getTrigger()!=null) {
						for (Permission permission : ((ManualEndCondition)endcondition).getTrigger().getPermissions()) {
							if (!permissions.contains(permission)) {
								permissions.add(permission);
							}
						}
					}
				}
			}
		}
		return permissions;
	}

	private List<ProcessLauncher> getLaunchers() {
		List<ProcessLauncher>  list = getDomain().getService(WorkflowDomainService.class).getLaunchers();
		return list;
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}

	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();	
	}
}
