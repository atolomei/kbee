package kbee.web.content.nav;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;

import com.novamens.beans.BeansService;
import com.novamens.content.library.LibraryService;

import com.novamens.content.web.admin.api.APIRequestsReportPage;
import com.novamens.content.web.admin.markup.SystemInfoGeneralPage;
import com.novamens.content.web.admin.markup.SystemInfoPage;
import com.novamens.content.web.admin.markup.SystemParametersPage;
import com.novamens.content.web.admin.markup.SystemParametersPanel;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemSchedulerMonitorPage;
import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SubmenuItemPanelV5;

import kbee.web.command.panel.CommandsPage;
import kbee.web.dashboard.DashboardFactoryHomePage;
import kbee.web.datamanagement.ReindexPage;
import kbee.web.datamanagement.ThumbnailServicePage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.objectstorage.ObjectStoragePage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.report.ReportFactory;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.service.ReportsLibraryService;

/**
 * <p>
 * Main Lateral Menu for the Factory tool
 * </p>
 *
 *
 */
@SuppressWarnings("serial")
public class MainLateralMenuFactoryV5 extends MainLateralMenuBaseV5 {

    private static final long serialVersionUID = 1L;

    static final protected String ICON_DOMAINS = "building-7";

    static final protected String ICON_API = "location-globe";

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MainLateralMenuContentV5.class.getName());

    public MainLateralMenuFactoryV5(String id, String applicationMenuSection) {
        super(id, applicationMenuSection);
    }

    // tasks
    //
    final boolean role_tasks_mytasks = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE.getId());
    final boolean role_tasks_auditor = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
    final boolean role_tasks_dashboard = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DASHBOARD.getId());
    final boolean role_tasks_pending = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());

    //
    //
    final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
    final boolean role_library = isAdmin() || getDomain().getService(LibraryService.class).readables();
    final boolean role_model = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
    final boolean role_dataset_members_read = role_model || isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId())
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
    final boolean role_security = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
    final boolean role_reports = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.REPORTS.getId());
    final boolean role_file_server = ServiceLocator.getService(com.novamens.service.SecurityService.class)
            .isMember(KbeeGlobalRole.FILE_SERVER.getId());
    final boolean role_portals = isAdmin()
            || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PORTAL_ADMIN.getId());

    final boolean role_work_notes = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.BILLBOARDS.getId());
    final boolean role_archive = isAdmin()
            || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.ARCHIVE.getId());

    private String application_section_menu = "";

    @Override
    public void onInitialize() {
        super.onInitialize();
        addFactoryMenu();
    }

    public String getApplicationSectionMenu() {
        return application_section_menu;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void addFactoryMenu() {

        boolean is_service = ServiceLocator.getService(com.novamens.service.SecurityService.class)
                .isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());

        addHomeMenu();
        // addMyWorkMenu();

        // Domains
        //
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getDomainsSubmenu(id); // Domains
            };
        });

        // addDraftResourcesMenu();
        // addUSerMessagesMenu();

        // Data Management
        //
        if (isAdmin() || is_service) {
            addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return getDataManagementSubmenu(id);
                };
            });
        }

        // Info
        //
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getInfoSubmenu(id); // Info
            };
        });

        if (role_support && !(isAdmin() || isRoot() || is_service))
            return;

        // API
        //
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getManagement_APISubmenu(id);
            };
        });

        addSettingsMenu();

        if (isAdmin() || is_service)
            addAlertSettingsMenu();

        if (isAdmin() || is_service)
            addSecurityMenu();

        addIntegrationMenu();
        // addReportsMenu();
        addAuditMenu();
        // addPaymentMenu();
    }

    protected List<ReportFactory> getUserSessionReports() {
        return ServiceLocator.getService(ReportsLibraryService.class).getUserSessionReports();
    }

    protected List<ReportExportSchedule> getUserSessionReportSchedules() {
        return ServiceLocator.getService(ReportsLibraryService.class).getUserDomainReportExportSchedules();
    }

    /***
     * HOME
     */

    protected void addHomeMenu() {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.HOME.getKey()) ? " selected" : "";

        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id, FA_PREFIX + " fa-home " + css_selected) {
                    @Override
                    public void onClick() {
                        try {
                            setResponsePage(new DashboardFactoryHomePage());
                        } catch (Exception e) {
                            logger.error(e);
                            setResponsePage(new ApplicationErrorPage<Void>(e));
                        }
                    }

                    @Override
                    public String getLabel() {
                        return "Home";
                    }

                    @Override
                    public String getIcon() {
                        return ICON_HOME; // "places-home-1";
                    }

                    @Override
                    public String getUrl() {
                        return "/factoryhome";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });
    }

    @SuppressWarnings("unused")
    private void addPortalMenu() {
        addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return getPortalSubmenu(id);
            };
        });
    }

    /**
     * -------------------------------------------------------
     * 
     * 
     *
     * DATA MANAGEMENT
     * 
     * 
     * 
     * @param id
     * @return
     */
    private AbstractMenuItemPanelV5<Void> getDataManagementSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.DATA_MANAGEMENT.getKey()) ? " selected"
                : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-database " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.datamanagement");
            }

            @Override
            public String getIcon() {
                return ICON_DATA_MANAGEMENT;
            }
        };

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
            @Override
            public void onClick() {
                getPage().setResponsePage(new SystemInfoGeneralPage("database"));
            }

            @Override
            public String getLabel() {
                return getMenuLabel("database");
            }

            @Override
            public String getUrl() {
                return "/systeminfo/database";
            }

            @Override
            public String getBeforeClick() {
                return "if (typeof submit === \"function\") { submit(); }";
            }
        });

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
            @Override
            public void onClick() {
                getPage().setResponsePage(new ObjectStoragePage());
            }

            @Override
            public String getLabel() {
                return getMenuLabel("objectstorage");
            }

            @Override
            public String getUrl() {
                return "/datamanagement/objectstorage";
            }
            // @Override
            // public String getBeforeClick() {
            // return "if (typeof submit === \"function\") { submit(); }";
            // }
        });

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
            @Override
            public void onClick() {
                getPage().setResponsePage(new ReindexPage());
            }

            @Override
            public String getLabel() {
                return getMenuLabel("search-platform");
            }

            @Override
            public String getUrl() {
                return "/datamanagement/reindex";
            }

            @Override
            public String getBeforeClick() {
                return "if (typeof submit === \"function\") { submit(); }";
            }
        });

        /**
         * menu.addItem(new MenuItemFactory<Void>() {
         * 
         * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
         *           new LinkMenuItemPanel<Void>(id) {
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemInfoGeneralPage("search")); }
         * @Override public String getLabel() { return getMenuLabel("search-platform");
         *           }
         * 
         * @Override public String getUrl() { return "/systeminfo/search"; }
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } }; } });
         **/

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
            @Override
            public void onClick() {
                getPage().setResponsePage(new SystemSchedulerMonitorPage());
            }

            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.scheduler");
            }

            @Override
            public String getUrl() {
                return "/datamanagement/scheduler";
            }

            @Override
            public String getBeforeClick() {
                return "if (typeof submit === \"function\") { submit(); }";
            }
        });

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
            @Override
            public void onClick() {
                getPage().setResponsePage(new CommandsPage());
            }

            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.commands");
            }

            @Override
            public String getUrl() {
                return "/commands";
            }

            @Override
            public String getBeforeClick() {
                return "if (typeof submit === \"function\") { submit(); }";
            }
        });

        /**
         * menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
         * 
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemDataManagementGeneralPage("sql-gateway")); }
         * @Override public String getLabel() { return "Database Gateway (root)"; }
         * @Override public String getUrl() { return "/datamanagement/sql-gateway"; }
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; }
         * @Override public boolean isVisible() { return isRoot(); } } );
         **/

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
            @Override
            public void onClick() {
                getPage().setResponsePage(new ThumbnailServicePage());
            }

            @Override
            public String getLabel() {
                return getMenuLabel("cache");
            }

            @Override
            public String getUrl() {
                return "/datamanagement/cache";
            }
        });

        /**
         * menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
         * 
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemDataManagementGeneralPage("commands")); }
         * 
         * @Override public String getLabel() { return
         *           getMenuLabel("mainmenu.datamanagement.deprecated"); }
         * 
         * @Override public String getUrl() { return "/datamanagement/commands"; }
         * 
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } });
         **/

        /**
         * menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
         * 
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemDataManagementGeneralPage("file-explorer")); }
         * @Override public String getLabel() { return
         *           getMenuLabel("mainmenu.file-explorer"); }
         * @Override public String getUrl() { return "/datamanagement/file-explorer"; }
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } } );
         **/

        /**
         * menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
         * 
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemDataManagementGeneralPage("deploy")); }
         * 
         * @Override public String getLabel() { return
         *           getMenuLabel("mainmenu.deploy-management"); }
         * 
         * @Override public String getUrl() { return "/datamanagement/deploy"; }
         * 
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } });
         **/
        return menu;
    }

    /**
     * PORTAL
     */
    private AbstractMenuItemPanelV5<Void> getPortalSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.SITES.getKey()) ? " selected" : "";

        /*
         * addItem(new MenuItemFactory<Void>() {
         * 
         * @Override public MenuItemPanel<Void> getItem(String id) { return new
         * AjaxMenuItemPanelV5<Void>(id, "fa-fw fad fa-sitemap " + css_selected) {
         * 
         * @Override public void onClick(AjaxRequestTarget target) { WebPage page =
         * (WebPage) ServiceLocator.getService(BeansService.class).getBean("sitesPage");
         * if (page!=null) getPage().setResponsePage(page); else
         * logger.error("page is null"); }
         * 
         * @Override public String getLabel() { return
         * getMenuLabel("mainmenu.portal.sites"); }
         * 
         * @Override public String getBeforeClick() { return
         * "if (typeof submit === \"function\") { submit(); }"; }
         * 
         * @Override public boolean isVisible() { return isAdmin() || role_support; } };
         * } });
         */

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-sitemap " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.portal");
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        };

        /**
         * Site Directory
         */
        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        WebPage page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("sitesPage");
                        if (page != null)
                            getPage().setResponsePage(page);
                        else
                            logger.error("page is null");

                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.portal.sites");
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }

                    @Override
                    public boolean isVisible() {
                        return true;
                    }
                };
            }
        });

        return menu;
    }

    /**
     * FACTORY. DOMAIN MANAGEMENT
     * 
     * @param id
     * @return
     */

    private AbstractMenuItemPanelV5<Void> getDomainsSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.DOMAINS.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-building " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("domains");
            }

            @Override
            public String getIcon() {
                return ICON_DOMAINS; // "building-8";
            }
        };

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(itemid) {
            @Override
            public void onClick() {
                setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-domains-page"));
            }

            @Override
            public String getLabel() {
                return getMenuLabel("domains");
            }

            @Override
            public String getUrl() {
                return "/factory/domains";
            }

            @Override
            public String getBeforeClick() {
                return "if (typeof submit === \"function\") { submit(); }";
            }
        });

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(id) {
            @Override
            public void onClick() {
                setResponsePage(
                        ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-domain-recycle-bin-page"));
            }

            @Override
            public String getLabel() {
                return getMenuLabel("domain-recycle-bin");
            }

            @Override
            public String getUrl() {
                return "/factory/domainrecyclebin";
            }

            @Override
            public String getBeforeClick() {
                return "if (typeof submit === \"function\") { submit(); }";
            }
        });

        return menu;
    }

    /***
     * INFO
     *
     * 
     * 
     * private void addInfoMenu() { addItem(new MenuItemFactory<Void>() {
     * 
     * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
     *           getInfoSubmenu(id); }; }); }
     **/

    private AbstractMenuItemPanelV5<Void> getInfoSubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.INFO.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-info " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("systeminfo");
            }

            @Override
            public String getUrl() {
                return "/systeminfo/keymetrics";
            }

            @Override
            public String getIcon() {
                return ICON_INFO; // "building-8";
            }
        };

        menu.addItem(itemid -> new LinkMenuItemPanel<Void>(itemid) {
            @Override
            public void onClick() {
                getPage().setResponsePage(new SystemInfoPage());
            }

            @Override
            public String getLabel() {
                return getMenuLabel("dashboard");
            }

            @Override
            public String getUrl() {
                return "/systeminfo/keymetrics";
            }

            @Override
            public String getBeforeClick() {
                return "if (typeof submit === \"function\") { submit(); }";
            }
        });

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        getPage().setResponsePage(new SystemInfoGeneralPage("hardware"));
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.hardware.os");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/hardware";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        /**
         * menu.addItem(new MenuItemFactory<Void>() {
         * 
         * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
         *           new LinkMenuItemPanel<Void>(id) {
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemSchedulerMonitorPage()); }
         * @Override public String getLabel() { return "Scheduler"; //
         *           getMenuLabel("mainmenu.domains.info.api.dashboard");
         * 
         *           }
         * @Override public String getUrl() { return "/systeminfo/scheduler"; }
         * 
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } //public String getCssClass() { //
         *           return "active"; //} }; } });
         **/

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        getPage().setResponsePage(new SystemInfoGeneralPage("api-dashboard")); // Dashboard
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.api.dashboard");

                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/api-dashboard";
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
                        getPage().setResponsePage(new SystemInfoGeneralPage("properties"));
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("properties");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/properties";
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
                        getPage().setResponsePage(new SystemInfoGeneralPage("system.parameters"));
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("system.parameters");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/parameters";
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
                        getPage().setResponsePage(new SystemParametersPage());
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("system.parameters");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/parameters";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }

                };
            }
        });

        // add(new SystemParametersPanel("info-panel"));

        /**
         * menu.addItem(new MenuItemFactory<Void>() {
         * 
         * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
         *           new LinkMenuItemPanel<Void>(id) {
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemInfoGeneralPage("logs")); }
         * @Override public String getLabel() { return "Logs";
         *           //getMenuLabel("mainmenu.domains.info"); }
         * 
         * @Override public String getUrl() { return "/systeminfo/logs"; }
         * 
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } }; } });
         **/

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        getPage().setResponsePage(new SystemInfoGeneralPage("logs"));
                    }

                    @Override
                    public String getLabel() {
                        return "Logs"; // getMenuLabel("mainmenu.domains.info");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/logs";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        /**
         * menu.addItem(new MenuItemFactory<Void>() {
         * 
         * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
         *           new LinkMenuItemPanel<Void>(id) {
         * @Override public void onClick() { getPage().setResponsePage(new
         *           SystemInfoGeneralPage("config")); }
         * 
         * @Override public String getLabel() { return getMenuLabel("configuration"); }
         * 
         * @Override public String getUrl() { return "/systeminfo/config"; }
         * 
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } }; } });
         **/

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        getPage().setResponsePage(new SystemInfoGeneralPage("version"));
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("version");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/version";
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
                        getPage().setResponsePage(new SystemInfoGeneralPage("jvm-threads"));
                    }

                    @Override
                    public String getLabel() {
                        return "JVM Threads"; // getMenuLabel("mainmenu.domains.info");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/jvm-threads";
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
     * FACTORY. API
     * 
     * @param id
     * @return
     */
    private AbstractMenuItemPanelV5<Void> getManagement_APISubmenu(String id) {

        String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.API.getKey()) ? " selected" : "";

        SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-laptop-code " + css_selected) {
            @Override
            public String getLabel() {
                return getMenuLabel("mainmenu.domains.api");
            }

            @Override
            public String getIcon() {
                return ICON_API; // "building-8";
            }

        };

        menu.addItem(new MenuItemFactory<Void>() {
            @Override
            public AbstractMenuItemPanelV5<Void> getItem(String id) {
                return new LinkMenuItemPanel<Void>(id) {
                    @Override
                    public void onClick() {
                        getPage().setResponsePage(new SystemInfoGeneralPage("api-dashboard")); // api-Dashboard
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.domains.api.dashboard");
                    }

                    @Override
                    public String getUrl() {
                        return "/systeminfo/api-dashboard";
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
                        getPage().setResponsePage(new APIRequestsReportPage());
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.domains.api.requests.report");

                    }

                    @Override
                    public String getUrl() {
                        return "/api/reports/requests";
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
                        setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class)
                                .getPage("factory-api-stats-report-page"));
                    }

                    @Override
                    public String getLabel() {
                        return getMenuLabel("mainmenu.domains.api.requests.stats");

                    }

                    @Override
                    public String getUrl() {
                        return "/api/reports/stats";
                    }

                    @Override
                    public String getBeforeClick() {
                        return "if (typeof submit === \"function\") { submit(); }";
                    }
                };
            }
        });

        /***
         * 
         * menu.addItem(new MenuItemFactory<Void>() {
         * 
         * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
         *           new LinkMenuItemPanel<Void>(id) {
         * @Override public void onClick() { getPage().setResponsePage(new
         *           APISOAPReportPage()); }
         * @Override public String getLabel() { return
         *           getMenuLabel("mainmenu.domains.api.soap.report"); }
         * 
         * @Override public String getUrl() { return "/api/reports/soap"; }
         * 
         * @Override public String getBeforeClick() { return "if (typeof submit ===
         *           \"function\") { submit(); }"; } }; } });
         **/

        return menu;
    }

    /***
     * FACTORY
     * 
     * private void addManagementMenu() { // Data Management // addItem(new
     * MenuItemFactory<Void>() {
     * 
     * @Override public AbstractMenuItemPanelV5<Void> getItem(String id) { return
     *           getDataManagementSubmenu(id); }; });
     * 
     *           }
     */

}
