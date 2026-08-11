package kbee.web.content.console;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.service.ContentService;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchEmailSelectorPanel;
import com.novamens.content.web.content.markup.GenericBatchActionPage;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.DeleteButton;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.service.ContentExportService;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.BaseBrowser;
import kbee.web.console.TargetBlankTitleColumnPanel;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.AttributeDateColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.tools.ExportContentToolButton;
import kbee.web.content.nav.ContentNavigationBar;
import kbee.web.nav.ArchiveBC;
import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.ContentSectionBC;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ExportContentsPage;
import kbee.web.query.ArchiveQuery;

/**

*/
@SuppressWarnings("serial")
public abstract class ArchiveConsole extends ContentConsole<Content> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ArchiveConsole.class.getName());
	
	private List<NavigationOrder> orders;
	//private List<ToolbarItem> items;
	private List<GridColumn<SearchResult,String>> columns;
	private List<ToolbarItem> selection_toolbar;

	public ArchiveConsole(Query query) {
		super("archive", query);
	}
	
	

	@Override
	protected String getIcon(IModel<Content> model) {
		return null;
	}
	
	protected  IModel<Content> getModel(Content object) {
		return new ObjectModel<Content>(object, true);
	}
	
	@Override
	public List<NavigationOrder> getOrders() {
		if (this.orders!=null) 
			return this.orders;
		this.orders = super.getOrders();
		Collections.sort(orders, new Comparator<NavigationOrder>() {
			public int compare(NavigationOrder order1, NavigationOrder order2) {
				try {
					return order1.getLabel().compareToIgnoreCase(order2.getLabel());
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		return orders;
	}
	
	
	
	@Override
	public void onDetach() {
		for (GridColumn<?,?> column: getColumns()) 
			column.detach();
		columns=null;
		if (this.selection_toolbar!=null) {
				for (ToolbarItem item: selection_toolbar) {
					item.detach();
				}
		}
		super.onDetach();
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new ArchiveQuery(getQueryIndex()));
	}
	
	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new ContentBaseBC());
	};
	
	@Override
	protected boolean isEditionEnabled() {
		return true;
	}
	
	@Override
	protected Panel getMenu(IModel<Content> model) {
	
		ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new MenuItemPanelV5<Content>(id) {
					public void onClick() {
						//, ArchiveConsole.this.getIndex(getModel().getObject())
						setResponsePage(ArchiveConsole.this.getPageV6(getModel()));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("archive.contextmenu.open").getObject();
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
				};
			}
		});


		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new SubMenuAjaxUserListItemPanel<Content>(id, model, ArchiveConsole.this.getName(), null, UserListItem.PUBLISHED);
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						public void onClick(AjaxRequestTarget target) {
							
							if (!getContent().isLocked()) {
								getContent().getService(ContentService.class).unArchive();
								resetSelection();
							}
							refresh(target);
						}
						@Override 
						public String getLabel() {																			
							return ArchiveConsole.this.getLabel("archive.contextmenu.movetolibray").getObject();
						}
						@Override
						public String getWorkingLabel() {										
							return ArchiveConsole.this.getLabel("archive.contextmenu.movetolibray").getObject();
						}

						@Override
						public boolean isVisible() {

							if (getModel().getObject().isExternal() && !isRoot())
								return false;
							
							return true;
						}

						
						@Override
						public boolean isEnabled() {

							if (isSupportUser() && !isRoot())
								return false;

							if (getModel().getObject().isExternal() && !isRoot())
								return false;
							
							return !getContent().isLocked() && isWriteable(getModel());
						}
						
						public Content getContent() {
							return getModel().getObject();
						}
					};
				}
			});
			
			

		

		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						Modal modal = ArchiveConsole.this.getAuditTrailModal();
						((AuditTrailModal<Content>)modal).open(target, getModel());
