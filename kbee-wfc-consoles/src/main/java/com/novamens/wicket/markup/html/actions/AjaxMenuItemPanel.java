package com.novamens.wicket.markup.html.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;


import com.novamens.util.KbeeRuntimeException;

@Deprecated
@SuppressWarnings("serial")
public abstract class AjaxMenuItemPanel<T> extends AbstractLinkMenuItemPanelV5<T> {
						
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(AjaxMenuItemPanel.class.getName());
	
 
	public AjaxMenuItemPanel(String id, String xzx, String xxx, String zxxzx, String zxx) {
		super(id);
	}
	
	public String getTarget() {
		return null;
	}
	
	public String getCssClass() {
		return null;
	}
	
	public String getBeforeClick() {
		return null;
	}
	
	public String getWorkingLabel() {
		return null;
	}
	
	public void onClick() throws Exception {
	}
	
	public abstract void onClick(AjaxRequestTarget target) throws Exception;;
	
	@Override
	protected AbstractLink getNewLink(String id) {
		AjaxLink<?> link = new AjaxLink<Void>(id) {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					AjaxMenuItemPanel.this.onClick(target);
				}
				catch (Exception e) {
					logger.error(e.getClass().getName(), e);
					throw new KbeeRuntimeException(e);
				}
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						if (getWorkingLabel()!=null) {
							s1 = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '"+getLabel()+"';";
							s ="setTimeout(function () {"+s1+"}, 1000);";
						}
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						if (getWorkingLabel()!=null) {
							s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<i class=\"far fa-sync fa-spin\"></i> "+getWorkingLabel()+"';";
						}
						if (getBeforeClick()!=null) {
							if (s==null) s = "";
							s += getBeforeClick();
						}
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		return link;
	}
}
