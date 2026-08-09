package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;


import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.dom.Object;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;

public class ObjectUserListMenuItem<T> extends AjaxMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectUserListMenuItem.class.getName());
	
	private IModel<T> object_model;
	private IModel<UserList> model;
	private long time = 0;
	private AjaxLink<?> link;
	private int version_match = UserListItem.NEWEST;
	private Boolean isenabled;
	
	public ObjectUserListMenuItem(String id, IModel<UserList> model, IModel<T> object_model, int versionMatch) {
		super(id);
		setOutputMarkupId(true);
		this.model = model;
		this.object_model = object_model;
		this.version_match = versionMatch;
		
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setEventPropagation(EventPropagation.STOP); 
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.model.detach();
		this.object_model.detach();
	}

	public IModel<T> getObjectModel() {
		return this.object_model;
	}
	
	public IModel<UserList> getUserListModel() {
		return this.model;
	}
	
	@Override
	public String getCssClass() {
		return isEnabledUserList() ? "label-selected": "label-no-selected";
	}
	
	@Override
	public String getLabel() {
		try {
		return model.getObject().getTitle();
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getSimpleName();
		}
	}

	@Override
	public void onClick(AjaxRequestTarget target) throws Exception {
		long now = System.currentTimeMillis();

		if (now-time<1000) 
			return;
		
		time = now;
		
		target.add(link);
		
		flipItem();
		
		onUpdate(target);
	}


	public String getIconCssClass() {
		return isEnabledUserList() ? (CHECK + " toright fa-fw") : ""; 	
	}
	
	
	@Override
	protected AbstractLink getNewLink(String id) {
		this.link = new AjaxLink<Void>(id) {
			private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
				try {
					ObjectUserListMenuItem.this.onClick(target);
				}
				catch (Exception e) {
					logger.error(e);
					throw new RuntimeException(e);
				}
			}
		};
		return link;
	}
	
	protected boolean isEnabledUserList() {
		
		if (isenabled!=null)
			return isenabled.booleanValue();
		try {
			isenabled = Boolean.valueOf(getUserListModel().getObject().belongs((Object)getModelObject()));
		} 
		catch (Exception e) {
			logger.error(e);
			isenabled=Boolean.valueOf(false);
		}
		return isenabled.booleanValue();
	}

	public int getVersionMatch() {
		return this.version_match;
	}
	
	
	/**
	 * changes the state of the object in this list
	 * adds or removes
	 */
	protected void flipItem() {
		
		if (isEnabledUserList()) { 
			getUserListModel().getObject().remove((Object)getModelObject());
			((KbeeUser) getUserListModel().getObject().getOwner()).getService(UserListService.class).save(getUserListModel().getObject());
			this.isenabled=Boolean.valueOf(false);
		}
		else {
			getUserListModel().getObject().add((Object)getModelObject(), getVersionMatch());
			((KbeeUser) getUserListModel().getObject().getOwner()).getService(UserListService.class).save(getUserListModel().getObject());
			this.isenabled=Boolean.valueOf(true);
		}
	}

	protected void onUpdate(AjaxRequestTarget target) {
		fire( new MyListsUserListItemUpdateObjectEvent<T>(target, getModel(), model));
	}
}
