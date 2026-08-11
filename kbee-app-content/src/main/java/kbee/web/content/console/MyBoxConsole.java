package kbee.web.content.console;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.LabelSet;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.UrlService;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.web.user.markup2.ContentLabelMenuItemFactory;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.ObjectState;import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.domain.provisioning.DomainModelBuilderService;
import com.novamens.kbee.content.multidimensional.GroupFacet;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.multidimensional.TaskFacet;
import com.novamens.kbee.wicket.markup.html.console.browser.LabelItem;
import com.novamens.kbee.wicket.markup.html.console.browser.LauncherButton;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.LauncherSelectorEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.SeparatorToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GlyphiconColumnPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.ImageColumnPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.UserListsColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ButtonPanelToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.workflow.Process;

import kbee.util.NumberFormatter;
import kbee.web.application.MyBoxBC;
import kbee.web.console.AdvancedSearchContentSelectorPanel;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.MyBoxQuery;
import kbee.web.console.TitleColumnPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.LabelEvent;
import kbee.web.nav.Navigator;
import kbee.web.resource.ResourceLink2;
import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.resource.UploadPanel;
import kbee.web.searcher.panel.SearcherSimpleErrorPanel;

@SuppressWarnings("serial")
public abstract class MyBoxConsole extends ContentConsole<Content> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyBoxConsole.class.getName());

	
	final public static String NAME = "myresources";
	
	private List<GridColumn<SearchResult,String>> columns = null;
	private List<ToolbarItem> items;
	private Map<Long, List<IModel<LabelMember>>> labels = new HashMap<Long, List<IModel<LabelMember>>>();

	private List<IModel<ProcessLauncher>> launchers;
	private IModel<ProcessLauncher> selected_launcher_model;
	private List<ToolbarItem> selection_toolbar;
	
	private  IModel<DataSetMember>  m_myBoxTypeModel;
	private  IModel<Classifier>     m_typeClassifier;
	
	private  IModel<ContentTemplate> ct_model;
	
	public MyBoxConsole(String name, Query query) {
		super(name, query);
	}
	
	public MyBoxConsole(Query query) { 
		super(NAME, query);
	}

	@Override
	protected String getIcon(IModel<Content> model) {
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		try {
				ContentTemplate ct = getDomain().getService(DomainService.class).getResourcesTemplate();
				if (ct==null)
					ct=getDomain().getService(DomainModelBuilderService.class).buildResourcesContentTemplate();
				ct_model = new ObjectModel<ContentTemplate> (ct);
				DataSetMember dm_resource=getDomain().getService(DomainService.class).getResourcesTypeDataSetMember();
				if (dm_resource!=null)
					m_myBoxTypeModel = new ObjectModel<DataSetMember>(dm_resource);
				Classifier cl_type=getDomain().getService(DomainService.class).getResourcesTypeClassifier();
				if (cl_type!=null)
					m_typeClassifier = new ObjectModel<Classifier>(cl_type);
		} catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}
	

	@Override
	public Query newQuery() {
		return setUserPreference(new MyBoxQuery(getQueryIndex()));
	}
	
	@Override
	public void addListeners() {
		super.addListeners();


		add(new WicketEventListener<LabelEvent>() {
			@Override
			public void onEvent(LabelEvent event) {
				MyBoxConsole.this.refresh(event.getRequestTarget());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof LabelEvent;
			}
		});
		
		add(new WicketEventListener<LauncherSelectorEvent<ProcessLauncher>>() {
			@Override
			public void onEvent(LauncherSelectorEvent<ProcessLauncher> event) {
				try {
					MyBoxConsole.this.setSelectedLauncherModel(event.getModel());
					if (event.getKey()==null ||	event.getKey().equals("one-for-all")) {
						Content content = (Content) addOneForAll(event.getRequestTarget());
						if (content!=null) {
							setResponsePage(MyBoxConsole.this.getTaskPage(new ObjectModel<Content>(content)));
						}
					}
					else {
						addOneForEach(event.getRequestTarget());
						setResponsePage(new WorkspacePage());
					}
				} 
				catch (Exception e) {
					logger.error(e);
					FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
				}
				
			}
		});
	}
	
	public void setSelectedLauncherModel( IModel<ProcessLauncher> model) {
		this.selected_launcher_model=model;
	}
	
	public IModel<ProcessLauncher> getSelectedLauncherModel() {
		return this.selected_launcher_model;
	}
	
	public List<IModel<ProcessLauncher>> getLaunchers() {
		if (this.launchers!=null)
			return this.launchers;
		this.launchers = new ArrayList<IModel<ProcessLauncher>>();
		List<ProcessLauncher> list = getDomain().getService(WorkflowDomainService.class).getLaunchers();
		for (ProcessLauncher launcher: list) {
			if (launcher.isEnabled() && 
				launcher.executeable() && 
				launcher.getContentTemplate()!=null && 
				launcher.getContentTemplate().getState()==ObjectState.ENABLED) { 
				this.launchers.add( new ObjectModel<ProcessLauncher>(launcher));
			}	
		}
		return this.launchers;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			
			if (ct_model!=null)
				ct_model.detach();
			
			
			if (m_myBoxTypeModel!=null)
				m_myBoxTypeModel.detach();
				
			if (m_typeClassifier!=null)
				m_typeClassifier.detach();
			
			if (launchers!=null) 
				launchers.forEach(item -> item.detach());

			if (selected_launcher_model!=null)
				selected_launcher_model.detach();
			
			getColumns().forEach(item -> item.detach());

			if (this.items!=null) 
				this.items.forEach(item -> item.detach());
			
			if (this.selection_toolbar!=null)
				this.selection_toolbar.forEach(item -> item.detach());
			
			if (this.labels!=null)
				this.labels.forEach((k, v) -> v.forEach(item->item.detach()));
			
			if (this.columns!=null)
				this.columns.forEach(item -> item.detach());
			
		} 
		catch (Exception e) {
			logger.	error(e);
			FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
		}
	}
	
	
	/**
	 * 
	 * @param file
	 */
	protected void createContent(KBFile file) {
		try {
			IDoc idoc = (IDoc)ServiceLocator.getService(ContentFactoryService.class).create( ct_model.getObject().getName(), file, ObjectState.DRAFT);
			if ( this.m_typeClassifier!=null && m_myBoxTypeModel !=null) 
				idoc.addClassification( this.m_typeClassifier.getObject(), m_myBoxTypeModel.getObject());
			idoc.getService(ContentService.class).update();
			
		}
		catch (Exception e) {
			logger.error(e);
			FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
			throw(e);
		}
	}

	/**
	 * Toolbar -  Selection 
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
		
		this.selection_toolbar.add(new SeparatorToolbarItem(getBrowser()));
		this.selection_toolbar.add(new LabelItem(browser, ToolbarItem.Align.TOP_LEFT,	new StringResourceModel("create", MyBoxConsole.this, null)));
		
		int n=0;
		
		for (IModel<ProcessLauncher> la: getLaunchers() ) {
			
			if (n++>0)
				this.selection_toolbar.add(new SeparatorToolbarItem(browser) {
					@Override
					public boolean isVisible() {
						return true;
					}	
				});
			
			this.selection_toolbar.add(new LauncherButton(la, browser, ToolbarItem.Align.TOP_LEFT,  new StringResourceModel("one-for-each", MyBoxConsole.this, null).getObject(), "one-for-each") {
				@Override
				public boolean isEnabled() {
					if (getBrowser().getSelection().isEmpty())
							return false;
						return true;
				}
			});
			
		this.selection_toolbar.add(new LauncherButton(la, browser, ToolbarItem.Align.TOP_LEFT, new StringResourceModel("one-for-all", MyBoxConsole.this, null).getObject(), "one-for-all") {
				@Override
				public boolean isEnabled() {
					if (getBrowser().getSelection().isEmpty())
							return false;
						return true;
				}
				
				@Override
				public boolean isVisible() {
					/**if (getBrowser().getSelection().isEmpty())
							return false;
					if (getBrowser().getSelection().size()==1)
						return false;**/
					return true;
				}
			});
		}

		
		this.selection_toolbar.add(new  SeparatorToolbarItem(getBrowser()));

		
		this.selection_toolbar.add(new LabelItem(browser, ToolbarItem.Align.TOP_LEFT,	new StringResourceModel("delete", MyBoxConsole.this, null)));

		
		
		/**
		 * 	Delete
		 */
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {

			@Override
			public boolean isEnabled() {
				return true;
			}
			
			protected String getIcon() {
				return "";
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
			
			protected String getLabelStr() {
				 return new StringResourceModel("delete", MyBoxConsole.this).getObject();
			 }
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					

					StringBuilder ss=new StringBuilder();
					getBrowser().getSelection().forEach(item -> ss.append(  (ss.length()>0?" | ":"")  +(((Content) item.getObject()).getTitle() + " ")));
					final String lis=ss.toString();
					String result=MyBoxConsole.this.delete(getBrowser().getSelection());
					if (result!=null)
						getErrorDialog().open(target, new Model<String>("Error") ,new Model<String>(result));
					else {
						FeedbackHelper.showInfoToast("Batch Delete",  lis);
						// fire(new InfoEvent(target, new Model<String>("Batch Delete"),  new Model<String>(lis), "warning"));
					}
					
					MyBoxConsole.this.resetSelection();
					MyBoxConsole.this.refresh(target);
					
				} catch (Exception e) {
					logger.error(e);
					getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName()) ,new Model<String>(e.getMessage()));
					MyBoxConsole.this.refresh(target);
				}
			}
		});

		return this.selection_toolbar;
		
		
		
	}
	
	/**
	 * String -> null if ok, errors
	 */
	protected String delete(List<?> selection) {

		StringBuilder str = new StringBuilder();
		
		@SuppressWarnings("unchecked")
		List<IModel<Content>> list = (List<IModel<Content>>)  selection;

		for (IModel<Content> c:list) {
			try {
				logger.debug(" deleteing " + c.getObject().getTitle());
				c.getObject().getService(ContentService.class).delete();

			} catch (Exception e) {
				logger.error(e);
				str.append(c.getObject().getTitle()+" -> " + e.getMessage());
				FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
			}
		}
		if (str.length()==0)
			return null;
		return str.toString();
	}

	protected ResourceContainer addOneForAll(AjaxRequestTarget target) {

		if (getSelectedLauncherModel()==null || getSelectedLauncherModel().getObject()==null) {
			getErrorDialog().open(target, new Model<String>("ERROR"), new Model<String>("Please Select a Content Class"));
			refresh(target);
			return null;
		}

		boolean has_errors = false;
		
		StringBuilder str = new StringBuilder();
					
		List<IModel<Content>> limodel = this.getBrowser().getSelection();
		
		ResourceContainer rc = null;
		
		try {
			List<IModel<KBFile>> lkb = new ArrayList<IModel<KBFile>>();
			for (IModel<Content> m:limodel) {
				KBFile fi=getFile(m.getObject());
				if (fi!=null)
					lkb.add(new ObjectModel<KBFile>(fi));
			}
					
			startProcess(lkb);
				rc = startProcess(lkb);
				
			for (IModel<Content> model: limodel) {
				model.getObject().getService(ContentService.class).delete();
				logger.debug("removing " + model.getObject().getTitle());
			}
		} 
		catch (ContentMgmtException e) {
			has_errors = true;
			str.append(e.getMessage());
			logger.error(e);
		}

		if (has_errors) {
			getErrorDialog().open(target, new Model<String>(str.toString()));
		}
		
		setUserPreference(newQuery());
		getBrowser().resetSelection();
		refresh(target);
		
		return rc;
		
	}
	
	protected void addOneForEach(AjaxRequestTarget target) {
		
			if (getSelectedLauncherModel()==null || getSelectedLauncherModel().getObject()==null) {
					getErrorDialog().open(target, new Model<String>("ERROR"), new Model<String>("Please Select a Content Class"));
					refresh(target);
					return;
			}

			boolean has_errors = false;
			StringBuilder str = new StringBuilder();
				
			List<IModel<Content>> limodel = this.getBrowser().getSelection();
			
			for (IModel<Content> model: limodel) {
				try {
					KBFile file = getFile(model.getObject());
					if (file!=null) {
						startProcess(new ObjectModel<KBFile>(file));
 					}
					logger.error("kbfile is null");
				} 
				catch (ContentMgmtException e) {
					has_errors = true;
					str.append("File: " + model.getObject().getTitle() + " | Error: " + e.getMessage() + " <br />");
					logger.error(e);
				}
			}

			if (has_errors) {
				getErrorDialog().open(target, new Model<String>(str.toString()));
			}
			
			setUserPreference(newQuery());
			
			getBrowser().resetSelection();
			refresh(target);
	}
	
	
	protected void onClickEvent(ClickEvent<Content> event) {
	
		Page page = MyBoxConsole.this.getPage(event.getModel(), getSearcher(), getIndex(event.getModel().getObject()), false);
		if (page!=null)
			setResponsePage(page);
	}

	
	/**
	 * Contextual Menu for each element
	 */
	@Override
	protected Panel getMenu(IModel<Content> model) {
		
		try {
			
			ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
			
			menu.addItem((id) ->
				new MenuItemPanelV5<Content>(id) {
					protected AbstractLink getNewLink(String id) {
						return new ResourceLink2(id, new ObjectModel<Resource>(getFile(getModel().getObject())));
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("open").getObject();
					}
				}
			);
			
			menu.addItem((id) ->
				new DonwloadMenuItemPanelV5<Content>(id) {
					@Override 
					public String getLabel() {
						return MyBoxConsole.this.getLabel("download").getObject();
					}
					@Override
					protected File getFile() {
						KBFile kf = MyBoxConsole.this.getFile(getModel().getObject());
						if (kf!=null) {
							File file;
							try {
								file = kf.getFile();
								if (!file.exists()) {
									logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + " file "  + ((KBFile) getModel().getObject()).getUrl() +  "  does not exists");
									return null;
								}
								return file;
							} 
							catch (IOException e) {
								FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
								logger.error(e);
							}
						}
						return null;
					}
				}
			);
			
			
			menu.addItem((id) ->
				new SeparatorMenuItemPanelV5<Content>(id) {
					public String getLabel() {
						return "";
					}
					@Override
					public String getCssClass() {
						return "divider";
					}
				});
			
			menu.addItem((id) ->
				new HeaderMenuItemPanelV5<Content>(id) {
					public String getLabel() {
						return "Crear";
					}
			});
			
			for (IModel<ProcessLauncher> launchermodel : getLaunchers()) {
				menu.addItem((id) ->
					new MenuItemPanelV5<Content>(id) {
						public void onClick() {
							Content content = startProcess(launchermodel, new ObjectModel<KBFile>(getFile(getModel().getObject())));
							getModel().getObject().getService(ContentService.class).delete();
							setResponsePage(new RedirectPage(content.getService(UrlService.class).getTaskUrl()));
						}
						@Override 
						public String getTarget() {
							return "_blank";
						}
						@Override
						public PopupSettings getPopupSettings() {
							PopupSettings popup = new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
								PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
								PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
							return popup;
						}
						@Override 
						public String getLabel() {
							return launchermodel.getObject().getDisplayName();
						}
					}
				);
			}
			
			menu.addItem((id) ->
				new SeparatorMenuItemPanelV5<Content>(id) {
					public String getLabel() {
						return "";
					}
					@Override
					public String getCssClass() {
						return "divider";
					}
				});

			
			menu.addItem((id) ->
				new SubMenuAjaxUserListItemPanel<Content>(id, model, MyBoxConsole.this.getName(), UserListItem.NEWEST)
			);
			
			menu.addItem((id) ->
				new SubmenuAjaxItemPanelV5<Content>(id, model) {
					@Override
					public boolean isVisible() {
						return isWriteable(getModel());
					}
					@Override
					public String getLabel() {
						return getConsoleLabel("labels").getObject();
					}
					protected void addItems() {
						for (IModel<LabelMember> label: getLabelMembers(getModel().getObject().getContentTemplate()))  {
							addItem(new ContentLabelMenuItemFactory(label, model) {
								@Override
								public void onUpdate(AjaxRequestTarget target) {
									 fire(new LabelEvent(target));
									 FeedbackHelper.showInfoToast("Label Applied " + getModel().getObject().getDisplayName());
								}
							});
						}
					}
				}
			);
			
			menu.addItem((id) ->
				new SeparatorMenuItemPanelV5<Content>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				}
			);

			menu.addItem((id) ->
				new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							try {
								getModel().getObject().getService(ContentService.class).deleteAllVersions();
								FeedbackHelper.showInfoToast("Deleted " + getModel().getObject().getDisplayName());
							} 
							catch (ContentMgmtException | ServiceNotFoundException e) {
								logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
								FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
							}
							resetSelection();
							refresh(target);
						} 
						catch (Exception e) {
							logger.error(e, getSessionUser().getUserName());
						}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("delete").getObject();
					}
				}
			);

			return menu;
			
		} 
		catch (Exception e) {
			logger.	error(e, getSessionUser().getUserName());
			return new InvisiblePanel("menu");
		}
	}
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;

		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		

		this.columns.add(new GridColumn<SearchResult, String>("icon", getLabel("icon")) {
			@Override
            public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
                try {
                	if (resultmodel.getObject() == null)
                        cellItem.add(new Label(componentId, "err"));
                    Object object = resultmodel.getObject().getObject();
                    
                    if (object != null) {
                        cellItem.add(new Label(componentId, ((Content) object).getDisplayName()));
                    } else {
                        cellItem.add(new Label(componentId, "err"));
                    }
                } 
                catch (Exception e) {
                    logger.error(e);
                    cellItem.add(new Label(componentId, e.getClass().getName()));
                }
            }
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object;
				try {
					object = resultmodel.getObject().getObject();
				} 
				catch (Exception e) {
					logger.	error(e, getSessionUser().getUserName() +  " | probably requires reindexing.");
					cellItem.add(new InvisiblePanel(componentId));
					return;
				}
				cellItem.add(new GlyphiconColumnPanel<Content>(componentId, getModel((Content)object)) {
					@Override
					public boolean isVisible() {
						return getFile()!=null;
					}
					public KBFile getFile() {
						return MyBoxConsole.this.getFile(getModelObject());
					}
					@Override
					protected String getGlyphiconClass() {
						return getFile().getGlyphIcon();
					}
					@Override
					protected Link<?> getNewLink(String id) {
						return new ResourceLink2(id, new ObjectModel<Resource>(getFile()));
					}
					@Override
					protected String getCss() {
						return "iconcolumn panel-centered";
					}
				});
			}
			@Override
			public boolean isExportable() {
				return false;
			}
			@Override
			protected String getContextKey() {
				return MyBoxConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
			@Override
			public int getDefaultWidth() {
				return 58;
			}
			public boolean isResizable() {
				return false;
			}
			public boolean isFixed() {
				return false;
			}
			public boolean isHeaderMenu() {
				return false;
			}
			public boolean isExpanded() {
				return false;
			}
		});
		
		this.columns.add(new GridColumn<SearchResult, String>("thumbnail", getLabel("thumbnail")) {
			@Override
			public boolean isExpanded() {
				return true;
			}
			@Override
            public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
                        populateItem(cellItem,componentId, resultmodel);
            }
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = null;
				try {
					object = resultmodel.getObject().getObject();
				}
				catch (Exception e) {
					logger.	error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
					cellItem.add(new InvisiblePanel(componentId));
					return;
				}
				cellItem.add(new ImageColumnPanel<Content>(componentId, getModel((Content)object)) {
					@Override
					protected Image getImage(String id) {
						try {
						Content object = (Content) getModel().getObject();
						if (object instanceof ResourceContainer) {
							ResourceContainer rc = 	(ResourceContainer) object; 
							for (KBFile res: rc.getFiles()) {
								if (res.isImage() || res.isVideo())
									return new ResourceThumbnailImage<>(id,  new ObjectModel<Resource>((Resource) res), ThumbnailSize.LARGE);
							}
							if(rc.getFiles().size()>0)
								return new ResourceThumbnailImage<>(id,  new ObjectModel<Resource>((Resource) rc.getFiles().get(0)) , ThumbnailSize.LARGE);
							}
						} 
						catch (Exception e) {
							logger.	error(e, getSessionUser().getUserName());
						}
						return null;
					}
				});
			}
			@Override
			public boolean isExportable() {
				return false;
			}
			@Override
			protected String getContextKey() {
				return MyBoxConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("title"), "title_sort") {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = null;
				try {
					object = resultmodel.getObject().getObject();
				} 
				catch (Exception e) {
					logger.	error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
					cellItem.add(new InvisiblePanel(componentId));
					return;
				}
				IModel<Content> objectmodel = getModel((Content)object);
				cellItem.add(new TitleColumnPanel<Content>(componentId, objectmodel) {
					@Override
					protected Link<?> getNewLink(String id) {
						return new ResourceLink2(id, new ObjectModel<Resource>(getFile(getModel().getObject())));
					}
					@Override
					protected String getCss() {
						return "cell-label btn-link";
					}
				});
			}
			@Override
			public int getDefaultWidth() {
				return GridColumn.DEFAULT_TITLE_COLUMN_WIDTH;
			}
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				//return new Model<Str>
				//Content content = (Content) object.getObject();
				return ()-> ((Content)object.getObject()).getTitle();
			}
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return MyBoxConsole.this.getName() + super.getContextKey();
			}
		});

		

		{
			KbeePredicateGridColumn<Content> filenameColumn = new KbeePredicateGridColumn<>("size", getLabel("size"),	obj -> getSizeColumnDisplayModel(obj).getObject());
			filenameColumn.setContextKey(this.getName() + filenameColumn.getContextKey());
			this.columns.add(filenameColumn);
		}
		
		this.columns.add(new LastModifiedColumn<Content>("modified", getLabel("modified"), "modified") {
			@Override
			protected String getContextKey() {
				return MyBoxConsole.this.getName() + super.getContextKey();
			}
		});

		{
			KbeePredicateGridColumn<Content> filenameColumn = new KbeePredicateGridColumn<Content>("uploaded", getLabel("uploaded"),
				obj -> getDateUploaded(obj).getObject());
			filenameColumn.setContextKey(this.getName() + filenameColumn.getContextKey());
			this.columns.add(filenameColumn);
		}
		
		
		{
			KbeePredicateGridColumn<Content> filenameColumn = new KbeePredicateGridColumn<Content>("uploadedby", getLabel("uploadedby"),
				obj -> obj.getLastModifiedUser().getFirstLastName());
			filenameColumn.setContextKey(this.getName() + filenameColumn.getContextKey());
			this.columns.add(filenameColumn);
		}

	
		return this.columns;
	}

	@Override
	public List<Classifier> getClassifiers() {
		
		if (ct_model==null) {
				logger.error("There must be a ContentTemplate with alias=" + ContentTemplate.RESOURCES);
				throw new KbeeRuntimeException("There must be a ContentTemplate with alias=\" + ContentTemplate.RESORUCES");
		}
		
		ContentTemplate ct = ct_model.getObject();
		List<Classifier> c = new ArrayList<Classifier>();
		for (ClassifierTemplate t:  ct.getClassifiers()) 
			c.add(t.getClassifier());
		return c;
	}

	
	
 	/** ------------------------------
	 * Browser Toolbar
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.items!=null)
			return this.items;
		
		this.items = super.getToolbarItems(browser);
		

		/**--------------------
		 * New Button
		 */
		this.items.add(new ButtonPanelToolbarItem(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			public Panel getPanel(String id) {
				return new UploadPanel(id)	{
					@Override
					public void onUpload(Resource resource) {
						try {
							logger.debug("creating -> " + resource.getName());	
							createContent((KBFile)resource);
						} 
						catch (Exception e) {
							addUploadError( (resource!=null? ("File: " + resource.getTitle()+" (id:"+resource.getId().toString()+")"):"null") + " -> " + e.getClass().getName()+ " | "+ (e.getMessage()!=null?e.getMessage():""));
							logger.error(e);
						}
					}
					@Override
					public void onAfterUpload(AjaxRequestTarget target) {
						try {
							MyBoxConsole.this.setUserPreference(newQuery());
							if (getUploadErrors()!=null) {
								logger.error(getUploadErrors());
								getErrorDialog().open(target, new Model<String>("Error"), new Model<String>(getUploadErrors()));
							}
							MyBoxConsole.this.refresh(target);
						} 
						finally {
							resetUploadErrors();
						}
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						setVisible(false);
						MyBoxConsole.this.refresh(target);
					}
				};
			}
			public IModel<String> getTitle() {
				return new StringResourceModel("upload", MyBoxConsole.this, null);
			}
		});
		
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return MyBoxConsole.this.getName();}, new Model<String>(MyBoxConsole.this.getDescription()));
			}
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		items.add(infoButton);
		
		return items;
	}
	

	
	@Override
	protected boolean isVisible(Facet facet) {
		Facet realfacet;
		if (facet instanceof FacetWrapper) {
			boolean visible = ((FacetWrapper)facet).isVisible(NAME);
			if (!visible) return false;
			realfacet = ((FacetWrapper)facet).getFacet();
		}
		else
			realfacet = facet;
		
		if (realfacet instanceof TaskFacet || realfacet instanceof GroupFacet) 
				return true;
		
		return !realfacet.getName().equals("lastmodifieduser") && !realfacet.getName().equals("usermember") && !realfacet.getName().equals("state");
	}
	
	
	/**
	 * 
	 */
	
	/**
	 * 
	 * @param file_model
	 * @throws ContentMgmtException
	 */
	protected void startProcess(IModel<KBFile> file_model) throws ContentMgmtException {

		if ( this.getSelectedLauncherModel()!=null && getSelectedLauncherModel().getObject()!=null) {
			Process process = startProcess(this.getSelectedLauncherModel().getObject());
			Content content = ((KbeeContext) process.getContext()).getContent();
		
			if (content instanceof ResourceContainer) {
				try {
					ResourceContainer idoc = null;
					if (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("KbeeIDoc")) {
						idoc = (ResourceContainer) getContentDao().findContentById(IDoc.class, content.getId());	
					}
					else if (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("KbeeOrganizationalText")) { 
						idoc = (ResourceContainer) getContentDao().findContentById(OrganizationalText.class, content.getId());
					}
					if (idoc!=null) {
						KBFile file = file_model.getObject();
						idoc.addFile(file, getResourceTag(content.getContentTemplate()));;
						((Content) idoc).setTitle(file.getTitle());
						List<String> ls = new ArrayList<String>();
						ls.add("add: " + file.getTitle());
						((Content) idoc).getService(ContentService.class).update(ls);
					}
					else {
						logger.error(this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId() + " not supported");
					}
				} 
				catch (ServiceNotFoundException e) {
					logger.error(e);
					throw new ContentMgmtException(e);
				}
			}
		}
	}
	
	protected Content startProcess(IModel<ProcessLauncher> launchermodel, IModel<KBFile> filemodel) throws ContentMgmtException {
		Process process = getDomain().getService(WorkflowDomainService.class).startProcess(launchermodel.getObject());
		Content content = ((KbeeContext) process.getContext()).getContent();
		content = (Content)getContentDao().unproxy(content);
		((ResourceContainer)content).addFile(filemodel.getObject(), getResourceTag(content.getContentTemplate()));;
		List<String> ls = new ArrayList<String>();
		ls.add("add: " + filemodel.getObject().getTitle());
		content.getService(ContentService.class).update(ls);
		return content;
	}

	
	/**
	 * 
	 * @param file_model_list
	 * @return
	 * @throws ContentMgmtException
	 */
	protected ResourceContainer startProcess(List<IModel<KBFile>> file_model_list) throws ContentMgmtException {
		
		if (file_model_list.size()==0)
			return null;
		
		if (this.getSelectedLauncherModel()==null)
			throw new ContentMgmtException ("getSelectedLauncherModel()==null");
		
		if (getSelectedLauncherModel().getObject()==null) 
			throw new ContentMgmtException ("getSelectedLauncherModel().getObject()==null");
		
		Process process = startProcess(getSelectedLauncherModel().getObject());
		Content content = ((KbeeContext) process.getContext()).getContent();
			
		if (!(content instanceof ResourceContainer)) 
			throw new ContentMgmtException ("content not instanceof ResourceContainer");
			
		try {
			ResourceContainer idoc= null;
			if      (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("KbeeIDoc"))					idoc = (ResourceContainer) getContentDao().findContentById(IDoc.class, content.getId());	
			else if (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("KbeeOrganizationalText")) 	idoc = (ResourceContainer) getContentDao().findContentById(OrganizationalText.class, content.getId());
			else if (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("TreeIDoc")) 				idoc = (ResourceContainer) getContentDao().findContentById(TreeIDoc.class, content.getId());

			if (idoc==null) { 
				logger.error(this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId() + " not supported");
				throw new ContentMgmtException ("idoc==null");
			}

			List<String> ls = new ArrayList<String>();
					
			for (IModel<KBFile> fm: file_model_list) {
					((KBFileImpl) fm.getObject()).setPublic(true);
					idoc.addFile(fm.getObject(), getResourceTag(content.getContentTemplate()));
					ls.add("add -> " + fm.getObject().getTitle());
			}
				
			((Content) idoc).setTitle(file_model_list.get(0).getObject().getTitle());
			((Content) idoc).getService(ContentService.class).update(ls);
				
			return idoc;
					
		} 
		catch (ServiceNotFoundException e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	protected Process startProcess(ProcessLauncher launcher) {
		Process process = getDomain().getService(WorkflowDomainService.class).startProcess(launcher);
		return process;
	}
	
	/**
	 * TODO: IMPROVE THIS. BY NOW WE SUPPORT ONLY 1 TAG CLASSIFIER (LABELSET) SYSTEM WIDE.
	 * 
	 * @param model
	 * @return
	 */
	protected List<IModel<LabelMember>> getLabelMembers(ContentTemplate ct) {
			if (this.labels.containsKey((Long) ct.getId()))
				return this.labels.get((Long) ct.getId());
			List<IModel<LabelMember>> xl = new ArrayList<IModel<LabelMember>>();
			 List<ClassifierTemplate> list = ct.getClassifiers(); //getDataSet().getClassifiers();
			 for (ClassifierTemplate ca: list) {
				 if (ca.getClassifier() !=null && ca.getClassifier().getState()==ObjectState.ENABLED && (ca.getClassifier().getDataSet() instanceof LabelSet)) {
					 for (DataSetMember dm: getContentDao().getMembers(ca.getClassifier().getDataSet(), "strValue")) {
						 if (dm.getState()==ObjectState.ENABLED)
							 xl.add(new ObjectModel<LabelMember>((LabelMember) dm)); 
					 }
				 }
			 }
			Collections.sort(xl, new Comparator<IModel<LabelMember>>() {
				@Override
				public int compare(IModel<LabelMember> a, IModel<LabelMember> b) {
					try { 
						if (a.getObject()!=null && b.getObject().getDisplayName()==null)
							return -1;
						if (b.getObject()!=null && a.getObject().getDisplayName()==null)
							return -1;
						return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
					} catch (Exception e) {
						logger.error(e);
						return 0;
					}
				}
			});
			this.labels.put((Long) ct.getId(), xl);
			return this.labels.get((Long) ct.getId());
	}
	
	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new MyBoxBC());
	}
	
	@Override
	protected String getSectionDisplayName(String key) {
		return new StringResourceModel(key, MyBoxConsole.this, null).getString();
	}

	private ResourceTag getResourceTag(ContentTemplate template) {
		ResourceTag selected = null;
		for (ResourceTag tag : template.getResourceTags()) {
			if (selected==null) {
				selected = tag;
			}
			if (tag.isDefault()) { 
				selected = tag;
				break;
			}	
		}
		return selected;
	}
	
	private IModel<String> getDateUploaded(Content obj) {
		try {
			KBFile file = getFile(obj);
			if (file!=null)
				return new Model<String>(file.getUploadOffsetDateTimeColloquial());
			return new Model<String>("");
		} 
		catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getName());
		}
	}
	
	private IModel<String> getSizeColumnDisplayModel(Content content) {
		KBFile file = getFile(content);
		return file!=null?new Model<String>(NumberFormatter.formatFileSize(file.getSize())):new Model<String>("0 bytes");
	}
	
	private KBFile getFile(Content content) {
		if (content instanceof IDoc) {
			 List<KBFile> list=((IDoc) content).getFiles();
			 if (list!=null && !list.isEmpty()) {
				 for (KBFile file: list) {
					 if (file.getSize()>0)
						 return file;
				 }
			 }
		}	 
		return null; 
	}
	
	@Override
	protected Panel getTopPanel() {
		try {
			return new AdvancedSearchContentSelectorPanel("top", getName());
		} 
		catch (Exception e) {
			logger.	error(e);
			return new SearcherSimpleErrorPanel("top", e.getClass().getSimpleName(), e.getMessage());
		}
	}
	
	@Override					
	protected Panel getPanel(IModel<Content> model, List<String> snippets) {
		return new ExpandedPanel<Content>("editor", this, model, snippets);
	}
	
	@Override
	protected Panel getPanel(IModel<Content> model) {
		return new ExpandedPanel<Content>("editor", this, model);
	}
	
	// No navega. Retorna despues de una accion a la consola
	@Override
	protected Navigator<Content> getNavigator(IModel<Content> model) {
		return null;
	}
	
	@Override
	protected boolean isWorkflowConsole() {
		return true;
	}
	
	@Override
	protected boolean isReadOnly() {
		return false;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return true;
	}
	
	@Override
	protected boolean hasTopPanel() {
		return false;
	}
	
	@Override
	protected boolean isEditionEnabled() {
		return true;
	}

}
