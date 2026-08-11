package kbee.web.domain;



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.model.object.AuditTrailObjectPanel;


/**
 * 
 */
@SuppressWarnings("serial")
public class DomainMainPanel extends  DomainObjectEditor<Domain> implements PageMainTabs {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainMainPanel.class.getName());
	
	public DomainMainPanel(IModel<Domain> model) {
		super("editor", model);

		setModel(model);

		List<ITab> tabs = new ArrayList<ITab>();

		tabs.add(new AbstractTabKB(new StringResourceModel("editor.settings", this, null), "settings") {
			@Override
			public Panel getPanel(String panelId) {
				return new DomainSettingsEditor(panelId, getModel());
			}
		});
		
		/**
		 * 
		 * IQL para foto de login 
		 * Logo login
		 * Mensaje
		 * Contacto/suscripción
		 * Más información -> con link
		 * estilo -> derecha-todo, izquierda-todo
		 * centro
		 * 
		 * --------------
		 * foto Sí/No
		 * --------------
		 * Olvidó su password texto
		 * Contacto/Suscripción texto
		 * 
		 * Login photo
		 * favicon
		 * 
		 */
		 
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.emailsettings", this, null), "alerts") {
			@Override
			public Panel getPanel(String panelId) {
				return new DomainEmailSettingsEditor(panelId, getModel());
			}
		});
		
														
		tabs.add(new AbstractTabKB(new StringResourceModel("digital-certificate", this, null), "certificate") {
			@Override
			public Panel getPanel(String panelId) {
				return new DomainCertificateEditor(panelId, getModel());
			}
		});

		
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.calendar", this, null), "calendar") {
				@Override
				public Panel getPanel(String panelId) {
					return new DomainCalendarSettingsEditor(panelId, getModel());
				}
		});
		
		
		
		
		if (isDomainKbee()) {
//				tabs.add(new AbstractTabKB(new StringResourceModel("editor.rootuser", this, null), "root") {
//						@Override
//						public Panel getPanel(String panelId) {						
//								User user = DomainMainPanel.this.getModel().getObject().getService(DomainService.class).getRootUser();
//								if (user!=null) {
//									UserProfile userprofile =getContentDao().findUserProfileByUser(user);
//									IModel<UserProfile> model = new ObjectModel<UserProfile>(userprofile);
//									// TODO VER AT
//									// return new UserEditor(panelId, model, false, false);	
//								}
//								return new  kbee.web.error.ErrorPanel(panelId, "Application Error", " <b>root@" +  DomainMainPanel.this.getModel().getObject().getName() +  "</b> user does not exist. Please Contact Support.");
//						}
//				});
				
			tabs.add(new AbstractTabKB(getLabel("editor.quotas"), "quotas") {
				@Override
				public Panel getPanel(String panelId) {
					return new DomainQuotasEditor(panelId, getModel());
				}
			});
		}
		
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.stats", this, null), "stats") {
			@Override
			public Panel getPanel(String panelId) {
				return new DomainStatsPanel(panelId, getModel());
			}
		});
		
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.audit", this, null), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Domain>(panelId, getModel());
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) { 
				@Override
				protected void onAjaxUpdate(AjaxRequestTarget target) {
					 String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
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
		if (sel==-1)
			sel=0;
		String str =  (tabs.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}
	
	private String initial_tab;
	@SuppressWarnings("unchecked")
	@Override
	public void setInitialTab(String a) {
			initial_tab=a;
			try {
					((VerticalLayout<ITab>) get("tabs")).setSelectedTab(a);
				
			} catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
	


	
}
