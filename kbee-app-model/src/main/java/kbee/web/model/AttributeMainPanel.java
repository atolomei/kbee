package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Attribute;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

@SuppressWarnings("serial")
public class AttributeMainPanel extends DomainObjectEditor<Attribute> implements PageMainTabs {
	private static final long serialVersionUID = 1L;
										
	private static Logger logger = Logger.getLogger(AttributeMainPanel.class.getName());
	private String initial_tab;

	public AttributeMainPanel(IModel<Attribute> model, final boolean editon, final boolean is_new) {
		super("editor", model);
		
		setIsNew(is_new);
		setModel(model);

		List<ITab> tabs = new ArrayList<ITab>();
		
		// Info
		tabs.add(new AbstractTab(getLabel("editor.info")) {
			@Override
			public Panel getPanel(String panelId) {
				return new AttributeModelEditor(panelId, getModel(), is_new) {
					protected void onClose(AjaxRequestTarget target) {
						AttributeMainPanel.this.onClose(target);							
					}
				};
			}
		});
		
		tabs.add(new AbstractTab(getLabel("editor.state")) {
			@Override
			public Panel getPanel(String panelId) {
				boolean read_only= isExpressVersion() && !isRoot();
				return new ObjectStateEditor<Attribute>(panelId, getModel(), read_only);
			}
		});

		// Grids
		tabs.add(new AbstractTab(getLabel("editor.grids")) {
			@Override
			public Panel getPanel(String panelId) {
				return new ElementGridEditor<Attribute>(panelId, getModel(), editon) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						// ((AttributeHeaderPanel)AttributeMainPanel.this.get("dataset-panel")).onUpdate(target);
					}
				};
			}
		});
		
		tabs.add(new AbstractTab(getLabel("used-by")) {
			@Override
			public Panel getPanel(String panelId) {
				return new ReferencesPanel<Attribute>(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.audit", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Attribute>(panelId, new ObjectModel<Attribute>(getModel().getObject()));
			}
		});
		
		
		VerticalLayout<ITab> xtabs = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				try {
					String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
					((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
					
				} catch (Exception e) {
					logger.error(e);
				}
			}
		};
		
		xtabs.setTitle(new StringResourceModel("sections", this, null));
		add(xtabs);
	}

	protected void onClose(AjaxRequestTarget target) {
	}
	
	@Override
	@SuppressWarnings("unchecked")
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
