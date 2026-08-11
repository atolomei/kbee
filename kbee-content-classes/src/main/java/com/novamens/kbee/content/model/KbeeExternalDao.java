package com.novamens.kbee.content.model;


import java.io.Serializable;
import java.time.OffsetDateTime;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExternalDao;
import com.novamens.content.model.ExternalMember;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.logging.DataSetValueCreateEvent;
import com.novamens.logging.ModelCreateEvent;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/** 
 * Domain 
 * 
 * Sitio, 
 * Repositorio
 */
public class KbeeExternalDao implements ExternalDao, EventListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeExternalDao.class.getName());

	/**
	 * Logger Sync with the Trx Thread
	 */
	 static private Logger txlogger = LogManager.getLogger("TXLogger");
	
	private SessionFactory sessionFactory;
		
	// TODO HA
	//											
	private Map<Long, DataSet> site_dataset				 		= new ConcurrentHashMap<Long, DataSet>();
	private Map<Long, DataSet> site_repository_dataset 			= new ConcurrentHashMap<Long, DataSet>();

	private Map<Long, Classifier> site_classifier 				= new ConcurrentHashMap<Long, Classifier>();
	private Map<Long, Classifier> site_repository_classifier 	= new ConcurrentHashMap<Long, Classifier>();
													
									
	private Map<Long, Classifier> portalhome_classifier 		= new ConcurrentHashMap<Long, Classifier>();
	
	
	
	private ReadWriteLock com_lock = new ReentrantReadWriteLock();
	
 
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/** 
	 * @param id
	 */
	@Override
	public ExternalMember create(Serializable external_id, String value, String url, ObjectState state, DataSet dataset) {
		
		logger.debug(external_id + " | " + value + " | " + url );
		
		ExternalMember member = new KbeeExternalMember(external_id, value, url, dataset);
		member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		member.setLastModifiedUser(getSessionUser());
		member.setDomain(dataset.getDomain());
		member.setState(state);
		((KbeeExternalMember) member).setExternalUrl(url);
		this.sessionFactory.getCurrentSession().save(member);
		return member;
	}

	/**
	 * @param id
	 * @param value
	 * @param url
	 * @param state
	 * @param dataset
	 */
	@Override
	public void update(Serializable external_id, String value, String url, ObjectState state, DataSet dataset) {

		if (external_id==null) 
			return;
		
		if (dataset==null) {
				logger.error("dataset is null.");
				return;
		}
	
		// ----
		//  Si No Existe lo crea,
		//  actualiza si tiene que haecerlo
		// ----

		DataSetMember member = findMemberByExternalId(external_id, dataset);

		if (member == null) {
			dataset = (DataSet) this.sessionFactory.getCurrentSession().get(KbeeDataSet.class, dataset.getId());
			member = new KbeeExternalMember(external_id, value, url, dataset);
			member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			member.setLastModifiedUser(getSessionUser());
			member.setDomain(dataset.getDomain());
			((KbeeExternalMember) member).setExternalUrl(url);
			this.sessionFactory.getCurrentSession().save(member);
			txlogger.info(new DataSetValueCreateEvent(member, "Create"));
			
		}
		else {
		
			boolean modified = false;
			
			boolean is_type= member instanceof com.novamens.content.model.ExternalMember;
						
			if (is_type) {
				
				modified =  changed(value,   ((com.novamens.content.model.ExternalMember) member).getStrValue()) ||
							changed(url,     ((com.novamens.content.model.ExternalMember) member).getExternalUrl()) ||
							changed(state,   ((com.novamens.content.model.ExternalMember) member).getState());
			}
			
			if (modified) {
				
				member.setStrValue(value);
				member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				member.setLastModifiedUser(getSessionUser());
				((KbeeExternalMember) member).setExternalUrl(url);
				member.setState(state);
				
				this.sessionFactory.getCurrentSession().save(member);
				//txlogger.info(new DataSetValueUpdateEvent(member, "Url, Value, Status"));
				
			}
		}
	}

		
	/** 
	 * @param id
	 * @param value
	 * @param dataset
	 */
	public void delete(Serializable id, String value, DataSet dataset) {

		if (id==null) { 
			logger.error("trying to delete DataSet with id null. value " + value + " Dataset: " + dataset);
			return;
		}
		if (dataset==null) {
			logger.error("trying to delete DataSet null. id: "+ id.toString() +" value " + value);
				return;
		}

		DataSetMember member = findMemberByExternalId(id, dataset);
		
		if (member != null) {
			try {
				sessionFactory.getCurrentSession().delete(member);
				//txlogger.info(new DataSetValueDeleteEvent(member, "Delete"));
				
			} catch (Exception e) {
				
				logger.error(e);
				
				try {
					String url  = (member instanceof ExternalMember ? ((ExternalMember) member).getExternalUrl() : null); 
					update(id, value, url, ObjectState.DELETED, dataset);
					
				} catch (Exception e2) {
					logger.error(e);
				}
			}
		}
	}

	

	public DataSetMember findMemberByExternalId(Serializable id) {
		return findMemberByExternalId(id, null);
	}
	

	@Override
	public DataSetMember findMemberByExternalId(Serializable id, DataSet dataset) {
	
		if (id == null || dataset==null)
			return null;
		
		String external_dataset_id =dataset.getId().toString();
		
		String hql = "FROM KbeeExternalMember M WHERE M.external_member_id = " + id.toString() +	
				(external_dataset_id!=null?
				" AND  M.dataset.id=" + external_dataset_id.toString()
				:"");
		
		
		logger.debug(hql);
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		
		@SuppressWarnings("rawtypes")
		List results = query.list();
		
		if (results.isEmpty())
			return null;
		DataSetMember member = (DataSetMember) results.get(0); 
		return member;
	}
	

	public void deleteAll(Domain domain, String dataSetName) {
		DataSet dataset = getContentDao().findDataSetByName(dataSetName, domain.getId());
		if (dataset!=null) {
			String hql = "FROM KbeeExternalMember M WHERE M.domain.id =" + domain.getId().toString()+" AND M.dataset.id="+ dataset.getId().toString();
			org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
			@SuppressWarnings("rawtypes")
			List results = query.list();
			for (Object member: results) {
				try {
					getContentDao().delete((DataSetMember) member);
				} catch (Exception e) {
						logger.error(e);
				}
			}
		}
	}

	public User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	public void resetCache() {
		this.site_dataset.clear();
		this.site_repository_dataset.clear();
	}

	@Override
	public DataSet createSiteDataSetIfNotExists(Domain domain)  throws ContentMgmtException {
		
		synchronized (this) {
			List<DataSet> datasets = getContentDao().getDataSets(domain);
			for (DataSet dataset: datasets) {
				if (dataset instanceof KbeeExternalSet && dataset.getDataSetType()==DataSetType.EXTERNAL) {
					if (((KbeeExternalSet) dataset).getExternalSubtype()==KbeeExternalSet.EXTERNAL_SITE) {
						return dataset;
					}
				}
			}
			
			String name = ResourceBundle.getBundle(getClass().getName(), domain.getLocale()).getString("site");
			
			KbeeExternalSet dataset = new KbeeExternalSet(name, DataSetType.EXTERNAL);
			
			dataset.setDomain(domain);
			dataset.setAlias(DataSet.PORTAL);
			dataset.setState(ObjectState.ENABLED);
			dataset.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			dataset.setLastModifiedUser(getSessionUser());
			dataset.setExternalSubtype(KbeeExternalSet.EXTERNAL_SITE);
			this.sessionFactory.getCurrentSession().save(dataset);
			txlogger.info(new ModelCreateEvent(dataset, "Create"));
			return dataset;
			
		}
	}
	

	@Override
	public DataSet createSiteRespositoryDataSetIfNotExists(Domain domain)  throws ContentMgmtException {
		
		synchronized (this) {
			List<DataSet> datasets = getContentDao().getDataSets(domain);
			for (DataSet dataset: datasets) {
				if (dataset instanceof KbeeExternalSet && dataset.getDataSetType()==DataSetType.EXTERNAL) {
					if (((KbeeExternalSet) dataset).getExternalSubtype()==KbeeExternalSet.EXTERNAL_SITE_REPOSITORY) {
						return dataset;
					}
				}
			}
			
			ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), domain.getLocale());
			
			KbeeExternalSet dataset = new KbeeExternalSet(resources.getString("site-repository"), DataSetType.EXTERNAL);
			dataset.setDomain(domain);
			dataset.setAlias(DataSet.PORTAL_REPOSITORY);
			dataset.setState(ObjectState.ENABLED);
			dataset.setLastModifiedUser(getSessionUser());
			dataset.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			dataset.setExternalSubtype(KbeeExternalSet.EXTERNAL_SITE_REPOSITORY);
			this.sessionFactory.getCurrentSession().save(dataset);
			txlogger.info(new ModelCreateEvent(dataset, "Create"));
			return dataset;
		}
	}
 
	
		
	@Override
	public Classifier createSiteClassifierIfNotExists(Domain domain, DataSet dataset)  throws ContentMgmtException {

		if (dataset==null)
		dataset = getSiteDataSet(domain);
		
		if (dataset!=null) {
			
			if (getSiteClassifier(domain)!=null)
				return  getSiteClassifier(domain);
			
			
			ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), domain.getLocale());

			KbeeClassifier clasi = new KbeeClassifier(dataset, resources.getString("site"));
			
			clasi.setDomain(domain);
			clasi.setPredicate(clasi.getName());
			clasi.setAlias(Classifier.PORTAL_CLASSIFIER_ALIAS);
			clasi.setUniqueName(Classifier.PORTAL_CLASSIFIER_SOLR);
			clasi.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			clasi.setLastModifiedUser(getSessionUser());
			clasi.setMultiplicity(Multiplicity.M01);
			clasi.setState(ObjectState.ENABLED);
			clasi.setSemantic(false);
			
			clasi.setVisibility("workspace", false);
			clasi.setVisibility("monitor", false);
			clasi.setVisibility("pending", false);
			clasi.setVisibility("all", false);
			
			for( Library lin: getLibraries(domain))
				clasi.setVisibility(lin.getKey(), false);

			sessionFactory.getCurrentSession().save(clasi);
			txlogger.info(new ModelCreateEvent(clasi, "Create"));
			return clasi;
		}
		else
			throw new ContentMgmtException( "Site DataSet does not exist");
	}
	
	
	/**
	 * 		
	 */
	@Override
	public Classifier createSiteRepositoryClassifierIfNotExists(Domain domain, DataSet dataset)  throws ContentMgmtException {

		if (dataset==null)
		dataset = getSiteRepositoryDataSet(domain);
		
		if (dataset!=null) {
			
			if (getSiteRepositoryClassifier(domain)!=null)
				return getSiteRepositoryClassifier(domain);
			

			ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), domain.getLocale());
			
			KbeeClassifier clasi = new KbeeClassifier(dataset, resources.getString("site-repository"));
			
			clasi.setDomain(domain);
			clasi.setUniqueName(Classifier.PORTAL_REPOSITORY_SOLR);
			clasi.setPredicate(Classifier.PORTAL_REPOSITORY_PREDICATE); //  "SiteRepository"
			clasi.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			clasi.setLastModifiedUser(getSessionUser());
			clasi.setMultiplicity(Multiplicity.M01);
			clasi.setState(ObjectState.ENABLED);
			clasi.setSemantic(false);
			
			clasi.setVisibility("workspace", false);
			clasi.setVisibility("monitor", false);
			clasi.setVisibility("pending", false);
			clasi.setVisibility("all", false);
			for( Library lin: getLibraries(domain)) {
				clasi.setVisibility(lin.getKey(), false);
			}
			
			sessionFactory.getCurrentSession().save(clasi);
			txlogger.info(new ModelCreateEvent(clasi, "Create"));
			return clasi;
		}
		else
			throw new ContentMgmtException( "SiteRepository DataSet does not exist");
	}


	
	
	
	
	/**
	 * 		
	 */
	@Override
	public Classifier createPortalHomeClassifierIfNotExists(Domain domain)  throws ContentMgmtException {

	
		
			Classifier c = getPortalHomeClassifier(domain);
			if (c!=null)
				return c;
			
		
			DataSet site_dataset = getSiteDataSet(domain);
			
			ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), domain.getLocale());
			KbeeClassifier clasi = new KbeeClassifier(site_dataset, resources.getString("portal-home"));
			
			clasi.setDomain(domain);
			clasi.setUniqueName(Classifier.PORTAL_HOME_CLASSIFIER_SOLR);
			clasi.setAlias(Classifier.PORTAL_HOME_CLASSIFIER_ALIAS);
			clasi.setPredicate(Classifier.PORTAL_HOME_PREDICATE);
			clasi.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			clasi.setLastModifiedUser(getSessionUser());
			clasi.setMultiplicity(Multiplicity.M0N);
			clasi.setState(ObjectState.ENABLED);
			clasi.setSemantic(false);
			
			clasi.setVisibility("workspace", false);
			clasi.setVisibility("monitor", false);
			clasi.setVisibility("pending", false);
			clasi.setVisibility("all", false);
			
			for( Library lin: getLibraries(domain))
				clasi.setVisibility(lin.getKey(), false);
			
			sessionFactory.getCurrentSession().save(clasi);
			txlogger.info(new ModelCreateEvent(clasi, "Create"));
			return clasi;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public Classifier getSiteClassifier(Domain domain) {
	
		if (domain==null)
			return null;
		
		if (this.site_classifier.containsKey((Long) domain.getId()))
			return site_classifier.get((Long) domain.getId());
		
		synchronized (this) {
			
			DataSet dataset = getSiteDataSet(domain);
			
			if (dataset==null)
				return null;
			
			List<Classifier> list_clasi = getContentDao().getClassifiers(domain);
			
			Classifier hit = null;
			for (Classifier clasi: list_clasi) {
				if (clasi.getDataSet().equals(dataset)) {
					hit=clasi;
					break;
				}
			}
			
			if (hit==null) 
				return null;
			
			this.site_classifier.put((Long) domain.getId(), hit);
			return site_classifier.get((Long) domain.getId());
		}
	}


	
	@Override				
	public Classifier getPortalHomeClassifier(Domain domain) {
	
		if (domain==null)
			return null;
		
		if (this.portalhome_classifier.containsKey((Long) domain.getId()))
			return portalhome_classifier.get((Long) domain.getId());
		
		synchronized (this) {
			List<Classifier> list_clasi = getContentDao().getClassifiers(domain);
			Classifier hit = null;
			for (Classifier clasi: list_clasi) {
				if (clasi.getAlias()!=null && clasi.getAlias().equals(Classifier.PORTAL_HOME_CLASSIFIER_ALIAS)) {
					hit=clasi;
					break;
				}
			}
			
			if (hit==null) 
				return null;
			
			this.portalhome_classifier.put((Long) domain.getId(), hit);
			return this.portalhome_classifier.get((Long) domain.getId());
		}
	}


	

	@Override
	public Classifier getSiteRepositoryClassifier(Domain domain) {
	
		if (domain==null)
			return null;
		
		if (this.site_repository_classifier.containsKey((Long) domain.getId()))
			return site_repository_classifier.get((Long) domain.getId());
		
		synchronized (this) {
			
			DataSet dataset = getSiteRepositoryDataSet(domain);
			
			if (dataset==null)
				return null;
			
			List<Classifier> list_clasi = getContentDao().getClassifiers(domain);
			
			Classifier hit = null;
			for (Classifier clasi: list_clasi) {
				if (clasi.getDataSet().equals(dataset)) {
					hit=clasi;
					break;
				}
			}
			
			if (hit==null) 
				return null;
			
			this.site_repository_classifier.put((Long) domain.getId(), hit);
			
			return site_repository_classifier.get((Long) domain.getId());
		}
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public DataSet getSiteDataSet(Domain domain) {
	
		if (domain==null)
			return null;
		
		if (!this.site_dataset.containsKey((Long) domain.getId())) {
			synchronized (this) {
				List<DataSet> datasets = getContentDao().getDataSets(domain);
				for (DataSet dataset: datasets) {
					if (dataset instanceof KbeeExternalSet && dataset.getDataSetType()==DataSetType.EXTERNAL) {
						if (((KbeeExternalSet) dataset).getExternalSubtype()==KbeeExternalSet.EXTERNAL_SITE) {
							this.site_dataset.put((Long) domain.getId(), dataset);
							break;
						}
					}
				}
			}
		}
		return site_dataset.get((Long) domain.getId());
	}


	@Override
	public DataSet getSiteRepositoryDataSet(Domain domain) {

		if (domain==null)
			return null;

		if (! this.site_repository_dataset.containsKey((Long) domain.getId())) {
			synchronized (this) {
				List<DataSet> datasets = getContentDao().getDataSets(domain);
				for (DataSet dataset: datasets) {
					if (dataset instanceof KbeeExternalSet && dataset.getDataSetType()==DataSetType.EXTERNAL) {
						if (((KbeeExternalSet) dataset).getExternalSubtype()==KbeeExternalSet.EXTERNAL_SITE_REPOSITORY) {
							this.site_repository_dataset.put((Long) domain.getId(), dataset);
							break;
						}
					}
				}
		}
		}
		return this.site_repository_dataset.get((Long) domain.getId());
	}
	
 

	private boolean changed( String value1, String value2) {
		
		boolean null_and_not_null = ((value1==null && value2!=null) ||
				                    (value1!=null && value2==null));

		if (null_and_not_null)
			return true;
		
		boolean both_null = (value1==null && value2==null);
		
		if (both_null)
			return false;
		
		if (value1.equals(value2))
			return false;
		
		return true;
		
	}

 

	private boolean changed(ObjectState value1, ObjectState value2) {
		
		boolean null_and_not_null = ((value1==null && value2!=null) ||
				                    (value1!=null && value2==null));

		if (null_and_not_null)
			return true;
		
		boolean both_null = (value1==null && value2==null);
		
		if (both_null)
			return false;
		
		if (value1.getId()==value2.getId())
			return false;
		
		return true;
		
	}

	private List<Library> getLibraries(Domain domain) {
		return ServiceLocator.getService(DomRepositoryService.class).getRepository(Library.class).findAll(domain);
	}
	

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

    @Override
    public boolean listen(Event event) {
        if (event instanceof EvictCacheServiceEvent)
            return true;
        return false;
    }

    @Override
    public void onEvent(Event event) {
        logger.debug("Evict Cache Received");
        try {
            this.com_lock.writeLock().lock();
        	site_dataset.clear();
        	site_repository_dataset.clear();
        	site_classifier.clear();
        	site_repository_classifier.clear();
        	portalhome_classifier.clear();
        	
        } finally {
            this.com_lock.writeLock().unlock();
        }
    }


}
