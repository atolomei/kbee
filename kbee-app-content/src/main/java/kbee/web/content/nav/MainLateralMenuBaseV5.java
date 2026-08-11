package kbee.web.content.nav;

import java.util.ArrayList;
import java.util.List;

import kbee.web.payment.PaymentsConsolePage;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonSet;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.web.console.audit.markup.AuditContentPage;
import com.novamens.content.web.console.markup.AuditActivityPage;
import com.novamens.content.web.console.markup.AuditEmailPage;
import com.novamens.content.web.console.markup.DashboardPage;
import com.novamens.content.web.integration.FileSystemIntegrationPage;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.util.PropertiesFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SideMenuPanel;
import com.novamens.wicket.markup.html.actions.SubmenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.alert.BillboardsPage;
import kbee.web.content.console.AuditResourcesPage;
import kbee.web.content.console.MyDocumentsPage;
import kbee.web.content.console.MyResourcesPage;
import kbee.web.content.console.PublicResourcesPage;
import kbee.web.content.console.WorkspacePage;
import kbee.web.dataset.DashboardDataSetMembersHomePage;

import kbee.web.dataset.DataSetMembersPage;
import kbee.web.emailtemplate.EmailTemplatesPage;
import kbee.web.enoti.ENotiRulesPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.library.LibrariesPage;
import kbee.web.model.DashboardInformationModelPage;
import kbee.web.multidimensional.FacetsPage;
import kbee.web.notification.UserNotificationsPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.report.ReportFactory;
import kbee.web.rule.ActionRulesPage;
import kbee.web.security.role.RolesPage;
import kbee.web.security.user.MyAccountPage;
import kbee.web.security.user.UsersPage;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.service.PortalPanelService;
import kbee.web.service.ReportsLibraryService;
import kbee.web.source.SourcesPage;
import kbee.web.util.NavigationEvent;

