package com.novamens.kbee.portal.service;



import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.hibernate.SessionFactory;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.query.HibernateQuery;

import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.portal.service.PortalDirectoryService;
import com.novamens.portal6.model.PortalException;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;
import com.novamens.security.User;

public class KbeePortalDirectoryService implements PortalDirectoryService, EventListener {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalDirectoryService.class.getName());

	protected static long CHECK_INTERVAL = 1000 * 60 * 2; // 2 minutes

	private PortalDao dao = null;
	private ContentDao contentdao = null;
	
	private SessionFactory sessionFactory;

	protected long last_check = 0;


	public KbeePortalDirectoryService() {
		
	}

	@Override
	public Site getHomeSite(Domain domain) {
		return getPortalDao().getHomeSite(domain);
	}
	
	@Override
	public Site findDashboardSite(User user) throws PortalException {
		return getPortalDao().findSiteByOwer(user,  Site.MAIN_DASHBOARD);
	}
	
	@Override
	public boolean existsSiteUrl(String relative_url, Domain domain) {
		return getPortalDao().findSiteByURI(relative_url, domain)!=null;
	}
	
	@Override
	public List<Site> getSites(Domain domain) {
		return getPortalDao().getSites(domain);
	}

	@Override
	public List<Site> getSitesPublic(Domain domain) {
		return getPortalDao().getSitesPublic(domain);
	}

	@Override
	public List<Site> getSites(Domain domain, SiteType site_type) {
		return getPortalDao().getSites(domain, site_type);
	}

	@Override
	public Site findSiteByURI(String uri, Domain domain) {
		return getPortalDao().findSiteByURI(uri, domain, null);
	}

	@Override
	public Site findSiteByURI(String uri, Domain domain, SiteType site_type) {
		if (uri != null && uri.endsWith("/"))
			uri = uri.substring(0, uri.length() - 1);
		return getPortalDao().findSiteByURI(uri, domain, site_type);
	}

	@Override
	public Site findSiteById(Long id) {
		return getPortalDao().findSiteById(id);
	}

	@Override
	public Site findSiteByOId(Long oid) {
		return getPortalDao().findSiteByOId(oid);
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	public HibernateQuery getHibernateQuery(String statement) {
		HibernateQuery hqy = new HibernateQuery();
		hqy.setStatement(statement);
		Map<String, Object> map = new HashMap<String, Object>();
		//map.put("filter", "Preguntas sin respuestas.");
		hqy.setParameters(map);
		return hqy;
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent) {
		}
	}

	
	/** Spring */
	public void  setPortalDao(PortalDao d) {
		dao=d;
	}
	
	/** Spring */
	public void  setContentDao(ContentDao d) {
		contentdao=d;
	}
	
	/** Spring */
	public ContentDao getContentDao() {
		return contentdao;
	}

	/** Spring */
	public PortalDao getPortalDao() {
		return dao;
	}

	@Override
	public Site findSiteByUserKey(User user, String key) {
		return getPortalDao().findSiteByOwer(user, key);
	}

}








































// private Map<Serializable, Map<Serializable, String>> domain_url = new HashMap<Serializable, Map<Serializable, String>>();



//@Override
//public Panel getGlobalFooterPanel() {
//	return new InvisiblePanel("footer");
//}

//@Override
//public Panel getGlobalHeaderPanel(IModel<Site> callerSiteModel) {
//	Panel panel = new AreaGlobalHeaderPanel("header", callerSiteModel);
//	return panel;
//
// }

//@Override
//public Site getGlobalHomeSite(Domain domain) {
//	return getPortalDao().getGlobalHomeSite(domain);
//}

//@Override
//public Site createHome(Domain domain) throws PortalException {
//	throw new RuntimeException("not implemented");
//}

