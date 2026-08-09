package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListService;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")

public class SubMenuAjaxUserListItemPanel<T> extends SubmenuAjaxItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
				
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubMenuAjaxUserListItemPanel.class.getName());
	
	private String console;
	private int versionMatch;
	private  IModel<Site> sitemodel;
	
	public  SubMenuAjaxUserListItemPanel(String id, IModel<T> model, String console,  int versionMatch) {
		this(id, model, console, null, versionMatch);
	}
	
	public  SubMenuAjaxUserListItemPanel(String id, IModel<T> model, String console, IModel<Site> sitemodel, int versionMatch) {
		super(id, model, null, null);
		this.console=console;
		this.sitemodel = 	 sitemodel;
		this.versionMatch=versionMatch;
	}

	public void setConsole(String console) {
		this.console=console;
	}
	
	public void onDetach() {
		super.onDetach();
		if (sitemodel!=null)
			sitemodel.detach();
	}
	
	public int getVersionMatch() {
		return this.versionMatch;
	}
	
	public String getLabel() {
		return SubMenuAjaxUserListItemPanel.this.getLabel("lists").getObject();
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
			
		add(new WicketEventListener<MyListsDeleteListEvent>() {
			@Override
			public void onEvent(MyListsDeleteListEvent event) {
				logger.debug(event.getClass().getSimpleName() + " | " + getDisplayName(SubMenuAjaxUserListItemPanel.this.getModelObject()));
				if (isListViewCreated()) {
					getItems().clear();
					addItems();				
				}
			}
		});
			

		add(new WicketEventListener<MyListsAddListEvent>() {
			@Override
			public void onEvent(MyListsAddListEvent event) {
				logger.debug(event.getClass().getSimpleName() + " | " + getDisplayName(SubMenuAjaxUserListItemPanel.this.getModelObject()));
				if (isListViewCreated()) {
					getItems().clear();
					addItems();
				}
			}
		});
			
			
		add(new WicketEventListener<MyListsRemoveAllEvent>() {
			@Override
			public void onEvent(MyListsRemoveAllEvent event) {
				logger.debug(event.getClass().getSimpleName() + " | " + getDisplayName(SubMenuAjaxUserListItemPanel.this.getModelObject()));
				if (isListViewCreated()) {
					getItems().clear();
					addItems();
				}
			}
		});
			
			
		add(new WicketEventListener<MyListsUpdateListEvent>() {
			@Override
			public void onEvent(MyListsUpdateListEvent event) {
				logger.debug(event.getClass().getSimpleName() + " | " + getDisplayName(SubMenuAjaxUserListItemPanel.this.getModelObject()));
				if (isListViewCreated()) {
					getItems().clear();
					addItems();
				}
			}
		});
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}
	
	protected String getDisplayName(T object) {
		return DisplayNameExtractor.get(object);
	}

	@Override
	protected void addItems() {
		getItems().clear();
		
		if (sitemodel!=null) {
			for (UserList uli: ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists( sitemodel.getObject())) {
				addItem(new KbeeUserListMenuItemFactory<T>(getModel(), new ObjectModel<UserList>(uli), getVersionMatch()));
			}
		}
		else {
			for (UserList uli: ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists( console )) {
				addItem(new KbeeUserListMenuItemFactory<T>(getModel(), new ObjectModel<UserList>(uli), getVersionMatch()));
			}
		}
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, SubMenuAjaxUserListItemPanel.this, null);
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setEventPropagation(EventPropagation.STOP); 
	}
}
