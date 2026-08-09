package kbee.web.portal6;

import java.util.Map;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.portal6.model.PortalDataProvider;
import com.novamens.portal6.model.PortalModel;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.BusinessObjectService;


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
public class KbeePortalObjectModelService implements BusinessObjectService, PortalObjectModelService {
			
	  private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalObjectModelService.class.getName());
	    
		
		
	    private PortalObject po  = null;
	    private ContentDao contentDao;
		private PortalDao portalDao;
		
		private Map<String, String> parameters;


		public KbeePortalObjectModelService() {
		}

		public KbeePortalObjectModelService(PortalObject po) {
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


}
