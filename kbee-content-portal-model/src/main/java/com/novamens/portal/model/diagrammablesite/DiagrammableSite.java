package com.novamens.portal.model.diagrammablesite;

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebPage;

// import org.apache.wicket.markup.html.WebPage;

import com.novamens.content.document.IDoc;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewBK;

/**
 * Diagramables
 * ------------
 * Secretarias
 * Area. RRHH, Banca Comercial, ....
 * Informacion Operativa
 * Hola
 * 
 * Externo
 * -------
 * 
 * Interno no diagramable
 * ----------------------
 * - BuscadorContenidos
 * - PhotoWall
 * - drbit
 * - manuales
 * - Digital Library/Knowledge Base
 * 
 * . Tipo de sitio
 * . Administrador
 * . LogVisit
 * . Directory
 *   Site Editor (blocks, Internos-no-diagramables)
 * 
 * 
 * ------------------------------
 * Site
 * DiagramableSite
 * NonDiagramableSite
 * ExternalSite
 * ------------------------------
 * 
 * SiteEditorCompatible. si soporta site editor
 * 
 * DiagrammableSite
 * NonDiagrammableSite
 * 
 * InternalSite
 * Externalsite
 * 
 * 
 * 
 * 
 * 
 * */
public interface DiagrammableSite extends Site {



	public boolean isDetailInformEnabled();
	public boolean isDetailCommentsEnabled();
	public boolean isDetailVotesEnabled();
	public boolean isDetailFollowEnabled();
	public boolean isDetailRelatedEnabled();
	public boolean isDetailSendEnabled();
	public boolean isDetailToolsEnabled();

	

	public Map<String, String> getDetailSettingsInfo();
	public KBFile getSiteImage();
	public IDoc getImageContainer();
	public boolean isSiteImageVisible();
	
 	
}










/**
 * 	// public List<DiagrammablePage> getPages();
// public void add(DiagrammablePage page);
// public void remove(DiagrammablePage page);

public void setDetailCommentsEnabled	(boolean b);
public void setDetailVotesEnabled		(boolean b);
public void setDetailFollowEnabled		(boolean b);
public void setDetailRelatedEnabled		(boolean b);
public void setDetailSendEnabled		(boolean b);
void setDetailInformEnabled(boolean b);
*/

//public boolean isHomPage();
//public DiagrammablePage getHomePage();
//public DiagrammablePage getPage(String page_name);
//public WebPage getWebPage();
//public PortalPage getPortalHomePage();
//public DiagrammablePage getPageByUrl(String relative_url);


// public void setStateAll(ObjectState enabled);

// public void setHeader(DiagrammableBlock header);
	// public DiagrammableBlock getHeader();

// public void addToNotify(ViewBK view);
// public void removeToNotify(ViewBK view);
// public void resetToNotify();
// public Map<String, ViewBK> getViewsToNotify();
// public boolean isGlobalElementsSite();
// public DiagrammablePage getPageHeaderFooter();
// public void setPageHeaderFooter(DiagrammablePage page);
// public void createPageHeaderFooter();
// public void setSiteImageVisible(boolean siteimagevisibile);
// public DiagrammablePage getReportsPage();
// public void setContentSiteImage(IDoc block_image);
