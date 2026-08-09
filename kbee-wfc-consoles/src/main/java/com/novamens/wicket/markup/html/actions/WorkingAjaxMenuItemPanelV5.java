package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.AbstractLink;


import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;



public abstract class WorkingAjaxMenuItemPanelV5<T> extends AbstractLinkMenuItemPanelV5<T> {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkingAjaxMenuItemPanelV5.class.getName());

	public WorkingAjaxMenuItemPanelV5(String id) {
		super(id);
	}

	@Override
	public void onClick() throws Exception {
	}
		
	public abstract void onClick(AjaxRequestTarget target) throws Exception;
	
	public String getIndicatingLabel() {
		return null;
	}
	
	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public String getCssClass() {
		return null;
	}
	
	@Override
	public String getBeforeClick() {
		return null;
	}

	@Override
	protected AbstractLink getNewLink(String id) {
		WorkingAjaxLink<T> link = new WorkingAjaxLink<T>(id) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				try {
					WorkingAjaxMenuItemPanelV5.this.onClick(target);
				}
				catch (Exception e) {
					logger.error(e);
					throw new RuntimeException(e);
				}
			}
		};
		
		if (getTarget()!=null)
			link.add(new AttributeModifier("target", getTarget()));
		
		if (getIndicatingLabel()!=null)
			link.setIndicatingLabel(getIndicatingLabel()); 
		
		return link;
	}
}
