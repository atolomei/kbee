package kbee.web.idoc;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.document.IDoc;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EHtmlStructField;
import com.novamens.content.form.EForm;
import com.novamens.kbee.content.form.KbeeEHtmlStructField;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EHtmlStructRawViewer;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.ContentBaseBC;
import kbee.web.page.KbeeWebPage;

public class IDocTextViewerPage extends KbeeWebPage<IDoc> {

	private static final long serialVersionUID = 1L;
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IDocTextViewerPage.class.getName());
	
	//private  AuditTrailModal<Content> audit_modal = null;
	//private  ShareModal<Content> share_modal	  = null;

	protected WebMarkupContainer modal_container;
	
	public IDocTextViewerPage() {
	}

	public IDocTextViewerPage(PageParameters parameters) {
		IDoc idoc = getContent(parameters);
		if (idoc!=null)
			 setModel(new ObjectModel<IDoc>(idoc));
	}

	public IDocTextViewerPage(IModel<IDoc> model) {
		super(model);
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();

		try {
			
			if (getModel()==null || getModel().getObject()==null) 
				throw new IllegalArgumentException("Model can not be null");
					
			//modal_container = new WebMarkupContainer("modal-container"); 
			//modal_container.setOutputMarkupId(true);
			//addOrReplace(modal_container);
		
			//getModalContainerMarkupContainer().add(new InvisiblePanel("audit-trail-modal"));
			//getModalContainerMarkupContainer().add(new InvisiblePanel("send-email-modal"));
	
			/** this page is not using hasPermissions */
			
//			if (!ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(getModel().getObject())) {
//				//setTopNavigation(new ErrorNavigationBar<IDoc>("navigation"));
//				//setMenu(new InvisiblePanel("menu"));
//				addOrReplace(new ErrorPanel("text", (new ContentBaseBC()).getLabel(), new Model<String>("content not found or access denied.")));
//				return;
//			}
				
			//setLogVisit(true);
			setPageTitle(new Model<String>(getModel().getObject().getTitle()));
					
			//setTopNavigation(getMainTopbar());
			//setMenu(getMainLaternalMenu());
					
//			List<ITab> tabs = new ArrayList<ITab>();

			EFormField<?>  textfield = null;
			IModel<EFormData> datamodel = null;
			for (EForm eform: getForms()) {
				for (EFormField<?> field : eform.getFields()) {
					if (field instanceof EHtmlStructField) {
						textfield = field;
						EFormData data = getModelObject().getFormData(eform);
						datamodel = new EFormDataModel(data);	
					}
				}
			}
			
			if (textfield!=null)
			add(new EHtmlStructRawViewer("text", (KbeeEHtmlStructField)textfield, datamodel)); 
			else
			add(new ErrorPanel("text", (new ContentBaseBC()).getLabel(), new Model<String>("content not found or access denied.")));
				
		
//			if (getForms().size()==0) {
//				tabs.add(new AbstractTabKB(getLabel("tab.main"), "main") {
//					@Override
//					public Panel getPanel(String panelId) {
//						return  new SearcherDetailMainPanel<IDoc>(panelId, getModel(), getSiteModel(), true);
//					}
//				});
//			}
		
//			if (getModel().getObject().getContentTemplate().isCustomAttributes()) {
//				tabs.add(new AbstractTabKB(new Model<String>(getModel().getObject().getContentTemplate().getCustomattributes_label()), "customtags") {
//					@Override
//					public Panel getPanel(String panelId) {
//						return new CustomAttributesPanel<IDoc>(panelId, getModel() );
//					}
//				});
//			}
			
//			if (getModelObject().getService(ContentService.class).getText()!=null) {
//				tabs.add(new AbstractTabKB(getLabel("tab.links"), "links") {
//					@Override
//					public Panel getPanel(String panelId) {
//						return new ContentLinksPanel<IDoc>(panelId, new ObjectModel<IDoc>((IDoc)getModelObject()));
//					}
//				});
//			}
				
//			tabs.add(new SeparatorTabKB("separator1"));
//			
//			tabs.add(new AbstractTabKB(getLabel("tab.versioncontrol"), "version") {
//				@Override
//		 		public Panel getPanel(String panelId) {
//		 			return  new ContentLibraryPanel<IDoc>(panelId, getModel(), getSiteModel(), isConsole());
//		 		}
//		 	});
//		 			
//		 	tabs.add(new AbstractTabKB(getLabel("tab.history"), "history") {
//				@Override
//		 		public Panel getPanel(String panelId) {
//		 			return new ProcessHistoryPanel<IDoc>(panelId, getModel(), getActivities());
//		 		}
//		 	});
//		 		
//		 	
//		 	
//		 	
//		 	if (isWriteable(getModel())) {
//		 		
//		 		
//		 		
//		 		tabs.add(new AbstractTabKB(getLabel("tab.audit"), "audit") {
//					@Override
// 					public Panel getPanel(String panelId) {
// 						return  new AuditTrailObjectPanel<IDoc>(panelId, getModel());
//					}
//		 		});
//		 	}
//		 	
// 			VerticalLayout<ITab> panel = new VerticalLayout<ITab>("tabs", "content", tabs);
// 			panel.setTitle(new StringResourceModel("sections", this, null));
// 			panel.setSections(VerticalLayout.COLS_9X3);
//		
// 			try {
//		 				if (getNavigator()!=null) {
//		 					NavigatorPanelV6<Content> na = new NavigatorPanelV6<Content>("panel", getNavigator());
//			 				na.setResultsPanel(true);
//			 				SearcherDetailHeaderPanel<IDoc> hpanel=new SearcherDetailHeaderPanel<IDoc>("page-content-header", getModel(), getSiteModel(), na, true);
//			 				hpanel.add(new AttributeModifier("class", "page-header"));
//			 				setPageContentHeader(hpanel);
//			 			}
//			 			else {
//			 				SearcherDetailHeaderPanel<IDoc> hpanel=new SearcherDetailHeaderPanel<IDoc>("page-content-header", getModel(), getSiteModel(), null, true);
//			 				hpanel.add(new AttributeModifier("class", "page-header"));
//			 				setPageContentHeader(hpanel);
//			 			}
//			} 
//			catch (Exception e) {
//						logger.error(e);
//		 				SearcherDetailHeaderPanel<IDoc> hpanel=new SearcherDetailHeaderPanel<IDoc>("page-content-header", getModel(), getSiteModel(), null, true);
//		 				hpanel.add(new AttributeModifier("class", "page-header"));
//		 				setPageContentHeader(hpanel);
// 			}
// 			//panel.setMenuItemFactory(getMenuItems());
// 			panel.setContentPanelCss("main-area detail text");
// 			add(panel);
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("tabs", e));
		}
	}

