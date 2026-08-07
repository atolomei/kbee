package com.novamens.content.web.workflow.markup.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.console.browser.LauncherSelectorEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.BaseBrowser;


public class LauncherSelector extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;

	private List<IModel<ProcessLauncher>> launchers = null;
	private IModel<ProcessLauncher> selected_launcher_model;
	
	
	public LauncherSelector(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		setOutputMarkupId(true);
		
		if (getLaunchers().size()>0) 
			setSelectedLauncherModel(getLaunchers().get(0));
		
		addChoices();
		
		add(new WicketEventListener<LauncherSelectorEvent<ProcessLauncher>>() {
			@SuppressWarnings("unchecked")
			public boolean handle(com.novamens.event.Event event) {
				return ( (super.handle(event)) &&  
					     (event instanceof LauncherSelectorEvent) && 
						 (LauncherSelector.this.getItemId() != ((LauncherSelectorEvent<ProcessLauncher>) event).getItemId())
					);
			}
			
			
			private static final long serialVersionUID = 1L;
			
			@Override		
			public void onEvent(LauncherSelectorEvent<ProcessLauncher> event) {
				LauncherSelector.this.setSelectedLauncherModel(event.getModel());
			}
		});
		

		
	}
	
	 
	 
	
	public IModel<ProcessLauncher>  getSelectedLauncherModel() {
		return this.selected_launcher_model;
	}
	
	
	
	
	
	
	public void setSelectedLauncherModel(IModel<ProcessLauncher>  lan) {
		this.selected_launcher_model= lan;
	}
	
	
	public List<IModel<ProcessLauncher>> getLaunchers() {
		
		if (this.launchers!=null)
			return this.launchers;
		
		this.launchers = new ArrayList<IModel<ProcessLauncher>>();
		List<ProcessLauncher> list = getDomain().getService(WorkflowDomainService.class).getLaunchers();
		for (ProcessLauncher launcher: list) {
			if (launcher.isEnabled() && launcher.executeable() && launcher.getContentTemplate().getState()==ObjectState.ENABLED) 
					this.launchers.add( new ObjectModel<ProcessLauncher>(launcher));
		}
		return this.launchers;
	}
	
	
	public void onUpdate(AjaxRequestTarget target) {
		getBrowser().refresh(target);
	}
	
	
	
	@Override
	public void onDetach() {
		
		if (this.launchers!=null) {
			for (IModel<ProcessLauncher> model: this.launchers)
				model.detach();
		}
		
		if (this.selected_launcher_model!=null)
			this.selected_launcher_model.detach();
		
		super.onDetach();
	}
	
	/**
	 * 
	 */
	protected void addChoices() {
		
		ExtendedChoiceField<IModel<ProcessLauncher>> ch = new ExtendedChoiceField<IModel<ProcessLauncher>>("source", new PropertyModel<IModel<ProcessLauncher>>(this, "SelectedLauncherModel"), new PropertyModel<List<IModel<ProcessLauncher>>>(this, "launchers")) {
			
			private static final long serialVersionUID = 1L;
			@Override
			public String getIdValue(IModel<ProcessLauncher> value) {
				return value.getObject().getLabel();
			}
			@Override
			public String getDisplayValue(IModel<ProcessLauncher>  value) {
				return value.getObject().getLabel();
			}
			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				fire (new LauncherSelectorEvent<ProcessLauncher>(target, LauncherSelector.this.getSelectedLauncherModel(), getItemId()));
				LauncherSelector.this.onUpdate(target);
			}
		};

		add(ch);
		
	}
	

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}
