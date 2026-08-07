package com.novamens.content.web.content.classify.markup;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.nav.WorkspaceBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

/**
 * This page allows files to Edit in Bulk  
 */
@SuppressWarnings("serial")
@Deprecated
public class BatchClassifyPage extends ApplicationPage<Content> {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<Content>> selection;

	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");

	public BatchClassifyPage(PageParameters parameters) {
		setSelection(new ArrayList<IModel<Content>>());
	}
	
	public BatchClassifyPage(List<IModel<Content>> selection) {
		setSelection(selection);
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(BL));
	}
	
	@Override 
	public void onInitialize() {
		super.onInitialize();
		addComponents();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<Content> model : getSelection()) 
			model.detach();
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
		panel.setTitle(new StringResourceModel("bc.batchclassify", this, null));
		MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
		bc.addElement(new TasksDropDownMenuBC());
		bc.addElement(new WorkspaceBC());
		bc.addElement(new BCElement(new StringResourceModel("bc.batchclassify", this, null)));
		panel.setBreadcrumbPanel(bc);
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);

		setPageTitle(new StringResourceModel("title", this, null));
		setPageDescription(getPageTitle());
		
		setTopNavigation(getMainTopbar()); 	
		setMenu(getMainLaternalMenu()); 	


		add(new BatchClassifyEditor("editor", getSelection()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				//setResponsePage(new WorkspacePage());
			}
			@Override
			public void onReturn() {
				//setResponsePage(new WorkspacePage());
			}
			@Override
			protected Page getPage(IModel<Content> model) {
				return BatchClassifyPage.this.getPage(model);
			}
		});
	}
}

