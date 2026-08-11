package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
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
import kbee.web.nav.HomeBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

/**
 * {@link DataSet} Editor. called from {@link DataSetConfigConsole}
 * Note that instances of a DataSet are called {@link DataSetMember}
 * They are edited in: {@link MemberPage}
 */ 
@SuppressWarnings("serial")
public class DataSetPage<T extends DataSet> extends ApplicationPage<T> {
	
	private static final long serialVersionUID = 1L;

	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean is_model_read = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	final boolean has_permission = is_root || is_domain_admin || is_model || is_model_read;

	boolean isNew=false;

	public DataSetPage(PageParameters parameters) {
		T dataset = getDataSet(parameters);
		if (dataset!=null)
			setModel(new ObjectModel<T>(dataset));
		
		StringValue isnew = parameters.get("isnew");
		if (!isnew.isNull() && !isnew.isEmpty()) {
			this.isNew=(isnew.toString().equals("yes") || isnew.toString().equals("true"));
		}
	}
	
	public DataSetPage(IModel<T> model) {
		this( model, null, false, false);
	}
	

	public DataSetPage(IModel<T> model, Panel navigationPanel, final boolean editon, final boolean is_new) {
		super(model,  navigationPanel);
		this.isNew=is_new;
	}
	
	public boolean isNew() {
		return isNew;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
		setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
			
		PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<>();
							
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new HomeBC());
		bc.addElement(new SettingsDropDownBC());
		bc.addElement(new InformationModelDropDownBC());
		bc.addElement(new DataSetsDropDownBC());
		
		bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName() + (getModel().getObject().isAggregation()?  ("<span class=\"ago\"> ("+  new StringResourceModel("built-in", DataSetPage.this, null).getObject()+ ")</span>"): "")				)));
		
		panel.setBreadcrumbPanel(bc);
		
		setPageTitle( new Model<String>(getModel().getObject().getDisplayName()));
		panel.setTitle(getModel().getObject().getDisplayName());
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
		
		if (hasPermissions()) {
			DataSetMainPanel<T> editor = new DataSetMainPanel<T>(getModel(), isNew()) {
				@Override
				protected void onClose(AjaxRequestTarget target) { 
					setResponsePage(new kbee.web.model.DataSetsPage<T>());
				}
			};
			add(editor);
			setLogVisit(true);
		}
		else
			add(new ErrorNotAuthorizedPanel<>("editor"));
												
		getPageParameters().set("id", getModel().getObject().getId());
			
	}
	
	protected Query newQuery() {
		return new DataSetsQuery();
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
				setResponsePage(new DataSetsPage<>(q));
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
	protected boolean hasPermissions() {
		return has_permission;
	}
	
	
	@SuppressWarnings("unchecked")
	private T getDataSet(PageParameters parameters) {
		T dataset = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty() && hasPermissions()) {
			dataset = (T) getContentDao().findModelObjectById(DataSet.class, id.toLong());
			if (dataset!=null && !dataset.getDomain().equals(getDomain())) {
				dataset = null;
			}
		}	
		return dataset;
	}
}
