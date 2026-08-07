package com.novamens.content.web.security.markup;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import kbee.web.model.object.AuditTrailObjectPanel;

@SuppressWarnings("serial")
public class GroupMainPanel extends ObjectEditor<Group> {
	private static final long serialVersionUID = 1L;

	public GroupMainPanel(IModel<Group> model, boolean gotolastselection, final boolean isNew) {
		super("editor", model);
		
		setModel(model);

		add(new GroupHeaderPanel(model));
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.info", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new GroupEditor(panelId, getModel(), isNew) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						//((GroupPanel) GroupMainPanel.this.get("group-panel")).onUpdate(target);
					}
					@Override
					public void onCancel(AjaxRequestTarget target) {
						GroupMainPanel.this.onClose(target);
					}
					
					@Override
					public void setEditionEnabled(boolean value) {
						GroupMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return GroupMainPanel.this.isEditionEnabled();
					}
				};
			}
		});

		tabs.add(new AbstractTab(new StringResourceModel("editor.users", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new GroupMembersEditor(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.rules", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new GroupRulesPanel(panelId, getModel());
			}
		});

		tabs.add(new AbstractTab(new StringResourceModel("editor.audit", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Group>(panelId, getModel());
			}
		});

		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(),tabs) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				getUser().getService(PreferencesService.class).setValue(GroupMainPanel.class.getSimpleName(), "selected_tab", str);
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
			}
		};
				
		
		editor.setTitle(new StringResourceModel("sections", this, null));

		
		add(editor);
		
    }

	/** ------------------------------------------------------------------------------
	 */ 

	@Override
	public void onInitialize() {
		super.onInitialize();
		

	}
	
	/** ------------------------------------------------------------------------------
	*/
	protected void onClose(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
	}

	/** ------------------------------------------------------------------------------
	*/
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/** ------------------------------------------------------------------------------
	*/ 
	private KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
		
}
