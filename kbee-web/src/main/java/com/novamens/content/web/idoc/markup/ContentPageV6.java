package com.novamens.content.web.idoc.markup;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.ContentId;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Proxy;
import com.novamens.kbee.wicket.markup.html.event.EventHandler;
import com.novamens.kbee.wicket.markup.html.event.EventListenerWicket;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;

import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ConsoleObjectPage;

public class ContentPageV6<T extends Content> extends ConsoleObjectPage<T> implements EventHandler, NavigablePage<Content> {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentPageV6.class.getName());

	
	IModel<Site> site_model;
	//private IModel<Cursor> cursor_model;
	
	private Navigator<Content> navigator;
	
	private Searcher searcher = null;
	//private long index = 0;
	private List<IModel<Activity>> activities;
	private Boolean readOnly = null;
	
	
	public ContentPageV6() {
	}
	
	public ContentPageV6(IModel<T> model) {
		super(model);
		setPageParameters();
	}
	
	/**
	 * 
	 * 
	 */
	public void setReadOnly(boolean s) {
		this.readOnly=Boolean.valueOf(s);
	}
	
	protected boolean isExternal() {
		return getModel().getObject().isExternal();
	}
	
	public boolean isReadOnly() {
		
		if (this.readOnly!=null)
			return this.readOnly.booleanValue();
		//  -------------------------------
		//
		// External Files are read-only
		//
		if (getModel().getObject().isExternal()) {
			this.readOnly=Boolean.valueOf(true);
			return this.readOnly.booleanValue();
		}

		//  -------------------------------
		//
		// user does not have permission to write 
		//
		if (!isWriteable()) {
			this.readOnly=Boolean.valueOf(true);
			return this.readOnly.booleanValue();
		}

		// -------------------------------
		//
		// Archived can only be moved to the Library
		//
		if (getModelObject().isArchived()) { 
				this.readOnly=Boolean.valueOf(true);
				return this.readOnly.booleanValue();
		}

		
		//  -------------------------------
		//
		// Recycled can be Restored
		// 
		if (getModelObject().isRecycled()) { 
			this.readOnly=Boolean.valueOf(false);
			return this.readOnly.booleanValue();
		}

		// If at least one of the Libraries of the file is not ReadOnly 
		// 
		if (getModelObject().isEnabled()) {
			List<Library> libraries = getModelObject().getDomain().getService(LibraryService.class).getLibraries(getModelObject());
			if (!libraries.isEmpty()) { 
				for (Library li: libraries)
					if (!li.isReadOnly()) {
						this.readOnly=Boolean.valueOf(false);
						return this.readOnly.booleanValue();
					}
			}
		}
		
		this.readOnly = Boolean.valueOf(true);
		return readOnly.booleanValue();
	}

	public IModel<Site> getSiteModel() {
		return this.site_model;
	}
	
	protected boolean isPrivateEnabled() {
		if (!getModelObject().getContentTemplate().isPrivateNotes())
			return false;
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getModelObject());
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}
	
	public void onDetach() {
		super.onDetach();
		if (site_model!=null)
			site_model.detach();
		if (activities!=null)
			activities.forEach(item -> item.detach());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	
	@SuppressWarnings("unchecked")
	protected T getContent(PageParameters parameters) {
		try {
			T content = null;		
			Class<T> contentclass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];		
			StringValue oid = parameters.get("oid");
			if (!oid.isNull() && !oid.isEmpty()) { 
				StringValue id = parameters.get("id");
				if (id.isNull() || id.isEmpty()) { 
					content = (T)getContentDao().findContentByOId(Long.valueOf(oid.toString()));
				}
				else {
					content = (T)getContentDao().findContentById(contentclass, id);
				}
			}	
			return content;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Searcher getSearcher() {
		return searcher;
	}

	protected boolean isWriteable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getModelObject());
	}
	
	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
	
	
	protected List<IModel<Activity>> getActivities() {
		if (activities!=null)
			return activities;
		
		com.novamens.workflow.Process process= getModel().getObject().getService(WorkflowService.class).getLastProcess();
		activities = new ArrayList<IModel<Activity>>();
		if (process==null)
			return activities;
		List<Activity> list= process.getActivities();
		if (list==null)
			return activities;
		for (Activity a: list) {
			activities.add(new ObjectModel<Activity>(a));
		}
		return activities;
	}
	
	protected void setPageParameters() {
		getPageParameters().set("oid", getModel().getObject().getOId().toString());
		
		if (!getModel().getObject().isHeadVersion()) {
			getPageParameters().set("ver", "v"+String.valueOf(getModel().getObject().getVersion()));
			getPageParameters().set("id", getModel().getObject().getId().toString());
		}
	}
	
	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}	
	
	protected boolean isDeletable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(model.getObject());
	}
	
	@Override
	protected String getName() {
		return "content";
	}
	
	@Override
	protected String getPageType() {
		return "det";
	}
	
	@Override
	protected String getContentTitle() {
		return getModel().getObject().getTitle(); 
	}
	
	@Override
	protected String getContentId() {
		return new ContentId(getModel().getObject()).toString();
	}
	
	@Override
	protected Serializable getContentOId() {
		return getModel().getObject().getOId();
	}
	
	@Override				
	protected Serializable getCId() {
		return getModel().getObject().getId();
	}
	
	@Override
	protected Integer getContentVersion() {
		return Integer.valueOf(getModel().getObject().getVersion());
	}
	
	@Override
	protected String getStatsPageTitle() {
		return "det-"+getModel().getObject().getContentTemplate().getContentClassCode();
	}

	
	@Override
	public Navigator<Content> getNavigator() {
		return this.navigator;
	}

	
	@Override
	public void setNavigator(Navigator<Content> navigator) {
		this.navigator=navigator;
	}
	

	@Override
	public void handle(final WicketAjaxEvent event) {
		visitChildren(new IVisitor<Component, Void>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void component(Component component, IVisit<Void> visit) {
 				List<EventListenerWicket> listeners = component.getBehaviors(EventListenerWicket.class);
				for (EventListenerWicket listener : listeners) {
					if (listener.handle(event))
						listener.onEvent(event);
				}
			}
		});
		visitChildren(new IVisitor<Component, Void>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void component(Component component, IVisit<Void> visit) {
 				List<WicketEventListener> listeners = component.getBehaviors(WicketEventListener.class);
				for (WicketEventListener listener : listeners) {
					if (listener.handle(event))
						listener.onEvent(event);
				}
			}
		});
	}
	
}