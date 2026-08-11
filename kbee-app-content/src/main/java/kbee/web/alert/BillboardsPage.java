package kbee.web.alert;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.notes.Billboard;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.AlertManagementDropDownBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class BillboardsPage extends ConsolePage<Billboard> {

	private static final long serialVersionUID = 1L;

	public BillboardsPage() {
	}
	
	public BillboardsPage(Query query) {
		super(query);
	}	
	
	@Override
	public Console<Billboard> newConsole(Query query) {
		return new BillboardConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return BillboardsPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.ALERT_SETTINGS;
	}

	@Override
	public boolean hasPermissions() {
		
		final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
		final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
		
		return is_domain_admin || is_root || is_support; 
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new BillboardsPage(query);
	}

	@Override
	public Page getConsolePage() {
		return new BillboardsPage();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<EmailTemplate> panel=new PageContentHeaderPanel<EmailTemplate>();
		MenuBreadCrumbPanel<Billboard>  bc = new MenuBreadCrumbPanel<Billboard>();
		bc.addElement( new HomeBC());
		bc.addElement( new AlertManagementDropDownBC());
		bc.addElement(new BCElement("bc.billboard"));
		panel.setBreadcrumbPanel(bc);

		
		
		setPageTitle(new StringResourceModel("bc.billboard", this, null));
		panel.setTitle(new StringResourceModel("bc.billboard", this, null));
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.billboard", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Billboard> toolbar = new PageTaskToolbar<Billboard>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		
		
		setPageContentHeader(panel);
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=getQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new BillboardsPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	
	
	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}
}
