package com.novamens.content.web.console.markup;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.TreeFileService;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchButton;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchResourcesSelectorPanel;
import com.novamens.content.web.resource.markup.model.TreeFileHitExpandedPanel;
import com.novamens.event.LogEvent;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.SimpleDateColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.NumberFormatter;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.AuditConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.grid.TargetBlankObjectTitleColumnPanel;
import kbee.web.resource.WebResourceReference;
						
public abstract class AuditTreeFileResourcesConsole extends AbstractFacetedConsole<TreeFile> implements AuditConsole  {
					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditTreeFileResourcesConsole.class.getName());

	
	private static final long serialVersionUID = 1L;

	private List<GridColumn<SearchResult,String>> columns;
	
	private OffsetDateTime from = OffsetDateTime.now().minusDays(1);
	private OffsetDateTime to   = OffsetDateTime.now();

	
	public AuditTreeFileResourcesConsole(String name, Query query) {
		super(name, query);
	}

	
	
	public AuditTreeFileResourcesConsole(Query query) {
		super("treexplorer", query);
		setOutputMarkupId(true);
	}

	@Override
	 protected  IModel<TreeFile> getModel(TreeFile object) {
			return new ObjectModel<TreeFile>(object, true);
	}
	
	
	@Override
	protected String getIcon(IModel<TreeFile> model) {
		return null;
	}
	
	protected boolean isDefaultTopPanelVisible() {
		return true;
	}
	
	
	@Override
	protected boolean hasTopPanel() {
		return true;
	}
	
	@Override
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	@Override
	public Query newQuery() {
		return new AuditTreeFileResourcesQuery(getQueryIndex(), isDomainKbee());
	}
	
	// protected abstract Page getConsolePage(Query query, long index);
	
	
	protected BreadCrumb getBreadCrumb() {
		return null;
	};

	@Override
	protected Panel getTopPanel() {
		return new  AdvancedSearchResourcesSelectorPanel("top", from, to);
	}

	@Override
	protected boolean isVisible(Facet facet) {
		return true;
	}


	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<TreeFile> browser) {
		return new ArrayList<>();
	}

	@Override
	protected Panel getMenu(IModel<TreeFile> model) {
		
		ContextMenuPanel<TreeFile> menu = new ContextMenuPanel<TreeFile>(model);
						
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<TreeFile>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<TreeFile> getItem(String id) {
				return new MenuItemPanelV5<TreeFile>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						open(getModel().getObject());
					}
					@Override 
					public String getLabel() {
						return AuditTreeFileResourcesConsole.this.getLabel("contextmenu.open").getObject();
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
				};
			}
		});

		return menu;
	}

	/**
	 * 
	 * 
	 */
	@SuppressWarnings("serial")
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		

		columns.add(new GridColumn<SearchResult, String>("glyphicon", getLabel("iconcolumn")) {
			private static final long serialVersionUID = 1L;
		
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {

				try {
					Object object = resultmodel.getObject().getObject();
					IModel<TreeFile> objectmodel = getModel((TreeFile)object);
					cellItem.add(new com.novamens.kbee.wicket.markup.html.console.grid.GlyphiconColumnPanel<TreeFile>(componentId, objectmodel) {
						private static final long serialVersionUID = 1L;
									@Override
									protected String getGlyphiconClass() {
										try {
											return getModel().getObject().getGlyphIcon();
										} catch (Exception e) {
											logger.error(e);
											return "";
										}
						 			}
									@Override
									protected String getCss() {
										if(getModel().getObject().isRoot())
											return "iconcolumn treefileroot";
										else
											return "iconcolumn treefilechild";
									}
						});
				 
					} catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					cellItem.add(new Label(componentId, e.getClass().getName())); 
				}
			}
			
			@Override
			public boolean isExportable() {
				return false;
			}
			
	 		@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}
	    });

		

		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("titlecolumn"), "title_sort") {
 			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {
					Object object = resultmodel.getObject().getObject();
					IModel<TreeFile> objectmodel = getModel((TreeFile)object);
					cellItem.add(new TargetBlankObjectTitleColumnPanel<TreeFile>(componentId, objectmodel) {
						private static final long serialVersionUID = 1L;
						@Override
						protected String getCss() {
							return "cell-label btn-link";
						}
					});
				} catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					cellItem.add(new Label(componentId, e.getClass().getName())); 
				}
			}
			
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}
		});
  		
  		
		
		this.columns.add(new GridColumn<SearchResult, String>("size", getLabel("sizecolumn")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				String size = null;
				try {		
						size=NumberFormatter.formatFileSize(((TreeFile) object.getObject()).getTotalSize(), getSessionUser().getLocale(), "ago");
						return new Model<String>(size);
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}
			@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}
		});

		
		
		this.columns.add(new GridColumn<SearchResult, String>("totalnodes", getLabel("totalnodescolumn")) {
			private static final long serialVersionUID = 1L;
					@Override
					protected IModel<String> getLabelModel(SearchResult object) {
						try {
							String str;
							str = NumberFormatter.formatNumber(((TreeFile) object.getObject()).getTotalNodes(), getSessionUser().getLocale());
							return new Model<String>(str);
						} catch (Exception e) {
							logger.error(e, getSessionUser().getUserName());
							return new Model<String>(e.getClass().getName());
						}
					}
					@Override
					protected String getContextKey() {
						return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
					}
		});


		
		this.columns.add(new GridColumn<SearchResult, String>("user", getLabel("usercolumn")) {
			private static final long serialVersionUID = 1L;
					@Override
					protected IModel<String> getLabelModel(SearchResult object) {
						try {
							String str = ((TreeFile) object.getObject()) .getLastModifiedUser().getFirstLastName();
							return new Model<String>(str);
						} catch (Exception e) {
							logger.error(e, getSessionUser().getUserName());
							return new Model<String>(e.getClass().getName());
						}
					}
					@Override
					protected String getContextKey() {
						return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
					}
		});



		this.columns.add(new GridColumn<SearchResult, String>("level", getLabel("levelcolumn"), "level") {
			private static final long serialVersionUID = 1L;
					@Override
					protected IModel<String> getLabelModel(SearchResult object) {
						try {
							String str = String.valueOf(((TreeFile) object.getObject()).getLevel());
							return new Model<String>(str);
						} catch (Exception e) {
							logger.error(e, getSessionUser().getUserName());
							return new Model<String>(e.getClass().getName());
						}
					}
					@Override
					protected String getContextKey() {
						return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
					}
		});

		
		this.columns.add(new SimpleDateColumn<TreeFile>("created", new Model<String>("Created"), "created") {
			protected OffsetDateTime getOffsetDateTime(TreeFile file) {
				 return file.getCreationOffsetDateTime();
			}
			@Override
			public String getDateFormat() {
				return (AuditTreeFileResourcesConsole.this.getBrowser().getPanel(GridPanel.class)).getDateFormat();
			}
		});
		
		columns.add(new LastModifiedColumn<TreeFile>("date", getLabel("datecolumn"), "modified"){
			private static final long serialVersionUID = 1L;
			@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}

		});
		
		this.columns.add(new GridColumn<SearchResult, String>("htmltree", getLabel("htmlcolumn")) {
			private static final long serialVersionUID = 1L;
					@Override
					protected IModel<String> getLabelModel(SearchResult object) {
						try {
							TreeFile tf= (TreeFile) object.getObject();
							return new Model<String>(tf.getService(TreeFileService.class).toHTMLString());
							
						} catch (Exception e) {
							logger.error(e, getSessionUser().getUserName());
							return new Model<String>(e.getClass().getName());
						}
					}
					@Override
					protected String getContextKey() {
						return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
					}
					
					@Override
					public boolean isPreferred() {
						return false;
					}
			
					public boolean isEscapeModelString() {
						return false;
					}
					
					public boolean isOnlyForExpandedHitPanel() {
						return true;
					}
			});
		

		this.columns.add(new GridColumn<SearchResult, String>("class", new Model<String>("Class")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				return new Model<String>(((TreeFile) object.getObject()).getType());
			}
			@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}
		});
		
		
		this.columns.add(new GridColumn<SearchResult, String>("name", getLabel("namecolumn")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				try {
					return new Model<String>((((TreeFile)object.getObject())).getName());
				} catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					return new Model<String>(e.getClass().getName());
				}
			}
			@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}
		});

		
		
		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("idcolumn")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					return new Model<String>(String.valueOf(((TreeFile)object.getObject()).getId()));
				} catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					return new Model<String>(e.getClass().getName());
				}
			}
			@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}
		});

		
		if (isDomainKbee()) {
			this.columns.add(new GridColumn<SearchResult, String>("domain", getLabel("domaincolumn")) {
	 			private static final long serialVersionUID = 1L;
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						String domain = ((TreeFile) object.getObject()).getDomain().getName();
						 return new Model<String>(domain);
					 } catch (Exception e) {
						 	logger.error(e, getSessionUser().getUserName());
							return new Model<String>(e.getClass().getName()); 
					 }
				}
				@Override
				protected String getContextKey() {
					return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
				}
			});
		}


		this.columns.add(new GridColumn<SearchResult, String>("parent", getLabel("parentcolumn")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				try {
					TreeFile tf=(TreeFile)object.getObject();
					if (tf.getParent()==null)
						return new Model<String>("");
					return new Model<String>(String.valueOf(tf.getParent().getId()));
					
				} catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					return new Model<String>(e.getClass().getName());
				}
			}
			@Override
			protected String getContextKey() {
				return AuditTreeFileResourcesConsole.this.getName() + super.getContextKey();
			}
		});

		return columns;
	}
	
	
	
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("content-header"));
			}
		});

		add(new WicketEventListener<ClickEvent<TreeFile>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<TreeFile> event) {
				//KBFile file = event.getModelObject();
				//AuditTreeFileResourcesConsole.this.open(file);
				logger.error(" not implemented");
				
			}
		});
	}
	
	protected Panel getPanel(IModel<TreeFile> model) {
		//return new ExpandedPanel<TreeFile>("editor", this, model);
		return new TreeFileHitExpandedPanel("editor", model);
		
	}
	

	protected Panel getPanel(IModel<TreeFile> model, List<String> list) {
		return new TreeFileHitExpandedPanel("editor", model);
		
		//return new ExpandedPanel<TreeFile>("editor", this, model, list);
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}

	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}


	
	private void open(TreeFile file) {
		
		if (file instanceof TreeFileKBFile) { 
			openTreeFileKBFile((TreeFileKBFile) file);
			return;
		}
		
		else if (file instanceof TreeFileDir) {
			for (TreeFile t: ((TreeFileDir) file).getChildren()) {
				if (t instanceof TreeFileKBFile) { 
					openTreeFileKBFile((TreeFileKBFile) t);
					return;
				}
			}
			logger.error("No TreeFileKBFile in Children");
			return;
		}
		else {
			throw new KbeeRuntimeException ("TreeFile must be Dir or KBFile");
		}
		// if file is non existent error page maybe cleaner
	}

	/**
	 * @param file
	 */
	private void openTreeFileKBFile(TreeFileKBFile file) {

//		KBFile tf_file = ((TreeFileKBFile) file).getFile();

//		if (tf_file.isImage()) {
//			String resourcehref;
//			ResourceReference resourceReference = new WebResourceReference(tf_file);
//			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
//			WebPage page = new RedirectPage(resourcehref);
//			setResponsePage(page);
//			
//		}
//		else if (tf_file.isVideo()) {
//			String resourcehref;
//			ResourceReference resourceReference = new WebResourceReference(tf_file);
//			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
//			setResponsePage(new RedirectPage(resourcehref));
//		}
//		else if (tf_file.isAudio()) {
//			String resourcehref;
//			ResourceReference resourceReference = new WebResourceReference(tf_file);
//			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
//			setResponsePage(new RedirectPage(resourcehref));
//		}
//		else {
			String resourcehref;
//			//ResourceReference resourceReference = new WebResourceReference(tf_file);
			ResourceReference resourceReference = new WebResourceReference(file);
			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			setResponsePage(new RedirectPage(resourcehref));
//		}
	}
	


}
