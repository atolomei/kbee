package kbee.web.multidimensional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.multidimensional.FacetService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.KbeeFacetWrapper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.model.object.WrapperModel;
import kbee.web.nav.FacetsBC;

import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class FacetPage extends ApplicationPage<Facet> {
	private static final long serialVersionUID = -1L;

	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean has_permission = is_root || is_domain_admin || is_model || is_support;
	
	private IModel<Cursor> cursor_model;
	
	public FacetPage(PageParameters parameters) {
		Facet facet = getFacet(parameters);
		if (facet != null) {
			if (facet instanceof Identifiable && ((Identifiable)facet).getId()!=null) {
				setModel(new ObjectModel<Facet>(facet));
			}
			else {
				setModel(new WrapperModel(facet));
			}
		}
		else 
			add(new ErrorPanel("editor", "facet not found!", ""));
	}

	public FacetPage(IModel<Facet> model) {
		super(model);
		
	}
	
	
	
	public void onDetach() {
		super.onDetach();
		if (cursor_model!=null)
			cursor_model.detach();
	}	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		if (getModel()!=null && hasPermissions()) {
			setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));

			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());  
			
			FacetMainPanel editor = new FacetMainPanel(getModel()) {
				@Override
				protected void onClose(AjaxRequestTarget target) {
					setResponsePage(new FacetsPage());
				}
			};
			add(editor);
			getPageParameters().set("name", getModel().getObject().getName());
			
			PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
			panel.setTitle(getModel().getObject().getDisplayName());
			
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
			bc.addElement(new SettingsDropDownBC());
			bc.addElement(new FacetsBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
			panel.setBreadcrumbPanel(bc);
			
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.facets", this, null).getObject()));
			setSearchPanel(false);
			
			setAdvancedSearch(false);
			setSuggester(false);
			panel.setSearchPanel(getSearchPanel());
			setPageContentHeader(panel);
		}
		else {
			add(new ErrorPanel("editor", "authorization error", ""));
		}
	}

	
	@Override
	protected boolean hasPermissions() {
		
			
		if (getDomain()==null || getModel()==null || getModel().getObject()==null)
			return false;

		 if (getModel().getObject() instanceof KbeeFacetWrapper) {
			 String a=((KbeeFacetWrapper) getModel().getObject()).getDomain().getId().toString();
			 String b=getDomain().getId().toString();
			 if (!a.equals(b))
				 return false;
		 }
		return has_permission;
	}

	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	private Facet getFacet(PageParameters parameters) {
		if (parameters.get("name")!=null && !"".equals(parameters.get("name").toString())) {
			String facetname = parameters.get("name").toString();
			for (Facet facet : getDomain().getService(FacetService.class).getFacets(getIndex())) {
				if (facet.getName().equals(facetname)) {
					return facet;
				}
			}
		}	
		return null;
	}
	
	private Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}
