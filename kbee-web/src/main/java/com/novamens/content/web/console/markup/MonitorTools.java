package com.novamens.content.web.console.markup;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.console.BaseBrowser;

import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

@SuppressWarnings("serial")
public class MonitorTools extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;
	
	private BaseBrowser<Content> browser;

	public MonitorTools(BaseBrowser<Content> browser, Align align) {
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
						// getPage().setResponsePage( new BatchClassifyPage(getSelection()));
					}
					@Override
					public String getLabel() {
						return MonitorTools.this.getLabel("tools.assign").getObject();
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
						// getPage().setResponsePage(new BatchActionsPage(getSelection()));
					}
					@Override
					public String getLabel() {
						return MonitorTools.this.getLabel("tools.assigntome").getObject();
					}
					@Override
					public boolean isEnabled() {
						return !getSelection().isEmpty();
					}
				};
			}
		});
		
		add(menu);
		
		add(new WicketEventListener<SelectionEvent>() {
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(MonitorTools.this);
			}
		});
	}
	
	public List<IModel<Content>> getSelection() {
		return this.browser.getSelection();
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
