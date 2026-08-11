package com.novamens.content.web.admin.markup.datamanagement;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.content.web.admin.markup.ActionsPanel;
import com.novamens.content.web.admin.markup.ConfigFilesInfoPanel;
import com.novamens.content.web.admin.markup.XAjaxLink;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.Tuple;
import kbee.web.nav.DataManagementBC;
				
public class SystemDataManagementPanel extends ModelPanel<Object> {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemDataManagementPanel.class.getName());

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());

	private IModel<Domain> domain_model;
	
	private List<Tuple> cacheThumbnailInfo() {
		List<Tuple> data = new ArrayList<Tuple>();
		ThumbnailService ths = ServiceLocator.getService(ThumbnailService.class);
		data.add(new Tuple("Thumbnail Server. Cache Hits", String.valueOf(ths.getCacheHits())));
		data.add(new Tuple("Thumbnail Server. Cache Miss", String.valueOf(ths.getCacheMiss())));
		return data;
	}		

	
	/**
	 * System Information
	 * 
	 * Service Management
	 * -----------------
	 * Data Management
	 * Scheduler
	 * Commands
	 * File Server
	 * 
	 * @param id
	 */
	public SystemDataManagementPanel(String id) {
		super(id);

		setOutputMarkupId(true);
	
		 MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
	 	 bc.addElement(new DataManagementBC());
		 add(bc);
		
		List<ITab> tabs = new ArrayList<ITab>();

		// Command Bean ------------------------------------------------------------------------------------------------
		//
		tabs.add(new AbstractTab(new StringResourceModel("command-bean", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new CommandBeanPanel(panelId);
			}
			@Override
			public boolean isVisible() {
				return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
			}
		});

		
		// Test ------------------------------------------------------------------------------------------------
				//
				tabs.add(new AbstractTab(new StringResourceModel("test", this, null)) {
					private static final long serialVersionUID = 1L;
					@Override
					public Panel getPanel(String panelId) {
						return new CommandBeanPanel(panelId);
					}
					@Override
					public boolean isVisible() {
						return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
					}
				});
				
				
		// Reindex Command ----------------------------------------------------------------------------------------------
		//
		tabs.add(new AbstractTab(new StringResourceModel("reindexcommand", this, null)) {
					private static final long serialVersionUID = 1L;
					@Override
					public Panel getPanel(String panelId) {
						return new ReindexCommandPanel(panelId);
					}
					@Override
					public boolean isVisible() {
						return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
					}
					
		});
		
		// Command ------------------------------------------------------------------------------------------------------
		//
		tabs.add(new AbstractTab(new StringResourceModel("command", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new CommandPanel2(panelId);
			}
			@Override
			public boolean isVisible() {
				return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
			}
		});

		// File Server  ------------------------------------------------------------------------------------------------
		//
		/*
		tabs.add(new AbstractTab(new StringResourceModel("file-server-command", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new FileServerCommandPanel(panelId);
			}
			@Override
			public boolean isVisible() {
				return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
			}
		});
		*/

 		
		// Reindex ------------------------------------------------------------------------------------------------------
		//
		tabs.add(new AbstractTab(new StringResourceModel("reindex", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new ReindexPanel(panelId);
			}
			@Override
			public boolean isVisible() {
				return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
			}
		});
		
		tabs.add(new AbstractTab(new StringResourceModel("config", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				AreaInfoPanel area = new AreaInfoPanel(panelId);
				area.setSections(AreaInfoPanel.ONE_SECTION);
				area.setCss("col-lg-12");
				area.addPanel(new ConfigFilesInfoPanel("element"));
				return area;
			}
		});

		// --------------------------------------------------------------------------------------------------
		//
		//
		tabs.add(new AbstractTab(new StringResourceModel("cache", this, null)) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Panel getPanel(String panelId) {
				AreaInfoPanel area = new AreaInfoPanel(panelId); 
				area.addPanel(new GridInfoPanel("element", cacheThumbnailInfo(), new Model<String>("Thumbnail Server")));
			
				ActionsPanel actions = new ActionsPanel("actions", new Model<String>("Actions"));
				
				XAjaxLink x4 = new XAjaxLink( new Model<String>("Clean Hibernate Cache")) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						
						ServiceLocator.getService(com.novamens.event.EventService.class).fire(new com.novamens.kbee.event.EvictCacheServiceEvent());
						
						// Cleah Hibernate Cache
						getContentDao().cleanHibernateCache();

						 try {
								Thread.sleep(600);
							} catch (InterruptedException e) {
						}
						target.add(SystemDataManagementPanel.this);
					}
				};
				actions.add(x4);

				XAjaxLink x5 = new XAjaxLink( new Model<String>("Reset Thumbnail Server")) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						try {

							logger.info("Remove all Thumbnail Server " + getDomain().getName());
							ServiceLocator.getService(ThumbnailService.class).removeAll();
							
							try {
								Thread.sleep(800);
							} catch (InterruptedException e) {
							}
							
							
						} catch (Exception e) {
							logger.error(e);
						}
						target.add(SystemDataManagementPanel.this.getParent());
					}
				};
				
				actions.add(x5);
				
				// Reset File Server Metadata
				// Reset UI Contexts
				// Reset Grid Columns
				//
				area.setActionsPanel(actions);
				
				return area;
			}
		});

		
		// Export
		//
		tabs.add(new AbstractTab(new StringResourceModel("export", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new ExportPanel2(panelId);
			}
			@Override
			public boolean isVisible() {
				return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
			}
		});

		/*
		// LDAP
		//
		tabs.add(new AbstractTab(new StringResourceModel("ldap", this, null)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Panel getPanel(String panelId) {
				return new GridInfoPanel(panelId, cacheThumbnailInfo());
			}
			
			@Override
			public boolean isVisible() {
				return is_domain_admin || is_service || ServiceLocator.getService(SecurityService.class).isRoot();
			}
		});
		*/

		VerticalLayout<ITab> sysinfo = new VerticalLayout<ITab>("tabs", "factory", tabs, VerticalLayout.VERTICAL) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getTabPanelContainerCss() {
				return "col-lg-10 col-md-9 col-xs-12 stacked";
			}
			protected String getNavCss() {
				return "nav nav-pills nav-stacked col-lg-2 col-md-3 col-xs-12";
			}
		};
			
		sysinfo.setTitle(new StringResourceModel("sections", this, null));
		add(sysinfo);
	}
	
	
	@Override
	public void onDetach() {
		if (domain_model!=null)
			domain_model.detach();
		super.onDetach();
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//
//	private Domain getDomain() {
//		if (domain_model==null)
//			domain_model= new ObjectModel<Domain>(ServiceLocator.getService(UserService.class).getDomain());
//		return domain_model.getObject();
//	}
	
	
	


	

}
