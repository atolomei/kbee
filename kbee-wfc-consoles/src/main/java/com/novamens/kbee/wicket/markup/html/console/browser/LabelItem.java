package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;

public class LabelItem extends ToolbarItem {

	static IModel<String> default_icon 		= new Model<String> ("far fa-tasks fa-fw");
	static IModel<String> default_cont_css 	= new Model<String> ("label-container");
								
	private static final long serialVersionUID = 1L;

	private IModel<String> icon_css = default_icon;
	private IModel<String> label;
	private IModel<String> cont_css = default_cont_css;

	
	public LabelItem(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	
	public LabelItem(BaseBrowser<?> browser, Align align, IModel<String> label) {
		this(browser, align, label, false);
		
	}
	
	public LabelItem(BaseBrowser<?> browser, Align align, boolean isicon) {
		this(browser, align, null, isicon);
	}

	
	public LabelItem(BaseBrowser<?> browser, Align align, IModel<String> label, boolean isicon) {
		super(browser, align, isicon);
		
		setOutputMarkupId(true);
		setLabel(label);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				//event.getRequestTarget().add(LinkButton.this);
			}
		});
		
	}

	public void onInitialize() {
		super.onInitialize();
		add();
	}
	
	public void setLabel(IModel<String> label) {
		this.label = label;
	}
	
	public IModel<String> getLabel() {
		return this.label;
	}
	
	public String getTarget() {
		return null;
	}
	
				
	public void setContainerCss(IModel<String> label) {
		this.cont_css = label;
	}
	
	public IModel<String> getContainerCss() {
		return this.cont_css != null ?  this.cont_css : default_cont_css;
	}
	
	
	public IModel<String> getIconCss() {
		return this.icon_css;
	}

	
	private void add() {

		
		WebMarkupContainer cont = new WebMarkupContainer("label-container");
		cont.add(new AttributeModifier("class", getContainerCss()));
		
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return isIcon() && getIconCss()!=null;
			}
		};
		
		Label la= new Label("label", getLabel()) {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return getLabel() !=null;
			}
		};
		
		cont.add(la);
		cont.add(icon);
		add(cont);
	}

}
