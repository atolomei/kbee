package kbee.web.idoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxPreventSubmitBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.form.EForm;
import com.novamens.content.model.Classification;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.web.content.markup.CustomAttributesPanel;
import com.novamens.content.web.workflow.markup.ResolutionPanel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTakeTaskEvent;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.KbeeWorkflowEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.event.RemoveLabelEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.PropertiesFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.SeparatorTabKB;
import com.novamens.wicket.markup.html.tabs.TitleTabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.DueDateAction;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;
import kbee.web.console.grid.LabelSetPanel;
import kbee.web.content.editor.ClassificationPanel;
import kbee.web.content.eform.ContentFormEditor;
import kbee.web.content.panel.ContentLinksPanel;
import kbee.web.content.workflow.EOpenFormEvent;
import kbee.web.content.workflow.TaskHomePanel;
import kbee.web.content.workflow.TaskKnowledgeBasePanelV6;
import kbee.web.content.workflow.TaskPanel;
import kbee.web.content.workflow.TaskRelatedFilesPanelv6;
import kbee.web.content.workflow.TaskResolutionPanel;
import kbee.web.content.workflow.TaskToolbarEmbeddedPanel;
import kbee.web.content.workflow.TitlePanel;
import kbee.web.eform.EAjaxFormEvent;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.nav.NavigatorPanelV6;
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.panel.ClickItemEvent;
import kbee.web.workflow.ClickHideShowWorkflowEvent;
import kbee.web.workflow.ProcessHistoryPanel;
import kbee.web.workflow.ProgressNotesPanel;
import kbee.web.workflow.TaskCommentPanel;
import kbee.web.workflow.TaskTempPanel;
import kbee.web.workflow.task.ActionEvent;
import kbee.web.workflow.task.EFormEvent;
import kbee.web.workflow.task.PageTaskToolbar;
import kbee.web.workflow.task.ValidationEvent;

/**
 *  <p> 
 *  1- Workflow Action 		-> if Content stays/goes to the User -> Same Content -> otherwise Next in the ResultSet | Previous if there is no next | Home if there is no previous
 *  2- Take 				-> Stays in the content
 *  4- ReAssign To Me 		-> Stays in the content
 *  4- ReAssign 			->  if Content stays/goes to the User -> Same Content ->otherwise Next in the ResultSet, Previous if there is no next, Home if there is no previous
 *  3- Delete 				->  Next in the ResultSet, Previous if there is no next, Home if there is no previous
 *  </p>
 */
@SuppressWarnings("serial")
public class IDocTaskPanelV6 extends TaskPanel<IDoc> implements PageMainTabs  {
	private static final long serialVersionUID = 1L;
	
	static final boolean PUBLIC_AREA = true;
	
	private static Logger logger = Logger.getLogger(IDocTaskPanelV6.class.getName());
	
	private Panel taskPanel = null;
	private TaskResolutionPanel<IDoc> taskActionsPanel = null; 
	private String initial_tab;
	private VerticalLayout<ITab> layout;
	private Form<?> form;
	private PageContentHeaderPanel<IDoc> header;
	private PageTaskToolbar<IDoc> toolbar;
	private NavigatorPanelV6<Content> navigation_panel;
	
	private static String onEndTask =
		PropertiesFactory
			.getInstance("kbee")
			.getProperties()
			.getProperty("kbee.user.behavior.onendtask", "navigate");

	public static class FormModelUpdateVisitor implements IVisitor<Component, Void> {
		@Override
		public void component(final Component component, final IVisit<Void> visit) {
			if (component instanceof IFormModelUpdateListener) {
				((IFormModelUpdateListener)component).updateModel();
			}
		}
	}

