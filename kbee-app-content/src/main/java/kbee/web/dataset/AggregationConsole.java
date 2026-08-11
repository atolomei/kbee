package kbee.web.dataset;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.query.AggregationQuery;
import com.novamens.kbee.content.query.MemberQuery;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.NewButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.NumberFormatter;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ClassificableNameColumnPanel;
import kbee.web.console.MenuButtonToolbarItem;

@SuppressWarnings("serial")
public class AggregationConsole extends AbstractFacetedConsole<DataSetMember> {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetMembersConsole.class.getName());
	
	private IModel<DataSet> aggregationmodel; 
	private IModel<DataSetMember> aggregatormodel;
	private List<GridColumn<SearchResult,String>> columns = null;
	private List<ToolbarItem> items;
	
	private List<ToolbarItem> selection_toolbar;

	final boolean role_admin =
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members	= role_model || role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_support	 = 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SUPPORT.getId());
	
	private Map<String, Long> references = new HashMap<String, Long>();
	
	public AggregationConsole(String id, IModel<DataSet> aggregation, IModel<DataSetMember> aggregator) {
		super(id, "members", null); 		
		this.aggregationmodel = aggregation;
		this.aggregatormodel = aggregator;
		setQuery(newQuery());
	}
	
	
	@Override
	protected String getIcon(IModel<DataSetMember> model) {
		return null;
	}
	
	protected  IModel<DataSetMember> getModel(DataSetMember object) {
		return new ObjectModel<DataSetMember>(object, true);
	}
	
	@Override
	public void onDetach() {

		this.columns=null;
		
		if (this.items!=null) {
			for (ToolbarItem item: items) {
				item.detach();
			}
		}
		
		if (this.selection_toolbar!=null) {
			for (ToolbarItem item: selection_toolbar) {
				item.detach();
			}
		}
		
		super.onDetach();
	}

	@Override
	public String getDownloadFileName() {
		String dname;
		try {
			dname =  aggregationmodel.getObject().getName().toLowerCase().replaceAll("[ |\\t|\\s|(|)]", "-") + "-" +	aggregatormodel.getObject().getStrValue().toLowerCase().replaceAll("[ |\\t|\\s|(|)]", "-") + "-";
		} catch (Exception e) {
				logger.error(e);
				dname=getName();
		}
		return  dname + new SimpleDateFormat("YYYY-MM-dd").format(new Date());
	}
	
	public DataSetMember getAggregator() {
		return aggregatormodel.getObject();
	}
	
	public IModel<DataSetMember> getAggregatorModel() {
		return aggregatormodel;
	}
	
	public DataSet getAggregation() {
		return aggregationmodel.getObject();
	}
	
	@Override
	public IModel<String> getDisplayName() {
		return new Model<String>(getName());
	}
	
	@Override
	public boolean isMyListsEnabled() {
		return false;
	}
	
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<DataSetMember>>() {
			@Override
			public void onEvent(ClickEvent<DataSetMember> event) {
				setResponsePage(AggregationConsole.this.getPage(event.getModel()));
			}
		});
	}
	
	protected Panel getMenu(IModel<DataSetMember> model) {
		ContextMenuPanel<DataSetMember> menu = new ContextMenuPanel<DataSetMember>(model);
		menu.addItem(new MenuItemFactory<DataSetMember>() {
			@Override
			public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
				return new LinkMenuItemPanel<DataSetMember>(id) {
					public void onClick() {
						setResponsePage(AggregationConsole.this.getPage(getModel()));
					}
					@Override 
					public String getLabel() {
						return getLabelString("menu.open");
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		menu.addItem(new MenuItemFactory<DataSetMember>() {
			@Override
			public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
				return new SeparatorMenuItemPanelV5<DataSetMember>(id) {
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
		menu.addItem(new MenuItemFactory<DataSetMember>() {
			@Override
			public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
				return new AjaxMenuItemPanelV5<DataSetMember>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IModel<String> msg;
						msg = new StringResourceModel("menu.delete-confirmation", AggregationConsole.this, null);
						((StringResourceModel)msg).setParameters(getModelObject().getDisplayName());
						getConfirmationDialog().open(target, msg, Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									delete(getModelObject());
									target.add(AggregationConsole.this);
								}
							}
						});
					}
					@Override
					public boolean isVisible() {
						return true;
					}
					@Override
					public String getLabel() {	
						return getLabelString("menu.delete");
					}
					@Override
					public boolean isEnabled()  {
						return role_dataset_members || ServiceLocator
							.getService(UserService.class)
							.isDeleteable(getModelObject());
					}
				};
			}
		});
		return menu;
	}
	
	public List<GridColumn<SearchResult, String>> getColumns() {

		if (this.columns!=null) 
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("column.title"), "title_sort") {
			
			   @Override
	            public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
	                try {
	                	if (resultmodel.getObject() == null)
	                        cellItem.add(new Label(componentId, "err"));
	                    Object object = resultmodel.getObject().getObject();
	                    if (object != null) {
	                        IModel<DataSetMember> objectmodel = getModel((DataSetMember) object);
	                        cellItem.add(new Label(componentId, objectmodel.getObject().getDisplayName()));
	                    } else {
	                        cellItem.add(new Label(componentId, "err"));
	                    }
	                } catch (Exception e) {
	                    logger.error(e);
	                    cellItem.add(new Label(componentId, e.getClass().getName()));
	                }
	            }
			   
			   
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {
					if (resultmodel.getObject()==null)
						cellItem.add(new Label(componentId, "err"));
					Object object = resultmodel.getObject().getObject();
					if (object!=null) {
						IModel<DataSetMember> objectmodel = getModel((DataSetMember)object);
						cellItem.add(new ClassificableNameColumnPanel<DataSetMember>(componentId, objectmodel) {
							@Override
							protected String getCss() {
								return "cell-label btn-link";
							}
							@Override	
							protected String getDisplayProperty() {
								return "strValue";
							}
						});
					}
					else {
						cellItem.add(new Label(componentId, "err"));
					}
				} 
				catch (Exception e) {
					logger.error(e);
					cellItem.add(new Label(componentId, e.getClass().getName()));
				} 
			}
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					String strValue = ((DataSetMember) object.getObject()).getStrValue();
					return new Model<>(strValue);
				} catch (Exception e) {
					return new Model<>(e.getClass().getName());
				}
			}
			
			
			@Override
			protected String getContextKey() {
				return AggregationConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public int getDefaultWidth() {
				return GridColumn.DEFAULT_TITLE_COLUMN_WIDTH;
			}
		});
		
		this.columns.add(new LastModifiedColumn<ENotiRule>("modified", getLabel("column.modified"), "modified") {
			@Override
			protected String getContextKey() {
				return AggregationConsole.this.getName() + super.getContextKey();
			}
		});

		
		
		
		// -------------------------------------
		
		if (isRoot() || isAdmin()) {
			
			this.columns.add(new GridColumn<SearchResult, String>("references", new Model<String>(getLabel("references").getObject()+" <span class=\"only-root\"> (admin)</span>")) {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						Long ref=getReferences((DataSetMember)object.getObject());
						String sr = NumberFormatter.formatNumber(ref,  getSessionUser().getLocale());  
						return new Model<String>(sr);
						
					} catch (Exception e) {
						logger.error(e);
						return new Model<String>(e.getClass().getSimpleName() + " | " +  e.getMessage());
					}
				}
				@Override
				protected String getContextKey() {
					return AggregationConsole.this.getName() + super.getContextKey();
				}
				
				
				@Override
				public String getCssClass() {
						return "col col-xs-1 col-md-1 col-lg-1 ui-resizable";
				}
				
				protected String getLabelCss(IModel<SearchResult> model) {
					Long ref=getReferences((DataSetMember) model.getObject().getObject());
					return ref>0?"number-md info" : "number-mdx";
				}
	
				@Override
				protected String getLabelCss() {
					return "number-mdx";
				}
				
				@Override
				public String getHeaderCssClass() {
					return super.getHeaderCssClass()+" centered";
				}
				
				
				@Override
				public boolean isPreferred() {
					return false;
				}
			});
		}
		

		
		
		
		
		
		
		
		
		return columns;
	}
	
	
	/**
	 * selection actions
	 * 
	 */
	
	/**
	 * Toolbar -  Selection 
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<DataSetMember> browser) {

		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
	
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
	
			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
			
			protected String getLabelStr() {
				 return new StringResourceModel("delete", AggregationConsole.this).getObject();
			 }
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					AggregationConsole.this.delete(getBrowser().getSelection());
					AggregationConsole.this.resetSelection();
					AggregationConsole.this.refresh(target);
					
				} catch (Exception e) {
					logger.error(e);
					
				}
			}
		});
		
		return this.selection_toolbar; 
	
	}
	

	
	/**
	 * toolbar actions
	 */
	@Override													
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<DataSetMember> browser) {
		
		if (items!=null)
			return items;
		
		items = new ArrayList<ToolbarItem>();
		
		items.add( new NewButton(browser, Align.TOP_LEFT) {
			@Override
			public void onClick() {
				try {
					DataSetMember value = createValue();
					setResponsePage(AggregationConsole.this.getPage(getModel(value)));
				}	
				catch (Exception e) {
					logger.error(e);
				}
			}
			@Override
			public boolean isVisible() {
				if (getAggregation().isExternal()) {
					return false;
				}	
				if (getAggregation().isReadonly() && !isRoot()) {
					return false;
				}	
				if (role_admin) {
					return true;
				}
				if (ServiceLocator
					.getService(UserService.class)
					.isAdmin(getAggregator().getDataSet())) {
					return true;
				}
				return false;
			}
			protected String getButtonCss() {
				return "btn btn-md btn-primary";
			}
			@Override
			protected IModel<String> getLabel() {
				String label = (new StringResourceModel("new-label",this, null)).getObject();
				return new Model<String>(label);
			}
		});
		
		// Bulk creation
		//
		MenuButtonToolbarItem<DataSet> mn = new MenuButtonToolbarItem<DataSet>(browser, ToolbarItem.Align.TOP_LEFT) {
			
			@Override
			public boolean isVisible() {
				if (getAggregation().isExternal())
					return false;
				if (getAggregation().isReadonly() && !isRoot())
					return false;
				return (role_admin) && !(getDomain().getDomainType()==DomainType.EXPRESS);
			}

			
			@Override
			public String getAddCss() {
				return "btn btn-default btn-md atright";
			}
		};
		
		mn.setTitle(new StringResourceModel("bulk-creation", this, null));
		
		ContextMenuPanel<DataSet> mp=new ContextMenuPanel<DataSet>(null);
		
		mp.addItem(new MenuItemFactory<DataSet>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<DataSet> getItem(String id) {
				return new MenuItemPanelV5<DataSet>(id) {
					@Override
					public void onClick() {
						setResponsePage(new MemberBatchCreationPageV5(new ObjectModel<DataSet>(getAggregation()),  new ObjectModel<DataSetMember>(getAggregator())));
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("add-list", AggregationConsole.this, null).getObject();
						
					}
					@Override
					public String getTarget() {
						return "_blank";
					}
					
					@Override
					public boolean isEnabled() {
						if (getAggregation().isExternal())
							return false;
						if (getAggregation().isReadonly() && !isRoot())
							return false;
						return (role_admin || role_support) && !(getDomain().getDomainType()==DomainType.EXPRESS);
					}

				};
			}
		});

 

		mn.setMenuPanel(mp);
		
		items.add(mn);
		
		return items;
	}
	
	@Override
	public Query newQuery() {
		return new AggregationQuery(getQueryIndex(), getAggregation(), getAggregator());
	}
	
	public Page getConsolePage(Query query, long index) {
		return null;
	}
	
	private Page getPage(IModel<DataSetMember> model) {
		return new MemberPage(model, getAggregatorModel(), null);
	}
	
	
	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	protected boolean isSelectionEnabled() {
		return true;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}
	
	protected DataSetMember createValue() {
		DataSet dataSet = getAggregation();
		String name = (new StringResourceModel("new-label",this, null)).getObject()+" "+dataSet.getDisplayName();
		Map<ModelElement, List<Object>> classification = new HashMap<ModelElement, List<Object>>();
		List<Object> members = new ArrayList<Object>();
		members.add(getAggregator());
		classification.put(getClassifier(getAggregation(), getAggregator()), members);
		DataSetMember value = (DataSetMember)ServiceLocator.getService(ObjectFactoryService.class).createMember(dataSet, name, classification);
		return value;
	}
	

	protected Panel getPanel(IModel<DataSetMember> model,  List<String> snippets) {
		return new DataSetMemberHitExpandedPanel("editor", this, model, snippets);
	}
	
	protected Panel getPanel(IModel<DataSetMember> model) {
		return new DataSetMemberHitExpandedPanel("editor", this, model);
	};
	
	
	/**
	 * 
	 * 
	 */
	protected void delete(List<?> selection) {
		@SuppressWarnings("unchecked")
		List<IModel<DataSetMember>> list = (List<IModel<DataSetMember>>)  selection;
		for (IModel<DataSetMember> c:list) {
			try {
				delete(c.getObject());
			} catch (Exception e) {
				logger.error(e);
				// TODO INFORM ERROR
			}
		}
	}
	
	private void delete(DataSetMember member) {
		try {
			member.getService(DOMObjectService.class).delete();
		}
		catch (Exception e1) {
			try {
				member.getService(DOMObjectService.class).markAsDeleted();
			}
			catch (Exception e2) {
				logger.error(e2);
			}
		}	
	}
	
	private Classifier getClassifier(DataSet aggregation, DataSetMember aggregator) {
		Classifier classifier = null;
		for (ModelElementTemplate template : aggregation.getStructure()) {
			if (template.getElement() instanceof Classifier) {
				if (template.getElement() instanceof Classifier) {
					classifier = (Classifier)template.getElement();
					if (classifier.getDataSet().equals(aggregator.getDataSet())) {
						return classifier;
					}
				}
			}
		}
		return classifier;
	}
	
	protected Long getReferences(DataSetMember member) {
		String id = String.valueOf(member.getId());
		Long references = this.references.get(id);
		if (references==null) {
			try {
				Query query = new MemberQuery(getQueryIndex(), member);
				references = Long.valueOf(query.execute().size());
				this.references.put(id, references);
			}
			catch (Exception e) {
				logger.error(e);
				references = (long)-1;
			}
		}
		return references;
	}


	

	
}
