package com.novamens.content.web.idoc.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.document.IDoc;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.content.markup.ContentPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.ErrorNavigationBar;
import kbee.web.page.ApplicationMenuSection;



/**
 * Stand Alone Page
 */
@Deprecated
@SuppressWarnings("serial")
public class IDocPage extends ContentPage<IDoc> {
	private static final long serialVersionUID = 1L;
	
	public IDocPage() {
	}

	/**
	 * We still dont address the issue when the page is accessed via this constructor and 
	 * the Cabinets read only and not external.
	 * 
	 * @param parameters
	 */
	public IDocPage(PageParameters parameters) {
		
		IDoc idoc = getContent(parameters);
	
		// this page is not using hasPermissions
		//
		if (idoc!=null && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(idoc)) {
			setPageTitle(new Model<String>(idoc.getTitle()));
			//ContentNavigationBar<IDoc> bar = new ContentNavigationBar<IDoc>(new ObjectModel<IDoc>(idoc));
			//bar.setCloseVisible(false);
			//setTopNavigation(bar);
			setMenu(new InvisiblePanel("menu"));
			setModel(new ObjectModel<IDoc>(idoc));
			
			add(new IDocPanel(new ObjectModel<IDoc>(idoc), false) {
				@Override
				public void onNavigate() {
					IDocPage.this.onNavigate();
				}
			});
		}
		else {
			setTopNavigation(new ErrorNavigationBar<IDoc>("navigation"));
			addOrReplace(new ErrorPanel("editor", (new ContentBaseBC()).getLabel(), new Model<String>("content not found or access denied.")));
		}
	}
	
	public IDocPage(IModel<IDoc> model) {
		this (model, false, false);
	}
	
	public IDocPage(IModel<IDoc> model, boolean select_preference) {
		this (model, select_preference, false);
	}
	
	public IDocPage(IModel<IDoc> model, Searcher searcher, long index) {
			this(model, false, true);
	}
	
	public IDocPage(IModel<IDoc> model, boolean select_preference, boolean read_only) {
		super(model, read_only);
		
		if (getModel()!=null && getModel().getObject()!=null && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(getModel().getObject())) {
			
			setPageTitle(new Model<String>( getModel().getObject().getTitle()));
			
			setMenu(new InvisiblePanel("menu"));
			//setTopNavigation(new ContentNavigationBar<IDoc>(model));
			add(new IDocPanel(model, select_preference) {
				@Override
				public void onNavigate() {
					IDocPage.this.onNavigate();
				}
			});
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
	



	@Override
	protected boolean hasLateralMenu() {
		return false;
	}
	
	

	
}