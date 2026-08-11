package com.novamens.content.web.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.FileSystemResourceReference;
import org.danekja.java.util.function.serializable.SerializableFunction;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.TreeFileContainer;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.web.admin.files.DMTextFileEditorPanel;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchButton;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.LabelItem;
import com.novamens.kbee.wicket.markup.html.console.browser.LauncherButton;
import com.novamens.kbee.wicket.markup.html.console.browser.LauncherSelectorEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.LinkButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.ImageColumnPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.SimpleDateColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.user.PreferencesService;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Process;

import kbee.util.FSUtils;
import kbee.util.PropertiesFactory;
import kbee.web.console.AbstractSimpleConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.AjaxLinkPredicateKbeeGridColumn;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.dashboard.LabelPanel;
import kbee.web.nav.DropDownMenuBC;

/**
 *
 */
public abstract class FileSystemIntegrationConsole extends AbstractSimpleConsole<File> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FileSystemIntegrationConsole.class.getName());
	
	final boolean role_file_server   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.FILE_SERVER.getId());
	final boolean role_admin   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	

	private List<ToolbarItem> selection_toolbar;
	private List<ToolbarItem> items;
	private List<GridColumn<SearchResult,String>> columns;
	
	private List<IModel<ProcessLauncher>> launchers;
	private IModel<ProcessLauncher> selected_launcher_model;
	
	private String drive_dir_name;
	private String home_dir_name;
	
	private File home_dir = new File(getDriveDir()+File.separator+getHomeDir()+File.separator+getUsernamePrefix());
	
	public FileSystemIntegrationConsole() {
		super("file_server", null);
		setOutputMarkupId(true);
	}
	
	
	
	public FileSystemIntegrationConsole(Query query) {
		super("file_server", query);
		setOutputMarkupId(true);
	}

	
	@Override
	protected Panel getItemListPanel(IModel<File> model, int index) {
		return new LabelPanel("item", new Model<String> (model.getObject().getName()));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.columns!=null)
			getColumns().forEach(item -> item.detach());
		
		if (this.items!=null) 
			this.items.forEach(item -> item.detach());
		
		if (this.selection_toolbar!=null) 
			selection_toolbar.forEach(item -> item.detach());
		
		 if (selected_launcher_model!=null)
			 selected_launcher_model.detach();
		 
		if (launchers!=null)
			launchers.forEach(item -> item.detach());
		
	}

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (getQuery()==null) {  
			setQuery(newQuery());
			setUserPreference(getQuery());
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		
		initiFS();
		
		add(new InvisiblePanel("fs-upload-panel"));
		
		MenuBreadCrumbPanel bc =new MenuBreadCrumbPanel();
		DropDownMenuBC dd = new DropDownMenuBC();
		dd.addElement(new IntegrationBC(), true);
		dd.addElement(new FileServerBC());
		bc.addElement(dd);
		bc.addElement(new FileServerBC());
		add(bc);
		
													
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getDriveDir() {
		if (drive_dir_name==null)
			drive_dir_name = ServiceLocator.getService(ApplicationServerService.class).getDriveDir();
			// drive_dir_name = ServiceLocator.getService(SystemParameterService.class).getParameter("integration.drive.home", "."+File.separator+"drive");
		return drive_dir_name;
	}
	
	
	public String getHomeDir() {
		if (home_dir_name==null)
			home_dir_name=getDomain().getName().replace(" ", "").toLowerCase().trim();
		return home_dir_name;
	}

	
	
	
	
	protected BreadCrumb getBreadCrumb() {
		return null;
	};

	
	/** 
	 * Browser toolbar
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<File> browser) {
		
		if (this.items!=null)
			return this.items;
		
		this.items = new ArrayList<ToolbarItem>();
		
		items.add(new LinkButton( browser, ToolbarItem.Align.TOP_LEFT) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					String folder=((LocalFSQuery)getQuery()).getDirectory().getAbsolutePath();
					IModel<Domain> model = new ObjectModel<Domain>(getDomain());
					String r_path=
							((LocalFSQuery)getQuery()).getRootDir().getParentFile() != null ?
							((LocalFSQuery)getQuery()).getRootDir().getParentFile().getAbsolutePath() :
							((LocalFSQuery)getQuery()).getRootDir().getAbsolutePath();
					setResponsePage(new FileUploadPage(model, getQuery(), folder, !isDomainKbee()?folder.replace(r_path,  ""):folder));
				}
				
				@Override
				public boolean isVisible() {
					return role_admin || isRoot();
				}
				
				@Override
				public boolean isEnabled() {
					return true;
				}
				
				@Override
				protected String getAnchorTitle() {
					return new StringResourceModel("upload", this, null).getObject();
				}
		
				@Override
				public IModel<String> getIconCss() {
					return new Model<String>("fal fa-cloud-upload"); 
				}
				
				@Override
				public IModel<String> getLabel() {
					return   new StringResourceModel("upload", this, null); //new StringResourceModel("tools.archive", this, null).getObject();
				}
			});
			
			items.add(new FileSystemBreadcrumbToolbarItem(	 browser, ToolbarItem.Align.BOTTOM_LEFT));
			
			
			InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					InfoDialog infoDialog = (InfoDialog) getInformationModal();
					infoDialog.open(target,() -> {return  FileSystemIntegrationConsole.this.getName();}, new Model<String>( FileSystemIntegrationConsole.this.getDescription()));
				}
				
				@Override
				public boolean isVisible() {
					return true;
				}
			};
			
			this.items.add(infoButton);
			
		return items;
	}
	
	
	protected void setSelectedLauncherModel( IModel<ProcessLauncher> model) {
		this.selected_launcher_model=model;
	}
	
	
	
	protected IModel<ProcessLauncher> getSelectedLauncherModel() {
		return this.selected_launcher_model;
	}
	

	
	protected List<IModel<ProcessLauncher>> getLaunchers() {
		if (this.launchers!=null)
			return this.launchers;
		this.launchers = new ArrayList<IModel<ProcessLauncher>>();
		List<ProcessLauncher> list = getDomain().getService(WorkflowDomainService.class).getLaunchers();
		for (ProcessLauncher launcher: list) {
			if (launcher.isEnabled() && launcher.executeable() && launcher.getContentTemplate().getState()==ObjectState.ENABLED) 
					this.launchers.add( new ObjectModel<ProcessLauncher>(launcher));
		}
		return this.launchers;
	}


	/**
	 * 
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<File> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		this.selection_toolbar.add(new LabelItem(browser, ToolbarItem.Align.TOP_LEFT, new StringResourceModel("create", this, null)));
		
		for (IModel<ProcessLauncher> la: getLaunchers() ) {
			this.selection_toolbar.add(new LauncherButton(la, browser, ToolbarItem.Align.TOP_LEFT) {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isEnabled() {
					if (getBrowser().getSelection().isEmpty())
							return false;
						return true;
				}
			});
		}
		
		return this.selection_toolbar;
	
	}
	
	@Override
	public boolean isSelectionEnabled() {
		return true;
	}
	
	@Override
	protected IModel<File> getModel(File object) {
		return new LocalFileModel(object);
	}
	
	
	@SuppressWarnings("serial")
	@Override
	protected Panel getMenu(IModel<File> model) {
						
		ContextMenuPanel<File> menu = new ContextMenuPanel<File>(model);
						
		menu.setOutputMarkupId(true);
	
		/*
		menu.addItem(new MenuItemFactory<File>() {
			private static final long serialVersionUID = 1L;
			@Override			
			public MenuItemPanel<File> getItem(String id) {
				return new MenuItemPanelV5<File>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						open(getModel().getObject());
					}
					@Override 
					public String getLabel() {
						return "Open File";
						//return FileSystemIntegrationConsole.this.getLabel("contextmenu.open").getObject();
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
					
					@Override
					public boolean isVisible()  {
						File file = (File) getModel().getObject();
						if (file.exists() && !file.isDirectory())
							return true;
						return false;
					}
				};
			}
		});
		*/
		
 		menu.addItem(new MenuItemFactory<File>() {
			private static final long serialVersionUID = 1L;
			@Override			
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new AjaxMenuItemPanelV5<File>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) throws Exception {
						File file = (File) getModel().getObject();
						if (file.exists() && file.isDirectory()) {
							((LocalFSQuery) getQuery()).setDirectory(file);
							fire(new LocalFSDirClickEvent<File>(target, (LocalFSQuery) getQuery(), getModel(), 0));
						}
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return new StringResourceModel("opendir", FileSystemIntegrationConsole.this, null).getObject(); // "Open Directory";
						//return FileSystemIntegrationConsole.this.getLabel("contextmenu.open").getObject();
					}
					@Override
					public boolean isVisible()  {
						File file = (File) getModel().getObject();
						if (file.exists() &&  file.isDirectory())
							return true;
						return false;
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<File>() {
			private static final long serialVersionUID = 1L;
			@Override				
			public AbstractMenuItemPanelV5<File> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<File>(id) {
						private static final long serialVersionUID = 1L;

						@Override 
						public String getLabel() {
							return FileSystemIntegrationConsole.this.getLabel("contextmenu.download").getObject();
						}
						
						@Override
						protected File getFile() {
								File file = (File) getModel().getObject();
									if (!file.exists()) {
										logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + " file "  + ((KBFile) getModel().getObject()).getUrl() +  "  does not exists");
										return null;
									}
									return file;
						}
						@Override
						public boolean isVisible()  {
							File file = (File) getModel().getObject();
							if (file.exists() &&  !file.isDirectory())
								return true;
							return false;
						}
						@Override
						public boolean isEnabled()  {
							try {
								File file = (File) getModel().getObject();
								if (!file.exists() || file.isDirectory())
									return false;
								if (isRoot())
									return true;
								return (!isSupportUser());
							} catch (Exception e) {
								return false;
							}
						}
					};
			}
		});
		
		
		// KBEE DOMAIN --------------------------------
		//
		//
		menu.addItem(new MenuItemFactory<File>() {
			@Override
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new MenuItemPanelV5<File>(id) {
					@Override
					public void onClick() {
						try {
							PageParameters pa = new PageParameters();
							pa.add("file", getModel().getObject().getAbsolutePath().toString());
							pa.add("id", "dm-text-file-editor-panel");
							setResponsePage(new SystemDataManagementGeneralPage(pa));
							//setResponsePage( new SystemDataManagementGeneralPage(new DMTextFileEditorPanel(getModel()), "textfile-editor"));
						} 
						catch (Exception e) {
							logger.error(e);
						}
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
					@Override 
					public String getLabel() {
						return new StringResourceModel("edit", FileSystemIntegrationConsole.this, null).getObject(); //"Edit";
					}
					@Override
					public boolean isVisible() {
						return isDomainKbee() && isRoot() && isTextEditable(getModel());
					}
					@Override
					public boolean isEnabled() {
						return isDomainKbee() && isRoot() && isTextEditable(getModel());
					}
				};
			}
		});
		
 		
		
		menu.addItem(new MenuItemFactory<File>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new SeparatorMenuItemPanelV5<File>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getCssClass() {
						return "divider";
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<File>() {
			private static final long serialVersionUID = 1L;
			@Override			
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new AjaxMenuItemPanelV5<File>(id) {
					private static final long serialVersionUID = 1L;
					@Override 
					public String getLabel() {
						return new StringResourceModel("sendrecycle", FileSystemIntegrationConsole.this, null).getObject();
 					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						deleteRecyleBin(getModel().getObject());
						getQuery().execute();
						refresh(target);
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<File>() {
			private static final long serialVersionUID = 1L;
			@Override			
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new AjaxMenuItemPanelV5<File>(id) {
					private static final long serialVersionUID = 1L;
					@Override 
					public String getLabel() {
						return new StringResourceModel("delete", FileSystemIntegrationConsole.this, null).getObject();
 					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						delete(getModel().getObject());
						getQuery().execute();
						refresh(target);
					}
				};
			}
		});

		return menu;
	}

	
	protected OffsetDateTime getOffsetDateTime(long lastModified) {
		return ServiceLocator.getService(DateTimeService.class).getOffsetDateTime(lastModified, getSessionUser());
	}
	
	
	/***
	 * 
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		{
	   		this.columns.add(new GridColumn<SearchResult, String>("thicon", getLabel("thcolumn")) {
				
	   			private static final long serialVersionUID = 1L;
	   			
				@Override
				public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
					Object object = resultmodel.getObject().getObject();
					IModel<File> objectmodel = getModel((File)object);
					
					cellItem.add(new ImageColumnPanel<File>(componentId, objectmodel) {
						private static final long serialVersionUID = 1L;
						@Override
						protected Image getImage(String id) {
							File object = (File) getModel().getObject();
							ThumbnailService ths = ServiceLocator.getService(ThumbnailService.class);
							String domain_name=getDomain().getName();
							File thfile;
							try {
								thfile = ths.getThumbnailFile( String.valueOf(object.hashCode()), domain_name, object, ThumbnailSize.MINI);
								Path path=FileSystems.getDefault().getPath(thfile.getAbsolutePath());
								FileSystemResourceReference ref=new FileSystemResourceReference(object.getName(), path);
								Image image = new Image(id, ref) {
									private static final long serialVersionUID = 1L;
									protected boolean shouldAddAntiCacheParameter()	{
										return false;
									}
								};
								return image;
							} catch (IOException e) {
								logger.debug(e);
								return new GenericPhoto("photo");
							}
						}
					});
				}
				@Override
				public boolean isExportable() {
					return false;
				}

				@Override
				public boolean isPreferred() {
					return false;
				}
				@Override
				protected String getContextKey() {
					return FileSystemIntegrationConsole.this.getName() + super.getContextKey();
				}
			});

			
			
			columns.add(new GridColumn<SearchResult, String>("glyphicon", getLabel("iconcolumn"), "icon") {
				private static final long serialVersionUID = 1L;
				@Override
				public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
					try {
						Object object = resultmodel.getObject().getObject();
						IModel<File> objectmodel = getModel((File)object);
 	 					
						cellItem.add(new com.novamens.kbee.wicket.markup.html.console.grid.GlyphiconColumnPanel<File>(componentId, objectmodel) {
							private static final long serialVersionUID = 1L;
										@Override
										protected String getGlyphiconClass() {
											try {
													return FSUtils.getGlyphIcon(getModel().getObject()); 
											} catch (Exception e) {
												logger.error(e);
												return "";
											}
							 			}
										
										@Override
										protected String getCss() {
											return "iconcolumn" + ((getModel().getObject()!=null && getModel().getObject().isDirectory()) ? " directory " : "");
										}
							});
					 
						} catch (Exception e) {
						logger.error(e, getSessionUser().getUserName());
						cellItem.add(new Label(componentId, e.getClass().getName())); 
					}
				}

		 		
		 		@Override
				protected String getContextKey() {
		 			return FileSystemIntegrationConsole.this.getName() + super.getContextKey();
				}
		 		
		 		public boolean isExportable() { 
		 			return false; 
		 		}
		 		

	    	});
		}

		{
			LinkPredicateKbeeGridColumn<File> titleColumn =	new AjaxLinkPredicateKbeeGridColumn<File>("title", getLabel("column.name"), "title_sort",
					obj ->  obj.isDirectory() || obj.getName().startsWith(".")?obj.getName():FilenameUtils.getBaseName((obj).getName()), obj -> getModel(obj));
			titleColumn.setCssValueResolver(obj -> ((obj.isDirectory() ?  " directory " : "") +  " col title col-xs-1 col-md-1 col-lg-1" ));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}
		
		
		{
			this.columns.add(new SimpleDateColumn<File>("modified", getLabel("modified"), "modified") {
				private static final long serialVersionUID = 1L;
				@Override
				protected String getContextKey() {
					return FileSystemIntegrationConsole.this.getName() + super.getContextKey();
				}
				protected OffsetDateTime getOffsetDateTime(File object) {
					if (object==null)
						return null;
					return FileSystemIntegrationConsole.this.getOffsetDateTime(object.lastModified());
				}
			});
		}
		
		{
			this.columns.add(new GridColumn<SearchResult, String>("size", getLabel("column.size"), "size") {
				private static final long serialVersionUID = 1L;
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {		
					try {
						File file = (File) object.getObject();
						if (!file.exists() || file.isDirectory())
							return new Model<String>("");
						return new Model<String>(ServiceLocator.getService(DateTimeService.class).formatFileSize(((File) object.getObject()).length(), getSessionUser().getLocale()));
					} catch (Exception e) {
						logger.error(e);
						return new Model<String>(e.getClass().getName());
					}
				}
				@Override
				protected String getContextKey() {
					return FileSystemIntegrationConsole.this.getName() + super.getContextKey();
				}
			});
		}

		{																									
			KbeePredicateGridColumn<File> statusColumn = new KbeePredicateGridColumn<File>("type", getLabel("column.type"), "type",
					obj -> obj.isDirectory()?"dir":FilenameUtils.getExtension(obj.getName()));
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			statusColumn.setPreferred(false);
			this.columns.add(statusColumn);
		}
		
				
		{																																						
			KbeePredicateGridColumn<File> rColumn = new KbeePredicateGridColumn<File>("rights", getLabel("column.rights"), null, obj -> FileSystemIntegrationConsole.this.getRights(obj));
			rColumn.setContextKey(this.getName() + rColumn.getContextKey());
			rColumn.setPreferred(false);
			this.columns.add(rColumn);
		}

		return columns;
	}


	
	/**
	 * 
	 * 
	 */
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("content-header"));
			}
		});

		add(new WicketEventListener<ClickEvent<File>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<File> event) {
				File file = event.getModelObject();
				if (file.exists() && file.isDirectory()) {
					if (event.getRequestTarget()!=null) {
						((LocalFSQuery) getQuery()).setDirectory(file);
						fire(new LocalFSDirClickEvent<File>(event.getRequestTarget(), (LocalFSQuery) getQuery(), event.getModel(), event.getIndex()));
						FileSystemIntegrationConsole.this.refresh(event.getRequestTarget());
					}
				}
				else 
					FileSystemIntegrationConsole.this.open(file);
			}
		});
		
		add(new WicketEventListener<LauncherSelectorEvent<ProcessLauncher>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(LauncherSelectorEvent<ProcessLauncher> event) {
				FileSystemIntegrationConsole.this.setSelectedLauncherModel(event.getModel());
				addOneForAll(event.getRequestTarget());
			}
		});
	}
	
	protected Panel getPanel(IModel<File> model) {
		return new ExpandedPanel<File>("editor", this, model);
	}
	
											
	protected Panel getPanel(IModel<File> model, List<String> list) {
		return new ExpandedPanel<File>("editor", this, model, list);
	}
	
	@Override
	protected boolean hasExpander() {
		return false;
	}

	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	private void open(File file) {
		return;
	}
	
	
	/**
	 * 
	 * Deletes file or if it is a dir, the dir and its subdirs
	 * @param object
	 * 
	 */
	 private void delete(File object) {
		 logger.debug("removing: " + object!=null? object.getAbsolutePath():"null");
		 KbeeFileUtils.deleteQuietly(object);
	 }

	private String getWorkDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getDriveDir() + File.separator + "recyclebin" + File.separator + getDomain().getName() + File.separator + getSessionUser().getUitheme();
	}
	
	private void deleteRecyleBin(File file) {
		try {
			if (!file.exists())
				return;
			
			if (!file.isDirectory()) {
				DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");
				File candidate = new File(getWorkDir() + File.separator + workdf.format(LocalDateTime.now()) + File.separator + file.getName());
				int n=0;
				while (candidate.exists())
					candidate = new File(getWorkDir() + File.separator + workdf.format(LocalDateTime.now()) + File.separator + file.getName()+String.valueOf(++n));
				File destfile = candidate;
				FileUtils.moveFile(file, destfile);
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	protected void addOneForAll(AjaxRequestTarget target) {
		if (getSelectedLauncherModel()==null || getSelectedLauncherModel().getObject()==null) {
			getErrorDialog().open(target, new Model<String>("ERROR"), new Model<String>("Please Select a Content Class"));
			refresh(target);
			return;
		}
		boolean has_errors = false;
		StringBuilder str = new StringBuilder();
		List<IModel<File>> limodel = this.getBrowser().getSelection();
			try {
				startProcess(limodel);
			} catch (ContentMgmtException e) {
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
	}

	
	/**
	 * 
	 * @param file_model_list
	 * @throws ContentMgmtException
	 */
	protected void startProcess(List<IModel<File>> file_model_list) throws ContentMgmtException {
		
		if (file_model_list.size()==0)
			return;
		
		if (this.getSelectedLauncherModel()==null)
			throw new ContentMgmtException ("getSelectedLauncherModel()==null");
		
		if (getSelectedLauncherModel().getObject()==null) 
			throw new ContentMgmtException ("getSelectedLauncherModel().getObject()==null");
			
		
		String label = this.getSelectedLauncherModel().getObject().getLabel();
		Process process = getDomain().getService( WorkflowDomainService.class).startProcess(label);
		Content content = ((KbeeContext) process.getContext()).getContent();

		if (!(content instanceof ResourceContainer)) 
			throw new ContentMgmtException ("content not instanceof ResourceContainer");
		
		List<File> files_to_delete = new ArrayList<File>();
					
		try {
		
			ResourceContainer idoc = null;
																																			
			if      (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("KbeeIDoc"))					idoc = (ResourceContainer) getContentDao().findContentById(IDoc.class, content.getId());	
			else if (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("KbeeOrganizationalText")) 	idoc = (ResourceContainer) getContentDao().findContentById(OrganizationalText.class, content.getId());
			else if (this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId().equals("TreeIDoc")) 				idoc = (ResourceContainer) getContentDao().findContentById(TreeIDoc.class, content.getId());
			
			if (idoc==null) { 
				logger.error(this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId() + " not supported");
				throw new ContentMgmtException ("idoc==null");
			}
			
			boolean has_title = false;

			List<String> ls = new ArrayList<String>();
			
			for (IModel<File> mfile: file_model_list) {
				logger.debug(mfile.getObject().getName());
			}
			
			for (IModel<File> mfile: file_model_list) {
				
					File file = mfile.getObject();
					
					if (file.isDirectory()) {
							if (idoc instanceof TreeFileContainer) {
								
								TreeFile kbfile = getDomain().getService(DomainService.class).importTreeFileFromLocalDisk(file);
								((TreeFileContainer) idoc).setTreeFile(kbfile);
								logger.debug("Adding to idoc " + kbfile.getTitle());
								 
								if (!has_title) {
									((Content) idoc).setTitle(kbfile.getTitle());
									has_title=true;
								}
								
								ls.add("add: " + kbfile.getTitle());
								 files_to_delete.add(file);
								 
							} else {
								logger.error("idoc is not TreeFileResourceContaniner");		
								throw new ContentMgmtException ("idoc is not TreeFileResourceContaniner");
							}
					}
					else {
							logger.debug("file");
							
							KBFile kbfile = getDomain().getService(DomainService.class).importFileFromLocalDisk(file);
							((KBFileImpl) kbfile).setPublic(true);
							 
							idoc.addFile(kbfile);
							
							logger.debug("Adding to idoc " + kbfile.getTitle());
							
							if (!has_title) {
									((Content) idoc).setTitle(kbfile.getTitle());
									has_title=true;
							}
								
							ls.add("add: " + kbfile.getTitle());							
							files_to_delete.add(file);
					}
			}
			
			((Content) idoc).getService(ContentService.class).update(ls);
			files_to_delete.forEach(item -> deleteRecyleBin(item));
			

		} catch (ServiceNotFoundException e) {
				logger.error(e);
				throw new ContentMgmtException(e);
		}
		finally {
			
			
		}
	}

	@Override
	protected boolean hasTopPanel() {
		return false;
	}
	
	@Override
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	
	@Override
	public Query newQuery() {
		return new LocalFSQuery(home_dir, true);
	}


	private void initiFS() {
		
 		File base = new File(getDriveDir());
 		
 		if (!base.exists()) {
 			synchronized (this) {
	 			try {
					KbeeFileUtils.forceMkdir(base);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}
 		else if (!base.isDirectory()) {
 			synchronized (this) {
 				logger.debug(base.getAbsolutePath());
	 			KbeeFileUtils.deleteQuietly(base);
	 			try {
					KbeeFileUtils.forceMkdir(base);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}
 			
 		if (!home_dir.exists()) {
 			synchronized (this) {
	 			try {
					KbeeFileUtils.forceMkdir(home_dir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}
 		else if (!home_dir.isDirectory()) {
 			synchronized (this) {
 				logger.debug(home_dir.getAbsolutePath());
	 			KbeeFileUtils.deleteQuietly(home_dir);
	 			try {
					KbeeFileUtils.forceMkdir(home_dir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}

 		if (!home_dir.exists()) {
 			synchronized (this) {
	 			try {
					KbeeFileUtils.forceMkdir(home_dir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}
 		
 		else if (!home_dir.isDirectory()) {
 			synchronized (this) {
	 			KbeeFileUtils.deleteQuietly(home_dir);
	 			try {
					KbeeFileUtils.forceMkdir(home_dir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}
 		
 		File workdir = new File(getWorkDir());
 		
 		if (!workdir.exists()) {
 			synchronized (this) {
	 			try {
					KbeeFileUtils.forceMkdir(workdir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}
 		else if (!workdir.isDirectory()) {
 			synchronized (this) {
 				logger.debug(workdir);
	 			KbeeFileUtils.deleteQuietly(workdir);
	 			try {
					KbeeFileUtils.forceMkdir(workdir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
 			}
 		}
	}
	

	/**
	 * @param col
	 * @return
	 */
	private String getRights(File col) {
		StringBuilder str = new StringBuilder();
		try {
			str.append(col.canRead()	? "r":"_" );
			str.append(col.canWrite()	? "w":"_" );
			str.append(col.canExecute()	? "x":"_" );
		} catch (Exception e) {
			str.append(e.getClass().getSimpleName());
		}
		return str.toString();
	}


	private String getUsernamePrefix() {
		return getSessionUser().getUserName().split("@")[0];
	}

	
	private boolean isTextEditable(IModel<File> model) {
		if (FSUtils.isText(model.getObject()))
			return true;
		return false;
 	}


	@SuppressWarnings("unused")
	private String getPreference(String name) {
		String value = getSessionUser().getService(PreferencesService.class).getValue(getName() + "-browser", name);
		return value;
	}
	
	
	@SuppressWarnings("unused")
	private void setPreference(String name, String value) {
		getSessionUser().getService(PreferencesService.class).setValue(getName() + "-browser", name, value);
	}

}























