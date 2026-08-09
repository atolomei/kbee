package com.novamens.wicket.markup.html.actions;


import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;


public abstract class AbstractMenuItemPanelV5<T> extends Panel implements MenuItem {
	
	private static final long serialVersionUID = 1L;
					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractMenuItemPanelV5.class.getName());
	
	private IModel<T> model;
	private IModel<T> model_sec;
	private int index;
	
	private String iconcss = null;  

	
	public AbstractMenuItemPanelV5(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public AbstractMenuItemPanelV5(String id, IModel<T> model, String iconcss) {
		super(id);
		this.model=model;
		model_sec=model;
		this.iconcss=iconcss;
	}
	
	public AbstractMenuItemPanelV5(String id, IModel<T> model) {
		super(id);
		this.model=model;
		model_sec=model;
	}
	
	public AbstractMenuItemPanelV5(String id, String iconcss) {
		super(id);
		this.iconcss=iconcss;
	}

	public String getTarget() {
		return null;
	}
	
	public IModel<T> getModelSec() {
		return model_sec;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	public void setModel(IModel<T> model)  {
		this.model = model;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	/**
	 * Css of the menu item <li>
	 * 
	 */
	public String getCssClass() {
		return null;
	}
	
	/**
	 * Css of the icon  <i>
	 * 
	 */
	public String getIconCssClass() {
		return iconcss; 	
	}
	
	public void setIconCssClass(String c) {
		iconcss=c; 	
	}
	
	public String getIcon() {
		return null;
	}
	
	public String getUrl() {
		return null;
	}
	
	public void onDetach() {
		try { 
			if (this.model!=null) 
				this.model.detach();
		} 
		catch (Exception e) {
			logger.error(e);
		}
		super.onDetach();
	}
	
	@SuppressWarnings("unchecked")
	public void fireScanAll(Event event) {
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
			}
		}
		
		fire(event, getPage().iterator(), false);
	}

	protected boolean fire(Event event, Iterator<Component> components) {
		return fire(event, components, true);
	}
	
	@SuppressWarnings("unchecked")
	protected boolean fire(Event event, Iterator<Component> components, boolean stop_first_hit) {
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
}
