package com.novamens.kbee.content.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.MemberRole;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.security.IQLRule;
import com.novamens.content.security.Role;
import com.novamens.content.service.DomainService;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Task;
import com.novamens.workflow.UserTrigger;
import com.novamens.workflow.WorkflowContext;

import kbee.query.Proxy;

public class KbeeContentSystemSecurityService implements ContentSystemSecurityService, EventListener {

	static private Logger logger = LogManager.getLogger(KbeeContentSystemSecurityService.class.getName());

	private ContentSecurityDao securityDao;

	private Map<String, Acl> aclcache = Collections.synchronizedMap(new HashMap<String, Acl>());
	private Map<Long, Long> workflowWorkspaces = Collections.synchronizedMap(new HashMap<Long, Long>());
	
	private Map<Serializable, String> cache = Collections.synchronizedMap(new HashMap<Serializable, String>());

	private com.novamens.service.SecurityService service = null;
	
//	private long i = 0;

	@Override
	public boolean isReadable(Content content, User user) {
		if (!getDomainName(user).equals(content.getDomain().getName()))
			return false;
		if (getSecurityService().isRoot(user) || getSecurityService().isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId())
				|| getSecurityService().isMember(user, KbeeGlobalRole.SUPPORT.getId()))
			return true;
		//if (content.getWorkspace() != null)
		//	return content.getWorkspace().equals((Long) user.getId());
		boolean value = getAcl(content).checkPermission(user, KbeePermission.READ);
		return value;
	};

	
	@Override
	public boolean isReadable(Content content) {
		User user = getSecurityService().getSessionUser();

		if (!getDomainName(user).equals(content.getDomain().getName()))
			return false;
		
		if (getSecurityService().isRoot() || getSecurityService().isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())
				|| getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			return true;
		
		if (content.getWorkspace() != null)
			if (content.getWorkspace().equals((Long) user.getId()))				
				return true;
		boolean value = getAcl(content).checkPermission(user, KbeePermission.READ);
		return value;
	};

	@Override
	public boolean isReadable(Content content, Resource resource) {
		if (getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			if (!getSecurityService().isRoot())
				return false;
		return true;
	};

	@Override
	public boolean isWriteable(Content content) {
		return isWriteable(content, getSecurityService().getSessionUser());
	}

	@Override
	public boolean isWriteable(Content content, User user) {

		if (!getDomainName(user).equals(content.getDomain().getName()))
			return false;

		if (getSecurityService().isRoot(user) || getSecurityService().isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;

		if (content.getWorkspace() != null)
			if (!workflowWorkspace(content.getDomain(), content.getWorkspace())) {
				return content.getWorkspace().equals((Long) user.getId());
			}

		boolean value = getAcl(content).checkPermission(user, KbeePermission.WRITE);

		if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			return false;

		return value;

	};
	
	@Override
	public boolean isWriteable(SecuredMember member) {
		return isWriteable(member, getSecurityService().getSessionUser());
	}
	
	@Override
	public boolean isWriteable(SecuredMember member, User user) {

		if (!getDomainName(user).equals(member.getDomain().getName()))
			return false;

		if (getSecurityService().isRoot(user) || 
			getSecurityService().isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;

		boolean value = getAcl(member).checkPermission(user, KbeePermission.WRITE);

		if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			return false;

		return value;
	}
	
	@Override
	public boolean hasPermission(SecuredMember member, Permission permission) {
		return hasPermission(member, getSecurityService().getSessionUser(), permission);
	}
	
	public boolean hasPermission(SecuredMember member, User user, Permission permission) {

		if (!getDomainName(user).equals(member.getDomain().getName()))
			return false;

		if (getSecurityService().isRoot(user) || 
			getSecurityService().isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;
		
		boolean value = getAcl(member).checkPermission(user, permission);
		
		if (!value && 
			user.equals(member.getLastModifiedUser())) {
			return true;
		}	
		
		if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId())) {
			return false;
		}	

		return value;
	}


	@Override
	public boolean isTakeable(Content content) {
		return isTakeable(content, ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser());
	}

	@Override
	public boolean isTakeable(Content content, User user) {
		
		if (content.getWorkspace() == null || !content.getWorkspace().equals(getWorkflowUser(content.getDomain()).getId()))
			return false;
		

		WorkflowContext workflowcontext = content.getService(WorkflowService.class).getContext();

		if (workflowcontext.getTime() != null)
			return false;

		Task task = workflowcontext.getTask();

		if (task==null ||!(task.getTrigger() instanceof UserTrigger))
			return false;

		if (getSecurityService().isRoot(user) || getSecurityService().isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;

		Permission takepermission = ((UserTrigger) task.getTrigger()).getManualPermission();

		boolean value = getAcl(content).checkPermission(user, takepermission);

		if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			return false;

		return value;
	}
	
	/**
	 * 
	 */
	@Override
	public List<Principal> getTakers(Content content) {
		List<Principal> takers = new ArrayList<Principal>();

		if (content.getWorkspace() == null
				|| !content.getWorkspace().equals(getWorkflowUser(content.getDomain()).getId())) {
			return takers;
		}

		WorkflowContext workflowcontext = content.getService(WorkflowService.class).getContext();

		if (workflowcontext.getTime() != null) {
			return takers;
		}	

		Task task = workflowcontext.getTask();

		if (!(task.getTrigger() instanceof UserTrigger)) {
			return takers;
		}	

		Permission takepermission = ((UserTrigger) task.getTrigger()).getManualPermission();

		takers = getEnabledPrincipals(content, takepermission);

		return takers;
	}
	
	@Override
	public List<Principal> getMonitors(Content content) {
		List<Principal> monitors = new ArrayList<Principal>();

		if (!inWorkflow(content))
			return monitors;
		
		String permissionname = String
				.valueOf(content.getService(WorkflowService.class).getContext().getProcedure().getId())
				+ "-" + KbeePermission.MONITOR.toString();
		
		monitors = getEnabledPrincipals(content, KbeePermission.valueOf(permissionname));

		return monitors;
	}
	
	@Override
	public List<Principal> getReaders(Content content) {
		List<Principal> monitors = new ArrayList<Principal>();

		if (!inWorkflow(content))
			return monitors;
		
//		String permissionname = String
//				.valueOf(content.getService(WorkflowService.class).getContext().getProcedure().getId())
//				+ "-" + KbeePermission.READ.toString();
		
		//monitors = getEnabledPrincipals(content, KbeePermission.valueOf(permissionname));
		monitors = getEnabledPrincipals(content, KbeePermission.READ);

		return monitors;
	}

	@Override
	public boolean isPublishable(Content content) {
		User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		if (!getDomainName(user).equals(content.getDomain().getName()))
			return false;
		if (getSecurityService().isRoot() || getSecurityService().isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;
		boolean value = getAcl(content).checkPermission(user, KbeePermission.WRITE);
		if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			return false;
		return value;
	};

	@Override
	public boolean isMonitorable(Content content) {

		if (!inWorkflow(content))
			return false;

		if (content.getService(WorkflowService.class).getContext().getTime() == null)
			return false;

		User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();

		if (!getDomainName(user).equals(content.getDomain().getName()))
			return false;

		if (getSecurityService().isRoot() || getSecurityService().isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;

		String permissionname = String
				.valueOf(content.getService(WorkflowService.class).getContext().getProcedure().getId())
				+ "-" + KbeePermission.MONITOR.toString();
		boolean value = getAcl(content).checkPermission(user, KbeePermission.valueOf(permissionname));
		if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			return false;
		return value;
	}

	@Override
	public boolean isTerminable(Content content) {
		if (!inWorkflow(content))
			return false;

		if (content.getService(WorkflowService.class).getContext().getTime() == null)
			return false;

		User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();

		if (!getDomainName(user).equals(content.getDomain().getName()))
			return false;

		if (getSecurityService().isRoot() || getSecurityService().isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;

		String permissionname = String
				.valueOf(content.getService(WorkflowService.class).getContext().getProcedure().getId()) + "-"
				+ KbeePermission.TERMINATE.toString();
		boolean value = getAcl(content).checkPermission(user, KbeePermission.valueOf(permissionname));
		if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
			return false;
		return value;
	}

	@Override
	public boolean isDeleteable(Content content) {
		User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();

		try {
			if (!content.isHeadVersion() && !ObjectState.DELETED.equals(content.getState()))
				return false;
			if (!getDomainName(user).equals(content.getDomain().getName()))
				return false;
			if (getSecurityService().isRoot() || getSecurityService().isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()))
				return true;
			boolean value = getAcl(content).checkPermission(user, KbeePermission.DELETE);
			if (value && getSecurityService().isMember(KbeeGlobalRole.SUPPORT.getId()))
				return false;
			return value;
		} catch (NullPointerException e) {

			logger.error(e.getClass().getSimpleName() + " allowing to delete the file temporarily.");
			return true;
		}
	};

	@Override
	public boolean isAuditTrailReadable(Content content) {
		User user=ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		boolean value = getAcl(content).checkPermission(user, KbeePermission.AUDIT_LOG);
		return value || isPrivateEnabled(content) || getSecurityService().isMember( user, KbeeGlobalRole.SUPPORT.getId());
	}

	@Override
	public boolean isPrivateEnabled(Content content) {
		return isPrivateEnabled(content, ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser());
	};

	@Override
	public boolean isPrivateEnabled(Content content, User user) {
	
		if (!getDomainName(user).equals(content.getDomain().getName()))
			return false;

		if (getSecurityService().isRoot(user) || getSecurityService().isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
			return true;

		boolean value = getAcl(content).checkPermission(user, KbeePermission.PRIVATE);
		
		if (value && getSecurityService().isMember(user, KbeeGlobalRole.SUPPORT.getId()))
			return false;

		return value;
	}
	
	@Override
	@Transactional
	public Group getGroup(EntityMember entity, Role role) {
		MemberRole memberRole = ((KbeeEntityMember)entity).getMemberRole(role);
		Group group = ((KbeeEntityMember)entity).getGroup(memberRole);
		group = (Group)Proxy.Unproxy(group);
		return group;
	}

	public List<Principal> getEnabledPrincipals(Content content, Permission permission) {
		List<Principal> principals = new ArrayList<Principal>();
		Acl acl = getAcl(content);
		Enumeration<AclEntry> entries = acl.entries();
		while (entries.hasMoreElements()) {
			AclEntry entry = entries.nextElement();
			if (entry.checkPermission(permission)) {
				Principal principal = (Principal) entry.getPrincipal();
				if (principal instanceof User) {
					User user = (User) principal;
					if (user.isActive() && user.isEnabled()) {
						principals.add(user);
					}
				} else {
					principals.add(principal);
				}
			}
		}
		return principals;
	}

	public Acl getAcl(Content content) {

		//long t1 = System.currentTimeMillis();
		
		if (cache.size()>60000) {
			synchronized (cache) {
				cache.clear();
			}
		}

		String aclkey = cache.get(content.getId());
		
		List<Acl> acls = new ArrayList<Acl>();
		
		if (aclkey==null) {
			StringBuilder ruleskey = new StringBuilder();
			for (IQLRule rule : getRules(content.getDomain())) {
				if (rule.evaluate(content)) {
					acls.add(rule.getAcl());
					ruleskey.append("+" + ((KbeeSecurityRule) rule).getId() + "+");
				}
			}
			aclkey = ruleskey.toString();
			synchronized (this) {
				cache.put(content.getId(), aclkey);
			}
		}

		Acl acl = null;
		synchronized (this) {
			acl = aclcache.get(aclkey);
		}

		if (acl == null) {
			if (acls.isEmpty()) {
				for (IQLRule rule : getRules(content.getDomain())) {
					if (rule.evaluate(content)) {
						acls.add(rule.getAcl());
					}
				}
			}
			acl = merge(acls);
			synchronized (this) {
				aclcache.put(aclkey, acl);
			}
		}
		
		if (content.getAcl()!=null) {
			KbeeAcl kbeeacl = (KbeeAcl)content.getAcl();
			((KbeeAcl)acl).merge(null, kbeeacl);
		}
		
		return acl;
	}
	
	public Acl getAcl(SecuredMember member) {
		
		Acl acl = merge(getAcls(member));
		
		return acl;
	}

	public List<SecurityRule> getRules(Content content) {
		List<SecurityRule> rules = new ArrayList<SecurityRule>();
		for (IQLRule rule : getRules(content.getDomain())) {
			if (rule.evaluate(content)) {
				rules.add(rule);
			}
		}
		return rules;
	}

	public List<IQLRule> getRules(Domain domain) {
		return getSecurityDao().getRules(domain);
	}

	public void onUpdate(IQLRule rule) {
		String ruleid = "+" + ((KbeeSecurityRule) rule).getId() + "+";
		boolean found = true;
		synchronized (this) {
			while (found) {
				found = false;
				for (String key : aclcache.keySet()) {
					if (key.contains(ruleid)) {
						aclcache.remove(key);
						found = true;
						break;
					}
				}
			}
			cache.clear();
		}
	}

	public void onUpdate(User user) {
		boolean found = true;
		synchronized (aclcache) {
			while (found) {
				found = false;
				for (Entry<String, Acl> cacheentry : aclcache.entrySet()) {
					Acl acl = cacheentry.getValue();
					for (AclEntry aclentry : ((KbeeAcl) acl).getEntries()) {
						for (Group group : user.getGroups()) {
							if (group.getName().equals(aclentry.getPrincipal().getName())) {
								aclcache.remove(cacheentry.getKey());
								found = true;
								break;
							}
						}
					}
					if (found)
						break;
				}
			}
		}
	}
	
	public boolean listen(Event event) {
		return event.getObject() instanceof Content ||
				event instanceof EvictCacheServiceEvent;
	}
	
	public void onEvent(Event event) {
		if (event.getObject()!=null && event.getObject() instanceof Content) {
			synchronized (cache) {
				cache.remove(((Content)event.getObject()).getId());
			}
		}
		else 
		if (event instanceof EvictCacheServiceEvent) {
			synchronized (cache) {
				cache.clear();
			}
		}
	}

	public void setSecurityDao(ContentSecurityDao securityDao) {
		this.securityDao = securityDao;
	}

	public ContentSecurityDao getSecurityDao() {
		return this.securityDao;
	}

	private String getDomainName(User user) {
		if (user == null)
			return "";
		int i = user.getName().indexOf("@");
		String domain = i > 0 ? user.getName().substring(i + 1) : "";
		return domain;
	}

	private Acl merge(List<Acl> acls) {
		KbeeAcl kbeeacl = new KbeeAcl();
		for (Acl acl : acls) {
			kbeeacl.merge(null, acl);
		}
		return kbeeacl;
	}
	
	private List<Acl> getAcls(SecuredMember member) {
		List<Acl> acls = new ArrayList<Acl>();
		if (member.getSecurityRule()!=null) {
			Acl acl = member.getSecurityRule().getAcl();
			if (!((KbeeAcl)acl).getEntries().isEmpty()) {
				acls.add(member.getSecurityRule().getAcl());
				return acls;
			}
		}
		if (member.getParents()!=null) {
			for (DataSetMember parent : member.getParents()) {
				parent = (DataSetMember)getContentDao().reload(parent);
				if (parent instanceof SecuredMember) {
					acls.addAll(getAcls((SecuredMember)parent));
				}
			}
		}
		return acls;
	}

	private SecurityService getSecurityService() {
		if (this.service != null)
			return this.service;
		this.service = ServiceLocator.getService(SecurityService.class);
		return this.service;
	}
	
	private boolean workflowWorkspace(Domain domain, long workspace) {
		Long workflowWorkspce = workflowWorkspaces.get((long) domain.getId());
		if (workflowWorkspce == null) {
			synchronized (this) {
				User workflowUser = getWorkflowUser(domain);
				if (workflowUser != null) {
					workflowWorkspce = Long.valueOf((long) workflowUser.getId());
					workflowWorkspaces.put((long) domain.getId(), workflowWorkspce);
				}
			}
		}
		return workspace == workflowWorkspce;
	}
	
	private ContentDao getContentDao() {
		 return  (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private User getWorkflowUser(Domain domain) {
		return domain.getService(DomainService.class).getWorkflowUser();
	}

	private boolean inWorkflow(Content content) {
		return domainWorkflow(content.getDomain()) && content.getService(WorkflowService.class).getTask() != null;
	}

	private boolean domainWorkflow(Domain domain) {
		return domain.getService(WorkflowDomainService.class) != null;
	}
}