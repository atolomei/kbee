package kbee.web.domain;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
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
import kbee.web.nav.CommandsBC;
import kbee.web.nav.DomainRecyleBinBC;
import kbee.web.nav.DomainsBC;
import kbee.web.nav.DropDownDomainsBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;

public class DomainsPage extends ConsolePage<Domain> implements FactoryPage {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainsPage.class.getName());

	final boolean is_root 			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin	= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin	= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	final boolean is_api			= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId());
	
	final boolean is_linux = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId());

	public DomainsPage() {
		this(null);
	}
	
	public DomainsPage(Query query) {
		super(query);
		logger.debug(this.getClass().getSimpleName());
		setPageTitle(new Model<String>("domains"));
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DOMAINS;
	}

	
	protected Panel getBreacrumbPanel() {
		MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<Void>();
		
		bc.addElement( new HomeBC());
		DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
		dd.addElement(new DomainsBC(), true);
		dd.addElement(new DomainsBC());
		dd.addElement(new DomainRecyleBinBC());
		//dd.addElement(new CommandsBC());
		bc.addElement(dd);
		bc.addElement(new BCElement("bc.domains"));
		
		return bc;
	}
	public void onInitialize() {
		super.onInitialize();
		
		setPageTitle(new StringResourceModel("bc.domains", this, null));
		
	    PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>();
		panel.setTitle(new StringResourceModel("bc.domains", this, null));
		panel.setBreadcrumbPanel(getBreacrumbPanel());
			

		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.domains", this, null).getObject()));
		
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		panel.setSearchPanel(getSearchPanel());
			
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
				setResponsePage(new DomainsPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	@Override
	public Console<Domain> newConsole(Query query) {
		return new DomainsConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return DomainsPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	public boolean hasPermissions() {
		return isDomainKbee() && (is_domain_admin || is_root || is_service_admin || is_factory_admin || is_api || is_linux);  
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new DomainsPage(query);
	}
	
	@Override
	protected String getTipCategory() {
		return Tip.MODEL;
	}
}
