package kbee.web.content.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ContentConsolePage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.ArchiveBC;
import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.LibraryBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.search.service.ParametricSearchSuggestionService;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class ContentBasePage extends ContentConsolePage<Content> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentBasePage.class.getName());

	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	final boolean is_archive				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.ARCHIVE.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_rb						= getDomain().getService(LibraryService.class).readables();

	private IModel<Library> librarymodel;
	
	public ContentBasePage() {
		super(null);
		setLibrary(getChosenLibrary());
	}
	
	public ContentBasePage(IModel<Library> model) {
		super(null);
		setLibrary(model);
	}
	
	public ContentBasePage(IModel<Library> model, Query query) {
		super(query);
		setLibrary(model);
	}
	
	public ContentBasePage(PageParameters parameters) {
		super(null);
		Library library = getLibrary(parameters);
		if (library!=null) {
			setLibrary(new ObjectModel<Library>(library));
		}
		else {
			setResponsePage(new ApplicationErrorPage<Content>(new Model<String>("library not found")));
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
			PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
			panel.setTitle(getLibrary()!=null?getLibrary().getDisplayName():"");
			panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject() + " " + getLibrary()!=null?getLibrary().getDisplayName():"");
			setSuggester(true); // Search supports suggester
			setSearchPanel(true); // include Search
			setAdvancedSearch(true); // button advanced search
			
			// panel.setSearchPanel(getSearchPanel());
			
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			r_list.add(getSearchPanel("panel"));
			PageTaskToolbar<Content> toolbar = new PageTaskToolbar<Content>("toolbar", getModel(), l_list, r_list);
			panel.setToolbarPanel(toolbar);
			setPageContentHeader(panel);
			
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage(new ApplicationErrorPage<Content>(e));
		}
	}
	
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?> bc=new MenuBreadCrumbPanel<Void>();
			bc.addElement( new HomeBC());
	 		DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
			dd.addElement(new ContentBaseBC(), true);
			for (Library library : getLibraries()) {
				if (is_root || (!isExpressVersion()) || (library.isReadOnly())) 
					dd.addElement(new LibraryBC( new ObjectModel<Library>(library)));
			}
			dd.addElement(new SeparatorBC());
			if ((is_archive || is_support) && (!isExpressVersion())) 
				dd.addElement(new ArchiveBC());
			if (!isExpressVersion())
				dd.addElement(new RecycleBinBC());
			bc.addElement(dd);
			bc.addElement(new BCElement(new Model<String>(getLibrary().getDisplayName())));
			return bc;
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.librarymodel!=null)
			this.librarymodel.detach();
	}
	
	@Override
	public String getPageHelpKey() {
		return super.getPageHelpKey()+"-"+ (getLibrary() != null ? getLibrary().getKey() : "");
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}

	public Console<Content> newConsole(Query query) {
		
		if (getLibrary()==null) {
			setLibrary(getChosenLibrary());
		}
		
		String name = getLibrary().getKey().toLowerCase();
		
		return new ContentBaseConsole(name, getLibraryModel(), query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return new ContentBasePage(getLibraryModel(), query);
			}
		};
	}

	public Library getLibrary() {
		return getLibraryModel()==null ? null : getLibraryModel().getObject();
	}
	
	public Library getChosenLibrary() {
		String preference = getUserPreference("library");
		try {
			List<Library> libraries = getDomain().getService(LibraryService.class).getLibraries();
			if (preference!=null) {
				for (Library library : libraries) {
					if (preference.equals(String.valueOf(library.getId())) && library.isReadable()) {
						return library;
					}
				}
			} 
			for (Library library : libraries) {
				if (library.isReadable()) {
					if (isExpressVersion()) {
						if (library.isReadOnly())
							return library;
					}
					else
						return library;
				}
			}
			return null;
		}
		catch (Throwable e) {
			logger.error(e);
			return null;
		}
	}
	
	public void setLibrary(IModel<Library> model) {
		this.librarymodel = model;
		setUserPreference("library", String.valueOf(model.getObject().getId()));
	}
	
	public void setLibrary(Library library) {
		if (library!=null)
			setLibrary(new ObjectModel<Library>(library));
	}

	public IModel<Library> getLibraryModel() {
		return librarymodel;
	}
	
	@Override
	public boolean hasPermissions() {

		if (isExpressVersion())
			return true;

		if (is_domain_admin || is_root || is_support)
			return true;
		
		
		if (getDomain().getService(LibraryService.class).readables())
			return true;
		
		return false;
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
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
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder ();
		str.append("<p><b>Domain Admin</b> and <b>Support users</b> can access this Page. ");
		str.append("If you are none of them: <br />If Knowledge Base Library is enabled, you need to have rights to the Library Knowledge Base in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.<br /> ");
		str.append("or if External Library is enabled, You need to have rights to the Library External in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.</p>");
		return str.toString();
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		ContentBaseConsole console = (ContentBaseConsole)get("console");
		IModel<Library> librarymodel = console!=null ? console.getLibraryModel() : null;
		if (librarymodel == null) 
			librarymodel = getLibraryModel();
		return new ContentBasePage(librarymodel, query);
	}

	@Override
	protected List<Suggestion> getSuggestions(String pattern) {
		return getDomain().getService(ParametricSearchSuggestionService.class).getSuggestions(pattern); 
	}
	
	@Override
	protected boolean isSuggester() {
		return true;
	}
	
	protected List<Library> getLibraries() {
		List<Library> cabinets = new ArrayList<Library>();
		try {
			for (Library cabinet : getRepository(Library.class).findAll()) {
				if (cabinet.isReadable()) 
					cabinets.add(cabinet);
			};
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return cabinets;
	}
	
	protected Library getLibrary(PageParameters parameters) {
		try {
			if (parameters.get("library")!=null && !"".equals(parameters.get("library").toString())) {
				String key = parameters.get("library").toString().toLowerCase();
				for (Library library : getLibraries()) {
					if (library.isReadable() && library.getKey()!=null && key.equals(library.getKey().toLowerCase())) 
						return library;
				};
			}	
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
}