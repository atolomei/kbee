package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Classifier;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

/**
 * ABM Launcher
 * 
 * L1 -> [remove] 
 * L2 -> [edit]
 * L3 -> [add]
 * 
 * role editor
 * 
 * -----------------------------------------
 * 
 * Libraries
 * Procedure On Start set attributes
 * ContentTemplates ->
 * DataSet ->
 * Macro Subtitles
 * Role Conditions
 * Search Filters
 * ENotiRule
 * ----------------------------------------- 
 */
@SuppressWarnings("serial")
public class ClassifierMainPanel extends DomainObjectEditor<Classifier>  implements PageMainTabs  {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassifierMainPanel.class.getName());

	private String initial_tab;
	
	public ClassifierMainPanel(IModel<Classifier> model, final boolean editon, final boolean is_new) {
		super("editor", model);
		
		setIsNew(is_new);
		setModel(model);

		List<ITab> tabs = new ArrayList<ITab>();
		
		// Info
		//
		tabs.add(new AbstractTabKB(getLabel("editor.info"), "info") {
			@Override
			public Panel getPanel(String panelId) {
				return new ClassifierEditor(panelId, getModel(), is_new) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						((ClassifierHeaderPanel)ClassifierMainPanel.this.get("dataset-panel")).onUpdate(target);
					}
					protected void onClose(AjaxRequestTarget target) {
						ClassifierMainPanel.this.onClose(target);							
					}
				};
			}
		});

		tabs.add(new AbstractTabKB(getLabel("editor.state"), "status") {
			@Override
			public Panel getPanel(String panelId) {
				boolean real_only= isExpressVersion() && !isRoot();
				return new ObjectStateEditor<Classifier>(panelId, getModel(), real_only);
			}
		});

		tabs.add(new AbstractTabKB(getLabel("editor.grids"), "grids") {
			@Override
			public Panel getPanel(String panelId) {
				ElementGridEditor<Classifier> eg = new ElementGridEditor<Classifier>(panelId, getModel(), editon) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						((ClassifierHeaderPanel)ClassifierMainPanel.this.get("dataset-panel")).onUpdate(target);
					}
				};
				eg.setInfo(getInfo());
				return eg;
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("editor.references"), "references") {
			@Override
			public Panel getPanel(String panelId) {
				return new ReferencesPanel<Classifier>(panelId, getModel());
			}
		});
		
		// 3. Operational type
		// -------------------
		// Standard   
		// Internal (for Rules for example)
		//
		tabs.add(new AbstractTabKB(new StringResourceModel("editor.audit", this, null), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Classifier>(panelId, new ObjectModel<Classifier>(getModel().getObject()));
			}
		});
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				try {
					String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
					((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
				} 
				catch (RuntimeException e) {
					logger.error(e);
				}
			}	
		};
		
		editor.setTitle(new StringResourceModel("sections", this, null));
		add(editor);
	}
	
	protected IModel<String> getInfo() {
		return getLabel("grid-info");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		@SuppressWarnings("unchecked")
		VerticalLayout<ITab> tabs = (VerticalLayout<ITab>) get("tabs");
		tabs.setTitle(new StringResourceModel("sections", this, null));
	}

	protected void onClose(AjaxRequestTarget target) {
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setInitialTab(String a) {
		try {
			initial_tab=a;
			((VerticalLayout<ITab>) get("tabs")).setSelectedTab(a);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
}