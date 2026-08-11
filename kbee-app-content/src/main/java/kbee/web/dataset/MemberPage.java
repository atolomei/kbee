package kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.service.DataSetService;
import com.novamens.content.user.UserService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.HREFBCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.logging.Logger;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.model.DataSetMemberAggregatorDropDownBC;
import kbee.web.nav.DataSetMembersBC;
import kbee.web.nav.DataSetMembersSectionBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.DataSetMembersQuery;
import kbee.web.search.SearcherNavigatorPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class MemberPage extends ConsoleObjectPage<DataSetMember> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ConsoleObjectPage.class.getName());
	
	private IModel<DataSetMember> aggregatormodel = null;
	
	private boolean isNew = false;
	private boolean isEditon  = false;
	private List<IModel<DataSet>> datasetlist;
	
	/**
	 * @param parameters
	 */
	public MemberPage(PageParameters parameters) {
		DataSetMember member = getMember(parameters);
		if (member!=null) 
			setModel(new ObjectModel<DataSetMember>(member));
	}
	
	public MemberPage(IModel<DataSetMember> model) {
		super(model);
	}

	public MemberPage(IModel<DataSetMember> model, IModel<Cursor> cursor_model) {
		super(model, cursor_model);
	}
	
	public MemberPage(IModel<DataSetMember> model, IModel<DataSetMember> aggregatormodel, IModel<Cursor> cursor_model) {
		super(model, cursor_model);
		setAggregatorModel(aggregatormodel);
	}
	
	@Override
	public String getPageHelpKey() {
		return super.getPageHelpKey() + 
			"-" + 
			(getModel()!=null 
				? getModel().getObject().getDataSet().getName()
				: "null");		
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	public void setNew(boolean b) {
		isNew=b;
	}
	
	public void setEditon(boolean b) {
		isEditon=b;
	}
	
	@SuppressWarnings("rawtypes")
	public void onInitialize() {
		super.onInitialize();
		
		if (getModel()==null || !hasPermissions()) {
			setTopNavigation(getMainTopbar()); 	
			setMenu(getMainLaternalMenu()); 	
			add(new ErrorPanel("editor", getLabel("not-found")));
			return;
		}
		
		setLogVisit(true);
		
		addComponents(getModel(), aggregatormodel, isEditon, isNew);
		
		if (aggregatormodel!=null) {
			getModelObject()
				.getDataSet()
				.getService(DataSetService.class)
				.getAggregatedValues(aggregatormodel.getObject());
		}
		
		PageContentHeaderPanel<?> panel = new PageContentHeaderPanel();
		
		MenuBreadCrumbPanel  bc = new MenuBreadCrumbPanel();
		bc.addElement( new HomeBC());
		bc.addElement(new SettingsDropDownBC());
		
		DropDownMenuBC<?> dd = new DropDownMenuBC<>();
		dd.addElement(new BCElement("bc.dataset.members"), true);
		dd.addElement(new DataSetMembersSectionBC());
		dd.addElement(new SeparatorBC());
		for (IModel<DataSet> ds: getDataSets()) {
			 dd.addElement(new DataSetMembersBC(ds));
		}	 
		bc.addElement(dd);
		
		// DataSetMembers Dropdown 
		if (getAggregatorModel()!=null) {
			bc.addElement(new HREFBCElement("bc-menu-item",
				"/dataset/"+getAggregatorModel().getObject().getDataSet().getId().toString(), 
				new Model<String>(getAggregatorModel().getObject().getDataSet().getName())));
			bc.addElement(new HREFBCElement("bc-menu-item",
				"/dataset/" + 
				getAggregatorModel().getObject().getDataSet().getId().toString() + 
				"/" + 
				getAggregatorModel().getObject().getId(), 
				new Model<String>(getAggregatorModel().getObject().getName())));
			bc.addElement(new DataSetMemberAggregatorDropDownBC(getModel().getObject().getDataSet(), 
				getAggregatorModel().getObject()));
		}
		else {
			bc.addElement(new HREFBCElement("bc-menu-item", 
				"/dataset/"+getModel().getObject().getDataSet().getId().toString(), 
				new Model<String>(getModel().getObject().getDataSet().getName() +
					(getModel().getObject().getDataSet().isAggregation()
						?  ("<span class=\"ago\"> (" + new StringResourceModel("built-in", MemberPage.this, null).getObject()+")</span>")
						:""))));
		}
		
		bc.addElement(new BCElement( new Model<String>(getModel().getObject().getName())));
		panel.setBreadcrumbPanel(bc);
		
		setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));
		
		panel.setTitle(getModel().getObject().getDisplayName());
		
		StringBuilder str = new StringBuilder();
		try {
			ExtractionRule rule =  getModel().getObject().getDataSet().getSublineRule();
			if (rule!=null) {
				String label = (String)rule.extract( getModel().getObject());
				str.append(label);
			}
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}

		if (str.length()>0)
			panel.setSubLine(new Model<String>(str.toString()));
		
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", getModel().getObject().getDataSet().getName()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		
		List<WebMarkupContainer> l_list = new ArrayList<>();
		List<WebMarkupContainer> r_list = new ArrayList<>();
		
		r_list.add(getSearchPanel("panel"));
		r_list.add(getSearchNavigation("panel"));
		
		PageTaskToolbar<DataSetMember> toolbar = new PageTaskToolbar<DataSetMember>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		setPageContentHeader(panel);
	}
	
	@Override
	public Panel getSearchNavigation(String id)  {
		
		if (getCursorModel()==null)
			return new InvisiblePanel(id);
																		
		SearcherNavigatorPanel<DataSetMember> na=new SearcherNavigatorPanel<DataSetMember>(id, getCursorModel()) {
			@Override
			public void onNavigate(DataSetMember object) {
				try {
					setResponsePage( getNavigatePage(object, getCursor()));
				} 
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<DataSetMember>(e));
				}
			}
		};
		return na;
	}

	@Override
	public boolean hasPermissions() {

		if (getModel()==null) {
			return false;
		}
		
		if (isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId()) || 
			isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId()) || 
			isMember(KbeeGlobalRole.SUPPORT.getId()) || 
			isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		}
		
		if (ServiceLocator
			.getService(UserService.class)
			.isReadable(getModel().getObject())) {
			return true;
		}
		
		if (getAggregatorModel()!=null) {
			if (ServiceLocator
				.getService(UserService.class)
				.isReadable(getAggregatorModel().getObject())) {
				return true;
			}	
		}

		return false;  
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.datasetlist!=null) {
			datasetlist.forEach(i->i.detach());
		}
		if (aggregatormodel!=null)
			aggregatormodel.detach();
	}
	
	@Override
	protected Page getNavigatePage(DataSetMember object, IModel<Cursor> mc) {
		return new MemberPage(new ObjectModel<DataSetMember>(object), mc);
	}
	
	protected Query newQuery() {
		return new DataSetMembersQuery(getQueryIndex(), getModel().getObject().getDataSet(), false);
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
				setResponsePage(new DataSetMembersPage( new ObjectModel<DataSet>(getModel().getObject().getDataSet()), q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private void addComponents(
			IModel<DataSetMember> model, 
			IModel<DataSetMember> aggregatormodel, 
			boolean edition, 
			boolean isNew) {
		
		DataSetMember member = model.getObject();
		
		setTopNavigation(getMainTopbar()); 
		setMenu(getMainLaternalMenu());
		
		setPageTitle(new Model<String>(member.getDisplayName()));
		setPageDescription( new Model<String>(member.getDataSet().getName()+". "+model.getObject().getDisplayName()));
		
		MemberMainPanel editor = new MemberMainPanel(model, aggregatormodel, isNew) {
			@Override
			public void onCancel(AjaxRequestTarget target) {
			}
		};
		editor.setEditionEnabled(edition);
		add(editor);
		
		getPageParameters().set("id", member.getDataSet().getId().toString());
		getPageParameters().set("memberid", member.getId());
	}
	
	private DataSetMember getMember(PageParameters parameters) {
		

		DataSetMember member = null;
		
		StringValue id = parameters.get("memberid");
		
		if (!id.isNull() && !id.isEmpty()) { 
			try {
				member = getContentDao().findMemberById(id.toLong());
			} 
			catch (Exception e) {
				logger.error(e);
				return null;
			}
		}	
		if (member!=null && isAggregation(member)) {
			DataSetMember aggregator = getAggregator(member);
			if (aggregator!=null) {
				setAggregatorModel(new ObjectModel<DataSetMember>(aggregator));
			}
		}
		return member;
	}
	
	private boolean isMember(String groupname) {
		return ServiceLocator.getService(SecurityService.class).isMember(groupname);
	}
	
	private void setAggregatorModel(IModel<DataSetMember> model) {
		this.aggregatormodel = model;
	}
	
	private IModel<DataSetMember> getAggregatorModel() {
		return this.aggregatormodel;
	}
	
	private boolean isAggregation(DataSetMember member) {
		return member.getDataSet().isAggregation();
	}
	
	private DataSetMember getAggregator(DataSetMember member) {
		DataSetMember aggregator = member.getDataSet()
			.getService(DataSetService.class)
			.getAggregator(member);
		return aggregator;
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
			this.datasetlist.add(new ObjectModel<DataSet>(dataset));
		}
		return this.datasetlist;
	}
}