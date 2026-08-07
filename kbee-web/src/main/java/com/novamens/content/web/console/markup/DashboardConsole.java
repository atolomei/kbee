package com.novamens.content.web.console.markup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Resource;
import com.novamens.content.base.Source;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.service.workflow.UserWorkLoadData;
import com.novamens.content.service.workflow.WorkflowLoadService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.ImageColumnPanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.grid.TargetBlankObjectTitleColumnPanel;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.DataSetBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.query.MonitorQuery;
import kbee.web.report.ReportColumn;
import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.security.UsersQuery;
import kbee.web.service.ApplicationSiteMapService;


/**
 *  Como indicar el "effective date" ?
 *
 */
public abstract class DashboardConsole extends AbstractFacetedConsole<Person> {
						
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardConsole.class.getName());

	private IModel<UserSet> datasetmodel;
							
	final boolean role_admin   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_pending = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	//final boolean bulk_create = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId());

	
	private List<GridColumn<SearchResult,String>> columns;
	
	static public String KEY="dashboard";
	
	public DashboardConsole(IModel<UserSet> datasetmodel, Query query) {
		super(KEY, query);
		setDataSet(datasetmodel);
		this.setOutputMarkupId(true);
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("header"));
			}
		});
	}
	@Override
	protected boolean hasIcon(IModel<Person> model) {
		return false;
	}

	@Override
	protected String getIcon(IModel<Person> model) {
		return null;
	}	
	
	  protected  IModel<Person> getModel(Person object) {
			return new ObjectModel<Person>(object, true);
		}
	
	public void setDataSet(IModel<UserSet> model) {
		this.datasetmodel = model;
	}

	public UserSet getDataSet() {
		return datasetmodel.getObject();
	}


	@Override
	public void onInitialize() {
		super.onInitialize();
		/**
 		
 		MenuBreadCrumbPanel bc =new MenuBreadCrumbPanel();
 		
 		bc.addElement( new HomeBC());
 		bc.addElement(new TasksDropdownMenuBC());
		bc.addElement(new BCElement("dashboard"));
		add(bc);
		**/
		
	}
	
	@Override
	public void onDetach() {
		for (GridColumn<?,?> column: getColumns()) 
			column.detach();
		this.datasetmodel.detach();
		super.onDetach();
	}

	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public Query newQuery() {
		UsersQuery uq = new UsersQuery(getQueryIndex(), getDataSet());
		uq.getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		return setUserPreference(uq);
	}
	
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new DataSetBC(getDataSet()));
	};
	
	
	@Override
	protected boolean hasExpander() {
		return true;
	}


	@Override
	protected Panel getMenu(IModel<Person> model) {
		return null;
	}
	
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		DateTimeService d_service = ServiceLocator.getService(DateTimeService.class);
		
		Locale locale = getSessionUser().getLocale();
		
		String zid = d_service.getMapZoneIds().get(getSessionUser().getTimeZone());

		if (zid==null) 
				zid=ZoneId.systemDefault().getId();
		
		String today_str 			= d_service.format( OffsetDateTime.now(), zid, locale, 				DateTimeService.Dow_Month_Day_year);
		String today1_str			= d_service.format( OffsetDateTime.now().plusDays(1), zid, locale, 	DateTimeService.Dow_Month_Day_year);
		String today2_str			= d_service.format( OffsetDateTime.now().plusDays(2), zid, locale, 	DateTimeService.Dow_Month_Day_year);
		String today3_str			= d_service.format( OffsetDateTime.now().plusDays(3), zid, locale, 	DateTimeService.Dow_Month_Day_year);
		String today4or_more_str	= d_service.format( OffsetDateTime.now().plusDays(4), zid, locale, 	DateTimeService.Dow_Month_Day_year); 
		
		this.columns.add(new GridColumn<SearchResult, String>("userphoto", getLabel("photo")) {
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Person> objectmodel = getModel((Person)object);
				cellItem.add(new ImageColumnPanel<Person>(componentId, objectmodel) {
					private static final long serialVersionUID = 1L;
					@Override
					protected Image getImage(String id) {
						try {
							Person object = getModelObject();
							return new ResourceThumbnailImage<>(id, null, new ObjectModel<Resource>( (Resource) object.getPhoto()), ThumbnailSize.MINI);
						} 
						catch (Exception e) {
							return null;
						}
					}

					protected String getCss() {
						return "userphotocolumn";
					}
				});
			}

			@Override
			public boolean isExportable() {
				return false;
			}

			/**
			 * for exporting to xls
			 */
			public IModel<String> getCellAsString(SearchResult result) {
				if (result==null)
					return new Model<String>("");
				Person person = (Person) result.getObject();
				if (person!=null) {
					try {
						if (person.getPhoto()!=null && person.getPhoto().isBinaryFile()) 
							return new Model<String>(person.getPhoto().getFileName());
					} catch (Exception e) {
						logger.error(e);
						return new Model<String>(e.getClass().getName());
					}
				}
				return new Model<String>("");
			}
			
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
		
		this.columns.add(new GridColumn<SearchResult, String>("lastname", getLabel("column.name"), "title_sort") {
			private static final long serialVersionUID = 1L;
			
			
			
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {
					Object object = resultmodel.getObject().getObject();
					
					IModel<Person> objectmodel = getModel((Person)object);
					
					cellItem.add(new  TargetBlankObjectTitleColumnPanel<Person>(componentId, objectmodel) {
						private static final long serialVersionUID = 1L;
						@Override
						protected String getCss() {
							return "cell-label btn-link";
						}
						public String getTitle() {
							return getModelObject().getDisplayName() + (getModelObject().getState()==ObjectState.ENABLED?"": (" <span class=\"archived\">( "+ getModelObject().getState().getLabel(getSessionUser().getLocale()) + ")</span>"));
						}

					});
				} catch (Exception e) {
					logger.error(e);
					cellItem.add(new Label(componentId, e.getClass().getSimpleName()));
				}
			}
			
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return  DashboardConsole.this.getName() + super.getContextKey();
			}
			
			
			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				if (result==null)
					return new Model<String>("");
				Person person = (Person) result.getObject();
				if (person!=null) {
					try {
							return new Model<String>(person.getDisplayName());
					} catch (Exception e) {
						logger.error(e);
						return new Model<String>(e.getClass().getName());
					}
				}
				return new Model<String>("");
			}
		});


		this.columns.add(new GridColumn<SearchResult, String>("totaltasks", getLabel("totaltasks")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				
				try {
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					return new Model<String>(String.valueOf(data.total));
					
				} catch (Exception e) {								
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				String exp = data.total>0?"info":"";
				return "number-md "+ exp;
			}

			// main div
			//
			@Override
			public String getCssClass() {
				return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});


		this.columns.add(new GridColumn<SearchResult, String>("duedate-today", getConsoleLabel("duedate-today", today_str)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					return new Model<String>(String.valueOf(data.today_due_date));
					
				} catch (Exception e) {								
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				String exp = data.today_due_date>0?"due-today":"";
				return "number-md "+ exp;
			}
			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});


		this.columns.add(new GridColumn<SearchResult, String>("duedate-past", getLabel("duedate-past")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					return new Model<String>(String.valueOf(data.past_due_date));
					
				} catch (Exception e) {								
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			// Label Container
			//
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				String exp = data.past_due_date>0?"expired":"";
				return "number-md "+ exp;
			}
						
			
			// Main Div 
			//
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});

		
		this.columns.add(new GridColumn<SearchResult, String>("duedate-plusone", getConsoleLabel("duedate-plusone", today1_str)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					return new Model<String>(String.valueOf(data.due_plus_one));
					
				} catch (Exception e) {								
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				String exp = data.due_plus_one>0?"due-today":"";
				return "number-md "+ exp;
			}
			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});


		this.columns.add(new GridColumn<SearchResult, String>("duedate-plustwo", getConsoleLabel("duedate-plustwo", today2_str)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					return new Model<String>(String.valueOf(data.due_plus_two));
					
				} catch (Exception e) {								
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				String exp = data.due_plus_two>0?"due-today":"";
				return "number-md "+ exp;
			}
			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});

		
		this.columns.add(new GridColumn<SearchResult, String>("duedate-plusthree", getConsoleLabel("duedate-plusthree", today3_str)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					return new Model<String>(String.valueOf(data.due_plus_three));
					
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				String exp = data.due_plus_three>0?"due-today":"";
				return "number-md "+ exp;
			}
			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});
		

		
		this.columns.add(new GridColumn<SearchResult, String>("duedate-plusfourormore", getConsoleLabel("duedate-plusfourormore", today4or_more_str)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
				
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					
					int val = data.due_plus_four+data.due_plus_five+data.due_plus_six+data.due_plus_n;
							
					return new Model<String>(String.valueOf(val));
					
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				int val = data.due_plus_four+data.due_plus_five+data.due_plus_six+data.due_plus_n;
				String exp = val>0?"due-today":"";
				return "number-md "+ exp;
			}
			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});

		
		
		this.columns.add(new GridColumn<SearchResult, String>("duedate-none", getLabel("duedate-none")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
					Person person = (Person) result.getObject();
					User user = person.getProfile(UserProfile.class).getUser();
					UserWorkLoadData data = service.getUserWorkLoad(user);
					return new Model<String>(String.valueOf(data.due_none));
					
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null)
					return null;
				WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
				Person person = (Person) model.getObject().getObject();
				User user = person.getProfile(UserProfile.class).getUser();
				UserWorkLoadData data = service.getUserWorkLoad(user);
				String exp = data.due_none>0?"info":"";
				return "number-md "+ exp;
			}			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
			
		});

		// ----
		//
		// TODO: Make this Spring based
		// This is Hardwired for Windsor
		//
		// ----
		
		/**
		if (getDomain().getName()!=null && getDomain().getName().toLowerCase().equals(((WorkflowLoadService) getDomain().getService(WorkflowLoadService.class)).getWindsorDomainName())) {
		
				int current_year = OffsetDateTime.now().getYear();
				
				for (int index=0; index<7; index++) {
					
						final int year = current_year -3 + index;
						final int y_index = index;
						
						String suffix;
						
						if (index==0)
							suffix=" or Before";
						else if (index==6)
							suffix=" or After";
						else
							suffix="";
						
						this.columns.add(new GridColumn<SearchResult, String>("effective_date-"+String.valueOf(year), new Model<String>("Effective Date " + String.valueOf(year)  + suffix )) {
							private static final long serialVersionUID = 1L;
							@Override
							protected IModel<String> getLabelModel(SearchResult result) {
								if (result.getObject()==null) 
									return new Model<String>("err");
								try {
									WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
									Person person = (Person) result.getObject();
									User user = person.getProfile(UserProfile.class).getUser();
									UserWorkLoadData data = service.getUserWorkLoad(user);
									int val = data.effective[y_index].intValue();
									return new Model<String>(String.valueOf(val));
									
								} catch (Exception e) {
									logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
									return new Model<String>(e.getClass().getSimpleName());
								}
							}
							@Override
							protected String getContextKey() {
								return DashboardConsole.this.getName() + super.getContextKey();
							}
							
							@Override
							protected String getLabelCss(IModel<SearchResult> model) {
								
								if (model.getObject()==null)
									return null;
								try {
									WorkflowLoadService service = (WorkflowLoadService) getDomain().getService(WorkflowLoadService.class);
									Person person = (Person) model.getObject().getObject();
									User user = person.getProfile(UserProfile.class).getUser();
									UserWorkLoadData data = service.getUserWorkLoad(user);
									int val = data.effective[y_index].intValue();
									String exp = val>0?"info":"";
									return "number-md "+ exp;
								} catch (Exception e) {
									return "number-md error"; 
								}
							}
							
							@Override
							public String getCssClass() {
									return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
							}
							
							@Override
							public boolean isEscapeModelString() {
								return false;
							}
							@Override
							public boolean isPreferred() {
								return true;
							}
						});
				}
		
		
		}
	*/
		
		
		
		
		for (Classifier classifier: getDataSet().getClassifiers()) {
			this.columns.add(new GridColumn<SearchResult, String>(String.valueOf(classifier.getId()), new Model<String>(classifier.getName())) {
				private static final long serialVersionUID = 1L;
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						DataSetMember member = (DataSetMember) object.getObject();
						StringBuilder str = new StringBuilder();
						for (Classifier classifier:  member.getDataSet().getClassifiers()) {
							if (String.valueOf(classifier.getId()).equals(this.getId())) {
								for (Classification classification : ((Classificable) member).getClassification()) {
									if (classification.getClassifier().equals(classifier)) {
										if (str.length()>0)
											str.append(", ");
										str.append(classification.getDataSetMember().getDisplayName());
									}
								}
							}
						}
						return new Model<String>(str.toString());
					} catch (Exception e) {			
						logger.error(e);

						return new Model<String>(e.getClass().getSimpleName());
					}
				}
				@Override
				protected String getContextKey() {
					return DashboardConsole.this.getName() + super.getContextKey();
				}
				
				@Override
				public boolean isPreferred() {
					return false;
				}
			});
		}
		

		this.columns.add(new GridColumn<SearchResult, String>("status", getLabel("status")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					
					UserProfile userProfile = ((Person)result.getObject()).getProfile(UserProfile.class);
					User user = userProfile.getUser();
					
					if (user==null)
						return new Model<String>("err");
					
					return new StringResourceModel((user.isEnabled() ? "enabled" : "archived"), DashboardConsole.this, null);
			
				} catch (Exception e) {								
					logger.error(e);

					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			
			
			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				
				if (result==null)
					return new Model<String>("err");

				try {
							UserProfile userProfile = ((Person)result.getObject()).getProfile(UserProfile.class);
							User user = userProfile.getUser();
							
							if (user==null)
								return new Model<String>("err");
							
							return new Model<String>(user.isEnabled() ? "enabled" : "archived");
							
				} catch (Exception e) {
					logger.error(e);
						return new Model<String>(e.getClass().getName());
				}
			}
			
			@Override
			protected String getLabelCss() {
				return null;
			}
			@Override
			protected String getContextKey() {
				return DashboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public boolean isEscapeModelString() {
				return false;
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
		return this.columns;
	}
	
	
	
	protected Panel getNavigationPanel(long index) {
																													
		GlobalNavigationBar<Person> navigationbar = new GlobalNavigationBar<Person>("navigation",  getDisplayName().getObject()) {
		//GlobalNavigationBar<Person> navigationbar = new GlobalNavigationBar<Person>("navigation", getSearcher(), index, getDisplayName().getObject()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onNavigate(Person person) {
				//UserPage page = new UserPage(getModel(person));
				//setResponsePage(page);
				setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-myaccount-page"));
				
			}
			@Override
			public void onReturn() {
				setResponsePage(getConsolePage(getQuery(), -1));
			}
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), -1));
			}
		};
		
		navigationbar.setHasSearchPanel(false);
		navigationbar.setIsAlerts(false);
		navigationbar.setSearchPlaceHolder(new StringResourceModel("searchplaceholder", DashboardConsole.this, null).getString());
		
		return navigationbar;
	}
	
	
	@Override
	protected Panel getPanel(IModel<Person> model, List<String> snippets) {
		return new WorkLoadHitExpandedPanel("editor", model);
	}
	
	@Override
	protected Panel getPanel(IModel<Person> model) {
		return new WorkLoadHitExpandedPanel("editor", model);
	};
	
	
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<Person>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Person> event) {
				//MonitorQuery query = new MonitorQuery( getQueryIndex());
				Query query = new MonitorQuery( getQueryIndex());
				Serializable id = event.getModel().getObject().getProfile(UserProfile.class).getUser().getId();
				query.getParameters().put("workspace", id.toString());
				setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.MonitorPage, query));
			}
		});
	}
	
	
	
	
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Person> browser) {

		List<ToolbarItem> items = super.getToolbarItems(browser);
		
	       
			InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					InfoDialog infoDialog = (InfoDialog) getInformationModal();
					infoDialog.open(target,() -> {return DashboardConsole.this.getName();}, new Model<String>(DashboardConsole.this.getDescription()));
				}
				
				@Override
				public boolean isVisible() {
					return true;
				}
			};

			items.add(infoButton);
			
		return items;
	}


	// name of the downloadable file
	//
	@Override
	public String getDownloadFileName() {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("YYYY-MM-dd");
		String name = getDomain().getName().replace(" ","")+"-dashboard-"+df.format(OffsetDateTime.now());
		return name;
	}
	

	// name of the temporary local file
	//
	protected File getDownloadFile() throws IOException {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("YYYY-MM-dd");
		String name = getDomain().getName().replace(" ","")+"-dashboard-"+df.format(OffsetDateTime.now());
		File file = File.createTempFile(name, ".csv");
		org.apache.wicket.util.file.Files.writeTo(file, getDownloadStream());
		return file;
	}


	/**
	 * 
	 * Only Visible Columns
	 * @return
	 * 
	 */
	
	
	
	/**
	 * 
	 * Only Visible Columns
	 * @return
	 * 
	 */
	protected InputStream getDownloadStream() {
		StringBuffer filebuffer = new StringBuffer();
		ResultSet resulSet = getQuery().execute();
		int c = 0;
		for (GridColumn<SearchResult, String> column : getColumns()) {
			if (column.isVisible()) {
				if (c++>0) 
					filebuffer.append(",");
				String s=(column.getDisplayModel().getObject()!=null?column.getDisplayModel().getObject().replace(",", ""):"");
				filebuffer.append(s);
			}
		}
		
		filebuffer.append("\r\n");
		while (resulSet.hasNext()) {
			SearchResult result = resulSet.next();
			c=0;
			for (GridColumn<SearchResult, String> column : getColumns()) {
					if (column.isVisible()) {
						if (c++>0) 
							filebuffer.append(",");
						if (column instanceof ReportColumn) {
							if (((ReportColumn) column).getValueModel(result).getObject()!=null) 
								filebuffer.append(((ReportColumn) column).getValueModel(result).getObject().replace(",", ""));
							else
								filebuffer.append("");
						}
						else {
							String s= ( (column.getCellAsString(result).getObject()!=null)? (column.getCellAsString(result).getObject().replace( ",", "")):"");
							filebuffer.append(s) ;
						}
					}
			}
			filebuffer.append("\r\n");
		}
		InputStream stream = new ByteArrayInputStream(filebuffer.toString().getBytes());
		return stream;
	}
	
	

	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}

	protected String getSectionDisplayName(String key) {
		return new StringResourceModel(key, DashboardConsole.this, null).getString();
	}

	
	// protected abstract Page getConsolePage(Query query, long index);

}
