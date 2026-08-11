package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.model.ContentTemplate;
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

/**
 * getProductKey()
 * Information Model 
 */
@SuppressWarnings("serial")
public class ResourceTagsPage extends ConsolePage<ResourceTag> {
	private static final long serialVersionUID = 1L;
									
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model_read 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	final boolean is_model			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	public ResourceTagsPage() {
		setPageTitle(getLabel("resourcetags"));
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "informationmodel",  ResourceTagsPage.class.getName());
	}
	
	public ResourceTagsPage(Query query) {
		super(query);
		setPageTitle(getLabel("resourcetags"));
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "informationmodel",  ResourceTagsPage.class.getName());
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
		PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<Void>();
		panel.setBreadcrumbPanel(new InformationModelBCPanel("bc.resourcetags"));
		setPageTitle(getLabel("bc.resourcetags"));
		panel.setTitle(getLabel("bc.resourcetags"));
		setSearchPlaceHolder(getLabel("search-in").getObject().replace("{0}", getLabel("bc.resourcetags").getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		
		// panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<ResourceTag> toolbar = new PageTaskToolbar<ResourceTag>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		
		setPageContentHeader(panel);
		setLogVisit(true);
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	
	@Override
	public Console<ResourceTag> newConsole(Query query) {
		return new ResourceTagsConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return ResourceTagsPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new ResourceTagsPage(query);
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