package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.notification.Notification;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.portal.service.PortalUserService;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;

import kbee.web.portal6.panel.PortalPanel;

public abstract class SimpleSiteManagerTopbar extends PortalPanel<Site> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleSiteManagerTopbar.class.getName());

	private static final long serialVersionUID = 1L;
	
	IModel<Site> model;
			
	public SimpleSiteManagerTopbar(IModel<Site> model) {
		this("site-manager-topbar", model);
	}
	
	public SimpleSiteManagerTopbar(String id, IModel<Site> model) {
		super(id, model);
		setModel(model);	
		setOutputMarkupId(true);
	}

	public void onDetach() {
		super.onDetach();
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		ContextMenuPanel<Site> menu = new ContextMenuPanel<Site>(getModel());
		add(menu);
		
		menu.setOutputMarkupId(true);

		/**
		 * SITE HOME
		 */
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_HOME));
					}
					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("site-home").getObject();
					}
				};
			}
		});

		
		/**
		 * SITE PAGES
		 */
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_PAGES));
					}
					@Override
					public boolean isVisible() {
						// if the site does not have pages?
						return true;
					}
					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("site-pages").getObject();
					}
				};
			}
		});

		

		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_EDITOR));
					}
					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("site-editor").getObject();
					}
				};
			}
		});

		/**
		 * SITE ATTRIBUTES
		 */
		
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_ATTRIBUTES));
					}
					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("site-attributes").getObject();
					}
				};
			}
		});

		
		
		/**
		 * SITE CONTENTS
		 */
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_CONTENTS));
					}

					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public boolean isVisible() {
						return true;
					}

					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("site-content").getObject();
					}
				};
			}
		});

		
		/**
		 * SITE SECURITY
		 */
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_SECURITY));

					}

					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public boolean isVisible() {
						return true;
					}

					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("site-security").getObject();
					}
				};
			}
		});
		
		
		
		/**
		 * SITE REPORTS
		 */
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						fire (new SiteManagerNavigationEvent(target, getModel(), SiteAdminEvent.NAV_SITE_REPORTS));
					}

					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public boolean isVisible() {
						return true;
					}

					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("site-reports").getObject();
					}
				};
			}
		});

		
		
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return true;
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
							SimpleSiteManagerTopbar.this.close(target);
					}
					@Override
					public String getLabel() {
							return SimpleSiteManagerTopbar.this.getLabel("close").getObject();
					}
				};
			}
		});

		
		WorkingIndicatorAjaxLinkV5<Site> close= new WorkingIndicatorAjaxLinkV5<Site>("close", "close") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				SimpleSiteManagerTopbar.this.close(target);
			}
		};
		
		close.setOutputMarkupId(true);
		
		add(close);
	}

	protected abstract void close(AjaxRequestTarget target);
	
	public String getStringLabel(String key) {
		return new StringResourceModel(key, this, null).getObject();
	}
}
