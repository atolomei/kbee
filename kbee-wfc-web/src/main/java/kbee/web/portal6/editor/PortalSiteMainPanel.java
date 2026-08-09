package kbee.web.portal6.editor;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemWithModelPanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.SimpleMenuPanel;
import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.object.ObjectStateEditor;
import kbee.web.portal6.PortalObjectDataProviderService;
import kbee.web.portal6.event.PortalEditEvent;
import kbee.web.searcher.editor.SearcherSiteIqlEditor;
import kbee.web.service.PortalPanelService;


/**
 * 
 *
 */
public class PortalSiteMainPanel extends DomainObjectMainPanel<Site> implements PageMainTabs {
			
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSiteMainPanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	   
    private String initial_tab;
    private VerticalLayout<ITab> xtabs;
    		
    public PortalSiteMainPanel(String id, IModel<Site> model) {
    	super(id, model);
    }
    
    @Override
    protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<PortalEditEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalEditEvent<PortalObject> event) {
						if (event.getModel().getObject() instanceof Page)
							setResponsePage(new PortalPageStructureEditorPage( new ObjectModel<Page>( (Page) event.getModel().getObject())));
						else if (event.getModel().getObject() instanceof PortalPersistentMenu) {
							logger.debug("menu");
						}
			}
		});
    }
		
    @Override
    protected void onInitialize() {
        super.onInitialize();

    List<ITab> tabs=new ArrayList<>();
        
    tabs.add(new AbstractTabKB(new StringResourceModel("pages", this, null),"pages") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new PortalSitePagesPanel(panelId, getModel());    		
        }
    });
    
    tabs.add(new AbstractTabKB(new StringResourceModel("info", this, null),"info") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new PortalSiteEditor(panelId, getModel());
        }
    });

    if (getModel().getObject().getSiteType()==SiteType.LIBRARY) {
	    tabs.add(new AbstractTabKB(new StringResourceModel("iql", this, null),"iql") {
	        private static final long serialVersionUID = 1L;
	        @Override
	        public Panel getPanel(String panelId) {
	        	return new SearcherSiteIqlEditor(panelId, getModel());

	        }
	    });
    }


    if (getModel().getObject().isPayloadEditor()) {
	    tabs.add(new AbstractTab(new StringResourceModel("content", this, null)) {
	        private static final long serialVersionUID = 1L;
	        @Override
	        public Panel getPanel(String panelId) {
	        	return getModel().getObject().getService(PortalObjectDataProviderService.class).getDataProviderEditor(panelId);	
	        }
	    });
    }

    
    tabs.add(new AbstractTabKB(new StringResourceModel("status", this, null),"status") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new ObjectStateEditor<Site>(panelId, getModel());
        }
    });

    /**
    tabs.add(new AbstractTabKB(new StringResourceModel("topsection", this, null),"topsection") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        		return new DummyBlockPanel(panelId);    		
        }
    });
    **/
    
    /**
    tabs.add(new AbstractTabKB(new StringResourceModel("bottomsection", this, null),"bottomsection") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        		return new DummyBlockPanel(panelId);    		
        }
    });
    **/

    
    tabs.add(new AbstractTabKB(new StringResourceModel("menus", this, null),"menus") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new  PortalSiteMenusPanel(panelId, getModel());
        }
    });
    
    
    
    
    
  /**  
    tabs.add(new AbstractTabKB(new StringResourceModel("portal", this, null),"portal") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new PortalSiteMenusPanel(panelId, getModel());
			WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(getModel().getObject());
			setResponsePage(page);
        }
    });
    
    */
    
    
    
/**
    
    tabs.add(new AbstractTabKB(new StringResourceModel("security", this, null),"security") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new SimpleSiteContentsEditor(panelId, getModel());
        	//return ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel(panelId, "object-storage");
        }
    });

    
    tabs.add(new AbstractTabKB(new StringResourceModel("reports", this, null),"reports") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	Site site= getModel().getObject();
    		Page page=site.getHomePage();
    		if (page!=null) {
    			return page.getService(PortalObjectViewerRenderService.class).build("panel");
    		}
    		return new ErrorPanel(panelId, new Model<String>("not found"));


        }
    });
    
    
    tabs.add(new AbstractTabKB(new StringResourceModel("integrations", this, null),"integrations") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new DummyBlockPanel(panelId);
        	//return ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel(panelId, "object-storage");
        }
    });
    

    tabs.add(new AbstractTabKB(new StringResourceModel("search", this, null),"search") {
        private static final long serialVersionUID = 1L;
        @Override
        public Panel getPanel(String panelId) {
        	return new DummyBlockPanel(panelId);
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
	
   */
    
    
    
    
    xtabs = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL) {
 	   
  		private static final long serialVersionUID = 1L;
  			@Override
  			protected void onAjaxUpdate(AjaxRequestTarget target) {
  				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
  				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
  			}
         };
    
         
         xtabs.setSections(VerticalLayout.COLS_9X3);
    


         /**
          *          ContextMenuPanel<Site> m=new ContextMenuPanel<Site>("header-bottom-panel", getModel());
     	 
			new MenuItemFactory<Site>() {
				private static final long serialVersionUID = 1L;
				@Override
				public AbstractMenuItemPanelV5<Site> getItem(String id) {
				
					return new MenuItemWithModelPanel<Site>(id,  PortalSiteMainPanel .this.getModel() ) {
						@Override
						public void onClick() {
							try {
								WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(getModel().getObject());
								
								setResponsePage(page);
							} catch (Exception e) {
								logger.error(e);
								setResponsePage(new ErrorPage(e));
							}
						}
						
						@Override
						public String getLabel() {
							return   getModel().getObject().getTitle();
						}
						
						
						public String getTarget() {
							return "_blank";
						}
					};
				}
			});

          */

        
     	
         List <MenuItemFactory<Site>>  menuitems =  new ArrayList<MenuItemFactory<Site>>();
         menuitems.add(
        		 new MenuItemFactory<Site>() {
     				private static final long serialVersionUID = 1L;
     				@Override
     				public AbstractMenuItemPanelV5<Site> getItem(String id) {
     					return new MenuItemWithModelPanel<Site>(id,  PortalSiteMainPanel .this.getModel() ) {
     						@Override
     						public void onClick() {
     							try {
     								WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(getModel().getObject());
     								setResponsePage(page);
     							} catch (Exception e) {
     								logger.error(e);
     								setResponsePage(new ApplicationErrorPage(e));
     							}
     						}
     						
     						@Override
     						public String getLabel() {
     							return   new StringResourceModel("open-portal", PortalSiteMainPanel.this, null).getObject();
     						}
     						
     						
     						public String getTarget() {
     							return "_blank";
     						}
     					};
     				}
     			}
        );
         
         
        SimpleMenuPanel<Site, Site> panel = new SimpleMenuPanel<Site, Site>("header-bottom-panel", getModel(), menuitems);
        panel.setTitle(new StringResourceModel("actions"));
        xtabs.setHeaderBottomPanel(panel);

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
