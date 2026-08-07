package com.novamens.content.web.content.markup;



import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;

import kbee.web.content.editor.ContentEditor;
import kbee.web.util.NavigationEvent;


@SuppressWarnings("serial")
public abstract class ContentPanel<T extends Content> extends ContentEditor<T> {
	private static final long serialVersionUID = 1L;

	private boolean is_right_visible = true;
	private boolean  isnew = false;
	
	 
	public ContentPanel(IModel<T> model) {
		this("editor", model);
	}
	
	
	public ContentPanel(String id, IModel<T> model) {
		super(id, model);
		addListeners();
	}
	 
	public void setRightPanelVisible(boolean value) {
		this.is_right_visible = value;
	}
	
	public boolean isRightPanelVisible() {
		return is_right_visible;
	}
	 
	@Override
	public boolean isNew() {
		return isnew;
	}
	
	@Override
	public void setIsNew(boolean isnew) {
		this.isnew=isnew;		
	}

	protected void addListeners() {
		add(new WicketEventListener<NavigationEvent>() {
			public void onEvent(NavigationEvent event) {
				onNavigate();
			}
		});
	}
	
	public void showInfoPanel(AjaxRequestTarget target) 	{}
	
	protected void onAuditTrail(AjaxRequestTarget target) 	{}
	
	protected void onNavigate() 							{}

	protected WebPage getPortalPreviewPage(IModel<T> model) {
		WebPage page = null;
		if (model.getObject().getContentTemplate().isVideo() || model.getObject().getContentTemplate().isAudio()) 
			 page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-video", model.getObject());
		else if (model.getObject().getContentTemplate().isImage()) 
			page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-video", model.getObject());
		else {
			page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-text" , model.getObject());
		}
		
		return page;
	}
	
	protected boolean isPrivateEnabled() {
		if (!getModelObject().getContentTemplate().isPrivateNotes())
			return false;
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getModelObject());
	}
}
