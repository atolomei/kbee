package kbee.web.datamanagement;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.web.admin.markup.SystemInfoSearchPanel;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;

import kbee.web.editor.DomainObjectMainPanel;

@SuppressWarnings("serial")
public class ReindexMainPanel extends DomainObjectMainPanel<Domain> implements PageMainTabs {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexMainPanel.class.getName());

	private VerticalLayout<ITab> xtabs;
	
    public ReindexMainPanel(String id, IModel<Domain> model) {
        super(id, model);
        setOutputMarkupId(true);
    }
    
	@Override
    protected void onInitialize() {
        super.onInitialize();

        List<ITab> tabs=new ArrayList<>();
        
        /**
         * 
        if (isDomainKbee()) {
            tabs.add(new AbstractTabKB(new StringResourceModel("info", this, null),"info") {
                private static final long serialVersionUID = 1L;
                @Override
                public Panel getPanel(String panelId) {
                	return ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel(panelId, "search");
                }
            });
        }
        **/
        
        if (isDomainKbee()) {
	        tabs.add(new AbstractTabKB(new StringResourceModel("info", this, null), "info") {
	            @Override
	            public Panel getPanel(String panelId) {
	            	return new SystemInfoSearchPanel(panelId);
	            }
	        });
        }
        
        tabs.add(new AbstractTabKB(new StringResourceModel("contents", this, null), "content") {
            @Override
            public Panel getPanel(String panelId) {
            	return new ReindexContentPanel(panelId);
            }
        });

       tabs.add(new AbstractTabKB(new StringResourceModel("general", this, null), "general") {
            @Override
            public Panel getPanel(String panelId) {
            	return new ReindexGenericQueryPanel(panelId);
            }
        });

       tabs.add(new AbstractTabKB(new StringResourceModel("rebuild", this, null), "rebuild") {
           @Override
           public Panel getPanel(String panelId) {
           	return new ReindexRebuildPanel(panelId);
           }
       });

        tabs.add(new AbstractTabKB(new StringResourceModel("clean", this, null), "clean") {
            @Override
            public Panel getPanel(String panelId) {
            	return new ReindexCleanIndexesPanel(panelId);
            }
        });
        
       xtabs = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs);
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