//						}
//						else {
//							// TODO: Avisar que no esta en la consola
//							refresh(target);	
//						}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("archive.contextmenu.audittrail").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						if (isSupportUser())
							return true;
						
						if (isWriteable(getModel()))
								return true;
						
						if (isAuditReadable(getModel()))
							return true;
						
						return false;

					}

					
					
					
					
					
					
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Content>(id) {
						@Override 
						public String getLabel() {
								return ArchiveConsole.this.getLabel("archive.contextmenu.download").getObject();
						}
						@Override
						public boolean isDeleteFileAfterDownload()  {
							return true;
						}
						@Override
						protected File getFile() {
							File file = getModelObject().getService(ContentExportService.class).getHTMLExport();
							return file;
						}
						
						@Override
						public boolean isEnabled()  {
							return (isRoot() || !isSupportUser());
						}
						
						
					};
			}
		});
		
		
		/**
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public MenuItemPanel<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id, null) {
					public void onClick(AjaxRequestTarget target) {
						if (getModelObject().getState()==ObjectState.ARCHIVED) {
							getModelObject().getService(ContentService.class).unArchive();
							resetSelection();
						}
						refresh(target);
					}
					public String getLabel() {
						return getConsoleLabel("archive.contextmenu.sendtobase").getObject();
					}
					public String getWorkingLabel() {
						return getConsoleLabel("archive.contextmenu.sendtobase.working").getObject();
					}
					@Override
					public boolean isEnabled() {
						return !isSupport();
					}
					
					@Override
					public boolean isVisible() {
						if (!isWriteable(getModel()))
							return false;
						return true;
					}
				};
			}
		});*/
		
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Content>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxCheckMenuItemPanelV5<Content>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						try {
							getModelObject().getService(ContentService.class).recycle();
						} catch (ContentMgmtException | ServiceNotFoundException e) {
							logger.error(e);
						}
						resetSelection();
						refresh(target);
					}
					@Override
					public String getLabel() {
						return getConsoleLabel("archive.contextmenu.delete").getObject();
					}
					@Override
					public String getWorkingLabel() {
						return getConsoleLabel("archive.contextmenu.delete.working").getObject();
					}
					@Override
					public boolean isEnabled() {
						return  !isSupport() && isDeleteable(getModel());
					}
				};
			}
		});
		
		return menu;
	}
	
	
												
	

	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {

		
		if (columns!=null)
			return columns;
		
		
		columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		columns.add(new GridColumn<SearchResult, String>("title", getLabel("titlecolumn"), "title_sort") {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				
				try {
						Object object = resultmodel.getObject().getObject();
						IModel<Content> objectmodel = getModel((Content)object);
						cellItem.add(new TargetBlankTitleColumnPanel<Content>(componentId, objectmodel) {
							protected String getCss() {
								return "btn-link";
							}
						});
				} catch (Exception e) {
					logger.error(e);
					cellItem.add(new Label(componentId, new Model<String>(e.getClass().getSimpleName())));
				} 
			}
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				Content content = (Content) object.getObject();
				return ()-> content.getTitle();
			}
			@Override
			protected String getContextKey() {
				return ArchiveConsole.this.getName() + super.getContextKey();
			}
		});
		
		for (Classifier classifier : getClassifiers()) {
			
			if (classifier.isDefaultGridColumn()) {
				columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
			}
		}
		
		

		
		if (getDomain().getDomainType()!=DomainType.EXPRESS) {
			
			columns.add(new GridColumn<SearchResult, String>("contentclass", getLabel("contentclasscolumn")) {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					return new Model<String>(((Content)object.getObject()).getContentTemplate().getDisplayName());
				}
				@Override
				protected String getContextKey() {
					return ArchiveConsole.this.getName() + super.getContextKey();
				}
			});
		}
		

		columns.add(new LastModifiedColumn<Content>("date", getLabel("datecolumn"), "modified") {
			@Override
			protected String getContextKey() {
				return ArchiveConsole.this.getName() + super.getContextKey();
			}
		});
		

		
		
		for (Classifier classifier : getClassifiers()) {
			if (!classifier.isDefaultGridColumn()) {
				columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier),  this.getName()));
			}
		}
		
		final String key = "archive";
		
		for (Attribute attribute: getAttributes()) {
			if (attribute.getState()==ObjectState.ENABLED  && attribute.isVisible(key)) {
				if (attribute.isDate())
					this.columns.add(new AttributeDateColumn(new ObjectModel<Attribute>(attribute), getName()));
				else
					this.columns.add(new AttributeColumn(new ObjectModel<Attribute>(attribute), getName()));
			}
		}


		columns.add(new GridColumn<SearchResult, String>("id", getLabel("idcolumn")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				return new Model<String>(String.valueOf(((Content)object.getObject()).getOId()));
			}
			@Override
			protected String getContextKey() {
				return ArchiveConsole.this.getName() + super.getContextKey();
			}
		});

		
		return columns;
	}

	

	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
	
		// Send
		// 
		//
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
			
			@Override
			protected String getLabelStr() {
				return new StringResourceModel("archive.tools.sendtobase", this, null).getObject();
			}
			@Override
			protected String getIcon() {
				return "far fa-folder  fa-fw";
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				
				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new ContentSectionBC());
				list.add(new ContentBaseBC());
				list.add(new ArchiveBC());
				list.add(new BCElement(getConsoleLabel("archive.batch.sendtobase")));	
				
																	
				GenericBatchActionPage page = new GenericBatchActionPage(ArchiveConsole.this.getBrowser().getSelection()) {
					@Override
					public IModel<String> getTitle() {
						return getConsoleLabel("archive.batch.sendtobase");
					}
					
					public String getIcon() {
						return "far fa-folder";
					}
					
					@Override
					public IModel<String> getType() {
						return getConsoleLabel("archive.batch.class");
					}
					@Override
					public IModel<String> getReturnLabel() {
						return getConsoleLabel("archive.batch.return");
					}
					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}
					@Override
					protected String executeAction(IModel<Content> model) {
						if (model.getObject().getState()==ObjectState.ARCHIVED) {
							model.getObject().getService(ContentService.class).unArchive();
						}
						return "";
					}
					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = ArchiveConsole.this.getPage(model);
						((AbstractApplicationPage<?>)page).setTopNavigation(new ContentNavigationBar<Content>(model));
						return page;
					}
				};
				
				page.setBreadCrumb(list);
				setResponsePage(page);
				
			}
		});
		
		
		// Delete
		// 
		//
		this.selection_toolbar.add(new DeleteButton(browser, ToolbarItem.Align.TOP_LEFT, true) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GenericBatchActionPage page = new GenericBatchActionPage(ArchiveConsole.this.getBrowser().getSelection()) {
					
					public String getIcon() {
						return "far fa-trash-alt";
					}

					@Override
					public IModel<String> getTitle() {
						return getConsoleLabel("archive.batch.delete");
					}
					@Override
					public IModel<String> getType() {
						return getConsoleLabel("archive.batch.class");
					}
					@Override
					public IModel<String> getReturnLabel() {
						return getConsoleLabel("archive.batch.return");
					}
					
					@Override
					protected IModel<String> getExecuteButtonLabel() {
						return new StringResourceModel("delete", ArchiveConsole.this, null);
					}
					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}
					@Override
					protected String executeAction(IModel<Content> model) {
						try {
							model.getObject().getService(ContentService.class).recycle();
						} catch (ContentMgmtException | ServiceNotFoundException e) {
							logger.error(e);
						}
						return "";
					}
					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = ArchiveConsole.this.getPage(model);
						((AbstractApplicationPage<?>)page).setTopNavigation(new ContentNavigationBar<Content>(model));
						return page;
					}
					@Override
					protected String getExecuteButtonCss() {
						return "btn btn-sm btn-danger";
					}
				};
				
				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new ContentSectionBC());
				list.add(new ContentBaseBC());
				list.add(new ArchiveBC());
				list.add(new BCElement(getConsoleLabel("archive.batch.delete")));
				page.setBreadCrumb(list);
				setResponsePage(page);


			}
		});

		// Export
		// 
		this.selection_toolbar.add(new ExportContentToolButton<Content>(browser, ToolbarItem.Align.TOP_LEFT, true) {
			@Override
			protected void onClick(AjaxRequestTarget target) {
				setResponsePage(new ExportContentsPage(getListModel(), new ArchiveBC()) {
						@Override
						public void onClose() {
							setResponsePage(new ArchivePage());
						}
					});
					refresh(target);
			}
		});


		return this.selection_toolbar;
	
	
	
	}
	

	@Override
	protected boolean isReadOnly() {
		return false;
	}
	
	
	@Override
	protected boolean hasTopPanel() {
		return false;
	}

	@Override
	protected Panel getTopPanel() {
		return new  AdvancedSearchEmailSelectorPanel("top");
	}


}
