package kbee.web.rule;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.rule.ActionRule;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

@SuppressWarnings("serial")
public class EntityRuleMainPanel extends ObjectEditor<ActionRule> {
	private static final long serialVersionUID = 1L;
	
	public EntityRuleMainPanel(IModel<ActionRule> model, boolean isNew) {
		this("editor", model, isNew);
	}
	
	public EntityRuleMainPanel(String id, IModel<ActionRule> model, boolean isNew) {
		super(id, model);
		setModel(model);
		setIsNew(isNew);
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(getLabel("editor.rule")) {
			@Override
			public Panel getPanel(String panelId) {
				return new EntityRuleEditor(panelId, getModel(), isNew()) {
					@Override
					public void setEditionEnabled(boolean value) {
						EntityRuleMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return EntityRuleMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						EntityRuleMainPanel.this.onClose(target);
					}
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) 
							EntityRuleMainPanel.this.onClose(target);
						else { 
							setEditionEnabled(false);
							target.add(EntityRuleMainPanel.this);
						}
					}
				};
			}
		});
		
		tabs.add(new AbstractTab(getLabel("editor.state")) {
			@Override
			public Panel getPanel(String panelId) {
				return new ObjectStateEditor<ActionRule>(panelId, getModel()) {
					@Override
					protected String getLabel(ObjectState state) {
						return getLabelString(state.name());
					}
				};
			}
		});
		
		tabs.add(new AbstractTab(getLabel("editor.audit")) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<ActionRule>(panelId, getModel());
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",  this.getClass().getName(), tabs)  {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				getUser().getService(PreferencesService.class).setValue(EntityRuleMainPanel.class.getSimpleName(), "selected_tab", str);
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
			}
		};
		
		editor.setSections(VerticalLayout.COLS_9X3);
		
		editor.setTitle(new StringResourceModel("sections", this, null));
		add(editor);

		int sel = editor.getSelectedTab();
		if (sel==-1)
			sel=0;
		String str =  (editor.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}
	
	private KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
}
