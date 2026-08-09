package kbee.web.listener;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.model.DataSet;
 
import com.novamens.content.model.ExternalDao;
import com.novamens.content.model.ExternalMember;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.BeforeUpdateEvent;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
 
import com.novamens.logging.DataSetValueUpdateEvent;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;

/**
 * 
 * Este listener escucha los eventos de actualización de Sitios, generados por
 * {@link PortalDiagrammableSiteService}
 * {@link SiteService}
 * 
 * Es llamado por el {@link SpringEventsService}
 */
public class SiteUpdateListener implements EventListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SiteUpdateListener.class.getName());
	
	// Logger synchronous with the TRX	*/
	static private Logger txlogger = LogManager.getLogger("TxLogger");

	
	private ExternalDao dao;
	// private DataSet siteSet;

	public boolean listen(Event event) {
		return ((event instanceof BeforeUpdateEvent) && event.getObject() instanceof Site);
	}

	/**
	 * 
	 * <p>
	 * {@link AppDeleteEvent} Los grupos y las Rules los Borra el Sitio
	 * {@link PortalDiagrammableSiteService}
	 * </p>
	 * 
	 * <p>
	 * <b>IMPORTANT</b>. 
	 * This method executes SYNCHRONOUSLY with the transaction, so it must be reasonable fast
	 * </p>
	 */
	public void onEvent(Event event) {

		Site site = (Site) event.getObject();

		// Si el sitio es externo no tiene que mapear a un DataSet
		// ---------------------------------
		//
		if (site == null || site.isExternal())
			return;

		// Si el evento es de DeleteEvent
		// ----------------------------------------------------------
		//
		try {

			if (!(event instanceof AppDeleteEvent)) {

				// Si el evento es de UpdateEvent
				// Actualizar el DataSetMember Sitio y Repositorio
				//
				Domain domain = site.getDomain();

				if (domain != null) {

					//String base = Portal-UriHelper.getInstance().getPortalURL(domain.getName());
					//String site_url = base + site.getURI();
					String site_url = ServiceLocator.getService(PortalUrlService.class).getSiteUrl(site);
					String site_title = site.getTitle().toLowerCase().trim();

					if (site_title == null)
						site_title = "";

					if (getExternalDao().getSiteDataSet(domain) != null && getExternalDao().getSiteRepositoryDataSet(domain) != null) {

						DataSet site_dataset = getExternalDao().getSiteDataSet(site.getDomain());
						ExternalMember dm_site = (ExternalMember) getExternalDao().findMemberByExternalId(site.getOId(), site_dataset);

						DataSet repo_dataset = getExternalDao().getSiteRepositoryDataSet(site.getDomain());
						ExternalMember dm_repo = (ExternalMember) getExternalDao().findMemberByExternalId(site.getOId(), repo_dataset);

						if (dm_site != null && dm_repo != null) {
							boolean b_title = dm_site.getStrValue().toLowerCase().trim().equals(site_title);
							boolean b_url = dm_site.getExternalUrl().trim().equals(site_url.trim());
							boolean b_state = (dm_site.getState() == site.getState() && dm_repo.getState() == site.getState());
							if (!b_title || !b_url || !b_state) {
								
								getExternalDao().update(site.getOId(), site.getTitle(), site_url, site.getState(),	getExternalDao().getSiteDataSet(domain));
								txlogger.info(new DataSetValueUpdateEvent(dm_site, "Update"));
								
								getExternalDao().update(site.getOId(), site.getTitle(), site_url, site.getState(),	getExternalDao().getSiteRepositoryDataSet(domain));
								txlogger.info(new DataSetValueUpdateEvent(dm_repo, "Update"));
							}
						}
					}
				}

				else {
					logger.warn("Site Domain is null. " + event.toString());
				}
			}

		} catch (Exception e) {
			logger.error(e);
			
		}
	}

	//public void setSiteSet(DataSet dataSet) {
	//	this.siteSet = dataSet;
	//}

	//public DataSet getSiteSet() {
	//	return this.siteSet;
	//}

	public ExternalDao getExternalDao() {
		return dao;
	}

	public void setExternalDao(ExternalDao dao) {
		this.dao = dao;
	}

	/**
	 */
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
