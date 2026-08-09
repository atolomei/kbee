package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.userlist.UserList;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.util.AjaxBCElement;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.service.ApplicationSiteMapService;

public class MyListsBasePanel extends KBPanel {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyListsBasePanel.class.getName());

	
	private String consoleKey;
	private boolean isClose  = true;
	private boolean isActions = true;
	private MyListsPanel myl;
	private IModel<Site> site_model;
	

	public MyListsBasePanel(String id, String consoleKey, IModel<Site> site_model, boolean isActions) {
		super(id);
		setOutputMarkupId(true);
		this.site_model = site_model;
		this.consoleKey=consoleKey;
		this.isActions=isActions;
		
	}
	
	
	public String getConsoleKey() {
		return consoleKey;
	}
	
	public void setIsClose(boolean b) {
		this.isClose=b;
		if (myl!=null)
			myl.setIsClose(isClose);
	}
	
	public boolean isClose() {
		return this.isClose;
	}
	
	
	public void onDetach() {
		super.onDetach();
		if (site_model!=null)
			site_model.detach();
	}
	
	protected void addBreadcumb(IModel<String> selected_list) {

		 if (selected_list==null) {
			 addOrReplace(new InvisiblePanel("breadcrumb"));
			 return;
		 }
		
		MenuBreadCrumbPanel<Void> bc =new MenuBreadCrumbPanel<Void>("breadcrumb");
		
		bc.addElement(new AjaxBCElement<Void>("mylists") {
				
			private static final long serialVersionUID = 1L;
			
			public void onClick(AjaxRequestTarget target) {
					MyListsBasePanel.this.addBreadcumb(null);
					MyListsBasePanel.this.get("my-lists").setVisible(true);
					MyListsBasePanel.this.get("list-items").setVisible(false);
					target.add(MyListsBasePanel.this);
				}
			});
		
			bc.addElement( new BCElement(selected_list));
		
		addOrReplace(bc);
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		addBreadcumb(null);
		
			/* my lists */
			myl = new MyListsPanel("my-lists", this.consoleKey, site_model, isActions) {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void close(AjaxRequestTarget target) {
				MyListsBasePanel.this.close(target);
			}
			
			@Override	
			protected void onListSelected(IModel<UserList> iModel, AjaxRequestTarget target) {
				if (getSiteModel()!=null) {
					try {
						
						// setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.UserListPage,new Object[] {iModel }));
							
						
						boolean apply=true;
						
						fire(new MyListsApplyUserListEvent(target, iModel, apply));
						target.add(MyListsBasePanel.this);
						
						
					} 
					catch (ContentMgmtException e) {
						logger.error(e);
						//setResponsePage(new ApplicationErrorPage(e));
					}
				}
				else {
					MyListItemsPanel panel = new MyListItemsPanel("list-items", iModel, MyListsBasePanel.this.isActions, MyListsBasePanel.this.getConsoleKey() ) {
						private static final long serialVersionUID = 1L;
						@Override
						protected void close(AjaxRequestTarget target) {
							MyListsBasePanel.this.close(target);
						}
					};
					
					panel.setIsClose(isClose());
					MyListsBasePanel.this.addBreadcumb(new Model<String>(iModel.getObject().getTitle()+ " (" +  String.valueOf(iModel.getObject().getTotalItems()) + ")"));
					MyListsBasePanel.this.addOrReplace(panel);
					MyListsBasePanel.this.get("my-lists").setVisible(false);
					target.add(MyListsBasePanel.this);
				}
			}
		};
		
		myl.setIsClose(isClose);
		add(myl);
		
		/* list items */
		add( new InvisiblePanel("list-items"));
	}
	
	
	protected IModel<Site> getSiteModel() {
		return site_model;
	}
	
	
	protected void close(AjaxRequestTarget target) {}
	
}
