package kbee.web.scheduler;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;


import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.datamanagement.FactoryDataManagementDropdownBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;

public class SchedulerCronJobsPage extends ConsolePage<AbstractCronJobRequest> implements FactoryPage {


	private static final long serialVersionUID = 1L;

	final boolean is_root 			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin	= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin	= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	final boolean is_api			= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId());
	final boolean is_operations		= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId());

	
	@SuppressWarnings("unused")
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(SchedulerCronJobsPage.class.getName());
	
	public SchedulerCronJobsPage() {
		super(new  CronJobListQuery());
		setPageTitle(getLabel("cronjobs"));
 	}
	
	public SchedulerCronJobsPage(Query query) {
		super(query);
		setPageTitle(getLabel("cronjobs"));
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}

	@Override
	public Console<AbstractCronJobRequest> newConsole(Query query) {
		return new SchedulerCronJobsConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return SchedulerCronJobsPage.this.getConsolePage(query, index);
			}
		};
	}

	
	  protected Panel getSchedulerPanelBreadcrumbPanel() {
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			
			bc.addElement( new HomeBC());
			bc.addElement( new FactoryDataManagementDropdownBC());
			bc.addElement( new SchedulerDropdownBC());
			bc.addElement(new BCElement(new StringResourceModel("cronjobs", SchedulerCronJobsPage.this, null)));
			return bc;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		 	
		    PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>();
			panel.setTitle(new StringResourceModel("cronjobs", this, null));
			panel.setBreadcrumbPanel(getSchedulerPanelBreadcrumbPanel());
			setSearchPlaceHolder(new StringResourceModel("cronjobs", SchedulerCronJobsPage.this, null).getObject());
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
				Query q=new  CronJobListQuery();
				q.getParameters().put("text", event.getText());
				setResponsePage(new SchedulerCronJobsPage(q));
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
		return new SchedulerCronJobsPage(query);
	}

	
	@Override
	protected String getTipCategory() {
		return Tip.FACTORY;
	}
	}	
	