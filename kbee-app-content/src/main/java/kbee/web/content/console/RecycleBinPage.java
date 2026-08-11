package kbee.web.content.console;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
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

@SuppressWarnings("serial")
public class RecycleBinPage extends ConsolePage<Content> {
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support 				= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_rb						= getDomain().getService(LibraryService.class).readables();

	final boolean is_archive				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.ARCHIVE.getId());
	

	public RecycleBinPage() {
		super(null);
	}
			
	public RecycleBinPage(Query query) {
		super(query);
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}
	
	


	
	
	public void onInitialize() {
		super.onInitialize();
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		panel.setTitle( new StringResourceModel("bc.recyclebin", this, null));
		
		String ret= getContentDao().findSystemParameterValueByKey( "recycle-bin-retention-internal-files-days", "365");
		
		StringResourceModel s=new StringResourceModel("retention-policy", this, null).setParameters(new Object[] {ret});
				
		 //new Model<String>("La Papelera guarda Copias de Trabajo borradas y Contenidos de Biblioteca borrados por <span class=\"highlight\">"
		//		 "1 año</span> antes de eliminarlos"
				 
		panel.setMessage(s);
		
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		setSearchPlaceHolder(new StringResourceModel("search-in-recycle", this, null).getObject());
		setSuggester(false); // Search supports suggester
		setSearchPanel(true); // include Search
		setAdvancedSearch(false); // button advanced search
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
			
			bc.addElement(new BCElement("bc.recyclebin"));
			return bc;
					
			
				} catch (Exception e) {
					//logger.error(e, getSessionUser().getUserName());
					return new InvisiblePanel("breadcrumb");
				}
			}

			
			
			
			
			
			
			
			
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
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
	
	public Console<Content> newConsole(Query query) {
		return new RecycleBinConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return new RecycleBinPage(query);
			}
		};
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new RecycleBinPage(query);
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_support || is_rb; 
	}
	
	@Override
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder ();
		str.append("<p><b>Domain Admin</b> and <b>Support users</b> can access this Page. ");
		str.append("If you are none of them: you need to have access to the <a class=\"btn-link\" href=\"/content\" target=\"_blank\">Library</a></b>. ");
		return str.toString();
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