/**
 * @param domain

@Override
public void addIconsIfNotPresent(Domain domain) {

	ContentDao dao = getContentDao();

	DataSet content_type_dataset = dao.findDataSetByName(CONTENT_TYPE_DATASET, domain.getId());
	Classifier content_type_classifier = null;
	List<Classifier> list_classifiers = dao.getClassifiers(domain.getId());
	for (Classifier c : list_classifiers) {
		if (c.getName().equals(CONTENT_TYPE_CLASSIFIER)) {
			content_type_classifier = c;
			break;
		}
	}

	if (content_type_classifier == null) {
		logger.error("Content Type Classifier not present: " + CONTENT_TYPE_CLASSIFIER);
		return;
	}

	if (content_type_dataset == null) {
		logger.error("Content Type not present: " + CONTENT_TYPE_DATASET);
		return;
	}

	DataSetMember icon_instance = dao.findMemberByValue(content_type_dataset, ICON_INSTANCE);

	if (icon_instance == null) {
		logger.error("Icon instance not present: " + CONTENT_TYPE_DATASET + "  " + ICON_INSTANCE);
		return;
	}

	Map<String, String> icons = getIcons();

	for (Entry<String, String> entry : icons.entrySet()) {
		Content content = (Content) getContentDao().findContentByName(KbeeIDoc.class, entry.getKey(),
				domain.getId());
		if (content == null) {
			try {
				IDoc idoc;
				idoc = (IDoc) ServiceLocator.getService(ContentFactoryService.class).create(CONTENT_TEMPLATE);
				// Agrego el file
				//
				String image_name = entry.getValue();
				String userdir = PropertiesFactory.getInstance("kbee").getProperties().getProperty("user.dir",
						"/opt/jetty/rio-base");
				String reldir = PropertiesFactory.getInstance("kbee").getProperties()
						.getProperty("com.novamens.kbee.portal.images.library", "webapps/root/images/kbeeportal");
				String path = userdir.replace('/', File.separatorChar).replace('\\', File.separatorChar)
						+ File.separator + reldir.replace('/', File.separatorChar).replace('\\', File.separatorChar)
						+ File.separator + image_name;

				Map<String, String> map = new HashMap<String, String>();
				map.put("name", entry.getKey());
				map.put("image", path);
				map.put("domain_id", domain.getId().toString());
				map.put("subtitle", image_name + " icon");
				map.put("title", entry.getKey());
				map.put("bucket", "portal");
				map.put("description", "icon to use in kbee portal blocks.");

				try {

					KBFile form_icon = createFromMap(map);
					form_icon.setLastModifiedOffsetDateTime(OffsetDateTime.now());

					User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();

					form_icon.setLastModifiedUser(session_user);

					getContentDao().save(form_icon);
					idoc.addFile(form_icon);
					idoc.addClassification(content_type_classifier, icon_instance);
					idoc.setTitle(entry.getKey());
					idoc.setName(entry.getKey());

					ContentTemplate template = getContentDao().findContentTemplateByName(CONTENT_TEMPLATE,
							domain.getId());
					idoc.setContentTemplate(template);

					idoc.setState(ObjectState.ENABLED);
					idoc.setWorkspace(Long.valueOf(session_user.getId().toString()));

					ContentService service = idoc.getService(ContentService.class);
					service.update();

					logger.info("Adding icon " + idoc.getTitle());

					service.checkin();

				} catch (Exception e) {
					logger.error(" {} | {} | {} | {} ",
							(getSessionUser() != null ? getSessionUser().getUserName() : ""),
							e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(),
							e.getMessage());
				}

			} catch (ContentCreationException | ContentMgmtException e) {
				logger.error(" {} | {} | {} | {} ",
						(getSessionUser() != null ? getSessionUser().getUserName() : ""), e.getClass().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
			}
		} else {
			logger.debug("Icon: " + content.getTitle() + " already exists.");
		}
	}
}

@Override
public Map<String, String> getIcons() {
	Map<String, String> icons = new HashMap<String, String>();
	icons.put("Actividades BlockIcon", "actividades.png"); // 1
	icons.put("Aplicaciones BlockIcon", "aplicaciones.png"); // 2
	icons.put("Comunicaciones BlockIcon", "comunicaciones.png"); // 3
	icons.put("Contacto BlockIcon", "sugerencias.png"); // 4
	icons.put("Documentos BlockIcon", "documentos.png"); // 5
	icons.put("Dr Bit BlockIcon", "drbit.png"); // 6
	icons.put("Formularios BlockIcon", "formularios.png"); // 7
	icons.put("Fotos BlockIcon", "fotos.png"); // 8
	icons.put("Herramientas BlockIcon", "herramientas.png"); // 9
	icons.put("Simuladores BlockIcon", "simuladores.png"); // 11
	icons.put("Sitios BlockIcon", "sitios.png"); // 12
	icons.put("Teléfonos BlockIcon", "telefonos.png"); // 13
	icons.put("Videos BlockIcon", "videos.png"); // 14
	icons.put("Logo BlockIcon", "logo.png"); // 15
	icons.put("Buscador BlockIcon", "search.png"); // 16
	// icons.put("Listados BlockIcon", "listado.png"); // 17

	return icons;
}
*/

