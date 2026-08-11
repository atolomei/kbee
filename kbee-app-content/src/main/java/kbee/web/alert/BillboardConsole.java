package kbee.web.alert;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.danekja.java.util.function.serializable.SerializableSupplier;


import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.notes.Billboard;
import com.novamens.content.security.Role;
import com.novamens.content.service.domain.DomainSettingsService;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.notes.KbeeBillboard;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.DateKbeeColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;

import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.notes.NewAlertButton;
import kbee.web.notes.WorkNoteColumnPanel;
import kbee.web.notes.WorkNotesGridUpdate;
import kbee.web.notes.BillboardPage;
import kbee.web.notes.BillboardsQuery;
import kbee.web.report.ReportConsole;
import kbee.web.report.ReportFactory;
import kbee.web.security.role.RolesConsole;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.service.ReportsLibraryService;

/**
 * <p>Billboards and regular alerts</p>
 */
public abstract class BillboardConsole extends AbstractFacetedConsole<Billboard> {
					
	private static final long serialVersionUID = 1L;

	private static final int TWO_DAY_HOURS  = 48;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BillboardConsole.class.getName());

	
	private final boolean is_root		  	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	private final boolean is_admin	 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	private final boolean is_support	  	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	private final boolean is_domain_admin 	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
												
	
	private List<GridColumn<SearchResult,String>> columns;
	private List<ToolbarItem> items;
	private List<ToolbarItem> selection_toolbar;
	
	
	public BillboardConsole(Query query) {
		super("billboards", query);
	}

	
	@Override
	protected String getIcon(IModel<Billboard> model) {
		return null;
	}
	
	@Override
	 public void onInitialize() {
		 super.onInitialize();
			Modal modal = new Modal("note-editor-dialog");
			modal.setTitle("note-editor-title");
			modal.setOutputMarkupId(true);
			modal.setModalType(Modal.MODAL_CENTER);
			add(modal);
	 }
	
	
	@Override
	public void onDetach() {
		super.onDetach();
	
		this.columns=null;
		
		if (this.items!=null) {
			for (ToolbarItem item: items) 
				item.detach();
		}
		
		if (this.selection_toolbar!=null) {
			for (ToolbarItem item: selection_toolbar) {
				item.detach();
			}
		}
	}

	
	/** 
	 * Selection toolbar
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Billboard> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
			private static final long serialVersionUID = 1L;
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
				 return new StringResourceModel("delete", BillboardConsole.this).getObject();
			 }
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					String result=BillboardConsole.this.delete(getBrowser().getSelection());
					if (result!=null)
						getErrorDialog().open(target, new Model<String>("Error") ,new Model<String>(result));
					BillboardConsole.this.resetSelection();
					BillboardConsole.this.refresh(target);
					
				} catch (Exception e) {
					logger.error(e);
					getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName()) ,new Model<String>(e.getMessage()));
					BillboardConsole.this.refresh(target);
				}
			}
		});

		return this.selection_toolbar;
	}
	

	
	protected String delete(List<?> selection) {
	
		StringBuilder str = new StringBuilder();
		
		@SuppressWarnings("unchecked")
		List<IModel<Billboard>> list = (List<IModel<Billboard>>)  selection;

		for (IModel<Billboard> c:list) {
			try {
				logger.debug(" deleteing " + c.getObject().getTitle());
				getDomain().getService(DomainSettingsService.class).remove(c.getObject());

			} catch (Exception e) {
				logger.error(e);
				str.append(c.getObject().getTitle()+" -> " + e.getMessage());
			}
		}
		if (str.length()==0)
			return null;
		return str.toString();


	}

	/** 
	 * Browser toolbar
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Billboard> browser) {
		if (this.items!=null)
			return this.items;
		this.items = new ArrayList<ToolbarItem>();
		
		items.add( new NewAlertButton(browser, Align.TOP_LEFT) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled() {
				if (is_root || is_admin) 
					return true;
				return false;
			}
			@Override
			public boolean isVisible() {
				if (is_root || is_admin) 
					return true;
				return false;
			}
			
			@Override
			protected void onCreate(String type) {
				try {
					Billboard note = getDomain().getService(DomainSettingsService.class).createBillboard(new StringResourceModel("new-alert", BillboardConsole.this, null).getString(), null,	type.equals("alert")?true:false);
					PageParameters pa= new PageParameters();
				    pa.add("id", note.getId().toString());
				    pa.add("isnew", "yes");
				    try { 
				    	setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-billboard-page", pa)); 
				    } catch (Exception e) {
				    	logger.error(e);
				    	setResponsePage( new ApplicationErrorPage<Billboard>(e));
				    }

				}
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>( new Model<String>(e.getMessage()), new Model<String>(e.getClass().getSimpleName())));
				}	
			}
		});

		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return "About";}, new Model<String>(BillboardConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		items.add(infoButton);
		
		return items;
	}


	protected String getDescription() {
		return new StringResourceModel("console-description", this, null).getObject();
	}


	@Override
	protected Panel getPanel(IModel<Billboard> model) {
		return new InvisiblePanel("editor");
	}
	
	@Override
	protected Panel getPanel(IModel<Billboard> model, List<String> list) {
		return getPanel(model);
	}
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

																					
		
		columns.add(new GridColumn<SearchResult, String>("title", getLabel("name"), "title_sort") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Billboard> objectmodel = getModel((Billboard) object);
				cellItem.add(new WorkNoteColumnPanel(componentId, objectmodel, getDateFormat(), isExpanded()) {
					private static final long serialVersionUID = 1L;
					@Override
					protected String getCss() {
						return "cell-label btn-link";
					}
				});
			}
			
			@Override
			protected String getContextKey() {
				return BillboardConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public String getCssClass() {
				return "col title col-xs-12 col-md-12 col-lg-12";
			}
			@Override
			public int getDefaultWidth() {
					return 680;
			}
			
			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				try {
					Billboard note = (Billboard) result.getObject();
					return new Model<String>(note.getTitle());
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
		});
		
		
		

		SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
		{
			DateKbeeColumn<Billboard> executedColumn = new DateKbeeColumn<>("modified", getLabel("modified"), "modified", (obj) -> obj.getModifiedOffsetDateTime(), formatSupplier);
			executedColumn.setPreferred(true);
			this.columns.add(executedColumn);
		}

		{																																																	
			KbeePredicateGridColumn<Billboard> descriptionColumn = new KbeePredicateGridColumn<Billboard>("icon", getLabel("icon"), obj -> ( "<i  title=\""+obj.getGlyphicon()+"\"  class=\" info "+KbeeBillboard.getFontAwesomeIcon(obj.getGlyphicon())+"\"></i>" ));
			descriptionColumn.setPreferred(true);
			descriptionColumn.setDefaultWidth(94);
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			this.columns.add(descriptionColumn);
		}

		
		{																																																	
			KbeePredicateGridColumn<Billboard> descriptionColumn = new KbeePredicateGridColumn<Billboard>("totalread", getLabel("total-read"), obj -> BillboardConsole.this.format(getContentDao().getTotalUsersRead(obj)));
			descriptionColumn.setPreferred(true);
			descriptionColumn.setHeaderCssClass("header  col col-xs-1 col-md-1 col-lg-1  centered no-sorting ui-resizable");
			descriptionColumn.setCssValueResolver(obj -> getContentDao().getTotalUsersRead(obj) > 0 ?  "col col-xs-1 col-md-1 col-lg-1 number-md info" : "col col-xs-1 col-md-1 col-lg-1 number-md");
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			this.columns.add(descriptionColumn);
		}
		
		
		
		{
			KbeePredicateGridColumn<Billboard> descriptionColumn = new KbeePredicateGridColumn<Billboard>("alert-type", getLabel("alert-type"), obj -> (obj.isAlert()? getLabel("alert").getObject() : getLabel("billboard").getObject()) );
			descriptionColumn.setHtmlValueResolver( obj -> obj.isAlert() ? getLabel("alert").getObject() : getLabel("billboard").getObject());
			descriptionColumn.setPreferred(true);
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			this.columns.add(descriptionColumn);
		}
	
	
		{
			KbeePredicateGridColumn<Billboard> descriptionColumn = new KbeePredicateGridColumn<Billboard>("receivers", getLabel("receivers"), obj -> getReceiversPlainString(obj));
			descriptionColumn.setPreferred(true);
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			descriptionColumn.setHtmlValueResolver(obj -> getReceiversHTML(obj));
			this.columns.add(descriptionColumn);
		}
		{
			KbeePredicateGridColumn<Billboard> descriptionColumn = new KbeePredicateGridColumn<Billboard>("email", getLabel("email"), obj -> (obj.isEmail()? getLabel("yes").getObject() : getLabel("no").getObject()) );
			descriptionColumn.setHtmlValueResolver( obj -> obj.isEmail() ? getLabel("html-yes").getObject() : getLabel("html-no").getObject());
			descriptionColumn.setPreferred(true);
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			this.columns.add(descriptionColumn);
		}
		{
			DateKbeeColumn<Billboard> executedColumn = new DateKbeeColumn<>("startpub", getLabel("startpub"), (obj) -> obj.getStartpub(), formatSupplier);
			executedColumn.setPreferred(false);
			columns.add(executedColumn);
		}
		{
			DateKbeeColumn<Billboard> executedColumn = new DateKbeeColumn<>("endpub", getLabel("endpub"), (obj) -> obj.getEndpub(), formatSupplier);
			executedColumn.setPreferred(false);
			columns.add(executedColumn);
		}
		
		
		{																						
			KbeePredicateGridColumn<Billboard> descriptionColumn = new KbeePredicateGridColumn<Billboard>("cronexpression", getLabel("cronexpression"), (obj -> obj.getCronExpression()!=null? obj.getCronExpression().toHTMLString() : "once" ));
			descriptionColumn.setHtmlValueResolver((obj -> getFrequency(obj)));
			descriptionColumn.setTextValueResolver((obj -> obj.getCronExpression()!=null? (obj.getCronExpression().toString() +" " + obj.getTimeZone()): "once" ));
			descriptionColumn.setPreferred(true);
			descriptionColumn.setDefaultWidth(480);
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			this.columns.add(descriptionColumn);
		}

		
		
		{
			DateKbeeColumn<Billboard> executedColumn = new DateKbeeColumn<>("created", getLabel("created"), "created", (obj) -> obj.getCreationOffsetDateTime(), formatSupplier);
			executedColumn.setPreferred(true);
			columns.add(executedColumn);
		}
		
		return this.columns;
	}

	/**
	 * @param obj
	 * @return
	 */
	protected String getFrequency(Billboard obj) {
		
		if (obj.getCronExpression()!=null) {
			StringBuilder str=new StringBuilder();
			ZonedDateTime zd = obj.getCronExpression().nextTimeAfter(ZonedDateTime.now( ZoneId.of( obj.getTimeZone() )));
			ZoneId userZoneId = ZoneId.of(getSessionUser().getTimeZone());
	        ZonedDateTime userDateTime = zd.withZoneSameInstant(userZoneId);
			str.append("Next -> "+  ServiceLocator.getService(DateTimeService.class).format(userDateTime));
			str.append("<br /> "+ obj.getCronExpression().toHTMLString() + " " + obj.getTimeZone());
			return str.toString();
			
		} else {
			return "";
		}
	}
	

	/**
	 * 
	 * @param obj
	 * @return
	 */
	private String getReceiversPlainString(Billboard obj) {
		StringBuilder str = new StringBuilder(); 
		for (Principal p: obj.getReceivers()) {
			if (str.length()>0)
				str.append(", ");
				str.append(p.getDisplayName() + "(" + (  (p instanceof User)? getLabel("user").getObject() : getLabel("group").getObject()) +  ") ");
		}
		return str.toString();
	}
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	private String getReceiversHTML(Billboard obj) {
		
		StringBuilder str = new StringBuilder(); 

		for (Principal p: obj.getReceivers()) {
			
			if (str.length()>0)
				str.append(", ");
			
			String url;
			
			if (p instanceof User) 
				url = getServerUrl()+"/security/users/"+p.getId().toString();
			else if  (p instanceof Group)
				url = getServerUrl()+"/security/groups/"+p.getId().toString();

			else if  (p instanceof Role)
				url = getServerUrl()+"/security/roles/"+p.getId().toString();

			else
				url="";
			
			str.append("<a class=\"btn-link\" href=\"" + url + "\"><span>"+ p.getDisplayName() + "</span><span class=\"ago\"> ( " + (  (p instanceof User)? getLabel("user").getObject():getLabel("group").getObject()) +  " ) </span></a>");
		}
		return str.toString();
	}

	
	@Override
	protected boolean hasExpander() {
		return true;
	}

	/**
	 * 
	 */
	@SuppressWarnings("serial")
	@Override
	protected Panel getMenu(IModel<Billboard> model) {
		
		ContextMenuPanel<Billboard> menu = new ContextMenuPanel<Billboard>(model);
		
		menu.setOutputMarkupId(true);

		
		menu.addItem(new MenuItemFactory<Billboard>() {
			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
				return new AjaxMenuItemPanelV5<Billboard>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							PageParameters pa= new PageParameters();
						    pa.add("id", getModel().getObject().getId().toString());
						    pa.add("readonly", "yes");
							setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-billboard-page", pa));
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<>(null, e));
						}
					
					}
					
					@Override 
					public String getLabel() {
						return BillboardConsole.this.getLabel("view").getObject();
					}
					/**
					 * it can be edited for the following 48 hs after being created.
					 */
					@Override
					public boolean isEnabled() {
						try {
						
							if ((!isRoot()) && (OffsetDateTime.now().isAfter(getModel().getObject().getCreationOffsetDateTime().plusHours(TWO_DAY_HOURS))))
								return  false;
							
							return true;
							
						} catch (Exception e) {
							logger.error(e);
							return true;
						}
					}
				};
			}
		});
	

		/*
		menu.addItem(new MenuItemFactory<Billboard>() {
			private static final long serialVersionUID = 1L;
			

			
			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {

				
				
				
				return new MenuItemPanelV5<Billboard>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						try {
							PageParameters pa= new PageParameters();
						    pa.add("id", getModel().getObject().getId().toString());
						    pa.add("readonly", "yes");
							setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-billboard-page", pa));
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent(null, e));
						}
					}
					@Override
					public PopupSettings getPopupSettings() {
						return new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
							PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
							PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
					}
					
					@Override 
					public String getLabel() {
						return BillboardConsole.this.getLabel("view").getObject();
					}
					@Override
					public boolean isEnabled() {
						try {
						
							if ((!isRoot()) && (OffsetDateTime.now().isAfter(getModel().getObject().getCreationOffsetDateTime().plusHours(TWO_DAY_HOURS))))
								return  false;
							
							return true;
							
						} catch (Exception e) {
							logger.error(e);
							return true;
						}
					}
				};
			}
		});
		*/

		
		menu.addItem(new MenuItemFactory<Billboard>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
				return new MenuItemPanelV5<Billboard>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						try {
							PageParameters pa= new PageParameters();
						    pa.add("id", getModel().getObject().getId().toString());
							setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-billboard-page", pa));
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent(null, e));
						}
					}
				
					
					/**@Override
					public PopupSettings getPopupSettings() {
						return new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
							PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
							PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
					}**/
					
					@Override 
					public String getLabel() {
						return BillboardConsole.this.getLabel("contextmenu.edit").getObject();
					}
					/**
					 * it can be edited for the following 48 hs after being created.
					 */
					@Override
					public boolean isEnabled() {
						try {
		 					if ((!isRoot()) && (OffsetDateTime.now().isAfter(getModel().getObject().getCreationOffsetDateTime().plusHours(TWO_DAY_HOURS))))
								return  false;
		 					return true;
		 				} catch (Exception e) {
							logger.error(e);
							return true;
						}
					}
					
				};
			}
		});


		
		
		
		menu.addItem(new MenuItemFactory<Billboard>() {
			private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
						return new AjaxMenuItemPanelV5<Billboard>(id) {
							private static final long serialVersionUID = 1L;
							@SuppressWarnings("unchecked")
							public void onClick(AjaxRequestTarget target) {
								Modal modal = BillboardConsole.this.getAuditTrailModal();
								((ObjectAuditModal<Billboard>)modal).open(target, getModel(), true);
							}
							@Override 
							public String getLabel() {
								return getConsoleLabel("audittrail").getObject();
							}
							@Override
							public boolean isVisible() {
								return  is_root || is_domain_admin;
							}
						};
					}
		});

		menu.addItem(new MenuItemFactory<Billboard>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
				return new MenuItemPanelV5<Billboard>(id) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
					public void onClick() {
						
						PageParameters pageParameters = new PageParameters();
						List<ReportFactory> factories = ServiceLocator.getService(ReportsLibraryService.class).getUserSessionReports();
						
						final String s=ReportConsole.AUDIT.toLowerCase();
						for (ReportFactory factory : factories) {
							String reportGroup = factory.getReport().getReportGroup();
							if (reportGroup!=null && reportGroup.toLowerCase().equals(s)) {
								
								pageParameters.set("reportGroup", reportGroup);
								pageParameters.set("reportKey", "user-alerts-audit");
								setResponsePage(new RedirectPage("/reports/"+reportGroup +"/"+  "user-alerts-audit"));
								return;
							}

						}
						// http://localhost:8080/reports/audit/user-alerts-audit
						try {
							getPage().setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("report-home-page"));
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Object>(new Model<String> ("Not found") , new Model<String>("Audit Report page not found.")));
						}
						
						
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("reports").getObject();
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
					@Override 
					public boolean isEnabled() {
						return  is_root || is_domain_admin || is_support;
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<Billboard>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Billboard>(id) {
					private static final long serialVersionUID = 1L;
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

		
		menu.addItem(new MenuItemFactory<Billboard>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
				return new AjaxMenuItemPanelV5<Billboard>(id) {

					private static final long serialVersionUID = 1L;
					
					public void onClick(AjaxRequestTarget target) {
						getConfirmationDialog().open(target, 
							getConsoleLabel("delete.confirmation.message", getModel().getObject().getTitle()), 
							Dialog.Delete, 
							new Dialog.Handler() {
								private static final long serialVersionUID = 1L;
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											getDomain().getService(DomainSettingsService.class).remove(getModel().getObject());
											fire(new WorkNotesGridUpdate(target));
											
										} catch (ContentMgmtException | ServiceNotFoundException e) {
											logger.error(e);
											fire(new ErrorEvent(target, e));
										}
										catch (Exception e2) {
											logger.error(e2);
											fire(new ErrorEvent(target, e2));
										}
										refresh(target);
									}
								}
						});
					}
					
					@Override
					public boolean isEnabled() {
						return  is_root || is_domain_admin;
					}
					
					@Override 
					public String getLabel() {
						return BillboardConsole.this.getLabel("contextmenu.delete").getObject();
					}
				};
			}
		});

		return menu;
	}

	/**
	 * 
	 */
	@Override
	public Query newQuery() {
		return setUserPreference(new BillboardsQuery(getQueryIndex()));
	}

	protected  IModel<Billboard> getModel(Billboard object) {
		return new ObjectModel<Billboard>(object, true);
	}
	
	/**
	 * 
	 */
	@Override
	public boolean isSelectionEnabled() {
		return true;
	}
	 
	/**
	 * 
	 */
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}
	
	

	/**
	 * 
	 */
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("header"));
			}
		});

	
		add(new WicketEventListener<ClickEvent<Billboard>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Billboard> event) {
				
				PageParameters pa= new PageParameters();
			    pa.add("id", event.getModel().getObject().getId().toString());
			    pa.add("isnew", "no");
				setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-billboard-page", pa));
				
				//setResponsePage( new BillboardPage(event.getModel()) 
				// event.getModel(), event.getIndex(), false, false)
			}
		});
		
	}
	
	
	


}
