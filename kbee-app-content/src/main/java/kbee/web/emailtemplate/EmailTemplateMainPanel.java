package kbee.web.emailtemplate;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.user.UserService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;

import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

public class EmailTemplateMainPanel extends ObjectEditor<EmailTemplate> implements PageMainTabs   {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailTemplateMainPanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	
	public EmailTemplateMainPanel(IModel<EmailTemplate> model, boolean isNew) {
		this("editor", model, isNew);
	}
	
	@SuppressWarnings("serial")
	public EmailTemplateMainPanel(String id, IModel<EmailTemplate> model, boolean isNew) {
		super(id, model);
		
		setModel(model);
		setIsNew(isNew);
		
		List<ITab> tabs = new ArrayList<ITab>();
		

		tabs.add(new AbstractTabKB(getLabel("emailtemplate.editor"), "info") {
			@Override
			public Panel getPanel(String panelId) {
				return new EmailTemplateEditor(panelId, getModel(), isNew()) {
					@Override
					public void setEditionEnabled(boolean value) {
						EmailTemplateMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return EmailTemplateMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						EmailTemplateMainPanel.this.onClose(target);
					}
					
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) {
							EmailTemplateMainPanel.this.onClose(target);
						}
						else { 
							setEditionEnabled(false);
							target.add(EmailTemplateMainPanel.this);
						}
					}
				};
			}
		});
		
		
		tabs.add(new AbstractTabKB(getLabel("emailtemplate.plain"), "plain") {
			@Override
			public Panel getPanel(String panelId) {
				return new PlainTextTemplateEditor(panelId, getModel(), false);
			}
		});

		
		tabs.add(new AbstractTabKB(getLabel("state"), "status") {
			@Override
			public Panel getPanel(String panelId) {
				return new ObjectStateEditor<EmailTemplate>(panelId, getModel());
			}
		});


		if (is_domain_admin || is_root) {
			tabs.add(new AbstractTabKB(getLabel("test"), "test") {
				@Override
				public Panel getPanel(String panelId) {
					return new EmailTemplateTestEditor2(panelId, getModel());
				}
			});
		}
		
		tabs.add(new AbstractTabKB(getLabel("audit"), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<EmailTemplate>(panelId, getModel());
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",  this.getClass().getName(), tabs)  {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				getUser().getService(PreferencesService.class).setValue(EmailTemplateMainPanel.class.getSimpleName(), "selected_tab", str);
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
			}
		};
		
		add(editor);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		@SuppressWarnings("unchecked")
		VerticalLayout<ITab> tabs = (VerticalLayout<ITab>) get("tabs");
		tabs.setTitle(new StringResourceModel("sections", this, null));
		
		int sel = tabs.getSelectedTab();
		if (sel==-1) sel=0;
		String str = (tabs.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}

	protected void onClose(AjaxRequestTarget target) {
	}
	 
	private KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}

	private String initial_tab;
	@SuppressWarnings("unchecked")
	@Override
	public void setInitialTab(String a) {
			initial_tab=a;
			try {
				Integer in=Integer.valueOf(a)-1;
				if (in>=0 && in < ((VerticalLayout<ITab>) get("tabs")).getTabs().size())
				((VerticalLayout<ITab>) get("tabs")).setSelectedTab(in.intValue());	
			} catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
}
