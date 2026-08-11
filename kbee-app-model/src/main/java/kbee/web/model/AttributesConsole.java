package kbee.web.model;

import java.io.File;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelReference;
import com.novamens.content.model.ModelService;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.NewButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarAlert;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
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
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.service.ApplicationSiteMapService;

/**
 * For the other components of the Information Model
 * see {@link ContentTemplatesConsoleConsole} {@link DataSetsConsole},  {@link AttributesConsole}, {@link ClassifiersConsole}
 * 
 */
@SuppressWarnings("serial")
public abstract class AttributesConsole extends  AbstractFacetedConsole<Attribute> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AttributesConsole.class.getName());
	
	private List<GridColumn<SearchResult,String>> columns;

	private Map<Serializable, List<String>> mp = null;
	private Map<Serializable, List<String>> mp_link = null;
	
	private List<ToolbarItem> items;

	
	public AttributesConsole(Query query) {
		super("attributes", query);
		this.is_deleted_visible = getUserPreference("deleted-visible", "no").equals("yes") ? true : false;
	}

	
	@Override
	protected String getIcon(IModel<Attribute> model) {
		return null;
	}

	
    protected  IModel<Attribute> getModel(Attribute object) {
		return new ObjectModel<Attribute>(object, true);
	}

    
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
		this.items=null;
	}
	
	/**
	 * No Search Filters
	 */
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new AttributesQuery(isDeletedVisible()));
	}
	

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new AttributesBC());
	};
	
	@Override
	protected boolean hasExpander() {
		return true;
	}

	/**
	 * 
	 * 
	 */
	@Override
	protected Panel getMenu(IModel<Attribute> model) {
		
		ContextMenuPanel<Attribute> menu = new ContextMenuPanel<Attribute>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<Attribute>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Attribute> getItem(String id) {
				return new AjaxMenuItemPanelV5<Attribute>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						PageParameters pa = new PageParameters();
						pa.add("id", getModel().getObject().getId().toString());
						setResponsePage(  ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-attribute-page", pa));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("open").getObject();
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Attribute>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Attribute> getItem(String id) {
				return new AjaxMenuItemPanelV5<Attribute>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							getModel().getObject().setState(ObjectState.ARCHIVED);
							
							getModel().getObject().getService(DOMObjectService.class).update(ObjectState.ARCHIVED.getLabel());
							AttributesConsole.this.refresh(target);
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
						return getConsoleLabel("archive-verb").getObject();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Attribute>() {
			@Override
			public AbstractMenuItemPanelV5<Attribute> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Attribute>(id) {
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
		
		
		
		
		
		
		
		
		menu.addItem(new MenuItemFactory<Attribute>() {
			private static final long serialVersionUID = 1L;
			@Override						
			public AbstractMenuItemPanelV5<Attribute> getItem(String id) {
				return new AjaxMenuItemPanelV5<Attribute>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						try {
						StringBuilder str = new StringBuilder();
						for (ContentTemplate c:getContentDao().getContentTemplates(getDomain())) {
							
							if (c.getState()!=ObjectState.DELETED) {
								boolean found = false;
								for (AttributeTemplate a: c.getAttributes()) {
									if (a.getAttribute().equals(getModel().getObject())) {
										found = true;
										break;
									}
								}
								if (!found) {
									 KbeeAttributeTemplate c_te = new KbeeAttributeTemplate(getModel().getObject());						
									 c_te.setMetadataSubtitle(false);
									 c.addAttribute(c_te);
									 c.getService(DOMObjectService.class).update("Add Attribute -> " + getModel().getObject());
									 if (str.length()>0)
										 str.append(", ");
									 str.append(c.getDisplayName());
								}
							}
						}
						FeedbackHelper.showInfoToast( str.toString());
						} catch (Exception e) {
							logger.error(e);
							setResponsePage( new ApplicationErrorPage<>(e));
						}

					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("add-to-all-content-templates").getObject();
					}
				};
			}
		});
		
		
		
		
		menu.addItem(new MenuItemFactory<Attribute>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Attribute> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Attribute>(id) {
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
						return  true;
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<Attribute>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Attribute> getItem(String id) {
				return new AjaxMenuItemPanelV5<Attribute>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						List<ModelReference> references = getReferences(); 
						if (!references.isEmpty()) {
							String message = "The classifier cannot be deleted because it is referenced by</br>";
							for (int i=0; i<4 && i<references.size(); i++) {
								message += "</br><a target=\"_blank\" href=\""+references.get(i).getUrl()+"\">"+references.get(i).getDescription()+"</a>";
							}
							if (references.size()>4) {
								message += "</br></br> and others...";
							}
							InfoDialog infoDialog = (InfoDialog) getInformationModal();
							infoDialog.open(target,() -> {return "References";}, new Model<String>(message));
						}
						else {
							getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getName()), Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											DOMObjectService objectService = getModel().getObject().getService(DOMObjectService.class);
											objectService.asyncDelete();
										}
										catch (DataIntegrityViolationException | ConstraintException e) {
											getErrorDialog().open(target, getConsoleLabel("error.constraint"));
										}
										catch (Exception e) {
											getErrorDialog().open(target, new Model<String>(e.getMessage()));
										}
										AttributesConsole.this.refresh(target);
									}
								}
							});
						}
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("contextmenu.delete").getObject();
					}
					public boolean isEnabled() {
						
						if (getModel().getObject().getState()==ObjectState.DELETED)
							return false;

						if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS && !isRoot()) 
							return false;
						
						return !isSupport();
					}
					public List<ModelReference> getReferences() {
						return getDomain().getService(ModelService.class).getReferences(getModelObject());
					}
				};
			}
		});
		
		
		return menu;
	}


	private Map<Serializable, List<String>> getAttContentTemplates() {
		if (mp==null) {
			generateAttContentTemplates();
		}
		return mp;
	}
	
	private Map<Serializable, List<String>> getAttContentTemplatesHTML() {
		if (mp==null) {
			generateAttContentTemplates();
		}
		return mp_link;
	}
	
	private void generateAttContentTemplates() {
		
			mp = new HashMap<Serializable, List<String>>();
			mp_link =new HashMap<Serializable, List<String>>();
			
			for (ContentTemplate t: getContentDao().getTemplates(getDomain())) {
		
				for (AttributeTemplate at: t.getAttributes()) {
				
					Attribute a=at.getAttribute();
					
					if (!mp.containsKey(a.getId()))  {
						mp.put(a.getId(), new ArrayList<String>());
						mp_link.put(a.getId(), new ArrayList<String>());
					}
					mp.get(a.getId()).add(t.getName());
					mp_link.get(a.getId()).add( "<a class=\"btn-link\"  href=\""+getLink(t)+"\">"+t.getName()+"</a>");
				}
			}
	}
	
	private String getLink(ContentTemplate t) {
		return getServerUrl()+"/model/contentclass/"+t.getId().toString();
	}

	/***
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (columns!=null)
			return columns;
	
		/**
		Map<Attribute, List<ContentTemplate>> mp=new HashMap<Attribute, List<ContentTemplate>>();
		for (ContentTemplate t: getContentDao().getTemplates(getDomain())) {
			for (AttributeTemplate at: t.getAttributes()) {
				Attribute a=at.getAttribute();
				if (!mp.containsKey(a)) 
					mp.put(a, new ArrayList<ContentTemplate>());
				mp.get(a).add(t);
			}
		}
		**/

		
		
		columns = new ArrayList<GridColumn<SearchResult,String>>();

		this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), new Model<String>("St")));
		
		/**
		{
			@Override
			protected String getContextKey() {
				return AttributesConsole.this.getName() + super.getContextKey();
			}
		});
		*/

		
		{
			LinkPredicateKbeeGridColumn<Attribute> titleColumn =
					new LinkPredicateKbeeGridColumn<>("title", getLabel("name"), "title_sort",
							obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			titleColumn.setDefaultWidth(GridPanel.TITLE_DEFAULT_WIDTH);
			columns.add(titleColumn);
		}


		this.columns.add(new LastModifiedColumn<Attribute>("modified", getLabel("modified"), "modified") {
			@Override
			protected OffsetDateTime getOffsetDateTime(Attribute object) {
					return object.getLastModifiedOffsetDateTime();
			}
			
			@Override
			protected String getContextKey() {
				return AttributesConsole.this.getName() + super.getContextKey();
			}
		});
		

		
		{
			KbeePredicateGridColumn<Attribute> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("status"),
					obj ->  obj.getState() != null ? obj.getState().getLabel(getUser().getLocale()) : "err"   );

			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getUser().getLocale()) : "err");
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			this.columns.add(statusColumn);
		}


		{
			KbeePredicateGridColumn<Attribute> typeColumn = new KbeePredicateGridColumn<>("type", getLabel("type"),
					obj ->  obj.getType().getLabel());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			this.columns.add(typeColumn);
		}

		
		
		{
			KbeePredicateGridColumn<Attribute> statusColumn = new KbeePredicateGridColumn<>("contenttemplates", getLabel("contenttemplates"), obj -> getContentTemplatesHTML(obj) );
			statusColumn.setHtmlValueResolver( obj -> getContentTemplatesHTML(obj) );
			statusColumn.setTextValueResolver( obj -> getContentTemplatesStr(obj) );
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			this.columns.add(statusColumn);
		}



		
		
		
		
		
		
		
		
		{
			KbeePredicateGridColumn<Attribute> multiplicityColumn = new KbeePredicateGridColumn<>("gridcolumn", getLabel("gridcolumn"),
					obj ->  getBooleanYesNoText(obj.isDefaultGridColumn(), false));

			multiplicityColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isDefaultGridColumn(), true));
			multiplicityColumn.setContextKey(this.getName() + multiplicityColumn.getContextKey());
			multiplicityColumn.setRowCssClass("centered");
			multiplicityColumn.setHeaderCssClass("centered");
			this.columns.add(multiplicityColumn);
		}


		
		{						
			KbeePredicateGridColumn<Attribute> typeColumn = new KbeePredicateGridColumn<>("alias", getLabel("alias"),	obj ->  obj.getAlias());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			this.columns.add(typeColumn);
		}

		{						
			KbeePredicateGridColumn<Attribute> typeColumn = new KbeePredicateGridColumn<>("predicate", getLabel("predicate"),	obj ->  obj.getPredicate());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			this.columns.add(typeColumn);
		}

		{
			KbeePredicateGridColumn<Attribute> metasubtitleColumn = new KbeePredicateGridColumn<>("defaultstructure", getLabel("defaultstructure"),
					obj ->  getBooleanYesNoText(obj.isDefaultStructure(), false));					
			metasubtitleColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isDefaultStructure(), true));
			metasubtitleColumn.setRowCssClass("centered");
			metasubtitleColumn.setHeaderCssClass("centered");
			metasubtitleColumn.setContextKey(this.getName() + metasubtitleColumn.getContextKey());
			metasubtitleColumn.setPreferred(false);
			this.columns.add(metasubtitleColumn);
		}

		{
			KbeePredicateGridColumn<Attribute> multiplicityColumn = new KbeePredicateGridColumn<>("multiplicity", getLabel("multiplicity"),
					obj ->  obj.getMultiplicity()!=null ? obj.getMultiplicity().getLabel(getUser().getLocale()) : "" );
			multiplicityColumn.setContextKey(this.getName() + multiplicityColumn.getContextKey());
			this.columns.add(multiplicityColumn);
		}

		{
			KbeePredicateGridColumn<Attribute> userColumn = new KbeePredicateGridColumn<>("user", getLabel("username"),
					obj ->  obj.getLastModifiedUser() != null ? obj.getLastModifiedUser().getFirstLastName() : "err");
			userColumn.setContextKey(this.getName() + userColumn.getContextKey());
			this.columns.add(userColumn);
		}
											
		
		
		{
			KbeePredicateGridColumn<Attribute> idColumn = new KbeePredicateGridColumn<>("id", getLabel("id"),
					obj ->  String.valueOf(obj.getId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			idColumn.setPreferred(false);
			this.columns.add(idColumn);
		}


		{						
			KbeePredicateGridColumn<Attribute> visibilityColumn = new KbeePredicateGridColumn<>("visibility", getLabel("visibility"),
					obj ->  getVisibilityColumnText(obj,false));
			visibilityColumn.setHtmlValueResolver(obj -> getVisibilityColumnText(obj,true));
			visibilityColumn.setContextKey(this.getName() + visibilityColumn.getContextKey());
			visibilityColumn.setPreferred(false);
			this.columns.add(visibilityColumn);
		}
		
		/**
		{
			KbeePredicateGridColumn<Attribute> metasubtitleColumn = new KbeePredicateGridColumn<>("metadatasubtitle", getLabel("metadatasubtitle"),
					obj ->  getBooleanYesNoText(obj.isMetadataSubtitle(), false));
			metasubtitleColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isMetadataSubtitle(), true));
			metasubtitleColumn.setRowCssClass("centered");
			metasubtitleColumn.setHeaderCssClass("centered");
			metasubtitleColumn.setPreferred(false);
			metasubtitleColumn.setContextKey(this.getName() + metasubtitleColumn.getContextKey());
			this.columns.add(metasubtitleColumn);
		}
		**/
		
		return columns;
	}

	protected KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	@Override
	protected Panel getPanel(IModel<Attribute> model, List<String> snippets) {
		return new ExpandedPanel<Attribute>("editor", this, model, snippets);
	}
	
	
	@Override
	protected Panel getPanel(IModel<Attribute> model) {
		return new ExpandedPanel<Attribute>("editor", this, model);
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
	 
		add(new WicketEventListener<ClickEvent<Attribute>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Attribute> event) {
				PageParameters pa = new PageParameters();
				pa.add("id", event.getModel().getObject().getId().toString());
				setResponsePage(  ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-attribute-page", pa));
			}
		});
	}

	
	@Override																						
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Attribute> browser) {
		
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
				if (isFreeVersion()) 
					return true;
				return false;
			}
		});
		
		this.items.add(new NewButton(browser, Align.TOP_LEFT) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled() {
				if (isFreeVersion()) 
					return false;
				return !isSupport();
			}
			@Override
			public boolean isVisible() {
				if (isFreeVersion() && !isRoot())
					return false;
				return true;
			}
			public void onClick() {
				try {
					Object cla = ServiceLocator.getService(ObjectFactoryService.class).createAttribute();
					((KbeeAttribute) cla).setName(new StringResourceModel("newelement", AttributesConsole.this, null).getObject());
					((KbeeAttribute) cla).setMultiplicity(Multiplicity.M01);
					PageParameters pa = new PageParameters();
					pa.add("id", ((KbeeAttribute) cla).getId().toString());
					pa.add("isnew", "true");
					setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-attribute-page", pa));
				}
				
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>(e));
					
				}
			}
		});
		
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return AttributesConsole.this.getName();}, new Model<String>(AttributesConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		this.items.add(infoButton);
		
		return items;
	}


	
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	protected String getContentTemplatesStr(Attribute obj) {
		StringBuilder str=new StringBuilder();

		if (!this.getAttContentTemplates().containsKey(obj.getId()))
			return str.toString();
		
		for (String s: this.getAttContentTemplates().get(obj.getId())) {
			if (str.length()>0)
				str.append(" | ");
			str.append(s);
		}
		return str.toString(); 
	}
	
	
	protected String getContentTemplatesHTML(Attribute obj) {
		
		StringBuilder str=new StringBuilder();
		
		if (!this.getAttContentTemplatesHTML().containsKey(obj.getId()))
			return str.toString();
		
		for (String s: this.getAttContentTemplatesHTML().get(obj.getId())) {
			if (str.length()>0)
				str.append(" | ");
			str.append(s);
		}
		return str.toString(); 
	}

	
	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}

	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {

		try {		
			
			if (((Attribute) rowmodel.getObject().getObject()).getState()==ObjectState.ARCHIVED) return "archived-state";
			if (((Attribute) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)	 return "deleted-state";
			return null;
				
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
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
	                        return new StringResourceModel("show-deleted", AttributesConsole.this, null).getObject();
	                    }
	
	                    @Override
	                    public void onClick(AjaxRequestTarget target) throws Exception {
	                    	AttributesConsole.this.setDeletedVisible(!AttributesConsole.this.isDeletedVisible());
	                    	setResponsePage(new AttributesPage());
						}
	
	                    @Override
	                    public boolean isIconVisible() {
	                        return AttributesConsole.this.isDeletedVisible();
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

    
    
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}


	private String getBooleanYesNoText(boolean value, boolean html) {
		String strValue = value ?"yes":"no";
		if(html)
			strValue = new StringResourceModel(strValue, this, null).getObject();
		return strValue;
	}


	private String getVisibilityColumnText(Attribute attr, boolean html) {
		List<Library> list = getRepository(Library.class).findAll(attr.getDomain() );
		StringBuilder str = new StringBuilder();
		for (Library ca: list) {
			if ( attr.isVisible(ca.getKey())) {
				if(str.length()>0) {
					if(html)
						str.append("<span class=\"ago\"> | </span>");
					else
						str.append(" | ");
				}
				str.append(ca.getDisplayName());
			}
		}
		return str.toString();
	}
}

