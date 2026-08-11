package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
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
 * Information Model
 *
 * @param <T>
 */
public class DataSetsPage<T extends DataSet> extends ConsolePage<T> {
	private static final long serialVersionUID = 1L;

	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean is_model_read		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	
	public DataSetsPage() {
		this(null);
	}
	
	public DataSetsPage(Query query) {
		super(query);
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "informationmodel",  DataSetsPage.class.getName());
	}
	
	@Override
	public Console<T> newConsole(Query query) {
			return new DataSetsConsole<T>(query) {
				private static final long serialVersionUID = 1L;
				@Override
				public Page getConsolePage(Query query, long index) {
					return DataSetsPage.this.getConsolePage(query, index);
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
				setResponsePage(new DataSetsPage<T>(q));
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
		panel.setBreadcrumbPanel(new InformationModelBCPanel("bc.datasets"));
		setPageTitle(new StringResourceModel("bc.datasets", this, null));
		panel.setTitle(new StringResourceModel("bc.datasets", this, null));
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.datasets", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		// panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<T> toolbar = new PageTaskToolbar<T>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		

		
		
		setPageContentHeader(panel);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_model || is_model_read; 
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new DataSetsPage<T>(query);
	}
	
	@Override
	protected String getTipCategory() {
		return Tip.MODEL;
	}
}
