package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.TextFilter;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class ClassifierModelPage extends ApplicationPage<Classifier> {
	private static final long serialVersionUID = 1L;
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean is_model_read = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	final boolean has_permission = is_root || is_domain_admin || is_model || is_model_read;				

	boolean isedition = false;
	boolean isnew = false;
	
	public ClassifierModelPage(PageParameters parameters) {
		Classifier classifier = getClassifier(parameters);
		if (classifier!=null)
			setModel(new ObjectModel<Classifier>(classifier));
	}
	
	public ClassifierModelPage(IModel<Classifier> model, boolean edition, boolean is_new) {
		super(model);
		this.isedition=edition;
		this.isnew=is_new;
	}
	
	public void onInitialize() {
		super.onInitialize();

		if (getModel()==null) { 
			add(new ErrorPanel("editor", new Model<String>("Classifier not found.")));
			return;
		}
		if (!hasPermissions()) { 
			addOrReplace(new ErrorNotAuthorizedPanel<>("editor", new Model<String>("Not authorized")));
			return;
		}
		
		setLogVisit(true);
		addComponents();

		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());
		
		PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<Void>();
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
			bc.addElement(new HomeBC());
			bc.addElement(new SettingsDropDownBC());
			bc.addElement(new InformationModelDropDownBC());
			bc.addElement(new ClassifiersDropdownBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
			panel.setBreadcrumbPanel(bc);
			setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
			panel.setTitle(getModel().getObject().getDisplayName());
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.classifiers", this, null).getObject()));
			setSearchPanel(true);
			setAdvancedSearch(false);
			setSuggester(false);
			// panel.setSearchPanel(getSearchPanel());
			
			
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			r_list.add(getSearchPanel("panel"));
			PageTaskToolbar<Classifier> toolbar = new PageTaskToolbar<Classifier>("toolbar", getModel(), l_list, r_list);
			panel.setToolbarPanel(toolbar);

			setPageContentHeader(panel);
	}
	
	protected Query newQuery() {
		return new ClassifiersQuery();
	}

	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=newQuery();
				q.getParameters().put("text", new TextFilter(event.getText()));
				q.getParameters().put("sort", "relevance");
				setResponsePage(new ClassifiersPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}	

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
	
	private void addComponents() {
		// IModel<Classifier> model, boolean edition, boolean is_new
		setPageTitle(new Model<String>(getModel().getObject().getName()!=null?getModel().getObject().getName() : getModel().getObject().getId().toString()));
		setPageDescription(new Model<String>( getModel().getObject().getName()));
		if (hasPermissions()) {
			ClassifierMainPanel editor = new ClassifierMainPanel(getModel(), this.isedition,this.isnew) {
				protected void onClose(AjaxRequestTarget target) {
					setResponsePage(new ClassifiersPage());
				}
			};
			editor.setEditionEnabled(this.isedition);
			add(editor);
		}
		else 
			add(new ErrorNotAuthorizedPanel<>("editor"));
		if(getModel().getObject().getId()!=null)
			getPageParameters().set("id", getModel().getObject().getId());
	}
	
	private Classifier getClassifier(PageParameters parameters) {
		Classifier classifier = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty() && hasPermissions()) {
			classifier = (Classifier)getContentDao().findModelObjectById(Classifier.class, id.toLong());
			if (classifier!=null && !classifier.getDomain().equals(getDomain())) {
				classifier = null;
			}
		}	
		return classifier;
	}
}