@SuppressWarnings("serial")
public class MainLateralMenuBaseV5 extends SideMenuPanel<Void> {
    private static final long serialVersionUID = 1L;

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MainLateralMenuBaseV5.class.getName());

    public String FA_PREFIX;

    static final protected String ICON_SUPPORT = "far fa-notes-medical";
    static final protected String ICON_HOME = "fa-home";
    static final protected String ICON_MYWORK = "fa-house-laptop";

    static final protected String ICON_TASK = "list-bullets-3";
    static final protected String ICON_LIBRARY = "bank-1";
    static final protected String ICON_PORTAL = "organization-hierarchy-3";
    static final protected String ICON_SETTINGS = "cog-gear-settings";
    static final protected String ICON_SECURITY = "key-1";
    static final protected String ICON_DATA_MANAGEMENT = "wrench";
    static final protected String ICON_REPORTS = "business-graph-bar-increase";
    static final protected String ICON_AUDIT = "content-modules";
    static final protected String ICON_PAYMENTS = "payments";
    static final protected String ICON_INTEGRATION = "content-view-module-2";
    static final protected String ICON_ALERT_SETTINGS = "clock"; // "fal fa-mail-bulk";
    static final protected String ICON_USER_MESSAGES = "email-envelope"; // "fal fa-mailbox";
    static final protected String ICON_INFO = "interface-information";
    static final protected String ICON_DRAFT_FOLDER = "fa-folder";

    final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
    final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

    // tasks
    final boolean role_tasks_mytasks = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE.getId());

    final boolean role_tasks_auditor = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
    final boolean role_tasks_dashboard = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DASHBOARD.getId());
    final boolean role_tasks_pending = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
    final boolean role_federated_security = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.FEDERATED_SECURITY.getId());
    final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
    final boolean role_library = role_admin || getDomain().getService(LibraryService.class).readables();
    final boolean role_model = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
    final boolean role_model_read = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MODEL_READ.getId());
    final boolean role_settings = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SETTINGS.getId());
    final boolean role_dataset_members_read = role_model || role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId())
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
    final boolean role_federated_values = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.FEDERATED_VALUES.getId());
    final boolean role_security = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
    final boolean role_reports = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.REPORTS.getId());
    final boolean role_file_server = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.FILE_SERVER.getId());
    final boolean role_auditor = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.AUDITOR.getId());
 
    final boolean role_work_notes = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.BILLBOARDS.getId());
    final boolean role_archive = role_admin
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.ARCHIVE.getId());

    protected boolean menu_settings = true;
    protected boolean role_useradmin = ServiceLocator.getService(UserService.class).isUserAdmin();
    
	private static String MyDocumentsEnabled =
			PropertiesFactory
				.getInstance("kbee")
				.getProperties()
				.getProperty("kbee.user.mydocuments.enabled", "true");

    private Boolean is_kbee_domain = null;

    private String application_section_menu = "";

    List<IModel<Library>> libraries;
    IModel<DataSet> person_dataset_model;

    public MainLateralMenuBaseV5(String id, String applicationMenuSection) {
        super(id, null);
        this.application_section_menu = applicationMenuSection;
    }

    public String getApplicationSectionMenu() {
        return application_section_menu;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        FA_PREFIX = "fa-fw " + getPerson().getProfile(UserProfile.class).getIconSet();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (libraries != null)
            libraries.forEach(item -> item.detach());

        if (this.person_dataset_model != null)
            this.person_dataset_model.detach();

    }

    /***
     * HOME
     */
    protected void addHomeMenu() {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.HOME.getKey()) ? " selected" : "";

        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new AjaxMenuItemPanelV5<Void>(id, FA_PREFIX + " " + ICON_HOME + css_selected) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            fireNavigationEvent();
    						setResponsePage( new RedirectPage("/myhome"));
                        } 
                        catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }
                    @Override
                    public String getLabel() {
                        return getMenuLabel("home");
                    }
                    @Override
                    public String getIcon() {
                        return ICON_HOME;
                    }
                    @Override
                    public String getUrl() {
                        return "/myhome";
                    }
                };
            }
        });
    }

    private AbstractMenuItemPanelV5<Void> getMyWorkspaceSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.MYWORK.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " " + ICON_MYWORK + " " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mywork");
            }

            @Override
            public String getIcon() {
                return ICON_MYWORK;
            }
        };

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            fireNavigationEvent();
                            getPage().setResponsePage(new WorkspacePage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.tasks.mytasks");
                    }

                    @Override
                    public String getUrl() {
                        return "/mytasks";
                    }
                };
            }
        });

        if ("true".equals(MyDocumentsEnabled)) {
	        menu.addItem(new MenuItemFactory<Void>() {
	            @Override
	            public AbstractMenuItemPanelV5<Void> getItem(String id) {
	                return new LinkMenuItemPanel<Void>(id) {
	                    @Override
	                    public void onClick() {
	                        try {
	                            fireNavigationEvent();
	                            getPage().setResponsePage(new MyDocumentsPage());
	                        } catch (Exception e) {
	                            logger.error(e);
	                            setResponsePage(new ApplicationErrorPage<Void>(e));
	                        }
	                    }
	                    @Override
	                    public String getLabel() {
	                        return getMenuLabel("mydocuments");
	                    }
	                    @Override
	                    public String getUrl() {
	                        return "/mydocuments";
	                    }
	                };
	            }
	        });
        }

        return menu;
    }

    /***
     * MyWork
     */
    protected void addMyWorkMenu() {

        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getMyWorkspaceSubmenu(id);
            };
        });

    }

    /***
     * SUPPORT
     */
    protected void addSupportMenu() {
        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.SUPPORT.getKey()) ? " selected" : "";
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id, FA_PREFIX + " fa-notes-medical " + css_selected) {
                    @Override
                    public void onClick() {
                        try {
                            setResponsePage(new DashboardPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("support");
                    }

                    @Override
                    public String getIcon() {
                        return ICON_SUPPORT; // "places-home-1";
                    }

                    @Override
                    public String getUrl() {
                        return "/support";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });
    }

    /***
     * SETTINGS
     */
    protected void addSettingsMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getSettingsSubmenu(id);
            };
        });
    }

    /***
     * DRAFT
     */
    protected void addDraftResourcesMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getDraftResourcesSubMenu(id);
            };
        });
    }

    /**
     * REPORTS
     */
    protected void addReportsMenu() {

        if (hasReports()) {
            addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return getRepoSubmenu(id);
                };
            });
        }
    }

    /***
     * USER MESSAGES
     */
    protected void addUSerMessagesMenu() {
        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.USER_MESSAGES.getKey()) ? " selected" : "";
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id, FA_PREFIX + " fa-mailbox " + css_selected) {
                    @Override
                    public void onClick() {
                        try {
                            setResponsePage(new UserNotificationsPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mymessages");
                    }

                    @Override
                    public String getIcon() {
                        return ICON_USER_MESSAGES;
                    }

                    @Override
                    public String getUrl() {
                        return "/mynotifications";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });
    }

    /**
     * 
     * @param id
     * @return
     */
    private AbstractMenuItemPanelV5<Void> getRepoSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.REPORTS.getKey()) ? " selected" : "";

        long start = System.currentTimeMillis();

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + "  fa-chart-line " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.reports");
            }

            @Override
            public String getIcon() {
                return ICON_REPORTS;
            }
        };

        // Subscriptions
        //
        if (getUserSessionReportSchedules().size() > 0) {

            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new LinkMenuItemPanel<Void>(id) {
                        @Override
                        public void onClick() {
                            if (getUserSessionReports() == null || getUserSessionReports().isEmpty())
                                setResponsePage(new ApplicationErrorPage<Object>(new Model<String>("Not authorized"),
                                        new Model<String>("Your account has no subscriptions enabled.")));
                            else
                                getPage().setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class)
                                        .getPage("reports-subscriptions-page"));
                        }

                        @Override
                        public String getLabel() {
                            return new StringResourceModel("bc.reportsubscription", this, null).getString();
                        }

                        @Override
                        public String getUrl() {
                            return "/reports/subscriptions";
                        }

                        @Override
                        public boolean isVisible() {
                            return (isAdmin() || role_support || role_reports) && hasReports();
                        }

                        @Override
                        public String getBeforeClick() {
                            return "if (typeof submit === \"function\") { submit(); }";
                        }
                    };
                }
            });
        }
        logger.debug("Report menu duration: " + String.valueOf(System.currentTimeMillis() - start) + " ms");
        return menu;
    }

    /***
     * 
     */
    protected AbstractMenuItemPanelV5<Void> getDraftResourcesSubMenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.DRAFTRESOURCES.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " " + ICON_DRAFT_FOLDER + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("draft-folder");
            }

            @Override
            public String getIcon() {
                return ICON_DRAFT_FOLDER;
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        };

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            setResponsePage(new MyResourcesPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("my-draft-folder");
                    }

                    @Override
                    public String getUrl() {
                        return "/mydrafts";

                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            setResponsePage(new PublicResourcesPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("draft-folder-public");
                    }

                    @Override
                    public String getUrl() {
                        return "/publicdrafts";

                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        return menu;

    }

    /**
     * 
     * 
     * 
     * @param id
     * @return
     */
    private AbstractMenuItemPanelV5<Void> getSettingsSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.SETTINGS.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-cog " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.settings");
            }

            @Override
            public String getIcon() {
                return ICON_SETTINGS;
            }

            @Override
            public boolean isVisible() {
                return is_root 
                		|| role_admin 
                		|| role_dataset_members_read 
                		|| role_support 
                		|| role_settings 
                		|| role_model
                        || role_model_read
                		|| role_federated_values;
            }
        };

        if (!isKbeeDomain()) {

            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new LinkMenuItemPanel<Void>(id) {
                        @Override
                        public void onClick() {
                            try {
                                getPage().setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class)
                                        .getPage("settings-dataset-members-home-page"));
                            } catch (Exception e) {
                                logger.error(e);
                                setResponsePage(new ApplicationErrorPage<Void>(e));
                            }
                        }
                        @Override
                        public String getLabel() {
                            return getMenuLabel("mainmenu.datasetvalues");
                        }
                        @Override
                        public String getUrl() {
                            return "/datasetmembers";
                        }
                        @Override
                        public String getBeforeClick() {
                            return "if (typeof submit === \"function\") { submit(); }";
                        }
                        @Override
                        public boolean isVisible() {
                            if (isKbeeDomain())
                                return false;
                            if (is_root)
                                return true;
                            return role_admin || role_dataset_members_read || role_support || role_federated_values;
                        }
                    };
                }
            });

            if (role_admin || is_root || role_model || role_model_read) {
                menu.addItem(new MenuItemFactory<Void>() {
                    @Override
                    public AbstractMenuItemPanelV5<Void> getItem(String id) {
                        return new LinkMenuItemPanel<Void>(id) {
                            @Override
                            public void onClick() {
                                try {
                                    getPage().setResponsePage(new DashboardInformationModelPage());
                                } catch (Exception e) {
                                    logger.error(e);
                                    setResponsePage(new ApplicationErrorPage<Void>(e));
                                }
                            }

                            @Override
                            public String getLabel() {
                                return getMenuLabel("bc.informationmodel");
                            }

                            @Override
                            public String getUrl() {
                                return "/model";
                            }

                            @Override
                            public String getBeforeClick() {
                                return "if (typeof submit === \"function\") { submit(); }";
                            }

                            @Override
                            public boolean isVisible() {

                                if (isKbeeDomain())
                                    return false;

                                return role_admin || is_root || role_model || role_model_read;
                            }
                        };
                    }
                });
            }
        

            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new LinkMenuItemPanel<Void>(id) {
                        @Override
                        public void onClick() {
                            try {
                                getPage().setResponsePage(new EmailTemplatesPage());
                            } catch (Exception e) {
                                logger.error(e);
                                setResponsePage(new ApplicationErrorPage<Void>(e));
                            }
                        }

                        @Override
                        public String getLabel() {
                            return getMenuLabel("mainmenu.email-templates");
                        }

                        @Override
                        public String getUrl() {
                            return "/emailtemplates";
                        }

                        @Override
                        public String getBeforeClick() {
                            return "if (typeof submit === \"function\") { submit(); }";
                        }

                        @Override
                        public boolean isVisible() {

                            return role_admin || role_support || role_settings;
                        }
                    };
                }
            });

            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new LinkMenuItemPanel<Void>(id) {
                        @Override
                        public void onClick() {
                            try {
                                getPage().setResponsePage(new LibrariesPage());
                            } catch (Exception e) {
                                logger.error(e);
                                setResponsePage(new ApplicationErrorPage<Void>(e));
                            }
                        }

                        @Override
                        public String getLabel() {
                            return getMenuLabel("mainmenu.domain.cabinets");
                        }

                        @Override
                        public String getUrl() {
                            return "/libraries";
                        }

                        @Override
                        public String getBeforeClick() {
                            return "if (typeof submit === \"function\") { submit(); }";
                        }

                        @Override
                        public boolean isVisible() {
                            if (isKbeeDomain())
                                return false;
                            return role_admin;
                        }
                    };
                }
            });

        
        }

        
        
        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            PageParameters parameters = new PageParameters();
                            parameters.add("id", getDomain().getId());
                            getPage().setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class)
                                    .getPage("settings-generalsettings-page", parameters));
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.domain.settings");
                    }

                    @Override
                    public String getUrl() {
                        return "/domain/" + getDomain().getId() + "/settings";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }

                    @Override
                    public boolean isVisible() {
                        return role_admin || role_support || role_settings;
                    }
                };
            }
        });


        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new SourcesPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.domain.sources");
                    }

                    @Override
                    public String getUrl() {
                        return "/sources";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }

                    @Override
                    public boolean isVisible() {
                        return role_admin;
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new FacetsPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.domain.facets");
                    }

                    @Override
                    public String getUrl() {
                        return "/facets";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }

                    @Override
                    public boolean isVisible() {
                        if (isKbeeDomain())
                            return false;
                        return role_admin;
                    }
                };
            }
        });

        return menu;
    }

    /**
     * 
     * MEMBERS
     * 
     * private AbstractMenuItemPanelV5<Void> getMembersSubmenu(String id) {
     * 
     * SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id) {
     * 
     * @Override public String getLabel() { return
     *           getMenuLabel("mainmenu.datasetvalues"); }
     * @Override public boolean isVisible() { return role_dataset_members_read ||
     *           role_support; } };
     * 
     *           if (!(role_dataset_members_read || role_support)) return menu;
     * 
     *           for (DataSet dataset: getContentDao().getDataSets(getDomain())) {
     *           if (dataset.getDataSetType().equals(DataSetType.STRING) ||
     *           dataset.getDataSetType().equals(DataSetType.EXTERNAL) ||
     *           dataset.getDataSetType().equals(DataSetType.SECURED) ||
     *           dataset.getDataSetType().equals(DataSetType.ENTITY) ||
     *           dataset.getDataSetType().equals(DataSetType.LABEL) ||
     *           dataset.getDataSetType().equals(DataSetType.USERSUBSET)) { final
     *           String datasetname = dataset.getName(); final String datasetalias =
     *           dataset.getAlias(); final String datasetid =
     *           String.valueOf(dataset.getId()); menu.addItem(new
     *           MenuItemFactory<Void>() {
     * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
     *           new AjaxMenuItemPanelV5<Void>(id) {
     * @Override public void onClick(AjaxRequestTarget target) { DataSet dataset =
     *           getDataSet(datasetid); if (dataset!=null) { try {
     *           getPage().setResponsePage(new DataSetMembersPage(new
     *           ObjectModel<DataSet>(dataset))); } catch (Exception e) {
     *           logger.error(e); setResponsePage(new ErrorPage<Void>(e)); } } }
     * @Override public String getLabel() { return datasetname; }
     * @Override public String getUrl() { return
     *           "/dataset/"+(datasetalias!=null?datasetalias.toLowerCase():datasetid);
     *           } }; } }); } }; return menu; }
     */

    /**
     * MODEL
     */
