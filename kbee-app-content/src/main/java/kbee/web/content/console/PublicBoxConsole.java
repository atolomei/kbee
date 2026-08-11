package kbee.web.content.console;


import org.apache.wicket.model.IModel;

import com.novamens.content.document.IDoc;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.MyBoxQuery;


public abstract class PublicBoxConsole extends MyBoxConsole {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PublicBoxConsole.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	final public static String NAME = "publicresources";
	
	IModel<User> um;
	
	public PublicBoxConsole(Query query) {
		super(NAME, query);
	}
	
	
	@Override
	public void onInitialize() {
		
		try {
			User user = getDomain().getService(DomainService.class).getPublicResourcesUser();
			if (user==null) 
				user=getDomain().getService(DomainService.class).createPublicResourcesUserIfNotExists();
		um = new ObjectModel<User>(user);
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
		
		super.onInitialize();
		
	}
	
	
	/**
	 * 
	 * @param file
	 */
	@Override
	protected void createContent(KBFile file) {
		try {
			IDoc idoc = (IDoc)ServiceLocator.getService(ContentFactoryService.class).create(getDomain().getService(DomainService.class).getResourcesTemplate().getName(), file, ObjectState.DRAFT, um.getObject());
			idoc.addClassification(getDomain().getService(DomainService.class).getResourcesTypeClassifier(), getDomain().getService(DomainService.class).getResourcesTypeDataSetMember());
			idoc.getService(ContentService.class).update();
			
		}
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}

	
	public void onDetach() {
		super.onDetach();
		if (um!=null)
			um.detach();
	}
	@Override
	public Query newQuery() {
		return setUserPreference(new MyBoxQuery(getQueryIndex(), um.getObject()));
	}
	
	

}
