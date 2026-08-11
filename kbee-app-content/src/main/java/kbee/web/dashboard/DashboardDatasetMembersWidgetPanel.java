package kbee.web.dashboard;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.console.CursorNavigator;
import kbee.web.cursor.CursorListModel;
import kbee.web.cursor.ModelListCursor;
import kbee.web.dataset.DataSetMembersPage;
import kbee.web.dataset.MemberPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.nav.Navigator;
import kbee.web.portal6.factory.PanelPortalModel;

/**
 * 
 * 
----------------------------------------------------------
- 1. Home -> DataSet personas
- 2. Menu ppal. Seguridad  -> personas
- 3. Shared page -> 
- 4. Home del portal navegar al home de gestion
- 5. Al ingresar usuario externo que vaya al portal
----------------------------------------------------------
Creacion de cuenta de usuario

1. Distribución -> lo vinculo a una persona que no existe, la creo y le comparto un doc publicado.
2. Usuario que recibe la Shared page gestiona su cuenta [1. mail con instrucciones | 2. mail habilita token temporal con rol externo | ]
3. Tarea -> Seleccion colaborator -> abrir la consola de dataset para crear una persona + cuenta de usuario -> gestionar su pwd, cuando el admin crea la cuenta indica si le permite autenticarse por FB | Google
4. Creacion desde la consola de usuario no existe! -> existe el dataset persona, hay crea la cuenta.

----------------------------------------------------------
. Usuario externo y luego genstionar 

----------------------------------------------------------
Notificaciones
- email 

Persona con o sin usuario -> al dominio express

Dominio express siempre va a tener dataset persona
TExtos , que hacemos ????
*
*
*
*/
 
public class DashboardDatasetMembersWidgetPanel extends DashboardListWidgetPanel<DataSetMember> implements PanelPortalModel<Block>, PortalViewRender  {
	private static final long serialVersionUID = 1L;

	static final public String PROPERTY_UNREAD = "unread";
	
