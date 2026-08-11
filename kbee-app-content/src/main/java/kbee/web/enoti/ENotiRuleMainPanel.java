package kbee.web.enoti;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.enoti.ENotiRule;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;

import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

@SuppressWarnings("serial")
public class ENotiRuleMainPanel extends ObjectEditor<ENotiRule> implements PageMainTabs {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ENotiRuleMainPanel.class.getName());

	private String initial_tab;

	private VerticalLayout<ITab> editor;
	
	/**
	 * 
	 * 
	 */
	public ENotiRuleMainPanel(IModel<ENotiRule> model) {
		this("editor", model);
	}
	
	public ENotiRuleMainPanel(String id, IModel<ENotiRule> model) {
		super(id, model);
		
		setModel(model);
		setOutputMarkupId(true);
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.rule", this, null), "alert") {
			@Override
			public Panel getPanel(String panelId) {
				return new ENotiRuleEditor(panelId, getModel(), isEditionEnabled()) {
					@Override
					public void setEditionEnabled(boolean value) {
						ENotiRuleMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return ENotiRuleMainPanel.this.isEditionEnabled();
					}
				};
			}
		});
		
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.state", this, null), "status") {
			@Override
			public Panel getPanel(String panelId) {
				return new ObjectStateEditor<ENotiRule>(panelId, getModel());
			}
		});

		tabs.add(new AbstractTabKB(new StringResourceModel("editor.audit", this, null), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<ENotiRule>(panelId, getModel());
			}
		});
		
		this.editor = new VerticalLayout<ITab>("tabs",  this.getClass().getName(), tabs, VerticalLayout.VERTICAL);
		this.editor.setTitle(new StringResourceModel("sections", this, null));
		add(this.editor);
	}
	
	@Override
	public void setInitialTab(String a) {
			try {
				this.initial_tab=a;
				this.editor.setSelectedTab(a);
			} catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}

}
