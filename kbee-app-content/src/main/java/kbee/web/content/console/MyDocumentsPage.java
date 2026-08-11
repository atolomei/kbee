package kbee.web.content.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.logging.Logger;
import kbee.web.console.Console;
import kbee.web.console.ContentConsolePage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.ArchiveBC;
import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.MyDocumentsBC;
import kbee.web.nav.MyWorkspaceDropDownBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.search.service.ParametricSearchSuggestionService;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class MyDocumentsPage extends ContentConsolePage<Content> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(MyDocumentsPage.class.getName());
	
	public MyDocumentsPage() {
		super(null);
	}
	
	public MyDocumentsPage(Query query) {
		super(query);
	}
	
	public MyDocumentsPage(PageParameters parameters) {
		super(null);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
			PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
			panel.setTitle(getLabelString("title"));
			panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
			setSearchPlaceHolder(getLabelString("search-in"));
			setSuggester(true); 
			setSearchPanel(true); 
			setAdvancedSearch(true);
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
	 		
			bc.addElement(new MyWorkspaceDropDownBC());
			bc.addElement(new MyDocumentsBC());
			
			/**
			DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
	 		dd.addElement(new ContentBaseBC(), true);
	 		dd.addElement(new SeparatorBC());
			if ((is_archive || is_support) && (!isExpressVersion())) 
				dd.addElement(new ArchiveBC());
			if (!isExpressVersion())
				dd.addElement(new RecycleBinBC());
			bc.addElement(dd);
			*/
			//bc.addElement(new BCElement(getLabel("title")));
			return bc;
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}
	
	@Override
	public String getPageHelpKey() {
		return super.getPageHelpKey()+"-mydocuments";
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}

	public Console<Content> newConsole(Query query) {
		return new MyDocumentsConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return new MyDocumentsPage(query);
			}
		};
	}
	
	@Override
	public boolean hasPermissions() {
		return true;
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
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
	public Page getConsolePage(Query query, long index) {
		return new MyDocumentsPage(query);
	}

	@Override
	protected List<Suggestion> getSuggestions(String pattern) {
		return getDomain().getService(ParametricSearchSuggestionService.class).getSuggestions(pattern); 
	}
	
	@Override
	protected boolean isSuggester() {
		return true;
	}
}