package com.novamens.content.web.report.markup;


import com.novamens.content.entity.Person;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.List;

public class ReportSubscriptionMainPanel extends ObjectEditor<Person> {

	private static final long serialVersionUID = 1L;

    public ReportSubscriptionMainPanel(IModel<Person> model) {
        super("editor", model);
        setModel(model);

    }

    @Override
    protected void onInitialize() {
    	super.onInitialize();

    	List<ITab> tabs = new ArrayList<>();
    	
        ITab subscriptionTabs = new AbstractTab(() -> "Subscriptions") {
			private static final long serialVersionUID = 1L;
			@Override
            public WebMarkupContainer getPanel(String s) {
                return new ReportSubscriptionEditor(s, ReportSubscriptionMainPanel.this.getModel());
            }
        };

        tabs.add(subscriptionTabs);

        SecurityService securityService = ServiceLocator.getService(SecurityService.class);

        if(securityService.isRoot() || securityService.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
            ITab manageTabs = new AbstractTab(() -> "Settings") {
				private static final long serialVersionUID = 1L;
				@Override
                public WebMarkupContainer getPanel(String s) {
                    return new SubscriptionAdminPanel(s);
                }
            };
            tabs.add(manageTabs);
        }

        VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",  this.getClass().getName(), tabs);
		editor.setTitle(new StringResourceModel("sections", this, null));
		add(editor);

        /**
		        final AjaxTabbedPanel<ITab> mainPanel = new AjaxTabbedPanel<ITab>("mainPanel",  tabs);
		        mainPanel.setTabMenuVisibility(tabs.size()>1);
		        add(mainPanel);
        **/
    }
}
