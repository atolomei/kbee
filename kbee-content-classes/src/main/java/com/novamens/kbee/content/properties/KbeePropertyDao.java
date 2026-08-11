package com.novamens.kbee.content.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ObjectId;
import com.novamens.content.properties.Property;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.user.UserService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.query.KbeeSavedQuery;
import com.novamens.kbee.content.user.KbeeUserProperty;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.kbee.content.userlist.KbeeUserListItem;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.transaction.TransactionSynchronization;
import com.novamens.util.KbeeRuntimeException;

public class KbeePropertyDao implements PropertyDao {
				
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePropertyDao.class.getName());

	
	private Map<Thread, Map<Serializable, List<Property>>> cache = Collections.synchronizedMap(new HashMap<Thread, Map<Serializable, List<Property>>>());
	private Map<Thread, TransactionSynchronization> transactions = Collections.synchronizedMap(new HashMap<Thread, TransactionSynchronization>());

	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void save(Property property) {
		if (property instanceof  KbeeObjectProperty) {
			if (property.getDomain()==null) {
				((KbeeObjectProperty) property).setDomain(getDomain());
			}	
		}
		else {
			if (property instanceof  KbeeProperty) {
				addTransactionSynchronization();
				List<Property> cache = getCache(((KbeeProperty)property).getContent());
				if (!cache.contains(property)) {
					cache.add(property);
				}
			}
		}
		sessionFactory.getCurrentSession().save(property);
	}

	@Override
	public void delete(Property property) {
		sessionFactory.getCurrentSession().delete(property);
	}
	
	@Override
	public Property reload(Property property) {
		sessionFactory.getCurrentSession().refresh(property);
		return property;
	}
	
	public UserList findUserListById(Serializable id) {
			return getUserList(id);
	}
	
	/**
	 * 
	 */
	@Override
	public List<Property> findPropertiesByContent(Content content) {
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeProperty> criteria = criteriabuilder.createQuery(KbeeProperty.class);
		Root<KbeeProperty> properties = criteria.from(KbeeProperty.class);
		ParameterExpression<Long> contentidparameter = criteriabuilder.parameter(Long.class);
		criteria.select(properties).where(criteriabuilder.equal(properties.get("content").get("id"), contentidparameter));
		TypedQuery<KbeeProperty> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(contentidparameter, (long)content.getId());
		
		List<Property> result = new ArrayList<Property>();
		result.addAll(query.getResultList());
		
		for (Property property : getCache(content)) {
			if (!result.contains(property)) {
				result.add(property);
			}
		}
		
		return result;
	}
	
	@Override
	public List<Content> findContentByProperty(String value) {
		List<Content> contents = new ArrayList<>();
		//try {
	        String hql = "FROM KbeeProperty P WHERE P.stringvalue= '" + value.trim() +"'";
	        org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
	        List results = query.list();
	        List<KbeeProperty> properties = (List<KbeeProperty>) results;
	        for (KbeeProperty property : properties) {
	        	contents.add(property.getContent());
	        }
		//}
		return contents;
	}


	/**
	 * 
	 * 
	 */

	@Override
	public List<Property> findPropertiesByUser(User user) {
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserProperty> criteria = criteriabuilder.createQuery(KbeeUserProperty.class);
		Root<KbeeUserProperty> properties = criteria.from(KbeeUserProperty.class);
		ParameterExpression<Long> contentidparameter = criteriabuilder.parameter(Long.class);
		criteria.select(properties).where(criteriabuilder.equal(properties.get("user").get("id"), contentidparameter));
		TypedQuery<KbeeUserProperty> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(contentidparameter, (long) user.getId());
		List<Property> result = new ArrayList<Property>();
		result.addAll(query.getResultList());
		return result;
	}

	
	/**
	 * 
	 */
	@Override
	public List<Property> findPropertiesByObject(com.novamens.dom.Object object) {
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeObjectProperty> criteria = criteriabuilder.createQuery(KbeeObjectProperty.class);
		Root<KbeeObjectProperty> properties = criteria.from(KbeeObjectProperty.class);
		ParameterExpression<String> objectidparameter = criteriabuilder.parameter(String.class);
		criteria.select(properties).where(criteriabuilder.equal(properties.get("objectId"), objectidparameter));
		TypedQuery<KbeeObjectProperty> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(objectidparameter, new ObjectId(object).toString());
		List<Property> result = new ArrayList<Property>();
		result.addAll(query.getResultList());
		return result;
	}
	
	/**
	 * 
	 */
	@Override
	public List<Property> findPropertiesByUser(User user, String set) {
		return findPropertiesByUser(user,set, 20000);
	}
	
	/**
	 * 
	 */
	@Override
	public List<Property> findPropertiesByUser(User user, String set, int maxItems) {
		
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserProperty> criteria = criteriabuilder.createQuery(KbeeUserProperty.class);
		
		Root<KbeeUserProperty> properties = criteria.from(KbeeUserProperty.class);
		
		ParameterExpression<Long> 	useridparameter = criteriabuilder.parameter(Long.class);
		ParameterExpression<String> setparameter	= criteriabuilder.parameter(String.class);

		criteria.select(properties).where(criteriabuilder.and(criteriabuilder.equal(properties.get("user").get("id"), useridparameter),  criteriabuilder.equal(properties.get("set"), setparameter)));
		criteria.select(properties).orderBy(criteriabuilder.desc(properties.get("lastModifiedDate")));
		
		TypedQuery<KbeeUserProperty> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setMaxResults(maxItems);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(useridparameter, (long) user.getId());
		query.setParameter(setparameter, set);
		
		List<Property> result = new ArrayList<Property>();
		result.addAll(query.getResultList());
		
		return result;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	

	@Override
	public List<UserListItem> getContentUserListItem(Serializable content_oid, Serializable user_id, String console) {
		throw new KbeeRuntimeException ("not done");
	}
		
	
	/**
	 * <p>{@code Content} -> returns all where <b>Oid<b> is the same as the Object</p>
	 */
	@Override
	public List<UserListItem> getContentUserListItem(Serializable content_oid) {
				
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserListItem> criteria = criteriabuilder.createQuery(KbeeUserListItem.class);
		
		Root<KbeeUserListItem> properties = criteria.from(KbeeUserListItem.class);
		ParameterExpression<Long> objectparameter = criteriabuilder.parameter(Long.class);
		
		criteria.select(properties).where(criteriabuilder.equal(properties.get("oid"), objectparameter));
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("content").get("id")));
		
		TypedQuery<KbeeUserListItem> query = sessionFactory.getCurrentSession().createQuery(criteria);
		// query.setMaxResults(10000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		
		query.setParameter(objectparameter, (long) content_oid); 

		List<UserListItem> res = new ArrayList<UserListItem>();
		res.addAll(query.getResultList());
		
		return res;
	}

	@Override
	public List<UserListItem> getMemberUserListItem(Serializable id) {
				
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserListItem> criteria = criteriabuilder.createQuery(KbeeUserListItem.class);
		
		Root<KbeeUserListItem> properties = criteria.from(KbeeUserListItem.class);
		ParameterExpression<Long> objectparameter = criteriabuilder.parameter(Long.class);
		
		criteria.select(properties).where(criteriabuilder.equal(properties.get("datasetmember").get("id"), objectparameter));
		TypedQuery<KbeeUserListItem> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(objectparameter, (long) id); 
		List<UserListItem> res = new ArrayList<UserListItem>();
		res.addAll(query.getResultList());
		
		return res;
	}
	
	
	
	@Override
	public UserList getUserList(Serializable listid) {
		UserList  list = (UserList) sessionFactory.getCurrentSession().get(KbeeUserList.class, listid);
		return list;
	}
	
	/**
	 * 
	 */
	@Override
	public List<UserList> getUserLists(User user, String console, com.novamens.dom.Object object) {
		
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserListItem> criteria = criteriabuilder.createQuery(KbeeUserListItem.class);
		
		Root<KbeeUserListItem> properties = criteria.from(KbeeUserListItem.class);
									
		ParameterExpression<Long> 			useridparameter = criteriabuilder.parameter(Long.class);
		ParameterExpression<String> 		consoleparameter = criteriabuilder.parameter(String.class);
		ParameterExpression<Long> 			objectparameter = criteriabuilder.parameter(Long.class);

		String ocontainer= null;
		
		if (object instanceof Content)						ocontainer="content";
		else if (object instanceof DataSetMember)			ocontainer="datasetmember";
		else if (object instanceof User)					ocontainer="userItem";

		if (ocontainer!=null) {

			criteria.select(properties).where(
					criteriabuilder.and(
						criteriabuilder.and
						(
							criteriabuilder.equal(properties.get("owner").get("id"), useridparameter),  
							criteriabuilder.equal(properties.get("console"), consoleparameter)
						),
						criteriabuilder.equal(properties.get(ocontainer).get("id"), objectparameter)
					)
				);
		}
		else {
			return new ArrayList<UserList>();
		}
		
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("userlist").get("title")));
		
		TypedQuery<KbeeUserListItem> query = sessionFactory.getCurrentSession().createQuery(criteria);
		// query.setMaxResults(10000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		
		query.setParameter(useridparameter, (long) user.getId());
		query.setParameter(consoleparameter, console);
		query.setParameter(objectparameter, (long) object.getId()); 
		
		List<KbeeUserListItem> list = query.getResultList();
		List<UserList> result = new ArrayList<UserList>();
		
		for (KbeeUserListItem i: list) 
			result.add(i.getUserlist());
		
		return result;
	}


	
	
	/**
	 * 
	 * Note that console and siteid in UlserListItem are redundant, they exist to speed up queries
	 * 
	 * 
	 */
	@Override
	public List<UserList> getUserLists(User user, Site site, com.novamens.dom.Object object) {
		
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserListItem> criteria = criteriabuilder.createQuery(KbeeUserListItem.class);
		
		Root<KbeeUserListItem> properties = criteria.from(KbeeUserListItem.class);
									
		ParameterExpression<Long> 			useridparameter = criteriabuilder.parameter(Long.class);
		ParameterExpression<Long> 			siteidparameter = criteriabuilder.parameter(Long.class);
		ParameterExpression<Long> 			objectparameter = criteriabuilder.parameter(Long.class);

		String ocontainer= null;
		
		if (object instanceof Content)						ocontainer="content";
		else if (object instanceof DataSetMember)			ocontainer="datasetmember";
		else if (object instanceof User)					ocontainer="userItem";

		if (ocontainer!=null) {

			criteria.select(properties).where(
					criteriabuilder.and(
						criteriabuilder.and
						(
							criteriabuilder.equal(properties.get("owner").get("id"), useridparameter),  
							criteriabuilder.equal(properties.get("site").get("id"), siteidparameter)
						),
						criteriabuilder.equal(properties.get(ocontainer).get("id"), objectparameter)
					)
				);
		}
		else {
			return new ArrayList<UserList>();
		}
		
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("userlist").get("title")));
		
		TypedQuery<KbeeUserListItem> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setMaxResults(1000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		
		query.setParameter(useridparameter, (long) user.getId());
		query.setParameter(siteidparameter, (long) site.getId());
		query.setParameter(objectparameter, (long) object.getId()); 
		
		List<KbeeUserListItem> list = query.getResultList();
		List<UserList> result = new ArrayList<UserList>();
		
		for (KbeeUserListItem i: list) 
			result.add(i.getUserlist());
		
		return result;
	}
	
	@Override
	public List<UserList> getUserLists(User user, String console) {
	
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserList> criteria = criteriabuilder.createQuery(KbeeUserList.class);
		
		Root<KbeeUserList> properties = criteria.from(KbeeUserList.class);
		
		ParameterExpression<Long> 			useridparameter = criteriabuilder.parameter(Long.class);
		ParameterExpression<String> 		consoleparameter = criteriabuilder.parameter(String.class);
		
		criteria.select(properties).where(criteriabuilder.and(criteriabuilder.equal(properties.get("owner").get("id"), useridparameter),  criteriabuilder.equal(properties.get("console"), consoleparameter)));
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("title")));
		
		TypedQuery<KbeeUserList> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setMaxResults(10000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		
		query.setParameter(useridparameter, (long) user.getId());
		query.setParameter(consoleparameter, console);
		
		List<UserList> result = new ArrayList<UserList>();
		result.addAll(query.getResultList());
		return result;
	}

	
	/**
	 * 
	 */
	@Override
	public List<UserList> getUserLists(User user) {

		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserList> criteria = criteriabuilder.createQuery(KbeeUserList.class);
		Root<KbeeUserList> properties = criteria.from(KbeeUserList.class);
		ParameterExpression<Long> 	useridparameter = criteriabuilder.parameter(Long.class);
		criteria.select(properties).where(criteriabuilder.equal(properties.get("owner").get("id"), useridparameter));
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("console")));
		
		TypedQuery<KbeeUserList> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setMaxResults(20000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(useridparameter, (long) user.getId());
		
		List<UserList> result = new ArrayList<UserList>();
		result.addAll(query.getResultList());
		return result;
	}
	
	@Override
	public void save(UserList list) {
		sessionFactory.getCurrentSession().save(list);
	}
	@Override
	public void delete(UserList list) {
		sessionFactory.getCurrentSession().delete(list);
	}
	
	@Override
	public UserList reload(UserList list) {
		sessionFactory.getCurrentSession().refresh(list);
		return list;
	}
	
	@Override
	public void save( UserListItem item) {
		sessionFactory.getCurrentSession().save(item);
	}
	
	@Override
	public void delete(UserListItem item) {
		sessionFactory.getCurrentSession().delete(item);
	}
	
	@Override
	public void deleteAllItems(UserList list) {
		for (UserListItem item: list.getItems()) {
			delete(item);
		}
	}

	@Override
	public long getTotalListConsole(User user, String console) {
		String hql = "select count(*) FROM KbeeUserList U where U.owner.id="+user.getId().toString()+" and U.console='" + console + "'";
		long start = System.currentTimeMillis();
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		//query.setCacheRegion("");
		logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis()-start) + " ms");
		return (Long)query.uniqueResult();
	}
	
	/**
	 *  SavedQuery 
	 */
	public List<SavedQuery> getSavedQueries(User user, String console) {
						
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeSavedQuery> criteria = criteriabuilder.createQuery(KbeeSavedQuery.class);
		
		Root<KbeeSavedQuery> properties = criteria.from(KbeeSavedQuery.class);
		
		ParameterExpression<Long> 	useridparameter = criteriabuilder.parameter(Long.class);
		
		criteria.select(properties).where(criteriabuilder.and(criteriabuilder.equal(properties.get("user").get("id"), useridparameter),  criteriabuilder.equal(properties.get("console"), console)));
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("title")));
		
		
		TypedQuery<KbeeSavedQuery> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setMaxResults(10000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(useridparameter, (long) user.getId());
		
		List<SavedQuery> result = new ArrayList<SavedQuery>();
		result.addAll(query.getResultList());
		return result;
	}
	
	

	/**
	 *  SavedQuery 
	 */
	public List<SavedQuery> getSavedQueries(User user, Site site) {
						
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeSavedQuery> criteria = criteriabuilder.createQuery(KbeeSavedQuery.class);
		
		Root<KbeeSavedQuery> properties = criteria.from(KbeeSavedQuery.class);
		
		ParameterExpression<Long> 	useridparameter = criteriabuilder.parameter(Long.class);
		ParameterExpression<Long> 	siteidparameter = criteriabuilder.parameter(Long.class);
		
		criteria.select(properties).where(criteriabuilder.and(criteriabuilder.equal(properties.get("user").get("id"), useridparameter),  criteriabuilder.equal(properties.get("site").get("id"), siteidparameter)));
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("title")));
		
		TypedQuery<KbeeSavedQuery> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setMaxResults(10000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		
		query.setParameter(useridparameter, (long) user.getId());
		query.setParameter(siteidparameter, (long) site.getId());
		
		List<SavedQuery> result = new ArrayList<SavedQuery>();
		result.addAll(query.getResultList());
		return result;
	}

	
	
	
	/**
	 *  SavedQuery 
	 */
	@Override
	public List<UserList> getUserLists(User user, Site site) {
						
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeUserList> criteria = criteriabuilder.createQuery(KbeeUserList.class);
		
		Root<KbeeUserList> properties = criteria.from(KbeeUserList.class);
		
		ParameterExpression<Long> 			useridparameter = criteriabuilder.parameter(Long.class);
		ParameterExpression<Long> 	siteidparameter = criteriabuilder.parameter(Long.class);
		
		criteria.select(properties).where(criteriabuilder.and(criteriabuilder.equal(properties.get("owner").get("id"), useridparameter),  criteriabuilder.equal(properties.get("site").get("id"), siteidparameter)));
		criteria.select(properties).orderBy(criteriabuilder.asc(properties.get("title")));
		
		TypedQuery<KbeeUserList> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setMaxResults(1000);
		
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		
		query.setParameter(useridparameter, (long) user.getId());
		query.setParameter(siteidparameter, (long) site.getId());
		
		
		List<UserList> result = new ArrayList<UserList>();
		result.addAll(query.getResultList());
		return result;

	}
	
	@Override
	public void save(SavedQuery sq) {
		sessionFactory.getCurrentSession().save(sq);
	}

	@Override
	public void delete(SavedQuery sq) {
		sessionFactory.getCurrentSession().delete(sq);
	}
	
	private List<Property> getCache(Content content) {
		Map<Serializable, List<Property>> threadcache = cache.get(Thread.currentThread());
		if (threadcache==null) {
			threadcache = new HashMap<Serializable, List<Property>>();
			cache.put(Thread.currentThread(), threadcache);
		}
		List<Property> contentcache = threadcache.get(content.getId());
		if (contentcache==null) {
			contentcache = new ArrayList<Property>();
			threadcache.put(content.getId(), contentcache);
		}
		return contentcache;
	}
	
	private void addTransactionSynchronization() {
		if (transactions.get(Thread.currentThread()) == null) {
			transactions.put(Thread.currentThread(), new TransactionSynchronization() {
				public void afterCompletion(int status) {
					try {
						cache.remove(Thread.currentThread());
					}
					catch (RuntimeException e) {
					}
					finally {
						transactions.remove(Thread.currentThread());
					}
				}
			});
		}
	}
}
