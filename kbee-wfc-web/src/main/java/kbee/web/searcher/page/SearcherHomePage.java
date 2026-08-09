package kbee.web.searcher.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.userlist.UserList;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsUserListItemUpdateObjectEvent;
import com.novamens.kbee.wicket.markup.html.event.EditableListEvent;
import com.novamens.kbee.wicket.markup.html.event.ExplorerOpenEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.event.wicket.LabelEvent;

import kbee.web.portal6.PortalObjectViewerRenderService;
import kbee.web.searcher.searchform.SearcherOnChangeEvent;
import kbee.web.workflow.task.WorkflowPriorityEvent;

@SuppressWarnings("serial")
public class SearcherHomePage extends AbstractSearcherPage<Site> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(SearcherHomePage.class.getName());

	private boolean advancedSearcherVisible = false;
	
	private boolean useImages = false;
	
	@Override
	protected boolean isEditableOn() {
		return false;
	}

	
	
	public SearcherHomePage(PageParameters parameters) {
		Site site = getSite(parameters);
		if (site!=null) 
			setSiteModel(new ObjectModel<Site>(site));
	}
	

	public SearcherHomePage(IModel<Site> siteModel) {
		super(siteModel);
		setSiteModel(siteModel);
		if (siteModel!=null && siteModel.getObject()!=null && getSiteModel().getObject().getUrl()!=null)
			getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		createFavListIfNotExists();
		setOutputMarkupId(true);
		
		if (getSiteModel()==null || getSiteModel().getObject()==null) {
			add( new InvisiblePanel("main_ps_payload"));	
			return;
		}
		
		setPageTitle(new Model<String>(getSiteModel().getObject().getTitle()));
		
		if (!hasPermissions()) {
			addOrReplace( new ErrorNotAuthorizedPanel<Site>("main_ps_payload"));
			return;
		}
		
		Page home=getSiteModel().getObject().getHomePage();
		
		if (logger.isDebugEnabled()) {
				for (Page p: getSiteModel().getObject().getPages()) {
					for (PageSection ps: p.getPageSections()) {
						logger.debug(p.getTitle()+ "  -> " + ps.getDisplayName());
					}	
				}
		}
		
		// new SearcherForm("main-searcher", getSiteModel(), getSiteModel().getObject().getTitle())
		
		PageSection ps = home.getPageSections().get(0);
		add( ps.getService(PortalObjectViewerRenderService.class).build("main_ps_payload", PortalViewMode.PRODUCTION));
	}
	
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener< EditableListEvent<Site>>() {
			public void onEvent(EditableListEvent<Site> event) {
				try {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
					SearcherResultsPage s= new SearcherResultsPage(getSiteModel(), sq);
					setResponsePage(s);
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}		
			}
		});
		
		add(new WicketEventListener<SearcherOnChangeEvent>() {
			public void onEvent(SearcherOnChangeEvent event) {
				try {
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex());
					setFilters(sq, event.getParameters());
					setUserPreference("explorer-mode", "no");
					setResponsePage(new SearcherResultsPage(getSiteModel(),sq));
				} 
				catch (Exception e) {
					logger.error(e);
				}		
			}
			private void setFilters(Query query, Map<String, Object> filters) {
				List<String> parameters = new ArrayList<String>();
				parameters.addAll(query.getParameters().keySet());
				for (String parameter : parameters) {
					if (query.getParameters().get(parameter) instanceof Filter) {
						synchronized (query) {
							query.getParameters().remove(parameter);
						}
					}
				}
				for (String filter: filters.keySet()) {
					query.setParameter(filter, filters.get(filter));
				}
			}
		});
		
		add(new WicketEventListener<ExplorerOpenEvent<Site>>() {
			public void onEvent(ExplorerOpenEvent<Site> event) {
				try {
					setResponsePage(new SearcherExplorerPage(event.getModel()));
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}		
			}
		});
		
		
	
	
		/**
		 * Apply UserList
		 * 
		 */
		add(new WicketEventListener<MyListsApplyUserListEvent>() {
			@Override
			public void onEvent(MyListsApplyUserListEvent event) {
				IModel<UserList> list= event.getUserList();
				SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex());
				ValueFilter filter = new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName());
				sq.getParameters().put("userlist", filter);
				setResponsePage(new SearcherResultsPage(getSiteModel(),sq));
				list.detach();
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsApplyUserListEvent;
			}
		});
		
		/**
		 * Apply Label
		 */
		
		add(new WicketEventListener<LabelEvent>() {
			@Override
			public void onEvent(LabelEvent event) {
				// WorkspaceConsole.this.refresh(event.getRequestTarget());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof LabelEvent;
			}
		});

		add(new WicketEventListener<WorkflowPriorityEvent>() {
			@Override
			public void onEvent(WorkflowPriorityEvent event) {
				// WorkspaceConsole.this.refresh(event.getRequestTarget());
			}
			
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof WorkflowPriorityEvent;
			}
			
		});

		/**
		 * Add/remove Object to List
		 */
		add(new WicketEventListener<MyListsUserListItemUpdateObjectEvent<Content>>() {
			@Override
			public void onEvent(MyListsUserListItemUpdateObjectEvent<Content> event) {
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsUserListItemUpdateObjectEvent;
			}
		});
		
		add(new WicketEventListener<ExplorerOpenEvent<Site>>() {
			public void onEvent(ExplorerOpenEvent<Site> event) {
				try {
					setUserPreference("explorer-mode", "yes");
					setResponsePage(new SearcherExplorerPage(event.getModel()));
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}		
			}
		});
	}
	
	public void setAdvancedSearcherVisible(boolean b) {
		this.advancedSearcherVisible=b;
	}
	
	public boolean isAdvancedSearcherVisible() {
		return this.advancedSearcherVisible;
	}

	
	@Override
	protected void addBillboard() {
		add(new InvisiblePanel("billboard"));
	}
	
	protected boolean isHome() {
		return true;
	}
	
	protected boolean hasLateralMenu() {
		return false;
	}

	
	protected void createFavListIfNotExists() {
		try {
			KbeeUser user=(KbeeUser) getSessionUser();
			String key=getSiteModel().getObject().getOId().toString();
			 long total = getPropertyDao().getTotalListConsole(getSessionUser(), key);
			 if (total==0)
				 ServiceLocator.getService(ObjectFactoryService.class).createUserList(user, key, new StringResourceModel("favorites", this, null).getObject());
		}
		catch (Exception e) {
			 logger.error(e);
		 }
	}

	protected PropertyDao getPropertyDao() {
		return (PropertyDao) ServiceLocator.getService(BeansService.class).getBean("propertyDao");
	}
	
	/** 
	 * Reports
	 *  
	 **/
	protected String getPageType()     {return "search-home";} 												// con | det  
	protected String getContentTitle() {return null;} 														// content title or user title, ...
										
	protected String getStatsPageTitle() {return "search home";} 											// for console page, it is the name of the console 
	protected Long getStatsPageId() {return Long.valueOf(0);} 								                // for console page, it is the name of the console
													
	protected String getObjectId()  {return null;} 												   			// for user, domain, ...
	protected String getContentId() {return null;}	  														// for content


	@Override
	protected boolean isExplorerOn() {
		// TODO Auto-generated method stub
		return false;
	}

}




