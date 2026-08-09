package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.util.KbeeRuntimeException;

public abstract class AjaxMenuItemPanelV5<T> extends AbstractLinkMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(AjaxMenuItemPanelV5.class.getName());
	
	public AjaxMenuItemPanelV5(String id,  IModel<T> model) {
		this(id, model, null);
		
	}
	public AjaxMenuItemPanelV5(String id,  IModel<T> model, String icon) {
		super(id, model, null);
	}
	
	public AjaxMenuItemPanelV5(String id, String icon) {
		super(id, icon);
	}

	public AjaxMenuItemPanelV5(String id) {
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
	
	public abstract void onClick(AjaxRequestTarget target) throws Exception;
	
	@Override
	public T getModelObject() {
		return getModel().getObject();
	}
	
	@Override
	public String getLabel() {
		return getClass().getName();
	}
	
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		
	}
	
	@Override
	protected AbstractLink getNewLink(String id) {
	
		AjaxLink<T> link = new AjaxLink<T>(id, getModel()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					AjaxMenuItemPanelV5.this.onClick(target);
				}
				
				catch (RuntimeException e2) {
					logger.error(e2);
					throw e2;
				}
				catch (Exception e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
			
			
			@Override
			public boolean isEnabled() {
				return AjaxMenuItemPanelV5.this.isEnabled();
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
							s ="setTimeout(function () {"+s1+"}, 700);";
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
							String iconCSS = AjaxMenuItemPanelV5.this.getIconCssClass();
							s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<i class=\"far fa-sync fa-spin " + iconCSS + "\"></i> "+getWorkingLabel()+"';";
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
				
				AjaxMenuItemPanelV5.this.updateAjaxAttributes(attributes);
			}
		};
		
		
		link.add(new AttributeModifier("class", new Model<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return  AjaxMenuItemPanelV5.this.isEnabled()? "" :" disabled";
			}
		}));
		
		return link;
	}
}
