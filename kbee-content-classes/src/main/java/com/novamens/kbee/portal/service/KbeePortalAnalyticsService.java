package com.novamens.kbee.portal.service;



import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
// import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


import com.novamens.content.dao.PortalDao;
import com.novamens.portal.service.PortalAnalyticsService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.ViewBKBlock;
import com.novamens.portal6.model.ViewBKContent;
import com.novamens.portal6.model.ViewBKLink;
import com.novamens.portal6.model.ViewBKSite;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
// import com.novamens.site.logging.SiteStatOutEvent;
import com.novamens.site.logging.SiteStatOutEvent;


/**
 * <p>
 * Servicio de Log de clicks salientes.
 * </p>
 * <p>
 * Se loguea los clicks salientes en las {@link ViewBK} únicamente (out).
 * </p>
 *
 * 
 */
public class KbeePortalAnalyticsService implements PortalAnalyticsService {

	static final int MAX_CACHE = 20;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalAnalyticsService.class.getName());
																
	// static private final String workdir = PropertiesFactory.getInstance("kbee").getProperties().getProperty("work",	"work");
	//static private final String dir = workdir + File.separator + "views";

	private PortalDao portalDao = null;

	//
	// TODO: HA
	//
	// private org.mapdb.DB db = null;
	@SuppressWarnings("unused")
	private Map<String, ConcurrentLinkedQueue<String>> data = null;

	private AtomicInteger err_counter = new AtomicInteger(0);

	/**
	static {
		File rootdir = new File(dir);
		if (!(rootdir.exists() && rootdir.isDirectory())) {
			logger.info("Creating root dir: " + dir);
			try {
				KbeeFileUtils.forceMkdir(rootdir);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}**/

	public KbeePortalAnalyticsService() {
	}

	@Override
	public void add(User user, Site site, ViewBK view) {
		add(user, site, view, true);
	}

	@Override
	public void add(User user, Site site, ViewBK view, boolean include_recent) {

		boolean requires_commit = false;

		if (err_counter.intValue() > 100)
			include_recent = false;

		//
		// TODO click en un ViewVKContent o Site pasa por aca
		// Excepto el menu de accesos frecuentes de cada usuario
		//
		// Agrega al log de registro de logEvent outbound.
		//

		if (!(view instanceof ViewBKBlock)) {

			SiteStatOutEvent stat = new SiteStatOutEvent();
			
			stat.domain_id = Long.valueOf(view.getDomain().getId().toString());
			
			if (site != null) {
				stat.site_id = (Long) site.getOId();
				stat.site_title = site.getTitle();
			}
			stat.view_id = (Long) view.getOId();
			stat.view_type = view.getViewType();
			stat.view_title = view.getTitle();

			try {
				if (view instanceof ViewBKContent)
					stat.view_content_id = ((ViewBKContent) view).getContent().getOId();
				else if (view instanceof ViewBKLink)
					stat.view_link = ((ViewBKLink) view).getLink();
				else if (view instanceof ViewBKSite)
					stat.view_site_id = (Long) ((ViewBKSite) view).getReferencedSite().getOId();

				if (view.getBlock() != null) {
					stat.page_id = (Long) view.getBlock().getParent().getOId();
					stat.page_title = view.getBlock().getParent().getTitle();
					stat.block_id = (Long) view.getBlock().getOId();
					stat.block_title = view.getBlock().getTitle();
				}

				stat.user_id = Long.valueOf(user.getId().toString());
				stat.user_name = user.getFirstLastName();
				stat.timestamp = OffsetDateTime.now();

				logger.info(stat);

			} catch (Exception e) {
				logger.error(e,  " logging  SiteStatOutEvent");
				return;
			}
		}

		// if the click does not require log (like a banner) or
		// if error_counter is greater than 100
		// there is some problem with MapDB and
		// we abort.
		//

		if (!include_recent) {
			return;
		}

		/*
		try {
			// agregar tb en el historial global
			//
			//
			String keyglobal = user.getId() + "-kbee-global-site-nm";
			ConcurrentLinkedQueue<String> list_global = getDB().get(keyglobal);

			if (list_global == null)
				list_global = new ConcurrentLinkedQueue<String>();

			requires_commit = true;

			if (list_global.contains(view.getId().toString())) {
				list_global.remove(view.getId().toString());
				list_global.add(view.getId().toString());
			} else {
				list_global.add(view.getId().toString());
				if (list_global.size() > MAX_CACHE)
					list_global.remove();
			}
			getDB().put(keyglobal, list_global);
		} catch (org.hibernate.ObjectNotFoundException e) {
			logger.warn(e);

		} catch (Exception e1) {
			logger.error(e1,  " logging  add in Site History");
			err_counter.incrementAndGet();

		} finally {
			if (requires_commit)
				if (db != null)
					db.commit();
		}
		*/
	}

	/**
	 */
	@Override
	public List<ViewBK> getRecentViews(User user) {
		return new ArrayList<ViewBK>();
		/**
		if (err_counter.intValue() > 100) {
			return new ArrayList<ViewBK>();
		}

		String key = user.getId() + "-kbee-global-site-nm";

		List<ViewBK> list;

		try {
			if (getDB().get(key) == null) {
				list = new ArrayList<ViewBK>();
			} else {

				Stack<String> stack = new Stack<String>();
				for (String view : getDB().get(key))
					stack.push(view);

				list = new ArrayList<ViewBK>(stack.size());

				while (!stack.isEmpty())
					list.add(getPortalDao().findViewById(Long.valueOf(stack.pop())));
			}

			return list;
		} catch (Exception e) {
			logger.error(e);
			err_counter.incrementAndGet();
			return new ArrayList<ViewBK>();
		}
		*/

	}

	public PortalDao getPortalDao() {
		return portalDao;
	}

	public void setPortalDao(PortalDao dao) {
		this.portalDao = dao;
	}

	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * 
	 * @return
	 */
	 
	/**
	private Map<String, ConcurrentLinkedQueue<String>> getDB() {
		//if (data == null)
			startMapDB();
		return data;
	}
	*/

	
	/**
	 * 
	 * @return

	 
	private void startMapDB() {
		logger.info("Starting MapDB.");
		try {

			db = DBMaker.fileDB(new File(dir + File.separator + "mapdb")).closeOnJvmShutdown().transactionEnable()
					.make();

			data = (Map<String, ConcurrentLinkedQueue<String>>) db
					.<String, ConcurrentLinkedQueue<String>>hashMap("views").createOrOpen();
			err_counter.set(0);

		} catch (Throwable e) {
			logger.error(e);
			logger.info("Trying to re-create MapDB files. Portal Analytics Service");
			File rootdir = new File(dir);
			try {
				if (rootdir.exists()) {
					FileUtils.force---Delete(rootdir);
					Thread.sleep(800);
				}
				KbeeFileUtils.forceMkdir(rootdir);
				db = DBMaker.fileDB(new File(dir + File.separator + "mapdb")).closeOnJvmShutdown().transactionEnable()
						.make();

				data = (Map<String, ConcurrentLinkedQueue<String>>) db
						.<String, ConcurrentLinkedQueue<String>>hashMap("views").createOrOpen();
				err_counter.set(0);

			} catch (Exception e1) {
				logger.error(e1);
				data = null;
				logger.error("Can not open MapDB. Data file is corrupt: " + dir + File.separator + "mapdb"
						+ " . Please shutdown the application, delete the file and restart");
			}
		}
	}
		 */
}
