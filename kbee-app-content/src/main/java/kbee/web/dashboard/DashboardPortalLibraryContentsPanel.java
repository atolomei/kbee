package kbee.web.dashboard;
     
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.library.Library;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.content.userlist.UserListService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Criteria;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.library.IqlCriteria;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsBasePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesPanel;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.ITabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.console.CursorNavigator;
import kbee.web.content.console.ContentBasePage;
import kbee.web.cursor.CursorListModel;
import kbee.web.cursor.ModelListCursor;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.query.LibraryQuery;
import kbee.web.query.ListModelQuery;
import kbee.web.searcher.page.SearcherDetailDocumentPage;
import kbee.web.searcher.page.SearcherResultsPage;

public class DashboardPortalLibraryContentsPanel extends DashboardListWidgetPanel<Content> implements PanelPortalModel<Block>, PortalViewRender  {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardLibraryWidgetPanel.class.getName());

	private int size;
	//private String my_q;
	//private String my_f;
	private String zid;
	private Locale locale;
	
	private  IModel<Site> sitemodel;
	private  IModel<Block> model;

	private String viewModeCriteria = "comfortable";
	
	public DashboardPortalLibraryContentsPanel(String id) {
		super(id);
		KbeeUser us = (KbeeUser) getSessionUser();
		locale=us.getLocale();
		zid = ServiceLocator.getService(DateTimeService.class).getMapZoneIds().get(us.getTimeZone());
	}
			
	@Override
	public void onInitialize() {
		
		if (getSiteModel()!=null) {
			setPreferencesKey( getSiteModel().getObject().getKey());
			setTitle( new Model<String>(getPortalModel().getObject().getTitle()));
		}

		setEdit(false);
		setHelp(true);
		addSite();
		
		super.onInitialize();
	}
	
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se=ServiceLocator.getService(InlineHelpWebService.class);
		 WebMarkupContainer  pa = se.getPanel("help", getLocale(), InlineHelpWebService.PORTAL_LIBRARY);
		 if (pa!=null)
			 return pa;
		 return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_NOTIFICATIONS));
	}
	
	
	@Override
	public void addListeners() {
		super.addListeners();
	}

	/**
	 * 
	 * 
	 */
	private void addSite() {


		List<IModel<Content>> list = new ArrayList<IModel<Content>>();
		
		KbeeUser us = (KbeeUser) getSessionUser();
		us.getService(UserDashboardService.class).getSiteContents(getSite(), 15).forEach(item -> list.add(new ObjectModel<Content>(item)));
		size=list.size();
		setItems(list);

		List<ITabKB> tabs = new ArrayList<ITabKB>();
		
		tabs.add( new AbstractTabKB( getQueriesTitle()) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				try {
					SearcherSiteQuery qe= new SearcherSiteQuery(getSite(), getIndex());
					String key=getSiteModel().getObject().getOId().toString();
					SavedQueriesPanel pa=new SavedQueriesPanel(panelId, key,  getSiteModel(), qe, false,  false, false);
					pa.setIsClose(false);
					return pa;
				} catch (Exception e) {
					logger.error(e);
					return new ErrorPanel(panelId, e);
				}
				
			}
		});
	 
		
		tabs.add( new AbstractTabKB( getMyListsTitle()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Panel getPanel(String panelId) {
				try {
					String key=getSiteModel().getObject().getOId().toString();
					MyListsBasePanel mm= new MyListsBasePanel(panelId, key,  getSiteModel(),  true);
					mm.setIsClose(false);
					return mm;
				} catch (Exception e) {
					logger.error(e);
					return new ErrorPanel(panelId, e);
				}
			}
		});

		setTabs(tabs);
		
	}
	
	public Site getSite() {
		return sitemodel.getObject();
	}
	
	public void onDetach() {
		super.onDetach();
		
		if (model!=null)
			model.detach();
		
		if (sitemodel!=null)
			sitemodel.detach();
	}

	@Override
	protected boolean isMenuVisible() {
		return false;
	}

	
	
	@Override
	protected IModel<String> getLabelContainerCss() {
		return new Model<String>(getViewModeCriteria().equals("comfortable") ? "label-container c100" :  "label-container c40");
	}

	@Override
	protected String getListContainerCss() {
		return (getViewModeCriteria().equals("comfortable") ? "cozy" : "standard");
	}

	
	
	
	protected String getViewModeCriteria() {
		return viewModeCriteria;
	}

	protected void setViewModeCriteria(String s) {
		viewModeCriteria=s;
	}

	
	
	protected IModel<String> getListTitle() {
		return new StringResourceModel("recent-activity",this, null);
	}
	

	
	/**
	 * 
	 */
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
				ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(zid));
				String tst = ServiceLocator.getService(DateTimeService.class).timeElapsed(zd, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				str.append(" - "+ tst);
			}
			

		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
		
	}

	@Override
	protected IModel<String> getViewingString() {
		return new StringResourceModel("recently-modified", this, null).setParameters(new Object[] {String.valueOf(size)} );
	}

	
	protected IModel<String> getAllString() {
		return new Model<String>(getPortalModel().getObject().getDisplayName());
	}

	
	@Override
	protected void onClick(IModel<Content> model, int index) {
		try {
			
			List<IModel<Content>> mi= new ArrayList<IModel<Content>>();
			getItems().forEach(item -> { mi.add(new ObjectModel<Content>((Content) item.getObject())); });
			
			if (model.getObject().getClassCode().equals(IDoc.CLASS_CODE)) {
				SearcherDetailDocumentPage<IDoc> pa = new SearcherDetailDocumentPage<IDoc>(new ObjectModel<IDoc>((IDoc) model.getObject()),getSiteModel());
				CursorListModel<Content> cursor = new CursorListModel<Content> (mi, index);
				CursorNavigator<IDoc> nav = new CursorNavigator<IDoc>(cursor, index);
				pa.setNavigator(nav);
				setResponsePage(pa);
						
				
				// TODO VER AT
				
				/**setResponsePage(new SearcherDetailDocumentPage<IDoc>( 
								new ObjectModel<IDoc>((IDoc) model.getObject()),		
								getSiteModel(), 
								new  ModelListCursor<Content>(cursor))
								);
				 **/
				
				
			}
			else if (model.getObject().getClassCode().equals(OrganizationalText.CLASS_CODE)) {
				throw new IllegalArgumentException(OrganizationalText.CLASS_CODE + " Text is not supported");
				/**
				setResponsePage(new SearcherDetailDocumentPage<Content>( 
						new ObjectModel<Content>((Content) model.getObject()),		
						getSiteModel(), 
						new  ModelListCursor<Content>(cursor))
						);
						**/
				
			}
		} catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
			
		}
	}
	
	@Override
	protected Panel getMenu(IModel<Content> model, final int index) {

			try {
				
				ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
										
				menu.setOutputMarkupId(true);
				
				menu.addItem(new MenuItemFactory<Content>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

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
									setResponsePage(new SearcherDetailDocumentPage<IDoc>( new ObjectModel<IDoc>((IDoc) model.getObject()),		getSiteModel() )) ;
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
							}
						};
					}
				});
				

				return menu;
				
			} catch (Exception e) {
				logger.error(e, getSessionUser().getUserName());
				return new InvisiblePanel("menu");
			}
	}


	@Override
	protected void onClickAll() {
		SearcherSiteQuery qe= new SearcherSiteQuery(getSite(), getIndex());
		setResponsePage(new SearcherResultsPage( getSiteModel(), qe	));
	}
	

	protected boolean isExpand() {
		return false;
	}
	
	
	protected String getName() {
		return getSite().getKey();
	}
	
	

	protected boolean isIconVisible() {
		return false;
	}

	



	public IModel<Block> getModel() {
		return this.model;
	}
	
	
	public void setModel(IModel<Block> model) {
			this.model=model;
			
			if (model!=null) {
				setSiteModel( new ObjectModel<Site>( model.getObject().getSite()));
			}
	}

	public IModel<Site> getSiteModel() {
		if (this.sitemodel==null) {
			if (getModel()!=null) {
				this.sitemodel=new ObjectModel<Site>( model.getObject().getSite());
			}
		}
		return this.sitemodel;
}

	
	
	public void setSiteModel(ObjectModel<Site> objectModel) {
			this.sitemodel=objectModel;
	}



	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}


	public IModel<String> getMyListTitle() {
		KbeeUser user = ((KbeeUser) getSessionUser());
		String ul= " (" + String.valueOf( user.getService(UserListService.class).getUserLists(getSiteModel().getObject().getKey()).size())+")";
		String myl=new StringResourceModel("my-favs", this, null).getObject();
		return new Model<String>(myl + ul);
	}
	
	
	public IModel<String> getQueriesTitle() {
		KbeeUser user = ((KbeeUser) getSessionUser());
		String key=getSiteModel().getObject().getKey();
 
		String uq;
		if (sitemodel!=null)
			uq= " (" + String.valueOf( user.getService(UserListService.class).getSavedQueries(sitemodel.getObject()).size())+")";
		else
			uq= " (" + String.valueOf( user.getService(UserListService.class).getSavedQueries(key).size())+")";
		
		String myq=new StringResourceModel("my-queries", this, null).getObject();
		return new Model<String>(myq + uq);
	}



	public IModel<String> getMyListsTitle() {
		KbeeUser user = ((KbeeUser) getSessionUser());
		String key=getSiteModel().getObject().getKey();
 
		String uq;
		if (sitemodel!=null)
			uq= " (" + String.valueOf( user.getService(UserListService.class).getUserLists(sitemodel.getObject()).size())+")";
		else
			uq= " (" + String.valueOf( user.getService(UserListService.class).getUserLists(key).size())+")";
		
		String myq=new StringResourceModel("my-lists", this, null).getObject();
		return new Model<String>(myq + uq);
	}


	
	@Override
	public void setPortalModel(IModel<Block> model) {
		setModel(model);
	}


	@Override
	public IModel<Block> getPortalModel() {
		return getModel();
	}
	

}
