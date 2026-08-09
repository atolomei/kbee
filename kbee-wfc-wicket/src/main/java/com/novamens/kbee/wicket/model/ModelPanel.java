package com.novamens.kbee.wicket.model;


import org.apache.wicket.model.IModel;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

/**
 * <p>A panel without markup.
 * The purpose of this panel is to hold a model and be able to propagate Events to the rest of the Page's components.
 * </p>
 * @param <T>
 */
public class ModelPanel<T> extends KBPanel {
																							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ModelPanel.class.getName());

	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;
	
	public ModelPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public ModelPanel(String id, IModel<T> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public T getModelObject() {
		if (getModel()!=null)
			return getModel().getObject();
		return null;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null) {
			try { 
				model.detach();
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	
}