//	private AbstractMenuItemPanelV5<Void> getModelSubmenu(String id) {
//		
//		SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id) {
//			@Override
//			public String getLabel() {
//				return getMenuLabel("mainmenu.model");
//			}
//			@Override
//			public boolean isVisible() {
//				return role_model || role_support;
//			}
//		};
//		
//		menu.addItem(new MenuItemFactory<Void>() {
//			@Override
//			public AbstractMenuItemPanelV5<Void> getItem(String id) {
//				return new LinkMenuItemPanel<Void>(id) {
//					@Override
//					public void onClick() {
//						try {
//							getPage().setResponsePage(new DataSetsPage<DataSet>());
//						} 
//						catch (Exception e) {
//							logger.error(e);
//							setResponsePage(new ErrorPage<Void>(e));
//						}
//					}
//					@Override
//					public String getLabel() {
//						return getMenuLabel("mainmenu.model.datasets");
//					}
//					@Override
//					public String getUrl() {
//						return "/model/datasets"; 
//					}
//					@Override
//					public String getBeforeClick() {
//						return "if (typeof submit === \"function\") { submit(); }";
//					}
//				};
//			}
//		});
//		
//		menu.addItem(new MenuItemFactory<Void>() {
//			@Override
//			public AbstractMenuItemPanelV5<Void> getItem(String id) {
//				return new LinkMenuItemPanel<Void>(id) {
//					@Override
//					public void onClick() {
//						try {
//							getPage().setResponsePage(new ClassifiersPage());
//						} 
//						catch (Exception e) {
//							logger.error(e);
//							setResponsePage(new ErrorPage<Void>(e));
//						}
//					}
//					@Override
//					public String getLabel() {
//						return getMenuLabel("mainmenu.model.classifiers");
//					}
//					@Override
//					public String getUrl() {
//						return "/model/classifiers"; 
//					}
//					@Override
//					public String getBeforeClick() {
//						return "if (typeof submit === \"function\") { submit(); }";
//					}
//				};
//			}
//		});
//		
//		menu.addItem(new MenuItemFactory<Void>() {
//			@Override
//			public AbstractMenuItemPanelV5<Void> getItem(String id) {
//				return new LinkMenuItemPanel<Void>(id) {
//					@Override
//					public void onClick() {
//						try {
//							getPage().setResponsePage(new AttributesPage());
//						} 
//						catch (Exception e) {
//							logger.error(e);
//							setResponsePage(new ErrorPage<Void>(e));
//						}
//					}
//					@Override
//					public String getLabel() {
//						return getMenuLabel("mainmenu.model.attributes");
//					}
//					@Override
//					public String getUrl() {
//						return "/model/attributes"; 
//					}
//					@Override
//					public String getBeforeClick() {
//						return "if (typeof submit === \"function\") { submit(); }";
//					}
//				};
//			}
//		});
//		
//		menu.addItem(new MenuItemFactory<Void>() {
//			@Override
//			public AbstractMenuItemPanelV5<Void> getItem(String id) {
//				return new LinkMenuItemPanel<Void>(id) {
//					@Override
//					public void onClick() {
//						try {
//							getPage().setResponsePage(new ContentClassesPage());
//						} 
//						catch (Exception e) {
//							logger.error(e);
//							setResponsePage(new ErrorPage<Void>(e));
//						}
//					}
//					@Override
//					public String getLabel() {
//						return getMenuLabel("mainmenu.model.classes");
//					}
//					@Override
//					public String getUrl() {
//						return "/model/contentclasses"; 
//					}
//					@Override
//					public String getBeforeClick() {
//						return "if (typeof submit === \"function\") { submit(); }";
//					}
//				};
//			}
//		});
//		return menu;
//	};

    /***
     * INTEGRATION
     * 
     */
    protected void addIntegrationMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getIntegrationSubmenu(id);
            };
        });
    }

    private AbstractMenuItemPanelV5<Void> getIntegrationSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.INTEGRATION.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-folder-tree " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.integration");
            }

            @Override
            public String getIcon() {
                return ICON_INTEGRATION; // "content-view-module-2";
            }

            @Override
            public boolean isVisible() {
                return role_admin || is_root || role_support || role_file_server;
            }
        };

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new FileSystemIntegrationPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.file-server");
                    }

                    @Override
                    public String getUrl() {
                        return "/fileserver";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }

                    @Override
                    public boolean isVisible() {
                        return role_file_server || role_admin || is_root || role_support;
                    }
                };
            }
        });

        return menu;
    }

    /***
     * SECURITY
     * 
     */
    protected void addSecurityMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getSecuritySubmenu(id);
            };
        });
        return;
    }

    private AbstractMenuItemPanelV5<Void> getSecuritySubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.SECURITY.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-key " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.security");
            }

            @Override
            public String getIcon() {
                return ICON_SECURITY; // "key-1";
            }

            @Override
            public boolean isVisible() {
                return role_security || role_useradmin || role_federated_security || isUserAdmin();
            }
        };

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new MyAccountPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.myaccount");

                    }

                    @Override
                    public String getUrl() {
                        return "/myaccount";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        if (!role_security && !role_useradmin && !role_federated_security && !isUserAdmin()) {
            return menu;
        }    

        if (isExpressVersion()) {

            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new LinkMenuItemPanel<Void>(id) {
                        @Override
                        public void onClick() {
                            try {
                                if (getPersonDataSet() != null)
                                    getPage().setResponsePage(new DataSetMembersPage(new ObjectModel<DataSet>(getPersonDataSet())));
                                else
                                    getPage().setResponsePage(new DashboardDataSetMembersHomePage());

                            } catch (Exception e) {
                                logger.error(e);
                                setResponsePage(new ApplicationErrorPage<Void>(e));
                            }
                        }

                        @Override
                        public String getLabel() {
                            return getMenuLabel("person");
                        }

                        @Override
                        public String getUrl() {
                            return getPersonDataSet() != null ? "/dataset/" + getPersonDataSet().getId().toString()
                                    : "/datasetmembers/";
                        }

                        @Override
                        public String getBeforeClick() {
                            return "if (typeof submit === \"function\") { submit(); }";
                        }
                    };
                }
            });

        }

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new UsersPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.security.users");
                    }

                    @Override
                    public String getUrl() {
                        return "/security/users";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        if (!role_security)
            return menu;

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new RolesPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.security.roles");
                    }

                    @Override
                    public String getUrl() {
                        return "/security/roles";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        return menu;
    }

    /***
     * AUDIT
     */
    protected void addAuditMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getAuditSubmenu(id);
            };
        });
    }

    /***
     * PAYMENT
     */
    protected void addPaymentMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getPaymentSubmenu(id);
            };
        });
    }

    private AbstractMenuItemPanelV5<Void> getPaymentSubmenu(String id) {
        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.PAYMENTS.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-credit-card " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.payments");
            }

            @Override
            public String getIcon() {
                return ICON_PAYMENTS;
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        };

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new PaymentsConsolePage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.payments.payments");
                    }

                    @Override
                    public String getUrl() {
                        return "/payments";
                    }
                };
            }
        });

        return menu;
    }

    private AbstractMenuItemPanelV5<Void> getAuditSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.LOGS.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-archive " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.systemlogs");
            }

            @Override
            public String getIcon() {
                return ICON_AUDIT;
            }

            @Override
            public boolean isVisible() {
                return role_admin || role_support || role_auditor;
            }
        };

        if (!(role_admin || role_support || role_auditor))
            return menu;

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new AuditActivityPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.systemlogs.activity");
                    }

                    @Override
                    public String getUrl() {
                        return "/logs/activity";
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new AuditContentPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.systemlogs.content");
                    }

                    @Override
                    public String getUrl() {
                        return "/logs/content";
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new AuditEmailPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.systemlogs.email");
                    }

                    @Override
                    public String getUrl() {
                        return "/logs/email";
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new AuditResourcesPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.systemlogs.explorer");
                    }

                    @Override
                    public String getUrl() {
                        return "/logs/resources";
                    }
                };
            }
        });

        // if (TREE_RESOURCES.equals("yes")) {
