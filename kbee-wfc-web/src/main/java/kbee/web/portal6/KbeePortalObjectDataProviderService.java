package kbee.web.portal6;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalDataProvider;
import com.novamens.portal6.model.PortalModel;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.portal6.model.Site;
import com.novamens.service.BusinessObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.portal6.panel.PortalErrorPanel;

/**
 * 
 * 
 * Standard MVC architecture:
 * 
 * {@link PortalModel}
 * {@link PortalDataProvider}
 * {@link PortalViewRender}
 * 
 *
 */
public class KbeePortalObjectDataProviderService implements BusinessObjectService, PortalObjectDataProviderService {
			
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalObjectDataProviderService.class.getName());
    
    private PortalObject po  = null;
	
	private Map<String, String> parameters;


	public KbeePortalObjectDataProviderService() {
	}

	public KbeePortalObjectDataProviderService(PortalObject po) {
		 this.po = po;
	}
	
	public PortalObject getObject() {
		return po;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	@Override
	public Panel getDataProviderEditor(String id) {
		return getDataProviderEditor(id, null);
	}

	
	@Override
	public Panel getDataProviderEditor(String id, Map<String, String> parameters) {
		setParameters(parameters);

		if (getObject()==null)
			throw new IllegalArgumentException("getObject is null");
		
		return ServiceLocator.getService(PortalMVCService.class).getEditor( getObject().getKey(), id, getObject());

		
		
		//if 		(getObject() instanceof Area) 			return new AreaPanel(id, new ObjectModel<Area>((Area) getObject()), tab_index, view_mode, getParameters());
		//else if (getObject() instanceof Page)			return new PagePanel(id, new ObjectModel<Page>((Page) getObject()), view_mode, getParameters());
		//else if (getObject() instanceof PageSection) 	return new PageSectionPanel(id, new ObjectModel<PageSection>((PageSection) getObject()), tab_index, view_mode, getParameters());
		//else if (getObject() instanceof Block) 			return new BlockPanel(id, new ObjectModel<Block>((Block) getObject()),tab_index, view_mode, getParameters());
		//logger.debug("Error -> " + getObject().getClass().getSimpleName() + " no Panel available.");
		//return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(getObject()), new Model<String>(getObject().getTitle()));

		
	}

    private ContentDao contentDao;
	private PortalDao portalDao;

    public ContentDao getContentDao() {
		return contentDao;
	}

	public void setContentDao(ContentDao contentDao) {
		this.contentDao = contentDao;
	}

	public PortalDao getPortalDao() {
		return portalDao;
	}

	public void setPortalDao(PortalDao portalDao) {
		this.portalDao = portalDao;
	}

	/**
	 * @return
	 */
	protected IModel<?> getPOModel() {
		
		if 		(getObject() instanceof Site) 			return new ObjectModel<Site>((Site) getObject());
		else if (getObject() instanceof Page) 			return new ObjectModel<Page>( (Page) getObject());
		else if (getObject() instanceof PageSection)	return new ObjectModel<PageSection>( (PageSection) getObject());
		else if (getObject() instanceof Area) 			return new ObjectModel<Area>((Area) getObject());
		else if (getObject() instanceof Block) 			return new ObjectModel<Block>( (Block) getObject());
		return new ObjectModel<PortalObject>(getObject());
	}


}
