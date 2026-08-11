package kbee.web.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.DataSetMembersBC;
import kbee.web.nav.DataSetMembersSectionBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class DataSetMembersPage extends ConsolePage<DataSetMember> {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetMembersPage.class.getName());

	final boolean is_root=ServiceLocator.getService(SecurityService.class).isRoot();
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_values = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_values_read = role_dataset_values || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private List<IModel<DataSet>> datasetlist;
	private IModel<DataSet> datasetmodel;
	

	public DataSetMembersPage() {
		DataSet dataSet = getSelectedDataSet();
		if (dataSet==null && getDataSets().size()>0)
			setDataSetModel(new ObjectModel<DataSet>(dataSet));
	}
	
	/**
	 * @param parameters
	 */
	public DataSetMembersPage(PageParameters parameters) {
		
		DataSet dataSet = getDataSet(parameters);
		if (dataSet == null) {
			dataSet = getSelectedDataSet();
			if (dataSet==null && getDataSets().size()>0)
				dataSet = getDataSets().get(0).getObject();
		}
		
		if (dataSet != null)
			setDataSetModel(new ObjectModel<DataSet>(dataSet));
	}

	
	public DataSetMembersPage(IModel<DataSet> model) {
		setDataSetModel(model);
	}
	
	/**
	 * 
	 * @param model
	 * @param query
	 */
	public DataSetMembersPage(IModel<DataSet> model, Query query) {
		super(query);
		((SolrQuery)query).getFilterParameters().remove("parent");
		setDataSetModel(model);
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());  
		
		if (datasetmodel==null || datasetmodel.getObject()==null) 
			return;
				
		 if (hasPermissions()) {

			setLogVisit(true);
			
			PageContentHeaderPanel<DataSet> header=new PageContentHeaderPanel<DataSet>(getDataSetModel());
			setPageTitle(new Model<String>(datasetmodel.getObject().getName()));
 			
			setPreference(datasetmodel.getObject());
			getPageParameters().set("id", datasetmodel.getObject().getId());
			header.setTitle( datasetmodel.getObject().getDisplayName() );
			header.setBreadcrumbPanel(getHeaderPanelBreadcrumbPanel());
			setSearchPlaceHolder(datasetmodel.getObject().getDisplayName());
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", datasetmodel.getObject().getDisplayName()));
			
			setSuggester(false); // Search supports suggester
			setSearchPanel(true); // include Search
			setAdvancedSearch(false); // button advanced search
			
			//header.setSearchPanel(getSearchPanel());
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			Panel s=getSearchPanel("panel");
			r_list.add(s);
			PageTaskToolbar<DataSetMember> toolbar = new PageTaskToolbar<DataSetMember>("toolbar", getModel(), l_list, r_list);
			header.setToolbarPanel(toolbar);
			setPageContentHeader(header);
			
		}
	}
	
	protected Panel getHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			
			bc.addElement( new HomeBC());
			bc.addElement( new SettingsDropDownBC());
			
			DropDownMenuBC<?> dd = new DropDownMenuBC<>();
			dd.addElement(new BCElement("bc.dataset.members"), true);
			dd.addElement(new DataSetMembersSectionBC());
			dd.addElement(new SeparatorBC());
			for (IModel<DataSet> ds: getDataSets())
				 dd.addElement( new DataSetMembersBC(ds)); 
			bc.addElement(dd);
			
			bc.addElement(new BCElement(
					new Model<String>(datasetmodel.getObject().getName() + (datasetmodel.getObject().isAggregation() ?(" <span class=\"ago\">("+ new StringResourceModel("built-in", DataSetMembersPage.this, null).getObject() +")</span>"):"")
							)
					));
			return bc;
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}

	/**
	 * 
	 */
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	public void setDataSetModel(IModel<DataSet> model) {
		this.datasetmodel = model;
	}

	public IModel<DataSet> getDataSetModel() {
		return datasetmodel;
	}
	
	public DataSet getDataSet() {
		return datasetmodel.getObject();
	}

	@Override
	public Console<DataSetMember> newConsole(Query query) {
		if (datasetmodel == null)
			return null;
		return new DataSetMembersConsole(getDataSetModel(), query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return DataSetMembersPage.this.getConsolePage(query, index);
			}
		};
	}

	/**
	 * Domain Admin Information Model Support
	 * can Read this sections.
	 */
	@Override
	public boolean hasPermissions() {
		if (role_dataset_values_read || role_support || role_model || isAdmin(getDataSet()))
			return true;
		return false;
	}

	@Override
	public void onDetach() {
		if (datasetmodel != null)
			datasetmodel.detach();
		this.datasetlist = null;
		super.onDetach();
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new DataSetMembersPage(getDataSetModel(), query);
	}
	
	public List<IModel<DataSet>> getDataSets() {
		if (this.datasetlist!=null)
			return this.datasetlist;
		this.datasetlist = new ArrayList<IModel<DataSet>>();
		for (DataSet dataset: getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
			if (dataset.getDataSetType() == DataSetType.STRING   ||
				dataset.getDataSetType() == DataSetType.EXTERNAL ||
				dataset.getDataSetType() == DataSetType.ENTITY 	||
				dataset.getDataSetType()== DataSetType.SECURED 	||
				dataset.getDataSetType() == DataSetType.LABEL 	||
				dataset.getDataSetType() == DataSetType.PEOPLE)
			this.datasetlist.add( new ObjectModel<DataSet>(dataset));
		}
		return this.datasetlist;
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
				q.getParameters().remove("parent");
				setResponsePage(new DataSetMembersPage(getDataSetModel(), q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	
	protected DataSet getDataSet(PageParameters parameters) {
		if (parameters == null) 
			return null;
 		DataSet dataSet = null;
		Serializable domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain().getId();
		if ((parameters.get("id") != null) && (parameters.get("id").toString().length() > 0)) {
			for (DataSet ds : getContentDao().getDataSets(domain)) {
				if (ds.getId().toString().equals(parameters.get("id").toString())) {
					dataSet = ds;
					return dataSet; 
				}
			}
		}
		return dataSet;
	}
	
	protected DataSet getSelectedDataSet() {
		DataSet dataset;
		String did = ((KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue("dataset-member-selected", "dataset");
		if (did==null)
			return null;
		dataset= (DataSet) getContentDao().findModelObjectById(DataSet.class, did);
		return dataset;
	}
	
	protected void setPreference(DataSet dataset) {
		((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue("useractions", "dataset", dataset.getId().toString());
	}

	private boolean isAdmin(DataSet ds) {
		return ServiceLocator.getService(UserService.class).isAdmin(ds);
	}
}