//			menu.addItem(new MenuItemFactory<Void>() {
//				@Override
//				public AbstractMenuItemPanelV5<Void> getItem(String id) {
//					return new LinkMenuItemPanel<Void>(id) {
//						@Override
//						public void onClick() {
//							try {
//								getPage().setResponsePage(new AuditTreeFileResourcesPage());
//							} 
//							catch (Exception e) {
//								logger.error(e);
//								setResponsePage(new ErrorPage<Void>(e));
//							}
//						}
//						@Override
//						public String getLabel() {					
//							return getMenuLabel("mainmenu.systemlogs.treeexplorer");
//						}
//					};
//				}
//			});
        // }

        // if (IS_API.equals("yes")) {
        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class)
                                    .getPage("factory-api-requests-report-page"));
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.systemlogs.api");
                    }

                    @Override
                    public String getUrl() {
                        return "/api/reports/requests";
                    }
                };
            }
        });
        // }

        return menu;
    }

    protected boolean isExpressVersion() {
        return getDomain().getDomainType() == DomainType.EXPRESS;
    }
    
    protected boolean isUserAdmin() {
        return role_federated_values &&
        	ServiceLocator
        		.getService(UserService.class)
        		.isAdmin(getContentDao().getUserSet());
    }


    protected boolean isAdmin() {
        return ServiceLocator
        	.getService(SecurityService.class)
        	.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    }

    protected boolean isRoot() {
        return ServiceLocator.getService(SecurityService.class).isRoot();
    }

    protected Domain getDomain() {
        return ServiceLocator.getService(UserService.class).getDomain();
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    @Override
    protected String getUitheme() {
        return "brand-" + ((KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser()).getUitheme();
    }

    protected boolean isKbeeDomain() {
        if (this.is_kbee_domain == null) {
            try {
                this.is_kbee_domain = Boolean.valueOf(getDomain().getName().toLowerCase().trim().equals("kbee"));
            } catch (Exception e) {
                logger.error(e);
                this.is_kbee_domain = Boolean.valueOf(false);
            }
        }
        return this.is_kbee_domain.booleanValue();
    }

    protected boolean hasReports() {
        return ServiceLocator.getService(ReportsLibraryService.class).hasReports(getDomain());
    }

    protected List<IModel<Library>> getLibraries() {

        if (libraries != null)
            return libraries;

        libraries = new ArrayList<IModel<Library>>();
        for (Library library : getDomain().getService(LibraryService.class).getLibraries(ObjectState.ENABLED, "listOrder")) {
            if (library.isReadable()) {
                libraries.add(new ObjectModel<Library>(library));
            }
        }

        return libraries;
    }

    private Boolean is_library_enabled = null;

    protected DataSet getDataSet(String id) {
        for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
            if (id.equals(String.valueOf(dataset.getId()))) {
                return dataset;
            }
            ;
        }
        return null;
    }

    protected String getMenuLabel(String key) {
        return (new StringResourceModel(key, this, null)).getObject();
    }

    protected boolean isLibraryEnabled() {
        if (this.is_library_enabled == null)
            this.is_library_enabled = Boolean.valueOf(getDomain().isPortalLibrary());
        return this.is_library_enabled.booleanValue();
    }

    protected DataSet getSelectedDataSet() {
        DataSet dataset;
        String did = ((KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue("dataset-member-selected",
                "dataset");
        if (did == null)
            return null;
        dataset = (DataSet) getContentDao().findModelObjectById(DataSet.class, did);
        return dataset;
    }

    public List<DataSet> getDataSets() {
        List<DataSet> datasetlist = new ArrayList<DataSet>();
        for (DataSet dataset : getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
            if (dataset.getDataSetType() == DataSetType.STRING || dataset.getDataSetType() == DataSetType.EXTERNAL
                    || dataset.getDataSetType() == DataSetType.ENTITY || dataset.getDataSetType() == DataSetType.LABEL
                    || dataset.getDataSetType() == DataSetType.PEOPLE)
                datasetlist.add(dataset);
        }
        return datasetlist;
    }

    protected User getSessionUser() {
        return ServiceLocator.getService(SecurityService.class).getSessionUser();
    }

    protected List<ReportFactory> getUserSessionReports() {
        return ServiceLocator.getService(ReportsLibraryService.class).getUserSessionReports();
    }

    protected List<ReportExportSchedule> getUserSessionReportSchedules() {
        return ServiceLocator.getService(ReportsLibraryService.class).getUserDomainReportExportSchedules();
    }

    protected void addAlertSettingsMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getAlertSettingsSubmenu(id);
            };
        });
    }

    protected AbstractMenuItemPanelV5<Void> getAlertSettingsSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.ALERT_SETTINGS.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-mail-bulk " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("alert-settings");
            }

            @Override
            public String getIcon() {
                return ICON_ALERT_SETTINGS;
            }
        };

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new BillboardsPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("billboards");
                    }

                    @Override
                    public String getUrl() {
                        return "/billboards";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new ENotiRulesPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("file-alerts");
                    }

                    @Override
                    public String getUrl() {
                        return "/emailnotifications";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }

                    public boolean isVisible() {
                        if (isKbeeDomain())
                            return false;
                        return true;
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        try {
                            getPage().setResponsePage(new ActionRulesPage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("time-based-alerts");
                    }

                    @Override
                    public boolean isVisible() {
                        if (isKbeeDomain())
                            return false;
                        return is_root || role_admin || role_support;
                    }

                    @Override
                    public String getUrl() {
                        return "/actionrules";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        return menu;
    }
    
	public WebPage getStartPage(UserProfile profile) {
		 return ServiceLocator.getService(PortalPanelService.class).getStartPage(profile);
	}

    protected void fireNavigationEvent() {
        Page page = getPage();
        if (page instanceof AbstractKbeeWebPage) {
            ((AbstractKbeeWebPage) page).fireScanAll(new NavigationEvent());
        }
    }

    protected DataSet getPersonDataSet() {

        if (person_dataset_model != null)
            return person_dataset_model.getObject();
        for (DataSet ds : getContentDao().getDataSets(getDomain().getId())) {
            if (ds instanceof PersonSet) {
                person_dataset_model = new ObjectModel<DataSet>(ds);
                return person_dataset_model.getObject();
            }
        }
        return null;
    }

    protected Person getPerson() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
    }
}