//@Override
//@Transactional
//public void deleteAllViewDetailContent(Domain domain) throws PortalException {
//	getPortalDao().deleteAllViewDetail(domain);
//}

/**
 * 
 * @param domain
 * @return
 * @throws ContentCreationException
 * @throws IOException
 
@Transactional
private synchronized DiagrammableSite makeDirectory(Domain domain) throws ContentMgmtException, ContentCreationException {

	User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();

	DiagrammableSite site;
	site = ServiceLocator.getService(DiagrammableSiteFactoryService.class).create();
	site.setState(ObjectState.ENABLED);
	site.setURI("directory");
	site.setDomain(domain);
	site.setTitle("Indice de Sitios");
	site.setPublicAccess(true);
	site.setSiteType(SiteType.DIRECTORY);
	site.setDescription("Indice de Sitios de la Intranet");

	DiagrammablePage page_home = site.getHomePage();

	DiagrammableArea main = page_home.getArea(0);
	main.setAreaType(DiagrammableArea.AREA_1S);
	main.setOrder(0);
	main.setState(ObjectState.ENABLED);
	main.setLastModifiedOffsetDateTime(OffsetDateTime.now());
	main.setLastModifiedUser(session_user);

	BlockX site_searcher = new KbeeBlockX("site-searcher");
	site_searcher.setName("site-searcher");
	site_searcher.setState(ObjectState.ENABLED);
	site_searcher.setTitle("Buscador de Sitios");
	site_searcher.setBlockImageVisible(false);
	site_searcher.setBlockIntroVisible(false);
	main.add(site_searcher);

	SiteService site_service = site.getService(SiteService.class);
	site_service.save();

	logger.info("done.");
	logger.info("Checking if icons are in the DataBase...");

	return site;
}
*/
/**
 * @param domain
 
@Override
public void deleteAllSitesExternal(Domain domain) {
	
	List<Site> sites = getPortalDao().getSites(domain);
	
	if (sites == null)
		return;

	for (Site site : sites) {
		if (site.isExternal()) {
			try {
				site.getService(SiteService.class).delete();
				} catch (ServiceNotFoundException e) {
					logger.error(e);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
}
*/
/**
 * 
 * Cada site se borra en 1 transacción.
 * 
 * @param domain
 * @param site_type
 * @throws ContentCreationException
 * @throws IOException

@Override
public void deleteAllSites(Domain domain, SiteType site_type, boolean only_internal) throws ContentMgmtException {

	List<Site> sites = getPortalDao().getSites(domain, site_type);

	if (sites == null)
		return;

	for (Site site : sites) {
		if (!only_internal || !site.isExternal()) {
			SiteService site_service = site.getService(SiteService.class);
			site_service.delete();
		}
	}
}
 */
