package kbee.web.content.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.model.ModelPanel;


import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;

@SuppressWarnings("serial")
public class SharePanel<T extends Content> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;
	
	public SharePanel(String id) {
		this(id, null);
	}
	
	public SharePanel(String id, IModel<T> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(getLabel("email")) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new SendByEmailPanel<T>(panelId, getModel()); 
			}
		});

		tabs.add(new AbstractTab(getLabel("link")) {
			@Override
			public Panel getPanel(String panelId) {
				return new PublicLinkPanel<T>(panelId, getModel()); 
			}
		});

		
		tabs.add(new AbstractTab(getLabel("encrypted")) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new SendPasswordProtectedPanel<T>(panelId, getModel()); 
			}
		});
		
		
		
		AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
			protected String getNavCss() {
				return "nav nav-tabs";
			}
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				SharePanel.this.onUpdate(target);
			}
		};
		
		addOrReplace(tabbedpanel);
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	@SuppressWarnings("unchecked")
	public int getPanelIndex() {
		return ((AjaxTabbedPanel<ITab>)get("tabs")).getSelectedTab();
	}
	
	@SuppressWarnings("unchecked")
	public Panel getPanel() {
		return (Panel)((AjaxTabbedPanel<ITab>)get("tabs")).getTab(getPanelIndex());
	}

	
}
