package kbee.web.dashboard;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.userlist.UserListService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryLinkEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsDeleteListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.ITabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.cursor.CursorListModel;
import kbee.web.cursor.ModelListCursor;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.searcher.page.SearcherDetailDocumentPage;
import kbee.web.searcher.page.SearcherResultsPage;

public class DashboardPortalSavedQueryWidgetPanel extends DashboardListWidgetPanel<SavedQuery> implements PanelPortalModel<Block>, PortalViewRender {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardPortalSavedQueryWidgetPanel.class.getName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private  IModel<Block> model;
	private int size;
	private  IModel<Site> sitemodel;
	private String zid;
	private Locale locale;

	public DashboardPortalSavedQueryWidgetPanel(String id) {
		super(id);
		KbeeUser us = (KbeeUser) getSessionUser();
		locale=us.getLocale();
		zid = ServiceLocator.getService(DateTimeService.class).getMapZoneIds().get(us.getTimeZone());
	}
	
	
	
	
	
	public DashboardPortalSavedQueryWidgetPanel(String id, List<IModel<SavedQuery>> list, IModel<String> title,		String preferences_key) {
		super(id, list, title, preferences_key);
 	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (sitemodel!=null)
			sitemodel.detach();
		if (model!=null)
			model.detach();
		
	}

	@Override
	public void setPortalModel(IModel<Block> model) {
		setModel(model);
		this.model=model;
	}


	@Override
	public IModel<Block> getPortalModel() {
		return model;
	}
	
	
	public Site getSite() {
		return sitemodel.getObject();
	}
	
	 

	@Override
	protected boolean isMenuVisible() {
		return isRoot() || isAdmin();
	}

	protected IModel<String> getLabelContainerCss() {
		return new Model<String>("label-container");
	}
	
	protected IModel<String> getListTitle() {
		return new StringResourceModel("recent-activity",this, null);
	}
	
	/**
	 * 
	 */
	protected IModel<String> getItemLabelMeta(IModel<SavedQuery> modelObject) {
		return null;
		
	}

	@Override
	protected IModel<String> getViewingString() {
		return null;
		//return new StringResourceModel("recently-modified", this, null).setParameters(new Object[] {String.valueOf(size)} );
	}

	protected IModel<String> getAllString() {
		return null;
		//return new Model<String>(getPortalModel().getObject().getDisplayName());
	}

	@Override
	protected void onClick(IModel<SavedQuery> model, int index) {
		fireScanAll(new ApplySavedQueryLinkEvent(model.getObject()));
	}
	
	@Override
	protected Panel getMenu(IModel<SavedQuery> model, final int index) {

			try {
				
				ContextMenuPanel<SavedQuery> menu = new ContextMenuPanel<SavedQuery>(model);
										
				menu.setOutputMarkupId(true);
				
				menu.addItem(new MenuItemFactory<SavedQuery>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public AbstractMenuItemPanelV5<SavedQuery> getItem(String id) {
						return new AjaxMenuItemPanelV5<SavedQuery>(id) {
							private static final long serialVersionUID = 1L;
							@Override 
							public String getLabel() {
								return new StringResourceModel("delete", this, null).getObject();
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									
									try {
										((KbeeUser) getModel().getObject().getUser()).getService(UserListService.class).delete(getModel().getObject());
										refresh(target);
										fireScanAll(new MyListsDeleteListEvent(target));
										
									} catch (Exception e) {
										logger.error(e);						
									}
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
	protected void refresh(AjaxRequestTarget target) {
		addList();
		super.refresh(target);
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

	@Override
	protected String getListContainerCss() {
		return "cozy";
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



	@Override
	public void onInitialize() {
		setHelp(true);
		setTitle( new Model<String>(getPortalModel().getObject().getTitle()));
		addList();
		super.onInitialize();
	}
	
	protected void addList() {
		List<IModel<SavedQuery>> list = new ArrayList<IModel<SavedQuery>>();
		try {
			
			// User w_user = getDomain().getService(DomainService.class).getWorkflowUser();
			// ((KbeeUser) w_user).getService(UserListService.class).getSavedQueries( getSiteModel().getObject()).forEach(item -> list.add(new ObjectModel<SavedQuery>(item)));
			
			User w_user =  getSessionUser();
																										
			
			
			((KbeeUser) w_user).getService(UserListService.class).getSavedQueries( "all" ).forEach(item -> list.add(new ObjectModel<SavedQuery>(item)));
			size=list.size();
																					
			
			//((KbeeUser) w_user).getService(UserListService.class).getSavedQueries( getSiteModel().getObject().getId().toString() ).forEach(item -> list.add(new ObjectModel<SavedQuery>(item)));
			//	size=list.size();
			
			
			

			
			
			logger.debug("Saved Queries -> " + String.valueOf(size));
			
			
		} catch (Exception e) {
			logger.error(e);
		}
		setItems(list);
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

	

}
