package kbee.web.notes;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ObjectId;
import com.novamens.content.notes.Billboard;
import com.novamens.content.web.nav.markup.CloseBehavior;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.TextFilter;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.alert.BillboardsPage;
import kbee.web.error.ErrorPanel;
import kbee.web.model.DataSetsConsole;
import kbee.web.nav.AlertManagementBC;
import kbee.web.nav.AlertManagementDropDownBC;
import kbee.web.nav.BillboardsBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.nav.TabNavigationBar;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;


/**
 * Billboards
 */
public class BillboardPage extends ConsoleObjectPage<Billboard> {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BillboardPage.class.getName());
	
	private boolean isnew = false;
	private boolean readonly = false;
	
	public BillboardPage() {
		this(new PageParameters());
	}
	
	
	public BillboardPage(PageParameters parameters) {
		
		Billboard note = getWorkNote(parameters);
		
		if (note!=null) {
			setModel(new ObjectModel<Billboard>(note));
			StringValue s =parameters.get("isnew");
			this.isnew = (s!=null) && (s.toOptionalString()!=null) && (s.toOptionalString().equals("yes"));
			
			StringValue r =parameters.get("readonly");
			this.readonly = (r!=null) && (r.toOptionalString()!=null) && (r.toOptionalString().equals("yes")); 
		}
	}
	
	public BillboardPage(IModel<Billboard> model) {
		setModel(model);
	}

	
	public BillboardPage(IModel<Billboard> model, boolean isnew) {
		setModel(model);
		this.isnew=isnew;
	}

	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	

	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=newQuery();
				q.getParameters().put("text",new TextFilter(event.getText()));
				q.getParameters().put("sort", "relevance");
				setResponsePage(new BillboardsPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	protected Query newQuery() {
		return new BillboardsQuery(getQueryIndex());
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//setTopNavigation(new TabNavigationBar<Billboard>("navigation"));
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());
		
		try {
		
		if (getModel()==null || getModel().getObject()==null) 
			throw new IllegalArgumentException("Model can not be null");
		
		
		setPageTitle(new StringResourceModel(getModel().getObject().isBillboard() ? "billboard": "alert", BillboardPage.this, null));
		setPageDescription(getPageTitle());
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		panel.setTitle(getModel().getObject().getDisplayName());
		
		setSuggester(false); // Search supports suggester
		setSearchPanel(false); // include Search
		setAdvancedSearch(false); // button advanced search
		
		setSearchPlaceHolder((new StringResourceModel("search-in", this, null).setParameters(new StringResourceModel("bc.billboard", BillboardPage.this, null).getObject())).getObject());
		
		panel.setSearchPanel(getSearchPanel());
		setPageContentHeader(panel);
		
			MenuBreadCrumbPanel<Billboard> bc = new MenuBreadCrumbPanel<Billboard>();
			bc.addElement( new AlertManagementDropDownBC());
			bc.addElement(new BillboardsBC());
			
			panel.setBreadcrumbPanel(bc);
			
			getPageParameters().set("id", getModel().getObject().getId());
		
			bc.addElement(new BCElement( new Model<String>(getModel().getObject().getDisplayName())));
			
			 IModel<Billboard> m=getModel();
			 
			 WorkNoteEditor ed=new WorkNoteEditor("editor", m, this.isnew) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					target.appendJavaScript((new CloseBehavior()).getScript());
					//setResponsePage(new WorkNotesPage());
				}
				@Override
				public void onCancel(AjaxRequestTarget target) {
					target.appendJavaScript("window.close();");
					//setResponsePage(new BillboardsPage());
				}
			};
			 
			  ed.setReadOnly(this.readonly);
			 add(ed);
			 
		} catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("editor", e));
		}
		
	}

			
	protected Billboard getWorkNote(PageParameters parameters) {
		
		if (parameters==null)
			return null;
		try {
		Billboard note = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			note = (Billboard)getContentDao().findWorkNote(id.toLong());
			if (note!=null && !note.getDomain().equals(getDomain())) {
				note = null;
			}
		}	
		return note;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	
	protected String getPageType()     {return "det";} 													 // con | det  
	protected String getContentTitle() {return getModel().getObject().getDisplayName();} 				// content title or user title, ...

	protected String getStatsPageTitle() {return getModel().getObject().getDisplayName();} 			// for console page, it is the name of the console 
	protected Long getStatsPageId() {return new Long(0);} 								                // for console page, it is the name of the console
													
	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content
}
