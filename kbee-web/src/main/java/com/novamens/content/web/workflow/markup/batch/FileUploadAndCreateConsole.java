package com.novamens.content.web.workflow.markup.batch;


import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;

import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;

import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.LabelItem;
import com.novamens.kbee.wicket.markup.html.console.browser.LauncherButton;
import com.novamens.kbee.wicket.markup.html.console.browser.LauncherSelectorEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.SeparatorToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.ImageColumnPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;

import com.novamens.wicket.model.ObjectModel;
 
import com.novamens.workflow.Process;

import kbee.util.NumberFormatter;
import kbee.web.console.AbstractSimpleConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.query.ListModelQuery;
import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.resource.UploadPanel;
import kbee.web.resource.WebResourceReference;




public abstract class FileUploadAndCreateConsole extends AbstractSimpleConsole<KBFile> {
						
	private static final long serialVersionUID = 1L;
					
	static public String KEY = "uploadandcreate";
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FileUploadAndCreateConsole.class.getName());
	
	public static int STATE_INITIAL = 1;
	public static int STATE_IMAGES_LOADED = 2;
	
	private int state = STATE_INITIAL;
	
	private  List<IModel<KBFile>> list_model = null;

	private IModel<IDoc> kbfiles_container_model;
	
	private List<IModel<ProcessLauncher>> launchers;
	private IModel<ProcessLauncher> selected_launcher_model;
	private List<ToolbarItem> selection_toolbar;

	
	private List<GridColumn<SearchResult,String>> columns;
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FileUploadAndCreateConsole(Query query, IModel<IDoc> idoc_model) {
		super(KEY, query);
		
		setOutputMarkupId(true);
		
		setUserPreference(query);
		this.kbfiles_container_model = idoc_model;
		this.list_model=((ListModelQuery) query).getListModel();
	}
	
	
	@Override
	 protected  IModel<KBFile> getModel(KBFile object) {
			return new ObjectModel<KBFile>(object, true);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		// this is to force display the upload panel 
		// 
		String pref=getPreference("sidepanel");
		if ( (pref==null)  || (!pref.equals(UploadPanel.class.getName()))) {
			setPreference("sidepanel", UploadPanel.class.getName());
		}
	}

	public IModel<IDoc> getContainerModel() {
		return kbfiles_container_model;
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
			if (launcher.isEnabled() && launcher.executeable() && launcher.getContentTemplate().getState()==ObjectState.ENABLED) 
					this.launchers.add( new ObjectModel<ProcessLauncher>(launcher));
		}
		return this.launchers;
	}

	
	@Override
	public void onDetach() {
		
		if (list_model!=null) {
			for (IModel<KBFile> model: list_model)
				model.detach();
		}		
		
		if (launchers!=null) 
			launchers.forEach(item -> item.detach());
		
		if (selected_launcher_model!=null)
			selected_launcher_model.detach();
		
		if (kbfiles_container_model!=null)
			kbfiles_container_model.detach();
		
		this.columns=null;
		
		if (this.selection_toolbar!=null)
			selection_toolbar.forEach(item -> item.detach());
		
		super.onDetach();
	}

	
	public void setState(int state) {
		this.state=state;
	}

	
	public int getState() {
		return this.state;
	}
			
 
	@Override
	public void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		getBrowser().reload(target);
	}
	
	/**
	 * Filters for the Search engine. There is no Search enabled in this page
	 */
	@Override
	protected boolean isFiltersEnabled() {
	 	return false;
	}
	
	

	/**
	 * Selection 
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<KBFile> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
		this.selection_toolbar.add(new LabelItem(browser, ToolbarItem.Align.TOP_LEFT, 
				new StringResourceModel("create", FileUploadAndCreateConsole.this, null)));
		
		int n=0;
		for (IModel<ProcessLauncher> la: getLaunchers() ) {
		
			if (n++>0)
				this.selection_toolbar.add(new SeparatorToolbarItem(browser));
			
			this.selection_toolbar.add(new LauncherButton(la, browser, ToolbarItem.Align.TOP_LEFT,  new StringResourceModel("one-for-each", FileUploadAndCreateConsole.this, null).getObject(), "one-for-each") {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isEnabled() {
					if (getBrowser().getSelection().isEmpty())
							return false;
						return true;
				}
			});
			
			this.selection_toolbar.add(new LauncherButton(la, browser, ToolbarItem.Align.TOP_LEFT, new StringResourceModel("one-for-all", FileUploadAndCreateConsole.this, null).getObject(), "one-for-all") {
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
	
		

	
	/**  
	 * 
	 * Select ContentClass
	 * 
	 * Create one for each
	 * Create one for all
	 *  
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<KBFile> browser) {
		
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		// if there is only 1 content class. The Selector is invisible ---------------------------------------------------------------------
		return items;
	}
	
	
	
	
	protected void addOneForEach(AjaxRequestTarget target) {
		
			if (getSelectedLauncherModel()==null || getSelectedLauncherModel().getObject()==null) {
					getErrorDialog().open(target, new Model<String>("ERROR"), new Model<String>("Please Select a Content Class"));
					refresh(target);
					return;
			}

			boolean has_errors = false;
			StringBuilder str = new StringBuilder();
				
			List<IModel<KBFile>> limodel = this.getBrowser().getSelection();

			boolean has_changed = false;
			
			for (IModel<KBFile> model: limodel) {
				try {
					startProcess(model);
					this.list_model.remove(model);
					
					IDoc idoc = this.getContainerModel().getObject();
					idoc.removeFile(model.getObject());
					idoc.getService(ContentService.class).update();
					has_changed = true;
				
				} catch (ContentMgmtException e) {
					has_errors = true;
					str.append("File: " + model.getObject().getTitle() + " | Error: " + e.getMessage() + " <br />");
					logger.error(e);
				}
			}

			/**
			if (has_changed) {
				try {
					IDoc idoc = this.getContainerModel().getObject();
					List<KBFile> fi = new ArrayList<KBFile>();
					for (IModel<KBFile> md: this.list_model) {
						fi.add(md.getObject());
					}
					idoc.setFiles(fi);
					
					long start=System.currentTimeMillis();
					idoc.getService(ContentService.class).update();
					logger.debug("ContentService.class).update() -> "+ String.valueOf(System.currentTimeMillis()-start)+" ms");
					
					
	
				} catch (ContentMgmtException e) {
					has_errors=true;
					str.append(e.getMessage());
					logger.error(e);

					
				} catch (ServiceNotFoundException e) {
					has_errors=true;
					str.append(e.getMessage());
					logger.error(e);
				}
			}
			**/
			
			if (has_errors) {
				getErrorDialog().open(target, new Model<String>(str.toString()));
			}
			
			
			setUserPreference(newQuery());
			
			getBrowser().resetSelection();
			refresh(target);
	}
	

	
	/**
	 * 
	 * 
	 * @param target
	 */
	protected void addOneForAll(AjaxRequestTarget target) {

		if (getSelectedLauncherModel()==null || getSelectedLauncherModel().getObject()==null) {
			getErrorDialog().open(target, new Model<String>("ERROR"), new Model<String>("Please Select a Content Class"));
			refresh(target);
			return;
		}

		boolean has_errors = false;
		boolean has_changed = false;
		
		StringBuilder str = new StringBuilder();
					
		List<IModel<KBFile>> limodel = this.getBrowser().getSelection();
		
		try {
				startProcess(limodel);
				IDoc idoc = this.getContainerModel().getObject();
				for (IModel<KBFile> model: limodel) {
					this.list_model.remove(model);
					idoc.removeFile(model.getObject());
					has_changed = true;
					logger.info("removing " + model.getObject().getTitle());
				}
				idoc.getService(ContentService.class).update();
			
		} catch (ContentMgmtException e) {
				has_errors = true;
				str.append(e.getMessage());
				logger.error(e);
		}

		/**
			if (has_changed) {
				try {
					IDoc idoc = this.getContainerModel().getObject();
					List<KBFile> fi = new ArrayList<KBFile>();
					for (IModel<KBFile> md: this.list_model) {
						fi.add(md.getObject());
					}
					idoc.setFiles(fi);
					long start=System.currentTimeMillis();
					idoc.getService(ContentService.class).update();
					logger.debug("ContentService.class).update() -> "+ String.valueOf(System.currentTimeMillis()-start)+" ms");
				} catch (ContentMgmtException e) {
					logger.error(e);
					has_errors=true;
					str.append(e.getMessage());
					
				} catch (ServiceNotFoundException e) {
					has_errors=true;
					str.append(e.getMessage());
					logger.error(e);
				}
			}
		**/

		if (has_errors) {
			getErrorDialog().open(target, new Model<String>(str.toString()));
		}
		
		setUserPreference(newQuery());
		getBrowser().resetSelection();
		refresh(target);
		
	}


	public ProcessLauncher getSelectedLauncher() {
		if (this.selected_launcher_model!=null  && this.selected_launcher_model.getObject()!=null)
			return this.selected_launcher_model.getObject();
		else 
			return null;
	}

	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}
		

	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	
	/** 
	 * SearchResult contiene IModel<KBFile>
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
	
		if (columns!=null)
			return columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();


		this.columns.add(new GridColumn<SearchResult, String>("glyphicon", getLabel("iconcolumn")) {
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
			
				Object object = resultmodel.getObject().getObject();
				IModel<KBFile> objectmodel = getModel((KBFile)object);
				
				cellItem.add(new com.novamens.kbee.wicket.markup.html.console.grid.GlyphiconColumnPanel<KBFile>(componentId, objectmodel) {
					private static final long serialVersionUID = 1L;
								@Override
								protected String getGlyphiconClass() {
									return getModel().getObject().getGlyphIcon();
					 			}
								@Override
								protected String getCss() {
									return "iconcolumn";
								}
					});
			}

			@Override
			public boolean isExportable() {
				return false;
			}

			@Override
			protected String getContextKey() {
				return FileUploadAndCreateConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
	    });
		
		{
			LinkPredicateKbeeGridColumn<KBFile> titleColumn =
					new LinkPredicateKbeeGridColumn<>("title", getLabel("titlecolumn"), "title",
							obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}

		{
			KbeePredicateGridColumn<KBFile> filenameColumn = new KbeePredicateGridColumn<>("filename", getLabel("filecolumn"),
					obj -> obj.getFileName());
			filenameColumn.setContextKey(this.getName() + filenameColumn.getContextKey());
			filenameColumn.setPreferred(false);
			this.columns.add(filenameColumn);
		}


		{
			KbeePredicateGridColumn<KBFile> filenameColumn = new KbeePredicateGridColumn<>("size", getLabel("sizecolumn"),
					obj -> getSizeColumnDisplayModel(obj).getObject());
			filenameColumn.setContextKey(this.getName() + filenameColumn.getContextKey());
			this.columns.add(filenameColumn);
		}
  		

		

		
		
  		this.columns.add(new LastModifiedColumn<KBFile>("date", getLabel("datecolumn"), "modified") {
							 private static final long serialVersionUID = 1L;
							 @Override
							 protected String getContextKey() {
								 return FileUploadAndCreateConsole.this.getName() + super.getContextKey();
							 }
						 }
		);

		
  		this.columns.add(new GridColumn<SearchResult, String>("thicon", getLabel("thumbnailcolumn")) {
			
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
			
				Object object = resultmodel.getObject().getObject();
				IModel<KBFile> objectmodel = getModel((KBFile)object);
				
				cellItem.add(new ImageColumnPanel<KBFile>(componentId, objectmodel) {
			
					private static final long serialVersionUID = 1L;

					@Override
					protected Image getImage(String id) {
						KBFile object = (KBFile) getModel().getObject();
						return new ResourceThumbnailImage<>(id,  new ObjectModel<Resource>((Resource) object)  , ThumbnailSize.MINI);
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
				return FileUploadAndCreateConsole.this.getName() + super.getContextKey();
			}
		});
		
		
		return this.columns;
	}


	private IModel<String> getSizeColumnDisplayModel(KBFile kbFile) {
		String size;
		if ((kbFile).getSize()>0)
				size= NumberFormatter.formatFileSize(kbFile.getSize());
		else
			size="0 bytes";
		return new Model<String>(size);
	}

	@Override
	protected ConsoleSidePanel getRightPanel() {
		
//		return new UploadPanel("side",  this.getContainerModel()) {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public void onAfterUpload(AjaxRequestTarget target) {
//				try {
//					Thread.sleep(800);
//				} catch (InterruptedException e) {
//				}
//				FileUploadAndCreateConsole.this.setUserPreference(newQuery());
//				FileUploadAndCreateConsole.this.refresh(target);
//			}
//		};
		return null;
	}
	

	
	
	@SuppressWarnings("serial")
	@Override
	protected Panel getMenu(IModel<KBFile> model) {
		
		ContextMenuPanel<KBFile> menu = new ContextMenuPanel<KBFile>(model);
						
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<KBFile>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<KBFile> getItem(String id) {
				return new MenuItemPanelV5<KBFile>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						open(getModel().getObject());
					}
					@Override 
					public String getLabel() {
						return FileUploadAndCreateConsole.this.getLabel("contextmenu.open").getObject();
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
					
				};
			}
		});
		

		menu.addItem(new MenuItemFactory<KBFile>() {
			@Override
			public AbstractMenuItemPanelV5<KBFile> getItem(String id) {
				return new SeparatorMenuItemPanelV5<KBFile>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return true;
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<KBFile>() {
			@Override
			public AbstractMenuItemPanelV5<KBFile> getItem(String id) {
				return new AjaxMenuItemPanelV5<KBFile>(id) {
					public void onClick(AjaxRequestTarget target) {

						boolean has_errors= false;
						StringBuilder str = new StringBuilder(); 
									
						try {
								IDoc idoc = getContainerModel().getObject();
								idoc.removeFile(getModel().getObject());
								idoc.getService(ContentService.class).update();
								FileUploadAndCreateConsole.this.list_model.remove(model);
				
						} catch (ContentMgmtException e) {
								has_errors=true;
								str.append(e.getMessage());
								
						} catch (ServiceNotFoundException e) {
								has_errors=true;
								str.append(e.getMessage());
						}
							
						setQuery(newQuery());
						getBrowser().resetSelection();
						refresh(target);

						if (has_errors) {
								getErrorDialog().open(target, new Model<String>(str.toString()));
						}
					}
					@Override 
					public String getLabel() {
						return FileUploadAndCreateConsole.this.getLabel("contextmenu.delete").getObject();
					}
					
					@Override 
					public String getWorkingLabel() {
						return "Processing";
					}
				};
			}
		});
		
		
	
		return menu;
	}


	 
	@Override
	public Query newQuery() {
		try {
			List<KBFile> list = this.getContainerModel().getObject().getFiles();
			this.list_model.clear();
			for (KBFile file: list) 
				this.list_model.add(new ObjectModel<KBFile>(file));
			ListModelQuery<KBFile> query = new ListModelQuery<KBFile>(this.list_model);
			return setUserPreference(query);
			
		} catch (Exception e) {
			logger.error(e);
			return 	new ListModelQuery<KBFile>(this.list_model);
		}
	}


	 
	@Override
	protected boolean hasExpander() {
		return true;
	}
	

	@Override
	protected boolean isSelectionEnabled() {
		return true;
	}
	

	@Override
	protected Panel getPanel(IModel<KBFile> model, List<String> snippets) {
		return new ExpandedPanel<KBFile>("editor", this, model, snippets);
	}
	

	@Override
	protected Panel getPanel(IModel<KBFile> model) {
		return new ExpandedPanel<KBFile>("editor", this, model);
	}
	

	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<KBFile>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<KBFile> event) {
				KBFile file = event.getModelObject();
				FileUploadAndCreateConsole.this.open(file);
			}
		});
		
		
		/**	
		  	esto es para que siempre haya un panel a la derecha 
		 	si es alertas o user notes ok
		 	pero al cerrar, que se muestre el default
		*/ 
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				if (!getBrowser().isRightPanelVisible()) 
					getBrowser().togglePanel(UploadPanel.class);
			}
		});		
		
		
		
		add(new WicketEventListener<LauncherSelectorEvent<ProcessLauncher>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(LauncherSelectorEvent<ProcessLauncher> event) {
				FileUploadAndCreateConsole.this.setSelectedLauncherModel(event.getModel());
				if (event.getKey()==null ||	event.getKey().equals("one-for-all"))
					addOneForAll(event.getRequestTarget());
				else
					addOneForEach(event.getRequestTarget());
			}
		});

	}
	

	/**
	 * 
	 * 
	 */
	protected void startProcess(IModel<KBFile> file_model) throws ContentMgmtException {
		
		if ( this.getSelectedLauncherModel()!=null && getSelectedLauncherModel().getObject()!=null) {
		
			String label = this.getSelectedLauncherModel().getObject().getLabel();
			
			Process process = startProcess(label);
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
						((KBFileImpl) file).setPublic(true);
						idoc.addFile(file);
						((Content) idoc).setTitle(file.getTitle());
						List<String> ls = new ArrayList<String>();
						ls.add("add: " + file.getTitle());
						((Content) idoc).getService(ContentService.class).update(ls);
					}
					else {
						logger.error(this.getSelectedLauncherModel().getObject().getContentTemplate().getContentClass().getId() + " not supported");
					}

					
				} catch (ServiceNotFoundException e) {
					logger.error(e);
					throw new ContentMgmtException(e);
				}
			}
		}
	}

	
	
	/** 
	 * 
	 * 
	 */
	protected void startProcess(List<IModel<KBFile>> file_model_list) throws ContentMgmtException {
		
		
		if (file_model_list.size()==0)
			return;
		
		if (this.getSelectedLauncherModel()==null)
			throw new ContentMgmtException ("getSelectedLauncherModel()==null");
		
		if (getSelectedLauncherModel().getObject()==null) 
			throw new ContentMgmtException ("getSelectedLauncherModel().getObject()==null");

		
			String label = this.getSelectedLauncherModel().getObject().getLabel();
			Process process = startProcess(label);
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
							idoc.addFile(fm.getObject());
							ls.add("add: " + fm.getObject().getTitle());
					}
					
					((Content) idoc).setTitle(file_model_list.get(0).getObject().getTitle());
					((Content) idoc).getService(ContentService.class).update(ls);
					
				} catch (ServiceNotFoundException e) {
					logger.error(e);
					throw new ContentMgmtException(e);
				}
			
		
	}
	
	

	protected Process startProcess(String launcherlabel) {
		for(ProcessLauncher launcher : getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcherlabel.equals(launcher.getLabel())) {
				Process process = getDomain().getService(WorkflowDomainService.class).startProcess(launcher);
				return process;
			}
		}
		return null;
	}
	

	@Override
	protected Panel getItemListPanel(IModel<KBFile> model, int index) {
			return new  kbee.web.dashboard.LabelPanel("item", new Label("label", ((Identifiable) model.getObject()).getDisplayName()));
	}
	
	
	private void open(KBFile file) {
		
		if (file.isImage()) {
			ResourceReference resourceReference = new WebResourceReference(file);
			String resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			WebPage page = new RedirectPage(resourcehref);
			setResponsePage(page);
		}
		
		else if (file.isVideo()) {
			ResourceReference resourceReference = new WebResourceReference(file);
			String resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			setResponsePage(new RedirectPage(resourcehref));
		}
		
		else if (file.isAudio()) {
			ResourceReference resourceReference = new WebResourceReference(file);
			String resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			setResponsePage(new RedirectPage(resourcehref));
		}
		else {
			ResourceReference resourceReference = new WebResourceReference(file);
			String resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			setResponsePage(new RedirectPage(resourcehref));
		}
	}
	
	private KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	
	private String getPreference(String name) {
		String value = getUser().getService(PreferencesService.class).getValue(getName() + "-browser", name);
		return value;
	}
	
	
	private void setPreference(String name, String value) {
		getUser().getService(PreferencesService.class).setValue(getName() + "-browser", name, value);
	}
	

}

