package com.novamens.wicket.markup.html.panel;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;

/**
 * 
 * <p>
 * Panel without markup.
 * base panel for the application. 
 * It can propagate Wicket Events to the rest of the Page</p>
 * 
 *
 */
public class KBPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	private IModel<?> model;
	
	public KBPanel(String id) {
		this(id, null);
	}
	
	public KBPanel(String id, IModel<?> model) {
		super(id, model);
		this.model=model;
		addListeners();
	}
	
	

	public void onDetach() {
		super.onDetach();
		if (this.model!=null)
			this.model.detach();
	}
	
	@SuppressWarnings("unchecked")
	public void fireScanAll(Event event) {
		if (findPage()!=null) {
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
			}
		}
		fire(event, getPage().iterator(), false);
		}
	}

	public boolean fire(Event event, Iterator<Component> components) {
		return fire(event, components, true);
	}
	
	@SuppressWarnings("unchecked")
	public boolean fire(Event event, Iterator<Component> components, boolean stop_first_hit) {
		boolean handled = false;
		while (components.hasNext()) {
			Component component = components.next();
			for (WicketEventListener<Event> listener : component.getBehaviors(WicketEventListener.class)) {
				if (listener.handle(event)) {
					listener.onEvent(event);
					if (stop_first_hit) {
						handled = true;
						break;
					}
				}
			}
			if (!handled) {
				if (component instanceof MarkupContainer) {
					handled = fire (event, ((MarkupContainer)component).iterator(), stop_first_hit);
				}
			}
			else {
				break;
			}
		}
		return handled;
	}

	/**
	 * Scans Page and all its components
	 * The first Component that listens to this event will handle it
	 * 
	 **/
	@SuppressWarnings("unchecked")
	public void fire(Event event) {
		boolean handled=false;
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
					handled = true;
					break;
				}
			}
		if (!handled) 
			fire(event, getPage().iterator());
	}

	
	/**
	 * 
	 * URL from HTTP Request received
	 * Wicket based
	 * 
	 * @return
	 */
	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}
	
	protected void addListeners() {
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected String getLabelString(String key) {
		return getLabel(key).getObject();
	}
	
	protected String getLabelString(String key, String... parameter) {
		return getLabel(key, parameter).getObject();
	}
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this, null);
		model.setParameters((Object[])parameter);
		return model;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected PortalDao getPortalDaoDao() {
		return (PortalDao) ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	protected String getSystemParameter(String name, String defaultvalue) {
		return ServiceLocator.getService(SystemParameterService.class).getParameter(name, defaultvalue);
	}
}
