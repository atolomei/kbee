package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.LauncherGroup;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

@SuppressWarnings("serial")
public class LauncherGroupMainPanel extends DomainObjectEditor<LauncherGroup> implements PageMainTabs  {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(LauncherGroupMainPanel.class.getName());

	private String initial_tab;
	
	public LauncherGroupMainPanel(IModel<LauncherGroup> model, final boolean editon, final boolean is_new) {
		super("editor", model);
		
		setIsNew(is_new);
		setModel(model);

		List<ITab> tabs = new ArrayList<ITab>();
		
		// Info
		//
		tabs.add(new AbstractTabKB(getLabel("editor.info"), "info") {
			@Override
			public Panel getPanel(String panelId) {
				return new LauncherGroupEditor(panelId, getModel(), is_new) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						//((ClassifierHeaderPanel)LauncherGroupMainPanel.this.get("dataset-panel")).onUpdate(target);
					}
					protected void onClose(AjaxRequestTarget target) {
						LauncherGroupMainPanel.this.onClose(target);							
					}
				};
			}
		});

		tabs.add(new AbstractTabKB(getLabel("editor.state"), "status") {
			@Override
			public Panel getPanel(String panelId) {
				boolean real_only= isExpressVersion() && !isRoot();
				return new ObjectStateEditor<LauncherGroup>(panelId, getModel(), real_only);
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("editor.audit"), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<LauncherGroup>(panelId, new ObjectModel<LauncherGroup>(getModel().getObject()));
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
		editor.setTitle(getLabel("menu"));
		add(editor);
	}

	
	protected IModel<String> getInfo() {
		return new StringResourceModel("grid-info", this, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onInitialize() {
		super.onInitialize();
		VerticalLayout<ITab> tabs = (VerticalLayout<ITab>) get("tabs");
		tabs.setTitle(getLabel("menu"));
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