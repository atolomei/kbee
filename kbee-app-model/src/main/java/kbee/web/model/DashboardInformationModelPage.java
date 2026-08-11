package kbee.web.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.XArray;
import com.novamens.workflow.Task;

import kbee.util.NumberFormatter;

import kbee.web.dashboard.DashboardPage;
import kbee.web.dashboard.DashboardWidgetSimpleWrapperPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.help.InlineHelpWebService;
import kbee.web.model.contentclass.ContentTemplatesPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.panel.AlertPanel;
import kbee.web.panel.ListSimplePanel;
import kbee.web.workflow.task.TaskPage;




/**
 * 
 * Mondelo de Información
 * At
 * Ds
 * Cl
 * 
 * 
 * 
 * Plantillas de Contenido
 * Procesos de Trabajo
 * Formularios
 * 
 */

public class DashboardInformationModelPage extends DashboardPage<Person> {
			
	private static final long serialVersionUID = 1L;

	static final public String PROPERTY_UNREAD = "unread";
	static final String KEY = "model-home";

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardInformationModelPage.class.getName());

	
	final boolean is_root=ServiceLocator.getService(SecurityService.class).isRoot();
	
	final boolean role_model 		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_model_read 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
	final boolean role_support 		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	
	final boolean role_dataset_values 		= is_domain_admin 		|| ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_values_read 	= role_dataset_values 	|| ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	
	private List<IModel<XArray>> list_ef;
	private List<IModel<XArray>> list_bp;
	private List<IModel<XArray>> list_ct;
	private List<IModel<XArray>> list;


	
	public DashboardInformationModelPage() {
		add(new RefreshBehavior());
	}
	
	public void onDetach() {
		super.onDetach();

		if (list_ef!=null)
			list_ef.forEach(item-> item.detach());

		if (list_bp!=null)
			list_bp.forEach(item-> item.detach());

		if (list_ct!=null)
			list_ct.forEach(item-> item.detach());

		if (list!=null)
			list.forEach(item-> item.detach());
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.MINI_SITE;
	}
	
	
	public void addListeners() {
		super.addListeners();
		

		add(new WicketEventListener<GeneralWicketEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(GeneralWicketEvent event) {
				logger.debug( event.getName());
			}
		});
		
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
		
		if (role_model_read && !(role_model || is_domain_admin)) 
			setAlertPanel( new AlertPanel<>("alert", AlertPanel.INFO, null, getLabel("readonly-title"), getLabel("readonly-text")));
	}

	protected Library getLibrary() {
		Library library = getDomain().getService(LibraryService.class).getDefault();
		return  library;
	}
	
	protected void addWidgets() {
		addWidget(new ListView<WidgetFactory>("widget-left", getLeftSectionsPanels()) {
			private static final long serialVersionUID = 1L;
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});
		
		addWidget(new ListView<WidgetFactory>("widget-center", getCenterSectionsPanels()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
		addWidget(new ListView<WidgetFactory>("widget-right", getRightSectionsPanels()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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

	
	public IModel<String> getTitle() {
		return new StringResourceModel("bc.informationmodel", this, null);
	}
	
	
	
	protected Panel getBreadcrumbPanel() {
		try {
			return new InformationModelBCPanel("bc.informationmodel");
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}

	
	@Override
	public boolean hasPermissions() {
		
		if (getDomain()==null)
			return false;
		
		return is_domain_admin || is_root || role_support || role_model || role_model_read; 
	}
	
	/** -----------------------------------------------
	 * 1st LEFT
	 */
	private List<WidgetFactory> getLeftSectionsPanels() {
		
		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
		
		widgets.add(new WidgetFactory() {
			private static final long serialVersionUID = 1L;
			public MarkupContainer getWidget(String id) {
		
				ListSimplePanel<XArray> pa = new ListSimplePanel<XArray>("payload", null, getInformationModelItems()) {

					private static final long serialVersionUID = 1L;
					@Override
					protected void onClick(IModel<XArray> modelObject, int index) {
						setResponsePage(new RedirectPage("/model/"+ modelObject.getObject().getKey()));
					}
					@Override
					protected IModel<String> getItemLabelMeta(IModel<XArray> modelObject) {
						return new Model<String>(" ("+ modelObject.getObject().getQuantity()+")");
					}
				};

				
				DashboardWidgetSimpleWrapperPanel<Person> mo = new DashboardWidgetSimpleWrapperPanel<Person>(id, getModel(), pa, DashboardInformationModelPage.KEY);
				mo.setHelpKey(InlineHelpWebService.HOME_MODEL_ELEMENTS);		
				mo.setTitle(DashboardInformationModelPage.this.getLabel("bc.informationmodel"));

				
				
				/**
				LinkCellItem<Person> notes_l=new LinkCellItem<Person>("item", new ObjectModel<Person>(getPerson()), bc.informationmodel.this.getLabel("mainmenu.mynotes")) {
					@Override
					public void onClick() {
						setResponsePage(new UserNotesPage());
					}
				};
				List<Panel> l_p =new ArrayList<Panel>();
				l_p.add(notes_l);
				DashboardSimpleBottomPanel db =new DashboardSimpleBottomPanel("base-bottom", l_p); 	
				wr.setBottomPanel(db);
				**/

				
				return mo;
			}
			public IModel<String> getLabel() {
				return  DashboardInformationModelPage.this.getLabel("bc.informationmodel");
			}
		});
		

		widgets.add(new WidgetFactory() {
			private static final long serialVersionUID = 1L;
			public MarkupContainer getWidget(String id) {

				ListSimplePanel<XArray> pa = new ListSimplePanel<XArray>("payload", null, getTemplateItems()) {
					
					private static final long serialVersionUID = 1L;
					
					protected void onClick(IModel<XArray> modelObject, int index) {
						setResponsePage(new RedirectPage(modelObject.getObject().getUrl()));
						
					}
					
					protected IModel<String> getItemLabel(IModel<XArray> modelObject) {
						return new Model<String>(modelObject.getObject().getDisplayName());
					}
					
					
				};
				DashboardWidgetSimpleWrapperPanel<Person> mo = new DashboardWidgetSimpleWrapperPanel<Person>(id, getModel(), pa, DashboardInformationModelPage.KEY) {
							/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

							@Override
							protected void onTitleClick() {
									setResponsePage( new ContentTemplatesPage());
								
							}
				};
				
				
				
				mo.setHelpKey(InlineHelpWebService.HOME_MODEL_TEMPLATES);		
				mo.setTitle(DashboardInformationModelPage.this.getLabel("contenttemplates"));
				
				
				return mo;
			}
			public IModel<String> getLabel() {
				return  DashboardInformationModelPage.this.getLabel("contenttemplates");
			}
		});
		

		
		
		return widgets;
	}

	
	
	
	
	
	
	
	
	
	
	
	/**
 	 * 1st CENTER
	 * @return
	 */
	private List<WidgetFactory> getCenterSectionsPanels() {
		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
		

		widgets.add(new WidgetFactory() {
			private static final long serialVersionUID = 1L;
			public MarkupContainer getWidget(String id) {
		
				ListSimplePanel<XArray> pa = new ListSimplePanel<XArray>("payload", null, getBPItems()) {
					private static final long serialVersionUID = 1L;
					protected void onClick(IModel<XArray> modelObject, int index) {
						setResponsePage(new RedirectPage(modelObject.getObject().getUrl()));
					}
					protected IModel<String> getItemAbstract(IModel<XArray> modelObject) {
						return new Model<String>(modelObject.getObject().getDescription());
					}
					protected IModel<String> getItemLabel(IModel<XArray> modelObject) {
						return new Model<String>(modelObject.getObject().getDisplayName());
					}
				};
				DashboardWidgetSimpleWrapperPanel<Person> mo = new DashboardWidgetSimpleWrapperPanel<Person>(id, getModel(), pa, DashboardInformationModelPage.KEY);
				mo.setHelpKey(InlineHelpWebService.HOME_MODEL_PROCESS);		
				mo.setTitle(DashboardInformationModelPage.this.getLabel("procedure"));
				
				
				
				pa.setAllExpanded(true);
				
				return mo;
				
			}
			public IModel<String> getLabel() {
				return  DashboardInformationModelPage.this.getLabel("bc.informationmodel");
			}
		});

		
							
		return widgets;
	}
	
	
	
	
	
	/**
	 * Entity datasets
	 *  
	 * @return
	 */
	/**
	public List<IModel<DataSet>> getDataSets() {
		if (entitiessets==null) {
			entitiessets = new ArrayList<IModel<DataSet>>();
			List<DataSet> list = getDomain().getService(DomainService.class).getEntitySets();
			for (DataSet ds : list) 
				if (hasRole(ds))
					entitiessets.add( new ObjectModel<DataSet>(ds));
		}
		return entitiessets;
	}
	*/
	/**
	 * 
 	 * 1st RIGHT

	 * @return
	 */
	private List<WidgetFactory> getRightSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();

		widgets.add(new WidgetFactory() {
			private static final long serialVersionUID = 1L;
			public MarkupContainer getWidget(String id) {
				ListSimplePanel<XArray> ef = new ListSimplePanel<XArray>("payload", null, geteFormsItems()) {
					private static final long serialVersionUID = 1L;
					protected void onClick(IModel<XArray> modelObject, int index) {
						setResponsePage(new RedirectPage(modelObject.getObject().getUrl()));
					}
				
					protected IModel<String> getItemAbstract(IModel<XArray> modelObject) {
						return new Model<String>(modelObject.getObject().getDescription());
					}
					
					protected IModel<String> getItemLabel(IModel<XArray> modelObject) {
						return new Model<String>(modelObject.getObject().getDisplayName());
					}
				};
				
			
				DashboardWidgetSimpleWrapperPanel<Person> mo = new DashboardWidgetSimpleWrapperPanel<Person>(id, getModel(), ef, DashboardInformationModelPage.KEY);
				mo.setHelpKey(InlineHelpWebService.HOME_MODEL_EFORMS);		
				
				ef.setAllExpanded(true);
				
				mo.setTitle(DashboardInformationModelPage.this.getLabel("eforms"));
				return mo;
			}
			public IModel<String> getLabel() {
				return  DashboardInformationModelPage.this.getLabel("eforms");
			}
		});

		return widgets;
	}
	
	
	/**
	 * @return
	 */
	public List<IModel<XArray>> getInformationModelItems() {
		
		if (list!=null)
			return list;
		
		list = new ArrayList<IModel<XArray>>();

		
		Locale locale = getSessionUser().getLocale();
		
		XArray da = new XArray(	getLabel("bc.datasets").getObject(), 
								getLabel("bc.datasets").getObject(), 
								NumberFormatter.formatNumber(getContentDao().getTotalDatasets(getDomain()), locale), 
								null, 
								"datasets",
								null);
		
		
		XArray cl = new XArray(	getLabel("bc.classifiers").getObject(), 
								getLabel("bc.classifiers").getObject(),
								NumberFormatter.formatNumber(getContentDao().getTotalClassifiers(getDomain()), locale), 
								null, 
								"classifiers",
								null);
		
		
		XArray at = new XArray(	getLabel("bc.attributes").getObject(), 
								getLabel("bc.attributes").getObject(),
								NumberFormatter.formatNumber(getContentDao().getTotalAttributes(getDomain()), locale), 
								null,
								"attributes",
								null);
		
		
		XArray vt = new XArray(	getLabel("bc.contentclasses").getObject(), 
								getLabel("bc.contentclasses").getObject(),
								NumberFormatter.formatNumber(getContentDao().getTotalContentTemplates(getDomain()), locale), 
								null, 
								"contentclasses",
								null);
		
		list.add(new Model<XArray>(da));
		list.add(new Model<XArray>(cl));
		list.add(new Model<XArray>(at));
		list.add(new Model<XArray>(vt));
		
		
				
		
		XArray lg = new XArray(	getLabel("bc.launchergroups").getObject(), 
								getLabel("bc.launchergroups").getObject(), 
								NumberFormatter.formatNumber(getRepository(LauncherGroup.class).getTotal(getDomain()), locale), 
								null, 
								"launchergroups",
								null);

		
		
		
		XArray rt = new XArray(	getLabel("bc.resourcetags").getObject(),	
									getLabel("bc.resourcetags").getObject(),
									NumberFormatter.formatNumber(getRepository(ResourceTag.class).getTotal(getDomain()), locale), 
									null, 
									"resourcetags",
									null);
		
		list.add(new Model<XArray>(lg));
		list.add(new Model<XArray>(rt));
		
		list.sort(new Comparator<IModel<XArray>>() {
			@Override
			public int compare(IModel<XArray> a, IModel<XArray> b) {
				try {
					return a.getObject().getSortLabel().compareToIgnoreCase(b.getObject().getSortLabel());
				} 
				catch (Exception e) {
					return 0;	
				}
			}
		});
		
		
		list.forEach(item -> logger.debug(item.getObject().toString()));
		
		
		return list;
	}

	/**
	 * 
	 * 
	 * 
	 * @return
	 */
	public List<IModel<XArray>> getTemplateItems() {
		
		if (list_ct!=null)
			return list_ct;
		
		list_ct = new ArrayList<IModel<XArray>>();
		
		for (ContentTemplate con: getContentDao().getContentTemplates(getDomain())) {
		
			if (con.getState()==ObjectState.ENABLED) {
				
				String name   = con.getDisplayName();
				StringBuilder str = new StringBuilder();
				
				if (con.isOnlyRootEdit())
					name=name + " ( <span class=\"ago\"> " +  new StringResourceModel("system", this, null).getObject()+" </span> )";
				
					str.append(name);
				
				XArray da= new XArray(    name,
										  name,
										  "",
										  "",
										  con.getId().toString(),
										  getServerUrl()+"/model/contentclass/"+ con.getId().toString()
									);
				
				list_ct.add(new Model<XArray>(da));
			}
			
		}
		
		
		list_ct.sort(new Comparator<IModel<XArray>>() {
			@Override
			public int compare(IModel<XArray> a, IModel<XArray> b) {
				try {
					return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
				} catch (Exception e) {
					return 0;	
				}
			}
		});
		

		
		list_ct.forEach(item -> logger.debug(item.getObject().toString()));
				
		return list_ct;
	}
	

	
	
	
	/**
	 * 
	 * 
	 * @return
	 */
	public List<IModel<XArray>> geteFormsItems() {
		
		if (list_ef!=null)
			return list_ef;
		
		list_ef = new ArrayList<IModel<XArray>>();
		
		
		for (ContentTemplate con: getContentDao().getContentTemplates(getDomain())) {

			if ((con.getState()==ObjectState.ENABLED) && (con.getForms()!=null) ) {
		
				for (EForm e: con.getForms()) {
					if (e!=null ) {
						String eform_id; 
						if (e instanceof Identifiable)
							eform_id= ((Identifiable) e).getId().toString();
						else
							eform_id=e.getName();
						String eform_name   = e.getDisplayName();
						StringBuilder str = new StringBuilder();
						str.append(eform_name);
						XArray da= new XArray(    str.toString(),  
												  str.toString(),
												  "",
												  new StringResourceModel("content-template", this, null).getObject() + ". " + con.getDisplayName() + "<br/>  " + 
												  
													new StringResourceModel("display-level", this, null).getObject()
														  + ". " +
													e.getFormAccessLevel().getLabel( getSessionUser().getLocale()), 
												  eform_id,
												  "/eform/"+ con.getId().toString()+"/" + eform_id
											);
						list_ef.add(new Model<XArray>(da));
					}
				}
			}
		}
		
		
		list_ef.sort(new Comparator<IModel<XArray>>() {
			@Override
			public int compare(IModel<XArray> a, IModel<XArray> b) {
				try {					
					return a.getObject().getSortLabel().compareToIgnoreCase(b.getObject().getSortLabel());
				} catch (Exception e) {
					return 0;	
				}
			}
		});
		
		return list_ef;
	}
	
	private List<ProcessLauncher> getLaunchers() {
		return getDomain().getService(WorkflowDomainService.class).getLaunchers();
	}
	

	public List<IModel<XArray>> getBPItems() {
		
		if (list_bp!=null)
			return list_bp;
		
		list_bp = new ArrayList<IModel<XArray>>();
		
		List<ProcessLauncher> p_list = getLaunchers();

		for (ProcessLauncher p: p_list) {
		
			if (p.getContentTemplate()!=null && p.getContentTemplate().getState()==ObjectState.ENABLED) {
				String id = p.getProcedure()!=null? p.getProcedure().getId().toString() :"#";
				String lau= p.getId().toString(); 

				/**
				XArray da= new XArray(   
						"<span class=\"predicate\">"+p.getContentTemplate().getDisplayName()
						+"</span><span class=\"ago\"> - </span><span> " + p.getDisplayName()+"</span>",
						 p.getLabel(),
						 "",
						 p.getProcedure()!=null?p.getProcedure().getDescription():"",
						p.getId().toString(),
						"/model/procedure/"+id+"/"+lau
						
						);
				**/
				XArray da= new XArray(   
						p.getDisplayName(),
						p.getLabel(),
						"",
						new StringResourceModel("content-template", this, null).getObject() + ". " + p.getContentTemplate().getDisplayName() +
						(p.getDescription()!=null? ( " <br/> " + p.getDescription()) : ""), 
						  
						
						
						p.getId().toString(),
						"/model/procedure/"+id
//						"/model/procedure/"+id+"/"+lau
						
						);
				
				
				
				list_bp.add(new Model<XArray>(da));
			}
		}
		
		
		list_bp.sort(new Comparator<IModel<XArray>>() {
			@Override
			public int compare(IModel<XArray> a, IModel<XArray> b) {
				try {
					return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
				} catch (Exception e) {
					return 0;	
				}
			}
		});
		
		return list_bp;
	}




}
