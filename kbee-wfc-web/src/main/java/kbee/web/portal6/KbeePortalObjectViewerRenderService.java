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
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.portal6.model.Site;
import com.novamens.service.BusinessObjectService;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.portal6.panel.AreaPanel;
import kbee.web.portal6.panel.BlockPanel;
import kbee.web.portal6.panel.PagePanel;
import kbee.web.portal6.panel.PageSectionPanel;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalObjectMetadataPanel;

public class KbeePortalObjectViewerRenderService implements BusinessObjectService, PortalObjectViewerRenderService {
			
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalObjectViewerRenderService.class.getName());

    /**
	 *   The </b>TxLogger</b> is set up in Log4J to log synchronoulsy with the Thread.
	 *   This is different from all the other logs that work asynchronously
     */
	static private Logger txLogger = LogManager.getLogger("TxLogger");
	
    private PortalObject po  = null;

    private ContentDao contentDao;
	private PortalDao portalDao;
	
	private Map<String, String> parameters;


	public KbeePortalObjectViewerRenderService() {
	}

	public KbeePortalObjectViewerRenderService(PortalObject po) {
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
	public Panel getMetaInfoPanel(String id) {
		
		if (getObject() instanceof Site) {
			return new PortalObjectMetadataPanel<Site>(id,  new ObjectModel<Site>((Site) getObject()));
		}
		else if (getObject() instanceof Page) {
			return new PortalObjectMetadataPanel<Page>(id,  new ObjectModel<Page>((Page) getObject()));
		}
		else if (getObject() instanceof PageSection) {
			return new PortalObjectMetadataPanel<PageSection>(id,  new ObjectModel<PageSection>((PageSection) getObject()));
		}
		else if (getObject() instanceof Area) {
			return new PortalObjectMetadataPanel<Area>(id,  new ObjectModel<Area>((Area) getObject()));
		}
		else if (getObject() instanceof Block) {
			return new PortalObjectMetadataPanel<Block>(id,  new ObjectModel<Block>((Block) getObject()));
		}
		return new PortalObjectMetadataPanel<PortalObject>(id,  new ObjectModel<PortalObject>((PortalObject) getObject()));
	}

	@Override
	public Panel build(String id) {
		return build(id, 0, PortalViewMode.PRODUCTION, null);
	}
	

	@Override
	public Panel build(String id, PortalViewMode view_mode) {
		return build(id, 0, view_mode, null);
	}

		
	@Override
	public Panel build(String id, int tab_index, PortalViewMode view_mode, Map<String, String> parameters) {
		
		setParameters(parameters);
		
		if (getObject()==null)
			throw new IllegalArgumentException("getObject uis null");
		
		if 		(getObject() instanceof Area) 			return new AreaPanel(id, new ObjectModel<Area>((Area) getObject()), tab_index, view_mode, getParameters());
		else if (getObject() instanceof Page)			return new PagePanel(id, new ObjectModel<Page>((Page) getObject()), view_mode, getParameters());
		else if (getObject() instanceof PageSection) 	return new PageSectionPanel(id, new ObjectModel<PageSection>((PageSection) getObject()), tab_index, view_mode, getParameters());
		else if (getObject() instanceof Block) 			return new BlockPanel(id, new ObjectModel<Block>((Block) getObject()),tab_index, view_mode, getParameters());
		
		logger.debug("Error -> " + getObject().getClass().getSimpleName() + " no Panel available.");
		
		return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(getObject()), new Model<String>(getObject().getTitle()));
	}
	
	@Override
	public Panel build() {
		return build("panel");
	}

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
