package kbee.web.portal6.panel;


import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;

// import com.novamens.portal.dao.PortalDiagrammableSiteDao;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.portal6.IPortalWebPanel;

/**
 * otros Panel auxiliares que deben disparar eventos
 * 
 */

public abstract class PortalPanel<T> extends KBPanel implements IPortalWebPanel {
					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalPanel.class.getName());

	
	private static final long serialVersionUID = 1L;

					
	private IModel<T> model;

	Map<String, String> parameters;
	
	
	public PortalPanel(String id) {
		super(id);
		addListeners();
	}

	public PortalPanel(String id, IModel<T> model) {
			this(id, model, null);
		
	}
	public PortalPanel(String id, IModel<T> model, Map<String, String> parameters) {
		super(id);
		setModel(model);
		setParameters(parameters);
		addListeners();
	}


	public  Map<String, String>  getParameters() {
		return parameters;
	}

	public void setParameters( Map<String, String>  parameters) {
		this.parameters = parameters;
	}
	
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}

	
	public  void setModel(IModel<T> model) {
		this.model=model;
	}
	
	public IModel<T> getModel() {
		return this.model;
	}

	
	
	protected IModel<String> getResourceModel(String string) {
		return new StringResourceModel(string, this, null);
	}


	protected IModel<String> getResourceModel(String string, Object[] param) {
		StringResourceModel s= new StringResourceModel(string, this);
		s.setParameters(param);
		return s;
	}
	
	
	protected void addListeners() {
		super.addListeners();
	}

	
	
	
	/**
	 * Domain of the Session User
	 */
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected PortalDao getPortalDao() {
		return (PortalDao) ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}

	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}
	
	
	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}
	
	
	protected String getUserPreference(String key) {
		return getUserPreference(key, null);
	}
	
	
	protected String getUserPreference(String key, String defaultValue) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null) {
			String s = user.getService(PreferencesService.class).getValue("portal", key);
			return s==null?defaultValue:s;
		}
		return null;
	}

	protected void setUserPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null)
			user.getService(PreferencesService.class).setValue("portal", key, value);
	}

	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}

	

}
