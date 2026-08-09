package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.console.Browser;

@SuppressWarnings("serial")
public abstract class ButtonPanelToolbarItem extends ToolbarItem {
	private static final long serialVersionUID = 1L;
	
	private IModel<String> title;
	boolean isInitialized = false;
	
	public ButtonPanelToolbarItem(Browser<?> browser, Align align) {
		this( browser, align, false);
	}
	
	public ButtonPanelToolbarItem(Browser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
	}
	
	@Override
	public void addListeners() {
		super.addListeners();

		add(new WicketEventListener<AfterUploadEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(AfterUploadEvent event) {
				onClick(event.getRequestTarget());
			}
		});
	}

		
	public abstract Panel getPanel(String id);
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		super.setOutputMarkupId(true);
		AjaxLink<Void> li = new AjaxLink<Void>("button") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ButtonPanelToolbarItem.this.onClick(target);
			}
		};
		add(li);
		
		if(getButtonCssClass()!=null)
			li.add( new AttributeModifier("class", getButtonCssClass()));
		
		
		if (getTitle()==null)
			setTitle(new Model<String>(""));
		
		Label t=new Label("title", getTitle());
		li.add(t);

		add(new InvisiblePanel("panel"));
		
	}

	protected String getButtonCssClass() {
		return "btn-md btn btn-primary";
	}

	protected void onClick(AjaxRequestTarget target) {
		
		if (get("panel") instanceof InvisiblePanel) { 
			Panel panel=getPanel("panel");
			panel.setVisible(true);
			addOrReplace(panel);
			target.add(this);
			return;
		}
		
		get("panel").setVisible(!get("panel").isVisible());
		target.add(this);
	}
	public void setTitle (IModel<String> title) {
		this.title=title;
	}
	
	public IModel<String> getTitle() {
		return this.title;
	}
}
