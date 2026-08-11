package com.novamens.kbee.portal.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExternalDao;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.Object;
import com.novamens.dom.ObjectState;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.EventService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

public class KbeeSiteService implements SiteService {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSiteService.class.getName());
												
	/**
	 * Logger Sync with the Trx Thread
	 */
	@SuppressWarnings("unused")
	static private Logger txlogger = LogManager.getLogger("TXLogger");

	private Site site = null;
	private PortalDao dao = null;

	private com.novamens.service.SecurityService secu;

	public KbeeSiteService() {
	}

	public KbeeSiteService(Site site) {
		this.site = site;
	}

	
	@Override
	@Transactional
	public void markAsDeleted() throws ContentMgmtException {
		getSite().setState(ObjectState.DELETED);
		getPortalDao().save(getSite());
		// txlogger.info() PORTAL_EVENT
	}

	// TODO VER AT
	
	@Override
	@Transactional
	public void delete() throws ContentMgmtException {

		boolean is_ok = true;
		/**
		 * --------------------------------------- Remove all classification "portal-home"
		 *  
		try {
			List<Content> list = getPortalDao().getLibraryPortalHomeContents(getSite());
			Classifier cla = getPortalDao().getPortalHomeClassifierSet(getSite());
			if (cla!=null) {
				for (Content c:list) {
					c.removeAllClassification(cla);
					c.getService(ContentService.class).update();
				}
			}
			
		} catch (DataIntegrityViolationException e) {
			logger.error(e);
			is_ok = false;
			markAsDeleted();
			
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
		*/
		
		/**
		 * --------------------------------------- Site (External) DataSetMember
		 */
			try {
				DataSet ext_site = getExternalDao().getSiteDataSet(getDomain());
				if (ext_site != null) {
					DataSetMember dm = getExternalDao().findMemberByExternalId(site.getOId(), ext_site);
					if (dm != null) {
						DOMObjectService objectService = dm.getService(com.novamens.content.service.DOMObjectService.class);
						objectService.delete();
					}
				}
			} catch (DataIntegrityViolationException  e) {
				logger.error(e);
				getSite().setState(ObjectState.DELETED);
				getPortalDao().save( getSite() );
				
			} catch (ContentMgmtException e) {
				logger.error(e);
				is_ok = false;
				markAsDeleted();
			}

			/**
			 * --------------------------------------- Deletes Site Repository DatasetMember
			 */
			try {
				DataSet ext_repo = getExternalDao().getSiteRepositoryDataSet(getDomain());
				if (ext_repo != null) {
					DataSetMember dm = getExternalDao().findMemberByExternalId(site.getOId(), ext_repo);
					if (dm != null) {
						DOMObjectService objectService = dm.getService(DOMObjectService.class);
						objectService.delete();
					}
				}
			} catch (DataIntegrityViolationException e) {
				logger.error(e);
				is_ok = false;
				markAsDeleted();
				
				
			} catch (ContentMgmtException e) {
				logger.error(e);

			} catch (Exception e) {
				logger.error(e);
				is_ok = false;
				markAsDeleted();
				
			}

			
			/**
			 * --------------------------------------- if site is "Project" 
			if (site.getSiteType().equals(com.novamens.portal6.model.SiteType.PROJECT)) {
				try {
					DataSet d = getContentDao().findDataSetByAlias(DataSet.PORTAL_PROJECTS, getDomain().getId());
					if (d != null) {
						DataSetMember dm = getExternalDao().findMemberByExternalId(site.getOId(), ext_repo);
						if (dm != null) {
							DOMObjectService objectService = dm.getService(DOMObjectService.class);
							objectService.delete();
						}
					}
				} catch (ConstraintException e) {
					logger.error(e);
				} catch (ContentMgmtException e) {
					logger.error(e);
	
				} catch (Exception e) {
					logger.error(e);
				}
			}
			*/
			
			if (is_ok) {
				
				try {
						getPortalDao().delete(getSite());
				} catch (DataIntegrityViolationException  e) {
						logger.error(e);
						is_ok = false;
						markAsDeleted();
				}
			}
			ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getSite()));

		// TODO: Log Site Delete
		// txlogger.info() PORTAL_EVENT
		//
	}

	/**
	 * @throws IOException
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save() throws ContentMgmtException {
		getPortalDao().save(getSite());
	}
	

	@Override
	@Transactional
	public void recycle() throws ContentMgmtException {
		((Object) getSite()).setState(ObjectState.DELETED);
		getPortalDao().save(getSite());
		// TODO: Log Recycle
		// logger.info(new RemoveEvent(getSite()));
	}

	@Override
	@Transactional
	public void update(String description) throws ContentMgmtException {
		List<String> list = new ArrayList<String>();
		list.add(description);
		update(list);

	}

	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(List<String> updatedParts) throws ContentMgmtException {
		getSite().setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getSite().setLastModifiedUser(getSessionUser());
		getPortalDao().save(getSite());
	}
	
	@Override
	public String getUrl(Content content) {
		String url = getSite().getUrl()+"/doc/" + content.getOId();
		return "/portal/"+url;
	}

	@Override
	public Site getSite() {
		return site;
	}

	public void setPortalDao(PortalDao dao) {
		this.dao = dao;
	}

	public PortalDao getPortalDao() {
		return this.dao;
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}

	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	protected SecurityDao getSecurityDao() {
		return (SecurityDao) ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}

	
	private ContentDao contentDao;
	
	public ContentDao getContentDao() {
		return contentDao;
	}

	public void setContentDao(ContentDao contentDao) {
		this.contentDao = contentDao;
	}
	
	/**
	 * 
	 * @param site
	 * @param page
	 * @return
	 * @throws IOException
	 * @throws ServiceNotFoundException
	 */

	public com.novamens.service.SecurityService getSecurityService() {
		if (this.secu != null)
			return this.secu;

		this.secu = ServiceLocator.getService(com.novamens.service.SecurityService.class);
		return this.secu;
	}

	private ExternalDao getExternalDao() {
		return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
	}

			
	//private ContentDao getContentDao() {
	//	return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	//}

	
	

	 

}

	
	

