package kbee.web.service;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.user.UserProfile;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.portal6.model.Site;
import com.novamens.service.BusinessSystemService;
import com.novamens.service.FactoryService;

/**
 *  Payload Panel contained by a structure element (Block, Area, PageSection)
 */
public interface PortalPanelService  extends BusinessSystemService, FactoryService , PortalService {

	/**
	 * @param key   Block, Area, PageSection key  getKey()
	 * @param classname WicketPanel to render
	 */
	//public void registerClassPanel(String key, String classname);

	public Panel getInternalHeadPanel(String id, Block block);
	public Panel getInternalHeaderPanel(String string, Block object, PortalViewMode viewMode,  Map<String, String> parameters);
	
	public Panel getInternalBodyPanel(String id, Block block);
	public Panel getInternalBodyPanel(String id,  Block block, PortalViewMode viewmode);
	public Panel getInternalBodyPanel(String id,  Block block, PortalViewMode viewmode, Map<String, String> parameters);

	public Panel getInternalHeaderPanel(String id, Area area);
	public Panel getInternalHeaderPanel(String id, Area area, PortalViewMode viewmode,  Map<String, String> parameters);

	public Panel getInternalBodyPanel(String id, Area area);
	public Panel getInternalBodyPanel(String id, Area area, PortalViewMode viewmode);
	public Panel getInternalBodyPanel(String id, Area area, PortalViewMode viewmode, Map<String, String> parameters);	
	
	
	public Panel getInternalHeaderPanel(String id, PageSection ps);
	public Panel getInternalHeaderPanel(String id, PageSection ps, PortalViewMode viewmode,  Map<String, String> parameters);

	public Panel getInternalBodyPanel(String id, PageSection ps);
	public Panel getInternalBodyPanel(String id, PageSection ps, PortalViewMode viewmode);
	public Panel getInternalBodyPanel(String id, PageSection ps, PortalViewMode viewmode, Map<String, String> parameters);
	

	//public Panel getInternalBodyPanel(String id, Page page);
	//public Panel getInternalBodyPanel(String id,  Page page, PortalViewMode viewmode);
	//public Panel getInternalHeaderPanel(String id, Page page);
	//public Panel getInternalHeaderPanel(String id, Page page, PortalViewMode viewmode);
	 
	
	public  Panel getGlobalFooterPanel();
	public  Panel getGlobalHeaderPanel(Site site);
	
	public WebPage getWebPage(Serializable siteid);
	public  WebPage getWebPage(Site site);
	public  WebPage getEditorWebPage(Site object);
	public  WebPage getStartPage(UserProfile profile);
	

}
