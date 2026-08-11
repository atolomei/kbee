package com.novamens.portal.service;

import java.util.Date;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.portal.reports.DVisit;
import com.novamens.portal.reports.UVisit;
import com.novamens.portal6.model.Site;
import com.novamens.service.BusinessSystemService;


/**
 * 
 * 
 * kbee-content-classes
 *
 */

public interface PortalReportsService extends BusinessSystemService {
	
	// Visitas totales ------------------------------------------
	//
	// a un Sitio (visitas y visitantes unicos)
	public List<DVisit> getSiteVisits(Site site, Date from, Date to);
	public List<DVisit> getSiteUniqueVisitors(Site site, Date from, Date to);

	// a una Pagina, y Contenido
	public List<DVisit> getPageUniqueVisitors(com.novamens.portal6.model.Page page, Date from, Date to);
	public List<DVisit> getContentUniqueVisitors(Content content, Date from, Date to);
									
	// a la Intranet
	public List<DVisit> getTotalUniqueVisitors(Date from, Date to);
	

	// Visitas detalladas ------------------------------------------
	//
	// lista de usuarios a un Sitio, Pagina, o Contenido,
	public List<UVisit> getContentDetailedVisitors(Content content, Date from, Date to);
	public List<UVisit> getPageDetailedVisitors(com.novamens.portal6.model.Page page, Date from, Date to);
	public List<UVisit> getSiteVisitors(Content content, Date from, Date to);
	
	
	// -------------------------------------------------------------
	//
	// public void setDataSource(DataSource dataSource);
	// public DataSource getDataSource();
	
}
