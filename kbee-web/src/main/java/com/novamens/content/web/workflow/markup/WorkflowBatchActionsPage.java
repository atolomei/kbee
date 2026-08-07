package com.novamens.content.web.workflow.markup;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.nav.WorkspaceBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class WorkflowBatchActionsPage extends ApplicationPage<Content> {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<Content>> selection;
	
	public WorkflowBatchActionsPage(List<IModel<Content>> selection) {
		setSelection(selection);
	}
	
    
	@Override 
	public void onInitialize() {
		super.onInitialize();
		
		setPageTitle(new StringResourceModel("title", this, null));
		setPageDescription(getPageTitle());
		
		setTopNavigation(getMainTopbar()); 	
		setMenu(getMainLaternalMenu()); 	

		addComponents(); 
	}

	@Override
	public void onDetach() {
		for (IModel<Content> model : getSelection()) 
			model.detach();
		super.onDetach();
	}
	
	protected Page getPage(IModel<Content> model) {
		return null;
	}
	
	private void setSelection(List<IModel<Content>> selection) {
		this.selection = selection;
	}

	private List<IModel<Content>> getSelection() {
		return selection;
	}
	
	private void addComponents() {
		
		PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>(null);
		panel.setTitle(new StringResourceModel("bc.batchactions", this, null));
		MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
		bc.addElement(new TasksDropDownMenuBC());
		bc.addElement(new WorkspaceBC());
		bc.addElement(new BCElement(new StringResourceModel("title", this, null)));
		panel.setBreadcrumbPanel(bc);
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);
		
		add(new WorkflowBatchActionsPanel("editor", getSelection()) {
			public void onReturn() {
				setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.WorkspacePage));
			}
			protected Page getPage(IModel<Content> model) {
				return WorkflowBatchActionsPage.this.getPage(model);
			}
		});
	}
}

