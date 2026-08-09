package kbee.web.dashboard;



import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.service.ApplicationSiteMapService;

public class DashboardWidgetAccountPanel extends DashboardWidgetBasePanel {
			
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardWidgetAccountPanel.class.getName());
	

	private WebMarkupContainer help;
	private WebMarkupContainer main_container;
	private IModel<Person> model;
	
	/**
	 * @param id
	 */
	public DashboardWidgetAccountPanel 	(String id, String preferences_key) {
		super(id, preferences_key);
		setModel(new ObjectModel<Person>(getPerson()));
		super.setTitle (new ResourceModel("mainmenu.myaccount"));
	}
	
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se=ServiceLocator.getService(InlineHelpWebService.class);
		 WebMarkupContainer  pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_ACCOUNT);
		 if (pa!=null)
			 return pa;
		 return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_ACCOUNT));
	}

	
	
		
	
	@Override
	public void onInitialize() {
			super.onInitialize();
			addAccess();	 
	}
	
	
	
	
	@Override
	public void onDetach() {
		super.onDetach();

		if (model!=null)
			model.detach();
		
	}
	
	@Override
	protected void onTitleClick() {
	}
	
	
	public IModel<Person> getModel() {
		return model;
	}

	public void setModel(IModel<Person> model) {
		this.model = model;
	}

	protected void onHelp(AjaxRequestTarget target) {
		toogleHelp(target);
	}

	/**
	protected void onHelp(AjaxRequestTarget target) {
		
		if (help==null || help instanceof InvisiblePanel) {
			help=new DummyBlockPanel("help");
			help.setVisible(false);
			file_factory.addOrReplace(help);
		}
		toogleHelp(target);
	}**/
	

	public void toogleHelp(AjaxRequestTarget target) {

		if (help==null) {
			help=getHelpPanel();
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		
		
		
		
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			main_container.get( "menuitem").setVisible(!main_container.get( "menuitem").isVisible());
			target.add(this.main_container);
		}
	}

	
	
	
	
 
	
	@SuppressWarnings("serial")
	private void addAccess() {
		
		long start=System.currentTimeMillis();
		
		setHelp(true);
		
		main_container = new WebMarkupContainer ("user-account");
		add(main_container);
		
		main_container.add(new InvisiblePanel("help"));
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>("menuitem", null);
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					public void onClick() {
						 setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-myaccount-page"));
					}
					@Override 
					public String getLabel() {
						return (new ResourceModel("mainmenu.myaccount")).getObject();
					}
				};
			}
		});
		

			
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=password"));
					}
					@Override
					public String getLabel() {
						return (new ResourceModel("password")).getObject();
					}
				};
			}

		});

		
		/**
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-notifications-page"));
					}
					@Override
					public String getLabel() {
						return (new ResourceModel("bc.user-alerts")).getObject();
					}
				};
			}

		});
		**/


		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=emailalerts"));
						
					}
					@Override
					public String getLabel() {
						return (DashboardWidgetAccountPanel.this.getLabel("my-email-alerts")).getObject();
					}
				};
			}

		});


		

			
			
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=roles"));
						
					}
					@Override
					public String getLabel() {
						
						return (DashboardWidgetAccountPanel.this.getLabel("my-roles")).getObject();
						
						
					}
				};
			}

		});

		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					public void onClick() {
						 setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("resources-mybox-page"));
					}
					@Override 
					public String getLabel() {
						return new StringResourceModel("mybox", DashboardWidgetAccountPanel.this, null).getObject();
						//return (new ResourceModel("mainmenu.myaccount")).getObject();
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public boolean isVisible() {
						return true;
					}
					@Override
					public String getCssClass() {
						return "divider";
					}
				};
			}
		});
		
		

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ServiceLocator.getService(UserService.class).logout();
						
						WebPage page = new RedirectPage("/logout");
						getPage().setResponsePage(page);
					}
					@Override
					public String getLabel() {
						return (new ResourceModel("mainmenu.exit")).getObject();
					}
				};
			}
		});

		

		main_container.setOutputMarkupId(true);
		main_container.add(menu);
		
		
		long end=System.currentTimeMillis();
		logger.debug("FileFactory -> " + String.valueOf(end-start)+ " ms");
	}


	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}
}