	final protected boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final protected boolean role_dataset_members = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	final protected boolean role_dataset_members_write = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());

	private static Logger logger = Logger.getLogger(DashboardDatasetMembersWidgetPanel.class.getName());

	private NumberFormat integer_nf = null;
	private String zid;
	private Locale locale;
	
	private DateTimeService _service;
	
	private int size;
	private IModel<DataSet> datasetmodel;
	private List<IModel<DataSet>> datasets;
	

	
	public DashboardDatasetMembersWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);

		integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
		integer_nf.setMinimumFractionDigits(0);
		integer_nf.setMaximumFractionDigits(0);
		integer_nf.setRoundingMode(java.math.RoundingMode.HALF_UP);
		
		KbeeUser us = (KbeeUser) getSessionUser();
		locale=us.getLocale();
		zid = getDateTimeService().getMapZoneIds().get(us.getTimeZone());
		
		setHelp(true);
		setEdit(false);
		setTitle( getLabel("datasetmembers"));
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<GeneralWicketAjaxEvent>() {
			@Override
			public void onEvent( GeneralWicketAjaxEvent event) {
				if (event.getName().equals( WidgetSelectorHeaderPanel.class.getName()) ) {
					DashboardDatasetMembersWidgetPanel .this.replacePanel(event);	
				}
			}
		});
	}

	public DataSet getDataSet() {
		return getDataSetModel()==null ? null : getDataSetModel().getObject();
	}
	
	public IModel<DataSet> getDataSetModel() {
		return datasetmodel;
	}
	
	public void setDataSet(IModel<DataSet> model) {
		this.datasetmodel = model;
		setUserPreference("dataset", model.getObject().getAlias());
	}
	
	public void setDataSet(DataSet da) {
		if (da!=null)
			setDataSet(new ObjectModel<DataSet>(da));
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<IModel<DataSet>> getDataSets() {
		if (datasets==null) {
			datasets = new ArrayList<IModel<DataSet>>();
			for (DataSet ds : getContentDao().getDataSets(getDomain().getId(), ObjectState.ENABLED)) {
				if (ds.getDataSetType()== DataSetType.STRING    ||
					ds.getDataSetType()== DataSetType.EXTERNAL  ||
					ds.getDataSetType()== DataSetType.ENTITY 	||
					ds.getDataSetType()== DataSetType.LABEL 	||
					ds.getDataSetType()== DataSetType.SECURED   ||
					ds.getDataSetType()== DataSetType.PEOPLE)
					if (role_dataset_members || role_dataset_members_write || isAdmin(ds)) {
						datasets.add( new ObjectModel<DataSet>(ds));
					}
			}
		}
		return datasets;
	}
	
	@Override
	public void setPortalModel(IModel<Block> model) {
	}
	
	@Override
	public IModel<Block> getPortalModel() {
		return null;
	}
	
	public void onInitialize() {
		if (getDataSets().size()>0) {
			String p=getUserPreference("dataset");
			if (p==null) {
				p=getDataSets().get(0).getObject().getAlias();
				setDataSet(getDataSets().get(0));
			}	
			else {
				boolean found=false;
				for (IModel<DataSet> l:getDataSets()) {
					if (l.getObject().getAlias()!=null && l.getObject().getAlias().equals(p)) {
						setDataSet(l);
						found=true;
						break;
					}
					if (!found)
						setDataSet(getDataSets().get(0));
				}
			}
		}
		
		WidgetSelectorHeaderPanel<DataSet> he=new WidgetSelectorHeaderPanel<DataSet>("header", "dataset", getTitle(), getDataSetModel(),  getDataSets()) {
			protected void refresh(AjaxRequestTarget target) {
				DashboardDatasetMembersWidgetPanel.this.refresh(target);
			}
			protected void onEdit(AjaxRequestTarget target) {
				DashboardDatasetMembersWidgetPanel.this.onEdit(target);
			}
			protected void onHelp(AjaxRequestTarget target) {
				DashboardDatasetMembersWidgetPanel.this.onHelp(target);
			}
			
			protected String getItemLabel(IModel<DataSet> value) {
				return  DashboardDatasetMembersWidgetPanel.this.getItemLabel(value);
			}
		};
		
		he.setHelp(true);
		he.setEdit(false);
		
		setHeader(he);

		if (getDataSet()!=null)
			addDataSet();
		
		super.onInitialize();
	}

	protected String getItemLabel(IModel<DataSet> value) {
			return getLabel("name", value.getObject().getDisplayName()).getObject();
	}

	public void onDetach() {
		super.onDetach();
		if (datasetmodel!=null)
			datasetmodel.detach();
		if (datasets!=null) 
			datasets.forEach(item -> item.detach());
		_service=null;
	}
	
	protected void replacePanel(GeneralWicketAjaxEvent event) {
		
		String lb = (String) event.getParameters().get("item");
		
		if (getDataSet()==null)
			setDataSet(getDataSets().get(0));

		if (lb==null || getDataSet().getId().equals(lb))
			return;
		
		boolean found=false;
		
		for (IModel<DataSet> l: getDataSets()) {
			if (l.getObject().getId().toString().equals(lb)) {
				setDataSet(l);
				found=true;
				break;
			}
		}
		if (found) {
			addDataSet();
			super.addTabs();
			event.getRequestTarget().add(this);
		}
	}

	protected void addDataSet() {
		setItems();
	}

	protected void setItems() {
		List<IModel<DataSetMember>> items = new ArrayList<IModel<DataSetMember>>();
		KbeeUser us = (KbeeUser) getSessionUser();
		us.getService(UserDashboardService.class).getDataSetMembers(getDataSet(), 15).forEach(item -> items.add(new ObjectModel<DataSetMember>(item)));
		setSize(items.size());
		setItems(items);
	}
	
	protected void onHelp(AjaxRequestTarget target) {
		super.toogleHelp(target);
	}
 
	protected WebMarkupContainer getHelpPanel() {

		InlineHelpWebService se = ServiceLocator.getService(InlineHelpWebService.class);
		
		if  (getPreferencesKey().equals("entities")) {
			WebMarkupContainer pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_DATASETMEMBERS_ENTITIES);
			if (pa!=null) return pa;
			return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_DATASETMEMBERS_ENTITIES));
		}
		else {
			WebMarkupContainer pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_DATASETMEMBERS);
			if (pa!=null) return pa;
			return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_DATASETMEMBERS));
		}
	}
	
	@Override
	protected void onClickAll() {
		setResponsePage(new DataSetMembersPage( getDataSetModel() ));			
	}
	
	protected Navigator<DataSetMember> getNavigator(IModel<DataSetMember> model, int index) {
		List<IModel<DataSetMember>> mi= new ArrayList<IModel<DataSetMember>>();
		getItems().forEach(item -> {mi.add(new ObjectModel<DataSetMember>((DataSetMember) item.getObject()));});
		CursorListModel<DataSetMember> cursor = new CursorListModel<DataSetMember> (  mi, index);
		return new CursorNavigator<DataSetMember>(cursor, index);
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected DateTimeService getDateTimeService() {
		if (_service==null)
			_service = ServiceLocator.getService(DateTimeService.class);			
		return _service;
	}
	
	
	protected IModel<String> getItemLabelMeta(IModel<DataSetMember> modelObject) {
		StringBuilder str = new StringBuilder();
		try {
			str.append( "" );
		} 
		catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString()									);
	}

	@Override
	protected IModel<String> getViewingString() {
		return getLabel("recently-modified-library", String.valueOf(size));
	}

	protected IModel<String> getAllString() {
		return getLabel("bc.datasetmembers");
	}

	@Override
	protected void onClick(IModel<DataSetMember> model, int index) {
		try {
			Page page = getPage( model, index);
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}
									
	protected Page getPage(IModel<DataSetMember> model, int index) 	{
		try {
			List<IModel<DataSetMember>> mi= new ArrayList<IModel<DataSetMember>>();
			getItems().forEach(item ->	 { mi.add(new ObjectModel<DataSetMember>((DataSetMember) item.getObject())); });
			CursorListModel<DataSetMember> cursor = new CursorListModel<DataSetMember> (  mi, index);
			return new MemberPage(model, new ModelListCursor<DataSetMember>(cursor));
		} 
		catch (Exception e) {
			logger.error(e);
			return new kbee.web.error.ApplicationErrorPage<Void>(e);
		}
	}

	@Override
	protected Panel getMenu(IModel<DataSetMember> model, int index) {
		return new InvisiblePanel("menu");
	}
	
	@Override	
	public IModel<String> getIconCss(IModel<DataSetMember> model) {
		return null;
	}
	
	protected String getInfo(DataSetMember value) {
		ExtractionRule rule = value.getDataSet().getSublineRule();
		if (rule!=null) {
			String label = (String)rule.extract(value);
			return label;
		}
		return null;
	}
	
	protected boolean isIconVisible() {
		return false;
	}
	
	protected boolean isMenuVisible() {
		return false;
	}

	protected NumberFormat getIntegerNumberFormat() {
		return integer_nf;
	}

	protected String getZid() {
		return zid;
	}

	protected Locale getSessionUserLocale() {
		return locale;
	}
	
	protected boolean isExpand() {
		return false;
	}
	
	protected String getName() {
		return "home-datasetmembers";
	}
	
	protected IModel<String> getListTitle() {
		return getLabel("recent-activity");
	}
	
	private boolean isAdmin(DataSet ds) {
		return ServiceLocator.getService(UserService.class).isAdmin(ds);
	}
}