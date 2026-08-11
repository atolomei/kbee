package kbee.web.datamanagement;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;

import kbee.web.editor.DomainObjectMainPanel;


/**
 * 
 * 
 *
 */
public class SchedulerRequestMainPanel extends DomainObjectMainPanel<Domain> implements PageMainTabs {
			
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexMainPanel.class.getName());

	private String initial_tab;
	
	private VerticalLayout<ITab> xtabs;
	

	public SchedulerRequestMainPanel(String id, IModel<Domain> model) {
        super(id, model);
        setOutputMarkupId(true);
    }
    
    /**
     *  [CronJob] / Standard]
     *  [ServiceRequest]
     *   Description
     *  [Parameters]
     *  [Submit]
     */
    @Override
    protected void onInitialize() {
        super.onInitialize();

        // add (new ApplicationPageHeader("application-page-header", new StringResourceModel("execute-request", this, null), new DataManagementPanelBC("reindex")));

        List<ITab> tabs=new ArrayList<>();
        
        tabs.add(new AbstractTabKB(new StringResourceModel("execute-request", this, null), "request") {
            private static final long serialVersionUID = 1L;
            @Override
            public Panel getPanel(String panelId) {
            	return new SchedulerRequestExecutePanel(panelId);
            }
        });


       xtabs = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs);
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