//@Override
//public ResourceReference getLogo() {
//		return getLogo(getDomain());
//}


/**
 * @param domain
 * @return

@Override
public ResourceReference getLogo(Domain domain) {
	ResourceReference ref = logos.get(domain.getId().toString());

	if (ref != null)
		return ref;

	long current_time = System.currentTimeMillis();

	if ((current_time - last_check) > CHECK_INTERVAL)
		loadLogo(domain);

	ref = logos.get(domain.getId().toString());

	if (ref != null)
		return ref;

	return DEFAULT_LOGO;
}
*/

/**
 *
 * El cache de Global Footers de los dominios se invalida
 * 
 * 
 * desde el {@link SiteUpdateListener} cada vez que se editar el sitio de Global
 * Elements
 * 
 * @param domain
 * @return
 * 
 * @Override public Area getGlobalFooter(Domain domain) {
 * 
 *           //if (this.global_footer.containsKey(domain.getId().toString())) //
 *           return this.global_footer.get(domain.getId().toString());
 *           //synchronized (this) { try { Site site =
 *           findGlobalElementsSite(domain); for (Area area:
 *           site.getHomePage().getAreas()) { if
 *           (area.getAreaType()==Area.AREA_FOOTER &&
 *           area.getState()==ObjectState.ENABLED) {
 *           //this.global_footer.put(domain.getId().toString(), area); //break;
 *           return area;
 * 
 *           } }
 * 
 *           } catch (Exception e) { logger.error(" {} | {} | {} | {} ",
 *           (getSessionUser()!=null?getSessionUser().getUserName():""),
 *           e.getClass().getName(),
 *           Thread.currentThread().getStackTrace()[1].getMethodName(),
 *           e.getMessage()); //return
 *           this.global_footer.get(domain.getId().toString()); return null; }
 *           //} //return this.global_footer.get(domain.getId().toString());
 *           return null;
 * 
 *           }
 */

/*
 * @Override public synchronized void invalidateCaches(Domain domain) { String
 * key = domain.getId().toString();
 * 
 * if (this.global_header.containsKey(key)) this.global_header.remove(key);
 * 
 * if (this.global_footer.containsKey(key)) this.global_footer.remove(key);
 * 
 * if (this.global_home.containsKey(key)) this.global_home.remove(key);
 * 
 * }
 */

/**
 * -----------------------------------------------------------------------------------------
 * El cache de Global Headers de los dominios se invalida desde el
 * {@link SiteUpdateListener} cada vez que se editar el sitio de Global Elements
 * 
 * @param domain
 * @return
 * 
 * @Override public Area getGlobalHeader(Domain domain) {
 * 
 *           try { Site site = findGlobalElementsSite(domain); if (site==null)
 *           site = makeGESite(domain);
 * 
 *           Page page = site.getHomePage();
 * 
 *           if (logger.isDebugEnabled()) {
 * 
 *           for (Area area: page.getAreas()) { logger.debug(area.toString()); }
 *           }
 * 
 *           return page.getArea(0);
 * 
 *           } catch (Exception e) { logger.error(" {} | {} | {} | {} ",
 *           (getSessionUser()!=null?getSessionUser().getUserName():""),
 *           e.getClass().getName(),
 *           Thread.currentThread().getStackTrace()[1].getMethodName(),
 *           e.getMessage()); //return
 *           this.global_header.get(domain.getId().toString()); return null; }
 */


