package com.novamens.kbee.content.dao;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.DomainWipeDao;
import com.novamens.dom.Domain;
import com.novamens.logging.DomainDeleteEvent;
import com.novamens.service.ServiceLocator;


/** -----------------------------------------------------------------------------
 * 
 * 
 */

public class KbeeDomainWipeDao implements DomainWipeDao {

	static private Logger logger = LogManager.getLogger(KbeeDomainWipeDao.class.getName());
	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");
	
	private SessionFactory sessionFactory;
	private String schema;

	/**
	 * Set up by Spring
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	

	public void setDataSource(DataSource dataSource) {
	}	
	
	
	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteDomain(Domain domain) throws ContentMgmtException {

		
		// Resources
		// Contents

		
		// ViewVK
		// Site, Page, Area, Block
		
		// Comment
		// Answer
		// Question
		
		// ContentClasses
		// Classifiers
		// DataSets


		deleteAllNotification(domain);
		deleteAllLogEvent(domain);
		
		
		// deleteAllVote(domain); 			// no domain
		// deleteAllComment(domain); 		// no domain
		// deleteAllSuscription(domain); PENDING

		// deleteAllResources(domain);
		// deleteAllContents(domain);


		// DataSet Values
		//
		deleteAllDatasetMembers(domain);
		
		
		// Model
		//
		//deleteAllContentClasses(domain);
		//deleteAllClassifiers(domain);
		//deleteAllDatasets(domain);


		
		// Security
		//
		// Rules
		// Groups
		// Users (Preferences, Labels, EnotiRule, Vote)

		deleteAllRules(domain);
		deleteAllGroups(domain);
		deleteAllUsers(domain);
		
		 try {
			 getContentDao().delete(domain);
			 DBLogger.info(new DomainDeleteEvent(domain));
		
		 } catch (ConstraintException e) {
			 throw new ContentMgmtException(e);
		}
	}
	
	/** -----------------------------------------------------------------------------
	 */
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllLogEvent(Domain domain) throws ContentMgmtException {
		try {
			org.hibernate.query.Query<?> query;
			logger.info("Deleting ObjectEvents for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from ObjectEvent K where K.domainId=" + domain.getId().toString());
			query.executeUpdate();
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}
	}

	/** -----------------------------------------------------------------------------
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllNotification(Domain domain) throws ContentMgmtException {
		try {
			org.hibernate.query.Query<?> query;
			logger.info("Deleting Notifications for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeNotification K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}
	}

	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllVote(Domain domain) throws ContentMgmtException {

		// try {
			// org.hibernate.query.Query<?> query;
			// logger.info("Deleting Votes for Domain " + domain.getName());
			// query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeVote K where K.user.id=" + domain.getId().toString());
			// query.executeUpdate();
		//}  catch (HibernateException e) {
		//	 throw new ContentMgmtException(e);
		//}
		
		logger.info("Deleting Votes for Domain " + domain.getName() + " [not implemented]");
	}
	

	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllReport(Domain domain) throws ContentMgmtException {
		logger.info("Deleting Report for Domain " + domain.getName() + " [not implemented]");
	}

	/** -----------------------------------------------------------------------------
	 */
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllComment(Domain domain) throws ContentMgmtException {
		try {
			org.hibernate.query.Query<?> query;
			logger.info("Deleting Comments for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeComment K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}

	}

	/** -----------------------------------------------------------------------------
	 */

	@Override
	public void deleteAllSuscription(Domain domain) throws ContentMgmtException {
		logger.info("Deleting Suscription for Domain " + domain.getName() + " [not implemented]");

	}

	/** -----------------------------------------------------------------------------
	

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllENotiRule(Domain domain) throws ContentMgmtException {
		try {
			org.hibernate.query.Query<?> query;
			logger.info("Deleting ENotiRules for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeENotiRule K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}
	}
	 */

	/** -----------------------------------------------------------------------------
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllResources(Domain domain) throws ContentMgmtException {

	}

	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllContents(Domain domain) throws ContentMgmtException {

	}


	/** -----------------------------------------------------------------------------
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllContentClasses(Domain domain) throws ContentMgmtException {

		
		
	}

	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllClassifiers(Domain domain) throws ContentMgmtException {

	}

	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllDatasets(Domain domain) throws ContentMgmtException {
		
	}

	/** -----------------------------------------------------------------------------
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllRules(Domain domain) throws ContentMgmtException {
		try {
			
			org.hibernate.query.Query<?> query;
			logger.info("Deleting SecurityRules for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeSecurityRule K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
			
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}
	}
	

	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllGroups(Domain domain) throws ContentMgmtException {
		try {
			org.hibernate.query.Query<?> query;
			logger.info("Deleting KbeeGroup for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeGroup K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
			
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}
	}

	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllDatasetMembers(Domain domain) throws ContentMgmtException {
		try {
			org.hibernate.query.Query<?> query;
			logger.info("Deleting KbeeDataSetMember for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeDataSetMember K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
			
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}
	}

	
	
	/** -----------------------------------------------------------------------------
	 */

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteAllUsers(Domain domain) throws ContentMgmtException {
		
		try {

			org.hibernate.query.Query<?> query;
			
			// UserProfile
			logger.info("Deleting KbeeUserProfile for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeUserProfile K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
			
			// Person -> Entity
			logger.info("Deleting KbeePerson for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeePerson K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
			
			// User
			logger.info("Deleting KbeeUser for Domain " + domain.getName());
			query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeUser K where K.domain.id=" + domain.getId().toString());
			query.executeUpdate();
			
		} catch (HibernateException e) {
			throw new ContentMgmtException(e);
		}
	}


	/** -----------------------------------------------------------------------------------
	 */
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		return dao;
	}

	
	/** -----------------------------------------------------------------------------
	 */

	@Override
	public void setSchema(String schema) {
		this.schema=schema;
	}

	/** -----------------------------------------------------------------------------------
	 */
	
	@Override
	public String getSchema() {
		if(schema!=null && schema.length()>0)
			return schema+".";
		return "";
	}


}
