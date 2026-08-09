package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.workflow.ProcessLauncher;

import kbee.web.console.BaseBrowser;

public class LauncherButton extends AddButtonToolbarItem {
	
	private static final long serialVersionUID = 1L;
	
	private IModel<ProcessLauncher> model;
	private String suffix;
	
	private String key;
	
	public LauncherButton(IModel<ProcessLauncher> model, BaseBrowser<?> browser, Align align) {
			this(model, browser, align, null, "one-for-each");
	}
	
	
	public LauncherButton(IModel<ProcessLauncher> model, BaseBrowser<?> browser, Align align, String suffix, String key) {
		super(browser, align);
		this.model=model;
		this.suffix=suffix;
		this.key=key;
		setLabel(new Model<String>(model.getObject().getDisplayName() + (getSuffix()!=null?  (" "+ getSuffix()+" ")  :"")));
		setOutputMarkupId(true);
	}
	

	public String getKey() {
		return this.key;
	}
	
	public 	IModel<ProcessLauncher> getModel() {
		return model;
	}


	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public String getSuffix() {
		return suffix;
	}
	

	@Override
	public void onClick(AjaxRequestTarget target) {
		fire (new LauncherSelectorEvent<ProcessLauncher>(target, getModel(), getItemId(), getSuffix(), getKey()));
	}
	
	
	
	
	
}
