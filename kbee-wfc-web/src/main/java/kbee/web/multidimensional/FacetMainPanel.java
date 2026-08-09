package kbee.web.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.indexer.query.Facet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import kbee.web.model.object.AuditTrailObjectPanel;

@SuppressWarnings("serial")
public class FacetMainPanel extends ObjectEditor<Facet> {
	private static final long serialVersionUID = 1L;
	
	public FacetMainPanel(IModel<Facet> model) {
		this("editor", model);
	}
	
	public FacetMainPanel(String id, IModel<Facet> model) {
		super(id, model);
		
		setModel(model);


		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.facet", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new FacetEditor(panelId, getModel()) {
					@Override
					public void setEditionEnabled(boolean value) {
						FacetMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return FacetMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						FacetMainPanel.this.onClose(target);
					}
					
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) {
							FacetMainPanel.this.onClose(target);
						}
						else { 
							setEditionEnabled(false);
							target.add(FacetMainPanel.this);
						}
					}
				};
			}
		});

		tabs.add(new AbstractTab(new StringResourceModel("editor.visibility", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new VisibilityPanel(panelId);
			}
		});
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.audit", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Facet>(panelId, getModel());
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",this.getClass().getName(), tabs)  {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				getUser().getService(PreferencesService.class).setValue(FacetMainPanel.class.getSimpleName(), "selected_tab", str);
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
}