	/** ----
	 * Toolbar
	 * resolution
	 * eform_1
	 * eform_2
	 * ...
	 * process history
	 * task
	 */
	public IDocTaskPanelV6(IModel<WorkflowContext> workflowmodel) {
		super(workflowmodel);
		setOutputMarkupId(true);
		this.taskActionsPanel = null;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setIsNew(getWorkflowModel().getObject().getProcess().getActivities().size()<=1);
		load();
	}
	
		
	public void load() {

		form = new Form<Void>("form", Disposition.VERTICAL) {
			public void process(IFormSubmitter submittingComponent) {
				super.process(submittingComponent);
			}
		};
		addOrReplace(form);
		
		
		// Header -----------------
		this.header = new PageContentHeaderPanel<IDoc>("header", getModel());
		Panel bc = new MenuBreadCrumbPanel<>();
		try {
			this.header.setTitle(getModel().getObject().getDisplayName());
			this.header.setTitle(new TitlePanel<IDoc>("titlepanel") {
				@Override
				public boolean isLabelsEnabled() {
					return true;
				}
			});
			this.header.setSubLine(new Model<String>(getModelObject().getService(ContentService.class).getConsoleSubtitleDefaultIfNull()));
			bc = getBreadCrumb();
		} 
		catch (Exception e) {
			logger.error(e);
		}
		
		header.setBreadcrumbPanel(bc);
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		try {
			r_list.add(new TaskToolbarEmbeddedPanel<IDoc>("panel", getWorkflowModel()));
			
		} catch (Exception e) {
			logger.error(e);
			r_list.add(new ErrorPanel("panel", e));
		}

		try {
			if (getPage() instanceof NavigablePage<?>) {
				@SuppressWarnings("unchecked")
				NavigablePage<Content> page = ((NavigablePage<Content>)getPage());
				Navigator<Content> navigator = page.getNavigator();
				if (navigator!=null) {
					navigation_panel = new NavigatorPanelV6<Content>("panel", navigator, Content.class);
					navigation_panel.setResultsPanel(true);
					r_list.add(navigation_panel);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			r_list.add(new ErrorPanel("panel", e));
		}

		try {
			if (getTask().isEnableLabels()) {
				LabelSetPanel<IDoc> labelset = new LabelSetPanel<IDoc>("panel", getModel(), 
						isUserWorkspace(), 		// is_remove_enabled 
						false, 					// is_label_list -> ACA VA SIEMPRE FALSE PORQUE LA LISTA SE MUESTRA AHORA EN EL TITLEPANEL
						isUserWorkspace());   	// is_dropdownmenu	  
				l_list.add(labelset);
			}
		} 
		catch (Exception e) {
			logger.error(e);
			l_list.add(new ErrorPanel("panel", e));
		}
		toolbar = new PageTaskToolbar<IDoc>("toolbar", getModel(), l_list, r_list);
		header.setToolbarPanel(toolbar);
		
		form.add(header);
		
		
		// Billboard  -----------------
		//
		form.add(getBillboard());


		
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		/** --------------------------------
		 * Task Resolution
		 */
		if (getTaskResolution()!=null) {
			tabs.add(new AbstractTabKB( new Model<String>(getTaskResolution().getResolutionTitle()), "letter") {
				@Override
				public Panel getPanel(String panelId) {
					return new ResolutionPanel(panelId, getWorkflowModel());
				}
			});
		}
		
		
		/** --------------------------------
		 Content Structure - eForms
		 */
		tabs.add(new TitleTabKB("structure", new StringResourceModel("content"),  "title-no-nav"));
		for (EForm eform : getForms()) {
			try {
				addEditor(new ContentFormEditor<IDoc>(getContent().getFormData(eform), this) {
					@Override
					public boolean isSignatureRequired() {
						return IDocTaskPanelV6.this.isSignatureRequired(eform);
					}
				});
				
				tabs.add(new AbstractTabKB(new Model<String>(eform.getDisplayName()), eform.getName()) {
					@Override
					public Panel getPanel(String panelId) {
						return (Panel)getEditor(eform);
					}
				});
			}
			catch (Throwable e) {
				logger.error(e);
				IModel<String> title = eform!=null? new Model<String>(eform.getDisplayName()): new Model<String>("null");
				 tabs.add(new AbstractTabKB(title, title.getObject()) {
					@Override
					public Panel getPanel(String panelId) {
						String message = e.getCause()!=null ? e.getCause().getMessage() : e.getMessage();
						return new ErrorPanel(panelId, new Model<String>("Form Error"), new Model<String>(message));
					}
				});
			}
		}

		


		/** --------------------------------
			Task Actions
		 */
		tabs.add(new SeparatorTabKB("separator-actions"));
		tabs.add(new TitleTabKB("notes", getLabel("editor.workflow"),  "title-no-nav"));
		tabs.add(new AbstractTabKB(getLabel("editor.actions"), "actions") {
			@Override
			public Panel getPanel(String panelId) {
				try {
					if (taskActionsPanel == null) {
						taskActionsPanel =  new TaskResolutionPanel<IDoc>(panelId, getWorkflowModel());
					}
					return taskActionsPanel;
				} 
				catch (Exception e) {
					logger.error(e);
					return new ErrorPanel(panelId, e);
				}
 			}
		});

		if(getTask()!=null && getTask().getRelatedCriteria()!=null) {
			tabs.add(new AbstractTab(new StringResourceModel("tab.related", this, null)) {
				@Override
				public Panel getPanel(String panelId) {
					return new TaskRelatedFilesPanelv6<IDoc>(panelId, getWorkflowModel());
				}
			});
		}
		
		if(getTask()!=null && getTask().getKnowledgeCriteria()!=null) {
			tabs.add(new AbstractTabKB(getLabel("tab.kbase"), "kbase") {
				@Override
				public Panel getPanel(String panelId) {
					return new TaskKnowledgeBasePanelV6<IDoc>(panelId, getWorkflowModel());
				}
			});
		}
		
		/** --------------------------------
			Custom Attributes
		 */
		if (getModel().getObject().getContentTemplate().isCustomAttributes()) {
			tabs.add(new AbstractTabKB(new Model<String>(getModel().getObject().getContentTemplate().getCustomattributes_label()), "customtags" ) {
				@Override
				public Panel getPanel(String panelId) {
					return new CustomAttributesPanel<IDoc>(panelId, getModel());
				}
			});
		}
		
		
		/** --------------------------------
		 *	Support
		 */
		
		boolean isTitleSupport = getTask().isEnableProgressNotes() || getModel().getObject().getContentTemplate().isInstanceTimeBasedNotification();
		
		if (isTitleSupport) {
			tabs.add(new SeparatorTabKB("separator-notes"));
			tabs.add(new TitleTabKB("support", getLabel("editor.supporting"),  "title-no-nav"));
		}
		
		/** --------------------------------
		 *	Progress Notes
		 */
		if (getTask().isEnableProgressNotes()) {
			tabs.add(new AbstractTabKB(getLabel("editor.progressnotes"), "notes") {
				@Override
				public Panel getPanel(String panelId) {
					ProgressNotesPanel<IDoc> panel = new ProgressNotesPanel<IDoc>(panelId, getWorkflowModel(),  IDocTaskPanelV6.this.getModel());
					panel.setEditionEnabled(true);
					return panel;
				}
			});
		}

		/** --------------------------------
		 *	Reminders and Timed Alerts
		 */
		if (getModel().getObject().getContentTemplate().isInstanceTimeBasedNotification()) {
			tabs.add(new AbstractTabKB(getLabel("editor.time-based-notifications"), "timedalerts") {
				@Override
				public Panel getPanel(String panelId) {
					ContentReminderPanel<IDoc> panel = new  ContentReminderPanel<IDoc>(panelId, IDocTaskPanelV6.this.getModel());
					return panel;
				}
			});
		}
		
		
		
		
		
		
		tabs.add(new SeparatorTabKB("separator-audit"));
		tabs.add(new TitleTabKB("audit", getLabel("audit"),  "title-no-nav"));
		
	
		/** --------------------------------
		 * 	Links
		 */
		if (getContent().getService(ContentService.class).getText()!=null) {
			tabs.add(new AbstractTabKB(getLabel("editor.links"), "links") {
				@Override
				public Panel getPanel(String panelId) {
					return new ContentLinksPanel<IDoc>(panelId, new ObjectModel<IDoc>((IDoc)getContent()));
				}
			});
		}
		
		/** file info --------------------------------
		 */	 
		tabs.add(new AbstractTabKB(getLabel("file-info"), "info") {
			@Override
			public Panel getPanel(String panelId) {
				try {
					if (taskPanel == null)
						taskPanel =  new TaskHomePanel<IDoc>(panelId, getWorkflowModel());
					return taskPanel;
				} 
				catch (Exception e) {
					logger.error(e);
					return new ErrorPanel(panelId, e);
				}
			}
		});
		
		/** --------------------------------
			ProcessHistory
		*/ 
		tabs.add(new AbstractTabKB(getLabel("editor.history"), "history") {
			@Override
			public Panel getPanel(String panelId) {
				return new ProcessHistoryPanel<IDoc>(panelId, getWorkflowModel(),  IDocTaskPanelV6.this.getModel());
			}
		});
		
		
		this.layout  = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				IDocTaskPanelV6.this.update(target);
			}
			@Override
			protected WebMarkupContainer newLink(final String linkId, final int index) {
				return new AjaxSubmitLink(linkId) {
					@Override
					public void onSubmit(AjaxRequestTarget target) 	{
						setSelectedTab(index);
						target.add(layout);
						onAjaxUpdate(target);
					}
					@Override
				    protected void onError(AjaxRequestTarget target) {
						target.add(layout);
						target.add(form);
						onAjaxUpdate(target);
					}
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						IAjaxCallListener listener = new IAjaxCallListener() {
						};
						attributes.getAjaxCallListeners().add(listener);
					}
				};
			}
		};
		
