package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.rule.ActionRule;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

public class AttributesPage extends ConsolePage<Attribute> {
	
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean is_model_read				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());

	public AttributesPage() {
		setPageTitle(getLabel("Attributes"));
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "informationmodel",  AttributesPage.class.getName());
	}
	
	public AttributesPage(Query query) {
		super(query);
		setPageTitle(getLabel("Attributes"));
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "informationmodel",  AttributesPage.class.getName());
	}
	
	@Override
	public Console<Attribute> newConsole(Query query) {

		return new AttributesConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return AttributesPage.this.getConsolePage(query, index);
			}
		};
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setPageTitle(new StringResourceModel("bc.attributes", this, null));
		PageContentHeaderPanel<ActionRule> panel=new PageContentHeaderPanel<ActionRule>();
		panel.setTitle(new StringResourceModel("bc.attributes", this, null));
		panel.setBreadcrumbPanel(new InformationModelBCPanel("bc.attributes"));
		
		
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.attributes", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Attribute> toolbar = new PageTaskToolbar<Attribute>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
	
		
		
		setPageContentHeader(panel);
	}

	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			/**
			 * 
			 */
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
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
	@Override
	public Page getConsolePage(Query query, long index) {
		return new AttributesPage(query);
	}
	
	@Override
	public Page getConsolePage() {
		return new AttributesPage();
	}
	 
	@Override
	protected String getTipCategory() {
		return Tip.MODEL;
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_model || is_model_read; 
	}
}