/**
 * 
 * Absolute url for a Content
 * 
 * @param content
 * @return

@Override
public String getContentUrl(Content content) {

	try {
		if (content instanceof Question) {
			String content_id = content.getOId().toString();
			return UriHelper.getInstance().getServerURLAndPort(content.getDomain().getName()) + "/drbit/question/"
					+ content_id + "/" + UriHelper.getInstance().getTitle(content);
		} else if (content instanceof Answer) {
			String content_id = ((Answer) content).getQuestion().getOId().toString();
			return UriHelper.getInstance().getServerURLAndPort(content.getDomain().getName()) + "/drbit/question/"
					+ content_id + "/" + UriHelper.getInstance().getTitle(content);
		}

		String base = PortalUriHelper.getInstance().getPortalURL(content.getDomain().getName());
		//DiagrammableSite site = getContentHomeSite(content);

		// TODO VER AT
		Site site = null;
		String site_url, url;

		if (site != null)
			site_url = site.getURI();
		else {
			logger.error("Home Site not found.");
			site_url = "home";
		}
		String classcode = content.getClassCode();
		String content_id = String.valueOf(content.getOId());
		url = base + site_url + "/" + classcode + content_id + "/" + UriHelper.getInstance().getTitle(content);
		return url;

	} catch (Exception e) {
		logger.error(e);
		return null;
	}

}
 */
/*
@Override
public DiagrammableSite getContentHomeSite(Content content) throws PortalException {

	try {
		Classifier clasi = getExternalDao().getSiteClassifier(content.getDomain());
		if (clasi == null) {
			logger.error("Site Classifier is null for Domain " + content.getDomain().getName() + " | "
					+ Thread.currentThread().getStackTrace()[1].getMethodName());
			return null;
		}
		ExternalMember datasetmember = (ExternalMember) content.getDataSetMember(clasi.getName());
		if (datasetmember != null)
			return getPortalDao().findSiteByOId(datasetmember.getExternalId());

	} catch (Exception e) {
		logger.error(e);


	}

	return null;
}**/

/**
protected synchronized void loadLogo(Domain domain) {
	try {
		IDoc logo_content = (IDoc) getContentDao().findContentByName(KbeeIDoc.class, "Logo Icon", domain.getId());
		if (logo_content != null && logo_content.getFiles() != null && logo_content.getFiles().get(0) != null) {
			WebResourceReference imagereference = new WebResourceReference(logo_content.getFiles().get(0),
					logo_content);
			logos.put(domain.getId().toString(), imagereference);
		}
		last_check = System.currentTimeMillis();

	} catch (Exception e) {
		logger.error(e);
	}
}
*/
//private Domain getDomain() {
//	return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
//}


//private User getSessionUser() {
//	try {
//		return ServiceLocator.getService(SecurityService.class).getSessionUser();
//	} catch (Exception e) {
//		return null;
//	}
//}

//private ExternalDao getExternalDao() {
//	return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
//}

//private KBFileImpl createFromMap(Map<String, String> map) throws RuntimeException {
//	return null;
//}

//static String CONTENT_TEMPLATE = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.icons.contenttemplate-image", "Fotos e imágenes");
//static String CONTENT_TYPE_CLASSIFIER = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.icons.content-type-classifier", "Tipo de Contenido");
//static String CONTENT_TYPE_DATASET = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.icons.content-type-dataset", "Tipo de Contenido");
//static String ICON_INSTANCE = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.icons.content-type-dataset.icon-instance", "Icono");

// public static final ResourceReference DEFAULT_LOGO = new PackageResourceReference(KbeePortalHeaderPanel.class,	"logo.png");

// TODO HA
// protected Map<String, ResourceReference> logos = new HashMap<String, ResourceReference>();

// -----------------------------------
	// TODO: HA
	// -----------------------------------
	/**
	 * TODO: Cuando se Edita el Sitio Global Footer se invalida el cache del global
	 * footer. Esto se hace desde el SiteUpdateListener
	 */
	// private Map<String, Area> global_footer = new HashMap<String, Area>();
	// private Map<String, Area> global_header = new HashMap<String, Area>();
	// private Map<String, Site> global_home = new HashMap<String, Site>();
	// private Map<String, Long> last_load = new HashMap<String, Long>();
