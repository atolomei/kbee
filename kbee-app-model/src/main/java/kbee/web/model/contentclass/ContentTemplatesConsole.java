package kbee.web.model.contentclass;


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
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.NewButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
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
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
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
import kbee.web.error.ApplicationErrorPage;
import kbee.web.model.ContentClassesQuery;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.object.ObjectStatusColumn;


/**
 * <p>
 * 
 * 
 * 
 * For the other components of the Information Model
 * see {@link ContentTemplatesConsoleConsole} {@link DataSetsConsole},  {@link AttributesConsole}, {@link ClassifiersConsole}
 *</p>
 */
@SuppressWarnings("serial")
public abstract class ContentTemplatesConsole extends AbstractFacetedConsole<ContentTemplate> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentTemplatesConsole.class.getName());

	private IModel<ContentTemplate> model;

	private List<GridColumn<SearchResult,String>> columns;
	private List<ToolbarItem> items;
	private boolean is_deleted_visible = false;
	

	/**
	 * 
	 * @param query
	 */
	public ContentTemplatesConsole(Query query) {
		super("contentclasses", query);
		this.is_deleted_visible = getUserPreference("deleted-visible", "no").equals("yes") ? true : false;
	}

	
	@Override
	protected String getIcon(IModel<ContentTemplate> model) {
		return null;
	}
	/**
	 * 
	 */
	@Override
	 protected  IModel<ContentTemplate> getModel(ContentTemplate object) {
			return new ObjectModel<ContentTemplate>(object, true);
	}

	/**
	 * 
	 */
	
	public ContentTemplate getContentClass() {
		return model.getObject();
	}

	@Override
	public void onDetach() {
		if (this.model!=null)
			this.model.detach();
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
		return setUserPreference(new ContentClassesQuery(isDeletedVisible()));
		
		
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new ContentClassesBC());
	}

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
	                        return new StringResourceModel("show-deleted", ContentTemplatesConsole.this, null).getObject();
	                    }
	
	                    @Override
	                    public void onClick(AjaxRequestTarget target) throws Exception {
	                    	ContentTemplatesConsole.this.setDeletedVisible(!ContentTemplatesConsole.this.isDeletedVisible());
	                     	//ContentTemplatesConsole.this.refresh(target);
	                    	setResponsePage(new ContentTemplatesPage());
						}
	
	                    @Override
	                    public boolean isIconVisible() {
	                        return ContentTemplatesConsole.this.isDeletedVisible();
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


	/***
	 * 
	 * 
	 */
	@Override
	protected Panel getMenu(IModel<ContentTemplate> model) {
	
		ContextMenuPanel<ContentTemplate> menu = new ContextMenuPanel<ContentTemplate>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<ContentTemplate>() {
			@Override
			public AbstractMenuItemPanelV5<ContentTemplate> getItem(String id) {
				return new MenuItemPanelV5<ContentTemplate>(id) {
					public void onClick() {
						try {
							getPage().setResponsePage(getContentTemplatePage(getModel(), 0, false, false));
						} catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<ContentTemplate>(e));
						}
					}
					
					@Override
					public String getTarget() {
						return "_blank";
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("open").getObject();
					}
				};
			}
		});

		
		/**
		menu.addItem(new MenuItemFactory<ContentTemplate>() {
			@Override
			public AbstractMenuItemPanelV5<ContentTemplate> getItem(String id) {
				return new MenuItemPanelV5<ContentTemplate>(id) {
					public void onClick() {
						try {
							Object template = ServiceLocator.getService(ObjectFactoryService.class).cloneTemplate(getModelObject());
							Page page = getContentTemplatePage(ContentClassesConsole.this.getModel((ContentTemplate)template), 0, true, true);
							setResponsePage(page);
						} catch (Exception e) {
							logger.error(e);
							setResponsePage(new ErrorPage<ContentTemplate>(new Model<String>(e.getClass().getName() +  " " + e.getMessage())));
						}
					}
					@Override
					public String getTarget() {
						return "_blank";
					}

					@Override 
					public String getLabel() {
						return getConsoleLabel("clone").getObject();
					}
				};
			}
		});
		**/


		menu.addItem(new MenuItemFactory<ContentTemplate>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<ContentTemplate> getItem(String id) {
				return new AjaxMenuItemPanelV5<ContentTemplate>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							getModel().getObject().setState(ObjectState.ARCHIVED);
							getModel().getObject().getService(DOMObjectService.class).update(ObjectState.ARCHIVED.getLabel());
							FeedbackHelper.showSuccessToast(getLabel()+ " <br/>" + getModel().getObject().getDisplayName());
							ContentTemplatesConsole.this.refresh(target);
						}
						catch (Exception e) {
							logger.error(e);
							FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
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

		
		
		
		
		menu.addItem(new MenuItemFactory<ContentTemplate>() {
			@Override
			public AbstractMenuItemPanelV5<ContentTemplate> getItem(String id) {
				return new SeparatorMenuItemPanelV5<ContentTemplate>(id) {
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
		
		
		menu.addItem(new MenuItemFactory<ContentTemplate>() {
			@Override
			public AbstractMenuItemPanelV5<ContentTemplate> getItem(String id) {
				return new AjaxMenuItemPanelV5<ContentTemplate>(id) {
					public void onClick(AjaxRequestTarget target) {
						getConfirmationDialog().open(target, getConsoleLabel("templatesconsole.deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									String s=executeDelete(target);
									if (s==null)
										FeedbackHelper.showSuccessToast(getLabel()+ " <br/>" + getModel().getObject().getDisplayName());
									ContentTemplatesConsole.this.refresh(target);
								}
							}
						});
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("delete").getObject();
					}
					@Override
					public boolean isEnabled() {
						
						if (getModel().getObject().getState()==ObjectState.DELETED)
							return false;
							
						if (isFreeVersion() && !isRoot())
							return false;
						
						
						return !isSupport();
					}
					
					protected String executeDelete(AjaxRequestTarget target) {
						try {
							
							getModelObject().getService(DOMObjectService.class).asyncDelete();
							return null;
						}
						catch (DataIntegrityViolationException | ConstraintException e) {
							getErrorDialog().open(target, getConsoleLabel("templatesconsole.error.constraint"));
							return e.getClass().getName();
						}
						catch (Exception e) {
							getErrorDialog().open(target, new Model<String>(e.getMessage()));
							return e.getClass().getName();
						}	
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
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		
		
		this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), new Model<String>("St")));
		
		{
			LinkPredicateKbeeGridColumn<ContentTemplate> titleColumn =	new LinkPredicateKbeeGridColumn<>("title", getLabel("name"), "title",	obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			titleColumn.setDefaultWidth(320);
			columns.add(titleColumn);
		}

		this.columns.add(new LastModifiedColumn<ContentTemplate>("modified", getLabel("modified"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected OffsetDateTime getOffsetDateTime(ContentTemplate object) {
					return object.getLastModifiedOffsetDateTime();
			}
			@Override
			protected String getContextKey() {
				return ContentTemplatesConsole.this.getName() + super.getContextKey();
			}
		});


		
		{
			KbeePredicateGridColumn<ContentTemplate> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("status"), "status",	obj ->  obj.getState() != null ? obj.getState().getLabel(getUser().getLocale()) : "err"   );
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getUser().getLocale()) : "err");
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			this.columns.add(statusColumn);
		}


		{
			KbeePredicateGridColumn<ContentTemplate> userColumn = new KbeePredicateGridColumn<>("launchers", getLabel("launchers"),	obj ->  getLaunchers(obj));
			userColumn.setContextKey(this.getName() + userColumn.getContextKey());
			userColumn.setPreferred(true);
			userColumn.setDefaultWidth(380);
			this.columns.add(userColumn);
		}


		{								
			
			KbeePredicateGridColumn<ContentTemplate> elementsColumn = new KbeePredicateGridColumn<>("elements1", getLabel("contents"),		obj ->  NumberFormatter.formatNumber(getContentDao().getTotalContents(obj), getSessionUser().getLocale()));
			elementsColumn.setContextKey(this.getName() + elementsColumn.getContextKey());
			elementsColumn.setCssValueResolver(obj ->   getNumberClass(obj));
			
			elementsColumn.setHeaderCssClass("centered");
			elementsColumn.setLabelCss("number-mdx");
			this.columns.add(elementsColumn);
		}
		

		
		
		{												
			KbeePredicateGridColumn<ContentTemplate> typeColumn = new KbeePredicateGridColumn<>("alias", getLabel("alias"),	obj ->  obj.getAlias());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			this.columns.add(typeColumn);
		}

		

		
		{																						
			KbeePredicateGridColumn<ContentTemplate> titleColumn =	new KbeePredicateGridColumn<>("onlyroot", getLabel("editable"),	obj -> getBooleanYesNoText(!obj.isOnlyRootEdit(),false, "user-space"));
			titleColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(!obj.isOnlyRootEdit(), true, "user-space"));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			titleColumn.setPreferred(true);
			columns.add(titleColumn);
		}

		
		/**
		{
			KbeePredicateGridColumn<ContentTemplate> dataSetColumn = new KbeePredicateGridColumn<>("contentclass", getLabel("class"),	obj ->  obj.getContentClass().getDisplayName()  );
			dataSetColumn.setContextKey(this.getName() + dataSetColumn.getContextKey());
			dataSetColumn.setPreferred(false);
			this.columns.add(dataSetColumn);
		}
		**/

		{
			KbeePredicateGridColumn<ContentTemplate> userColumn = new KbeePredicateGridColumn<>("user", getLabel("username"),	obj ->  obj.getLastModifiedUser() != null ? obj.getLastModifiedUser().getFirstLastName() : "err");
			userColumn.setContextKey(this.getName() + userColumn.getContextKey());
			this.columns.add(userColumn);
		}

		/**
		{
			KbeePredicateGridColumn<ContentTemplate> aliasColumn = new KbeePredicateGridColumn<>("classcode", new Model<String>("Class Code"),	obj ->  obj.getContentClassCode() );
			aliasColumn.setContextKey(this.getName() + aliasColumn.getContextKey());
			aliasColumn.setPreferred(false);
			this.columns.add(aliasColumn);
		}**/
		
		

		/**
		{
			KbeePredicateGridColumn<ContentTemplate> kbaseColumn = new KbeePredicateGridColumn<>("standard", getLabel("standard"),	obj -> getBooleanYesNoText(obj.isComplianceCabinet(),false));			
			kbaseColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isComplianceCabinet(),true));
			kbaseColumn.setRowCssClass("centered");
			kbaseColumn.setHeaderCssClass("centered");
			kbaseColumn.setContextKey(this.getName() + kbaseColumn.getContextKey());
			kbaseColumn.setPreferred(false);
			this.columns.add(kbaseColumn);
			
		}
		{
			KbeePredicateGridColumn<ContentTemplate> kbaseColumn = new KbeePredicateGridColumn<>("kbase", getLabel("kbase"), obj -> getBooleanYesNoText(obj.isKnowledgeBaseCabinet(),false));
			kbaseColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isKnowledgeBaseCabinet(),true));
			kbaseColumn.setRowCssClass("centered");
			kbaseColumn.setHeaderCssClass("centered");
			kbaseColumn.setPreferred(false);
			kbaseColumn.setContextKey(this.getName() + kbaseColumn.getContextKey());
			this.columns.add(kbaseColumn);
		}

		{
			KbeePredicateGridColumn<ContentTemplate> kbaseColumn = new KbeePredicateGridColumn<>("templates", getLabel("templates"), obj -> getBooleanYesNoText(obj.isTemplatesCabinet(),false));
			kbaseColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isTemplatesCabinet(),true));
			kbaseColumn.setRowCssClass("centered");
			kbaseColumn.setHeaderCssClass("centered");
			kbaseColumn.setPreferred(false);
			kbaseColumn.setContextKey(this.getName() + kbaseColumn.getContextKey());
			this.columns.add(kbaseColumn);
		}
		*/
		/**
		{
			KbeePredicateGridColumn<ContentTemplate> kbaseColumn = new KbeePredicateGridColumn<>("external", getLabel("external"),
			obj -> getBooleanYesNoText(obj.isExternalCabinet(),false));
			kbaseColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isTemplatesCabinet(),true));
			kbaseColumn.setRowCssClass("centered");
			kbaseColumn.setHeaderCssClass("centered");
			kbaseColumn.setContextKey(this.getName() + kbaseColumn.getContextKey());
			this.columns.add(kbaseColumn);
		}
		*/
		// --------------------------
		// Templates
		// KBase
		//
		
		
		{
			KbeePredicateGridColumn<ContentTemplate> idColumn = new KbeePredicateGridColumn<>("id", getLabel("id"),	obj ->  String.valueOf(obj.getId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			this.columns.add(idColumn);
		}
		return this.columns;
	}

	
	private String getLaunchers(ContentTemplate obj) {
		StringBuilder str = new StringBuilder();
		for( ProcessLauncher p: obj.getProcessLaunchers()) {
			try {
				if (str.length()>0)
					str.append(" | ");
				if (p.getProcedure()!=null) 
					str.append("<a class=\"btn-link\" target=\"_blank\"   href= /model/procedure/" + p.getProcedure().getId().toString() +  "/" + p.getId().toString() + "><span>" + p.getDisplayName() + "</span></a>");	
				else
					str.append("<a class=\"btn-link\" target=\"_blank\"   href= /model/contentclass/" + p.getContentTemplate().getId().toString() + "><span>" + p.getDisplayName() + "</span></a>");
			} catch (Exception e) {
				str.append(e.getClass().getName());
				logger.error(e);	
			}
		}
		return str.toString();
	}



	/**
	 * 
	 * user-space-yes-html= 
	 * user-space-no-html=
	 * user-space-yes= 
	 * user-space-no=
	 * 
	 * yes-html=
	 * no-html=
	 * yes=
	 * no=
	 * 
	 * 
	 * @param value
	 * @param html
	 * @param property_yes
	 * @param property_no
	 * @return
	 */
	
	private String getBooleanYesNoText(boolean value, boolean html) {
		if(html)
			return new StringResourceModel(value ?  "yes-html" : "no-html", this, null).getObject();
		else
			return new StringResourceModel(value ?  "yes" : "no", this, null).getObject();
	}

	/**
	 * user-space
	 * yes
	 * 
	 * @param value
	 * @param html
	 * @param property
	 * @return
	 */
	private String getBooleanYesNoText(boolean value, boolean html, String property) {
		if(html)
			return new StringResourceModel(value ?  property+"-yes" : property+"-no-html", this, null).getObject();
		else
			return new StringResourceModel(value ?  property+"-yes" : property+"-no", this, null).getObject();
		
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	

	
	protected Panel getPanel(IModel<ContentTemplate> model) {
		return new ExpandedPanel<ContentTemplate>("editor", this, model);
	}
	
	protected Panel getPanel(IModel<ContentTemplate> model, List<String> snippets) {
		return new ExpandedPanel<ContentTemplate>("editor", this, model, snippets);
	}
	
	
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("header"));
			}
		});


		
		add(new WicketEventListener<ClickEvent<ContentTemplate>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<ContentTemplate> event) {
				getPage().setResponsePage(getContentTemplatePage(event.getModel(), 0, false, false));
			}
		});
	}
	
	
	
	
	@Override																						
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<ContentTemplate> browser) {
		
		if (items!=null)
			return items;
		
		items = new ArrayList<ToolbarItem>();

		items.add(new ToolbarAlert(browser, Align.TOP_LEFT) {
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
		
		
		
		items.add(new NewButton(browser, Align.TOP_LEFT) {
			protected String getButtonCss() {
				return "btn btn-primary btn-md";
			}
			
			@Override
			public boolean isEnabled() {
				if (isFreeVersion() && !isRoot())
					return false;
				return true;
			}
			
			@Override
			public boolean isVisible() {
				if (isFreeVersion() && !isRoot()) 
					return false;
				return true;
			}

			@Override
			public void onClick() {
				try {
					Object template = ServiceLocator.getService(ObjectFactoryService.class).createTemplate();
					Page page = getContentTemplatePage(ContentTemplatesConsole.this.getModel((ContentTemplate)template), 0, true, true);
					setResponsePage(page);
				}
				catch (Exception e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			};
		});
		
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return ContentTemplatesConsole.this.getName();}, new Model<String>(ContentTemplatesConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		this.items.add(infoButton);
		
		return items;
	}


	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}

	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {		
			if (((ContentTemplate) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)	return "deleted-state";
			return null;
				
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

    protected void setDeletedVisible(boolean b) {
        this.is_deleted_visible = b;
        setUserPreference("deleted-visible", (b ? "yes" : "no"));
    }

    protected boolean isDeletedVisible() {
        return this.is_deleted_visible;
    }

    
	/**
	 * 
	 * @param model
	 * @param index
	 * @param editon
	 * @return
	 */
	protected Page getContentTemplatePage(IModel<ContentTemplate> model, int index, final boolean editon, final boolean isNew) {
		Page page = new ContentTemplatePage(model, editon, isNew);
		return page;
	}
	
	protected String getNumberClass(ContentTemplate obj) {
		try {
		long ref=  getContentDao().getTotalContents(obj);
		return ref>0?"col number-mdx info" : "col number-mdx";
		} catch (Exception e) {
			logger.error(e);
			return "number-mdx";
		}
		
	}

}