//	@SuppressWarnings("unchecked")
//	public void onNavigate(Content content) {
//		try {
//			if (content.getWorkspace()!=null && content.getWorkspace()>0) {
//				Task task = content.getService(WorkflowService.class).getTask();
//				
//				if (task==null)
//					throw new KbeeRuntimeException("Task is null for content -> " + content.getDisplayName());
//				
//				Page page = (TaskPage<Content>)((com.novamens.kbee.content.workflow.WebTask)task).getPage(content.getService(WorkflowService.class).getContext());
//				
//				if (page instanceof NavigablePage<?>) 
//						((NavigablePage<Content>)page).setNavigator( getNavigator() );
//				
//				setResponsePage(page);
//			}
//			else {
//				Page page=(Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page" , new ObjectModel<Content>(content));
//				if (page instanceof NavigablePage<?>) { 
//					if (getNavigator()!=null)
//						((NavigablePage<Content>)page).setNavigator(getNavigator() );
//				}
//				setResponsePage(page);
//			}
//			
//		} 
//		catch (Exception e) {
//			logger.error(e);
//			setResponsePage( new ApplicationErrorPage<>(e));
//		}
//	}
	
//	@Override
//	public void addListeners() {
//		super.addListeners();
//		
//		add(new WicketEventListener<ClickBackEvent<Content>>() {
//			@Override
//			public void onEvent(ClickBackEvent<Content> event) {
//				if (IDocTextViewerPage.this.getModel().getObject().getWorkspace()!=null) {
//
//					if (getSource()==WorkspaceConsole.NAME) {
//						setResponsePage( new WorkspacePage());
//						return;
//					}
//					else {
//						setResponsePage( new MonitorPage());
//						return;
//					}
//				}
//				else
//					setResponsePage( new ContentBasePage());
//			}
//		});
//		
//		add(new WicketEventListener<ClickItemEvent<Content>>() {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public void onEvent(ClickItemEvent<Content> event) {
//				try {
//					logger.debug(event.toString());
//					Content content = event.getModel().getObject();
//		
//					if (!(content instanceof IDoc)) {
//						logger.error("TBA ASSUMES CONTENT IS IDOC !!!");
//						return;
//					}
//					// TBA ASSUMES CONTENT IS IDOC
//					if (content.getWorkspace()!=null) {
//						IDocTaskPageV6 page = new IDocTaskPageV6(content.getService(WorkflowService.class).getContext(), false);
//						Navigator<Content> navigator= getNavigator();
//						navigator.getCursor().setIndex(event.getIndex());
//						page.setNavigator(navigator);
//						setResponsePage(page);
//						getPage().detach();
//						return;
//					}
//					else {
//						IDocTextViewerPage page = new IDocTextViewerPage( new ObjectModel<IDoc>( (IDoc) content));
//						page.setNavigator(getNavigator());
//						getNavigator().getCursor().setIndex(event.getIndex());
//						setResponsePage(page);
//						getPage().detach();
//						return;
//					}
//				} 
//				catch (Exception e) {
//					logger.error(e);
//					
//				}
//			}
//		});
//		
//		add(new WicketEventListener<ClickEvent<IDoc>>() {
//			@Override
//			public void onEvent(ClickEvent<IDoc> event) {
//				logger.debug( event.toString());
//			}
//		});
//		
//		add(new WicketEventListener<CursorNavigationEvent<Content>>() {
//			public void onEvent(CursorNavigationEvent<Content> event) {
//				IDocTextViewerPage.this.onNavigate((Content) event.getModelObject());
//				event.detach();
//			}
//		});
//		
//		add(new WicketEventListener<ErrorEvent<?>>() {
//			@Override
//			public void onEvent(ErrorEvent<?> event) {
//				FeedbackHelper.showErrorToast( 
//					event.getThrowable()!=null? event.getThrowable().getClass().getName() : 
//					(event.getModel()!=null ? event.getModel().getObject().toString() : "Error"), 
//					event.getThrowable()!=null? event.getThrowable().getMessage() : 
//						(event.getModel()!=null ? event.getModel().getObject().toString() : "Error")
//					);
//			}
//		});
//		
//		add(new WicketEventListener<AuditTrailContentEvent<Content>>() {
//			@Override
//			public void onEvent(AuditTrailContentEvent<Content> event) {
//				getAuditModal().open(event.getRequestTarget(), event.getModel());
//				event.getRequestTarget().add(IDocTextViewerPage.this.getModalContainerMarkupContainer());
//			}
//		});
//		
//		add(new WicketEventListener<ShareContentEvent<Content>>() {
//			@Override
//			public void onEvent(ShareContentEvent<Content> event) {
//				getShareModal().open(event.getRequestTarget(), event.getModel());
//				event.getRequestTarget().add(IDocTextViewerPage.this.getModalContainerMarkupContainer());
//			}
//		});
//	}
//	
//	
//	
//	public AuditTrailModal<Content> getAuditModal() {
//		if (audit_modal==null) {
//			audit_modal = new AuditTrailModal<Content>("audit-trail-modal");
//			getModalContainerMarkupContainer().addOrReplace(audit_modal);
//		}
//		return this.audit_modal;
//	}
//	
//	public ShareModal<Content> getShareModal() {
//		if (share_modal==null) {
//			share_modal = new ShareModal<Content>("send-email-modal");
//			getModalContainerMarkupContainer().addOrReplace(share_modal);
//		}
//		return this.share_modal;
//	}

	
	@Override
	protected boolean hasLateralMenu() {
		return false;
	}
	
