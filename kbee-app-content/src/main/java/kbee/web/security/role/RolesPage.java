package kbee.web.security.role;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.security.Role;
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
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.RolesBC;
import kbee.web.nav.RulesBC2;
import kbee.web.nav.SecurityBC;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.nav.UsersBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.FacetsQuery;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class RolesPage extends ConsolePage<Role> {
	
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	
	public RolesPage() {
		setPageTitle(new Model<String>("Roles"));
		setTopNavigation(new GlobalNavigationBar<DataSetMember>("navigation", getPageTitle().getObject()) {
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public void onDetach() {
				super.onDetach();
				RolesPage.this.onDetach();
			}
		});
	}
	
	
	

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}
	
	public RolesPage(Query query) {
		super(query);
	}

	@Override
	public Console<Role> newConsole(Query query) {
		return new RolesConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return RolesPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
		
		bc.addElement( new HomeBC());
		
		/**DropdownMenuBC<?>  dd = new DropdownMenuBC<>();
		dd.addElement(new SecurityBC(), true);
		dd.addElement(new UsersBC());
		dd.addElement(new RolesBC());
		if (is_root)
			dd.addElement(new RulesBC2());
		bc.addElement(dd);
		**/
		
		bc.addElement(new SecurityDropDownMenuBC());
		
		bc.addElement(new BCElement("bc.roles"));
		
		setPageTitle(new StringResourceModel("bc.roles", this, null));
		PageContentHeaderPanel<Role> panel=new PageContentHeaderPanel<Role>(getModel());
		
		panel.setTitle(new StringResourceModel("bc.roles", this, null));
		panel.setBreadcrumbPanel(bc);
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.roles", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Role> toolbar = new PageTaskToolbar<Role>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		setPageContentHeader(panel);
	}

	@Override
	protected String getTipCategory() {
		return Tip.SECURITY;
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=new FacetsQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new RolesPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new RolesPage(query);
	}
	
	@Override
	public boolean hasPermissions() {
	
		if (getDomain().getDomainType()==DomainType.EXPRESS)
			return is_root;

		return is_domain_admin || is_root || is_security || is_support; 
	}
}
