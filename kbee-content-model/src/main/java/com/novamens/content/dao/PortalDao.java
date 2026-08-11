package com.novamens.content.dao;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ObjectId;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectID;
import com.novamens.dom.ObjectState;
import com.novamens.portal.favorites.SiteFavorites;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.ViewDetailContent;
import com.novamens.security.User;

public interface PortalDao extends Dao {

		public Classifier getPortalHomeClassifierSet(Site site);		
		public List<Content> getLibraryPortalHomeContents(Site site);
		public List<Content> getLibrarySiteIQLContents(Site site, String iql, int max);
		public List<Content> getLibrarySiteIQLContents(Site site, String iql, int max, String sort);
		public List<Content> getLibrarySiteQueryContents(Site site, String statement, int max);
		
		public Site findSiteByOwer(User user, String key);
	
		void save(Page page);
		void save(Area area);
		void save(Block block);
		void save(PageSection page_section);

		public Page 			findPageById(Serializable id);
		public PageSection 	    findPageSectionById(Serializable id);
		public Area		 	    findAreaById(Serializable id);
		public Block	 	    findBlockById(Serializable id);

		/**
		 * SiteFavorites
		 * 
		 * @param user
		 * @return
		 */
		public SiteFavorites getSiteFavorites(User user);

		public void save(SiteFavorites site_fav);
		public void delete(SiteFavorites site_fav);
		
		
		/**		
		 * Sites
		 * 
		 * @param domain
		 * @return
		 */
		
		public Site getLibrarySite(Library library);
		
		public List<Site> getSites(Domain domain);
		public List<Site> getSitesPublic(Domain domain);
		public List<Site> getSites(Domain domain, SiteType site_type);
		public List<Site> getSitesNotSiteType(Domain domain, SiteType not_site_type, ObjectState state, boolean only_public);
		public List<Site> getSites(Domain domain, ObjectState state);
		public List<Site> getSites(Domain domain, SiteType site_type, ObjectState state);

		public Site findSiteByURI(String uri, Domain domain, SiteType site_type);
		public Site findSiteById(Serializable id);
		public Site findSiteByOId(Serializable id);
		public boolean existsURI(String uri, Domain domain, SiteType site_type);
		
		public Site getHomeSite(Domain domain);
		
		public void save(Site site);
		public void delete(Site site);
		
		/**
		 * PortalObject
		 *  
		 * @param oid
		 * @return
		 */
		public PortalObject findPortalObjectById(ObjectID oid);
		public PortalObject findObjectById(ObjectId oid);

		
		/**
		 * ViewBK
		 * 
		 * @param id
		 * @return
		 */
		public void delete(ViewBK c);
		public void save(ViewBK view);
		public ViewBK findViewById(Serializable id);
		public ViewBK findViewByOId(Serializable oid);
		
		
		
		/**
		 * ViewDetailContent
		 * @param view
		 */
		public void save(ViewDetailContent view);
		public void delete(ViewDetailContent view);
		public int getViewDetailCountByOId(Serializable oid);
		public List<ViewDetailContent> getViewDetailByOId(Serializable oid);
		public ViewDetailContent findViewDetailByContent(Content content);
		public ViewDetailContent findViewDetailByContentVersion(Content content);
		public ViewDetailContent findViewDetailById(Serializable id);
		public ViewDetailContent findViewDetailByHeadVersionContent(Content content);
		public Site findSiteByURI(String uri, Domain domain);
		public Site findSiteByAlias(String url, Domain domain);
		
		public void delete(Block block);
		public void delete(Area c);
		
		public void delete(PortalObject po);		
		
		public void save(PortalObject o);

		
		
		/**
		 * Block
		 * 
		 * @param id
		 * @return
		 */
		//public Block findBlockById(Serializable id, Domain domain);
		//public void save(Block block);
		//public void delete(Block block);

		
		
		
		
}
