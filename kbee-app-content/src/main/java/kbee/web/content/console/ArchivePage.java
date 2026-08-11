package kbee.web.content.console;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.ArchiveBC;
import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.LibraryBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

public class ArchivePage extends ConsolePage<Content> {
			
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_archive				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.ARCHIVE.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ArchivePage.class.getName());

	
	
	public ArchivePage() {
		super(null);
	}
	
	public ArchivePage(Query query) {
		super(query);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		//panel.setMenuPanel(getContentHeaderPanelMenuPanel());
		panel.setTitle(new StringResourceModel("bc.archive", this, null).getObject());
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		setSearchPlaceHolder(new StringResourceModel("bc.archive", this, null).getObject());
		setSuggester(false); 				// Search supports suggester
		setSearchPanel(true); 				// include Search
		setAdvancedSearch(false); 			// button advanced search

		//panel.setSearchPanel(getSearchPanel());
		
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Content> toolbar = new PageTaskToolbar<Content>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		setPageContentHeader(panel);
		setLogVisit(true);
		

		
	}
	
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?> bc=new MenuBreadCrumbPanel<Void>();
			
			
			bc.addElement( new HomeBC());
			
			DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
			
			
			dd.addElement(new ContentBaseBC(), true);
			for (Library library : getLibraries()) {
				if ((is_root || getDomain().getDomainType()!=DomainType.EXPRESS) || (library.isReadOnly())) 
					dd.addElement(new LibraryBC( new ObjectModel<Library>(library)));
			}

			dd.addElement(new SeparatorBC());
			
			if (is_archive || is_support) 
				dd.addElement(new ArchiveBC());
				
			dd.addElement(new RecycleBinBC());
			bc.addElement(dd);

			bc.addElement(new BCElement("bc.archive"));
			return bc;

		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}
	
	
	public Console<Content> newConsole(Query query) {
		return new ArchiveConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return new ArchivePage(query);
			}
		};
	}
	
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
					// getQuery().getParameters().put("text", new TextFilter(event.getText()));
					getQuery().getParameters().put("text", event.getText());
					getQuery().getParameters().put("sort", "relevance");
					setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}

	 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new ArchivePage(query);
	}
	
	@Override
	public Page getConsolePage() {
		return new ArchivePage();
	}

	
	@Override
	public boolean hasPermissions() {
		return this.is_domain_admin || this.is_root || this.is_archive || this.is_support; 
	}

	protected List<Library> getLibraries() {
		List<Library> cabinets = new ArrayList<Library>();
		for (Library cabinet : getRepository(Library.class).findAll()) {
 			if (cabinet.isReadable()) 
 				cabinets.add(cabinet);
		};
		return cabinets;
	}	
}
