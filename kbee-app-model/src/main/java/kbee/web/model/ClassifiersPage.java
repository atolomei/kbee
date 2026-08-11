package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class ClassifiersPage extends ConsolePage<Classifier> {
	private static final long serialVersionUID = 1L;
									
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean is_model_read		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());

	public ClassifiersPage() {
		setPageTitle(getLabel("classifiers"));
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "informationmodel",  ClassifiersPage.class.getName());
	}
	
	public ClassifiersPage(Query query) {
		super(query);
		setPageTitle(getLabel("classifiers"));
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "informationmodel",  ClassifiersPage.class.getName());
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
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<EmailTemplate> panel=new PageContentHeaderPanel<EmailTemplate>();
		panel.setBreadcrumbPanel(new InformationModelBCPanel("bc.classifiers"));
		setPageTitle(getLabel("bc.classifiers"));
		panel.setTitle(getLabel("bc.classifiers"));
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.classifiers", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		//panel.setSearchPanel(getSearchPanel());
		
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Classifier> toolbar = new PageTaskToolbar<Classifier>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		
		setPageContentHeader(panel);
		setLogVisit(true);
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	
	@Override
	public Console<Classifier> newConsole(Query query) {
		return new ClassifiersConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return ClassifiersPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new ClassifiersPage(query);
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_model || is_model_read; 
	}

	@Override
	protected String getTipCategory() {
		return Tip.MODEL;
	}
}