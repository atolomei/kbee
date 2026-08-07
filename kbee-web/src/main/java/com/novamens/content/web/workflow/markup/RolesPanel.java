package com.novamens.content.web.workflow.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.web.security.markup.GroupStandAlonePage;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dao.SecurityDao;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.security.acl.KbeeGroupProxy;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.WorkflowContext;

/**
 * 
 * Roles assigned in a workflow under execution.
 */
@SuppressWarnings("serial")
public class RolesPanel  extends ModelPanel<WorkflowContext>{	
	private static final long serialVersionUID = 1L;
																								
	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(RolesPanel.class.getName()));
	
	private Map<RoleInProcess, List<Principal>> providedroles;
	private Map<RoleInProcess, User> assignedroles;

	public RolesPanel(String id, IModel<WorkflowContext> model) {
		super(id, model);
		
		add(new ListView<RoleInProcess>("roles", new PropertyModel<List<RoleInProcess>>(this, "roles")) {
			public void populateItem(ListItem<RoleInProcess> item) {
				RoleInProcess role = item.getModelObject();
				item.add(new Label("role", role.getLabel()));
				
				
				item.add(new ListView<Principal>("user", () -> getUsers(role)) {
					public void populateItem(ListItem<Principal> item) {
						item.add(new Label("name", item.getModelObject().getDisplayName()));
					}
				});
				
				item.add(new ListView<IModel<Principal>>("provided-user", () -> getProvidedUsers(role)) {
					public void populateItem(ListItem<IModel<Principal>> item) {
						item.add(new Label("name", item.getModelObject().getObject().getDisplayName()));
						item.add(new Label("legend", new StringResourceModel("provided-user-legend", RolesPanel.this)));
						item.getModelObject().detach();
					}
				});

				//item.add(new ListView<Principal>("provided-group", getProvidedGroups(role)) {
				item.add(new ListView<Principal>("provided-group", () -> getProvidedGroups(role)) {
					public void populateItem(ListItem<Principal> item) {
						Link<?> link = new Link<Void>("link") {
							public void onClick() {
								IModel<Group> model = new ObjectModel<Group>((Group)item.getModelObject());
								model.detach();
								setResponsePage(new GroupStandAlonePage(model));
							}
						};
						link.add(new Label("name", item.getModelObject().getDisplayName()));
						item.add(link);
						item.add(new Label("legend", new StringResourceModel("provided-group-legend", RolesPanel.this)));
					}
				});
			}
		});
	}
	
	public List<RoleInProcess> getRoles() {
		return getWorkflowContext().getProcedure().getRoles();
	}
	
	public Map<RoleInProcess, List<Principal>> getProvidedRoles() {
		if (providedroles==null) {
			providedroles = getContent().getService(WorkflowService.class).getRoles(getWorkflowContext());
			if (providedroles == null) providedroles = new HashMap<RoleInProcess, List<Principal>>();
		}
		return providedroles;
	}
	
	public Map<RoleInProcess, User> getAssignedRoles() {
		if (assignedroles==null) {
			assignedroles = getWorkflowContext().getRoles();
			if (assignedroles == null) assignedroles = new HashMap<RoleInProcess, User>();
		}
		return assignedroles;
	}
	
	public WorkflowContext getWorkflowContext() {
		return getModelObject();
	}
	
	public Content getContent() {
		return ((KbeeContext)getModelObject()).getContent();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		providedroles = null;
		assignedroles = null;
	}
	
	private List<Principal> getUsers(RoleInProcess role) {
		List<Principal> principals = new ArrayList<Principal>(); 
		
		User user = getAssignedRoles().get(role);
		if (user!=null) {
			principals.add(user);
		}
		
		return principals;
	}
	
	public List<IModel<Principal>> getProvidedUsers(RoleInProcess role) {
		
		List<IModel<Principal>> principals = new ArrayList<IModel<Principal>>();
		
		if (!getUsers(role).isEmpty()) 
			return principals;
		
		List<Principal> provides = getProvidedRoles().get(role);
		
		if (provides==null) 
			return principals;
		
		for (Principal principal : provides) {
			if (principal instanceof User) {
				principals.add(new ObjectModel<Principal>(principal));
			}
		};
		
		return principals;
	}
	
	private List<Principal> getProvidedGroups(RoleInProcess role) {
		
		List<Principal> principals = new ArrayList<Principal>();
		
		if (!getUsers(role).isEmpty()) return principals;
		
		if (getProvidedRoles().get(role)!=null) {
			for (Principal principal : getProvidedRoles().get(role)) {
				if (principal instanceof Group) {
					if (principal instanceof KbeeGroupProxy) {
						principal = getSecurityDao().findGroupById(principal.getId());
					}			
					principals.add(principal);
				}
			};
			
		}
		else
			logger.error("getProvidedRoles().get(role)==null");
		
		return principals;
	}
	
	private SecurityDao  getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}

}
