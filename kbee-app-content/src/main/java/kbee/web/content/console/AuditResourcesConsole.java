package kbee.web.content.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.*;
import com.novamens.kbee.wicket.markup.html.console.grid.GlyphiconColumnPanel;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.UrlService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchResourcesSelectorPanel;
import com.novamens.dom.Proxy;
import com.novamens.event.LogEvent;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrDateRangeFilter;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.util.NumberFormatter;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.AuditConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.TargetBlankTitleColumnPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.query.AuditResourcesQuery;
import kbee.web.resource.WebResourceReference;

import org.danekja.java.util.function.serializable.SerializableSupplier;


@SuppressWarnings("serial")
public abstract class AuditResourcesConsole extends AbstractFacetedConsole<KBFile> implements AuditConsole {
	private static final long serialVersionUID = 1L;
																					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditResourcesConsole.class.getName());

	private List<GridColumn<SearchResult,String>> columns;

	
	
	public AuditResourcesConsole(Query query) {
		super("xplorer", query);
		from = OffsetDateTime.now().minusDays(1);
		to   = OffsetDateTime.now();
		setOutputMarkupId(true);
	}

	@Override
	 protected  IModel<KBFile> getModel( KBFile object) {
			return new ObjectModel<KBFile>(object, true);
	}

	@Override
	protected String getIcon(IModel<KBFile> model) {
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
		return getDomain().getService(FileIndexerService.class).getIndex();
	}
	
	private OffsetDateTime from;
	private OffsetDateTime to;

	
	@Override
	public Query newQuery() {

		
		ZonedDateTime zoned_from = ZonedDateTime.ofInstant(from.toInstant(), ZoneId.of(getDomain().getTimeZone())).truncatedTo(ChronoUnit.DAYS);
		ZonedDateTime zoned_to  = ZonedDateTime.ofInstant(to.plusDays(1).toInstant(), ZoneId.of(getDomain().getTimeZone())).truncatedTo(ChronoUnit.DAYS);
		
		OffsetDateTime d_from  	 = zoned_from.withZoneSameInstant(ZoneId.of(getDomain().getTimeZone())).toOffsetDateTime();
		OffsetDateTime d_to		 = zoned_to.withZoneSameInstant(ZoneId.of(getDomain().getTimeZone())).toOffsetDateTime();

		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put("modified", new SolrDateRangeFilter("modified",	d_from, d_to));
		
		return new AuditResourcesQuery(getQueryIndex(), getDomain(),  filters, isDomainKbee());
	}
	
	
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
	public void onInitialize() {
		super.onInitialize();
	}
	
	
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
						return AuditResourcesConsole.this.getLabel("contextmenu.open").getObject();
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
				};
			}
		});
		
		
		
		menu.addItem(new MenuItemFactory<KBFile>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<KBFile> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<KBFile>(id) {
						private static final long serialVersionUID = 1L;

						@Override 
						public String getLabel() {
							return AuditResourcesConsole.this.getLabel("contextmenu.download").getObject();
						}
						
						@Override
						protected File getFile() {
							if (getModel().getObject() instanceof KBFile) {
								File file;
								try {
									file = ((KBFile) getModel().getObject()).getFile();

									if (!file.exists()) {
										logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + " file "  + ((KBFile) getModel().getObject()).getUrl() +  "  does not exists");
										return null;
									}
									
									return file;
								} catch (IOException e) {
									logger.error(e);
								}
							}
							return null;
						}
						
						@Override
						public boolean isVisible()  {
							try {
								return (getModel().getObject() instanceof KBFile);
							} catch (Exception e) {
								return false;
							}
						}
						
						@Override
						public boolean isEnabled()  {
							try {
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
		
		return menu;
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

		
		columns.add(new GridColumn<SearchResult, String>("glyphicon", getLabel("iconcolumn")) {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {
					Object object = resultmodel.getObject().getObject();
					IModel<KBFile> objectmodel = getModel((KBFile)object);
					cellItem.add(new GlyphiconColumnPanel<KBFile>(componentId, objectmodel) {
						@Override
						protected String getGlyphiconClass() {
							try {
								return getModel().getObject().getGlyphIcon();
							} 
							catch (Exception e) {
								logger.error(e);
								return "";
							}
						 }
						@Override
						protected String getCss() {
							return "iconcolumn";
						}
					});
				} 
				catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					cellItem.add(new Label(componentId, e.getClass().getName() + " " + e.getMessage())); 
				}
			}
			public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> model) {
				Object object = model.getObject().getObject();
				IModel<KBFile> objectmodel = getModel((KBFile)object);
				cellItem.add(new GlyphiconColumnPanel<KBFile>(componentId, objectmodel) {
					@Override
					protected String getGlyphiconClass() {
						try {
							return getModel().getObject().getGlyphIcon();
						} 
						catch (Exception e) {
							logger.error(e);
							return "";
						}
					 }
					@Override
					protected String getCss() {
						return "iconcolumn";
					}
				});
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
				return true;
			}
			public boolean isFixed() {
				return true;
			}
			public boolean isHeaderMenu() {
				return false;
			}
			@Override
			protected String getContextKey() {
				return AuditResourcesConsole.this.getName() + super.getContextKey();
			}
	    });

		
		{
			LinkPredicateKbeeGridColumn<KBFile> titleColumn = new LinkPredicateKbeeGridColumn<>("title", getLabel("titlecolumn"), "title_sort", 	obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setTarget("_blank");
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}




		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("name", getLabel("namecolumn"),		obj -> obj.getName());
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			nameColumn.setDefaultWidth(380);
			nameColumn.setPreferred(false);
			this.columns.add(nameColumn);
		}

		
		{
		
			if (isDomainKbee()) {
				KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("domain", getLabel("domaincolumn"),	obj -> obj.getDomain().getName());
				nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
				this.columns.add(nameColumn);
				nameColumn.setPreferred(true);
			}
		}


		
		{
			KbeePredicateGridColumn<KBFile> sizeColumn = new KbeePredicateGridColumn<>("size", getLabel("sizecolumn"),	obj ->   getSizeColumnText(obj,false) );
			sizeColumn.setHtmlValueResolver(obj ->   getSizeColumnText(obj,true));
			sizeColumn.setDefaultWidth(80);
			sizeColumn.setContextKey(this.getName() + sizeColumn.getContextKey());
			this.columns.add(sizeColumn);
		}

		columns.add(new LastModifiedColumn<KBFile>("lastmodified", getLabel("datecolumn"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getContextKey() {
				return AuditResourcesConsole.this.getName() + super.getContextKey();
			}
			public boolean isResizable() {
				return true;
			}
		});

		{
			KbeePredicateGridColumn<KBFile> col = new KbeePredicateGridColumn<>("isencrypted", new Model<String>("Encrypted"),		obj ->   new StringResourceModel((obj.getIsEncrypted()?"yes":"no"), AuditResourcesConsole.this, null).getObject());
			col.setContextKey(this.getName() + col.getContextKey());
			col.setPreferred(false);
			this.columns.add(col);
		}

		{
			KbeePredicateGridColumn<KBFile> kbfstypeColumn = new KbeePredicateGridColumn<>("kbfstype", getLabel("kbfstypecolumn"),	obj -> obj.getStorageType().getLabel(getSessionUser().getLocale()) );
			kbfstypeColumn.setContextKey(this.getName() + kbfstypeColumn.getContextKey());
			this.columns.add(kbfstypeColumn);
		}


		/**
		{
			KbeePredicateGridColumn<KBFile> pathColumn = new KbeePredicateGridColumn<>("path", getLabel("pathcolumn"),				obj ->  obj.getUrl() );
			pathColumn.setContextKey(this.getName() + pathColumn.getContextKey());
			pathColumn.setPreferred(false);
			this.columns.add(pathColumn);
		}
**/

		{
			KbeePredicateGridColumn<KBFile> bucketColumn = new KbeePredicateGridColumn<>("bucket", getLabel("bucketnamecolumn"), obj ->  obj.getBucketName() );
			bucketColumn.setContextKey(this.getName() + bucketColumn.getContextKey());
			this.columns.add(bucketColumn);
		}


		


		{
			KbeePredicateGridColumn<KBFile> objectNameColumn = new KbeePredicateGridColumn<>("objectname", getLabel("objectnamecolumn"), obj -> obj.getObjectName() );
			objectNameColumn.setContextKey(this.getName() + objectNameColumn.getContextKey());
			objectNameColumn.setDefaultWidth(580);
			this.columns.add(objectNameColumn);
		}


		{
			KbeePredicateGridColumn<KBFile> col = new KbeePredicateGridColumn<>("shard", new Model<String>("Shard"),		obj ->  String.valueOf(obj.getShard()));
			col.setContextKey(this.getName() + col.getContextKey());
			col.setPreferred(true);
			this.columns.add(col);
		}
		

		
		
		{
			KbeePredicateGridColumn<KBFile> col = new KbeePredicateGridColumn<>("fsid", new Model<String>("FSID"),		obj ->   obj.getFSID());
			col.setContextKey(this.getName() + col.getContextKey());
			col.setPreferred(true);
			this.columns.add(col);
		}
		
		

		

		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("url", getLabel("urlcolumn"),	obj -> obj.getUrl());
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			nameColumn.setDefaultWidth(600);
			this.columns.add(nameColumn);
		}

		
		{
			KbeePredicateGridColumn<KBFile> userColumn = new KbeePredicateGridColumn<>("user", getLabel("usercolumn"),		obj ->  obj.getLastModifiedUser().getFirstLastName() );
			userColumn.setContextKey(this.getName() + userColumn.getContextKey());
			this.columns.add(userColumn);
		}

	 
		
		
		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("id", getLabel("idcolumn"),	obj -> String.valueOf(obj.getId()));
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			this.columns.add(nameColumn);
		}
		
		
		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("oid", new Model<String>("OId"),
					obj -> String.valueOf(obj.getOId()));
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			this.columns.add(nameColumn);
		}

		
		{
			KbeePredicateGridColumn<KBFile> classColumn = new KbeePredicateGridColumn<>("class", new Model<String>("Class"), obj -> Proxy.getClassName(obj) );
			classColumn.setContextKey(this.getName() + classColumn.getContextKey());
			classColumn.setPreferred(false);
			this.columns.add(classColumn);
		}
		
		/**
		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("oid", getLabel("oidcolumn"),
					obj -> String.valueOf(obj.getI.getOId()));
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			this.columns.add(nameColumn);
		}

		
		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("crc32", getLabel("crc32column"),					obj -> obj.getCRC32());
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			nameColumn.setPreferred(false);
			this.columns.add(nameColumn);
		}
				**/
		

		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("sha256", getLabel("sha256column"),	obj -> obj.getSHA256());
			nameColumn.setPreferred(false);
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			this.columns.add(nameColumn);
		}
		

		{
			KbeePredicateGridColumn<KBFile> nameColumn = new KbeePredicateGridColumn<>("stored", new Model<String>("Object Storage"),obj -> isStored(obj));
			nameColumn.setPreferred(false);
			nameColumn.setContextKey(this.getName() + nameColumn.getContextKey());
			this.columns.add(nameColumn);
		}

		
		
		
		columns.add(new GridColumn<SearchResult, String>("content", getLabel("contentcolumn")) {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> model) {
				Content content = null;
				try {
					Object object = model.getObject().getObject();
					content = getContentDao().findContentByResource((KBFile)object);
				} 
				catch (Exception e) {
				}
				if (content!=null) {
					IModel<Content> contentmodel = new ObjectModel<Content>(content);
					cellItem.add(new TargetBlankTitleColumnPanel<Content>(componentId, contentmodel) {
						@Override
						protected String getCss() {
							return "cell-label btn-link";
						}
					});
				}
				else {
					cellItem.add(new InvisiblePanel(componentId));
				}
			}
			public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> model) {
				Content content = null;
				try {
					Object object = model.getObject().getObject();
					content = getContentDao().findContentByResource((KBFile)object);
				} 
				catch (Exception e) {
				}
				if (content!=null) {
					IModel<Content> contentmodel = new ObjectModel<Content>(content);
					cellItem.add(new TargetBlankTitleColumnPanel<Content>(componentId, contentmodel) {
						@Override
						protected String getCss() {
							return "cell-label btn-link";
						}
					});
				}
				else {
					cellItem.add(new InvisiblePanel(componentId));
				}
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
//			@Override
//			public int getDefaultWidth() {
//				return 58;
//			}
			public boolean isResizable() {
				return true;
			}
//			public boolean isFixed() {
//				return true;
//			}
			public boolean isHeaderMenu() {
				return false;
			}
			@Override
			protected String getContextKey() {
				return AuditResourcesConsole.this.getName() + super.getContextKey();
			}
	    });


		{
			SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
			DateKbeeColumn<KBFile> executedColumn = new DateKbeeColumn<>("created", new Model<String>("Created"), "created", (obj) -> obj.getCreationOffsetDateTime(), formatSupplier);
			columns.add(executedColumn);
			executedColumn.setPreferred(false);
		}

		
		{
			KbeePredicateGridColumn<KBFile> externallyStoredColumn = new KbeePredicateGridColumn<>("externallystored", new Model<String>("Gateway"),
					obj -> getExternallyStoredDisplayModel(obj, false).getObject() );
			externallyStoredColumn.setHtmlValueResolver(obj -> getExternallyStoredDisplayModel(obj, true).getObject());
			externallyStoredColumn.setContextKey(this.getName() + externallyStoredColumn.getContextKey());
			this.columns.add(externallyStoredColumn);
			externallyStoredColumn.setPreferred(false);
		}
		

		
		
		
		return columns;
		
	}

	

	private String isStored(KBFile obj) {
		InputStream  is = null;
		
		try {
			is = obj.getService(KBFSResourceService.class).getObject();
			return "ok";
			
		} catch (Exception e) {
			return e.getClass().getName()+" | " + e.getMessage();
		}
		finally {
		
			if (is!=null)
				try {
					is.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
		
	}

	private IModel<String> getExternallyStoredDisplayModel(KBFile file, boolean html) {
		String str= file.isGateway()?"yes":"no";
		if(html)
			return new StringResourceModel(str, AuditResourcesConsole.this, null);
		else
			return new Model<String>(str);
	}


	private String getSizeColumnText(KBFile object1, boolean html) {
		String size = null;

		String css = html ? "ago":null;
		if (  object1.getSize()>0)
					size= NumberFormatter.formatFileSize(object1.getSize(), getSessionUser().getLocale(), css);
			else if (object1.getSize()<0)
				size="n/a";
			else
				size="0 bytes";
		return size;
	}

	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<KBFile> browser) {
		return new ArrayList<>();
	}

	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			@Override
			public void onEvent(SidePanelEvent event) {
			}
		});

		add(new WicketEventListener<ClickEvent<KBFile>>() {
			@Override
			public void onEvent(ClickEvent<KBFile> event) {
				KBFile file = event.getModelObject();
				AuditResourcesConsole.this.open(file);
			}
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof ClickEvent && ((ClickEvent<?>)event).getModelObject() instanceof KBFile;
			}
		});
		
		add(new WicketEventListener<ClickEvent<Content>>() {
			@Override
			public void onEvent(ClickEvent<Content> event) {
				AuditResourcesConsole.this.open((Content)event.getModelObject());
			}
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof ClickEvent && ((ClickEvent<?>)event).getModelObject() instanceof Content;
			}
		});
	}
	
	protected Panel getPanel(IModel<KBFile> model) {
		return new ExpandedPanel<KBFile>("editor", this, model);
	}
	

	protected Panel getPanel(IModel<KBFile> model, List<String> list) {
		return new ExpandedPanel<KBFile>("editor", this, model, list);
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

	private void open(KBFile file) {
		
		//
		// if file is non existent
		// error page maybe cleaner
		//
		if (file.isImage()) {
			String resourcehref;
			ResourceReference resourceReference = new WebResourceReference(file);
			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			WebPage page = new RedirectPage(resourcehref);
			setResponsePage(page);
			
		}
		else if (file.isVideo()) {
			String resourcehref;
			ResourceReference resourceReference = new WebResourceReference(file);
			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			setResponsePage(new RedirectPage(resourcehref));
		}
		else if (file.isAudio()) {
			String resourcehref;
			ResourceReference resourceReference = new WebResourceReference(file);
			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			setResponsePage(new RedirectPage(resourcehref));
		}
		else {
			String resourcehref;
			ResourceReference resourceReference = new WebResourceReference(file);
			resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
			setResponsePage(new RedirectPage(resourcehref));
		}
	}
	
	private void open(Content content) {
		Page page = new RedirectPage(content.getService(UrlService.class).getUrl(false));
		if (page!=null)
			setResponsePage(page);		
	}
	
	@Override
	protected boolean isMyListsEnabled() {
		return false;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

}