		layout.setContentTopPanel( getProcessChartPanel("content-top-panel"));
		layout.setTitle(getLabel("sections"));
		layout.setSections(VerticalLayout.COLS_9X3);
		TaskTempPanel<IDoc> tmp;
		
		/**  --------------------------
		 * Due Date
		 */
		if (isOverdue()) {
			
			tmp = 		new TaskTempPanel<IDoc>("header-bottom-panel", getModel(), 
						new TaskCommentPanel("panel1", getWorkflowModel()), 
						getOverduePanel("panel2"));
		}
		else {
			tmp = new TaskTempPanel<IDoc>("header-bottom-panel", 
					getModel(),
					new TaskCommentPanel("panel1", getWorkflowModel()), 
					null);
		}
			
		
		layout.setHeaderBottomPanel(tmp);

											
		List<MenuItemFactory<Panel>> menu_items = new ArrayList<MenuItemFactory<Panel>>();
		
		menu_items.add(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new AjaxMenuItemPanelV5<Panel>(id) {
					@Override 
					public String getLabel() {
						return new StringResourceModel("show-workflow", IDocTaskPanelV6.this, null).getObject();
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						try {
							fire(new ClickHideShowWorkflowEvent(target));
						} 
						catch (Exception e) {
							logger.error(e);	
						}
					}
				};
			}
		});
	 
		
		

		menu_items.add(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new AjaxMenuItemPanelV5<Panel>(id) {
					@Override
					public boolean isVisible() {
						Content content=IDocTaskPanelV6.this.getModelObject();
						return !content.getService(ContentSubscriptionService.class).isSubscribed(getPerson());	
					}
					
					@Override 
					public String getLabel() {
						return new StringResourceModel("subscribe", IDocTaskPanelV6.this, null).getObject();
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {

						try {
							Content content=IDocTaskPanelV6.this.getModelObject();
							content.getService(ContentSubscriptionService.class).subscribe(getPerson());
							target.add(IDocTaskPanelV6.this);
							FeedbackHelper.showInfoToast("OK");
						} 
						catch (Exception e) {
							logger.error(e);
							FeedbackHelper.showErrorToast(e.toString());
						}
					}
				};
			}
		});
		
		

		menu_items.add(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new AjaxMenuItemPanelV5<Panel>(id) {
					@Override
					public boolean isVisible() {
						Content content=IDocTaskPanelV6.this.getModelObject();
						return content.getService(ContentSubscriptionService.class).isSubscribed(getPerson());	
					}
					
					@Override 
					public String getLabel() {
						return new StringResourceModel("unsubscribe", this, null).getObject();
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {

						try {
							Content content=IDocTaskPanelV6.this.getModelObject();
							content.getService(ContentSubscriptionService.class).unsubscribe(getPerson());
							target.add(IDocTaskPanelV6.this);
							FeedbackHelper.showInfoToast("OK");
						} 
						catch (Exception e) {
							logger.error(e);
							FeedbackHelper.showErrorToast(e.toString());
						}
					}
				};
			}
		});

		
		layout.setMenuItemFactory(menu_items);
		

		form.add(layout);
		form.add(new AjaxPreventSubmitBehavior());
		
	}

	@Override
	public void update(boolean auto) {
		try {
			if (!getUpdatedFields().isEmpty()) {
				((TitlePanel<?>)header.get("header-container:header-internal-container:titlepanel")).updateModel();
				for (ClassificationPanel<?> editor : getEditors()) {
					if (editor instanceof ContentFormEditor) {
						ContentFormEditor<?> contenteditor = ((ContentFormEditor<?>)editor);
						contenteditor.updateModel();
					}
				}
				getModelObject().getService(ContentService.class).updateFields(getUpdatedFields());
				if (DueDateAction.CALCULATE_ON_UPDATE.equals(getTask().getDuedateAction())) {
					getModelObject().getService(WorkflowService.class).updateDueDate();
				}
				super.reset();
			}
		}
		catch (Exception e) {
			LogManager.getLogger(IDocTaskPanelV6.class.getName()).error(e.getClass().getName());
			throw new KbeeRuntimeException(e);
		}
	}
	
	@Override
	public void update(AjaxRequestTarget target, boolean auto) {
		// lock
		if (validateUser()) {
			update(false);
		}
		else {
			setEditionEnabled(false);
			//target.add(this);
		}	
	}
	
	public VerticalLayout<ITab> getLayout() {
		return layout;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (getInitialTab()!=null)
			layout.setSelectedTab(getInitialTab());
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickItemEvent<Content>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickItemEvent<Content> event) {
				try {
						logger.debug(event.toString());
						Content content = event.getModel().getObject();
		
						if (!(content instanceof IDoc)) {
							logger.error("--------------------------------\n TBA ASSUMES CONTENT IS IDOC --------------------------------\n");
							return;
						}
							
						// TBA ASSUMES CONTENT IS IDOC !!!
						//
						if (content.getWorkspace()!=null) {
							IDocTaskPageV6 page = new IDocTaskPageV6(content.getService(WorkflowService.class).getContext(), false);
							Navigator<Content> navigator=navigation_panel.getNavigator();
							navigator.getCursor().setIndex(event.getIndex());
							page.setNavigator(navigator);
							setResponsePage(page);
							getPage().detach();
							return;
						}
						else {
							IDocPageV6 page = new IDocPageV6( new ObjectModel<IDoc>( (IDoc) content));
							page.setNavigator(navigation_panel.getNavigator());
							setResponsePage(page);
							getPage().detach();
							return;
						}
				} catch (Exception e) {
					logger.error(e);
					
				}
			}
		});
		
		add(new WicketEventListener<RemoveLabelEvent<IDoc>>() {
			public void onEvent(RemoveLabelEvent<IDoc> event) {
				for (Classification c: event.getModel().getObject().getClassification()) {
					if (c.getDataSetMember()!=null && c.getDataSetMember().equals( event.getMemberModel().getObject())) {
						event.getModel().getObject().removeClassification(c);
						break;
					}
				}
				event.getModel().getObject().getService(ContentService.class).update("Remove Tag " + event.getMemberModel().getObject().getDisplayName());
				event.getRequestTarget().add(IDocTaskPanelV6.this.getPage());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof RemoveLabelEvent;
			}
			
		});
		
		
		/**
		 * TAKE -> stays in the same Task after Taking it
		 */
		add(new WicketEventListener<KbeeTakeTaskEvent>() {
			public void onEvent(KbeeTakeTaskEvent event) {
				IDocTaskPanelV6.this.handle(event);
				if (getContent().getWorkspace()!=null && getContent().getWorkspace()>0) {
					IDocTaskPageV6 page = new IDocTaskPageV6(getWorkflowContext(), false);
					page.setNavigator(navigation_panel.getNavigator());
					setResponsePage(page);
					getPage().detach();
					return;
				}
				else {
					if (navigation_panel!=null) {
						if (navigation_panel.getNavigator().getCursor().hasMoreElements())
							navigation_panel.navigateNext();
						else if ((navigation_panel.getNavigator().getIndex()>0))
							navigation_panel.navigatePrevious();
						else {
							setResponsePage( new RedirectPage("/home"));
						}
						getPage().detach();
					}
				}
			}
		});

		add(new WicketEventListener<KbeeWorkflowEvent>() {
			public void onEvent(KbeeWorkflowEvent event) {
				
				update(false);
				getWorkflowService().handle(event, getWorkflowModel().getObject());
				
				Session.get().success("Tarea de " + getTask().getDisplayName() +
					" terminada en " +
					getModel().getObject().getDisplayName());
				
				clearValidation();
				
				if (navigation_panel==null) {
					logger.debug("navigation_panel is null");
					setResponsePage( new RedirectPage("/myhome"));
					return;
				}
				
				
				
				// -----------------
				// 
				// if After the Action the Content stays/goes to the User Workspace
				//
				if (    getContent().getWorkspace()!=null && 
						getContent().getWorkspace().equals( getSessionUser().getId()) &&
						!"thread_end".equals(event.getId())) {
					

					logger.debug("Content stays/goes to the User's Workspace");

					IDocTaskPageV6 page = new IDocTaskPageV6(getWorkflowContext(), false);
					page.setNavigator(navigation_panel.getNavigator());
					setResponsePage(page);
					getPage().detach();
					return;
				}

				// if the ResultSet has more elemente, try next or previous
				else {
					if (navigation_panel.getNavigator()!=null && 
						navigation_panel.getNavigator().getCursor()!=null &&
						"navigate".equals(onEndTask)) {
						try {	
							if (navigation_panel.getNavigator().getCursor().hasMoreElements()) {
								logger.debug("Navigate NEXT");
								navigation_panel.navigateNext();
							}
							else if ((navigation_panel.getNavigator().getIndex()>0)) {
								logger.debug("Navigate PREVIOUS");
								navigation_panel.navigatePrevious();
							}
							else {
								logger.debug("goes to HOME");
								setResponsePage(new RedirectPage("/myhome"));
							}
						} catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<>(e));
						}
					}
					else {
						setResponsePage( new RedirectPage("/myhome"));
					}
					getPage().detach();
				}
			}
		});
		
		add(new WicketEventListener<ValidationEvent>() {
			public void onEvent(ValidationEvent event) {
				for (ITab tab : getLayout().getTabs(ContentFormEditor.class)) {
					getLayout().setSelectedTab(tab);
					int selected = getLayout().getSelectedTab();
					Panel panel =  (Panel)getLayout().getTab(selected);
					if (panel instanceof ContentFormEditor) {
						if (((ContentFormEditor<?>)panel).getForm().equals(event.getForm())) {
							((ContentFormEditor<?>)panel).setFocus(event.getRequestTarget(), event.getField());
							break;
						}
					}
				}
				event.getRequestTarget().add(IDocTaskPanelV6.this);
			}
		});
		
		add(new WicketEventListener<EOpenFormEvent>() {
			public void onEvent(EOpenFormEvent event) {
				for (ITab tab : getLayout().getTabs(ContentFormEditor.class)) {
					Panel panel = getLayout().setSelectedTab(tab);
					if (((ContentFormEditor<?>)panel).getData().getForm().getName().equals(event.getName())) {
						break;
					}
				}
				event.getRequestTarget().add(IDocTaskPanelV6.this);
			}
		});
		
		add(new WicketEventListener<ActionEvent>() {
			public void onEvent(ActionEvent event) {
				update(false);
				IDocTaskPanelV6.this.handle(event);
			}
		});
		
		add(new WicketEventListener<EFormEvent>() {
			public void onEvent(EFormEvent event) {
				IDocTaskPanelV6.this.handle(event);
			}
		});
		
		add(new WicketEventListener<EAjaxFormEvent>() {
			@Override
			public void onEvent(EAjaxFormEvent event) {
				for (ClassificationPanel<?> editor : getEditors()) {
					if (editor instanceof ContentFormEditor) {
						ContentFormEditor<?> contenteditor = ((ContentFormEditor<?>)editor);
						if (event.getFormData()!=null && contenteditor.getForm().equals(event.getFormData().getForm())) {
							if (!contenteditor.iterator().hasNext() || !contenteditor.renderedForm()) {
								// not rendered yet or editor not rendered.
								contenteditor.handle(event);
							}
							else {
								// interface
								contenteditor.setUpdated(true);
								contenteditor.fire(event, contenteditor.iterator(), false);
							}
						}
					}
				}
			}
		});
	}

	@Override
	public void setInitialTab(String a) {
		initial_tab=a;
		try {
			if (layout!=null)
			layout.setSelectedTab(a);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
	
	/**
	 * OnAfterCancel Task
	 */
	protected void onAfterCancelWorkflow(AjaxRequestTarget target) {
		if (navigation_panel !=null && navigation_panel.getNavigator()!=null) {
			
			try {	
					if (navigation_panel.getNavigator().getCursor().hasMoreElements()) {
						navigation_panel.navigateNext();
					}
					else if ((navigation_panel.getNavigator().getIndex()>0)) {
						navigation_panel.navigatePrevious();
					}
					else {
						setResponsePage( new RedirectPage("/home"));
					}
			} catch (Exception e) {
				logger.error(e);
				setResponsePage(new ApplicationErrorPage<>(e));
			}
		}
		else {
			setResponsePage( new RedirectPage("/home"));
		}
	}
	
	protected Panel getBreadCrumb() {
		MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<>();
		try {
			bc.addElement( new HomeBC());
			bc.addElement(new TasksDropDownMenuBC());

			StringBuilder ow = new StringBuilder();
			if (isPending()) { 
				ow.append(" - <span class=\"highlight\"> " +getLabelString("pending")+" </span>");
			}
			else {
				if (getWorkflowModel().getObject().getCurrentActivity()!=null) {
				BCElement owner = new BCElement( new Model<String>(getWorkflowModel().getObject().getCurrentActivity().getUser().getFirstLastName()));
				bc.addElement(owner);
				}
			}
			//------------------
			//  THIS IS TEMPORARY 
			//  WE HAVE TO MOVE THIS TO A LAZY PANEL
			//  AND CARRY OUT THE NAVIGATOR SO AS TO OPEN IN THE SAME TAB
			//------------------
			BCElement task = new BCElement( new Model<String>(getWorkflowModel().getObject().getTask().getDisplayName() + ow.toString()));
			task.setHTMLTitleAttribute(getLabel("task"));
			bc.addElement(task);
		} 
		catch (Exception e) {
			logger.error(e);
			bc.addElement(new BCElement( new Model<String>("err - " + e.getClass().getName())));
		}
		return bc;
	}
	
	protected boolean isSignatureRequired(EForm eform) {
		return eform instanceof KbeeTaskForm && ((KbeeTaskForm)eform).isSignatureRequired();
	}

	protected boolean isMonitorable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(getContent());
	}
	
	protected boolean isPending() {
		return ((KbeeContext) getWorkflowModel().getObject()).isPending(); 
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected boolean isUserWorkspace() {
		if (getModel().getObject().getWorkspace()==null)
			return false;
		return (getModel().getObject().getWorkspace().equals(getSessionUser().getId()));
	}
}