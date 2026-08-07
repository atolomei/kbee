package com.novamens.content.web.nav.markup;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.user.UserService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

@Deprecated
@SuppressWarnings("serial")
public class NotificationsPanel extends Panel{
	private static final long serialVersionUID = 1L;

	
	Component panel;

	public NotificationsPanel(String id) {
		super(id);
		
		AjaxLink<Void> link = new AjaxLink<Void>("notifications-link") {
			public void onClick(AjaxRequestTarget target) {
				showPanel(target);
			}
		};
		
		link.add(new Label("total", new Model<String>() {
			public String getObject() {
				return String.valueOf(getTotal());
			}
		}));
		
		add(link);
		
		WebMarkupContainer container = new WebMarkupContainer("notifications-container");
		container.setOutputMarkupId(true);
		WebMarkupContainer lzy = new WebMarkupContainer("notifications-panel");
		container.add(lzy);
		add(container);
	}
	
	public long getTotal() {
		return 0;
	}
	
	protected void showPanel(AjaxRequestTarget target) {
		if (getTotal()==0)
			return;
		
		if (panel==null) {
			panel = new kbee.web.notification.NotificationsPanel("notifications-panel", new ObjectModel<User>(getUser())) {
				@Override					 
				public void close(AjaxRequestTarget target) {
					//NotificationsAction.this.onClose(target);
				}
				@Override					 
				public void onTitleClick(AjaxRequestTarget target, IModel<Content> model) {
					//NotificationsAction.this.onTitleClick(model);
				}
			};
			get("notifications-container:notifications-panel").replaceWith(panel);
			panel.setVisible(false);
		}
		
		if (panel.isVisible())
			panel.setVisible(false);
		else
			panel.setVisible(true);
		target.add(get("notifications-container"));
	}
	
	private User getUser() {
		return (User)ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
}
