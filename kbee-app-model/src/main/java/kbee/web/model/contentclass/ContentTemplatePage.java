package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.model.ContentTemplate;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.model.ContentClassesQuery;
import kbee.web.model.InformationModelDropDownBC;
import kbee.web.model.ResourceTagsConsole;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class ContentTemplatePage extends ApplicationPage<ContentTemplate> {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentTemplatePage.class.getName());
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean is_model_read = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	final boolean has_permission = is_root || is_domain_admin || is_model || is_model_read;				
	
	
	boolean is_new=false;
	boolean is_editon=false;
	/**
	 * 
	 * @param parameters
	 */
	public ContentTemplatePage(PageParameters parameters) {
		ContentTemplate template = getTemplate(parameters);
		if (template!=null)
			setModel(new ObjectModel<ContentTemplate>(template));

	}
	
	public ContentTemplatePage(IModel<ContentTemplate> model, boolean edition, boolean isNew) {
		super(model);
		this.is_new=isNew;
		this.is_editon=edition;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		

		addComponents();

		if (getModel()==null) 
			return;

		setTopNavigation(getMainTopbar());   
		setMenu(getMainLaternalMenu());      
		setPageDescription( new Model<String>(getModel().getObject().getName()));
		PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<Void>();
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
		
		bc.addElement(new HomeBC());
		bc.addElement(new SettingsDropDownBC());
		bc.addElement(new InformationModelDropDownBC());
		bc.addElement(new ContentTemplatesDropDownBC());
		bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
		panel.setBreadcrumbPanel(bc);
		
		setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
		panel.setTitle(getModel().getObject().getDisplayName());
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.contentclasses", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		// panel.setSearchPanel(getSearchPanel());
		
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<ContentTemplate> toolbar = new PageTaskToolbar<ContentTemplate>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		setPageContentHeader(panel);
		
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	protected Query newQuery() {
		return new ContentClassesQuery();
	}

	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=newQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new ContentTemplatesPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	
	@Override
	protected boolean hasPermissions() {
		
		if (getModel()==null || getModel().getObject()==null)
			return false;
		
		// Session User's Domain must be the same as  ObjectModel´s Domain
		if (!getDomain().getId().toString().equals(getModel().getObject().getDomain().getId().toString()))
			return false;
		return has_permission;
	}

	private void addComponents() {

		if (getModel()==null) {
			addOrReplace(new ErrorPanel("editor"));
			return;
		}
		
		getPageParameters().set("id", getModel().getObject().getId());
		
		if (!hasPermissions()) {
			addOrReplace(new ErrorPanel("editor"));			
			return;
		}

		setLogVisit(true);
		addOrReplace(new ContentTemplateMainPanel(getModel(), is_new) {
				private static final long serialVersionUID = 1L;
				protected void onClose(AjaxRequestTarget target) {
						setResponsePage(new ContentTemplatesPage());
			}
		});
		
	}

	private ContentTemplate getTemplate(PageParameters parameters) {
		
		ContentTemplate template = null;
		
		try {

		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			template = (ContentTemplate)getContentDao().findModelObjectById(ContentTemplate.class, id.toLong());
			if (template!=null && !template.getDomain().equals(getDomain())) {
				template = null;
			}
		}	
		} catch (Exception e) {
			template=null;
			logger.error(e);
		}
		return template;
	}
}