package com.novamens.portal.diagrammablesite.dao;




import java.io.Serializable;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.ObjectId;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectID;
import com.novamens.dom.ObjectState;
import com.novamens.portal.factory.BlockFactory;
import com.novamens.portal.favorites.SiteFavorites;

import com.novamens.portal.model.diagrammablesite.DiagrammableSite;
import com.novamens.portal.model.diagrammablesite.SiteUserRightsDEPRECATED;
import com.novamens.portal6.model.PortalException;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.SiteType;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.ViewDetailContent;
import com.novamens.security.User;

public interface PortalDiagrammableDao  extends Dao { 
	
	public void create(DiagrammableSite site);
	public void delete(DiagrammableSite site);
	public void save(DiagrammableSite site);
	
	
	//public void removeUser(DiagrammableSite site, User user);
	//public void addContent(DiagrammableBlock block, Content content, int order);
	//public void removeContent(DiagrammableBlock block, Content content);
	
	 						
	//public DiagrammableBlock findBlockById(Serializable id, Domain domain);
	//public DiagrammableSite getHomeSite(Content content) throws PortalException;
	//public DiagrammableSite getGlobalHomeSite(Domain domain);

	
	// ---------------------------------------------------------------------------------------
	//
	// PORTALDAO
	//
	//
	//public List<DiagrammableSite> getSites(Domain domain);
	//public List<DiagrammableSite> getSitesPublic(Domain domain);
	//public List<DiagrammableSite> getSites(Domain domain, SiteType site_type);
	//public DiagrammableSite findSiteByURI(String uri, Domain domain, SiteType site_type);
	//public List<DiagrammableSite> getSites(Domain domain, ObjectState state);
	//public List<DiagrammableSite> getSites(Domain domain, SiteType site_type, ObjectState state);

	// Mis accesos rápidos  -----------------------------------------------------------
	//
	//public SiteFavorites getSiteFavorites(User user);  // PortalDao
	//public void save(SiteFavorites site_fav);  // PortalDao
	//public void delete(SiteFavorites site_fav);  // PortalDao


	public ViewBK findViewById(Serializable id);
	public ViewBK findViewByOId(Serializable oid);

	public PortalObject findPortalObjectById(ObjectID oid);
	public PortalObject findObjectById(ObjectId oid);

	
	// Block-------------  -----------------------------------------------------------
	//
	//public void delete(DiagrammableBlock block);
	//public void delete(ViewBK c);
	//public void save(DiagrammableBlock oldblock);
	
	//public ListViewBlockV5 findListViewBlockById(Serializable id);
	//public List<DiagrammableSite> getSitesNotSiteType(Domain domain, SiteType not_site_type, ObjectState state, boolean only_public);

	
	// View  --------------------------------------------------------------------------
	//
	public void save(ViewBK view);
	
	
	// View Detail Content -----------------------------------------------------------
	//
	public ViewDetailContent findViewDetailByContent(Content content);
	public ViewDetailContent findViewDetailByContentVersion(Content content);
	public ViewDetailContent findViewDetailByHeadVersionContent(Content referencedContent);
	
	public void save(ViewDetailContent view);
	public void delete(ViewDetailContent view);
	
	public List<ViewDetailContent> getViewDetailByOId(Serializable id);
	public int getViewDetailCountByOId(Serializable oid);
	ViewDetailContent findViewDetailById(Serializable id);
	
	public void deleteAllViewDetail(Domain domain);
	public DiagrammableSite findSiteById(Serializable id);
	public DiagrammableSite findSiteByOId(Serializable id);
	
	public boolean existsURI(String uri, Domain domain, SiteType site_type);

	public List<BlockFactory> getBlockFactories(Domain domain, DiagrammableSite site);
	
	
}
