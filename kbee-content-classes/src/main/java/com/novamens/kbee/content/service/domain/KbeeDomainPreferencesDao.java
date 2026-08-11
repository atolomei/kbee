package com.novamens.kbee.content.service.domain;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;

import com.novamens.content.service.DomainPreferences;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;



public class KbeeDomainPreferencesDao implements Dao {
			
	private SessionFactory sessionFactory;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainPreferencesDao.class.getName());
	
	public KbeeDomainPreferencesDao() {
	}

	/**
	 */ 
	public KbeeDomainPreferencesDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @param preference
	 */
	public void save(DomainPreferences preference) {
		sessionFactory.getCurrentSession().save(preference);
	}

	
	/**
	 */ 
	public DomainPreferences findDomainPreferences(Domain domain, String name) {
		try {
			
			CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
			CriteriaQuery<KbeeDomainPreferences> criteria = criteriabuilder.createQuery(KbeeDomainPreferences.class);
			
			Root<KbeeDomainPreferences> preferences = criteria.from(KbeeDomainPreferences.class);
			
			ParameterExpression<Long> didparameter = criteriabuilder.parameter(Long.class);
			ParameterExpression<String> nameparameter = criteriabuilder.parameter(String.class);
			
			criteria.select(preferences).where(criteriabuilder.and(
				criteriabuilder.equal(preferences.get("domain").get("id"), didparameter),
				criteriabuilder.equal(preferences.get("name"), nameparameter)));
			
			TypedQuery<KbeeDomainPreferences> query = sessionFactory.getCurrentSession().createQuery(criteria);
			query.setHint("org.hibernate.cacheable", true);
			query.setParameter(didparameter, (Long) domain.getId());
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
	public void deleteAllDomainPreferences(Domain domain) {
		try {
			org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeDomainPreferences K where K.domain.id="+domain.getId().toString());
			query.executeUpdate();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
