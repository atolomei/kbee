package kbee.web.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;

import com.novamens.content.entity.Person;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Task;

import kbee.util.NumberFormatter;
import kbee.web.dashboard.DashboardDatasetEntititesWidgetPanel;
import kbee.web.dashboard.DashboardPage;
import kbee.web.dashboard.DashboardWidgetSimpleWrapperPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.help.InlineHelpWebService;

import kbee.web.nav.DataSetMembersBC;
import kbee.web.nav.DataSetMembersSectionBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.panel.ListSimplePanel;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings("serial")
public class DashboardDataSetMembersHomePage extends DashboardPage<Person> {
				
	private static final long serialVersionUID = 1L;

	static final public String PROPERTY_UNREAD = "unread";
	static final String KEY = "model-home";

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardDataSetMembersHomePage.class.getName());

	final boolean is_root=ServiceLocator.getService(SecurityService.class).isRoot();
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_values = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_values_read = role_dataset_values || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private Map<Serializable, String> recent = new HashMap<>();
	private Map<Serializable, StringBuilder> map = new HashMap<>();
	private Map<Serializable, Long> d_count = new HashMap<>();

	private List<IModel<DataSet>> list;
	
	public DashboardDataSetMembersHomePage() {
		add(new RefreshBehavior());
	}

	public IModel<String> getTitle() {
		return new StringResourceModel("bc.datasetmembers", this, null);
	}
	
	@Override
	public boolean hasPermissions() {
		if (getDomain()==null)
			return false;
		if (role_dataset_values || role_dataset_values_read || role_support || role_model || role_federated_values)
			return true;
		return false; 
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<GeneralWicketEvent>() {
			@Override
			public void onEvent(GeneralWicketEvent event) {
				logger.debug( event.getName());
			}
		});
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}
	
	public List<IModel<DataSet>> getItems() {
		
		if (list!=null)
			return list;
		
		this.list = new ArrayList<>();
		
		for (DataSet dataset: getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
			if (hasPermissions(dataset)) {
				list.add(new ObjectModel<DataSet>(dataset));
			}
		}
		
		list.sort(new Comparator<IModel<DataSet>>() {
			@Override
			public int compare(IModel<DataSet> a, IModel<DataSet> b) {
				try {
					return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
				} 
				catch (Exception e) {
					return 0;	
				}
			}
		});
		
		return list;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (!hasPermissions()) {
			return;
		}
		
		getModalContainerMarkupContainer().add(new InvisiblePanel("audit-trail-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("send-email-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("error-dialog"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("confirmation-dialog"));

		
		for (ContentTemplate t:getContentDao().getTemplates(getDomain(), ObjectState.ENABLED)) {
			for (ClassifierTemplate c: t.getClassifiers()) {
				DataSet da=c.getClassifier().getDataSet();
		
				if (!map.containsKey(da.getId()))
					map.put(da.getId(), new StringBuilder());
				
				if ( map.get(da.getId()).length()>0)
					map.get(da.getId()).append(", ");
				
				map.get(da.getId()).append( getLink(t));
			
			}
		}
		
		setSuggester(false); 
		setSearchPanel(false);
		setAdvancedSearch(false); 
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (list!=null)
			list.forEach(item -> item.detach());
	}

	protected void addWidgets() {
		addWidget(new ListView<WidgetFactory>("widget-left", getLeftSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});
		
		addWidget(new ListView<WidgetFactory>("widget-center", getCenterSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
		addWidget(new ListView<WidgetFactory>("widget-right", getRightSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
	}

	protected void onSiteClick(IModel<Site> modelObject) {
	}

	@SuppressWarnings("unchecked")
	protected void onClick(IModel<Content> model) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		try {
			TaskPage<Content> page = null;
			if (workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) {
				Task task = workflowService.getTask();
				page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
				if (model.getObject().getWorkspace()>0) {
					if (getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
						page.setEditionEnabled(true);
						page.setReadOnly(false);
					}
					else {
						page.setEditionEnabled(false);
						page.setReadOnly(true);
					}
				}
				else {
					page.setEditionEnabled(false);
					page.setReadOnly(true);
				}
			}
			if (page==null)
				throw new IllegalArgumentException("page is null");
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}

	@Override
	protected String getPageKey() {
		return KEY;
	}
	
	protected List<WidgetFactory> getRightSectionsPanels() {
		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
		return widgets;
	}
	
	protected List<WidgetFactory> getCenterSectionsPanels() {
		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
		if (getDataSets().size()>0) {  
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardDatasetEntititesWidgetPanel(id, DashboardDataSetMembersHomePage.KEY);
				}	
				public IModel<String> getLabel() {
					return DashboardDataSetMembersHomePage.this.getLabel("entities");
				}
			});
		}
		return widgets;
	}

	protected List<WidgetFactory> getLeftSectionsPanels() {
		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				ListSimplePanel<DataSet> pa= new ListSimplePanel<DataSet>("payload", "datasetmembers", getItems()) {
					private static final long serialVersionUID = 1L;
					protected void onClick(IModel<DataSet> modelObject, int index) {
						setResponsePage(new RedirectPage("/dataset/"+ modelObject.getObject().getId().toString()));
					}
					@Override
					protected String getTitleMeta() {
						return null;
					}
					
					protected IModel<String> getItemAbstract(IModel<DataSet> modelObject) {
						return  getAbstractHMTL(modelObject);
					}
					
					@Override
					protected IModel<String> getItemLabelMeta(IModel<DataSet> modelObject) {
						long total =  getTotalMembers(modelObject).longValue();
						return new Model<String>(" ("+ NumberFormatter.formatNumber(total, getSessionUser().getLocale())+")");
					}
				};
				pa.setMenu(false);
				pa.setExpand(true);
				DashboardWidgetSimpleWrapperPanel<Person> mo = new DashboardWidgetSimpleWrapperPanel<Person>(id, getModel(), pa, DashboardDataSetMembersHomePage.KEY);
				mo.setHelpKey(InlineHelpWebService.HOME_DATASETMEMBERS);		
				mo.setTitle(DashboardDataSetMembersHomePage.this.getLabel("bc.datasetmembers"));
				
				return mo;
			}
			public IModel<String> getLabel() {
				return  DashboardDataSetMembersHomePage.this.getLabel("bc.informationmodel");
			}
		});

		return widgets;
	}

	

	
	@Override
	protected Panel getBreadcrumbPanel() {
		try {
			
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			
			bc.addElement( new HomeBC());
			
			bc.addElement( new SettingsDropDownBC());
			DropDownMenuBC<?> dd = new DropDownMenuBC<>();
			dd.addElement(new BCElement("bc.dataset.members"), true);
			dd.addElement(new DataSetMembersSectionBC());
			dd.addElement(new SeparatorBC());
			
			for (IModel<DataSet> ds: getItems()) 
				 dd.addElement( new DataSetMembersBC(ds)); 
			bc.addElement(dd);
			bc.addElement(new BCElement("bc.dataset.members.home"));
			return bc;
			
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}

	
	/**
	 * 
	 * @return
	 */


	protected String getDataSetUrl(DataSet object) {
		return getServerUrl()+"/model/datasets/"+object.getId().toString();
	}
	
    protected String getTitle(IModel<DataSet> m) { 
    	return m.getObject().getDisplayName();
    }
    
	protected String getTitleMeta() {
		return " ("+String.valueOf(getItems().size()+")");
	}
	
	protected String getLink(ContentTemplate t) {
		return "<a class=\"btn-link\" target=\"_blank\" href=\""+ getServerUrl()+"/model/contentclass/"+t.getId().toString()+"\">"+ t.getName()+"</a>";
	}
	
	protected String getRecentDataSetMembersHTML(IModel<DataSet> modelObject) {
	
		if (recent.containsKey(modelObject.getObject().getId()))
			return recent.get(modelObject.getObject().getId());
			
		StringBuilder str = new StringBuilder ();
		
		int n = 0;

		List<DataSetMember> list = getContentDao().getMembers(modelObject.getObject(), "lastmodifieddate desc", 5);
		
		if (list==null || list.size()==0)
			return null;

		str.append( new StringResourceModel("recent-activity", this, null).getObject() +": ");
		
		for (DataSetMember dm : list) {
				if (n>0)
					str.append(",  ");
				
				str.append("<a class=\"btn-link\" href=\""+ getServerUrl()+"/dataset/"
						+dm.getDataSet().getId().toString() + "/"+ dm.getId().toString()+"\">"+ (dm.getName()!=null?dm.getName():"null")+"</a>");
			if (n++>4)
				break;
		}
		
		recent.put(modelObject.getObject().getId(), str.toString());
		
		return str.toString();
	}

	protected Long getTotalMembers(IModel<DataSet> modelObject) {
		if (d_count.containsKey(modelObject.getObject().getId()))
			return d_count.get(modelObject.getObject().getId());
		long total = getContentDao().getAllStatesTotalMembers(modelObject.getObject());
		d_count.put(modelObject.getObject().getId(), Long.valueOf(total));
		return Long.valueOf(total);
	}

	
	protected IModel<String> getAbstractHMTL(IModel<DataSet> modelObject) {
		StringBuilder ret = new StringBuilder();
		String rec=getRecentDataSetMembersHTML(modelObject);
		ret.append( (rec!=null && rec.length()>0) ? (rec+"<br/>") :"");
		String d=modelObject.getObject().getDescription();
		if (d!=null && d.length()>0)
			ret.append("<br/>"+d);
		return new Model<String>(ret.toString());
	}
	
	public boolean hasPermissions(DataSet ds) {
		DataSetType type = ds.getDataSetType();
		
		if (!(type == DataSetType.STRING  ||
			type == DataSetType.EXTERNAL ||
			type == DataSetType.ENTITY 	||
			type == DataSetType.SECURED ||
			type == DataSetType.LABEL 	||
			type == DataSetType.PEOPLE)) {
			return false;
		}	
		
		if (role_dataset_values || role_dataset_values_read || role_support || role_model)
			return true;
		
		if (ServiceLocator.getService(UserService.class).isAdmin(ds)) {
			return true;
		}
		
		return false;
	}
}