//	private List<MenuItemFactory<Panel>> getMenuItems() {
//	
//		List<MenuItemFactory<Panel>> list = new ArrayList<MenuItemFactory<Panel>>();
//		
//		if (getLaunchers().size()>0) {
//			list.add(id ->
//				new HeaderMenuItemPanelV5<Panel>(id) {
//					@Override
//					public String getLabel() {
//						return new StringResourceModel("edit", this, null).getObject();
//					}
//			});
//				
//			for (int p=0; p<getLaunchers().size(); p++) {
//				int p_i = p;
//				list.add(new MenuItemFactory<Panel>() {
//					@Override
//					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
//						return new MenuItemPanelV5<Panel>(id) {
//							@Override
//							public void onClick() {
//								try {
//									ProcessLauncher launcher = getLaunchers().get(p_i);
//									Content content = IDocTextViewerPage.this.getModel().getObject().getService(ContentService.class).checkout();
//									content.getService(WorkflowService.class).startProcess(launcher.getProcedure());
//									setResponsePage(new RedirectPage(content.getService(UrlService.class).getTaskUrl()));
//								} 
//								catch (Exception e) {
//									logger.error(e.getClass().getName() + "| Checkout in ContentBaseConsole contextual menu" );
//									setResponsePage(new ApplicationErrorPage<>(e));
//								}
//							}
//							@Override
//							public String getLabel() {	
//								return  IDocTextViewerPage.this.getLabel("checkout").getObject()+ " - " + getLaunchers().get(p_i).getDisplayName();
//							}
//							@Override 
//							public boolean isVisible() {
//								if (isReadOnly())
//									return false;
//								if (IDocTextViewerPage.this.getModelObject().isArchived())
//									return false;
//								if (IDocTextViewerPage.this.getModelObject().isRecycled())
//									return false;
//								return true;
//							}
//							@Override 
//							public boolean isEnabled() {
//								if (isReadOnly() || getDomain()==null)
//									return false;
//								return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(IDocTextViewerPage.this.getModelObject()) &&
//									 IDocTextViewerPage.this.getModelObject().isHeadVersion() 	&&
//									!IDocTextViewerPage.this.getModelObject().isLocked() 		&&
//									!IDocTextViewerPage.this.getModelObject().isRecycled() 		&&
//									!IDocTextViewerPage.this.getModelObject().isArchived();
//							}
//						};	
//					}	
//				});
//			}
//				
//			list.add(new MenuItemFactory<Panel>() {
//				@Override
//				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
//					return new SeparatorMenuItemPanelV5<Panel>(id) {
//						@Override
//						public String getCssClass() {
//							return "divider";
//						}
//						@Override
//						public boolean isVisible() {
//								return true;
//						}
//					};
//				}
//			});
//		}
//		
//		list.add(new MenuItemFactory<Panel>() {
//			@Override
//			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
//				return new MenuItemPanelV5<Panel>(id) {
//					@Override
//					public void onClick() {
//						try {
//							// new WorkspacePage();
//						} 
//						catch (Exception e) {									
//							logger.error(e.getClass().getName() + "archive in ContentBaseconsole contextual menu" );
//						}
//					}
//					@Override
//					public String getLabel() {	
//						return  IDocTextViewerPage.this.getLabel("modal.sendbyemail.title").getObject();
//					}
//				};	
//			}	
//		});
//		
//		list.add(new MenuItemFactory<Panel>() {
//			@Override
//			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
//				return new AjaxMenuItemPanelV5<Panel>(id) {
//					public void onClick(AjaxRequestTarget target) {
//						if (!IDocTextViewerPage.this.getModel().getObject().isLocked()) {
//							try {
//								IDocTextViewerPage.this.getModel().getObject().getService(ContentService.class).recycle();
//								
//							} catch (ContentMgmtException | ServiceNotFoundException e) {
//								logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
//								fire (new ErrorEvent<>(target, e));
//							}
//						}
//						setResponsePage( new ContentBasePage());
//					}
//					@Override 
//					public String getLabel() {
//						return IDocTextViewerPage.this.getLabel("delete").getObject();
//					}
//					@Override
//					public String getWorkingLabel() {
//						return IDocTextViewerPage.this.getLabel("working").getObject();
//					}
//					@Override
//					public boolean isEnabled() {
//						return !IDocTextViewerPage.this.getModel().getObject().isLocked() && isDeletable(IDocTextViewerPage.this.getModel());
//					}
//				};
//			}
//		});		
//		
//		return list;
//	}
//	
	protected boolean isConsole() {
		return true;
	}
	
	protected List<EForm> getForms() {
		List<EForm> forms = new ArrayList<EForm>();
		for (EForm form : getModelObject().getContentTemplate().getForms()) {
			if (form!=null && form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL)) {
				if (form.isVisible(getModelObject().getFormData(form))) {
					forms.add(new KbeeTaskForm(form));
				}
			}
			else if (form!=null && form.getFormAccessLevel().equals(EFormAccessLevel.INTERNAL_INFO) && isInternalInfoReadable()) {
				forms.add(new KbeeTaskForm(form));
			}
		}
		return forms;
	}
	
	protected boolean isInternalInfoReadable() {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected IDoc getContent(PageParameters parameters) {
		try {
			IDoc content = null;		
			Class<IDoc> contentclass = (Class<IDoc>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];		
			StringValue oid = parameters.get("oid");
			if (!oid.isNull() && !oid.isEmpty()) { 
				StringValue id = parameters.get("id");
				if (id.isNull() || id.isEmpty()) { 
					content = (IDoc)getContentDao().findContentByOId(Long.valueOf(oid.toString()));
				}
				else {
					content = (IDoc)getContentDao().findContentById(contentclass, id);
				}
			}	
			return content;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

//	protected  WebMarkupContainer getModalContainerMarkupContainer() {
//		return modal_container;
//	}

//	@SuppressWarnings("unchecked")
//	protected Page getTaskPage(IModel<Content> model, IModel<Cursor> cursor) {
//		try {
//			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
//			Task task = workflowService.getTask();
//			TaskPage<Content> page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
//			page.setNavigator(getNavigator());
//			return page;
//		} 
//		catch (Exception e) {
//			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
//			return new kbee.web.error.ApplicationErrorPage<Void>(e);
//		}
//	}
	
//	private List<ProcessLauncher> getLaunchers() {
//		if (getDomain()==null)
//			return  new ArrayList<ProcessLauncher>();
//		return getDomain().getService(WorkflowDomainService.class)==null ? new ArrayList<ProcessLauncher>() :
//			getDomain().getService(WorkflowDomainService.class).getContextLaunchers(IDocTextViewerPage.this.getModelObject());
//	}
}