package com.novamens.portal6.model;

/**
 * Vista de un elemente publicado
 * Las vistas pueden ser:
 * 
 * ViewBK: Vista publicada en un bloque agregador
 * ViewBKLink
 * ViewBKContent
 * ViewBKSite
 * 
 * 
 * ViewContent -> Vista de detalle de un content.
 * 
 *
 */
public interface View extends PortalObject {

	
	//public void setMetadata(String metadata);
	//public String getMetadata();
	
	public boolean isSearchable();
	
	
	


}
