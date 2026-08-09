package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.portal6.panel.PortalPanel;

public class PortalPageMainPanel extends PortalPanel<Page> {

	private static final long serialVersionUID = 1L;
	
	private AjaxTabbedPanel<ITab> tabbed_panel;

	
	public PortalPageMainPanel(String id, IModel<Page> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Label title = new Label("title", getModel().getObject().getTitle() + " <span class=\"suffix\">( " +getModel().getObject().getClassKey()+" ) </span>");
		title.setEscapeModelStrings(false);
		add(title);
		AjaxLink<Page> close = new AjaxLink<Page>("close", PortalPageMainPanel.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll (new PortalCloseEditAjaxEvent<Page>(target, PortalPageMainPanel.this.getModel()));
			}
		};
		add(close);		
		
		List<ITab> tabs=new ArrayList<>();
	        
	    tabs.add(new AbstractTab(new StringResourceModel("control", this, null)) {
		        private static final long serialVersionUID = 1L;
		        @Override
		        public Panel getPanel(String panelId) {
		        	return new PortalPageEditor(panelId, getModel());    		
		        }
	    });
		    
	    
	    
	    tabs.add(new AbstractTab(new StringResourceModel("css", this, null)) {
	        private static final long serialVersionUID = 1L;
	        @Override
	        public Panel getPanel(String panelId) {
	        	return new PortalCssEditor<Page>(panelId, getModel());    		
	        }
	    });

	    
	    
	    if (getModel().getObject().isPayloadEditor()) {
		    tabs.add(new AbstractTab(new StringResourceModel("content", this, null)) {
		        private static final long serialVersionUID = 1L;
		        @Override
		        public Panel getPanel(String panelId) {
		        	return new DummyBlockPanel(panelId);
		        	//return new PortalBlockContentEditor(panelId, getModel());
		        }
		    });
	    }



		tabbed_panel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
				private static final long serialVersionUID = 1L;
				protected String getNavCss() {
					return "nav nav-tabs";
				}
		};
		
		add(tabbed_panel);
	}


}
