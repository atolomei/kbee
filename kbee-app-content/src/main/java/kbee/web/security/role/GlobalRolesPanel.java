package kbee.web.security.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;

import com.novamens.content.user.UserService;
import com.novamens.content.web.security.markup.GroupStandAlonePage;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.acl.KbeeGroup;

import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeArea;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;


import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class GlobalRolesPanel extends RelationEditor<Role, Group> {
	private static final long serialVersionUID = 1L;

	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GlobalRolesPanel.class.getName());

	private Map< Serializable, Serializable> complete_subset;
	
	KbeeArea area;
	
	boolean admin_groups = false;
	boolean settings_groups = false;
	boolean content_groups = false;
				
	boolean factory_groups = false;
	
	boolean all_groups = false;
	boolean workflow_groups = false;

	@Override
	protected String getTitle(Group value) {
		return value.getDisplayName( getSessionUser().getLocale());
	}
	
	
	public GlobalRolesPanel() {
		super("groups");
		all_groups = true;
	}
		
	public GlobalRolesPanel(String id) {
		super(id, "groups");
		all_groups = id!=null && id.equals("all");
	}
	
	
	@Override
	public boolean isItemLink() {
		return false;
	}
	
	public GlobalRolesPanel(KbeeArea area) {
		super(area.getCode(), "groups");
		this.area = area;
	}
		
	
	
	 
	
	
	@Override
	public void updateModel() {

		if (!isUpdated()) 
			return;

		 if (this.complete_subset==null)
			return;
		
		List<Group> values = new ArrayList<Group>();
			
		for (IModel<Group> model : getValues())
				values.add(model.getObject());
		
		for (Group group: getContentSecurityDao().getCanonicalGroups(getDomain())) {
				
			if (values.contains(group)       && this.complete_subset.containsKey(group.getId())) {
				logger.debug("add " + group.getName());
				((com.novamens.kbee.content.security.KbeeAbstractRole) getModel().getObject()).addGroup(group);			
			}
			else if (values.contains(group)  && !this.complete_subset.containsKey(group.getId())) {
				logger.error("impossible -> " + group.getDisplayName());
			}
			else if (!values.contains(group) && this.complete_subset.containsKey(group.getId())) {
				logger.debug("remove " + group.getName());
				((com.novamens.kbee.content.security.KbeeAbstractRole) getModel().getObject()).removeGroup(group);
			}
			else if (!values.contains(group) && !this.complete_subset.containsKey(group.getId())) {
				logger.debug("do nothing " + group.getName());
				// do nothing
			}
		}
		
		setUpdatedPart("Global Rights - "+ getId() + "");
		setUpdated(false);
	}

	@Override
	public boolean ordered() {
		return false;
	}

	/** 
	 * form group title
	 */
	@Override
	public IModel<String> getLabel() {
		return null;
	}
					
	public KbeeArea  getArea() {
		return area;
	}

	public boolean isFactoryGroups() {
		return all_groups || factory_groups;
	}

	
	public boolean isWorkflowGroups() {
		return all_groups || workflow_groups;
	}
					
	public boolean isContentGroups() {
		return all_groups || content_groups;
	}

	public boolean isAdminGroups() {
		return all_groups || admin_groups;
	}
					
	public boolean isSettingsGroups() {
		return all_groups || settings_groups;
	}

	public void setWorkflowGroups(boolean b) {
		this.workflow_groups=b;
	}
	
	public boolean isAllGroups() {
		return all_groups;
	}

	public void setAllGroups(boolean b) {
		all_groups=b;
	}

	public void setAdminGroups(boolean b) {
		admin_groups=b;
	}
	
	public void setSettingsGroups(boolean b) {
		settings_groups=b;
	}
	
	public void setContentGroups(boolean b) {
		content_groups=b;
	}
	
	
	
	
	public List<Group> getGroups() {
		
		List<Group> base = new ArrayList<Group>();
		
		this.complete_subset = new HashMap<Serializable ,  Serializable>();
		
		for (Group group : getContentSecurityDao().getCanonicalGroups(getDomain())) {
			
			boolean isok = false;
			
			if (group.isEnabled()) {
			
				if (!group.isOnlyInternalUse()) {
					
					
					if (group instanceof KbeeGroup && getArea().equals(((KbeeGroup) group).getArea())) {
					
						if (group.getName()!=null && group.getName().equals(KbeeGlobalRole.BILLBOARDS.getId())) {
							// 
						}
						else 	
							isok = true;
					}	
					
					
					if (isok) {
						if (group.isOnlyPortal()) {
							 if (isPortalEnabled()) 
								 base.add(group);
						}
						else {
							
							if (group.isOnlyInternalUse()) {
								 if (is_root)
									 base.add(group);
							}
							else
								base.add(group);
						}
					}
				}
			}
		}
		
		List<Group> groups = new ArrayList<Group>();
									 										
		base.forEach(item -> this.complete_subset.put(item.getId(), item.getId()));
		
		Map<Serializable ,  Serializable> subset = new HashMap<Serializable ,  Serializable>();
		getValues().forEach(item -> subset.put(item.getObject().getId(), item.getObject().getId()));
		
		for (Group group : base) {
 			if (!subset.containsKey(group.getId()))
 				groups.add(group);
		}
		
		Collections.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group a, Group b) {
				try {
					if (getStringValue(a) == null)
						return (getStringValue(b)!=null?1:0);
					else if(getStringValue(b)==null)
						return -1;
					return  getStringValue(a).compareToIgnoreCase(getStringValue(b));
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		return groups;
	}
	
	@Override
	protected Property<?> getKey() {
		return new Property<Group>() {
			public String getName() {
 				return "general-rights";
			}
			public List<Group> getChoices() {
				return getGroups();
			}
		};
	}
	
	@Override
	protected boolean isValid(Group group) {
		
		if (isAllGroups()) {
			return true;
		}
		
		if (group instanceof KbeeGroup && getArea().equals(((KbeeGroup) group).getArea())) {
			return true;
		}						
		
		return false;
	}

	@Override
	protected void onValueClick(IModel<Group> model) {
		final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(com.novamens.security.acl.KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(com.novamens.security.acl.KbeeGlobalRole.SECURITY.getId());
		
		if (role_security)
			setResponsePage(new GroupStandAlonePage(model));
		else  {
			setResponsePage(new kbee.web.error.ApplicationErrorPage<Object>( 
				new Model<String>("Your user account doesn't have rights to read Group " + model.getObject().getName()), 
				new Model<String>("Groups")));
			// TODO Alert Window
			//
		}
	}
	
	@Override	
	protected String getStringValue(Object value) {
		return value instanceof Group ? ((Group)value).getDisplayName() : super.getStringValue(value);
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	/**
	 * This settings should be at domain level 
	 */													
	private boolean isPortalEnabled() {
		return ServiceLocator.getService(BrandingService.class).isPortalEnabled();
	}

	public void addGroup(Group group) {
 
		
	}
}
