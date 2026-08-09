package kbee.web.dashboard;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.novamens.content.base.Content;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Proxy;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.CursorNavigator;
import kbee.web.cursor.CursorListModel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.Navigator;

public abstract class DashboardContentWidgetPanel extends DashboardListWidgetPanel<Content> {

    static private final long serialVersionUID = 1L;
    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger
            .getLogger(DashboardContentWidgetPanel.class.getName());

    static public final String PROPERTY_UNREAD = "unread";
    static public final String TO_ESC = "<br\\s*/>\\s*<br\\s*/>";

    static protected final int MAX = 280;

    private boolean is_send_email;
    private NumberFormat integer_nf = null;
    private String zid;
    private Locale locale;
    private DateTimeService service = ServiceLocator.getService(DateTimeService.class);
    private IModel<User> model_wuser;

    /**
     * 
     * 
     * @param id
     * @param preferences_key
     */
    public DashboardContentWidgetPanel(String id, String preferences_key) {
        this(id, null, null, preferences_key);
    }

    public DashboardContentWidgetPanel(String id, List<IModel<Content>> list, String preferences_key) {
        this(id, list, null, preferences_key);
    }

    public DashboardContentWidgetPanel(String id, List<IModel<Content>> list, IModel<String> title, String preferences_key) {
        super(id, list, title, preferences_key);

        integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
        integer_nf.setMinimumFractionDigits(0);
        integer_nf.setMaximumFractionDigits(0);
        integer_nf.setRoundingMode(RoundingMode.HALF_UP);

        KbeeUser us = (KbeeUser) getSessionUser();
        locale = us.getLocale();
        zid = getDateTimeService().getMapZoneIds().get(us.getTimeZone());
        setHelp(true);
        setEdit(false);
        this.is_send_email = (isRoot() || isAdmin()) || getPerson().getProfile(UserProfile.class).isSendFilesEmail();
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
    }

    @Override
    public IModel<String> getIconCss(IModel<Content> model) {
        try {
            String nr = (String) model.getObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
            if (nr != null && nr.equals("yes")) {
                return new Model<String>("fa fa-square panel-centered");
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e, getSessionUser().getUserName() + " | probably requires reindexing.");
            return null;
        }
    }

    /**
     * 
     * if open Item In New Tab ->
     * 
     * return new PopupSettings( PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR
     * | PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS |
     * PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
     **/
    @Override
    public PopupSettings getPopupSettings() {
        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        service = null;

        if (model_wuser != null)
            model_wuser.detach();

    }

    protected void onHelp(AjaxRequestTarget target) {
        super.toogleHelp(target);
    }

    protected Navigator<Content> getNavigator(int index) {
        List<IModel<Content>> mi = new ArrayList<IModel<Content>>();
        getItems().forEach(item -> {
            mi.add(new ObjectModel<Content>((Content) item.getObject()));
        });
        CursorListModel<Content> cursor = new CursorListModel<Content>(mi, index);
        CursorNavigator<Content> c = new CursorNavigator<Content>(cursor, index);
        return c;
    }

    protected Index getQueryIndex() {
        return getDomain().getService(JavaIndexerService.class).getIndex();
    }

    protected DateTimeService getDateTimeService() {
        if (service == null)
            service = ServiceLocator.getService(DateTimeService.class);
        return service;
    }

    protected IModel<User> getPendingModelUser() {
        if (model_wuser == null) {
            User user = getDomain().getService(DomainService.class).getWorkflowUser();
            model_wuser = new ObjectModel<User>(user);
        }
        return model_wuser;
    }

    protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
        StringBuilder str = new StringBuilder();
        try {

            String task = modelObject.getObject().getService(WorkflowService.class).getActivity().getTask().getDisplayName();
            str.append(task);

            if (task != null)
                str.append(" - ");

            String ty = modelObject.getObject().getService(ContentService.class).getConsoleSubtitle();

            if (ty != null && ty.length() > 0)
                str.append(ty);
            else {
                String ta = modelObject.getObject().getContentTypeClassificationAsString();
                if (ta != null && ta.length() > 0) {
                    str.append(ta);
                    str.append(", ");
                }
                String st = modelObject.getObject().getWorkflowStatusClassificationAsString();
                str.append(st);
            }

        } catch (Exception e) {
            logger.error(e);
            str.append(e.getClass().getName());
        }
        return new Model<String>(str.toString());
    }

    @Override
    protected IModel<String> getViewingString() {
        if (getItems() != null) {
            String tot = "XXX";
            return new Model<String>("<b>" + getItems().size() + "</b> of <b>" + tot + "</b>");
        }
        return new Model<String>("");
    }

    protected IModel<String> getAllString() {
        return getLabel("mytasks");
    }

    @Override
    protected void onClick(IModel<Content> modelObject, int index) {
    }

    @Override
    protected Panel getMenu(IModel<Content> model, int index) {
        return new InvisiblePanel("menu");
    }

    @Override
    protected void onClickAll() {
    }

    protected boolean isMenuVisible() {
        return true;
    }

    protected NumberFormat getIntegerNumberFormat() {
        return integer_nf;
    }

    protected String getZid() {
        return zid;
    }

    protected Locale getSessionUserLocale() {
        return locale;
    }

    protected boolean isDownload() {
        return is_send_email;
    }

    protected boolean isSendByEmail() {
        return is_send_email;
    }

    public boolean isMenu() {
        return true;
    }

    protected IModel<String> getSnippet(String text) {
        if (text == null || text.isEmpty())
            return new Model<String>();
        String s = null;
        if (text.length() > MAX)
            s = text.substring(0, MAX) + "...";
        else
            s = text;
        Safelist list = Safelist.basic();
        list.removeTags("p");

        String cleaned = Jsoup.clean(s, list);
        String t1 = cleaned;
        return new Model<String>(t1);
    }

    @Override
    public void addListeners() {
        super.addListeners();

        add(new WicketEventListener<SortWicketAjaxEvent>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onEvent(SortWicketAjaxEvent event) {
                DashboardContentWidgetPanel.this.applySort(event);
                logger.debug(event.getCriteria() + " -> " + DashboardContentWidgetPanel.this.getClass().getName());
            }
        });

    }

    protected void applySort(SortWicketAjaxEvent event) {

    }

    /**
     * FeedbackHelper.showInfoToast(getLabel());
     */
    protected Panel getMenu() {
        try {

            ContextMenuPanel<Panel> menu = new ContextMenuPanel<Panel>("menu", new Model<Panel>(this));
            menu.setOutputMarkupId(true);

            menu.addItem(new MenuItemFactory<Panel>() {
                private static final long serialVersionUID = 1L;

                @Override
                public AbstractMenuItemPanelV5<Panel> getItem(String id) {
                    return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onCheckClick(AjaxRequestTarget target) {
                            try {
                                DashboardContentWidgetPanel.this.onViewMode(target, "compact");
                            } catch (Exception e) {
                                setResponsePage(new ApplicationErrorPage<>(e));
                                logger.error(e);
                            }

                        }

                        @Override
                        public String getLabel() {
                            return new StringResourceModel("compact-view", this, null).getObject();
                        }

                        @Override
                        public boolean isEnabled() {
                            return !getViewModeCriteria().equals("compact");
                        }

                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            attributes.setEventPropagation(EventPropagation.STOP);
                        }

                        @Override
                        public boolean isVisible() {
                            return true;
                        }

                        @Override
                        public boolean isIconVisible() {
                            return getViewModeCriteria().equals("compact");
                        }

                        @Override
                        public String getWorkingLabel() {
                            return new StringResourceModel("working", DashboardContentWidgetPanel.this, null).getString();
                        }
                    };
                }
            });

            menu.addItem(new MenuItemFactory<Panel>() {
                private static final long serialVersionUID = 1L;

                @Override
                public AbstractMenuItemPanelV5<Panel> getItem(String id) {
                    return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onCheckClick(AjaxRequestTarget target) {
                            try {
                                if (getViewModeCriteria().equals("comfortable"))
                                    return;
                                DashboardContentWidgetPanel.this.onViewMode(target, "comfortable");
                            } catch (Exception e) {
                                setResponsePage(new ApplicationErrorPage<>(e));
                                logger.error(e);
                            }

                        }

                        @Override
                        public String getLabel() {
                            return new StringResourceModel("comfortable-view", this, null).getObject();
                        }

                        @Override
                        public boolean isEnabled() {
                            return !getViewModeCriteria().equals("comfortable");
                        }

                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            attributes.setEventPropagation(EventPropagation.STOP);
                        }

                        @Override
                        public boolean isVisible() {
                            return true;
                        }

                        @Override
                        public boolean isIconVisible() {
                            return getViewModeCriteria().equals("comfortable");
                        }

                        @Override
                        public String getWorkingLabel() {
                            return new StringResourceModel("working", DashboardContentWidgetPanel.this, null).getString();
                        }
                    };
                }
            });

            if (isSort()) {

                menu.addItem(new MenuItemFactory<Panel>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public AbstractMenuItemPanelV5<Panel> getItem(String id) {
                        return new SeparatorMenuItemPanelV5<Panel>(id) {
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

                menu.addItem(new MenuItemFactory<Panel>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public AbstractMenuItemPanelV5<Panel> getItem(String id) {
                        return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onCheckClick(AjaxRequestTarget target) {
                                try {

                                    DashboardContentWidgetPanel.this.onSort(target, "title");
                                    // FeedbackHelper.showInfoToast(getLabel());
                                } catch (Exception e) {
                                    setResponsePage(new ApplicationErrorPage<>(e));
                                    logger.error(e);
                                }

                            }

                            @Override
                            public String getLabel() {
                                return new StringResourceModel("sort-title", this, null).getObject();
                            }

                            @Override
                            public boolean isEnabled() {
                                return !getSortCriteria().equals("title");
                            }

                            @Override
                            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                                attributes.setEventPropagation(EventPropagation.STOP);
                            }

                            @Override
                            public boolean isVisible() {
                                return true;
                            }

                            @Override
                            public boolean isIconVisible() {
                                return getSortCriteria().equals("title");
                            }

                            @Override
                            public String getWorkingLabel() {
                                return new StringResourceModel("working", DashboardContentWidgetPanel.this, null).getString();
                            }
                        };
                    }
                });

                menu.addItem(new MenuItemFactory<Panel>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public AbstractMenuItemPanelV5<Panel> getItem(String id) {
                        return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<Panel>(id) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onCheckClick(AjaxRequestTarget target) {
                                try {
                                    DashboardContentWidgetPanel.this.onSort(target, "date");
                                    // FeedbackHelper.showInfoToast(getLabel());
                                } catch (Exception e) {
                                    setResponsePage(new ApplicationErrorPage<>(e));
                                    logger.error(e);
                                }

                            }

                            @Override
                            public String getLabel() {
                                return new StringResourceModel("sort-date", this, null).getObject();
                            }

                            @Override
                            public boolean isEnabled() {
                                return !getSortCriteria().equals("date");
                            }

                            @Override
                            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                                attributes.setEventPropagation(EventPropagation.STOP);
                            }

                            @Override
                            public boolean isVisible() {
                                return true;
                            }

                            @Override
                            public boolean isIconVisible() {
                                return getSortCriteria().equals("date");
                            }

                            @Override
                            public String getWorkingLabel() {
                                return new StringResourceModel("working", DashboardContentWidgetPanel.this, null).getString();
                            }
                        };
                    }
                });

            }

            return menu;

        } catch (Exception e) {
            logger.error(e);
            return new InvisiblePanel("menu");
        }
    }

    abstract protected void onSort(AjaxRequestTarget target, String string);

    protected boolean isSort() {
        return true;
    }

    protected String getContentClass(Content content) {
        return Proxy.getClassName(content).toLowerCase();
    }

}
