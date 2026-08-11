package kbee.web.model;


import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarAlert;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.util.NumberFormatter;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.DataSetBC;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.service.ApplicationSiteMapService;


/**
 * For the other components of the Information Model
 * see {@link ContentTemplatesConsoleConsole} {@link DataSetsConsole},  {@link AttributesConsole}, {@link ClassifiersConsole}

 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class DataSetsConsole<T extends DataSet> extends AbstractFacetedConsole<T> {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetsConsole.class.getName());
							
 	private IModel<T> datasetmodel;
	private List<GridColumn<SearchResult,String>> columns;
	private List<ToolbarItem> items;

	
	public DataSetsConsole(Query query) {
		super("datasets", query);
		this.is_deleted_visible = getUserPreference("deleted-visible", "no").equals("yes") ? true : false;
	}

	
	
	@Override
	protected String getIcon(IModel<T> model) {
		return null;
	}	

	
	@Override
	 protected  IModel<T> getModel(T object) {
			return new ObjectModel<T>(object, true);
	}

	
	public void setDataSet(IModel<T> model) {
		this.datasetmodel = model;
	}
	
	public T getDataSet() {
		return datasetmodel.getObject();
	}

	@Override
	public void onDetach() {
		if (this.datasetmodel!=null)
			this.datasetmodel.detach();
		this.columns=null;
		this.items=null;
		super.onDetach();
	}
	
	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new DataSetsQuery(isDeletedVisible()));
	}
	
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new DataSetBC(getDataSet()));
	}
	

	@Override
	protected boolean hasExpander() {
		return true;
	}

	
	
    /**
     * 
     * 
     */
    @Override
    protected GridMenu getGridToolbarMenuItem() {
        GridMenu gridToolbarMenuItem = super.getGridToolbarMenuItem();

        gridToolbarMenuItem.addItem((itemId) -> new SeparatorMenuItemPanelV5<File>(itemId) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public String getCssClass() {
                return "divider";
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        });

        MenuItemFactory<?> showDeletedUsersItem = (itemId) ->
                
	        new AjaxCheckMenuItemPanelV5<Object>(itemId) {
	                    private static final long serialVersionUID = 1L;
	                 
					    @Override
	                    public String getLabel() {
	                        return new StringResourceModel("show-deleted", DataSetsConsole.this, null).getObject();
	                    }
	
	                    @Override
	                    public void onClick(AjaxRequestTarget target) throws Exception {
	                    	DataSetsConsole.this.setDeletedVisible(!DataSetsConsole.this.isDeletedVisible());
	                    	setResponsePage(new DataSetsPage<>());
						}
	
	                    @Override
	                    public boolean isIconVisible() {
	                        return DataSetsConsole.this.isDeletedVisible();
	                    }
	
	                    @Override
	                    public String getCssClass() {
	                        if (isIconVisible())
	                            return "label-selected";
	                        else
	                            return "label-no-selected";
	                    }
	
	
	                };
					
        gridToolbarMenuItem.addItem(showDeletedUsersItem);
        return gridToolbarMenuItem;
    }


    private boolean is_deleted_visible = false;
    protected void setDeletedVisible(boolean b) {
        this.is_deleted_visible = b;
        setUserPreference("deleted-visible", (b ? "yes" : "no"));
    }

    protected boolean isDeletedVisible() {
        return this.is_deleted_visible;
    }

    
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	protected Panel getMenu(IModel<T> model) {
		
		ContextMenuPanel<T> menu = new ContextMenuPanel<T>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(getDataSetPage(getModel(), getIndex(), false, false));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("open").getObject();
					}
				};
			}
		});
		

		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new SeparatorMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
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
		
		menu.addItem(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new MenuItemPanelV5<T>(id) {
					@Override
					public void onClick() {
						try {
							Object member = ServiceLocator.getService(ObjectFactoryService.class).createMember(getModel().getObject());
							((DataSetMember) member).setStrValue(getConsoleLabel("new", getModel().getObject().getName()).getObject());
							setResponsePage(DataSetsConsole.this.getPage(new ObjectModel<DataSetMember>((DataSetMember)member), 0, true, true));
						}
						catch (ContentCreationException e) {
							logger.error(e);
							throw new KbeeRuntimeException(e);
						}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("add-element").getObject();
					}
					 
				};
			}
		});


		menu.addItem(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new MenuItemPanelV5<T>(id) {
					@Override
					public void onClick() {
						DataSet d=getModel().getObject();
						PageParameters pa = new PageParameters();
						pa.add("id", d.getId().toString());
						setResponsePage(  ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-bulk-page", pa));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("add-bulk-elements").getObject();
					}
					 

				};
			}
		});

	 	menu.addItem(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new MenuItemPanelV5<T>(id) {
					@Override
					public void onClick() {
						PageParameters pa = new PageParameters();
						pa.add("id", getModel().getObject().getId().toString());
						setResponsePage(  ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-page", pa));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("open-values").getObject();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							Object cla = ServiceLocator.getService(ObjectFactoryService.class).createClassifier(getModel().getObject());
							Page page = getClassifierPage(new ObjectModel<Classifier>((Classifier) cla), true, true);
							setResponsePage(page);
						}
						catch (Exception e) {
							logger.error(e);
							throw new KbeeRuntimeException(e);
						}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("create-classifier").getObject();
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							try {
								getModel().getObject().setState(ObjectState.ENABLED);
								getModel().getObject().setName(getModel().getObject().getName().replace(DOMObjectService._DELETED_  , ""));
								getModel().getObject().getService(DOMObjectService.class).update(ObjectState.ENABLED.getLabel());
								FeedbackHelper.showInfoToast(getLabel()+ " <br/>" + getModel().getObject().getDisplayName());
								DataSetsConsole.this.refresh(target);
							}
							catch (Exception e) {
								logger.error(e);
								throw new KbeeRuntimeException(e);
							}
						}
						catch (Exception e) {
							logger.error(e);
							throw new KbeeRuntimeException(e);
						}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("set-enabled").getObject();
					}
					@Override
					public boolean isVisible() {
						return getModel().getObject().getState()!=ObjectState.ENABLED;
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							getModel().getObject().setState(ObjectState.ARCHIVED);
							getModel().getObject().getService(DOMObjectService.class).update(ObjectState.ARCHIVED.getLabel());
							FeedbackHelper.showInfoToast(getLabel()+ " <br/>" + getModel().getObject().getDisplayName());
							DataSetsConsole.this.refresh(target);
						}
						catch (Exception e) {
							logger.error(e);
							throw new KbeeRuntimeException(e);
						}
					}
					
					@Override
					public boolean isVisible() {
						return getModel().getObject().getState()!=ObjectState.ARCHIVED;
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("bc.archive").getObject();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new SeparatorMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;
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
		
		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					public void onClick(AjaxRequestTarget target) {
						getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									try {
										DOMObjectService objectService = getModel().getObject().getService(DOMObjectService.class);
										objectService.asyncDelete();

										resetSelection();
										refresh(target);
									}
									catch (DataIntegrityViolationException | ConstraintException e) {
										getErrorDialog().open(target, getConsoleLabel("error.constraint"));
									}
									catch (Exception e) {
										logger.error(e);
										getErrorDialog().open(target, new Model<String>(e.getMessage()));
									}
									DataSetsConsole.this.refresh(target);
								}
							}
						});
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("contextmenu.delete").getObject();
					}
	
					// Domain Basic no puede borrar DataSets existentes.
					//
					@Override
					public boolean isEnabled() {
					
						if (getModel().getObject().getState()==ObjectState.DELETED)
							return false;

						if (getModel().getObject().getDataSetType()==DataSetType.USER)
							return false;
						if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS && !isRoot()) 
							return false;
						if (getModel().getObject().isCanonical())
							return isRoot();
						return !getModel().getObject().isCanonical() && !isSupport();
					}
				};
			}
		});
		
		return menu;
	}

	
	/***
	 *
	 * 
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
			
		if (columns!=null)
			return columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		
		this.columns.add(new ObjectStatusColumn<Person>("icon_status", getName(), new Model<String>("St")));

		{
			LinkPredicateKbeeGridColumn<T> titleColumn = new LinkPredicateKbeeGridColumn<>("title", getLabel("name"), "title", obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}


		this.columns.add(new LastModifiedColumn<T>("modified", getLabel("modified"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected OffsetDateTime getOffsetDateTime(DataSet object) {
					return object.getLastModifiedOffsetDateTime();
			}
			@Override
			protected String getContextKey() {
				return DataSetsConsole.this.getName() + super.getContextKey();
			}
		});


		
		this.columns.add(new GridColumn<SearchResult, String>("type", getLabel("type"), "type") {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				try {
					DataSet ds = (DataSet) result.getObject();

					return getDisplayModel(ds,true);
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				try {
					DataSet ds = (DataSet) result.getObject();
					return getDisplayModel(ds, false);
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}


			private IModel<String> getDisplayModel(DataSet ds, boolean html) {
				if (ds==null)
					return new Model<String>("err");
				String type = ds.getDataSetType().getLabel();
				if (type==null)
					return new Model<String>("err");
				boolean isexternal = ds.isExternal();
				if (isexternal && html)
						return new Model<String>("<span class=\"external\">" + type + "</span>");
				else
					return new Model<String>(type);
			}

			@Override
			protected String getContextKey() {
				return DataSetsConsole.this.getName() + super.getContextKey();
			}
		});


		{
			KbeePredicateGridColumn<T> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("status"), "status", 	obj ->  obj.getState() != null ? obj.getState().getLabel(getUser().getLocale()) : "err"   );
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getUser().getLocale()) : "err");
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			statusColumn.setDefaultWidth(200);
			this.columns.add(statusColumn);
		}


		{
			KbeePredicateGridColumn<T> elementsColumn = new KbeePredicateGridColumn<>("elements1", getLabel("elements"),
			obj ->  NumberFormatter.formatNumber( getContentDao().getTotalElements(obj), getSessionUser().getLocale()));
			elementsColumn.setContextKey(this.getName() + elementsColumn.getContextKey());
			elementsColumn.setCssValueResolver(obj ->   getNumberClass(obj));
			elementsColumn.setHeaderCssClass("centered");
			elementsColumn.setLabelCss("number-md");
			this.columns.add(elementsColumn);
		}

		
		

		{							
			KbeePredicateGridColumn<T> aliasColumn = new KbeePredicateGridColumn<>("alias", getLabel("alias"), obj ->   obj.getAlias() );
			aliasColumn.setContextKey(this.getName() + aliasColumn.getContextKey());
			this.columns.add(aliasColumn);
		}

		
		{							
			KbeePredicateGridColumn<T> cColumn = new KbeePredicateGridColumn<>("canonical", getLabel("canonical-title"), 
			obj ->  (obj.isCanonical() ? getLabel("canonical").getObject() : getLabel("standard").getObject()));
			
			cColumn.setHtmlValueResolver(obj -> (obj.isCanonical() ? ("<span class=\"no\">"+getLabel("canonical").getObject()+"</span>") : 
			("<span class=\"yes\">"+getLabel("standard").getObject()+"</span>")));
			
			cColumn.setContextKey(this.getName() + cColumn.getContextKey());
			this.columns.add(cColumn);
		}
		
		
		{																						
			KbeePredicateGridColumn<T> userColumn = new KbeePredicateGridColumn<>("builtin", getLabel("builtin"), obj ->  (obj.isAggregation() ? getLabel("built-in").getObject() : getLabel("standard").getObject()));
			userColumn.setHtmlValueResolver(obj -> (obj.isAggregation() ? ("<span class=\"no\">"+getLabel("built-in").getObject()+"</span>") : ("<span class=\"ysses\">"+getLabel("standard").getObject()+"</span>")));
			userColumn.setContextKey(this.getName() + userColumn.getContextKey());
			this.columns.add(userColumn);
		}

		
		
		{
			KbeePredicateGridColumn<T> statusColumn = new KbeePredicateGridColumn<>("classifiers", getLabel("classifiers"), obj -> getClassifiersStr(obj) );
			statusColumn.setHtmlValueResolver(obj -> getClassifiersHTML(obj));
			statusColumn.setTextValueResolver(obj -> getClassifiersStr(obj));
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			statusColumn.setDefaultWidth(640);
			statusColumn.setPreferred(false);
			this.columns.add(statusColumn);
		}

		

		
		{
			KbeePredicateGridColumn<T> userColumn = new KbeePredicateGridColumn<>("user", getLabel("username"), obj ->   obj.getLastModifiedUser() != null ? obj.getLastModifiedUser().getFirstLastName() : "err" );
			userColumn.setContextKey(this.getName() + userColumn.getContextKey());
			this.columns.add(userColumn);
		}


		

		{
			KbeePredicateGridColumn<T> idColumn = new KbeePredicateGridColumn<>("id", getLabel("id"), "id", obj ->  String.valueOf(obj.getId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			idColumn.setPreferred(false);
			this.columns.add(idColumn);
		}

		return this.columns;
	}

	
	protected String getClassifiersHTML(T obj) {
		StringBuilder str=new StringBuilder(); 
		for (Classifier c:((DataSet) obj).getClassifiers()) {
			
			if (str.length()>0)
				str.append("<span class=\"ago\"> | </span>");

			String ln = getServerUrl()+"/model/classifiers/" + String.valueOf(c.getId());
			String an = "<a class=\"btn-link\" href=\""+ ln + "\">"+ c.getName() + "</a>";
					
			str.append(an);
		}
		return str.toString(); 
				
	}

	
	
	protected String getClassifiersStr(T obj) {
		StringBuilder str=new StringBuilder(); 
		for (Classifier c:((DataSet) obj).getClassifiers()) {
			if (str.length()>0)
				str.append(" | ");
				str.append(c.getName());
		}
		return str.toString(); 
				
	}

	protected String getNumberClass(T obj) {
		try {
		long ref=  getContentDao().getTotalElements((DataSet) obj);
		return ref>0?"col number-md info" : "col number-md";
		} catch (Exception e) {
			logger.error(e);
			return "number-md";
		}
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	protected Page getPage(IModel<T> model, long index, boolean edition) {
		throw new KbeeRuntimeException("not implemented");
	}

	
	private void addLabels(DataSet dataset) {
		DataSetType type = dataset.getDataSetType();
		if (type==DataSetType.ENTITY || type==DataSetType.STRING) {
			for (Classifier c: getContentDao().getClassifiers(getDomain())) {
				if (c.getDataSet().getDataSetType()==DataSetType.LABEL) {
					List<Classifier> list = dataset.getClassifiers();
					if (!list.contains(c)) {
						list.add(c);
						dataset.setClassifiers(list);
						dataset.getService(DOMObjectService.class).update("add classfier " + c.getName());
					}
					break;
				}
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private void createDataSet(String key) {
		if (key.equals("string")) {
			try {
				Object dataset = ServiceLocator.getService(ObjectFactoryService.class).createDataSet(DataSetType.STRING);
				((T) dataset).setName(new StringResourceModel("newelement", DataSetsConsole.this, null).getObject());
				addLabels((DataSet) dataset);
				Page page = getDataSetPage(DataSetsConsole.this.getModel((T)dataset), 0, true, true);
				setResponsePage(page);
			}
			catch (Exception e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			}
		}
		else if (key.equals("entity")) {
			try {
				Object dataset = ServiceLocator.getService(ObjectFactoryService.class).createDataSet(DataSetType.ENTITY);
				((T) dataset).setName(new StringResourceModel("newelement", DataSetsConsole.this, null).getObject());
				addLabels((DataSet) dataset);
				Page page = getDataSetPage(DataSetsConsole.this.getModel((T)dataset), 0, true, true);
				setResponsePage(page);
			}
			catch (Exception e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			}
		}
		else if (key.equals("person")) {
			try {
				Object dataset = ServiceLocator.getService(ObjectFactoryService.class).createDataSet(DataSetType.PEOPLE);
				((T) dataset).setName(new StringResourceModel("newelement", DataSetsConsole.this, null).getObject());
				addLabels((DataSet) dataset);
				Page page = getDataSetPage(DataSetsConsole.this.getModel((T)dataset), 0, true, true);
				setResponsePage(page);
			}
			catch (Exception e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			}
		}
		else {
			try {
				Object dataset = ServiceLocator.getService(ObjectFactoryService.class).createDataSet(DataSetType.LABEL);
				((T) dataset).setName(new StringResourceModel("newelement", DataSetsConsole.this, null).getObject());
				Page page = getDataSetPage(DataSetsConsole.this.getModel((T)dataset), 0, true, true);
				setResponsePage(page);
				// refresh(target);
			}
			catch (Exception e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			}
		}
	}

	/**
	 *
	 * 
	 */
	@Override													
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<T> browser) {
		
		if (this.items!=null)
			return this.items;
		
		this.items = new ArrayList<ToolbarItem>();
				
		this.items.add(new ToolbarAlert(browser, Align.TOP_LEFT) {
			protected IModel<String> getLabel() {
				return new StringResourceModel("readonly", this, null);
			}
			@Override
			public boolean isVisible() {
				if (isRoot())
					return false;
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS) 
					return true;
				return false;
			}
		});

		/**
		 * New Menu
		 */
		this.items.add(new NewDataSetButton(browser, ToolbarItem.Align.TOP_LEFT) {
			
			@Override
			protected void create(String s) {
				createDataSet(s);
			}
			
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS)
					return false;
				return true;
			}
			
			@Override
			public boolean isVisible() {
				if (isRoot())
					return true;
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS) 
					return false;
				return true;
			}
		});
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return DataSetsConsole.this.getName();}, new Model<String>(DataSetsConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		this.items.add(infoButton);
		
		
		return this.items;
	}
	

	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {

		try {		
			if (((DataSet) rowmodel.getObject().getObject()).getState()==ObjectState.ARCHIVED)	return "archived-state";
			if (((DataSet) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)	return "deleted-state";
			return null;
				
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	
	@Override
	protected Panel getPanel(IModel<T> model) {
		return new ExpandedPanel<T>("editor", this, model);
	}

	
	@Override
	protected Panel getPanel(IModel<T> model, List<String> list) {
		return new ExpandedPanel<T>("editor", this, model, list);
	}


	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<T>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<T> event) {
				setResponsePage(getDataSetPage(event.getModel(), event.getIndex(), false, false));
			}
		});
	}
	
	@Override
	protected void addModals() {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}
	
	/**
	 * 
	 * @param model
	 * @param index
	 * @param is_new
	 * @param editon
	 * @return
	 * 
	 */
	protected Page getDataSetPage(IModel<T> model, int index, final boolean is_new, final boolean editon) {
		PageParameters pa = new PageParameters();
		pa.add("id", model.getObject().getId().toString());
		pa.add("isnew", is_new?"yes":"no");
		return ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-dataset-page", pa);
	}

	
	protected Page getPage(IModel<DataSetMember> model, long index, boolean edition, boolean isnew) {
		PageParameters pa = new PageParameters();
		pa.add("id", model.getObject().getId().toString());
		pa.add("isnew", isnew?"yes":"no");
		return ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-member-page", pa);
	}
	
	
	/**
	protected Panel getNavigationPanel(long index) {
		GlobalNavigationBar<DataSet> navigationbar = new GlobalNavigationBar<DataSet>("navigation",  getDisplayName().getObject()) {
			@Override
			public void onNavigate(DataSet dataset) {
				IModel<DataSet> model = new ObjectModel<DataSet>(dataset);
				model.detach();
				Page page = new DataSetPage<DataSet>(model,  this, false, false);
				setResponsePage(page);
			}
			@Override
			public void onDetach() {
				super.onDetach();
				DataSetsConsole.this.onDetach();
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
		navigationbar.setSearchPlaceHolder(new StringResourceModel("searchplaceholder", DataSetsConsole.this, null).getString());
		return navigationbar;
	}
	**/
	
	protected Page getClassifierPage(IModel<Classifier> model, final boolean editon, final boolean is_new) {
		return new ClassifierModelPage(model, editon, is_new);
	}
	
	
	/**
	 * Built in DataSet
----------------

	 */
	
	protected String getDescription() {
		StringBuilder str = new StringBuilder();
			str.append("<section>");
			str.append("<h3>"+ getDisplayName().getObject() + "</h3>");
			str.append(new StringResourceModel("console-description", this, null).getString());
			str.append("</section>");
			return str.toString();
	}

}
