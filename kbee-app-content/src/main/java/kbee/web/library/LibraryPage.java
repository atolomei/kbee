package kbee.web.library;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.library.Library;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.security.role.RolePage;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class LibraryPage extends ApplicationPage<Library> {
				
	private static final long serialVersionUID = -1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LibraryPage.class.getName());

	private boolean isNew;
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean has_permission = is_root || is_domain_admin || is_model;				
	
	/**
	 * @param parameters
	 */
	public LibraryPage(PageParameters parameters) {
		Library cabinet = getLibrary(parameters);
		if (cabinet != null) 
			setModel(new ObjectModel<Library>(cabinet));
	}
	
	
	public LibraryPage(IModel<Library> model) {
		super(model);
		this.isNew=false;
	}
	
	public LibraryPage(IModel<Library> model, boolean isnew) {
		super(model);
		this.isNew=isnew;
	}
	
	
	public void onInitialize()  {
		super.onInitialize();

		try {
			
		
				if (getModel()==null || getModel().getObject()==null) 
					throw new IllegalArgumentException("Model can not be null");
				
				if (hasPermissions()) {
				
					PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<Void>();
					
					MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
					bc.addElement(new SettingsDropDownBC());
					bc.addElement(new LibraryDropDownBC());
					bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
					panel.setBreadcrumbPanel(bc);
					
					setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
					panel.setTitle(getModel().getObject().getDisplayName());
					setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.libraries", this, null).getObject()));
					panel.setSearchPanel(getSearchPanel());
					
					
					
					List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
					List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
					r_list.add(getSearchPanel("panel"));
					PageTaskToolbar<Library> toolbar = new PageTaskToolbar<Library>("toolbar", getModel(), l_list, r_list);
					panel.setToolbarPanel(toolbar);
					
					setPageContentHeader(panel);
					
					setTopNavigation(getMainTopbar());
					setMenu(getMainLaternalMenu());
		
					setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));
					
					LibraryMainPanel editor = new LibraryMainPanel(getModel(), isNew()) {
						@Override
						protected void onClose(AjaxRequestTarget target) {
							setResponsePage(new com.novamens.content.web.security.markup.RulesPage());
						}
					};
					
					editor.setEditionEnabled(isNew());
					add(editor);
					getPageParameters().set("id", getModel().getObject().getId());
				}
				else {
					addOrReplace(new InvisiblePanel("editor"));
				}
			
		} catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("editor", e));
		}
	}

	
	public boolean isNew() {
		return this.isNew;
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	
	@Override
	protected boolean hasPermissions() {
		
		if (getModel()==null || getModel().getObject()==null)
			return false;
		
		if (!getDomain().getId().toString().equals(getModel().getObject().getDomain().getId().toString()))
			return false;

		return has_permission;
	}
	
	private Library getLibrary(PageParameters parameters) {
		try {
		if (parameters.get("id")!=null && !"".equals(parameters.get("id").toString())) {
			String id = parameters.get("id").toString();
			Library library = getRepository(Library.class).findById(Long.valueOf(id));
			return library;
		}	
		return null;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	protected String getPageType() { 
		return "det";
	}
	
	protected String getStatsPageTitle() { 
		return getModel().getObject().getDisplayName(); 
	} 
}