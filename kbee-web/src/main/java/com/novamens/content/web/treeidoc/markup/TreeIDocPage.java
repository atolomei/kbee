package com.novamens.content.web.treeidoc.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.content.markup.ContentPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.ErrorNavigationBar;
import kbee.web.page.ApplicationMenuSection;

public class TreeIDocPage extends ContentPage<TreeIDoc> {
			
	private static final long serialVersionUID = 1L;

	public TreeIDocPage() {
	}

	/**
	 * We still dont address the issue when the page is accessed via this constructor and 
	 * the Cabinetis read only and not external.
	 * 
	 * @param parameters
	 */
	public TreeIDocPage(PageParameters parameters) {
		TreeIDoc idoc = getContent(parameters);

		
		// this page is not using hasPermissions
		//
		if (idoc!=null && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(idoc)) {
			setPageTitle(new Model<String>(idoc.getTitle()));
		//	ContentNavigationBar<TreeIDoc> bar = new ContentNavigationBar<TreeIDoc>(new ObjectModel<TreeIDoc>(idoc));
			//bar.setCloseVisible(false);
			//setTopNavigation(bar);
			setModel(new ObjectModel<TreeIDoc>(idoc));
			setMenu(new InvisiblePanel("menu"));
			
			/*
			add(new IDocPanel(new ObjectModel<TreeIDoc>(idoc), false) {
				@Override
				public void onNavigate() {
					IDocPage.this.onNavigate();
				}
			});*/
			add( new InvisiblePanel("editor"));
		}
		else {
			setTopNavigation(new ErrorNavigationBar<IDoc>("navigation"));
			add(new ErrorPanel("editor", (new ContentBaseBC()).getLabel(), new Model<String>("content not found or access denied.")));
		}
	}

	
	public TreeIDocPage(IModel<TreeIDoc> model) {
		this (model, false);
	}


	
	public TreeIDocPage(IModel<TreeIDoc> model, boolean select_preference) {
		super(model);
		
		if (getModel()!=null && getModel().getObject()!=null && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(getModel().getObject())) {
			setPageTitle(new Model<String>( getModel().getObject().getTitle()));
			setMenu(new InvisiblePanel("menu"));
			//setTopNavigation(new ContentNavigationBar<TreeIDoc>(model));
			
			/*
			add(new TreeIDocPanel(model, select_preference) {
				@Override
				public void onNavigate() {
					TreeIDocPage.this.onNavigate();
				}
			});
			*/
			add( new InvisiblePanel("editor"));
			
		}
		else {
			setTopNavigation(new ErrorNavigationBar<IDoc>("navigation"));
			setMenu(new InvisiblePanel("menu"));
			add(new ErrorPanel("editor", (new ContentBaseBC()).getLabel(), new Model<String>("content not found or access denied.")));
		}
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}
	
	@Override
	public void onDetach() {
		if (get("editor")!=null)
			get("editor").detach();
		super.onDetach();
	}
	
	@Override
	protected void refresh(AjaxRequestTarget target) {
		target.add(get("navigation"));
		target.add(get("editor"));
	}

}
