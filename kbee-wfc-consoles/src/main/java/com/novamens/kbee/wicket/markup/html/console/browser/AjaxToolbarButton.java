package com.novamens.kbee.wicket.markup.html.console.browser;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;

import kbee.web.console.BaseBrowser;


@SuppressWarnings("serial")
public abstract class AjaxToolbarButton extends AbstractToolbarButton {

	@SuppressWarnings("unused")
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AjaxToolbarButton.class.getName());

	private static final long serialVersionUID = 1L;

	private boolean is_spin = true;
	private boolean is_send_on_complete = true;
	
	
	public AjaxToolbarButton(BaseBrowser<?> browser, Align align) {
				this(browser,  align, true, true);
	}
	
	public AjaxToolbarButton(BaseBrowser<?> browser, Align align, boolean isspin) {
		this(browser,  align, true, true);
}

	public AjaxToolbarButton(BaseBrowser<?> browser, Align align, boolean spin, boolean send_on_complete) {
		super(browser, align);
		this.is_spin=spin;
		this.is_send_on_complete=send_on_complete;
		setOutputMarkupId(true);
	}
	
	public void setSpin(boolean b) {
		this.is_spin=b;
	}
	
	public boolean isSpin() {
		return this.is_spin;
	}

	public boolean isSendOnComplete() {
		return this.is_send_on_complete;
	}
	
	 
	@Override
	protected void addLink() {
		
		WorkingIndicatorAjaxLinkV5<Void> link = new WorkingIndicatorAjaxLinkV5<Void> ("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AjaxToolbarButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return AjaxToolbarButton.this.isEnabled();
			}
		};
		
		if (getLinkCss()!=null)
			link.add(new AttributeModifier("class", getLinkCss()));
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");		

 		icon.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return getIcon();
				}
		}));
										
 		Label label = new Label("label", new Model<String>() {
 			public String getObject() {
 				return getLabelStr();
 			}
 		}) {
 			@Override
 			public boolean isVisible() {
 				return getLabelStr()!=null;
 			}
 		};

		link.add(icon);
		link.add(label);
		add(link);
		
		link.add(new AttributeModifier("title", new Model<String>() {
			@Override
			public String getObject() {
				if (getAnchorTitle()==null)
						return "";
				return getAnchorTitle();
			}
		}));
	}

	public abstract void onClick(AjaxRequestTarget target);
	
}
