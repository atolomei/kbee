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
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;

import kbee.web.portal6.PortalObjectDataProviderService;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalPanel;

public class PortalBlockMainPanel extends PortalPanel<Block> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalBlockMainPanel.class.getName());
   
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AjaxTabbedPanel<ITab> tabbed_panel;
    
	public PortalBlockMainPanel(String id, IModel<Block> model) {
		super(id, model);
		setOutputMarkupId(true);
		
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Label title = new Label("title", getModel().getObject().getTitle() + " <span class=\"suffix\">( " +getModel().getObject().getClassKey()+" ) </span>");
		title.setEscapeModelStrings(false);
		add(title);


		AjaxLink<Block> close = new AjaxLink<Block>("close", PortalBlockMainPanel.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll (new PortalCloseEditAjaxEvent<Block>(target,PortalBlockMainPanel.this.getModel()));
			}
		};
		add(close);
		
		
			List<ITab> tabs=new ArrayList<>();

		    if (getModel().getObject().isPayloadEditor()) {
			    tabs.add(new AbstractTab(new StringResourceModel("content", this, null)) {
			        private static final long serialVersionUID = 1L;
			        @Override
			        public Panel getPanel(String panelId) {
			        	try {
			        		return getModel().getObject().getService(PortalObjectDataProviderService.class).getDataProviderEditor(panelId);
			        	} catch (Exception e) {
			        		logger.error(e);
			        		return new PortalErrorPanel<Block>(panelId,e);
			        	}
			        	
			        }
			    });
		    }
		    
		    tabs.add(new AbstractTab(new StringResourceModel("control", this, null)) {
		        private static final long serialVersionUID = 1L;
		        @Override
		        public Panel getPanel(String panelId) {
		        	return new PortalBlockEditor(panelId, getModel());    		
		        }
		    });
		    

		    
		    tabs.add(new AbstractTab(new StringResourceModel("css", this, null)) {
		        private static final long serialVersionUID = 1L;
		        @Override
		        public Panel getPanel(String panelId) {
		        	return new PortalCssEditor<Block>(panelId, getModel());    		
		        }
		    });

		    
		    
			this.tabbed_panel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
				private static final long serialVersionUID = 1L;
				protected String getNavCss() {
					return "nav nav-tabs";
				}
			};
			
			add(tabbed_panel);
	}
	
			
}
