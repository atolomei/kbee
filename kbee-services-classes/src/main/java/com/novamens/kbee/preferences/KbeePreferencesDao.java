package com.novamens.kbee.preferences;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;

import com.novamens.dao.Dao;
import com.novamens.preferences.Preferences;
import com.novamens.security.User;

public class KbeePreferencesDao  implements Dao {

	private SessionFactory sessionFactory;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePreferencesDao.class.getName());
	
	public KbeePreferencesDao() {
	}

	/**
	 */ 
	public KbeePreferencesDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @param preference
	 */
	public void save(Preferences preference) {
		sessionFactory.getCurrentSession().save(preference);
	}

	
	public List<Preferences> findPreferencesByPrefix(User user, String prefix) {
		try {
			CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
			CriteriaQuery<KbeePreferences> criteria = criteriabuilder.createQuery(KbeePreferences.class);
			
			Root<KbeePreferences> preferences = criteria.from(KbeePreferences.class);
			
			ParameterExpression<Long> useridparameter = criteriabuilder.parameter(Long.class);
			ParameterExpression<String> nameparameter = criteriabuilder.parameter(String.class);
			
			criteria.select(preferences).where(criteriabuilder.and(
				criteriabuilder.equal(preferences.get("user").get("id"), useridparameter),
				criteriabuilder.like(preferences.get("name"), nameparameter)));
		
			TypedQuery<KbeePreferences> query = sessionFactory.getCurrentSession().createQuery(criteria);
			query.setHint("org.hibernate.cacheable", true);
			query.setParameter(useridparameter, (long)user.getId());
		 	
			query.setParameter(nameparameter, prefix+"%");
		 	
			query.setFlushMode(FlushModeType.COMMIT);
			
			List<KbeePreferences> p_list = (List<KbeePreferences>) query.getResultList();
			List<Preferences> list = new ArrayList<Preferences>(p_list); 
			return list;
			

		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	
	
	/**
	 */ 
	public Preferences findPreferences(User user, String name) {
		try {
			CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
			CriteriaQuery<KbeePreferences> criteria = criteriabuilder.createQuery(KbeePreferences.class);
			Root<KbeePreferences> preferences = criteria.from(KbeePreferences.class);
			ParameterExpression<Long> useridparameter = criteriabuilder.parameter(Long.class);
			ParameterExpression<String> nameparameter = criteriabuilder.parameter(String.class);
			criteria.select(preferences).where(criteriabuilder.and(
				criteriabuilder.equal(preferences.get("user").get("id"), useridparameter),
				criteriabuilder.equal(preferences.get("name"), nameparameter)));
		
			TypedQuery<KbeePreferences> query = sessionFactory.getCurrentSession().createQuery(criteria);
			query.setHint("org.hibernate.cacheable", true);
			query.setParameter(useridparameter, (long)user.getId());
		 	query.setParameter(nameparameter, name);
			query.setFlushMode(FlushModeType.COMMIT);
		 	
		 	return !query.getResultList().isEmpty() ? query.getSingleResult() : null;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	

	/**
	 */ 
	public void deleteAllPreferences(User user) {
		org.hibernate.query.Query<?> query;
		logger.debug("Delete from KbeePreferences K where K.user.id="+user.getId().toString());
		query = sessionFactory.getCurrentSession().createQuery("Delete from KbeePreferences K where K.user.id="+user.getId().toString());
		query.executeUpdate();
		
	}

	public void deleteAllPreferences(User user, String prefix) {

		
		
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery("Delete from KbeePreferences K where K.user.id="+user.getId().toString() + " and  K.name like '" + prefix +"'");
						
		
		logger.debug(query.getQueryString());
		
		
		// "Delete from KbeePreferences K where K.user.id="+user.getId().toString() + " and name like " +prefix +"%"
		
		
		// String hql="Delete from KbeePreferences K where K.user.id="+user.getId().toString() + " and name like " +prefix +"%";
		//logger.debug(hql);
		//query = sessionFactory.getCurrentSession().createQuery(query);
		
		query.executeUpdate();

		
	}
}
