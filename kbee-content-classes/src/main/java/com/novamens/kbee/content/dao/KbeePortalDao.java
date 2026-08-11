
package com.novamens.kbee.content.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;


import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ObjectId;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectID;
import com.novamens.dom.ObjectState;
import com.novamens.event.BeforeUpdateEvent;
import com.novamens.event.EventService;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.portal.model.KbeeArea;
import com.novamens.kbee.portal.model.KbeeBlock;
import com.novamens.kbee.portal.model.KbeeBlockListView;
import com.novamens.kbee.portal.model.KbeePage;
import com.novamens.kbee.portal.model.KbeePageSection;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.portal.model.KbeeSiteFavorites;
import com.novamens.kbee.portal.model.KbeeViewBK;
import com.novamens.kbee.portal.model.KbeeViewBKIQL;
import com.novamens.kbee.portal.model.KbeeViewBKLink;
import com.novamens.kbee.portal.model.KbeeViewBKSite;
import com.novamens.kbee.portal.model.KbeeViewDetailContent;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.portal.favorites.SiteFavorites;
import com.novamens.portal.model.diagrammablesite.DiagrammableSite;
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
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class KbeePortalDao implements PortalDao {
																							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalDao.class.getName());

	private SessionFactory sessionFactory;
	private JdbcTemplate jdbcTemplate;
	private SqlPlatform sqlplatform;
	private String schema;

	

	@Override
	public List<Content> getLibrarySiteQueryContents(Site site, String statement, int max) {
		List<Content> list_c = new ArrayList<Content>();
		SearcherSiteQuery qe= new SearcherSiteQuery(site, getIndex(site.getDomain()));
		qe.getParameters().put("sort", "title");
		qe.setPageSize(max);
		com.novamens.indexer.query.ResultSet res=qe.execute();
		if (res==null) 
    		return list_c;
    	int total = 0;
    	int limit = max;
    	while (res.hasNext() && total++<limit) {
    		SearchResult r=res.next();
    		if (r.getObject() instanceof Content)
    			list_c.add( (Content) r.getObject());
    	}
    	/**
    	list_c.sort(new Comparator<Content>() {
			@Override
			public int compare(Content o1, Content o2) {
				try {
					return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
    	});
    	**/
		return list_c;
	}
	
	
	@Override
	public List<Content> getLibrarySiteIQLContents(Site site, String iql, int max) {
		return getLibrarySiteIQLContents(site, iql, max, null);
	}
	
	@Override
	public List<Content> getLibrarySiteIQLContents(Site site, String iql, int max, String sort) {
		List<Content> list_c = new ArrayList<Content>();
		IqlQuery qe = site.getDomain().getService(IqlService.class).getNewQuery(iql);
		if (sort!=null && sort.endsWith("asc")) {
			qe.setParameter("ascending", "true");
		}
		if (sort!=null && sort.endsWith("des")) {
			qe.setParameter("ascending", "false");
		}
		com.novamens.indexer.query.ResultSet res=qe.execute();
		if (res==null) 
    		return list_c;
    	int total = 0;
    	int limit = max;
    	while (res.hasNext() && total++<limit) {
    		SearchResult r=res.next();
    		if (r.getObject() instanceof Content)
    			list_c.add( (Content) r.getObject());
    	}	
    	if (sort==null || !sort.startsWith("modified")) {
	    	list_c.sort(new Comparator<Content>() {
				@Override
				public int compare(Content o1, Content o2) {
					try {
						return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
					} 
					catch (Exception e) {
						logger.error(e);
						return 0;
					}
				}
	    	});
    	}
		return list_c; 
	}
	
	
	@Override
	public List<Content> getLibraryPortalHomeContents(Site site) {
		List<Content> list_c = new ArrayList<Content>();
		
		//String sentence = Classifier.PORTAL_HOME_PREDICATE + "(" +  site.getId().toString()  + ")";
		
		String sentence = "ishead(true)";
		
		IqlQuery qe = site.getDomain().getService(IqlService.class).getNewQuery(sentence);
		com.novamens.indexer.query.ResultSet res=qe.execute();
		if (res==null) 
    		return list_c;
    	int total = 0;
    	int limit = 1000;
    	while (res.hasNext() && total++<limit) {
    		SearchResult r=res.next();
    		if (r.getObject() instanceof Content)
    			list_c.add( (Content) r.getObject());
    	}
    	list_c.sort(new Comparator<Content>() {
			@Override
			public int compare(Content o1, Content o2) {
				try {
					return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
    	});
		return list_c; 
	}

	public DataSet getPortalHomeDataSet(Site site) {
		if (site==null)
			return null;
		return getContentDao().findDataSetByAlias(DataSet.PORTAL_HOME_DATASET_ALIAS, site.getDomain().getId());
		/**
		List<DataSet> list = getContentDao().getDataSets(site.getDomain());
		for (DataSet d: list) {
			if (d.getAlias().equals(DataSet.PORTAL_HOME_DATASET_ALIAS));
				return d;
		}
		return null;
		**/
	}
	
	@Override
	public Classifier getPortalHomeClassifierSet(Site site) {
		if (site==null)
			return null;
		return getContentDao().findClassifierByAlias(Classifier.PORTAL_HOME_CLASSIFIER_ALIAS, site.getDomain().getId());
		/**
		List<Classifier> list = getContentDao().getClassifiers(site.getDomain());
		for (Classifier d: list) {
			if (d.getAlias().equals(Classifier.PORTAL_HOME_CLASSIFIER_ALIAS));
				return d;
		}
		return null;
		**/
	}

	/**
	 * 
	 */
	public List<Content> getLibrarySiteContents(Site site, int limit) {
		List<Content> list_c = new ArrayList<Content>();
    	try {
    		SearcherSiteQuery qe= new SearcherSiteQuery(site, getIndex(site.getDomain()));
    		qe.getParameters().put("sort", "title");
   		 	com.novamens.indexer.query.ResultSet res=qe.execute();
	    	if (res==null) 
	    		return list_c;
	    	int total = 0;
	    	while (res.hasNext() && total++<limit) {
	    		SearchResult r=res.next();
	    		if (r.getObject() instanceof Content)
	    			list_c.add( (Content) r.getObject());
	    	}

	    	/**
	    	list_c.sort(new Comparator<Content>() {

				@Override
				public int compare(Content o1, Content o2) {
					try {
						return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
					} catch (Exception e) {
						logger.error(e);
						return 0;
					}
				}
	    	});
	    	**/
    	
    	} catch (Exception e) {
    		logger.error(e);
    	}
    	return list_c;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Site findSiteById(Serializable id) {
		if (id==null)
			return null;
		final HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("id", (Long) id);
	    final List<Site> resultSet = (List<Site>) getResultSet("FROM KbeeSite p where p.id = :id", parameters);
	    return resultSet.isEmpty() ? null : resultSet.get(0);
	    
	    /**
		String hql = "FROM KbeeSite S WHERE S.id =" + id.toString();
		@SuppressWarnings("unchecked")
		Query<Site> query = (Query<Site>) sessionFactory.getCurrentSession().createQuery(hql);
		List<Site> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return results.get(0);
			**/
	}
	
	
	@Override
	public Site findSiteByOwer(User user, String key) {
		String hql = "FROM KbeeSite S WHERE S.owner.id =" + user.getId().toString() + " and S.key='"+key+"'";
		@SuppressWarnings("unchecked")
		Query<Site> query = (Query<Site>) sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheRegion("query");
		List<Site> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return results.get(0);
	}
	

						
	public PageSection 	findPageSectionById(Serializable id) {
		if (id==null)
			return null;
		final HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("id", (Long) id );
	    final List<PageSection> resultSet = (List<PageSection>) getResultSet("FROM KbeePageSection p where p.id = :id", parameters);
	    return resultSet.isEmpty() ? null : resultSet.get(0);
		/**
		String hql = "FROM KbeePageSection S WHERE S.id =" + id.toString();
		@SuppressWarnings("unchecked")
		Query<PageSection> query = (Query<PageSection>) sessionFactory.getCurrentSession().createQuery(hql);
		logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (PageSection) query.uniqueResult();
        **/
		
	}
					
	public Area	findAreaById(Serializable id) {
		/**
		long start = System.currentTimeMillis();
		String hql = "FROM KbeeArea S WHERE S.id =" + id.toString();
		@SuppressWarnings("unchecked")
		Query<Area> query = (Query<Area>) sessionFactory.getCurrentSession().createQuery(hql);
		logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Area) query.uniqueResult();
        **/
        
		if (id==null)
			return null;
		final HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("id", (Long) id);
	    final List<Area> resultSet = (List<Area>) getResultSet("FROM KbeeArea p where p.id = :id", parameters);
	    return resultSet.isEmpty() ? null : resultSet.get(0);
		
	}
	
					
	public Block findBlockById(Serializable id) {
	
		/**
		long start = System.currentTimeMillis();
		String hql = "FROM KbeeBlock S WHERE S.id =" + id.toString();
		@SuppressWarnings("unchecked")
		Query<Block> query = (Query<Block>) sessionFactory.getCurrentSession().createQuery(hql);
		logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Block) query.uniqueResult();
        */
		
		if (id==null)
			return null;
		final HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("id",  (Long) id);
	    final List<Block> resultSet = (List<Block>) getResultSet("FROM KbeeBlock p where p.id = :id", parameters);
	    return resultSet.isEmpty() ? null : resultSet.get(0);

	}

	
	
	@Override
	public Page findPageById(Serializable id) {
		
		/**
		long start = System.currentTimeMillis();
		String hql = "FROM KbeePage S WHERE S.id =" + id.toString();
		@SuppressWarnings("unchecked")
		Query<Page> query = (Query<Page>) sessionFactory.getCurrentSession().createQuery(hql);
		logger.debug(hql + " -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return (Page) query.uniqueResult();
        **/
        
		if (id==null)
			return null;
		final HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("id", (Long) id);
	    final List<Page> resultSet = (List<Page>) getResultSet("FROM KbeePage p where p.id = :id", parameters);
	    return resultSet.isEmpty() ? null : resultSet.get(0);
	}
	
	@Override
	public void save(PortalObject o) {
		this.sessionFactory.getCurrentSession().save(o);
	}
	

	@Override
	public void delete(PortalObject po) {
		this.sessionFactory.getCurrentSession().delete(po);
	}
	
	@Override
	public void save(ViewBK view) {
		if (view.isKBFile())
			this.sessionFactory.getCurrentSession().save(view.getFile());
		view.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		this.sessionFactory.getCurrentSession().save(view);
	}

	@Override
	public Site getHomeSite(Domain domain) {
		String hql = "FROM KbeeSite S WHERE S.domain.id=" + domain.getId().toString() + " AND S.site_type="+ String.valueOf(SiteType.HOME.getId());
		@SuppressWarnings("unchecked")
		Query<Site> query = (Query<Site>) sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<Site> results = query.list();
		if (!results.isEmpty())
			return results.get(0);
		else
			return null;
	}

	
	@Override
	public Site getLibrarySite(Library library) {
		String hql = "FROM KbeeSite S WHERE S.library.id=" + library.getId().toString();
		@SuppressWarnings("unchecked")
		Query<Site> query = (Query<Site>) sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");
		List<Site> results = query.list();
		if (!results.isEmpty())
			return results.get(0);
		else
			return null;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public List<Site> getSitesNotSiteType(Domain domain, SiteType not_site_type, ObjectState state,	boolean only_public) {
		StringBuilder hql = new StringBuilder();
		hql.append("FROM KbeeSite S WHERE S.domain.id=" + domain.getId().toString());
		if (only_public)					hql.append(" AND S.is_public=true");
		if (not_site_type != null)			hql.append(" AND S.site_type !=" + not_site_type.getId());
		if (state != null)					hql.append(" AND S.state=" + String.valueOf(state.getId()));
		
		hql.append(" order by lower(S.title) ");
		Query<Site> query = sessionFactory.getCurrentSession().createQuery(hql.toString());
		List<Site> results = query.list();
		if (results.isEmpty())
			return null;
		return (List<Site>) results;
	}

	/**
	 * @param site
	 */
	@Override
	public void save(Site site) {
		
		if (site.getLastModifiedOffsetDateTime()==null)
			site.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		if (site.getLastModifiedUser()==null)
			site.setLastModifiedUser(getSessionUser());
		
		ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(site));
		this.sessionFactory.getCurrentSession().persist(site);
		
	}

	
	@Override
	public void save(Page page) {
		
		if (page.getLastModifiedOffsetDateTime()==null)
			page.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		if (page.getLastModifiedUser()==null)
			page.setLastModifiedUser(getSessionUser());
		
		ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(page));
		this.sessionFactory.getCurrentSession().persist(page);
	}
	

	@Override
	public void save(PageSection page) {

		page.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		page.setLastModifiedUser(getSessionUser());
		ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(page));
		this.sessionFactory.getCurrentSession().persist(page);
	}


	@Override
	public void save(Area page) {
		page.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		page.setLastModifiedUser(getSessionUser());
		ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(page));
		this.sessionFactory.getCurrentSession().persist(page);
	}


	@Override
	public void save(Block b) {
		b.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		b.setLastModifiedUser(getSessionUser());
		ServiceLocator.getService(EventService.class).fire(new BeforeUpdateEvent(b));
		this.sessionFactory.getCurrentSession().save(b);
	}

	
	
	@Override
	public void delete(Site site) {
		this.sessionFactory.getCurrentSession().delete(site);
	}


	
	
	@Override
	public void delete(Block block) {
		this.sessionFactory.getCurrentSession().delete(block);
	}

	@Override
	public void delete(Area c) {
		this.sessionFactory.getCurrentSession().delete(c);
	}

	@Override
	public void save(ViewDetailContent view) {
		try {
			view.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			if (view.getLastModifiedUser() == null)
				view.setLastModifiedUser(getSessionUser());
			this.sessionFactory.getCurrentSession().persist(view);
		} catch (Exception e) {
			logger.error(e);
			throw (e);
		}

	}

	
	@Override
	public void delete(ViewBK c) {
		this.sessionFactory.getCurrentSession().delete(c);
	}

	
	@Override
	public void delete(ViewDetailContent view) {
		this.sessionFactory.getCurrentSession().delete(view);
	}


	@Override
	public List<Site> getSitesPublic(Domain domain) {
		return getSites(domain, null, null, true);
	}

	@Override
	public List<Site> getSites(Domain domain, ObjectState state) {
		return getSites(domain, null, state, false);
	}

	@Override
	public List<Site> getSites(Domain domain, SiteType site_type, ObjectState state) {
		return getSites(domain, site_type, state, false);
	}

	@Override
	public List<Site> getSites(Domain domain, SiteType site_type) {
		return getSites(domain, site_type, null, false);
	}

	@Override
	public List<Site> getSites(Domain domain) {
		return getSites(domain, null, null, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean existsURI(String uri, Domain domain, SiteType site_type) {
		String hql = "FROM KbeeSite S WHERE S.uri= '" + uri + "' AND S.site_type= '" + String.valueOf(site_type.getId())+ "'" + " AND S.domain.id =" + domain.getId().toString();
		Query<DiagrammableSite> query = (Query<DiagrammableSite>) sessionFactory.getCurrentSession().createQuery(hql);
		List<DiagrammableSite> results = query.list();
		if (results.isEmpty())
			return false;
		else
			return true;
	}


	@SuppressWarnings("unchecked")
	@Override
	public Site findSiteByURI(String uri, Domain domain, SiteType site_type) {
		if (uri==null || domain==null)
			return null;
		String hql = "FROM KbeeSite S WHERE S.uri = '" + uri.trim().toLowerCase() + "'" + " AND S.domain.id=" + domain.getId().toString() + (site_type != null ? " AND S.site_type='" + String.valueOf(site_type.getId()) + "' " : "");
		Query<Site> query = (Query<Site>) sessionFactory.getCurrentSession().createQuery(hql);
		List<Site> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return results.get(0);
	}


	
	@Override				
	public Site findSiteByAlias(String alias, Domain domain) {
		if (alias==null || domain==null)
			return null;
		
		final HashMap<String, Object> parameters = new HashMap<>();
		
		parameters.put("id", (Long) domain.getId());
		parameters.put("alias", alias);
		
	    @SuppressWarnings("unchecked")
		final List<Site> resultSet = (List<Site>) getResultSet("FROM KbeeSite p where p.alias = :alias and p.domain.id:id", parameters);
	    return resultSet.isEmpty() ? null : resultSet.get(0);
		
		
		//String hql = "FROM KbeeSite S WHERE S.alias = '" + alias.trim().toLowerCase() + "' AND S.domain.id=" + domain.getId().toString();
		//Query<Site> query = (Query<Site>) sessionFactory.getCurrentSession().createQuery(hql);
		//List<Site> results = query.list();
		//if (results.isEmpty())
//			return null;
	//	else
		//	return results.get(0);
	    
	}

	
	
	@Override
	public Site findSiteByURI(String uri, Domain domain) {
		return findSiteByURI(uri, domain, null);
	}

	
	
	@Override
	public Site findSiteByOId(Serializable id) {
		String hql = "FROM KbeeSite S WHERE S.oid=" + id.toString();
		@SuppressWarnings("unchecked")
		Query<Site> query = (Query<Site>) sessionFactory.getCurrentSession().createQuery(hql);
		List<Site> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return results.get(0);
	}

	/**
	@SuppressWarnings("unchecked")
	@Override
	public Block findBlockById(Serializable id, Domain domain) {
		String hql = "FROM KbeeBlock S WHERE S.id = '" + id.toString() + "'" + " AND S.domain.id='"
				+ domain.getId().toString() + "'";
		Query<Block> query = (Query<Block>) sessionFactory.getCurrentSession().createQuery(hql);

		List<Block> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return (DiagrammableBlock) results.get(0);
	}
	**/

	@Override
	public int getViewDetailCountByOId(Serializable oid) {
		String hql = "select count(*) FROM KbeeViewDetailContent K WHERE K.content_oid = " + oid.toString();
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");
		return ((Long) query.uniqueResult()).intValue();
	}

	@Override
	public List<ViewDetailContent> getViewDetailByOId(Serializable oid) {
		String hql = "FROM KbeeViewDetailContent K WHERE K.content_oid = " + oid.toString()	+ " order by K.content.version";
		@SuppressWarnings("unchecked")
		Query<ViewDetailContent> query = (Query<ViewDetailContent>) sessionFactory.getCurrentSession().createQuery(hql);
		List<ViewDetailContent> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return results;
	}

	@Override
	public ViewDetailContent findViewDetailByContent(Content content) {
		String hql = "FROM KbeeViewDetailContent K WHERE K.content.id = " + content.getId().toString();
		logger.debug(hql);
		@SuppressWarnings("unchecked")
		Query<ViewDetailContent> query = (Query<ViewDetailContent>) sessionFactory.getCurrentSession().createQuery(hql);
		List<ViewDetailContent> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return (ViewDetailContent) results.get(0);
	}

	/**
	 * La Vista esta asociada a una version del Contenido. Al publicarse una nueva
	 * version, se debe actualizar la vista para que apunte a la nueva version.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ViewDetailContent findViewDetailByContentVersion(Content content) {
		String hql = "FROM KbeeViewDetailContent K WHERE K.content.id = " + content.getId().toString();
		Query<ViewDetailContent> query = (Query<ViewDetailContent>) sessionFactory.getCurrentSession().createQuery(hql);
		List<ViewDetailContent> results = query.list();
		if (results.isEmpty())
			return null;
		return (ViewDetailContent) results.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ViewDetailContent findViewDetailById(Serializable id) {
		String hql = "FROM KbeeViewDetailContent V WHERE V.id =" + id.toString();
		Query<ViewDetailContent> query = (Query<ViewDetailContent>) sessionFactory.getCurrentSession().createQuery(hql);
		List<ViewDetailContent> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return (ViewDetailContent) results.get(0);
	}

	@Override
	public ViewDetailContent findViewDetailByHeadVersionContent(Content content) {
		Content src = null;
		if (!content.isHeadVersion())
			src = getContentDao().findContentByOId(content.getOId());
		else
			src = content;

		if (src == null) {
			logger.error("Content does not have a head version  id: " + content.getId().toString());
			return null;
		}
		return findViewDetailByContent(src);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ViewBK findViewById(Serializable id) {
		String hql = "FROM KbeeViewBK V WHERE V.id =" + id.toString();
		Query<ViewBK> query = (Query<ViewBK>) sessionFactory.getCurrentSession().createQuery(hql);
		List<ViewBK> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return (ViewBK) results.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ViewBK findViewByOId(Serializable oid) {
		String hql = "FROM KbeeViewBK V WHERE V.oid = '" + oid.toString() + "'";
		Query<ViewBK> query = (Query<ViewBK>) sessionFactory.getCurrentSession().createQuery(hql);
		List<ViewBK> results = query.list();
		if (results.isEmpty())
			return null;
		else
			return (ViewBK) results.get(0);
	}

	@Override
	public PortalObject findPortalObjectById(ObjectID id) {
		return (findById(id.getClassName(), id.getId()));
	}

	public PortalObject findObjectById(ObjectId id) {
		PortalObject object = null;
		switch (id.getClassName().toLowerCase()) {
		case "kbeesite":				object = sessionFactory.getCurrentSession().get(KbeeSite.class, Long.valueOf(id.getId()));	break;
		
		case "kbeepage":				object = sessionFactory.getCurrentSession().get(KbeePage.class, Long.valueOf(id.getId()));	break;
		case "kbeepagesection":			object = sessionFactory.getCurrentSession().get(KbeePageSection.class, Long.valueOf(id.getId())); break;
		case "kbeearea":				object = sessionFactory.getCurrentSession().get(KbeeArea.class, Long.valueOf(id.getId()));	break;
		case "kbeeblock":				object = sessionFactory.getCurrentSession().get(KbeeBlock.class, Long.valueOf(id.getId())); break;
		case "kbeeblocklistview":		object = sessionFactory.getCurrentSession().get(KbeeBlockListView.class, Long.valueOf(id.getId())); break;
		

		case "kbeeviewbk":				object = sessionFactory.getCurrentSession().get(KbeeViewBK.class, Long.valueOf(id.getId()));                break;
		case "kbeeviewbkiql":			object = sessionFactory.getCurrentSession().get(KbeeViewBKIQL.class, Long.valueOf(id.getId()));             break;
		case "kbeeviewbksite":			object = sessionFactory.getCurrentSession().get(KbeeViewBKSite.class, Long.valueOf(id.getId()));            break;
		case "kbeeviewbklink":			object = sessionFactory.getCurrentSession().get(KbeeViewBKLink.class, Long.valueOf(id.getId()));			break;
		case "kbeeviewdetailcontent":	object = sessionFactory.getCurrentSession().get(KbeeViewDetailContent.class, Long.valueOf(id.getId()));		break;
		}
		
		return object;
	}

	@Override
	public void save(SiteFavorites site_fav) {
		sessionFactory.getCurrentSession().save(site_fav);
	}

	@Override
	public void delete(SiteFavorites site_fav) {
		sessionFactory.getCurrentSession().delete(site_fav);
	}


	public Long getNewOId() {
		SqlPlatform sqlplatform = getSqlPlatform();
		Long value = this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(getSchema() + "portalid_sequence"),
				new ResultSetExtractor<Long>() {
					public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
						if (rs.next())
							return rs.getLong(1);
						return null;
					}
				});
		return value;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		if (schema != null && schema.length() > 0)
			return schema + ".";
		return "";
	}

	
	/**@Override
	public void save(Block block) {
		block.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		this.sessionFactory.getCurrentSession().save(block);
	}**/

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private SqlPlatform getSqlPlatform() {

		if (this.sqlplatform != null)
			return this.sqlplatform;

		Connection connection = null;
		try {
			connection = this.jdbcTemplate.getDataSource().getConnection();
			this.sqlplatform = SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		} catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		}
		return this.sqlplatform;
	}


	/**
	 * Esto no esta guardando el Cache, hace la query cada vez. ver con AF
	 */
	@Override
	public SiteFavorites getSiteFavorites(User user) {
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeSiteFavorites> criteria = criteriabuilder.createQuery(KbeeSiteFavorites.class);
		Root<KbeeSiteFavorites> sites = criteria.from(KbeeSiteFavorites.class);
		Predicate e = criteriabuilder.equal(sites.get("user"), user);
		criteria.select(sites).where(e);
		TypedQuery<KbeeSiteFavorites> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", Boolean.TRUE);
		query.setFlushMode(FlushModeType.COMMIT);
		List<KbeeSiteFavorites> result = new ArrayList<KbeeSiteFavorites>();
		result.addAll(query.getResultList());
		if (result.size() == 0)
			return null;
		return result.get(0);
	}

	protected Index getIndex(Domain domain) {
		return  domain.getService(JavaIndexerService.class).getIndex();
	}


	private List<? extends Object> getResultSet(String hql, Map<String, Object> parameters) {
		return getResultSet(hql, parameters, 0);
	}


	private List<? extends Object> getResultSet(String hql, Map<String, Object> parameters, int limit) {
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");
		if (limit > 0) 
			query.setMaxResults(limit);
		for (Map.Entry<String, Object> stringObjectEntry : parameters.entrySet())
			query.setParameter(stringObjectEntry.getKey(), stringObjectEntry.getValue());
		long start = System.currentTimeMillis();
		List<?> results = query.list();
		logger.debug(String.valueOf(System.currentTimeMillis() - start) + " ms -> " + hql);
		return results;
		
	}
	
	/**
	 * 
	 */
	private List<Site> getSites(Domain domain, SiteType site_type, ObjectState state, boolean only_public) {

		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeSite> criteria = criteriabuilder.createQuery(KbeeSite.class);

		Root<KbeeSite> sites = criteria.from(KbeeSite.class);

		ParameterExpression<Boolean> publicparameter = criteriabuilder.parameter(Boolean.class);
		ParameterExpression<SiteType> typeparameter = criteriabuilder.parameter(SiteType.class);
		ParameterExpression<ObjectState> stateparameter = criteriabuilder.parameter(ObjectState.class);
		ParameterExpression<Domain> domainparameter = criteriabuilder.parameter(Domain.class);
						
		List<Predicate> predicates = new ArrayList<Predicate>();
		
		if (only_public) {
			Predicate publicpredicate = criteriabuilder.equal(sites.get("is_public"), publicparameter);
			predicates.add(publicpredicate);
		}
		
		
		if (domain !=null) {
			predicates.add(criteriabuilder.equal(sites.get("domain"), domainparameter));
			
		}
		
		if (site_type != null) {
			predicates.add(criteriabuilder.equal(sites.get("site_type"), typeparameter));
		}
		
		if (state != null) {
			predicates.add(criteriabuilder.equal(sites.get("state"), stateparameter));
		}

		Order order = new OrderImpl(criteriabuilder.function("lower", String.class, sites.get("title")));

		criteria.select(sites).where(criteriabuilder.and(predicates.toArray(new Predicate[0]))).orderBy(order);

		TypedQuery<KbeeSite> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", Boolean.TRUE);
		query.setFlushMode(FlushModeType.COMMIT);

		if (only_public) {
			query.setParameter(publicparameter, Boolean.TRUE);
		}
		if (site_type != null) {
			query.setParameter(typeparameter, site_type);
		}
		if (state != null) {
			query.setParameter(stateparameter, state);
		}
		
		if (domain != null) {
			query.setParameter(domainparameter, domain);
		}

		List<Site> result = new ArrayList<Site>();
		result.addAll(query.getResultList());
		return result;
	}
	
	private PortalObject findById(String clazz, String id) {
		PortalObject po = null;
		switch (clazz.toLowerCase()) {
		case "kbeesite":			po = sessionFactory.getCurrentSession().get(KbeeSite.class, Long.valueOf(id));			break;
		case "kbeepage":			po = sessionFactory.getCurrentSession().get(KbeePage.class, Long.valueOf(id));			break;
		case "kbeepagesection":		po = sessionFactory.getCurrentSession().get(KbeePageSection.class, Long.valueOf(id));	break;
		case "kbeearea":			po = sessionFactory.getCurrentSession().get(KbeeArea.class, Long.valueOf(id));			break;
		case "kbeeblock":			po = sessionFactory.getCurrentSession().get(KbeeBlock.class, Long.valueOf(id));			break;
		
			
		//	case "kbeeblocklistcontent":
		//	po = sessionFactory.getCurrentSession().get(KbeeBlockListContent.class, Long.valueOf(id));
		//	break;
		//	case "kbeeblocklistsite":
		//	po = sessionFactory.getCurrentSession().get(KbeeBlockListSite.class, Long.valueOf(id));
		//	break;
		//	case "kbeeblocklistview":
		//	po = sessionFactory.getCurrentSession().get(KbeeBlockListView.class, Long.valueOf(id));
		//	break;
		//	case "kbeeblockx":
		//	po = sessionFactory.getCurrentSession().get(KbeeBlockX.class, Long.valueOf(id));
		//	break;
		//	case "kbeeviewbkcontent":
		//	po = sessionFactory.getCurrentSession().get(KbeeViewBKContent.class, Long.valueOf(id));
		//	break;
		//	case "kbeeblockcontact":
		//	po = sessionFactory.getCurrentSession().get(KbeeBlockContact.class, Long.valueOf(id));
		//	break;
		//	case "kbeeviewbksite":
		//	po = sessionFactory.getCurrentSession().get(KbeeViewBKSite.class, Long.valueOf(id));
		//	break;
		//	case "kbeeviewbklink":
		//	po = sessionFactory.getCurrentSession().get(KbeeViewBKLink.class, Long.valueOf(id));
		//	break;
		//	case "kbeeblocktext":
		//	po = sessionFactory.getCurrentSession().get(KbeeBlock.class, Long.valueOf(id));
		//	break;
		//	case "riocumpleanosblock":
		//	po = sessionFactory.getCurrentSession().get(KbeeBlock.class, Long.valueOf(id));
		//	break;
		//	case "kbeeviewdetailcontent":
		//	po = sessionFactory.getCurrentSession().get(KbeeViewDetailContent.class, Long.valueOf(id));
		//	break;

		default: {
			logger.error(" {} | {} | {} | {}", getSessionUser().getUserName(), "",	Thread.currentThread().getStackTrace()[1].getMethodName(), "Class not found " + clazz);
			throw new KbeeRuntimeException("Class not found " + clazz);
		}
		}
		;
		return po;
	}





    
}
