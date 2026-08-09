package kbee.web.source;


import com.novamens.content.base.Source;
import com.novamens.content.user.UserService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SourceMainPanel extends ObjectEditor<Source> implements PageMainTabs  {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SourceMainPanel.class.getName());

	public SourceMainPanel(IModel<Source> model, boolean isNew) {
		this("editor", model, isNew);
	}

	public SourceMainPanel(String id, IModel<Source> model, boolean isNew) {
		super(id, model);
		
		setModel(model);
		setIsNew(isNew);
		// add(new SourceHeaderPanel(model));
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.source", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new SourceEditor(panelId, getModel(), isNew()) {
					@Override
					public void setEditionEnabled(boolean value) {
						SourceMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return SourceMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						SourceMainPanel.this.onClose(target);
					}
					
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) {
							SourceMainPanel.this.onClose(target);
						}
						else { 
							setEditionEnabled(false);
							target.add(SourceMainPanel.this);
						}
					}
				};
			}
		});
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.state", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				//boolean real_only= isFreeVersion() && !isRoot();
				return new ObjectStateEditor<Source>(panelId, getModel());
			}
		});

		tabs.add(new AbstractTab(new StringResourceModel("editor.audit", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Source>(panelId, getModel());
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs)  {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				getUser().getService(PreferencesService.class).setValue(SourceMainPanel.class.getSimpleName(), "selected_tab", str);
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
			}
		};
		
		editor.setTitle(new StringResourceModel("sections", this, null));
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
