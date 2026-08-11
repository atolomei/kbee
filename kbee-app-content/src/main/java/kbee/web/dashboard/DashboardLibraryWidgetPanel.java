package kbee.web.dashboard;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.library.Library;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.service.ContentService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.idoc.markup.ContentPageV6;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Proxy;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListItemsPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.ITabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.content.console.ContentBaseConsole;
import kbee.web.content.console.ContentBasePage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.idoc.IDocHitExpandedPanelV6;
import kbee.web.nav.NavigablePage;
import kbee.web.query.LibraryQuery;

@SuppressWarnings("serial")
public class DashboardLibraryWidgetPanel extends DashboardContentWidgetPanel implements PortalViewRender  {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardLibraryWidgetPanel.class.getName());

	static final int LIMIT = 20;
	
	private int size;
	private IModel<Library> librarymodel;
	private List<IModel<Library>> libraries;
	private List<IModel<UserList>> m_lists = null;
	
	private String my_q;
	private String qTerm = "all";

	
	/**
	 * 
	 */
	public DashboardLibraryWidgetPanel(String id) {
		this(id, "library");
	}
	
	public DashboardLibraryWidgetPanel(String id,  String preferences_key) {
		super(id, preferences_key);
		setTitle( new StringResourceModel("library",DashboardLibraryWidgetPanel.this, null));
		my_q =new StringResourceModel("my-queries", this, null).getObject();
	}
	
	public Library getLibrary() {
		return getLibraryModel()==null ? null : getLibraryModel().getObject();
	}
	
	public IModel<Library> getLibraryModel() {
		return librarymodel;
	}
	
	public void setLibrary(IModel<Library> model) {
		this.librarymodel = model;
		setUserPreference("library", model.getObject().getKey());
	}
	
	public void setLibrary(Library library) {
		if (library!=null)
			setLibrary(new ObjectModel<Library>(library));
	}

	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<Content>>() {
			@Override
			public void onEvent(ClickEvent<Content> event) {
				if ((event.getContext()!=null) && event.getContext().equals(getLibrary().getKey()))
					DashboardLibraryWidgetPanel.this.onClick(event.getModel(), event.getIndex());
			}
		});
		
		add(new WicketEventListener<ApplySavedQueryEvent>() {
			@Override
			public void onEvent(ApplySavedQueryEvent sevent) {
				if ((getLibrary()!=null) && (sevent.getQuery().getConsole().equals(getLibrary().getKey()))) {
					LibraryQuery q=new LibraryQuery(getQueryIndex(), getLibrary());
					q.setParameters(sevent.getQuery().getParameters());
					setResponsePage( new ContentBasePage(getLibraryModel(), q));
				}
			}
		});
		add(new WicketEventListener<GeneralWicketAjaxEvent>() {
			@Override
			public void onEvent( GeneralWicketAjaxEvent event) {
				DashboardLibraryWidgetPanel.this.replacePanel(event);
			}
		});
	}

	@Override
	public void onInitialize() {
		
		setViewModeCriteria(getUserPreference("view-list", "comfortable"));
		setSortCriteria(getUserPreference("sort", "title"));
		
		if (getLibraries().size()>0) {
			String p=getUserPreference("library");
			if (p==null) {
				p=getLibraries().get(0).getObject().getKey();
				setLibrary(getLibraries().get(0));
			}	
			else {
				boolean found=false;
				for (IModel<Library> l:getLibraries()) {
					if (l.getObject().getKey()!=null && l.getObject().getKey().equals(p)) {
						setLibrary(l);
						found=true;
						break;
					}
					if (!found)
						setLibrary(getLibraries().get(0));
				}
			}
		}

		try {
			LibraryWidgetHeaderPanel he=new LibraryWidgetHeaderPanel("header", getTitle(), getLibraryModel(), getLibraries()) {
				protected void refresh(AjaxRequestTarget target) {
					DashboardLibraryWidgetPanel.this.refresh(target);
				}
				protected void onEdit(AjaxRequestTarget target) {
					DashboardLibraryWidgetPanel.this.onEdit(target);
				}
				
				protected void onTitleClick() {
					DashboardLibraryWidgetPanel.this.onTitleClick();	
				}
				public boolean isViewMode() {
					return DashboardLibraryWidgetPanel.this.isViewMode();
				}
				
				public boolean isSort() {
					return DashboardLibraryWidgetPanel.this.isSort();
				}
				protected void onHelp(AjaxRequestTarget target) {
					DashboardLibraryWidgetPanel.this.onHelp(target);
				}
				
				@Override
				protected void onViewMode(AjaxRequestTarget target, String criteria) {
					DashboardLibraryWidgetPanel.this.onViewMode(target, criteria);
				}
				
				@Override
				protected void onSort(AjaxRequestTarget target, String criteria) {
					DashboardLibraryWidgetPanel.this.onSort(target, criteria);
				}
				
				@Override
				protected void onClickCollapse(AjaxRequestTarget target) {
					DashboardLibraryWidgetPanel.this.onClickCollapse(target);
				}
				@Override
				protected String getPreferencesKey() {
					return 	DashboardLibraryWidgetPanel.this.getPreferencesKey();
				}
				@Override
				protected String getViewModeCriteria() {
					return 	DashboardLibraryWidgetPanel.this.getViewModeCriteria();
				}
				@Override
				protected String getSortCriteria() {
					return 	DashboardLibraryWidgetPanel.this.getSortCriteria();
				}
			};
			
			he.setHelp(true);
			he.setEdit(false);
			
			setHeader(he);
	
			if (getLibrary()!=null)
				addLibrary();
			
		} catch (Exception e) {
			logger.error(e);
			
		}
		
		super.onInitialize();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (librarymodel!=null)
			librarymodel.detach();
		if (libraries!=null) 
			libraries.forEach(item -> item.detach());
		if (m_lists!=null) 
			m_lists.forEach(item -> item.detach());
	}
	
	protected void replacePanel(GeneralWicketAjaxEvent event) {
		
		String lb = (String) event.getParameters().get("library");
		
		if (getLibrary()==null) {
			
			if (getLibraries().size()>0) {
				setLibrary(getLibraries().get(0));
			}
			else
				return;
		}
		
		if (getLibrary().getKey().equals(lb))
			return;
		
		boolean found=false;
		
		for (IModel<Library> l:getLibraries()) {
			if (l.getObject().getKey()!=null && l.getObject().getKey().equals(lb)) {
				setLibrary(l);
				found=true;
				break;
			}
		}
		if (found) {
			addLibrary();
			super.addTabs();
			event.getRequestTarget().add(this);
		}
	}
	
	@Override
	protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
		StringBuilder str = new StringBuilder();
		try {

			String ty=modelObject.getObject().getService(ContentService.class).getConsoleSubtitle();
			
			if (ty!=null &&  ty.length()>0) {
				str.append(ty);
			}
			else {
				String ta=modelObject.getObject().getContentTypeClassificationAsString();
				
				if (ta!=null &&  ta.length()>0) {
					str.append(ta);
				}
				
				
				String st=modelObject.getObject().getWorkflowStatusClassificationAsString();
				
				if (st!=null &&  st.length()>0) {
					if (ta!=null && ta.length()>0)
						str.append(", ");
					str.append(st);
				}
			}
			
			OffsetDateTime date=modelObject.getObject().getLastModifiedOffsetDateTime();
			
			if (date!=null) {
				ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(getZid()));
				String tst = getDateTimeService().timeElapsed(zd, ZoneId.of(getZid()), getSessionUserLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				str.append(" - "+ tst);
			}

		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
		
	}

	
	protected void onClick(IModel<Content> model, int index) {
		try {
			Page page = getPage(model, index);
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Page getPage(IModel<Content> model, int index) 	{
		try {
			List<IModel<Content>> mi= new ArrayList<IModel<Content>>();
			getItems().forEach(item ->	 {  mi.add(new ObjectModel<Content>((Content) item.getObject()));	 });
			Page page=(Page) ServiceLocator.getService(BeansService.class).getBean( getContentClass(model.getObject()) + "-page" , model);
			if (page instanceof NavigablePage<?>)
				((NavigablePage<Content>)page).setNavigator(getNavigator(index));
			((ContentPageV6) page).setSource(ContentBaseConsole.NAME);

			return page;
		} 
		catch (Exception e) {
			logger.error(e);
			return new kbee.web.error.ApplicationErrorPage<Void>(e);
		}
	}
	
	
	@Override
	protected Panel getMenu(IModel<Content> model, final int index) {
		try {
			
			ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
			menu.setOutputMarkupId(true);

			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						private static final long serialVersionUID = 1L;
						@Override 
						public String getLabel() {
							return new StringResourceModel("open", this, null).getObject();
						}
						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								DashboardLibraryWidgetPanel.this.onClick(getModel(), index);				
							} 
							catch (Exception e) {
								setResponsePage(new ApplicationErrorPage<>(e));
								logger.error(e);	
							}
						}
					};
				}
			});
			
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						public void onClick(AjaxRequestTarget target) {
								fire(new ShareContentEvent<Content>(target, getModel()));
						}
						@Override 
						public String getLabel() {
							return DashboardLibraryWidgetPanel.this.getLabel("share").getObject();
						}
						@Override 
						public boolean isEnabled() {
							return isSendByEmail();
						}
					};
				}
			});
			
			

			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new SubMenuAjaxUserListItemPanel<Content>(id, model, getLibrary().getKey(), UserListItem.PUBLISHED);
				}
			});

			
			

			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						public void onClick(AjaxRequestTarget target) {
							fire(new AuditTrailContentEvent<Content>(target, getModel()));
						}
						@Override 
						public String getLabel() {				
							return DashboardLibraryWidgetPanel.this.getLabel("audit").getObject();
						}
						
						@Override
						public boolean isEnabled() {
							return true;
						}
					};
				}
			});
		 
			return menu;
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("menu");
		}
	}
	
	protected List<IModel<Library>> getLibraries() {
		
		if (libraries!=null)
			return libraries;
		
		libraries = new ArrayList<IModel<Library>>();
		
		for (Library cabinet : getRepository(Library.class).findAll()) {
			if (cabinet.isReadable()) 
				libraries.add( new ObjectModel<Library>(cabinet));
		};
		return libraries;
	}
	

	protected void addLibrary() {
		final boolean isExportSavedQueries= false; // ();
		setItems();

		List<ITabKB> tabs = new ArrayList<ITabKB>();
		
		if(getLibrary()!=null) {
			
			for (IModel<UserList> m_list:getLists() ) {		
				tabs.add(new AbstractTabKB(new Model<String>(m_list.getObject().getDisplayName())) {
					
					final String f_title=m_list.getObject().getDisplayName();
					final IModel<UserList> f_m_list = m_list;
					
					@Override
					public IModel<String> getTitle() {
						return new Model<String>(f_title);
					}
					@Override
					public Panel getPanel(String panelId) {
						MyListItemsPanel panel=new MyListItemsPanel(panelId, f_m_list, false, getLibrary().getKey());
						panel.setTargetBlank(false);
						return  panel;
					}
				});
			}

			tabs.add( new AbstractTabKB(new StringResourceModel("my-published", this, null)) {
				@Override
				public Panel getPanel(String panelId) {
					setQueryTerm("published-by-me");
					DashboardLibraryWidgetPanel.this.setItems();
					return DashboardLibraryWidgetPanel.this.getListPanel(panelId);
				}
			});

			
			tabs.add( new AbstractTabKB(new StringResourceModel("my-queries", this, null)) {
				@Override
				public IModel<String> getTitle() {
					if(getLibrary()==null)
						return new Model<String>(my_q);
					List<SavedQuery> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getSavedQueries(getLibrary().getKey());
					if (list!=null) {
						return new Model<String>(my_q +  " (" + String.valueOf(list .size())+")");
					}
					return new Model<String>(my_q);
				}
				
				@Override
				public Panel getPanel(String panelId) {
					Library li=DashboardLibraryWidgetPanel.this.getLibrary();
					return new SavedQueriesPanel(panelId, li.getKey(), null,  new LibraryQuery(getQueryIndex(),li), isExportSavedQueries,  false, false);
				}
			});
		
		
		
		}		
		
		setTabs(tabs);
	}
	
	@Override
	protected void refresh(AjaxRequestTarget target) {
		
		//logger.debug( getQueryTerm() );
		
		setItems();
		addTabsLists();
		super.refresh(target);
	}
	
	protected void setItems() {
		int index = 0;
		List<IModel<Content>> items = new ArrayList<IModel<Content>>();
		ResultSet contents = getContents();
		while (contents.hasNext() && index++<LIMIT) {
			items.add(new ObjectModel<Content>((Content)contents.next().getObject()));
		}
		size=items.size();
		
		boolean b_title_sort = getSortCriteria()==null || getSortCriteria().equals("title");
		
		if (b_title_sort) {
		items.sort(new Comparator<IModel<Content>>() {
			@Override
			public int compare(IModel<Content> o1, IModel<Content> o2) {
				try {
					String value1 = o1.getObject().getDisplayName();
					String value2 = o2.getObject().getDisplayName();
					if (value1==null) value1 = "";
					if (value2==null) value2 = "";
					return value1.compareToIgnoreCase(value2);
				}
				catch (Exception e) {
					return 0;	
				}
			}
		});
		}
		else {
			items.sort(new Comparator<IModel<Content>>() {
				@Override
				public int compare(IModel<Content> o1, IModel<Content> o2) {
					try {							
						boolean after= o1.getObject().getLastModifiedOffsetDateTime().isAfter(o2.getObject().getLastModifiedOffsetDateTime());
						return after ? -1 : 1;
					}
					catch (Exception e) {
						return 0;	
					}
				}
			});
		}

		
		setItems(items);
	}
	
	
	
	
	protected void setQueryTerm(String  s) {
		qTerm = s;
	}
	
	protected String getQueryTerm() {
		return qTerm;
	}
	protected ResultSet getContents() {
		KbeeUser us = (KbeeUser) getSessionUser();
		if (qTerm.equals("all"))
				return us.getService(UserDashboardService.class).getLibraryContents(getLibrary());
		else
			return us.getService(UserDashboardService.class).getUserLibraryContents(getLibrary(), getSessionUser());
	}
	
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se = ServiceLocator.getService(InlineHelpWebService.class);
		WebMarkupContainer pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_LIBRARY);
		if (pa!=null) return pa;
		return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_LIBRARY));
	}
	
	@Override
	protected boolean isMenuVisible() {
		return true;
	}

	
	
	
	protected IModel<String> getListTitle() {
		return getLabel("recent-activity");
	}
	
	@Override
	protected void onClickAll() {
		Page page;
		if (getLibrary().getPage()==null) {
			page = new ContentBasePage(getLibraryModel());
		}
		else {
			page = (Page)ServiceLocator.getService(BeansService.class).getBean(getLibrary().getPage(), getLibraryModel());
		}
		setResponsePage(page);			
	}
	
	@Override
	protected IModel<String> getViewingString() {
		return getLabel("recently-modified-library", String.valueOf(size));
	}
	
	@Override
	protected String getListContainerCss() {
		return (getViewModeCriteria().equals("comfortable") ?"cozy" : "standard");
	}

	
	@Override
	protected IModel<String> getLabelContainerCss() {
		return new Model<String>(getViewModeCriteria().equals("comfortable") ? "label-container c100" :  "label-container c40");
	}
	

	protected void onSort(AjaxRequestTarget target, String string) {
		setSortCriteria(string);
		setUserPreference("sort", string);
		refresh(target);
	}
	
	@Override
	protected  WebMarkupContainer getExpandedPanel(String id, IModel<Content> model) {
		try {
			if (model.getObject()!=null && model.getObject() instanceof IDoc) {
				IDocHitExpandedPanelV6 panel = new IDocHitExpandedPanelV6(id, new ObjectModel<IDoc>( (IDoc) model.getObject()));
				return panel;
		}
		else {
			return new ErrorPanel(id, new Model<String>("not IDOC") );
		}
		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel(id, e); 
		}
	}
	
	
	protected IModel<String> getAllString() {
		return getLabel("library");
	}
	
	protected boolean isIconVisible() {
		return false;
	}

	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
	
	
	protected boolean isExpand() {
		return false;
	}
	
	protected String getName() {
		return "home-library";
	}
	
	
	public boolean isViewMode() {
		return true;
	}

	protected boolean isSort() 	{
		return true;
	}
	

	@Override
	protected void onViewMode(AjaxRequestTarget target, String criteria) {
		setViewModeCriteria(criteria);
		setUserPreference("view-list", getViewModeCriteria());
		refresh(target);
	}

	
	@Override
	protected boolean isExpandVisible() {
		return true;
	}
	
	@Override
	protected String getBodyStyle() {
		return "min-height: 400px;";
	}


	@Override
	protected WebMarkupContainer getMoreInfoPanel(IModel<Content> modelObject) {
		return new InvisiblePanel("more-info-container");
		
		/**
		try {
			if (!view_list.booleanValue())
				return new InvisiblePanel("more-info-container");
		return new LabelPanel("more-info-container", new Model<String>("more info"));
		
		}  catch (Exception e) {
			logger.error(e);
			return new LabelPanel("more-info-container",  new Model<String>(e.getClass().getSimpleName()));
		}
		**/
	}
	


	/**
	@Override
	protected WebMarkupContainer getMoreInfoPanel(IModel<Content> modelObject) {
		try {
			
			if (getViewModeCriteria().equals("compact"))
				return new InvisiblePanel("more-info-container");
					
			String note = modelObject.getObject().getService(WorkflowService.class).getTaskComment();
			if (note==null)
				return new InvisiblePanel("more-info-container");
			note=note.replaceAll(TO_ESC,"<br />");
		return new LabelPanel("more-info-container", getSnippet(note));
		}  catch (Exception e) {
			logger.error(e);
			return new LabelPanel("more-info-container",  new Model<String>(e.getClass().getSimpleName()));
		}
	}
	**/


	
	public List<IModel<UserList>> getLists() {
		
		if (m_lists!=null)
			return m_lists;
		
		m_lists = new ArrayList<IModel<UserList>>();
		//if (site_model!=null) {
		//	for (UserList list: ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(site_model.getObject())) {
		//		m_lists .add( new ObjectModel<UserList>(list));		
		//	}
		//}
		//else {
			
		for (UserList list: ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(String.valueOf(getLibrary().getKey().toLowerCase()))) {
				m_lists .add( new ObjectModel<UserList>(list));		
		//	}
		}
			
		return m_lists;
	}

}
