package com.novamens.kbee.security;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.hibernate.FlushMode;
import org.hibernate.query.Query;
import org.hibernate.SessionFactory;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.lock.Lock;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

/**
 * <p>Dao de Seguridad de bajo nivel. 
 * No tiene conocimiento de {@link Content} ni {@link Domain}</p>
 */
public class KbeeSecurityDao implements SecurityDao {
				
	// static private com.novamens.logging.Logger logger = com.novamens.logging.Logger.getLogger(KbeeSecurityDao.class.getName());
	
	private SessionFactory sessionFactory;
	
	public KbeeSecurityDao() {
	}
	
	public KbeeSecurityDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	
	
	@Override
	public User findUserById(Long id) {
		User user = (User) sessionFactory.getCurrentSession().get(KbeeUser.class, id);
		return user;
	}
	

	public void save(Lock lock) {
		sessionFactory.getCurrentSession().save(lock);
	}
	
	
	
	
	
	@SuppressWarnings("rawtypes")
	public User findUserByName(String username){
		Query query = sessionFactory.getCurrentSession().createQuery("from KbeeUser where username=:username");
		query.setParameter("username", username);
		query.setCacheable(true);
		query.setCacheRegion("query");
		query.setHibernateFlushMode(FlushMode.COMMIT);
		List list = query.list();
		
		if (list.isEmpty()) 
			return null;
		
		return (User)list.get(0);
	}
	

	@Override
	public Set<Principal> getDomainSupportUsers(String domain_id) {
		Query query = sessionFactory.getCurrentSession().createQuery("from KbeeGroup K where K.name='" + KbeeGlobalRole.SUPPORT.getId()+"' AND K.domain.id=" + domain_id);
		List<Group> list =  query.list();
		if (list == null)
			return null;
		return ((KbeeGroup) list.get(0)).getMembers();
	}
	
	
	
	@Override
	public Set<Principal> getDomainAdminUsers(String domain_id) {
		Query query = sessionFactory.getCurrentSession().createQuery("from KbeeGroup K where K.name='" + KbeeGlobalRole.DOMAIN_ADMIN.getId()+"' AND K.domain.id=" + domain_id);
		List<Group> list =  query.list();
		if (list == null)
			return null;
		return ((KbeeGroup) list.get(0)).getMembers();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Group> findGroupByName(String groupName, String domain_id) {
		@SuppressWarnings("rawtypes")
		Query query = sessionFactory.getCurrentSession().createQuery("from KbeeGroup K where K.name=:name AND K.domain.id=" + domain_id);
		query.setParameter("name", groupName);
		return query.list();
	}
	
	public List<Group> findGroupByName(String groupName, Domain domain) {
		return findGroupByName(groupName, String.valueOf(domain.getId()));
	}
	
	public Group findGroupByName(String name) {
		UserProfile callerProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		List<Group> groups = findGroupByName(name, callerProfile.getDomain());
		if (groups.isEmpty()) return null;
		return groups.get(0);
	}
	
	@Override	
	public Principal findPrincipalById(Serializable id) {
		Principal p = ( Principal) sessionFactory.getCurrentSession().get(KbeePrincipal.class, id);
		return p;
	}
	
	
	@Override	
	public Group findGroupById(Serializable id) {
		Group group = (Group) sessionFactory.getCurrentSession().get(KbeeGroup.class, id);
		return group;
	}
	
	@Override
	public Acl findAclById(Serializable id){
		Acl acl = (Acl) sessionFactory.getCurrentSession().load(KbeeAcl.class, id);
		return acl; 
	}
	
	@Override
	public void save(Group group) {
		sessionFactory.getCurrentSession().save(group);
	}
	
	@Override
	public void save(User user) {
		sessionFactory.getCurrentSession().update(user);
	}
	
	@Override
	public void save(Acl acl) {
		sessionFactory.getCurrentSession().save(acl);
	}


}