package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.util.KbeeRuntimeException;

public abstract class TwoButtonsMenuItemPanelV5<T> extends AjaxMenuItemPanelV5<T> {
						
	private static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(TwoButtonsMenuItemPanelV5.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	static public final String LEFT_ICON	 = "far fa-chevron-left";
	static public final String RIGHT_ICON 	= "far fa-chevron-right";
	
	
	public TwoButtonsMenuItemPanelV5(String id) {
		super(id);
		setLeftIconCssClass(null);
		setRightIconCssClass(null);

	}

	public  TwoButtonsMenuItemPanelV5(String id, IModel<T> model, final String lefticoncss, final String righticoncss) {
		super(id, model, lefticoncss);
	
		setLeftIconCssClass(lefticoncss);
		setRightIconCssClass(righticoncss);
	}
		
		
	public  TwoButtonsMenuItemPanelV5(String id, final String lefticoncss, final String righticoncss) {
		super(id, lefticoncss);
		setLeftIconCssClass(lefticoncss);
		setRightIconCssClass(righticoncss);
	}
	
	
	public String getLeftIconCssClass() {
		return lefticoncss; 	
	}
	
	public void setLeftIconCssClass(String c) {
		lefticoncss=c; 	
	}
	
	public String getRightIconCssClass() {
		return righticoncss; 	
	}
	
	public void setRightIconCssClass(String c) {
		righticoncss=c; 	
	}
	
	
	String righticoncss;
	String lefticoncss;
	
	
	@SuppressWarnings("serial")
	protected void addComponents() {

		WebMarkupContainer mc = new WebMarkupContainer("tbcontainer");
		mc.setOutputMarkupId(true);
		add(mc);
		
		
		Label mlabel = new Label("label", getLabel());
		mlabel.setEscapeModelStrings(false);
		mc.add(mlabel);
		
		
		{
		AbstractLink link = getNewLinkLeft("left-item-link");
		WebMarkupContainer w = new WebMarkupContainer ("item-icon") {
			public boolean isVisible() {
				return  getLeftIconCssClass()!=null;
			}
		};
		link.add(w);
		w.add(new AttributeModifier("class", new Model<String>() {
				@Override
				public String getObject() {
					return getLeftIconCssClass()!=null?getLeftIconCssClass():"";
				}
			}));
		Label label = new Label("item-label", new Model<String>() {
			public String getObject() {
				return TwoButtonsMenuItemPanelV5.this.getLeftLabel();
			}
		});
		
		if (isEscapeModelString()) 
			label.setEscapeModelStrings(true);
		else
			label.setEscapeModelStrings(false);
		
		
		if (getLeftTitleAttribute()!=null)
			link.add(new AttributeModifier("title",getLeftTitleAttribute()));
		
		link.add(label);
		mc.add(link);
		}

		{											
		AbstractLink right = getNewLinkRight("right-item-link");
		WebMarkupContainer w = new WebMarkupContainer ("item-icon") {
			public boolean isVisible() {
				return  getRightIconCssClass()!=null;
			}
		};
		right.add(w);
		w.add(new AttributeModifier("class", new Model<String>() {
				@Override
				public String getObject() {
					return getRightIconCssClass()!=null?getRightIconCssClass():"";
				}
			}));
		Label label = new Label("item-label", new Model<String>() {
			public String getObject() {
				return TwoButtonsMenuItemPanelV5.this.getRightLabel();
			}
		});
		
		if (isEscapeModelString()) 
			label.setEscapeModelStrings(true);
		else
			label.setEscapeModelStrings(false);
		
		
		if (getRightTitleAttribute()!=null)
			right.add(new AttributeModifier("title", getRightTitleAttribute()));
		
		right.add(label);
		mc.add(right);
		}
		
	}
	
	

	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	 
	}
	

	public void onClick(AjaxRequestTarget target) {
			onLeftClick(target);
	}
	
	public abstract void onRightClick(AjaxRequestTarget target);
	public abstract void onLeftClick(AjaxRequestTarget target);
	
	
	
	protected String getLeftLabel() {		return null;	}
	protected String getRightLabel() {		return null;	}
	
	protected String getLeftTitleAttribute() 	{		return null;	}
	protected String getRightTitleAttribute() {		return null;	}

	
					
	//protected boolean isBorderTop() 				{return false;	}
	//protected boolean isBorderBottom()	 			{return false;	}
	//protected boolean isBorderButtonSeparator() 	{return false;	}
	

	
	
	protected boolean isEscapeModelString() {
		return false;
	}
	
	
	
	protected AbstractLink getNewLinkRight(String id) {
		AjaxLink<T> link = new AjaxLink<T>(id, getModel()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					TwoButtonsMenuItemPanelV5.this.onRightClick(target);
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
				return TwoButtonsMenuItemPanelV5.this.isRightEnabled();
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
							s ="setTimeout(function () {"+s1+"}, 7080);";
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
							String iconCSS = TwoButtonsMenuItemPanelV5.this.getRightIconCssClass();
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
				
				TwoButtonsMenuItemPanelV5.this.updateAjaxAttributes(attributes);
			}
		};
		
		
		link.add(new AttributeModifier("class", new Model<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
	StringBuilder str = new StringBuilder();
				
				str.append(" btn-mini ");
				
				str.append(TwoButtonsMenuItemPanelV5.this.isLeftEnabled()? "" :" disabled ");
				
				//if (isBorderTop())
				//	str.append(" border-top ");
					
				//if (isBorderBottom())
				//	str.append(" border-bottom ");
				

				//if (isBorderButtonSeparator())
				//	str.append(" border-separator ");

				return str.toString();
			}
		}));
		
		return link;
	}
	
	
	
	
	
	
						
	protected boolean isLeftEnabled()	 {		return true;	}
	protected boolean isRightEnabled() {		return true;	}
	

	
	protected AbstractLink getNewLinkLeft(String id) {
	
		AjaxLink<T> link = new AjaxLink<T>(id, getModel()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					TwoButtonsMenuItemPanelV5.this.onLeftClick(target);
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
				return TwoButtonsMenuItemPanelV5.this.isLeftEnabled();
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
							s ="setTimeout(function () {"+s1+"}, 7080);";
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
							String iconCSS = TwoButtonsMenuItemPanelV5.this.getLeftIconCssClass();
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
				
				TwoButtonsMenuItemPanelV5.this.updateAjaxAttributes(attributes);
			}
		};
		
		
		link.add(new AttributeModifier("class", new Model<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {

				StringBuilder str = new StringBuilder();
				
				str.append(" btn-mini ");
				
				str.append(TwoButtonsMenuItemPanelV5.this.isLeftEnabled()? "" :" disabled ");
				
				//if (isBorderTop())
				//	str.append(" border-top ");
					
				//if (isBorderBottom())
				//	str.append(" border-bottom ");
				

				//if (isBorderButtonSeparator())
				//	str.append(" border-separator ");

				return str.toString();
				
			}
		}));
		
		return link;
	}
	
	
	
	
	
	
	
	
	

}
