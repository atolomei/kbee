package kbee.web.emailtemplate;



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.dom.DomainType;
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
import kbee.web.nav.HomeBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

public class EmailTemplatesPage extends ConsolePage<EmailTemplate> {
			
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_settings				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SETTINGS.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
				
	
	public EmailTemplatesPage() {
		this(new PageParameters());
	}
	
	public EmailTemplatesPage(PageParameters parameters) {
	}
	
	public EmailTemplatesPage(Query query) {
		super(query);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<EmailTemplate> panel=new PageContentHeaderPanel<EmailTemplate>();
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		
		bc.addElement( new HomeBC());
		
		bc.addElement( new SettingsDropDownBC());
		bc.addElement(new BCElement("bc.emailtemplates"));
		panel.setBreadcrumbPanel(bc);
		setPageTitle(new StringResourceModel("bc.emailtemplates", this, null));
		panel.setTitle(new StringResourceModel("bc.emailtemplates", this, null));
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.emailtemplates", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		
		//panel.setSearchPanel(getSearchPanel());
		
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<EmailTemplate> toolbar = new PageTaskToolbar<EmailTemplate>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		
		setPageContentHeader(panel);
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	@Override
	public Console<EmailTemplate> newConsole(Query query) {
		return new EmailTemplatesConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return EmailTemplatesPage.this.getConsolePage(query, index);
			}
		};
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
				setResponsePage(new EmailTemplatesPage(q));
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
 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new EmailTemplatesPage(query);
	}
	 
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root  || is_support || is_settings;  
	}
}
