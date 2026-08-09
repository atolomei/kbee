package kbee.web.command.panel;



import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.command.Command;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.command.CommandListQuery;
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
import kbee.web.nav.DomainsBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;


public class CommandsPage extends ConsolePage<Command> implements FactoryPage {
			
	private static final long serialVersionUID = 1L;

	final boolean is_root 			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin	= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin	= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	final boolean is_api			= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId());
	final boolean is_operations		= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId());

	
	@SuppressWarnings("unused")
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(CommandsPage.class.getName());
	
	public CommandsPage() {
		super(new CommandListQuery());
		setPageTitle(getLabel("Commands"));
 	}
	
	public CommandsPage(Query query) {
		super(query);
		setPageTitle(getLabel("Commands"));
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}


	
	
	
	@Override
	public Console<Command> newConsole(Query query) {
		return new kbee.web.command.panel.CommandsConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return CommandsPage.this.getConsolePage(query, index);
			}
			@Override
			protected boolean hasIcon(IModel<Command> model) {
				return false;
			}
			@Override
			protected String getIcon(IModel<Command> model) {
				return "";
			}
		};
	}

	
	protected Panel getBreadcrumbPanel() {
	    MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<Void>();
		bc.addElement( new HomeBC());
		DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
		dd.addElement(new DomainsBC(), true);
		dd.addElement(new DomainsBC());
		dd.addElement(new CommandsBC());
		bc.addElement(dd);
		bc.addElement(new BCElement("bc.commands"));
		return bc;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		 
		   PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>();
			panel.setTitle(new StringResourceModel("bc.commands", this, null));
			panel.setBreadcrumbPanel(getBreadcrumbPanel());
			setSearchPlaceHolder(new StringResourceModel("bc.commands", CommandsPage.this, null).getObject());
			setSearchPanel(true);
			setAdvancedSearch(false);
			setSuggester(false);
			panel.setSearchPanel(getSearchPanel());
			setPageContentHeader(panel);
		 
		 
	}

	
	@SuppressWarnings("serial")
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=new CommandListQuery();
				q.getParameters().put("text", event.getText());
				setResponsePage(new CommandsPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}	
	
	
	
	
	@Override
	public boolean hasPermissions() {
		return isDomainKbee() && (is_domain_admin || is_root || is_service_admin || is_factory_admin || is_api || is_operations);  
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new CommandsPage(query);
	}

	
	@Override
	protected String getTipCategory() {
		return Tip.FACTORY;
	}
	
	

}
