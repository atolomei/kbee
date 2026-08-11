package com.novamens.kbee.content.security;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import com.novamens.beans.BeansService;
import com.novamens.cache.SelfExpiringHashMap;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.base.SiteIQLRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.IQLRule;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

public class KbeeContentSecurityDao implements ContentSecurityDao, EventListener {
			
	static private final int MINUTES_30 = 1000 * 60 * 30;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentSecurityDao.class.getName());
	
	static private Logger startlogger = LogManager.getLogger("startup");

	private SessionFactory sessionFactory;
	private SecurityDao securityDao;

	// Token service
	// 
	private SelfExpiringHashMap<Serializable, List<SecurityRule>> rules_map =  new SelfExpiringHashMap<Serializable, List<SecurityRule>>(MINUTES_30);
		
	public KbeeContentSecurityDao() {
	}
	
	public KbeeContentSecurityDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private boolean includePrincipal(SecurityRule rule, User user) {
		try {
			for (AclEntry entry: ((KbeeAcl)rule.getAcl()).getEntries()) {
				if (entry.getPrincipal().equals(user)) {
					return true;
				}
				else { 
					if (entry.getPrincipal() instanceof Group) {
						if (user.isMember((Group)entry.getPrincipal()))
							return true;
					}
				}
			}
			return false;
			
		} 
		catch (RuntimeException e) {
			logger.error(e);
			return false;
		}
	}
	
	@Override
	public Acl findAclById(Serializable id){
		return getSecurityDao().findAclById(id);
	}
	
	@Override
	public void save(User user) {
		((KbeeUser)user).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeUser)user).setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		getSecurityDao().save(user);
	}
	
	@Override
	public void save(User user, User sessionUser) {
		((KbeeUser)user).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeUser)user).setLastModifiedUser(sessionUser);
		getSecurityDao().save(user);
	}
	
	@Override
	public void save(UserProfile profile) {
		((KbeeUser)profile.getUser()).setDomain(profile.getDomain());
		((KbeeUser)profile.getUser()).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeUser)profile.getUser()).setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	 	this.sessionFactory.getCurrentSession().save(profile);
	}

	@Override
	public void save(Acl acl) {
		((KbeeAcl) acl).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeAcl) acl).setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		getSecurityDao().save(acl);
	}
	
	@Override
	public List<SecurityRule> getRules(User user) {
	
		if (rules_map.containsKey(user.getId()))
			return rules_map.get(user.getId());
		
		synchronized (this) {
			List<SecurityRule> rules = new ArrayList<SecurityRule>();
	 		for (SecurityRule rule : getRules(getContentDao().findUserProfileByUser(user).getDomain())) {
					if (rule.getCondition()!=null && includePrincipal(rule, user)) {
						rules.add(rule);
					}
			}
			Collections.sort(rules, new Comparator<SecurityRule>() {
					@Override
					public int compare(SecurityRule a, SecurityRule b) {
						try {
						if (a.getName()==null)
							return (b.getName()!=null?1:0);
						else if (b.getName()==null)
							return -1;
						return a.getName().compareToIgnoreCase(b.getName());
						} catch (Exception e) {
							logger.error(e);
							return 0;
						}
					}
					
			});	
		
			rules_map.put(user.getId(), rules);
 			return rules_map.get(user.getId());
		}
	}

	@Override
	public void save(IQLRule rule) {
		((KbeeSecurityRule)rule).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeSecurityRule)rule).setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		this.sessionFactory.getCurrentSession().save(rule);
	}
	
	@Override
	public void delete(IQLRule rule) {
		this.sessionFactory.getCurrentSession().delete(rule);
	}
	
	public void deleteSiteRule(SiteIQLRule rule) {
		this.sessionFactory.getCurrentSession().delete(rule);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SiteIQLRule> findRuleByRelatedObjectId(Serializable id) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeSiteSecurityRule R where R.related_object_id='"+id.toString()+"'");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		if (results.isEmpty())
			return null;
		return (List<SiteIQLRule>) results;
	}
	
	@Override
	public IQLRule findRuleById(Serializable id) {
		return (IQLRule) sessionFactory.getCurrentSession().get(KbeeSecurityRule.class, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IQLRule> getRules(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeSecurityRule R where R.domain.id='"+String.valueOf(domain.getId())+"'");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<IQLRule>) results;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> getCanonicalGroups(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeGroup T where T.domain.id=" + domain.getId().toString() + " AND T.canonical=true" );
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Group>) results;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Group> getGroups(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeGroup K where K.domain.id=" + domain.getId().toString() + " order by lower(K.name)");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Group>) results;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Group> getGroups() {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeGroup");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Group>) results;
	}
	
	@Override
	public List<String> canonicalGroupsMissing(Domain domain) {
		
		Map<String, Boolean> gcanonical = new HashMap<String, Boolean>();

		gcanonical.put(KbeeGlobalRole.USER.getId(), Boolean.valueOf(false));							
		gcanonical.put(KbeeGlobalRole.DOMAIN_ADMIN.getId(), Boolean.valueOf(false)); 					
		gcanonical.put(KbeeGlobalRole.INFORMATION_MODEL.getId(), Boolean.valueOf(false)); 				
		gcanonical.put(KbeeGlobalRole.MODEL_READ.getId(), Boolean.valueOf(false)); 				
		gcanonical.put(KbeeGlobalRole.SETTINGS.getId(), Boolean.valueOf(false)); 				
		gcanonical.put(KbeeGlobalRole.SECURITY.getId(), Boolean.valueOf(false));						
		gcanonical.put(KbeeGlobalRole.EXTERNAL_USER.getId(), Boolean.valueOf(false));						
		gcanonical.put(KbeeGlobalRole.FEDERATED_SECURITY.getId(), Boolean.valueOf(false));						
		gcanonical.put(KbeeGlobalRole.FEDERATED_VALUES.getId(), Boolean.valueOf(false));						

		gcanonical.put(KbeeGlobalRole.MONITOR_AUDIT.getId(), Boolean.valueOf(false));					
		gcanonical.put(KbeeGlobalRole.DATASET_VALUES_WRITE.getId(), Boolean.valueOf(false));			
		gcanonical.put(KbeeGlobalRole.DATASET_VALUES_READ.getId(), Boolean.valueOf(false));				
		
		gcanonical.put(KbeeGlobalRole.WORKFLOW.getId(), Boolean.valueOf(false));						
		gcanonical.put(KbeeGlobalRole.WORKSPACE.getId(), Boolean.valueOf(false));
		gcanonical.put(KbeeGlobalRole.PENDING_TASKS.getId(), Boolean.valueOf(false));
		
		
		gcanonical.put(KbeeGlobalRole.ARCHIVE.getId(), Boolean.valueOf(false));							
		gcanonical.put(KbeeGlobalRole.RECYCLE_BIN.getId(), Boolean.valueOf(false));
		
		gcanonical.put(KbeeGlobalRole.SUPPORT.getId(), Boolean.valueOf(false));							
								
		gcanonical.put(KbeeGlobalRole.BILLBOARDS.getId(), Boolean.valueOf(false));						
		gcanonical.put(KbeeGlobalRole.NOTIFICATIONS.getId(), Boolean.valueOf(false));						
		gcanonical.put(KbeeGlobalRole.PORTAL_ADMIN.getId(), Boolean.valueOf(false)); 					
		gcanonical.put(KbeeGlobalRole.SU.getId(), Boolean.valueOf(false));								
		
		gcanonical.put(KbeeGlobalRole.FILE_SERVER.getId(), Boolean.valueOf(false));
		gcanonical.put(KbeeGlobalRole.AUDITOR.getId(), Boolean.valueOf(false));
		
		// --------
		if (domain.getName().equals("kbee")) {
			gcanonical.put(KbeeGlobalRole.SERVICE_ADMIN.getId(), Boolean.valueOf(false));					
			gcanonical.put(KbeeGlobalRole.API_DEVELOPER.getId(), Boolean.valueOf(false));					
			gcanonical.put(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId(), Boolean.valueOf(false));
			gcanonical.put(KbeeGlobalRole.OPERATIONS_ENGINEER.getId(), Boolean.valueOf(false));
			gcanonical.put(KbeeGlobalRole.SUPPORT_AGENT.getId(), Boolean.valueOf(false));
		}
		
		String hql = "FROM KbeeGroup G WHERE G.canonical=true and G.domain.id="+ domain.getId().toString();

		try {
			@SuppressWarnings("unchecked")
			Query<Group> query = (Query<Group>) sessionFactory.getCurrentSession().createQuery(hql);
			query.setCacheable(false);

			Iterator<Group> it = query.list().iterator();
			while (it.hasNext()) {
				Group group = (Group) it.next();
				if (gcanonical.containsKey(group.getName()))
						gcanonical.remove(group.getName());
			}
			List<String> gnames= new ArrayList<String>();
			for ( Map.Entry<String, Boolean> entry: gcanonical.entrySet()) {
				gnames.add(entry.getKey());
			}
			
			return gnames;
		
		} 
		catch (Exception e) {
			logger.error(e);
			startlogger.error(e);
			return new ArrayList<String>(); 
		}
	}
	
	@Override
	public void delete(Group group) {
		sessionFactory.getCurrentSession().delete(group);
	}
	
	@Override
	public void save(Group group) {
		((KbeeGroup)group).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeGroup)group).setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		getSecurityDao().save(group);
	}
	
	@Override
	public Group findGroupById(Long id) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeGroup G where G.id="+String.valueOf(id));
		List<?> results = query.list();
		if (results.isEmpty())
			return null;
		return (Group) results.get(0);
	}
	
	@Override
	public Group findGroupByName(String name) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeGroup G where G.name='"+name+"'");
		List<?> results = query.list();
		if (results.isEmpty())
			return null;
		return (Group) results.get(0);
	}
	
	@Override
	public Group findGroupByName(String name, Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("from KbeeGroup K where K.name=:name AND K.domain.id=:domainid");
		query.setParameter("domainid", domain.getId());
		query.setParameter("name", name);
		List<?> list = query.list();
		if (list.isEmpty()) return null;
		return (Group)list.get(0);
	}
	
	@Override
	public void save(Role role) {
		((KbeeAbstractRole)role).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeAbstractRole)role).setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		this.sessionFactory.getCurrentSession().save(role);
	}
	
	
	@Override
	public void delete(Role role) {
		this.sessionFactory.getCurrentSession().delete(role);
	}

	@Override
	public boolean isDeletable(Role role) {
		String sql = "select count(*) from kb_user_role where role_id="+String.valueOf(role.getId().toString());
		NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
		long start = System.currentTimeMillis();
		if (isOracle()) {
			java.math.BigDecimal res = (java.math.BigDecimal) query.uniqueResult();
			logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis()-start) + " ms");
			return (res!=null ? res.longValue()==0: true);
		}
		else {
			java.math.BigInteger res = (java.math.BigInteger) query.uniqueResult();
			logger.debug(sql + " -> " + String.valueOf(System.currentTimeMillis()-start) + " ms");
			logger.debug(role.getName() + ": " + (res!=null ? String.valueOf(res.longValue()):""));
			boolean b=(res!=null ? res.longValue()==0: true);
			return b;
		}
	}
	
	@Override
	public EntityRole findEntityRoleById(Long id) {												
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeEntityRole R where R.id="+String.valueOf(id));
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		if (results.isEmpty())
			return null;
		return (EntityRole) results.get(0);
		
	}
	
	@Override
	public DomainRole findGeneralRoleById(Long id) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeDomainRole R where R.id="+String.valueOf(id));
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		if (results.isEmpty())
			return null;
		return (DomainRole) results.get(0);
		
	}
	
	@Override
	public DomainRole findRoleByName(String name, Serializable domainid) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeAbstractRole R where R.name='"+name.trim() + "' AND R.domain.id= " + domainid.toString());
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		if (results.isEmpty())
			return null;
		return (DomainRole) results.get(0);
		
	}
	
	@Override
	public Role findRoleById(Long id) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeAbstractRole R where R.id="+String.valueOf(id));
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		if (results.isEmpty())
			return null;
		return (Role) results.get(0);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Role> getRoles(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeAbstractRole R where R.domain.id=" + domain.getId().toString() + " order by lower(R.name)");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Role>) results;
	}


	@Override
	@SuppressWarnings("unchecked")
	public List<Role> getDomainRoles(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeDomainRole R where R.domain.id=" + domain.getId().toString() + " order by lower(R.name)");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Role>) results;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<Role> getAPIAssignableRoles(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeAbstractRole R where R.domain.id=" + domain.getId().toString() + " and R.api_enabled=true " + " order by lower(R.name)");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Role>) results;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Role> getRolesByEntitySet(EntitySet dataset) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeEntityRole R where R.classifier.dataset1.id=" + dataset.getId().toString() + " or R.classifier.dataset2.id=  " + dataset.getId().toString() +" order by lower(R.name)");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Role>) results;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Role> getCanonicalRoles(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeAbstractRole R where R.domain.id=" + domain.getId().toString() + " and R.canonical=true order by lower(R.name)");
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<Role>) results;
	}
	
	@SuppressWarnings("unchecked")
	public List<UserRole> findUserRolesByEntityMember(Role role, EntityMember member) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeUserRole R where R.role.id=" + role.getId().toString() + " and R.entity.id=" + member.getId().toString()+ " order by lower(user.lastname)");
		//query.setCacheable(true);
		//query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<UserRole>) results;
	}
			
	@Override
	public List<UserRole> findUserRolesByRole(Role role) {
		return findUserRolesByRole(role, null);
		
	}

	@Override
	public long getTotalMembers(Role role) {
		String hql = "select count(*) FROM KbeeUserRole R where R.role.id="+role.getId().toString();
		long start = System.currentTimeMillis();
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");
		logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis()-start) + " ms");
		return (Long)query.uniqueResult();
	}	
	
	@Override
	@SuppressWarnings("unchecked")
	public List<UserRole> findUserRolesByRole(Role role, String order) {
		
		Query<?> query;
		
		String orderby = null;
		
		if (order==null)
			orderby="";
		else if (order.equals("name"))
			orderby="  order by (R.user.lastname, R.user.firstname)";
		
		query = sessionFactory.getCurrentSession().createQuery("FROM KbeeUserRole R where R.role.id=" + ((KbeeAbstractRole)role).getId().toString() + orderby);
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<UserRole>) results;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserRole> findUserRolesByEntityMember(EntityMember  entity) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeUserRole R where R.entity.id=" + ((KbeeEntityMember)entity).getId().toString());
		//query.setCacheable(true);
		//query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<UserRole>) results;
	}
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			evict();
	}
	
	public void setSecurityDao(SecurityDao userDao) {
		this.securityDao = userDao;
	}
	
	protected SecurityDao getSecurityDao() {
		return securityDao;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	private void evict() {
		rules_map.clear();
	}
	
	private Boolean boracle = null;
	
	private boolean isOracle() {
		if (boracle != null)
			return boracle.booleanValue();
		String database = PropertiesFactory.getInstance("kbee").getProperties().getProperty("jdbc.url", null);
		if (database!=null && database.trim().toLowerCase().contains("oracle"))
			boracle = Boolean.valueOf(true);
		else
			boracle = Boolean.valueOf(false);
		return boracle.booleanValue(); 
	}
}
