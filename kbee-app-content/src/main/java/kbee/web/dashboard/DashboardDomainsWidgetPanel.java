package kbee.web.dashboard;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.domain.DomainPage;
import kbee.web.domain.DomainsPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.service.ApplicationSiteMapService;

public class DashboardDomainsWidgetPanel extends DashboardListWidgetPanel<Domain> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardDomainsWidgetPanel.class.getName());
	
	int size;
	long total;
	final boolean is_root 			= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_service_admin	= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId()));
	final boolean is_factory_admin	= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()));
	final boolean is_api			= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId()));
	final boolean is_domain_admin	= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()));

	
	public DashboardDomainsWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);
		setTitle( new StringResourceModel("domains", this, null));
	
	}

	
	protected IModel<String> getItemLabelMeta(IModel<Domain> modelObject) {
		StringBuilder str = new StringBuilder();
		try {

		//	str.append(modelObject.getObject().getDomainType().getDisplayName());
			
			
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString()	);
	}

	
	protected String getListContainerCss() {
		return "standard";
	}
	
	protected void onHelp(AjaxRequestTarget target) {
		super.toogleHelp(target);
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se=ServiceLocator.getService(InlineHelpWebService.class);
		 WebMarkupContainer  pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_PORTALS);
		 if (pa!=null)
			 return pa;
		 return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_PORTALS));
		
	}
	

	
	//@Override
	//protected String getListContainerCss() {
	//	return "twocol";
	//}
	
	@Override
	public void onInitialize() {

		setHelp(true);
		List<IModel<Domain>> list_site = new ArrayList<IModel<Domain>>();
		KbeeUser us = (KbeeUser) getSessionUser();
		us.getService(UserDashboardService.class).getDomains().forEach(item -> 
		{
			if (item.getDomainType()!=DomainType.SYSTEM)
				list_site.add( new ObjectModel<Domain>(item));
			}
		);
		size=list_site.size();
		total=list_site.size();
		
		setItems(list_site);
		
		
		super.onInitialize();
	}
	protected boolean isIconVisible() {
		return false;
	}
	
	
	protected Panel addVoidPanel(String id) {
		return new  DashboardSimpleInfoPanel("tabs",  new StringResourceModel("no-items", this,null), "fad fa-sitemap");	
	}
	
	protected boolean isMenuVisible() {
		return true;
	}
	
	
	
	@Override
	protected IModel<String> getViewingString() {
		if (total==0)
			return new Model<String>("");
		
		if (size==total)
			return new Model<String>("Total: <b>" + String.valueOf(size) +"</b>");
		
		return new Model<String>("<b>" + String.valueOf(size) +"</b> of <b>"+ String.valueOf(total) +"</b>");
	}

	protected IModel<String> getAllString() {
		return new StringResourceModel("domains",this, null);
	}

	@Override
	protected void onClick(IModel<Domain> modelObject, int index) {
		try {
			setResponsePage( new DomainPage(modelObject));
		} catch (Exception e) {
			logger.error(e);
			setResponsePage(new ApplicationErrorPage<>(e));
		}
			
	}
	
	@Override
	protected void onClickAll() {
		setResponsePage( new DomainsPage());
	}
	

	@Override
	protected Panel getMenu(IModel<Domain> model, int index) {
		
		ContextMenuPanel<Domain> menu = new ContextMenuPanel<Domain>(model);
		
		menu.setOutputMarkupId(true);
	
		
		
			
	
		
		// Open
		//
		menu.addItem(new MenuItemFactory<Domain>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Domain> getItem(String id) {
				return new AjaxMenuItemPanelV5<Domain>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						setResponsePage( new DomainPage(getModel()));
					}
					@Override 
					public String getLabel() {
						return new StringResourceModel("open", this, null).getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return is_service_admin	|| is_factory_admin	|| is_domain_admin;
					}
				};
			}
		});


		menu.addItem(new MenuItemFactory<Domain>() {
					private static final long serialVersionUID = 1L;

					@Override
					public AbstractMenuItemPanelV5<Domain> getItem(String id) {
						return new AjaxMenuItemPanelV5<Domain>(id) {
							private static final long serialVersionUID = 1L;
							public void onClick(AjaxRequestTarget target) {
								try {
									User us = ServiceLocator.getService(SecurityService.class).findUserByUsername("root@"+getModel().getObject().getName());
									ServiceLocator.getService(UserService.class).impersonate(us);
									WebPage page = ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.HomePage);
									page.getSession().setLocale(us.getLocale());
									setResponsePage(page);
								} 
								catch (Exception e) {
									logger.error(e);
									setResponsePage(new ApplicationErrorPage<>(e));
								}
							}
							
							@Override
							public boolean isEnabled() {
								return is_domain_admin || is_service_admin || is_factory_admin;
							}

							@Override
							public boolean isVisible() {
								return true;
							}
							
							@Override 
							public String getLabel() {
								return new StringResourceModel("sign-as-root",  DashboardDomainsWidgetPanel.this, null ).getObject();
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", this, null).getObject();

							}
						};
					}
				});

			 
		 

		
		
		/**
		menu.addItem(new MenuItemFactory<Domain>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Domain> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Domain>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		**/


		return menu;
	}

	
	private Boolean is_domain_kbee = null;
	
	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(
						getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} catch (Exception e) {
				logger.error(e);
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}

}
