package com.novamens.content.web.console.markup;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.user.UserService;
import com.novamens.content.web.content.classify.markup.BatchClassifyPage;
import com.novamens.content.web.workflow.markup.WorkflowBatchActionsPage;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class WorkspaceTools extends ToolbarItem {
	
	private static final long serialVersionUID = 1L;
	
	private BaseBrowser<Content> browser;

	public WorkspaceTools(BaseBrowser<Content> browser, Align align) {
		super(browser, align);
		
		setOutputMarkupId(true);
		
		this.browser = browser;
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						getPage().setResponsePage( new BatchClassifyPage(getSelection()));
					}
					@Override
					public String getLabel() {
						return WorkspaceTools.this.getLabel("tools.classify").getObject();
					}
					@Override
					public boolean isEnabled() {
						return !getSelection().isEmpty();
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						getPage().setResponsePage( new WorkflowBatchActionsPage(getSelection()));
					}
					@Override
					public String getLabel() {
						return WorkspaceTools.this.getLabel("tools.actions").getObject();
					}
					@Override
					public boolean isEnabled() {
						return !getSelection().isEmpty();
					}
				};
			}
		});
		
		
//		 
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						// getPage().setResponsePage( new BatchActionsPage(getSelection()));
					}
					@Override
					public String getLabel() {
						return WorkspaceTools.this.getLabel("tools.delete").getObject();
					}
					@Override
					public boolean isEnabled() {
						return !getSelection().isEmpty();
					}
					
					@Override
					public String getCssClass() {
						return "label-danger";
					}
				};
			}
		});
		
		add(menu);

		
		add(new WicketEventListener<SelectionEvent>() {
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(WorkspaceTools.this);
			}
		});
	}
	
	public List<IModel<Content>> getSelection() {
		return browser.getSelection();
	}
	

	@Override
	public boolean isEnabled() {
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	protected boolean isCommon(String procedureId, String taskId) {
		boolean common = !getSelection().isEmpty();
		for (IModel<Content> model : getSelection()) {
			WorkflowService workflowservice = model.getObject().getService(WorkflowService.class);
			if (workflowservice.getTask()!=null) {
				KbeeTask kbeetask = (KbeeTask)workflowservice.getTask();
				if (!kbeetask.getId().equals(taskId) || !String.valueOf(kbeetask.getProcedure().getId()).equals(procedureId)) {
					return false;
				}
			}
		}
		return common;
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
}
