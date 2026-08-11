package kbee.web.objectstorage;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AbstractLinkMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.HREFMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.service.ApplicationSiteMapService;

/**
 * 
 *  <p>Info (like Dashboard)
 *  
 *  <ul>
  	<li>{@code <bean id="ObjectStorageDomainMoveCommand" class="com.novamens.kbee.content.command.ObjectStorageDomainMoveCommand"/>}</li>
	<li>{@code <bean id="ObjectStorageDomainDeleteCommand" class="kbee.objectstorage.command.ObjectStorageDomainDeleteCommand"/>}</li>
	<li>{@code <bean id="ObjectStorageDomainEncryptCommand" class="kbee.objectstorage.command.ObjectStorageDomainEncryptCommand"/>}</li>
	</ul>
	</p>
	
	

 *
 */
public class ObjectStorageMainPanel extends DomainObjectMainPanel<Domain> implements PageMainTabs  {

	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStorageMainPanel.class.getName());

	VerticalLayout<ITab> xtabs;
	
    public ObjectStorageMainPanel(String id, IModel<Domain> model) {
        super(id, model);
        setOutputMarkupId(true);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();

        List<ITab> tabs=new ArrayList<>();
        
        tabs.add(new AbstractTabKB(new StringResourceModel("info", ObjectStorageMainPanel.this, null),"info") {
            private static final long serialVersionUID = 1L;
            @Override
            public Panel getPanel(String panelId) {
            	return ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel(panelId, "object-storage");
            }
        });

        if (isDomainKbee() && isRoot()) {
        
        	tabs.add(new AbstractTabKB(new StringResourceModel("migrate", ObjectStorageMainPanel.this, null), "migrate") {
	            private static final long serialVersionUID = 1L;
	            @Override
	            public Panel getPanel(String panelId) {
	            	try {
	            	return new ObjectStorageMoveDomainObjects(panelId);
	            	} catch (Exception e) {
	            		logger.error(e);
	            		return new ErrorPanel(panelId,e);
	            	}
	            }
	        });
	       
	        
	        tabs.add(new AbstractTabKB(new StringResourceModel("encrypt", ObjectStorageMainPanel.this, null), "encrypt") {
	            private static final long serialVersionUID = 1L;
	            @Override
	            public Panel getPanel(String panelId) {
	            	return new ObjectStorageEncryptDomainObjects(panelId);
	            }
	        });
	        
	        
        	tabs.add(new AbstractTabKB(new StringResourceModel("delete", ObjectStorageMainPanel.this, null), "delete") {
	            private static final long serialVersionUID = 1L;
	            @Override
	            public Panel getPanel(String panelId) {
	            	return new ObjectStorageDeleteDomainObjects(panelId);
	            }
	        });
        	
        															
        	tabs.add(new AbstractTabKB(new StringResourceModel("purge", ObjectStorageMainPanel.this, null), "purge") {
	            private static final long serialVersionUID = 1L;
	            @Override
	            public Panel getPanel(String panelId) {
	            	return new ObjectStoragePurgeDomainObjects(panelId);
	            }
	        });

        	 
        	
       
        }

    
        
        
       xtabs = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
    	   
		private static final long serialVersionUID = 1L;
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
				((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue( ObjectStorageMainPanel.class.getName(), "selectedtab", getSelectedTab());
				
			}

       };
        
       xtabs.setTitle(new StringResourceModel("sections", this, null));
       xtabs.setContentBottomPanel(new InvisiblePanel("content-bottom-panel"));
       add(xtabs);

    }
    
	private String initial_tab;
	
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
