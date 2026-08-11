package kbee.web.security.role;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;

import kbee.util.logging.Logger;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

@SuppressWarnings("serial")
public class RoleMainPanel extends ObjectEditor<Role> implements PageMainTabs {
	private static final long serialVersionUID = 1L;
			
	private static Logger logger = Logger.getLogger(RoleMainPanel.class.getName());

	private String initial_tab;
	
	final static boolean is_root = 
		ServiceLocator
		.getService(SecurityService.class)
		.isRoot();
	
	final boolean role_admin = is_root || 
		ServiceLocator.
		getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public RoleMainPanel(IModel<Role> model, boolean isNew) {
		this("editor", model, isNew);
	}
	
	public RoleMainPanel(String id, IModel<Role> model, boolean isNew) {
		super(id, model);

		setModel(model);
		setIsNew(isNew);
		
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		
		
		/**
		 * Info
		 * Access to Sections
		 * Permissions
		 **/
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.role", this, null), "info") {
			@Override
			public Panel getPanel(String panelId) {
				return new RoleEditor(panelId, getModel(), isNew()) {
					@Override
					public void setEditionEnabled(boolean value) {
						RoleMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return RoleMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						RoleMainPanel.this.onClose(target);
					}
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) 
							RoleMainPanel.this.onClose(target);
						else { 
							setEditionEnabled(false);
							target.add(RoleMainPanel.this);
						}
					}
				};
			}
		});
		
		/**
		 * 
		 */
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.sections", this, null), "sections") {
			@Override
			public Panel getPanel(String panelId) {
				return new RoleSectionsEditor(panelId, getModel(), false) {
					@Override
					public void setEditionEnabled(boolean value) {
						RoleMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return RoleMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						RoleMainPanel.this.onClose(target);
					}
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) 
							RoleMainPanel.this.onClose(target);
						else { 
							setEditionEnabled(false);
							target.add(RoleMainPanel.this);
						}
					}
				};
			}
		});

		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.contentpermissions", this, null), "contentpermissions") {
			@Override
			public Panel getPanel(String panelId) {
				return new RolePermissionsEditor(panelId, getModel(), false, false) {
					@Override
					public void setEditionEnabled(boolean value) {
						RoleMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return RoleMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						RoleMainPanel.this.onClose(target);
					}
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) 
							RoleMainPanel.this.onClose(target);
						else { 
							setEditionEnabled(false);
							target.add(RoleMainPanel.this);
						}
					}
				};
			}
		});

		

		tabs.add(new AbstractTabKB(new StringResourceModel("editor.permissions", this, null), "permissions") {
			@Override
			public Panel getPanel(String panelId) {
				return new RolePermissionsEditor(panelId, getModel(), false, true) {
					@Override
					public void setEditionEnabled(boolean value) {
						RoleMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return RoleMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						RoleMainPanel.this.onClose(target);
					}
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) 
							RoleMainPanel.this.onClose(target);
						else { 
							setEditionEnabled(false);
							target.add(RoleMainPanel.this);
						}
					}
				};
			}
		});

		if (includesUserAdmin()) {
			
			tabs.add(new AbstractTabKB(new StringResourceModel("editor.managedentities", this, null), "admin") {
				@Override
				public Panel getPanel(String panelId) {
					return new ManagedEntitiesPanel(panelId, getModel(), false ) {
						@Override
						public void onCancel(AjaxRequestTarget target) {
							if (isNew()) 
								RoleMainPanel.this.onClose(target);
							else { 
								setEditionEnabled(false);
								target.add(RoleMainPanel.this);
							}
						}
					};
				}
			});
		}
		
		tabs.add(new AbstractTabKB(getLabel("editor.users"), "users") {
			@Override
			public Panel getPanel(String panelId) {
				return new UserSetPanel(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("editor.state"), "status") {
			@Override
			public Panel getPanel(String panelId) {
  				return new ObjectStateEditor<Role>(panelId, getModel(), !role_admin);
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("editor.audit"), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Role>(panelId, getModel());
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",  this.getClass().getSimpleName(), tabs);
		
		add(editor);
		

		editor.setTitle(new StringResourceModel("sections", this, null));
		
		int sel = editor.getSelectedTab();
		if (sel==-1)
			sel=0;
		String str =  (editor.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}

	protected void onClose(AjaxRequestTarget target) {
		
	}
	
	// desahbilitado en release
	private boolean includesUserAdmin() {
		if (!(getModelObject() instanceof EntityRole))
			return false;
		return true;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public void setInitialTab(String a) {
			try {
				initial_tab=a;
				((VerticalLayout<ITab>) get("tabs")).setSelectedTab(a);
			} 
			catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
	

}
