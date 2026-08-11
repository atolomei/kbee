package com.novamens.kbee.content.enoti;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.content.enoti.ENotiRuleDao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.content.enoti.ENotiRule;



public class KbeeENotiRuleDao implements ENotiRuleDao {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeENotiRuleDao.class.getName());

	
	private SessionFactory sessionFactory;

	public KbeeENotiRuleDao() {
	}

	public KbeeENotiRuleDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public ENotiRule findENotiRuleById(Serializable id) {
		ENotiRule rule = (ENotiRule) sessionFactory.getCurrentSession().get(KbeeENotiRule.class, id);
		return rule;
	}

	
	/**
	 * Returns Domain Rules that are ENABLED
	 * whether personal or system
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ENotiRule> getENotiRules(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeENotiRule R where R.domain.id="+String.valueOf(domain.getId())+" and R.state="+String.valueOf(ObjectState.ENABLED.getId()));
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<ENotiRule>) results;
	}

	/**
	 * Returns User Rules that are ENABLED and personal (not system rules)
	 */

	@SuppressWarnings("unchecked")
	@Override
	public List<ENotiRule> getENotiRules(User owner) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeENotiRule R where R.is_system=false AND R.owner.id='"+String.valueOf(owner.getId())+"' and R.state="+String.valueOf(ObjectState.ENABLED.getId()));
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<ENotiRule>) results;
	}

	
	/**
	 * Returns Domain Rules that are ENABLED and for event event_type (both personal and system)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ENotiRule> getENotiRules(Domain domain, int event_type) {
		try {
			
			Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeENotiRule R where R.domain.id="+String.valueOf(domain.getId())+" and event_type=" + String.valueOf(event_type) +" and R.state="+String.valueOf(ObjectState.ENABLED.getId()));
			query.setCacheable(true);
			query.setCacheRegion("query");
			logger.debug(query.getQueryString());
			List<?> results = query.list();
			return (List<ENotiRule>) results;
		} catch (Exception e) {
			logger.error(e);
			throw(e);
			
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ENotiRule> getSystemENotiRules(Domain domain) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("FROM KbeeENotiRule R where R.domain.id="+String.valueOf(domain.getId())+" and R.is_system=true and R.state="+String.valueOf(ObjectState.ENABLED.getId()));
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<?> results = query.list();
		return (List<ENotiRule>) results;
	}
	


	@Override
	public void save(ENotiRule rule) {
		((KbeeENotiRule)rule).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeENotiRule)rule).setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		sessionFactory.getCurrentSession().save(rule);
	}

	@Override
	public void delete(ENotiRule rule) {
		sessionFactory.getCurrentSession().delete(rule);
	}
	
}
