package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.object.ObjectStateEditor;


public class PortalPageEditorMainPanel extends DomainObjectMainPanel<Page> implements PageMainTabs {
		
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalPageEditorMainPanel.class.getName());
	
    private String initial_tab;
    private  VerticalLayout<ITab> xtabs;

	public PortalPageEditorMainPanel(String id, IModel<Page> model) {
		super(id, model);
	}
	
    @Override
    protected void onInitialize() {
        super.onInitialize();

        //add (new ApplicationPageHeader("application-page-header", new StringResourceModel("objectstorage", this, null), new DataManagementPanelBC("reindex")));

        List<ITab> tabs=new ArrayList<>();


        tabs.add(new AbstractTabKB(new StringResourceModel("info", this, null),"info") {
            private static final long serialVersionUID = 1L;
            @Override
            public Panel getPanel(String panelId) {
            	return new PortalPageEditor(panelId,  getModel());
            }
        });


        
       tabs.add(new AbstractTabKB(new StringResourceModel("structure", this, null),"structure") {
            private static final long serialVersionUID = 1L;
            @Override
            public Panel getPanel(String panelId) {
            	PortalPageStructurePanel panel = new PortalPageStructurePanel(panelId, getModel()); 
            	return panel;
            	// Page page= getModel().getObject();
            	// if (page!=null) {
        		// 	return page.getService(PortalObjectViewerRenderService.class).build("panel");
        		// }
        		// return new ErrorPanel(panelId, new Model<String>("not found"));
            }
       });

        
    
    tabs.add(new AbstractTabKB(new StringResourceModel("status", this, null),"status") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new ObjectStateEditor<Page>(panelId, getModel());
        	//return ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel(panelId, "object-storage");
        }
    });

    
    
    
    tabs.add(new AbstractTabKB(new StringResourceModel("audit", this, null), "audit") {
		private static final long serialVersionUID = 1L;
		@Override
		public Panel getPanel(String panelId) {
        	return new DummyBlockPanel(panelId);
			//return new AuditTrailObjectPanel<Role>(panelId, getModel());
		}
	});
	
    
    
    
    
    
    
    
    
    
    xtabs = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
 	   
  		private static final long serialVersionUID = 1L;
  			@Override
  			protected void onAjaxUpdate(AjaxRequestTarget target) {
  				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
  				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
  				//((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue( ObjectStorageMainPanel.class.getName(), "selectedtab", getSelectedTab());
  				
  			}

         };
         
         
         //xtabs.setExpandDown(false);
          
         xtabs.setTitle(new StringResourceModel("sections", this, null));
         xtabs.setContentBottomPanel(new InvisiblePanel("content-bottom-panel"));
         add(xtabs);
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
