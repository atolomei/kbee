package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.editor.DomainObjectMainPanel;

/**
 * 
 * 
 * Home
 * About
 * Contact
 * Other
 * 
 *
 */
public class SearcherSettingsMainPanel extends DomainObjectMainPanel<Domain> {
			
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherSettingsMainPanel.class.getName());
	
	public SearcherSettingsMainPanel(String id, IModel<Domain> model) {
		super(id, model);
	}
	
	public void onInitialize() {
		super.onInitialize();

		add(new InvisiblePanel("title-panel"));
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("home", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				//return new ObjectStateEditor<T>(panelId, getModel(),  real_only);
				return new InvisiblePanel(panelId);
			}
		});
		
		
		tabs.add(new AbstractTab(new StringResourceModel("about", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				//return new ObjectStateEditor<T>(panelId, getModel(),  real_only);
				return new InvisiblePanel(panelId);
			}
		});
		
		
		tabs.add(new AbstractTab(new StringResourceModel("contact", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				//return new ObjectStateEditor<T>(panelId, getModel(),  real_only);
				return new InvisiblePanel(panelId);
			}
		});
		
	}

}
