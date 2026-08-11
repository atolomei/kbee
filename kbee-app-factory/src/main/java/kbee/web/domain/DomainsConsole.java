package kbee.web.domain;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.service.WebSessionService;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.service.DomainService;

import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectState;

import com.novamens.indexer.query.Query;

import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.event.GridPanelNullObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.DateColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.NameColumnPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.query.DomainsQuery;
import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public abstract class DomainsConsole extends AbstractFacetedConsole<Domain> {

    private static final long serialVersionUID = 1L;

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainsConsole.class.getName());

    private static final double GB = 1000000000.0;

    private NumberFormat integer_nf = null;

    final boolean is_root = isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
    final boolean is_service_admin = isDomainKbee() && (is_root || ServiceLocator
            .getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId()));
    final boolean is_factory_admin = isDomainKbee() && (is_root || ServiceLocator
            .getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()));
    final boolean is_api = isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class)
            .isMember(KbeeGlobalRole.API_DEVELOPER.getId()));
    final boolean is_domain_admin = isDomainKbee() && (is_root
            || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()));
    final boolean is_operations = isDomainKbee() && (is_root || ServiceLocator
            .getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId()));
    final boolean is_support = isDomainKbee()
            && (ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId()));

    private NumberFormat nf;

    private List<GridColumn<SearchResult, String>> columns;

    private List<ToolbarItem> items;

    long start = System.currentTimeMillis();

    public DomainsConsole(String name, Query query) {
        super(name, query);
    }

    public DomainsConsole(Query query) {
        super("domains", query);
    }

    @Override
    protected String getIcon(IModel<Domain> model) {
        return null;
    }

    @Override
    protected IModel<Domain> getModel(Domain object) {
        return new ObjectModel<Domain>(object, true);
    }

    public void onAfterRender() {
        super.onAfterRender();
        if (logger.isDebugEnabled()) {
            long end = System.currentTimeMillis();
            logger.debug("Total time " + String.valueOf(end - start) + " ms");
        }
    }

    protected boolean isFiltersEnabled() {
        return true;
    }

    protected boolean isSavedQueriesEnabled() {
        return true;
    }

    @Override
    public IModel<String> getDisplayName() {
        return getLabel(getName());
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        this.nf = NumberFormat.getInstance(getSessionUser().getLocale());
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);

        this.integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
        integer_nf.setMinimumFractionDigits(0);
        integer_nf.setMaximumFractionDigits(0);
        integer_nf.setRoundingMode(RoundingMode.HALF_UP);
    }

    /**
     * 
     */
    @Override
    public void onDetach() {
        super.onDetach();
        this.columns = null;
        this.items = null;
    }

    @Override
    public Query newQuery() {
        return setUserPreference(new DomainsQuery(getQueryIndex()));
    }

    public Page getConsolePage(Query query) {
        return getConsolePage(query, -1);
    }

    protected BreadCrumb getBreadCrumb() {
        return null;
    };

    @Override
    protected boolean hasExpander() {
        return true;
    }

    @Override
    protected Panel getMenu(IModel<Domain> model) {

        ContextMenuPanel<Domain> menu = new ContextMenuPanel<Domain>(model);

        menu.setOutputMarkupId(true);

        if (is_support) {

            String sus = getSupportUser(model.getObject());

            if (sus != null && sus.length() > 0) {
                String arr[] = sus.split(";");
                for (String s : arr) {

                    final String uname = s;

                    menu.addItem(new MenuItemFactory<Domain>() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                            return new AjaxMenuItemPanelV5<Domain>(id) {
                                private static final long serialVersionUID = 1L;

                                public void onClick(AjaxRequestTarget target) {
                                    try {
                                        User us = ServiceLocator.getService(SecurityService.class).findUserByUsername(uname);
                                        ServiceLocator.getService(UserService.class).impersonate(us);
                                        WebPage page = ServiceLocator.getService(ApplicationSiteMapService.class)
                                                .getPage(ApplicationSiteMapService.HomePage);
                                        page.getSession().setLocale(us.getLocale());
                                        setResponsePage(page);
                                    } catch (Exception e) {
                                        logger.error(e);
                                        setResponsePage(new ApplicationErrorPage(e));
                                    }
                                }

                                @Override
                                public boolean isEnabled() {

                                    if (getModel().getObject().getName().equals("kbee"))
                                        return is_root;

                                    return true;
                                }

                                @Override
                                public boolean isVisible() {
                                    return true;
                                }

                                @Override
                                public String getLabel() {
                                    return new StringResourceModel("signas", this, null).getObject() + ":" + uname;
                                }

                                @Override
                                public String getWorkingLabel() {
                                    return new StringResourceModel("working", this, null).getObject();
                                }
                            };
                        }
                    });

                }
            }

            return menu;
        }

        // Open
        //
        menu.addItem(new MenuItemFactory<Domain>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                return new AjaxMenuItemPanelV5<Domain>(id) {
                    private static final long serialVersionUID = 1L;

                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(getDomainPage(getModel(), getIndex(), false, false));
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("open").getObject();
                    }

                    @Override
                    public boolean isEnabled() {
                        return is_service_admin || is_factory_admin || is_domain_admin;
                    }
                };
            }
        });

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
                        return true;
                    }
                };
            }
        });

        // up to 5 users with Super User rights
        // String sus =
        // getContentDao().findSystemParameterValueByKey("su."+model.getObject().getName(),
        // null);

        String sus = getAdminUser(model.getObject());

        if (sus != null && sus.length() > 0) {
            String arr[] = sus.split(";");
            for (String s : arr) {

                final String uname = s;

                menu.addItem(new MenuItemFactory<Domain>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                        return new AjaxMenuItemPanelV5<Domain>(id) {
                            private static final long serialVersionUID = 1L;

                            public void onClick(AjaxRequestTarget target) {
                                try {
                                    User us = ServiceLocator.getService(SecurityService.class).findUserByUsername(uname);
                                    ServiceLocator.getService(UserService.class).impersonate(us);
                                    WebPage page = ServiceLocator.getService(ApplicationSiteMapService.class)
                                            .getPage(ApplicationSiteMapService.HomePage);
                                    page.getSession().setLocale(us.getLocale());
                                    setResponsePage(page);
                                } catch (Exception e) {
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
                                return "Sign as " + uname;
                            }

                            @Override
                            public String getWorkingLabel() {
                                return new StringResourceModel("working", this, null).getObject();
                                // return getConsoleLabel("usersconsole.contextmenu.working").getObject();
                            }
                        };
                    }
                });

            }
        }

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
                        return true;
                    }
                };
            }
        });

        //
        // Set Type * Enterprise (paid) / Express (Free) / System (kbee Domain)

        menu.addItem(new MenuItemFactory<Domain>() {

            private static final long serialVersionUID = 1L;

            @Override
            public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                return new AjaxMenuItemPanelV5<Domain>(id) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            getModel().getObject().setDomainType(DomainType.PREMIUM);
                            List<String> list = new ArrayList<String>();
                            list.add("Set as Enterprise");
                            getModel().getObject().getService(DomainService.class).update(list);
                            DomainsConsole.this.refresh(target);

                        } catch (Exception e) {
                            logger.error(e);

                        }
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("type.enterprise").getObject();
                    }

                    @Override
                    public boolean isVisible() {
                        return getModel().getObject().getDomainType() == DomainType.EXPRESS;
                    }

                    @Override
                    public boolean isEnabled() {
                        return is_domain_admin || is_service_admin || is_factory_admin;
                    }

                    @Override
                    public String getWorkingLabel() {
                        return new StringResourceModel("working", this, null).getObject();
                    }
                };
            }
        });

        //
        // Set Type Enterprise (paid) / * Express (Free) / System (kbee Domain)
        //

        menu.addItem(new MenuItemFactory<Domain>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                return new AjaxMenuItemPanelV5<Domain>(id) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            getModel().getObject().setDomainType(DomainType.EXPRESS);

                            List<String> list = new ArrayList<String>();
                            list.add("Set as Express");
                            getModel().getObject().getService(DomainService.class).update(list);
                            DomainsConsole.this.refresh(target);

                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("type.express").getObject();
                    }

                    @Override
                    public boolean isVisible() {
                        return getModel().getObject().getDomainType() == DomainType.PREMIUM;
                    }

                    @Override
                    public boolean isEnabled() {
                        return is_domain_admin || is_service_admin || is_factory_admin;
                    }

                    @Override
                    public String getWorkingLabel() {
                        return new StringResourceModel("working", this, null).getObject();
                    }
                };
            }
        });

        // Archive
        //
        menu.addItem(new MenuItemFactory<Domain>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                return new AjaxMenuItemPanelV5<Domain>(id) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            getModel().getObject().getService(DomainService.class).archive();
                            DomainsConsole.this.refresh(target);

                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("archive").getObject();
                    }

                    @Override
                    public boolean isEnabled() {

                        if (getModel().getObject().getDomainType() == DomainType.SYSTEM)
                            return false;

                        return is_root || is_service_admin || is_factory_admin;

                    }

                    @Override
                    public boolean isVisible() {
                        return getModel().getObject().getState() == ObjectState.ENABLED;
                    }

                    @Override
                    public String getWorkingLabel() {
                        return new StringResourceModel("working", this, null).getObject();
                    }
                };
            }
        });

        //
        // Enable (not for Deleted)
        //
        menu.addItem(new MenuItemFactory<Domain>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                return new AjaxMenuItemPanelV5<Domain>(id) {
                    private static final long serialVersionUID = 1L;

                    public void onClick(AjaxRequestTarget target) {
                        try {
                            getModel().getObject().setState(ObjectState.ENABLED);
                            List<String> list = new ArrayList<String>();
                            list.add("Enabled");
                            getModel().getObject().getService(DomainService.class).update(list);
                            DomainsConsole.this.refresh(target);

                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("enable").getObject();
                    }

                    @Override
                    public boolean isEnabled() {

                        if (getModel().getObject().getDomainType() == DomainType.SYSTEM)
                            return false;

                        return is_domain_admin || is_service_admin || is_factory_admin;

                    }

                    @Override
                    public boolean isVisible() {
                        return ((getModel().getObject().getState() != ObjectState.ENABLED)
                                && (getModel().getObject().getState() != ObjectState.DELETED));
                    }

                    @Override
                    public String getWorkingLabel() {
                        return new StringResourceModel("working", this, null).getObject();
                    }
                };
            }
        });

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
                        return true;
                    }
                };
            }
        });

        // ----------------------------
        //
        // Enable / Disable API
        //

        menu.addItem(new MenuItemFactory<Domain>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                return new AjaxMenuItemPanelV5<Domain>(id) {
                    private static final long serialVersionUID = 1L;

                    public void onClick(AjaxRequestTarget target) {
                        try {
                            getModel().getObject().setAPIEnabled(!getModel().getObject().isAPIEnabled());
                            List<String> list = new ArrayList<String>();
                            list.add("Enable API");
                            getModel().getObject().getService(DomainService.class).update(list);
                            DomainsConsole.this.refresh(target);

                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getModel().getObject().isAPIEnabled() ? "Disable API" : "Enable API";
                    }

                    @Override
                    public boolean isEnabled() {
                        if (getModel().getObject().getDomainType() == DomainType.SYSTEM)
                            return false;

                        return is_domain_admin || is_service_admin || is_factory_admin;

                    }

                    @Override
                    public boolean isVisible() {
                        return true;
                    }

                    @Override
                    public String getWorkingLabel() {
                        return new StringResourceModel("working", this, null).getObject();
                    }
                };
            }
        });

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
                        return true;
                    }
                };
            }
        });

        // Restore (for Deleted Domains)
        //
        menu.addItem(new MenuItemFactory<Domain>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AbstractMenuItemPanelV5<Domain> getItem(String id) {
                return new AjaxMenuItemPanelV5<Domain>(id) {
                    public void onClick(AjaxRequestTarget target) {
                        DomainService objectService = getModel().getObject().getService(DomainService.class);
                        try {
                            objectService.restore();
                        } catch (Exception e) {
                            logger.error(e);
                        }
                        DomainsConsole.this.refresh(target);
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("contextmenu.restore").getObject();
                    }

                    @Override
                    public boolean isVisible() {
                        if (getModel().getObject().getState() == ObjectState.DELETED)
                            return true;
                        return false;
                    }

                    @Override
                    public boolean isEnabled() {

                        if (getModel().getObject().getState() == ObjectState.DELETED)
                            return true;

                        return is_domain_admin || is_service_admin || is_factory_admin;

                    }

                    @Override
                    public String getWorkingLabel() {
                        return new StringResourceModel("working", this, null).getObject();
                    }
                };
            }
        });

        // Delete
        //
        menu.addItem(id -> new AjaxMenuItemPanelV5<Domain>(id) {
            public void onClick(AjaxRequestTarget target) {
                getConfirmationDialog().open(target,
                        getConsoleLabel("deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete,
                        new Dialog.Handler() {
                            @Override
                            public void onClick(AjaxRequestTarget target, Button button) {
                                if (button.key().equals(Dialog.Delete.key())) {
                                    try {
                                        if (getModel().getObject().getState() == ObjectState.DELETED) {
                                            getModel().getObject().getService(DomainService.class).delete();
                                            // [ EMAIL ! ]
                                            try {
                                                Thread.sleep(2000);
                                            } catch (Exception e) {
                                            }
                                        } else {
                                            getModel().getObject().getService(DomainService.class).markAsDeleted();
                                        }
                                    } catch (Exception e) {
                                        logger.error(e);
                                        getErrorDialog().open(target, new Model<String>(e.getMessage()));
                                    }
                                    DomainsConsole.this.refresh(target);
                                }
                            }
                        });
                refresh(target);
            }

            @Override
            public String getLabel() {
                if (getModel().getObject().getState() == ObjectState.DELETED)
                    return "Delete from Server (can not be undone)";
                else
                    return getConsoleLabel("contextmenu.delete").getObject();
            }

            @Override
            public boolean isEnabled() {
                if (getModel().getObject().getDomainType() == DomainType.SYSTEM)
                    return false;
                return is_domain_admin || is_service_admin || is_factory_admin;
            }

            @Override
            public String getWorkingLabel() {
                return new StringResourceModel("working", this, null).getObject();
            }
        });

        return menu;
    }

    /**
     * 
     * @param dom
     * @return
     */
    protected String getAdminUser(Domain dom) {

        StringBuilder str = new StringBuilder();
        int n = 0;

        try {
            List<Principal> set = ServiceLocator.getService(SecurityService.class).getDomainAdminUsers(dom.getId().toString());
            boolean is_root = false;
            for (Principal p : set) {
                if (p instanceof User) {
                    if ((n > 8 && !is_root) || n > 9) {
                        break;
                    }
                    if (((User) p).isEnabled()) {
                        String na = ((User) p).getUserName();
                        if (str.length() > 0)
                            str.append(";");
                        str.append(na);
                        if (na.startsWith("root@"))
                            is_root = true;
                        n++;
                    }
                }
            }
            if (!is_root)
                return "root@" + dom.getName() + ";" + str.toString();
            return str.toString();
        } catch (Exception e) {
            logger.error(e);
            return (dom != null && dom.getName() != null ? ("root@" + dom.getName()) : "");
        }
    }

    protected String getSupportUser(Domain dom) {

        StringBuilder str = new StringBuilder();
        int n = 0;

        try {
            List<Principal> set = ServiceLocator.getService(SecurityService.class).getDomainSupportUsers(dom.getId().toString());
            for (Principal p : set) {
                if (p instanceof User) {
                    if ((n > 8 && !is_root) || n > 9) {
                        break;
                    }
                    if (((User) p).isEnabled()) {
                        String na = ((User) p).getUserName();
                        if (str.length() > 0)
                            str.append(";");
                        str.append(na);
                        n++;
                    }
                }
            }
            return str.toString();

        } catch (Exception e) {
            logger.error(e);
            return "";
        }
    }

    /**
     * 1. Sorting properties must exists in SolR <b>schema.xml</b> like: title_sort,
     * type, ...
     * 
     * 2. They must be indexed in kbee-content content-index-context-xml
     * 
     * 3. they must have a default schema here:
     * {@link com.novamens.kbee.content.indexer.JavaContentIndexFactory}
     * 
     * and they must be retrievable by Id from ContentDao
     * 
     * 
     * 
     */
    @Override
    public List<GridColumn<SearchResult, String>> getColumns() {

        if (this.columns != null)
            return this.columns;

        this.columns = new ArrayList<GridColumn<SearchResult, String>>();

        this.columns.add(new ObjectStatusColumn<Domain>("iconstatus", getName(), getLabel("st")));

        this.columns.add(new GridColumn<SearchResult, String>("domain", getLabel("domain"), "name") {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    return new Model<String>(((Domain) object.getObject()).getName());
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName() + " | " + e.getMessage());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("name", getLabel("name"), "organization_sort") {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId,
                    IModel<SearchResult> resultmodel) {
                try {
                    Object object = resultmodel.getObject().getObject();
                    IModel<Domain> objectmodel = getModel((Domain) object);
                    cellItem.add(new NameColumnPanel<Domain>(componentId, objectmodel) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected String getCss() {
                            return "cell-label btn-link";
                        }
                    });
                } catch (Exception e) {
                    logger.error(e);
                    cellItem.add(new Label(componentId, e.getClass().getName() + " | " + e.getMessage()));
                }
            }

            @Override
            public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId,
                    IModel<SearchResult> resultmodel) {
                try {
                    Object object = resultmodel.getObject().getObject();
                    IModel<Domain> objectmodel = getModel((Domain) object);
                    cellItem.add(new Label(componentId, objectmodel.getObject().getOrganization()));

                } catch (Exception e) {
                    logger.error(e);
                    cellItem.add(new Label(componentId, e.getClass().getName() + " | " + e.getMessage()));
                }
            }

            @Override
            public String getCssClass() {
                return "col title col-xs-1 col-md-1 col-lg-1";
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                return new Model<String>(((Domain) result.getObject()).getOrganization());
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("type", getLabel("type")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");

                    String type = ((Domain) result.getObject()).getDomainType().getLabel();

                    if (type == null)
                        return new Model<String>("err");

                    String css = ((Domain) result.getObject()).getDomainType().getCss();
                    return new Model<String>("<span class=\"" + css + "\">" + type + "</span>");
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                return new Model<String>(((Domain) result.getObject()).getDomainType().getLabel());
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable";
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("status", getLabel("status"), "state") {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    ObjectState state = ((Domain) result.getObject()).getState();
                    if (state == null)
                        return new Model<String>("err");
                    return new Model<String>(state.getHTMLLabel(getUser().getLocale()));
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName() + " | " + e.getMessage());
                }
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    ObjectState state = ((Domain) result.getObject()).getState();
                    if (state == null)
                        return new Model<String>("err");
                    return new Model<String>(state.getLabel());
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName() + " | " + e.getMessage());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("api", new Model<String>("API"), "api") {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    return new Model<String>(
                            ("<span class=\"" + (((Domain) result.getObject()).isAPIEnabled() ? "yes" : "no") + "\">")
                                    + (((Domain) result.getObject()).isAPIEnabled() ? "Yes" : "No") + "</span>");
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                return new Model<String>(((Domain) result.getObject()).isAPIEnabled() ? "yes" : "no");
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("kbfs", new Model<String>("Object Storage"), "kbfs_sort") {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    String label = ((Domain) result.getObject()).getStorageType().getLabel();
                    // if (label!=null && label.toUpperCase().trim().contains("File System"))
                    return new Model<String>(("<span class=\"" + ((Domain) result.getObject()).getStorageType().getKey() + "\">"
                            + label + "</span>"));
                    // else
                    // return new Model<String>(( "<span class=\"\">" + label + "</span>"));

                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            public boolean isPreferred() {
                return false;
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    String label = ((Domain) result.getObject()).getStorageType().getLabel();
                    return new Model<String>(label);
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

        });

        /**
        this.columns.add(new GridColumn<SearchResult, String>("harddisk", getLabel("harddisk")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    long val = getDomainMetricsServices().getHardDisk(((Domain) result.getObject()));

                    String sval = ServiceLocator.getService(DateTimeService.class).formatFileSize(val, getSessionUser().getLocale(),
                            "ago");
                    return new Model<String>(sval);
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }


            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-xxl";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }

            @Override
            public boolean isEscapeModelString() {
                return false;
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                long val = getDomainMetricsServices().getHardDisk(((Domain) result.getObject()));
                String sval = ServiceLocator.getService(DateTimeService.class).formatFileSize(val, getSessionUser().getLocale(),
                        null);
                return new Model<String>(sval);
            }
        });
            **/
        
        {
            // final WebSessionService webSessionService =
            // ServiceLocator.getService(WebSessionService.class);
            final KbeePredicateGridColumn<Domain> totalActiveSessionsColumn = new KbeePredicateGridColumn<>("totalActiveSessions",
                    getLabel("totalActiveSessions"), domain -> String.valueOf(
                            ServiceLocator.getService(WebSessionService.class).countDomainTotalActiveSessions(domain.getName())));
            totalActiveSessionsColumn.setCssValueResolver(domain -> ServiceLocator.getService(WebSessionService.class)
                    .countDomainTotalActiveSessions(domain.getName()) > 0 ? "col col-xs-1 col-md-1 col-lg-1 number-md info"
                            : "col col-xs-1 col-md-1 col-lg-1 number-md");
            this.columns.add(totalActiveSessionsColumn);
        }

        {
            // final WebSessionService webSessionService =
            // ServiceLocator.getService(WebSessionService.class);
            final KbeePredicateGridColumn<Domain> totalActiveSessionsColumn = new KbeePredicateGridColumn<>(
                    "usersWithActiveSessions", getLabel("usersWithActiveSessions"), domain -> String.valueOf(
                            ServiceLocator.getService(WebSessionService.class).countUsersWithActiveSessions(domain.getName())));
            totalActiveSessionsColumn.setCssValueResolver(
                    domain -> ServiceLocator.getService(WebSessionService.class).countUsersWithActiveSessions(domain.getName()) > 0
                            ? "col col-xs-1 col-md-1 col-lg-1 number-md info"
                            : "col col-xs-1 col-md-1 col-lg-1 number-md");
            this.columns.add(totalActiveSessionsColumn);
        }

        {
            KbeePredicateGridColumn<Domain> aliasColumn = new KbeePredicateGridColumn<>("oauth", new Model<String>("Oauth"),
                    obj -> "<span class=\"" + (obj.isOAuthAuthentication() ? "yes" : "no") + "\">"
                            + (obj.isOAuthAuthentication() ? "Yes" : "No") + "</span>");
            aliasColumn.setContextKey(this.getName() + aliasColumn.getContextKey());
            this.columns.add(aliasColumn);
        }

        this.columns.add(new GridColumn<SearchResult, String>("gatewayharddisk", getLabel("gatewayharddisk")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    long val = getDomainMetricsServices().getHardDisk(((Domain) result.getObject()), KBFSStorageType.External);
                    String sval = ServiceLocator.getService(DateTimeService.class).formatFileSize(val, getSessionUser().getLocale(),
                            "ago");

                    return new Model<String>(sval);
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                long val = getDomainMetricsServices().getHardDisk(((Domain) result.getObject()), KBFSStorageType.External);
                return new Model<String>(String.valueOf(val));
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-xxl";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }

            @Override
            public boolean isEscapeModelString() {
                return false;
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("percentage", getLabel("percentage")) {
            private static final long serialVersionUID = 1L;

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    Domain domain = ((Domain) result.getObject());
                    double val = getDomainMetricsServices().getHardDisk(domain);
                    return new Model<String>(String.valueOf(val));

                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {

                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    long val = getDomainMetricsServices().getHardDisk(((Domain) result.getObject()));
                    if (((Domain) result.getObject()).getQuota() > 0) {
                        Double d_qt = Double.valueOf(((Domain) result.getObject()).getQuota());
                        Double d_va = Double.valueOf((double) val / (double) GB);

                        String perc = getNumberFormat().format(100.0 * d_va / d_qt);
                        return new Model<String>(perc + "<span class=\"ago\">%</span>");
                    } else {
                        return new Model<String>("");
                    }
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-md";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }

            @Override
            public boolean isEscapeModelString() {
                return false;
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("monthlyusage", getLabel("monthlyusage")) {
            private static final long serialVersionUID = 1L;

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    Domain domain = ((Domain) result.getObject());
                    double val = getDomainMetricsServices().getMeanHardDiskIncrease30d(domain);
                    return new Model<String>(String.valueOf(val));

                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    Domain domain = ((Domain) result.getObject());
                    if (getDomainMetricsServices().getMeanHardDiskIncrease30d(domain) > 0) {
                        String sval_30d = getNumberFormat().format(
                                Double.valueOf((double) getDomainMetricsServices().getMeanHardDiskIncrease30d(domain) / (double) GB)
                                        .doubleValue())
                                + " <span class=\"ago\">GB / month</span>";
                        return new Model<String>(sval_30d);
                    } else
                        return new Model<String>("");
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-xxl";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }

            @Override
            public boolean isEscapeModelString() {
                return false;
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        this.columns.add(new DateColumn<Domain>("created", getLabel("created"), "created") {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {

                try {
                    Domain ds = (Domain) result.getObject();
                    String tst = ds.getCreationOffsetDateTimeColloquial();
                    return new Model<String>(tst);
                } catch (Exception e) {
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected OffsetDateTime getOffsetDateTime(Domain object) {
                return object.getCreationOffsetDateTime();
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("user", getLabel("username")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    User user = ((Domain) result.getObject()).getLastModifiedUser();
                    if (user == null)
                        return new Model<String>("err");
                    return new Model<String>(user.getFirstLastName());
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }
        });

        this.columns.add(new LastModifiedColumn<Domain>("modified", getLabel("modified"), "modified") {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    Domain ds = (Domain) result.getObject();
                    String tst = ds.getLastModifiedOffsetDateTimeColloquial();
                    return new Model<String>(tst);
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }

            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();

            }
        });

        // "Mean Rate. 1m 5m 15m"

        this.columns.add(new GridColumn<SearchResult, String>("recentconsoleactivity", getLabel("recentconsoleactivity")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    if (object.getObject() == null)
                        return new Model<String>("err");
                    try {
                        Domain domain = ((Domain) object.getObject());
                        SystemMetricsService se = ServiceLocator.getService(SystemMetricsService.class);
                        StringBuilder str = new StringBuilder();
                        str.append(getNumberFormat()
                                .format(se.getMeterContentCheckin(domain.getId()).getMeanRate() * 60.0 * 60.0 * 24.0));

                        return new Model<String>(str.toString());
                    } catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName());
                    }
                } catch (Exception e) {
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-xl";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("totalcontents", getLabel("totalcontents")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    if (object.getObject() == null)
                        return new Model<String>("err");
                    try {
                        Domain domain = ((Domain) object.getObject());
                        if (getDomainMetricsServices().getResources(domain) > 0) {
                            String res = getIntegerNumberFormat().format(getDomainMetricsServices().getContents(domain));
                            return new Model<String>(res);
                        } else
                            return new Model<String>("");
                    } catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName());
                    }
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-xl";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }

            @Override
            public boolean isPreferred() {
                return false;
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    Domain domain = ((Domain) result.getObject());
                    long val = getDomainMetricsServices().getContents(domain);
                    return new Model<String>(String.valueOf(val));

                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("totalfiles", getLabel("totalfiles")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    if (object.getObject() == null)
                        return new Model<String>("err");
                    try {
                        Domain domain = ((Domain) object.getObject());
                        if (getDomainMetricsServices().getResources(domain) > 0) {
                            String res = getIntegerNumberFormat().format(getDomainMetricsServices().getResources(domain));
                            return new Model<String>(res);
                        } else
                            return new Model<String>("");
                    } catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName());
                    }
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    Domain domain = ((Domain) result.getObject());
                    long val = getDomainMetricsServices().getResources(domain);
                    return new Model<String>(String.valueOf(val));

                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-xl";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        KbeePredicateGridColumn<Domain> domainIdColumn = new KbeePredicateGridColumn<>("id", getLabel("id"),
                (obj) -> String.valueOf(obj.getId()));
        domainIdColumn.setContextKey(this.getName() + domainIdColumn.getContextKey());
        columns.add(domainIdColumn);

        KbeePredicateGridColumn<Domain> eIdColumn = new KbeePredicateGridColumn<>("externalid", new Model<String>("External Id"),
                (obj) -> String.valueOf(obj.getExternalId()));
        eIdColumn.setContextKey(this.getName() + eIdColumn.getContextKey());
        columns.add(eIdColumn);

        KbeePredicateGridColumn<Domain> aliasColumn = new KbeePredicateGridColumn<>("encrypted", new Model<String>("Encrypted"),
                obj -> "<span class=\"" + (obj.isEncryptFiles() ? "yes" : "no") + "\">" + (obj.isEncryptFiles() ? "Yes" : "No")
                        + "</span>");
        aliasColumn.setContextKey(this.getName() + aliasColumn.getContextKey());
        this.columns.add(aliasColumn);

        this.columns.add(new GridColumn<SearchResult, String>("quota", getLabel("quota")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    int val = ((Domain) result.getObject()).getQuota();
                    String sval = val > 0 ? (getIntegerNumberFormat().format(val) + " <span class=\"ago\">GB<span>") : "";
                    return new Model<String>(sval);
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            public boolean isPreferred() {
                return false;
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                int val = ((Domain) result.getObject()).getQuota();
                return new Model<String>(String.valueOf(val));
            }

            @Override
            protected String getContextKey() {
                return DomainsConsole.this.getName() + super.getContextKey();
            }

            @Override
            protected String getLabelCss() {
                return "number-xl";
            }

            @Override
            public String getCssClass() {
                return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
            }
        });

        return this.columns;

    }

    protected Panel getPanel(IModel<Domain> model) {
        return new ExpandedPanel<Domain>("editor", this, model, null);
    };

    protected Panel getPanel(IModel<Domain> model, List<String> list) {
        return new ExpandedPanel<Domain>("editor", this, model, list);
    };

    protected void addListeners() {
        super.addListeners();

        add(new WicketEventListener<GridPanelNullObjectEvent<?>>() {
            @Override
            public void onEvent(GridPanelNullObjectEvent<?> event) {
                ServiceLocator.getService(AppMonitoringService.class).attempToFixDomainIndex();
            }
        });

        add(new WicketEventListener<SidePanelEvent>() {
            @Override
            public void onEvent(SidePanelEvent event) {
            }
        });

        add(new WicketEventListener<ClickEvent<Domain>>() {
            @Override
            public void onEvent(ClickEvent<Domain> event) {
                setResponsePage(getDomainPage(event.getModel(), event.getIndex(), false, false));
            }
        });
    }

    /**
     * Grid Toolbar
     */
    @Override
    protected List<ToolbarItem> getToolbarItems(BaseBrowser<Domain> browser) {

        if (this.items != null)
            return this.items;

        this.items = super.getToolbarItems(browser);

        // New Button
        items.add(new NewDomainButton(browser, ToolbarItem.Align.TOP_LEFT));

        InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                InfoDialog infoDialog = (InfoDialog) getInformationModal();
                infoDialog.open(target, () -> {
                    return DomainsConsole.this.getName();
                }, new Model<String>(DomainsConsole.this.getDescription()));
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        };

        this.items.add(infoButton);

        return items;
    }

    @Override
    protected void addModals() {
        super.addModals();
    }

    protected Component newIcon() {
        return new WebMarkupContainer("icon");
    }

    @Override
    protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
        try {
            if (((Domain) rowmodel.getObject().getObject()).getState() == ObjectState.ARCHIVED)
                return "archived-state";
            if (((Domain) rowmodel.getObject().getObject()).getState() == ObjectState.DELETED)
                return "deleted-state";
            return null;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * 
     * 
     * @param model
     * @param index
     * @param is_new
     * @param editon
     * @return
     */
    protected Page getDomainPage(IModel<Domain> model, int index, final boolean is_new, final boolean editon) {

        /**
         * GlobalNavigationBar<Domain> navigationbar = new
         * GlobalNavigationBar<Domain>("navigation") { private static final long
         * serialVersionUID = 1L;
         * 
         * @Override public void onNavigate(Domain Classifier) { IModel<Domain> model =
         *           getModel(Classifier); model.detach(); }
         * 
         *           // IMPORTANT: Because it's inline class // If not Wicket throws a
         *           Non Serializable Exception
         * 
         * @Override public void onDetach() { super.onDetach();
         *           DomainsConsole.this.onDetach(); }
         * 
         * @Override protected void onReturn(AjaxRequestTarget target) { }
         * 
         * @Override protected void onSearch(AjaxRequestTarget target, String text) { }
         *           };
         * 
         * 
         *           navigationbar.setIsAlerts(false);
         * 
         *           Page page = new DomainPage(model, navigationbar, editon, is_new);
         *           return page;
         */
        return new DomainPage(model);

    }

    private DomainMetricsService getDomainMetricsServices() {
        return ServiceLocator.getService(DomainMetricsService.class);
    }

    private NumberFormat getIntegerNumberFormat() {
        return this.integer_nf;
    }

    private NumberFormat getNumberFormat() {
        return this.nf;
    }

}
